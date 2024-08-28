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

package com.ericsson.patternextractor.controller;

import static com.ericsson.configmaster.constants.Constants.DEFAULT_OFFSET;
import static com.ericsson.configmaster.constants.Constants.DEFAULT_SECONDS;
import static com.ericsson.configmaster.constants.Constants.END_DATE;
import static com.ericsson.configmaster.constants.Constants.END_TIME;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.FDN;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.MINUTES;
import static com.ericsson.configmaster.constants.Constants.START_DATE;
import static com.ericsson.configmaster.constants.Constants.START_TIME;
import static com.ericsson.configmaster.constants.Constants.START_TIME_OFFSET;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.services.PatternConfigLoader;
import com.ericsson.patternextractor.services.EventIterator;
import com.ericsson.patternextractor.services.PatternIdentifierThread;
import com.ericsson.patternextractor.services.SessionIdentifierThread;
import com.ericsson.utilities.services.Utils;

/**
 * The Class PatternMasterController.
 */
@Service
public class PatternMasterController {

    /** The pattern config loader. */
    @Autowired
    private PatternConfigLoader patternConfigLoader;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /** The pattern extractor. */
    @Autowired
    private EventIterator eventIterator;

    /**
     * Inits the.
     *
     * @throws Exception
     *             the exception
     */
    @PostConstruct
    public void init() throws Exception{
        String[] inputFileNames = null;
        File inputLocation = new File(patternConfigLoader.getInputLocation());
        if(!inputLocation.exists()){
            Utils.logMessage(ERROR,"Input Location does not exist");
            throw new FileNotFoundException();
        } else{
            inputFileNames = inputLocation.list(Utils.getInputFileNameFilter());
            if(0 == inputFileNames.length){
                Utils.logMessage(ERROR,"Files does not exist at input location");
                throw new FileNotFoundException();
            }
        }
        Map<String, TreeSet<String>> fdnToFileNameListMap = extractFdnsAndMapFileNames(inputFileNames);

        // Calculate ropDuration and offset
        int ropDuration = 0;
        String offset = DEFAULT_OFFSET;
        List<String> fileDateTimeAttributeList = new ArrayList<>();
        fileDateTimeAttributeList.add(START_DATE);
        fileDateTimeAttributeList.add(START_TIME);
        fileDateTimeAttributeList.add(START_TIME_OFFSET);
        fileDateTimeAttributeList.add(END_DATE);
        fileDateTimeAttributeList.add(END_TIME);
        for(String fdn : fdnToFileNameListMap.keySet()){
            Map<String, String> fileAttributeValues = Utils.getAttributeFromFileName(patternConfigLoader.getInputLocation(),fdnToFileNameListMap.get(
                    fdn).first(),patternConfigLoader.getDatasource(),fileDateTimeAttributeList);
            if(null != fileAttributeValues.get(START_TIME_OFFSET) && !fileAttributeValues.get(START_TIME_OFFSET).isEmpty()){
                offset = fileAttributeValues.get(START_TIME_OFFSET);
            }
            Date startDate = Utils.getDateObj(fileAttributeValues.get(START_DATE),fileAttributeValues.get(START_TIME) + DEFAULT_SECONDS,offset);
            if(null == fileAttributeValues.get(END_DATE) || fileAttributeValues.get(END_DATE).isEmpty()){
                fileAttributeValues.put(END_DATE,fileAttributeValues.get(START_DATE));
            }
            Date endDate = Utils.getDateObj(fileAttributeValues.get(END_DATE),fileAttributeValues.get(END_TIME) + DEFAULT_SECONDS,offset);
            ropDuration = Utils.calcDateDiff(endDate,startDate,MINUTES);
            Utils.logMessage(INFO,"Rop duration - " + ropDuration + " minutes");
            Utils.logMessage(INFO,"Offset - " + offset);
            break;
        }
        distributeFdnToThreads(fdnToFileNameListMap,ropDuration,offset);
    }

