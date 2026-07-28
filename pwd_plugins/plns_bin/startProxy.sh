#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2007, 2013
#
#
# IBM_PROLOG_END_TAG

######################################################################################
# This Utility is used to start the Proxy process of a Password Synchronizer.        #
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
	# We should be getting path to a config file as the first parameter
	#
	if [ $# -eq 0 -o ! -r "$1" ]; then
		echo "Usage: `basename $0` <PWSync_config_file_path>" >&2
		exit 1
	else
		PWS_CONFIG_FILE="$1"
	fi

	#
	# Do the setup of the shell
	#
	PWS_BIN_DIR=`${CMDFINDER} $0 2>/dev/null`
	RETURN_CODE=$?
		
	# If /usr/bin/which has failed or $PWS_BIN_DIR is empty, the PATH 
	# environment variable is not set. Thus, we set it and try again.
	if [ $RETURN_CODE -ne 0 -o -z "${PWS_BIN_DIR}" ]; then
		# if local varaible $PATH is empty, we set it to /bin:/usr/bin
		export PATH=${PATH:=/bin:/usr/bin}	
		PWS_BIN_DIR=`${CMDFINDER} $0`
	fi
	
	PWS_BIN_DIR=`dirname "${PWS_BIN_DIR}"`

	setupPWSyncEnv "${PWS_BIN_DIR}"
}

#
# Initialize the environment
#
init $@

#
# This is the place where the Java Proxy will redirect the standard 
# output and the error output to.
#
PROXY_ERR_LOG="${PWS_CONFIG_DIR}/proxy.stdout.log"

#
# Start the Java Proxy Process
#
"${TDI_JAVA_PROGRAM}" ${TDI_MIXEDMODE_FLAG} -cp "${PWS_CLASSPATH}" -DproxyConfigFile="${PWS_CONFIG_FILE}" -Xrs com.ibm.di.plugin.proxy.Proxy $@ &>"${PROXY_ERR_LOG}" &

exit 0
