package com.ericsson.patternreviser.analyzer.services;

import static com.ericsson.configmaster.constants.Constants.ANALYSIS_OUTPUT_FILE_NAME;
import static com.ericsson.configmaster.constants.Constants.COMMA;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.NEWLINE;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ericsson.utilities.services.Utils;

@Service
public class AnalyzerHandler {

    /**
     * Write analysis results.
     *
     * @param outputLocation
     * @param analysisAllThreadsResults
     */
    public void writeAnalysisResults(String outputLocation, Map<String, Map<String, Map<String, Integer>>> analysisAllThreadsResults){
        try{
            Utils.logMessage(INFO,"Writing analysis results to output location");
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputLocation + File.separator + ANALYSIS_OUTPUT_FILE_NAME)));
            bw.write("Pattern_ID,Event.Parameter,ParameterValue,Number_Of_Sessions" + NEWLINE);
            for(String patternID : analysisAllThreadsResults.keySet()){
                Map<String, Map<String, Integer>> eventParamValueMap = analysisAllThreadsResults.get(patternID);
                for(String eventParamName : eventParamValueMap.keySet()){
                    Map<String, Integer> valueSessionCountMap = eventParamValueMap.get(eventParamName);
                    for(String value : valueSessionCountMap.keySet()){
                        bw.write(patternID + COMMA + eventParamName + COMMA + value + COMMA + valueSessionCountMap.get(value) + NEWLINE);
                    }
                }
            }
            bw.close();
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error while writing analysis output " + e);
        }
    }
}
