This is an example parser written in java that shows some of the basic steps to implement a parser. Keep in mind that this is a sample that aims to give you a general idea of a parser implementation and if you develop a professional parser you may need to have more validations for the data.

To build the example follow these steps (execute these steps from <Install_Directory>\examples\parser_java directory):

1.Compile the parser source
        mkdir build
        javac -classpath <TDI>\jars\common\miserver.jar;<TDI>\jars\common\miconfig.jar -d build ExampleParser.java
		
		Note: You must use Java 5 or older version.

2.Modify the "tdi.xml" file if you changed anything in the source file. Otherwise keep the file as it is so that you can use it without having to modify anything.

4.Create a jar file with the compiled class and "tdi.xml" file.
        copy tdi.xml build
        cd build
        jar cvf ExampleParser.jar tdi.xml example_parser\ExampleParser.class

5.Copy the newly created "ExampleParser.jar" to the <IBM Security Verify Directory Integrator>\jars\parsers directory. This is the directory where all parsers are located.

6.Start ibmditk and verify that you have a new parser installed.

Note: 
 - The path separator is / on Unixes. 
 - The Classpath seperator is : on Unixes.
 - <Install_Directory> is the Install directory of IBM Security Verify Directory Integrator.
