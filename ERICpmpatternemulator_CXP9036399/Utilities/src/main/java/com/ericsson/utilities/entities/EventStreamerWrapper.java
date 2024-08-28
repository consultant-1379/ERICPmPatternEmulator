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

import org.springframework.stereotype.Component;

/**
 * The Class EventStreamerWrapper.
 */
@Component
public class EventStreamerWrapper implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The event ID. */
    private int eventID;

    /** The event byte. */
    private byte[] eventByte;

    /** The fdn. */
    private String fdn;

    /**
     * Gets the event ID.
     *
     * @return the eventID
     */
    public int getEventID(){
        return eventID;
    }

    /**
     * Sets the event ID.
     *
     * @param eventID
     *            the eventID to set
     */
    public void setEventID(int eventID){
        this.eventID = eventID;
    }

    /**
     * Gets the fdn.
     *
     * @return the fdn
     */
    public String getFdn(){
        return fdn;
    }

    /**
     * Sets the fdn.
     *
     * @param fdn
     *            the fdn to set
     */
    public void setFdn(String fdn){
        this.fdn = fdn;
    }

    /**
     * Gets the event byte.
     *
     * @return the event byte
     */
    public byte[] getEventByte(){
        return eventByte;
    }

    /**
     * Sets the event byte.
     *
     * @param eventByte
     *            the new event byte
     */
    public void setEventByte(byte[] eventByte){
        this.eventByte = eventByte;
    }

}
