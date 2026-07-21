#!/bin/sh

### Restore TDI v7.0 CE user data
### ---------------------------------------

displayUsage()
{
	echo This script makes copies of TDI CE files that may have been altered/created by users.
	echo
	echo "Usage: $0 TDI_Install_Folder"
}

if [ -z "$1" ]
then
	displayUsage
	exit -1
fi

TDI_HOME_DIR="$1"
DEST=$TDI_HOME_DIR/backup_tdi/backup_ce

# Re-store from Backup Directory ###
#----------------------------

echo restoring ce...
if [ -d $DEST/configuration ]; then
   if [ -d $TDI_HOME_DIR/ce/eclipsece/configuration ]; then
      cp -rf $DEST/configuration $TDI_HOME_DIR/ce/eclipsece/configuration 
   fi
   if [ -d $DEST/workspace ]; then
      cp -rf $DEST/workspace $TDI_HOME_DIR/ce/eclipsece/workspace     
   fi
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
                  cp -rf $DEST/workspace $wrkspc
               fi
            fi

            str1=$left

            if [ "$wrkspc" = "$left" ]; then
               x=0
            fi
        done
    fi
fi