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

import java.util.List;

import org.springframework.stereotype.Component;

/**
 * The Class NodeWrapper.
 */
@Component
public class NodeWrapper {

    /** The FDN name. */
    private String fdnName;

    /** The node name. */
    private String nodeName;

    /** The enb id. */
    private int enbID;

    /** The mnc. */
    private String mnc;

    /** The mcc. */
    private String mcc;

    /** The cell details. */
    private List<CellWrapper> cellWrapperList;

    /** The neighbor node. */
    private List<NodeWrapper> neighboreNodeBList; // For EVENT_PARAM_GENBID

    /** The neighbor mme list. */
    private List<MmeNodeWrapper> neighborMmeList; // For TermPointToMMe

    /** The event param GENBID. */
    private byte[] eventParamGENBID;

    /**
     * Gets the fdn name.
     *
     * @return the fdn name
     */
    public String getFdnName(){
        return fdnName;
    }

    /**
     * Sets the fdn name.
     *
     * @param fdnName
     *            the new fdn name
     */
    public void setFdnName(String fdnName){
        this.fdnName = fdnName;
    }

    /**
     * Gets the enb ID.
     *
     * @return the enb ID
     */
    public int getEnbID(){
        return enbID;
    }

    /**
     * Sets the enb ID.
     *
     * @param enbID
     *            the new enb ID
     */
    public void setEnbID(int enbID){
        this.enbID = enbID;
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
     * Gets the node name.
     *
     * @return the node name
     */
    public String getNodeName(){
        return nodeName;
    }

    /**
     * Sets the node name.
     *
     * @param nodeName
     *            the new node name
     */
    public void setNodeName(String nodeName){
        this.nodeName = nodeName;
    }

    /**
     * Gets the cell wrapper list.
     *
     * @return the cell wrapper list
     */
    public List<CellWrapper> getCellWrapperList(){
        return cellWrapperList;
    }

    /**
     * Sets the cell wrapper list.
     *
     * @param cellWrapperList
     *            the new cell wrapper list
     */
    public void setCellWrapperList(List<CellWrapper> cellWrapperList){
        this.cellWrapperList = cellWrapperList;
    }

    /**
     * Gets the neighbore node B list.
     *
     * @return the neighbore node B list
     */
    public List<NodeWrapper> getNeighboreNodeBList(){
        return neighboreNodeBList;
    }

    /**
     * Sets the neighbore node B list.
     *
     * @param neighboreNodeBList
     *            the new neighbore node B list
     */
    public void setNeighboreNodeBList(List<NodeWrapper> neighboreNodeBList){
        this.neighboreNodeBList = neighboreNodeBList;
    }

    /**
     * Gets the neighbor mme list.
     *
     * @return the neighbor mme list
     */
    public List<MmeNodeWrapper> getNeighborMmeList(){
        return neighborMmeList;
    }

    /**
     * Sets the neighbor mme list.
     *
     * @param neighborMmeList
     *            the new neighbor mme list
     */
    public void setNeighborMmeList(List<MmeNodeWrapper> neighborMmeList){
        this.neighborMmeList = neighborMmeList;
    }

    /**
     * Gets the event param GENBID.
     *
     * @return the event param GENBID
     */
    public byte[] getEventParamGENBID(){
        return eventParamGENBID;
    }

    /**
     * Sets the event param GENBID.
     *
     * @param eventParamGENBID
     *            the new event param GENBID
     */
    public void setEventParamGENBID(byte[] eventParamGENBID){
        this.eventParamGENBID = eventParamGENBID;
    }

}
