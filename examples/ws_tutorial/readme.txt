SyncWeave Web Services Tutorial
=========================


Overview
This guide describes the main usage scenarios for the new SyncWeave Web Services package. For each usage scenario an actual example SyncWeave AssemblyLine is set up. You can find the AssemblyLines which are set up in this guide in "Public Web Service invocation example.xml", "Web_Service_Connectors_in_Server_Mode_example.xml" and "Web_Service_Connectors_in_Server_Mode_SSL_example.xml" SyncWeave configurations which are included in this package. That is why in order to set up these examples yourself you can either follow this guide's instructions or you can load the respective SyncWeave configuration and use the AssemblyLines straight away.


I.	Invoking a remote web service
-------------------------------------
What follows is a detailed description of how to set up an AssemblyLine for invoking a remote web service. The web service accessed in this example is called "DelayedStockQuote" . This service is listed on www.xmethods.com (http://www.xmethods.com/ve2/ViewListing.po;jsessionid=dS2wFEseXG0PVz7-3NE8SKHR(QHyMHiRM)?key=uuid:41D558B6-61E2-8BBF-2886-467FA13F8255). What this service does is take an stock symbol (for example "IBM") as input, lookup the latest data for it on the stock exchange and then return the that data.
The "Invoke_DelayedStockQuote" AssemblyLine can be found in the "Public Web Service invocation example.xml" SyncWeave configuration.

Here is what you have to do in order to access the "DelayedStockQuote" web service:
1.	Create a new AssemblyLine. You can name it "Invoke_DelayedStockQuote".
2.	Add an Complex Types Generator function component to this AssemblyLine. You can name it "ComplexTypesGenerator".
a.	Note: The "DelayedStockQuote" web service uses some proprietary complex types (as most real-world web services do), so that it can return the stock quote in a structured way. These proprietary complex types are not directly supported by the SyncWeave Web Service components. That is why the SyncWeave Web Service components need Java classes which implement these proprietary complex types and which are able to serialize (turn a Java object representing a web service parameter, for example, into an XML string of characters) and deserialize (turn an XML string of characters into a Java object representing a web service return parameter, for example) these proprietary complex types. The Complex Types Generator function component is provided with the sole purpose of generating these Java classes and packing them into a JAR file. This function component (FC) is supposed to be used in design mode only, not during runtime. That means that you add this FC to your AssemblyLine, use it to generate the JAR file you need and then remove it from your AssemblyLine. But even of you leave this FC in your AssemblyLine, it will not disturb your AssemblyLine flow, because its runtime behavior is to do nothing.
3.	Fill in the "WSDL URL" parameter value. This value must be the URL pointing to the WSDL that describes the web service you want to invoke. In the case of the "DelayedStockQuote" web service this value must be http://ws.cdyne.com/delayedstockquote/delayedstockquote.asmx?wsdl 
4.	Fill in the "JAR file name" parameter value. This value must be the name of the JAR file you want to create. Since the SyncWeave Web Service components will later need to load the classes packed in this JAR file, this JAR file will ultimately have to be put in a location where SyncWeave can load it. In general if you drop a JAR file in the user-defined "jars" folder or a subfolder and then restart the Config Editor or the SyncWeave Server or both (whichever you need) this JAR file will be loaded. You can (a) create this JAR file anywhere you like and later copy it under the user-defined "jars" folder, or you can (b) create the JAR file in a location under the user-defined "jars" folder in the first place by filling in the "JAR file name" FC parameter value accordingly. In our case we will use option (b) and set the parameter value to "jars/delayed_stock_quote.jar". The already generated "delayed_stock_quote.jar" file can be found in the "ws_tutorial/invoke_public_ws" folder.
5.	Fill in the "JDK path" parameter value. This is an optional parameter. If filled in its value must be the file system path to an installation of a Java Developer Kit (JDK). The ComplexTypesGenerator FC needs access to a JDK, because it uses the Java Compiler (javac) executable as well as the JAR utility (which creates JAR files) executable. If this parameter value is left blank then this FC assumes that these two JDK executables can be found on the system executable path (specified by the PATH environment variable on Windows, Linux and UNIX). If on your machine these two JDK executables are not on the system executable path you have to fill in this parameter value accordingly.
6.	The "Generate Java Source Files" check-box. The ComplexTypesGenerator FC performs 2 distinct steps before actually generating the JAR file.
(1) it generates the Java source files based on the WSDL (using the AXIS WSDL2Java tool) and writes them to the "<SOLUTION_FOLDER>/temp/ComplexTypesJavaFiles" folder and (2) then it compiles these generated Java source files into Java class files and writes them to the "<SOLUTION_FOLDER>/temp/ComplexTypesClassFiles" folder. The first of these 2 steps is optional. If the "Generate Java Source Files" check-box is checked then the first of these 2 steps is executed; otherwise this step is omitted. Note that if the first step is executed then any previously generated (either automatically or manually) Java source files in that folder will be overwritten by this step. That is why if you have previously generated the Java source files and have edited them manually you can uncheck this check-box so that your changes won't be overwritten by the automatic generation of Java source files. For our case we will leave the check-box at its default setting (checked).
7.	Click the "Generate complex types" button.
8.	At the information message click "OK".
9.	If an error message is displayed and you need more information about the error that has occurred, please see the troubleshooting section of this guide.
10.	If all goes well a message box indicating that the JAR file has been successfully created is displayed. Click the "OK" button on this message box.
11.	Now that the complex types JAR file is created we no longer need the Complex Types Generator FC in our AssemblyLine. Remove this FC from the AssemblyLine.

12.	Add an Axis Easy Invoke Soap Web Service function component to this AssemblyLine. You can name it "AxisEasyInvokeSoapWS".
13.	Fill in the "WSDL URL" parameter value. This value must be the URL pointing to the WSDL that describes the web service you want to invoke. In the case of the "DelayedStockQuote" web service this value must be http://ws.cdyne.com/delayedstockquote/delayedstockquote.asmx?wsdl 
14.	Fill in the "SOAP Operation" parameter value. The WSDL for a web service defines one or more SOAP operations. You have to specify which of these SOAP operations you want to invoke. In the case of the "DelayedStockQuote" web service we want to invoke the "GetQuote" SOAP operation, so we fill in "GetQuote" for the parameter value.
15.	Fill in the "Complex Types" parameter value. The Complex Types Generator FC uses the AXIS WSDL2Java tool for generating the Java source files from a WSDL. You can read the AXIS user documentation for more detailed information about this process. The "Complex Types" parameter value is a list of all the generated complex types which are used by the SOAP operation specified in the "SOAP Operation" parameter. If you are not certain which classes are used you can specify all the generated classes. In the case of the "GetQuote" web service we fill in the parameter value with (place each class name on a separate line):
com.cdyne.ws.DataSet
com.cdyne.ws.GetQuickQuote
com.cdyne.ws.GetQuickQuoteResponse
com.cdyne.ws.GetQuote
com.cdyne.ws.GetQuoteDataSet
com.cdyne.ws.GetQuoteDataSetResponse
com.cdyne.ws.GetQuoteDataSetResponseGetQuoteDataSetResult
com.cdyne.ws.GetQuoteResponse
com.cdyne.ws.QuoteData
16.	Fill in the "Operation Parameters" FC parameter value. A SOAP operation can have zero, one or more parameters. The Axis Easy Invoke Soap Web Service FC expects to find the SOAP operation parameters in the work Entry. This FC parameter is a list of the names of the Attributes in the work Entry, which store the SOAP operation parameters. Note that the order of the Attribute names is important. The order must exactly match the order of the SOAP operation parameters as defined in the WSDL. Separate different Attribute names with spaces. In the case of the "GetQuote" SOAP operation there is only one parameter. That is why we only fill in only one Attribute name & we name it "param". This instructs the Axis Easy Invoke Soap WS FC to get the input parameter from the "param" Attribute of the work Entry.
17.	In the Output Map of the Axis EasyInvoke Soap WS add the "param" Attribute.
18.	In the Input Map of the Axis Easy Invoke Soap WS add the "return" Attribute. The Axis Easy Invoke Soap WS FC stores the response/return value from the web service in the "return" Attribute of the work Entry.
19.	We store the "param" Attribute in the work Entry using Javascript code in the AssemblyLine Prolog. Let us suppose that we are looking for the stock quote for "IBM". In this case you have to put the following Javascript code in the AssemblyLine Prolog("After Init" hook in the AssemblyLine settings menu):
var param = Packages.com.cdyne.ws.GetQuote();
param.setStockSymbol("IBM");
param.setLicenseKey("0");
var entry = system.newEntry();
entry.setAttribute("param", param);
task.setWork(entry);
20.	Add a new script component called "printResult" after the Axis Easy Invoke Soap WS FC. This script component will be used to print in a readable way the results returned by the "DelayedStockQuote" web service. Put the following Javascript code in this script component:
var attr = work.getAttribute("return");
var quoteResponse = attr.getValue(0);
main.logmsg("==================== RESULTS =====================");
var quoteResult = quoteResponse.getGetQuoteResult();

main.logmsg("Stock Symbol: " + quoteResult.getStockSymbol());
main.logmsg("Last Trade Amount: " + quoteResult.getLastTradeAmount());
main.logmsg("Stock Change: " + quoteResult.getStockChange());
main.logmsg("Open Amount: " + quoteResult.getOpenAmount());
main.logmsg("Day High: " + quoteResult.getDayHigh());
main.logmsg("Day Low: " + quoteResult.getDayLow());
main.logmsg("Stock Volume: " + quoteResult.getStockVolume());
main.logmsg("Market Capitalization: " + quoteResult.getMktCap());
main.logmsg("PrevCls: " + quoteResult.getPrevCls());
main.logmsg("Change Percent: " + quoteResult.getChangePercent());
main.logmsg("52-Week Range: " + quoteResult.getFiftyTwoWeekRange());
main.logmsg("Earnings per Share: " + quoteResult.getEarnPerShare());
main.logmsg("Price-to-Earnings: " + quoteResult.getPE());
main.logmsg("Company Name: " + quoteResult.getCompanyName());
main.logmsg("Is Quote Error: " + quoteResult.isQuoteError());
main.logmsg("Average Daily Volume: " + quoteResult.getAverageDailyVolume());

main.logmsg("==================== END OF RESULTS =====================");
21.	Now we are ready to run the AssemblyLine. Run the AssemblyLine.
22.	If all goes well you should see the result printed like this:
15:23:35  ==================== RESULTS =====================
15:23:35  Stock Symbol: IBM
15:23:35  Last Trade Amount: 84.31
15:23:35  Stock Change: 0
15:23:35  Open Amount: 0
15:23:35  Day High: 0
15:23:35  Day Low: 0
15:23:35  Stock Volume: 0
15:23:35  Market Capitalization: 141.2B
15:23:35  PrevCls: 84.31
15:23:35  Change Percent: 0.00%
15:23:35  52-Week Range: 81.90 - 100.43
15:23:35  Earnings per Share: 4.66
15:23:35  Price-to-Earnings: 18.09
15:23:35  Company Name: INTL BUS MACHINE
15:23:35  Is Quote Error: false
15:23:35  Average Daily Volume: 4664545
15:23:35  ==================== END OF RESULTS =====================





II.	Creating a web service (the flexible way)
-------------------------------------------------
What follows is a detailed description of how to set up an AssemblyLine which exposes some functionality as a web service. The functionality exposed in this example is a Javascript which given two integer numbers returns their sum and their product (for example given 5 and 6, the Javascript returns 11 and 30). 
This example uses the more flexible way of exposing a web service, it uses the "Web Service Receiver Server Connector" Connector. Using the "Web Service Receiver Server Connector" Connector gives you greater control over the parsing of the SOAP request and serializing the SOAP response. What the "Web Service Receiver Server Connector" Connector does, is input to the AssemblyLine the raw SOAP request message as sent by the web service client and then get the raw SOAP response message generated by the AssemblyLine and return it to the web service client. The AssemblyLine is responsible for parsing the SOAP request and then serializing the SOAP response. The AssemblyLine in this example uses the "Axis Soap-To-Java" and the "Axis Java-To-Soap" function components to parse and serialize SOAP messages respectively.

Here is what you have to do in order to expose that piece of functionality as a web service:
1.	Create a new AssemblyLine. You can name it "A_times_B_FlexibleWebService".
2.	Add a "Web Service Receiver Server Connector" component to this AssemblyLine. You can name it "WSReceiverServer".
3.	Add two "If" branch components to the AssemblyLine. You can name them "wsdlRequest" and "soapRequest".
4.	Add a script component inside the block of the "wsdlRequest" branch component. You can name it "readWSDL".
5.	Add the following Javascript to the "readWSDL" component:
work.setAttribute("responseContentType", "text/xml");
var wsdl = WSReceiverServer.connector.readFile("examples/ws_tutorial/math_ws/a_times_b.wsdl");
work.setAttribute("soapResponse", wsdl);

6.	Add an Axis Soap-To-Java function component inside the block of the "soapRequest" branch component. You can name it "A_times_B_SoapToJava". The AssemblyLine will use this function component to parse the incoming SOAP request messages.
7.	Add a script component inside the block of the "soapRequest" branch component. You can name it "A_times_B". This script component represents the piece of functionality to be exposed as a web service.
8.	Paste the following Javascript into the "A_times_B" script component (this script is the actual piece of functionality that will be exposed):

main.logmsg("work: " + work);

var attrA = work.getAttribute("a");
var a = attrA.getValue(0);
var attrB = work.getAttribute("b");
var b = attrB.getValue(0);
main.logmsg("a: " + a);
main.logmsg("a.getClass(): " + a.getClass());
main.logmsg("b: " + b);
main.logmsg("b.getClass(): " + b.getClass());

var a_plus_b = a.doubleValue() + b.doubleValue();
main.logmsg("a_plus_b: " + a_plus_b);
a_plus_b = new java.lang.Double(a_plus_b);
main.logmsg("a_plus_b: " + a_plus_b);

var a_times_b = a * b;
main.logmsg("a_times_b: " + a_times_b);
a_times_b = new java.lang.Double(a_times_b);
main.logmsg("a_times_b: " + a_times_b);

work.setAttribute("a_plus_b", a_plus_b);
work.setAttribute("a_times_b", a_times_b);

9.	Add an Axis Java-To-Soap function component inside the block of the "soapRequest" branch component. You can name it "A_times_B_JavaToSoap".
10.	Go to the "Operations" option from the AssemblyLine settings menu.
11.	Click the "Insert" button.Specify a name for the operation for example Default. Create a new Attribute with Name "a" in Input map. Create another 
new Attribute with Name "b".
12.	Click on the "Output map" and Create a new Attribute with Name "a_plus_b" . Create another new Attribute with Name "a_times_b" .
13.	Go back to the "Data Flow" tab of the AssemblyLine.
14.	Click on the "WSReceiverServer" Connector.
15.	Fill in the "WSDL Output to Filename" parameter. Set it to "/examples/ws_tutorial/math_ws/a_times_b.wsdl".
16.	Fill in the "Web Service provider URL" parameter. Set it to "http://localhost:9998/".
17.	Click on the "Generate WSDL button".
18.	If all goes well a message box that indicates that the WSDL file was generated successfully is displayed. Click OK to close it.
19.	Fill in the "TCP Port" parameter. This value must match the port specified in the "Web Service provider URL" used for creation of the WSDL thus both the client (which parses the WSDL) and the server will use the same port. For this example fill in "9998".
20.	Click on the "A_times_B_SoapToJava" function component.
21.	Set the "WSDL URL" FC parameter to the generated WSDL file "examples/ws_tutorial/math_ws/a_times_b.wsdl".
22.	Fill in the "SOAP Operation" FC parameter by clicking the "Operations..." button and selecting the only available option  "A_times_B_FlexibleWebService". The tool for generating WSDL sets the name of the SOAP operation in the WSDL file the same as the name of the AssemblyLine. 
23.	Fill in the "Input the SOAP message as" FC parameter. For this example leave the default value of "String".
24.	Leave the "Complex types" FC parameter blank this example does not use any complex types.
25.     Set the value of the "Mode" FC parameter to "Request".
26.	Click on the "A_times_B_JavaToSoap" function component.
27.	Set the "WSDL URL" FC parameter to "examples/ws_tutorial/math_ws/a_times_b.wsdl".
28.	Fill in the "SOAP Operation" FC parameter by clicking the "Operations..." button and selecting the only available option "A_times_B_FlexibleWebService". The tool for generating WSDL sets the name of the SOAP operation in the WSDL file the same as the name of the AssemblyLine.
29.	Fill in the "Return XML as" FC parameter. For this example leave the default value of "String".
30.	Leave the "Complex types" FC parameter blank, this example does not use any complex types.
31.	Set the "Mode" FC parameter to "Response". This specifies to the FC that it is a response SOAP message that it should generate.
32.	Set the "Operation Parameters" FC parameter to "a_times_b a_plus_b". These are the names of the Attributes, which the "A_times_B" script will store into the work Entry.
33.	Click the "Attrribute Map" tab of the "A_times_B_SoapToJava" FC.
34.	Go to the "Output Map".
35.	Map the "soapRequest" Attribute that is coming from the "WSReceiverServer" Connector to the "xmlString" Attribute that the FC expects.
36.	Go to the "Input Map" of the FC.
37.	Map the "a" and "b" Attributes that the FC returns (don't change the names of the Attributes).
38.	Click the "A_times_B_JavaToSoap" FC.
39.	Go to the "Output Map" tab of the FC.
40.	Map the "a_plus_b" and "a_times_b" Attributes from the work Entry (stored there by the "A_times_B" script component) to the FC.
41.	Go to the Input Map.
42.	Map the "xmlString" Attribute that the FC returns to the "soapResponse" Attribute the "WSReceiverServer" Connector expects.
43.	Check the "Detailed log" check-box for the "WSReceiverServer" Connector, the "A_times_B_SoapToJava" and the "A_times_B_JavaToSoap" function components. This will print more detailed information on the console.
44.	Go to the "WSReceiverServer" Connector "Input Map" tab. Select all available connector Attributes from the right pane and move them to the left pane called "Work Attribute".
45.	Go to the "WSReceiverServer" Connector "Output Map" tab. Select all available Attributes from the right pane and move them to the left pane called "Connector Attribute".
46.	Start the AssemblyLine.
47.	Note that you can retrieve the WSDL file by accessing the following URL with a web browser from the local machine: http://localhost:9998/?WSDL




III.	Invoking an SyncWeave web service (the flexible way)
-------------------------------------------------------
This example demonstrates how to invoke a web service the flexible way. This is achieved by using the Axis Java-To-Soap, invokeSoapWS and Axis Soap-To-Java function components. This way is flexible, because it gives you control over the generation of the SOAP request message and over the parsing of the SOAP response message. The responsibility for handling the raw SOAP messages lies with the AssemblyLine. The AssemblyLine in this example uses the "Axis Java-To-Soap" and the "Axis Soap-To-Java" function components to serialize and parse SOAP messages respectively.

1.	Create a new AssemblyLine. You can name it "A_times_B_FlexibleClient".
2.	Add an Axis Java-To-Soap function component to this AssemblyLine. You can name it "SquareJavaToSoap".
3.	Set the "WSDL URL" parameter to "examples/ws_tutorial/math_ws/a_times_b.wsdl".
4.	Set the "SOAP Operation" parameter by clicking the "Operations..." button and selecting the only available option "A_times_B_FlexibleWebService".
5.	Leave the "Return XML as" parameter to its default value "String".
6.	Leave the "Complex Types" parameter blank.
7.	Leave the "Mode" parameter to its default value "Request".
8.	Set the "Operation Parameters" parameter to "a b".
9.	Add an "invokeSoap Web Service" function component to the AssemblyLine. You can name it "InvokeWebService".
10.	Set the "WSDL URL" parameter to "examples/ws_tutorial/math_ws/a_times_b.wsdl".
11.	Set the "SOAP Operation" parameter by clicking the "Operations..." button and selecting the only available option "A_times_B_FlexibleWebService".
12.	You can leave the rest of the "InvokeWebService" FC parameters to their default values.
13.	Add an  Axis Soap-To-Java function component to the AssemblyLine. You can name it "SquareSoapToJava".
14.	Set the "WSDL URL" parameter to "examples/ws_tutorial/math_ws/a_times_b.wsdl".
15.	Set the "SOAP Operation" parameter by clicking the "Operations..." button and selecting the only available option "A_times_B_FlexibleWebService".
16. Set the value of the "Mode" FC parameter to "Response".
17.	You can leave the rest of the FC parameters to their default values.
18.	Put in the AssemblyLine Prolog the following Javascript:
var entry = system.newEntry();
entry.setAttribute("a", new java.lang.Double(5.0));
entry.setAttribute("b", new java.lang.Double(6.0));
task.setWork(entry);
19.	Go to the Output Map of "SquareJavaToSoap" FC.
20.	Add "a" and "b" Attributes to the work Entry to the FC.
21.	Go to the Input Map of "SquareJavaToSoap" FC.
22.	Map the "xmlString" Attribute from the FC to the work Entry.
23.	Go to the Output Map of "InvokeWebService" FC.
24.	Map the "xmlString" Attribute from the work Entry to the FC.
25.	Go to the Input Map of "InvokeWebService" FC.
26.	Map the "xmlString" Attribute from the FC to the work Entry.
27.	Go to the Output Map of "SquareSoapToJava" FC.
28.	Map the "xmlString" Attribute from the work Entry to the FC.
29.	Go to the Input Map of "SquareSoapToJava" FC.
30.	Add the "a_plus_b" and "a_times_b" Attributes to the FC to the work Entry.
31.	Add a script component to the AssemblyLine. You can name it "PrintResult".
32.	Put the following Javascript in the "PrintResult" script component:
main.logmsg("======== RESULT ========");
var a_plus_b_Attr = work.getAttribute("a_plus_b");
var a_plus_b = a_plus_b_Attr.getValue(0);
main.logmsg("a_plus_b: " + a_plus_b);
var a_times_b_Attr = work.getAttribute("a_times_b");
var a_times_b = a_times_b_Attr.getValue(0);
main.logmsg("a_times_b: " + a_times_b);
main.logmsg("======== END OF RESULT ========");



IV.	Creating a web service (the simple way)
-------------------------------------------
This example demonstrates how to set up an AssemblyLine which exposes some functionality as a web service the simple way. The functionality exposed in this example is a Javascript which given an integer number returns its square (for example given 5, the Javascript returns 25). 
This example uses the "Axis Easy Web Service Server Connector", which is the "easy" way of doing it. It is the "easy" way, because the AssemblyLine receives the already parsed SOAP request as a Java object, does not have to take care of parsing the SOAP request itself. Analogously the AssemblyLine returns to the Connector a Java object and it is the Connector's responsibility to serialize that Java object in order to get the raw SOAP response message.

Here is what you have to do in order to expose that piece of functionality as a web service:
1.	Create a new AssemblyLine. You can name it "Square_SimpleWebService".
2.	Add an "Axis Easy Web Service Server Connector" Connector component to this AssemblyLine. You can name it "EasyWSServer".
3.	Go to the "Operations" option from the AssemblyLine settings menu.
4.	Click the "Insert" button and specify a name for the operation (for example 'Default'). 
5.	Create a new Attribute with name "number" in the Input Schema. 
6.	Create a new Attribute with name "square" in the Output Schema.
7.	Go back to the "Data Flow" tab of the AssemblyLine.
8.	Add a "IF" Branch component to the AssemblyLine. You can name it "soapRequest".
9.	Add a condition to this Branch component, which says: the "wsdlRequested" Attribute equals "false". In this way the Script component which we will put inside this Branch component's block will be executed only if the "wsdlRequested" Attribute of the work Entry is equals to "false".
10.	When the "wsdlRequested" Attribute is "true", then the Connector will see to it that the contents of the WSDL be returned to the web service client.
11.	Add a Script component to the block of the "soapRequest" Branch component. You can name it "Square". This Script component will get executed only if the condition specified by the Branch component evaluates to true.
12.	Paste the following Javascript into the "Square" script component (this script is the actual piece of functionality that will be exported):

var attr = work.getAttribute("requestObjArray");
var array = attr.getValue(0);
var number = array[0];
var square = number*number;
square = new java.lang.Integer(square);
var obj = new java.lang.Object();
var responseObjArray = java.lang.reflect.Array.newInstance(obj.getClass(), 1);
responseObjArray[0] = square;
work.setAttribute("responseObjArray", responseObjArray);

13.	Set the "EasyWSServer" "TCP Port" parameter to "9998".
14.	Set the "WSDL Output to Filename" parameter to "examples/ws_tutorial/math_ws/square.wsdl".
15.	Set the "Web Service Provider URL" to "http://localhost:9998/".
16.	Click the "Generate WSDL" button. A message box indicating success should appear. Click "OK" to close it.
17.	Set the "WSDL File" parameter to the just generated "square.wsdl" WSDL file (using its absolute path name, which you can achieve by selecting the WSDL file using the "Select..." button). See the complete configuration for an example.
18.	Set the "SOAP Operation" parameter by clicking the "Operations..." button and select the only available option - "Square".
19.	Leave the "Complex types" parameter blank.
20.	Leave the "Use SSL" parameter to its default value "unchecked".
21.	You can set the "Detailed log" parameter to "checked" in order to see more detailed log messages.
22.	Go to the "EasyWSServer" Connector "Attribute maps" tab. Select all available connector Attributes from the top right pane and move them to the left Input map.
23.	Select all available Attributes from the right bottom pane and move them to the left Output map.
24.	Start the AssemblyLine.
25.	Note that you can retrieve the WSDL file by accessing the following URL with a web browser from the local machine: http://localhost:9998/?WSDL



V.	Invoking an SyncWeave web service (the simple way)
-------------------------------------------------
What follows is a detailed description of how to set up an AssemblyLine which invokes the web service set up in the previous example. This example uses the "Axis Easy Invoke Soap Web Service" function component. Using this component is the "easy" way of accessing a web service. It is the "easy" way, because the AssemblyLine does not have to take care of generating the SOAP request message and parsing the SOAP response message, the AssemblyLine only works with Java objects.

1.	Create a new AssemblyLine. You can name it "Square_SimpleClient".
2.	Add a new "Axis Easy Invoke Soap Web Service" function component. You can name it "EasyInvokeWebService".
3.	Set the value of the "WSDL URL" FC parameter to "examples/ws_tutorial/math_ws/square.wsdl".
4.	Set the value for the "Soap Operation" FC parameter by clicking the "Operations..." button and selecting the only available option "Square".
5.	Leave the "Complex Types" FC parameter blank.
6.	Set the "Operation parameters" FC parameter to "number".
7.	You can set the "Detailed log" parameter to "checked" in order to see more detailed log messages.
8.	Put the following Javascript into the AssemblyLine Prolog:

var entry = system.newEntry();
entry.setAttribute("number", new java.lang.Integer(5));
task.setWork(entry);

9.	Go to the Output Map of the FC.
10.	Add "number" Attribute from the work Entry to the FC.
11.	Go to the Input Map of the FC.
12.	Map the "return" Attribute from the FC to the work Entry.
13.	Create a new script component for printing the result of the web service. You can name it "PrintResult".
14.	Put the following Javascript in the "PrintResult" script component:
main.logmsg("======== RESULT ========");
var resAttr = work.getAttribute("return");
main.logmsg(resAttr.getValue(0));
main.logmsg("======== END OF RESULT ========");



Running the above examples using SSL
------------------------------------
For each of the above examples you have to do the following so that the communication between the client and the server to be over SSL:
1.	At every place in the respective AssemblyLines where there is a reference to a WSDL file (including references in Javascript code), change that reference to the SSL version of that WSDL file, i.e. change "examples/ws_tutorial/math_ws/square.wsdl" to "examples/ws_tutorial/math_ws_ssl/square_ssl.wsdl" and "examples/ws_tutorial/math_ws/a_times_b.wsdl" to "examples/ws_tutorial/math_ws_ssl/a_times_b_ssl.wsdl" respectively. The difference between the SSL and non-SSL versions of the WSDL files is the URL used to access the web service (the value of the location attribute of the soap:address WSDL element). One begins with "http://"and the other begins with "https://".
2.	For each of the two Server Connectors check the "Use SSL" check-box parameter.
3.	Set the SSL parameters in the "global.properties" file or the "solution.properties" file in the following way:

## server authentication
## example
## javax.net.ssl.trustStore=d:\test\KeyRings\namtp2.jks
## javax.net.ssl.trustStorePassword=secret
## javax.net.ssl.trustStoreType=jks

javax.net.ssl.trustStore=examples/ws_tutorial/math_ws_ssl/client.jks
javax.net.ssl.trustStorePassword=secret
javax.net.ssl.trustStoreType=jks

## client authentication
## example
## javax.net.ssl.keyStore=d:\test\KeyRings\namtp2.jks
## javax.net.ssl.keyStorePassword=secret
## javax.net.ssl.keyStoreType=jks

javax.net.ssl.keyStore=examples/ws_tutorial/math_ws_ssl/server.jks
javax.net.ssl.keyStorePassword=secret
javax.net.ssl.keyStoreType=jks


4.	Restart the server and client AssemblyLines.
5.	Note that the URL for retrieving the WSDL file with a web browser from the local machine changes to: https://localhost:9998/?WSDL



SSL client authentication by using client SSL certificates
----------------------------------------------------------
Both Server Connectors provide a "Require Client Authentication" Connector parameter in the form of a check-box. When checked this param causes the Server Connector to require the client to provide an SSL certificate which the Server Connector trusts. If the client does not provide such a certificate or if the certificate provided is not trsusted by the Server Connector, then the Server Connector rejects the client request. If the client provides a certificate which is trusted by the Server Connector then the communication link is established and the processing of the client request continues.
The set of certificates which the Server Connector trusts is established by the truststore configured in the "global.properties" / "solution.properties" SyncWeave configuration file (the value of the "javax.net.ssl.trustStore" property).



Creating SSL certificates for use with the Web Service components
-----------------------------------------------------------------
If you need to create your own SSL certificates, because the provided sample ones have expired or for some other reason, then you can use the standard Java utility keytool:

1. keytool -genkey -dname cn=localhost -validity 18263 -keystore server.jks -storepass secret -keypass secret
2. keytool -export -alias mykey -file server.cer -keystore server.jks -storepass secret
3. keytool -import -trustcacerts -file server.cer -keystore server.jks -storepass secret -alias mytrustedkey

Answer "yes" and press Enter to the "Certificate already exists in keystore under alias <mykey> Do you still want to add it? [no]:" question.

After these steps are executed a server.jks keystore file is created which contains a key for the server. This file is also a truststore which contains the server public key, i.e. trusts the server key. In this way this server.jks file can be used as both the server keystore and client truststore file.

Note: An SSL server certificate for use with the web service components must contain a key whose distinguished name (dname) must match the name of the host on which the server AssemblyLine runs. For this example we have chosen to use "cn=localhost", which means that only requests from the local machine are allowed.



Troubleshooting
---------------
1.	If the Complex Types Generator FC displays an error message box and you need further information about the error that has occurred do the following:
a.	Change the log level of the "Root" logger in <SOLUTION_FOLDER>/ce-log4j2.xml to debug. For example change the line '<Root level="info">' to '<Root level="debug">'.
b.	Restart the Config Editor.
c.	Run the ComplexTypesGen utility again.
