@echo off

if "%1" == "" goto usage
if "%2" == "" goto usage
if not "%2" == "start" if not "%2" == "stop" goto usage

if "%2"=="start" sc start %1
if "%2"=="stop" sc stop %1
goto end

:usage
echo "Usage: %0 <service_name> start|stop"

:end