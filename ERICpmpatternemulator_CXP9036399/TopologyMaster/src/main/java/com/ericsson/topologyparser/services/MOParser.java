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
import static com.ericsson.configmaster.constants.Constants.HYPHEN;
import static com.ericsson.configmaster.constants.Constants.INTERNAL_EUTRAN;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.topologyparser.autogen.Attr;
import com.ericsson.topologyparser.autogen.Item;
import com.ericsson.topologyparser.autogen.Mo;
import com.ericsson.topologyparser.autogen.Seq;
import com.ericsson.topologyparser.autogen.Struct;
import com.ericsson.topologyparser.entities.CellRelationWrapper;
import com.ericsson.topologyparser.entities.CellWrapper;
import com.ericsson.utilities.services.Utils;

/**
 * The Class MOParser.
 */
@Service
public class MOParser {

    /** The application context. */
    @Autowired
    ApplicationContext applicationContext;

    /**
     * Gets the e utran cell FDD.
     *
     * @param mo
     *            the mo
     * @return the e utran cell FDD
     */
    public Map<String, String> getEUtranCellFDD(Mo mo){
        Map<String, String> attributeDetails = new HashMap<String, String>();
        for(int index = 0; index < mo.getAttr().size(); index++){
            if(mo.getAttr().get(index).getName().equalsIgnoreCase("cellId")){
                attributeDetails.put("cellId",mo.getAttr().get(index).getContent().get(0).toString());
            } else if(mo.getAttr().get(index).getName().equalsIgnoreCase("EUtranCellFDDId")){
                attributeDetails.put("EUtranCellFDDId",mo.getAttr().get(index).getContent().get(0).toString());
            }
        }
        return attributeDetails;
    }

