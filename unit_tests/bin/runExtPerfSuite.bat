@echo off
setlocal

set UNIT_TESTS_HOME=%~d0%~p0..
set TEMP_BIN_DIR=%~d0%~p0..\..\bin

set SKIP_ISCDIR_SETUP=1
set SKIP_SOLDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

rem CD into unit_tests directory
call "%TDI_BIN_DIR%\ibmdicwd" "%UNIT_TESTS_HOME%"

"%TDI_JAVA_PROGRAM%" "-Dcom.ibm.di.installdir=%TDI_BIN_DIR%\.." "-Dcom.ibm.di.test.runner=com.ibm.di.test.runner.PerfFrameworkRunner" -jar "%UNIT_TESTS_HOME%\boot.jar" %* -ctx=ext

endlocal
