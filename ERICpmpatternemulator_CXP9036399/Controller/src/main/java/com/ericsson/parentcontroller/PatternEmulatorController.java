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

package com.ericsson.parentcontroller;

import static com.ericsson.configmaster.constants.Constants.ANALYZER;
import static com.ericsson.configmaster.constants.Constants.CTR;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.NETWORK_EVOLUTION;
import static com.ericsson.configmaster.constants.Constants.PATTERN_EXTRACTOR;
import static com.ericsson.configmaster.constants.Constants.STREAM_PROCESSOR;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.ericsson.eventenricher.controller.EventEnricherController;
import com.ericsson.patternextractor.controller.PatternMasterController;
import com.ericsson.patternreviser.controller.PatternReviserController;
import com.ericsson.schemamaster.iservices.SchemaParser;
import com.ericsson.schemamaster.services.CTRSchemaParser;
import com.ericsson.utilities.services.Utils;

/**
 * The Class ConfigMasterController.
 */

@SpringBootApplication
@ComponentScan(basePackages = { "com.ericsson" })
public class PatternEmulatorController implements CommandLineRunner {

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * The main method.
     *
     * @param args
     *            the arguments
     */
    public static void main(String[] args){
        // Set application path
        System.setProperty("APP_ROOT_PATH",args[0]);
        SpringApplication app = new SpringApplication(PatternEmulatorController.class);
        Map<String, Object> customPropertyMap = new HashMap<>(1);
        customPropertyMap.put("SERVER_PORT",args[1]);
        app.setDefaultProperties(customPropertyMap);
        app.run(args);
    }

    /**
     * Run.
     *
     * @param args
     *            the args
     * @throws Exception
     *             the exception
     */
    /*
     * (non-Javadoc)
     *
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(String... args) throws Exception{
        Utils.logMessage(INFO,"Application started",true);
        try{
            String mode = args[2];
            if(mode.equalsIgnoreCase(PATTERN_EXTRACTOR)){
                initiatePatternExtractorMode();
            } else if(mode.equalsIgnoreCase(NETWORK_EVOLUTION)){
                initiateNetworkEvolutionMode(args[3],args[4]);
            } else if(mode.equalsIgnoreCase(STREAM_PROCESSOR)){
                initiateStreamingMode();
            } else if(mode.equalsIgnoreCase(ANALYZER)){
                initiateAnalyzerMode();
            }
            Utils.logMessage(INFO,"Application stopped",true);
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error occurred. Please check logs for details",true);
            Utils.logMessage(ERROR,e.getMessage());
            Utils.logMessage(INFO,"Application stopped",true);
            System.exit(0);
        }
    }

    /**
     * Initiate network evolution mode.
     *
     * @param datasource
     *            the datasource
     * @param schemaFilePath
     *            the schema file path
     * @throws Exception
     *             the exception
     */
    private void initiateNetworkEvolutionMode(String datasource, String schemaFilePath) throws Exception{
        Utils.logMessage(INFO,"Network evolution started",true);
        switch(datasource){
        case CTR:
            SchemaParser schemaParser = applicationContext.getBean(CTRSchemaParser.class);
            schemaParser.parseSchema(schemaFilePath);
            break;
        }
    }

    /**
     * Initiate pattern extractor mode.
     *
     * @throws Exception
     *             the exception
     */
    private void initiatePatternExtractorMode() throws Exception{
        Utils.logMessage(INFO,"Pattern extraction started",true);
        applicationContext.getBean(PatternMasterController.class);
    }

    /**
     * Initiate streaming mode.
     *
     * @throws Exception
     *             the exception
     */
    private void initiateStreamingMode() throws Exception{
        Utils.logMessage(INFO,"Streaming data processing started",true);
        EventEnricherController eventEnricherController = applicationContext.getBean(EventEnricherController.class);
        eventEnricherController.processEventsAndStream();
    }

    /**
     * Initiate analyzer mode.
     *
     * @throws Exception
     *             the exception
     */
    private void initiateAnalyzerMode() throws Exception{
        Utils.logMessage(INFO,"Analyzer mode started",true);
        PatternReviserController patternReviserController = applicationContext.getBean(PatternReviserController.class);
        patternReviserController.start();
    }

}
