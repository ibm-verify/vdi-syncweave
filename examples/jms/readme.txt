
This demo package demonstrates the use of the JMS Connector.

These files are included with this example:
jms.xml, input.txt, expected_output.txt, readme.txt.

To run this demo you must:
o Install IBM Security Verify Directory Integrator.
o Have access (account) to an IBM MQ Server.
o Copy IBM MQ driver files to <IDI-HOME>\jars\3rdparty\IBM directory and as stated in the JMS Connector documentation if applicable.
	com.ibm.mqjms.jar (this will replace an existing file)
	com.ibm.mq.jar
	connector.jar
	dhbcore.jar
	jms.jar
	jta.jar

This package contains a single IBM Security Verify Directory Integrator configuration file - jms.xml. It consists of two AssemblyLines:
1. "JMS Sender" AssemblyLine reads a text file in Simple format (input.txt) and sends all Entries read to a Queue on the IBM MQ Server. 
The configuration of the JMSOutput Connector can be seen by selecting IBM Security Verify Directory Integrator CE -> "AssemblyLine" section -> "JMS Sender" AssemblyLine -> "JMSOutput" Connector, double clicking on it and selecting the Connection tab.
2. "JMS Receiver" AssemblyLine connects to the same Queue, receives all messages (Entries) posted and writes these Entries in another text file in Simple format (output.txt).
The configuration of the JMSOutput Connector can be seen by selecting IBM Security Verify Directory Integrator CE -> "AssemblyLine" section -> "JMS Receiver" AssemblyLine -> "JMSInput" Connector, double clicking on it and selecting the Connection tab.

Some important points concerning the configuration of the two JMS Connectors used ("JMSOutput" and "JMSInput"):
  o The "Broker" parameter is set to "localhost:1414"; however if the IBM MQ broker is not located on the localhost you must change "localhost" (from this URL) to the IP address of the broker you use. Also if the queue manager listener is using port different from 1414 it must be changed in the url too.
  o "Username" and "Password" parameters are left blank. If authorization is required from the IBM MQ broker you must input the user name and the password of your account.
  o "Topic/Queue" parameter is set to "default". If you use a different name for your queue in IBM MQ this values MUST be changed to that name. Make sure no one else is using that Queue or you can receive somebody else's messages.
  o In the "Advanced" section the "Server Channel" parameter must be set to the name of the channel used by the queue manager (e.g. MyServerChannel)

No Attributes are mapped in any of the Connectors in the two AssemblyLines. Instead the option "Automatically map all attributes" is turned on (IBM Security Verify Directory Integrator ->  "AssemblyLine" section -> "JMS Sender"/"JMS Receiver" AssemblyLine -> AssemblyLine settings -> "AssemblyLine settings" menu item). Entries with arbitrary Attributes structures are handled.

Also note the "JMS Receiver" AssemblyLine is set to only iterate through 2 entries (otherwise it will continue to monitor the queue until stopped).  You can change this behavior in IBM Security Verify Directory Integrator -> "AssemblyLine" section -> "JMS Receiver" AssemblyLine -> AssemblyLine settings -> "AssemblyLine settings" menu item.

To run the demo:
1. Start the IBM Security Verify Directory Integrator CE.
2. Open the jms.xml file.
3. Go to the "AssemblyLines" section.
4. Select the "JMS Sender" AssemblyLine.
5. Double click on the JMSOutput component. Go to the "Connection" tab and in the "Advanced" section specify the value of the "Server Channel" parameter (use the name of the channel you use on your MQ Server). 
6. Click "Run".
7. Select the "JMS Receiver" AssemblyLine.
8. Double click on the JMSInput component. Go to the "Connection" tab and in the "Advanced" section specify the value of the "Server Channel" parameter (use the name of the channel you use on your MQ Server).
9. Click "Run".
10. Wait for completion of both AssemblyLines.
11. Check the content of the generated output output.txt against the expected_output.txt included in this package.


Enabling SSL

The SSL (Secure Socket Layer) protocol enables secure communications with MQ queue managers. In order to enable it, adjustments must be made to the MQ server as well as the JMS connectors in your IBM Security Verify Directory Integrator configuration. 

