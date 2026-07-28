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
# This Utility is used for creating an encrypted password.                           #
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
	if [ $# -eq 0 ]; then
		echo "Usage: `basename $0` <password_to_encrypt>" >&2
		exit 1
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

"${TDI_JAVA_PROGRAM}" ${TDI_MIXEDMODE_FLAG} -cp "${PWS_CLASSPATH}" com.ibm.di.plugin.security.EncodePW $@

exit $?
