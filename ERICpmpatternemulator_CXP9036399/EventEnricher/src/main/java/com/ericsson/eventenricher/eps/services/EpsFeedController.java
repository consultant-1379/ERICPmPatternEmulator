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
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.ONE_MINUTE_SECONDS;
import static com.ericsson.configmaster.constants.Constants.ONE_SECOND_MILLISECS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.eventenricher.common.services.Distributor;
import com.ericsson.eventenricher.controller.EventEnricherController;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.BytePackEncoder;
import com.ericsson.schemamaster.services.SchemaLoader;
import com.ericsson.topologymaster.controller.TopologyMasterController;
import com.ericsson.utilities.entities.EventStreamerWrapper;
import com.ericsson.utilities.services.HttpRequestHandler;
import com.ericsson.utilities.services.Utils;

/**
 * The Class FeedController.
 */
@Service
public class EpsFeedController {

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /** The schema loader. */
    @Autowired
    private SchemaLoader schemaLoader;

    @Autowired
    private HttpRequestHandler httpRequestHandler;

    /** The byte pack encoder. */
    @Autowired
    private BytePackEncoder bytePackEncoder;

    /** The schema release obj. */
    private SchemaRelease schemaReleaseObj;

    @Autowired
    private Distributor distributor;

    /** The event list per second. */
    private List<Map<Integer, List<EventStreamerWrapper>>> eventListPerSecond;

    /** The per cell event count. */
    private int perCellEventCount;

    /**
     * Inits
     */
    public void init(String schemaReleaseVersion){
        this.schemaReleaseObj = schemaLoader.loadSchema(schemaReleaseVersion);

        // Calculate eps
        int remainder = streamingConfigLoader.getEps() % TopologyMasterController.getTopologyCellList().size();
        if(remainder > 1){
            remainder = 1;
        }
        perCellEventCount = streamingConfigLoader.getEps() / TopologyMasterController.getTopologyCellList().size() + remainder;
        Utils.logMessage(INFO,"Overall EPS required : " + streamingConfigLoader.getEps() + " and EPS per cell : " + perCellEventCount);

        // prepare header data
        Map<String, byte[]> nodeHeaderMap = new HashMap<>();
        for(String simulatedNodeName : TopologyMasterController.getNodeWrapperMap().keySet()){
            nodeHeaderMap.put(simulatedNodeName,bytePackEncoder.getTCPHeaderByteBuffer(simulatedNodeName,schemaReleaseObj));
        }
        httpRequestHandler.processHeaderData(nodeHeaderMap,EventEnricherController.getUniqueProcessID());
        eventListPerSecond = distributor.distributeTopologyCellsAmongThreads(perCellEventCount,schemaReleaseObj);
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

        // Put something here
        while (true){
            long startTime = System.currentTimeMillis();
            eventListPerSecond = distributor.distributeTopologyCellsAmongThreads(perCellEventCount,schemaReleaseObj);
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
        Utils.logMessage(INFO,"Sending event data to streamer");
        for(int second = 0; second < streamingConfigLoader.getInitialBufferInSeconds(); second++){
            List<EventStreamerWrapper> perSecondAllEventsList = new ArrayList<>();
            for(Map<Integer, List<EventStreamerWrapper>> perSecondEventListMap : eventListPerSecond){
                perSecondAllEventsList.addAll(perSecondEventListMap.get(second));
            }
            httpRequestHandler.sendEventData(perSecondAllEventsList,streamingConfigLoader.getInitialBufferInSeconds(),EventEnricherController
                    .getUniqueProcessID());
        }
        eventListPerSecond.clear();
        Utils.logMessage(INFO,"Data sent to streamer");
    }

}
