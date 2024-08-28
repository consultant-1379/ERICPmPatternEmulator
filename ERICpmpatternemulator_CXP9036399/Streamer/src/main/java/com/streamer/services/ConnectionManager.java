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

package com.streamer.services;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.streamer.entities.Connection;

/**
 * The Class ConnectionManager.
 */
@Component
public class ConnectionManager {

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The node header map. */
    private Map<String, byte[]> nodeHeaderMap;

    /** The node to connection obj map. */
    private Map<String, Connection> nodeToConnectionObjMap;

    /** The process ID. */
    private String processID;

    /**
     * Map fdn to connection.
     *
     * @param destinationAddressInfo
     *            the destination address info
     * @param fdnDetails
     *            the fdn details
     */
    public void mapFdnToConnection(List<String> destinationAddressInfo, Map<String, byte[]> fdnDetails){
        nodeToConnectionObjMap = new HashMap<>();
        int fdnCounter = 0;
        for(String fdn : fdnDetails.keySet()){
            String destinationAddress;
            if(fdnCounter == destinationAddressInfo.size()){
                fdnCounter = 0;
            }
            destinationAddress = destinationAddressInfo.get(fdnCounter);
            createConnectionObject(destinationAddress.split(":"),fdn);
            fdnCounter++;
        }
        StreamerAppLogger.getLOGGER().info(processID + " : Total number of nodes : " + nodeToConnectionObjMap.keySet().size());
    }

    /**
     * Creates the connection object.
     *
     * @param con
     *            the con
     * @param fdn
     *            the fdn
     */
    private void createConnectionObject(String[] con, String fdn){
        String sourceIP = "127.0.0.1";
        try{
            if(isAddressIPv4(con[0])){
                sourceIP = InetAddress.getLocalHost().getHostAddress();
            } else if(isAddressIPv6(con[0])){
                sourceIP = InetAddress.getLocalHost().getHostAddress();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        Connection connection = applicationContext.getBean(Connection.class);
        connection.setSourceIP(sourceIP);
        connection.setDestIP(con[0]);
        connection.setDestPort(Integer.parseInt(con[1]));
        nodeToConnectionObjMap.put(fdn,connection);
    }

    /**
     * Write header.
     *
     * @param set
     *            the set
     * @return true, if successful
     */
    public boolean writeHeader(Set<String> set){
        for(String fdn : set){
            Connection con = nodeToConnectionObjMap.get(fdn);
            if(con.openConnection()){
                if(con.write(updateHeaderTimestamp(fdn))){
                    StreamerAppLogger.getLOGGER().info(processID + " : Header Sent Successfully to " + con.getDestIP() + ":" + con.getDestPort()
                            + " for " + fdn);
                } else{
                    closeAllConnections();
                    return false;
                }
            } else{
                closeAllConnections();
                return false;
            }
        }
        return true;
    }

    /**
     * Close all connections.
     */
    public void closeAllConnections(){
        for(Connection con : nodeToConnectionObjMap.values()){
            con.close();
        }
        StreamerAppLogger.getLOGGER().info(processID + " : Destination Server is not reachable please check the logs for more details.");
    }

    /**
     * Gets the node to connection obj map.
     *
     * @return the node to connection obj map
     */
    public Map<String, Connection> getNodeToConnectionObjMap(){
        return nodeToConnectionObjMap;
    }

    /**
     * Gets the node header map.
     *
     * @return the nodeHeaderMap
     */
    public Map<String, byte[]> getNodeHeaderMap(){
        if(null == nodeHeaderMap){
            nodeHeaderMap = new HashMap<>();
        }
        return nodeHeaderMap;
    }

    /**
     * Checks if is address I pv 4.
     *
     * @param ipAddress
     *            the ip address
     * @return true, if is address I pv 4
     */
    public boolean isAddressIPv4(String ipAddress){
        boolean result = false;
        if(ipAddress.contains(".") && Character.isDigit(ipAddress.charAt(0))){
            result = true;
        }
        return result;
    }

    /**
     * Checks if is address I pv 6.
     *
     * @param ipAddress
     *            the ip address
     * @return true, if is address I pv 6
     */
    public boolean isAddressIPv6(String ipAddress){
        boolean result = false;
        if(ipAddress.contains(":")){
            result = true;
        }
        return result;
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

    /**
     * Gets the time stamp byte array.
     *
     * @return the time stamp byte array
     */
    public byte[] getTimeStampByteArray(){
        ByteBuffer timeBuf = ByteBuffer.allocate(9);
        // get the current timestamp in UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(System.currentTimeMillis());

        timeBuf.putShort((short) cal.get(Calendar.YEAR)); // 2 bytes
        timeBuf.put((byte) (cal.get(Calendar.MONTH) + 1)); // 1 byte = calender month counts from zero
        timeBuf.put((byte) cal.get(Calendar.DAY_OF_MONTH)); // 1 byte
        timeBuf.put((byte) cal.get(Calendar.HOUR_OF_DAY)); // 1 byte
        timeBuf.put((byte) cal.get(Calendar.MINUTE)); // 1 byte
        timeBuf.put((byte) cal.get(Calendar.SECOND)); // 1 byte
        timeBuf.putShort((short) cal.get(Calendar.MILLISECOND)); // 2 bytes

        return timeBuf.array();
    }

    /**
     * Update header timestamp.
     *
     * @param fdn
     *            the fdn
     * @return the byte buffer
     */
    private ByteBuffer updateHeaderTimestamp(String fdn){
        byte[] headerTime = getTimeStampByteArray();
        ByteBuffer headerByteBuffer = ByteBuffer.wrap(nodeHeaderMap.get(fdn));
        if(headerByteBuffer.limit() == 425){
            headerByteBuffer.position(27); // Starting position of timestamp parameters for T releases
        } else{
            headerByteBuffer.position(14); // Starting position of timestamp parameters for lower releases
        }
        headerByteBuffer.put(headerTime,0,7);
        headerByteBuffer.position(headerByteBuffer.capacity());
        return headerByteBuffer;
    }
}
