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

package com.ericsson.schemamaster.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

import com.ericsson.schemamaster.entities.Event;
import com.ericsson.schemamaster.entities.Parameter;
import com.ericsson.schemamaster.entities.Record;
import com.ericsson.schemamaster.services.BitPackDecoder;
import com.ericsson.schemamaster.services.BytePackDecoder;
import com.ericsson.schemamaster.services.BytePackEncoder;
import com.ericsson.schemamaster.services.SchemaLoader;
import com.ericsson.schemamaster.services.SessionAttributeDecoder;

/**
 * The Class SchemaMasterConfiguration.
 */
@Configuration
@Lazy
public class SchemaMasterConfiguration {

    /**
     * Event.
     *
     * @return the event
     */
    @Bean
    @Scope("prototype")
    public Event event(){
        return new Event();
    }

    /**
     * Parameter.
     *
     * @return the parameter
     */
    @Bean
    @Scope("prototype")
    public Parameter parameter(){
        return new Parameter();
    }

    /**
     * Record.
     *
     * @return the record
     */
    @Bean
    @Scope("prototype")
    public Record record(){
        return new Record();
    }

    /**
     * Schema loader.
     *
     * @return the schema loader
     */
    @Bean
    public SchemaLoader schemaLoader(){
        return new SchemaLoader();
    }

    /**
     * Session attribute decoder.
     *
     * @return the session attribute decoder
     */
    @Bean
    public SessionAttributeDecoder sessionAttributeDecoder(){
        return new SessionAttributeDecoder();
    }

    /**
     * Bit pack decoder.
     *
     * @return the bit pack decoder
     */
    @Bean
    public BitPackDecoder bitPackDecoder(){
        return new BitPackDecoder();
    }

    /**
     * Byte pack decoder.
     *
     * @return the byte pack decoder
     */
    @Bean
    public BytePackDecoder bytePackDecoder(){
        return new BytePackDecoder();
    }

    /**
     * Byte pack encoder.
     *
     * @return the byte pack encoder
     */
    @Bean
    public BytePackEncoder bytePackEncoder(){
        return new BytePackEncoder();
    }
}
