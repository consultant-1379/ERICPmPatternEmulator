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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The Class Mo.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "attr" })
@XmlRootElement(name = "mo")
public class Mo {

    /** The attr. */
    @XmlElement(required = true)
    protected List<Attr> attr;

    /** The fdn. */
    @XmlAttribute(required = true)
    protected String fdn;

    /** The mim name. */
    @XmlAttribute
    protected String mimName;

    /** The mim version. */
    @XmlAttribute
    protected String mimVersion;

    /** The last modified. */
    @XmlAttribute
    protected String lastModified;

    /**
     * Gets the attr.
     *
     * @return the attr
     */
    public List<Attr> getAttr(){
        if(this.attr == null){
            this.attr = new ArrayList<>();
        }
        return this.attr;
    }

    /**
     * Gets the fdn.
     *
     * @return the fdn
     */
    public String getFdn(){
        return this.fdn;
    }

    /**
     * Sets the fdn.
     *
     * @param paramString
     *            the new fdn
     */
    public void setFdn(String paramString){
        this.fdn = paramString;
    }

    /**
     * Gets the mim name.
     *
     * @return the mim name
     */
    public String getMimName(){
        return this.mimName;
    }

    /**
     * Sets the mim name.
     *
     * @param paramString
     *            the new mim name
     */
    public void setMimName(String paramString){
        this.mimName = paramString;
    }

    /**
     * Gets the mim version.
     *
     * @return the mim version
     */
    public String getMimVersion(){
        return this.mimVersion;
    }

    /**
     * Sets the mim version.
     *
     * @param paramString
     *            the new mim version
     */
    public void setMimVersion(String paramString){
        this.mimVersion = paramString;
    }

    /**
     * Gets the last modified.
     *
     * @return the lastModified
     */
    public String getLastModified(){
        return lastModified;
    }

    /**
     * Sets the last modified.
     *
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified(String lastModified){
        this.lastModified = lastModified;
    }

}
