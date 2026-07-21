#!/usr/bin/env python3

import sys
import subprocess
import argparse
import re
import os
import textwrap

############################################################################
# Section: Global variables.

# Should we run in verbose mode or not?  Controlled by the '-v' command
# line argument.
verbose = False

# The name of our repository in 'artifact'.
repo_name = "na.artifactory.swg-devops.com/hyc-sec-isdi-team-docker-local"

# The name of our image.
image_name = repo_name + "/isdi-sw-build"

# The prefix which is used when building the container name.
container_prefix="isdi_build."

# The IP address which will be set-up on the system.
lo_ip = "10.10.10.200"

# The file which is used to set-up the loopback address on an OS-X system.
lo_plist = """
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple Computer//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
  <dict>
      <key>Label</key>
      <string>com.runlevel1.lo0.{}</string>
      <key>RunAtLoad</key>
      <true/>
      <key>ProgramArguments</key>
      <array>
          <string>/sbin/ifconfig</string>
          <string>lo0</string>
          <string>alias</string>
          <string>{}</string>
      </array>
      <key>StandardErrorPath</key>
      <string>/var/log/loopback-alias.log</string>
      <key>StandardOutPath</key>
      <string>/var/log/loopback-alias.log</string>
  </dict>
</plist>
""".format(lo_ip, lo_ip)

############################################################################
# Section: error messages

missing_workspace = """
Error> the workspace, {}, does not correspond to a directory in your root path.
The full path name which has been used is {}.

The contents of the full path is as follows:
"""

invalid_workspace = """
Error> the workspace, {}, has been located but the workspace does not contain
the files which are associated with the IVDI source tree.  The workspace should 
point to the top-level directory of the IVDI source tree.  The top level 
directory contents found in the workspace are:
"""

############################################################################
# Section: utility functions

def execute(cmd, capture_out = False):
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
            print("   output: {0}\n".format(out))

        if type(out).__name__ == "bytes":
            return out.decode("utf-8")
        else:
            return out

    except OSError:
        print("\nError> " + " ".join(cmd))
        sys.exit(1)
    except subprocess.CalledProcessError:
        print("\nError> " + " ".join(cmd))
        sys.exit(1)

def get_os_timezone():
    """
    This function will attempt to retrieve the time zone information for
    the system which can then be used to set a default timezone for our
    containers.
    """

    timezone = ""

    if os.path.islink('/etc/localtime'):
        timezone = re.sub(r".*zoneinfo/", "", os.readlink('/etc/localtime'))

    return timezone

def validate_workspace(workspace_path):
    """
    Validate the workspace that is pointed to by workspace_path.
    """

    # It must be a valid directory.
    if not os.path.isdir(workspace_path):
        print(textwrap.fill(missing_workspace.format(
                    os.path.basename(workspace_path), workspace_path), 
                    subsequent_indent="        "))

        basename = os.path.dirname(workspace_path)

        if os.path.isdir(basename):
            for name in os.listdir(basename):
                print("            {}".format(name))
        else:
            print("            {} is not a known path".format(basename))

        sys.exit(1)

    # Let's validate the source path.
    if os.path.isdir(workspace_path + "/src"):
        print("Creating an IVDI build container...")
    else:
        print(textwrap.fill(invalid_workspace.format(
                    os.path.basename(workspace_path)), 
                    subsequent_indent="        "))

        if os.path.isdir(workspace_path):
            for name in os.listdir(workspace_path):
                print("            {}".format(name))
        else:
            print("            {} is not a known path".format(workspace_path))

        sys.exit(1)

############################################################################
# Section: Commands for docker images

def pull_command(args):
    """
    Handle the processing of the pull command.  This will involve pulling
    the IVDI build image from artifactory.
    """

    if verbose:
        print("Command: pull")

    # Now we can execute the pull command.
    print("Logging into the artifactory server (use your intranet id):")
    execute(["docker", "login", "https://" + repo_name])
    execute(["docker", "pull", image_name + ":" + args.version])

############################################################################
# Section: Commands on Docker containers

def run_command(args):
    """
    Handle the processing of the run command.  This will involve creating a
    new IVDI build container.
    """

    if verbose:
        print("Command: run")

    # Ensure that this is a valid workspace.
    if not args.root:
        args.root = os.environ['HOME'] + "/workspace"

    workspace_path = args.root + "/" + args.workspace

    # Validate that the workspace exists.
    validate_workspace(workspace_path)

    # Build up the docker command.  

    name = ""
    if args.container:
        name = args.container
    else:
        name = args.workspace.replace(" ","_")

    cmd  = ["docker", "run", "-t",  "--interactive",
                "--hostname", name, "--name", container_prefix + name,
                "-e", "WORKSPACE="+args.workspace,
                "--privileged" ]

    cmd.append("-v")
    cmd.append(workspace_path + ":/workspace")
    cmd.append("-v")
    cmd.append(args.root + "/" + args.adks + ":/adks")
    cmd.append("-v")
    cmd.append(args.root + "/" + args.tools + ":/tools")
    cmd.append("-v")
    cmd.append(os.environ['HOME'] + ":/mnt")

    # We also need to add in our docker socket (if available).
    docker_sock = "/var/run/docker.sock"

    if os.path.exists(docker_sock):
        cmd.append("-v")
        cmd.append("{0}:{0}".format(docker_sock))

    # We want to build using a shared volume so that we can improve
    # performance (i.e. the layered file system is a little bit slow).
    cmd.append("-v")

    prefix = ""
    if args.volume:
        prefix = args.volume
    else:
        prefix = container_prefix + args.workspace + ".volume"

    cmd.append(prefix + ":/build")

    # Attempt to work out the timezone and add this to the environment
    # of the docker container.

    timezone = get_os_timezone()

    if timezone:
        cmd.append("-e")
        cmd.append("CONTAINER_TIMEZONE=" + timezone)

    # Potentially set up the autobuild environment variable.
    if args.autobuild:
        cmd.append("-e")
        cmd.append("AUTOBUILD=1")

    # Potentially set up the release environment variable.
    if args.release:
        cmd.append("-e")
        cmd.append("RELEASE_BUILD=1")

    # Create a temporary file system to help speed up the build.
    cmd.append("--tmpfs")
    cmd.append("/tmp:rw,exec,dev,suid,relatime,mode=755")

    cmd.append(image_name + ":" + args.version)

    # Now we can create the docker container.
    execute(cmd)

