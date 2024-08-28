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

package com.ericsson.configmaster.entities;

import java.util.Map;

import com.ericsson.configmaster.appconfig.autogen.AppConfiguration.DataSource.ComplexParameters.Parameter.Component;

/**
 * The Class AppConfigDatasourceWrapper.
 */
@org.springframework.stereotype.Component
public class AppConfigDatasourceWrapper {

    /** The datasource name. */
    private String datasourceName;

    /** The complex parameter info map. */
    private Map<String, Map<Integer, Component>> complexParameterInfoMap;

    /**
     * Gets the datasource name.
     *
     * @return the datasource name
     */
    public String getDatasourceName(){
        return datasourceName;
    }

    /**
     * Sets the datasource name.
     *
     * @param datasourceName
     *            the new datasource name
     */
    public void setDatasourceName(String datasourceName){
        this.datasourceName = datasourceName;
    }

    /**
     * Gets the complex parameter info map.
     *
     * @return the complex parameter info map
     */
    public Map<String, Map<Integer, Component>> getComplexParameterInfoMap(){
        return complexParameterInfoMap;
    }

    /**
     * Sets the complex parameter info map.
     *
     * @param complexParameterInfoMap
     *            the complex parameter info map
     */
    public void setComplexParameterInfoMap(Map<String, Map<Integer, Component>> complexParameterInfoMap){
        this.complexParameterInfoMap = complexParameterInfoMap;
    }

}
