#!/bin/sh

#
#
# Copyright contributors to the SyncWeave project
#

### INSTALL VERIFY SCRIPT TDI RELATED COMPONENTS ###
#----------------------------------------------------

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
   . "$TEMP_BIN_DIR/setupCmdLine.sh"
}

# depending on the platform.
#
CMDFINDER=which

UNAME_OS=`uname`

if [ "$UNAME_OS" = "OS/390" -o "$UNAME_OS" = "OS400" ] ; then
	CMDFINDER=whence
fi

TEMP_BIN_DIR=`$CMDFINDER $0`
TEMP_BIN_DIR=`dirname $TEMP_BIN_DIR`

SKIP_ISCDIR_SETUP=1
SKIP_SOLDIR_SETUP=1

setupTDIEnv "$TEMP_BIN_DIR"

##Verifying Server files and jars###

SERVER=`cat $TDI_HOME_DIR/.registry | grep "<SERVER>"`

if [ "$SERVER" = "<SERVER>" ]; then

	if [ ! -d $TDI_HOME_DIR/xsl ]; then
	echo 1
	exit 1
	fi
		if [ ! -d $TDI_HOME_DIR/jars/3rdparty ]; then
	echo 1
	exit 1
	fi
		if [ ! -d $TDI_HOME_DIR/jars/3rdparty/IBM  ]; then
	echo 1
	exit 1
	fi
		if [ ! -d $TDI_HOME_DIR/jars/3rdparty/others  ]; then
	echo 1
	exit 1
	fi
		if [ ! -d $TDI_HOME_DIR/jars/connectors ]; then
	echo 1
	exit 1
	fi
		if [ ! -d $TDI_HOME_DIR/jars/functions ]; then
	echo 1
	exit 1
	fi
		if [ ! -d $TDI_HOME_DIR/jars/parsers ]; then
	echo 1
	exit 1
	fi
	
	if [ ! -d $TDI_HOME_DIR/osgi ]; then
		echo 1
		exit 1
	fi
	if [ ! -d $TDI_HOME_DIR/osgi/plugins ]; then
		echo 1
		exit 1
	fi
	if [ ! -d $TDI_HOME_DIR/SCIM ]; then
		echo 1
		exit 1
	fi
	if [ ! -d $TDI_HOME_DIR/LDAPSync ]; then
		echo 1
		exit 1
	fi
	
	
		if [ ! -f $TDI_HOME_DIR/ibmdisrv ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/serverapi/cryptoutils.sh ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/serverapi/registry.enc ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/serverapi/registry.txt ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/idisrv.sth ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/IDILoader.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/testserver.jks ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/testserver.der ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/etc/global.properties ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/tdisrvctl ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/tdimiggbl ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/jars/common/diserverapi.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/jars/common/miserver.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/jars/common/mmconfig.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/jars/common/miconfig.jar ]; then
	echo 1
	exit 1
	fi

		if [ ! -f $TDI_HOME_DIR/jars/common/diserverapirmi.jar ]; then
	echo 1
	exit 1
	fi

		if [ ! -f $TDI_HOME_DIR/jars/common/tdiresource.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/jars/common/miggbl.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/jars/common/cli.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/tools/CSMigration/migratetoderby.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/maintenance/UpdateInstaller.jar ]; then
	echo 1
	exit 1
	fi
	
	if [ ! -f $TDI_HOME_DIR/SCIM/SCIM.xml ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/LDAPSync/LDAPSync.xml ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.api.bind.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.api.connection.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.api.impl.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.api.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.api.rest.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.bundle.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.component.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.config.bind.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.config.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.connector.taddm.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.http.jetty.listener.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.jaxrs.impl.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.jaxrs.jackson.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.jaxrs.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.jaxrs.storage.atom.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.log.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.log.slf4j.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.log.slf4j-tdi.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.schema.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.server.entry.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.server.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.systemqueue.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.ui.easyetl.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.ui.webui.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.util.jar ]; then
		echo 1
		exit 1
	fi
	if [ ! -f $TDI_HOME_DIR/osgi/plugins/com.ibm.di.web.common.jar ]; then
		echo 1
		exit 1
	fi
	
fi
###Verify CE files and jars ###

CE=`cat $TDI_HOME_DIR/.registry | grep "<CE>"`

if [ "$CE" = "<CE>" ]; then
	if [ ! -f $TDI_HOME_DIR/ibmditk ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/ce/eclipsece/miadmin ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/ce/eclipsece/miadmin.ini ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/ce/eclipsece/plugins/com.ibm.tdi.rcp_11.0.0.1.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/ce/eclipsece/plugins/com.ibm.tdi.loader_11.0.0.1.jar ]; then
	echo 1
	exit 1
	fi
fi
###Verify AMC files and jars ###
AMC=`cat $TDI_HOME_DIR/.registry | grep "<AMC>"`

if [ "$AMC" = "<AMC>" ]; then

	if [ ! -f $TDI_HOME_DIR/bin/amc/start_tdiamc.sh ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/startNetworkServer.sh ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/startAM.sh ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/install.sh ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/amc/tdiamc.war ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/ActionManager/jars/action_manager.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/ActionManager/jars/tdiresource.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/ActionManager/jars/db2jcc.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/ActionManager/jars/derby.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/ActionManager/jars/derbyclient.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/ActionManager/jars/derbynet.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/ActionManager/jars/derbytools.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/bin/amc/ActionManager/am_config.properties ]; then
	echo 1
	exit 1
	fi
fi
###Checking sub components####
###ceupdate###
CEUPDATE=`cat $TDI_HOME_DIR/.registry | grep "<CE UPDATE>"`

if [ "$CEUPDATE" = "<CE UPDATE>" ]; then

	if [ ! -f $TDI_HOME_DIR/ce/update_site/site.xml ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/ce/update_site/features/com.ibm.tdi.feature_11.0.0.1.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -d $TDI_HOME_DIR/ce/update_site/plugins ]; then
	echo 1
	exit 1
	fi
fi
###Plugins###
PLUGIN=`cat $TDI_HOME_DIR/.registry | grep "<PLUGINS>"`

if [ "$PLUGIN" = "<PLUGINS>" ]; then

		if [ ! -f $TDI_HOME_DIR/pwd_plugins/jars/proxy.jar ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/pwd_plugins/bin/encryptPasswd.sh ]; then
	echo 1
	exit 1
	fi
		if [ ! -f $TDI_HOME_DIR/pwd_plugins/bin/startProxy.sh ]; then
	echo 1
	exit 1
	fi
fi
##Here all are fine.##
echo 0
exit 0
