:: IBM_PROLOG_BEGIN_TAG
::
:: %I%, %G%
::
:: Licensed Materials - Property of IBM
::
:: Restricted Materials of IBM
::
:: (C) COPYRIGHT International Business Machines Corp. 2009, 2010
:: All Rights Reserved
::
:: US Government Users Restricted Rights - Use, duplication or
:: disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
::
:: IBM_PROLOG_END_TAG
@echo off

setlocal

set TEMP_BIN_DIR=%~d0%~p0

call "%TEMP_BIN_DIR%\setupCmdLine.bat"
call "%TEMP_BIN_DIR%\defaultSolDir.bat"

rem ### BACKUP SCRIPT FOR BACKING UP TDI RELATED FILES AND FOLDERS ###
rem ------------------------------------------------------------------

if "x%1" == "x" goto :defaultDir
if not "%1" == "-d" goto usage
if x%2==x goto usage

set DEST=%2
goto begin

:defaultDir
SET DEST="%TDI_HOME_DIR%\backup_tdi"


:begin
rem ### Create Backup Directory ###
rem -------------------------------
if exist %DEST% goto :server
mkdir %DEST%


:server
REM ############################
REM # Backup server components #
REM ############################
echo Backing up server...
if exist "%TDI_HOME_DIR%\serverapi\testadmin.jks"        copy /V /Y "%TDI_HOME_DIR%\serverapi\testadmin.jks" %DEST%
if exist "%TDI_HOME_DIR%\serverapi\testadmin.der"        copy /V /Y "%TDI_HOME_DIR%\serverapi\testadmin.der" %DEST%
if exist "%TDI_HOME_DIR%\serverapi\registry.enc"         copy /V /Y "%TDI_HOME_DIR%\serverapi\registry.enc" %DEST%
if exist "%TDI_HOME_DIR%\serverapi\registry.txt"         copy /V /Y "%TDI_HOME_DIR%\serverapi\registry.txt" %DEST%
if exist "%TDI_HOME_DIR%\idisrv.sth"                     copy /V /Y "%TDI_HOME_DIR%\idisrv.sth" %DEST%
if exist "%TDI_HOME_DIR%\testserver.jks"                 copy /V /Y "%TDI_HOME_DIR%\testserver.jks" %DEST%
if exist "%TDI_HOME_DIR%\testserver.der"                 copy /V /Y "%TDI_HOME_DIR%\testserver.der" %DEST%
if exist "%TDI_HOME_DIR%\etc\global.properties"          copy /V /Y "%TDI_HOME_DIR%\etc\global.properties" %DEST%
if exist "%TDI_HOME_DIR%\etc\reconnect.rules"            copy /V /Y "%TDI_HOME_DIR%\etc\reconnect.rules" %DEST%
if exist "%TDI_HOME_DIR%\etc\derby.properties"           copy /V /Y "%TDI_HOME_DIR%\etc\derby.properties" %DEST%
if exist "%TDI_HOME_DIR%\etc\jlog.properties"            copy /V /Y "%TDI_HOME_DIR%\etc\jlog.properties" %DEST%
if exist "%TDI_HOME_DIR%\etc\log4j.properties"           copy /V /Y "%TDI_HOME_DIR%\etc\log4j.properties" %DEST%
if exist "%TDI_HOME_DIR%\etc\log4j2.xml"                 copy /V /Y "%TDI_HOME_DIR%\etc\log4j2.xml" %DEST%
if exist "%TDI_HOME_DIR%\etc\tdisrvctl-log4j.properties" copy /V /Y "%TDI_HOME_DIR%\etc\tdisrvctl-log4j.properties" %DEST%
if exist "%TDI_HOME_DIR%\configs"                        mkdir %DEST%/configs
if exist "%TDI_HOME_DIR%\configs"                        xcopy /V /Y /I /K /E "%TDI_HOME_DIR%\configs" %DEST%\configs
if exist %TDI_SOLDIR%\solution.properties            	 copy /V /Y %TDI_SOLDIR%\solution.properties %DEST%
if exist "%TDI_HOME_DIR%\etc\tdimiggbl-log4j.properties" copy /V /Y "%TDI_HOME_DIR%\etc\tdimiggbl-log4j.properties" %DEST%
if exist "%TDI_HOME_DIR%\etc\updateinstaller-log4j.properties" copy /V /Y "%TDI_HOME_DIR%\etc\updateinstaller-log4j.properties" %DEST%
if exist "%TDI_HOME_DIR%\etc\it_registry.properties" 	 copy /V /Y "%TDI_HOME_DIR%\etc\it_registry.properties" %DEST%
if exist "%TDI_HOME_DIR%\etc\tp.xml" 					 copy /V /Y "%TDI_HOME_DIR%\etc\tp.xml" %DEST%
if exist "%TDI_HOME_DIR%\etc\activemq.xml"				 copy /V /Y "%TDI_HOME_DIR%\etc\activemq.xml" %DEST%

