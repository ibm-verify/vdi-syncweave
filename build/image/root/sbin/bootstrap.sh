#!/bin/sh

#
# Copyright contributors to the SyncWeave project
# This script is used to bootstrap the IVDI build environment.  This will 
# involve creating the sandbox, if not already created, and then changing
# into the sandbox.  If the build doesn't currently exist the build will be
# started.

workspace=/workspace
build=/build

##############################################################################
# Initialize our environment.

init_environment()
{
    # Enable bash-like command history within our ksh.
    if [ ! -e $HOME/.kshrc ] ; then
        echo 'set -o vi' >> $HOME/.kshrc
        echo 'set -o multiline' >> $HOME/.kshrc
        echo 'alias __A=$(print '\\0020')' >> $HOME/.kshrc
        echo 'alias __B=$(print '\\0016')' >> $HOME/.kshrc
        echo 'alias __C=$(print '\\0006')' >> $HOME/.kshrc
        echo 'alias __D=$(print '\\0002')' >> $HOME/.kshrc
        echo 'alias __H=$(print '\\0001')' >> $HOME/.kshrc
    fi

    # Clean out the existing build.
    if [ "${CLEAN}" = 1 ] ; then
        echo "Cleaning up the build area...."
        rm -rf $build
    fi
}

##############################################################################
# Set up the SDI software build environment.

setup_sdi_software()
{
    # Set up our Java path
    export JAVA_HOME=/adks/ibm/jdk/jdk-11.0.19+7/

    export PATH="$JAVA_HOME/bin:/bin:$PATH"
    export CONTAINER_BUILD=1
    export TOOLS_HOME=$build/tools_git

    if [ "${RELEASE_BUILD}" != 1 ] ; then
        export DEBUGGING=1
    fi

    # Check to see if the sandbox is already there.  If it isn't we create
    # it now.
    if [ ! -e $build/build_sdi.sh ] ; then
        echo "Setting up the build area...."

        cp -as $workspace/* $build/
        rsync -a /tools/* $build/tools_git
        ln -sf /adks $build/adks
    fi

    # Set up the relink command.
    grep -q relink $HOME/.bashrc

    if [ $? -eq 1 ] ; then
        echo "alias relink=\"cp -ans /workspace/* /build/ && rsync -a /tools/* /build/tools_git\"" >> $HOME/.bashrc
    fi
}

##############################################################################
# A utility function to show a progress indicator.

progress()
{
    echo "Phase: $1"
    while [ 1 ] ; do
        printf "."
        sleep 10
    done
}

##############################################################################
# Perform the initial SDI software build.  We need to send the output to
# a log file as the automatic build will mostly be used in a Travis-CI build
# environment and Travis-CI has restrictions on the length of the build log.

build_sdi_software()
{
    echo "Building SDI....."
    cd $build
    ant -f build.xml

    return $?
}

##############################################################################
# Our main line.

if [ ! -d $build ] ; then
    mkdir $build
fi

# Change our locale to a UTF-8 locale, otherwise our UTF-8 catalog files become
# unreadable.
locale -a | grep -q "^C.utf8"
if [ $? -eq 0 ] ; then
    export LC_ALL=C.utf8
else
    export LC_ALL=en_US.utf8
fi

# Initialize our environment.
init_environment

# Work out if this is the first time that this script has been run.
is_initial=0
if [ ! -e $build/build_sdi.sh ] ; then
    is_initial=1
fi

# Set-up the build environment.
setup_sdi_software

# Build the source code.
if [ ! -z "$AUTOBUILD" -a $is_initial -eq 1 ] ; then
    build_sdi_software

    rc=$?

    if [ ! -z "$EXIT_AFTER_BUILD" ] ; then
        exit $rc
    fi
fi

# Start the shell which is going to be used.
cd $build
bash

