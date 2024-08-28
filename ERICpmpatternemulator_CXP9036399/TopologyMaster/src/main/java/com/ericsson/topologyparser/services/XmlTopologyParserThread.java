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

package com.ericsson.topologyparser.services;

import static com.ericsson.configmaster.constants.Constants.COMMA;
import static com.ericsson.configmaster.constants.Constants.EQUAL;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ericsson.topologyenricher.services.TopologyEnricher;
import com.ericsson.topologymaster.controller.TopologyMasterController;
import com.ericsson.topologyparser.autogen.Mo;
import com.ericsson.topologyparser.autogen.Model;
import com.ericsson.topologyparser.entities.CellWrapper;
import com.ericsson.topologyparser.entities.NodeWrapper;

/**
 * The Class TopologyParserChild.
 */
@Component
public class XmlTopologyParserThread implements Runnable {

    /** The topology file list. */
    private List<File> topologyFileList;

    /** The topology parser util. */
    @Autowired
    private TopologyParserUtil topologyParserUtil;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The mo parser. */
    @Autowired
    private MOParser moParser;

    /** The node wrapper obj. */
    @SuppressWarnings("unused")
    @Autowired
    private NodeWrapper nodeWrapperObj;

    /** The cell wrapper obj. */
    @SuppressWarnings("unused")
    @Autowired
    private CellWrapper cellWrapperObj;

    /** The topology enricher. */
    @Autowired
    private TopologyEnricher topologyEnricher;

    /** The node wrapper map. */
    private Map<String, NodeWrapper> nodeWrapperMap;

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run(){
        nodeWrapperMap = new HashMap<String, NodeWrapper>();
        for(File topologyFile : topologyFileList){
            processXML(topologyParserUtil.getXMLObject(topologyFile));
        }
        for(String fdn : nodeWrapperMap.keySet()){
            topologyEnricher.enrichTopology(nodeWrapperMap.get(fdn));
        }
        TopologyMasterController.getNodeWrapperMap().putAll(nodeWrapperMap);
    }

    /**
     * Process XML.
     *
     * @param model
     *            the model
     */
    private void processXML(Model model){
        if(null != model){
            NodeWrapper nodeInfo = applicationContext.getBean(NodeWrapper.class);
            nodeInfo.setFdnName(model.getMo().get(0).getFdn());
            List<CellWrapper> cellWrapperList = new ArrayList<>();

            // Fetch enodeB name from first MO
            for(int count = 0; count < model.getMo().get(0).getAttr().size(); count++){
                if(model.getMo().get(0).getAttr().get(count).getName().equals("userLabel")){
                    nodeInfo.setNodeName(model.getMo().get(0).getAttr().get(count).getContent().get(0).toString());
                    break;
                }
            }

            // Fetch Cell informations from remaining MO's
            for(int index = 1; index < model.getMo().size(); index++){
                Mo mo = model.getMo().get(index);
                String token[] = mo.getFdn().split(COMMA);
                String moClass = token[token.length - 1].split(EQUAL)[0];

                // This will fetch EnodeB id and MNC and MCC attributes
                if("ENodeBFunction".equals(moClass)){
                    topologyParserUtil.getEnodeBPlmnAtrributesValue(mo,nodeInfo);
                }

                // Get Cell Relation details
                if("EUtranCellFDD".equals(moClass) || "EUtranCellTDD".equals(moClass)){
                    CellWrapper cellWrapper = getCellWrapperObject(model,mo,nodeInfo);
                    if(null != cellWrapper){
                        cellWrapperList.add(cellWrapper);
                    }
                }
            }
            nodeInfo.setCellWrapperList(cellWrapperList);
            topologyParserUtil.findNodeRelationDetails(model,nodeInfo);
            nodeWrapperMap.put(nodeInfo.getFdnName(),nodeInfo);
        }
    }

    /**
     * Gets the cell wrapper object.
     *
     * @param model
     *            the model
     * @param mo
     *            the mo
     * @param nodeInfo
     *            the node info
     * @return the cell wrapper object
     */
    private CellWrapper getCellWrapperObject(Model model, Mo mo, NodeWrapper nodeInfo){
        CellWrapper cellWrapper = null;
        Map<String, String> attributeDetails = moParser.getEUtranCellFDD(mo);
        if(!attributeDetails.isEmpty()){
            cellWrapper = applicationContext.getBean(CellWrapper.class);
            cellWrapper.setEnodeBID(nodeInfo.getEnbID());
            cellWrapper.setFdn(nodeInfo.getFdnName());
            cellWrapper.setCellID(Integer.parseInt(attributeDetails.get("cellId")));
            cellWrapper.setUserLabel(attributeDetails.get("EUtranCellFDDId"));
            topologyParserUtil.findCellRelationDetails(model,cellWrapper);

        }
        return cellWrapper;
    }

    /**
     * Sets the topology file list.
     *
     * @param topologyFileList
     *            the new topology file list
     */
    public void setTopologyFileList(List<File> topologyFileList){
        this.topologyFileList = topologyFileList;
    }

}
