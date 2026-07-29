
Service Component Architecture (SCA) is a set of specifications which describe a model for building applications and systems using Service-Oriented Architecture. This example demonstrates how SDI can be integrated with SCA. 

Prerequisites -
-------------  

1. Websphere Integration Developer 6.0
2. Websphere Process Server 6.0
3. SyncWeave 10.0.0.6

The example ships a WID(Websphere Integration Developer) ProjectInterchange file which consists of the following modules - 

1. SampleCallModule � Sample SCA module that invokes the SDI Module
2. TDI_Module �  Core module which exposes �StartAL� service.
3. TDI_Common Library module � Library Component that contains Business Objects and interfaces shared among the modules. 
4. SampleJSP war � Sample UI application which is packaged along with the SampleCallModule when deployed on the Process Server.

Note : The SDI server API should be running on the default port- 1099.

To setup example -
-----------------

1. Import the TDI_SCA_ProjectInterchange.zip file into the WID workspace.
2. Add the following jars files as J2EE utility files on the TDI_Module ear.
	derby.jar
	derbyclient.jar
	derbynet.jar
	derbytools.jar
	diserverapi.jar
	diserverapirmi.jar
	icu4j-51_1.jar
	log4j-1.2-api-2.20.0.jar
	log4j-api-2.20.0.jar
	log4j-core-2.20.0.jar
	miconfig.jar
	miserver.jar
	mmconfig.jar
	tdiresource.jar
   These can be picked up from the following location <Installation Directory>/jars/3rdparty/IBM, <Installation Directory>/jars/3rdparty/Others and <Installation Directory>/jars/common.

Note : When importing these jars as utility jars to the TDI_Module project, specify option to copy all jars to EAR file . 

3. Do a complete build on all projects and resolve any errors.
4. Deploy SampleCallModule and TDI_Module on Websphere Process Server.(Server can be configured from within WID.)
5. Start SyncWeave Server API on port 1099.
6. Run sample.jsp on -  <Server Address>:9080/SampleJSP/sampleJSP.jsp

For more info refer to the User's guide ->SyncWeave Examples ->SCA
