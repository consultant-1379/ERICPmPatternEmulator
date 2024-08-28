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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class Record.
 */
public class Record implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The record name. */
    private String recordName;

    /** The type. */
    private int type;

    /** The record param map. */
    private Map<String, String> recordParamMap;

    /**
     * Gets the record name.
     *
     * @return the record name
     */
    public String getRecordName(){
        return recordName;
    }

    /**
     * Sets the record name.
     *
     * @param recordName
     *            the new record name
     */
    public void setRecordName(String recordName){
        this.recordName = recordName;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public int getType(){
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type
     *            the new type
     */
    public void setType(int type){
        this.type = type;
    }

    /**
     * Gets the record param map.
     *
     * @return the record param map
     */
    public Map<String, String> getRecordParamMap(){
        if(null == recordParamMap){
            recordParamMap = new LinkedHashMap<String, String>();
        }
        return recordParamMap;
    }

    /**
     * Sets the record param map.
     *
     * @param paramMap
     *            the param map
     */
    public void setRecordParamMap(Map<String, String> paramMap){
        this.recordParamMap = paramMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(){
        return "Record [recordName=" + recordName + ", type=" + type + ", recordParamMap=" + recordParamMap.keySet() + "]";
    }

}
