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
package com.ericsson.configmaster.services;

import static com.ericsson.configmaster.constants.Constants.COLON;
import static com.ericsson.configmaster.constants.Constants.PARAM_CONFIG_FILE_PATH;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.constants.Constants;
import com.ericsson.configmaster.entities.EventToAddWrapper;
import com.ericsson.configmaster.paramconfig.autogen.ParamConfig;
import com.ericsson.configmaster.paramconfig.autogen.ParamConfig.Analysis.EventToAnalyze;
import com.ericsson.configmaster.paramconfig.autogen.ParamConfig.Analysis.EventToAnalyze.ParamToAnalyze;
import com.ericsson.configmaster.paramconfig.autogen.ParamConfig.EventAddition.EventToAdd;

/**
 * The Class ParamConfigLoader.
 */
@Service
public class ParamConfigLoader {

    /** The pattern location. */
    private String patternLocation;

    /** The output location. */
    private String outputLocation;

    /** The param config events. */
    private Map<String, Map<String, ParamToAnalyze>> analysisEventsDetailsMap;

    /** The complex or array param config events. */
    private Map<String, Map<String, Map<Integer, ParamToAnalyze>>> complexOrArrayAnalysisEvents;

    /** The param config events list. */
    private List<String> analysisEventList;

    /** The event to add details. */
    private Map<String, Map<String, List<EventToAddWrapper>>> eventToAddDetails;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Load analyzer config file.
     *
     * @throws Exception
     *             the exception
     */
    @PostConstruct
    public void loadAnalyzerConfigFile() throws Exception{
        JAXBContext jaxbContext = null;
        File file = new File(PARAM_CONFIG_FILE_PATH);
        jaxbContext = JAXBContext.newInstance(ParamConfig.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        ParamConfig paramConfig = (ParamConfig) jaxbUnmarshaller.unmarshal(file);
        if(paramConfig.getAnalysis().getIsRequired().equalsIgnoreCase(Constants.YES)){
            analysisEventsDetailsMap = new HashMap<>();
            complexOrArrayAnalysisEvents = new HashMap<>();
            analysisEventList = new ArrayList<>();
            for(EventToAnalyze eventToAnalyze : paramConfig.getAnalysis().getEventToAnalyze()){
                if(analysisEventsDetailsMap.containsKey(eventToAnalyze.getName())){
                    System.out.println("Duplicate configuration for event : " + eventToAnalyze.getName()
                            + " Please merge all the parameters in single event tag");
                    return;
                }
                analysisEventList.add(eventToAnalyze.getName());
                loadAnalysisParameterDetails(eventToAnalyze);
            }
        } else{
            if(paramConfig.getEventAddition().getIsRequired().equalsIgnoreCase(Constants.YES)){
                eventToAddDetails = new HashMap<>();
                for(EventToAdd eventToAdd : paramConfig.getEventAddition().getEventToAdd()){
                    EventToAddWrapper eventToAddWrapper = applicationContext.getBean(EventToAddWrapper.class);
                    eventToAddWrapper.setEventName(eventToAdd.getName());
                    Map<String, List<EventToAddWrapper>> positionBasedEventMap = eventToAddDetails.get(eventToAdd.getReferenceEvent());
                    if(null == positionBasedEventMap){
                        positionBasedEventMap = new HashMap<>();
                        List<EventToAddWrapper> eventToAddWrapperList = new ArrayList<>();
                        eventToAddWrapperList.add(eventToAddWrapper);
                        positionBasedEventMap.put(eventToAdd.getPosition(),eventToAddWrapperList);
                    } else{
                        List<EventToAddWrapper> eventToAddList = positionBasedEventMap.get(eventToAdd.getPosition());
                        if(null == eventToAddList){
                            eventToAddList = new ArrayList<>();
                        }
                        eventToAddList.add(eventToAddWrapper);
                        positionBasedEventMap.put(eventToAdd.getPosition(),eventToAddList);
                    }
                    eventToAddDetails.put(eventToAdd.getReferenceEvent(),positionBasedEventMap);
                }
            }
            if(paramConfig.getEnrichment().getIsRequired().equalsIgnoreCase(Constants.YES)){
                // yet to develop
            }
        }
        patternLocation = paramConfig.getPatternLocation();
        outputLocation = paramConfig.getOutputLocation();
    }

    /**
     * Update parameter details.
     *
     * @param eventToAnalyze
     *            the event to analyze
     */
    private void loadAnalysisParameterDetails(EventToAnalyze eventToAnalyze){
        Map<String, ParamToAnalyze> parameterDetails = new HashMap<>();
        Map<String, Map<Integer, ParamToAnalyze>> complexOrArrayAnalysisParam = new HashMap<>();
        for(ParamToAnalyze paramObj : eventToAnalyze.getParamToAnalyze()){
            if(paramObj.getName().contains(COLON)){
                String[] paramComponentString = paramObj.getName().split(COLON);
                Map<Integer, ParamToAnalyze> componentIDMap = complexOrArrayAnalysisParam.get(paramComponentString[0]);
                if(null == componentIDMap){
                    componentIDMap = new TreeMap<>();
                }
                componentIDMap.put(Integer.parseInt(paramComponentString[1]),paramObj);
                complexOrArrayAnalysisParam.put(paramComponentString[0],componentIDMap);
            } else{
                parameterDetails.put(paramObj.getName(),paramObj);
            }
        }
        if(!parameterDetails.isEmpty()){
            analysisEventsDetailsMap.put(eventToAnalyze.getName(),parameterDetails);
        }
        if(!complexOrArrayAnalysisParam.isEmpty()){
            complexOrArrayAnalysisEvents.put(eventToAnalyze.getName(),complexOrArrayAnalysisParam);
        }
    }

    /**
     * Gets the pattern location.
     *
     * @return the pattern location
     */
    public String getPatternLocation(){
        return patternLocation;
    }

    /**
     * Gets the output location.
     *
     * @return the output location
     */
    public String getOutputLocation(){
        return outputLocation;
    }

    /**
     * Gets the analysis events details map.
     *
     * @return the analysis events details map
     */
    public Map<String, Map<String, ParamToAnalyze>> getAnalysisEventsDetailsMap(){
        return analysisEventsDetailsMap;
    }

    /**
     * Gets the complex or array analysis events.
     *
     * @return the complex or array analysis events
     */
    public Map<String, Map<String, Map<Integer, ParamToAnalyze>>> getComplexOrArrayAnalysisEvents(){
        return complexOrArrayAnalysisEvents;
    }

    /**
     * Gets the param config events list.
     *
     * @return the param config events list
     */
    public List<String> getAnalysisEventList(){
        return analysisEventList;
    }

    /**
     * Gets the event to add details.
     *
     * @return the event to add details
     */
    public Map<String, Map<String, List<EventToAddWrapper>>> getEventToAddDetails(){
        return eventToAddDetails;
    }

    /**
     * Sets the event to add details.
     *
     * @param eventToAddDetails
     *            the event to add details
     */
    public void setEventToAddDetails(Map<String, Map<String, List<EventToAddWrapper>>> eventToAddDetails){
        this.eventToAddDetails = eventToAddDetails;
    }

}