if exist "%TDI_HOME_DIR%\osgi" (
	mkdir %DEST%/osgi
	xcopy /V /Y /I /K /E "%TDI_HOME_DIR%\osgi" %DEST%\osgi
)
if exist "%TDI_HOME_DIR%\SCIM" (
	mkdir %DEST%/SCIM
	xcopy /V /Y /I /K /E "%TDI_HOME_DIR%\SCIM" %DEST%\SCIM
)
if exist "%TDI_HOME_DIR%\LDAPSync" (
	mkdir %DEST%/LDAPSync
	xcopy /V /Y /I /K /E "%TDI_HOME_DIR%\LDAPSync" %DEST%\LDAPSync
)



:ce
REM ############################
REM # Backup CE components     #
REM ############################
if not exist "%TDI_HOME_DIR%\ce" goto :amc
echo Backing up CE...
if not exist %DEST%/backup_ce mkdir %DEST%/backup_ce
if exist "%TDI_HOME_DIR%\ce\eclipsece\configuration" xcopy /V /Y /I /K /E "%TDI_HOME_DIR%\ce\eclipsece\configuration" %DEST%\backup_ce
if exist "%TDI_HOME_DIR%\ce\eclipsece\workspace"     xcopy /V /Y /I /K /E "%TDI_HOME_DIR%\ce\eclipsece\workspace" %DEST%\backup_ce

if not exist "%TDI_HOME_DIR%\ce\eclipsece\configuration\.settings\org.eclipse.ui.ide.prefs" goto :amc
REM backup workspaces
for /f "tokens=2* delims== usebackq" %%X in (`findstr "^RECENT_WORKSPACES=" "%TDI_HOME_DIR%\ce\eclipsece\configuration\.settings\org.eclipse.ui.ide.prefs"`) do set rawwrkspcs=%%X
for /f "delims=| usebackq" %%X in (`cscript "%TDI_BIN_DIR%\manipulate_workspaces.vbs" "%rawwrkspcs%"`) do set workspaces=%%X

:parse
for /f "delims=? tokens=1,2*" %%X in ("%workspaces%") do (set cur_workspace=%%X& set workspaces=%%Y)
echo "%cur_workspace%" | find "%TDI_HOME_DIR%"
if not %ERRORLEVEL% == 0 goto :next
if exist "%cur_workspace%" xcopy /V /Y /I /K /E "%cur_workspace%" %DEST%\backup_ce
:next
if not "x%workspaces%"=="x" goto parse


:amc
REM ############################
REM # Backup AMC components    #
REM ############################
if not exist "%TDI_HOME_DIR%\bin\amc" goto :MQePWStore
echo Backing up AMC...


call "%TDI_HOME_DIR%\bin\amc\backupam.bat" -d %DEST%
call "%TDI_HOME_DIR%\bin\amc\backupamc.bat" -d %DEST%

