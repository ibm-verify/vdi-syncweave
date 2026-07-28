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
::
:: IBM_PROLOG_END_TAG

@echo off

setlocal

set TDI_BIN_DIR=%~d0%~p0

set SKIP_SOLDIR_SETUP=1
set SKIP_ISCDIR_SETUP=1

call "%TDI_BIN_DIR%\setupCmdLine.bat"

set SKIP_SOLDIR_SETUP=
set SKIP_ISCDIR_SETUP=

if  not "%RC%"=="0" exit /b 1

REM echo TDI install dir = %TDI_HOME_DIR%
set MAINT_DIR_1=%TDI_HOME_DIR%\maintenance\BACKUP\SDI-7.2-FP0010\jars\3rdparty\others\ActiveMQ
set MAINT_DIR_2=%TDI_HOME_DIR%\maintenance\BACKUP\SDI-7.2-FP0010\pwd_plugins
mkdir %MAINT_DIR_1%
mkdir %MAINT_DIR_2%

set AC_DIR=%TDI_HOME_DIR%\jars\3rdparty\others\ActiveMQ
set FILE1=%AC_DIR%\spring*.jar
set FILE2=%AC_DIR%\activemq-core.jar
set FILE3=%AC_DIR%\geronimo-j2ee-management_1.0_spec-1.0.jar
set FILE4=%AC_DIR%\geronimo-jta_1.0.1B_spec-1.0.1.jar
set FILE5=%AC_DIR%\xbean-spring-3.6.jar

set PWD_PLUGIN_DIR=%TDI_HOME_DIR%\pwd_plugins\jars
set FILE6=%PWD_PLUGIN_DIR%\activemq-core.jar
set FILE7=%PWD_PLUGIN_DIR%\geronimo-j2ee-management_1.0_spec-1.0.jar

 if exist "%AC_DIR%" (
   echo moving old versioned files from %AC_DIR% to %MAINT_DIR_1%
   move /Y "%FILE1%" "%MAINT_DIR_1%" 2>nul
   move /Y "%FILE2%" "%MAINT_DIR_1%" 2>nul
   move /Y "%FILE3%" "%MAINT_DIR_1%" 2>nul
   move /Y "%FILE4%" "%MAINT_DIR_1%" 2>nul
   move /Y "%FILE5%" "%MAINT_DIR_1%" 2>nul
 )

 if exist "%PWD_PLUGIN_DIR%" (
   echo moving old versioned files from %PWD_PLUGIN_DIR% to %MAINT_DIR_2%
   move /Y "%FILE6%" "%MAINT_DIR_2%" 2>nul
   move /Y "%FILE7%" "%MAINT_DIR%" 2>nul
 )

