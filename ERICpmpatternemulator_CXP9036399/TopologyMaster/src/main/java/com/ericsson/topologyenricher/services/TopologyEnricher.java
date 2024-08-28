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

package com.ericsson.topologyenricher.services;

import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_GENBID;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_GLOBAL_CELL_ID;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_GUMMEI;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_NEIGHBOR_CGI;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.appconfig.autogen.AppConfiguration.DataSource.ComplexParameters.Parameter.Component;
import com.ericsson.configmaster.services.AppConfigLoader;
import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.BytePackDecoder;
import com.ericsson.schemamaster.services.BytePackEncoder;
import com.ericsson.topologyparser.entities.CellRelationWrapper;
import com.ericsson.topologyparser.entities.CellWrapper;
import com.ericsson.topologyparser.entities.MmeNodeWrapper;
import com.ericsson.topologyparser.entities.NodeWrapper;
import com.ericsson.utilities.services.Utils;

/**
 * The Class TopologyEnricher.
 */
@Service
public class TopologyEnricher {

    /** The app config loader. */
    @Autowired
    private AppConfigLoader appConfigLoader;

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /** The byte pack encoder. */
    @Autowired
    private BytePackEncoder bytePackEncoder;

    /** The byte pack decoder. */
    @Autowired
    private BytePackDecoder bytePackDecoder;

    /**
     * Enrich topology.
     *
     * @param nodeWrapper
     *            the node wrapper
     */
    public void enrichTopology(NodeWrapper nodeWrapper){
        Map<String, Map<Integer, Component>> complexParam = appConfigLoader.getAppConfigDatasourceWrappereMap().get(streamingConfigLoader
                .getDatasource()).getComplexParameterInfoMap();
        for(String componentName : complexParam.keySet()){
            switch(componentName){
            case EVENT_PARAM_GLOBAL_CELL_ID:
                enrichEventParamGlobalCellID(nodeWrapper.getCellWrapperList(),complexParam.get(componentName));
                break;
            case EVENT_PARAM_NEIGHBOR_CGI:
                enrichEventParamNeighborCGI(nodeWrapper,complexParam.get(componentName));
                break;
            case EVENT_PARAM_GUMMEI:
                enrichEventParamGUMMEI(nodeWrapper,complexParam.get(componentName));
                break;
            case EVENT_PARAM_GENBID:
                enrichEventParamGENBID(nodeWrapper.getNeighboreNodeBList(),complexParam.get(componentName));
                break;
            }
        }
    }

    /**
     * Enrich event param global cell ID.
     *
     * @param cellWrapperList
     *            the cell wrapper list
     * @param complexParam
     *            the complex param
     */
    private void enrichEventParamGlobalCellID(List<CellWrapper> cellWrapperList, Map<Integer, Component> complexParam){
        for(CellWrapper cellWrapperObj : cellWrapperList){
            String binaryData = Utils.numToBinary(cellWrapperObj.getEnodeBID(),complexParam.get(1).getLengthBits());
            binaryData += Utils.numToBinary(cellWrapperObj.getCellID(),complexParam.get(2).getLengthBits());
            long data = Long.parseLong(binaryData,2);
            byte[] eventParamGlobalCellID = bytePackEncoder.convertLongValueToByteArray(data,complexParam.get(-1).getLengthBits());
            cellWrapperObj.setEventParamGlobalCellID(eventParamGlobalCellID);
        }
    }

    /**
     * Enrich event param neighbor CGI.
     *
     * @param nodeWrapper
     *            the node wrapper
     * @param complexParam
     *            the complex param
     */
    private void enrichEventParamNeighborCGI(NodeWrapper nodeWrapper, Map<Integer, Component> complexParam){
        List<CellWrapper> cellWrapperList = nodeWrapper.getCellWrapperList();
        for(CellWrapper cellWrapperObj : cellWrapperList){
            enrichEutranCellRelation(cellWrapperObj.getinternalEutranRelations(),complexParam,nodeWrapper.getMcc(),nodeWrapper.getMnc());
            enrichEutranCellRelation(cellWrapperObj.getExternaleutranRelations(),complexParam,null,null);
            enrichGeranCellRelation(cellWrapperObj.getGeranRelations(),complexParam);
            enrichUtranCellRelation(cellWrapperObj.getUtranRelations(),complexParam);
        }
    }

