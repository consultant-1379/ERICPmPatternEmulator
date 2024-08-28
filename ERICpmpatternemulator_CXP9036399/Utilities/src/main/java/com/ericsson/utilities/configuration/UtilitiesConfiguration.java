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

package com.ericsson.utilities.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ericsson.utilities.entities.CellToPatternMetaDataWrapper;
import com.ericsson.utilities.entities.EventStreamerWrapper;
import com.ericsson.utilities.entities.HeaderStreamerWrapper;
import com.ericsson.utilities.entities.PatternToCellMetaDataWrapper;
import com.ericsson.utilities.entities.SessionPerCallWrapper;
import com.ericsson.utilities.services.HttpRequestHandler;
import com.ericsson.utilities.services.Utils;

/**
 * The Class UtilitiesConfiguration.
 */
@Configuration
public class UtilitiesConfiguration {

    /**
     * Utils.
     *
     * @return the utils
     */
    @Bean
    public Utils utils(){
        return new Utils();
    }

    /**
     * Event streamer wrapper.
     *
     * @return the event streamer wrapper
     */
    @Bean
    @Scope("prototype")
    public EventStreamerWrapper eventStreamerWrapper(){
        return new EventStreamerWrapper();
    }

    /**
     * Event streamer wrapper.
     *
     * @return the event streamer wrapper
     */
    @Bean
    public HeaderStreamerWrapper headerStreamerWrapper(){
        return new HeaderStreamerWrapper();
    }

    /**
     * Pattern to cell meta data wrapper.
     *
     * @return the pattern to cell meta data wrapper
     */
    @Bean
    public PatternToCellMetaDataWrapper patternToCellMetaDataWrapper(){
        return new PatternToCellMetaDataWrapper();
    }

    /**
     * Cell to pattern meta data wrapper.
     *
     * @return the cell to pattern meta data wrapper
     */
    @Bean
    public CellToPatternMetaDataWrapper cellToPatternMetaDataWrapper(){
        return new CellToPatternMetaDataWrapper();
    }

    /**
     * Http handler.
     *
     * @return the http handler
     */
    @Bean
    public HttpRequestHandler httpRequestHandler(){
        return new HttpRequestHandler();
    }

    /**
     * Session per call wrapper.
     *
     * @return the session per call wrapper
     */
    @Bean
    @Scope("prototype")
    public SessionPerCallWrapper sessionPerCallWrapper(){
        return new SessionPerCallWrapper();
    }

}
