JMS Script Driver Sample
--------------------------



1. Introduction
-----------------
This sample demonstrates how the SyncWeave JMS components (JMS Connector, and System Queue connector) use the JMS Script Driver to connect to the WebSphere Default JMS provider.


1.1. SyncWeave JMS Drivers
--------------------------------------------------
SyncWeave 6.1 introduced the concept of SyncWeave JMS Drivers - these are modules which allow SyncWeave to use any JMS provider.
Please note that these are different from the JMS provider drivers.
The SyncWeave JMS Driver for a particular provider deals with the specifics of obtaining a connection to the JMS provider (according to the JMS specification this initialization is done in a provider-specific way).
The SyncWeave JMS driver implementations for the following JMS providers are designated standard and are provided out of the box in SyncWeave:
	IBM WebSphere MQ
	IBM WebSphere MQ Everyplace (MQe)
	IBM WebSphere ESB
	A JMS Script Driver
SyncWeave solution developers and SyncWeave bundlers can either
	1.	Use one of the standard SyncWeave JMS drivers above, or
	2.	Write a custom SyncWeave JMS driver
		a.	either in JavaScript using the SyncWeave JMS Script Driver (this is what this JMS Script Driver sample uses), or
		b.	in Java

For more information on SyncWeave JMS Drivers please see the SyncWeave documentation.


1.2 The JMS Script Driver SyncWeave JMS Driver
----------------------------------------------------------------------
This sample uses the SyncWeave JMS Script Driver - a custom JavaScript file (which is the implementation of this SyncWeave JMS driver for WebSphere Default JMS provider) is provided as part of this sample which allows the SyncWeave JMS component to use Websphere Application Server(WAS) as a Default JMS provider.




2. Configuration
------------------
This custom SyncWeave JavaScript of JMS Script Driver uses the following parameters:
	jms.broker : [required] the URL of the message broker in the format host:port:bootstrap_messaging
	jms.serverChannel: [required] the bus_name of JMS Provider (Websphere Application Server)
	jms.username : [optional] username for authentication before the message broker; the default is an empty string
	jms.password : [optional] password for authentication before the message broker; the default is an empty string

For the JMS Connector these properties are taken from the Connector configuration in the SyncWeave configuration file.




3. Examples
-------------



