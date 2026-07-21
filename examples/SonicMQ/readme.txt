SonicMQ Sample
----------------





1. Introduction
-----------------
This sample demonstrates how the IBM Security Verify Directory Integrator JMS components (JMS Connector, System Queue) can use the SonicMQ server as a JMS provider.



1.1. IBM Security Verify Directory Integrator JMS Drivers
----------------------
Since IBM Security Verify Directory Integrator 6.1 is introduced the concept of IBM Security Verify Directory Integrator JMS Drivers - these are modules which allow IBM Security Verify Directory Integrator to use any JMS provider. Please note that these are different 
from the JMS provider drivers.
The IBM Security Verify Directory Integrator JMS Driver for a particular provider deals with the specifics of obtaining a connection to the JMS provider (since according to the JMS 
specification this initialization is done in a provider-specific way).
The IBM Security Verify Directory Integrator JMS driver implementations for the following JMS providers are called standard and are provided out of the box in IBM Security Verify Directory Integrator:
	IBM WebSphere MQ
	IBM WebSphere MQ Everyplace (MQe)
	A JMS Script Driver
IBM Security Verify Directory Integrator solution developers and IBM Security Verify Directory Integrator bundlers can either
	1.	use one of the standard IBM Security Verify Directory Integrator JMS drivers above, or
	2.	write a custom IBM Security Verify Directory Integrator JMS driver
		a.	either in Javascript using the IBM Security Verify Directory Integrator JMS Script Driver (this is what this SonicMQ sample uses), or
		b.	in Java

For more information on IBM Security Verify Directory Integrator JMS Drivers please see the IBM Security Verify Directory Integrator documentation.



1.2 The SonicMQ IBM Security Verify Directory Integrator JMS Driver
--------------------------------
This SonicMQ sample uses the IBM Security Verify Directory Integrator JMS Script Driver - a custom Javascript file (which is the implementation of this IBM Security Verify Directory Integrator JMS driver for SonicMQ) is 
provided as part of this sample which allows the IBM Security Verify Directory Integrator JMS component to use SonicMQ as a JMS provider.






2. Configuration
------------------
This custom IBM Security Verify Directory Integrator Javascript Sonic MQ driver uses the following parameters:
	jms.broker : [required] the URL of the message broker in the format host:port
	jms.username : [optional] username for authentication before the message broker; the default is an empty string
	jms.password : [optional] password for authentication before the message broker; the default is an empty string
	jms.connectid : [optional] the id string used to identify connection; the default is null
	jms.sslUseFlag : [optional] whether to use SSL for the communication; the default is false

For the System Queue these parameters are specified in global.properties/solution.properties - see the provided sample_global.properties file.
For the JMS Connector these properties are taken from the Connector configuration in the IBM Security Verify Directory Integrator config file.






3. Examples
-------------



3.1 Examples without using SSL
--------------------------------


Setup
-------
These examples assume that there is a Sonic MQ broker running on the local host, which accepts TCP communication on port 2506. The username and 
password used for authentication are empty. A queue named �SampleQ1� is used for messaging.
Make sure the Sonic MQ driver sonic_jms_driver.js is located in the working folder of your IBM Security Verify Directory Integrator installation.
For the examples to work, put the sonic_Client.jar file (you can find this in a Sonic MQ installation) in the jars folder of IBM Security Verify Directory Integrator.


Configure a Sonic Message Broker to use SSL via JSSE
------------------------------------------------------
Here is an example guide of how to configure a Sonic Message Broker to service connections over SSL using the JSSE provider:
1.	From the Sonic Management Console open the "Configure" tab.
2.	Find the broker in the tree control and open its "Properties".
(Generally you would better apply this procedure on a Message Broker rather than the Management Broker in case something goes wrong).
3.	Open the "SSL" tab in the "Properties" dialog.
4.	In the "Provider" combo box select "progress.message.net.ssl.jsse.jsseSSLImpl".
5.	Click the "Add" button and select all listed SSL cipher suites in the displayed dialog.
6.	Click the "JSSE" button to open the "Edit JSSE Parameters Properties" dialog.
7.	In the "Keystore" panel of the "Edit JSSE Parameters Properties" dialog do the following:
	- In the "Type" combo select "JKS".
	- In the "Location" field enter the full path of the keystore file, e.g.: "C:\Sonic_JSSE_files\sonic_store.jks".
	- In the "Password" field enter the password of the keystore, e.g.: "secret".
	- In the "Alias" field enter the alias of the key in the keystore, which will be used as the Message Broker�s private key during the 
	SSL communication. E.g.: "sonic".
