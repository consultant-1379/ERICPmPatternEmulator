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

import static com.ericsson.configmaster.constants.Constants.APP_CONFIG_FILE_PATH;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.ericsson.configmaster.appconfig.autogen.AppConfiguration;
import com.ericsson.configmaster.appconfig.autogen.AppConfiguration.DataSource;
import com.ericsson.configmaster.appconfig.autogen.AppConfiguration.DataSource.ComplexParameters.Parameter;
import com.ericsson.configmaster.appconfig.autogen.AppConfiguration.DataSource.ComplexParameters.Parameter.Component;
import com.ericsson.configmaster.entities.AppConfigDatasourceWrapper;

/**
 * The Class AppConfigLoader.
 */
@Service
public class AppConfigLoader {

    /** The app config datasource map. */
    private Map<String, AppConfigDatasourceWrapper> appConfigDatasourceWrappereMap;

    /** The app config datasource map. */
    private Map<String, DataSource> appConfigDatasourceAutogenMap;

    /** The application context. */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Load app config file.
     *
     * @throws Exception
     *             the exception
     */
    @PostConstruct
    public void loadAppConfigFile() throws Exception{
        JAXBContext jaxbContext = null;
        File file = new File(APP_CONFIG_FILE_PATH);

        jaxbContext = JAXBContext.newInstance(AppConfiguration.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        AppConfiguration appConfiguration = (AppConfiguration) jaxbUnmarshaller.unmarshal(file);

        updateAppConfigRelatedMaps(appConfiguration);

    }

    /**
     * Update app config related maps.
     *
     * @param appConfiguration
     *            the app configuration
     */
    private void updateAppConfigRelatedMaps(AppConfiguration appConfiguration){
        appConfigDatasourceWrappereMap = new HashMap<String, AppConfigDatasourceWrapper>(appConfiguration.getDataSource().size());
        appConfigDatasourceAutogenMap = new HashMap<String, DataSource>(appConfiguration.getDataSource().size());

        for(DataSource datasourceObj : appConfiguration.getDataSource()){
            AppConfigDatasourceWrapper appConfigDatasourceWrapperObj = applicationContext.getBean(AppConfigDatasourceWrapper.class);
            appConfigDatasourceWrapperObj.setDatasourceName(datasourceObj.getValue());

            Map<String, Map<Integer, Component>> complexParameterInfoMap = new HashMap<String, Map<Integer, Component>>(datasourceObj
                    .getComplexParameters().getParameter().size());
            for(Parameter complexParameter : datasourceObj.getComplexParameters().getParameter()){
                List<Component> componentList = complexParameter.getComponent();
                Map<Integer, Component> componentInfoMap = new HashMap<Integer, Component>(componentList.size());
                for(Component component : componentList){
                    componentInfoMap.put(component.getId(),component);
                }
                complexParameterInfoMap.put(complexParameter.getName(),componentInfoMap);
            }

            appConfigDatasourceWrapperObj.setComplexParameterInfoMap(complexParameterInfoMap);
            appConfigDatasourceWrappereMap.put(datasourceObj.getValue(),appConfigDatasourceWrapperObj);
            appConfigDatasourceAutogenMap.put(datasourceObj.getValue(),datasourceObj);
        }
    }

    /**
     * Gets the app config datasource wrappere map.
     *
     * @return the app config datasource wrappere map
     */
    public Map<String, AppConfigDatasourceWrapper> getAppConfigDatasourceWrappereMap(){
        return appConfigDatasourceWrappereMap;
    }

    /**
     * Sets the app config datasourc wrappere map.
     *
     * @param appConfigDatasourceWrappereMap
     *            the app config datasource wrappere map
     */
    public void setAppConfigDatasourcWrappereMap(Map<String, AppConfigDatasourceWrapper> appConfigDatasourceWrappereMap){
        this.appConfigDatasourceWrappereMap = appConfigDatasourceWrappereMap;
    }

    /**
     * Gets the app config datasource autogen map.
     *
     * @return the app config datasource autogen map
     */
    public Map<String, DataSource> getAppConfigDatasourceAutogenMap(){
        return appConfigDatasourceAutogenMap;
    }

    /**
     * Sets the app config datasource autogen map.
     *
     * @param appConfigDatasourceAutogenMap
     *            the app config datasource autogen map
     */
    public void setAppConfigDatasourceAutogenMap(Map<String, DataSource> appConfigDatasourceAutogenMap){
        this.appConfigDatasourceAutogenMap = appConfigDatasourceAutogenMap;
    }

}
