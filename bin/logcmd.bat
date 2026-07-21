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

set TEMP_BIN_DIR=%~d0%~p0

set SKIP_ISCDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

"%TDI_JAVA_PROGRAM%" -cp "%TDI_HOME_DIR%\jars\3rdparty\IBM\jlog.jar" com.ibm.log.cmd.LogCmd %1 %2 %3 %4 %5 %6 %7 %8 %9

endlocal
