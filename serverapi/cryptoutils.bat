@echo off

setlocal

set TEMP_BIN_DIR=%~d0%~p0..\bin

set SKIP_ISCDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

"%TDI_JAVA_PROGRAM%" -cp "%TDI_HOME_DIR%\jars\common\tdiresource.jar;%TDI_HOME_DIR%\jars\common\diserverapi.jar;%TDI_HOME_DIR%\jars\common\miserver.jar;%TDI_HOME_DIR%\jars\common\diserverapirmi.jar;%TDI_HOME_DIR%\jars\common\miconfig.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-1.2-api-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-api-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\others\log4j-core-2.25.4.jar;%TDI_HOME_DIR%\jars\3rdparty\IBM\icu4j-51_1.jar;%TDI_HOME_DIR%\jars\3rdparty\others\mail.jar;%TDI_HOME_DIR%\jars\3rdparty\others\activation.jar" com.ibm.di.api.security.CryptoUtils %*

endlocal
