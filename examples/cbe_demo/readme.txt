--------------------------------------------------------------------
CBE DEMO - Generation of Common Base Events(CBE) using SDI
--------------------------------------------------------------------
This example demonstrates how various SDI components can be used together
to generate a CBE event object, a CBE XML, a CBE log file and finally to emit
a CBE event to a CEI (Common Event Infrastructure) server.

The solution demo contains the following files:
cbe_demo.xml          -> The config XML file.
event_config.txt      -> The input file that contains the parameters needed to create a
                         CBE event object.
wsnt-axis-samples.jar -> The jar file containing Serializers for CommonBaseEvent object.
wsdl/ folder          -> Contains the wsdl definitions for CEI Web Service and Common Base 
                         Event XSD.
cbe.xml               -> Sample CBE Event representation in XML format.
cbe_log.xml           -> Sample CBE log XML.

------------------------------
SOLUTION DETAILS(cbe_demo.xml)
-------------------------------
The cbe_demo.xml config file contains two AssemblyLines: 

generateCBELog:  This AssemblyLine generates a CBE event, and then writes out that event as 
                 a CBE xml and also as a CBE Log xml file. 
sendCBEEvent  :  This AssemblyLine generates a CBE event, and then emits that event out to a 
                 CEI Webservice which in-turn sends that event to the CEI server.

-----------------------------
AssemblyLine - generateCBELog
------------------------------
The function of this AssemblyLine is to read the event parameters from event_config.txt file
and generate a CBE Event using the CBEGeneratorFC function component. Then this assembly line
writes the generated CBE event as a CBE XML and as a CBE Log XML. The flow of the AssemblyLine
is as follows: 
   1) readFile: The readFile is a file system connector (iterator mode) that reads the event 
      parameters from the event_config.txt file and maps them into the work entry. The "filePath" 
      variable points to the event_config.txt file. 
   2) generateCBE: This was the newly introduced CBE Generator FC in SDI v6.1. This FC 
      exposes the standard CBE attributes in its Output Map. Attributes from the work entry are 
      mapped to suitable elements in the CBE Generator FC's output map. For in-depth details on the 
      various CBE attributes and their respective values, refer the SDI Reference Guide
      or the CBE Specification. The Input Map of this FC has two attributes - the "event" attribute
      that contains the CBE Event object and the "eventXml" attribute that contains the CBE event
      in its xml representation. It is this "eventXml" that gets finally written out by this AL
      as cbe.xml file.
   3) writeFile: The writeFile is a file system connector (add only mode) that writes out the eventXml
      as a "cbe.xml" file. This connector has a Line Reader parser attached with the parsers "attributeName"
      set to "eventXml".
   4) getLog: This is a script component that generates the CBE Log xml by calling the CBEGeneratorFC.getCBELogXml( )
      API and passing the CBE event object as a parameter. See the javadocs of CBEGeneratorFC for more details.
      The generated CBE Log xml is then put into the work entry's "logXML" attribute.
   5) writeLogFile: The writeLogFile is a file system connector (add only mode) that writes out the log Xml
      as a "cbe_log.xml" file. This connector has a Line Reader parser attached with the parsers "attributeName"
      set to "logXML".

In this way - this Assembly Line makes use of the "Entry to CBE FC" - also called the CBEGenerator FC to 
generate a CBE event and then write it out to a CBE XML file and a CBE Log XML file. 


-----------------------------
AssemblyLine - sendCBEEvent
------------------------------
CEI Setup  Details
------------------
SDI users can use SDI to emit/receive CBE events directly to the IBM CEI Server component. 
Currently, the IBM CEI server is a component that is shipped along with IBM WebSphere Process Server 
version 6.0. For an external java application (not running inside WAS), the only way to emit events 
to a CEI server is to make use of the TEC web service available at:  
https://cs.opensource.ibm.com/projects/mainstream/   

