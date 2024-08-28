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
 * The Class CellRelationWrapper.
 */
@Component
public class CellRelationWrapper {

    /** The type of relation. */
    private int typeOfRelation; // 1 for EUtran,2 for Utran and 3 for Geran

    /** The reference id. */
    private int referenceID; // eNodeBID or RNC ID or LAC // Depends on type of relation

    /** The cell id. */
    private int cellID;

    /** The event param neighbor CGI. */
    private byte[] eventParamNeighborCGI;

    /** The mcc. */
    private String mcc;

    /** The mnc. */
    private String mnc;

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
     * Gets the reference id.
     *
     * @return the reference id
     */
    public int getReferenceID(){
        return referenceID;
    }

    /**
     * Sets the reference id.
     *
     * @param referenceID
     *            the new reference id
     */
    public void setReferenceID(int referenceID){
        this.referenceID = referenceID;
    }

    /**
     * Gets the cell id.
     *
     * @return the cell id
     */
    public int getCellID(){
        return cellID;
    }

    /**
     * Sets the cell id.
     *
     * @param cellID
     *            the new cell id
     */
    public void setCellID(int cellID){
        this.cellID = cellID;
    }

    /**
     * Gets the event param neighbor CGI.
     *
     * @return the eventParamNeighborCGI
     */
    public byte[] getEventParamNeighborCGI(){
        return eventParamNeighborCGI;
    }

    /**
     * Sets the event param neighbor CGI.
     *
     * @param eventParamNeighborCGI
     *            the eventParamNeighborCGI to set
     */
    public void setEventParamNeighborCGI(byte[] eventParamNeighborCGI){
        this.eventParamNeighborCGI = eventParamNeighborCGI;
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
     *            the mcc to set
     */
    public void setMcc(String mcc){
        this.mcc = mcc;
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
     *            the mnc to set
     */
    public void setMnc(String mnc){
        this.mnc = mnc;
    }

}
