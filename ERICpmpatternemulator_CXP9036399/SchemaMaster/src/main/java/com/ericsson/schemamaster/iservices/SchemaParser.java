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

package com.ericsson.schemamaster.iservices;

import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * The Interface SchemaParser.
 */
public interface SchemaParser {

    /**
     * Parses the schema.
     *
     * @param file
     *            the file
     * @throws Exception
     *             the exception
     */
    public void parseSchema(String file) throws Exception;

    /**
     * Gets the start element.
     *
     * @param startElement
     *            the start element
     * @param xmlEvent
     *            the xml event
     * @return the start element
     */
    public void getStartElement(StartElement startElement, XMLEvent xmlEvent);

    /**
     * Gets the end element.
     *
     * @param endElement
     *            the end element
     * @return the end element
     * @throws Exception
     *             the exception
     */
    public void getEndElement(EndElement endElement) throws Exception;

    /**
     * Gets the parameter elements.
     *
     * @param xmlEvent
     *            the xml event
     * @param startElement
     *            the start element
     * @return the parameter elements
     */
    public void getParameterElements(XMLEvent xmlEvent, StartElement startElement);

    /**
     * Gets the event elements.
     *
     * @param xmlEvent
     *            the xml event
     * @return the event elements
     */
    public void getEventElements(XMLEvent xmlEvent);

    /**
     * Gets the record elements.
     *
     * @param xmlEvent
     *            the xml event
     * @param startElement
     *            the start element
     * @return the record elements
     */
    public void getRecordElements(XMLEvent xmlEvent, StartElement startElement);

    /**
     * Gets the release info elements.
     *
     * @param xmlEvent
     *            the xml event
     * @return the release info elements
     */
    public void getReleaseInfoElements(XMLEvent xmlEvent);

    /**
     * Creates the schema info file.
     *
     * @throws Exception
     *             the exception
     */
    public void createSchemaInfoFile() throws Exception;
}
