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

package com.ericsson.schemamaster.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class Parameter.
 */
public class Parameter implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The parameter name. */
    private String parameterName;

    /** The type. */
    private String type;

    /** The length. */
    private String length;

    /** The array size. */
    private int arraySize = 1;

    /** The high range value. */
    private long highRangeValue;

    /** The low range value. */
    private long lowRangeValue;

    /** The is bit packed. */
    private boolean isBitPacked;

    /** The is use valid. */
    private boolean isUseValid;

    /** The is variable size. */
    private boolean isVariableSize;

    /** The enum param value map. */
    private Map<String, String> enumParamValueMap;

    /**
     * Gets the parameter name.
     *
     * @return the parameter name
     */
    public String getParameterName(){
        return parameterName;
    }

    /**
     * Sets the parameter name.
     *
     * @param parameterName
     *            the new parameter name
     */
    public void setParameterName(String parameterName){
        this.parameterName = parameterName;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType(){
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(String type){
        this.type = type;
    }

    /**
     * Gets the length.
     *
     * @return the length
     */
    public String getLength(){
        return length;
    }

    /**
     * Sets the length.
     *
     * @param length
     *            the new length
     */
    public void setLength(String length){
        this.length = length;
    }

    /**
     * Checks if is use valid.
     *
     * @return true, if is use valid
     */
    public boolean isUseValid(){
        return isUseValid;
    }

    /**
     * Sets the use valid.
     *
     * @param isUseValid
     *            the new use valid
     */
    public void setUseValid(boolean isUseValid){
        this.isUseValid = isUseValid;
    }

    /**
     * Checks if is variable size.
     *
     * @return true, if is variable size
     */
    public boolean isVariableSize(){
        return isVariableSize;
    }

    /**
     * Sets the variable size.
     *
     * @param isVariableSize
     *            the new variable size
     */
    public void setVariableSize(boolean isVariableSize){
        this.isVariableSize = isVariableSize;
    }

    /**
     * Gets the high range value.
     *
     * @return the high range value
     */
    public long getHighRangeValue(){
        return highRangeValue;
    }

    /**
     * Sets the high range value.
     *
     * @param highRangeValue
     *            the new high range value
     */
    public void setHighRangeValue(long highRangeValue){
        this.highRangeValue = highRangeValue;
    }

    /**
     * Gets the low range value.
     *
     * @return the low range value
     */
    public long getLowRangeValue(){
        return lowRangeValue;
    }

    /**
     * Sets the low range value.
     *
     * @param lowRangeValue
     *            the new low range value
     */
    public void setLowRangeValue(long lowRangeValue){
        this.lowRangeValue = lowRangeValue;
    }

    /**
     * Checks if is bit packed.
     *
     * @return true, if is bit packed
     */
    public boolean isBitPacked(){
        return isBitPacked;
    }

    /**
     * Sets the bit packed.
     *
     * @param isBitPacked
     *            the new bit packed
     */
    public void setBitPacked(boolean isBitPacked){
        this.isBitPacked = isBitPacked;
    }

    /**
     * Gets the array size.
     *
     * @return the array size
     */
    public int getArraySize(){
        return arraySize;
    }

    /**
     * Sets the array size.
     *
     * @param arraySize
     *            the new array size
     */
    public void setArraySize(int arraySize){
        this.arraySize = arraySize;
    }

    /**
     * Gets the enum param value map.
     *
     * @return the enum param value map
     */
    public Map<String, String> getEnumParamValueMap(){
        if(null == enumParamValueMap){
            enumParamValueMap = new HashMap<String, String>();
        }
        return enumParamValueMap;
    }

    /**
     * Sets the enum param value map.
     *
     * @param enumParamValueMap
     *            the enum param value map
     */
    public void setEnumParamValueMap(Map<String, String> enumParamValueMap){
        this.enumParamValueMap = enumParamValueMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(){
        return "Parameter [parameterName=" + parameterName + ", type=" + type + ", length=" + length + ", isUseValid=" + isUseValid
                + ", isVariableSize=" + isVariableSize + ", highRangeValue=" + highRangeValue + ", lowRangeValue=" + lowRangeValue + ", isBitPacked="
                + isBitPacked + ", enumParamValueMap=" + enumParamValueMap.toString() + "]";
    }

}
