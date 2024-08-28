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

import java.io.File;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The Class Instrumentation.
 */
@Component
public class Instrumentation {

    /** The Constant INST_LOGGER. */
    private static final Logger INST_LOGGER = Logger.getLogger("INSTRUMENTATION");

    /** The process ID. */
    private String processID;

    /** The event cnt. */
    private long eventCnt = 0L; // number of events sent

    /** The old event cnt. */
    private long oldEventCnt = 0L;

    /** The failed cnt. */
    private long failedCnt = 0L; // number of events failed

    /** The last called. */
    // private long lastCalled = 0L;

    @Scheduled(fixedRate = 60000, initialDelay = 1000)
    public void run(){
        // long timeNow = System.currentTimeMillis();
        long totalEventsStreamed = eventCnt - oldEventCnt;
        int eps = Math.round(totalEventsStreamed * 1000 / 60000);// (timeNow - lastCalled)); // events sent per second
        INST_LOGGER.info("EPS sent: " + eps + " Events Streamed: " + totalEventsStreamed + " Events failed: " + failedCnt);
        oldEventCnt = eventCnt;
        // lastCalled = timeNow;
    }

    /**
     * Update instrumentation.
     *
     * @param eventCount
     *            the event count
     */
    public void updateInstrumentation(int eventCount){
        eventCnt += eventCount;
    }

    /**
     * Update instrumentation.
     */
    public void updateInstrumentation(){
        eventCnt++;
    }

    /**
     * Update failed instrumentation.
     */
    public void updateFailedInstrumentation(){
        failedCnt++;
    }

    /**
     * Start.
     *
     * @param processID
     *            the process ID
     * @param appPath
     *            the app path
     */
    public void init(String processID, String appPath){
        this.processID = processID;
        initLogger(appPath);
        INST_LOGGER.info(processID);
        StreamerAppLogger.getLOGGER().info(processID + " : Started instrumentation timer");
        // lastCalled = System.currentTimeMillis();
    }

    /**
     * Inits the logger.
     *
     * @param appPath
     *            the app path
     */
    public void initLogger(String appPath){
        try{
            INST_LOGGER.setUseParentHandlers(false);
            INST_LOGGER.setLevel(Level.FINE);

            // Creating fileHandler
            final FileHandler fileHandler = new FileHandler(appPath + File.separator + "logs/streamingLogs/inst/inst_" + processID + ".log");
            fileHandler.setLevel(Level.FINE);

            // Creating and assigning SimpleFormatter
            Formatter simpleFormatter = new SimpleFormatter() {
                private static final String format = "[%1$tb %1$td,%1$tY %1$tT] %2$-7s: %3$s %n";

                @Override
                public synchronized String format(LogRecord lr){
                    return String.format(format,new Date(lr.getMillis()),lr.getLevel().getLocalizedName(),lr.getMessage());
                }
            };
            fileHandler.setFormatter(simpleFormatter);
            INST_LOGGER.addHandler(fileHandler);
        } catch(Exception e){
            System.out.println("Exception occurred while initializing logger. " + e.getMessage());
        }
    }

}
