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
package com.ericsson.patternreviser.analyzer.services;

import static com.ericsson.configmaster.constants.Constants.COLON;
import static com.ericsson.configmaster.constants.Constants.CTR;
import static com.ericsson.configmaster.constants.Constants.DOT;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.LIST_TYPE;
import static com.ericsson.configmaster.constants.Constants.LONG;
import static com.ericsson.configmaster.constants.Constants.MIN_MAX_TYPE;
import static com.ericsson.configmaster.constants.Constants.UINT;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.configmaster.paramconfig.autogen.ParamConfig.Analysis.EventToAnalyze.ParamToAnalyze;
import com.ericsson.configmaster.services.ParamConfigLoader;
import com.ericsson.patternreviser.controller.PatternReviserController;
import com.ericsson.schemamaster.entities.Event;
import com.ericsson.schemamaster.entities.Parameter;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.BytePackDecoder;
import com.ericsson.utilities.entities.SessionPerCallWrapper;
import com.ericsson.utilities.services.Utils;

/**
 * The Class AnalyzerThread.
 */
@Component
public class AnalyzerThread implements Runnable {

    /** The param config loader. */
    @Autowired
    private ParamConfigLoader paramConfigLoader;

    /** The byte pack decoder. */
    @Autowired
    private BytePackDecoder bytePackDecoder;

    /** The pattern file list. */
    private List<File> patternFileList;

    /** The schema release. */
    private SchemaRelease schemaRelease;

    /** The analysis results. */
    private Map<String, Map<String, Integer>> analysisResults;

    /**
     * Run.
     */
    @Override
    public void run(){
        for(File patternFolder : patternFileList){
            analysisResults = new HashMap<String, Map<String, Integer>>();
            try{
                File[] sessionFiles = patternFolder.listFiles();
                if(sessionFiles.length > 1){
                    for(int i = 0; i < sessionFiles.length; i++){
                        SessionPerCallWrapper sessionPerCallObj = Utils.deserializeSessionFile(sessionFiles[i]);
                        decodeEvents(sessionPerCallObj);
                    }
                }
                PatternReviserController.analysisAllThreadsResults.put(patternFolder.getName(),analysisResults);
            } catch(Exception e){
                Utils.logMessage(ERROR,"Error occurred while analyzing pattern ID " + patternFolder.getName() + " " + e);
            }
        }
    }

    /**
     * Decode events.
     *
     * @param sessionPerCallObj
     *            the session per call obj
     */
    private void decodeEvents(SessionPerCallWrapper sessionPerCallObj){
        for(String paramConfigEventName : paramConfigLoader.getAnalysisEventList()){
            Event schemaEvent = schemaRelease.getEventNameMap().get(paramConfigEventName);
            for(int i = 0; i < sessionPerCallObj.getEventIDList().size(); i++){
                int sessionEventID = sessionPerCallObj.getEventIDList().get(i);
                if(sessionEventID == schemaEvent.getEventID()){
                    int sameSessionOccurences = Collections.frequency(sessionPerCallObj.getEventIDList(),sessionEventID);
                    decodeAttributesValue(sessionPerCallObj.getEventDataList().get(i),schemaEvent,sameSessionOccurences);
                }
            }
        }
    }

    /**
     * Decode attributes value.
     *
     * @param eventData
     *            the event data
     * @param schemaEvent
     *            the schema event
     * @param sameSessionOccurences
     *            the same session occurrences
     */
    private void decodeAttributesValue(byte[] eventData, Event schemaEvent, int sameSessionOccurences){
        int startIndex = 7;
        Map<String, String> decodedParamValuesMap = new HashMap<String, String>();
        Map<String, com.ericsson.schemamaster.entities.Parameter> schemaParameterMap = schemaEvent.getParameterMap();

        for(Entry<String, com.ericsson.schemamaster.entities.Parameter> entrySet : schemaParameterMap.entrySet()){
            com.ericsson.schemamaster.entities.Parameter schemaParam = entrySet.getValue();
            int paramElementCalculatedLength = bytePackDecoder.calculateTotalParamLength(schemaParam,schemaParameterMap,startIndex,eventData);
            if(null != paramConfigLoader.getComplexOrArrayAnalysisEvents().get(schemaEvent.getEventName()) && paramConfigLoader
                    .getComplexOrArrayAnalysisEvents().get(schemaEvent.getEventName()).containsKey(entrySet.getKey())){
                // This can be complex parameter or array parameter
                Map<Integer, ParamToAnalyze> componentIDParameterMap = paramConfigLoader.getComplexOrArrayAnalysisEvents().get(schemaEvent
                        .getEventName()).get(entrySet.getKey());
                bytePackDecoder.decodeComplexAndArrayParameter(entrySet.getValue(),startIndex,eventData,paramElementCalculatedLength,
                        componentIDParameterMap.keySet(),decodedParamValuesMap,CTR);
                resolveComplexArrayParamResults(decodedParamValuesMap,componentIDParameterMap,schemaParameterMap.get(entrySet.getKey()),schemaEvent
                        .getEventName(),sameSessionOccurences);
            } else{
                if(null != paramConfigLoader.getAnalysisEventsDetailsMap().get(schemaEvent.getEventName())){
                    ParamToAnalyze configParam = paramConfigLoader.getAnalysisEventsDetailsMap().get(schemaEvent.getEventName()).get(entrySet
                            .getKey());
                    if(null != configParam){
                        String paramValue = bytePackDecoder.getDecodedValueOnValidityBits(schemaParam,startIndex,eventData,
                                paramElementCalculatedLength);
                        compareTypesAndValues(schemaEvent.getEventName(),configParam,schemaParameterMap.get(entrySet.getKey()),paramValue,
                                sameSessionOccurences);
                    }
                }
            }
            if(entrySet.getValue().getArraySize() > 1){
                startIndex = startIndex + entrySet.getValue().getArraySize() * paramElementCalculatedLength;
            } else{
                startIndex += paramElementCalculatedLength;
            }
        }
    }

