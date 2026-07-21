:: IBM_PROLOG_BEGIN_TAG
::
:: %I%, %G%
::
:: Licensed Materials - Property of IBM
::
:: Restricted Materials of IBM
::
:: (C) COPYRIGHT International Business Machines Corp. 2007, 2023
:: All Rights Reserved
::
:: US Government Users Restricted Rights - Use, duplication or
:: disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
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

set APPLY_NEW=%TDI_BIN_DIR%\applyUpdates.new.bat
set APPLY_CURRENT=%TDI_BIN_DIR%\applyUpdates.bat
dir /O-D /b %TDI_BACKUP_DIR% | findstr SDI-7.2-FP00 > tmp_sdi
set /p BACKUP_BIN= <tmp_sdi
del tmp_sdi
echo %BACKUP_BIN%
set BACKUP_BIN=%TDI_BACKUP_DIR%\%BACKUP_BIN%\%JARDIR%
set APPLY_OLD=%TDI_BIN_DIR%\applyUpdates.old.bat

if exist "%APPLY_NEW%" (
  if exist "%BACKUP_BIN%" (
    move "%APPLY_CURRENT%" "%BACKUP_BIN%"
  ) else (
    move "%APPLY_CURRENT%" "%APPLY_OLD%"
  )
  move "%APPLY_NEW%" "%APPLY_CURRENT%" && echo "Successfully created new %APPLY_CURRENT%"
)

set JARDIR=jars\3rdparty\others
set JARFILE=%JARDIR%\log4j-1.2.16.jar
set FROM=%TDI_HOME_DIR%\%JARFILE%
dir /O-D /b %TDI_BACKUP_DIR% | findstr SDI-7.2-FP00 > tmp_sdi
set /p BACKUP_DIR= < tmp_sdi
del tmp_sdi
echo %BACKUP_DIR%
set BACKUP_DIR=%TDI_BACKUP_DIR%\%BACKUP_DIR%\%JARDIR%
set TO=%BACKUP_DIR%\log4j-1.2.16.jar

if exist "%FROM%" (
  if exist "%BACKUP_DIR%" (
    REM echo Attempting the command: move "%FROM%" "%TO%"
    move "%FROM%" "%TO%" && echo "Success!"
  ) else (
    REM echo Attempting the command: del "%FROM%"
    del "%FROM%" && echo "Success!"
  )
) else (
  echo The old log4j file "%FROM%" was not found.
  rem exit 1
)
