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

import static com.ericsson.configmaster.constants.Constants.DEFAULT_OFFSET;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_ENBS1APID;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_GLOBAL_CELL_ID;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_RAC_UE_REF;
import static com.ericsson.configmaster.constants.Constants.HANDOVER_RELATION_TYPE;
import static com.ericsson.configmaster.constants.Constants.MINUTES;
import static com.ericsson.configmaster.constants.Constants.UNDEFINED;
import static com.ericsson.configmaster.constants.Constants.UNDERSCORE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.PatternConfigLoader;
import com.ericsson.patternextractor.entities.SessionEvent;
import com.ericsson.patternextractor.entities.SessionPerCall;
import com.ericsson.schemamaster.entities.Event;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.BytePackDecoder;
import com.ericsson.schemamaster.services.SchemaLoader;
import com.ericsson.schemamaster.services.SessionAttributeDecoder;
import com.ericsson.utilities.services.Utils;

/**
 * The Class EventIterator.
 */
@Service
public class EventIterator {

    /** The termination flag. */
    private boolean terminationFlag;

    /** The session attribute decoder. */
    @Autowired
    private SessionAttributeDecoder sessionAttributeDecoder;

    /** The byte pack decoder. */
    @Autowired
    private BytePackDecoder bytePackDecoder;

    /** The schema loader. */
    @Autowired
    private SchemaLoader schemaLoader;

    /** The pattern config loader. */
    @Autowired
    private PatternConfigLoader patternConfigLoader;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The session per call. */
    @SuppressWarnings("unused")
    @Autowired
    private SessionPerCall sessionPerCall;

    /** The session event. */
    @SuppressWarnings("unused")
    @Autowired
    private SessionEvent sessionEvent;

    /** The events containing null values. */
    public Set<Integer> eventsContainingNullValues = new HashSet<Integer>();

    /** The shared queue. */
    private BlockingQueue<SessionPerCall> sharedQueue = new LinkedBlockingQueue<SessionPerCall>();

    /** The schema release obj. */
    private SchemaRelease schemaReleaseObj;

    /**
     * Gets the rop wise session per call map.
     *
     * @param fileName
     *            the file name
     * @param ropWiseSessionPerCallObjMap
     *            the rop wise session per call obj map
     * @param currentRopDate
     *            the current rop date
     * @param missingKeySession
     *            the missing key session
     * @return the rop wise session per call map
     * @throws Exception
     *             the exception
     */
    public void getRopWiseSessionPerCallMap(String fileName, Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap, Date currentRopDate,
            Map<String, List<SessionEvent>> missingKeySession) throws Exception{
        InputStream inputStream = null;
        File binaryFile = null;
        try{
            binaryFile = new File(patternConfigLoader.getInputLocation() + File.separator + fileName);
            inputStream = Utils.readBinaryFile(binaryFile);
        } catch(IOException ex){
            Utils.logMessage(ERROR,"Exception occurred while reading binary file ");
            throw ex;
        }

        int inputStreamDataLength = 0;
        int actualBufferSize = Utils.getBufferSizeForBinaryFiles(binaryFile);
        byte[] eventFileByteBuffer = new byte[actualBufferSize];
        try{
            while ((inputStreamDataLength = inputStream.read(eventFileByteBuffer)) != -1){
                int recordStartIndex = 0;
                for(int cursor = 0; cursor < inputStreamDataLength; cursor++){
                    int recordLength = bytePackDecoder.getIntegerValueForTwoBytesRecord(recordStartIndex,recordStartIndex + 1,eventFileByteBuffer);
                    int recordType = bytePackDecoder.getIntegerValueForTwoBytesRecord(recordStartIndex + 2,recordStartIndex + 3,eventFileByteBuffer);
                    if(recordStartIndex == 0){
                        if(null == schemaReleaseObj){
                            String release = bytePackDecoder.getRelease(eventFileByteBuffer,recordLength);
                            schemaReleaseObj = schemaLoader.loadSchema(release);
                            if(null == schemaReleaseObj){
                                throw new Exception("Could not load schema for release : " + release);
                            }
                        }
                    } else if(recordType == 4){
                        byte[] eventByteArray = new byte[recordLength];
                        System.arraycopy(eventFileByteBuffer,recordStartIndex,eventByteArray,0,recordLength);
                        decodeEventData(eventByteArray,ropWiseSessionPerCallObjMap,currentRopDate,missingKeySession);
                    }
                    cursor = cursor + recordLength - 1;
                    recordStartIndex = recordStartIndex + recordLength;
                }
            }
            inputStream.close();
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occurred in method " + e);
            throw e;
        }
    }

