/*********************************************************************
Ericsson Inc.
**********************************************************************

(c) Ericsson Inc. 2018 - All rights reserved.

The copyright to the computer program(s) herein is the property of
Ericsson Inc. The programs may be used and/or copied only with written
permission from Ericsson Inc. or in accordance with the terms and
conditions stipulated in the agreement/contract under which the
program(s) have been supplied.

***********************************************************************/
package com.ericsson.eventenricher.cps.services;

import static com.ericsson.configmaster.constants.Constants.ERROR;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.eventenricher.controller.EventEnricherController;
import com.ericsson.eventenricher.entities.CallPerSecondEventsWrapper;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.topologyenricher.services.TopologyEnricher;
import com.ericsson.topologymaster.controller.TopologyMasterController;
import com.ericsson.topologyparser.entities.CellRelationWrapper;
import com.ericsson.topologyparser.entities.CellWrapper;
import com.ericsson.topologyparser.entities.MmeNodeWrapper;
import com.ericsson.topologyparser.entities.NodeWrapper;
import com.ericsson.utilities.entities.EventStreamerWrapper;
import com.ericsson.utilities.entities.SessionPerCallWrapper;
import com.ericsson.utilities.services.Utils;

/**
 * The Class CpsEventEnricherThread.
 */
@Component
public class CpsEventEnricherThread implements Runnable {

    /** The cps feed controller. */
    @Autowired
    private CpsFeedController cpsFeedController;

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /** The topology enricher. */
    @Autowired
    private TopologyEnricher topologyEnricher;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The schema release obj. */
    private SchemaRelease schemaReleaseObj;

    /** The pattern group per thread list. */
    private List<Integer> patternGroupPerThreadList;

    @Override
    public void run(){
        for(int eventTimeContoller = 0; eventTimeContoller < streamingConfigLoader.getInitialBufferInSeconds(); eventTimeContoller++){
            List<CallPerSecondEventsWrapper> callPerSecondEventsList = new ArrayList<>();
            for(int patternID : patternGroupPerThreadList){
                int totalNumberOfCalls = cpsFeedController.getCpsPerPattern().get(patternID);
                List<String> topoCellList = cpsFeedController.getBackupPatternToListOfTopologyCellMap().get(patternID);
                ListIterator<String> iterator = topoCellList.listIterator();
                int currentCallsCount = 0;
                while (currentCallsCount < totalNumberOfCalls){
                    if(topoCellList.isEmpty()){
                        cpsFeedController.getBackupPatternToListOfTopologyCellMap().put(patternID,new ArrayList<>(TopologyMasterController
                                .getTopologyCellList()));
                        topoCellList = cpsFeedController.getBackupPatternToListOfTopologyCellMap().get(patternID);
                        iterator = topoCellList.listIterator();
                    }
                    while (iterator.hasNext()){
                        String topoCell = iterator.next();
                        List<String> realDataCellList = cpsFeedController.getPatternBasedCellMap().get(patternID).get(topoCell);
                        String realDataCell = realDataCellList.get(randomNumGenerator(realDataCellList.size()));
                        if(callPerSecondHandlerWithDistribution(topoCell,realDataCell,patternID,callPerSecondEventsList)){
                            currentCallsCount++;
                            iterator.remove();
                        }
                        if(currentCallsCount >= totalNumberOfCalls){
                            break;
                        }
                    }
                }
            }
            cpsFeedController.addCallPerSecondWrapperMap(eventTimeContoller,callPerSecondEventsList);
        }
    }

