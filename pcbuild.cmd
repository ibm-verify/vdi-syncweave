@echo on

REM * ------------------------------------------------------------------------------
REM *  Licensed Materials - Property of IBM
REM *
REM *  5724-O62
REM *  (C) . 2008
REM *
REM *  IBM Corp.
REM * ------------------------------------------------------------------------------
REM This file is the top level build command file.  It is referenced
REM by PCBUILD.INI and launches the build process.
REM This file depending on user parameter "BUILD_TYPE" decided whether to invoke pcbuild.cmd 
REM for ibmdi_dev build or pcbuild.cmd for ibmdi_docs_dev build
REM   ------------------------------------------------------------------------

REM   Set the Environment variables
rem call etc\build\userinput.data.bat

REM   ------------------------------------------------------------------------
@echo "VVS ==> Value of BUILD_TYPE variable: %BUILD_TYPE%"
REM   ------------------------------------------------------------------------
REM  Display environment
SET | sort
@echo.

REM IF "%BUILD_TYPE%"=="DEV" call pcbuild_dev.cmd
REM IF "%BUILD_TYPE%"=="DOCS" call pcbuild_docs.cmd
@echo ------------------------------------------------------------------------
@echo Calling respective PCBUILD.CMD
IF "%BUILD_TYPE%"=="DOCS" (
   call pcbuild_docs.cmd
) ELSE (
   call pcbuild_dev.cmd
)

@echo ------------------------------------------------------------------------
@echo BUILD finished