8.	In the "Truststore" panel of the "Edit JSSE Parameters Properties" dialog do the following:
	- In the "Type" combo select "JKS".
	- In the "Location" field enter the full path of the truststore file, e.g.: "C:\Sonic_JSSE_files\sonic_store.jks".
	- In the "Password" field enter the password of the truststore, e.g.: "secret".
9.	Restart the broker to reload its configuration.
10.	From the Sonic Management Console open the "Configure" tab, find the broker in the tree control and from the "Acceptors" node open 
	"New" -> "TCP / SSL". 
11.	The "New TCP/SSL Acceptor" dialog should be displayed.
12.	In the "New TCP/SSL Acceptor" dialog under the "General" tab, do the following:
	- In the "Name" field enter a name for the acceptor, e.g.: "MY_SSL_ACCEPTOR".
	- In the "URL" field enter the host of the machine, on which the broker is running, e.g.: "localhost".
	- In the port field (next to the "URL" field) enter the port, on which the broker will accept SSL connections, e.g.: 12508.
	- Check the "SSL" checkbox.
13.	In the "New TCP/SSL Acceptor" dialog under the "SSL" tab, do the following:
	- In the "Client Authentication" panel check the "Enable" checkbox.
14.	Now with the acceptor configured, the message broker can service SSL connections.


Sonic MQ with JMS Connector
----------------------------
This example demonstrates how to use the Sonic MQ driver with the JMS Connector of IBM Security Verify Directory Integrator.
There are two pairs of Assembly Lines - the first pair uses queue messaging and the second pair uses publish/subscribe messaging. In each pair 
there is an Assembly Line which sends a message and an Assembly Line which iterates through the messages.


Sonic MQ with System Queue Connector
--------------------------------------
This example shows how to use the Sonic MQ driver with the System Queue Connector.
There are two Assembly Lines in this example - one which sends messages to the System Queue and the other which reads messages from the System Queue.
Before running this example, IBM Security Verify Directory Integrator System Queue must be properly configured. You should enter lines like the following in the global.properties/
solution.properties configuration file of IBM Security Verify Directory Integrator:

	systemqueue.on=true
	systemqueue.jmsdriver.name=com.ibm.di.systemqueue.driver.JMSScriptDriver

	systemqueue.jmsdriver.param.js.jsfile=sonic_jms_driver.js
	systemqueue.jmsdriver.param.jms.broker=localhost:2506
	systemqueue.jmsdriver.param.jms.username=
	systemqueue.jmsdriver.param.jms.password=

Note:
When in Iterator mode the System Queue Connector expects to find an Object Message (javax.jms.ObjectMessage) in the specified Queue - this is the 
type of messages which the System Queue Connector writes to and reads from a queue. If another type of message is found (if for example the JMS 
Connector or an external system has written a different type of message to the same queue) then an Exception with an appropriate message will be 
thrown by the Connector.



3.2 Examples using SSL
------------------------


Setup
-------
These examples assume that there is a Sonic MQ broker running on the local host, which accepts SSL communication on port 12508. The username and 
password used for authentication are empty. A queue named �SampleQ1� is used for messaging.
Make sure the Sonic MQ driver sonic_jms_driver.js is located in the working folder of your IBM Security Verify Directory Integrator installation.
The examples come with two JKS keystores - tdi_store.jks and sonic_store.jks.
tdi_store.jks is meant to be the JSSE keystore and truststore used by IBM Security Verify Directory Integrator for the SSL communication.
sonic_store.jks is meant to be the JSSE keystore and truststore used by the Sonic message broker for the SSL communication.
The stores are created in such a way so that the message broker will trust IBM Security Verify Directory Integrator and IBM Security Verify Directory Integrator will trust the broker.
Before running the examples, the following properties should be entered in the global.properties/solution.properties configuration file of IBM Security Verify Directory Integrator:

	javax.net.ssl.trustStore=tdi_store.jks
	javax.net.ssl.trustStorePassword=secret
	javax.net.ssl.trustStoreType=jks

	javax.net.ssl.keyStore=tdi_store.jks
	javax.net.ssl.keyStorePassword=secret
	javax.net.ssl.keyStoreType=jks

	SSL_PROVIDER_CLASS=progress.message.net.ssl.jsse.jsseSSLImpl
	sonic.mq.ssl.keyStoreAlias=tdi

