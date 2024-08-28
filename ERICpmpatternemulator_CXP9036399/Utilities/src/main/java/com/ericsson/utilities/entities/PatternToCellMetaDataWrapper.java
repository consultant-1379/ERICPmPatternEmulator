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
package com.ericsson.utilities.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * The Class PatternToCellMetaDataWrapper.
 */
@Component
public class PatternToCellMetaDataWrapper implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The pattern to cell meta data list map. */
    private Map<Integer, Map<String, List<String>>> patternToCellMetaDataListMap;

    /** The pattern to event list map. */
    private Map<Integer, List<Integer>> patternToEventListMap;

    /** The schema release version. */
    private String schemaReleaseVersion;

    /**
     * Gets the pattern to cell meta data list map.
     *
     * @return the pattern to cell meta data list map
     */
    public Map<Integer, Map<String, List<String>>> getPatternToCellMetaDataListMap(){
        return patternToCellMetaDataListMap;
    }

    /**
     * Sets the pattern to cell meta data list map.
     *
     * @param patternToCellMetaDataListMap
     *            the pattern to cell meta data list map
     */
    public void setPatternToCellMetaDataListMap(Map<Integer, Map<String, List<String>>> patternToCellMetaDataListMap){
        this.patternToCellMetaDataListMap = patternToCellMetaDataListMap;
    }

    /**
     * Gets the schema release version.
     *
     * @return the schema release version
     */
    public String getSchemaReleaseVersion(){
        return schemaReleaseVersion;
    }

    /**
     * Sets the schema release version.
     *
     * @param schemaReleaseVersion
     *            the new schema release version
     */
    public void setSchemaReleaseVersion(String schemaReleaseVersion){
        this.schemaReleaseVersion = schemaReleaseVersion;
    }

    /**
     * Gets the pattern to event list map.
     *
     * @return the pattern to event list map
     */
    public Map<Integer, List<Integer>> getPatternToEventListMap(){
        return patternToEventListMap;
    }

    /**
     * Sets the pattern to event list map.
     *
     * @param patternToEventListMap
     *            the pattern to event list map
     */
    public void setPatternToEventListMap(Map<Integer, List<Integer>> patternToEventListMap){
        this.patternToEventListMap = patternToEventListMap;
    }

}
