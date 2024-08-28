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

package com.ericsson.patternextractor.entities;

import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * The Class SessionEvent.
 */
@Component
public class SessionEvent implements Comparable<SessionEvent> {

    /** The session ID. */
    private String sessionID;

    /** The event ID. */
    private int eventID;

    /** The event name. */
    private String eventName;

    /** The cell ID. */
    private String cellID;

    /** The event data. */
    private byte[] eventData;

    /** The pattern selection parameter map. */
    private Map<String, String> patternSelectionParameterMap;

    /** The type of relation. */
    private int typeOfRelation; // 0-internal (full call) ; 1-external ; 2-utran ; 3-geran ; -1-undefined

    /** The missing key. */
    private boolean missingKey;

    /** The date time. */
    private Date dateTime;

    /**
     * Gets the session ID.
     *
     * @return the session ID
     */
    public String getSessionID(){
        return sessionID;
    }

    /**
     * Sets the session ID.
     *
     * @param sessionID
     *            the new session ID
     */
    public void setSessionID(String sessionID){
        this.sessionID = sessionID;
    }

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
     * Gets the cell ID.
     *
     * @return the cell ID
     */
    public String getCellID(){
        return cellID;
    }

    /**
     * Sets the cell ID.
     *
     * @param cellID
     *            the new cell ID
     */
    public void setCellID(String cellID){
        this.cellID = cellID;
    }

    /**
     * Gets the event data.
     *
     * @return the event data
     */
    public byte[] getEventData(){
        return eventData;
    }

    /**
     * Sets the event data.
     *
     * @param eventData
     *            the new event data
     */
    public void setEventData(byte[] eventData){
        this.eventData = eventData;
    }

    /**
     * Gets the pattern selection parameter map.
     *
     * @return the pattern selection parameter map
     */
    public Map<String, String> getPatternSelectionParameterMap(){
        return patternSelectionParameterMap;
    }

    /**
     * Sets the pattern selection parameter map.
     *
     * @param patternSelectionParameterMap
     *            the pattern selection parameter map
     */
    public void setPatternSelectionParameterMap(Map<String, String> patternSelectionParameterMap){
        this.patternSelectionParameterMap = patternSelectionParameterMap;
    }

    /**
     * Gets the type of relation.
     *
     * @return the type of relation
     */
    public int getTypeOfRelation(){
        return typeOfRelation;
    }

    /**
     * Sets the type of relation.
     *
     * @param typeOfRelation
     *            the new type of relation
     */
    public void setTypeOfRelation(int typeOfRelation){
        this.typeOfRelation = typeOfRelation;
    }

    /**
     * Checks if is missing key.
     *
     * @return true, if is missing key
     */
    public boolean isMissingKey(){
        return missingKey;
    }

    /**
     * Sets the missing key.
     *
     * @param missingKey
     *            the new missing key
     */
    public void setMissingKey(boolean missingKey){
        this.missingKey = missingKey;
    }

    /**
     * Gets the date time.
     *
     * @return the date time
     */
    public Date getDateTime(){
        return dateTime;
    }

    /**
     * Sets the date time.
     *
     * @param dateTime
     *            the new date time
     */
    public void setDateTime(Date dateTime){
        this.dateTime = dateTime;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString(){
        return "SessionEvent [sessionID=" + sessionID + ", eventID=" + eventID + ", eventName=" + eventName + ", cellID=" + cellID
                + ", patternSelectionParameterMap=" + patternSelectionParameterMap + "]";
    }

    /**
     * Compare to.
     *
     * @param obj
     *            the obj
     * @return the int
     */
    @Override
    public int compareTo(SessionEvent obj){
        if(this.getDateTime().compareTo(obj.getDateTime()) > 0){
            return 1;
        } else if(this.getDateTime().compareTo(obj.getDateTime()) < 0){
            return -1;
        } else{
            return 0;
        }
    }
}
