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

import static com.ericsson.configmaster.constants.Constants.INFO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.configmaster.services.PatternConfigLoader;
import com.ericsson.patternextractor.entities.SessionEvent;
import com.ericsson.patternextractor.entities.SessionPerCall;
import com.ericsson.utilities.services.Utils;

/**
 * The Class PatternIdentifierThread.
 */
@Component
public class PatternIdentifierThread implements Runnable {

    /** The base pattern map. */
    private Map<Integer, Map<File, SessionPerCall>> basePatternMap;

    /** The event list for each pattern. */
    private List<List<Integer>> eventListForEachPattern;

    /** The output folder map. */
    private Map<Integer, File> outputFolderMap;

    /** The pattern config loader. */
    @Autowired
    private PatternConfigLoader patternConfigLoader;

    /** The pattern meta data writer. */
    @Autowired
    private PatternMetaDataWriter patternMetaDataWriter;

    /** The pattern extractor. */
    @Autowired
    private EventIterator fileProcessor;

    /** The total number of sessions. */
    private int totalNumberOfSessions;

    /**
     * Run.
     */
    @Override
    public void run(){
        basePatternMap = new TreeMap<>();
        eventListForEachPattern = new ArrayList<List<Integer>>();
        outputFolderMap = new HashMap<>();
        while (fileProcessor.isTerminationFlag()){
            if(fileProcessor.getSharedQueue().isEmpty()){
                try{
                    Thread.sleep(500);
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            } else{
                identifyBasePatternID();
            }
        }
        if(!fileProcessor.getSharedQueue().isEmpty()){
            Utils.logMessage(INFO,"Number of sessions remaining to be processed after termination of session identifier threads " + fileProcessor
                    .getSharedQueue().size());
            identifyBasePatternID();
        }
        Utils.logMessage(INFO,"Number of base patterns identified : " + basePatternMap.size());
        Utils.logMessage(INFO,"Number of sessions identified : " + totalNumberOfSessions);

        // Divide identified patterns based on value
        if(!patternConfigLoader.getPatternSelectionAttributesSet().isEmpty()){
            identifySecondLevelOfPatternID();
            Utils.logMessage(INFO,"Total number of patterns after comparing attributes value : " + basePatternMap.size());
        }
        patternMetaDataWriter.writePatternsInfoFile(basePatternMap);
        patternMetaDataWriter.writeCellBasedPatternMap(basePatternMap);
        patternMetaDataWriter.writePatternBasedCellMetaData(basePatternMap);
    }

    /**
     * Identify base pattern ID.
     */
    private void identifyBasePatternID(){
        while (!fileProcessor.getSharedQueue().isEmpty()){
            // poll session from queue
            SessionPerCall sessionPerCall = fileProcessor.getSharedQueue().poll();
            Collections.sort(sessionPerCall.getEventObjList());
            List<Integer> eventIDList = new ArrayList<Integer>();
            for(SessionEvent eventObj : sessionPerCall.getEventObjList()){
                eventIDList.add(eventObj.getEventID());
            }
            sessionPerCall.setEventIDList(eventIDList);
            int patternID = eventListForEachPattern.indexOf(eventIDList);
            if(patternID == -1){
                eventListForEachPattern.add(eventIDList);
                patternID = eventListForEachPattern.size() - 1;
                // Create new pattern folder at output location
                File patternFolder = new File(patternConfigLoader.getOutputLocation() + File.separator + patternID);
                patternFolder.mkdir();
                outputFolderMap.put(patternID,patternFolder);
            }
            Map<File, SessionPerCall> sessionPerCallFileMap = basePatternMap.get(patternID);
            if(null == sessionPerCallFileMap){
                sessionPerCallFileMap = new HashMap<File, SessionPerCall>();
            }
            File sessionFile = patternMetaDataWriter.serializeSessionPerCall(outputFolderMap.get(patternID),sessionPerCall);
            totalNumberOfSessions++;
            sessionPerCallFileMap.put(sessionFile,sessionPerCall);
            basePatternMap.put(patternID,sessionPerCallFileMap);
        }
    }

    /**
     * Identify second level of pattern ID.
     */
    private void identifySecondLevelOfPatternID(){
        int patternIncrementer = basePatternMap.size() - 1;
        Map<Integer, Map<File, SessionPerCall>> valueBasedPatternFileMap = new HashMap<>();
        for(int basePatternID : basePatternMap.keySet()){
            Map<File, SessionPerCall> baseSessionPerCallFileMap = basePatternMap.get(basePatternID);
            Map<File, SessionPerCall> baseSessionPerCallFileMapCopy = new HashMap<File, SessionPerCall>(baseSessionPerCallFileMap);

            // compare all sessions of a pattern to further divide into new patterns based on pattern selection parameter
            Iterator<Entry<File, SessionPerCall>> copyIterator = baseSessionPerCallFileMapCopy.entrySet().iterator();
            for(File sessionFile : baseSessionPerCallFileMap.keySet()){
                Map<File, SessionPerCall> sameValueFileMap = new HashMap<>();
                SessionPerCall origSessionPerCallObj = baseSessionPerCallFileMap.get(sessionFile);
                while (copyIterator.hasNext()){
                    Entry<File, SessionPerCall> entry = copyIterator.next();
                    if(origSessionPerCallObj.compareTo(entry.getValue()) == 0){
                        sameValueFileMap.put(entry.getKey(),entry.getValue());
                        copyIterator.remove();
                    }
                }
                if(!sameValueFileMap.isEmpty()){
                    if(valueBasedPatternFileMap.isEmpty()){
                        valueBasedPatternFileMap.put(basePatternID,sameValueFileMap);
                    } else{
                        patternIncrementer = patternIncrementer + 1;
                        valueBasedPatternFileMap.put(patternIncrementer + 1,sameValueFileMap);
                    }
                }
            }
        }
        copyFilesForNewPatterns(valueBasedPatternFileMap);
    }

    /**
     * Copy files for new patterns.
     *
     * @param valueBasedPatternFileMap
     *            the value based pattern file map
     */
    private void copyFilesForNewPatterns(Map<Integer, Map<File, SessionPerCall>> valueBasedPatternFileMap){
        for(Integer newPatternID : valueBasedPatternFileMap.keySet()){
            Map<File, SessionPerCall> newPatternFileMap = valueBasedPatternFileMap.get(newPatternID);
            Map<File, SessionPerCall> oldPatternFileMap = basePatternMap.get(newPatternID);
            if(null == oldPatternFileMap){
                File patternFolder = new File(patternConfigLoader.getOutputLocation() + File.separator + newPatternID);
                patternFolder.mkdir();
                for(File newSessionFile : newPatternFileMap.keySet()){
                    newSessionFile.renameTo(new File(patternFolder + File.separator + newSessionFile.getName()));
                }
            }
            basePatternMap.put(newPatternID,newPatternFileMap);
        }
    }

}
