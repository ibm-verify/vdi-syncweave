#!/bin/sh


# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2009, 2010
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

### BACKUP SCRIPT FOR BACKING UP TDI RELATED FILES AND FOLDERS ###
#-----------------------------------------------------------------

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
   . "$TEMP_BIN_DIR/setupCmdLine.sh"
   . "$TEMP_BIN_DIR/defaultSolDir.sh"
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

setupTDIEnv "$TEMP_BIN_DIR" || exit 1

### Set Variables ###
#--------------------
if [ "$1" = "" ]; then
   DEST=$TDI_HOME_DIR/backup_tdi
elif [ "$1" = "-d" -a "$2" != "" ]; then   
   DEST=$2
else   
   echo Utility to backup TDI files
   echo
   echo Usage: $0 -d [folder_to_create_backup_in]
   echo
   echo The archived info is created in backup folder
   exit
fi

echo dest is $DEST
# Create Backup Directory ###
#------------------------------
if [ ! -d "$DEST" ]; then
   mkdir -p "$DEST"
fi


############################
# Backup server components #
############################
echo Backing up server...
if [ -f $TDI_HOME_DIR/serverapi/testadmin.jks ]; then
   cp -f $TDI_HOME_DIR/serverapi/testadmin.jks        $DEST
fi   
if [ -f $TDI_HOME_DIR/serverapi/testadmin.der ]; then
   cp -f $TDI_HOME_DIR/serverapi/testadmin.der        $DEST
fi   
if [ -f $TDI_HOME_DIR/serverapi/registry.enc ]; then
   cp -f $TDI_HOME_DIR/serverapi/registry.enc         $DEST
fi
if [ -f $TDI_HOME_DIR/serverapi/registry.txt ]; then
   cp -f $TDI_HOME_DIR/serverapi/registry.txt         $DEST
fi
if [ -f $TDI_HOME_DIR/idisrv.sth ]; then
   cp -f $TDI_HOME_DIR/idisrv.sth                     $DEST
fi
if [ -f $TDI_HOME_DIR/testserver.jks ]; then
   cp -f $TDI_HOME_DIR/testserver.jks                 $DEST
fi
if [ -f $TDI_HOME_DIR/testserver.der ]; then
   cp -f $TDI_HOME_DIR/testserver.der                 $DEST
fi   
if [ -f $TDI_HOME_DIR/etc/global.properties ]; then
   cp -f $TDI_HOME_DIR/etc/global.properties          $DEST
fi
if [ -f $TDI_HOME_DIR/etc/reconnect.rules ]; then
   cp -f $TDI_HOME_DIR/etc/reconnect.rules            $DEST
fi
if [ -f $TDI_HOME_DIR/etc/derby.properties ]; then
   cp -f $TDI_HOME_DIR/etc/derby.properties           $DEST
fi
if [ -f $TDI_HOME_DIR/etc/jlog.properties ]; then
   cp -f $TDI_HOME_DIR/etc/jlog.properties            $DEST
fi
if [ -f $TDI_HOME_DIR/etc/log4j.properties ]; then
   cp -f $TDI_HOME_DIR/etc/log4j.properties           $DEST
fi
if [ -f $TDI_HOME_DIR/etc/log4j2.xml ]; then
   cp -f $TDI_HOME_DIR/etc/log4j2.xml                 $DEST
fi
if [ -f $TDI_HOME_DIR/etc/tdisrvctl-log4j.properties ]; then
   cp -f $TDI_HOME_DIR/etc/tdisrvctl-log4j.properties $DEST
fi
if [ -d $TDI_HOME_DIR/configs ]; then
   cp -rf $TDI_HOME_DIR/configs                       $DEST
fi
if [ -f $TDI_SOLDIR/solution.properties ]; then
   cp -f $TDI_SOLDIR/solution.properties            $DEST
fi
if [ -f $TDI_HOME_DIR/etc/tdimiggbl-log4j.properties ]; then
   cp -f $TDI_HOME_DIR/etc/tdimiggbl-log4j.properties $DEST
fi
if [ -f $TDI_HOME_DIR/etc/updateinstaller-log4j.properties ]; then
   cp -f $TDI_HOME_DIR/etc/updateinstaller-log4j.properties   $DEST
fi
if [ -f $TDI_HOME_DIR/etc/it_registry.properties ]; then
   cp -f $TDI_HOME_DIR/etc/it_registry.properties   $DEST
fi
if [ -f $TDI_HOME_DIR/etc/tp.xml ]; 				then
   cp -f $TDI_HOME_DIR/etc/tp.xml            		$DEST
fi
if [ -f $TDI_HOME_DIR/etc/activemq.xml ]; 				then
   cp -f $TDI_HOME_DIR/etc/activemq.xml            		$DEST
fi
if [ -d $TDI_HOME_DIR/osgi ]; then
   cp -rf $TDI_HOME_DIR/osgi                       $DEST
fi
if [ -d $TDI_HOME_DIR/SCIM ]; then
   cp -rf $TDI_HOME_DIR/SCIM                       $DEST
fi
if [ -d $TDI_HOME_DIR/LDAPSync ]; then
   cp -rf $TDI_HOME_DIR/LDAPSync                       $DEST
fi

############################
# Backup CE components     #
############################
echo Backing up ce...
if [ ! -d "$DEST/backup_ce" ]; then
   mkdir -p "$DEST/backup_ce"
fi

