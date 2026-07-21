#!/bin/sh
#createLWIInstance.sh

displayUsage()
{
	echo This script creates an LWI instance on i5OS.
	echo
	echo Usage: $0 TDIAMC_Properties_File
}

if [ -z "$1" ]
then
	displayUsage
	exit -1
fi

mkdir /QIBM/UserData/TDI/V7.2
java -cp /QIBM/ProdData/OS/OSGi/LWI81/native/iasadmin.jar:/QIBM/ProdData/OS400/jt400/lib/jt400Native.jar com.ibm.lwi.admin.IntegratedServerAdmin -createServer $1