    /**
     * Decode event data.
     *
     * @param eventByteArray
     *            the event byte array
     * @param ropWiseSessionPerCallObjMap
     *            the rop wise session per call obj map
     * @param currentRopDate
     *            the current rop date
     * @param missingKeySession
     *            the missing key session
     */
    private void decodeEventData(byte[] eventByteArray, Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap, Date currentRopDate,
            Map<String, List<SessionEvent>> missingKeySession){
        int eventID = bytePackDecoder.getEventId(eventByteArray);
        Event schemaEventObj = schemaReleaseObj.getEventIDMap().get(eventID);
        // Ignore events check
        if(!patternConfigLoader.getIgnoreEventsSet().contains(schemaEventObj.getEventName())){
            // Parameter missing for a event check
            Set<String> parameterNameSet = schemaEventObj.getParameterMap().keySet();
            if(parameterNameSet.containsAll(patternConfigLoader.getSessionParameterToIgnoreCheckSet())){
                // Decode session attributes
                Map<String, String> decodedSessionParamValuesMap = sessionAttributeDecoder.findSession(schemaEventObj.getParameterMap(),
                        eventByteArray);
                SessionEvent sessionEventObj = createSessionEventObj(decodedSessionParamValuesMap,eventID,schemaEventObj.getEventName(),
                        eventByteArray,currentRopDate);
                if(sessionEventObj.isMissingKey()){
                    if(missingKeySession.containsKey(sessionEventObj.getSessionID())){
                        missingKeySession.get(sessionEventObj.getSessionID()).add(sessionEventObj);
                    } else{
                        List<SessionEvent> session = new ArrayList<>();
                        session.add(sessionEventObj);
                        missingKeySession.put(sessionEventObj.getSessionID(),session);
                    }
                } else{
                    addSessionEventInSessionPerCall(ropWiseSessionPerCallObjMap,currentRopDate,sessionEventObj,missingKeySession);
                }
            } else{
                eventsContainingNullValues.add(eventID);
            }
        }
    }

    /**
     * Creates the session event obj.
     *
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @param eventID
     *            the event ID
     * @param eventName
     *            the event name
     * @param eventByteArray
     *            the event byte array
     * @param currentRopDate
     *            the current rop date
     * @return the session event
     */
    private SessionEvent createSessionEventObj(Map<String, String> decodedSessionParamValuesMap, int eventID, String eventName, byte[] eventByteArray,
            Date currentRopDate){
        StringBuilder sessionKeyBuilder = getSessionKeyBuilder(decodedSessionParamValuesMap);
        if(sessionKeyBuilder.length() == 0){
            return null;
        }
        boolean missingKey = isMissingKey(decodedSessionParamValuesMap);
        // Create Session event object
        SessionEvent sessionEventObj = applicationContext.getBean(SessionEvent.class);
        sessionEventObj.setMissingKey(missingKey);
        sessionEventObj.setEventID(eventID);
        sessionEventObj.setEventName(eventName);
        sessionEventObj.setEventData(eventByteArray);
        sessionEventObj.setSessionID(sessionKeyBuilder.toString());
        sessionEventObj.setCellID(decodedSessionParamValuesMap.get(EVENT_PARAM_GLOBAL_CELL_ID));
        sessionEventObj.setDateTime(calculateEventDateTime(decodedSessionParamValuesMap,currentRopDate));

        if(null != decodedSessionParamValuesMap.get(HANDOVER_RELATION_TYPE)){
            sessionEventObj.setTypeOfRelation(Integer.parseInt(decodedSessionParamValuesMap.get(HANDOVER_RELATION_TYPE)));
            decodedSessionParamValuesMap.remove(HANDOVER_RELATION_TYPE);
        }
        decodedSessionParamValuesMap.keySet().removeAll(patternConfigLoader.getSessionAttributesSet());
        decodedSessionParamValuesMap.remove("EVENT_PARAM_TIMESTAMP_HOUR");
        decodedSessionParamValuesMap.remove("EVENT_PARAM_TIMESTAMP_MINUTE");
        decodedSessionParamValuesMap.remove("EVENT_PARAM_TIMESTAMP_SECOND");
        if(!decodedSessionParamValuesMap.isEmpty()){
            sessionEventObj.setPatternSelectionParameterMap(new HashMap<String, String>(decodedSessionParamValuesMap));
        }
        return sessionEventObj;
    }

