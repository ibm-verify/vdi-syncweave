/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.naming.Name;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.server.ResourceHash;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;
import com.ibm.wsdl.extensions.soap.SOAPConstants;
import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;
import com.ibm.wsdl.factory.WSDLFactoryImpl;
import com.ibm.wsdl.xml.WSDLWriterImpl;

/**
 * Generates WSDL from Java using WSDL4J (Axis2 version)
 * This implementation uses WSDL4J directly to generate complete WSDL documents,
 * maintaining compatibility with the original Axis 1.x implementation while
 * being part of the Axis2 migration package.
 */
public class WSDLGenerator {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * Namespace definition
     */
    private static final String DEFINITION_NAMESPACE = "";
    
    /**
     * XML schema prefix - xsd
     */
    private static final String XML_SCHEMA_PREFIX = "xsd";
    
    /**
     * XML schema namespace - http://www.w3.org/2001/XMLSchema
     */
    private static final String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
    
    /**
     * Namespace prefix of the current object - tns
     */
    private static final String THIS_NAMESPACE_PREFIX = "tns";
    
    /**
     * SOAP namespace prefix - soap
     */
    private static final String SOAP_NAMESPACE_PREFIX = "soap";
    
    /**
     * Service name suffix - Service
     */
    private static final String SERVICE_NAME_SUFFIX = "Service";
    
    /**
     * Port name suffix - Port
     */
    private static final String PORT_NAME_SUFFIX = "Port";
    
    /**
     * Binding name suffix - binding
     */
    private static final String BINDING_NAME_SUFFIX = "Binding";
    
    /**
     * RPC style - rpc
     */
    private static final String RPC_STYLE = "rpc";
    
    /**
     * SOAP operation style - rpc
     */
    private static final String SOAP_OPERATION_STYLE = RPC_STYLE;
    
    /**
     * SOAP operation style - rpc
     */
    private static final String SOAP_BINDING_STYLE = RPC_STYLE;
    
    /**
     * Binding transport URI - http://schemas.xmlsoap.org/soap/http
     */
    private static final String BINDING_TRANSPORT_URI = "http://schemas.xmlsoap.org/soap/http";
    
    /**
     * Port type suffix - PortType
     */
    private static final String PORT_TYPE_NAME_SUFFIX = "PortType";
    
    /**
     * Encoded body
     */
    private static final String SOAP_BODY_USE = "encoded";
    
    /**
     * The encoding namespace - http://schemas.xmlsoap.org/soap/encoding/
     */
    private static final String SOAP_ENCODING_NAMESPACE = "http://schemas.xmlsoap.org/soap/encoding/";
    
    /**
     * Suffix for the input message - request
     */
    private static final String INPUT_MESSAGE_NAME_SUFFIX = "Request";
    
    /**
     * Suffix for the output message - response
     */
    private static final String OUTPUT_MESSAGE_NAME_SUFFIX = "Response";
    
    /**
     * Default parameter - String
     */
    private static final String DEFAULT_PARAM_TYPE = "java.lang.String";
    
    /**
     * Default name for the Assembly line
     */
    private static final String DEFAULT_AL_NAME = "DefaultAssemblyLineName";
    
    /**
     * Vector holding the operation names
     */
    private Vector<String> mOperationNames = null;
    
    /**
     * Namespace for the current message
     */
    private String mThisNamespace = null;
    
    /**
     * Definition name for the current message
     */
    private String mDefinitionName = null;
    
    /**
     * Location of the URI for the current message
     */
    private String mWebServiceLocation = null;
    
    /**
     * {@link Definition} for the current message
     */
    private Definition mDefinition = null;
    
    /**
     * Name of the Assembly line
     */
    private String mAlName = null;
    
    /**
     * Is this an operation Assembly line
     */
    private boolean mOperationAl = false;
    
    /**
     * Configuration of the Assembly line
     */
    private AssemblyLineConfig mALConfig = null;

