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
# Purpose : This script to used to host streamer application.
#
# Usage   : ${INSTALL_DIR}/bin/startStreamer.sh
#
# ********************************************************************

INSTALL_DIR="$(cd .. ; pwd)"
MAIN_CLASS="com.streamer.controller.StreamingController"
PORT=8000

java -Xms2m -Xmx60g -cp "${INSTALL_DIR}/lib/*" ${MAIN_CLASS} ${INSTALL_DIR} ${PORT}