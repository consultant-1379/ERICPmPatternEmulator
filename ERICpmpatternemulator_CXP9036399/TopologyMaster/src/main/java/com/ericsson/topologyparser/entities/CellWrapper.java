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

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * The Class CellWrapper.
 */
@Component
public class CellWrapper {

    /** The user label. */
    private String userLabel;

    /** The cell id. */
    private int cellID;

    /** The enode bid. */
    private int enodeBID;

    /** The internaleutranRelations . */
    private List<CellRelationWrapper> internaleutranRelations;

    /** The externaleutranRelations . */
    private List<CellRelationWrapper> externaleutranRelations;

    /** The utran relations. */
    private List<CellRelationWrapper> utranRelations;

    /** The geran relations. */
    private List<CellRelationWrapper> geranRelations;

    /** The event param global cell ID. */
    private byte[] eventParamGlobalCellID;

    /** The fdn. */
    private String fdn;

    /**
     * Gets the user label.
     *
     * @return the user label
     */
    public String getUserLabel(){
        return userLabel;
    }

    /**
     * Sets the user label.
     *
     * @param userLabel
     *            the new user label
     */
    public void setUserLabel(String userLabel){
        this.userLabel = userLabel;
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
     * Gets the enode bid.
     *
     * @return the enode bid
     */
    public int getEnodeBID(){
        return enodeBID;
    }

    /**
     * Sets the enode bid.
     *
     * @param enodeBID
     *            the new enode bid
     */
    public void setEnodeBID(int enodeBID){
        this.enodeBID = enodeBID;
    }

    /**
     * Gets the eutran relations.
     *
     * @return the eutran relations
     */
    public List<CellRelationWrapper> getinternalEutranRelations(){
        if(internaleutranRelations == null){
            internaleutranRelations = new ArrayList<>();
        }
        return internaleutranRelations;
    }

    /**
     * Sets the eutran relations.
     *
     * @param eutranRelations
     *            the new eutran relations
     */
    public void setinternalEutranRelations(List<CellRelationWrapper> eutranRelations){
        this.internaleutranRelations = eutranRelations;
    }

    /**
     * Gets the externaleutran relations.
     *
     * @return the externaleutranRelations
     */
    public List<CellRelationWrapper> getExternaleutranRelations(){
        if(externaleutranRelations == null){
            externaleutranRelations = new ArrayList<>();
        }
        return externaleutranRelations;
    }

    /**
     * Sets the externaleutran relations.
     *
     * @param externaleutranRelations
     *            the externaleutranRelations to set
     */
    public void setExternaleutranRelations(List<CellRelationWrapper> externaleutranRelations){
        this.externaleutranRelations = externaleutranRelations;
    }

    /**
     * Gets the utran relations.
     *
     * @return the utran relations
     */
    public List<CellRelationWrapper> getUtranRelations(){
        if(utranRelations == null){
            utranRelations = new ArrayList<>();
        }
        return utranRelations;
    }

    /**
     * Sets the utran relations.
     *
     * @param utranRelations
     *            the new utran relations
     */
    public void setUtranRelations(List<CellRelationWrapper> utranRelations){
        this.utranRelations = utranRelations;
    }

    /**
     * Gets the geran relations.
     *
     * @return the geran relations
     */
    public List<CellRelationWrapper> getGeranRelations(){
        if(geranRelations == null){
            geranRelations = new ArrayList<>();
        }
        return geranRelations;
    }

    /**
     * Sets the geran relations.
     *
     * @param geranRelations
     *            the new geran relations
     */
    public void setGeranRelations(List<CellRelationWrapper> geranRelations){
        this.geranRelations = geranRelations;
    }

    /**
     * Gets the event param global cell ID.
     *
     * @return the eventParamGlobalCellID
     */
    public byte[] getEventParamGlobalCellID(){
        return eventParamGlobalCellID;
    }

    /**
     * Sets the event param global cell ID.
     *
     * @param eventParamGlobalCellID
     *            the eventParamGlobalCellID to set
     */
    public void setEventParamGlobalCellID(byte[] eventParamGlobalCellID){
        this.eventParamGlobalCellID = eventParamGlobalCellID;
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

}