    /**
     * Gets the e utran cell relation.
     *
     * @param mo
     *            the mo
     * @param moList
     *            the mo list
     * @param cellWrapper
     *            the cell wrapper
     * @param cellRelationObjMap
     *            the cell relation obj map
     * @return the e utran cell relation
     */
    public void getEUtranCellRelation(Mo mo, List<Mo> moList, CellWrapper cellWrapper, Map<String, List<CellRelationWrapper>> cellRelationObjMap){
        String refFDN = getRefFDN(mo,"neighborCellRef");
        Mo refMo = getRefMo(moList,refFDN);
        if(null != refFDN && null != refMo){
            Map<String, String> eUtranCellRelationParams = new HashMap<String, String>();
            boolean isInternalRelationFlag = false;

            if(refFDN.contains("ExternalENodeBFunction")){
                String node = null;
                String[] nodeName = refMo.getFdn().split(COMMA);
                for(String index : nodeName){
                    if(index.contains("ExternalENodeBFunction")){
                        node = index.split(EQUAL)[1];
                    }
                }
                for(Mo moInfo : moList){
                    String token[] = moInfo.getFdn().split(COMMA);
                    if(token[token.length - 1].equalsIgnoreCase("ExternalENodeBFunction=" + node)){
                        for(Attr at : moInfo.getAttr()){
                            if(at.getName().equalsIgnoreCase("eNBId")){
                                eUtranCellRelationParams.put("eNBId",at.getContent().get(0).toString());
                            } else if(at.getName().equalsIgnoreCase("eNodeBPlmnId")){
                                getMccMnc(at.getContent(),eUtranCellRelationParams);
                            }
                        }
                    }
                }
            } else{
                eUtranCellRelationParams.put("eNBId",String.valueOf(cellWrapper.getEnodeBID()));
                isInternalRelationFlag = true;
            }

            // Fetch attribute from reference MO
            for(int index = 0; index < refMo.getAttr().size(); index++){
                Attr attr = refMo.getAttr().get(index);
                if(attr.getName().equalsIgnoreCase("localCellId")){
                    eUtranCellRelationParams.put("localCellId",attr.getContent().get(0).toString());
                } else if(attr.getName().equalsIgnoreCase("cellId")){
                    eUtranCellRelationParams.put("cellId",attr.getContent().get(0).toString());
                } else if(refMo.getAttr().get(index).getName().equalsIgnoreCase("activePlmnList")){
                    if(!isInternalRelationFlag){
                        if(!eUtranCellRelationParams.containsKey("mcc") || !eUtranCellRelationParams.containsKey("mnc")){
                            List<Object> seqValue = refMo.getAttr().get(index).getContent();
                            for(final Object obj : seqValue){
                                if(obj instanceof Seq){
                                    Seq seq = (Seq) obj;
                                    if(null != seq){
                                        for(Item item : seq.getItem()){
                                            getMccMnc(item.getContent(),eUtranCellRelationParams);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if(!eUtranCellRelationParams.isEmpty()){
                getEutranCellRelationInfoObject(eUtranCellRelationParams,isInternalRelationFlag,cellRelationObjMap);
            }
        }
    }

    /**
     * Gets the utran cell relation.
     *
     * @param mo
     *            the mo
     * @param moList
     *            the mo list
     * @return the utran cell relation
     */
    public CellRelationWrapper getUtranCellRelation(Mo mo, List<Mo> moList){
        String refFDN = getRefFDN(mo,"externalUtranCellFDDRef");
        Mo refMo = getRefMo(moList,refFDN);
        if(null != refMo){
            CellRelationWrapper cellRelationInfo = applicationContext.getBean(CellRelationWrapper.class);
            cellRelationInfo.setTypeOfRelation(2);
            for(int index = 0; index < refMo.getAttr().size(); index++){
                if(refMo.getAttr().get(index).getName().equalsIgnoreCase("cellIdentity")){
                    List<Object> structObjList = refMo.getAttr().get(index).getContent();
                    for(Object object : structObjList){
                        if(object instanceof Struct){
                            Struct struct = (Struct) object;
                            if(null != struct){
                                for(Attr attr : struct.getAttr()){
                                    if(attr.getName().equalsIgnoreCase("rncId")){
                                        cellRelationInfo.setReferenceID(Integer.parseInt(attr.getContent().get(0).toString()));
                                    } else if(attr.getName().equalsIgnoreCase("cId")){
                                        cellRelationInfo.setCellID(Integer.parseInt(attr.getContent().get(0).toString()));
                                    }
                                }
                            }
                        }
                    }
                } else if(refMo.getAttr().get(index).getName().equalsIgnoreCase("plmnIdentity")){
                    // Fetch MNC and MCC attribute value
                    Map<String, String> mccMncDetailsMap = new HashMap<String, String>(2);
                    getMccMnc(refMo.getAttr().get(index).getContent(),mccMncDetailsMap);
                    cellRelationInfo.setMcc(mccMncDetailsMap.get("mcc"));
                    cellRelationInfo.setMnc(mccMncDetailsMap.get("mnc"));
                }
            }
            return cellRelationInfo;
        }
        return null;
    }

    /**
     * Gets the geran relation.
     *
     * @param mo
     *            the mo
     * @param moList
     *            the mo list
     * @return the geran relation
     */
    public CellRelationWrapper getGeranRelation(Mo mo, List<Mo> moList){
        String refFDN = getRefFDN(mo,"extGeranCellRef");
        Mo refMo = getRefMo(moList,refFDN);
        if(null != refMo){
            CellRelationWrapper cellRelationInfo = applicationContext.getBean(CellRelationWrapper.class);
            cellRelationInfo.setTypeOfRelation(3);
            for(int index = 0; index < refMo.getAttr().size(); index++){
                if(refMo.getAttr().get(index).getName().equalsIgnoreCase("lac")){
                    cellRelationInfo.setReferenceID(Integer.parseInt(refMo.getAttr().get(index).getContent().get(0).toString()));
                } else if(refMo.getAttr().get(index).getName().equalsIgnoreCase("cellIdentity")){
                    cellRelationInfo.setCellID(Integer.parseInt(refMo.getAttr().get(index).getContent().get(0).toString()));
                } else if(refMo.getAttr().get(index).getName().equalsIgnoreCase("plmnIdentity")){
                    Map<String, String> mccMncDetailsMap = new HashMap<String, String>(2);
                    getMccMnc(refMo.getAttr().get(index).getContent(),mccMncDetailsMap);
                    cellRelationInfo.setMcc(mccMncDetailsMap.get("mcc"));
                    cellRelationInfo.setMnc(mccMncDetailsMap.get("mnc"));
                }
            }
            return cellRelationInfo;
        }
        return null;
    }

    /**
     * Gets the eutran cell relation info object.
     *
     * @param cellRelationMap
     *            the cell relation map
     * @param isInternalRelationFlag
     *            the is internal relation flag
     * @param cellRelationObjMap
     *            the cell relation obj map
     * @return the eutran cell relation info object
     */
    private void getEutranCellRelationInfoObject(Map<String, String> cellRelationMap, boolean isInternalRelationFlag,
            Map<String, List<CellRelationWrapper>> cellRelationObjMap){

        CellRelationWrapper cellRelationInfo = applicationContext.getBean(CellRelationWrapper.class);
        cellRelationInfo.setTypeOfRelation(1);
        if(null != cellRelationMap.get("eNBId")){
            cellRelationInfo.setReferenceID(Integer.parseInt(cellRelationMap.get("eNBId")));
        } else{
            cellRelationInfo.setReferenceID(-1);
        }

        if(isInternalRelationFlag){
            cellRelationInfo.setCellID(Integer.parseInt(cellRelationMap.get("cellId")));
            List<CellRelationWrapper> internalCellList = cellRelationObjMap.get(INTERNAL_EUTRAN);
            internalCellList.add(cellRelationInfo);
            cellRelationObjMap.put(INTERNAL_EUTRAN,internalCellList);
        } else{
            cellRelationInfo.setCellID(Integer.parseInt(cellRelationMap.get("localCellId")));
            cellRelationInfo.setMcc(cellRelationMap.get("mcc"));
            cellRelationInfo.setMnc(cellRelationMap.get("mnc"));
            List<CellRelationWrapper> externalCellList = cellRelationObjMap.get(EXTERNAL_EUTRAN);
            externalCellList.add(cellRelationInfo);
            cellRelationObjMap.put(EXTERNAL_EUTRAN,externalCellList);
        }
    }

    /**
     * Gets the term point en B.
     *
     * @param mo
     *            the mo
     * @param moList
     *            the mo list
     * @return the term point en B
     */
    public Map<String, String> getTermPointEnB(Mo mo, List<Mo> moList){
        Map<String, String> termPointToEnbDetails = new HashMap<String, String>();
        for(int index = 0; index < mo.getAttr().size(); index++){
            if(mo.getAttr().get(index).getName().equalsIgnoreCase("targetFDN")){
                List<Object> structList = mo.getAttr().get(index).getContent();
                for(final Object object : structList){
                    if(object instanceof Struct){
                        Struct struct = (Struct) object;
                        if(null != struct){
                            for(Attr attr : struct.getAttr()){
                                if(attr.getName().equalsIgnoreCase("eNBId")){
                                    termPointToEnbDetails.put("eNBId",attr.getContent().get(0).toString());
                                } else if(attr.getName().equalsIgnoreCase("eNodeBPlmnId")){
                                    getMccMnc(attr.getContent(),termPointToEnbDetails);
                                }
                            }
                        }
                    }
                }
            }
        }
        if(!termPointToEnbDetails.containsKey("eNBId")){
            if(mo.getFdn().contains("ExternalENodeBFunction")){
                String node = null;
                String[] nodeArray = mo.getFdn().split(COMMA);
                for(String index : nodeArray){
                    if(index.contains("ExternalENodeBFunction")){
                        node = index.split(EQUAL)[1].split(HYPHEN)[0];
                    }
                }
                for(Mo moInfo : moList){
                    if(moInfo.getFdn().contains("ExternalENodeBFunction=" + node)){
                        for(Attr at : moInfo.getAttr()){
                            if(at.getName().equalsIgnoreCase("eNBId")){
                                termPointToEnbDetails.put("eNBId",at.getContent().get(0).toString());
                                break;
                            }
                        }
                    }
                    if(termPointToEnbDetails.containsKey("eNBId")){
                        break;
                    }
                }
            }
        }
        return termPointToEnbDetails;
    }

    /**
     * Gets the mme term point.
     *
     * @param mo
     *            the mo
     * @return the mme term point
     */
    public Map<String, String> getMmeTermPoint(Mo mo){
        Map<String, String> mmeTermPointDetailsMap = new HashMap<String, String>();
        for(int index = 0; index < mo.getAttr().size(); index++){
            String attributeName = mo.getAttr().get(index).getName();
            if("mmeCodeListLTERelated".equalsIgnoreCase(attributeName) || "mmeGIListLTERelated".equalsIgnoreCase(attributeName)
                    || "servedPlmnListLTERelated".equalsIgnoreCase(attributeName)){
                List<Object> seqList = mo.getAttr().get(index).getContent();
                for(final Object obj : seqList){
                    if(obj instanceof Seq){
                        Seq seq = (Seq) obj;
                        if(null != seq){
                            if("servedPlmnListLTERelated".equalsIgnoreCase(attributeName)){
                                for(Item item : seq.getItem()){
                                    getMccMnc(item.getContent(),mmeTermPointDetailsMap);
                                }
                            } else{
                                for(Item item : seq.getItem()){
                                    mmeTermPointDetailsMap.put(attributeName,item.getContent().get(0).toString());
                                }
                            }
                        }
                    }
                }
            }
        }
        return mmeTermPointDetailsMap;
    }

    /**
     * Gets the ref FDN.
     *
     * @param mo
     *            the mo
     * @param refAttr
     *            the ref attr
     * @return the ref FDN
     */
    private String getRefFDN(Mo mo, String refAttr){
        for(int index = 0; index < mo.getAttr().size(); index++){
            if(mo.getAttr().get(index).getName().equalsIgnoreCase(refAttr)){
                String refFDN = mo.getAttr().get(index).getContent().get(0).toString();
                return refFDN;
            }
        }
        return null;
    }

    /**
     * Gets the ref mo.
     *
     * @param moList
     *            the mo list
     * @param fdn
     *            the fdn
     * @return the ref mo
     */
    private Mo getRefMo(List<Mo> moList, String fdn){
        for(int count = 0; count < moList.size(); count++){
            if(moList.get(count).getFdn().equalsIgnoreCase(fdn)){
                Mo refMo = moList.get(count);
                return refMo;
            }
        }
        return null;
    }

    /**
     * Gets the mcc mnc.
     *
     * @param structObjList
     *            the struct obj list
     * @param mccMncDetailsMap
     *            the mcc mnc details map
     * @return the mcc mnc
     */
    public void getMccMnc(List<Object> structObjList, Map<String, String> mccMncDetailsMap){
        for(final Object obj : structObjList){
            if(obj instanceof Struct){
                Struct struct = (Struct) obj;
                if(null != struct){
                    for(Attr attr : struct.getAttr()){
                        try{
                            switch(attr.getName()){
                            case "mcc":
                                mccMncDetailsMap.put("mcc",attr.getContent().get(0).toString());
                                break;
                            case "mnc":
                                mccMncDetailsMap.put("mnc",attr.getContent().get(0).toString());
                                break;
                            }
                        } catch(Exception e){
                            Utils.logMessage(ERROR,"Exception occured while parsing struct..." + e);
                        }
                    }
                    if(mccMncDetailsMap.size() == 2){
                        break;
                    }
                }
            }
        }
    }

}
