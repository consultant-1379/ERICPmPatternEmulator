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

package com.ericsson.topologyparser.autogen;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class Attr.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "content" })
@XmlRootElement(name = "attr")
public class Attr {

    /** The content. */
    @XmlElementRefs({ @javax.xml.bind.annotation.XmlElementRef(name = "struct", namespace = "http://www.w3.org/namespace/", type = Struct.class),
            @javax.xml.bind.annotation.XmlElementRef(name = "seq", namespace = "http://www.w3.org/namespace/", type = Seq.class) })
    @XmlMixed
    protected List<Object> content;

    /** The name. */
    @XmlAttribute(required = true)
    protected String name;

    /** The node ip address. */
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String nodeIpAddress;

    /** The node ipv 6 address. */
    @XmlAttribute(required = true)
    @XmlSchemaType(name = "anySimpleType")
    protected String nodeIpv6Address;

    /**
     * Gets the content.
     *
     * @return the content
     */
    public List<Object> getContent(){
        if(this.content == null){
            this.content = new ArrayList<>();
        }
        return this.content;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName(){
        return this.name;
    }

    /**
     * Sets the name.
     *
     * @param paramString
     *            the new name
     */
    public void setName(String paramString){
        this.name = paramString;
    }

    /**
     * Gets the node ip address.
     *
     * @return the node ip address
     */
    public String getNodeIpAddress(){
        return this.nodeIpAddress;
    }

    /**
     * Sets the node ip address.
     *
     * @param paramString
     *            the new node ip address
     */
    public void setNodeIpAddress(String paramString){
        this.nodeIpAddress = paramString;
    }

    /**
     * Gets the node ipv 6 address.
     *
     * @return the node ipv 6 address
     */
    public String getNodeIpv6Address(){
        return this.nodeIpv6Address;
    }

    /**
     * Sets the node ipv 6 address.
     *
     * @param paramString
     *            the new node ipv 6 address
     */
    public void setNodeIpv6Address(String paramString){
        this.nodeIpv6Address = paramString;
    }
}
