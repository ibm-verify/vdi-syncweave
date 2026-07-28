#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2007, 2013
# All Rights Reserved
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

######################################################################################
# This script is used to setup the common environment variables used by the plugins. #
# This script uses the TDI_BIN_DIR/setupCmdLine.sh script for setting up common      #
# variables.                                                                         #
# This script is not expected to run on i5/OS or z/OS                                #
#                                                                                    #
# This script checks for the existence of the following environment variables:       #
#   PWS_BIN_DIR - if not already set then will check if it is passed as the first    #
#           argument or will try to resolve it.                                      #
#   PWS_JARS_DIR - if not provided then the script will use the jars folder,         #
#           sibling of the PWS_BIN_DIR directory.                                    #
#   PWS_CLASSPATH - if not provided then the script will construct the classpath     #
#           from all the jar files in the PWS_JARS_DIR directory.                    #
#   PWS_CONFIG_FILE - if provided the will resolve the absolute path to the file.    #
#           The file will be resolved based on the pwd.								 #
#                                                                                    #
# This script exports the following variables:                                       #
#   PWS_BIN_DIR - the directory containing plugins' executables                      #
#   PWS_HOME_DIR - the home directory of the plugins                                 #
#   TDI_BIN_DIR - the directory containing TDI's executables                         #
#   PWS_JARS_DIR - the directory containing plugins' jar files                       #
#   PWS_CLASSPATH - list of the jar files separated by column                        #
#   PWS_CONFIG_FILE - the resolved absolut path to the provided config file          #
#   PWS_CONFIG_DIR - the directory known as the authentication folder                #
######################################################################################

CMDFINDER="/usr/bin/which"
FIND="/usr/bin/find"

#
# Only set PWS_BIN_DIR if it hasn't been set already in caller's shell
#
if [ "${PWS_BIN_DIR}" = "" ]; then

	# use the first variable passed in as the bin dir
	if [ ! -d "$1" ] ; then
		PWS_BIN_DIR=`${CMDFINDER} $0`
		PWS_BIN_DIR=`dirname "${PWS_BIN_DIR}"`
	else
		PWS_BIN_DIR="$1"
	fi

	# The PWSync bin folder
	export PWS_BIN_DIR
fi

# The PWSync home folder
PWS_HOME_DIR="${PWS_BIN_DIR}/.."
export PWS_HOME_DIR

# The bin folder of SDI
TDI_BIN_DIR="${PWS_BIN_DIR}/../../bin"
export TDI_BIN_DIR

setupCmdLine() {
	# Run the standard setup script
	. "${TDI_BIN_DIR}/setupCmdLine.sh"
}

setupCmdLine "${TDI_BIN_DIR}"

#
# Only set PWS_JARS_DIR if it hasn't been set already in caller's shell
#
if [ "${PWS_JARS_DIR}" = "" ]; then
	# The folder where all the jar files are held
	PWS_JARS_DIR="${PWS_BIN_DIR}/../jars"
	export PWS_JARS_DIR
fi

#
# Only set PWS_CLASSPATH if it hasn't been set already in caller's shell
#
if [ "${PWS_CLASSPATH}" = "" ]; then

	PWS_CLASSPATH="${PWS_JARS_DIR}"
	# Will list all the files in the provided sub-tree and will construct a
        # list of files separated by the colomn characteR
	for line in `"${FIND}" "${PWS_JARS_DIR}" -name "*.jar"`
	do
		if [ -f "${prev}${line}" ] ; then
			PWS_CLASSPATH="${PWS_CLASSPATH}:${prev}${line}"
			prev=""
		else
			prev="${prev}${line} "
		fi
	done
	export PWS_CLASSPATH
fi

# We need to hava a class path!!! If not then provide a dummy value for the 
# PWS_CLASSPATH variable to skip this error.
if [ "${PWS_CLASSPATH}" = "" ]; then
	echo "Incorrect PWS_CLASSPATH (${PWS_CLASSPATH})" >&2
	exit 1
fi

# Make sure we resolve the absolute path to the PWS_CONFIG_FILE. Will also
# export a PWS_CONFIG_DIR which is the absolute path to the folder where
# the config file is placed.
if [ -r "${PWS_CONFIG_FILE}" ]; then
	SLASH=`echo "${PWS_CONFIG_FILE}" | sed -e "s/\/.*//"`
	if [ "$SLASH" != "" ]; then
		PWS_CONFIG_FILE="`pwd`/${PWS_CONFIG_FILE}"
		export PWS_CONFIG_FILE
		PWS_CONFIG_DIR=`dirname "${PWS_CONFIG_FILE}"`
		export PWS_CONFIG_DIR
	fi
fi
