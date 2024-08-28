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

package com.ericsson.eventenricher.eps.services;

import static com.ericsson.configmaster.constants.Constants.ERROR;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.eventenricher.controller.EventEnricherController;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.BytePackDecoder;
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
 * The Class EventEnricherThread.
 */
@Component
public class EpsEventEnricherThread implements Callable<Map<Integer, List<EventStreamerWrapper>>> {

    /** The topology enricher. */
    @Autowired
    private TopologyEnricher topologyEnricher;

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The schema release obj. */
    private SchemaRelease schemaReleaseObj;

    /** The byte pack decoder. */
    @Autowired
    private BytePackDecoder bytePackDecoder;

    /** The splitted cell sub list. */
    private List<String> splittedCellSubList;

    /** The per cell event count. */
    private int perCellEventCount;

    /**
     * Process cells.
     *
     * @return the map
     */
    private Map<Integer, List<EventStreamerWrapper>> processCells(){
        Map<Integer, List<EventStreamerWrapper>> eventPerSecondMap = new HashMap<>();
        List<String> patternCellIDList = new ArrayList<String>(EventEnricherController.getCellToPatternMetaDataListMap().keySet());
        Collections.shuffle(patternCellIDList);
        int totalNumberOfPatternCells = patternCellIDList.size() - 1;

        for(int eventTimeContoller = 0; eventTimeContoller < streamingConfigLoader.getInitialBufferInSeconds(); eventTimeContoller++){
            List<EventStreamerWrapper> eventsPerSecondList = new ArrayList<>();
            for(String cellId : splittedCellSubList){
                if(totalNumberOfPatternCells < 0){
                    totalNumberOfPatternCells = patternCellIDList.size() - 1;
                }
                CellWrapper cellWrapper = TopologyMasterController.getTopologyCellInfoMap().get(cellId);
                NodeWrapper nodeWrapper = TopologyMasterController.getNodeWrapperMap().get(cellWrapper.getFdn());
                epsHandler(cellWrapper,nodeWrapper,patternCellIDList.get(totalNumberOfPatternCells),eventsPerSecondList);
                totalNumberOfPatternCells--;
            }
            eventPerSecondMap.put(eventTimeContoller,eventsPerSecondList);
        }
        return eventPerSecondMap;
    }

    /**
     * Eps handler.
     *
     * @param cellWrapper
     *            the cell wrapper
     * @param nodeWrapper
     *            the node wrapper
     * @param patternCellID
     *            the pattern cell ID
     * @param eventsPerSecondList
     *            the events per second list
     */
    private void epsHandler(CellWrapper cellWrapper, NodeWrapper nodeWrapper, String patternCellID, List<EventStreamerWrapper> eventsPerSecondList){
        List<CellRelationWrapper> eutranCellRelation = new ArrayList<>();
        eutranCellRelation.addAll(cellWrapper.getinternalEutranRelations());
        eutranCellRelation.addAll(cellWrapper.getExternaleutranRelations());
        List<MmeNodeWrapper> mmeNodeWrapperList = nodeWrapper.getNeighborMmeList();
        byte[] eventParamGenBID = nodeWrapper.getEventParamGENBID();

        Map<Integer, List<String>> patternDataMap = EventEnricherController.getCellToPatternMetaDataListMap().get(patternCellID);
        List<Integer> patternsList = new ArrayList<>(patternDataMap.keySet());
        Collections.shuffle(patternsList);
        int counter = 0;
        outerloop: for(int eps = 0; eps < perCellEventCount - counter; eps++){
            for(int patternID : patternsList){
                List<String> sessionFileNameList = patternDataMap.get(patternID);
                String sessionFileName = sessionFileNameList.get(randomNumGenerator(sessionFileNameList.size()));
                String sessionFilePath = streamingConfigLoader.getPatternLocation() + File.separator + patternID + File.separator + sessionFileName;
                counter += enrichTopologyParametersForSession(sessionFilePath,eutranCellRelation,cellWrapper,mmeNodeWrapperList,eventParamGenBID,
                        eventsPerSecondList);
                if(counter >= perCellEventCount){
                    break outerloop;
                }
            }
        }
    }

