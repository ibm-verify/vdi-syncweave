#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2009
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

####################################################################
# Start up script for SyncWeave's Unit Tests #
####################################################################

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
. "${TEMP_BIN_DIR}/setupCmdLine.sh"
}

#
# CMDFINDER holds the command which is used to find other commands
# depending on the platform.
#
CMDFINDER="which"

UNAME_OS=`uname`

if [ "$UNAME_OS" = "OS/390" -o "$UNAME_OS" = "OS400" ] ; then
	CMDFINDER="whence"
fi

TEMP_BIN_DIR=`${CMDFINDER} $0`
TEMP_BIN_DIR=`dirname ${TEMP_BIN_DIR}`
UNIT_TESTS_HOME="${TEMP_BIN_DIR}/.."
TEMP_BIN_DIR="${TEMP_BIN_DIR}/../../bin"

SKIP_ISCDIR_SETUP=1
SKIP_SOLDIR_SETUP=1
setupTDIEnv "${TEMP_BIN_DIR}"

# CD into unit_tests directory
cd "${UNIT_TESTS_HOME}"

"${TDI_JAVA_PROGRAM}" $TDI_MIXEDMODE_FLAG "-Dcom.ibm.di.installdir=${TDI_BIN_DIR}/.." "-Dcom.ibm.di.test.runner=com.ibm.di.test.runner.PerfFrameworkRunner" -jar "${UNIT_TESTS_HOME}/boot.jar" "$@" -ctx=ext

