@echo off
setlocal

set TEMP_BIN_DIR=%~d0%~p0

"%TEMP_BIN_DIR%\..\ibmdisrv.bat" -Y
