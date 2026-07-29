#!/usr/bin/env bash

#
# Copyright contributors to the SyncWeave project
##############################################################################
# This script is called as a part of the SPS pipeline to publish our build
# artifacts.

set -e

#
# Set up some debugging.
#

if [[ "$PIPELINE_DEBUG" == 1 ]]; then
    trap env EXIT
    env
    set -x
fi

#
# Check the command line options.
#

if [ $# -ne 1 ] ; then
    echo "Usage: $0 [artifact-dir]"
    exit 1
fi

artifact_dir=$1

#
# A simple function to display a banner in the log output.
#

banner()
{
    echo ""
    echo "##################################################################"
    echo $1
    echo ""
}

cd "$WORKSPACE/$(load_repo app-repo path)"

publish_script=$WORKSPACE/$PIPELINE_CONFIG_REPO_PATH/build-scripts/publish-release.py

export GITHUB_API_KEY="$(get_env GITHUB_API_KEY)" # pragma: allowlist secret

#
# Build up the description of the build.
#

description()
{
    echo "# SPS Build"
    echo ""
    echo "|Field|Details"
    echo "|-----|------"
    echo "|Date|`date`"
    echo "|Build Number|${BUILD_NUMBER} "
    echo "|Build URL|[${PIPELINE_RUN_URL}](${PIPELINE_RUN_URL})"
    echo "|Last Commit|[${APP_REPO}/commit/${COMMIT_SHA}](${APP_REPO}/commit/${COMMIT_SHA})"
    echo "|Triggered By|${TRIGGERED_BY}"
}

#
# Only run the publish in the CI pipeline
#

if [ $PIPELINE_NAMESPACE == "ci" ] ; then
    banner "Publishing the build artifacts to GitHub."

    # Install our required python modules.
    #python3 -m venv python.env
    #. python.env/bin/activate

    apt-get install -y python3-pip
    pip3 install requests
    pip3 install pygithub

    # Now we should publish the release.
    description_file=/tmp/description.txt

    description > $description_file

    $publish_script publish \
        --source-path $artifact_dir \
        --commit $COMMIT_SHA \
        --description "`cat $description_file`"

    # Prune the GitHub releases.
    banner "Pruning old GitHub releases."

    $publish_script prune
fi