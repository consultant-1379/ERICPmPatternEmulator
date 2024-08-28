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

package com.ericsson.utilities.entities;

import java.io.Serializable;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * The Class SessionPerCallWrapper.
 */
@Component
public class SessionPerCallWrapper implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The event data list. */
    private List<byte[]> eventDataList;

    /** The type of relation. */
    private int typeOfRelation;

    /** The event ID list. */
    private List<Integer> eventIDList;

    /**
     * Gets the event data list.
     *
     * @return the event data list
     */
    public List<byte[]> getEventDataList(){
        return eventDataList;
    }

    /**
     * Sets the event data list.
     *
     * @param eventDataList
     *            the new event data list
     */
    public void setEventDataList(List<byte[]> eventDataList){
        this.eventDataList = eventDataList;
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
     * Gets the event ID list.
     *
     * @return the event ID list
     */
    public List<Integer> getEventIDList(){
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

}
