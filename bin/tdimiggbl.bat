:: IBM_PROLOG_BEGIN_TAG
::
:: %I%, %G%
::
:: Licensed Materials - Property of IBM
::
:: Restricted Materials of IBM
::
:: (C) COPYRIGHT International Business Machines Corp. 2007, 2010
:: All Rights Reserved
::
:: US Government Users Restricted Rights - Use, duplication or
:: disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
::
:: IBM_PROLOG_END_TAG

@echo off
setlocal

set TEMP_BIN_DIR=%~d0%~p0

set SKIP_ISCDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

set PATH=%TDI_JAVA_BIN_DIR%;%TDI_LIB_DIR%

set COMMON_JARS_PATH=%TDI_HOME_DIR%\jars\common
set THRD_IBM_JAR_PATH=%TDI_HOME_DIR%\jars\3rdparty\IBM
set THRD_OTHERS_JAR_PATH=%TDI_HOME_DIR%\jars\3rdparty\others
set JAR_FILES=%COMMON_JARS_PATH%\miggbl.jar;%COMMON_JARS_PATH%\miconfig.jar;%COMMON_JARS_PATH%\miserver.jar;%COMMON_JARS_PATH%\mmconfig.jar;%THRD_OTHERS_JAR_PATH%\log4j-1.2-api-2.25.4.jar;%THRD_OTHERS_JAR_PATH%\log4j-api-2.25.4.jar;%THRD_OTHERS_JAR_PATH%\log4j-core-2.25.4.jar;%THRD_IBM_JAR_PATH%\icu4j-51_1.jar;%THRD_IBM_JAR_PATH%\ITLMToolkit.jar;%TDI_HOME_DIR%\jars\common\tdiresource.jar

rem Get solution directory parameter (overrides TDI_SOLDIR)
:checksol
if .%1==.-s (
	rem Make sure we are on the correct drive
	%~d2
	rem At this point overwrite the TDI_SOLDIR
	set TDI_SOLDIR=%2
	goto changedir
)
shift
if not .%1==. goto checksol

:changedir
rem Create the directory if it does not exist
if not exist %TDI_SOLDIR% mkdir %TDI_SOLDIR%
rem CD into solution directory
call "%TDI_BIN_DIR%\ibmdicwd" %TDI_SOLDIR%

if not exist logs mkdir logs

rem Take the supported env variables and pass them to Java program
set LOG_4J=-Dlog4j.configuration="file:etc\tdimiggbl-log4j.properties"
set ENV_VARIABLES=%LOG_4J%

"%TDI_JAVA_PROGRAM%" -cp "%JAR_FILES%" %ENV_VARIABLES% com.ibm.di.miggbl.MigrateGlobalProperties -i "%TDI_HOME_DIR%" -j "%TDI_JAVA_HOME%" %*

endlocal