    /**
     * Gets the session key builder.
     *
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @return the session key builder
     */
    private StringBuilder getSessionKeyBuilder(Map<String, String> decodedSessionParamValuesMap){
        StringBuilder sessionKeyBuilder = new StringBuilder();
        for(String sessionAttribute : patternConfigLoader.getSessionAttributesSet()){
            sessionKeyBuilder.append(decodedSessionParamValuesMap.get(sessionAttribute) + "_");
        }
        sessionKeyBuilder = sessionKeyBuilder.deleteCharAt(sessionKeyBuilder.length() - 1);
        return sessionKeyBuilder;
    }

    /**
     * Checks if is missing key.
     *
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @return true, if is missing key
     */
    private boolean isMissingKey(Map<String, String> decodedSessionParamValuesMap){
        boolean isMissingKey = false;
        for(String sessionAttribute : patternConfigLoader.getSessionAttributesSet()){
            if(sessionAttribute.equals(EVENT_PARAM_RAC_UE_REF) || sessionAttribute.equals(EVENT_PARAM_ENBS1APID)){
                if(decodedSessionParamValuesMap.get(sessionAttribute).equalsIgnoreCase(UNDEFINED)){
                    isMissingKey = true;
                    break;
                }
            }
        }
        return isMissingKey;
    }

    /**
     * Calculate event date time.
     *
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @param currentRopDate
     *            the current rop date
     * @return the date
     */
    private Date calculateEventDateTime(Map<String, String> decodedSessionParamValuesMap, Date currentRopDate){
        Date eventDateTime = new Date(currentRopDate.getTime());
        Calendar cal = Calendar.getInstance();
        cal.setTime(eventDateTime);
        int monthIndex = cal.get(Calendar.MONTH) + 1;
        String year = cal.get(Calendar.YEAR) < 10 ? "0" + cal.get(Calendar.YEAR) : String.valueOf(cal.get(Calendar.YEAR));
        String month = monthIndex < 10 ? "0" + monthIndex : String.valueOf(monthIndex);
        String day = cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + cal.get(Calendar.DAY_OF_MONTH) : String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        eventDateTime = Utils.getDateObj(year + month + day,decodedSessionParamValuesMap.get("EVENT_PARAM_TIMESTAMP_HOUR")
                + decodedSessionParamValuesMap.get("EVENT_PARAM_TIMESTAMP_MINUTE") + decodedSessionParamValuesMap.get("EVENT_PARAM_TIMESTAMP_SECOND"),
                DEFAULT_OFFSET);
        return eventDateTime;
    }

    /**
     * Adds the session event in session per call.
     *
     * @param ropWiseSessionPerCallObjMap
     *            the rop wise session per call obj map
     * @param currentRopDate
     *            the current rop date
     * @param sessionEventObj
     *            the session event obj
     * @param missingKeySession
     *            the missing key session
     */
    private void addSessionEventInSessionPerCall(Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap, Date currentRopDate,
            SessionEvent sessionEventObj, Map<String, List<SessionEvent>> missingKeySession){
        if(null != sessionEventObj){
            boolean eventAdded = false;
            Date prevRopStartTimeToDelete = null;
            for(Entry<Date, Map<String, SessionPerCall>> entry : ropWiseSessionPerCallObjMap.entrySet()){
                SessionPerCall sessionPerCallObj = entry.getValue().get(sessionEventObj.getSessionID());
                if(null != sessionPerCallObj){
                    if(Utils.calcDateDiff(sessionEventObj.getDateTime(),sessionPerCallObj.getEventObjList().get(sessionPerCallObj.getEventObjList()
                            .size() - 1).getDateTime(),MINUTES) >= patternConfigLoader.getSameSessionGapInterval()){
                        sessionPerCallObj.setSessionID(sessionEventObj.getSessionID() + UNDERSCORE + sessionEventObj.getDateTime().getTime());
                        for(SessionEvent sessionEvent : sessionPerCallObj.getEventObjList()){
                            sessionEvent.setSessionID(sessionPerCallObj.getSessionID());
                        }
                        prevRopStartTimeToDelete = entry.getKey();
                        getSharedQueue().add(sessionPerCallObj);
                    } else{
                        sessionPerCallObj.getEventObjList().add(sessionEventObj);
                        if(!missingKeySession.isEmpty()){
                            processMissingkey(sessionPerCallObj.getSessionID(),ropWiseSessionPerCallObjMap,missingKeySession,entry.getKey());
                        }
                        eventAdded = true;
                    }
                    break;
                }
            }
            if(null != prevRopStartTimeToDelete){
                ropWiseSessionPerCallObjMap.get(prevRopStartTimeToDelete).remove(sessionEventObj.getSessionID());
            }
            if(!eventAdded){
                SessionPerCall sessionPerCallDupObj = applicationContext.getBean(SessionPerCall.class);
                sessionPerCallDupObj.getEventObjList().add(sessionEventObj);
                sessionPerCallDupObj.setSessionID(sessionEventObj.getSessionID());
                Map<String, SessionPerCall> sessionPerCallMap = ropWiseSessionPerCallObjMap.get(currentRopDate);
                if(null == sessionPerCallMap){
                    sessionPerCallMap = new HashMap<>();
                }
                sessionPerCallMap.put(sessionEventObj.getSessionID(),sessionPerCallDupObj);
                ropWiseSessionPerCallObjMap.put(currentRopDate,sessionPerCallMap);
                if(!missingKeySession.isEmpty()){
                    processMissingkey(sessionPerCallDupObj.getSessionID(),ropWiseSessionPerCallObjMap,missingKeySession,currentRopDate);
                }
            }
        }
    }