    /**
     * Enrich eutran cell relation.
     *
     * @param cellRelation
     *            the cell relation
     * @param complexParam
     *            the complex param
     * @param mcc
     *            the mcc
     * @param mnc
     *            the mnc
     */
    private void enrichEutranCellRelation(List<CellRelationWrapper> cellRelation, Map<Integer, Component> complexParam, String mcc, String mnc){
        for(CellRelationWrapper cellInfo : cellRelation){
            String binaryData = Utils.numToBinary(0,complexParam.get(0).getLengthBits());
            if(null == mcc || null == mnc){
                binaryData += bytePackEncoder.handlePlmnID(cellInfo.getMcc(),cellInfo.getMnc());
            } else{
                binaryData += bytePackEncoder.handlePlmnID(mcc,mnc);
            }
            binaryData += Utils.numToBinary(cellInfo.getReferenceID(),complexParam.get(4).getLengthBits());
            binaryData += Utils.numToBinary(cellInfo.getCellID(),complexParam.get(5).getLengthBits());
            binaryData += Utils.numToBinary(0,complexParam.get(6).getLengthBits());
            long data = Long.parseLong(binaryData,2);
            byte[] eventParamNeighborCGI = bytePackEncoder.convertLongValueToByteArray(data,complexParam.get(-1).getLengthBits());
            cellInfo.setEventParamNeighborCGI(eventParamNeighborCGI);
        }
    }

    /**
     * Enrich geran cell relation.
     *
     * @param cellRelation
     *            the cell relation
     * @param complexParam
     *            the complex param
     */
    private void enrichGeranCellRelation(List<CellRelationWrapper> cellRelation, Map<Integer, Component> complexParam){
        for(CellRelationWrapper cellRelationWrapper : cellRelation){
            String binaryData = Utils.numToBinary(0,complexParam.get(0).getLengthBits());
            binaryData += bytePackEncoder.handlePlmnID(cellRelationWrapper.getMcc(),cellRelationWrapper.getMnc());
            binaryData += Utils.numToBinary(cellRelationWrapper.getReferenceID(),complexParam.get(9).getLengthBits());
            binaryData += Utils.numToBinary(cellRelationWrapper.getCellID(),complexParam.get(10).getLengthBits());
            long data = Long.parseLong(binaryData,2);
            byte[] eventParamNeighborCGI = bytePackEncoder.convertLongValueToByteArray(data,complexParam.get(-1).getLengthBits());
            cellRelationWrapper.setEventParamNeighborCGI(eventParamNeighborCGI);
        }
    }

    /**
     * Enrich utran cell relation.
     *
     * @param cellRelation
     *            the cell relation
     * @param complexParam
     *            the complex param
     */
    private void enrichUtranCellRelation(List<CellRelationWrapper> cellRelation, Map<Integer, Component> complexParam){
        for(CellRelationWrapper cellRelationWrapper : cellRelation){
            String binaryData = Utils.numToBinary(0,complexParam.get(0).getLengthBits());
            binaryData += bytePackEncoder.handlePlmnID(cellRelationWrapper.getMcc(),cellRelationWrapper.getMnc());
            binaryData += Utils.numToBinary(cellRelationWrapper.getReferenceID(),complexParam.get(7).getLengthBits());
            binaryData += Utils.numToBinary(cellRelationWrapper.getCellID(),complexParam.get(8).getLengthBits());
            binaryData += Utils.numToBinary(0,complexParam.get(6).getLengthBits());
            long data = Long.parseLong(binaryData,2);
            byte[] eventParamNeighborCGI = bytePackEncoder.convertLongValueToByteArray(data,complexParam.get(-1).getLengthBits());
            cellRelationWrapper.setEventParamNeighborCGI(eventParamNeighborCGI);
        }
    }

