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
# Command to set the JRE used by all other TDI commands
#################################################################

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
. "$TEMP_BIN_DIR/setupCmdLine.sh"
}

if [ $# -ne 1 -o ! -d "$1" ] ; then
	echo "Usage Error: tdiSetJavaHome TDI_JAVA_HOMEdir" >&2
	exit 1
fi


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

SKIP_JAVA_SETUP=1
SKIP_SOLDIR_SETUP=1
SKIP_ISCDIR_SETUP=1
setupTDIEnv "$TEMP_BIN_DIR" || exit 1

SAVE_DIR=`pwd`

cd "$1" > /dev/null || exit 1
TDI_JAVA_HOME_DIR=`pwd`
cd "$SAVE_DIR" > /dev/null || exit 1

if [ $UNAME_OS = "Linux" -o $UNAME_OS = "AIX" -o $UNAME_OS = "SunOS" -o $UNAME_OS = "HP-UX" ] ; then
	ls -l "$TDI_JAVA_HOME_DIR/jre/bin/java" > /dev/null || exit 1
else
	ls -l "$TDI_JAVA_HOME_DIR/bin/java" > /dev/null || exit 1
fi

echo "TDI_JAVA_HOME=$TDI_JAVA_HOME_DIR" > "$TDI_BIN_DIR/javaHome.sh" || exit 1

cat "$TDI_BIN_DIR/javaHome.sh" || exit 1


if [ $? -eq 0 ] ; then 
	echo "TDI_JAVA_HOME successfully set."
fi
