@echo off
REM * ------------------------------------------------------------------------------
REM *  Licensed Materials - Property of IBM
REM *
REM *  5724-O62
REM *  (C) . 2008
REM *
REM *  US Government Users Restricted Rights - Use, duplication, or
REM *  disclosure restricted by GSA ADP Schedule Contract with
REM *  IBM Corp.
REM * ------------------------------------------------------------------------------
REM This file is the top level build command file.  It is referenced
REM by PCBUILD.INI and launches the build process.
REM   ------------------------------------------------------------------------
REM   Configure build tools
SET PATH=%PATH%;%BLDTOOLS%

REM   ------------------------------------------------------------------------
@echo "==>  Configure path to access Cygwin"
REM   ------------------------------------------------------------------------
set CYGWIN_BIN_DIR=%CYGWIN%\cc_Tools\wnt-4.0-x86\Cygwin-1.5.9-1\bin
@echo "==> %CYGWIN_BIN_DIR%"


REM   ------------------------------------------------------------------------
@echo "==> Running Robocopy to sync files with GSA tools"
REM   ------------------------------------------------------------------------

SET ROBO_JOBS_DIR=%RABROOT%\JOBs
%BLDTOOLS%\robocopy.exe /JOB:%ROBO_JOBS_DIR%\common
%BLDTOOLS%\robocopy.exe /JOB:%ROBO_JOBS_DIR%\x86_nt_4


@echo ------------------------------------------------------------------------
@echo BUILD started
@echo ------------------------------------------------------------------------
@echo.

REM   ------------------------------------------------------------------------
REM  Display environment
SET | sort
@echo.

@echo Setting up the SDI Docs build environment based on itdi_buildsetup.bat

rem Set the release name up.
set ITDI_RELEASE_NAME=%LMF_CMVC_RELEASE%

rem set whether this is the main part of the build or the install part of the build.
rem main is for main build and install is for install build.
set ITDI_BUILD_TYPE=docs

rem set the location where the setup script is located at.  that way its always in your
rem path if you need to invoke this script again.
set SETUP_SCRIPTS=%LMF_BUILD_DRIVE%

rem Setup the path to the Tools.
set TOOLS_HOME=%TDITOOLS%

rem set up the platform.
set ARCH=x86_nt_4

%LMF_BUILD_DRIVE%
cd \

setlocal

@echo ---------------------------------------------------------------------------
@echo Set the build variables
@echo ---------------------------------------------------------------------------
set GSA=%GSA_PREREQS%\%LMF_CMVC_RELEASE%
set ANT_VERSION=1.8
set ANT_HOME=%TOOLS_HOME%\common\apache\ant\%ANT_VERSION%
rem set ANT_HOME=%LMF_ANT%
set JAVA_HOME=%LMF_JDK%
set IDWB_HOME=%LMF_IDWB_4.3.1%
set CBUTIL_HOME=%LMF_CBUTIL%

set IDWBCODE=%IDWB_HOME%\bin
set IDWBTMP=%LMF_BUILD_DRIVE%\temp
set IDWORKB=%IDWB_HOME%
set IDXFORM=%IDWB_HOME%\bin

@echo ---------------------------------------------------------------------------
@echo Set the PATH
@echo ---------------------------------------------------------------------------

set LOCPATH=%IDWB_HOME%\locale
set PATH=%ANT_HOME%\bin;%JAVA_HOME%\bin;%IDWB_HOME%\bin;%IDWB_HOME%\locale;%IDWB_HOME%\help;%IDWB_HOME%\EPIC\bin;%LMF_RAB_CMVC%;%LMF_RAB_CMVC%\bin;%CBUTIL_HOME%;%PATH%

@echo ---------------------------------------------------------------------------
@echo %LMF_BUILD_DRIVE%
@echo THE LMF_BUILD_Directory
@echo ---------------------------------------------------------------------------

@echo ---------------------------------------------------------------------------
@echo %LMF_CMVC_RELEASE% Build started
@echo ---------------------------------------------------------------------------

ant -f tdi_docs\build_idddocs.xml

@echo ---------------------------------------------------------------------------
@echo Build finished
@echo ---------------------------------------------------------------------------
goto END

:END

