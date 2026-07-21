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
MAINT_DIR_1=$TDI_HOME_DIR/maintenance/BACKUP/SDI-7.2-FP0010/jars/3rdparty/others/ActiveMQ
MAINT_DIR_2=$TDI_HOME_DIR/maintenance/BACKUP/SDI-7.2-FP0010/pwd_plugins
mkdir -p $MAINT_DIR_1
mkdir -p $MAINT_DIR_2

AC_DIR="$TDI_HOME_DIR/jars/3rdparty/others/ActiveMQ"
FILE1="$AC_DIR/activemq-core.jar"
FILE2="$AC_DIR/spring-context.jar"
FILE21="$AC_DIR/spring-core.jar"
FILE22="$AC_DIR/spring-beans.jar"
FILE3="$AC_DIR/geronimo-j2ee-management_1.0_spec-1.0.jar"
FILE4="$AC_DIR/geronimo-jta_1.0.1B_spec-1.0.1.jar"
FILE5="$AC_DIR/xbean-spring-3.6.jar"

PWD_PLUGIN_DIR="$TDI_HOME_DIR/pwd_plugins/jars"
FILE6="$PWD_PLUGIN_DIR/activemq-core.jar"
FILE7="$PWD_PLUGIN_DIR/geronimo-j2ee-management_1.0_spec-1.0.jar"


if [ -d "$AC_DIR" ] && [ -d "$MAINT_DIR_1" ] ; then
  echo Moving old versioned files from $AC_DIR to $MAINT_DIR_1

  if [ -f "$FILE1" ] ; then mv "$FILE1" "$MAINT_DIR_1" 
  fi

  if [ -f "$FILE2" ] ; then mv "$FILE2" "$MAINT_DIR_1" 
  fi
  
  if [ -f "$FILE21" ] ; then mv "$FILE21" "$MAINT_DIR_1" 
  fi
  
  if [ -f "$FILE22" ] ; then mv "$FILE22" "$MAINT_DIR_1" 
  fi
  
  if [ -f "$FILE3" ] ; then mv "$FILE3" "$MAINT_DIR_1" 
  fi
	
  if [ -f "$FILE4" ] ; then mv "$FILE4" "$MAINT_DIR_1" 
  fi
  
  if [ -f "$FILE5" ] ; then mv "$FILE5" "$MAINT_DIR_1" 
  fi

fi

if [ -d "$PWD_PLUGIN_DIR" ] && [ -d "$MAINT_DIR_2" ] ; then
	echo Moving old versioned files from $PWD_PLUGIN_DIR to $MAINT_DIR_2
	if [ -f "$FILE6" ] ; then 
		mv "$FILE6" "$MAINT_DIR_2" 
	fi
	
	if [ -f "$FILE7" ] ; then 
		mv "$FILE7" "$MAINT_DIR_2"
	fi
fi
