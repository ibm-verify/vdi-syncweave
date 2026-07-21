@echo off

:: This BAT builds the service's executable.
:: Procedure:	1. The environment is set with 'vcvarsall.bat';
:: 			    2. The build is performed with 'nmake.exe'.

:: The location of the NMAKE command. Edit it according to your configuration.
set NMAKE_LOCATION=C:\Program Files\Microsoft Visual Studio 9.0\VC\bin

:: The location of the VCVARSALL command. Edit it according to your configuration.
set VCVARSALL_LOCATION=C:\Program Files\Microsoft Visual Studio 9.0\VC

:: The environment parameter used. Either 'x86' (for 32-bit exe) or 'x86_amd64' (for 64-bit exe).
set ENV_PARAM=x86
::set ENV_PARAM=x86_amd64

:: The makefile that will be used for building the ibmdiservice.exe.
set USED_MAKEFILE=Makefile
::set USED_MAKEFILE=Makefile64

:: Setup the environment. 
call "%VCVARSALL_LOCATION%\vcvarsall.bat" %ENV_PARAM%

:: Build the DLL.
"%NMAKE_LOCATION%\nmake.exe" /f %USED_MAKEFILE%