    /**
     * Enrich topology parameters for session.
     *
     * @param sessionFilePath
     *            the session file path
     * @param eutranCellRelation
     *            the eutran cell relation
     * @param cellWrapper
     *            the cell wrapper
     * @param mmeNodeWrapperList
     *            the mme node wrapper list
     * @param eventParamGenBID
     *            the event param gen BID
     * @param eventsPerSecondList
     *            the events per second list
     * @return the int
     */
    private int enrichTopologyParametersForSession(String sessionFilePath, List<CellRelationWrapper> eutranCellRelation, CellWrapper cellWrapper,
            List<MmeNodeWrapper> mmeNodeWrapperList, byte[] eventParamGenBID, List<EventStreamerWrapper> eventsPerSecondList){
        try{
            File sessionFile = new File(sessionFilePath);
            if(!sessionFile.exists()){
                return 0;
            }
            GZIPInputStream fis = new GZIPInputStream(new FileInputStream(sessionFile));
            ObjectInputStream ois = new ObjectInputStream(fis);
            SessionPerCallWrapper sessionPerCallObj = (SessionPerCallWrapper) ois.readObject();
            List<CellRelationWrapper> cellRelationWrapperList = null;
            if(sessionPerCallObj.getTypeOfRelation() != 0){
                cellRelationWrapperList = getListOfCellRelationWrapper(sessionPerCallObj.getTypeOfRelation(),cellWrapper,eutranCellRelation);
            }
            for(byte[] eventData : sessionPerCallObj.getEventDataList()){
                int eventID = bytePackDecoder.getEventId(eventData);
                byte[] updatedEventData = topologyEnricher.setTopologyParametersValue(eventID,eventData,schemaReleaseObj,eventParamGenBID,cellWrapper,
                        getRandomCellRelationWrapper(cellRelationWrapperList),getMmeNodeWrapper(mmeNodeWrapperList));
                eventsPerSecondList.add(getEventStreamWrapper(updatedEventData,eventID,cellWrapper.getFdn()));
            }
            ois.close();
            fis.close();
            return sessionPerCallObj.getEventDataList().size();
        } catch(Exception ex){
            Utils.logMessage(ERROR,"Exception while enriching topology parameters for a session file: " + sessionFilePath + " " + ex);
        }
        return 0;
    }

    /**
     * Gets the event stream wrapper.
     *
     * @param eventData
     *            the event data
     * @param fdn
     *            the fdn
     * @return the event stream wrapper
     */
    private EventStreamerWrapper getEventStreamWrapper(byte[] eventData, int eventID, String fdn){
        EventStreamerWrapper eventStreamWrapper = applicationContext.getBean(EventStreamerWrapper.class);
        eventStreamWrapper.setEventByte(eventData);
        eventStreamWrapper.setEventID(eventID);
        eventStreamWrapper.setFdn(fdn);
        return eventStreamWrapper;
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
     * Sets the schema release obj.
     *
     * @param schemaReleaseObj
     *            the new schema release obj
     */
    public void setSchemaReleaseObj(SchemaRelease schemaReleaseObj){
        this.schemaReleaseObj = schemaReleaseObj;
    }

    /**
     * Sets the splitted cell sub list.
     *
     * @param splittedCellSubList
     *            the new splitted cell sub list
     */
    public void setSplittedCellSubList(List<String> splittedCellSubList){
        this.splittedCellSubList = splittedCellSubList;
    }

    /**
     * Sets the per cell event count.
     *
     * @param perCellEventCount
     *            the new per cell event count
     */
    public void setPerCellEventCount(int perCellEventCount){
        this.perCellEventCount = perCellEventCount;
    }

    /**
     * Call.
     *
     * @return the map
     * @throws Exception
     *             the exception
     */
    @Override
    public Map<Integer, List<EventStreamerWrapper>> call() throws Exception{
        return processCells();
    }

}
