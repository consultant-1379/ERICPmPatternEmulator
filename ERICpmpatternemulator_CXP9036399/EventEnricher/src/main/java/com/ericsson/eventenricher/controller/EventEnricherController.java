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

package com.ericsson.eventenricher.controller;

import static com.ericsson.configmaster.constants.Constants.CELL_META_DATA_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.ERROR;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.StreamingConfigLoader;
import com.ericsson.eventenricher.cps.services.CpsFeedController;
import com.ericsson.eventenricher.eps.services.EpsFeedController;
import com.ericsson.topologymaster.controller.TopologyMasterController;
import com.ericsson.utilities.entities.CellToPatternMetaDataWrapper;
import com.ericsson.utilities.entities.PatternToCellMetaDataWrapper;
import com.ericsson.utilities.services.Utils;

/**
 * The Class EventEnricherController.
 */
@Service
public class EventEnricherController {
    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The streaming config loader. */
    @Autowired
    private StreamingConfigLoader streamingConfigLoader;

    /** The feed controller. */
    @Autowired
    private EpsFeedController epsFeedController;

    /** The cps feed controller. */
    @Autowired
    private CpsFeedController cpsFeedController;

    private String schemaReleaseVersion;

    /** The cell to pattern meta data list map. */
    private static Map<String, Map<Integer, List<String>>> cellToPatternMetaDataListMap;

    /** The pattern to cell meta data list map. */
    private static Map<Integer, Map<String, List<String>>> patternToCellMetaDataListMap;

    /** The unique process ID. */
    private static String uniqueProcessID;

    static{
        uniqueProcessID = ManagementFactory.getRuntimeMXBean().getName();
    }

    /**
     * Process events and stream.
     *
     * @throws Exception
     *             the exception
     */
    public void processEventsAndStream() throws Exception{
        TopologyMasterController topologyParserController = applicationContext.getBean(TopologyMasterController.class);
        topologyParserController.parseTopologyFiles();

        // Identify if its eps or cps based streaming
        if(streamingConfigLoader.getEps() > 0 && streamingConfigLoader.getCps() == 0){
            deserializeCellMetaDataFile();
            epsFeedController.init(schemaReleaseVersion);
            epsFeedController.startFeeding();
        } else{
            PatternToCellMetaDataWrapper patternToCellMetaDataWrapper = Utils.deserializePatternMetaDataFile(streamingConfigLoader
                    .getPatternLocation());
            patternToCellMetaDataListMap = patternToCellMetaDataWrapper.getPatternToCellMetaDataListMap();
            schemaReleaseVersion = patternToCellMetaDataWrapper.getSchemaReleaseVersion();
            cpsFeedController.init(schemaReleaseVersion);
            cpsFeedController.startFeeding();
        }
    }

    /**
     * Deserialize pattern meta data file.
     *
     * @throws Exception
     *             the exception
     */
    private void deserializeCellMetaDataFile() throws Exception{
        File cellMetaDataFile = new File(streamingConfigLoader.getPatternLocation() + File.separator + CELL_META_DATA_FILE_NAME);
        try{
            FileInputStream fis = new FileInputStream(cellMetaDataFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            CellToPatternMetaDataWrapper cellToPatternMetaDataWrapper = (CellToPatternMetaDataWrapper) ois.readObject();
            cellToPatternMetaDataListMap = cellToPatternMetaDataWrapper.getCellToPatternMetaDataListMap();
            schemaReleaseVersion = cellToPatternMetaDataWrapper.getSchemaReleaseVersion();
            ois.close();
            fis.close();
        } catch(Exception e){
            Utils.logMessage(ERROR,"Unable to read cell meta data file " + e);
            throw e;
        }
    }

    /**
     * Gets the cell to pattern meta data list map.
     *
     * @return the cell to pattern meta data list map
     */
    public static Map<String, Map<Integer, List<String>>> getCellToPatternMetaDataListMap(){
        return cellToPatternMetaDataListMap;
    }

    /**
     * Gets the unique process ID.
     *
     * @return the unique process ID
     */
    public static String getUniqueProcessID(){
        return uniqueProcessID;
    }

    /**
     * Gets the pattern to cell meta data list map.
     *
     * @return the pattern to cell meta data list map
     */
    public static Map<Integer, Map<String, List<String>>> getPatternToCellMetaDataListMap(){
        return patternToCellMetaDataListMap;
    }

}
