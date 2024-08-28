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

import static com.ericsson.configmaster.constants.Constants.BINARY;
import static com.ericsson.configmaster.constants.Constants.COLON;
import static com.ericsson.configmaster.constants.Constants.ENUM;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_GENBID;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_GUMMEI;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_NEIGHBOR_CGI;
import static com.ericsson.configmaster.constants.Constants.FORWARD_SLASH;
import static com.ericsson.configmaster.constants.Constants.LONG;
import static com.ericsson.configmaster.constants.Constants.STRING;
import static com.ericsson.configmaster.constants.Constants.UINT;
import static com.ericsson.configmaster.constants.Constants.UNDEFINED;
import static com.ericsson.configmaster.constants.Constants.UNDERSCORE;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.appconfig.autogen.AppConfiguration.DataSource.ComplexParameters.Parameter.Component;
import com.ericsson.configmaster.services.AppConfigLoader;
import com.ericsson.schemamaster.entities.Parameter;
import com.ericsson.utilities.services.Utils;

/**
 * The Class BytePackDecoder.
 */
@Service
public class BytePackDecoder {

    /** The app config loader. */
    @Autowired
    private AppConfigLoader appConfigLoader;

    /** The bit pack decoder. */
    @Autowired
    private BitPackDecoder bitPackDecoder;

    /**
     * Calculate total param length.
     *
     * @param paramElement
     *            the param element
     * @param schemaParameterMap
     *            the schema parameter map
     * @param startIndex
     *            the start index
     * @param eventByteArray
     *            the event byte array
     * @return the int
     */
    public int calculateTotalParamLength(Parameter paramElement, Map<String, Parameter> schemaParameterMap, int startIndex, byte[] eventByteArray){
        if(paramElement.isVariableSize()){
            String lengthElementName = paramElement.getLength();
            int variableElementLength = Integer.parseInt(schemaParameterMap.get(lengthElementName).getLength());
            return getIntegerValueForTwoBytesRecord(startIndex - variableElementLength,startIndex - 1,eventByteArray);
        } else{
            return Integer.parseInt(paramElement.getLength());
        }
    }

    /**
     * Gets the integer value for two bytes record.
     *
     * @param startIndex
     *            the start index
     * @param endIndex
     *            the end index
     * @param eventBuffer
     *            the event buffer
     * @return the integer value for two bytes record
     */
    public int getIntegerValueForTwoBytesRecord(int startIndex, int endIndex, byte[] eventBuffer){
        byte[] recordByte = { 0, 0, eventBuffer[startIndex], eventBuffer[endIndex] };
        ByteBuffer byteBuffer = ByteBuffer.wrap(recordByte);
        return byteBuffer.getInt();
    }

    /**
     * Gets the event id.
     *
     * @param eventData
     *            the event data
     * @return the event id
     */
    public int getEventId(byte[] eventData){
        byte[] len = { 0, eventData[4], eventData[5], eventData[6] };
        ByteBuffer buffer = ByteBuffer.wrap(len);
        return buffer.getInt();
    }

    /**
     * Decode complex and array parameter.
     *
     * @param paramElement
     *            the param element
     * @param startIndex
     *            the start index
     * @param eventByteArray
     *            the event byte array
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @param componentIDSet
     *            the component ID set
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @param datasource
     *            the datasource
     * @return the int
     */
    public void decodeComplexAndArrayParameter(Parameter paramElement, int startIndex, byte[] eventByteArray, int paramElementCalculatedLength,
            Set<Integer> componentIDSet, Map<String, String> decodedSessionParamValuesMap, String datasource){
        if(paramElement.getArraySize() > 1){
            decodeArrayParameter(paramElement,startIndex,eventByteArray,paramElementCalculatedLength,componentIDSet,decodedSessionParamValuesMap);
        } else{
            decodeComplexParameter(paramElement,startIndex,eventByteArray,paramElementCalculatedLength,componentIDSet,decodedSessionParamValuesMap,
                    datasource);
        }
    }

    /**
     * Decode array parameter.
     *
     * @param paramElement
     *            the param element
     * @param startIndex
     *            the start index
     * @param eventByteArray
     *            the event byte array
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @param componentIDSet
     *            the component ID set
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     */
    public void decodeArrayParameter(Parameter paramElement, int startIndex, byte[] eventByteArray, int paramElementCalculatedLength,
            Set<Integer> componentIDSet, Map<String, String> decodedSessionParamValuesMap){
        for(int componentID : componentIDSet){
            int arrayStartIndex = startIndex + componentID * paramElementCalculatedLength;
            String paramValue = getDecodedValueOnValidityBits(paramElement,arrayStartIndex,eventByteArray,paramElementCalculatedLength);
            decodedSessionParamValuesMap.put(paramElement.getParameterName() + COLON + componentID,paramValue);
        }
    }

