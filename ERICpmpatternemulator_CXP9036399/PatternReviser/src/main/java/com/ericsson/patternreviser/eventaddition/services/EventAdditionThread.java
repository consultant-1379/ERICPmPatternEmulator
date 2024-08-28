package com.ericsson.patternreviser.eventaddition.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.configmaster.constants.Constants;
import com.ericsson.configmaster.entities.EventToAddWrapper;
import com.ericsson.configmaster.services.ParamConfigLoader;
import com.ericsson.schemamaster.entities.Parameter;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.BytePackDecoder;
import com.ericsson.utilities.entities.SessionPerCallWrapper;
import com.ericsson.utilities.services.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class EventAdditionThread.
 */
@Component
public class EventAdditionThread implements Runnable {

    /** The pattern file list. */
    private List<File> patternFileList;

    /** The schema release. */
    private SchemaRelease schemaRelease;

    /** The param config loader. */
    @Autowired
    private ParamConfigLoader paramConfigLoader;

    /** The byte pack decoder. */
    @Autowired
    private BytePackDecoder bytePackDecoder;

    /** The pattern info. */
    private Map<Integer, List<Integer>> patternInfo;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run(){
        for(File patternFolder : patternFileList){
            try{
                List<Integer> eventsList = new ArrayList<>();
                File[] sessionFiles = patternFolder.listFiles();
                boolean flag = true;
                for(File sessionFile : sessionFiles){
                    List<byte[]> eventDataCopyList = new ArrayList<>();
                    List<Integer> eventIDCopyList = new ArrayList<>();
                    SessionPerCallWrapper sessionPerCallObj = Utils.deserializeSessionFile(sessionFile);
                    for(int i = 0; i < sessionPerCallObj.getEventIDList().size(); i++){
                        String refEventName = schemaRelease.getEventIDMap().get(sessionPerCallObj.getEventIDList().get(i)).getEventName();
                        Map<String, List<EventToAddWrapper>> positionBasedEventToAddMap = paramConfigLoader.getEventToAddDetails().get(refEventName);
                        if(null != positionBasedEventToAddMap){
                            updateEventCopyListBasedOnPosition(positionBasedEventToAddMap,eventDataCopyList,sessionPerCallObj.getEventDataList().get(
                                    i),eventIDCopyList,refEventName);
                        } else{
                            eventDataCopyList.add(sessionPerCallObj.getEventDataList().get(i));
                            eventIDCopyList.add(sessionPerCallObj.getEventIDList().get(i));
                        }
                    }
                    sessionPerCallObj.setEventIDList(eventIDCopyList);
                    sessionPerCallObj.setEventDataList(eventDataCopyList);
                    writeSessions(sessionPerCallObj,patternFolder.getName(),sessionFile);
                    if(flag){
                        flag = false;
                        eventsList.addAll(eventIDCopyList);
                    }
                }
                patternInfo.put(Integer.parseInt(patternFolder.getName()),eventsList);
            } catch(Exception e){
                Utils.logMessage(Constants.ERROR,"Error while adding an event " + e);
            }
        }
    }

    /**
     * Write sessions.
     *
     * @param sessionPerCallObj
     *            the session per call obj
     * @param patternFolder
     *            the pattern folder
     * @param sessionFile
     *            the session file
     */
    private void writeSessions(SessionPerCallWrapper sessionPerCallObj, String patternFolder, File sessionFile){
        String outputLocation = paramConfigLoader.getOutputLocation() + File.separator + patternFolder;
        File file = new File(outputLocation);
        if(!file.exists()){
            file.mkdirs();
        }
        try{
            FileOutputStream fileOut = new FileOutputStream(outputLocation + File.separator + sessionFile.getName());
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(sessionPerCallObj);
            out.close();
            fileOut.close();
        } catch(Exception ex){
            Utils.logMessage(Constants.ERROR,"Error while creating serializing file " + ex);
        }
    }

    /**
     * Update event copy list based on position.
     *
     * @param positionBasedEventToAddMap
     *            the position based event to add map
     * @param eventDataCopyList
     *            the event data copy list
     * @param referenceEventData
     *            the reference event data
     * @param eventIDCopyList
     *            the event ID copy list
     * @param refEventName
     *            the ref event name
     */
    private void updateEventCopyListBasedOnPosition(Map<String, List<EventToAddWrapper>> positionBasedEventToAddMap, List<byte[]> eventDataCopyList,
            byte[] referenceEventData, List<Integer> eventIDCopyList, String refEventName){
        Map<String, byte[]> refEventParamValues = decodeReferenceEventParam(referenceEventData,refEventName);

        // update ref Event Param in event to add
        List<EventToAddWrapper> beforeEventsList = positionBasedEventToAddMap.get("BEFORE");
        if(null != beforeEventsList){
            for(EventToAddWrapper beforeEvent : beforeEventsList){
                eventDataCopyList.add(updateNewEventWithRefEventParamValues(beforeEvent,refEventParamValues));
                eventIDCopyList.add(schemaRelease.getEventNameMap().get(beforeEvent.getEventName()).getEventID());
            }
        }
        eventDataCopyList.add(referenceEventData);
        eventIDCopyList.add(schemaRelease.getEventNameMap().get(refEventName).getEventID());
        List<EventToAddWrapper> afterEventsList = positionBasedEventToAddMap.get("AFTER");
        if(null != afterEventsList){
            for(EventToAddWrapper afterEvent : beforeEventsList){
                eventDataCopyList.add(updateNewEventWithRefEventParamValues(afterEvent,refEventParamValues));
                eventIDCopyList.add(schemaRelease.getEventNameMap().get(afterEvent.getEventName()).getEventID());
            }
        }
    }