3.1 Setup
----------
These examples assume that there is a Websphere Application Server (WAS) is running on the local host, with a profile (default) created, which accepts SIB communication on port 7276. 
The username and password used for authentication are empty. A queue named �SampleQueue� and a topic named 'SampleTopic' are used for messaging.
Make sure the JMS Script Driver jms_driver.js is located in the working folder of your SyncWeave installation.
For the examples to work, put the sibc.jms.jar file(can be found at http://www-01.ibm.com/support/docview.wss?uid=swg24012804) in the jars folder of SyncWeave.



3.2 Configure WAS for WebSphere Default JMS provider
-----------------------------------------------------
Here is an example guide of how to configure a WebSphere Default JMS provider:
1.  Open WAS Admin Console and Login to the profile.
2.  Go to Service integration -> Buses in left navigation.
3.  Create a bus named "JMS_Example_Bus" and don't select Bus security, save it. This is optional and if you select the Bus Security, there are username/password authentication methods. 
    (IF select any then configure it. You need to provide the username and password in SyncWeave Connector configuration. For WAS 6.1 to use this security WAS Server security should be enabled.)
4.  Select the bus and go to 'Bus Member' in list shown right side.
5.  Add new bus member, Select the server, Next select Message store as file store, Next select default settings and Finish. Save it.
6.  Return to Bus and select 'Destinations', there will be default destinations for queue and topic space.
7.  Create a new destination, Select Queue; provide Queue Identifier as 'SampleQueue', Select Node and Finish. Save it.
8.  Create another new destination, Select Topic Space; provide Identifier as 'SampleTopic' and Finish. Save it.



3.3 Examples without using SSL
--------------------------------



3.3.1 WebSphere Default JMS provider with JMS Connector
--------------------------------------------------------
This example demonstrates how to use the JMS Script driver with the JMS Connector of SyncWeave.

The Broker will be in the format IP:Port:SIB_ENDPOINT_ADDRESS

The Port and SIB_ENDPOINT_ADDRESS can be found in WAS Admin Console 'Application servers > server1 > Ports'.
Default port will be 7276 and SIB_ENDPOINT_ADDRESS will be 'BootstrapBasicMessaging'.

There are two pairs of Assembly Lines - the first pair uses queue messaging and the second pair uses publish/subscribe messaging. In each pair 
there is an Assembly Line that sends a message and an Assembly Line that iterates through the messages.


	
	
3.4 Examples using SSL
------------------------



3.4.1 Configure WAS to use SSL 
-------------------------------
Here is an example guide of how to configure a WebSphere Default JMS provider to service connections over SSL:
1.  Check the default keystore and trust store of WAS for client with ssl, in file <AppServerProfile>/properties/ssl.client.props
    (You can create a keyStore/trustStore in WAS and can configure it in the second option in this file.) 
2.  Open the IBM keyManager from <WAS_install_dir>/bin/ikeyman.bat and open this KeyStore file. (Default password is �WebAS�)
3.  Select Personal Certificates and Extract a certificate from this store named it 'was_store.der'.
4.  Open the Admin Console of WAS and Login with System User.
5.  Go to security menu in left navigation.
6.  Check SSL certificate and key management -> SSL certificate and key management -> SSL configurations, there should be NodeDefaultSSLSettings created already.
    (You can create another one or can use this.)
7.  Now with this configuration, the WebSphere Default JMS provider can service SSL connections.




3.4.2 Configure SyncWeave and WAS to connect via SSL 
----------------------------------------------------------------------------
Here is an example guide of how to configure a WebSphere Default JMS provider with SyncWeave:
1.  SyncWeave and WAS provides default SSL keyStore and trustStore.
2.  Open ikeyMan and open SDI keyStore shown in global/solution.properties. <TDI_install_dir>/serverapi/testadmin.jks (Default password is �administrator�)
3.  Select Personal Certificate and Extract a certificate named 'tdi_store.der'.
4.  Select signer certificate and add the 'was_store.der' certificate created above. Choose label 'was'.
5.  Now open WAS keyStore and open keyStore file. (Default password is 'WebAS')
6.  Select signer certificate and Add 'tdi_store.der' in this store. Choose label 'tdi'. 





3.4.3 WebSphere Default JMS provider with JMS Connector
--------------------------------------------------------
This example demonstrates how to use the JMS Script driver with the JMS Connector of SyncWeave.

The Broker will be in the format IP:Port:SIB_ENDPOINT_SECURE_ADDRESS

The Port and SIB_ENDPOINT_SECURE_ADDRESS can be found in WAS Admin Console 'Application servers > server1 > Ports'.
Default port will be 7286 and SIB_ENDPOINT_SECURE_ADDRESS will be 'BootstrapSecureMessaging'.

There are two pairs of Assembly Lines - the first pair uses queue messaging and the second pair uses publish/subscribe messaging. In each pair there is an Assembly Line that sends a message and an Assembly Line that iterates through the messages.


3.5 Websphere Default JMS provider with System Queue Connector
---------------------------------------------------------------


This example demonstrates how to use the JMS Script driver with the System Queue Connector of SyncWeave.


3.5.1 Connection without SSL
------------------------------
The sample solution.properties is shipped with this example the configuration properties are:

	systemqueue.on=true
	systemqueue.jmsdriver.name=com.ibm.di.systemqueue.driver.JMSScriptDriver
	systemqueue.jmsdriver.param.jms.broker=localhost:7276:BootstrapBasicMessaging
	systemqueue.jmsdriver.param.jms.serverChannel=JMS_Example_Bus
	systemqueue.jmsdriver.param.jms.sslUseFlag=false
	systemqueue.jmsdriver.param.js.jsfile=jms_driver.js

3.5.2 Connection with SSL
--------------------------
1. Configure WAS to use SSL step 3.4.1
2. Configure SDI to use SSL step 3.4.2
3. The sample solution.properties is shipped with this example the configuration properties are:
	systemqueue.on=true
	systemqueue.jmsdriver.name=com.ibm.di.systemqueue.driver.JMSScriptDriver
	systemqueue.jmsdriver.param.jms.broker=localhost:7286:BootstrapSecureMessaging
	systemqueue.jmsdriver.param.jms.serverChannel=JMS_Example_Bus
	systemqueue.jmsdriver.param.jms.sslCipher=SSL_RSA_WITH_RC4_128_MD5
	systemqueue.jmsdriver.param.jms.sslUseFlag=true
	systemqueue.jmsdriver.param.js.jsfile=jms_driver.js

3.5.3 Use the SDI Configuration
--------------------------------

There is a pair of Assembly Lines � that uses queue messaging. In this pair there is an Assembly Line that sends a message and an Assembly Line that iterates the messages




Note:
For Queue, first add a message to queue and then read it. For topic, first run the subscriber and then run publisher.
