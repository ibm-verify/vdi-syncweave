This is an example connector written in java that shows how to implement a connector that supports Iterator, AddOnly, Update, Lookup and Delete mode.

To build the example follow these steps (execute these steps from <Install_Directory>\examples\connector_java directory):

1.Compile the connector source (you need JDK 1.5 or newer)
        mkdir build
        javac -classpath "<SDI>\jars\common\miserver.jar;<TDI>\jars\common\miconfig.jar" -d build DirectoryConnector.java

2.Modify the "tdi.xml" file if you changed anything in the source file

3.Create NLS folder and put idi_conn_sample.properties file in it.
        mkdir build\NLS
        copy "<SDI>\examples\connector_java\idi_conn_sample.properties" .\NLS 

4.Create a jar file with the compiled class and "tdi.xml" file.
        copy tdi.xml build
        cd build
        jar cvf myconn.jar .

5.Copy the newly created "myconn.jar" to the <SyncWeave>\jars\connector directory

6.Start ibmditk and verify that you have a new connector installed

Note: 
 - The path separator is / on Unixes. 
 - The Classpath seperator is : on Unixes.
 - <Install_Directory> is the Install directory of SyncWeave.
