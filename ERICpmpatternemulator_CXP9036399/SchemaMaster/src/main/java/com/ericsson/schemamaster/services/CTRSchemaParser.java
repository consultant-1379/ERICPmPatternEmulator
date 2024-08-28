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

package com.ericsson.schemamaster.services;

import static com.ericsson.configmaster.constants.Constants.DOCNUMBER_TAG;
import static com.ericsson.configmaster.constants.Constants.ENUM_TAG;
import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.EVENT_TAG;
import static com.ericsson.configmaster.constants.Constants.FFV_TAG;
import static com.ericsson.configmaster.constants.Constants.GENERAL_TAG;
import static com.ericsson.configmaster.constants.Constants.HIGHRANGEVALUE_TAG;
import static com.ericsson.configmaster.constants.Constants.ID_TAG;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.INTERNAL_ATTRIBUTE;
import static com.ericsson.configmaster.constants.Constants.ISUSEVALID_TAG;
import static com.ericsson.configmaster.constants.Constants.LOWRANGEVALUE_TAG;
import static com.ericsson.configmaster.constants.Constants.NAME_TAG;
import static com.ericsson.configmaster.constants.Constants.NUMBEROFBYTES_TAG;
import static com.ericsson.configmaster.constants.Constants.PARAMETERTYPE_TAG;
import static com.ericsson.configmaster.constants.Constants.PARAM_TAG;
import static com.ericsson.configmaster.constants.Constants.RECORD_TAG;
import static com.ericsson.configmaster.constants.Constants.REVISION_TAG;
import static com.ericsson.configmaster.constants.Constants.SCHEMA_HOLDER_PATH;
import static com.ericsson.configmaster.constants.Constants.SER_EXTENSION;
import static com.ericsson.configmaster.constants.Constants.TYPE_TAG;
import static com.ericsson.configmaster.constants.Constants.YES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.ericsson.schemamaster.configuration.SchemaMasterConfiguration;
import com.ericsson.schemamaster.entities.Event;
import com.ericsson.schemamaster.entities.Parameter;
import com.ericsson.schemamaster.entities.Record;
import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.schemamaster.iservices.SchemaParser;
import com.ericsson.utilities.services.Utils;

/**
 * The Class CTRSchemaParser.
 */
@Component
public class CTRSchemaParser implements SchemaParser {

    /** The bean factory. */
    @Autowired
    SchemaMasterConfiguration beanFactory;

    /** The resource loader. */
    @Autowired
    ResourceLoader resourceLoader;

    /** The is parameter. */
    private boolean isParameter;

    /** The is event. */
    private boolean isEvent;

    /** The is record. */
    private boolean isRecord;

    /** The is general info. */
    private boolean isGeneralInfo;

    /** The event obj. */
    private Event eventObj = null;

    /** The param obj. */
    private Parameter paramObj = null;

    /** The record obj. */
    private Record recordObj = null;

    /** The tag identifier. */
    private String tagIdentifier = "";

    /** The array param set value flag map. */
    private Map<String, Boolean> arrayParamSetValueFlagMap;

