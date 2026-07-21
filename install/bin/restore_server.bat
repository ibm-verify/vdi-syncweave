@echo off


rem ### Restore TDI v6.X.X Server user data ###
rem ### This script also moves current global.properties file to current release
rem ### So this script must be run before the call to tdimiggbl
rem ------------------------------------------------

if '%1' == '/?' goto usage
if '%1.'== '.'  goto usage

@REM %2=60 61 611 or 70
if '%2' == '60'  goto migrate
if '%2' == '61'  goto begin61X
if '%2' == '611' goto begin611
if '%2' == '70' goto :begin7x
if '%2' == '71' goto :begin7x
if '%2' == '711' goto :begin7x
if '%2' == '72' goto :begin7x

goto usage

:begin611
copy /Y /V %1\etc\act-jlog.properties.v%2 %1\etc\act-jlog.properties

:begin61X
copy /Y /V %1\etc\reconnect.rules.v%2 %1\etc\reconnect.rules
copy /Y /V %1\etc\derby.properties.v%2 %1\etc\derby.properties
copy /Y /V %1\etc\jlog.properties.v%2 %1\etc\jlog.properties
copy /Y /V %1\etc\log4j.properties.v%2 %1\etc\log4j.properties
copy /Y /V %1\etc\tdisrvctl-log4j.properties.v%2 %1\etc\tdisrvctl-log4j.properties


@REM Look in global.properties for systemqueue.on=false...if found, we must set up the MQe Server
find "systemqueue.on=false" %1\etc\global.properties.v%2
if ERRORLEVEL 1 goto migrate
call %1\jars\plugins\mqeconfig.bat %1\jars\plugins\mqeconfig.props create server


:migrate
@REM migrate global.properties
copy /Y /V %1\etc\global.properties %1\etc\global.properties.v71
call %1\bin\tdimiggbl.bat -f %1\etc\global.properties.v%2 -n %1\etc\global.properties
goto end

@REM Restore previous release configuration files to current release - Assuming we already backedup current release files
:begin7x
set SRC=%1\backup_tdi
if exist %SRC%\reconnect.rules copy /Y /V %SRC%\reconnect.rules %1\etc\reconnect.rules
if exist %SRC%\derby.properties copy /Y /V %SRC%\derby.properties %1\etc\derby.properties
if exist %SRC%\jlog.properties copy /Y /V %SRC%\jlog.properties %1\etc\jlog.properties
if exist %SRC%\log4j.properties copy /Y /V %SRC%\log4j.properties %1\etc\log4j.properties
if exist %SRC%\tdisrvctl-log4j.properties copy /Y /V %SRC%\tdisrvctl-log4j.properties %1\etc\tdisrvctl-log4j.properties
if not exist %1\configs mkdir %1\configs
if exist %SRC%\configs xcopy /V /Y /I /K /E %SRC%\configs %1\configs
if exist %SRC%\tdimiggbl-log4j.properties copy /Y /V %SRC%\tdimiggbl-log4j.properties %1\etc\tdimiggbl-log4j.properties
if exist %SRC%\updateinstaller-log4j.properties copy /Y /V %SRC%\updateinstaller-log4j.properties %1\etc\updateinstaller-log4j.properties
if exist %SRC%\it_registry.properties copy /Y /V %SRC%\it_registry.properties %1\etc\it_registry.properties
if exist %SRC%\tp.xml copy /Y /V %SRC%\tp.xml %1\etc\tp.xml
if exist %SRC%\activemq.xml copy /Y /V %SRC%\activemq.xml %1\etc\activemq.xml

REM Migrate global.properties
call %1\bin\tdimiggbl.bat -f %SRC%\global.properties -n %1\etc\global.properties

REM Migrate solution.properties

call %1\bin\defaultSolDir.bat
if %TDI_SOLDIR%x == x goto end
if exist %TDI_SOLDIR%\solution.properties DEL /F /Q %TDI_SOLDIR%\solution.properties

if not exist %SRC%\solution.properties goto end
	
call %1\bin\tdimiggbl.bat -f %SRC%\solution.properties -n %TDI_SOLDIR%\solution.properties

goto end


:usage
echo.
echo.
echo This script restores TDI Server files that may have been altered by users.
echo.
echo.
echo Usage: %~n0 TDI_Install_Folder 60 ^| 61 ^| 611 ^| 70 ^| 71


:end

