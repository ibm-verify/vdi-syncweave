@echo off

:: This BAT builds the needed DLL.
:: Procedure:	1. the environment is set with 'vcvarsall.bat';
:: 			2. the build is performed with 'nmake.exe'.
:: By default a 32-bit dll is created. In order to build a 64-bit dll you need to:
::			1. Modify the ENV_PARAM;
::			2. Modify the USED_MAKEFILE.

:: The location of adks on your machine. Edit it according to your configuration.
set ADKS_ROOT="D:\CMVC\adks"

:: The location of the NMAKE command. Edit it according to your configuration.
set NMAKE_LOCATION=C:\Program Files\Microsoft Visual Studio 9.0\VC\bin

:: The location of the VCVARSALL command. Edit it according to your configuration.
set VCVARSALL_LOCATION=C:\Program Files\Microsoft Visual Studio 9.0\VC

:: The environment parameter used. Either 'x86' (for 32-bit dll) or 'x86_amd64' (for 64-bit dll).
set ENV_PARAM=x86
::set ENV_PARAM=x86_amd64

:: The makefile that will be used for building the dll. Use 'Makefile' for 32-bit dll or 'Makefile64' for 64-bit.
set USED_MAKEFILE=Makefile
::set USED_MAKEFILE=Makefile64


:: Setup the environment. 
call "%VCVARSALL_LOCATION%\vcvarsall.bat" %ENV_PARAM%

:: Build the DLL.
"%NMAKE_LOCATION%\nmake.exe" /f %USED_MAKEFILE%