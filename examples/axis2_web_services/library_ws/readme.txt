This example demonstrates the use of Axis2WSServerConnector and Axis2WSClientComponent.

These files are included in this example:
	* axis2_library_example.xml (configuration),
	* library_wsdl2.wsdl (wsdl describing the web service, its according to the WSDL2.0 standard),
	* booklist.csv (a comma-separated file containing author and title values)
	* readme.txt  (this document).

To run this example you must:
o Install IBM Security Verify Directory Integrator.
o Import the supplied configuration.

This package contains a single IBM Security Verify Directory Integrator configuration file - axis2_library_example.xml, 
which consists of two AssemblyLines:
1. "axis2wsclient" AssemblyLine uses an Axis2WSClientFunctionComponent and a FileSystemConnector. 
The FileSystemConnector reads information (author and title attributes) from the booklist.csv. A request is formed 
and Axis2WSClientFunctionComponent sends it to the server as a 'book' attribute. Then it accepts a response 
(whether the book is present in the library or not). The 'book' attribute has two elements - the book's title, and its author.
The configuration of the Axis2WSClientFunctionComponent can be seen by selecting IBM Security Verify Directory Integrator CE ->
 "AssemblyLine" section -> "axis2wsclient" AssemblyLine -> "Axis2WebServiceClientFunctionComponent", double clicking 
 on it and selecting the Connection tab.
2. "axis2wsserver" AssemblyLine uses an Axis2WSServerConnector. It receives requests ('book' attribute), and sends a 
response (whether the book is present in the library or not). The list of books contained in the library is created 
and populated in 'CreateBookMap' script component. If you wish you can add additional books to it.
The configuration of the Axis2WSServerConnector can be seen by selecting IBM Security Verify Directory Integrator CE ->
 "AssemblyLine" section -> "axis2wsserver" AssemblyLine -> "Axis2WebServiceServerConnector", double clicking 
 on it and selecting the Connection tab.
 
Some important points concerning the configuration of the two Axis2 components used:
1. "Axis2WebServiceServerConnector"):
  o In the "Advanced" section the "Service" parameter is set to "LibraryService"; you can try the other service 
  "SecondLibraryService" (it provides the same web service interface but uses SOAP 1.2 binding instead of SOAP 1.1).
  If you choose to make the change you must also modify the "TCP Port" value to 9988 (the port used by the other service).
  Also the Axis2WebServiceClientFunctionComponent's parameters "Service", "Endpoint" and "WSDL URL" must be modified (
  "SecondLibraryService", "SecondLibraryEndPoint" and "http://localhost:9988/library_wsdl2.wsdl" accordingly).
  o If Axis2WebServiceServerConnector receives a HTTP GET request it returns the contents of library_wsdl2.wsdl as a response.
  Otherwise if an HTTP POST request is recived it is parsed and processed normally.
2. "Axis2WebServiceClientFunctionComponent":
  o The "WSDL URL" parameter is set to "http://localhost:9998/library_wsdl2.wsdl"; however if  
  Axis2WSServerConnector is not on your machine or uses a different port this parameter must be changed.
3. The header of the SOAP messages is used for transporting time information. That way, when the client's request is 
  processed and the result is logged, we can see how much time was needed to receive and process the request.
4. The Axis2WebServiceServerConnector performs custom authentication of the clients that connect to it, so in the 
  Advanced section of the configuration of Axis2WebServiceClientFunctionComponent the parameters Username ant Password 
  are set (to 'user' and 'pass'). If you modify these values and try to run this example a SOAP fault is returned.

To run the demo:
1. Start the IBM Security Verify Directory Integrator CE.
2. Open the axis2_library_example.xml file.
3. Go to the "AssemblyLines" section.
4. Select the "axis2wsserver" AssemblyLine.
5. Click "Run".
6. Select the "axis2wsclient" AssemblyLine.
7. Click "Run".
8. Wait for completion of both AssemblyLines.
9. Check the contents of the generated log of "axis2wsclient" AssemblyLine.