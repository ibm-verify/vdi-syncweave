#
# Copyright IBM Corp. 2025

#
# This script is used to set up the environment variables used by the
# SPS-CI build, dependent on the branch name.
#

export PUBLISH_BUILD=0
export RELEASE_BUILD=0

if [ ${BRANCH} = "eap-build" ] ; then
    export PUBLISH_BUILD=1
    export RELEASE_BUILD=1
elif [ ${BRANCH} = "main" ] ; then
    export PUBLISH_BUILD=1
fi

