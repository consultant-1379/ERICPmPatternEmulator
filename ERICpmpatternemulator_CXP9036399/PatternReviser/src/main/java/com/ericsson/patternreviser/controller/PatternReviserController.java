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
package com.ericsson.patternreviser.controller;

import static com.ericsson.configmaster.constants.Constants.COLON;
import static com.ericsson.configmaster.constants.Constants.COMMA;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.NEWLINE;
import static com.ericsson.configmaster.constants.Constants.PATTERN_INFO_CSV_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.PATTERN_META_DATA_FILE_NAME;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.constants.Constants;
import com.ericsson.configmaster.services.ParamConfigLoader;
import com.ericsson.patternreviser.analyzer.services.AnalyzerHandler;
import com.ericsson.patternreviser.analyzer.services.AnalyzerThread;
import com.ericsson.patternreviser.eventaddition.services.EventAdditionHandler;
import com.ericsson.patternreviser.eventaddition.services.EventAdditionThread;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.services.SchemaLoader;
import com.ericsson.utilities.entities.PatternToCellMetaDataWrapper;
import com.ericsson.utilities.services.Utils;

/**
 * The Class PatternReviserController.
 */
@Service
public class PatternReviserController {

    /** The param config loader. */
    @Autowired
    private ParamConfigLoader paramConfigLoader;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The schema loader. */
    @Autowired
    private SchemaLoader schemaLoader;

    /** The event addition handler. */
    @Autowired
    private EventAdditionHandler eventAdditionHandler;

    /** The analyzer handler. */
    @Autowired
    private AnalyzerHandler analyzerHandler;

    /** The analysis all threads results. */
    public static Map<String, Map<String, Map<String, Integer>>> analysisAllThreadsResults;

    /** The pattern info. */
    private Map<Integer, List<Integer>> patternInfo;

