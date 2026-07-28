#!/bin/sh

# IBM_PROLOG_BEGIN_TAG
#
# %I%, %G%
#
#
#
# (C) COPYRIGHT International Business Machines Corp. 2006, 2010
# All Rights Reserved
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
#
# IBM_PROLOG_END_TAG

##################################################################
# ibmdisrvtp.sh: This script is used to start the TDI server with
# the configuaration specified in the configuration file. The
# configutaion file can be passed as a command line parameter
# using the -f parameter.
# eg: ibmdisrvtp.sh -f benchmark.properties
##################################################################

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
. "$TEMP_BIN_DIR/setupCmdLine.sh"
}

# Function: Demonstrates server usage
serv_usage ()
{
  echo "Usage: ibmdisrvtp.sh -f file"
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


# Check for command line parameters
if [ $# -lt 2 ];then
     serv_usage
     exit 1
fi


# Parse input parameters
while [ $# -ne 0 ]; do
        case $1 in
                -f | --file )           shift
                                      TDI_PERF_PROPS=$1
                                       ;;
                -h | --help )          serv_usage
                                       exit
                                       ;;
                * )                    echo "Error: Incorrect arguments"
                                       serv_usage
                                       exit 2
                                       ;;
            esac
        shift
  done

# Check if the performance property exists
if [ ! -r $TDI_PERF_PROPS ];then
	echo "Error: $TDI_PERF_PROPS does not exist"
	exit 3
fi


# Setup the directory structure
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
TIME_STAMP="Created on $RIGHT_NOW by $USER on $MYHOST"
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
check_tp_folder


PATH="$JRE_TDI_BIN_DIR:$PATH"
export PATH
if [ $debug = "true" ];then
	echo "Dumping PATH variable" >>$TPDEBUGFILE
	echo "$PATH" >>$TPDEBUGFILE
fi

#Record the start time
PID=$$
FULL_START_TIME=`date`


# If debug is true dump the server startup information
if [ $debug = "true" ];then
    echo "Starting server with following parameters ..." >>$TPDEBUGFILE
    echo "/usr/bin/time" "$TDI_JAVA_PROGRAM" $TDI_MIXEDMODE_FLAG -Xrs $jvmcmdoptions "-Duser.dir=$solutiondir" "-Dlog4j2.configurationFile=file:$TDI_HOME_DIR/etc/log4j2.xml" -jar "$TDI_HOME_DIR/IDILoader.jar" com.ibm.di.server.RS -c "$configfile" -r "$assemblyline" "$cmdoptions" >>$TPDEBUGFILE 
fi

# Start the server with the parameters specified
"/usr/bin/time" "$TDI_JAVA_PROGRAM" $TDI_MIXEDMODE_FLAG -Xrs $jvmcmdoptions "-Duser.dir=$solutiondir" "-Dlog4j2.configurationFile=file:$TDI_HOME_DIR/etc/log4j2.xml" -jar "$TDI_HOME_DIR/IDILoader.jar" com.ibm.di.server.RS -c "$configfile" -r "$assemblyline" "$cmdoptions" >>$TPDEBUGFILE 2>>$TPERRORFILE

# Record end time
FULL_END_TIME=`date`
ELAPSED=`cat $TPERRORFILE`

echo "******************************************************" >> $TPLOGFILE
echo "PATH				:$PATH" >> $TPLOGFILE
echo "Server PID                        :$PID" >> $TPLOGFILE
echo "Server Start time                 :$FULL_START_TIME" >> $TPLOGFILE
echo "Server End time                   :$FULL_END_TIME" >> $TPLOGFILE
echo "Total Execution time (in seconds) :$ELAPSED" >> $TPLOGFILE
echo "******************************************************" >> $TPLOGFILE

echo "Throughput is recorded in file: $TPLOGFILE"