    // Generally there exist several XSD types which map into a single Java
    // type.
    // Since mapping from Java types into XSD types is ambiguous, you can
    // specify
    // the XSD type to map into by putting it before the other XSD types which
    // map to
    // the same Java type in the map below. For example both the "decimal" and
    // "double"
    // XSD types map into java.lang.Double. By putting XSDDouble before
    // XSDDecimal in the
    // map below we specify that we want a java.lang.Double to be mapped to the
    // "double"
    // XSD type. This behavior is implemented by the mapToXsd(...) method below.
    /**
     * XSD to Java type mapping
     */
    private static HashMap<String, String> XSD_TYPE_MAP = new HashMap<String, String>();
    static {
        XSD_TYPE_MAP.put("string", "java.lang.String");
        XSD_TYPE_MAP.put("boolean", "java.lang.Boolean");
        XSD_TYPE_MAP.put("byte", "java.lang.Byte");
        XSD_TYPE_MAP.put("short", "java.lang.Short");
        XSD_TYPE_MAP.put("int", "java.lang.Integer");
        XSD_TYPE_MAP.put("long", "java.lang.Integer");
        XSD_TYPE_MAP.put("number", "java.lang.Long");
        XSD_TYPE_MAP.put("integer", "java.lang.Long");
        XSD_TYPE_MAP.put("double", "java.lang.Double");
        XSD_TYPE_MAP.put("decimal", "java.lang.Double");
        XSD_TYPE_MAP.put("float", "java.lang.Float");
        XSD_TYPE_MAP.put("dateTime", "java.util.Date");
        XSD_TYPE_MAP.put("date", "java.util.Date");
        XSD_TYPE_MAP.put("time", "java.util.Date");
        XSD_TYPE_MAP.put("hexBinary", "[B");
        XSD_TYPE_MAP.put("base64Binary", "[B");
    }
    
    /**
     * Component properties.
     */
    private static final String PROPERTIES_FILE = "webserviceutil";
    
    /**
     * NLS String Property set
     */
    private final static com.ibm.di.server.ResourceHash sResHash = new com.ibm.di.server.ResourceHash(PROPERTIES_FILE);

    /**
     * Class constructor
     * 
     * @param aALConfig
     *            the configuration of the Assembly line
     * @param aWebServiceLocation
     *            String
     * @throws Exception
     *             if parameter type is not supported
     */
    public WSDLGenerator(AssemblyLineConfig aALConfig,
            String aWebServiceLocation) throws Exception {
        mALConfig = aALConfig;
        Name name = mALConfig.getName();
        mAlName = DEFAULT_AL_NAME;
        if (name != null) {
            mAlName = name.get(name.size() - 1);
        }
        mThisNamespace = "ns:" + mAlName + "_thisNamespace";
        mWebServiceLocation = aWebServiceLocation;
        mDefinitionName = mAlName + "_definitionName";

        mOperationNames = new Vector<String>();

        ContainerConfig containerConfig = mALConfig.getOperations();
        TreeMap<?, ?> data = containerConfig.getData();

        Iterator<?> operationsIter = data.values().iterator();
        while (operationsIter.hasNext()) {
            Vector<?> operationsVector = (Vector<?>) operationsIter.next();
            for (int i = 0; i < operationsVector.size(); i++) {
                String operation = operationsVector.get(i).toString();
                if (operation.equals(mAlName)) {
                    mOperationAl = true;
                }
            }
        }

        operationsIter = data.values().iterator();
        while (operationsIter.hasNext()) {
            Vector<?> operationsVector = (Vector<?>) operationsIter.next();
            for (int i = 0; i < operationsVector.size(); i++) {
                String operation = operationsVector.get(i).toString();
                if (operation.equalsIgnoreCase("Default") && !mOperationAl) {
                    mOperationNames.addElement(mAlName);
                } else {
                    mOperationNames.addElement(operation);
                }
            }
        }

        createDefinition();
    }

    /**
     * Class constructor
     * 
     * @param aMMConfig
     *            {@link MetamergeConfig}
     * @param aOperationNames
     *            Vector
     * @param aWebServiceLocation
     *            String
     * @param aThisNamespace
     *            namespace
     * @param aDefinitionName
     *            definition name of the message
     * @throws Exception
     *             if parameter type is not supported
     */
    @SuppressWarnings("unchecked")
    public WSDLGenerator(MetamergeConfig aMMConfig, Vector aOperationNames,
            String aWebServiceLocation, String aThisNamespace,
            String aDefinitionName) throws Exception {
        mOperationNames = aOperationNames;
        mThisNamespace = aThisNamespace;
        mDefinitionName = aDefinitionName;
        mWebServiceLocation = aWebServiceLocation;

        createDefinition();
    }

