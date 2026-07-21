#!/bin/sh

# IBM_PROLOG_BEGIN_TAG 
#  
# %I%, %G%
#  
# Licensed Materials - Property of IBM 
#  
# Restricted Materials of IBM 
#  
# (C) COPYRIGHT International Business Machines Corp. 2006, 2010
# All Rights Reserved 
#  
# US Government Users Restricted Rights - Use, duplication or 
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
#  
# IBM_PROLOG_END_TAG 

#################################################################
# ibmdibenchmark.sh: This utility is used to collect the 	#
# system information and regularly track the system resources 	#
# when the ibmdisrv is running
#################################################################

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
. "$TEMP_BIN_DIR/setupCmdLine.sh"
}

bench_usage ()
{
  echo "usage: ibmdibenchmark.sh -f file"

}

#
# CMDFINDER holds the command which is used to find other commands
# depending on the platform.
#
CMDFINDER=which

UNAME_OS=`uname`

if [ "$UNAME_OS" = "OS/390" -o "$UNAME_OS" = "OS400" ] ; then
	CMDFINDER=whence
fi
	
TEMP_BIN_DIR=`$CMDFINDER $0`
TEMP_BIN_DIR=`dirname $TEMP_BIN_DIR`
TEMP_BIN_DIR=`dirname "$TEMP_BIN_DIR/../bin/setupCmdLine.sh"`

SKIP_ISCDIR_SETUP=1
setupTDIEnv "$TEMP_BIN_DIR"

#Parse input parameters
 while [ $# -ne 0 ]; do
        case $1 in
                -f | --file )           shift
                                        TDI_PERF_PROPS=$1
                			;;
                -h | --help )       	bench_usage
                                    	exit
                 			;;
                * )   			Error: Incorrect arguments    
	              			bench_usage
                      			exit 1
					;;
            esac
        shift
        done
if [ ! -f $TDI_PERF_PROPS ];then
	echo "Error: $TDI_PERF_PROPS Not found"
	exit 3
fi
#. tdiperfhead.sh $TDI_PERF_PROPS


#################################################################

#Check for arguments
        PERFPROPS=$TDI_PERF_PROPS

if [ -r $PERFPROPS ];then
        . $soldir$/$PERFPROPS
fi

#Set the global parameters
PERFPATH="$TDI_HOME_DIR/performance"
PERFLOGPATH="$PERFPATH/logs"
TPLOGPATH="$PERFLOGPATH/throughput"
BENCHLOGPATH="$PERFLOGPATH/benchmark"

#Create the logs folder if it does not exist
if [ ! -d $PERFLOGPATH ];then
        mkdir $PERFLOGPATH
fi

BENCHLOGTMPPATH="$BENCHLOGPATH/tmp"
CONFIGPATH="$BENCHLOGTMPPATH/config"
BENCHSTATS="$BENCHLOGTMPPATH/benchstats"

REPORTFILE=$BENCHLOGPATH/report_`date +%m%d`_`date +%H_%M_%S`.html

TPLOGFILE=$TPLOGPATH/tp_`date +%m%d`_`date +%H_%M_%S`
TPERRORFILE=$TPLOGPATH/error_`date +%m%d`_`date +%H_%M_%S`.log
TPDEBUGFILE=$TPLOGPATH/debug_`date +%m%d`_`date +%H_%M_%S`.log

ERRORFILE=$BENCHLOGPATH/error_`date +%m%d`_`date +%H_%M_%S`.log
DEBUGFILE=$BENCHLOGPATH/debug_`date +%m%d`_`date +%H_%M_%S`.log


TITLE="SDI Benchmarking Report"
#RIGHT_NOW=$(date +"%x %r %Z")
RIGHT_NOW=`date`
MYHOST=`uname -n`
TIME_STAMP="Created on $RIGHT_NOW on host $MYHOST"
OS=""
USER_EXIT=0

check_bench_folder ()
{
        if [ ! -d $BENCHLOGPATH ];then
                mkdir -p $BENCHLOGPATH
                DEBUG "Creating $BENCHLOGPATH"
        fi
        if [ ! -d $BENCHLOGTMPPATH ];then
                mkdir -p $BENCHLOGTMPPATH
                DEBUG "Creating $BENCHLOGTMPPATH"
        fi
        if [ ! -d $CONFIGPATH ]; then
                mkdir -p $CONFIGPATH
                DEBUG "Creating $CONFIGPATH"
        fi
        if [ ! -d $BENCHSTATS ]; then
                mkdir -p $BENCHSTATS
                DEBUG "Creating $BENCHSTATS"
        fi
}



DEBUG ()
{
   if [ $debug = "true" ];then
  	echo `date` >> $DEBUGFILE 
   fi
}
ERROR ()
{
   echo `date` >> $ERRORFILE
}

check_tp_folder ()
{
   if [ ! -d $TPLOGPATH ];then
        mkdir -p $TPLOGPATH
   fi
}


write_report ()
{
cat <<- _EOF_
        <html>
            <head>
                <title>$TITLE</title>
            </head>
                <body>
                <h1>$TITLE</h1>
            <p>$TIME_STAMP</p>
                `add_config`
                `add_benchstats`
                </body>
        </html>
_EOF_
}
get_server_instance_count ()
{
        SERV_NO_INST=`ps -ef | grep ibmdisrv | grep -v grep | wc -l`
        DEBUG "Number of Server Instances: $SERV_NO_INST"
}

get_server_instances ()
{
        SERV_INST=`ps -ef | grep ibmdisrv | grep -v grep`
        DEBUG "Server instances"
        DEBUG "$SERV_INST"
}

