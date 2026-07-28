#! /bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2007, 2013
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

######################################################################################
# This Utility is used to stop the Proxy process of a Password Synchronizer.         #
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
	PWS_BIN_DIR=`${CMDFINDER} $0`
	PWS_BIN_DIR=`dirname "${PWS_BIN_DIR}"`

	setupPWSyncEnv "${PWS_BIN_DIR}"
}

#
# Initialize the environment
#
init $@

#
# Execute the Stop Proxy utility
#
"${TDI_JAVA_PROGRAM}" ${TDI_MIXEDMODE_FLAG} -cp "${PWS_CLASSPATH}" com.ibm.di.plugin.proxy.StopProxy "${PWS_CONFIG_FILE}"

# exit with the same code the above utility exit with
exit $?