    /**
     * Compare types and values of complex array param.
     *
     * @param decodedParamValuesMap
     *            the decoded param values map
     * @param componentIDParameterMap
     *            the component ID parameter map
     * @param schemaParam
     *            the schema param
     * @param eventName
     *            the event name
     * @param sameSessionOccurences
     *            the same session occurences
     */
    private void resolveComplexArrayParamResults(Map<String, String> decodedParamValuesMap, Map<Integer, ParamToAnalyze> componentIDParameterMap,
            Parameter schemaParam, String eventName, int sameSessionOccurences){
        for(String paramComponentName : decodedParamValuesMap.keySet()){
            String[] complexArrayConfigParamArray = paramComponentName.split(COLON);
            ParamToAnalyze complexArrayConfigParam = componentIDParameterMap.get(Integer.parseInt(complexArrayConfigParamArray[1]));
            compareTypesAndValues(eventName,complexArrayConfigParam,schemaParam,decodedParamValuesMap.get(paramComponentName),sameSessionOccurences);
        }
    }

    /**
     * Compare types and values.
     *
     * @param eventName
     *            the event name
     * @param configParam
     *            the config param
     * @param schemaParam
     *            the schema param
     * @param paramValue
     *            the param value
     * @param sameSessionOccurences
     *            the same session occurences
     */
    private void compareTypesAndValues(String eventName, ParamToAnalyze configParam, com.ericsson.schemamaster.entities.Parameter schemaParam,
            String paramValue, int sameSessionOccurences){
        switch(configParam.getType()){
        case MIN_MAX_TYPE:
            if((UINT.equals(schemaParam.getType()) || LONG.equals(schemaParam.getType())) && org.apache.commons.lang3.StringUtils.isNumeric(
                    paramValue)){
                long value = Long.parseLong(paramValue);
                if(value > configParam.getMinValue() && value < configParam.getMaxValue() || value == configParam.getMinValue()
                        || value == configParam.getMaxValue()){
                    updateParamValueSessionCountMap(eventName + DOT + configParam.getName(),paramValue,sameSessionOccurences);
                }
            }
            break;
        case LIST_TYPE:
            if(configParam.getListValue().contains(paramValue.toUpperCase())){
                updateParamValueSessionCountMap(eventName + DOT + configParam.getName(),paramValue,sameSessionOccurences);
            }
            break;
        default:
            updateParamValueSessionCountMap(eventName + DOT + configParam.getName(),paramValue,sameSessionOccurences);
            break;
        }
    }

    /**
     * Update param value session count map.
     *
     * @param eventParamName
     *            the event param name
     * @param paramValue
     *            the param value
     * @param sameSessionOccurences
     *            the same session occurences
     */
    private void updateParamValueSessionCountMap(String eventParamName, String paramValue, int sameSessionOccurences){
        Map<String, Integer> paramValueSessionCountMap = analysisResults.get(eventParamName);
        if(null == paramValueSessionCountMap){
            paramValueSessionCountMap = new HashMap<>();
        }
        if(paramValueSessionCountMap.containsKey(paramValue)){
            if(sameSessionOccurences == 1){
                int sessionCount = paramValueSessionCountMap.get(paramValue) + 1;
                paramValueSessionCountMap.put(paramValue,sessionCount);
            }
        } else{
            paramValueSessionCountMap.put(paramValue,1);
        }
        analysisResults.put(eventParamName,paramValueSessionCountMap);
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

}
