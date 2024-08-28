/*********************************************************************
Ericsson Inc.
**********************************************************************

(c) Ericsson Inc. 2018 - All rights reserved.

The copyright to the computer program(s) herein is the property of
Ericsson Inc. The programs may be used and/or copied only with written
permission from Ericsson Inc. or in accordance with the terms and
conditions stipulated in the agreement/contract under which the
program(s) have been supplied.

***********************************************************************/
package com.ericsson.patternextractor.services;

import static com.ericsson.configmaster.constants.Constants.DEFAULT_SECONDS;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.MINUTES;
import static com.ericsson.configmaster.constants.Constants.START_DATE;
import static com.ericsson.configmaster.constants.Constants.START_TIME;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.configmaster.services.PatternConfigLoader;
import com.ericsson.patternextractor.entities.SessionEvent;
import com.ericsson.patternextractor.entities.SessionPerCall;
import com.ericsson.utilities.services.Utils;

/**
 * The Class SessionIdentifierThread.
 */
@Component
public class SessionIdentifierThread implements Runnable {

    /** The pattern config loader. */
    @Autowired
    private PatternConfigLoader patternConfigLoader;

    /** The pattern extractor. */
    @Autowired
    private EventIterator eventIterator;

    /** The fdn to file name list map. */
    private Map<String, TreeSet<String>> fdnToFileNameListMap;

    /** The fdn list. */
    private List<String> fdnList;

    /** The rop duration. */
    private int ropDuration;

    /** The offset. */
    private String offset;

    /**
     * Run.
     */
    @Override
    public void run(){
        List<String> fileDateTimeAttributeList = new ArrayList<>();
        fileDateTimeAttributeList.add(START_TIME);
        fileDateTimeAttributeList.add(START_DATE);
        for(String fdn : fdnList){
            TreeSet<String> fileNameList = fdnToFileNameListMap.get(fdn);
            Map<String, String> fileAttributeValues = Utils.getAttributeFromFileName(patternConfigLoader.getInputLocation(),fileNameList.first(),
                    patternConfigLoader.getDatasource(),fileDateTimeAttributeList);
            Date previousRopDate = Utils.getDateObj(fileAttributeValues.get(START_DATE),fileAttributeValues.get(START_TIME) + DEFAULT_SECONDS,offset);
            Calendar cal = Calendar.getInstance();
            cal.setTime(previousRopDate);
            Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap = new HashMap<>();
            Map<String, List<SessionEvent>> missingKeySession = new HashMap<String, List<SessionEvent>>();
            identifySessions(fileNameList,fileDateTimeAttributeList,previousRopDate,ropWiseSessionPerCallObjMap,missingKeySession,cal);
            Utils.logMessage(INFO,"Events that does not contain session attributes  : " + eventIterator.eventsContainingNullValues.toString()
                    + "  for fdn : " + fdn);
            addRemainingSessionsToQueue(ropWiseSessionPerCallObjMap);
            Utils.logMessage(INFO,"Number of sessions left with missing key : " + missingKeySession.size());
            eventIterator.remainingMissingKeyHandle(ropWiseSessionPerCallObjMap,missingKeySession);
        }
    }

    private void identifySessions(TreeSet<String> fileNameList, List<String> fileDateTimeAttributeList, Date previousRopDate,
            Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap, Map<String, List<SessionEvent>> missingKeySession, Calendar cal){
        for(String fileName : fileNameList){
            try{
                Map<String, String> currentFileAttributeValues = Utils.getAttributeFromFileName(patternConfigLoader.getInputLocation(),fileName,
                        patternConfigLoader.getDatasource(),fileDateTimeAttributeList);
                Date currentRopDate = Utils.getDateObj(currentFileAttributeValues.get(START_DATE),currentFileAttributeValues.get(START_TIME) + "00",
                        offset);
                if(currentRopDate.compareTo(previousRopDate) != 0){
                    ropWiseSessionPerCallObjMap.put(currentRopDate,new HashMap<String, SessionPerCall>());
                    if(Utils.calcDateDiff(currentRopDate,previousRopDate,MINUTES) >= patternConfigLoader.getSameSessionGapInterval()){
                        closePreviousSessions(ropWiseSessionPerCallObjMap,currentRopDate);
                        cal.add(Calendar.MINUTE,ropDuration);
                    }
                }
                eventIterator.getRopWiseSessionPerCallMap(fileName,ropWiseSessionPerCallObjMap,currentRopDate,missingKeySession);
            } catch(Exception e){
                Utils.logMessage(ERROR,"Error occurred for file : " + fileName + " " + e);
            }
        }
    }

