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

package com.ericsson.configmaster.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import com.ericsson.configmaster.entities.AppConfigDatasourceWrapper;
import com.ericsson.configmaster.entities.EventToAddWrapper;
import com.ericsson.configmaster.services.AppConfigLoader;
import com.ericsson.configmaster.services.AppLogger;
import com.ericsson.configmaster.services.ParamConfigLoader;
import com.ericsson.configmaster.services.PatternConfigLoader;

/**
 * The Class ConfigMasterConfiguration.
 */
@Configuration
public class ConfigMasterConfiguration {

    /**
     * App logger.
     *
     * @return the app logger
     */
    @Bean
    public AppLogger appLogger(){
        return new AppLogger();
    }

    /**
     * App config loader.
     *
     * @return the app config loader
     */
    @Bean
    public AppConfigLoader appConfigLoader(){
        return new AppConfigLoader();
    }

    /**
     * App config datasource wrapper.
     *
     * @return the app config datasource wrapper
     */
    @Bean
    @Scope("prototype")
    public AppConfigDatasourceWrapper appConfigDatasourceWrapper(){
        return new AppConfigDatasourceWrapper();
    }

    /**
     * Pattern config loader.
     *
     * @return the pattern config loader
     */
    @Bean
    @Lazy
    public PatternConfigLoader patternConfigLoader(){
        return new PatternConfigLoader();
    }

    /**
     * Param config loader.
     *
     * @return the param config loader
     */
    @Bean
    public ParamConfigLoader paramConfigLoader(){
        return new ParamConfigLoader();
    }

    @Bean
    @Scope("prototype")
    public EventToAddWrapper eventToAddWrapper(){
        return new EventToAddWrapper();
    }

}
