
                    Fixed Daily Rolling File Appender example

1. Overview

The existing Daily Rolling File Appender never deletes files when rolling a new file.
So as part of the Security Verify Directory Integrator installation is shipped an example of
Fixed Daily Rolling File Appender which has the ability to specify the number of backup
files. 

The example is located in <installation_directory/examples/FixedRollingLogger/ folder
and contains:
        1) FixedDailyRollingFileAppender.java file - the source code of the Fixed Daily
           Rolling File Appender;
        2) fixeddailyroller.properties;
        3) tdi.xml
        4) readme.txt - this document

This file appender extends the functionality of the already provided Daily Rolling File
Appender by adding an additional parameter to its configuration - 'Number of files'. By
specifying this parameter the user can limit the number of the created backup log files.


2. Configuration

"File Path" parameter:
    The name of the file used for logging.
        Config Editor GUI control:
        - type: text field
        - default value: empty
        - Required: true

"Append to file" parameter:
    If true, then the log file will be opened in append mode;
    otherwise, it will be opened in truncate mode.
      Config Editor GUI control:
        - type: boolean
        - default value: false
        - Required: false

"Date pattern" parameter:
    This option determines the rollover schedule.The 'Date pattern' takes a
    string in the same format as expected by SimpleDateFormat. 
      Config Editor GUI control:
        - type: drop down;  The available values are:
          - '.'yyyy-MM - Rollover at the beginning of each month.
          - '.'yyyy-MM-dd -  Rollover at midnight each day.
          - '.'yyyy-MM-dd-HH - Rollover at the top of every hour.
          - '.'yyyy-MM-dd-HH-mm - Rollover at the top of every minute.
        - default value: ''.'yyyy-MM-dd'
        - Required: true

"Layout" parameter:
    Specifies log layout format.
      Config Editor GUI control:
        - type: drop down;  The available values are:
          - 'Pattern' - Output logs in specified pattern.
          - 'Simple' - Output logs in simple format.
          - 'HTML' - Output logs in HTML format.
          - 'XML' - Output logs in XML format.
        - default value: Pattern
        - Required: false

"Pattern" parameter:
    Specifies the log pattern. It is considered only if 'Layout' parameter is set
    to 'Pattern'.
      Config Editor GUI control:
        - type: drop down;  ;  The available values are:
          - '%d{DEFAULT} %-5p [%c] - %m%n'
          - '%d{HH:mm:ss} %p [%t] - %m%n'
          - '%p [%t] %c %d{HH:mm:ss,SSS} - %m%n'
        - default value: '%d{DEFAULT} %-5p [%c] - %m%n'
        - Required: false

"Log level" parameter:
    Specifies the level of logging.
      Config Editor GUI control:
        - type: drop down; The available values are:
          - 'INFO'
          - 'DEBUG'
          - 'WARN'
          - 'ERROR'
          - 'FATAL'
        - default value: 'INFO'
        - Required: false
        
"Log enabled" parameter:
    Enables the logger.
      Config Editor GUI control:
        - type: boolean
        - default value: false
        - Required: false
        
"Character Encoding" parameter:
    Specifies the character encoding.
      Config Editor GUI control:
        - type: text field
        - default value: empty
        - Required: false
        
"Number of Files" parameter:
    Configures the number of files to be backed up.
      Config Editor GUI control:
        - type: number
        - default value: none
        - Required: false


3. Using the Fixed Daily Rolling File Appender

To build the example follow these steps (execute these steps from <installation_directory>\examples\FixedRollingLogger directory):

1.Compile the connector source
        mkdir build
        javac -classpath <installation_directory>\jars\common\miserver.jar;<installation_directory>\jars\common\miconfig.jar;<installation_directory>\jars\3rdparty\others\log4j-1.2.16.jar;<installation_directory>\jars\common\tdiresource.jar -d build FixedDailyRollingFileAppender.java

2.Modify the "tdi.xml" file if you changed anything in the source file

3.Create NLS folder and put fixeddailyroller.properties file in it.
        mkdir build\NLS
        copy fixeddailyroller.properties build\NLS\fixeddailyroller.properties

4.Create a jar file with the compiled class and "tdi.xml" file.
        copy tdi.xml build
        cd build
        jar cvf FixedDailyRollingFileAppender.jar .

5.Copy the newly created "FixedDailyRollingFileAppender.jar" to the <installation_directory>\jars\FixedRollingLogger directory
        mkdir <installation_directory>\jars\FixedRollingLogger
        copy FixedDailyRollingFileAppender.jar <installation_directory>\jars\FixedRollingLogger

6. Open the CE and add a logger

Then there are two ways to use the newly provided Fixed Daily Rolling File Appender
from the CE:
    1) Add a logger for a solution from the 'Solution Settings' option and then choose 'Log Settings' tab
    2) Add a logger for an assembly line from its 'Log Settings' option

With either way you will see a window from where you can insert a logger. After you add
the Fixed Daily Rolling File Appender you will be able to modify all the parameters
described in 2.
