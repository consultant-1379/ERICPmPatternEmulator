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
package com.ericsson.utilities.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class HeaderStreamerWrapper implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private Map<String, byte[]> nodeHeaderMap;

    private List<String> destIpPortList;

    private String processID;

    public Map<String, byte[]> getNodeHeaderMap(){
        return nodeHeaderMap;
    }

    public void setNodeHeaderMap(Map<String, byte[]> nodeHeaderMap){
        this.nodeHeaderMap = nodeHeaderMap;
    }

    public List<String> getDestIpPortList(){
        return destIpPortList;
    }

    public void setDestIpPortList(List<String> destIpPortList){
        this.destIpPortList = destIpPortList;
    }

    public String getProcessID(){
        return processID;
    }

    public void setProcessID(String processID){
        this.processID = processID;
    }
}
