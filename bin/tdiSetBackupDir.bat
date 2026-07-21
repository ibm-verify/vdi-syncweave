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


set TDI_BIN_DIR=%~d0%~p0

set SKIP_JAVA_SETUP=1
set SKIP_SOLDIR_SETUP=1
set SKIP_ISCDIR_SETUP=1
call "%TDI_BIN_DIR%\setupCmdLine.bat"

if  not "%RC%"=="0" exit /b 1
if "%~1"=="" (
        echo Usage Error: tdiSetBackupDir TDI_BACKUP_DIR or default >&2
	exit /b 1
)

if "%~1" == "default" goto set_default

if not exist "%~1" (
	echo Usage Error: tdiSetBackupDir TDI_BACKUP_DIR or default >&2
	exit /b 1
)

goto setdir

:set_default
set TDI_BACKUP_DIR=%TDI_HOME_DIR%\maintenance\BACKUP
goto after_setdir

:setdir
set TDI_BACKUP_DIR=%~f1

:after_setdir
if exist "%TDI_BIN_DIR%\backupDir.bat"  del  "%TDI_BIN_DIR%\backupDir.bat"

if not exist "%TDI_BIN_DIR%\backupDir.bat"  goto NO_TDI_BACKUP_DIR_BAT

echo  "%TDI_BIN_DIR%\backupDir.bat"
exit /b 1

:NO_TDI_BACKUP_DIR_BAT
echo set TDI_BACKUP_DIR=%TDI_BACKUP_DIR%> "%TDI_BIN_DIR%\backupDir.bat"

if exist "%TDI_BIN_DIR%\backupDir.bat"  goto TDI_BACKUP_DIR_CREATED_OKAY

@rem
@rem Batch file could not be created. Error.
@rem
echo "%TDI_BIN_DIR%\backupDir.bat" >&2
exit /b 1

:TDI_BACKUP_DIR_CREATED_OKAY


echo TDI_BACKUP_DIR=%TDI_BACKUP_DIR%
echo "TDI_BACKUP_DIR successfully set."


