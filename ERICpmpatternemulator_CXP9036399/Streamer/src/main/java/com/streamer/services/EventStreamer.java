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
package com.streamer.services;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ericsson.utilities.entities.EventStreamerWrapper;
import com.streamer.entities.Connection;

@Component
public class EventStreamer {

    /** The connection manager. */
    private ConnectionManager connectionManager;

    /** The inst. */
    private Instrumentation inst;

    /** The data holder. */
    private Queue<List<com.ericsson.utilities.entities.EventStreamerWrapper>> dataHolder;

    /** The data backup. */
    private List<List<EventStreamerWrapper>> dataBackup;

    /** The process ID. */
    private String processID;

    public void init(Instrumentation inst, String appPath){
        StreamerAppLogger.getLOGGER().info(processID + " : Started stream timer");
        dataHolder = new LinkedList<List<EventStreamerWrapper>>();
        dataBackup = new LinkedList<>();
        this.inst = inst;
        this.inst.init(processID,appPath);
    }

    @Scheduled(fixedRate = 1000)
    public void streamEvents(){
        List<EventStreamerWrapper> eventsToStreamList = dataHolder.poll();
        if(null == eventsToStreamList){
            dataHolder.addAll(dataBackup);
            eventsToStreamList = dataHolder.poll();
        }

        if(null != eventsToStreamList){
            byte[] eventTime = connectionManager.getTimeStampByteArray();
            for(EventStreamerWrapper eventStreamerWrapperObj : eventsToStreamList){
                ByteBuffer eventData = setEventTime(eventStreamerWrapperObj.getEventByte(),eventTime);
                if(writeEvents(eventStreamerWrapperObj.getFdn(),eventData)){
                    inst.updateInstrumentation();
                }
            }
        }
    }

    private boolean writeEvents(String fdn, ByteBuffer eventByteBuffer){
        Connection connection = connectionManager.getNodeToConnectionObjMap().get(fdn);
        if(!connection.write(eventByteBuffer)){
            inst.updateFailedInstrumentation();
        }
        return true;
    }

    public ByteBuffer setEventTime(byte[] buf, byte[] timeStamp){
        ByteBuffer eventByteBuffer = ByteBuffer.wrap(buf);
        eventByteBuffer.position(7); // Starting position of timestamp parameters
        eventByteBuffer.put(timeStamp,4,5);
        eventByteBuffer.position(eventByteBuffer.capacity());
        return eventByteBuffer;
    }

    public void setDataBackup(List<List<EventStreamerWrapper>> dataBackup){
        getDataHolder().addAll(dataBackup);
        this.dataBackup.clear();
        this.dataBackup.addAll(dataBackup);
    }

    /**
     * Gets the data holder.
     *
     * @return the data holder
     */
    public Queue<List<EventStreamerWrapper>> getDataHolder(){
        return this.dataHolder;
    }

    /**
     * Gets the data backup.
     *
     * @return the data backup
     */
    public List<List<EventStreamerWrapper>> getDataBackup(){
        return this.dataBackup;
    }

    /**
     * Sets the connection manager.
     *
     * @param connectionManager
     *            the new connection manager
     */
    public void setConnectionManager(ConnectionManager connectionManager){
        this.connectionManager = connectionManager;
    }

    /**
     * Sets the process ID.
     *
     * @param processID
     *            the new process ID
     */
    public void setProcessID(String processID){
        this.processID = processID;
    }
}
