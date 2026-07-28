:: IBM_PROLOG_BEGIN_TAG
::
:: %I%, %G%
::
:: Licensed Materials - Property of IBM
::
:: Restricted Materials of IBM
::
:: (C) COPYRIGHT International Business Machines Corp. 2007, 2013
:: All Rights Reserved
::
::
:: IBM_PROLOG_END_TAG

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: This utility is used to migrate the pwsync.props file
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

@echo off
setlocal

set TEMP_BIN_DIR=%~d0%~p0

set PWS_JARS_DIR=
set PWS_CLASSPATH=
set SKIP_ISCDIR_SETUP=1
set SKIP_SOLDIR_SETUP=1
call "%TEMP_BIN_DIR%setupPwSyncEnv.bat" "%TEMP_BIN_DIR%"

:: We are working inside the pws_home_dir
call "%TDI_BIN_DIR%\ibmdicwd" "%PWS_HOME_DIR%"

:: Include the log4j jar to the plugins classpath
set PWS_CLASSPATH=%PWS_CLASSPATH%;%TDI_BIN_DIR%\..\jars\3rdparty\others\log4j-1.2-api-2.25.4.jar;%TDI_BIN_DIR%\..\jars\3rdparty\others\log4j-api-2.25.4.jar;%TDI_BIN_DIR%\..\jars\3rdparty\others\log4j-core-2.25.4.jar;%TDI_BIN_DIR%\..\jars\common\miserver.jar

"%TDI_JAVA_PROGRAM%" -cp "%PWS_CLASSPATH%" "-Dlog4j.configuration=file:///%PWS_HOME_DIR%\etc\migpwsync-log4j.properties" com.ibm.di.migration.plugin.PluginMigrationUtility %*

endlocal
