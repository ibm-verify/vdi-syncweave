GLA Connector Example.
======================

Overview:
This example shows how to use the GLA Connector with already configured adapter file.

This example consists of the following files:
GLAConnectorExample.xml � the SDI configuration file.
adapter.adapter � the GLA adapter configuration file.
example.log � the example log file which will be processed by the adapter file.
readme.txt � this file.

How to run the example:
1.	Start ITDI Config Editor (ibmditk.bat).
2.	Open the GLAConnectorExample.xml file.
3.	Start AL1.

Brief explanation of the example�s workflow:
At the beginning the GLA Connector verifies the given adapter configuration file and if it is not a valid config file an Exception is thrown. Then the GLA Connector is registered to the TDIOutputter�s using the correlation ID (in this case: unique_correlation_ID). Later the adapter configuration file is started. All CBE objects sent by the TDIOutputter are delivered to the GLA Connector which sets the object attributes, as well the raw CBE object, to the work Entry. Once this is done users could get the values of all attributes using the public methods of the Entry class.