@echo off


rem ### Backup TDI v6.X.X Server user data ###
rem ------------------------------------------------

if '%1' == '/?' goto usage
if '%1.'== '.'  goto usage

@REM %2=60 61 611 or 70
if '%2' == '60'  goto begin
if '%2' == '61'  goto begin61X
if '%2' == '611' goto begin61X
if '%2' == '70' goto begin61X

goto usage

:begin
@REM Create etc if migrating from v60
mkdir %1\etc
copy /Y /V %1\global.properties %1\etc\global.properties.v%2
goto next


:begin61X
@REM backup global.properties
copy /Y /V %1\etc\global.properties %1\etc\global.properties.v%2

:next
@REM backup other important files
copy /Y /V %1\serverapi\testadmin.jks %1\serverapi\testadmin.jks.v%2
copy /Y /V %1\serverapi\testadmin.der %1\serverapi\testadmin.der.v%2
copy /Y /V %1\serverapi\registry.enc %1\serverapi\registry.enc.v%2
copy /Y /V %1\serverapi\registry.txt %1\serverapi\registry.txt.v%2
copy /Y /V %1\idisrv.sth %1\idisrv.sth.v%2
copy /Y /V %1\testserver.jks %1\testserver.jks.v%2
copy /Y /V %1\testserver.der %1\testserver.der.v%2

if '%2' == '60' goto end

@REM This must be v6.1.X or 7.x
copy /Y /V %1\etc\reconnect.rules %1\etc\reconnect.rules.v%2
copy /Y /V %1\etc\derby.properties %1\etc\derby.properties.v%2
copy /Y /V %1\etc\jlog.properties %1\etc\jlog.properties.v%2
copy /Y /V %1\etc\log4j.properties %1\etc\log4j.properties.v%2
copy /Y /V %1\etc\tdisrvctl-log4j.properties %1\etc\tdisrvctl-log4j.properties.v%2

if '%2' == '61' goto end
if '%2' == '70' goto extra

@REM 611 specific stuff
copy /Y /V %1\etc\act-jlog.properties %1\etc\act-jlog.properties.v%2

goto end

:extra
@REM 70 specific backup 

SET TDI_HOME_DIR=%1
if exist "%TDI_HOME_DIR%\solution.properties"            copy /V /Y "%TDI_HOME_DIR%\solution.properties" "%TDI_HOME_DIR%\solution.properties.v%2"

:usage
echo.
echo.
echo This script makes copies of TDI Server files that may have been altered by users.
echo.
echo.
echo Usage: %~n0 TDI_Install_Folder 60 ^| 61 ^| 611 ^|70



:end