    /**
     * Decode complex parameter.
     *
     * @param paramElement
     *            the param element
     * @param startIndex
     *            the start index
     * @param eventByteArray
     *            the event byte array
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @param componentIDSet
     *            the component ID set
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @param datasource
     *            the datasource
     */
    public void decodeComplexParameter(Parameter paramElement, int startIndex, byte[] eventByteArray, int paramElementCalculatedLength,
            Set<Integer> componentIDSet, Map<String, String> decodedSessionParamValuesMap, String datasource){
        Set<Integer> tempComponentIDSet = new TreeSet<Integer>(componentIDSet);

        String parameterName = paramElement.getParameterName();
        if(parameterName.equals(EVENT_PARAM_GUMMEI) || parameterName.equals(EVENT_PARAM_GENBID) || parameterName.equals(EVENT_PARAM_NEIGHBOR_CGI)){
            String tbcdValue = null;
            if(componentIDSet.contains(1) || componentIDSet.contains(2) || componentIDSet.contains(3)){
                // Decode PLMN Identity parameter value
                byte[] tbcdByte = new byte[3];
                System.arraycopy(eventByteArray,startIndex + 1,tbcdByte,0,3);
                tbcdValue = getPLMNValue(tbcdByte);

                // PLMN
                if(componentIDSet.contains(1)){
                    decodedSessionParamValuesMap.put(parameterName + COLON + 1,tbcdValue);
                    tempComponentIDSet.remove(1);
                }
                // MCC
                if(componentIDSet.contains(2)){
                    decodedSessionParamValuesMap.put(parameterName + COLON + 2,tbcdValue.substring(0,3));
                    tempComponentIDSet.remove(2);
                }
                // MNC
                if(componentIDSet.contains(3)){
                    decodedSessionParamValuesMap.put(parameterName + COLON + 3,tbcdValue.substring(3,tbcdValue.length()));
                    tempComponentIDSet.remove(3);
                }
            }

            if(componentIDSet.contains(-1)){
                decodedSessionParamValuesMap.put(parameterName,getCompleteComplexParamValue(tbcdValue,startIndex,paramElementCalculatedLength,
                        eventByteArray));
                tempComponentIDSet.remove(-1);
            }
            if(!tempComponentIDSet.isEmpty()){
                getComplexParamComponentValue(tempComponentIDSet,parameterName,startIndex,paramElementCalculatedLength,eventByteArray,
                        decodedSessionParamValuesMap,datasource);
            }
        } else{
            if(componentIDSet.contains(-1)){
                decodedSessionParamValuesMap.put(parameterName,getDecodedValueOnValidityBits(paramElement,startIndex,eventByteArray,
                        paramElementCalculatedLength));
                tempComponentIDSet.remove(-1);
            }
            if(!tempComponentIDSet.isEmpty()){
                getComplexParamComponentValue(tempComponentIDSet,parameterName,startIndex,paramElementCalculatedLength,eventByteArray,
                        decodedSessionParamValuesMap,datasource);
            }
        }
    }

    /**
     * Gets the value by type.
     *
     * @param type
     *            the type
     * @param startIndex
     *            the start index
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @param eventByteArray
     *            the event byte array
     * @return the value by type
     */
    public String getValueByType(String type, int startIndex, int paramElementCalculatedLength, byte[] eventByteArray){
        switch(type){
        case UINT:
        case LONG:
        case ENUM:
            return getParamLongValue(startIndex,paramElementCalculatedLength,eventByteArray);
        case BINARY:
        case STRING:
            return getParamStringValue(startIndex,paramElementCalculatedLength,eventByteArray);
        }
        return UNDEFINED;
    }

    /**
     * Gets the param long value.
     *
     * @param startIndex
     *            the start index
     * @param paramElementLength
     *            the param element length
     * @param eventByteArray
     *            the event byte array
     * @return the param long value
     */
    public String getParamLongValue(int startIndex, int paramElementLength, byte[] eventByteArray){
        long value = 0;
        for(int byteIndex = startIndex; byteIndex < paramElementLength + startIndex; byteIndex++){
            value = value << 8 | eventByteArray[byteIndex] & 0xFF;
        }
        return String.valueOf(value);
    }

    /**
     * Gets the param string value.
     *
     * @param startIndex
     *            the start index
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @param sourceArray
     *            the source array
     * @return the param string value
     */
    public String getParamStringValue(int startIndex, int paramElementCalculatedLength, byte[] sourceArray){
        byte[] targetArray = new byte[paramElementCalculatedLength];
        System.arraycopy(sourceArray,startIndex,targetArray,0,paramElementCalculatedLength);
        return new String(targetArray).trim();
    }

    /**
     * Gets the complex param component value.
     *
     * @param tempComponentIDSet
     *            the temp component ID set
     * @param parameterName
     *            the parameter name
     * @param startIndex
     *            the start index
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @param eventByteArray
     *            the event byte array
     * @param decodedSessionParamValuesMap
     *            the decoded session param values map
     * @param datasource
     *            the datasource
     * @return the complex param component value
     */
    public void getComplexParamComponentValue(Set<Integer> tempComponentIDSet, String parameterName, int startIndex, int paramElementCalculatedLength,
            byte[] eventByteArray, Map<String, String> decodedSessionParamValuesMap, String datasource){

        Map<Integer, Component> schemaComponentMap = appConfigLoader.getAppConfigDatasourceWrappereMap().get(datasource).getComplexParameterInfoMap()
                .get(parameterName);

        byte[] componentBytes = new byte[paramElementCalculatedLength];
        System.arraycopy(eventByteArray,startIndex,componentBytes,0,paramElementCalculatedLength);

        for(int componentId : tempComponentIDSet){
            Component component = schemaComponentMap.get(componentId);
            String componentValue = bitPackDecoder.decodeBits(component.getStartBit(),component.getLengthBits(),componentBytes);
            decodedSessionParamValuesMap.put(parameterName + COLON + componentId,componentValue);
        }
    }

