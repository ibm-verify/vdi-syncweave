@echo on

REM * ------------------------------------------------------------------------------
REM *  Licensed Materials - Property of IBM
REM *
REM *  5724-O62
REM *  (C) Copyright IBM Corp. 2008
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
@echo "RAB ==>  Configure path to access Cygwin"
REM   ------------------------------------------------------------------------
set CYGWIN_BIN_DIR=%CYGWIN%\cc_Tools\wnt-4.0-x86\Cygwin-1.5.9-1\bin
@echo "RAB ==> %CYGWIN_BIN_DIR%"

REM   ------------------------------------------------------------------------
@echo "RAB ==> Running Robocopy to sync files with GSA tools"
REM   ------------------------------------------------------------------------
SET ROBO_JOBS_DIR=%RABROOT%\JOBs
%BLDTOOLS%\robocopy.exe %SETUP_DRIVE%\GSA\common %GSA_PREREQS%\common
%BLDTOOLS%\robocopy.exe %SETUP_DRIVE%\GSA\x86_nt_4 %GSA_PREREQS%\x86_nt_4

@echo ------------------------------------------------------------------------
@echo BUILD started
@echo ------------------------------------------------------------------------
@echo.

REM   ------------------------------------------------------------------------
REM  Display environment
SET | sort
@echo.


REM   ------------------------------------------------------------------------
REM  Start the build of the Windows install image
@echo ------------------------------------------------------------------------
%LMF_BUILD_DRIVE%
chdir \%WIN_BLDTREE%

REM # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
@echo Setting up the TDI build environment based on itdi_buildsetup.bat

rem Set the release name up.
set ITDI_RELEASE_NAME=%LMF_CMVC_RELEASE%

rem set whether this is the main part of the build or the install part of the build.
rem main is for main build and install is for install build.
set ITDI_BUILD_TYPE=main

rem set the location where the setup script is located at.  that way its always in your
rem path if you need to invoke this script again.
set SETUP_SCRIPTS=%LMF_BUILD_DRIVE%

rem Setup the path to the Tools.
set TOOLS_HOME=%TDITOOLS%

rem set up the platform.
set ARCH=x86_nt_4

rem clear out environment variables that may mess up the build.
set CLASSPATH=
set JAVA_HOME=
set INCLUDE=
set PATH=
set LIB=
set MsDevDir=
set LIBPATH=

rem point to the miscellaneous tools required for build.
set RAB_VERSION=1.0
set RABTOOLS_HOME=%TOOLS_HOME%\%ARCH%\rabtools\%RAB_VERSION%
set RABTOOLS_BIN=%RABTOOLS_HOME%\bin

rem point to the version of perl to use.
set PERL_VERSION=5.8.7.813
set PERL_HOME=%TOOLS_HOME%\%ARCH%\compilers\activestate\perl\%PERL_VERSION%
set PERL_BIN=%PERL_HOME%\bin

set JAVA_VERSION=1.6

set JAVA_HOME=%TOOLS_HOME%\%ARCH%\compilers\ibm\java\%JAVA_VERSION%
set JAVA_BIN=%JAVA_HOME%\bin

set ANT_VERSION=1.7
set ANT_HOME=%TOOLS_HOME%\common\apache\ant\%ANT_VERSION%
set ANT_BIN=%ANT_HOME%\bin

rem set the version of ISMP that we use.
set ISMP_VERSION=5.03
set ISMP_HOME=%TOOLS_HOME%\common\is\ismp\%ISMP_VERSION%
set ISMP_BIN=%ISMP_HOME%
set ismp50=%ISMP_HOME%

rem set up a few ISSI related variables.
set TDI_HOME=C:\Builds\%LMF_CMVC_RELEASE%\%TODAYS_BUILD_LEVEL%
set TDI_TOOLS=%TOOLS_HOME%
set IS_VERSION=1.2.1
set IS_HOME=%TOOLS_HOME%\common\is\isi\%IS_VERSION%

rem set up the PATH so that we can build everything.
set PATH=%PATH%;%SETUP_SCRIPTS%;C:\WINNT\system32
rem set PATH=%SETUP_SCRIPTS%;C:\WINNT\system32

rem Now add all of the tools that are needed.
set PATH=%IBMREX_HOME%;%PATH%
set PATH=%IDWORRKBENCH_BIN%;%PATH%
set PATH=%CMVC_BIN%;%PATH%
set PATH=%JAVA_BIN%;%PATH%
set PATH=%PERL_BIN%;%PATH%
set PATH=%ANT_BIN%;%PATH%
set PATH=%ISMP_BIN%;%PATH%
set PATH=%RABTOOLS_BIN%;%PATH%

rem Now set up the CLASSPATH
set CLASSPATH=.;
set CLASSPATH=%ANT_HOME%\lib\ant.jar;%CLASSPATH%
set CLASSPATH=%ANT_HOME%\build\lib;%CLASSPATH%
set CLASSPATH=%ANT_HOME%\lib\xercesImpl.jar;%CLASSPATH%
set CLASSPATH=%ANT_HOME%\lib\xml-apis.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\aixppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\genericunixppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\hpuxppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\linux390ppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\linuxppcppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\linuxppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\solarisppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\webppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\ppk\win32ppk.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\conversion.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\help.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\icebrowserbean.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\icebrowserlitebean.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\ide.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\jhall.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\parser.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\platform.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\product.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\swing.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\wizard.jar;%CLASSPATH%
set CLASSPATH=%ISMP_HOME%\lib\xt.jar;%CLASSPATH%

REM # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
chdir \%WIN_BLDTREE%
call autogen.bat
echo RAB %ANT_HOME%
%ANT_HOME%\bin\ant -f build.xml

@echo ------------------------------------------------------------------------
@echo BUILD finished
@echo.

