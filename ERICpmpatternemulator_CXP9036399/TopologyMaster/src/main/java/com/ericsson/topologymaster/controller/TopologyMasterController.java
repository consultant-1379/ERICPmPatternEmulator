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

package com.ericsson.topologymaster.controller;

import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.UNDERSCORE;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.topologyparser.entities.CellWrapper;
import com.ericsson.topologyparser.entities.NodeWrapper;
import com.ericsson.topologyparser.services.XmlTopologyParserThread;
import com.ericsson.utilities.services.Utils;

/**
 * The Class TopologyParser.
 */
@Service
public class TopologyMasterController {

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /** The node wrapper map. */
    private static Map<String, NodeWrapper> nodeWrapperMap;

    /** The splitted cell info. */
    private static Map<String, CellWrapper> topologyCellInfoMap;

    /** The topology cell list. */
    private static List<String> topologyCellList;

    /**
     * Parses the topology files.
     *
     * @throws Exception
     *             the exception
     */
    public void parseTopologyFiles() throws Exception{
        try{
            nodeWrapperMap = new HashMap<String, NodeWrapper>();
            int poolSize = streamingConfigLoader.getNumberOfThreads();
            File[] topologyFiles = null;
            if(null != streamingConfigLoader.getSelectedNodeList() && !streamingConfigLoader.getSelectedNodeList().isEmpty()){
                topologyFiles = new File(streamingConfigLoader.getTopologyLocation()).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name){
                        if(streamingConfigLoader.getSelectedNodeList().contains(name)){
                            return true;
                        }
                        return false;
                    }
                });
            } else{
                topologyFiles = new File(streamingConfigLoader.getTopologyLocation()).listFiles();
            }

            int numberOfFiles = topologyFiles.length;
            Utils.logMessage(INFO,"Loading topology for " + numberOfFiles + " node(s). ");
            if(numberOfFiles > 0){
                int factor = 1;
                int mod = 0;
                int startIndex = 0;
                int endIndex = 0;
                if(numberOfFiles < poolSize){
                    poolSize = numberOfFiles;
                    mod = -1;
                } else{
                    factor = numberOfFiles / poolSize;
                    mod = numberOfFiles % poolSize;
                }
                ThreadPoolExecutor threadPoolTaskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
                List<File> topologyFileList = Arrays.asList(topologyFiles);
                for(int threadindex = 0; threadindex < poolSize; threadindex++){
                    startIndex = endIndex;
                    if(threadindex == poolSize - 1){
                        endIndex = numberOfFiles;
                    } else{
                        endIndex = endIndex + factor + (mod > 0 ? 1 : 0);
                        mod--;
                    }
                    List<File> topologyFileSubList = topologyFileList.subList(startIndex,endIndex);
                    XmlTopologyParserThread xmlTopologyParserThread = applicationContext.getBean(XmlTopologyParserThread.class);
                    xmlTopologyParserThread.setTopologyFileList(topologyFileSubList);
                    threadPoolTaskExecutor.execute(xmlTopologyParserThread);
                }

                threadPoolTaskExecutor.shutdown();
                while (!threadPoolTaskExecutor.isTerminated()){
                    Thread.sleep(5000);
                }
                loadSplittedCellInfo();
                Utils.logMessage(INFO,"Topology loaded for " + numberOfFiles + " node(s) and " + topologyCellInfoMap.size() + " cell(s).",true);
            } else{
                Utils.logMessage(ERROR,"No files found in the topology location",true);
            }
        } catch(

        Exception e){
            Utils.logMessage(ERROR,"Error occurred while distributing fdn to threads : " + e);
            throw e;
        }

    }

    /**
     * Load splitted cell info.
     */
    private void loadSplittedCellInfo(){
        topologyCellInfoMap = new HashMap<String, CellWrapper>();
        for(String fdn : nodeWrapperMap.keySet()){
            List<CellWrapper> listOfCells = nodeWrapperMap.get(fdn).getCellWrapperList();
            for(CellWrapper cellWrapper : listOfCells){
                topologyCellInfoMap.put(cellWrapper.getCellID() + UNDERSCORE + fdn,cellWrapper);
            }
        }
        topologyCellList = new ArrayList<>(topologyCellInfoMap.keySet());
    }

    /**
     * Gets the node wrapper map.
     *
     * @return the node wrapper map
     */
    public static Map<String, NodeWrapper> getNodeWrapperMap(){
        return nodeWrapperMap;
    }

    /**
     * Gets the topology cell info map.
     *
     * @return the topology cell info map
     */
    public static Map<String, CellWrapper> getTopologyCellInfoMap(){
        return topologyCellInfoMap;
    }

    /**
     * Gets the topology cell list.
     *
     * @return the topology cell list
     */
    public static List<String> getTopologyCellList(){
        return topologyCellList;
    }

}
