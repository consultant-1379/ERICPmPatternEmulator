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

package com.streamer.controller;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.utilities.entities.EventStreamerWrapper;
import com.ericsson.utilities.entities.HeaderStreamerWrapper;
import com.streamer.services.ConnectionManager;
import com.streamer.services.EventStreamer;
import com.streamer.services.Instrumentation;
import com.streamer.services.StreamerAppLogger;

/**
 * The Class StreamingController.
 */
@RestController
@EnableAutoConfiguration
@ComponentScan("com.streamer")
public class StreamingController {

    /** The app path. */
    private static String appPath;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The process to temp list. */
    private Map<String, List<List<EventStreamerWrapper>>> processToTempEventsList;

    /** The process to connection manager map. */
    private Map<String, ConnectionManager> processToConnectionManagerMap;

    /** The process to streamer map. */
    private Map<String, EventStreamer> processToStreamerMap;

    /**
     * Inits the.
     */
    @PostConstruct
    public void init(){
        processToConnectionManagerMap = new HashMap<>();
        processToStreamerMap = new HashMap<>();
        processToTempEventsList = new ConcurrentHashMap<>();
    }

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    @RequestMapping("/")
    public static void main(String[] args){
        appPath = args[0];
        SpringApplication app = new SpringApplication(StreamingController.class);
        Map<String, Object> customPropertyMap = new HashMap<>(1);
        customPropertyMap.put("SERVER_PORT",args[1]);
        app.setDefaultProperties(customPropertyMap);
        app.run(args);
        System.out.println("Streaming mode started");
        StreamerAppLogger.intializeLogger(appPath);
        StreamerAppLogger.getLOGGER().info("Streaming application is hosted and ready to accept the requests");
    }

    /**
     * Creates the connection.
     *
     * @param request
     *            the request
     * @param header_data
     *            the header data
     * @param ip_port
     *            the ip port
     * @param processID
     *            the process ID
     * @return the int
     */
    @RequestMapping(value = "/stream_headers", method = RequestMethod.POST)
    private int createConnection(HttpServletRequest request){
        try{
            ObjectInputStream objIn = new ObjectInputStream(request.getInputStream());
            HeaderStreamerWrapper headerStreamerWrapper = (HeaderStreamerWrapper) objIn.readObject();
            List<String> destIpPortList = headerStreamerWrapper.getDestIpPortList();
            Map<String, byte[]> nodeHeaderDetails = headerStreamerWrapper.getNodeHeaderMap();
            if(null != nodeHeaderDetails && !nodeHeaderDetails.isEmpty() && null != destIpPortList && !destIpPortList.isEmpty()
                    && null != headerStreamerWrapper.getProcessID()){
                ConnectionManager connectionManager = applicationContext.getBean(ConnectionManager.class);
                connectionManager.setProcessID(headerStreamerWrapper.getProcessID());
                connectionManager.getNodeHeaderMap().putAll(nodeHeaderDetails);
                connectionManager.mapFdnToConnection(destIpPortList,nodeHeaderDetails);
                if(!connectionManager.writeHeader(nodeHeaderDetails.keySet())){
                    return 1;
                }

                EventStreamer eventStreamer = applicationContext.getBean(EventStreamer.class);
                eventStreamer.setConnectionManager(connectionManager);
                eventStreamer.setProcessID(headerStreamerWrapper.getProcessID());
                eventStreamer.init(applicationContext.getBean(Instrumentation.class),appPath);

                processToConnectionManagerMap.put(headerStreamerWrapper.getProcessID(),connectionManager);
                processToStreamerMap.put(headerStreamerWrapper.getProcessID(),eventStreamer);
            }
        } catch(Exception e){
            StreamerAppLogger.getLOGGER().severe("Exception occured while receiving header data from stream processor. " + e);
            return 1;
        }
        return 0;
    }

    /**
     * Stream data.
     *
     * @param request
     *            the request
     * @return the int
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/stream_events", method = RequestMethod.POST)
    private int streamEvents(HttpServletRequest request){
        try{

            ObjectInputStream objIn = new ObjectInputStream(request.getInputStream());
            List<EventStreamerWrapper> eventsToStreamList = (List<EventStreamerWrapper>) objIn.readObject();
            String processID = request.getHeader("processID");
            if(null == processToTempEventsList.get(processID)){
                processToTempEventsList.put(processID,new LinkedList<List<EventStreamerWrapper>>());
            }
            List<List<EventStreamerWrapper>> tempList = processToTempEventsList.get(processID);
            tempList.add(eventsToStreamList);
            processToTempEventsList.put(processID,tempList);

            if(tempList.size() == Integer.parseInt(request.getHeader("preparedDataDuration"))){
                processToStreamerMap.get(processID).setDataBackup(tempList);
                tempList.clear();
                processToTempEventsList.put(processID,tempList);
                StreamerAppLogger.getLOGGER().info(processID + " : Event data received");
            }
        } catch(Exception e){
            StreamerAppLogger.getLOGGER().severe("Exception occured while receiving event data from stream processor. " + e);
            return 1;
        }
        return 0;
    }

}
