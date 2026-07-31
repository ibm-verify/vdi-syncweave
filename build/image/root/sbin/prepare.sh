#!/bin/sh

#
# Copyright contributors to the SyncWeave project
##############################################################################

# This script is used to install additional RPMs and files into the build 
# environment

set -e

##############################################################################
# Install some required RPMs from the CentOS-8 repository.

centos_repo_file="/etc/yum.repos.d/centos.repo"

cat <<EOT >> $centos_repo_file
[base]
name=CentOS-8 - Base
baseurl=http://mirror.centos.org/centos/8-stream/BaseOS/x86_64/os
gpgcheck=0

[appstream]
name=CentOS-8 - AppStream
baseurl=http://mirror.centos.org/centos/8-stream/AppStream/x86_64/os
gpgcheck=0
EOT

dnf -y install ant rsync libxslt

#
# Add the Docker package.
#

yum -y config-manager --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo

dnf -y install docker-ce unzip

#
# Clean up before we return.
#

dnf clean all

exit 0