    /**
     * Writes the WSDL to a file
     * 
     * @param aWsdlFileName
     *            String , file name
     * @throws WSDLException
     *             if the file stream cannot be closed
     * @throws FileNotFoundException
     *             If the file exists but is a directory rather than a regular
     *             file, does not exist but cannot be created, or cannot be
     *             opened for any other reason then a FileNotFoundException is
     *             thrown.
     */
    public void writeToFile(String aWsdlFileName) throws WSDLException,
            FileNotFoundException {
        if (mDefinition != null) {
            WSDLWriter writer = new WSDLWriterImpl();
            FileOutputStream fos = new FileOutputStream(aWsdlFileName);
            try {
                writer.writeWSDL(mDefinition, fos);
            } finally {
                try {
                    fos.close();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    throw new WSDLException(
                            "writeToFile: ",
                            sResHash.getString("WSUTIL.WSDLGEN.COULD.NOT.CLOSE.FILE.STREAM", e.getMessage()),
                            e);
                }
            }
        } else {
            throw new WSDLException(
                    getClass().getName(),
                    sResHash.getString("WSUTIL.WSDLGEN.THE.OBJECT.HAS.NOT.BEEN.CREATED.PROPERLY"));
        }
    }

    /**
     * Creates the Definition for the WSDL using WSDL4J
     * 
     * @throws Exception
     *             if error occurs
     */
    protected void createDefinition() throws Exception {
        WSDLFactory defFactory = WSDLFactoryImpl.newInstance();
        mDefinition = defFactory.newDefinition();

        mDefinition.setQName(new QName(DEFINITION_NAMESPACE, mDefinitionName));
        mDefinition.addNamespace(SOAP_NAMESPACE_PREFIX,
                SOAPConstants.NS_URI_SOAP);
        mDefinition.addNamespace(XML_SCHEMA_PREFIX, XML_SCHEMA_NAMESPACE);
        mDefinition.addNamespace(THIS_NAMESPACE_PREFIX, mThisNamespace);
        mDefinition.setTargetNamespace(mThisNamespace);

        addService();
    }

    /**
     * Adds {@link Port} , {@link QName} and {@link Service} to the definition
     * 
     * @throws Exception
     *             if error occurs
     */
    protected void addService() throws Exception {
        Service service = mDefinition.createService();
        service.setQName(new QName(mThisNamespace, mDefinitionName
                + SERVICE_NAME_SUFFIX));
        Port port = createPort();
        service.addPort(port);

        mDefinition.addService(service);
    }

    /**
     * Creates {@link Port}
     * 
     * @return Port
     * @throws Exception
     *             if error occurs
     */
    protected Port createPort() throws Exception {
        Port port = mDefinition.createPort();
        port.setName(THIS_NAMESPACE_PREFIX + ":" + mDefinitionName
                + PORT_NAME_SUFFIX);

        Binding binding = createBinding();
        port.setBinding(binding);

        SOAPAddress soapAddr = new SOAPAddressImpl();
        soapAddr.setLocationURI(mWebServiceLocation);
        port.addExtensibilityElement(soapAddr);

        return port;
    }

    /**
     * Creates {@link Binding} for the message {@link Definition}
     * 
     * @return Binding
     * @throws Exception
     *             if error occurs
     */
    protected Binding createBinding() throws Exception {
        Binding binding = mDefinition.createBinding();

        binding.setQName(new QName(mThisNamespace, mDefinitionName
                + BINDING_NAME_SUFFIX));
        binding.setUndefined(false);

        PortType portType = createPortType();
        binding.setPortType(portType);

        for (int i = 0; i < mOperationNames.size(); i++) {
            BindingOperation bindingOperation = createBindingOperation(mOperationNames
                    .get(i).toString());
            binding.addBindingOperation(bindingOperation);
        }

        SOAPBinding soapBinding = new SOAPBindingImpl();
        soapBinding.setStyle(SOAP_BINDING_STYLE);
        soapBinding.setTransportURI(BINDING_TRANSPORT_URI);
        binding.addExtensibilityElement(soapBinding);

        mDefinition.addBinding(binding);

        return binding;
    }

