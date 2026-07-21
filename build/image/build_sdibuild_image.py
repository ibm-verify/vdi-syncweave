#!/usr/bin/env python3

import sys
import subprocess
import argparse
import os
import tempfile
import shutil

############################################################################
# Section: Global variables.

# The name of our repository in 'artifact'.
repo_name = "na.artifactory.swg-devops.com/hyc-sec-isdi-team-docker-local"

# The name of our image.
image_name = repo_name + "/isdi-sw-build"

# Should we run in verbose mode or not?  Controlled by the '-v' command
# line argument.
verbose = False

############################################################################
# Section: utility functions

def execute(cmd, capture_out = False, ignore_error = False):
    """
    This function will execute the specified command.  If the command fails
    the program will exit.
    
    @param cmd         [in] : An array which represents the command to be 
                              executed
    @param capture_out [in] : Should we capture stdout?
    
    @retval stdout from the command
    """

    try:
        if verbose:
            print("  executing: " + " ".join(cmd))

        if capture_out:
            out = subprocess.check_output(cmd);
        else:
            out = subprocess.check_call(cmd)

        if verbose and capture_out:
            print("   output: \n" + out)

        return out

    except OSError:
        print("\nError> " + " ".join(cmd))
        if not ignore_error:
            sys.exit(1)
    except subprocess.CalledProcessError:
        print("\nError> " + " ".join(cmd))
        if not ignore_error:
            sys.exit(1)

############################################################################
# Section: General commands.

def publish_command(args):
    """
    Handle the publishing of the image to artifactory.
    """

    if verbose:
        print("Command: publish")

    # Work out the version number.
    if args.version:
        version = args.version
    else:
        tags = execute(["docker", "images", "--format", "{{.Tag}}", image_name],
                    True).splitlines()

        if not tags:
            print("Error> no matching image has been found: {}".format(
                            image_name))
            sys.exit(1)

        version = tags[-1];

    # Now we can execute the build command.
    print("Logging into the artifactory server (use your intranet id):")
    execute(["docker", "login", "https://" + repo_name])

    execute(["docker", "push", image_name + ":" + version])

def build_command(args):
    """
    Handle the building of the docker image.
    """

    temp_dir = tempfile.mkdtemp()

    try:
        print("Copying our files to the staging area....")

        stage_dir = temp_dir + "/stage"

        # Our source files....
        execute(["cp", "-r", 
            os.path.dirname(os.path.realpath(__file__)) + "/ContainerFile", temp_dir])
        execute(["cp", "-r", 
            os.path.dirname(os.path.realpath(__file__)) + "/root", stage_dir])

        # Write the version file.
        etc_dir = stage_dir + "/etc"
        os.mkdir(etc_dir)

        version_file = open(etc_dir + "/build.version", "w")
        version_file.write(args.version)
        version_file.close()

        # Now we can create the docker image.
        print("Creating the docker image....")
        execute(["docker", "build", 
            "--tag", 
            image_name + ":" + args.version, temp_dir])

    finally:
        print("Cleaning the stages files....")
        execute(["chmod", "-R", "777", temp_dir])
        shutil.rmtree(temp_dir)

############################################################################
# Section: Argument Parser

def process_argv():
    """
    Create the command parser and then parse the arguments.
    """

    parser = argparse.ArgumentParser(description="This script is designed "
        "to build the IVDI build docker image.")

    parser.add_argument("-v", "--verbose", help="Display the commands as " 
                "they are executed.", action="store_true")

    subparsers = parser.add_subparsers(help='sub-command help')

    # build command.....
    cmd = subparsers.add_parser("build", help="This command will " 
        "build the IVDI 'build' image.")

    cmd.add_argument("-v", "--version", help="The build version number.  This " 
        "will be used as the label for the docker image.", required=True)

    cmd.set_defaults(func=build_command)

    # publish command....
    cmd = subparsers.add_parser("publish", help="This command will publish "
        "the build image contained on the local system to artifactory.")

    cmd.add_argument("--version", help="The image version number, e.g. "
        "9040. If no version number is supplied the script will use the latest "
        "available image.",
        required=False)

    cmd.set_defaults(func=publish_command)

    return parser.parse_args()

############################################################################
# Section: Main Line

args    = process_argv()
verbose = args.verbose

args.func(args)

