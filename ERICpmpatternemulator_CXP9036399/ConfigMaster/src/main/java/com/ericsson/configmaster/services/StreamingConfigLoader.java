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

package com.ericsson.configmaster.services;

import static com.ericsson.configmaster.constants.Constants.COMMA;
import static com.ericsson.configmaster.constants.Constants.STREAMING_CONFIG_FILE_PATH;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * The Class StreamingConfigLoader.
 */
@Configuration
@PropertySource("file:${APP_ROOT_PATH}" + STREAMING_CONFIG_FILE_PATH)
public class StreamingConfigLoader {

    /** The datasource. */
    @Value("#{'${DATASOURCE}'.toUpperCase()}")
    private String datasource;

    /** The topology location. */
    @Value("${TOPOLOGY_LOCATION}")
    private String topologyLocation;

    /** The pattern location. */
    @Value("${PATTERN_LOCATION}")
    private String patternLocation;

    /** The eps. */
    @Value("${EPS}")
    private int eps;

    /** The cps. */
    @Value("${CALLS_PER_SECOND}")
    private int cps;

    /** The callDurationInSeconds. */
    @Value("${CALL_DURATION_IN_SECONDS}")
    private int callDurationInSeconds;

    /** The destination ip port set. */
    @Value("#{'${DESTINATION_IP_PORT}'.split('" + COMMA + "')}")
    private Set<String> destinationIpPortSet;

    /** The pattern location. */
    @Value("${SELECTED_NODE_FILE_LOCATION}")
    private String selectedNodeFileLocation;

    /** The selected node list. */
    private List<String> selectedNodeList;

    /** The enable timestamp. */
    @Value("${ENABLE_TIMESTAMP}")
    private String enableTimestamp;

    /** The number of threads. */
    @Value("${NUMBER_OF_THREADS}")
    private int numberOfThreads;

    /** The initial buffer in seconds. */
    @Value("${INITIAL_BUFFER_IN_SECONDS}")
    private int initialBufferInSeconds;

    /** The streamer hosted ip port. */
    @Value("${STREAMER_HOSTED_IP_PORT}")
    private String streamerHostedIpPort;

    /**
     * Inits.
     */
    @PostConstruct
    public void init(){
        if(1 == destinationIpPortSet.size() && destinationIpPortSet.contains("")){
            destinationIpPortSet.clear();
        }
        if(null != selectedNodeFileLocation && !selectedNodeFileLocation.isEmpty()){
            try{
                selectedNodeList = Files.readAllLines(Paths.get(selectedNodeFileLocation),StandardCharsets.UTF_8);
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the topology location.
     *
     * @return the topology location
     */
    public String getTopologyLocation(){
        return topologyLocation;
    }

    /**
     * Gets the datasource.
     *
     * @return the datasource
     */
    public String getDatasource(){
        return datasource;
    }

    /**
     * Gets the pattern location.
     *
     * @return the pattern location
     */
    public String getPatternLocation(){
        return patternLocation;
    }

    /**
     * Gets the eps.
     *
     * @return the eps
     */
    public int getEps(){
        return eps;
    }

    /**
     * Gets the list destination ip port.
     *
     * @return the list destination ip port
     */
    public Set<String> getListDestinationIpPort(){
        return destinationIpPortSet;
    }

    /**
     * Gets the selected node list.
     *
     * @return the selected node list
     */
    public List<String> getSelectedNodeList(){
        return selectedNodeList;
    }

    /**
     * Gets the enable timestamp.
     *
     * @return the enable timestamp
     */
    public String getEnableTimestamp(){
        return enableTimestamp;
    }

    /**
     * Gets the number of threads.
     *
     * @return the number of threads
     */
    public int getNumberOfThreads(){
        return numberOfThreads;
    }

    /**
     * Gets the initial buffer in seconds.
     *
     * @return the initial buffer in seconds
     */
    public int getInitialBufferInSeconds(){
        return initialBufferInSeconds;
    }

    /**
     * Sets the initial buffer in seconds.
     *
     * @param initialBufferInSeconds
     *            the new initial buffer in seconds
     */
    public void setInitialBufferInSeconds(int initialBufferInSeconds){
        this.initialBufferInSeconds = initialBufferInSeconds;
    }

    /**
     * Gets the streamer hosted ip port.
     *
     * @return the streamer hosted ip port
     */
    public String getStreamerHostedIpPort(){
        return streamerHostedIpPort;
    }

    /**
     * Gets the cps.
     *
     * @return the cps
     */
    public int getCps(){
        return cps;
    }

    /**
     * Sets the cps.
     *
     * @param cps
     *            the new cps
     */
    public void setCps(int cps){
        this.cps = cps;
    }

    /**
     * Gets the call duration in seconds.
     *
     * @return the call duration in seconds
     */
    public int getCallDurationInSeconds(){
        return callDurationInSeconds;
    }

    /**
     * Sets the call duration in seconds.
     *
     * @param callDurationInSeconds
     *            the new call duration in seconds
     */
    public void setCallDurationInSeconds(int callDurationInSeconds){
        this.callDurationInSeconds = callDurationInSeconds;
    }

}
