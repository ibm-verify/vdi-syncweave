#!/usr/bin/env python3

#
# Copyright IBM Corp. 2023

#############################################################################

# This script is used to prune the github repository of all older nightly 
# builds.  This just saves on disk space on the github repository.  We were
# originally using the git client to handle this, but have since decided to
# shift to the github developers API as there were some problems with git,
# and the documentation for GitHub states that you need to delete the 
# release artifacts before deleting a release.

import sys
import requests
import json
import re
import urllib3

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

#############################################################################
# Global variables.

# The maximum number of builds to keep.
max_builds = 5

# The build prefix.  This will be set in the main line.
prefix = None

#############################################################################
# Send a request to GitHub.

def send_json_request(function, user, api_key, url, ignore_failure):
    """
    Send the specified request to the RTC server and return the JSON response.
    """

    headers = {
        'Accept': 'text/json',
        'Content-type': 'text/json'
    }

    rsp = function(url, verify=False, headers=headers, auth=(user, api_key))

    if rsp.status_code != 200 and rsp.status_code != 204:
        print("Error> failed to retrieve the data ({}).".format(url))
        print(rsp.status_code)
        print(rsp.text)

        if not ignore_failure:
            sys.exit(1)

    # Return the results.
    if rsp.text:
        return json.loads(rsp.text)
    else:
        return ""

#############################################################################
# Sort based on the key.

def sortKey(elem):
    # Extract the build number using the build prefix.
    pattern  = "{0}(.*)".format(prefix)
    result   = re.search(pattern, elem)

    return int(result.group(1))

#############################################################################
# Process a repository for releases.

def process_repo_releases(user, api_key, repo, release_name, pattern_string):
    """
    Prune the specified repository.
    """

    release_names  = []
    release_hash   = {}
    builds_to_keep = max_builds

    print("Processing the {0} repository {1}....".format(repo, pattern_string))

    if release_name is None:
        print("Pruning the github releases (leaving the last {0} travis builds)".\
                format(builds_to_keep))

        #
        # Compile our pattern matching expression.  This is used to match our 
        # travis builds and ensure that we don't delete any valid releases.
        #

        pattern = re.compile(pattern_string)

        #
        # Retrieve the list of releases from github.
        #

        releases = send_json_request(requests.get, user, api_key, 
            "https://github.ibm.com/api/v3/repos/sec-di/{0}/releases?per_page=100".format(repo),
            False)

        #
        # Build up a sorted list of matching releases.
        #

        for release in releases:
            if release['name'] is not None and pattern.match(release['name']):
                release_names.append(release['name'])
                release_hash[release['name']] = release

        release_names.sort(reverse=True, key=sortKey)

        print("All releases: {}".format(release_names))

    else:
        print("Pruning the github release: {}".format(release_name))

        #
        # Retrieve the list of releases from github.
        #

        releases = send_json_request(requests.get, user, api_key, 
            "https://github.ibm.com/api/v3/repos/sec-di/{0}/releases".format(repo),
            False)

        #
        # Build up a sorted list of matching releases.
        #

        for release in releases:
            if release['name'] is not None and release['name'] == release_name:
                release_names.append(release['name'])
                release_hash[release['name']] = release

        builds_to_keep = 0

    #
    # Now that we have the sorted list of release names we can work out which
    # releases need to be deleted.
    #

    print("\nDeleting:")

    for to_delete in release_names[builds_to_keep:]:
        print(to_delete)

        #
        # Delete the assets.
        #

        for asset in release_hash[to_delete]['assets']:
            print("    {}".format(asset['url']))

            send_json_request(
                    requests.delete, user, api_key, asset['url'], True)

        #
        # Delete the release and associated tag.
        #

        send_json_request(requests.delete, user, api_key, 
                release_hash[to_delete]['url'], True)
        send_json_request(requests.delete, user, api_key, 
            "https://github.ibm.com/api/v3/repos/sec-di/{0}/git/refs/tags/{1}".
                    format(repo, to_delete), True)

    #
    # We also want to delete any untagged-xxx tags as these are automatically
    # created by Travis and we don't need them.
    #

    print("Cleaning tags....")

    tags = send_json_request(requests.get, user, api_key,
            "https://github.ibm.com/api/v3/repos/sec-di/{0}/tags".format(repo),
            False)

    for tag in tags:
        if tag['name'].startswith("untagged-"):
            print("    {}".format(tag['name']))
            send_json_request(requests.delete, user, api_key, 
                "https://github.ibm.com/api/v3/repos/sec-di/{0}/git/refs/tags/{1}".
                    format(repo, tag['name']), True)

#############################################################################
# Process a repository for tags.

def process_repo_tags(user, api_key, repo, pattern_string):
    """
    Prune the specified repository.
    """

    release_names  = []
    release_hash   = {}
    builds_to_keep = max_builds

    print("Processing the {0} repository {1}....".format(repo, pattern_string))

    print("Pruning the github releases (leaving the last {0} travis builds)".\
                format(builds_to_keep))

    #
    # Compile our pattern matching expression.  This is used to match our 
    # travis builds and ensure that we don't delete any valid releases.
    #

    pattern = re.compile(pattern_string)

    #
    # Retrieve the list of tag from github.
    #

    releases = send_json_request(requests.get, user, api_key, 
            "https://github.ibm.com/api/v3/repos/sec-di/{0}/tags?per_page=100".format(repo),
            False)

    #
    # Build up a sorted list of matching tags.
    #

    for release in releases:
        if release['name'] is not None and pattern.match(release['name']):
            release_names.append(release['name'])
            release_hash[release['name']] = release

    release_names.sort(reverse=False)

    print("All tags: {}".format(release_names))

    #
    # Now that we have the sorted list of release names we can work out which
    # releases need to be deleted.
    #

    print("\nDeleting:")

    for to_delete in release_names[builds_to_keep:]:
        print(to_delete)

        #
        # Delete the tag.
        #

        send_json_request(requests.delete, user, api_key, 
            "https://github.ibm.com/api/v3/repos/sec-di/{0}/git/refs/tags/{1}".
                    format(repo, to_delete), True)

#############################################################################
# Main line.

#
# Check the command line usage.
#

if len(sys.argv) != 4:
    print("usage: {} [github user] [github api key] [build prefix]".format(\
                        sys.argv[0]))
    sys.exit(1)

user         = sys.argv[1]
api_key      = sys.argv[2]
prefix       = sys.argv[3]
pattern      = "{0}.*".format(prefix)
release_name = None
repos        = [ "SDI" ] 

for repo in repos:
    process_repo_releases(user, api_key, repo, release_name, pattern)

    if release_name is None:
        process_repo_tags(user, api_key, repo, pattern)

sys.exit(0)

