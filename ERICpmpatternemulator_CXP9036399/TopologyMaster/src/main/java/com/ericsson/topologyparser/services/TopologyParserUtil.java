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
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.EXTERNAL_EUTRAN;
import static com.ericsson.configmaster.constants.Constants.INTERNAL_EUTRAN;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.ericsson.topologyparser.autogen.Mo;
import com.ericsson.topologyparser.autogen.Model;
import com.ericsson.topologyparser.entities.CellRelationWrapper;
import com.ericsson.topologyparser.entities.CellWrapper;
import com.ericsson.topologyparser.entities.MmeNodeWrapper;
import com.ericsson.topologyparser.entities.NodeWrapper;
import com.ericsson.utilities.services.Utils;

/**
 * The Class ParserHelper.
 */
@Service
public class TopologyParserUtil {

    /** The mo parser. */
    @Autowired
    private MOParser moParser;

    /** The m ME node wrapper obj. */
    @SuppressWarnings("unused")
    @Autowired
    private MmeNodeWrapper mMENodeWrapperObj;

    /** The node wrapper obj. */
    @SuppressWarnings("unused")
    @Autowired
    private NodeWrapper nodeWrapperObj;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Gets the XML object.
     *
     * @param topologyFile
     *            the topology file
     * @return the XML object
     */
    public Model getXMLObject(File topologyFile){
        try{
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
            spf.setFeature("http://xml.org/sax/features/validation",false);
            spf.setNamespaceAware(true); // Binding attributes
            EntityResolver entityResolver = new EntityResolver() {
                @Override
                public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException{
                    final InputSource saxIs = new InputSource(new ByteArrayInputStream("".getBytes()));
                    return saxIs;
                }
            };
            XMLReader xmlReader = spf.newSAXParser().getXMLReader();
            xmlReader.setEntityResolver(entityResolver);
            SAXSource source = new SAXSource(xmlReader, new InputSource(new FileInputStream(topologyFile)));
            JAXBContext jaxbContext = JAXBContext.newInstance(Model.class);
            Unmarshaller um = jaxbContext.createUnmarshaller();
            return (Model) um.unmarshal(source);
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occured while parsing xml topology file. " + e);
            return null;
        }
    }

    /**
     * Gets the enode B plmn atrributes value.
     *
     * @param mo
     *            the mo
     * @param nodeInfo
     *            the node info
     * @return the enode B plmn atrributes value
     */
    public void getEnodeBPlmnAtrributesValue(Mo mo, NodeWrapper nodeInfo){
        for(int count = 0; count < mo.getAttr().size(); count++){
            if(mo.getAttr().get(count).getName().equalsIgnoreCase("eNodeBPlmnId")){
                Map<String, String> mccMncDetailsMap = new HashMap<String, String>(2);
                moParser.getMccMnc(mo.getAttr().get(count).getContent(),mccMncDetailsMap);
                nodeInfo.setMcc(mccMncDetailsMap.get("mcc"));
                nodeInfo.setMnc(mccMncDetailsMap.get("mnc"));
            } else if(mo.getAttr().get(count).getName().equalsIgnoreCase("eNBId")){
                nodeInfo.setEnbID(Integer.parseInt(mo.getAttr().get(count).getContent().get(0).toString()));
            }
        }
    }

    /**
     * Find cell relation details.
     *
     * @param model
     *            the model
     * @param cellWrapper
     *            the cell wrapper
     */
    public void findCellRelationDetails(Model model, CellWrapper cellWrapper){
        Map<String, List<CellRelationWrapper>> cellRelationObjMap = new HashMap<String, List<CellRelationWrapper>>();
        cellRelationObjMap.put(INTERNAL_EUTRAN,new ArrayList<CellRelationWrapper>());
        cellRelationObjMap.put(EXTERNAL_EUTRAN,new ArrayList<CellRelationWrapper>());

        List<CellRelationWrapper> utranList = new ArrayList<CellRelationWrapper>();
        List<CellRelationWrapper> geranList = new ArrayList<CellRelationWrapper>();

        for(int index = 1; index < model.getMo().size(); index++){
            String token[] = model.getMo().get(index).getFdn().split(COMMA);
            if(model.getMo().get(index).getFdn().contains("EUtranCellFDD=" + cellWrapper.getUserLabel()) || model.getMo().get(index).getFdn()
                    .contains("EUtranCellTDD=" + cellWrapper.getUserLabel())){
                String relationType = token[token.length - 1].split(EQUAL)[0];
                // EUtran Relations
                if("EUtranCellRelation".equalsIgnoreCase(relationType)){
                    moParser.getEUtranCellRelation(model.getMo().get(index),model.getMo(),cellWrapper,cellRelationObjMap);
                }
                // Utran Relations
                else if("UtranCellRelation".equalsIgnoreCase(relationType)){
                    CellRelationWrapper cellWrapperObj = moParser.getUtranCellRelation(model.getMo().get(index),model.getMo());
                    if(null != cellWrapperObj){
                        utranList.add(cellWrapperObj);
                    }
                }
                // Geran Relations
                else if("GeranCellRelation".equalsIgnoreCase(relationType)){
                    CellRelationWrapper cellWrapperObj = moParser.getGeranRelation(model.getMo().get(index),model.getMo());
                    if(null != cellWrapperObj){
                        geranList.add(cellWrapperObj);
                    }
                }
            }
        }
        cellRelationObjMap.put("UTRAN",utranList);
        cellRelationObjMap.put("GERAN",geranList);
        setCellRelations(cellRelationObjMap,cellWrapper);
    }

