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

set SKIP_JAVA_SETUP=1
set SKIP_SOLDIR_SETUP=1
set SKIP_ISCDIR_SETUP=1
call "%TDI_BIN_DIR%\setupCmdLine.bat"

if  not "%RC%"=="0" exit /b 1

if "%~1"=="" (
	echo Usage Error: setDefaultSolDir Solution_Dir >&2
	exit /b 1
)

if not exist "%~1" (
	echo Usage Error: setDefaultSolDir Solution_Dir >&2
	exit /b 1
)

if "%~1" == "." (set SOL_DIR=.) else (set SOL_DIR="%~f1")

if exist %SOL_DIR% goto FOUND_SOLDIR


@rem
@rem No Solution Dir found. Print error
@rem
echo %SOL_DIR%
dir %SOL_DIR%

exit /b 1


:FOUND_SOLDIR

if exist "%TDI_BIN_DIR%\defaultSolDir.bat"  del  "%TDI_BIN_DIR%\defaultSolDir.bat"

if not exist "%TDI_BIN_DIR%\defaultSolDir.bat"  goto NO_SOL_DIR_BAT

echo  "%TDI_BIN_DIR%\defaultSolDir.bat"
exit /b 1

:NO_SOL_DIR_BAT

echo set TDI_SOLDIR=%SOL_DIR%> "%TDI_BIN_DIR%\defaultSolDir.bat"

if exist "%TDI_BIN_DIR%\defaultSolDir.bat"  goto SOL_DIR_CREATED_OKAY

@rem
@rem Batch file could not be created. Error.
@rem
echo "%TDI_BIN_DIR%\defaultSolDir.bat" >&2
exit /b 1

:SOL_DIR_CREATED_OKAY


echo TDI_SOLDIR=%SOL_DIR%
echo "TDI_SOLDIR successfully set."