    /** The schemarelease obj. */
    private SchemaRelease schemareleaseObj;

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.schemamaster.iservices.SchemaParser#parseSchema(java.lang.String)
     */
    @Override
    public void parseSchema(String schemaFilePath) throws FileNotFoundException, Exception{
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try{
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(schemaFilePath));
            while (xmlEventReader.hasNext()){
                XMLEvent xmlEvent = xmlEventReader.nextEvent();

                if(xmlEvent.isStartElement()){
                    StartElement startElement = xmlEvent.asStartElement();
                    getStartElement(startElement,xmlEventReader.nextEvent());
                }

                if(xmlEvent.isEndElement()){
                    EndElement endElement = xmlEvent.asEndElement();
                    getEndElement(endElement);
                }
            }
        } catch(FileNotFoundException ef){
            Utils.logMessage(ERROR,"Schema input file does not exists" + ef);
            throw ef;
        } catch(Exception e){
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.schemamaster.iservices.SchemaParser#getStartElement(javax.xml.stream.events.StartElement, javax.xml.stream.events.XMLEvent)
     */
    @Override
    public void getStartElement(StartElement startElement, XMLEvent xmlEvent){
        tagIdentifier = startElement.getName().getLocalPart();
        if(tagIdentifier.equals(PARAMETERTYPE_TAG)){
            paramObj = beanFactory.parameter();
            isParameter = true;
        } else if(tagIdentifier.equals(EVENT_TAG)){
            eventObj = beanFactory.event();
            eventObj.setParameterMap(new LinkedHashMap<String, Parameter>());
            isEvent = true;
        } else if(tagIdentifier.equals(RECORD_TAG)){
            recordObj = beanFactory.record();
            isRecord = true;
        } else if(tagIdentifier.equals(GENERAL_TAG)){
            schemareleaseObj = new SchemaRelease();
            isGeneralInfo = true;
        }

        if(isParameter){
            getParameterElements(xmlEvent,startElement);
        }
        if(isEvent){
            getEventElements(xmlEvent);
        }
        if(isRecord){
            getRecordElements(xmlEvent,startElement);
        }
        if(isGeneralInfo){
            getReleaseInfoElements(xmlEvent);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.schemamaster.iservices.SchemaParser#getEndElement(javax.xml.stream.events.EndElement)
     */
    @Override
    public void getEndElement(EndElement endElement) throws Exception{
        if(endElement.getName().getLocalPart().equals(EVENT_TAG)){
            isEvent = false;
            if(null != arrayParamSetValueFlagMap){
                for(String paramName : arrayParamSetValueFlagMap.keySet()){
                    arrayParamSetValueFlagMap.put(paramName,true);
                }
            }
            schemareleaseObj.getEventIDMap().put(eventObj.getEventID(),eventObj);
            schemareleaseObj.getEventNameMap().put(eventObj.getEventName(),eventObj);
        } else if(endElement.getName().getLocalPart().equals(PARAMETERTYPE_TAG)){
            isParameter = false;
            schemareleaseObj.getParameterMap().put(paramObj.getParameterName(),paramObj);
        } else if(endElement.getName().getLocalPart().equals(RECORD_TAG)){
            isRecord = false;
            schemareleaseObj.getRecordMap().put(recordObj.getRecordName(),recordObj);
        } else if(endElement.getName().getLocalPart().equals("eventspecification")){
            createSchemaInfoFile();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.schemamaster.iservices.SchemaParser#getEventElements(javax.xml.stream.events.XMLEvent)
     */
    @Override
    public void getEventElements(XMLEvent xmlEvent){
        try{
            switch(tagIdentifier){
            case NAME_TAG:
                eventObj.setEventName(xmlEvent.asCharacters().getData());
                break;
            case ID_TAG:
                eventObj.setEventID(Integer.parseInt(xmlEvent.asCharacters().getData()));
                break;
            case PARAM_TAG:
                String paramName = xmlEvent.asCharacters().getData();
                Parameter parameterObj = schemareleaseObj.getParameterMap().get(paramName);
                if(eventObj.getParameterMap().containsKey(paramName)){
                    if(null == arrayParamSetValueFlagMap){
                        arrayParamSetValueFlagMap = new HashMap<String, Boolean>();
                    }
                    if(null == arrayParamSetValueFlagMap.get(paramName)){
                        arrayParamSetValueFlagMap.put(paramName,false);
                    }
                    if(!arrayParamSetValueFlagMap.get(paramName)){
                        parameterObj.setArraySize(parameterObj.getArraySize() + 1);
                    }
                }
                eventObj.getParameterMap().put(paramName,parameterObj);
                break;
            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error while reading events data from CTR schema file. " + e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.schemamaster.iservices.SchemaParser#getParameterElements(javax.xml.stream.events.XMLEvent,
     * javax.xml.stream.events.StartElement)
     */
    @Override
    public void getParameterElements(XMLEvent xmlEvent, StartElement startElement){
        try{
            switch(tagIdentifier){
            case NAME_TAG:
                paramObj.setParameterName(xmlEvent.asCharacters().getData());
                break;
            case TYPE_TAG:
                paramObj.setType(xmlEvent.asCharacters().getData());
                break;
            case NUMBEROFBYTES_TAG:
                paramObj.setLength(xmlEvent.asCharacters().getData());
                paramObj.setVariableSize(!Utils.isNumeric(paramObj.getLength()));
                break;
            case ISUSEVALID_TAG:
                if(xmlEvent.asCharacters().getData().equalsIgnoreCase(YES)){
                    paramObj.setUseValid(true);
                }
                break;
            case HIGHRANGEVALUE_TAG:
                paramObj.setHighRangeValue(Long.parseLong(xmlEvent.asCharacters().getData()));
                break;
            case LOWRANGEVALUE_TAG:
                paramObj.setLowRangeValue(Long.parseLong(xmlEvent.asCharacters().getData()));
                break;
            case ENUM_TAG:
                @SuppressWarnings("unchecked")
                Iterator<Attribute> enumAttributes = startElement.getAttributes();
                String name = "";
                String value = "";
                while (enumAttributes.hasNext()){
                    Attribute attr = enumAttributes.next();
                    if(attr.getName().toString().equalsIgnoreCase(INTERNAL_ATTRIBUTE)){
                        name = attr.getValue().toString();
                    } else{
                        value = attr.getValue();
                    }
                }
                paramObj.getEnumParamValueMap().put(name,value);
                break;
            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error while reading parameter data from CTR schema file. " + e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.schemamaster.iservices.SchemaParser#getRecordElements(javax.xml.stream.events.XMLEvent, javax.xml.stream.events.StartElement)
     */
    @Override
    public void getRecordElements(XMLEvent xmlEvent, StartElement startElement){
        try{
            switch(tagIdentifier){
            case NAME_TAG:
                recordObj.setRecordName(xmlEvent.asCharacters().getData());
                break;
            case TYPE_TAG:
                recordObj.setType(Integer.parseInt(xmlEvent.asCharacters().getData()));
                break;
            case PARAM_TAG:
                @SuppressWarnings("unchecked")
                Iterator<Attribute> enumAttributes = startElement.getAttributes();
                Attribute attr = enumAttributes.next();
                if(attr.getName().toString().equalsIgnoreCase(TYPE_TAG)){
                    recordObj.getRecordParamMap().put(attr.getValue(),xmlEvent.asCharacters().getData());
                }
                break;
            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error while reading record data from CTR schema file. " + e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.schemamaster.iservices.SchemaParser#getReleaseInfoElements(javax.xml.stream.events.XMLEvent)
     */
    @Override
    public void getReleaseInfoElements(XMLEvent xmlEvent){
        try{
            switch(tagIdentifier){
            case DOCNUMBER_TAG:
                schemareleaseObj.setDocNumber(xmlEvent.asCharacters().getData());
                break;
            case REVISION_TAG:
                schemareleaseObj.setRevision(xmlEvent.asCharacters().getData());
                break;
            case FFV_TAG:
                schemareleaseObj.setFfv(xmlEvent.asCharacters().getData());
                break;
            }
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error while reading release information from CTR schema file. " + e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.schemamaster.iservices.SchemaParser#createSchemaInfoFile()
     */
    @Override
    public void createSchemaInfoFile() throws Exception{
        Utils.logMessage(INFO,"Schema file is parsed and serialization started");
        try{
            String docno = schemareleaseObj.getDocNumber();
            if(docno.contains("/")){
                docno = docno.split("/")[1];
            }
            String fileName = schemareleaseObj.getFfv() + "_" + docno + "_" + schemareleaseObj.getRevision();
            Utils.logMessage(INFO,"Release identified : " + fileName);
            schemareleaseObj.setReleaseName(fileName);
            File schemaFile = new File(SCHEMA_HOLDER_PATH + File.separator + fileName + SER_EXTENSION);
            if(schemaFile.exists()){
                Utils.logMessage(INFO,"Schema file is already present in schema_holder location for the given release");
                if(!schemaFile.delete()){
                    Utils.logMessage(ERROR,"Couldnot delete schema file from schema_holder location. Please delete it manully and restart the tool.");
                    throw new Exception();
                } else{
                    Utils.logMessage(INFO,"Old version of schema file is deleted");
                }
            }
            if(!schemaFile.exists()){
                schemaFile.createNewFile();
            }
            FileOutputStream fileOut = new FileOutputStream(schemaFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(schemareleaseObj);
            out.close();
            fileOut.close();
            Utils.logMessage(INFO,"Schema object is written to schema_holder location");
        } catch(Exception e){
            Utils.logMessage(ERROR,"Error while writing schema object to serialized file " + e);
            throw e;
        }
    }

}
