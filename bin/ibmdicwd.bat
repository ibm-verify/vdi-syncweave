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
:: US Government Users Restricted Rights - Use, duplication or
:: disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
::
:: IBM_PROLOG_END_TAG

rem 
rem This script changes the working directory to the value of the first parameter
rem

if not .%1==. (
	rem Make sure we are on the correct drive
	%~d1
	rem CD into solution directory
	CD %1
)