def start_command(args):
    """
    Handle the processing of the start command.  
    """

    if verbose:
        print("Command: start")

    execute( ["docker", "start", "--attach",  "--interactive", 
                                container_prefix + args.workspace] )

############################################################################
# Section: Sundry Commands

def instructions_command(args):
    """
    Display the build environment set-up instructions.
    """

    print("""
Overview:
---------

This script can be used to manage a docker IVDI build environment.  A single
docker image, available from artifactory, has been created to encapsulate the
build environment for the IVDI build.  A separate container can
then be created from this image for each workspace which you wish to build.

When the container is first created a full build will be completed and then
you will be dropped into a shell.  When you exit from the shell the container
will be stopped.  When the container is next started you will simply be dropped
into a shell (i.e. a full rebuild is not automatically done), allowing you to
build whatever you like.

Pre-Req:
--------

1. A recent version of docker must be installed and running.  The script has 
   been tested on a Macbook which is running docker v17.06.
2. The docker build image must be pulled from artifactory.  The 'pull' command
   option for this script will automate the pulling of the image.
3. You need a copy of the workspace that you are building.  

Options:
--------

The following options are available as a part of this script:

'pull':   Used to pull the build image from artifactory.  For example:
            'ivdi_build.py pull --version 9050'
'run':    Used to create a new container for a specific workspace.  When the 
          container is first started an initial build will take place.
          Once the build is completed you will be dropped into a shell.  The
          container will stop once you exit from the shell.  For example:
            'ivdi_build.py run --root ~/git.workspace IVDI'
'start':  Used to start a build container which is currently stopped.  You will
          be dropped into a shell in the build environment allowing you to 
          control which build commands are executed.  The container will stop
          once your exit from the shell.  For example:
            'ivdi_build.py start build.IVDI'
    """)

############################################################################
# Section: Argument Parser

def process_argv():
    """
    Create the command parser and then parse the arguments.
    """

    parser = argparse.ArgumentParser(description="This script is designed "
        "to help manage the IVDI build docker containers.  At a "
        "high level you should pull the build image into your environment, "
        "and then you can start an individual container for each workspace "
        "that you want to build.")

    parser.add_argument("-v", "--verbose", help="Display the commands as " 
                "they are executed.", action="store_true")

    subparsers = parser.add_subparsers(help='sub-command help')

    # pull command.....
    cmd = subparsers.add_parser("pull", help="This command will " 
        "pull the IVDI build image from artifactory.")

    cmd.add_argument("-v", "--version", help="The version of the build image "
        "e.g. 9050).  The list of available build images can be obtained "
        "directly from artifactory: "
        "https://na.artifactory.swg-devops.com/artifactory/webapp/#/artifacts/browse/tree/General/hyc-sec-isdi-team-docker-local.  If no version is "
        "specified the 'latest' version will be used.", 
        required=False,
        default="latest")

    cmd.set_defaults(func=pull_command)

    # run command....
    cmd = subparsers.add_parser("run", help="This command will create and "
        "start the docker container.  A shell will be established "
        "and the container will be stopped when the shell is exited.")

    cmd.add_argument("workspace", help="The name of the workspace used "
        "for this build.")

    cmd.add_argument("-r", "--root", help="The directory which houses all of "
        "your workspaces.  This will default to $HOME/workspace. ",
        required=False)

    cmd.add_argument("-v", "--version", help="The version of the build image "
        "(e.g 9050).", required=False, default="latest")

    cmd.add_argument("-a", "--adks", help="The directory which houses the "
        "adks repository, relative to the workspace root.",
        required=False, default="adks")

    cmd.add_argument("-t", "--tools", help="The directory which houses the "
        "tools repository, relative to the workspace root.",
        required=False, default="tools")

    cmd.add_argument("--volume", help="The volume name which will house the "
        "build atifacts.  This could be the name of a directory on the local "
        "file system.  If no volume name is specified the volume name will be "
        "constructed as: ivdi_build.{workspace}.volume", required=False)

    cmd.add_argument("--autobuild", help="Automatically perform the " 
        "initial build when the container is created.", 
        action="store_true")

    cmd.add_argument("--release", help="The container will be set up for "
        "a release build instead of a development build.",
        action="store_true")

    cmd.add_argument("--container", help="The name of the container which will be "
        "created (prefixed by 'ivdi_build.').  If no container name is specified "
        "the container name will be constructed as: ivdi_build.{workspace}", 
        required=False)

    cmd.set_defaults(func=run_command)

    # start command...
    cmd = subparsers.add_parser("start", help="This command will start a "
        "pre-created docker container.  A shell will be established "
        "and the container will be stopped when the shell is exited.")

    cmd.add_argument("workspace", help="The name of the workspace used "
        "for this build.")

    cmd.set_defaults(func=start_command)

    # instructions command...
    cmd = subparsers.add_parser("instructions", help="This command will "
        "display set-up instructions for the build environment.")

    cmd.set_defaults(func=instructions_command)

    return parser.parse_args()

############################################################################
# Section: Main Line

args    = process_argv()
verbose = args.verbose

args.func(args)