    /**
     * Call per second handler with distribution.
     *
     * @param topoCell
     *            the topo cell
     * @param realDataCell
     *            the real data cell
     * @param patternID
     *            the pattern ID
     * @param callPerSecondEventsList
     *            the call per second events list
     * @return true, if successful
     */
    private boolean callPerSecondHandlerWithDistribution(String topoCell, String realDataCell, int patternID,
            List<CallPerSecondEventsWrapper> callPerSecondEventsList){
        CellWrapper cellWrapper = TopologyMasterController.getTopologyCellInfoMap().get(topoCell);
        NodeWrapper nodeWrapper = TopologyMasterController.getNodeWrapperMap().get(cellWrapper.getFdn());
        List<CellRelationWrapper> eutranCellRelation = new ArrayList<>();
        eutranCellRelation.addAll(cellWrapper.getinternalEutranRelations());
        eutranCellRelation.addAll(cellWrapper.getExternaleutranRelations());
        List<MmeNodeWrapper> mmeNodeWrapperList = nodeWrapper.getNeighborMmeList();
        byte[] eventParamGenBID = nodeWrapper.getEventParamGENBID();

        List<String> realDataSessionFileNameList = EventEnricherController.getPatternToCellMetaDataListMap().get(patternID).get(realDataCell);
        String sessionFilePath = streamingConfigLoader.getPatternLocation() + File.separator + patternID + File.separator
                + realDataSessionFileNameList.get(randomNumGenerator(realDataSessionFileNameList.size()));
        File sessionFile = new File(sessionFilePath);
        if(!sessionFile.exists()){
            return false;
        }
        return enrichTopologyParametersForSession(sessionFile,eutranCellRelation,cellWrapper,mmeNodeWrapperList,eventParamGenBID,
                callPerSecondEventsList);
    }

    /**
     * Enrich topology parameters for session.
     *
     * @param sessionFile
     *            the session file
     * @param eutranCellRelation
     *            the eutran cell relation
     * @param cellWrapper
     *            the cell wrapper
     * @param mmeNodeWrapperList
     *            the mme node wrapper list
     * @param eventParamGenBID
     *            the event param gen BID
     * @param callPerSecondEventsList
     *            the call per second events list
     * @return true, if successful
     */
    private boolean enrichTopologyParametersForSession(File sessionFile, List<CellRelationWrapper> eutranCellRelation, CellWrapper cellWrapper,
            List<MmeNodeWrapper> mmeNodeWrapperList, byte[] eventParamGenBID, List<CallPerSecondEventsWrapper> callPerSecondEventsList){
        try{
            SessionPerCallWrapper sessionPerCallObj = Utils.deserializeSessionFile(sessionFile);
            List<CellRelationWrapper> cellRelationWrapperList = null;
            if(sessionPerCallObj.getTypeOfRelation() != 0){
                cellRelationWrapperList = getListOfCellRelationWrapper(sessionPerCallObj.getTypeOfRelation(),cellWrapper,eutranCellRelation);
            }
            CallPerSecondEventsWrapper callPerSecondEventsWrapper = getCallPerSecondEventsWrapper(cellWrapper.getFdn());
            for(int i = 0; i < sessionPerCallObj.getEventDataList().size(); i++){
                byte[] updatedEventData = topologyEnricher.setTopologyParametersValue(sessionPerCallObj.getEventIDList().get(i),sessionPerCallObj
                        .getEventDataList().get(i),schemaReleaseObj,eventParamGenBID,cellWrapper,getRandomCellRelationWrapper(
                                cellRelationWrapperList),getMmeNodeWrapper(mmeNodeWrapperList));
                callPerSecondEventsWrapper.getEventList().add(getEventStreamWrapper(updatedEventData,sessionPerCallObj.getEventIDList().get(i),
                        cellWrapper.getFdn()));
            }
            callPerSecondEventsList.add(callPerSecondEventsWrapper);
            return true;
        } catch(Exception ex){
            Utils.logMessage(ERROR,"Exception while enriching topology parameters for a session file: " + sessionFile.getAbsolutePath() + " " + ex);
            return false;
        }
    }

