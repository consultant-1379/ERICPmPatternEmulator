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

package com.streamer.entities;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.springframework.stereotype.Component;

/**
 * The Class Connection.
 */
@Component
public class Connection {

    /** The socket channel. */
    private SocketChannel socketChannel;

    /** The source IP. */
    private String sourceIP;

    /** The dest IP. */
    private String destIP;

    /** The dest port. */
    private Integer destPort;

    /**
     * Open connection.
     *
     * @return true, if successful
     */
    public boolean openConnection(){
        try{
            InetAddress destAddress = InetAddress.getByName(destIP);
            InetAddress srcAddress = InetAddress.getByName("127.0.0.1");

            socketChannel = SocketChannel.open();
            socketChannel.bind(new InetSocketAddress(srcAddress, 0));
            socketChannel.socket().connect(new InetSocketAddress(destAddress, destPort),10 * 1000); // Connection timeout after 10
                                                                                                    // seconds
            socketChannel.configureBlocking(true);
            if(socketChannel.finishConnect()){
                socketChannel.configureBlocking(false);
                return true;
            } else{
                return false;
            }
        } catch(Exception e){
            System.out.println("Error in opening connection :" + e);
            return false;
        }
    }

    /**
     * Close.
     */
    public void close(){
        try{
            if(null != socketChannel){
                socketChannel.close();
            }
        } catch(Exception e){
        }
    }

    /**
     * Write.
     *
     * @param event
     *            the event
     * @return true, if successful
     */
    public boolean write(ByteBuffer event){
        try{
            event.flip();
            socketChannel.write(event);
            while (event.hasRemaining()){
                socketChannel.write(event);
            }
            return true;
        } catch(Exception e){
            close();
            return false;
        }
    }

    /**
     * Gets the dest IP.
     *
     * @return the destIP
     */
    public String getDestIP(){
        return destIP;
    }

    /**
     * Sets the dest IP.
     *
     * @param destIP
     *            the destIP to set
     */
    public void setDestIP(String destIP){
        this.destIP = destIP;
    }

    /**
     * Gets the dest port.
     *
     * @return the destPort
     */
    public Integer getDestPort(){
        return destPort;
    }

    /**
     * Sets the dest port.
     *
     * @param destPort
     *            the destPort to set
     */
    public void setDestPort(Integer destPort){
        this.destPort = destPort;
    }

    /**
     * Gets the source IP.
     *
     * @return the source IP
     */
    public String getSourceIP(){
        return sourceIP;
    }

    /**
     * Sets the source IP.
     *
     * @param sourceIP
     *            the new source IP
     */
    public void setSourceIP(String sourceIP){
        this.sourceIP = sourceIP;
    }

}