    /**
     * Extract fdns and map file names.
     *
     * @param inputFileNames
     *            the input file names
     * @return the map
     * @throws IllegalArgumentException
     *             the illegal argument exception
     */
    private Map<String, TreeSet<String>> extractFdnsAndMapFileNames(String[] inputFileNames) throws IllegalArgumentException{
        Utils.logMessage(INFO,"Number of input files - " + inputFileNames.length);
        Map<String, TreeSet<String>> fdnToFileNameListMap = new HashMap<String, TreeSet<String>>();
        for(String fileName : inputFileNames){
            String fdn = Utils.getAttributeFromFileName("",fileName,patternConfigLoader.getDatasource(),FDN);
            TreeSet<String> fileNameList = fdnToFileNameListMap.get(fdn);
            if(null == fileNameList){
                Utils.logMessage(INFO,"FDN - " + fdn);
                fileNameList = new TreeSet<String>();
            }
            fileNameList.add(fileName);
            fdnToFileNameListMap.put(fdn,fileNameList);
        }
        Utils.logMessage(INFO,"Number of nodes - " + fdnToFileNameListMap.size());
        return fdnToFileNameListMap;
    }

    /**
     * Distribute fdn to threads.
     *
     * @param fdnToFileNameListMap
     *            the fdn to file name list map
     * @throws Exception
     *             the exception
     */
    private void distributeFdnToThreads(Map<String, TreeSet<String>> fdnToFileNameListMap, int ropDuration, String offset) throws Exception{
        try{
            // Start Pattern identifier thread
            eventIterator.setTerminationFlag(true);
            Thread patternIdentifierThread = new Thread(applicationContext.getBean(PatternIdentifierThread.class));
            patternIdentifierThread.start();
            Utils.logMessage(INFO,"Pattern identifier thread started");

            // Start file processor and session identifier thread
            int poolSize = patternConfigLoader.getNumberOFThreads();
            int fdnCount = fdnToFileNameListMap.size();
            if(fdnCount > 0){
                int factor = 1;
                int mod = 0;
                int startIndex = 0;
                int endIndex = 0;
                if(fdnCount < poolSize){
                    poolSize = fdnCount;
                    mod = -1;
                } else{
                    factor = fdnCount / poolSize;
                    mod = fdnCount % poolSize;
                }
                ThreadPoolExecutor threadPoolTaskExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
                List<String> fdnList = new ArrayList<String>(fdnToFileNameListMap.keySet());
                for(int threadindex = 0; threadindex < poolSize; threadindex++){
                    startIndex = endIndex;
                    if(threadindex == poolSize - 1){
                        endIndex = fdnCount;
                    } else{
                        endIndex = endIndex + factor + (mod > 0 ? 1 : 0);
                        mod--;
                    }
                    List<String> subfdnList = fdnList.subList(startIndex,endIndex);
                    SessionIdentifierThread sessionIdentifierThread = applicationContext.getBean(SessionIdentifierThread.class);
                    sessionIdentifierThread.setFdnList(subfdnList);
                    sessionIdentifierThread.setFdnToFileNameListMap(fdnToFileNameListMap);
                    sessionIdentifierThread.setRopDuration(ropDuration);
                    sessionIdentifierThread.setOffset(offset);
                    threadPoolTaskExecutor.execute(sessionIdentifierThread);
                }
                threadPoolTaskExecutor.shutdown();
                Utils.logMessage(INFO,"All session identifier threads started");
                while (!threadPoolTaskExecutor.isTerminated()){
                    Thread.sleep(5000);
                }
                eventIterator.setTerminationFlag(false);
                Utils.logMessage(INFO,"All session identifier threads terminated successfully.");
                Utils.logMessage(INFO,"Waiting for pattern identifier thread to terminate.");
                patternIdentifierThread.join();
            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occurred while distributing fdn to threads : " + e);
            throw e;
        }
    }

}
