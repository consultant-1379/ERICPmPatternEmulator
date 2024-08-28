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
# Name    : startListener.sh
# Purpose : This script to used to start listener
#
# Usage   : ${INSTALL_DIR}/bin/startListener.sh
#
# ********************************************************************

INSTALL_DIR="$(cd .. ; pwd)"
MAIN_CLASS="com.ericsson.utilities.services.Listener"

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
    echo ""
    echo "Usage: ${INSTALL_DIR}/bin/startListener.sh <Start Port> <End Port>"
}


### Function: Main ###
# ********************************************************************
#   Validates the command line arguments and calls the intermediate 
#	scripts
# ********************************************************************
startPort=$1
endPort=$2
java -cp "${INSTALL_DIR}/lib/*" ${MAIN_CLASS} ${INSTALL_DIR} ${startPort} ${endPort}

exit 0;
