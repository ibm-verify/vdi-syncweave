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
:: This Utility is used to start the Proxy process of a Password Synchronizer.
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

@echo off
setlocal

set TEMP_BIN_DIR=%~d0%~p0
set BASENAME=%~0

set PWS_CONFIG_FILE=%~1
set PWS_JARS_DIR=
set PWS_CLASSPATH=
set SKIP_ISCDIR_SETUP=1
set SKIP_SOLDIR_SETUP=1
call "%TEMP_BIN_DIR%setupPwSyncEnv.bat" "%TEMP_BIN_DIR%"

if exist "%PWS_CONFIG_FILE%" goto :params_ok

	echo "Usage: %BASENAME% <PWSync_config_file_path>" >&2
	set RC=1
	exit /b 1
	
:params_ok

"%TDI_JAVA_PROGRAM%" -cp "%PWS_CLASSPATH%" com.ibm.di.plugin.proxy.StopProxy "%PWS_CONFIG_FILE%"

endlocal
