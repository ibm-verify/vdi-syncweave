#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2007, 2010
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

#################################################################
# Internal command to setup a set of environment variables used
# by other TDI commands
#################################################################

#
# CMDFINDER holds the command which is used to find other commands
# depending on the platform.
#
CMDFINDER="/usr/bin/which"

UNAME_OS=`uname`

if [ "$UNAME_OS" = "OS/390" -o "$UNAME_OS" = "OS400" ] ; then
	CMDFINDER="whence"
fi

# use the variable passed in if possible for the bin dir
if [ ! -d "$1" ] ; then
	TDI_BIN_DIR=`${CMDFINDER} $0`
	TDI_BIN_DIR=`dirname ${TDI_BIN_DIR}`
else
	TDI_BIN_DIR="$1"
fi

TDI_HOME_DIR="${TDI_BIN_DIR}/../"

START_DIR=`pwd`

cd "${TDI_HOME_DIR}" > /dev/null || exit 1

TDI_HOME_DIR=`pwd`

cd "${START_DIR}" > /dev/null || exit 1

TDI_ETC_DIR="${TDI_HOME_DIR}/etc"
TDI_LIB_DIR="${TDI_HOME_DIR}/libs"

if [ "${SKIP_ISCDIR_SETUP}" = "" ] ; then

	#
	# Only set TDI_ISC_HOME if it hasn't been set already in caller's shell
	#
	if [ -z "${TDI_ISCDIR}" ]; then
		. "${TDI_BIN_DIR}/tdiISCHome.sh"
		export TDI_ISC_HOME
		export TDI_ISC_RUNTIME
	fi

	if [ ! -d "${TDI_ISC_HOME}" ] ; then
		echo "Incorrect TDI_ISC_HOME (${TDI_ISC_HOME})" >&2
		exit 1
	fi

fi

if [ "${SKIP_SOLDIR_SETUP}" = "" ] ; then

	#
	# Only set TDI_SOLDIR if it hasn't been set already in caller's shell
	#
	if [ -z "${TDI_SOLDIR}" ]; then
		. "${TDI_BIN_DIR}/defaultSolDir.sh"
		export TDI_SOLDIR
	fi

	if [ ! -d "${TDI_SOLDIR}" ] ; then
		echo "Incorrect TDI_SOLDIR (${TDI_SOLDIR})" >&2
		break 1
	fi

fi

if [ "${SKIP_JAVA_SETUP}" = "" ] ; then
	
	#
	# Do java initialization
	#
	if [ -x "${TDI_BIN_DIR}/javaHome.sh" ] ; then
		. "${TDI_BIN_DIR}/javaHome.sh"
		export TDI_JAVA_HOME
	fi

	if [ ! -d "${TDI_JAVA_HOME}" ] ; then
		echo "Incorrect TDI_JAVA_HOME (${TDI_JAVA_HOME})" >&2
		exit 1
	fi

	JAVA_EXECUTABLE="java"
	if [ "${UNAME_OS}" = "Linux" -o "${UNAME_OS}" = "AIX" -o "${UNAME_OS}" = "SunOS" -o "${UNAME_OS}" = "HP-UX" ] ; then
		TDI_JAVA_PROGRAM="${TDI_JAVA_HOME}/jre/bin/${JAVA_EXECUTABLE}"
		TDI_JAVA_BIN_DIR="${TDI_JAVA_HOME}/jre/bin"
	else
		TDI_JAVA_PROGRAM="${TDI_JAVA_HOME}/bin/${JAVA_EXECUTABLE}"
		TDI_JAVA_BIN_DIR="${TDI_JAVA_HOME}/bin"
	fi

	if [ ! -x "${TDI_JAVA_PROGRAM}" ] ; then
		echo "Cannot run ${TDI_JAVA_PROGRAM}" >&2
		exit 1
	fi

	#javaw doesn't exist on Solaris and HP...we need to take that into account
	JAVAW_EXECUTABLE="javaw"
	if [ ! -x "${TDI_JAVA_BIN_DIR}/${JAVAW_EXECUTABLE}" ] ; then
		JAVAW_EXECUTABLE="${JAVA_EXECUTABLE}"
	fi	
	TDI_JAVAW_PROGRAM="${TDI_JAVA_BIN_DIR}/${JAVAW_EXECUTABLE}"

	#Set bitmode flag for use on opteron
	TDI_MIXEDMODE_FLAG=""
	if [ "${UNAME_OS}" = "SunOS" ] ; then
		PROC_TYPE=`isainfo | cut -f 1 -d " "` #Our opteron is giving us "amd64 i386" from isainfo
		if [ "${PROC_TYPE}" = "amd64" ] ; then
			TDI_MIXEDMODE_FLAG="-d64"
		fi
	fi
fi


