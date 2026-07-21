#!/bin/sh

### Restore TDI Server user data ###
### This script also moves current global.properties file to current release
### So this script must be run before the call to miggbl
### ---------------------------------------

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
   . "$TEMP_BIN_DIR/setupCmdLine.sh"
   . "$TEMP_BIN_DIR/defaultSolDir.sh"
}

displayUsage()
{
	echo This script restores TDI Server files that may have been altered by users.
	echo
	echo Usage: $0 TDI_Install_Folder version
}

migrate6xGlobalProps()
{
cp -fp "$TDI_HOME_DIR/etc/global.properties" "$TDI_HOME_DIR/etc/global.properties.v71"
$TDI_HOME_DIR/bin/tdimiggbl -f $TDI_HOME_DIR/etc/global.properties.v$2 -n $TDI_HOME_DIR/etc/global.properties

if [ -e $TDI_HOME_DIR/solution.properties.v$2 ]; then
   $TDI_HOME_DIR/bin/tdimiggbl -f $TDI_HOME_DIR/solution.properties.v$2 -n $TDI_HOME_DIR/solution.properties
fi
}

migrate7xGlobalProps()
{
	$TDI_HOME_DIR/bin/tdimiggbl -f $TDI_HOME_DIR/backup_tdi/global.properties -n $TDI_HOME_DIR/etc/global.properties
	
	if [ -e $TDI_SOLDIR/solution.properties ]; then
		rm -rf $TDI_SOLDIR/solution.properties
	fi
	
	if [ -e $TDI_HOME_DIR/backup_tdi/solution.properties ]; then
		$TDI_HOME_DIR/bin/tdimiggbl -f $TDI_HOME_DIR/backup_tdi/solution.properties -n $TDI_SOLDIR/solution.properties
	fi
}
### -------------------------------------
# depending on the platform.
#
CMDFINDER=which

UNAME_OS=`uname`

if [ "$UNAME_OS" = "OS/390" -o "$UNAME_OS" = "OS400" ] ; then
	CMDFINDER=whence
fi

TEMP_BIN_DIR=`$CMDFINDER $0`
TEMP_BIN_DIR=`dirname $TEMP_BIN_DIR`

setupTDIEnv "$TEMP_BIN_DIR" || exit 1

### -------------------------------------
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

if [ "$2" -eq "60" ]
then
	migrate6xGlobalProps
fi

if [ "$2" -eq "61" ] || [ "$2" -eq "611" ] ;
then
	cp -fp "$1/etc/reconnect.rules.v$2" "$1/etc/reconnect.rules"
	cp -fp "$1/etc/derby.properties.v$2" "$1/etc/derby.properties"
	cp -fp "$1/etc/jlog.properties.v$2" "$1/etc/jlog.properties"
	cp -fp "$1/etc/log4j.properties.v$2" "$1/etc/log4j.properties"
	cp -fp "$1/etc/tdisrvctl-log4j.properties.v$2" "$1/etc/tdisrvctl-log4j.properties"

   #Look in global.properties for systemqueue.on=false...if found, we must set up the MQe Server
   QEXISTS=`grep systemqueue.on=false %1/etc/global.properties.v%2 | wc -l`
   if [ "$QEXISTS" != "1" ] 
   then
      chmod 755 $1/jars/plugins/mqeconfig.sh
      $1/jars/plugins/mqeconfig.sh $1/jars/plugins/mqeconfig.props create server
   fi
   
   if [ "$2" -eq "611" ]
	then
		cp -fp "$1/etc/act-jlog.properties.v$2" "$1/etc/act-jlog.properties"
	fi
   
   migrate6xGlobalProps
fi

if [ "$2" -ne "60" ] || [ "$2" -ne "61" ] || [ "$2" -ne "611" ] ;
then
	cp -fp "$1/backup_tdi/reconnect.rules" "$1/etc/reconnect.rules"
	cp -fp "$1/backup_tdi/derby.properties" "$1/etc/derby.properties"
	cp -fp "$1/backup_tdi/jlog.properties" "$1/etc/jlog.properties"
	cp -fp "$1/backup_tdi/log4j.properties" "$1/etc/log4j.properties"
	cp -fp "$1/backup_tdi/tdisrvctl-log4j.properties" "$1/etc/tdisrvctl-log4j.properties"
	cp -fp "$1/backup_tdi/tdimiggbl-log4j.properties" "$1/etc/tdimiggbl-log4j.properties"
	cp -fp "$1/backup_tdi/updateinstaller-log4j.properties" "$1/etc/updateinstaller-log4j.properties"
	cp -fp "$1/backup_tdi/it_registry.properties" "$1/etc/it_registry.properties"
	cp -fp "$1/backup_tdi/tp.xml" "$1/etc/tp.xml"
	cp -fp "$1/backup_tdi/activemq.xml" "$1/etc/activemq.xml"
	cp -r "$1/backup_tdi/configs" "$1"
	migrate7xGlobalProps
fi

exit 0