    /**
     * Decode reference event param.
     *
     * @param referenceEventData
     *            the reference event data
     * @param refEventName
     *            the ref event name
     * @return the map
     */
    private Map<String, byte[]> decodeReferenceEventParam(byte[] referenceEventData, String refEventName){
        int startIndex = 7;
        Map<String, byte[]> refEventParamValues = new HashMap<>(7);
        Map<String, Parameter> refEventParameterMap = schemaRelease.getEventNameMap().get(refEventName).getParameterMap();
        for(Entry<String, Parameter> entrySet : refEventParameterMap.entrySet()){
            Parameter paramElement = entrySet.getValue();
            int paramElementCalculatedLength = bytePackDecoder.calculateTotalParamLength(paramElement,refEventParameterMap,startIndex,
                    referenceEventData);
            if(Constants.EVENT_PARAM_GLOBAL_CELL_ID.equals(paramElement.getParameterName()) || Constants.EVENT_PARAM_RAC_UE_REF.equals(paramElement
                    .getParameterName()) || Constants.EVENT_PARAM_ENBS1APID.equals(paramElement.getParameterName()) || paramElement.getParameterName()
                            .equals("EVENT_PARAM_TIMESTAMP_HOUR") || paramElement.getParameterName().equals("EVENT_PARAM_TIMESTAMP_MINUTE")
                    || paramElement.getParameterName().equals("EVENT_PARAM_TIMESTAMP_SECOND") || "EVENT_PARAM_TIMESTAMP_MILLISEC".equals(paramElement
                            .getParameterName())){
                refEventParamValues.put(paramElement.getParameterName(),Arrays.copyOfRange(referenceEventData,startIndex,startIndex
                        + paramElementCalculatedLength));
            }
            startIndex = startIndex + paramElementCalculatedLength;
        }
        return refEventParamValues;
    }

    /**
     * Update new event with ref event param values.
     *
     * @param newEvent
     *            the new event
     * @param refEventParamValues
     *            the ref event param values
     * @return the byte[]
     */
    private byte[] updateNewEventWithRefEventParamValues(EventToAddWrapper newEvent, Map<String, byte[]> refEventParamValues){
        int startIndex = 7;
        byte[] newEventData = Arrays.copyOf(newEvent.getEventData(),newEvent.getEventData().length);
        Map<String, Parameter> newEventParameterMap = schemaRelease.getEventNameMap().get(newEvent.getEventName()).getParameterMap();
        for(Entry<String, Parameter> entrySet : newEventParameterMap.entrySet()){
            Parameter paramElement = entrySet.getValue();
            int paramElementCalculatedLength = bytePackDecoder.calculateTotalParamLength(paramElement,newEventParameterMap,startIndex,newEventData);
            if(Constants.EVENT_PARAM_GLOBAL_CELL_ID.equals(paramElement.getParameterName()) || Constants.EVENT_PARAM_RAC_UE_REF.equals(paramElement
                    .getParameterName()) || Constants.EVENT_PARAM_ENBS1APID.equals(paramElement.getParameterName()) || paramElement.getParameterName()
                            .equals("EVENT_PARAM_TIMESTAMP_HOUR") || paramElement.getParameterName().equals("EVENT_PARAM_TIMESTAMP_MINUTE")
                    || paramElement.getParameterName().equals("EVENT_PARAM_TIMESTAMP_SECOND") || "EVENT_PARAM_TIMESTAMP_MILLISEC".equals(paramElement
                            .getParameterName())){
                System.arraycopy(refEventParamValues.get(paramElement.getParameterName()),0,newEventData,startIndex,paramElementCalculatedLength);
            }
            startIndex = startIndex + paramElementCalculatedLength;
        }
        return newEventData;
    }

    /**
     * Gets the pattern file list.
     *
     * @return the pattern file list
     */
    public List<File> getPatternFileList(){
        return patternFileList;
    }

    /**
     * Sets the pattern file list.
     *
     * @param patternFileList
     *            the new pattern file list
     */
    public void setPatternFileList(List<File> patternFileList){
        this.patternFileList = patternFileList;
    }

    /**
     * Gets the schema release.
     *
     * @return the schema release
     */
    public SchemaRelease getSchemaRelease(){
        return schemaRelease;
    }

    /**
     * Sets the schema release.
     *
     * @param schemaRelease
     *            the new schema release
     */
    public void setSchemaRelease(SchemaRelease schemaRelease){
        this.schemaRelease = schemaRelease;
    }

    /**
     * Gets the pattern info.
     *
     * @return the pattern info
     */
    public Map<Integer, List<Integer>> getPatternInfo(){
        return patternInfo;
    }

    /**
     * Sets the pattern info.
     *
     * @param patternInfo
     *            the pattern info
     */
    public void setPatternInfo(Map<Integer, List<Integer>> patternInfo){
        this.patternInfo = patternInfo;
    }

}
