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
package com.ericsson.eventenricher.common.services;

import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.INFO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.eventenricher.controller.EventEnricherController;
import com.ericsson.eventenricher.cps.services.CpsEventEnricherThread;
import com.ericsson.eventenricher.entities.CallPerSecondEventsWrapper;
import com.ericsson.eventenricher.eps.services.EpsEventEnricherThread;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.topologymaster.controller.TopologyMasterController;
import com.ericsson.utilities.entities.EventStreamerWrapper;
import com.ericsson.utilities.services.Utils;

// TODO: Auto-generated Javadoc
/**
 * The Class Distributor.
 */
@Service
public class Distributor {

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /**
     * Creates the real cell groups and mapwith topo cells.
     *
     * @param realDataCellList
     *            the real data cell list
     * @return the map
     */
    public Map<String, List<String>> createRealCellGroupsAndMapwithTopoCells(List<String> realDataCellList){
        Map<String, List<String>> topoCellToRealCellMap = new HashMap<>();
        int factor = 1;
        int mod = 0;
        int startIndex = 0;
        int endIndex = 0;
        int topoCellSize = TopologyMasterController.getTopologyCellList().size();
        if(realDataCellList.size() < topoCellSize){
            topoCellSize = realDataCellList.size();
            mod = -1;
        } else{
            factor = realDataCellList.size() / topoCellSize;
            mod = realDataCellList.size() % topoCellSize;
        }
        for(int i = 0; i < topoCellSize; i++){
            startIndex = endIndex;
            if(i == topoCellSize - 1){
                endIndex = realDataCellList.size();
            } else{
                endIndex = endIndex + factor + (mod > 0 ? 1 : 0);
                mod--;
            }
            List<String> groups = realDataCellList.subList(startIndex,endIndex);
            topoCellToRealCellMap.put(TopologyMasterController.getTopologyCellList().get(i),groups);
        }
        return topoCellToRealCellMap;
    }

    /**
     * Cluster algorithm.
     *
     * @param callDurationInSeconds
     *            the call duration in seconds
     * @param callPerSecondWrapper
     *            the call per second wrapper
     * @param callIndex
     *            the call index
     * @param callDurationMap
     *            the call duration map
     */
    public void clusterAlgorithm(int callDurationInSeconds, CallPerSecondEventsWrapper callPerSecondWrapper, int callIndex,
            Map<Integer, List<EventStreamerWrapper>> callDurationMap){
        int factor = 1;
        int mod = 0;
        int startIndex = 0;
        int endIndex = 0;
        int eventListSize = callPerSecondWrapper.getEventList().size();
        if(eventListSize < callDurationInSeconds){
            callDurationInSeconds = eventListSize;
            mod = -1;
        } else{
            factor = eventListSize / callDurationInSeconds;
            mod = eventListSize % callDurationInSeconds;
        }
        for(int i = 0; i < callDurationInSeconds; i++){
            startIndex = endIndex;
            if(i == callDurationInSeconds - 1){
                endIndex = eventListSize;
            } else{
                endIndex = endIndex + factor + (mod > 0 ? 1 : 0);
                mod--;
            }
            List<EventStreamerWrapper> groups = callPerSecondWrapper.getEventList().subList(startIndex,endIndex);
            List<EventStreamerWrapper> eventList = callDurationMap.get(callIndex);
            if(null == eventList){
                eventList = new ArrayList<>();
            }
            eventList.addAll(groups);
            callDurationMap.put(callIndex,eventList);
            callIndex++;
        }
    }