These properties set the truststore and the keystore for JSSE and tell the Sonic libraries to use the JSSE SSL provider and to use the key under 
the �tdi� alias as IBM Security Verify Directory Integratorator side private key during the SSL communication.

If one wants to use the sample JKS store sonic_store.jks for the Sonic message broker, the broker should be configured to use the JSSE SSL provider 
and to use sonic_store.jks as a truststore and a keystore (the private key in sonic_store.jks is under the �sonic� alias).

tdi_store.jks description:
	purpose: JSSE keystore and truststore for IBM Security Verify Directory Integrator
	contains IBM Security Verify Directory Integrator private key under alias �tdi�
	contains Sonic certificate (in order to trust Sonic) under alias �sonic�
	password for the store is �secret�

sonic_store.jks description:
	purpose: JSSE keystore and truststore for Sonic message broker
	contains Sonic private key under alias �sonic�
	contains IBM Security Verify Directory Integrator certificate (in order to trust IBM Security Verify Directory Integrator) under alias �tdi�
	password for the store is �secret�

Note: the cipher suite parameter of the JMS Connector is not actually used by the Sonic driver, because the cipher suites are configured globally for 
the Sonic client libraries by setting a java property. If you want to override the default SSL cipher suites which the Sonic client libraries use, set 
the SSL_CIPHER_SUITES property in global.properties/solution.properties of IBM Security Verify Directory Integrator (it is a property used internally by the Sonic client libraries). 
For example:

	SSL_CIPHER_SUITES=SSL_RSA_WITH_RC4_128_MD5, SSL_RSA_WITH_DES_CBC_SHA

For these examples to work, put the sonic_Client.jar, sonic_SSL.jar, sonic_Crypto.jar and broker.jar files (you can find these in a Sonic MQ installation) 
in the jars folder of IBM Security Verify Directory Integrator.


Sonic MQ with JMS Connector using SSL
---------------------------------------
This example demonstrates how to use the Sonic MQ driver with the JMS Connector of IBM Security Verify Directory Integrator using SSL.
There are two pairs of Assembly Lines - the first pair uses queue messaging and the second pair uses publish/subscribe messaging. In each pair there is 
an Assembly Line which sends a message and an Assembly Line which iterates through the messages.


Sonic MQ with System Queue Connector using SSL
------------------------------------------------
This example shows how to use the Sonic MQ driver with the System Queue Connector using SSL.
The example uses the same IBM Security Verify Directory Integrator configuration file as the example without SSL. The only difference is the way the System Queue is configured. To use SSL 
put the following properties in the global.properties/solution.properties file of IBM Security Verify Directory Integrator:

	systemqueue.on=true
	systemqueue.jmsdriver.name=com.ibm.di.systemqueue.driver.JMSScriptDriver

	systemqueue.jmsdriver.param.js.jsfile=sonic_jms_driver.js
	systemqueue.jmsdriver.param.jms.broker=localhost:12508
	systemqueue.jmsdriver.param.jms.username=
	systemqueue.jmsdriver.param.jms.password=
	systemqueue.jmsdriver.param.jms.sslUseFlag=true

Note:
When in Iterator mode the System Queue Connector expects to find an Object Message (javax.jms.ObjectMessage) in the specified Queue - this is the type 
of messages which the System Queue Connector writes to and reads from a queue. If another type of message is found (if for example the JMS Connector or 
an external system has written a different type of message to the same queue) then an Exception with an appropriate message will be thrown by the Connector.

