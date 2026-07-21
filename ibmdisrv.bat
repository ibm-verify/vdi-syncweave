@echo off
setlocal

set TEMP_BIN_DIR=%~d0%~p0bin

set SKIP_ISCDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

set PATH=%TDI_HOME_DIR%;%TDI_JAVA_BIN_DIR%;%TDI_LIB_DIR%;%PATH%

rem Get solution directory parameter (overrides TDI_SOLDIR)
:checksol
if .%1==.-s (
	rem Make sure we are on the correct drive
	%~d2
	rem At this point overwrite the TDI_SOLDIR
	set TDI_SOLDIR=%2
	goto changedir
)
shift
if not .%1==. goto checksol

:changedir
rem first remove quotes, as double quotes will be bad
set TDI_SOLDIR_TMP=###%TDI_SOLDIR%###
set TDI_SOLDIR_TMP=%TDI_SOLDIR_TMP:"###=%
set TDI_SOLDIR_TMP=%TDI_SOLDIR_TMP:###"=%
set TDI_SOLDIR=%TDI_SOLDIR_TMP:###=%

rem Create the directory if it does not exist
if not exist "%TDI_SOLDIR%" mkdir "%TDI_SOLDIR%"
rem CD into solution directory
call "%TDI_BIN_DIR%\ibmdicwd" "%TDI_SOLDIR%"

:execute
if not exist logs mkdir logs

rem Always add the Solution Directory libs dir to the path
set PATH=%TDI_SOLDIR%\libs;%PATH%

rem Take the supported env variables and pass them to Java program

rem Log4j2 configuration file
set LOG_4J="-Dlog4j2.configurationFile=file:///%TDI_SOLDIR:\=/%/etc/log4j2.xml"

rem Java Util Logging configuration
set JAVA_LOGGING="-Djava.util.logging.config.file=%TDI_SOLDIR%\etc\logging.properties"

set ENV_VARIABLES=%LOG_4J% %JAVA_LOGGING%
set JMX=-Dcom.sun.management.jmxremote=false -Dcom.sun.management.jmxremote.local.only=true -Dcom.sun.management.jmxremote.host=localhost

"%TDI_JAVA_PROGRAM%" %JMX% -Dosgi.serviceloader.processor=true -classpath "%TDI_HOME_DIR%\IDILoader.jar" %ENV_VARIABLES% --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-exports java.base/sun.security.util=ALL-UNNAMED com.ibm.di.loader.ServerLauncher %*
set RC=%ERRORLEVEL%

exit /b %RC%

endlocal
