#!/bin/sh

### Restore TDI Plugin user data ###
### ---------------------------------------

displayUsage()
{
	echo This script restores TDI Plugin files that may have been altered by users.
	echo
	echo Usage: $0 TDI_Install_Folder version
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

#Taking backup of 71 pwsync.props
#if [ -e $1/pwd_plugins/pam/pwsync.props ]; then
#   cp -f $1/pwd_plugins/pam/pwsync.props $1/pwd_plugins/pam/pwsync.props.v71
#
#if [ -e $1/pwd_plugins/sun/pwsync.props ]; then
#   cp -f $1/pwd_plugins/sun/pwsync.props $1/pwd_plugins/sun/pwsync.props.v71
#
#if [ -e $1/pwd_plugins/tds/pwsync.props ]; then
#   cp -f $1/pwd_plugins/tds/pwsync.props $1/pwd_plugins/tds/pwsync.props.v71
#
#if [ -e $1/pwd_plugins/windows/pwsync.props ]; then
#   cp -f $1/pwd_plugins/windows/pwsync.props $1/pwd_plugins/windows/pwsync.props.v71
#
#if [ -e $1/pwd_plugins/domino/pwsync.props ]; then
#   cp -f $1/pwd_plugins/domino/pwsync.props $1/pwd_plugins/domino/pwsync.props.v71
#   

#migrate pwsync.props
if [ -e $1/backup_tdi/backup_plugins/pam/pwsync.props ]; then
   $1/pwd_plugins/bin/migpwsync.sh -f $1/backup_tdi/backup_plugins/pam/pwsync.props -n $1/pwd_plugins/pam/pwsync.props
fi

if [ -e $1/backup_tdi/backup_plugins/sun/pwsync.props ]; then
   $1/pwd_plugins/bin/migpwsync.sh -f $1/backup_tdi/backup_plugins/sun/pwsync.props -n $1/pwd_plugins/sun/pwsync.props
fi

if [ -e $1/backup_tdi/backup_plugins/tds/pwsync.props ]; then
   $1/pwd_plugins/bin/migpwsync.sh -f $1/backup_tdi/backup_plugins/tds/pwsync.props -n $1/pwd_plugins/tds/pwsync.props
fi

if [ -e $1/backup_tdi/backup_plugins/domino/pwsync.props ]; then
   $1/pwd_plugins/bin/migpwsync.sh -f $1/backup_tdi/backup_plugins/domino/pwsync.props -n $1/pwd_plugins/domino/pwsync.props
fi

exit 0