    /**
     * Gets the complete complex param value.
     *
     * @param tbcdValue
     *            the tbcd value
     * @param startIndex
     *            the start index
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @param eventByteArray
     *            the event byte array
     * @return the complete complex param value
     */
    private String getCompleteComplexParamValue(String tbcdValue, int startIndex, int paramElementCalculatedLength, byte[] eventByteArray){
        if(null == tbcdValue){
            // Decode PLMN Identity parameter value
            byte[] tbcdByte = new byte[3];
            System.arraycopy(eventByteArray,startIndex + 1,tbcdByte,0,3);
            tbcdValue = getPLMNValue(tbcdByte);
        }

        // Decode remaining value
        byte[] remainingBytes = new byte[paramElementCalculatedLength - 4];
        System.arraycopy(eventByteArray,startIndex + 4,remainingBytes,0,remainingBytes.length);
        BigInteger remainingValue = new BigInteger(1, remainingBytes);

        return tbcdValue + remainingValue.toString();
    }

    /**
     * Gets the release.
     *
     * @param eventFileByteBuffer
     *            the event file byte buffer
     * @param recordLength
     *            the record length
     * @return the release
     */
    public String getRelease(byte[] eventFileByteBuffer, int recordLength){
        String fileFormatVersion = getParamStringValue(4,5,eventFileByteBuffer);
        String pmRecordingRevision;
        // Handling for 14A Schema
        if(recordLength >= 417){
            String pmRecordingVersion = getParamStringValue(9,13,eventFileByteBuffer).split(FORWARD_SLASH)[1];
            pmRecordingRevision = getParamStringValue(22,5,eventFileByteBuffer);
            return fileFormatVersion.concat(UNDERSCORE).concat(pmRecordingVersion).concat(UNDERSCORE).concat(pmRecordingRevision);
        } else{
            pmRecordingRevision = getParamStringValue(22,5,eventFileByteBuffer);
            return fileFormatVersion.concat(UNDERSCORE).concat(pmRecordingRevision);
        }
    }

    /**
     * Gets the PLMN value.
     *
     * @param tbcdData
     *            the tbcd data
     * @return the PLMN value
     */
    public String getPLMNValue(byte[] tbcdData){
        String tbcdBytesValue = "";
        for(byte b : tbcdData){
            tbcdBytesValue += Utils.numToBinary(b,8);
        }
        // Create an array to hold each integer value of mcc & mnc
        String[] plmnAttributes = new String[6];
        int counter = 0;
        int sizeOfChunks = 4;
        for(int bitIndex = 0; bitIndex + sizeOfChunks <= tbcdBytesValue.length(); bitIndex = bitIndex + sizeOfChunks){
            plmnAttributes[counter] = String.valueOf(Integer.parseInt(tbcdBytesValue.substring(bitIndex,bitIndex + sizeOfChunks),2));
            counter++;
        }
        // swap elements and create mcc as well as mnc value
        String mcc = plmnAttributes[1] + plmnAttributes[0] + plmnAttributes[3];
        if(plmnAttributes[2].equals("15")){
            plmnAttributes[2] = "F";
        }
        String mnc = plmnAttributes[2] + plmnAttributes[5] + plmnAttributes[4];
        return mcc + mnc;
    }

    /**
     * Gets the decoded value on validity bits.
     *
     * @param paramElement
     *            the param element
     * @param startIndex
     *            the start index
     * @param eventByteArray
     *            the event byte array
     * @param paramElementCalculatedLength
     *            the param element calculated length
     * @return the decoded value on validity bits
     */
    public String getDecodedValueOnValidityBits(Parameter paramElement, int startIndex, byte[] eventByteArray, int paramElementCalculatedLength){
        if(paramElement.isUseValid()){
            if(validityCheck(eventByteArray,startIndex)){
                return getValueByType(paramElement.getType(),startIndex,paramElementCalculatedLength,eventByteArray);
            } else{
                return UNDEFINED;
            }
        } else{
            return getValueByType(paramElement.getType(),startIndex,paramElementCalculatedLength,eventByteArray);
        }
    }

    /**
     * Validity check.
     *
     * @param eventByteArray
     *            the event byte array
     * @param startIndex
     *            the start index
     * @return true, if successful
     */
    private boolean validityCheck(byte[] eventByteArray, int startIndex){
        int validityCheck = (eventByteArray[startIndex] & 0xff) >> 7;
        if(validityCheck == 0){
            return true;
        }
        return false;
    }

}
