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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2018.07.11 at 03:24:54 PM CEST
//

package com.ericsson.configmaster.appconfig.autogen;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * com.ericsson.configmaster.appconfig.autogen package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the Java representation for XML content. The Java representation of XML
 * content can consist of schema derived interfaces and classes representing the binding of schema type definitions, element declarations and model
 * groups. Factory methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * com.ericsson.configmaster.appconfig.autogen
     */
    public ObjectFactory(){
    }

    /**
     * Create an instance of {@link AppConfiguration }
     */
    public AppConfiguration createAppConfiguration(){
        return new AppConfiguration();
    }

    /**
     * Create an instance of {@link AppConfiguration.DataSource }
     */
    public AppConfiguration.DataSource createAppConfigurationDataSource(){
        return new AppConfiguration.DataSource();
    }

    /**
     * Create an instance of {@link AppConfiguration.DataSource.ComplexParameters }
     */
    public AppConfiguration.DataSource.ComplexParameters createAppConfigurationDataSourceComplexParameters(){
        return new AppConfiguration.DataSource.ComplexParameters();
    }

    /**
     * Create an instance of {@link AppConfiguration.DataSource.ComplexParameters.Parameter }
     */
    public AppConfiguration.DataSource.ComplexParameters.Parameter createAppConfigurationDataSourceComplexParametersParameter(){
        return new AppConfiguration.DataSource.ComplexParameters.Parameter();
    }

    /**
     * Create an instance of {@link AppConfiguration.DataSource.GeneralInfo }
     */
    public AppConfiguration.DataSource.GeneralInfo createAppConfigurationDataSourceGeneralInfo(){
        return new AppConfiguration.DataSource.GeneralInfo();
    }

    /**
     * Create an instance of {@link AppConfiguration.DataSource.ComplexParameters.Parameter.Component }
     */
    public AppConfiguration.DataSource.ComplexParameters.Parameter.Component createAppConfigurationDataSourceComplexParametersParameterComponent(){
        return new AppConfiguration.DataSource.ComplexParameters.Parameter.Component();
    }

}
