#!/bin/sh
#set +x

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2006, 2010
# All Rights Reserved
#
# US Government Users Restricted Rights - Use, duplication or
#
# IBM_PROLOG_END_TAG

#################################################################
# MIGRATE SCRIPT FOR Cloudscape v5 to v10
#################################################################

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
. "$TEMP_BIN_DIR/setupCmdLine.sh"
}

displayUsage()
{
	echo This script calls the CS migration utility
	echo
	echo Usage: $0 [Path_Of_CloudscapeV5_folder] [Path_Of_New_CloudscapeV10_Folder_To_Create]
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
TEMP_BIN_DIR=`dirname "$TEMP_BIN_DIR/../../bin/setupCmdLine.sh"`

SKIP_ISCDIR_SETUP=1
setupTDIEnv "$TEMP_BIN_DIR"

if [ -z "$1" ]
then
	displayUsage
exit -1
fi

if [ -z "$2" ]
then
	displayUsage
exit -1
fi

### Set Variables
### -------------
OLD_DB_PATH="$1"
NEW_DB_PATH="$2"
CP="$TDI_HOME_DIR/tools/CSMigration/migratetoderby.jar:$TDI_HOME_DIR/jars/3rdparty/IBM/derby.jar"

### Call Migrate Utility
### --------------------
"$TDI_JAVA_PROGRAM" -cp $CP $TDI_MIXEDMODE_FLAG -Ddb2j.migrate.ddlOnly=false -Ddb2j.migrate.appendLog=false -Ddb2j.migrate.verbose=true "-Ddb2j.migrate.newDBURL=jdbc:derby:$NEW_DB_PATH" -Ddb2j.migrate.migrateLog=migrate.log -Ddb2j.migrate.debugLog=debug.log com.ibm.db2j.tools.MigrateFrom51 "jdbc:db2j:$OLD_DB_PATH"

### Restore completed
### -----------------
exit 0

