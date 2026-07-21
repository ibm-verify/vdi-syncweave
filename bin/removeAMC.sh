#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# Licensed Materials - Property of IBM
#
# Restricted Materials of IBM
#
# (C) COPYRIGHT International Business Machines Corp. 2022
# All Rights Reserved
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

# Function to source in the TDI setupCmdLine.sh and backupDir.sh script
setupTDIEnv ()
{
   . "$TEMP_BIN_DIR/setupCmdLine.sh"
}

#
# CMDFINDER holds the command which is used to find other commands
# depending on the platform.
#
CMDFINDER=which

UNAME_OS=`uname`

if [ "$UNAME_OS" = "OS/390" -o "$UNAME_OS" = "OS400" ] ; then
	CMDFINDER=whence
fi


TEMP_BIN_DIR=`$CMDFINDER $0`
TEMP_BIN_DIR=`dirname $TEMP_BIN_DIR`

SKIP_SOLDIR_SETUP=1
SKIP_ISCDIR_SETUP=1

setupTDIEnv "$TEMP_BIN_DIR" || exit 1

unset SKIP_SOLDIR_SETUP
unset SKIP_ISCDIR_SETUP

# echo Home dir = $TDI_HOME_DIR

AMC_DIR="$TDI_HOME_DIR/amc"
BIN_DIR="$TDI_HOME_DIR/bin/amc"
LWI_DIR="$TDI_HOME_DIR/lwi"

if [ -d "$AMC_DIR" ] ; then
  echo Removing $AMC_DIR
  rm -rf "$AMC_DIR"
fi

if [ -d "$BIN_DIR" ] ; then
  echo Removing $BIN_DIR
  rm -rf "$BIN_DIR"
fi

if [ -d "$LWI_DIR" ] ; then
  echo Removing $LWI_DIR
  rm -rf "$LWI_DIR"
fi

