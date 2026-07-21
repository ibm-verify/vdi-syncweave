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

set SKIP_SOLDIR_SETUP=1
set SKIP_ISCDIR_SETUP=1

call "%TDI_BIN_DIR%\setupCmdLine.bat"

set SKIP_SOLDIR_SETUP=
set SKIP_ISCDIR_SETUP=

if  not "%RC%"=="0" exit /b 1

REM echo TDI install dir = %TDI_HOME_DIR%

set AMC_DIR=%TDI_HOME_DIR%\amc
set BIN_DIR=%TDI_HOME_DIR%\bin\amc
set LWI_DIR=%TDI_HOME_DIR%\lwi

if exist "%AMC_DIR%" (
  echo Removing %AMC_DIR%
  rd "%AMC_DIR%" /s /q
)

if exist "%BIN_DIR%" (
  echo Removing %BIN_DIR%
  rd "%BIN_DIR%" /s /q
)

if exist "%LWI_DIR%" (
  echo Removing %LWI_DIR%
  rd "%LWI_DIR%" /s /q
)

