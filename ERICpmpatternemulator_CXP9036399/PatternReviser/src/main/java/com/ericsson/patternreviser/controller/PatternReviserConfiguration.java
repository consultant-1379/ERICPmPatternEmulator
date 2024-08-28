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
package com.ericsson.patternreviser.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.ericsson.patternreviser.analyzer.services.AnalyzerThread;
import com.ericsson.patternreviser.eventaddition.services.EventAdditionThread;

@Configuration
public class PatternReviserConfiguration {

    @Bean
    public PatternReviserController patternReviserController(){
        return new PatternReviserController();
    }

    @Bean
    @Scope("prototype")
    public AnalyzerThread analyzerThread(){
        return new AnalyzerThread();
    }

    @Bean
    @Scope("prototype")
    public EventAdditionThread eventAdditionThread(){
        return new EventAdditionThread();
    }
}
