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

package com.ericsson.eventenricher.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ericsson.eventenricher.common.services.Distributor;
import com.ericsson.eventenricher.cps.services.CpsEventEnricherThread;
import com.ericsson.eventenricher.cps.services.CpsFeedController;
import com.ericsson.eventenricher.entities.CallPerSecondEventsWrapper;
import com.ericsson.eventenricher.eps.services.EpsEventEnricherThread;
import com.ericsson.eventenricher.eps.services.EpsFeedController;

/**
 * The Class EventEnricherConfiguration.
 */
@Configuration
public class EventEnricherConfiguration {

    /**
     * Event enricher controller.
     *
     * @return the event enricher controller
     */
    @Bean
    public EventEnricherController eventEnricherController(){
        return new EventEnricherController();
    }

    /**
     * Event processor thread.
     *
     * @return the event processor thread
     */
    @Bean
    @Scope("prototype")
    public EpsEventEnricherThread epsEventEnricherThread(){
        return new EpsEventEnricherThread();
    }

    /**
     * Feed controller.
     *
     * @return the feed controller
     */
    @Bean
    public EpsFeedController epsFeedController(){
        return new EpsFeedController();
    }

    /**
     * Cps feed controller.
     *
     * @return the cps feed controller
     */
    @Bean
    public CpsFeedController cpsFeedController(){
        return new CpsFeedController();
    }

    /**
     * Cps event enricher thread.
     *
     * @return the cps event enricher thread
     */
    @Bean
    @Scope("prototype")
    public CpsEventEnricherThread cpsEventEnricherThread(){
        return new CpsEventEnricherThread();
    }

    /**
     * Distributor.
     *
     * @return the distributor
     */
    @Bean
    public Distributor distributor(){
        return new Distributor();
    }

    /**
     * Call per second events wrapper.
     *
     * @return the call per second events wrapper
     */
    @Bean
    @Scope("prototype")
    public CallPerSecondEventsWrapper callPerSecondEventsWrapper(){
        return new CallPerSecondEventsWrapper();
    }

}
