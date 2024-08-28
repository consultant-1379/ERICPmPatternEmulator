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

import static com.ericsson.configmaster.constants.Constants.COMMA;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.ONE_MINUTE_SECONDS;
import static com.ericsson.configmaster.constants.Constants.ONE_SECOND_MILLISECS;
import static com.ericsson.configmaster.constants.Constants.PATTERN_INFO_CSV_FILE_NAME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.eventenricher.common.services.Distributor;
import com.ericsson.eventenricher.controller.EventEnricherController;
import com.ericsson.eventenricher.entities.CallPerSecondEventsWrapper;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.BytePackEncoder;
import com.ericsson.schemamaster.services.SchemaLoader;
import com.ericsson.topologymaster.controller.TopologyMasterController;
import com.ericsson.utilities.entities.EventStreamerWrapper;
import com.ericsson.utilities.services.HttpRequestHandler;
import com.ericsson.utilities.services.Utils;

/**
 * The Class CpsFeedController.
 */
@Service
public class CpsFeedController {

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /** The schema loader. */
    @Autowired
    private SchemaLoader schemaLoader;

    /** The byte pack encoder. */
    @Autowired
    private BytePackEncoder bytePackEncoder;

    /** The distributor. */
    @Autowired
    private Distributor distributor;

    /** The http handler. */
    @Autowired
    private HttpRequestHandler httpRequestHandler;

    /** The pattern based cell map. */
    private Map<Integer, Map<String, List<String>>> patternBasedCellMap;

    /** The cps per pattern. */
    private Map<Integer, Integer> cpsPerPattern;

    /** The schema release obj. */
    private SchemaRelease schemaReleaseObj;

    /** The backup pattern to list of topology cell map. */
    private Map<Integer, List<String>> backupPatternToListOfTopologyCellMap;

    /** The call per second wrapper map all threads. */
    private Map<Integer, List<CallPerSecondEventsWrapper>> callPerSecondWrapperMapAllThreads;

    /** The final event stream map. */
    private Map<Integer, List<EventStreamerWrapper>> finalEventStreamMap;

    /**
     * Inits the.
     *
     * @throws Exception
     *             the exception
     */
    public void init(String schemaReleaseVersion) throws Exception{
        calculateCallsPerPattern();
        if(cpsPerPattern.isEmpty()){
            Utils.logMessage(INFO,"No patterns found",true);
            System.exit(0);
        }
        EventEnricherController.getPatternToCellMetaDataListMap().keySet().retainAll(cpsPerPattern.keySet()); // Remove unwanted patterns from the
        mapSimulatedNetworkToRealDataCells();

        this.schemaReleaseObj = schemaLoader.loadSchema(schemaReleaseVersion);
        backupPatternToListOfTopologyCellMap = new ConcurrentHashMap<>();
        initializeBackupPatternToListOfTopologyCellMap();
        Map<String, byte[]> nodeHeaderMap = new HashMap<>();
        for(String simulatedNodeName : TopologyMasterController.getNodeWrapperMap().keySet()){
            nodeHeaderMap.put(simulatedNodeName,bytePackEncoder.getTCPHeaderByteBuffer(simulatedNodeName,schemaReleaseObj));
        }
        httpRequestHandler.processHeaderData(nodeHeaderMap,EventEnricherController.getUniqueProcessID());

        callPerSecondWrapperMapAllThreads = new HashMap<>();
        finalEventStreamMap = new LinkedHashMap<>();
        distributor.distributePatternAmongThreads(schemaReleaseObj);
    }

    /**
     * Map simulated network to real data cells.
     */
    private void mapSimulatedNetworkToRealDataCells(){
        patternBasedCellMap = new HashMap<>();
        for(int patternID : EventEnricherController.getPatternToCellMetaDataListMap().keySet()){
            List<String> realDataCellSet = new ArrayList<>(EventEnricherController.getPatternToCellMetaDataListMap().get(patternID).keySet());
            // Cyclic order
            if(realDataCellSet.size() <= TopologyMasterController.getTopologyCellList().size()){
                patternBasedCellMap.put(patternID,mapInCyclicOrder(realDataCellSet));
            } // divide into groups
            else{
                patternBasedCellMap.put(patternID,distributor.createRealCellGroupsAndMapwithTopoCells(realDataCellSet));
            }
        }
    }

    /**
     * Map in cyclic order.
     *
     * @param realDataCellList
     *            the real data cell list
     * @return the map
     */
    private Map<String, List<String>> mapInCyclicOrder(List<String> realDataCellList){
        Map<String, List<String>> topoCellToRealCellMap = new HashMap<>();
        int index = 0;
        for(String topoCell : TopologyMasterController.getTopologyCellList()){
            if(index == realDataCellList.size()){
                index = 0;
            }
            List<String> realCellList = new ArrayList<>(1);
            realCellList.add(realDataCellList.get(index));
            topoCellToRealCellMap.put(topoCell,realCellList);
            index++;
        }
        return topoCellToRealCellMap;
    }