    /**
     * Start.
     */
    public void start(){
        try{
            analysisAllThreadsResults = new ConcurrentHashMap<>();
            PatternToCellMetaDataWrapper patternToCellMetaDataWrapper = Utils.deserializePatternMetaDataFile(paramConfigLoader.getPatternLocation());
            SchemaRelease schemaRelease = schemaLoader.loadSchema(patternToCellMetaDataWrapper.getSchemaReleaseVersion());
            List<String> patternIDFilteredList = filterPatternsBasedOnReferenceEvents(schemaRelease,patternToCellMetaDataWrapper
                    .getPatternToEventListMap());

            List<File> patternFolderList = null;
            if(!patternIDFilteredList.isEmpty()){
                File patternLocationFile = new File(paramConfigLoader.getPatternLocation());
                File[] patternFolders = patternLocationFile.listFiles(Utils.getPatternLocationFileNameFilter(patternIDFilteredList,paramConfigLoader
                        .getOutputLocation()));
                patternFolderList = Arrays.asList(patternFolders);
            } else{
                Utils.logMessage(INFO,"Input events are not present in patterns");
                System.exit(0);
            }
            distributeFilteredPatterns(patternFolderList,schemaRelease,patternToCellMetaDataWrapper.getPatternToEventListMap());
            if(null != paramConfigLoader.getAnalysisEventList()){
                analyzerHandler.writeAnalysisResults(paramConfigLoader.getOutputLocation(),analysisAllThreadsResults);
            } else{
                updateMetaInfo();
                updateSerFile();
            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occurred in start method of PatternReviserController : " + e);
        }
    }

    /**
     * Filter patterns based on reference events.
     *
     * @param schemaRelease
     *            the schema release
     * @param patternToEventListMap
     *            the pattern to event list map
     * @return the list
     */
    private List<String> filterPatternsBasedOnReferenceEvents(SchemaRelease schemaRelease, Map<Integer, List<Integer>> patternToEventListMap){
        List<String> eventNameList = null;
        List<Integer> eventIDList = new ArrayList<>();
        List<String> patternIDFilteredList = new ArrayList<>();
        if(null != paramConfigLoader.getAnalysisEventList()){
            eventNameList = paramConfigLoader.getAnalysisEventList();
        } else if(null != paramConfigLoader.getEventToAddDetails()){
            eventNameList = new ArrayList<>();
            eventAdditionHandler.makeEventSkeleton(eventNameList,schemaRelease);
        }

        if(null != eventNameList){
            for(String eventName : eventNameList){
                eventIDList.add(schemaRelease.getEventNameMap().get(eventName).getEventID());
            }
            for(int patternID : patternToEventListMap.keySet()){
                List<Integer> patternEventIDList = patternToEventListMap.get(patternID);
                if(patternEventIDList.containsAll(eventIDList)){
                    patternIDFilteredList.add(String.valueOf(patternID));
                }
            }
        }
        return patternIDFilteredList;
    }

    /**
     * Distribute filtered patterns.
     *
     * @param patternFolderList
     *            the pattern folder list
     * @param schemaRelease
     *            the schema release
     * @param patternToEventIDListMap
     *            the pattern to event ID list map
     */
    private void distributeFilteredPatterns(List<File> patternFolderList, SchemaRelease schemaRelease,
            Map<Integer, List<Integer>> patternToEventIDListMap){
        try{
            patternInfo = new LinkedHashMap<>();
            int totalPatterns = patternFolderList.size();
            int poolSize = 32;
            if(totalPatterns > 0){
                int factor = 1;
                int mod = 0;
                int startIndex = 0;
                int endIndex = 0;
                if(totalPatterns < poolSize){
                    poolSize = totalPatterns;
                    mod = -1;
                } else{
                    factor = totalPatterns / poolSize;
                    mod = totalPatterns % poolSize;
                }
                ExecutorService executorService = Executors.newFixedThreadPool(poolSize);
                for(int threadindex = 0; threadindex < poolSize; threadindex++){
                    startIndex = endIndex;
                    if(threadindex == poolSize - 1){
                        endIndex = totalPatterns;
                    } else{
                        endIndex = endIndex + factor + (mod > 0 ? 1 : 0);
                        mod--;
                    }
                    List<File> patternFileSubList = patternFolderList.subList(startIndex,endIndex);
                    if(null != paramConfigLoader.getAnalysisEventList()){
                        AnalyzerThread analyzerThread = applicationContext.getBean(AnalyzerThread.class);
                        analyzerThread.setPatternFileList(patternFileSubList);
                        analyzerThread.setSchemaRelease(schemaRelease);
                        executorService.execute(analyzerThread);

                    } else if(null != paramConfigLoader.getEventToAddDetails()){
                        EventAdditionThread eventAdditionThread = applicationContext.getBean(EventAdditionThread.class);
                        eventAdditionThread.setPatternFileList(patternFileSubList);
                        eventAdditionThread.setSchemaRelease(schemaRelease);
                        eventAdditionThread.setPatternInfo(patternInfo);
                        executorService.execute(eventAdditionThread);
                    }
                }
                executorService.shutdown();
                while (!executorService.isTerminated()){
                    Thread.sleep(5000);
                }

            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occurred while distributing pattern folders to threads : " + e);
        }
    }

    /**
     * Update meta info.
     */
    private void updateMetaInfo(){
        try{
            BufferedReader file = new BufferedReader(new FileReader(paramConfigLoader.getPatternLocation() + File.separator
                    + PATTERN_INFO_CSV_FILE_NAME));
            String line;
            StringBuffer inputBuffer = new StringBuffer();
            boolean flag = true;
            while ((line = file.readLine()) != null){
                if(flag){
                    flag = false;
                    inputBuffer.append(line);
                    inputBuffer.append(NEWLINE);
                    continue;
                }
                String[] split = line.split(COMMA);
                if(patternInfo.containsKey(Integer.parseInt(split[0].trim()))){
                    String listOfEvent = StringUtils.join(patternInfo.get(Integer.parseInt(split[0].trim())),COLON);
                    split[2] = String.valueOf(patternInfo.get(Integer.parseInt(split[0].trim())).size());
                    split[3] = listOfEvent;
                    inputBuffer.append(StringUtils.join(split,COMMA));
                } else{
                    inputBuffer.append(line);
                }
                inputBuffer.append(NEWLINE);
            }
            FileOutputStream fileOut = new FileOutputStream(paramConfigLoader.getOutputLocation() + File.separator + PATTERN_INFO_CSV_FILE_NAME);
            fileOut.write(inputBuffer.toString().getBytes());
            fileOut.close();
            file.close();
        } catch(Exception ex){
            Utils.logMessage(Constants.ERROR,"Error while updating CSV " + ex);
        }
    }

    /**
     * Update ser file.
     */
    private void updateSerFile(){
        try{
            PatternToCellMetaDataWrapper patternMetaData = Utils.deserializePatternMetaDataFile(paramConfigLoader.getOutputLocation());
            for(Integer pattern : patternInfo.keySet()){
                patternMetaData.getPatternToEventListMap().put(pattern,patternInfo.get(pattern));
            }
            File patternMetaDataFile = new File(paramConfigLoader.getOutputLocation() + File.separator + PATTERN_META_DATA_FILE_NAME);
            if(patternMetaDataFile.exists()){
                patternMetaDataFile.delete();
            }
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(patternMetaDataFile));
            oos.writeObject(patternMetaData);
            oos.close();
        } catch(Exception ex){
            Utils.logMessage(Constants.ERROR,"Error while updating Ser file " + ex);
        }
    }

}
