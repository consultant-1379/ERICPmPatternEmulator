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
 * The Class CellToPatternMetaDataWrapper.
 */
@Component
public class CellToPatternMetaDataWrapper implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The cell to pattern meta data list map. */
    private Map<String, Map<Integer, List<String>>> cellToPatternMetaDataListMap;

    /** The schema release version. */
    private String schemaReleaseVersion;

    /**
     * Gets the cell to pattern meta data list map.
     *
     * @return the cell to pattern meta data list map
     */
    public Map<String, Map<Integer, List<String>>> getCellToPatternMetaDataListMap(){
        return cellToPatternMetaDataListMap;
    }

    /**
     * Sets the cell to pattern meta data list map.
     *
     * @param cellToPatternMetaDataListMap
     *            the cell to pattern meta data list map
     */
    public void setCellToPatternMetaDataListMap(Map<String, Map<Integer, List<String>>> cellToPatternMetaDataListMap){
        this.cellToPatternMetaDataListMap = cellToPatternMetaDataListMap;
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

}
