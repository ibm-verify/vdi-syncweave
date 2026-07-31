@echo off
IF {%1}=={} GOTO Usage
sc.exe delete "%1"
GOTO End
:Usage
echo Usage: uninstallService.bat [servicename]
:End
