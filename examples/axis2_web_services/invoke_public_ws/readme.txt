This example demonstrates the use of Axis2WSClientComponent with a public web service.

These files are included in this example:
	* axis2_isbn_example.xml (configuration),
	* IBM_booklist.csv (a comma separated file containing ISBN, title and author for several books),
	* readme.txt  (this document).

To run this example you must:
o Install SyncWeave.
o Import the supplied configuration.

This package contains a single SyncWeave configuration file - axis2_isbn_example.xml, 
which consists of one AssemblyLine:
1. "axis2wsclient" AssemblyLine uses two Axis2WSClientFunctionComponent-s. It sends a request to a public web service 
and accepts a response. If you want to take a closer look at the service go to http://www.xmethods.net/ve2/index.po 
and search for a service  with name 'ISBNTest' and publisher 'VOORSPRONG'. 
(or you can just go to http://www.xmethods.net/ve2/ViewListing.po?key=425547). The request is a ISBN number (read from 
a file - IBM_booklist.csv, by a FileSystemConnector), and depending on its format (10 or 13 digits) one of the two 
Axis2WebServiceClient Function Components is used. Each of this components uses a different operation of the public 
Web Service to validate the ISBN's format.
The configuration of the Axis2WSClientFunctionComponent-s can be seen by selecting SyncWeave CE 
 -> "AssemblyLine" section -> "axis2wsclient" AssemblyLine -> "Axis2WebServiceClientFunctionComponent_10"/
 "Axis2WebServiceClientFunctionComponent_13", double clicking on it and selecting the Connection tab.

 To run the demo:
1. Start the SyncWeave CE.
2. Open the axis2_isbn_example.xml file.
3. Go to the "AssemblyLines" section.
4. Select the "axis2wsclient" AssemblyLine.
5. Click "Run".
6. Check the resulting list in the generated log of "axis2wsclient" AssemblyLine.