    /**
     * Process missingkey.
     *
     * @param sessionID
     *            the session ID
     * @param ropWiseSessionPerCallObjMap
     *            the cell id to session per call obj map
     * @param missingKeySession
     *            the missing key session
     * @param currentRopDate
     *            the current rop date
     */
    private void processMissingkey(String sessionID, Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap,
            Map<String, List<SessionEvent>> missingKeySession, Date currentRopDate){
        String[] sessionKey = sessionID.split(UNDERSCORE);
        String[] cloneSession = sessionKey.clone();
        sessionKey[patternConfigLoader.getRAC_UE_REF()] = UNDEFINED;
        cloneSession[patternConfigLoader.getENBS1APID()] = UNDEFINED;
        String rac_ue_ref_key = StringUtils.join(sessionKey,UNDERSCORE);
        String enbs1apid_key = StringUtils.join(cloneSession,UNDERSCORE);
        if(missingKeySession.containsKey(rac_ue_ref_key)){
            List<SessionEvent> sessionEvent = missingKeySession.get(rac_ue_ref_key);
            for(SessionEvent eventSession : sessionEvent){
                ropWiseSessionPerCallObjMap.get(currentRopDate).get(sessionID).getEventObjList().add(eventSession);
                missingKeySession.remove(rac_ue_ref_key);
            }
        } else if(missingKeySession.containsKey(enbs1apid_key)){
            List<SessionEvent> sessionEvent = missingKeySession.get(enbs1apid_key);
            for(SessionEvent eventSession : sessionEvent){
                ropWiseSessionPerCallObjMap.get(currentRopDate).get(sessionID).getEventObjList().add(eventSession);
                missingKeySession.remove(enbs1apid_key);
            }
        }
    }

    /**
     * Remaining missing key handle.
     *
     * @param ropWiseSessionPerCallObjMap
     *            the cell id to session per call obj map
     * @param missingKeySession
     *            the missing key session
     */
    public void remainingMissingKeyHandle(Map<Date, Map<String, SessionPerCall>> ropWiseSessionPerCallObjMap,
            Map<String, List<SessionEvent>> missingKeySession){
        if(!missingKeySession.isEmpty()){
            for(String sessionKey : missingKeySession.keySet()){
                SessionPerCall sessionPerCallObj = applicationContext.getBean(SessionPerCall.class);
                sessionPerCallObj.setSessionID(sessionKey);
                List<SessionEvent> sessionEvent = missingKeySession.get(sessionKey);
                for(SessionEvent session : sessionEvent){
                    sessionPerCallObj.getEventObjList().add(session);
                }
                getSharedQueue().add(sessionPerCallObj);
            }
        }
    }

    /**
     * Checks if is termination flag.
     *
     * @return true, if is termination flag
     */
    public boolean isTerminationFlag(){
        return terminationFlag;
    }

    /**
     * Sets the termination flag.
     *
     * @param terminationFlag
     *            the new termination flag
     */
    public void setTerminationFlag(boolean terminationFlag){
        this.terminationFlag = terminationFlag;
    }

    /**
     * Gets the shared queue.
     *
     * @return the shared queue
     */
    public BlockingQueue<SessionPerCall> getSharedQueue(){
        return sharedQueue;
    }

    /**
     * Sets the shared queue.
     *
     * @param sharedQueue
     *            the new shared queue
     */
    public void setSharedQueue(BlockingQueue<SessionPerCall> sharedQueue){
        this.sharedQueue = sharedQueue;
    }

    /**
     * Gets the schema release obj.
     *
     * @return the schema release obj
     */
    public SchemaRelease getSchemaReleaseObj(){
        return schemaReleaseObj;
    }

}
