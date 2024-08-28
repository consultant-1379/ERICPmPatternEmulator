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
 * The Class SchemaRelease.
 */
public class SchemaRelease implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The event map. */
    private Map<Integer, Event> eventIDMap = new HashMap<Integer, Event>();

    private Map<String, Event> eventNameMap = new HashMap<String, Event>();

    /** The parameter map. */
    private Map<String, Parameter> parameterMap = new HashMap<String, Parameter>();

    /** The record map. */
    private Map<String, Record> recordMap = new HashMap<String, Record>();

    /** The release name. */
    private String releaseName;

    /** The ffv. */
    private String ffv;

    /** The doc number. */
    private String docNumber;

    /** The revision. */
    private String revision;

    /**
     * Gets the event map.
     *
     * @return the event map
     */
    public Map<Integer, Event> getEventIDMap(){
        return eventIDMap;
    }

    /**
     * Sets the event map.
     *
     * @param eventMap
     *            the event map
     */
    public void setEventIDMap(Map<Integer, Event> eventIDMap){
        this.eventIDMap = eventIDMap;
    }

    /**
     * Gets the parameter map.
     *
     * @return the parameter map
     */
    public Map<String, Parameter> getParameterMap(){
        return parameterMap;
    }

    /**
     * Sets the parameter map.
     *
     * @param parameterMap
     *            the parameter map
     */
    public void setParameterMap(Map<String, Parameter> parameterMap){
        this.parameterMap = parameterMap;
    }

    /**
     * Gets the record map.
     *
     * @return the record map
     */
    public Map<String, Record> getRecordMap(){
        return recordMap;
    }

    /**
     * Sets the record map.
     *
     * @param recordMap
     *            the record map
     */
    public void setRecordMap(Map<String, Record> recordMap){
        this.recordMap = recordMap;
    }

    /**
     * Gets the release name.
     *
     * @return the release name
     */
    public String getReleaseName(){
        return releaseName;
    }

    /**
     * Sets the release name.
     *
     * @param releaseName
     *            the new release name
     */
    public void setReleaseName(String releaseName){
        this.releaseName = releaseName;
    }

    /**
     * Gets the ffv.
     *
     * @return the ffv
     */
    public String getFfv(){
        return ffv;
    }

    /**
     * Sets the ffv.
     *
     * @param ffv
     *            the new ffv
     */
    public void setFfv(String ffv){
        this.ffv = ffv;
    }

    /**
     * Gets the doc number.
     *
     * @return the doc number
     */
    public String getDocNumber(){
        return docNumber;
    }

    /**
     * Sets the doc number.
     *
     * @param docNumber
     *            the new doc number
     */
    public void setDocNumber(String docNumber){
        this.docNumber = docNumber;
    }

    /**
     * Gets the revision.
     *
     * @return the revision
     */
    public String getRevision(){
        return revision;
    }

    /**
     * Sets the revision.
     *
     * @param revision
     *            the new revision
     */
    public void setRevision(String revision){
        this.revision = revision;
    }

    public Map<String, Event> getEventNameMap(){
        return eventNameMap;
    }

    public void setEventNameMap(Map<String, Event> eventNameMap){
        this.eventNameMap = eventNameMap;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(){
        return "SchemaRelease [releaseName=" + releaseName + ", ffv=" + ffv + ", docNumber=" + docNumber + ", revision=" + revision + "]";
    }

}
