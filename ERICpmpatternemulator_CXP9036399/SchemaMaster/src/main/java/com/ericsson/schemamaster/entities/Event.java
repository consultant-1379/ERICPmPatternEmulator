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
import java.util.Map;

/**
 * The Class Event.
 */
public class Event implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The event ID. */
    private int eventID;

    /** The event name. */
    private String eventName;

    /** The parameter map. */
    private Map<String, Parameter> parameterMap;

    /**
     * Gets the event ID.
     *
     * @return the event ID
     */
    public int getEventID(){
        return eventID;
    }

    /**
     * Sets the event ID.
     *
     * @param eventID
     *            the new event ID
     */
    public void setEventID(int eventID){
        this.eventID = eventID;
    }

    /**
     * Gets the event name.
     *
     * @return the event name
     */
    public String getEventName(){
        return eventName;
    }

    /**
     * Sets the event name.
     *
     * @param eventName
     *            the new event name
     */
    public void setEventName(String eventName){
        this.eventName = eventName;
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString(){
        return "Event [eventID=" + eventID + ", eventName=" + eventName + ", parameterMap=" + parameterMap.keySet() + "]";
    }

}
