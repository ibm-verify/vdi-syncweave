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
call "%TDI_BIN_DIR%\backupDir.bat"

set SKIP_SOLDIR_SETUP=
set SKIP_ISCDIR_SETUP=


if  not "%RC%"=="0" exit /b 1

REM echo Backup dir = %TDI_BACKUP_DIR%
REM echo Home dir = %TDI_HOME_DIR%
REM echo Java prg = %TDI_JAVA_PROGRAM%

set TDI_UPDATE_PRG=com.ibm.di.UpdateInstaller.UpdateInstaller
set TDI_UPDATE_CP=%TDI_HOME_DIR%\maintenance\UpdateInstaller.jar;%TDI_HOME_DIR%\jars\3rdparty\IBM\icu4j-51_1.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-1.2-api-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-api-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-core-2.25.4.jar;%TDI_HOME_DIR%\jars\common\tdiresource.jar

set LOG_4J=-DlogDir="%TDI_HOME_DIR%" -Dlog4j2.configurationFile="%TDI_HOME_DIR%\etc\updateinstaller-log4j.properties"
set UPDATE_JRE=0
set UPDATE_UI=0
if not "%1" == "-update" goto check_rollback
if "%~2" == "" goto run_update_installer
goto check_jre

:check_rollback
if not "%1" == "-rollback" goto run_update_installer
if not "%~2" == "" goto run_update_installer

:check_jre
set TDI_CHECK4JRE_PRG=com.ibm.di.UpdateInstaller.CheckForJREUpdate
"%TDI_JAVA_PROGRAM%" %LOG_4J% -cp "%TDI_UPDATE_CP%" "%TDI_CHECK4JRE_PRG%" "%TDI_HOME_DIR%" "%TDI_BACKUP_DIR%" "JRE.zip" %*

if %ERRORLEVEL% == 255 SET UPDATE_JRE=1
REM echo UPDATE_JRE=%UPDATE_JRE%
if "%UPDATE_JRE%" == "0" goto check_UI

REM copy JRE
xcopy /E /V /I /Q /H /R /Y "%TDI_HOME_DIR%\jvm" "%TDI_HOME_DIR%\maintenance\jvm" > NUL
set TDI_JAVA_PROGRAM=%TDI_HOME_DIR%\maintenance\jvm\jre\bin\java.exe

:check_UI
set TDI_CHECK4UI_PRG=com.ibm.di.UpdateInstaller.CheckForUpdateInstallerUpdate
"%TDI_JAVA_PROGRAM%" %LOG_4J% -cp "%TDI_UPDATE_CP%" "%TDI_CHECK4UI_PRG%" "%TDI_HOME_DIR%" "%TDI_BACKUP_DIR%" %*
if %ERRORLEVEL% == 255 SET UPDATE_UI=1
REM echo UPDATE_UI=%UPDATE_UI%
if "%UPDATE_UI%" == "0" goto run_update_installer
REM use backup of UpdateInstaller jar
copy /V /Y "%TDI_HOME_DIR%\maintenance\UpdateInstaller.jar" "%TDI_HOME_DIR%\maintenance\UpdateInstaller_tmp.jar" > NUL
set TDI_UPDATE_CP=%TDI_HOME_DIR%\maintenance\UpdateInstaller_tmp.jar;%TDI_HOME_DIR%\jars\3rdparty\IBM\icu4j-51_1.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-1.2-api-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-api-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-core-2.25.4.jar;%TDI_HOME_DIR%\jars\common\tdiresource.jar
set TDI_REPLACE_UI_PRG=com.ibm.di.UpdateInstaller.ReplaceUpdateInstallerJar
"%TDI_JAVA_PROGRAM%" %LOG_4J% -cp "%TDI_UPDATE_CP%" "%TDI_REPLACE_UI_PRG%" "%TDI_HOME_DIR%" "%TDI_BACKUP_DIR%" %*
set TDI_UPDATE_CP=%TDI_HOME_DIR%\maintenance\UpdateInstaller.jar;%TDI_HOME_DIR%\jars\3rdparty\IBM\icu4j-51_1.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-1.2-api-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-api-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-core-2.25.4.jar;%TDI_HOME_DIR%\jars\common\tdiresource.jar



:run_update_installer
set TDI_UPDATE_CP=%TDI_UPDATE_CP%;%TDI_HOME_DIR%\jars\3rdparty\IBM\LUMClient.jar
set PATH=%PATH%;%TDI_HOME_DIR%\libs
"%TDI_JAVA_PROGRAM%" %LOG_4J% -cp "%TDI_UPDATE_CP%" "%TDI_UPDATE_PRG%" "%TDI_HOME_DIR%" "%TDI_BACKUP_DIR%" %*

if "%UPDATE_JRE%" == "0" goto remove_tmp_UI

rmdir /S /Q "%TDI_HOME_DIR%\maintenance\jvm"

:remove_tmp_UI
if "%UPDATE_UI%" == "0" goto done

del "%TDI_HOME_DIR%\maintenance\UpdateInstaller_tmp.jar"

:done
