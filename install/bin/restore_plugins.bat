@echo off


rem ### Restore TDI Plug-in user data ###
rem ------------------------------------------------

if '%1' == '/?' goto usage
if '%1.'== '.'  goto usage

@REM %2=70
REM if '%2' == '70'  goto migrate
REM goto usage

REM :migrate
@REM Taking backup of 71 pwsync.props 
REM if exist %1\pwd_plugins\pam\pwsync.props     copy /V /Y %1\pwd_plugins\pam\pwsync.props %1\pwd_plugins\pam\pwsync.props.v71
REM if exist %1\pwd_plugins\sun\pwsync.props     copy /V /Y %1\pwd_plugins\sun\pwsync.props %1\pwd_plugins\sun\pwsync.props.v71
REM if exist %1\pwd_plugins\tds\pwsync.props     copy /V /Y %1\pwd_plugins\tds\pwsync.props %1\pwd_plugins\tds\pwsync.props.v71
REM if exist %1\pwd_plugins\windows\pwsync.props     copy /V /Y %1\pwd_plugins\windows\pwsync.props %1\pwd_plugins\windows\pwsync.props.v71
REM if exist %1\pwd_plugins\domino\pwsync.props     copy /V /Y %1\pwd_plugins\domino\pwsync.props %1\pwd_plugins\domino\pwsync.props.v71

@REM Restore previous release plugin configuration files to current release - Assuming we already backedup current release files

SET SRC=%1\backup_tdi\backup_plugins
REM if exist %SRC%\pam\pwsync.props call %1\pwd_plugins\bin\migpwsync.bat -f %SRC%\pam\pwsync.props -n %1\pwd_plugins\pam\pwsync.props
if exist %SRC%\sun\pwsync.props call %1\pwd_plugins\bin\migpwsync.bat -f %SRC%\sun\pwsync.props -n %1\pwd_plugins\sun\pwsync.props
if exist %SRC%\tds\pwsync.props call %1\pwd_plugins\bin\migpwsync.bat -f %SRC%\tds\pwsync.props -n %1\pwd_plugins\tds\pwsync.props
if exist %SRC%\windows\pwsync.props call %1\pwd_plugins\bin\migpwsync.bat -f %SRC%\windows\pwsync.props -n %1\pwd_plugins\windows\pwsync.props
if exist %SRC%\domino\pwsync.props call %1\pwd_plugins\bin\migpwsync.bat -f %SRC%\domino\pwsync.props -n %1\pwd_plugins\domino\pwsync.props


goto end

:usage
echo.
echo.
echo This script restores TDI Plugin files that may have been altered by users.
echo.
echo.
echo Usage: %~n0 TDI_Install_Folder


:end