    /**
     * Calculate calls per pattern.
     *
     * @throws Exception
     *             the exception
     */
    private void calculateCallsPerPattern() throws Exception{
        cpsPerPattern = new HashMap<>();
        String line = "";
        try (BufferedReader br = new BufferedReader(new FileReader(streamingConfigLoader.getPatternLocation() + File.separator
                + PATTERN_INFO_CSV_FILE_NAME))){
            int firstLine = 0;
            while ((line = br.readLine()) != null){
                if(firstLine == 0){
                    firstLine++;
                    continue;
                }
                String[] patternInfo = line.split(COMMA);
                if(patternInfo.length == 5){
                    double patternDistributionPercentage = Double.parseDouble(patternInfo[4].trim()) / 100;
                    if(patternDistributionPercentage != 0.0){
                        cpsPerPattern.put(Integer.parseInt(patternInfo[0]),(int) Math.round(streamingConfigLoader.getCps()
                                * patternDistributionPercentage));
                    }
                }
            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Unable to read pattern distribution csv file " + e);
            throw e;
        }
    }

    /**
     * Start feeding.
     */
    public void startFeeding(){
        prepareAllFdnPerSecondData();
        long initialDataPrepDuration = streamingConfigLoader.getInitialBufferInSeconds() * ONE_SECOND_MILLISECS;
        if(streamingConfigLoader.getInitialBufferInSeconds() > ONE_MINUTE_SECONDS){
            streamingConfigLoader.setInitialBufferInSeconds(ONE_MINUTE_SECONDS);
        } else{
            streamingConfigLoader.setInitialBufferInSeconds(ONE_MINUTE_SECONDS / 2);
        }

        while (true){
            long startTime = System.currentTimeMillis();
            distributor.distributePatternAmongThreads(schemaReleaseObj);
            prepareAllFdnPerSecondData();
            long endTime = System.currentTimeMillis();
            long timeTaken = endTime - startTime;
            Utils.logMessage(INFO,"Time taken to prepare " + streamingConfigLoader.getInitialBufferInSeconds() + " seconds of data : " + timeTaken
                    / ONE_SECOND_MILLISECS + " seconds");
            long timeToSleep = initialDataPrepDuration - timeTaken;
            if(timeToSleep > 0){
                try{
                    Utils.logMessage(INFO,"Feeding thread sleeping for " + timeToSleep / ONE_SECOND_MILLISECS + " seconds");
                    Thread.sleep(timeToSleep);
                } catch(Exception e){
                    Utils.logMessage(ERROR,"Exception while sleeping in startFeeding method. " + e);
                }
            }
        }
    }

    /**
     * Prepare all fdn per second data.
     */
    private void prepareAllFdnPerSecondData(){
        Utils.logMessage(INFO,"Sending calls to streamer");
        int callDurationInSeconds = streamingConfigLoader.getCallDurationInSeconds();
        for(int seconds = 0; seconds < streamingConfigLoader.getInitialBufferInSeconds(); seconds++){
            for(CallPerSecondEventsWrapper callPerSecondWrapper : callPerSecondWrapperMapAllThreads.get(seconds)){
                int callIndex = seconds;
                if(callDurationInSeconds > callPerSecondWrapper.getEventList().size()){
                    if(callPerSecondWrapper.getEventList().size() >= callDurationInSeconds / 2){
                        subtractionBasedAlgorithm(callDurationInSeconds,callPerSecondWrapper,callIndex);
                    } else{
                        fillCallGapsAlgorithm(callDurationInSeconds,callPerSecondWrapper,callIndex);
                    }
                } else{
                    distributor.clusterAlgorithm(callDurationInSeconds,callPerSecondWrapper,callIndex,finalEventStreamMap);
                }
            }
        }
        handleAddtionalEvents();
        Utils.logMessage(INFO,"Data sent to streamer");
    }

    /**
     * Subtraction based algorithm.
     *
     * @param callDurationInSeconds
     *            the call duration in seconds
     * @param callPerSecondWrapper
     *            the call per second wrapper
     * @param callIndex
     *            the call index
     */
    private void subtractionBasedAlgorithm(int callDurationInSeconds, CallPerSecondEventsWrapper callPerSecondWrapper, int callIndex){
        int gapsCount = callDurationInSeconds - callPerSecondWrapper.getEventList().size();
        for(EventStreamerWrapper event : callPerSecondWrapper.getEventList()){
            List<EventStreamerWrapper> eventsHolder = finalEventStreamMap.get(callIndex);
            if(null == eventsHolder){
                eventsHolder = new ArrayList<>();
            }
            eventsHolder.add(event);
            finalEventStreamMap.put(callIndex,eventsHolder);
            callIndex++;
            if(gapsCount != 0){
                gapsCount--;
                callIndex++;
            }
        }
    }

    /**
     * Fill call gaps algorithm.
     *
     * @param callDurationInSeconds
     *            the call duration in seconds
     * @param callPerSecondWrapper
     *            the call per second wrapper
     * @param callIndex
     *            the call index
     */
    private void fillCallGapsAlgorithm(int callDurationInSeconds, CallPerSecondEventsWrapper callPerSecondWrapper, int callIndex){
        int gapBetweenEvents = callDurationInSeconds / callPerSecondWrapper.getEventList().size();
        if(callDurationInSeconds % callPerSecondWrapper.getEventList().size() > 0){
            gapBetweenEvents++;
        }
        int remainingEvents = callPerSecondWrapper.getEventList().size();
        int remainingSeconds = callDurationInSeconds;
        int endCallIndex = callIndex + callDurationInSeconds - 1;
        for(EventStreamerWrapper event : callPerSecondWrapper.getEventList()){
            List<EventStreamerWrapper> eventsHolder = finalEventStreamMap.get(callIndex);
            if(null == eventsHolder){
                eventsHolder = new ArrayList<>();
            }
            eventsHolder.add(event);
            remainingEvents--;
            finalEventStreamMap.put(callIndex,eventsHolder);
            remainingSeconds = remainingSeconds - gapBetweenEvents;
            if(remainingEvents + gapBetweenEvents >= remainingSeconds){
                if(gapBetweenEvents > 1){
                    gapBetweenEvents--;
                }
            }
            if(remainingEvents == 1){
                callIndex = endCallIndex;
            } else{
                callIndex += gapBetweenEvents;
            }
        }
    }

    /**
     * Process call with duration.
     */
    private void handleAddtionalEvents(){
        callPerSecondWrapperMapAllThreads.clear();
        for(int i = 0; i < streamingConfigLoader.getInitialBufferInSeconds(); i++){
            List<EventStreamerWrapper> perSecondAllEventsList = finalEventStreamMap.get(i);
            if(null != perSecondAllEventsList){
                httpRequestHandler.sendEventData(perSecondAllEventsList,streamingConfigLoader.getInitialBufferInSeconds(),EventEnricherController
                        .getUniqueProcessID());
                finalEventStreamMap.remove(i);
            }
        }
        Map<Integer, List<EventStreamerWrapper>> temp = new LinkedHashMap<>();
        int startIndex = 0;
        for(int secondIndex : finalEventStreamMap.keySet()){
            temp.put(startIndex,finalEventStreamMap.get(secondIndex));
            startIndex++;
        }
        finalEventStreamMap.clear();
        finalEventStreamMap.putAll(temp);
    }

    /**
     * Adds the call per second wrapper map.
     *
     * @param eventTimeContoller
     *            the event time contoller
     * @param callPerSecondEventsList
     *            the call per second events list
     */
    public synchronized void addCallPerSecondWrapperMap(int eventTimeContoller, List<CallPerSecondEventsWrapper> callPerSecondEventsList){
        List<CallPerSecondEventsWrapper> cpsList = callPerSecondWrapperMapAllThreads.get(eventTimeContoller);
        if(null == cpsList){
            cpsList = new ArrayList<>();
        }
        cpsList.addAll(callPerSecondEventsList);
        callPerSecondWrapperMapAllThreads.put(eventTimeContoller,cpsList);
    }

    /**
     * Initialize backup pattern to list of topology cell map.
     */
    private void initializeBackupPatternToListOfTopologyCellMap(){
        for(int patternID : cpsPerPattern.keySet()){
            backupPatternToListOfTopologyCellMap.put(patternID,new ArrayList<>(TopologyMasterController.getTopologyCellList()));
        }
    }

    /**
     * Gets the cps per pattern.
     *
     * @return the cps per pattern
     */
    public Map<Integer, Integer> getCpsPerPattern(){
        return cpsPerPattern;
    }

    /**
     * Sets the cps per pattern.
     *
     * @param cpsPerPattern
     *            the cps per pattern
     */
    public void setCpsPerPattern(Map<Integer, Integer> cpsPerPattern){
        this.cpsPerPattern = cpsPerPattern;
    }

    /**
     * Gets the pattern based cell map.
     *
     * @return the pattern based cell map
     */
    public Map<Integer, Map<String, List<String>>> getPatternBasedCellMap(){
        return patternBasedCellMap;
    }

    /**
     * Sets the pattern based cell map.
     *
     * @param patternBasedCellMap
     *            the pattern based cell map
     */
    public void setPatternBasedCellMap(Map<Integer, Map<String, List<String>>> patternBasedCellMap){
        this.patternBasedCellMap = patternBasedCellMap;
    }

    /**
     * Gets the backup pattern to list of topology cell map.
     *
     * @return the backup pattern to list of topology cell map
     */
    public Map<Integer, List<String>> getBackupPatternToListOfTopologyCellMap(){
        return backupPatternToListOfTopologyCellMap;
    }

    /**
     * Sets the backup pattern to list of topology cell map.
     *
     * @param backupPatternToListOfTopologyCellMap
     *            the backup pattern to list of topology cell map
     */
    public void setBackupPatternToListOfTopologyCellMap(Map<Integer, List<String>> backupPatternToListOfTopologyCellMap){
        this.backupPatternToListOfTopologyCellMap = backupPatternToListOfTopologyCellMap;
    }

}
