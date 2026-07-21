@echo off
setlocal

set UNIT_TESTS_HOME=%~d0%~p0..
set TEMP_BIN_DIR=%~d0%~p0..\..\bin

set SKIP_ISCDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

if not "%TDI_BIN_DIR%"=="%TDI_SOLDIR%" copy "%TDI_BIN_DIR%\perf.properties" "%TDI_SOLDIR%\perf.properties"

"%TDI_BIN_DIR%\ibmdisrv.bat" -c "%UNIT_TESTS_HOME%\configs\perf\BasePerfTestingAL.xml" -r BasePerfAL

endlocal
