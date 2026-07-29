
This example contains the following files:
regexp_parser.xml, regexp_input.txt, xml_input.xml, expected_regexp_output.txt, expected_xml_output.xml, readme.txt, 
RegExpParser.jar

Here's the background information on this parser:

Regular Expression parser
The Regular Expression parser validates and parses Connectors' input/output against some regular expression. 
It uses the free Regular Expressions for Java library "gnu.regxep" available at 

1 http://www.mirrorservice.org/sites/ftp.freebsd.org/pub/FreeBSD/ports/distfiles/gnu.regexp-1.1.4.tar.gz  or 

2 http://www.mirrorservice.org/sites/master.us.finkmirrors.net/distfiles/md5/4b5a3722f1d814ee30591ad21b519d28/gnu.regexp-.1.4.tar.gz

Consult "gnu.regexp" documentation for the regular expression notation supported and for the library's specification.

Note:	The reference to the free Regular Expressions for Java library "gnu.regxep" available at 
 http://www.mirrorservice.org/sites/ftp.freebsd.org/pub/FreeBSD/ports/distfiles/gnu.regexp-1.1.4.tar.gz  or 
 http://www.mirrorservice.org/sites/master.us.finkmirrors.net/distfiles/md5/4b5a3722f1d814ee30591ad21b519d28/gnu.regexp-.1.4.tar.gz
 is provided as a convenience. IBM does not support code obtained from 	this site and is not responsible for the continued availability or any consequences of use of such code.

The Regular Expression parser is designed as a useful example that shows how to integrate 
a custom parser in the SyncWeave.


Functional specification:

Configuration
The parser provides the following parameters:
	class: com.ibm.com.di.parser.RegExpParser

	regularExpression:
		Specifies the regular expression the parser will use.
		
		Subexpressions are enclosed in parentheses, for example: "ab(c*)d(e*)f".
		When the parser is used in read mode, those subexpressions correspond to
		the Entry's Attributes (in the example above, "c*" corresponds to the first
		Attribute and "e*" corresponds to the second Entry's Attribute).

	attributeNames:
		Specifies the names of the Attributes delimited with semi-colons, for
		example, "Name;Value".

		The interpretation of this parameter depends on the parser mode:
		 > read mode: The names are used for the Attributes corresponding to the
			subexpressions of the regular expression. Mapping is done in the order
			of appearance, i.e. the first subexpression will correspond to an Attribute
			named with the first name from the "attributeNames" parameter, etc.)
		 > write mode: The names are used to define the output text. It is formed by
			concatenating the values of the Attributes enumerated in the
			"attributeNames" parameter.

		Input: A single line f	rom the input will correspond to a single Entry.
		 > If the line doesn't match the regularExpression then an Entry with no Attributes is
			returned.
		 > If the line matches the regularExpressionthen an Entry is populated with
			Attributes and returned. The number of Attributes assigned is equal to the
			number of subexpressions in the regularExpression and each Attribute's value is
			the substring of the input line that matches the corresponding subexpression.
		
		If the number of the names in the attributeNames parameter is less than the number
		of the subexpressions in the regularExpression parameter then Attribute names are
		added - as many as needed to make those numbers equal. The Attribute names
		added consist of the prefix "ATTR_NAME_" and the number of the Attribute name
		added (starting from 0), for example, ATTR_NAME_0, ATTR_NAME_1, ATTR_NAME_2, and so forth.
	
		Output: All Attributes enumerated in the attributeNames parameter that exist in
		the Entry are concatenated to form a single string (in the order they appear in the
		attributeNames parameter).
		 > If this string matches the regularExpression, it is printed on a single line in the output.
		 > If this string does not match the regularExpression, nothing is printed in the
		   output and the "no-match event" is logged.


Installation:

Do the following to install:
1. Create a new folder, named RegExpParser, in the jars subfolder of the SyncWeave
	installation directory.
2. Go to the the Regular Expressions for Java Web site (http://www.cacas.org/java/gnu/regexp/) and 
	download the package gnu.regexp-1.1.4.tar.gz
3. Extract the archive's contents keeping path information. Copy the file "gnu-regexp-1.1.4.jar" (placed in the lib folder) 
	to the newly created jars/RegExpParser folder.
4. Copy the RegExpParser.jar file from to the jars/RegExpParser folder.

The next time you start ibmditk, you can choose RegExpParser as a Parser type.

#######################################################################################################
##
##	Instructions for using this example
##
#######################################################################################################



Imagine that the regexp_input.txt file is an internal technical document of a software company. This 
technical document contains the major milestones in the development of an imaginary software product. 
The company regulations specify a particular format, which must be followed when inserting a new 
release/version Entry into the internal technical document. This format specifies that the version/release 
entry must be matched by the following regular expression: 
	.*([0-9][0-9][0-9][A-Z])-(Dept0[0-9]*)-(.*)
The first subexpression in parentheses is the version number, the second one is the department number and 
the third one is the description of the release.

To run this demo you must:
o install SyncWeave.
o install the Regular Expression Parser as a Base Template in SyncWeave (as described above)
o the file RegExpParser.jar has a precompiled version of parser as well as the neccessary tdi.xml and properties files.

Note:  you will still need to download regular expression for java package as mentioned above.

A single configuration file regexp_parser.xml is included in this package. It contains two AssemblyLines. 
These two AssemblyLines demonstrate regular expression input and output respectively:

"RegExp_Input": iterates through the lines of the regexp_input.txt file and outputs the Entries that matched the 
regular expression to the xml_output.xml file. You can check the output (xml_output.xml) file against the 
expected_xml_output.xml file from this package.

"RegExp_Output": iterates through the Entries from the xml_input.xml file and outputs those of them that matched 
the regular expression to the regexp_output.txt file. You can check the output (regexp_output.txt) file against 
the expected_regexp_output.txt file from this package.


JavaScript code is used to assign value to the helper "Hyphen" Attribute.


To run any of the AssemblyLines:
1. Start the SyncWeave Config Editor.
2. Open the regexp_parser.xml file.
3. Go to the "AssemblyLine" section.
4. Select the AssemblyLine you have chosen ("RegExp_Input" or "RegExp_Output").
5. Click "Run".
