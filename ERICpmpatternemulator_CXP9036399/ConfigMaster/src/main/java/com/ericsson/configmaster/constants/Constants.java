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

package com.ericsson.configmaster.constants;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * The Class Constants.
 */
public final class Constants {

    /** The Constant APP_PATH. */
    public static final String APP_PATH;
    static{
        APP_PATH = System.getProperty("APP_ROOT_PATH");
    }

    /** The Constant GENERAL_TAG. */
    // Tags used in schema
    public static final String GENERAL_TAG = "general";

    /** The Constant DOCNUMBER_TAG. */
    public static final String DOCNUMBER_TAG = "docno";

    /** The Constant REVISION_TAG. */
    public static final String REVISION_TAG = "revision";

    /** The Constant FFV_TAG. */
    public static final String FFV_TAG = "ffv";

    /** The Constant EVENT_TAG. */
    public static final String EVENT_TAG = "event";

    /** The Constant PARAMETERTYPE_TAG. */
    public static final String PARAMETERTYPE_TAG = "parametertype";

    /** The Constant NAME_TAG. */
    public static final String NAME_TAG = "name";

    /** The Constant ID_TAG. */
    public static final String ID_TAG = "id";

    /** The Constant PARAM_TAG. */
    public static final String PARAM_TAG = "param";

    /** The Constant TYPE_TAG. */
    public static final String TYPE_TAG = "type";

    /** The Constant NUMBEROFBYTES_TAG. */
    public static final String NUMBEROFBYTES_TAG = "numberofbytes";

    /** The Constant ISUSEVALID_TAG. */
    public static final String ISUSEVALID_TAG = "usevalid";

    /** The Constant HIGHRANGEVALUE_TAG. */
    public static final String HIGHRANGEVALUE_TAG = "high";

    /** The Constant LOWRANGEVALUE_TAG. */
    public static final String LOWRANGEVALUE_TAG = "low";

    /** The Constant ENUM_TAG. */
    public static final String ENUM_TAG = "enum";

    /** The Constant INTERNAL_ATTRIBUTE. */
    public static final String INTERNAL_ATTRIBUTE = "internal";

    /** The Constant RECORD_TAG. */
    public static final String RECORD_TAG = "record";

    /** The Constant CTR. */
    // Data sources
    public static final String CTR = "CTR";

    /** The Constant YES. */
    public static final String YES = "Yes";

    /** The Constant UNDEFINED. */
    public static final String UNDEFINED = "undefined";

    /** The Constant MODE. */
    public static final String MODE = "MODE";

    /** The Constant DATASOURCE. */
    public static final String DATASOURCE = "DATASOURCE";

    /** The Constant PATTERN_EXTRACTOR. */
    public static final String PATTERN_EXTRACTOR = "PE";

    /** The Constant NETWORK_EVOLUTION. */
    public static final String NETWORK_EVOLUTION = "NE";

    /** The Constant STREAM_PROCESSOR. */
    public static final String STREAM_PROCESSOR = "SP";

    /** The Constant ANALYZER. */
    public static final String ANALYZER = "AZ";

    /** The Constant CELL_META_DATA_FILE_NAME. */
    public static final String CELL_META_DATA_FILE_NAME = "cell_meta_data.ser";

    /** The Constant PATTERN_META_DATA_FILE_NAME. */
    public static final String PATTERN_META_DATA_FILE_NAME = "pattern_meta_data.ser";

    /** The Constant PATTERN_INFO_TEXT_FILE_NAME. */
    public static final String PATTERN_INFO_CSV_FILE_NAME = "pattern_info.csv";

    /** The Constant ANALYSIS_OUTPUT. */
    public static final String ANALYSIS_OUTPUT_FILE_NAME = "analysis_output.csv";

    /** The Constant INTERNAL_EUTRAN. */
    public static final String INTERNAL_EUTRAN = "internal";

    /** The Constant EXTERNAL_EUTRAN. */
    public static final String EXTERNAL_EUTRAN = "external";

    /** The Constant HANDOVER_RELATION_TYPE. */
    public static final String HANDOVER_RELATION_TYPE = "HANDOVER_RELATION_TYPE";

    /** The Constant ALL_PARAM_VALUES_TYPE. */
    public static final String ALL_PARAM_VALUES_TYPE = "ALL";

    /** The Constant MIN_MAX_TYPE. */
    public static final String MIN_MAX_TYPE = "MinMax";

    /** The Constant LIST_TYPE. */
    public static final String LIST_TYPE = "List";

    /** The Constant UINT. */
    // Data types
    public static final String UINT = "UINT";

    /** The Constant LONG. */
    public static final String LONG = "LONG";

    /** The Constant ENUM. */
    public static final String ENUM = "ENUM";

    /** The Constant BINARY. */
    public static final String BINARY = "BINARY";

    /** The Constant STRING. */
    public static final String STRING = "STRING";

    /** The Constant TBCD. */
    public static final String TBCD = "TBCD";

    /** The Constant SER_EXTENSION. */
    // File Extensions
    public static final String SER_EXTENSION = ".ser";

    /** The Constant GZ_EXTENSION. */
    public static final String GZ_EXTENSION = ".gz";

    /** The Constant BIN_EXTENSION. */
    public static final String BIN_EXTENSION = ".bin";

    /** The Constant FDN. */
    // File Attributes
    public static final String FDN = "UniqueId";

    /** The Constant START_DATE. */
    public static final String START_DATE = "Date";

    /** The Constant END_DATE. */
    public static final String END_DATE = "EndDate";

    /** The Constant START_TIME. */
    public static final String START_TIME = "StartTime";

