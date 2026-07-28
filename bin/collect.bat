:: IBM_PROLOG_BEGIN_TAG
::
:: %I%, %G%
::
:: Licensed Materials - Property of IBM
::
:: Restricted Materials of IBM
::
:: (C) COPYRIGHT International Business Machines Corp. 2007, 2010
:: All Rights Reserved
::
::
:: IBM_PROLOG_END_TAG

@echo off

setlocal

set TEMP_BIN_DIR=%~d0%~p0

set SKIP_ISCDIR_SETUP=1
call "%TEMP_BIN_DIR%\setupCmdLine.bat"

if "%1"=="/?" goto usage

if "%1" == "" goto copyinst
if NOT "%1" == "-s" goto usage
if "%2" == "" goto usage
SET solndir="%2"



:copyinst
SET DEST=traceDumps
mkdir %DEST%
FOR /F "eol=#" %%i IN (filelist.txt) DO xcopy /KCQO %%i %DEST%
rem we cannot use /S with files. xcopy cribs.
FOR /F "eol=#" %%i IN (dirlist.txt) DO xcopy /SKCQO %%i %DEST%

if defined solndir xcopy /KECQYO %solndir% %DEST%
exit /b


:usage
echo.
echo.
echo This script collects serviceability info to a directory called traceDumps. 
echo The traceDumps will be created under install directory. 
echo filelist.txt and dirlist.txt under the install directory is used to 
echo specify the list of files and directories that you want copied.
echo. 
echo.
echo Usage: %~n0 [-s solndir_path
]

echo.
echo This script should be run from the install directory.

endlocal
