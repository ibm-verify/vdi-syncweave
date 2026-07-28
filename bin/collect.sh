#!/bin/sh

# IBM_PROLOG_BEGIN_TAG 
#  
# %I%, %G%
#  
#  
#  
# (C) COPYRIGHT International Business Machines Corp. 2005, 2010
#  
# US Government Users Restricted Rights - Use, duplication or 
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
#  
# IBM_PROLOG_END_TAG 

#################################################################
# Collects a set of files specified in filelist.txt
#################################################################

# Function to source in the TDI setupCmdLine.sh script
setupTDIEnv ()
{
. "$TEMP_BIN_DIR/setupCmdLine.sh"
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

SKIP_ISCDIR_SETUP=1
setupTDIEnv "$TEMP_BIN_DIR"

if test "x$1" = "x-h"
then
echo Utility to collect servicibility info
echo 
echo Usage: $0 [-s solndir_path]
echo 
echo filelist.txt is used to specify the files to copy.
echo The archived info is named traceDumps.tar
exit
fi


dest="traceDumps"
`mkdir $dest` 

FILES=`cat filelist.txt`
for i in $FILES
do
`cp -R $i $dest/`
done

if test "x$1" = "x-s" 
then `
cp -R $2 $dest/`
fi

tar -cvf $dest.tar $dest