    /** The Constant END_TIME. */
    public static final String END_TIME = "EndTime";

    /** The Constant START_TIME_OFFSET. */
    public static final String START_TIME_OFFSET = "StartTimeOffset";

    /** The Constant END_TIME_OFFSET. */
    public static final String END_TIME_OFFSET = "EndTimeOffset";

    /** The Constant ROP_INDEX. */
    public static final String ROP_INDEX = "RopIndex";

    /** The Constant HOUR. */
    public static final String HOUR = "HOUR";

    /** The Constant MINUTES. */
    public static final String MINUTES = "MINUTES";

    /** The Constant SECONDS. */
    public static final String SECONDS = "SECONDS";

    /** The Constant FORWARD_SLASH. */
    // Symbols & Characters
    public static final String FORWARD_SLASH = "/";

    /** The Constant UNDERSCORE. */
    public static final String UNDERSCORE = "_";

    /** The Constant COLON. */
    public static final String COLON = ":";

    /** The Constant DOT. */
    public static final String DOT = ".";

    /** The Constant NEWLINE. */
    public static final String NEWLINE = "\n";

    /** The Constant COMMA. */
    public static final String COMMA = ",";

    /** The Constant EQUAL. */
    public static final String EQUAL = "=";

    /** The Constant HYPHEN. */
    public static final String HYPHEN = "-";

    // Number constants
    /** The Constant ONE_HOUR_MINUTES. */
    public static final int ONE_HOUR_MINUTES = 60;

    /** The Constant ONE_MINUTE_SECONDS. */
    public static final int ONE_MINUTE_SECONDS = 60;

    /** The Constant ONE_SECOND_MILLISECS. */
    public static final int ONE_SECOND_MILLISECS = 1000;

    /** The Constant MAX_ROW_LIMIT_IN_EXCEL. */
    public static final int MAX_ROW_LIMIT_IN_EXCEL = 100000;

    // Parameters
    /** The Constant EVENT_PARAM_GLOBAL_CELL_ID. */
    public static final String EVENT_PARAM_GLOBAL_CELL_ID = "EVENT_PARAM_GLOBAL_CELL_ID";

    /** The Constant EVENT_PARAM_GUMMEI. */
    public static final String EVENT_PARAM_GUMMEI = "EVENT_PARAM_GUMMEI";

    /** The Constant EVENT_PARAM_NEIGHBOR_CGI. */
    public static final String EVENT_PARAM_NEIGHBOR_CGI = "EVENT_PARAM_NEIGHBOR_CGI";

    /** The Constant EVENT_PARAM_GENBID. */
    public static final String EVENT_PARAM_GENBID = "EVENT_PARAM_GENBID";

    /** The Constant EVENT_PARAM_RAC_UE_REF. */
    public static final String EVENT_PARAM_RAC_UE_REF = "EVENT_PARAM_RAC_UE_REF";

    /** The Constant EVENT_PARAM_ENBS1APID. */
    public static final String EVENT_PARAM_ENBS1APID = "EVENT_PARAM_ENBS1APID";

    /** The Constant TCP_STREAM_INDICATOR_RECORD. */
    public static final String TCP_STREAM_INDICATOR_RECORD = "TCP_STREAM_INDICATOR_RECORD";

    /** The Constant TCP_STREAM_INDICATOR_RECORD_TYPE. */
    public static final int TCP_STREAM_INDICATOR_RECORD_TYPE = 1;

    /** The Constant FILE_HEADER_RECORD_TYPE. */
    public static final int FILE_HEADER_RECORD_TYPE = 0;

    /** The Constant FILE_BUFFER_SIZE. */
    public static final int FILE_BUFFER_SIZE = 1024;

    /** The Constant DEFAULT_OFFSET. */
    public static final String DEFAULT_OFFSET = "+0000";

    /** The Constant DEFAULT_SECONDS. */
    public static final String DEFAULT_SECONDS = "00";

    /** The Constant LOG_FILE_PATH. */
    // File Locations
    public static final String LOG_FILE_PATH = APP_PATH + File.separator + "logs/PatternEmulator.log";

    /** The Constant APP_CONFIG_FILE_PATH. */
    public static final String APP_CONFIG_FILE_PATH = APP_PATH + "/config/app_config.xml";

    /** The Constant SCHEMA_HOLDER_PATH. */
    public static final String SCHEMA_HOLDER_PATH = APP_PATH + File.separator + "schemaHolder";

    /** The Constant PARAM_CONFIG_FILE_PATH. */
    public static final String PARAM_CONFIG_FILE_PATH = APP_PATH + "/config/param_config.xml";

    /** The Constant PATTERN_CONFIG_FILE_PATH. */
    public static final String PATTERN_CONFIG_FILE_PATH = "/config/pattern_config.properties";

    /** The Constant STREAMING_CONFIG_FILE_PATH. */
    public static final String STREAMING_CONFIG_FILE_PATH = "/config/streaming_config.properties";

    /** The Constant DEBUG. */
    // logging level constants
    public static final Level DEBUG = Level.FINE;

    /** The Constant INFO. */
    public static final Level INFO = Level.INFO;

    /** The Constant WARN. */
    public static final Level WARN = Level.WARNING;

    /** The Constant ERROR. */
    public static final Level ERROR = Level.SEVERE;

    /** The Constant HANDOVER_PARAM_LIST. */
    // Collections
    public static final List<String> HANDOVER_PARAM_LIST = Arrays.asList(new String[] { "EVENT_PARAM_HO_SOURCE_OR_TARGET_TYPE",
            "EVENT_PARAM_RAT_TYPE", "EVENT_PARAM_RAT", "EVENT_PARAM_HO_TYPE" });

}
