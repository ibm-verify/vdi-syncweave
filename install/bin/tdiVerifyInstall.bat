::
:: IBM Confidential
:: PID 5724-K74
::
:: . 2009, 2025
:: All Rights Reserved
::
:: INSTALL VERIFY SCRIPT TDI RELATED COMPONENTS
:: --------------------------------------------
@echo off

setlocal

set TEMP_BIN_DIR=%~d0%~p0

set SKIP_SOLDIR_SETUP=1
set SKIP_ISCDIR_SETUP=1

call "%TEMP_BIN_DIR%\setupCmdLine.bat"

REM ### Checking OS architecture is 32bit or 64bit
IF "%PROCESSOR_ARCHITECTURE%"=="x86" (set ARCH=x86) else (set ARCH=x64)

REM ### Check if Server installed preoperly ###

set SERVERFIND=0
FOR /F "tokens=1 delims=\n" %%A IN ('FINDSTR "<SERVER>" "%TDI_HOME_DIR%\.registry"') DO IF "%%A"=="<SERVER>" set SERVERFIND=1

if	%SERVERFIND%==1	goto :Server
if	%SERVERFIND%==0	goto :CE

:Server
REM ###Check for TDI Server files and jars###
REM ###Dirs####
if not exist "%TDI_HOME_DIR%\xsl"								goto :fail
if not exist "%TDI_HOME_DIR%\jars\3rdparty"						goto :fail
if not exist "%TDI_HOME_DIR%\jars\3rdparty\IBM"					goto :fail
if not exist "%TDI_HOME_DIR%\jars\3rdparty\others"				goto :fail
if not exist "%TDI_HOME_DIR%\jars\connectors"					goto :fail
if not exist "%TDI_HOME_DIR%\jars\functions"					goto :fail
if not exist "%TDI_HOME_DIR%\jars\parsers"						goto :fail
if not exist "%TDI_HOME_DIR%\osgi"								goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins"	 					goto :fail 
if not exist "%TDI_HOME_DIR%\SCIM"								goto :fail
if not exist "%TDI_HOME_DIR%\LDAPSync"							goto :fail
REM ###Files####
if not exist "%TDI_HOME_DIR%\ibmdisrv.bat"   					goto :fail
if not exist "%TDI_HOME_DIR%\IDILoader.jar"						goto :fail
if not exist "%TDI_HOME_DIR%\idisrv.sth"						goto :fail
if not exist "%TDI_HOME_DIR%\testserver.jks"					goto :fail
if not exist "%TDI_HOME_DIR%\testserver.der"					goto :fail
if not exist "%TDI_HOME_DIR%\etc\global.properties"				goto :fail
if not exist "%TDI_HOME_DIR%\bin\tdisrvctl.bat"					goto :fail
if not exist "%TDI_HOME_DIR%\bin\tdimiggbl.bat"					goto :fail
if not exist "%TDI_HOME_DIR%\libs\COMProxy.dll"					goto :fail
if not exist "%TDI_HOME_DIR%\libs\WindowsUsers.dll"				goto :fail
if not exist "%TDI_HOME_DIR%\serverapi\cryptoutils.bat"   		goto :fail
if not exist "%TDI_HOME_DIR%\serverapi\registry.enc"			goto :fail
if not exist "%TDI_HOME_DIR%\serverapi\registry.txt"   			goto :fail
if not exist "%TDI_HOME_DIR%\jars\common\diserverapi.jar"		goto :fail
if not exist "%TDI_HOME_DIR%\jars\common\miserver.jar"			goto :fail
if not exist "%TDI_HOME_DIR%\jars\common\mmconfig.jar"			goto :fail
if not exist "%TDI_HOME_DIR%\jars\common\miconfig.jar"			goto :fail
if not exist "%TDI_HOME_DIR%\jars\common\diserverapirmi.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\jars\common\tdiresource.jar"		goto :fail
if not exist "%TDI_HOME_DIR%\jars\common\miggbl.jar"			goto :fail
if not exist "%TDI_HOME_DIR%\jars\common\cli.jar"				goto :fail
if not exist "%TDI_HOME_DIR%\tools\CSMigration\migratetoderby.jar" goto :fail
if not exist "%TDI_HOME_DIR%\win32_service\ibmdiservice.exe"	goto :fail
if not exist "%TDI_HOME_DIR%\maintenance\UpdateInstaller.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\LDAPSync\LDAPSync.xml"				goto :fail
if not exist "%TDI_HOME_DIR%\SCIM\SCIM.xml"						goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.api.bind.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.api.connection.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.api.impl.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.api.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.api.rest.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.bundle.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.component.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.config.bind.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.config.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.connector.taddm.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.http.jetty.listener.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.jaxrs.impl.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.jaxrs.jackson.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.jaxrs.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.jaxrs.storage.atom.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.log.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.log.slf4j.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.log.slf4j-tdi.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.schema.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.server.entry.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.server.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.systemqueue.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.ui.easyetl.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.ui.webui.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.util.jar"	goto :fail
if not exist "%TDI_HOME_DIR%\osgi\plugins\com.ibm.di.web.common.jar"	goto :fail
REM ##SOL_DIR files not created just after install#### 
:CE

