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
package com.ericsson.eventenricher.entities;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ericsson.utilities.entities.EventStreamerWrapper;

@Component
public class CallPerSecondEventsWrapper {

    private List<EventStreamerWrapper> eventList;
    private String fdn;

    public List<EventStreamerWrapper> getEventList(){
        return eventList;
    }

    public void setEventList(List<EventStreamerWrapper> eventList){
        this.eventList = eventList;
    }

    public String getFdn(){
        return fdn;
    }

    public void setFdn(String fdn){
        this.fdn = fdn;
    }

}
