#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
#
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
   . "$TEMP_BIN_DIR/backupDir.sh"
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

#echo Backup dir = $TDI_BACKUP_DIR
#echo Home dir = $TDI_HOME_DIR

# Find the latest backup directory
BACKUP_DIR=`find "$TDI_BACKUP_DIR" -type d -name "SDI-7.2-FP*" | sort -r | head -n 1`
if [ -z "$BACKUP_DIR" ]; then
  echo "No backup directory found in $TDI_BACKUP_DIR"
  exit 1
fi

# Check if log4j-1.2.16.jar exists in $TDI_HOME_DIR
JARDIR=jars/3rdparty/others
JARFILE=log4j-1.2.16.jar
FROM="$TDI_HOME_DIR/$JARDIR/$JARFILE"
if [ ! -f "$FROM" ]; then
  echo "The old log4j file $FROM was not found."
  exit 1
fi

# Backup or remove the old log4j-1.2.16.jar file
TO="$BACKUP_DIR/$JARDIR"
if [ ! -d "$TO" ]; then
  mkdir -p "$TO"
fi

mv -v "$FROM" "$TO" && echo "Successfully backed up $JARFILE" || rm -v "$FROM" && echo "Successfully removed $JARFILE"

# Execute the remaining script

APPLY_NEW="$TDI_BIN_DIR/applyUpdates.new.sh"
APPLY_CURRENT="$TDI_BIN_DIR/applyUpdates.sh"
BACKUP_BIN="$BACKUP_DIR/bin"
APPLY_OLD="$TDI_BIN_DIR/applyUpdates.old.sh"

if [ -f "$APPLY_NEW" ]; then
  if [ -d "$BACKUP_BIN" ]; then
    mv -v "$APPLY_CURRENT" "$BACKUP_BIN"
  else
    mv -v "$APPLY_CURRENT" "$APPLY_OLD"
  fi
  mv -v "$APPLY_NEW" "$APPLY_CURRENT" && echo "Successfully created new $APPLY_CURRENT"
  chmod +x "$APPLY_CURRENT"
fi