set AMC_PROPS_FILE=%TDI_HOME_DIR%\lwi\runtime\isc\eclipse\plugins\AMC_7.0.0\amc.properties
if not exist "%AMC_PROPS_FILE%" set AMC_PROPS_FILE=%TDI_HOME_DIR%\lwi\runtime\isc\eclipse\plugins\AMC_7.1.0\amc.properties
if not exist "%AMC_PROPS_FILE%" set AMC_PROPS_FILE=%TDI_HOME_DIR%\lwi\runtime\isc\eclipse\plugins\AMC_7.1.1.0\amc.properties
if not exist "%AMC_PROPS_FILE%" set AMC_PROPS_FILE=%TDI_HOME_DIR%\lwi\runtime\isc\eclipse\plugins\AMC_7.2.0.0\amc.properties
if not exist "%AMC_PROPS_FILE%" goto :MQePWStore

call "%TDI_HOME_DIR%\bin\amc\backupamcdb.bat" -d %DEST% -p "%AMC_PROPS_FILE%"


:MQePWStore
REM ############################
REM # Backup MQePWStore files  #
REM ############################
set MQE_PROPS=%TDI_HOME_DIR%\jars\plugins\mqeconfig.props
if not exist "%MQE_PROPS%" goto :TDISysStore
echo Backing up MQePWStore...
for /f "tokens=2* delims== usebackq" %%X in (`findstr "^serverRootFolder=" "%MQE_PROPS%"`) do set MQE_LOC=%TDI_HOME_DIR%\%%X
if not exist "%MQE_LOC%" goto :TDISysStore
if not exist %DEST%\backup_systemqueue mkdir %DEST%\backup_systemqueue
xcopy /V /Y /I /K /E "%MQE_LOC%" %DEST%\backup_systemqueue

:TDISysStore
REM ############################
REM # Backup TDISysStore files #
REM ############################
echo Backing up TDISysStore...
for /f "tokens=2* delims== usebackq" %%X in (`findstr "^com.ibm.di.store.database=" "%TDI_HOME_DIR%\etc\global.properties"`) do set TDISTORE_LOC=%%X
for /f "delims=| usebackq" %%X in (`call cscript "%TDI_BIN_DIR%\manipulate_store.vbs" "%TDISTORE_LOC%"`) do set TDISTORE_LOC=%%X
if not exist "%TDISTORE_LOC%" goto :plugins
if not exist %DEST%\backup_systemstore mkdir %DEST%\backup_systemstore
xcopy /V /Y /I /K /E "%TDISTORE_LOC%" %DEST%\backup_systemstore



:plugins
REM ############################
REM # Backup Plugins files     #
REM ############################
echo Backing up plugins...
SET DEST=%DEST%\backup_plugins
if not exist %DEST% mkdir %DEST%
if not exist %DEST%\pam mkdir %DEST%\pam
if not exist %DEST%\sun mkdir %DEST%\sun
if not exist %DEST%\tds mkdir %DEST%\tds
if not exist %DEST%\windows mkdir %DEST%\windows
if not exist %DEST%\domino mkdir %DEST%\domino
if exist "%TDI_HOME_DIR%\pwd_plugins\pam\pwsync.props"     copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\pam\pwsync.props"     %DEST%\pam
if exist "%TDI_HOME_DIR%\pwd_plugins\sun\pwsync.props"     copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\sun\pwsync.props"     %DEST%\sun
if exist "%TDI_HOME_DIR%\pwd_plugins\tds\pwsync.props"     copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\tds\pwsync.props"     %DEST%\tds
if exist "%TDI_HOME_DIR%\pwd_plugins\windows\pwsync.props" copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\windows\pwsync.props" %DEST%\windows
if exist "%TDI_HOME_DIR%\pwd_plugins\domino\pwsync.props" copy /V /Y "%TDI_HOME_DIR%\pwd_plugins\domino\pwsync.props" %DEST%\domino

goto :end

:usage
echo Utility to backup TDI files
echo. 
echo Usage: tdiBackup.bat -d folder_to_create_backup_in
echo. 
echo The archived info is created in the backup folder


:end

endlocal
