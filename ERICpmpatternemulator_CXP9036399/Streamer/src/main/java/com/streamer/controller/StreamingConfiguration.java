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

package com.streamer.controller;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.streamer.entities.Connection;
import com.streamer.services.ConnectionManager;
import com.streamer.services.EventStreamer;
import com.streamer.services.Instrumentation;
import com.streamer.services.StreamerAppLogger;

/**
 * The Class StreamingConfiguration.
 */
@Configuration
@EnableScheduling
public class StreamingConfiguration {

    /**
     * Streamer app logger.
     *
     * @return the streamer app logger
     */
    @Bean
    public StreamerAppLogger streamerAppLogger(){
        return new StreamerAppLogger();
    }

    /**
     * Connection.
     *
     * @return the connection
     */
    @Bean
    @Scope("prototype")
    public Connection connection(){
        return new Connection();
    }

    /**
     * Event streamer.
     *
     * @return the event streamer
     */
    @Bean
    @Scope("prototype")
    public EventStreamer eventStreamer(){
        return new EventStreamer();
    }

    /**
     * Instrumentation.
     *
     * @return the instrumentation
     */
    @Bean
    @Scope("prototype")
    public Instrumentation instrumentation(){
        return new Instrumentation();
    }

    /**
     * Connection manager.
     *
     * @return the connection manager
     */
    @Bean
    @Scope("prototype")
    public ConnectionManager connectionManager(){
        return new ConnectionManager();
    }

    /**
     * Container factory.
     *
     * @return the tomcat embedded servlet container factory
     */
    @Bean
    public TomcatEmbeddedServletContainerFactory containerFactory(){
        TomcatEmbeddedServletContainerFactory containerFactory = new TomcatEmbeddedServletContainerFactory();
        TomcatConnectorCustomizer tomcatConnectorCustomizer = new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector){
                if(connector.getProtocolHandler() instanceof AbstractHttp11Protocol){
                    ((AbstractHttp11Protocol<?>) connector.getProtocolHandler()).setMaxSwallowSize(-1);
                }
                connector.setMaxPostSize(-1);
            }
        };
        containerFactory.addConnectorCustomizers(tomcatConnectorCustomizer);
        return containerFactory;
    }
}
