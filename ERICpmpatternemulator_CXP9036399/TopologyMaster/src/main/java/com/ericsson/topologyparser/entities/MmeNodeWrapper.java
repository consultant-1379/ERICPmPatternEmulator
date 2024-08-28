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

package com.ericsson.topologyparser.entities;

import org.springframework.stereotype.Component;

/**
 * The Class MMENodeWrapper.
 */
@Component
public class MmeNodeWrapper {

    /** The mme gi. */
    private int mmeGI;

    /** The mme ci. */
    private int mmeCI;

    /** The mnc. */
    private String mnc;

    /** The mcc. */
    private String mcc;

    /** The event param GUMMEI. */
    private byte[] eventParamGUMMEI;

    /**
     * Gets the mme gi.
     *
     * @return the mme gi
     */
    public int getMmeGI(){
        return mmeGI;
    }

    /**
     * Sets the mme gi.
     *
     * @param mmeGI
     *            the new mme gi
     */
    public void setMmeGI(int mmeGI){
        this.mmeGI = mmeGI;
    }

    /**
     * Gets the mme ci.
     *
     * @return the mme ci
     */
    public int getMmeCI(){
        return mmeCI;
    }

    /**
     * Sets the mme ci.
     *
     * @param mmeCI
     *            the new mme ci
     */
    public void setMmeCI(int mmeCI){
        this.mmeCI = mmeCI;
    }

    /**
     * Gets the mnc.
     *
     * @return the mnc
     */
    public String getMnc(){
        return mnc;
    }

    /**
     * Sets the mnc.
     *
     * @param mnc
     *            the new mnc
     */
    public void setMnc(String mnc){
        this.mnc = mnc;
    }

    /**
     * Gets the mcc.
     *
     * @return the mcc
     */
    public String getMcc(){
        return mcc;
    }

    /**
     * Sets the mcc.
     *
     * @param mcc
     *            the new mcc
     */
    public void setMcc(String mcc){
        this.mcc = mcc;
    }

    /**
     * Gets the event param GUMMEI.
     *
     * @return the eventParamGUMMEI
     */
    public byte[] getEventParamGUMMEI(){
        return eventParamGUMMEI;
    }

    /**
     * Sets the event param GUMMEI.
     *
     * @param eventParamGUMMEI
     *            the eventParamGUMMEI to set
     */
    public void setEventParamGUMMEI(byte[] eventParamGUMMEI){
        this.eventParamGUMMEI = eventParamGUMMEI;
    }

}