    /**
     * Creates a {@link PortType} for the {@link Definition}
     * 
     * @return PortType
     * @throws Exception
     *             if error occurs
     */
    protected PortType createPortType() throws Exception {
        PortType portType = mDefinition.createPortType();
        portType.setQName(new QName(mThisNamespace, mDefinitionName
                + PORT_TYPE_NAME_SUFFIX));
        portType.setUndefined(false);

        for (int i = 0; i < mOperationNames.size(); i++) {
            Operation operation = createOperation(mOperationNames.get(i)
                    .toString());
            portType.addOperation(operation);
        }

        mDefinition.addPortType(portType);

        return portType;
    }

    /**
     * Creates {@link Operation} for the specified operation name
     * 
     * @param aOperationName
     *            String
     * @return Operation
     * @throws Exception
     *             if parameter is not supported
     */
    protected Operation createOperation(String aOperationName) throws Exception {
        Operation operation = mDefinition.createOperation();
        operation.setName(aOperationName);
        operation.setUndefined(false);
        operation.setStyle(OperationType.REQUEST_RESPONSE);

        Input input = createInput(aOperationName);
        operation.setInput(input);

        Output output = createOutput(aOperationName);
        operation.setOutput(output);

        return operation;
    }

    /**
     * Creates {@link Input} for specified operation name
     * 
     * @param aOperationName
     *            String
     * @return Input
     * @throws Exception
     *             if parameter type is not supported
     */
    protected Input createInput(String aOperationName) throws Exception {
        Message inMessage = createMessage(aOperationName, true);
        Input input = mDefinition.createInput();
        input.setMessage(inMessage);
        return input;
    }

    /**
     * Creates {@link Output} for the specified operation name
     * 
     * @param aOperationName
     *            String
     * @return Output
     * @throws Exception
     *             if parameter is not supported
     */
    protected Output createOutput(String aOperationName) throws Exception {
        Message outMessage = createMessage(aOperationName, false);
        Output output = mDefinition.createOutput();
        output.setMessage(outMessage);
        return output;
    }

    /**
     * Creates a {@link Message} for the specified operation name and boolean
     * parameter
     * 
     * @param aOperationName
     *            name of the operation.
     * @param aIsInputMessage
     *            boolean, input/output suffix
     * @return Message
     * @throws Exception
     *             if the parameter type is not supported
     */
    protected Message createMessage(String aOperationName,
            boolean aIsInputMessage) throws Exception {
        Message message = mDefinition.createMessage();
        String messageSuffix = null;
        if (aIsInputMessage) {
            messageSuffix = INPUT_MESSAGE_NAME_SUFFIX;
        } else {
            messageSuffix = OUTPUT_MESSAGE_NAME_SUFFIX;
        }
        message.setQName(new QName(mThisNamespace, aOperationName
                + messageSuffix));
        message.setUndefined(false);

        Part part = null;
        HashMap<String, String> params = getOperationParamNamesAndTypes(aOperationName,
                aIsInputMessage);
        Iterator<Map.Entry<String, String>> paramNames = params.entrySet().iterator();
        while (paramNames.hasNext()) {
            Map.Entry<String, String> mpEntry = paramNames.next();
            String paramName = mpEntry.getKey();
            String paramType = mpEntry.getValue();
            String xsdType = mapToXsd(paramType);
            if (xsdType == null) {
                throw new Exception(sResHash.getString(
                        "WSUTIL.WSDLGEN.PARAMETER.IS.UNSUPPORTED.TYPE",
                        new Object[] { paramName, aOperationName, paramType }));
            }
            part = createPart(paramName, xsdType);
            message.addPart(part);
        }

        mDefinition.addMessage(message);

        return message;
    }

    /**
     * Creates a {@link Part} for the specified parameters
     * 
     * @param aParameterName
     *            String , name of the part
     * @param aXsdType
     *            String
     * @return Part
     */
    protected Part createPart(String aParameterName, String aXsdType) {
        Part part = mDefinition.createPart();
        part.setName(aParameterName);
        part.setTypeName(new QName(XML_SCHEMA_NAMESPACE, aXsdType));

        return part;
    }

