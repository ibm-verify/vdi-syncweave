@echo off


rem ### Backup TDI v7.0 plugins user data ###
rem ------------------------------------------------

if '%1' == '/?' goto usage
if '%1.'== '.'  goto usage


SET TDI_HOME_DIR="%1"
SET DEST="%TDI_HOME_DIR%\backup_tdi"

if exist "%DEST%" goto :plugins
mkdir "%DEST%"

:plugins
rem Backing up plugins...
rem ---------------------
if exist "%TDI_HOME_DIR%\pwd_plugins\pam\pwsync.props"     copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\pam\pwsync.props"     "%DEST%\pwsync.props.pam"
if exist "%TDI_HOME_DIR%\pwd_plugins\sun\pwsync.props"     copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\sun\pwsync.props"     "%DEST%\pwsync.props.sun"
if exist "%TDI_HOME_DIR%\pwd_plugins\tds\pwsync.props"     copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\tds\pwsync.props"     "%DEST%\pwsync.props.tds"
if exist "%TDI_HOME_DIR%\pwd_plugins\windows\pwsync.props" copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\windows\pwsync.props" "%DEST%\pwsync.props.windows"
if exist "%TDI_HOME_DIR%\pwd_plugins\domino\pwsync.props" copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\domino\pwsync.props" "%DEST%\pwsync.props.domino"


:usage
echo.
echo.
echo This script makes copies of TDI plugins Files that may have been altered/Set by users.
echo.
echo.
echo Usage: %~n0 TDI_Install_Folder

