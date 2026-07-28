#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2009, 2010
#
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

usage()
{
echo "Usage Error: tdiSetBackupDir TDI_BACKUP_DIR or default" >&2
        exit 1
}

if [ $# -ne 1 ]; then
   usage
fi

if [ $1 != "default" -a ! -d "$1" ]; then
   usage
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

if [ "$1" = "default" ] ; then
   TDI_BACKUP_DIR=$TDI_HOME_DIR/maintenance/BACKUP
else
   TDI_BACKUP_DIR=$1
fi

echo "TDI_BACKUP_DIR=$TDI_BACKUP_DIR" > "$TDI_BIN_DIR/backupDir.sh" || exit 1

cat "$TDI_BIN_DIR/backupDir.sh" || exit 1


if [ $? -eq 0 ] ; then
	echo "TDI_BACKUP_DIR successfully set."
fi