set CEFIND=0
FOR /F "tokens=1 delims=\n" %%A IN ('FINDSTR "<CE>" "%TDI_HOME_DIR%\.registry"') DO IF "%%A"=="<CE>" set CEFIND=1

if	%CEFIND%==1	goto :CheckCE
if	%CEFIND%==0 goto :amc
REM ### Check if CE installed preoperly ###

:CheckCE
REM ###Check for TDI CE files and jars###
if not exist "%TDI_HOME_DIR%\ibmditk.bat" 											goto :fail
if not exist "%TDI_HOME_DIR%\ce\eclipsece\miadmin.exe"   							goto :fail
if not exist "%TDI_HOME_DIR%\ce\eclipsece\miadmin.ini"   							goto :fail
if not exist "%TDI_HOME_DIR%\ce\eclipsece\plugins\com.ibm.tdi.rcp_11.0.0.1.jar"   	goto :fail
if not exist "%TDI_HOME_DIR%\ce\eclipsece\plugins\com.ibm.tdi.loader_11.0.0.1.jar"   goto :fail

:amc

set AMCFIND=0
FOR /F "tokens=1 delims=\n" %%A IN ('FINDSTR "<AMC>" "%TDI_HOME_DIR%\.registry"') DO IF "%%A"=="<AMC>" set AMCFIND=1

if %AMCFIND%==1	goto :CheckAMC
if %AMCFIND%==0 goto :ceupdate

REM ###Check for TDI AMC files and jars###
:CheckAMC
REM ###Check AMC Files and jars.#####
if not exist "%TDI_HOME_DIR%\bin\amc\start_tdiamc.bat"				goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\startNetworkServer.bat"		goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\startAM.bat"					goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\install.bat"					goto :fail

if not exist "%TDI_HOME_DIR%\amc\tdiamc.war"										goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\ActionManager\jars\action_manager.jar"			goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\ActionManager\jars\tdiresource.jar"			goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\ActionManager\jars\db2jcc.jar"					goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\ActionManager\jars\derby.jar"					goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\ActionManager\jars\derbyclient.jar"			goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\ActionManager\jars\derbynet.jar"				goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\ActionManager\jars\derbytools.jar"				goto :fail
if not exist "%TDI_HOME_DIR%\bin\amc\ActionManager\am_config.properties"			goto :fail


REM ###Checking subcomponents######
:ceupdate

set UPDATEFIND=0
FOR /F "tokens=1 delims=\n" %%A IN ('FINDSTR "UPDATE" "%TDI_HOME_DIR%\.registry"') DO IF "%%A"=="<CE UPDATE>" set UPDATEFIND=1

if %UPDATEFIND%==1 goto :Checkceupdate
if %UPDATEFIND%==0 goto :checkPlugin

:Checkceupdate
if not exist "%TDI_HOME_DIR%\ce\update_site\site.xml"											goto :fail
if not exist "%TDI_HOME_DIR%\ce\update_site\features\com.ibm.tdi.feature_11.0.0.1.jar"			goto :fail
if not exist "%TDI_HOME_DIR%\ce\update_site\plugins"											goto :fail

:checkPlugin

set PLUGFIND=0
FOR /F "tokens=1 delims=\n" %%A IN ('FINDSTR "<PLUGINS>" "%TDI_HOME_DIR%\.registry"') DO IF "%%A"=="<PLUGINS>" set PLUGFIND=1

if %PLUGFIND%==1 goto :Plugins
if %PLUGFIND%==0 goto :last

:Plugins
if not exist "%TDI_HOME_DIR%\pwd_plugins\jars\proxy.jar"							goto :fail
if not exist "%TDI_HOME_DIR%\pwd_plugins\bin\encryptPasswd.bat"						goto :fail
if not exist "%TDI_HOME_DIR%\pwd_plugins\bin\startProxy.bat"						goto :fail
if "%ARCH%"=="x86" (
	if not exist "%TDI_HOME_DIR%\pwd_plugins\windows\pwsync_admin.exe"				goto :fail
) else (
	if not exist "%TDI_HOME_DIR%\pwd_plugins\windows\pwsync_admin_64.exe"			goto :fail
)
:last

:Success
echo 0
goto :end

:fail
echo 1

:end

endlocal
