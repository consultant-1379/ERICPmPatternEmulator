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
package com.ericsson.patternextractor.services;

import static com.ericsson.configmaster.constants.Constants.CELL_META_DATA_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.COLON;
import static com.ericsson.configmaster.constants.Constants.COMMA;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.FILE_BUFFER_SIZE;
import static com.ericsson.configmaster.constants.Constants.GZ_EXTENSION;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.NEWLINE;
import static com.ericsson.configmaster.constants.Constants.PATTERN_INFO_CSV_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.PATTERN_META_DATA_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.SER_EXTENSION;
import static com.ericsson.configmaster.constants.Constants.UNDERSCORE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.PatternConfigLoader;
import com.ericsson.patternextractor.entities.SessionEvent;
import com.ericsson.patternextractor.entities.SessionPerCall;
import com.ericsson.utilities.entities.CellToPatternMetaDataWrapper;
import com.ericsson.utilities.entities.PatternToCellMetaDataWrapper;
import com.ericsson.utilities.entities.SessionPerCallWrapper;
import com.ericsson.utilities.services.Utils;

/**
 * The Class PatternMetaDataWriter.
 */
@Service
public class PatternMetaDataWriter {

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The pattern config loader. */
    @Autowired
    private PatternConfigLoader patternConfigLoader;

    /** The event stream wrapper obj. */
    @SuppressWarnings("unused")
    @Autowired
    private SessionPerCallWrapper sessionPerCallWrapperObj;

    /** The event stream wrapper obj. */
    @SuppressWarnings("unused")
    @Autowired
    private CellToPatternMetaDataWrapper cellToPatternMetaDataWrapper;

    /** The event stream wrapper obj. */
    @SuppressWarnings("unused")
    @Autowired
    private PatternToCellMetaDataWrapper patternToCellMetaDataWrapper;

    /** The event iterator. */
    @Autowired
    private EventIterator eventIterator;

