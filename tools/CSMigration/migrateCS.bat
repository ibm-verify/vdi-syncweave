@echo off

setlocal

set TEMP_BIN_DIR=%~d0%~p0..\..\bin

set SKIP_ISCDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

rem ### MIGRATE SCRIPT FOR Cloudscape v5 to v10 ###
rem ------------------------------------------------

if '%1' == '/?' goto usage
if '%1.'== '.' goto usage
if '%2.' == '.' goto usage

rem ### Set Variables ###
rem ---------------------
SET OLD_DB_PATH=%1
SET NEW_DB_PATH=%2
SET CLASSPATH=%TDI_HOME_DIR%\tools\CSMigration\migratetoderby.jar;%TDI_HOME_DIR%\jars\3rdparty\IBM\derby.jar


rem ### Check existence of DB Directory ###
rem -----------------------------------------
if not exist %OLD_DB_PATH% goto oldDBNotFound


rem ### Call Migrate Utility ###
rem -------------------------------
"%TDI_JAVA_PROGRAM%" -Ddb2j.migrate.ddlOnly=false -Ddb2j.migrate.appendLog=false -Ddb2j.migrate.verbose=true -Ddb2j.migrate.newDBURL=jdbc:derby:%NEW_DB_PATH% -Ddb2j.migrate.migrateLog=migrate.log -Ddb2j.migrate.debugLog=debug.log com.ibm.db2j.tools.MigrateFrom51 jdbc:db2j:%OLD_DB_PATH%



rem ### Migration completed
rem -----------------------
exit /b


:oldDBNotFound
echo.
echo.
echo.Unable to find Cloudscape directory %OLD_DB_PATH%
echo.Check if directory path is correct.
echo.
exit /b -2


:usage
echo.
echo.
echo This script calls the CS migration utility. 
echo.
echo.
echo Usage: %~n0 [Path_Of_CloudscapeV5_folder] [Path_Of_New_CloudscapeV10_Folder_To_Create]

echo.
echo. 

endlocal