1. Configuring SSL security for IBM WebSphere MQ v6.0:
These settings are meant for version 6.0 of the product. For version 5.3 refer to Dr Kareem Yusef's article "Configuring SSL Connections" (http://www7b.software.ibm.com/wsdd/techjournal/0211_yusuf/yusuf.html).	
  1.1 Managing certificates:
    To manage the SSL certificates on your local computer using a GUI, use IBM Key Management (iKeyman).
    1.1.1 Create a key database file: 
      Start "IBM Key Management" tool and choose "Key database file" -> "New" menu item. The "Key database type" must be CMS. You can choose the name and location of the file but keep in mind that they must be set later in the queue manager's Key repository attribute. Check the "Stash the password to a file?" option and specify a password (it is used to access the file).
    1.1.2 Obtain a certificate:
      You can request a certificate from a Certification Authority (CA) but for the purposes of this example will be used a self-signed certificate. Select "Create" -> "New self-signed certificate" and fill the form. The "Key label" attribute value must be in the form <ibmwebspheremq<aQueueManagerNameinLowerCase> (e.g. ibmwebspheremqmyqueuemanager).
    1.1.3 Extract the created certificate for further use:
      Use the "Extract certificate" button, specify a name, location and data type and click OK. 
  1.2 Configuring SSL on queue managers:
    For these configurations use WebSphere MQ Explorer.
    1.2.1 Set the queue manager key repository:
      Select <your queue manager> -> "Properties" -> "SSL" and modify the value of the "Key repository" attribute. The value must be the location and name of the key database file from 1.1.1 but without the ".kdb" extention.  
  1.3 Configuring SSL channels:
    1.3.1 Select <your queue manager> -> "Advanced" -> "Channels" -> <your channel name>. Right click and select "Properties" -> "SSL" and set a SSL CipherSpec (for this example set it to 'NULL_MD5'). This specifies the encryption method and hash function used when sending the message. 
    1.3.2 Filtering certificates on their owner's name: 
      Certificates contain the distinguished name of the owner of the certificate. You can optionally configure the channel to accept only certificates with attributes in the distinguished name of the owner that match given values. To do this, select the "Accept only certificates with Distinguished Names matching these values" check box.  
    1.3.3 Authenticating parties initiating connections to a queue manager:
      When another party initiates an SSL-enabled connection to a queue manager, the queue manager must send its personal certificate to the initiating party as proof of identity. You can also optionally configure the queue manager's channel so that the queue manager refuses the connection if the initiating party does not send its own personal certificate. To do this, on the SSL page of the Channel properties dialog, select Required from the Authentication of parties initiating connections list. For this example we won't need this additional check so select the "Optional" value.

2. Configuring SSL security for the JMS connector:
  2.1 Additional settings for JMS Connector configuration:
    2.1.1 Check "Use SSL Connection" checkbox.
    2.1.2 Specify the "SSL Server Channel" which you configured in 1.3.
    2.1.3 Specify the Queue Manager used.
    2.1.4 Select the "SSL_RSA_WITH_NULL_MD5" option from the "SSL CipherSuite" pull down list. 
  2.2 Adding the digital certificate to the IBM Security Verify Directory Integrator truststore:
    For this operation use the IBM Key Management again.
    2.2.1 Adding the certificate:
      When a ssl connection is made the queue manager will send its certificate as part of the initial handshake and IBM Security Verify Directory Integrator truststore will be checked in order to validate the received cetificate. If it is not validated the connection will be terminated.
      You can either edit the existing truststore "testServer.jks" or create new java key store with the IBM Key Management tool. After that select the "Signer sertificates" option from the combo box and click "Add". Browse to the location you saved the extracted certificate from 1.1.3 and select it. When you are prompted for a label use the same as in point 1.1.2 (ibmwebspheremqmyqueuemanager). 
      If you chose Required for the "Authentication of parties initiating connections" option you will need to create your own personal self-signed sertificate in the IBM Security Verify Directory Integrator keystore and add it to the queue manager's key database file as a signer certificate. The steps are identical with these specified above. This new certificate will be sent by our connector to the queue manager as part of the ssl handshake and if not present will result in termination of the connection.
      As stated above this is not needed if you chose Optional authentication of parties initiating connections. 
    2.2.2 Modifying the solution.properties file:
      If you created new key stores or changed the location of the existing this must be covered in solution.properties. 
      For example: 
	javax.net.ssl.trustStore=C:\\Program Files\\IBM\\SDI\\V10.0.0.6\\jmsTrustStore
	javax.net.ssl.trustStorePassword=
	javax.net.ssl.trustStoreType=jks

	javax.net.ssl.keyStore=C:\\Program Files\\IBM\\WebSphere MQ\\Java\\bin\\jmsKeyStore
	javax.net.ssl.keyStorePassword=changeit
	javax.net.ssl.keyStoreType=jks
      This modifications should be made prior to starting IBM Security Verify Directory Integrator.


Additional information:
If you uncheck the "Use SSL Connection" checkbox the following fields will be retained in the saved configuration, 
but not used in subsequent non-ssl connections.
1. SSL Server Channel
2. QueueManger
3. SSL CipherSuite
When the "Use SSL Connection" checkbox is NOT checked, the value specified for "Server Channel" will be used.
