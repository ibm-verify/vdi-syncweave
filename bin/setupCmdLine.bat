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

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Internal command to setup a set of environment variables used
:: by other TDI commands
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

set RC=1

set TDI_HOME_DIR=%~d0%~p0..

rem
rem Save the current directory for later.
rem

for /F "usebackq delims==" %%i IN (`cd`) DO set SAVE_DIR=%%i

rem
rem CD to the TDI directory to get a clean directory path
rem
cd /d "%TDI_HOME_DIR%"

for /F "usebackq delims==" %%i IN (`cd`) DO set TDI_HOME_DIR=%%i

cd /d "%SAVE_DIR%"


set TDI_BIN_DIR=%TDI_HOME_DIR%\bin
set TDI_ETC_DIR=%TDI_HOME_DIR%\etc
set TDI_LIB_DIR=%TDI_HOME_DIR%\libs

if not "%SKIP_ISCDIR_SETUP%"==""  goto NO_ISCDIR_PROCESSING

	if not exist "%TDI_BIN_DIR%\tdiISCHome.bat" goto NO_ISCDIR_BAT

	rem Only set TDI_ISC_HOME if it hasn't been set already in caller's shell
	if .%TDI_ISCDIR%==. (
		call "%TDI_BIN_DIR%\tdiISCHome.bat"
	)

	:NO_ISC_BAT

	if  exist "%TDI_ISC_HOME%" goto :OK_ISCDIR

		echo Incorrect TDI_ISC_HOME %TDI_ISC_HOME%
		set RC=6
		break /b 1

	:OK_ISCDIR

:NO_ISCDIR_PROCESSING

if not "%SKIP_SOLDIR_SETUP%"==""  goto NO_SOLDIR_PROCESSING

	if not exist "%TDI_BIN_DIR%\defaultSolDir.bat" goto NO_SOLDIR_BAT

	rem Only set TDI_SOLDIR if it hasn't been set already in caller's shell
	if .%TDI_SOLDIR%==. (
		call "%TDI_BIN_DIR%\defaultSolDir.bat"
	)

	:NO_SOLDIR_BAT


	if  exist %TDI_SOLDIR% goto :OK_SOLDIR

		echo Incorrect TDI_SOLDIR %TDI_SOLDIR%
		set RC=5
		exit /b 1

	:OK_SOLDIR

:NO_SOLDIR_PROCESSING

if not "%SKIP_JAVA_SETUP%"==""  goto NO_JAVA_PROCESSING


	if not exist "%TDI_BIN_DIR%\javaHome.bat" goto NO_TDI_JAVA_HOME_BAT

		call "%TDI_BIN_DIR%\javaHome.bat"

	:NO_TDI_JAVA_HOME_BAT


	if  exist "%TDI_JAVA_HOME%" goto :OK_TDI_JAVA_HOME

		echo Incorrect TDI_JAVA_HOME %TDI_JAVA_HOME%
		set RC=3
		exit /b 1

	:OK_TDI_JAVA_HOME

	set JAVA_EXECUTABLE=java.exe

	set TDI_JAVA_PROGRAM=%TDI_JAVA_HOME%\jre\bin\%JAVA_EXECUTABLE%
	set TDI_JAVA_BIN_DIR=%TDI_JAVA_HOME%\jre\bin

	if exist "%TDI_JAVA_PROGRAM%" goto OK_TDI_JAVA_PROGRAM

		echo Cannot run %TDI_JAVA_PROGRAM% >&2
		set RC=4
		exit /b 1

	:OK_TDI_JAVA_PROGRAM

        set JAVAW_EXECUTABLE=javaw.exe        
        if exist "%TDI_JAVA_BIN_DIR%\%JAVAW_EXECUTABLE%" goto :OK_JAVAW
        set JAVAW_EXECUTABLE=%JAVA_EXECUTABLE%

        :OK_JAVAW
        set TDI_JAVAW_PROGRAM=%TDI_JAVA_BIN_DIR%\%JAVAW_EXECUTABLE%


:NO_JAVA_PROCESSING

set RC=0
exit /b 0



