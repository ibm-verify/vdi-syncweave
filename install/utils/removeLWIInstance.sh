#!/bin/sh
#removeLWIInstance.sh

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

#Must export path to be able to use which on V5R4
ver=`uname -v`
if [ $ver -eq 5 ]
then
   export PATH=/usr/bin:.:/qopensys/usr/bin
fi

#Need service name
line=`grep "com.ibm.lwi.instance.name" $1`
service_name=`echo $line | cut -d '=' -f 2`

#stop LWI
/www/$service_name/lwi/bin/lwistop.sh

#This call takes the location of the properties file as well as "true" or "false" which indicates
#whether or not to also delete the apache server.
CLASSPATH=/QIBM/ProdData/OS/OSGi/LWI81/native/iasadmin.jar:/QIBM/ProdData/OS400/jt400/lib/jt400Native.jar
java -cp $CLASSPATH com.ibm.lwi.admin.IntegratedServerAdmin -removeServer $1

#Remove the directory
rm -rf /www/$service_name/

