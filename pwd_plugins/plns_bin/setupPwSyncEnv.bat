:: IBM_PROLOG_BEGIN_TAG
::
:: %I%, %G%
::
:: Licensed Materials - Property of IBM
::
:: Restricted Materials of IBM
::
:: (C) COPYRIGHT International Business Machines Corp. 2008, 2013
:: All Rights Reserved
::
:: US Government Users Restricted Rights - Use, duplication or
:: disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
::
:: IBM_PROLOG_END_TAG

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: This script is used to setup the common environment variables used by the plugins.
:: This script uses the TDI_BIN_DIR/setupCmdLine.sh script for setting up common
:: variables.
:: This script is not expected to run on i5/OS or z/OS
::
:: This script checks for the existence of the following environment variables:
::   PWS_BIN_DIR - if not already set then will check if it is passed as the first
::           argument or will try to resolve it.
::   PWS_JARS_DIR - if not provided then the script will use the jars folder,
::           sibling of the PWS_BIN_DIR directory.
::   PWS_CLASSPATH - if not provided then the script will construct the classpath
::           from all the jar files in the PWS_JARS_DIR directory.
::   PWS_CONFIG_FILE - if provided the will resolve the absolute path to the file. 
::           The file will be resolved based on the pwd.
::
:: This script exports the following variables:
::   PWS_BIN_DIR - the directory containing plugins' executables
::   PWS_HOME_DIR - the home directory of the plugins
::   TDI_BIN_DIR - the directory containing TDI's executables
::   PWS_JARS_DIR - the directory containing plugins' jar files
::   PWS_CLASSPATH - list of the jar files separated by column
::   PWS_CONFIG_FILE - the resolved absolut path to the provided config file
::   PWS_CONFIG_DIR - the directory known as the authentication folder
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

::
:: Only set PWS_BIN_DIR if it hasn't been set already in caller's shell
::
if not "%PWS_BIN_DIR%"=="" goto :skip_bin_dir

	:: use the first variable passed in as the bin dir
	if not "%~1"=="" goto :use_first
		set PWS_BIN_DIR=%~d0%~p0
	:use_first
		set PWS_BIN_DIR=%~1
:skip_bin_dir

:: The PWSync home folder
call :begin_which "%PWS_BIN_DIR%\.." PWS_HOME_DIR

:: The bin folder of SDI
call :begin_which "%PWS_BIN_DIR%\..\..\bin" TDI_BIN_DIR

:: Run the standard setup script
call "%TDI_BIN_DIR%\setupCmdLine.bat" "%TDI_BIN_DIR%"

::
:: Only set PWS_JARS_DIR if it hasn't been set already in caller's shell
::
:: The folder where all the jar files are held
if "%PWS_JARS_DIR%"=="" set PWS_JARS_DIR=%PWS_HOME_DIR%\jars

::
:: Only set PWS_CLASSPATH if it hasn't been set already in caller's shell
::
if "%PWS_CLASSPATH%"=="" for /f "tokens=* delims= " %%i in ('"%PWS_BIN_DIR%\gatherClassPath.bat"') do set PWS_CLASSPATH=%%i

:: We need to hava a class path!!! I not then provide a dummy value for the 
:: PWS_CLASSPATH variable to skip this error.
if "%PWS_CLASSPATH%"=="" (
	echo "Incorrect PWS_CLASSPATH (%PWS_CLASSPATH%)" >&2
	set RC=1
	exit /b 1
)

:: Make sure we resolve the absolute path to the PWS_CONFIG_FILE. Will also
:: export a PWS_CONFIG_DIR which is the absolute path to the folder where
:: the config file is placed.
if not "%PWS_CONFIG_FILE%"=="" ( 
	call :begin_which "%PWS_CONFIG_FILE%" PWS_CONFIG_FILE
	call :begin_dirname "%PWS_CONFIG_FILE%" PWS_CONFIG_DIR
)

:: assuming the script is called in order to execute the file resoving functions: which, dirname, basename
if not "%WHICH%"=="" call :begin_which "%WHICH%" WHICH
if not "%DIRNAME%"=="" call :begin_dirname "%DIRNAME%" DIRNAME
if not "%BASENAME%"=="" call :begin_basename "%BASENAME%" BASENAME

:: This is a function that will expand the provided relative path to a full one
:: Parameters:
::	#1 [in] - the relative path to the file/dir to expand
::	#2 [out] - the variable name to set the expanded path into.
:: Usage example: {
::	set myVar=
::	call:begin_get_full_path "relative path/to/external.file" myVar
::	echo %myVar%
:: }
:: Example's output:
::	c:\my documents\relative path/to/external.file
goto :end_which
:begin_which

set result=%~dpnx1

if "%~2" NEQ "" set %~2=%result%

:: end function
exit /b
:end_which


:: This is a function that will return the full path of the parent directory
:: Parameters:
::	#1 [in] - the file/dir which parent dir to get.
::	#2 [out] - the variable name to set the path into.
:: Usage example: {
::	set myVar=
::	call:begin_get_full_path "parent/file.name" myVar
::	echo %myVar%
:: }
:: Example's output:
::	c:\my documents\parent
goto :end_dirname
:begin_dirname

set result=%~dp1

if "%~2" NEQ "" set %~2=%result%

:: end function
exit /b
:end_dirname


:: This is a function that will return the name of the provided file.
:: Parameters:
::	#1 [in] - the file/dir which name to return
::	#2 [out] - the variable name to set the name into.
:: Usage example: {
::	set myVar=
::	call:begin_get_full_path "parent/file.name" myVar
::	echo %myVar%
:: }
:: Example's output:
::	file.name
goto :end_basename
:begin_basename

set result=%~nx1

if "%~2" NEQ "" set %~2=%result%

:: end function
exit /b
:end_basename
