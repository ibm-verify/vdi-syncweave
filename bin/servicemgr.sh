#!/bin/bash

if [ ! -z "$1" ] && [ ! -z "$2" ] && [ "$2" == "start" -o "$2" == "stop" ]; then
	SERVICE_NAME=$1
	if [ -r /etc/inittab ]; then
		# Take every line from the /etc/inittab file, split it by ":" 
		# and if the first string is equal to the service name
		# get the forth string of that line (the service command)
		COMMAND=`awk -F':' '{if($1=="'"$SERVICE_NAME"'") print $4}' /etc/inittab`
		
		# Execute the command that starts the service
		if [ "${2}" = "start" ] ; then
			echo Service \""${SERVICE_NAME}"\" is starting
			exec $COMMAND
		fi

		# Find the PID of the service started with the command and terminate the process
		if [ "${2}" = "stop" ] ; then
			echo Service \""${SERVICE_NAME}"\" is stopping
			PID=`ps -aef | grep "${COMMAND}" | grep -v "grep" | awk -F" " '{print $2}'`
			kill -s SIGTERM $PID
		fi
	fi
else
	echo "Usage: $0 <service_name> stop|start"
fi
