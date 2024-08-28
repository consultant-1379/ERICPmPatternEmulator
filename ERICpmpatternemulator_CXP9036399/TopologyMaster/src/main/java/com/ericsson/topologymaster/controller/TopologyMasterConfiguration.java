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

package com.ericsson.topologymaster.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ericsson.topologyenricher.services.TopologyEnricher;
import com.ericsson.topologyparser.entities.CellRelationWrapper;
import com.ericsson.topologyparser.entities.CellWrapper;
import com.ericsson.topologyparser.entities.MmeNodeWrapper;
import com.ericsson.topologyparser.entities.NodeWrapper;
import com.ericsson.topologyparser.services.MOParser;
import com.ericsson.topologyparser.services.TopologyParserUtil;
import com.ericsson.topologyparser.services.XmlTopologyParserThread;

/**
 * The Class TopologyMasterConfiguration.
 */
@Configuration
public class TopologyMasterConfiguration {

    /**
     * Topology master controller.
     *
     * @return the topology master controller
     */
    @Bean
    public TopologyMasterController topologyMasterController(){
        return new TopologyMasterController();
    }

    /**
     * Mo parser.
     *
     * @return the MO parser
     */
    @Bean
    public MOParser moParser(){
        return new MOParser();
    }

    /**
     * Xml topology parser thread.
     *
     * @return the xml topology parser thread
     */
    @Bean
    @Scope("prototype")
    public XmlTopologyParserThread xmlTopologyParserThread(){
        return new XmlTopologyParserThread();
    }

    /**
     * Topology parser util.
     *
     * @return the topology parser util
     */
    @Bean
    public TopologyParserUtil topologyParserUtil(){
        return new TopologyParserUtil();
    }

    /**
     * Node wrapper.
     *
     * @return the node wrapper
     */
    @Bean
    @Scope("prototype")
    public NodeWrapper nodeWrapper(){
        return new NodeWrapper();
    }

    /**
     * Cell wrapper.
     *
     * @return the cell wrapper
     */
    @Bean
    @Scope("prototype")
    public CellWrapper cellWrapper(){
        return new CellWrapper();
    }

    /**
     * Cell relation wrapper.
     *
     * @return the cell relation wrapper
     */
    @Bean
    @Scope("prototype")
    public CellRelationWrapper cellRelationWrapper(){
        return new CellRelationWrapper();
    }

    /**
     * Mme node wrapper.
     *
     * @return the mme node wrapper
     */
    @Bean
    @Scope("prototype")
    public MmeNodeWrapper mmeNodeWrapper(){
        return new MmeNodeWrapper();
    }

    /**
     * Topology enricher.
     *
     * @return the topology enricher
     */
    @Bean
    public TopologyEnricher topologyEnricher(){
        return new TopologyEnricher();
    }

}
