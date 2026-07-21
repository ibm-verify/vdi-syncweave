0. You need to have SDI installed. I will refer to the installation dir as <SDI-INSTALL-DIR>

1. Download UpdateInstaller.jar and SDI-7.2-FP0008.zip. 

2. Upgrade to IBM JVM 8. JVM 8 is required for log4j v2.
See https://www.ibm.com/support/pages/720-ISS-SDI-LA0023
for one version of IBM JVM 8, do not use a very old version.
It is best to install and test this first.

3. Copy UpdateInstaller.jar to <SDI-INSTALL-DIR>/maintenance/UpdateInstaller.jar

3. Run applyUpdates.sh / applyUpdates.bat 
   On Linux, cd to <SDI-INSTALL-DIR> and give the command
   bin/applyUpdates.sh -update <PATH-TO-zip-File>/SDI-7.2-FP0008.zip

4. (Only on Linux) After upgrade, need to chmod  new shell script:
   chmod +x bin/updateLog4j.sh bin/removeAMC.sh

5. Use the new command updateLog4j.sh or updateLog4j.bat to move log4j-1.2.16.jar to the backup folder.
   This will also update bin/applyUpdates.sh or bin\applyUpdates.bat.
   bin/updateLog4j.sh

6. Use the new command removeAMC.sh or removeAMC.bat to remove the AMC and LWI folders.
   bin/removeAMC.sh

7. If using Windows Service and the solution folder is different from the installation folder:
win32_service/ibmdiservice.exe should be copied to the solution folder(s). 
Use OS commands (ex grep) to check for all instance of ibmdiservice.exe outside of ‘win32_service’ that should be manually updated.

8. If it is necessary to rollback the fixpack, a temporary batch file must be used.
On Windows:
  copy bin\applyUpdates.bat bin\applyUpdates2.bat
  bin\applyUpdates2.bat -rollback
  del bin\applyUpdates2.bat
On Linux
  cp bin/applyUpdates.sh bin/applyUpdates2.sh
  bin/applyUpdates2.sh -rollback
  rm bin/applyUpdates2.sh

9. Some info about log4j v2: Instead of etc/log4j.properties we now use etc/log4j2.xml.
The old etc/log4j.properties is still available for viewing in case special logging was added there, but it is not used.

The naming convention for old log files is a little bit different, with the version number in front of ".log".

Feel free to make local changes in etc/log4j2.xml, to try out exciting new log4j v2 features.
Here is a link describing the log4j v2 configuration format:
	https://logging.apache.org/log4j/2.x/manual/appenders.html 

By adding .gz to a filePattern, the rolled file will be compressed, saving disk space.

The date format %d{ISO8601} now has a T between date and time. %d{DEFAULT} behaves the same way as the old %d{ISO8601}.
