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
package com.ericsson.utilities.services;

import static com.ericsson.configmaster.constants.Constants.ERROR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.SerializableEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.utilities.entities.EventStreamerWrapper;
import com.ericsson.utilities.entities.HeaderStreamerWrapper;

/**
 * The Class HttpRequestHandler.
 */
@Service
public class HttpRequestHandler {

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /** The header streamer wrapper. */
    @Autowired
    private HeaderStreamerWrapper headerStreamerWrapper;

    /**
     * Process header data.
     *
     * @param nodeHeaderMap
     *            the node header map
     * @param processID
     *            the process ID
     */
    public void processHeaderData(Map<String, byte[]> nodeHeaderMap, String processID){
        headerStreamerWrapper.setNodeHeaderMap(nodeHeaderMap);
        headerStreamerWrapper.setProcessID(processID);
        headerStreamerWrapper.setDestIpPortList(new ArrayList<>(streamingConfigLoader.getListDestinationIpPort()));
        sendHeaderData();
    }

    /**
     * Send header data.
     */
    public void sendHeaderData(){
        try{
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://" + streamingConfigLoader.getStreamerHostedIpPort() + "//stream_headers");
            httppost.setEntity(new SerializableEntity(headerStreamerWrapper, false));
            handleResponse(httpclient,httppost);
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occurred while sending header data to streamer : " + e);
        }
    }

    /**
     * Send event data.
     *
     * @param eventsToStreamList
     *            the events to stream list
     * @param preparedDataDuration
     *            the prepared data duration
     * @param processID
     *            the process ID
     */
    public void sendEventData(List<EventStreamerWrapper> eventsToStreamList, int preparedDataDuration, String processID){
        try{
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://" + streamingConfigLoader.getStreamerHostedIpPort() + "//stream_events");
            httppost.addHeader("preparedDataDuration",String.valueOf(preparedDataDuration));
            httppost.addHeader("processID",processID);
            httppost.setEntity(new SerializableEntity((Serializable) eventsToStreamList, false));
            handleResponse(httpclient,httppost);
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occurred while sending event data to streamer : " + e);
        }
    }

    /**
     * Handle response.
     *
     * @param httpclient
     *            the httpclient
     * @param httppost
     *            the httppost
     */
    private void handleResponse(HttpClient httpclient, HttpPost httppost){
        try{
            httpclient.execute(httppost);
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occured in http responese " + e,true);
        }
    }

}
