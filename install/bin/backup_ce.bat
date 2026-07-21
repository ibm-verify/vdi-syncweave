@echo off

rem ### Backup TDI v7.0 CE user data ###
rem ------------------------------------------------

if '%1' == '/?' goto usage
if '%1.'== '.'  goto usage

SET TDI_HOME_DIR=%1
SET DEST=%TDI_HOME_DIR%\backup_tdi


if exist "%DEST%" goto :ce
mkdir "%DEST%"

:ce

if not exist "%TDI_HOME_DIR%\ce" goto :end
if exist "%TDI_HOME_DIR%\ce\eclipsece\configuration" xcopy /V /Y /I /K /E "%TDI_HOME_DIR%\ce\eclipsece\configuration" "%DEST%"
if exist "%TDI_HOME_DIR%\ce\eclipsece\workspace"     xcopy /V /Y /I /K /E "%TDI_HOME_DIR%\ce\eclipsece\workspace" "%DEST%"

if not exist "%TDI_HOME_DIR%\ce\eclipsece\configuration\.settings\org.eclipse.ui.ide.prefs" goto :end
REM backup workspaces
for /f "tokens=2* delims== usebackq" %%X in (`findstr "^RECENT_WORKSPACES=" "%TDI_HOME_DIR%\ce\eclipsece\configuration\.settings\org.eclipse.ui.ide.prefs"`) do set rawwrkspcs=%%X
for /f "delims=| usebackq" %%X in (`cscript "%TDI_BIN_DIR%\manipulate_workspaces.vbs" "%rawwrkspcs%"`) do set workspaces=%%X

:parse
for /f "delims=? tokens=1,2*" %%X in ("%workspaces%") do (set cur_workspace=%%X& set workspaces=%%Y)
echo "%cur_workspace%" | find "%TDI_HOME_DIR%"
if not %ERRORLEVEL% == 0 goto :next
if exist "%cur_workspace%" xcopy /V /Y /I /K /E "%cur_workspace%" "%DEST%"
:next
if not "x%workspaces%"=="x" goto parse


:usage
echo.
echo.
echo This script makes copies of TDI CE Files that may have been altered/Set by users.
echo.
echo.
echo Usage: %~n0 TDI_Install_Folder


:end