#!/bin/sh

# IBM_PROLOG_BEGIN_TAG 
#  
# %I%, %G%
#  
# Licensed Materials - Property of IBM 
#  
# Restricted Materials of IBM 
#  
# (C) COPYRIGHT International Business Machines Corp. 2007, 2010
# All Rights Reserved 
#  
# US Government Users Restricted Rights - Use, duplication or 
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
#  
# IBM_PROLOG_END_TAG 

#################################################################
# Command sets the ISC Home directory for all TDI commands
#################################################################

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
. "${TEMP_BIN_DIR}/setupCmdLine.sh"
}

if [ $# -ne 1 -o ! -d "$1" ] ; then
	echo "Usage Error: setISCHome TDI_ISC_HOMEdir" >&2
	exit 1
fi

#
# CMDFINDER holds the command which is used to find other commands
# depending on the platform.
#
CMDFINDER="which"

UNAME_OS=`uname`

if [ "${UNAME_OS}" = "OS/390" -o "${UNAME_OS}" = "OS400" ] ; then
	CMDFINDER="whence"
fi
	

TEMP_BIN_DIR=`${CMDFINDER} $0`
TEMP_BIN_DIR=`dirname ${TEMP_BIN_DIR}`

SKIP_JAVA_SETUP=1
SKIP_SOLDIR_SETUP=1
SKIP_ISCDIR_SETUP=1

setupTDIEnv "${TEMP_BIN_DIR}" || exit 1

SAVE_DIR=`pwd`

cd "$1" > /dev/null || exit 1
TDI_ISC_HOME=`pwd`
cd "${SAVE_DIR}" > /dev/null || exit 1

# if its lwi, set things up for LWI.  If its WAS, use WAS. otherwise its an error.
# For the error, we will use the lwi check so check for WAS first.
if [ -r "${TDI_ISC_HOME}/bin/startServer.sh" ] ; then
	ls -l "${TDI_ISC_HOME}/bin/startServer.sh" > /dev/null || exit 1
	echo "TDI_ISC_RUNTIME=WAS" > "${TDI_BIN_DIR}/tdiISCHome.sh" || exit 1
else
	ls -l "${TDI_ISC_HOME}/bin/lwistart.sh" > /dev/null || exit 1
	echo "TDI_ISC_RUNTIME=LWI" > "${TDI_BIN_DIR}/tdiISCHome.sh" || exit 1
fi

echo "TDI_ISC_HOME=${TDI_ISC_HOME}" >> "${TDI_BIN_DIR}/tdiISCHome.sh" || exit 1

cat "${TDI_BIN_DIR}/tdiISCHome.sh" || exit 1


if [ $? -eq 0 ] ; then 
	echo "TDI_ISC_HOME successfully set."
fi
