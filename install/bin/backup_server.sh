#!/bin/sh

### Backup TDI v6.X.X and v7.0 Server user data
### ---------------------------------------

displayUsage()
{
	echo This script makes copies of TDI Server files that may have been altered by users.
	echo
	echo "Usage: $0 TDI_Install_Folder 60|61|611|70"
}

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

#Create etc if migrating from v60
if [ "$2" -eq "60" ]
then
mkdir $1/etc
cp -fp "$1/global.properties" "$1/etc/global.properties.v$2"
else
cp -fp "$1/etc/global.properties" "$1/etc/global.properties.v$2"
fi

#backup global.properties

#backup other important files
cp -fp "$1/serverapi/testadmin.jks" "$1/serverapi/testadmin.jks.v$2"
cp -fp "$1/serverapi/testadmin.der" "$1/serverapi/testadmin.der.v$2"
cp -fp "$1/serverapi/registry.enc" "$1/serverapi/registry.enc.v$2"
cp -fp "$1/serverapi/registry.txt" "$1/serverapi/registry.txt.v$2"
cp -fp "$1/idisrv.sth" "$1/idisrv.sth.v$2"
cp -fp "$1/testserver.jks" "$1/testserver.jks.v$2"
cp -fp "$1/testserver.der" "$1/testserver.der.v$2"

if [ "$2" -ne "60" ]
then
#This must be v6.1.X or 7.0
cp -fp "$1/etc/reconnect.rules" "$1/etc/reconnect.rules.v$2"
cp -fp "$1/etc/derby.properties" "$1/etc/derby.properties.v$2"
cp -fp "$1/etc/jlog.properties" "$1/etc/jlog.properties.v$2"
cp -fp "$1/etc/log4j.properties" "$1/etc/log4j.properties.v$2"
cp -fp "$1/etc/tdisrvctl-log4j.properties" "$1/etc/tdisrvctl-log4j.properties.v$2"
fi

if [ "$2" -eq "611" ]
then
#This is 6.1.1
cp -fp "$1/etc/act-jlog.properties" "$1/etc/act-jlog.properties.v$2"
fi

if [ "$2" -eq "70" ]
then

TDI_HOME_DIR="$1"

if [ -e $TDI_HOME_DIR/solution.properties ]; then
   cp -f "$TDI_HOME_DIR/solution.properties" "$TDI_HOME_DIR/solution.properties.v$2"
fi

fi

exit 0
