package com.ericsson.patternreviser.eventaddition.services;

import static com.ericsson.configmaster.constants.Constants.ERROR;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.entities.EventToAddWrapper;
import com.ericsson.configmaster.services.ParamConfigLoader;
import com.ericsson.schemamaster.entities.Event;
import com.ericsson.schemamaster.entities.Parameter;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.BytePackEncoder;
import com.ericsson.utilities.services.Utils;

/**
 * The Class EventAdditionHandler.
 */
@Service
public class EventAdditionHandler {

    /** The param config loader. */
    @Autowired
    private ParamConfigLoader paramConfigLoader;

    /** The byte pack encoder. */
    @Autowired
    private BytePackEncoder bytePackEncoder;

    /**
     * Make event skeleton.
     *
     * @param eventNameList
     *            the event name list
     * @param schemaRelease
     *            the schema release
     */
    public void makeEventSkeleton(List<String> eventNameList, SchemaRelease schemaRelease){
        for(String referenceEvent : paramConfigLoader.getEventToAddDetails().keySet()){
            eventNameList.add(referenceEvent);
            Map<String, List<EventToAddWrapper>> positionBasedEventToAdd = paramConfigLoader.getEventToAddDetails().get(referenceEvent);
            for(String position : positionBasedEventToAdd.keySet()){
                for(EventToAddWrapper eventToAddWrapper : positionBasedEventToAdd.get(position)){
                    Event schemaEvent = schemaRelease.getEventNameMap().get(eventToAddWrapper.getEventName());
                    Map<String, byte[]> paramValueMap = new LinkedHashMap<>();
                    int totalLength = 7;
                    for(String schemaParamName : schemaEvent.getParameterMap().keySet()){
                        Parameter schemaParam = schemaEvent.getParameterMap().get(schemaParamName);
                        if(schemaParam.isVariableSize()){
                            String lengthParamName = schemaParam.getLength();
                            paramValueMap.put(lengthParamName,bytePackEncoder.convertLongValueToByteArray(1,paramValueMap.get(lengthParamName).length
                                    * 8));
                            paramValueMap.put(schemaParamName,new byte[1]);
                            totalLength++;
                        } else if(schemaParam.getArraySize() > 1){
                            int totalArrayBytes = schemaParam.getArraySize() * Integer.parseInt(schemaParam.getLength());
                            paramValueMap.put(schemaParamName,new byte[totalArrayBytes]);
                            totalLength += totalArrayBytes;
                        } else{
                            paramValueMap.put(schemaParamName,new byte[Integer.parseInt(schemaParam.getLength())]);
                            totalLength += Integer.parseInt(schemaParam.getLength());
                        }
                    }
                    try{
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        outputStream.write(bytePackEncoder.convertLongValueToByteArray(totalLength,16));
                        outputStream.write(bytePackEncoder.convertLongValueToByteArray(4,16));
                        outputStream.write(bytePackEncoder.convertLongValueToByteArray(schemaEvent.getEventID(),24));
                        for(String paramName : paramValueMap.keySet()){
                            outputStream.write(paramValueMap.get(paramName));
                        }
                        byte[] finalEvent = outputStream.toByteArray();
                        eventToAddWrapper.setEventData(finalEvent);
                        eventToAddWrapper.setRefEventID(schemaRelease.getEventNameMap().get(referenceEvent).getEventID());
                    } catch(Exception e){
                        Utils.logMessage(ERROR,"Error while preparing new event data. " + e);
                    }
                }
            }
        }
    }
}