Download the 3.9.0-TIV-TEC-FO0001.zip package and unzip it. The unzipped package will contain another
zip file called tec-390-fo1-docs.zip which contains the documented steps to install and configure
the CEI WebServices Receiver. Install and deploy the CEI WebServices Receiver on your IBM WebSphere
Process Server and then run this SDI AssemblyLine to emit CEI events to this WebService.

This web service makes use of WS Notification to receive CBE events from external applications, 
and then making use of the IBM CEI SDK, transmits these CBE events to the CEI Server. This web service 
does not currently provide any means to consume or subscribe to events.

AssemblyLine Details - sendCBEEvent
-----------------------------------
The function of this AssemblyLine is to read the event parameters from the event_config.txt file, 
generate a CBE event using the CBEGeneratorFC function component and then emit this CBE event
to a CEI Web service using SDI's web service components. In essence the flow is as follows: 

Read Event Parameters --- >  Generate CBE Event --- > Create a Soap Event message  --- > Send a WS Notification 
                                                                                           to CEI WebServer
                                                       Event Received by CEI Server  < ----------|

Note: Before running this AL, copy the wsnt-axis-samples.jar file into the <TDI_Install_Dir>/jars folder.
Also update the providerURL parameter of the invokeWS FC to point to the CEI WebService WSDL URL. 
To ensure that this URL is valid - paste this URL onto the browser and you must see the 
message "Hi there, this is a Web service!".

The following components are present in the AssemblyLine:
   1) ReadCBEevent: The ReadCBEevent is a file system connector (iterator mode) that reads the event 
      parameters from the event_config.txt file and maps them into the work entry. The "filePath" 
      variable points to the event_config.txt file. 
   2) testCbeGenerator: This was the newly introduced CBE Generator FC in SDI v6.1. This FC 
      exposes the standard CBE attributes in its Output Map. Attributes from the work entry are 
      mapped to suitable elements in the CBE Generator FC's output map. For in-depth details on the 
      various CBE attributes and their respective values, refer the SDI Reference Guide
      or the CBE Specification. The Input Map of this FC has two attributes - the "event" attribute
      that contains the CBE Event object and the "eventXml" attribute that contains the CBE event
      in its xml representation. The CBE Event "event" is added to the work entry by the FC (See Input Map).
   3) prepareParamsForWS: This is a script component that extracts the "event" object from the work
      entry and prepares a notification array containing a topic, the event and information about the 
      producer of the event. The IP address mentioned here is just an information field, and can be set
      to any value. Finally, this script component sets the notification array into the work entry
      as a "event_as_notification" attribute.
   4) eventToSoap: This is the Axis Java-To-Soap Function Component that converts a given Java object to
      a Soap message. The "event_as_notification" attribute is sent to this function component (See Output Map)
      and its soap converion is received as "xmlString" attribute (See Input Map). This "xmlString" attribute
      is then added to the work entry. Note the config parameters for this component: 
      WSDL URL: Points to the wsdl in the examples/cbe_demo/wsdl folder.
      SOAP Operation: Notify (Since the CEI WebService is based on WS-Notifications)
      Operation Parameter: event_as_notification
      For most typical scenarios users will not need to change any parameters for this component.
   5) watchEventAsSoap: This is a simple script component that prints the "xmlString" attribute value so that
      users can see the SOAP message which is going to be sent by the next component to the CEi WebService.
   6) invokeWS: This is the final component that actually emits the CBE Event as a SOAP message to the CEI
      Webservice. The SOAP message is encompassed into the "xmlString" attribute. 
      NOTE: The users must modify the providerURL parameter of this component to point to their CEI WebService 
      WSDL URL. To ensure that this URL is valid - paste this URL onto the browser and you must see the 
      message "Hi there, this is a Web service!".
  
For more information on CBE, CEI and Autonomic Computing visit the following URLs:
http://www-128.ibm.com/developerworks/webservices/library/ws-cbe/
http://www-128.ibm.com/developerworks/library-combined/ac-cei/
http://www-128.ibm.com/developerworks/autonomic/books/fpy0mst.htm#ToC_91
-------------
END OF README
-------------    