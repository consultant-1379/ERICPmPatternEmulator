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

package com.ericsson.configmaster.services;

import static com.ericsson.configmaster.constants.Constants.COLON;
import static com.ericsson.configmaster.constants.Constants.COMMA;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_ENBS1APID;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_GLOBAL_CELL_ID;
import static com.ericsson.configmaster.constants.Constants.EVENT_PARAM_RAC_UE_REF;
import static com.ericsson.configmaster.constants.Constants.PATTERN_CONFIG_FILE_PATH;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * The Class PatternConfigLoader.
 */
@Configuration
@PropertySource("file:${APP_ROOT_PATH}" + PATTERN_CONFIG_FILE_PATH)
public class PatternConfigLoader {

    /** The datasource. */
    @Value("#{'${DATASOURCE}'.toUpperCase()}")
    private String datasource;

    /** The input location. */
    @Value("${INPUT_LOCATION}")
    private String inputLocation;

    /** The output location. */
    @Value("${OUTPUT_LOCATION}")
    private String outputLocation;

    /** The session attributes list. */
    @Value("#{'${SESSION_ATTRIBUTES}'.split('" + COMMA + "')}")
    private Set<String> sessionAttributesSet;

    /** The pattern selection attributes list. */
    @Value("#{'${PATTERN_SELECTION_ATTRIBUTES}'.split('" + COMMA + "')}")
    private Set<String> patternSelectionAttributesSet;

    /** The ignore events set. */
    @Value("#{'${EVENTS_TO_IGNORE}'.split('" + COMMA + "')}")
    private Set<String> ignoreEventsSet;

    /** The complex and array attributes map. */
    private Map<String, Set<Integer>> complexAndArrayAttributesMap;

    /** The session Event list is to maintain event para. */
    private Set<String> sessionParameterToIgnoreCheckSet;

    /** The number OF threads. */
    @Value("${NUMBER_OF_THREADS}")
    private int numberOFThreads;

    /** The same session gap interval. */
    @Value("${SAME_SESSIONID_MINUTES_INTERVAL}")
    private int sameSessionGapInterval;

    /** The app config loader. */
    @Autowired
    private AppConfigLoader appConfigLoader;

    /** The rac ue ref. */
    private int RAC_UE_REF;

    /** The ENB s1 apid. */
    private int ENBS1APID;

    /** The event param cell id. */
    private int EVENT_PARAM_CELL_ID;

    /**
     * Inits the.
     */
    @PostConstruct
    public void init(){
        if(!validateProperties()){
            System.exit(0);
        }
        complexAndArrayAttributesMap = new HashMap<String, Set<Integer>>();
        sessionParameterToIgnoreCheckSet = new HashSet<String>(sessionAttributesSet.size());
        sessionAttributesSet.add(EVENT_PARAM_GLOBAL_CELL_ID);

        Set<String> complexParamNames = appConfigLoader.getAppConfigDatasourceWrappereMap().get(datasource).getComplexParameterInfoMap().keySet();

        int index=0;
        // Set attributes from session parameters
        for(String sessionAttribute : sessionAttributesSet){
            if(sessionAttribute.contains(EVENT_PARAM_RAC_UE_REF)){
                setRAC_UE_REF(index);
            }
            if(sessionAttribute.contains(EVENT_PARAM_ENBS1APID)){
                setENBS1APID(index);
            }
            if(sessionAttribute.contains(EVENT_PARAM_GLOBAL_CELL_ID)){
                setEVENT_PARAM_CELL_ID(index);
            }
            setComplexAndArrayAttributesMap(sessionAttribute,complexParamNames,true);
            index++;
        }

        // Set attributes from pattern selection parameters
        if(null != patternSelectionAttributesSet){
            for(String patternSelectionAttribute : patternSelectionAttributesSet){
                setComplexAndArrayAttributesMap(patternSelectionAttribute,complexParamNames,false);
            }
        }
    }