    /**
     * Close previous sessions.
     *
     * @param ropWiseSessionPerCallObjMap
     *            the rop wise session per call obj map
     * @param currentRopDate
     *            the current rop date
     */
    private void closePreviousSessions(Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap, Date currentRopDate){
        Map<Date, Map<String, SessionPerCall>> updatedRopWiseSessionPerCallObjMap = new HashMap<>();
        for(Date sessionToCloseDate : ropWiseSessionPerCallObjMap.keySet()){
            List<String> sessionObjToBeRemovedList = new ArrayList<>();
            Map<String, SessionPerCall> sessionsToCloseMap = ropWiseSessionPerCallObjMap.get(sessionToCloseDate);
            for(Entry<String, SessionPerCall> entry : sessionsToCloseMap.entrySet()){
                SessionEvent lastEventOfSession = entry.getValue().getEventObjList().get(entry.getValue().getEventObjList().size() - 1);
                if(Utils.calcDateDiff(currentRopDate,lastEventOfSession.getDateTime(),MINUTES) >= patternConfigLoader.getSameSessionGapInterval()){
                    eventIterator.getSharedQueue().add(entry.getValue());
                    sessionObjToBeRemovedList.add(entry.getKey());
                }
            }
            // Remove closed sessions from the map
            for(String sessionID : sessionObjToBeRemovedList){
                sessionsToCloseMap.remove(sessionID);
            }
            if(!sessionsToCloseMap.isEmpty()){
                updatedRopWiseSessionPerCallObjMap.put(sessionToCloseDate,new HashMap<>(sessionsToCloseMap));
            }
        }
        for(Date remainingDate : updatedRopWiseSessionPerCallObjMap.keySet()){
            ropWiseSessionPerCallObjMap.put(remainingDate,updatedRopWiseSessionPerCallObjMap.get(remainingDate));
        }
    }

    /**
     * Adds the remaining sessions to queue.
     *
     * @param ropWiseSessionPerCallObjMap
     *            the rop wise session per call obj map
     */
    private void addRemainingSessionsToQueue(Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap){
        for(Date ropStartDate : ropWiseSessionPerCallObjMap.keySet()){
            eventIterator.getSharedQueue().addAll(ropWiseSessionPerCallObjMap.get(ropStartDate).values());
        }
    }

    /**
     * Sets the fdn to file name list map.
     *
     * @param fdnToFileNameListMap
     *            the fdn to file name list map
     */
    public void setFdnToFileNameListMap(Map<String, TreeSet<String>> fdnToFileNameListMap){
        this.fdnToFileNameListMap = fdnToFileNameListMap;
    }

    /**
     * Sets the fdn list.
     *
     * @param fdnList
     *            the new fdn list
     */
    public void setFdnList(List<String> fdnList){
        this.fdnList = fdnList;
    }

    /**
     * Gets the rop duration.
     *
     * @return the rop duration
     */
    public int getRopDuration(){
        return ropDuration;
    }

    /**
     * Sets the rop duration.
     *
     * @param ropDuration
     *            the new rop duration
     */
    public void setRopDuration(int ropDuration){
        this.ropDuration = ropDuration;
    }

    /**
     * Gets the offset.
     *
     * @return the offset
     */
    public String getOffset(){
        return offset;
    }

    /**
     * Sets the offset.
     *
     * @param offset
     *            the new offset
     */
    public void setOffset(String offset){
        this.offset = offset;
    }

}
