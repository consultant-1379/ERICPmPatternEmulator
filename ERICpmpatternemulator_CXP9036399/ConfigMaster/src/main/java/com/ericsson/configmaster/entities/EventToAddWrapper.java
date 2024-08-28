package com.ericsson.configmaster.entities;

import org.springframework.stereotype.Component;

/**
 * The Class EventToAddWrapper.
 */
@Component
public class EventToAddWrapper {

    /** The event name. */
    private String eventName;

    /** The event data. */
    private byte[] eventData;

    /** The ref event ID. */
    private int refEventID;

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
     * Gets the ref event ID.
     *
     * @return the ref event ID
     */
    public int getRefEventID(){
        return refEventID;
    }

    /**
     * Sets the ref event ID.
     *
     * @param refEventID
     *            the new ref event ID
     */
    public void setRefEventID(int refEventID){
        this.refEventID = refEventID;
    }

}