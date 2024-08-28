#!/bin/bash
# ********************************************************************
# Ericsson Inc.	                                     			SCRIPT
# ********************************************************************
#
# (c) Ericsson Inc. 2018 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property of
# Ericsson Inc. The programs may be used and/or copied only with written
# permission from Ericsson Inc. or in accordance with the terms and
# conditions stipulated in the agreement/contract under which the
# program(s) have been supplied.
#
# ********************************************************************
# Name    : startTool.sh
# Purpose : This script to used to play multiple modes
#
# Usage   : ${INSTALL_DIR}/bin/startPatternEmulator.sh
#
# ********************************************************************

INSTALL_DIR="$(cd .. ; pwd)"
MAIN_CLASS="com.ericsson.parentcontroller.PatternEmulatorController"
PORT=8001

### Function: usage ###
# ********************************************************************
#   Print out the usage message
#
# Arguments:
#       none
# Return Values:
#       none
# ********************************************************************
usage(){
	echo "The modes of this tool and its usage are as follows :"
	echo "1. PATTERN_EXTRACTOR mode"
	echo "	Usage: ${INSTALL_DIR}/bin/startPatternEmulator.sh <-m PE>"
	echo "2. NETWORK_EVOLUTION mode"
	echo "	Usage: ${INSTALL_DIR}/bin/startPatternEmulator.sh <-m NE> <-d datasource> <-s schema file location>" 
	echo "3. STREAM_PROCESSOR mode"
	echo "	Usage: ${INSTALL_DIR}/bin/startPatternEmulator.sh <-m SP>"
	echo "4. ANALYZER mode"
	echo "	Usage: ${INSTALL_DIR}/bin/startPatternEmulator.sh <-m AZ>"
	echo ""
}


### Function: Main ###
# ********************************************************************
#   Validates the command line arguments and starts the tool for 
#	given mode
# ********************************************************************
while getopts "m:d:s:h" args; do
	case "$args" in
		m) mode="${OPTARG^^}";;
		d) datasource="${OPTARG^^}";;
		s) schemaFileLocation="${OPTARG}";;
		h) usage
		   exit 1;;
		*) echo "Invalid arguments"
		   usage
		   exit 1;;
	esac
done

# Validating arguments
if [ -z "${mode}" ]; then
	echo "ERROR : Incorrect number of arguments"
	usage
	exit 1
else
	if [ ${mode} == "NE" ]; then
		if [ -z "${datasource}" ] || [ -z "${schemaFileLocation}" ];then
			echo "ERROR : Incorrect number of arguments"
			usage
			exit 1 
		fi
		java -cp "${INSTALL_DIR}/lib/*" ${MAIN_CLASS} ${INSTALL_DIR} ${PORT} ${mode} ${datasource} ${schemaFileLocation}
		
	elif [ ${mode} == "PE" ]; then
		##Read memory allocation from pattern_config.properties file
		sed $'s/\r//' -i ${INSTALL_DIR}/config/pattern_config.properties
		MEMORY=`cat ${INSTALL_DIR}/config/pattern_config.properties | grep "MEMORY" | cut -d"=" -f2`
		java -Xms2m -Xmx${MEMORY} -cp "${INSTALL_DIR}/lib/*" ${MAIN_CLASS} ${INSTALL_DIR} ${PORT} ${mode}
		
	elif [ ${mode} == "SP" ]; then
		##Read memory allocation from pattern_config.properties file
		sed $'s/\r//' -i ${INSTALL_DIR}/config/streaming_config.properties
		MEMORY=`cat ${INSTALL_DIR}/config/streaming_config.properties | grep "MEMORY" | cut -d"=" -f2`
		java -Xms2m -Xmx${MEMORY} -cp "${INSTALL_DIR}/lib/*" ${MAIN_CLASS} ${INSTALL_DIR} ${PORT} ${mode}
	
	elif [ ${mode} == "AZ" ]; then
		java -cp "${INSTALL_DIR}/lib/*" ${MAIN_CLASS} ${INSTALL_DIR} ${PORT} ${mode}
	
	else
		echo "Invalid Mode"
		usage
	fi
fi

