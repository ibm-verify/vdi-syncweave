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

set PATH=%TDI_JAVA_BIN_DIR%;%TDI_LIB_DIR%;%PATH%

rem use the trustore and keystore properties if server is running with 
rem property "api.remote.ssl.on" set to true
rem for eg :
rem use -D -Djavax.net.ssl.trustStore=serverapi\testadmin.jks -Djavax.net.ssl.keyStore=serverapi\testadmin.jks -Djavax.net.ssl.keyStorePassword=administrator -Djavax.net.ssl.trustStorePassword=administrator

set COMMON_JARS_PATH=%TDI_HOME_DIR%\jars\common
set THRD_IBM_JAR_PATH=%TDI_HOME_DIR%\jars\3rdparty\IBM
set THRD_OTHERS_JAR_PATH=%TDI_HOME_DIR%\jars\3rdparty\others
set JAR_FILES=%COMMON_JARS_PATH%\cli.jar;%COMMON_JARS_PATH%\diserverapi.jar;%COMMON_JARS_PATH%\diserverapirmi.jar;%COMMON_JARS_PATH%\miconfig.jar;%COMMON_JARS_PATH%\miserver.jar;%COMMON_JARS_PATH%\mmconfig.jar;%THRD_OTHERS_JAR_PATH%\log4j-1.2-api-2.25.4.jar;%THRD_OTHERS_JAR_PATH%\log4j-api-2.25.4.jar;%THRD_OTHERS_JAR_PATH%\log4j-core-2.25.4.jar;%THRD_IBM_JAR_PATH%\icu4j-51_1.jar;%THRD_IBM_JAR_PATH%\ITLMToolkit.jar;%TDI_HOME_DIR%\jars\common\tdiresource.jar;%THRD_IBM_JAR_PATH%\jlog.jar;%THRD_OTHERS_JAR_PATH%\mail.jar;%THRD_OTHERS_JAR_PATH%\activation.jar

rem Take the supported env variables and pass them to Java program

if not defined TDI_RSRV goto :stepA
set SRV=-DTDI_RSRV=%TDI_RSRV%

:stepA
if not defined TDI_RPORT goto :stepB
set PORT=-DTDI_RPORT=%TDI_RPORT%

:stepB

rem Get solution directory parameter (overrides TDI_SOLDIR)
rem the first and the second arguments could set different solution directory that overrides the default one
if .%1==.-s (
	rem Make sure we are on the correct drive
	%~d2
	rem At this point overwrite the TDI_SOLDIR
	set TDI_SOLDIR=%2

	rem get rid of the first two params
	shift
	shift
)

rem first remove quotes, as double quotes will be bad
set TDI_SOLDIR_TMP=###%TDI_SOLDIR%###
set TDI_SOLDIR_TMP=%TDI_SOLDIR_TMP:"###=%
set TDI_SOLDIR_TMP=%TDI_SOLDIR_TMP:###"=%
set TDI_SOLDIR=%TDI_SOLDIR_TMP:###=%

if exist "%TDI_SOLDIR%" goto OK_SOL_DIR
set LOG_4J=-Dlog4j.configuration="file:///%TDI_HOME_DIR%\etc\tdisrvctl-log4j.properties"
goto SET_ENV_VARS

:OK_SOL_DIR
set LOG_4J=-Dlog4j.configuration="file:etc/tdisrvctl-log4j.properties"
call "%TEMP_BIN_DIR%\ibmdicwd" "%TDI_SOLDIR%"
if not exist logs mkdir logs


:SET_ENV_VARS
rem Take the supported env variables and pass them to Java program

set ENV_VARIABLES=%SRV% %PORT% %LOG_4J%

"%TDI_JAVA_PROGRAM%" -cp "%JAR_FILES%" %ENV_VARIABLES% com.ibm.di.cli.RemoteServerCommand %*
set RC=%ERRORLELVEL%

exit /b %RC%

endlocal
