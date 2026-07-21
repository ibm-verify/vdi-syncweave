#!/bin/sh
set -x

echo $CMVC_RELEASE Build level $DATENAME
echo Starting preapreTree.sh  Time:`date`
echo " "

# Copy the latest commit of adks release ( eg: ibmdi_dev.adks) to the adks directory in ibmdi_dev
# ADKS_RELEASE is defined in release.ini or environment file.
# move "adks" directory up 1 level to TOP
cp -r ${ADKS_RELEASE}/adks .

# move content of "ibmdi_72" directory up 1 level to TOP
mv ibmdi_72/* .
		 
# remove empty ibmdi_dev directory
rm -rf ibmdi_72

# Copy the latest commit of MOCK release on top of ibmdi_72 content
# IBMDI_72_MOCK variable is defined in release.ini as IBMDI_72_MOCK=/project/ibmdi_72_mock/build/bldbase
# uncomment following comments to enable mock build 
#	unzip -o ${IBMDI_72_MOCK}/Commit.zip

echo Completed preapreTree.sh  Time:`date`
exit 0
