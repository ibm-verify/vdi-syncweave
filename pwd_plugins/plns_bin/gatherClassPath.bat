:: IBM_PROLOG_BEGIN_TAG
::
:: %I%, %G%
::
:: Licensed Materials - Property of IBM
::
:: Restricted Materials of IBM
::
:: (C) COPYRIGHT International Business Machines Corp. 2008, 2013
:: All Rights Reserved
::
:: US Government Users Restricted Rights - Use, duplication or
:: disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
::
:: IBM_PROLOG_END_TAG

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: This script is used to gather all of the jars in the provided as variable PWS_JARS_DIR.
:: The gathering is done recursively. The classpath is then set in the PWS_CLASSPATH
:: variable.
::
:: This script checks for the existence of the following environment variables:
::   PWS_JARS_DIR - if not provided then the script will use the current working dir as 
::           the jars folder to traverse.
::
:: This script exports the following variables:
::   PWS_CLASSPATH - list of the jar files separated by column
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

@echo off

setlocal ENABLEDELAYEDEXPANSION

set PWS_CLASSPATH=%PWS_JARS_DIR%
for /f "tokens=* delims= " %%a in ('dir /b/a-d/s "%PWS_JARS_DIR%\*.jar"') do (
	set PWS_CLASSPATH=!PWS_CLASSPATH!;%%a
)
echo !PWS_CLASSPATH!

endlocal

exit /b	
