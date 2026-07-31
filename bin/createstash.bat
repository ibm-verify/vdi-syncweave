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

set TEMP_BIN_DIR=%~d0%~p0

set SKIP_ISCDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

"%TDI_JAVA_PROGRAM%" -cp "%TDI_HOME_DIR%\jars\common\miserver.jar;%TDI_HOME_DIR%\jars\common\tdiresource.jar;%TDI_HOME_DIR%\jars\3rdparty\IBM\icu4j-51_1.jar" com.ibm.di.server.StashFile %*

endlocal
