#!/usr/bin/bash
# ****************************************************** {COPYRIGHT-TOP} ***
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
# 08/29/2008 warbirds  11260   Create image.sh for ibmdi_dev
# 01/30/2009 warbirds  12084   Add ship/zip_bundles/trial_upgrade to GSA Publish
# 04/30/2009 udaya     l2739   Modify and commonize build files for the cloned release
# 07/28/2009 kkolev    l2953   Add ship/tests to GSA Publish
# 09/06/2010 pravetha  14345   Updated version number in name of installer binaries
#=============================================================================


# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #
#  For   W I N D O W S   Builds                                                         #
#  $TREETOP resolves to /build/$CMVC_RELEASE, e.g., /build/ibmdi_dev                    #
#  Under $TREETOP is BLD/WIN                                                            #
#  So, the path to $topDir is $TREETOP/intel/BLD/WIN                                    #
#  $DATENAME contains the build level, e.g., 200806161402                               #
# - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - #

set -x
echo "Anchoring paths to variables"

RELEASE=$CMVC_RELEASE

gsa_base=/gsa/rtpgsa/projects/i/idi/builds/$RELEASE

# This is the example code to remove old GSA slots.
# it must be placed AFTER the assignment of variable $gsa_base .
gsa_slot_max=3
num_save_slots=`expr $gsa_slot_max - 1`
cd $gsa_base
rm -rf `ls -1dt 20* | sed "1,${num_save_slots}d"`

gsa_path=$gsa_base/$DATENAME

gsa_protected_path=$gsa_base/src_ref
gsa_protected_src_ref_ce_dir=$gsa_protected_path/ce
gsa_protected_src_ref_amc_dir=$gsa_protected_path/amc
gsa_protected_src_ref_srv_dir=$gsa_protected_path/srv

gsaExportLogsDir=$gsa_path/export/logs
gsaExportDocsDir=$gsa_path/export/docs_internal
gsaExportSCADir=$gsa_path/export/sca

gsaShipZipBundlesDir=$gsa_path/ship/zip_bundles
gsaShipZipBundlesTrialUpgradeDir=$gsa_path/ship/zip_bundles/trial_upgrade
gsaShipSupportDir=$gsa_path/ship/support
gsaShipCDImageDir=$gsa_path/ship/cdimage
gsaShipCDImageDirIDE=$gsaShipCDImageDir/identity_edition
gsaShipTestsDir=$gsa_path/ship/tests

topDir=$TREETOP/intel/BLD/WIN

win_build_output01=$topDir/ship/zip_bundles
win_build_output01a=$topDir/ship/zip_bundles/trial_upgrade
win_build_output03=$topDir/ship/cdimage
win_build_output03IDE=$topDir/ship/cdimage/identity_edition
win_build_output04=$topDir/export/sca
win_build_output05=$topDir/export/docs_internal
win_build_output06=$topDir/export/logs
win_build_output07=$topDir/export/src_zips
win_build_output08=$topDir/ship/support
win_build_output09=$topDir/ship/tests
win_build_log=$topDir/BUILD.LOG


#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-# Array of Expected Binary Files #-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#
#BUILD_DIR_01=$win_build_output03/identity_edition

#fileArray=(

#  $BUILD_DIR_01/disk1/aix_ppc/install_sdiv72_aix_ppc.bin
#  $BUILD_DIR_01/disk1/aix_ppc_64/install_sdiv72_aix_ppc_64.bin
#  $BUILD_DIR_01/disk1/linux_ppc/install_sdiv72_ppclinux.bin
#  $BUILD_DIR_01/disk1/linux_s390x/install_sdiv72_zlinux.bin
#  $BUILD_DIR_01/disk1/linux_x86/install_sdiv72_linux_x86.bin
#  $BUILD_DIR_01/disk1/linux_x86_64/install_sdiv72_linux_x86_64.bin
#  $BUILD_DIR_01/disk1/solaris_sparc/install_sdiv72_solaris_sparc.bin
#  $BUILD_DIR_01/disk1/windows_x86/install_sdiv72_win_x86.exe
#  $BUILD_DIR_01/disk1/windows_x86_64/install_sdiv72_win_x86_64.exe

#)

#for file in ${fileArray[@]}
#do
#  echo $file
#  if [ -f $file ]
#  then
#    echo $file found
#  else
#    echo $file not found
#    export DO_NOT_COPY=1
#    echo "Missing Installer Images - FAILED BUILD"
#    exit 1
#  fi
#done

export OK_2_COPY=1

#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-# Array of Expected Binary Files #-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#-#

echo "Making post build directory hierarchy"
mkdir $TREETOP/$WHICHTREE/$DATENAME
chmod 775 $TREETOP/$WHICHTREE/$DATENAME

echo "Making post build directory hierarchy"
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/ship
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/ship/zip_bundles
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/ship/zip_bundles/trial_upgrade
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/ship/cdimage
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/ship/cdimage/identity_edition
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/ship/support
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/ship/tests
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/export
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/export/logs
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/export/docs_internal
mkdir -p $TREETOP/$WHICHTREE/$DATENAME/export/sca

# Ensure GSA target dirs exist
[ ! -d $gsa_protected_src_ref_amc_dir ] && 
	mkdir -p $gsa_protected_src_ref_amc_dir

[ ! -d $gsa_protected_src_ref_ce_dir ] && 
	mkdir -p $gsa_protected_src_ref_ce_dir

[ ! -d $gsa_protected_src_ref_srv_dir ] && 
	mkdir -p $gsa_protected_src_ref_srv_dir

scp -r $TREETOP/$WHICHTREE/$DATENAME  $gsa_base

echo "Copying TKLM Install Images to GSA"
scp -r $win_build_output01/*    $gsaShipZipBundlesDir
scp -r $win_build_output01a/*   $gsaShipZipBundlesTrialUpgradeDir
#scp -r $win_build_output03IDE/* $gsaShipCDImageDirIDE
scp    $win_build_output04/*    $gsaExportSCADir
scp -r $win_build_output05/*    $gsaExportDocsDir
scp    $win_build_output06/*    $gsaExportLogsDir
scp    $win_build_output08/*    $gsaShipSupportDir
scp    $win_build_output09/*    $gsaShipTestsDir
scp    ${LOGS}/*		$gsaExportLogsDir

scp $win_build_output07/di_amc-*.zip     $gsa_protected_src_ref_amc_dir
scp $win_build_output07/di_ce-*.zip      $gsa_protected_src_ref_ce_dir
scp $win_build_output07/di_src-*.zip     $gsa_protected_src_ref_srv_dir
scp $win_build_output07/di_autogen-*.zip $gsa_protected_src_ref_srv_dir

#echo "Creating the CMVCInfo.html file on GSA"
#perl $topDir/bigbbin/cmvcinfo -l $DATENAME -f integrat -d /tmp  
#perl $topDir/bigbbin/myMail.pl
 
cd $gsa_base
rm currentBuild
ln -s $DATENAME currentBuild

# Clean up temp files
echo "Cleaning up the temporary files and directories"
rm -rf $TREETOP/$WHICHTREE/$DATENAME