    /**
     * Create {@link BindingOperation} for the specified operation.
     * 
     * @param aOperationName
     *            name of the operation.
     * @return BindingOperation
     */
    protected BindingOperation createBindingOperation(String aOperationName) {
        BindingOperation bindingOperation = mDefinition
                .createBindingOperation();
        bindingOperation.setName(aOperationName);

        BindingInput bindingInput = mDefinition.createBindingInput();
        BindingOutput bindingOutput = mDefinition.createBindingOutput();
        SOAPBody soapBody = createSoapBody();
        bindingInput.addExtensibilityElement(soapBody);
        bindingOutput.addExtensibilityElement(soapBody);

        bindingOperation.setBindingInput(bindingInput);
        bindingOperation.setBindingOutput(bindingOutput);

        SOAPOperation soapOperation = new SOAPOperationImpl();
        soapOperation.setSoapActionURI(mThisNamespace + "#" + aOperationName);
        soapOperation.setStyle(SOAP_OPERATION_STYLE);
        bindingOperation.addExtensibilityElement(soapOperation);

        return bindingOperation;
    }

    /**
     * Creates a {@link SOAPBody} for the message
     * 
     * @return the SOAP body
     */
    protected SOAPBody createSoapBody() {
        SOAPBody soapBody = new SOAPBodyImpl();
        soapBody.setUse(SOAP_BODY_USE);
        Vector<String> vector = new Vector<String>();
        vector.add(SOAP_ENCODING_NAMESPACE);
        soapBody.setEncodingStyles(vector);
        soapBody.setNamespaceURI(mThisNamespace);
        return soapBody;
    }

    /**
     * The method returns a {@link HashMap} containing the parameter names and
     * types of the operation
     * 
     * @param aOperationName
     * @param aDirection
     * @return HashMap
     */
    private HashMap<String, String> getOperationParamNamesAndTypes(String aOperationName,
            boolean aDirection) {
        HashMap<String, String> params = new HashMap<String, String>();
        if (mALConfig != null) {
            SchemaConfig schemaConfig = null;

            if (aOperationName.equalsIgnoreCase(mAlName) && !mOperationAl) {
                aOperationName = "Default";
            }

            schemaConfig = mALConfig.getOperation(aOperationName).getSchema(
                    aDirection);

            if (schemaConfig != null) {
                Iterator<?> iterator = schemaConfig.getItemNames().iterator();
                while (iterator.hasNext()) {
                    String paramName = (String) iterator.next();
                    String paramType = schemaConfig.getItem(paramName)
                            .getExternalSyntax();
                    params.put(paramName, paramType);
                }
            }
        }

        return params;
    }

    /**
     * Retrieves from provided java type the corresponding XSD type.
     * 
     * @param aParamType
     *            java type
     * @return XSD type
     */
    private String mapToXsd(String aParamType) {
        if (aParamType == null) {
            aParamType = DEFAULT_PARAM_TYPE;
        } else {
            aParamType = aParamType.trim();
            if (aParamType.equals("")) {
                aParamType = DEFAULT_PARAM_TYPE;
            }
        }

        java.util.Iterator<String> iter = XSD_TYPE_MAP.keySet().iterator();
        String retXSDType = null;
        while (iter.hasNext()) {
            String xsdType = iter.next();
            String javaType = XSD_TYPE_MAP.get(xsdType);
            if (javaType != null && javaType.equals(aParamType)) {
                retXSDType = xsdType;
                break;
            }
        }

        return retXSDType;
    }

    /**
     * Generates WSDL with the specified {@link BaseConfiguration} and location
     * of the web service in the specified file
     * 
     * @param aBaseConfig
     *            {@link BaseConfiguration}
     * @param aWsdlFileName
     *            String , file name
     * @param aWebServiceLocation
     *            String , location of the web service
     * @throws Exception
     *             if parameters are not valid
     */
    public static void generateWsdl(BaseConfiguration aBaseConfig,
            String aWsdlFileName, String aWebServiceLocation) throws Exception {
        BaseConfiguration parent = aBaseConfig.getParent().getParent()
                .getParent();
        AssemblyLineConfig alConfig = null;
        if (parent instanceof AssemblyLineConfig) {
            alConfig = (AssemblyLineConfig) parent;
        } else {
            throw new Exception(
                    sResHash.getString("WSUTIL.WSDLGEN.GREAT.GRANDPARENT.CONFIG.IS.NOT.AN.INSTANCE.OF.ASSEMBLYLINECONFIG",
                        parent.getClass().getName()));
        }

        WSDLGenerator wsdlGen = new WSDLGenerator(alConfig, aWebServiceLocation);
        wsdlGen.writeToFile(aWsdlFileName);
    }
}