if [ -d $TDI_HOME_DIR/ce ]; then
   if [ -d $TDI_HOME_DIR/ce/eclipsece/configuration ]; then
      cp -rf $TDI_HOME_DIR/ce/eclipsece/configuration $DEST/backup_ce
   fi
   if [ -d $TDI_HOME_DIR/ce/eclipsece/workspace ]; then
      cp -rf $TDI_HOME_DIR/ce/eclipsece/workspace     $DEST/backup_ce
   fi

   #backup workspaces
   if [ -f $TDI_HOME_DIR/ce/eclipsece/configuration/.settings/org.eclipse.ui.ide.prefs ]; then
      str1=`egrep "^RECENT_WORKSPACES=" $TDI_HOME_DIR/ce/eclipsece/configuration/.settings/org.eclipse.ui.ide.prefs`
      if [ "$str1" != "" ]; then
         str1=`echo $str1 | cut -d= -f2`
         str1=`echo $str1 | sed 's/\\\\n/=/'`

         x=1
         while [ $x -gt 0 ]
         do
            wrkspc=`echo $str1 | cut -d= -f1`
            left=`echo $str1 | cut -d= -f2`

            #Copy the workspace if it's in the install directory
            str2=`echo $wrkspc | egrep $TDI_HOME_DIR`
            if [ "$str2" != "" ]; then
               if [ -d $wrkspc ]; then
                  cp -rf $wrkspc $DEST/backup_ce
               fi
            fi

            str1=$left

            if [ "$wrkspc" = "$left" ]; then
               x=0
            fi
         done
      fi
   fi
fi


############################
# Backup AMC components    #
############################
echo Backing up AMC...
if [ -d $TDI_HOME_DIR/bin/amc ]; then   
   $TDI_HOME_DIR/bin/amc/backupam.sh -d $DEST
   $TDI_HOME_DIR/bin/amc/backupamc.sh -d $DEST

   AMC_PROPS_FILE=$TDI_HOME_DIR/lwi/runtime/isc/eclipse/plugins/AMC_7.0.0/amc.properties
   if [ ! -f $AMC_PROPS_FILE ]; then
      AMC_PROPS_FILE=$TDI_HOME_DIR/lwi/runtime/isc/eclipse/plugins/AMC_7.1.0/amc.properties
   fi
   if [ ! -f $AMC_PROPS_FILE ]; then
      AMC_PROPS_FILE=$TDI_HOME_DIR/lwi/runtime/isc/eclipse/plugins/AMC_7.1.1.0/amc.properties
   fi
   if [ ! -f $AMC_PROPS_FILE ]; then
      AMC_PROPS_FILE=$TDI_HOME_DIR/lwi/runtime/isc/eclipse/plugins/AMC_7.2.0.0/amc.properties
   fi
   if [ -f $AMC_PROPS_FILE ]; then
      $TDI_HOME_DIR/bin/amc/backupamcdb.sh -d $DEST -p $AMC_PROPS_FILE
	else
	   echo amc.properties file not found
   fi
fi


############################
# Backup MQePWStore files  #
############################
echo Backing up MQePWStore...
MQE_PROPS=$TDI_HOME_DIR/jars/plugins/mqeconfig.props
if [ -f "$MQE_PROPS" ]; then
   MQE_DIR=`egrep "^serverRootFolder=" $MQE_PROPS`
   MQE_DIR=`echo $MQE_DIR | cut -d= -f2`
   if [ -d $TDI_HOME_DIR/$MQE_DIR ]; then
		if [ ! -d $DEST/backup_systemqueue ]; then
			mkdir -p $DEST/backup_systemqueue
		fi
		cp -rf $TDI_HOME_DIR/$MQE_DIR $DEST/backup_systemqueue
   fi
fi


############################
# Backup TDISysStore files #
############################
echo Backing up TDISysStore...
TDI_STORE=`egrep "^com.ibm.di.store.database=" $TDI_HOME_DIR/etc/global.properties`
TDI_STORE=`echo $TDI_STORE | sed 's/.*\/\//\//'`
TDI_STORE=`echo $TDI_STORE | cut -d\; -f1`
if [ -d $TDI_STORE ]; then
	if [ ! -d $DEST/backup_systemstore ]; then
		mkdir -p $DEST/backup_systemstore
	fi
	cp -rf $TDI_STORE $DEST/backup_systemstore
fi   


############################
# Backup Plugins files     #
############################
echo Backing up plugins...
if [ -f $TDI_HOME_DIR/pwd_plugins/pam/pwsync.props ]; then
	if [ ! -d $DEST/backup_plugins/pam ]; then
		mkdir -p $DEST/backup_plugins/pam
	fi
   cp -f $TDI_HOME_DIR/pwd_plugins/pam/pwsync.props     $DEST/backup_plugins/pam
fi   
if [ -f $TDI_HOME_DIR/pwd_plugins/sun/pwsync.props ]; then
	if [ ! -d $DEST/backup_plugins/sun ]; then
		mkdir -p $DEST/backup_plugins/sun
	fi
   cp -f $TDI_HOME_DIR/pwd_plugins/sun/pwsync.props     $DEST/backup_plugins/sun
fi
if [ -f $TDI_HOME_DIR/pwd_plugins/tds/pwsync.props ]; then   
	if [ ! -d $DEST/backup_plugins/tds ]; then
		mkdir -p $DEST/backup_plugins/tds
	fi
   cp -f $TDI_HOME_DIR/pwd_plugins/tds/pwsync.props     $DEST/backup_plugins/tds
fi   
if [ -f $TDI_HOME_DIR/pwd_plugins/domino/pwsync.props ]; then   
    if [ ! -d $DEST/backup_plugins/domino ]; then
		mkdir -p $DEST/backup_plugins/domino
	fi
   cp -f $TDI_HOME_DIR/pwd_plugins/domino/pwsync.props $DEST/backup_plugins/domino
fi
