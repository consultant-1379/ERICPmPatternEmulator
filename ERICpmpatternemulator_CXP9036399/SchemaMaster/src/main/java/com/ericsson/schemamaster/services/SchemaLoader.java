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

import static com.ericsson.configmaster.constants.Constants.ERROR;
import static com.ericsson.configmaster.constants.Constants.INFO;
import static com.ericsson.configmaster.constants.Constants.SCHEMA_HOLDER_PATH;
import static com.ericsson.configmaster.constants.Constants.SER_EXTENSION;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ericsson.schemamaster.entities.SchemaRelease;
import com.ericsson.utilities.services.Utils;

/**
 * The Class SchemaLoader.
 */
@Service
public class SchemaLoader {

    /** The release object map. */
    private Map<String, SchemaRelease> releaseObjectMap = new HashMap<String, SchemaRelease>();

    /**
     * Load schema.
     *
     * @param release
     *            the release
     * @return the schema release
     */
    public SchemaRelease loadSchema(String release){
        SchemaRelease schemaReleaseObj = releaseObjectMap.get(release);
        if(null != schemaReleaseObj){
            return schemaReleaseObj;
        } else{
            return getSchemaReleaseObj(release);
        }

    }

    /**
     * Gets the schema release obj.
     *
     * @param release
     *            the release
     * @return the schema release obj
     */
    private synchronized SchemaRelease getSchemaReleaseObj(String release){
        try{
            SchemaRelease schemaReleaseObj = releaseObjectMap.get(release);
            if(null != schemaReleaseObj){
                return schemaReleaseObj;
            }
            File schemaFile = new File(SCHEMA_HOLDER_PATH + File.separator + release + SER_EXTENSION);
            if(!schemaFile.exists()){
                Utils.logMessage(INFO,"Tool does not support schema for " + release,true);
                return null;
            }
            FileInputStream fileOut = new FileInputStream(schemaFile);
            ObjectInputStream out = new ObjectInputStream(fileOut);
            schemaReleaseObj = (SchemaRelease) out.readObject();
            out.close();
            fileOut.close();

            releaseObjectMap.put(release,schemaReleaseObj);
            Utils.logMessage(INFO,"Schema loaded for release - " + release);

            return schemaReleaseObj;
        } catch(Exception ex){
            Utils.logMessage(ERROR,"Schema not supported for release - " + release,true);
            return null;
        }
    }
}
