#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# 1.19, 7/2/09
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2007, 2010
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
#echo Java prg = $TDI_JAVA_PROGRAM

TDI_UPDATE_PRG=com.ibm.di.UpdateInstaller.UpdateInstaller
TDI_UPDATE_UTIL_CP=$TDI_HOME_DIR/jars/3rdparty/IBM/icu4j-51_1.jar:$TDI_HOME_DIR/jars/3rdparty/others/log4j-1.2-api-2.25.4.jar:$TDI_HOME_DIR/jars/3rdparty/others/log4j-api-2.25.4.jar:$TDI_HOME_DIR/jars/3rdparty/others/log4j-core-2.25.4.jar:$TDI_HOME_DIR/jars/common/tdiresource.jar
TDI_UPDATE_CP=$TDI_HOME_DIR/maintenance/UpdateInstaller.jar:$TDI_UPDATE_UTIL_CP

LOG_4J="-Dlog4j.configuration=file:$TDI_HOME_DIR/etc/updateinstaller-log4j.properties"

UPDATE_JRE=0
UPDATE_UI=0

CHECK_JRE_UI=0
if [ "$1" = "-update" -a "$2" != "" ] ; then
   CHECK_JRE_UI=1         
elif [ "$1" = "-rollback" -a "$2" = "" ] ; then
   CHECK_JRE_UI=1         
fi   

if [ "$CHECK_JRE_UI" = "1" ]; then
   #Need to use a copy of the JRE if it's being replaced
   TDI_CHECK4JRE_PRG=com.ibm.di.UpdateInstaller.CheckForJREUpdate
   $TDI_JAVA_PROGRAM $LOG_4J -cp $TDI_UPDATE_CP $TDI_CHECK4JRE_PRG $TDI_HOME_DIR $TDI_BACKUP_DIR JRE.zip $*
   if [ "$?" = "255" ] ; then
      UPDATE_JRE=1
      #copy JRE
      cp -rf $TDI_HOME_DIR/jvm $TDI_HOME_DIR/maintenance/jvm
      TDI_JAVA_PROGRAM=$TDI_HOME_DIR/maintenance/jvm/jre/bin/java
   fi


   #Use a different copy of the jar if it's being replaced  
   TDI_CHECK4UI_PRG=com.ibm.di.UpdateInstaller.CheckForUpdateInstallerUpdate
   TDI_REPLACE_UI_PRG=com.ibm.di.UpdateInstaller.ReplaceUpdateInstallerJar
   $TDI_JAVA_PROGRAM $LOG_4J -cp $TDI_UPDATE_CP $TDI_CHECK4UI_PRG $TDI_HOME_DIR $TDI_BACKUP_DIR $*
   if [ "$?" = "255" ] ; then
      UPDATE_UI=1
      #copy UpdateInstaller
      cp -f $TDI_HOME_DIR/maintenance/UpdateInstaller.jar $TDI_HOME_DIR/maintenance/UpdateInstaller_tmp.jar
      TDI_UPDATE_CP=$TDI_HOME_DIR/maintenance/UpdateInstaller_tmp.jar:$TDI_UPDATE_UTIL_CP
      $TDI_JAVA_PROGRAM $LOG_4J -cp $TDI_UPDATE_CP $TDI_REPLACE_UI_PRG $TDI_HOME_DIR $TDI_BACKUP_DIR $*
      TDI_UPDATE_CP=$TDI_HOME_DIR/maintenance/UpdateInstaller.jar:$TDI_UPDATE_UTIL_CP
   fi
   
fi   

#Fix paths for LUM
TDI_UPDATE_CP=$TDI_UPDATE_CP:$TDI_HOME_DIR/jars/3rdparty/IBM/LUMClient.jar
LUM_LIB=-Djava.library.path=$TDI_HOME_DIR/libs

#Run the update installer
$TDI_JAVA_PROGRAM $LOG_4J $LUM_LIB -cp $TDI_UPDATE_CP $TDI_UPDATE_PRG $TDI_HOME_DIR $TDI_BACKUP_DIR $* 

if [ "$UPDATE_JRE" = "1" ] ; then
  rm -rf $TDI_HOME_DIR/maintenance/jvm
fi  

if [ "$UPDATE_UI" = "1" ] ; then
  rm -f $TDI_HOME_DIR/maintenance/UpdateInstaller_tmp.jar
fi  

