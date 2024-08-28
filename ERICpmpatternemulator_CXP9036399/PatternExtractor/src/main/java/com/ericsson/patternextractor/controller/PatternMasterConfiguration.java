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

package com.ericsson.patternextractor.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import com.ericsson.patternextractor.entities.SessionEvent;
import com.ericsson.patternextractor.entities.SessionPerCall;
import com.ericsson.patternextractor.services.EventIterator;
import com.ericsson.patternextractor.services.PatternIdentifierThread;
import com.ericsson.patternextractor.services.PatternMetaDataWriter;
import com.ericsson.patternextractor.services.SessionIdentifierThread;

/**
 * The Class PatternMasterConfiguration.
 */
@Configuration
@Lazy
public class PatternMasterConfiguration {

    /**
     * Pattern master controller.
     *
     * @return the pattern master controller
     */
    @Bean
    public PatternMasterController patternMasterController(){
        return new PatternMasterController();
    }

    /**
     * Event iterator.
     *
     * @return the event iterator
     */
    @Bean
    public EventIterator eventIterator(){
        return new EventIterator();
    }

    /**
     * Session identifier thread.
     *
     * @return the session identifier thread
     */
    @Bean
    @Scope("prototype")
    public SessionIdentifierThread sessionIdentifierThread(){
        return new SessionIdentifierThread();
    }

    /**
     * Pattern identifier thread.
     *
     * @return the pattern identifier thread
     */
    @Bean
    public PatternIdentifierThread patternIdentifierThread(){
        return new PatternIdentifierThread();
    }

    /**
     * Pattern meta data writer.
     *
     * @return the pattern meta data writer
     */
    @Bean
    public PatternMetaDataWriter patternMetaDataWriter(){
        return new PatternMetaDataWriter();
    }

    /**
     * Session event.
     *
     * @return the session event
     */
    @Bean
    @Scope("prototype")
    public SessionEvent sessionEvent(){
        return new SessionEvent();
    }

    /**
     * Session per call.
     *
     * @return the session per call
     */
    @Bean
    @Scope("prototype")
    public SessionPerCall sessionPerCall(){
        return new SessionPerCall();
    }
}
