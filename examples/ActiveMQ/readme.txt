This example demonstrates how to configure the JMS Connector of Security Verify Directory Integrator to connect to an ActiveMQ 5.3.0
broker (http://activemq.apache.org/activemq-530-release.html).



This text is accompanied by the following files:

- activemq_jms_driver.js :
	Javascript JMS driver for Security Verify Directory Integrator.
	
- JMSConnector_ActiveMQ_example.xml : 
	Configuration for Security Verify Directory Integrator which shows how to add messages to an ActiveMQ queue and iterate
	over them.
	
	
	
How to run the example ?
------------------------

	1. Start ActiveMQ using the default xml configuration shipped with ActiveMQ 5.3.0:
	
		On Windows enter ActiveMQ folder and run "bin/activemq.bat".
		On UNIX enter the ActiveMQ folder and run "bin/activemq".
	
		The default xml configuration is located in conf/activemq.xml. It describes a broker which listens for TCP connections 
		on port 61616. This broker requires no authentication and uses no SSL.
		
	2. Stop the Security Verify Directory Integrator Configuration Editor and stop the Security Verify Directory Integrator Server.
		
	3. Copy the activemq-all-5.3.0.jar from the ActiveMQ installation to the 'jars' folder of Security Verify Directory Integrator.
	
	4. Copy the example configuration JMSConnector_ActiveMQ_example.xml and the Javascript driver activemq_jms_driver.js to
		the solution folder of the Security Verify Directory Integrator Server.
		
	5. Execute the 'queue_add' AssemblyLine to put a sample message to the 'myqueue' queue of the ActiveMQ broker:
		ibmdisrv -c JMSConnector_ActiveMQ_example.xml -r queue_add
			
	6. Execute the 'queue_iterate" AssemblyLine to iterate over the messages in the 'myqueue' queue:
		ibmdisrv -c JMSConnector_ActiveMQ_example.xml -r queue_iterate


		
Notes/clarifications:
---------------------

- The activemq-all-5.3.0.jar contains the implementation of the JMS provider for ActiveMQ. It must be put on the classpath
	of Security Verify Directory Integrator (the 'jars' sub-folder in the installation folder) before you can connect to ActiveMQ.

- Set the jms.broker parameter of the JMS Connector to the URL of the ActiveMQ broker, e.g.: "tcp://localhost:61616".
	If you use SSL that URL would be "ssl://localhost:61616". The protocol of the URL ("tcp", "ssl", ...) depends on the
	type of transport that the ActiveMQ broker supports (see http://activemq.apache.org/configuring-version-5-transports.html).
	
- Set the jms.connectionType parameter to "Queue" or "Topic" depending on whether you want to use queue messaging or
	a publish/subscribe model. ActiveMQ supports them both.
	
- Set the jms.driver parameter to "com.ibm.di.systemqueue.driver.JMSScriptDriver".

- Set the jms.driverAttributes parameter to "js.jsfile=activemq_jms_driver.js".
	For this to work the activemq_jms_driver.js file must be located in the solution folder of the Server.

- Set the jms.topic parameter of the JMS Connector to the name of the queue/topic you want to use.
	You don't have to explicitly create queues/topics - ActiveMQ will create them on demand.
	
- If the ActiveMQ broker requires authentication, fill proper credentials in the jms.username and jms.password parameters of
	the JMS Connector.
	
- If the ActiveMQ broker uses SSL, import the SSL certificate of the broker inside the default truststore of the Security Directory
	Integrator Java Virtual Machine (see the "javax.net.ssl.trustStore" property in global.properties/solution.properties).
	
- If the ActiveMQ broker requires SSL client authentication, import the default SSL certificate of the Security Verify Directory Integrator
	JVM into the truststore of the ActiveMQ broker and restart the broker. The default keystore of the Security Verify Directory Integrator
	Java Virtual Machine is configured by the "javax.net.ssl.keyStore" property in global.properties/solution.properties.
	
- You can use the 'keytool' Java utility to export/import SSL certificates.