    /**
     * Gets the list of cell relation wrapper.
     *
     * @param typeOfRelation
     *            the type of relation
     * @param cellWrapper
     *            the cell wrapper
     * @param eutranCellRelation
     *            the eutran cell relation
     * @return the list of cell relation wrapper
     */
    private List<CellRelationWrapper> getListOfCellRelationWrapper(int typeOfRelation, CellWrapper cellWrapper,
            List<CellRelationWrapper> eutranCellRelation){
        List<CellRelationWrapper> cellRelationWrapperList = null;
        switch(typeOfRelation){
        case 1:
            cellRelationWrapperList = eutranCellRelation;
            break;
        case 2:
            cellRelationWrapperList = cellWrapper.getUtranRelations();
            break;
        case 3:
            cellRelationWrapperList = cellWrapper.getGeranRelations();
            break;
        }
        return cellRelationWrapperList;
    }

    /**
     * Gets the call per second events wrapper.
     *
     * @param fdn
     *            the fdn
     * @return the call per second events wrapper
     */
    private CallPerSecondEventsWrapper getCallPerSecondEventsWrapper(String fdn){
        CallPerSecondEventsWrapper callPerSecondEventsWrapper = applicationContext.getBean(CallPerSecondEventsWrapper.class);
        callPerSecondEventsWrapper.setFdn(fdn);
        callPerSecondEventsWrapper.setEventList(new ArrayList<EventStreamerWrapper>());
        return callPerSecondEventsWrapper;
    }

    /**
     * Gets the event stream wrapper.
     *
     * @param eventData
     *            the event data
     * @return the event stream wrapper
     */
    private EventStreamerWrapper getEventStreamWrapper(byte[] eventData, int eventID, String fdn){
        EventStreamerWrapper eventStreamerWrapper = applicationContext.getBean(EventStreamerWrapper.class);
        eventStreamerWrapper.setEventByte(eventData);
        eventStreamerWrapper.setEventID(eventID);
        eventStreamerWrapper.setFdn(fdn);
        return eventStreamerWrapper;
    }

    /**
     * Gets the random cell relation wrapper.
     *
     * @param cellRelationWrapperList
     *            the cell relation wrapper list
     * @return the random cell relation wrapper
     */
    private CellRelationWrapper getRandomCellRelationWrapper(List<CellRelationWrapper> cellRelationWrapperList){
        if(null != cellRelationWrapperList && cellRelationWrapperList.size() != 0){
            int randomInternal = randomNumGenerator(cellRelationWrapperList.size());
            return cellRelationWrapperList.get(randomInternal);
        }
        return null;
    }

    /**
     * Gets the mme node wrapper.
     *
     * @param mmeNodeWrapperList
     *            the mme node wrapper list
     * @return the mme node wrapper
     */
    private MmeNodeWrapper getMmeNodeWrapper(List<MmeNodeWrapper> mmeNodeWrapperList){
        if(null != mmeNodeWrapperList && mmeNodeWrapperList.size() != 0){
            int randomMME = randomNumGenerator(mmeNodeWrapperList.size());
            return mmeNodeWrapperList.get(randomMME);
        }
        return null;
    }

    /**
     * Random num generator.
     *
     * @param maxValue
     *            the max value
     * @return the int
     */
    private int randomNumGenerator(int maxValue){
        return ThreadLocalRandom.current().nextInt(0,maxValue);
    }

    /**
     * Gets the pattern group per thread list.
     *
     * @return the pattern group per thread list
     */
    public List<Integer> getPatternGroupPerThreadList(){
        return patternGroupPerThreadList;
    }

    /**
     * Sets the pattern group per thread list.
     *
     * @param patternGroupPerThreadList
     *            the new pattern group per thread list
     */
    public void setPatternGroupPerThreadList(List<Integer> patternGroupPerThreadList){
        this.patternGroupPerThreadList = patternGroupPerThreadList;
    }

    /**
     * Gets the schema release obj.
     *
     * @return the schema release obj
     */
    public SchemaRelease getSchemaReleaseObj(){
        return schemaReleaseObj;
    }

    /**
     * Sets the schema release obj.
     *
     * @param schemaReleaseObj
     *            the new schema release obj
     */
    public void setSchemaReleaseObj(SchemaRelease schemaReleaseObj){
        this.schemaReleaseObj = schemaReleaseObj;
    }

}