    /**
     * Enrich event param GUMMEI.
     *
     * @param nodeWrapper
     *            the node wrapper
     * @param complexParam
     *            the complex param
     */
    private void enrichEventParamGUMMEI(NodeWrapper nodeWrapper, Map<Integer, Component> complexParam){
        for(MmeNodeWrapper mme : nodeWrapper.getNeighborMmeList()){
            String binaryData = Utils.numToBinary(0,complexParam.get(0).getLengthBits());
            binaryData += bytePackEncoder.handlePlmnID(mme.getMcc(),mme.getMnc());
            binaryData += Utils.numToBinary(mme.getMmeGI(),complexParam.get(5).getLengthBits());
            binaryData += Utils.numToBinary(mme.getMmeCI(),complexParam.get(6).getLengthBits());
            long data = Long.parseLong(binaryData,2);
            byte[] eventParamGummei = bytePackEncoder.convertLongValueToByteArray(data,complexParam.get(-1).getLengthBits());
            mme.setEventParamGUMMEI(eventParamGummei);
        }
    }

    /**
     * Enrich event param GENBID.
     *
     * @param gnbIDList
     *            the gnb ID list
     * @param complexParam
     *            the complex param
     */
    private void enrichEventParamGENBID(List<NodeWrapper> gnbIDList, Map<Integer, Component> complexParam){
        for(NodeWrapper termPointEnb : gnbIDList){
            String binaryData = Utils.numToBinary(0,complexParam.get(0).getLengthBits());
            binaryData += bytePackEncoder.handlePlmnID(termPointEnb.getMcc(),termPointEnb.getMnc());
            binaryData += Utils.numToBinary(termPointEnb.getEnbID(),complexParam.get(4).getLengthBits());
            binaryData += Utils.numToBinary(0,complexParam.get(5).getLengthBits());
            binaryData += Utils.numToBinary(0,complexParam.get(6).getLengthBits());
            long data = Long.parseLong(binaryData,2);
            byte[] eventParamGenbId = bytePackEncoder.convertLongValueToByteArray(data,complexParam.get(-1).getLengthBits());
            termPointEnb.setEventParamGENBID(eventParamGenbId);
        }
    }

    /**
     * Sets the topology parameters value.
     *
     * @param eventData
     *            the event data
     * @param schemaReleaseObj
     *            the schema release obj
     * @param eventParamGenBID
     *            the event param gen BID
     * @param cellWrapper
     *            the cell wrapper
     * @param cellRelation
     *            the cell relation
     * @param mmeNodeWrapper
     *            the mme node wrapper
     * @return the byte buffer
     */
    public byte[] setTopologyParametersValue(int eventID, byte[] eventData, SchemaRelease schemaReleaseObj, byte[] eventParamGenBID,
            CellWrapper cellWrapper, CellRelationWrapper cellRelation, MmeNodeWrapper mmeNodeWrapper){
        byte[] updatedEventData = eventData.clone();
        int startIndex = 7;
        for(String paramName : schemaReleaseObj.getEventIDMap().get(eventID).getParameterMap().keySet()){
            int paramElementCalculatedLength = bytePackDecoder.calculateTotalParamLength(schemaReleaseObj.getParameterMap().get(paramName),
                    schemaReleaseObj.getParameterMap(),startIndex,updatedEventData);
            switch(paramName){
            case EVENT_PARAM_GLOBAL_CELL_ID:
                System.arraycopy(cellWrapper.getEventParamGlobalCellID(),0,updatedEventData,startIndex,paramElementCalculatedLength);
                break;
            case EVENT_PARAM_GUMMEI:
                if(null != mmeNodeWrapper){
                    System.arraycopy(mmeNodeWrapper.getEventParamGUMMEI(),0,updatedEventData,startIndex,paramElementCalculatedLength);
                }
                break;
            case EVENT_PARAM_NEIGHBOR_CGI:
                if(null != cellRelation){
                    System.arraycopy(cellRelation.getEventParamNeighborCGI(),0,updatedEventData,startIndex,paramElementCalculatedLength);
                }
                break;
            case EVENT_PARAM_GENBID:
                if(null != eventParamGenBID){
                    System.arraycopy(eventParamGenBID,0,updatedEventData,startIndex,paramElementCalculatedLength);
                }
                break;
            }
            startIndex += paramElementCalculatedLength;
        }
        return updatedEventData;

    }
}
