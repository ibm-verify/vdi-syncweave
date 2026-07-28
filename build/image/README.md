# Overview
The files in this repository are used to manage a container based build environment for the SyncWeave software.

Two main python scripts are supplied in this directory:

|Script|Description
|------|-----------
|build\_sdibuild\_image.py|This script is used to construct a new Docker build image.
|sdi\_build.py|This script is used to manage a build container, which is used to build the software.  A pre-constructed build image has been published to artifactory and will be automatically pulled when you first create the container.  If you use the 'pull' option of the script a login will be automatically attempted before the pull takes place.

Further details on how to execute each of the scripts can be obtained using the '-h' option of the script, for example:

```
sdi_build.py -h
```

# Quick Start

If you simply want to build the SDI software you should do the following:

1. Ensure that you have a Docker environment available.  The script has been tested with Docker Desktop on Mac, but should work on other platforms providing that bind mounting from the host is available, and a `docker` command is available.  
2. Ensure that you have a local copy of the following GitHub repositories:
	1. [SDI](https://github.ibm.com/sec-di/SDI)
	2. [adks](https://github.ibm.com/sec-di/adks)
	3. [tools](https://github.ibm.com/sec-di/tools)
3. Ensure that you have a copy of the [sdi_build.py](https://github.ibm.com/sec-di/SDI/blob/build/image/sdi_build.py) python script.  You will need to have python 3 installed on your local machine.  The script only requires a couple of basic python modules which should already be installed with python, but if any additional modules are required you can install them using the `pip3 install` command.
4. The build image is stored on artifactory and you need to authenticate to artifactory before you are able to pull the build image.  The authentication will take place as a part of the execution of the sdi_build.py python script, but you will need your credential information in order to be able to authenticate.  In order to obtain your API key you need to log onto [artifactory](https://na.artifactory.swg-devops.com/ui/#/home), and then edit your profile.  The API key which should be used will appear under the 'Authentication Settings' section.
4. Start the build container using something like the following command.  This command assumes that the IVDI repository has been cloned into the ~/git.workspace directory: 

    ```
    sdi_build.py run -r ~/git.workspace -v latest SDI
    ```

6. The sdi_build.py command will create a new container, named `isdi_build.SDI`, and drop you into a shell.  You can build the software from within this shell by executing the following commands.  When you exit the shell the docker container will stopped:

   ```
   ant
   ```
   
If you want to start the build container again (after exiting from the original shell), you can just execute the following command - which will drop you back into a shell within the build container:

```
sdi_build.py start SDI
``` 
