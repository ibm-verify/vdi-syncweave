@echo off
IF {%1}=={} GOTO Usage
IF {%2}=={} GOTO Usage
IF {%3}=={} GOTO Usage
sc.exe create "%1" binPath= "%2" DisplayName= %3
GOTO End
:Usage
echo Usage: installService.bat [servicename] [fullPathToServiceExe] [displayName]
:End