    /**
     * Validate properties.
     *
     * @return true, if successful
     */
    private boolean validateProperties(){
        if(1 == sessionAttributesSet.size() && sessionAttributesSet.contains("")){
            System.out.println("Session attributes missing in the properties file");
            return false;
        }
        if(1 == patternSelectionAttributesSet.size() && patternSelectionAttributesSet.contains("")){
            patternSelectionAttributesSet.clear();
        }
        if(1 == ignoreEventsSet.size() && ignoreEventsSet.contains("")){
            ignoreEventsSet.clear();
        }

        return true;
    }

    /**
     * Sets the complex and array attributes map.
     *
     * @param sessionAttribute
     *            the session attribute
     * @param complexParamNames
     *            the complex param names
     * @param isSessionEventSet
     *            the is session event set
     */
    private void setComplexAndArrayAttributesMap(String sessionAttribute, Set<String> complexParamNames, boolean isSessionEventSet){
        Set<Integer> componentIDSet;

        if(sessionAttribute.contains(COLON)){
            String[] sessionAttributeSplittedArray = sessionAttribute.split(COLON);
            componentIDSet = complexAndArrayAttributesMap.get(sessionAttributeSplittedArray[0]);
            if(null == componentIDSet){
                componentIDSet = new TreeSet<Integer>();
            }
            componentIDSet.add(Integer.parseInt(sessionAttributeSplittedArray[1]));
            complexAndArrayAttributesMap.put(sessionAttributeSplittedArray[0],componentIDSet);
            if(isSessionEventSet){
                sessionParameterToIgnoreCheckSet.add(sessionAttributeSplittedArray[0]);
            }

        } else{
            if(complexParamNames.contains(sessionAttribute)){
                componentIDSet = complexAndArrayAttributesMap.get(sessionAttribute);
                if(null == componentIDSet){
                    componentIDSet = new TreeSet<Integer>();
                }
                componentIDSet.add(-1);
                complexAndArrayAttributesMap.put(sessionAttribute,componentIDSet);
            }
            if(isSessionEventSet){
                sessionParameterToIgnoreCheckSet.add(sessionAttribute);
            }
        }
    }

    /**
     * Gets the datasource.
     *
     * @return the datasource
     */
    public String getDatasource(){
        return datasource;
    }

    /**
     * Sets the datasource.
     *
     * @param datasource
     *            the new datasource
     */
    public void setDatasource(String datasource){
        this.datasource = datasource;
    }

    /**
     * Gets the input location.
     *
     * @return the input location
     */
    public String getInputLocation(){
        return inputLocation;
    }

    /**
     * Sets the input location.
     *
     * @param inputLocation
     *            the new input location
     */
    public void setInputLocation(String inputLocation){
        this.inputLocation = inputLocation;
    }

    /**
     * Gets the output location.
     *
     * @return the output location
     */
    public String getOutputLocation(){
        return outputLocation;
    }

    /**
     * Sets the output location.
     *
     * @param outputLocation
     *            the new output location
     */
    public void setOutputLocation(String outputLocation){
        this.outputLocation = outputLocation;
    }

    /**
     * Gets the session attributes list.
     *
     * @return the session attributes list
     */
    public Set<String> getSessionAttributesSet(){
        return sessionAttributesSet;
    }

    /**
     * Sets the session attributes list.
     *
     * @param sessionAttributesSet
     *            the new session attributes set
     */
    public void setSessionAttributesSet(Set<String> sessionAttributesSet){
        this.sessionAttributesSet = sessionAttributesSet;
    }

    /**
     * Gets the complex and array attributes map.
     *
     * @return the complex and array attributes map
     */
    public Map<String, Set<Integer>> getComplexAndArrayAttributesMap(){
        return complexAndArrayAttributesMap;
    }

