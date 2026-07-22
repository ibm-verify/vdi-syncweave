#!/usr/bin/bash
# ****************************************************** {COPYRIGHT-TOP} ***
# Licensed Materials - Property of IBM
# 5724-M07
#
# Copyright contributors to the SyncWeave project
All Rights Reserved.
#
# US Government Users Restricted Rights - Use, duplication, or
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
# ****************************************************** {COPYRIGHT-END} ***
#=============================================================================
# Change History:
#
# mm/dd/yyyy  userid   track   change history description here
# ---------- --------  ------- -----------------------------------------------
# 12/17/2008 warbirds  11843   Update image.sh with ibmdi_docs_dev deliverables
# 04/30/2009 udaya     l2739   Modify and commonize build files for the cloned release
#=============================================================================


# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #
#  For   W I N D O W S   Builds                                                         #
#  $TREETOP resolves to /build/ibmdi_docs_dev, e.g., /build/ibmdi_docs_dev               #
#  Under $TREETOP is BLD/WIN                                                            #
#  So, the path to $topDir is $TREETOP/intel/BLD/WIN                                    #
#  $DATENAME contains the build level, e.g., 200806161402                               #
# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #

echo "Anchoring paths to variables"

RELEASE=ibmdi_docs_dev

gsa_base=/gsa/rtpgsa/projects/i/idi/builds/$RELEASE
gsa_path=$gsa_base/$DATENAME

gsaExportRoot=$gsa_path/tdi_docs
gsaShipDir=$gsaExportRoot/ship
gsaExportDir=$gsaExportRoot/export

gsaID="lib2@rtpgsa.ibm.com"
topDir=$TREETOP/intel/BLD/WIN

win_build_output01=$topDir/tdi_docs/ship
win_build_output02=$topDir/tdi_docs/export
win_build_log=$topDir/BUILD.LOG

echo "Making post build directory hierarchy"
echo "  ***  mkdir -p $TREETOP/$WHICHTREE/$DATENAME ***"
mkdir -p $TREETOP/$WHICHTREE/$DATENAME
chmod 775 $TREETOP/$WHICHTREE/$DATENAME

echo "Making post build directory hierarchy"
echo "  ***  mkdir -p $TREETOP/$WHICHTREE/$DATENAME/tdi_docs ***"
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/tdi_docs

echo "Making post build directory hierarchy"
echo "  ***  mkdir -p $TREETOP/$WHICHTREE/$DATENAME/tdi_docs/ship ***"
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/tdi_docs/ship

echo "Making post build directory hierarchy"
echo "  ***  mkdir -p $TREETOP/$WHICHTREE/$DATENAME/tdi_docs/export ***"
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/tdi_docs/export

scp -r $TREETOP/$WHICHTREE/$DATENAME  $gsaID:$gsa_base

echo "Copying ibmdi_docs_dev deliverables to GSA"
scp -r $win_build_output01/*    $gsaID:$gsaShipDir
scp -r $win_build_output02/*    $gsaID:$gsaExportDir
scp    $topDir/BUILD.LOG        $gsaID:$gsaExportRoot

echo "Creating the CMVCInfo.html file on GSA"
#perl $topDir/bigbbin/cmvcinfo -l $DATENAME -f integrat -d /tmp  
#perl $topDir/bigbbin/myMail.pl
 
# Create batch file for sftp. Creates a link from current build to latest.
echo "Creating /tmp/ibmdi_docs_dev_batch"
echo "cd $gsa_base"               > /tmp/ibmdi_docs_dev_batch
echo "-rm currentBuild"          >> /tmp/ibmdi_docs_dev_batch 
echo "ln $DATENAME currentBuild" >> /tmp/ibmdi_docs_dev_batch

# Execute sftp batch file
echo "Running /tmp/ibmdi_docs_dev_batch"
sftp -b /tmp/ibmdi_docs_dev_batch $gsaID 

# Clean up temp files
echo "Cleaning up the temporary files and directories"
rm -rf $TREETOP/$WHICHTREE/$DATENAME
rm /tmp/ibmdi_docs_dev_batch

