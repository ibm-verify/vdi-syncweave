#!/usr/bin/env bash

#
# Copyright contributors to the SyncWeave project
##############################################################################
# This script is called as a part of the SPS pipeline to build our source
# code.

# Exit immediately if a command exits with a non-zero status
set -e
# Treat unset variables as an error when substituting
set -u
# Pipeline exits with status of last command to exit non-zero or zero if all succeeded
set -o pipefail

#
# Utility functions
#

# Error handling function
error_exit() {
    banner "ERROR: $1" >&2
    exit 1
}

# A simple function to display a banner in the log output
banner() {
    local message="$1"
    local width=70
    local line=$(printf '%*s' "$width" | tr ' ' '#')
    
    echo ""
    echo "$line"
    echo "### ${message}"
    echo "$line"
    echo ""
}

# Function to create directories if they don't exist
create_dirs() {
    local dirs=("$@")
    for dir in "${dirs[@]}"; do
        mkdir -p "$dir" || error_exit "Failed to create directory: $dir"
    done
}

# Function to clone git repositories
clone_repo() {
    local repo_url="$1"
    local branch="$2"
    local target_dir="$3"
    local repo_name=$(basename "$repo_url" .git)
    
    banner "Cloning $repo_name repository (branch: $branch)"
    git clone --single-branch --branch "$branch" "$repo_url" "$target_dir" || \
        error_exit "Failed to clone $repo_name repository"
}

# Set up debugging if needed
if [[ "${PIPELINE_DEBUG:-0}" == 1 ]]; then
    trap env EXIT
    env
    set -x
fi

#
# Define environment variables
#

# Set the build directory (equivalent to TRAVIS_BUILD_DIR)
cd "$WORKSPACE/$(load_repo app-repo path)"
BUILD_DIR=${PWD}
BUILD_NUMBER=${BUILD_NUMBER:-$(date +%s)}

# Define global environment variables from .travis.yml
export max_work_items=20
export OCTOKIT_API_ENDPOINT=https://github.ibm.com/api/v3
export TAG=travis-ci-isvdi-11.0-b${BUILD_NUMBER}
export ICR_REPO="icr.io/isvdi-internal"

# Get credentials from environment
export GITHUB_API_KEY=$(get_env GITHUB_API_KEY)
export ARTIFACTORY_USERNAME=$(get_env Functional_ID)
export ICR_PUBLISH_KEY_IAMDEV=$(get_env ICR_PUBLISH_KEY_IAMDEV)

# Set up trap to catch errors
trap 'error_exit "Build failed at line $LINENO"' ERR

#
# Begin the build process
#

banner "Starting build process"

# Create necessary directories
banner "Creating build directories"
create_dirs "${BUILD_DIR}/tmp/adks" "${BUILD_DIR}/tmp/tools"

# Install dependencies
banner "Installing dependencies"
sudo apt-get update || error_exit "Failed to update package lists"
sudo apt-get -y install python3-pip || error_exit "Failed to install system dependencies"
pip3 install requests || error_exit "Failed to install Python dependencies"

# Set up Docker
banner "Setting up Docker"
echo "${ICR_PUBLISH_KEY_IAMDEV}" | docker login "${ICR_REPO}" -u iamapikey --password-stdin || error_exit "Failed to log in to Docker registry"

# Source environment file if it exists
if [ -f "${BUILD_DIR}/build-scripts/sps_env.sh" ]; then
    banner "Sourcing environment file"
    source "${BUILD_DIR}/build-scripts/sps_env.sh"
fi

# Clone required repositories
clone_repo "https://${GITHUB_API_KEY}@github.ibm.com/sec-di/adks.git" "main" "${BUILD_DIR}/tmp/adks"
clone_repo "https://${GITHUB_API_KEY}@github.ibm.com/sec-di/tools.git" "tools_fp13" "${BUILD_DIR}/tmp/tools"

# Run build in Docker container
banner "Running build in Docker container"
docker run -i --rm --name build \
    -e AUTOBUILD=1 \
    -e EXIT_AFTER_BUILD=1 \
    -e RELEASE=1 \
    -v ${BUILD_DIR}/tmp/adks:/adks \
    -v ${BUILD_DIR}/tmp/tools:/tools \
    -v ${BUILD_DIR}:/workspace \
    -v ${BUILD_DIR}/build:/build \
    --tmpfs /tmp:rw,exec,dev,suid,relatime,mode=755 \
    ${ICR_REPO}/isdi-sw-build:latest || error_exit "Docker build failed"

# Create build number file
banner "Creating build number file"
mkdir -p ${BUILD_DIR}/artifacts
echo "BUILD NUMBER ${BUILD_NUMBER}" > ${BUILD_DIR}/artifacts/BUILD_NUMBER.txt

banner "Copy artifacts files from ${BUILD_DIR}/build/ship/install/ to ${BUILD_DIR}/artifacts"
cp ${BUILD_DIR}/build/ship/install/*.zip ${BUILD_DIR}/artifacts/
cp ${BUILD_DIR}/build/ship/install/*.tgz ${BUILD_DIR}/artifacts/

# Get last 20 Git commit details
git log --pretty='%s %b' -n $max_work_items > ${BUILD_DIR}/artifacts/vdi_sw_latest_commits.txt

banner "Checking build files from ${BUILD_DIR}/artifacts "
ls -lRh ${BUILD_DIR}/artifacts

# Uncomment these lines if you need to run dependency checks
# banner "Checking dependencies"
# ${BUILD_DIR}/travis/check_dependencies.py check \
#     --branch ${BRANCH_NAME:-main} \
#     --buildDepFile /tmp/build/export/dependencies.xml \
#     --depFile ${BUILD_DIR}/travis/data/dependencies.xml

# Uncomment this line if you need to run OWASP scan
# /bin/sh ${BUILD_DIR}/travis/owasp_scan.sh

# Uncomment these lines if you need to run contrast scan
# ${BUILD_DIR}/travis/contrast_scan.sh \
#     /tmp/build \
#     /tmp/contrast

# Deploy steps

banner "Build completed successfully"