    /**
     * Sets the complex and array attributes map.
     *
     * @param complexAndArrayAttributesMap
     *            the complex and array attributes map
     */
    public void setComplexAndArrayAttributesMap(Map<String, Set<Integer>> complexAndArrayAttributesMap){
        this.complexAndArrayAttributesMap = complexAndArrayAttributesMap;
    }

    /**
     * Gets the pattern selection attributes set.
     *
     * @return the pattern selection attributes set
     */
    public Set<String> getPatternSelectionAttributesSet(){
        return patternSelectionAttributesSet;
    }

    /**
     * Sets the pattern selection attributes set.
     *
     * @param patternSelectionAttributesSet
     *            the new pattern selection attributes set
     */
    public void setPatternSelectionAttributesSet(Set<String> patternSelectionAttributesSet){
        this.patternSelectionAttributesSet = patternSelectionAttributesSet;
    }

    /**
     * Gets the session parameter to ignore check set.
     *
     * @return the session parameter to ignore check set
     */
    public Set<String> getSessionParameterToIgnoreCheckSet(){
        return sessionParameterToIgnoreCheckSet;
    }

    /**
     * Sets the session parameter to ignore check set.
     *
     * @param sessionParameterToIgnoreCheckSet
     *            the new session parameter to ignore check set
     */
    public void setSessionParameterToIgnoreCheckSet(Set<String> sessionParameterToIgnoreCheckSet){
        this.sessionParameterToIgnoreCheckSet = sessionParameterToIgnoreCheckSet;
    }

    /**
     * Gets the ignore events set.
     *
     * @return the ignore events set
     */
    public Set<String> getIgnoreEventsSet(){
        return ignoreEventsSet;
    }

    /**
     * Sets the ignore events set.
     *
     * @param ignoreEventsSet
     *            the new ignore events set
     */
    public void setIgnoreEventsSet(Set<String> ignoreEventsSet){
        this.ignoreEventsSet = ignoreEventsSet;
    }

    /**
     * Gets the number OF threads.
     *
     * @return the number OF threads
     */
    public int getNumberOFThreads(){
        return numberOFThreads;
    }

    /**
     * Sets the number OF threads.
     *
     * @param numberOFThreads
     *            the new number OF threads
     */
    public void setNumberOFThreads(int numberOFThreads){
        this.numberOFThreads = numberOFThreads;
    }

    /**
     * Gets the same session gap interval.
     *
     * @return the same session gap interval
     */
    public int getSameSessionGapInterval(){
        return sameSessionGapInterval;
    }

    /**
     * Sets the same session gap interval.
     *
     * @param sameSessionGapInterval
     *            the new same session gap interval
     */
    public void setSameSessionGapInterval(int sameSessionGapInterval){
        this.sameSessionGapInterval = sameSessionGapInterval;
    }

    /**
     * @return the rAC_UE_REF
     */
    public int getRAC_UE_REF() {
        return RAC_UE_REF;
    }

    /**
     * @param rAC_UE_REF the rAC_UE_REF to set
     */
    public void setRAC_UE_REF(int rAC_UE_REF) {
        RAC_UE_REF = rAC_UE_REF;
    }

    /**
     * @return the eNBS1APID
     */
    public int getENBS1APID() {
        return ENBS1APID;
    }

    /**
     * @param eNBS1APID the eNBS1APID to set
     */
    public void setENBS1APID(int eNBS1APID) {
        ENBS1APID = eNBS1APID;
    }

    /**
     * @return the eVENT_PARAM_CELL_ID
     */
    public int getEVENT_PARAM_CELL_ID() {
        return EVENT_PARAM_CELL_ID;
    }

    /**
     * @param eVENT_PARAM_CELL_ID the eVENT_PARAM_CELL_ID to set
     */
    public void setEVENT_PARAM_CELL_ID(int eVENT_PARAM_CELL_ID) {
        EVENT_PARAM_CELL_ID = eVENT_PARAM_CELL_ID;
    }
}
