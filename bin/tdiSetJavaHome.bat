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
	echo Usage Error: tdiSetJavaHome TDI_JAVA_HOMEdir >&2
	exit /b 1
)

if not exist "%~1" (
	echo Usage Error: tdiSetJavaHome TDI_JAVA_HOMEdir >&2
	exit /b 1
)

set TDI_JAVA_HOME_DIR=%~f1

set TDI_JAVA_PROGRAM=%TDI_JAVA_HOME_DIR%\jre\bin\java.exe

if exist "%TDI_JAVA_PROGRAM%" goto FOUND_JAVA


@rem
@rem No java found. Print error
@rem
echo "%TDI_JAVA_PROGRAM%"
dir "%TDI_JAVA_PROGRAM%"

exit /b 1


:FOUND_JAVA

if exist "%TDI_BIN_DIR%\javaHome.bat"  del  "%TDI_BIN_DIR%\javaHome.bat"

if not exist "%TDI_BIN_DIR%\javaHome.bat"  goto NO_TDI_JAVA_HOME_BAT 

echo  "%TDI_BIN_DIR%\javaHome.bat"
exit /b 1

:NO_TDI_JAVA_HOME_BAT

echo set TDI_JAVA_HOME=%TDI_JAVA_HOME_DIR%> "%TDI_BIN_DIR%\javaHome.bat"

if exist "%TDI_BIN_DIR%\javaHome.bat"  goto TDI_JAVA_HOME_CREATED_OKAY 

@rem
@rem Batch file could not be created. Error.
@rem
echo "%TDI_BIN_DIR%\javaHome.bat" >&2
exit /b 1

:TDI_JAVA_HOME_CREATED_OKAY


echo TDI_JAVA_HOME=%TDI_JAVA_HOME_DIR%
echo "TDI_JAVA_HOME successfully set."