    /**
     * Distribute pattern among threads.
     *
     * @param schemaReleaseObj
     *            the schema release obj
     */
    public void distributePatternAmongThreads(SchemaRelease schemaReleaseObj){
        try{
            Utils.logMessage(INFO,"Started data processing for " + streamingConfigLoader.getInitialBufferInSeconds() + " seconds");
            int poolSize = streamingConfigLoader.getNumberOfThreads();
            List<Integer> patternList = new ArrayList<Integer>(EventEnricherController.getPatternToCellMetaDataListMap().keySet());
            int numberOfPatterns = patternList.size();
            if(numberOfPatterns > 0){
                int factor = 1;
                int mod = 0;
                int startIndex = 0;
                int endIndex = 0;
                if(numberOfPatterns < poolSize){
                    poolSize = numberOfPatterns;
                    mod = -1;
                } else{
                    factor = numberOfPatterns / poolSize;
                    mod = numberOfPatterns % poolSize;
                }
                ThreadPoolExecutor threadPoolTaskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
                for(int threadindex = 0; threadindex < poolSize; threadindex++){
                    startIndex = endIndex;
                    if(threadindex == poolSize - 1){
                        endIndex = numberOfPatterns;
                    } else{
                        endIndex = endIndex + factor + (mod > 0 ? 1 : 0);
                        mod--;
                    }
                    List<Integer> patternGroupPerThreadList = patternList.subList(startIndex,endIndex);
                    CpsEventEnricherThread cpsEventEnricherThread = applicationContext.getBean(CpsEventEnricherThread.class);
                    cpsEventEnricherThread.setPatternGroupPerThreadList(patternGroupPerThreadList);
                    cpsEventEnricherThread.setSchemaReleaseObj(schemaReleaseObj);
                    threadPoolTaskExecutor.execute(cpsEventEnricherThread);
                }
                threadPoolTaskExecutor.shutdown();
                while (!threadPoolTaskExecutor.isTerminated()){
                    Thread.sleep(5000);
                }
                Utils.logMessage(INFO,"Completed data processing for " + streamingConfigLoader.getInitialBufferInSeconds() + " seconds");
            }
        } catch(Exception ex){
            Utils.logMessage(ERROR,"Error occurred while distributing patterns to threads : " + ex);
        }
    }

    /**
     * Distribute topology nodes among threads.
     *
     * @param perCellEventCount
     *            the per cell event count
     * @param schemaReleaseObj
     *            the schema release obj
     * @return the list
     */
    public List<Map<Integer, List<EventStreamerWrapper>>> distributeTopologyCellsAmongThreads(int perCellEventCount, SchemaRelease schemaReleaseObj){
        List<Map<Integer, List<EventStreamerWrapper>>> eventListPerSecond = new ArrayList<>();
        try{
            int totalCells = TopologyMasterController.getTopologyCellList().size();
            Utils.logMessage(INFO,"Started Data processing for " + streamingConfigLoader.getInitialBufferInSeconds() + " seconds");
            int poolSize = streamingConfigLoader.getNumberOfThreads();
            if(totalCells > 0){
                int factor = 1;
                int mod = 0;
                int startIndex = 0;
                int endIndex = 0;
                if(totalCells < poolSize){
                    poolSize = totalCells;
                    mod = -1;
                } else{
                    factor = totalCells / poolSize;
                    mod = totalCells % poolSize;
                }
                ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
                List<Future<Map<Integer, List<EventStreamerWrapper>>>> returnedList = new ArrayList<Future<Map<Integer, List<EventStreamerWrapper>>>>();
                for(int threadindex = 0; threadindex < poolSize; threadindex++){
                    startIndex = endIndex;
                    if(threadindex == poolSize - 1){
                        endIndex = totalCells;
                    } else{
                        endIndex = endIndex + factor + (mod > 0 ? 1 : 0);
                        mod--;
                    }
                    List<String> spilttedCellSubList = TopologyMasterController.getTopologyCellList().subList(startIndex,endIndex);
                    EpsEventEnricherThread eventEnricherThread = applicationContext.getBean(EpsEventEnricherThread.class);
                    eventEnricherThread.setPerCellEventCount(perCellEventCount);
                    eventEnricherThread.setSplittedCellSubList(spilttedCellSubList);
                    eventEnricherThread.setSchemaReleaseObj(schemaReleaseObj);
                    Future<Map<Integer, List<EventStreamerWrapper>>> future = executorService.submit(eventEnricherThread);
                    returnedList.add(future);
                }
                executorService.shutdown();
                for(Future<Map<Integer, List<EventStreamerWrapper>>> fut : returnedList){
                    eventListPerSecond.add(fut.get());
                }
                Utils.logMessage(INFO,"Completed data processing for " + streamingConfigLoader.getInitialBufferInSeconds() + " seconds");
            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occurred while distributing fdn to threads : " + e);
        }
        return eventListPerSecond;
    }
}
