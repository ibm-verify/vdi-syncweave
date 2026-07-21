#!/usr/bin/env python3

#
# This script is used to handle the publication of a release to GitHub.
# The only non-standard python modules which is being used is the
# pygithub module, which can be installed with pip.
#

import argparse
import glob
import logging
import os
import re
import sys
from github import Github, Auth
from datetime import datetime

############################################################################
# Section: static variables

github_enterprise_url = "https://github.ibm.com/api/v3"
github_repository = "sec-di/SDI"

############################################################################
# Section: command line parsing

class from_environment(argparse.Action):
    """
    This class is a custom argument parser which will pull the default
    value from the specified environment variable.
    """

    def __init__(self, envvar, required=True, default=None, **kwargs):
        if envvar in os.environ:
            default = os.environ[envvar]
        if required and default:
            required = False
        super().__init__(default=default, required=required, **kwargs)

    def __call__(self, parser, namespace, values, option_string=None):
        setattr(namespace, self.dest, values)

def process_argv():
    """
    Create the command parser and then parse the arguments.
    """

    parser = argparse.ArgumentParser(description="This script is designed "
                                     "to publish a release to GitHub.")

    parser.add_argument("--debug", help="Turn on debugging output.",
                        required=False, action='store_true')

    subparsers = parser.add_subparsers(help='sub-command help')

    # Refresh command.
    cmd = subparsers.add_parser("publish", help="This command will "
                                "publish a new release to GitHub.")

    cmd.add_argument("--github-api-key", help="The API key which is "
                     "used to access GitHub.",
                     action=from_environment, envvar='GITHUB_API_KEY', required=True)

    cmd.add_argument("--source-path", help="The path of the directory which "
                     "contains the files to be published.",
                     action=from_environment, envvar='SOURCE_PATH', required=True)

    cmd.add_argument("--description", help="The details of the build, in markdown "
                     "format",
                     action=from_environment, envvar='DESCRIPTION', required=True)

    cmd.add_argument("--commit", help="The sha of the GitHub commit.",
                     action=from_environment, envvar='COMMIT', required=False)

    cmd.set_defaults(func=publish_command)

    # Prune command.
    cmd = subparsers.add_parser("prune", help="This command will "
                                "prune published releases from GitHub.")

    cmd.add_argument("--github-api-key", help="The API key which is "
                     "used to access GitHub.",
                     action=from_environment, envvar='GITHUB_API_KEY', required=True)

    cmd.add_argument("--releases-to-keep", help="The number of published "
                     "releases to keep.",
                     action=from_environment, envvar='RELEASES_TO_KEEP', default=5)

    cmd.set_defaults(func=prune_command)

    args = parser.parse_args()

    if "func" not in args:
        parser.print_help(sys.stderr)
        sys.exit(1)

    return args

############################################################################
# Section: Utility functions

def get_repository(args):
    """
    This function will return a handle to our repository.
    """

    auth = Auth.Token(args.github_api_key)
    gh = Github(auth=auth, base_url=github_enterprise_url)

    username = gh.get_user().login

    logging.info("Authenticated to GitHub as: {0}".format(username))

    return gh.get_repo(github_repository)

############################################################################
# Section: Prune

def prune_command(args):
    """
    Prune the olld releases from GitHub.
    """

    logging.info(
        f"Pruning all but the last {args.releases_to_keep} releases from GitHub.")

    repo = get_repository(args)

    # Retrieve and build a list of SPS releases.
    logging.info("Retrieving the current list of releases.")

    all_releases = repo.get_releases()
    releases = {}
    release_names = []

    for release in all_releases:
        if re.fullmatch(r'sps-ci-ivdi-\d{8}-\d{4}', release.name):
            release_names.append(release.name)
            releases[release.name] = release

    release_names.sort(reverse=True)
    del release_names[:int(args.releases_to_keep)]

    # Delete each release.
    for release_name in release_names:
        logging.info(
            f"Deleting the tag for the release: {releases[release_name].tag_name}")
        repo.get_git_ref(f"tags/{releases[release_name].tag_name}").delete()
        logging.info(f"Deleting the release: {release_name}")
        releases[release_name].delete_release()

############################################################################
# Section: Publish

def publish_command(args):
    """
    Publish the specified files to a GitHub release.
    """

    logging.info("Publishing the files to GitHub.")

    current_datetime = datetime.now()
    release_tag = current_datetime.strftime("%Y%m%d-%H%M")

    repo = get_repository(args)

    # Get commit SHA
    if hasattr(args, 'commit') and args.commit:
        commit = args.commit
    else:
        branch = repo.get_branch(repo.default_branch)
        commit = branch.commit.sha

    logging.info(f"Using commit SHA: {commit}")

    # Verify commit exists
    try:
        repo.get_commit(commit)
    except Exception as e:
        logging.error(f"Commit {commit} not found: {e}")
        return

    # Create tag and release
    logging.info(f"Creating release for tag: {release_tag}")
    try:
        release = repo.create_git_tag_and_release(
            tag=f"sps-ci-ivdi-{release_tag}",
            tag_message=f"sps-ci-ivdi-{release_tag}",
            release_name=f"sps-ci-ivdi-{release_tag}",
            release_message=args.description,
            object=commit,
            type="commit",
            draft=False,
            prerelease=True
        )
    except Exception as e:
        logging.error(f"An error occurred: {e}")
        return

    # Upload all files in the given source directory
    source_dir = "/workspace/app/SDI/artifacts"

    if not os.path.isdir(source_dir):
        logging.error(f"Source directory does not exist: {source_dir}")
        return

    logging.info(f"Uploading files from directory: {source_dir}")

    for filename in sorted(os.listdir(source_dir)):
        logging.info(f"Trying uploading: {filename}")
        file_path = os.path.join(source_dir, filename)

        if not os.path.isfile(file_path):
            continue  # Skip directories or non-regular files

        # Optional: skip files over 2GB (GitHub release limit)
        if os.path.getsize(file_path) > 2 * 1024 * 1024 * 1024:
            logging.warning(f"kipping {filename} — exceeds 2GB limit")
            continue

        # Initialize default content type
        content_type = "application/octet-stream"
        
        # Manually override for known split file patterns
        if filename.endswith(".zip"):
            content_type = "application/zip"
        elif filename.endswith(".tgz"):
            content_type = "application/gzip"
        elif filename.endswith(".txt"):
            content_type = "text/plain"
        elif filename.endswith(".html"):
            content_type = "text/html"
        else:
            content_type = "application/octet-stream"
        
        # Delete existing asset with same name (to avoid 422)
        for asset in release.get_assets():
            if asset.name == filename:
                logging.info(f"Deleting existing asset: {filename}")
                asset.delete_asset()

        try:
            logging.info(f"Uploading: {filename}")
            release.upload_asset(
                path=file_path,
                name=filename,
                content_type=content_type
            )
            logging.info(f"Uploaded: {filename}")
        except Exception as e:
            logging.error(f"Failed to upload {filename}: {e}")

############################################################################
# Section: Main Line

try:
    args = process_argv()

    # Set up the logging.
    level = logging.INFO
    if args.debug:
        level = logging.DEBUG

    logging.basicConfig(level=level)

    # Now we can process the command.
    args.func(args)

    logging.info("Finished")

except Exception as e:
    message = "An error occurred: {0}".format(e)
    logging.error(message)
    sys.exit(1)
