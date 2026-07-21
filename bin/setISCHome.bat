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
	echo Usage Error: setISCHome TDI_ISC_HOMEdir >&2
	exit /b 1
)

if not exist "%~1" (
	echo Usage Error: setISCHome TDI_ISC_HOMEdir >&2
	exit /b 1
)

set TDI_ISC_HOME=%~f1

set TDI_LWI_PROGRAM=%TDI_ISC_HOME%\bin\lwistart.bat
set TDI_WAS_PROGRAM=%TDI_ISC_HOME%\bin\startServer.bat

if exist "%TDI_LWI_PROGRAM%" goto FOUND_ISC_LWI
if exist "%TDI_WAS_PROGRAM%" goto FOUND_ISC_WAS

@rem
@rem No ISC found. Print error
@rem
echo "%TDI_LWI_PROGRAM%"
dir "%TDI_LWI_PROGRAM%"

exit /b 1

:FOUND_ISC_LWI
set USING_LWI=yes
goto FOUND_ISC

:FOUND_ISC_WAS
set USING_LWI=no
goto FOUND_ISC

:FOUND_ISC

if exist "%TDI_BIN_DIR%\tdiISCHome.bat"  del  "%TDI_BIN_DIR%\tdiISCHome.bat"

if not exist "%TDI_BIN_DIR%\tdiISCHome.bat"  goto NO_TDI_ISC_HOME_BAT 

echo  "%TDI_BIN_DIR%\tdiISCHome.bat"
exit /b 1

:NO_TDI_ISC_HOME_BAT

if .%USING_LWI%==.yes (
		echo set TDI_ISC_RUNTIME=LWI> "%TDI_BIN_DIR%\tdiISCHome.bat"
)
if .%USING_LWI%==.no (
		echo set TDI_ISC_RUNTIME=WAS> "%TDI_BIN_DIR%\tdiISCHome.bat"
)

echo set TDI_ISC_HOME=%TDI_ISC_HOME%>> "%TDI_BIN_DIR%\tdiISCHome.bat"

if exist "%TDI_BIN_DIR%\tdiISCHome.bat"  goto TDI_ISC_HOME_CREATED_OKAY 

@rem
@rem Batch file could not be created. Error.
@rem
echo "%TDI_BIN_DIR%\tdiISCHome.bat" >&2
exit /b 1

:TDI_ISC_HOME_CREATED_OKAY


echo TDI_ISC_HOME=%TDI_ISC_HOME%
echo "TDI_ISC_HOME successfully set."


