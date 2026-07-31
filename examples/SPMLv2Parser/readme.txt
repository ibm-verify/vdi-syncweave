SPMLv2Parser Example.

Overview:
---------
The example contains two SyncWeave configurations - "SPMLExample" and "SPMLStreamSearchExample". 
The first configuration is SyncWeave solution illustrating the ability to perform SPML communication over HTTP. 
It is a general solution, which could handle different SPML requests.
The "SPMLStreamSearchExample" is a solution, developed to handle only SPML search requests.
It provides the opportunity to flush large result sets on smaller parts thus lowering the SyncWeave Server memory overhead.
For this purpose communication over TCP is used.  
 

SPMLExample
-----------
This SyncWeave configuration consists of three AssemblyLines. The first two AssemblyLines act as the SPMLv2 clients
and contain HTTP Client Connectors configured with SPMLv2 Parsers. The first AssemblyLine (SendAddRequest) 
creates a simple SPMLv2 Batch Add Request and sends it over http. The second AssemblyLine (SendSearchRequest)
creates a simple SPMLv2 Search Request and sends it over http. The third AssemblyLine (Server) 
contains HTTP Server Connector and acts as an SPMLv2 Server. It receives the Add/Search Request from 
the first two AssemblyLines parses it again with the SPMLv2 Parser and performs a real LDAP 
Add/Search operation by using one of the LDAP Connectors. At the end, an Add/Search Response is returned 
back to the calling AssemblyLine and written by the SPMLv2 Parser.

SPMLStreamSearchExample
-----------------------
The 'SPMLStreamSearchExample.xml' configuration contains two AssemblyLines - 'Server' and 'SendSearchRequest'.
The 'SendSearchRequest' Assembly Line acts as an SPMLv2 client and for this purpose implements a script connector in Iterator mode (client).
In the selectEntries() function the 'client' connector opens a connection to the already running TCP server,
creates a simple SPMLv2 Search Request, using an SPMLv2 Parser, and sends it over TCP. Note that the Connector sends the length of the 
request together with '\n' symbol as delimiter to the server before sending the request itself. This is required by the 
TCP server in order to distinguish between the different requests coming through the stream.
Using the getNextEntry() function the client reads a single Search response on each iteration instead of the whole batch response.
Afterwards the AL stores the SPMLv2 Search Responses to a flat file using a File System Connector (SearchResponse).
The other Assembly Line  - 'Server' - contains a TCP Server connector(TCPServer) and acts as an SPMLv2 Server. 
The 'TCPServer' receives the Search request from the previous AL and handles it via SPMLv2 Parser in the 'After GetNext' hook.        
Afterwards the parsed result is forwarded to LDAP Connector(performSearch), which performs a real LDAP Search operation.
The LDAP connector is also configured with a SPMLv2 Parser, which sends the SPML Search Responses over the TCP socket.
When more than one Entry is returned to the LDAP connector, then each one is send separately to the client as single search response. 
  

This example consists of the following files:

SPMLExample.xml �SyncWeaver configuration file for handling SPML Add and Search requests
SPMLStreamSearchExample - SyncWeave configuration file for handling SPML Search requests and processing stream search results  
AddRequest.xml - The file which contains the Add Request.
SearchRequest.xml - The file which contains the Search Request.
readme.txt � this file.

How to run the sample SyncWeave configs:
----------------------------------

SPMLExample
-----------
1.	Start SyncWeave Config Editor (ibmditk.bat).
2.	Open the SPMLExample.xml file.
3.	Reconfigure the LDAP Connector to connect to your LDAP Server.
4.	Start Server with Run, not with Step Mode.
5.	Start SendAddRequest with Run, not with Step Mode.
6.	Start SendSearchRequest with Run, not with Step Mode.

If you start the SendAddRequest AL more than once the LDAPConnector will ignore all the entries that were already in the LDAP Server.

SPMLStreamSearchExample
-----------------------
1.	Start SyncWeave Config Editor (ibmditk.bat).
2.	Open the SPMLStreamSearchExample.xml file.
3.	Reconfigure the LDAP Connector to connect to your LDAP Server.
4.	Start Server with Run, not with Step Mode.
5.	Start SendSearchRequest with Run, not with Step Mode.