    /**
     * Sets the cell relations.
     *
     * @param cellRelationObjMap
     *            the cell relation obj map
     * @param cellWrapper
     *            the cell wrapper
     */
    private void setCellRelations(Map<String, List<CellRelationWrapper>> cellRelationObjMap, CellWrapper cellWrapper){
        for(String mode : cellRelationObjMap.keySet()){
            if(mode.equals(INTERNAL_EUTRAN)){
                cellWrapper.setinternalEutranRelations(cellRelationObjMap.get(mode));
            } else if(mode.equals(EXTERNAL_EUTRAN)){
                cellWrapper.setExternaleutranRelations(cellRelationObjMap.get(mode));
            } else if(mode.equals("UTRAN")){
                cellWrapper.setUtranRelations(cellRelationObjMap.get(mode));
            } else if(mode.equals("GERAN")){
                cellWrapper.setGeranRelations(cellRelationObjMap.get(mode));
            }
        }
    }

    /**
     * Find node relation details.
     *
     * @param model
     *            the model
     * @param nodeInfo
     *            the node info
     */
    public void findNodeRelationDetails(Model model, NodeWrapper nodeInfo){
        List<NodeWrapper> termPointToEnbList = new ArrayList<>();
        List<MmeNodeWrapper> termPointToMmeList = new ArrayList<>();

        for(int index = 0; index < model.getMo().size(); index++){
            String token[] = model.getMo().get(index).getFdn().split(COMMA);
            String nodeRelationType = token[token.length - 1].split(EQUAL)[0];
            if("TermPointToENB".equalsIgnoreCase(nodeRelationType)){
                Map<String, String> getTerm = moParser.getTermPointEnB(model.getMo().get(index),model.getMo());
                NodeWrapper termNw = applicationContext.getBean(NodeWrapper.class);
                termNw.setNodeName(token[token.length - 2].split(EQUAL)[1]);
                termNw.setEnbID(Integer.parseInt(getTerm.get("eNBId")));
                termNw.setMcc(getTerm.get("mcc"));
                termNw.setMnc(getTerm.get("mnc"));
                termPointToEnbList.add(termNw);
            } else if("TermPointToMme".equalsIgnoreCase(nodeRelationType)){
                Map<String, String> mmeTermpPointValuesMap = moParser.getMmeTermPoint(model.getMo().get(index));
                if(Integer.parseInt(mmeTermpPointValuesMap.get("mcc")) == 0 || Integer.parseInt(mmeTermpPointValuesMap.get("mnc")) == 0){
                    continue;
                }
                MmeNodeWrapper mmeNw = applicationContext.getBean(MmeNodeWrapper.class);
                mmeNw.setMcc(mmeTermpPointValuesMap.get("mcc"));
                mmeNw.setMnc(mmeTermpPointValuesMap.get("mnc"));
                mmeNw.setMmeCI(Integer.parseInt(mmeTermpPointValuesMap.get("mmeCodeListLTERelated")));
                mmeNw.setMmeGI(Integer.parseInt(mmeTermpPointValuesMap.get("mmeGIListLTERelated")));
                termPointToMmeList.add(mmeNw);
            }
        }
        nodeInfo.setNeighboreNodeBList(termPointToEnbList);
        nodeInfo.setNeighborMmeList(termPointToMmeList);
    }
}
