/*------------------------------------------------------------------------------
 ******************************************************************************
 * (c) Ericsson Inc. 2018 - All rights reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 ******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.schemamaster.services;

import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_NEIGHBOR_CGI;
import static com.ericsson.configmaster.constants.Constants.HANDOVER_PARAM_LIST;
import static com.ericsson.configmaster.constants.Constants.HANDOVER_RELATION_TYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.PatternConfigLoader;
import com.ericsson.schemamaster.entities.Parameter;

/**
 * The Class BytePackDecoder.
 */
@Service
public class SessionAttributeDecoder {

    /** The pattern config loader. */
    @Autowired
    private PatternConfigLoader patternConfigLoader;

    /** The byte pack decoder. */
    @Autowired
    private BytePackDecoder bytePackDecoder;

    /**
     * Find session.
     *
     * @param schemaParameterMap
     *            the schema parameter map
     * @param eventByteArray
     *            the event byte array
     * @return the map
     */
    public Map<String, String> findSession(Map<String, Parameter> schemaParameterMap, byte[] eventByteArray){
        int startIndex = 7; // Skip initial bytes of event and start with time stamp attributes
        Map<String, String> decodedSessionParamValuesMap = new HashMap<String, String>(patternConfigLoader.getSessionAttributesSet().size());
        String handoverParamName = findHandoverParam(schemaParameterMap);
        for(Entry<String, Parameter> entrySet : schemaParameterMap.entrySet()){
            Parameter paramElement = entrySet.getValue();
            int paramElementCalculatedLength = bytePackDecoder.calculateTotalParamLength(paramElement,schemaParameterMap,startIndex,eventByteArray);

            if(patternConfigLoader.getComplexAndArrayAttributesMap().keySet().contains(paramElement.getParameterName())){
                // This can be complex parameter or array parameter
                decodeSessionComplexOrArrayParam(decodedSessionParamValuesMap,paramElement,startIndex,eventByteArray,paramElementCalculatedLength);
            } else{
                // Decode pattern selection attributes other than complex and array type
                if(patternConfigLoader.getSessionAttributesSet().contains(paramElement.getParameterName()) || patternConfigLoader
                        .getPatternSelectionAttributesSet().contains(paramElement.getParameterName()) || paramElement.getParameterName().equals(
                                "EVENT_PARAM_TIMESTAMP_HOUR") || paramElement.getParameterName().equals("EVENT_PARAM_TIMESTAMP_MINUTE")
                        || paramElement.getParameterName().equals("EVENT_PARAM_TIMESTAMP_SECOND")){

                    String paramValue = bytePackDecoder.getDecodedValueOnValidityBits(paramElement,startIndex,eventByteArray,
                            paramElementCalculatedLength);
                    if(paramElement.getParameterName().equals("EVENT_PARAM_TIMESTAMP_HOUR") || paramElement.getParameterName().equals(
                            "EVENT_PARAM_TIMESTAMP_MINUTE") || paramElement.getParameterName().equals("EVENT_PARAM_TIMESTAMP_SECOND")){
                        if(paramValue.length() == 1){
                            paramValue = "0" + paramValue;
                        }
                    }
                    decodedSessionParamValuesMap.put(paramElement.getParameterName(),paramValue);
                }
                // Decode handover param
                else if(!handoverParamName.isEmpty()){
                    getHandoverType(paramElement,startIndex,eventByteArray,paramElementCalculatedLength,decodedSessionParamValuesMap);
                }
            }
            if(paramElement.getArraySize() > 1){
                startIndex = startIndex + paramElement.getArraySize() * paramElementCalculatedLength;
            } else{
                startIndex = startIndex + paramElementCalculatedLength;
            }
        }
        return decodedSessionParamValuesMap;
    }

    /**
     * Find handover param.
     *
     * @param schemaParameterMap
     *            the schema parameter map
     * @return the string
     */
    private String findHandoverParam(Map<String, Parameter> schemaParameterMap){
        if(schemaParameterMap.containsKey(EVENT_PARAM_NEIGHBOR_CGI)){
            for(String handoverParam : HANDOVER_PARAM_LIST){
                if(schemaParameterMap.containsKey(handoverParam)){
                    return handoverParam;
                }
            }
        }
        return "";
    }

    /**
     * Gets the handover type.
     *
     * @param paramElement
     *            the param element
     * @param startIndex
     *            the start index
     * @param eventByteArray
     *            the event byte array
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @return the handover type
     */
    private void getHandoverType(Parameter paramElement, int startIndex, byte[] eventByteArray, int paramElementCalculatedLength,
            Map<String, String> decodedSessionParamValuesMap){
        String relationType = bytePackDecoder.getValueByType(paramElement.getType(),startIndex,paramElementCalculatedLength,eventByteArray);
        if(paramElement.getParameterName().equals("EVENT_PARAM_RAT") && relationType.equals("1")){
            decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"3");
        } else if(paramElement.getParameterName().equals("EVENT_PARAM_HO_TYPE")){
            switch(relationType){
            case "0":
                decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"2"); // UTRAN
                break;
            case "1":
            case "2":
                decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"1"); // EUTRAN
                break;
            case "3":
                decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"3"); // GERAN
                break;
            case "undefined":
                decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"-1");
                break;
            }
        } else{
            switch(relationType){
            case "0":
            case "1":
                decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"1"); // EUTRAN
                break;
            case "2":
            case "5":
                decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"2"); // UTRAN
                break;
            case "3":
                decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"3"); // GERAN
                break;
            case "undefined":
                decodedSessionParamValuesMap.put(HANDOVER_RELATION_TYPE,"-1");
                break;
            }
        }
    }

    /**
     * Decode session complex or array param.
     *
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @param paramElement
     *            the param element
     * @param startIndex
     *            the start index
     * @param eventByteArray
     *            the event byte array
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @return the int
     */
    private void decodeSessionComplexOrArrayParam(Map<String, String> decodedSessionParamValuesMap, Parameter paramElement, int startIndex,
            byte[] eventByteArray, int paramElementCalculatedLength){
        Set<Integer> componentIDSet = patternConfigLoader.getComplexAndArrayAttributesMap().get(paramElement.getParameterName());
        bytePackDecoder.decodeComplexAndArrayParameter(paramElement,startIndex,eventByteArray,paramElementCalculatedLength,componentIDSet,
                decodedSessionParamValuesMap,patternConfigLoader.getDatasource());
    }
}
