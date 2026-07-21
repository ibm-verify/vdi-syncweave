#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
# Licensed Materials - Property of IBM
#
# Restricted Materials of IBM
#
# (C) COPYRIGHT International Business Machines Corp. 2007, 2013
# All Rights Reserved
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

######################################################################################
# This utility is used to migrate the pwsync.props file                              #
######################################################################################

#
# CMDFINDER holds the command which is used to find other commands
# depending on the platform.
#
CMDFINDER="/usr/bin/which"

setupPWSyncEnv() {
	SKIP_ISCDIR_SETUP="1"
    SKIP_SOLDIR_SETUP="1"
    . "${PWS_BIN_DIR}/setupPwSyncEnv.sh" 
}

init() {
	#
	# Do the setup of the shell
	#
	PWS_BIN_DIR=`${CMDFINDER} $0`
	PWS_BIN_DIR=`dirname "${PWS_BIN_DIR}"`

	setupPWSyncEnv "${PWS_BIN_DIR}"
}

#
# Initialize the environment
#
init $@

#
# We are working inside the pws_home_dir
#
cd "${PWS_HOME_DIR}"

#
# Include the log4j jar to the plugins classpath
#
PWS_CLASSPATH="${PWS_CLASSPATH}:${TDI_BIN_DIR}/../jars/3rdparty/others/log4j-1.2-api-2.25.4.jar:${TDI_BIN_DIR}/../jars/3rdparty/others/log4j-api-2.25.4.jar:${TDI_BIN_DIR}/../jars/3rdparty/others/log4j-core-2.25.4.jar:${TDI_BIN_DIR}/../jars/common/miserver.jar"

#
# Start the Java Proxy Process
#
"${TDI_JAVA_PROGRAM}" \
	${TDI_MIXEDMODE_FLAG} \
	-cp "${PWS_CLASSPATH}" \
	"-Dlog4j.configuration=file://${PWS_HOME_DIR}/etc/migpwsync-log4j.properties" \
	com.ibm.di.migration.plugin.PluginMigrationUtility $@

exit 0
