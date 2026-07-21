@echo off


rem ### Restore TDI v7.0 CE user data ###
rem ------------------------------------------------

if '%1' == '/?' goto end
if '%1.'== '.'  goto end

SET TDI_HOME_DIR=%1
SET SRC=%TDI_HOME_DIR%\backup_tdi\backup_ce

REM Restore previous release configuration files & workspaces to current release - Assuming we already backedup current release files

if exist "%SRC%\configuration" xcopy /V /Y /I /K /E "%SRC%\configuration" "%TDI_HOME_DIR%\ce\eclipsece\configuration"
REM if exist "%DEST%\workspace"     xcopy /V /Y /I /K /E "%DEST%\workspace" "%TDI_HOME_DIR%\ce\eclipsece\workspace"

if not exist "%TDI_HOME_DIR%\ce\eclipsece\configuration\.settings\org.eclipse.ui.ide.prefs" goto :end
REM restore workspaces
for /f "tokens=2* delims== usebackq" %%X in (`findstr "^RECENT_WORKSPACES=" "%TDI_HOME_DIR%\ce\eclipsece\configuration\.settings\org.eclipse.ui.ide.prefs"`) do set rawwrkspcs=%%X
for /f "delims=| usebackq" %%X in (`cscript "%TDI_HOME_DIR%\bin\manipulate_workspaces.vbs" "%rawwrkspcs%"`) do set workspaces=%%X

:parse
for /f "delims=? tokens=1,2*" %%X in ("%workspaces%") do (set cur_workspace=%%X& set workspaces=%%Y)
echo "%cur_workspace%" | find "%TDI_HOME_DIR%"
if not %ERRORLEVEL% == 0 goto :next
if exist "%cur_workspace%" xcopy /V /Y /I /K /E "%SRC%/workspace" "%cur_workspace%" 
:next
if not "x%workspaces%"=="x" goto parse


:end