    /**
     * Serialize session per call.
     *
     * @param patternFolder
     *            the pattern folder
     * @param sessionPerCall
     *            the session per call
     * @return the file
     */
    public File serializeSessionPerCall(File patternFolder, SessionPerCall sessionPerCall){
        SessionPerCallWrapper sessionPerCallWrapper = applicationContext.getBean(SessionPerCallWrapper.class);
        List<byte[]> eventDataList = new ArrayList<>();
        for(SessionEvent sessionEvent : sessionPerCall.getEventObjList()){
            if(sessionEvent.getTypeOfRelation() != 0){
                sessionPerCallWrapper.setTypeOfRelation(sessionEvent.getTypeOfRelation());
            }
            eventDataList.add(sessionEvent.getEventData());
        }
        sessionPerCallWrapper.setEventIDList(sessionPerCall.getEventIDList());
        sessionPerCallWrapper.setEventDataList(eventDataList);
        File sessionFile = new File(patternFolder + File.separator + "Session" + UNDERSCORE + sessionPerCall.getSessionID() + SER_EXTENSION
                + GZ_EXTENSION);
        if(sessionFile.exists()){
            sessionFile = new File(patternFolder + File.separator + "Session" + UNDERSCORE + sessionPerCall.getSessionID() + UNDERSCORE
                    + sessionPerCall.getEventObjList().get(0).getDateTime().getTime() + SER_EXTENSION + GZ_EXTENSION);
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(sessionFile), FILE_BUFFER_SIZE))){
            oos.writeObject(sessionPerCallWrapper);
        } catch(IOException e){
            Utils.logMessage(ERROR,"Error while writing session objects to file " + e);
        }
        return sessionFile;
    }

    /**
     * Write patterns info file.
     *
     * @param basePatternMap
     *            the base pattern map
     */
    public void writePatternsInfoFile(Map<Integer, Map<File, SessionPerCall>> basePatternMap){
        try{
            Utils.logMessage(INFO,"Writing pattern details to pattern_info text file");
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(patternConfigLoader.getOutputLocation() + File.separator
                    + PATTERN_INFO_CSV_FILE_NAME)));
            bw.write("Pattern_ID,No. of sessions,Events count in session,Event list,Pattern frequency" + NEWLINE);
            for(Integer finalPatternID : basePatternMap.keySet()){
                Entry<File, SessionPerCall> entry = basePatternMap.get(finalPatternID).entrySet().iterator().next();
                bw.write(finalPatternID + COMMA + basePatternMap.get(finalPatternID).size() + COMMA + entry.getValue().getEventIDList().size() + COMMA
                        + StringUtils.join(entry.getValue().getEventIDList(),COLON) + NEWLINE);
            }
            bw.close();
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error while writing pattern info file " + e);
        }
    }

    /**
     * Serialize cell based pattern map.
     *
     * @param basePatternMap
     *            the base pattern map
     */
    public void writeCellBasedPatternMap(Map<Integer, Map<File, SessionPerCall>> basePatternMap){
        Utils.logMessage(INFO,"Distributing patterns and session files based on the cell IDs");
        Map<String, Map<Integer, List<String>>> cellToPatternMetaDataListMap = new HashMap<>();
        for(Integer finalPatternID : basePatternMap.keySet()){
            Map<File, SessionPerCall> finalSessionMap = basePatternMap.get(finalPatternID);
            for(File sessionFile : finalSessionMap.keySet()){
                String cellID = finalSessionMap.get(sessionFile).getEventObjList().get(0).getCellID();
                Map<Integer, List<String>> patternToSessionFileNameListMap = cellToPatternMetaDataListMap.get(cellID);
                if(null == patternToSessionFileNameListMap){
                    patternToSessionFileNameListMap = new HashMap<>();
                    List<String> sessionFileNameList = new ArrayList<String>();
                    sessionFileNameList.add(sessionFile.getName());
                    patternToSessionFileNameListMap.put(finalPatternID,sessionFileNameList);
                } else{
                    List<String> sessionFileNameList = patternToSessionFileNameListMap.get(finalPatternID);
                    if(null == sessionFileNameList){
                        sessionFileNameList = new ArrayList<>();
                    }
                    sessionFileNameList.add(sessionFile.getName());
                    patternToSessionFileNameListMap.put(finalPatternID,sessionFileNameList);
                }
                cellToPatternMetaDataListMap.put(cellID,patternToSessionFileNameListMap);
            }
        }
        serializeCellBasedPatternMap(cellToPatternMetaDataListMap);
    }

    /**
     * Serialize cell based pattern map.
     *
     * @param cellToPatternMetaDataListMap
     *            the cell to pattern meta data list map
     */
    private void serializeCellBasedPatternMap(Map<String, Map<Integer, List<String>>> cellToPatternMetaDataListMap){
        Utils.logMessage(INFO,"Writing cell details to cell_meta_data file");
        CellToPatternMetaDataWrapper cellToPatternMetaDataWrapper = applicationContext.getBean(CellToPatternMetaDataWrapper.class);
        cellToPatternMetaDataWrapper.setCellToPatternMetaDataListMap(cellToPatternMetaDataListMap);
        cellToPatternMetaDataWrapper.setSchemaReleaseVersion(eventIterator.getSchemaReleaseObj().getReleaseName());

        File cellMetaDataFile = new File(patternConfigLoader.getOutputLocation() + File.separator + CELL_META_DATA_FILE_NAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cellMetaDataFile))){
            oos.writeObject(cellToPatternMetaDataWrapper);
        } catch(IOException e){
            Utils.logMessage(ERROR,"Error while writing cell meta data file " + e);
        }
    }

    /**
     * Write pattern based cell meta data.
     *
     * @param basePatternMap
     *            the base pattern map
     */
    public void writePatternBasedCellMetaData(Map<Integer, Map<File, SessionPerCall>> basePatternMap){
        Map<Integer, Map<String, List<String>>> patternToCellMetaDataListMap = new HashMap<>();
        Map<Integer, List<Integer>> patternToEventListMap = new HashMap<>();
        for(Integer finalPatternID : basePatternMap.keySet()){
            Map<String, List<String>> cellMap = patternToCellMetaDataListMap.get(finalPatternID);
            if(null == cellMap){
                cellMap = new HashMap<>();
            }
            Map<File, SessionPerCall> finalSessionMap = basePatternMap.get(finalPatternID);
            for(File sessionFile : finalSessionMap.keySet()){
                String cellID = finalSessionMap.get(sessionFile).getEventObjList().get(0).getCellID();
                List<String> sessionList = cellMap.get(cellID);
                if(null == sessionList){
                    sessionList = new ArrayList<>();
                }
                sessionList.add(sessionFile.getName());
                cellMap.put(cellID,sessionList);
            }
            patternToCellMetaDataListMap.put(finalPatternID,cellMap);
            patternToEventListMap.put(finalPatternID,new ArrayList<>(finalSessionMap.get(finalSessionMap.keySet().toArray()[0]).getEventIDList()));
        }
        serializePatternBasedCellMetaDataMap(patternToCellMetaDataListMap,patternToEventListMap);
    }

    /**
     * Serialize pattern based cell meta data map.
     *
     * @param patternToCellMetaDataListMap
     *            the pattern to cell meta data list map
     * @param patternToEventListMap
     */
    private void serializePatternBasedCellMetaDataMap(Map<Integer, Map<String, List<String>>> patternToCellMetaDataListMap,
            Map<Integer, List<Integer>> patternToEventListMap){
        Utils.logMessage(INFO,"Writing pattern details to pattern_meta_data file");
        PatternToCellMetaDataWrapper patternToCellMetaDataWrapper = applicationContext.getBean(PatternToCellMetaDataWrapper.class);
        patternToCellMetaDataWrapper.setPatternToCellMetaDataListMap(patternToCellMetaDataListMap);
        patternToCellMetaDataWrapper.setSchemaReleaseVersion(eventIterator.getSchemaReleaseObj().getReleaseName());
        patternToCellMetaDataWrapper.setPatternToEventListMap(patternToEventListMap);

        File patternMetaDataFile = new File(patternConfigLoader.getOutputLocation() + File.separator + PATTERN_META_DATA_FILE_NAME);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(patternMetaDataFile))){
            oos.writeObject(patternToCellMetaDataWrapper);
        } catch(IOException e){
            Utils.logMessage(ERROR,"Error while writing pattern meta data file " + e);
        }
    }
}
