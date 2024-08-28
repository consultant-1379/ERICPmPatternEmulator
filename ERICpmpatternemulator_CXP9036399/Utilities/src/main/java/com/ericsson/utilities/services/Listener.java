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

package com.ericsson.utilities.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class Listener.
 */
public class Listener {

    /** The selector. */
    private Selector selector;

    /** The server channel. */
    private ServerSocketChannel serverChannel;

    /** The address. */
    private String address;

    /** The data tracking. */
    private Map<SocketChannel, List<byte[]>> dataTracking;

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String args[]){
        try{
            int startPort = Integer.parseInt(args[0]);
            int endPort = Integer.parseInt(args[1]);
            Listener listener = new Listener();
            listener.registerChannels(startPort,endPort);
            listener.start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Register channels.
     *
     * @param startPort
     *            the start port
     * @param endPort
     *            the end port
     * @throws Exception
     *             the exception
     */
    private void registerChannels(int startPort, int endPort) throws Exception{
        address = "127.0.0.1";// InetAddress.getLocalHost().getHostAddress();
        this.selector = Selector.open();
        for(int portNumber = startPort; portNumber <= endPort; portNumber++){
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);

            InetSocketAddress listenAddr = new InetSocketAddress(address, portNumber);
            serverChannel.socket().bind(listenAddr);
            serverChannel.register(this.selector,SelectionKey.OP_ACCEPT);
            System.out.println(portNumber + " port opened for IP : " + address);
        }
        dataTracking = new HashMap<SocketChannel, List<byte[]>>();
    }

    /**
     * Start.
     *
     * @throws Exception
     *             the exception
     */
    private void start() throws Exception{
        while (!Thread.currentThread().isInterrupted()){
            selector.select();
            Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
            while (keys.hasNext()){
                SelectionKey key = keys.next();
                keys.remove();
                if(!key.isValid()){
                    continue;
                }
                if(key.isAcceptable()){
                    accept(key);
                } else if(key.isReadable()){
                    read(key);
                } else if(key.isWritable()){
                    write(key);
                }
            }
        }
    }

    /**
     * Accept.
     *
     * @param key
     *            the key
     * @throws Exception
     *             the exception
     */
    private void accept(SelectionKey key) throws Exception{
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        dataTracking.put(channel,new ArrayList<byte[]>());
        channel.register(this.selector,SelectionKey.OP_READ);
    }

    /**
     * Read.
     *
     * @param key
     *            the key
     * @throws Exception
     *             the exception
     */
    private void read(SelectionKey key) throws Exception{
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int numRead = -1;
        try{
            numRead = channel.read(buffer);
        } catch(IOException e){
            e.printStackTrace();
        }

        if(numRead == -1){
            dataTracking.remove(channel);
            channel.close();
            key.cancel();
            return;
        }
    }

    /**
     * Write.
     *
     * @param key
     *            the key
     * @throws Exception
     *             the exception
     */
    private void write(SelectionKey key) throws Exception{
        SocketChannel channel = (SocketChannel) key.channel();
        List<byte[]> pendingData = dataTracking.get(channel);
        Iterator<byte[]> items = pendingData.iterator();
        while (items.hasNext()){
            byte[] item = items.next();
            items.remove();
            channel.write(ByteBuffer.wrap(item));
        }
        key.interestOps(SelectionKey.OP_READ);
    }

}
