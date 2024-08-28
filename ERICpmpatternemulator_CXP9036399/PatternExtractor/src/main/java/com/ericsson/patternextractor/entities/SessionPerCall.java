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

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * The Class Session.
 */
@Component
public class SessionPerCall implements Comparable<SessionPerCall> {

    /** The session ID. */
    private String sessionID;

    /** The event ID list. */
    private List<Integer> eventIDList;

    /** The event obj list. */
    private List<SessionEvent> eventObjList;

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
     * Gets the event ID list.
     *
     * @return the event ID list
     */
    public List<Integer> getEventIDList(){
        if(null == eventObjList){
            this.eventIDList = new ArrayList<>();
        }
        return eventIDList;
    }

    /**
     * Sets the event ID list.
     *
     * @param eventIDList
     *            the new event ID list
     */
    public void setEventIDList(List<Integer> eventIDList){
        this.eventIDList = eventIDList;
    }

    /**
     * Gets the event obj list.
     *
     * @return the event obj list
     */
    public List<SessionEvent> getEventObjList(){
        if(null == eventObjList){
            eventObjList = new ArrayList<>();
        }
        return eventObjList;
    }

    /**
     * Sets the event obj list.
     *
     * @param eventObjList
     *            the new event obj list
     */
    public void setEventObjList(List<SessionEvent> eventObjList){
        this.eventObjList = eventObjList;
    }

    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString(){
        return "SessionPerCall [eventIDList=" + eventIDList + "]";
    }

    /**
     * Compare to.
     *
     * @param obj
     *            the obj
     * @return the int
     */
    @Override
    public int compareTo(SessionPerCall obj){
        int returnValue = 0;
        if(null != this.getEventObjList() && null != obj.getEventObjList() && this.getEventObjList().size() == obj.getEventObjList().size()){
            for(int i = 0; i < this.getEventObjList().size(); i++){
                if(null == this.getEventObjList().get(i).getPatternSelectionParameterMap() && null == obj.getEventObjList().get(i)
                        .getPatternSelectionParameterMap()){
                    continue;
                } else{
                    if(null != this.getEventObjList().get(i).getPatternSelectionParameterMap() && null != obj.getEventObjList().get(i)
                            .getPatternSelectionParameterMap()){
                        if(!this.getEventObjList().get(i).getPatternSelectionParameterMap().entrySet().equals(obj.getEventObjList().get(i)
                                .getPatternSelectionParameterMap().entrySet())){
                            returnValue = 1;
                            break;
                        }
                    }
                }

            }
        }
        return returnValue;
    }
}
