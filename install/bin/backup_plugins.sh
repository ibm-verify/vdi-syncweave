#!/bin/sh

### Backup TDI v7.0 plugins user data
### ---------------------------------------

displayUsage()
{
	echo This script makes copies of TDI plugins files that may have been altered by users.
	echo
	echo "Usage: $0 TDI_Install_Folder"
}

if [ -z "$1" ]
then
	displayUsage
	exit -1
fi

TDI_HOME_DIR="$1"

DEST=$TDI_HOME_DIR/backup_tdi

# Create Backup Directory ###
#------------------------------
if [ ! -d "$DEST" ]; then
   mkdir -p "$DEST"
fi

echo Backing up plugins...

if [ -e $TDI_HOME_DIR/pwd_plugins/pam/pwsync.props ]; then
   cp -f $TDI_HOME_DIR/pwd_plugins/pam/pwsync.props     $DEST/pwsync.props.pam
fi   
if [ -e $TDI_HOME_DIR/pwd_plugins/sun/pwsync.props ]; then
   cp -f $TDI_HOME_DIR/pwd_plugins/sun/pwsync.props     $DEST/pwsync.props.sun
fi
if [ -e $TDI_HOME_DIR/pwd_plugins/tds/pwsync.props ]; then   
   cp -f $TDI_HOME_DIR/pwd_plugins/tds/pwsync.props     $DEST/pwsync.props.tds
fi   
if [ -e $TDI_HOME_DIR/pwd_plugins/windows/pwsync.props ]; then   
   cp -f $TDI_HOME_DIR/pwd_plugins/windows/pwsync.props $DEST/pwsync.props.windows
fi 
if [ -e $TDI_HOME_DIR/pwd_plugins/domino/pwsync.props ]; then   
   cp -f $TDI_HOME_DIR/pwd_plugins/domino/pwsync.props $DEST/pwsync.props.domino
fi