get_os ()
{
        OS=`uname -srv`
        CURRENT_OS=""
        if      expr "$OS" : "SunOS" >/dev/null 2>&1
                then
                        CURRENT_OS="solaris"
        elif    expr "$OS" : "Linux" >/dev/null 2>&1
                then
                        CURRENT_OS="linux"
        elif    expr "$OS" : "HP-UX" >/dev/null 2>&1
                then
                        CURRENT_OS="hpux"
        elif    expr "$OS" : "AIX" >/dev/null 2>&1
                then
                        CURRENT_OS="aix"
        else    DEBUG "Unknown Operating System"
        fi
        DEBUG "Operating system: $CURRENT_OS"
}

system_config ()
{
        DEBUG "Getting system configuration ..."
        case "$CURRENT_OS" in
                linux)
                        df -k >$CONFIGPATH/disk_space 2>>$ERRORFILE
                        /sbin/ifconfig >$CONFIGPATH/ifconfig 2>>$ERRORFILE
                ;;
                aix)
                        df -k >$CONFIGPATH/disk_space 2>>$ERRORFILE
                        ifconfig en0 >$CONFIGPATH/ifconfig_en0 2>>$ERRORFILE
                        lsps -a >$CONFIGPATH/lsps_-a 2>>$ERRORFILE
                        lsfs >$CONFIGPATH/lsfs 2>>$ERRORFILE
                        ;;
                solaris)
                        df -k >$CONFIGPATH/disk_space 2>>$ERRORFILE
                        /usr/sbin/ifconfig -a >$CONFIGPATH/ifconfig_-a 2>>$ERRORFILE
                        showrev >$CONFIGPATH/showrev 2>>$ERRORFILE
                        /etc/swap -l >$CONFIGPATH/swap_-l 2>>$ERRORFILE
                        cp /etc/system $CONFIGPATH/system 2>>$ERRORFILE
                        cp /etc/vfstab $CONFIGPATH/vfstab 2>>$ERRORFILE
                        ps -ef >$CONFIGPATH/ps_-ef 2>>$ERRORFILE
                        ;;
                esac
        # common routines for all platforms
                uname -a  >$CONFIGPATH/uname_-a 2>>$ERRORFILE
}

add_config ()
{
        if [ -w $CONFIGPATH ];
                then
                        for filename in `ls $CONFIGPATH`
                        do
                                echo "<H2>$filename</H2>"
                                echo "<pre>"
                                cat $CONFIGPATH/$filename
                                echo "</pre>"
                        done
                fi
}

add_benchstats ()
{
        if [ -w $BENCHSTATS ];
                then
                        for benchfilename in `ls $BENCHSTATS`
                        do
                                echo "<H2>$benchfilename</H2>"
                                echo "<pre>"
                                cat $BENCHSTATS/$benchfilename
                                echo "</pre>"
                        done
                fi
}

cleanup ()
{
         rm -rf $BENCHLOGTMPPATH
}

clean_quit ()
{
        DEBUG "Inside clean quit"
        while [ true ]
        do
                echo "*** User Break: Just [Q]uit, Quit and [R]eport ?"
                read ans
                case $ans in
                Q*|q*)  echo "User Exit.  No results were generated."
                        cleanup
                        exit 0
                ;;
                R*|r*)  DEBUG "Writing report $REPORTFILE ..."
                        echo "Writing report $REPORTFILE ..."
                        write_report >$REPORTFILE
                        cleanup
                        exit 0
                ;;
                *) echo Incorrent argument
                ;;
                esac
        done
}


##########################################################################
#Validate the interval and duration parameters
if [ $interval -lt 1 ];
        then
                ERROR "Interval cannot be less than 1 second"
                exit 2
fi

if [ $duration -eq -1 ];
        then
                COUNT=-1
                echo "Recording indefinitely at every $interval seconds..."
        elif [ $duration -lt $interval ];
                then
                        ERROR "Duration cannot be less than $interval"
                        exit 3
                else
                        #COUNT=( $duration / $interval )
			COUNT=`eval $duration / $interval`
			echo "Benchmarking will run $COUNT times, every $interval seconds ..."
        fi


#Create the temporary file structure
cleanup
check_bench_folder

#Get the operating system parameters
get_os
system_config
trap clean_quit INT

vmstat 1 1 | head -5 >> $BENCHSTATS/vmstat
while [ true ]
        do
	        #We have completed the required duration. hence exiting
                if [ $COUNT -eq 0 ];
                        then
                               break
                fi

                #We have more iterations to go
                if [ $COUNT -gt 0 ];
                        then
                                #COUNT=$((COUNT - 1))
				#COUNT=`eval $COUNT - 1`
				COUNT=`eval $COUNT - 1`
                fi


		#If number of server instance counts is 0, Dont record
		#Get the server instances
		#get_server_instance_count
		
		#if [ $SERV_NO_INST -eq 0 ]
               	#then
		#	DEBUG "No server instance running. Checking after $interval seconds ..."
               	#	sleep $interval
               	#	continue
		#fi

		#If we are here there is a server instance running hence start recording
		
		#Record vmstat information		
		if [ $vms = "y"   ];
        	then
		      vmstat 2 2 >/tmp/vmstat.out 2>> $ERRORFILE
		      cat /tmp/vmstat.out | tail -1 >>$BENCHSTATS/vmstat
		      rm -rf /tmp/vmstat.out
		fi

		#Record iostat information
		if [ $ios = "y"   ];
        	then        
		      iostat >>$BENCHSTATS/iostat 2>>$ERRORFILE
		fi

		#Record netstat information
		if [ $nets = "y"   ];
       		then		
			netstat >>$BENCHSTATS/netstat 2>>$ERRORFILE
		fi

		sleep $interval
		
		
	done			#while end
 clean_quit
