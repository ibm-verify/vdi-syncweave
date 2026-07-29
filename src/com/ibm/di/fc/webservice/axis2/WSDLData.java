/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.xml.WSDLLocator;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.axis2.description.AxisService;

import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.wsdl.xml.WSDLReaderImpl;

/**
 * Class that holds the WSDL data. This is the Axis2 version of the original
 * WsdlData class.
 */
public class WSDLData {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * Indicates that this is a document style.
     */
    private static final String DOCUMENT_STYLE = "document";
    
    /**
     * Target namespace.
     */
    private String targetNamespace;
    
    /**
     * Qualified service name.
     */
    private QName serviceQName;
    
    /**
     * Port name.
     */
    private String portName;
    
    /**
     * Location URL.
     */
    private String locationUrl;
    
    /**
     * SOAP action URI.
     */
    private String soapActionURI;
    
    /**
     * Style (document or rpc).
     */
    private String style;
    
    /**
     * Encoding (literal or encoded).
     */
    private String encoding;
    
    /**
     * Operation type (one-way or request-response).
     */
    private OperationType operationType;
    
    /**
     * Input parameter names in order.
     */
    private List<String> inputParameterNames;

    /**
     * Output parameter names in order.
     */
    private List<String> outputParameterNames;
    
    /**
     * Logger.
     */
    private static Log log;
    
    /**
     * Resource hash.
     */
    private static ResourceHash resHash;
    
    static {
        try {
            log = new Log("WSDLData");
            // In Axis2 1.7.6, ResourceHash might have a different initialization method
            resHash = new ResourceHash("webserviceutil");
        } catch (Exception e) {
            // Cannot log here since log is not initialized
            e.printStackTrace();
        }
    }
    
    /**
     * Default constructor.
     */
    public WSDLData() {
        // Default constructor
    }
    
    /**
     * Sets the target namespace.
     * 
     * @param targetNamespace The target namespace
     */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }
    
    /**
     * Gets the target namespace.
     * 
     * @return The target namespace
     */
    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    /**
     * Sets the service QName.
     * 
     * @param serviceQName The service QName
     */
    public void setServiceQName(QName serviceQName) {
        this.serviceQName = serviceQName;
    }
    
    /**
     * Gets the service QName.
     * 
     * @return The service QName
     */
    public QName getServiceQName() {
        return serviceQName;
    }
    
    /**
     * Sets the port name.
     * 
     * @param portName The port name
     */
    public void setPortName(String portName) {
        this.portName = portName;
    }
    
    /**
     * Gets the port name.
     * 
     * @return The port name
     */
    public String getPortName() {
        return portName;
    }
    
    /**
     * Sets the location URL.
     * 
     * @param locationUrl The location URL
     */
    public void setLocationUrl(String locationUrl) {
        this.locationUrl = locationUrl;
    }
    
    /**
     * Gets the location URL.
     * 
     * @return The location URL
     */
    public String getLocationUrl() {
        return locationUrl;
    }
    
    /**
     * Sets the SOAP action URI.
     * 
     * @param soapActionURI The SOAP action URI
     */
    public void setSoapActionURI(String soapActionURI) {
        this.soapActionURI = soapActionURI;
    }
    
    /**
     * Gets the SOAP action URI.
     * 
     * @return The SOAP action URI
     */
    public String getSoapActionURI() {
        return soapActionURI;
    }
    
    /**
     * Sets the style (document or rpc).
     * 
     * @param style The style
     */
    public void setStyle(String style) {
        this.style = style;
    }
    
    /**
     * Gets the style.
     * 
     * @return The style
     */
    public String getStyle() {
        return style;
    }
    
    /**
     * Sets the encoding (literal or encoded).
     * 
     * @param encoding The encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    /**
     * Gets the encoding.
     * 
     * @return The encoding
     */
    public String getEncoding() {
        return encoding;
    }
    
    /**
     * Checks if the operation is one-way.
     *
     * @return true if the operation is one-way, false otherwise
     */
    public boolean isOperationOneWay() {
        return (operationType != null && operationType.equals(OperationType.ONE_WAY));
    }
    
    /**
     * Gets the input parameter names.
     *
     * @return The list of input parameter names in order
     */
    public List<String> getInputParameterNames() {
        return inputParameterNames;
    }
    
    /**
     * Sets the input parameter names.
     *
     * @param parameterNames The list of input parameter names
     */
    public void setInputParameterNames(List<String> parameterNames) {
        this.inputParameterNames = parameterNames;
    }

    /**
     * Gets the output parameter names.
     *
     * @return The list of output parameter names in order
     */
    public List<String> getOutputParameterNames() {
        return outputParameterNames;
    }

    /**
     * Sets the output parameter names.
     *
     * @param parameterNames The list of output parameter names
     */
    public void setOutputParameterNames(List<String> parameterNames) {
        this.outputParameterNames = parameterNames;
    }
    
    /**
     * Gets WSDL data for the specified WSDL URL and operation name.
     * 
     * @param wsdlUrl The WSDL URL
     * @param operationName The operation name
     * @return The WSDL data
     * @throws Exception if an error occurs
     */
    public static WSDLData getWsdlData(String wsdlUrl, String operationName) throws Exception {
        if (wsdlUrl == null || wsdlUrl.isEmpty() || operationName == null || operationName.isEmpty()) {
            throw new Exception(resHash.getString("WSUTIL.WSDLDATA.PARAMETERS.MUST.NOT.BE.EMPTY"));
        }
        
        String locationUrl = null;
        WSDLData wsdlData = new WSDLData();
        
        WSDLReader reader = new WSDLReaderImpl();
        reader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
        Definition def = reader.readWSDL(wsdlUrl);
        String targetNamespace = def.getTargetNamespace();
        wsdlData.setTargetNamespace(targetNamespace);
        Map<?, ?> mapServices = def.getServices();
        Iterator<?> iterServices = mapServices.entrySet().iterator();
        
        boolean found = false;
        Service service = null;
        Port port = null;
        while (iterServices.hasNext() && !found) {
            Map.Entry<?, ?> entryService = (Map.Entry<?, ?>) iterServices.next();
            Service currentService = def.getService((QName) entryService.getKey());
            Iterator<?> iterPorts = currentService.getPorts().entrySet().iterator();
            while (iterPorts.hasNext() && !found) {
                Map.Entry<?, ?> entryPort = (Map.Entry<?, ?>) iterPorts.next();
                Port currentPort = (Port) entryPort.getValue();
                Binding currentBinding = currentPort.getBinding();
                List<?> bindingExtElems = currentBinding.getExtensibilityElements();
                for (int i = 0; i < bindingExtElems.size(); i++) {
                    ExtensibilityElement bindingExtElem = (ExtensibilityElement) bindingExtElems.get(i);
                    if (bindingExtElem instanceof SOAPBinding) {
                        List<?> listOperations = currentBinding.getBindingOperations();
                        for (int listOperationsIdx = 0; listOperationsIdx < listOperations.size(); listOperationsIdx++) {
                            BindingOperation operation = (BindingOperation) listOperations.get(listOperationsIdx);
                            if (operationName.equals(operation.getName())) {
                                wsdlData.operationType = operation.getOperation().getStyle();
                                
                                // Extract parameter names from the operation
                                wsdlData.inputParameterNames = extractParameterNames(def, operation.getOperation());
                                // Extract output parameter names from the operation
                                wsdlData.outputParameterNames = extractOutputParameterNames(def, operation.getOperation());
                                
                                List<?> listExtElems = currentPort.getExtensibilityElements();
                                for (int listExtElemsIdx = 0; listExtElemsIdx < listExtElems.size(); listExtElemsIdx++) {
                                    Object portExtElem = listExtElems.get(listExtElemsIdx);
                                    SOAPAddress address = null;
                                    if (portExtElem instanceof SOAPAddress) {
                                        // several SOAP operations with the same name?
                                        address = (SOAPAddress) portExtElem;
                                        locationUrl = address.getLocationURI();
                                        wsdlData.setLocationUrl(locationUrl);
                                        
                                        found = true;
                                        port = currentPort;
                                        
                                        List<?> listOpExtElems = operation.getExtensibilityElements();
                                        for (int listOpExtElemsIdx = 0; listOpExtElemsIdx < listOpExtElems.size(); listOpExtElemsIdx++) {
                                            Object extElemSoapOperation = listOpExtElems.get(listOpExtElemsIdx);
                                            if (extElemSoapOperation instanceof SOAPOperation) {
                                                SOAPOperation soapOperation = (SOAPOperation) extElemSoapOperation;
                                                wsdlData.setSoapActionURI(soapOperation.getSoapActionURI());
                                                
                                                String operationStyle = soapOperation.getStyle();
                                                if (operationStyle != null && !operationStyle.isEmpty()) {
                                                    wsdlData.setStyle(operationStyle);
                                                } else {
                                                    SOAPBinding soapBinding = (SOAPBinding) bindingExtElem;
                                                    String bindingStyle = soapBinding.getStyle();
                                                    if (bindingStyle != null && !bindingStyle.isEmpty()) {
                                                        wsdlData.setStyle(bindingStyle);
                                                    } else {
                                                        wsdlData.setStyle(DOCUMENT_STYLE);
                                                    }
                                                }
                                                
                                                List<?> list = operation.getBindingInput().getExtensibilityElements();
                                                for (int l = 0; l < list.size(); l++) {
                                                    Object obj = list.get(l);
                                                    if (obj instanceof SOAPBody) {
                                                        SOAPBody soapBody = (SOAPBody) obj;
                                                        wsdlData.setEncoding(soapBody.getUse());
                                                    }
                                                }
                                            }
                                        }
                                        
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            service = currentService;
        }
        
        if (!found) {
            throw new Exception(resHash.getString("WSUTIL.WSDLDATA.SOAP.OPERATION.NOT.FOUND", 
                    new Object[] { operationName, wsdlUrl }));
        }
        
        wsdlData.setPortName(port.getName());
        wsdlData.setServiceQName(service.getQName());
        
        return wsdlData;
    }
    
    /**
     * Gets WSDL data for the specified WSDL URL and operation name using a custom WSDL locator.
     * 
     * @param wsdlUrl The WSDL URL
     * @param operationName The operation name
     * @param locator The WSDL locator
     * @return The WSDL data
     * @throws Exception if an error occurs
     */
    public static WSDLData getWsdlData(String wsdlUrl, String operationName, WSDLLocator locator) throws Exception {
        if (locator == null || operationName == null || operationName.isEmpty()) {
            throw new Exception(resHash.getString("WSUTIL.WSDLDATA.PARAMETERS.MUST.NOT.BE.EMPTY"));
        }
        
        String locationUrl = null;
        WSDLData wsdlData = new WSDLData();
        
        WSDLReader reader = new WSDLReaderImpl();
        reader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
        
        Definition def = reader.readWSDL(locator);
        
        String targetNamespace = def.getTargetNamespace();
        wsdlData.setTargetNamespace(targetNamespace);
        Map<?, ?> mapServices = def.getServices();
        Iterator<?> iterServices = mapServices.entrySet().iterator();
        
        boolean found = false;
        Service service = null;
        Port port = null;
        while (iterServices.hasNext() && !found) {
            Map.Entry<?, ?> entryService = (Map.Entry<?, ?>) iterServices.next();
            Service currentService = def.getService((QName) entryService.getKey());
            Iterator<?> iterPorts = currentService.getPorts().entrySet().iterator();
            while (iterPorts.hasNext() && !found) {
                Map.Entry<?, ?> entryPort = (Map.Entry<?, ?>) iterPorts.next();
                Port currentPort = (Port) entryPort.getValue();
                Binding currentBinding = currentPort.getBinding();
                List<?> bindingExtElems = currentBinding.getExtensibilityElements();
                for (int i = 0; i < bindingExtElems.size(); i++) {
                    ExtensibilityElement bindingExtElem = (ExtensibilityElement) bindingExtElems.get(i);
                    if (bindingExtElem instanceof SOAPBinding) {
                        List<?> listOperations = currentBinding.getBindingOperations();
                        for (int listOperationsIdx = 0; listOperationsIdx < listOperations.size(); listOperationsIdx++) {
                            BindingOperation operation = (BindingOperation) listOperations.get(listOperationsIdx);
                            if (operationName.equals(operation.getName())) {
                                wsdlData.operationType = operation.getOperation().getStyle();
                                
                                // Extract parameter names from the operation
                                wsdlData.inputParameterNames = extractParameterNames(def, operation.getOperation());
                                // Extract output parameter names from the operation
                                wsdlData.outputParameterNames = extractOutputParameterNames(def, operation.getOperation());
                                
                                List<?> listExtElems = currentPort.getExtensibilityElements();
                                for (int listExtElemsIdx = 0; listExtElemsIdx < listExtElems.size(); listExtElemsIdx++) {
                                    Object portExtElem = listExtElems.get(listExtElemsIdx);
                                    SOAPAddress address = null;
                                    if (portExtElem instanceof SOAPAddress) {
                                        // several SOAP operations with the same name?
                                        address = (SOAPAddress) portExtElem;
                                        locationUrl = address.getLocationURI();
                                        wsdlData.setLocationUrl(locationUrl);
                                        
                                        found = true;
                                        port = currentPort;
                                        
                                        List<?> listOpExtElems = operation.getExtensibilityElements();
                                        for (int listOpExtElemsIdx = 0; listOpExtElemsIdx < listOpExtElems.size(); listOpExtElemsIdx++) {
                                            Object extElemSoapOperation = listOpExtElems.get(listOpExtElemsIdx);
                                            if (extElemSoapOperation instanceof SOAPOperation) {
                                                SOAPOperation soapOperation = (SOAPOperation) extElemSoapOperation;
                                                wsdlData.setSoapActionURI(soapOperation.getSoapActionURI());
                                                
                                                String operationStyle = soapOperation.getStyle();
                                                if (operationStyle != null && !operationStyle.isEmpty()) {
                                                    wsdlData.setStyle(operationStyle);
                                                } else {
                                                    SOAPBinding soapBinding = (SOAPBinding) bindingExtElem;
                                                    String bindingStyle = soapBinding.getStyle();
                                                    if (bindingStyle != null && !bindingStyle.isEmpty()) {
                                                        wsdlData.setStyle(bindingStyle);
                                                    } else {
                                                        wsdlData.setStyle(DOCUMENT_STYLE);
                                                    }
                                                }
                                                
                                                List<?> list = operation.getBindingInput().getExtensibilityElements();
                                                for (int l = 0; l < list.size(); l++) {
                                                    Object obj = list.get(l);
                                                    if (obj instanceof SOAPBody) {
                                                        SOAPBody soapBody = (SOAPBody) obj;
                                                        wsdlData.setEncoding(soapBody.getUse());
                                                    }
                                                }
                                            }
                                        }
                                        
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            service = currentService;
        }
        
        if (!found) {
            throw new Exception(resHash.getString("WSUTIL.WSDLDATA.SOAP.OPERATION.NOT.FOUND", 
                    new Object[] { operationName, wsdlUrl }));
        }
        
        wsdlData.setPortName(port.getName());
        wsdlData.setServiceQName(service.getQName());
        
        return wsdlData;
    }
    
    /**
     * Gets SOAP operations from the specified WSDL URL.
     * 
     * @param wsdlUrl The WSDL URL
     * @return A vector of operation names
     * @throws Exception if an error occurs
     */
    public static Vector<String> getSoapOperations(String wsdlUrl) throws Exception {
        if (wsdlUrl == null || wsdlUrl.isEmpty()) {
            throw new Exception(resHash.getString("WSUTIL.WSDLDATA.WSDL.FILENAME.MUST.NOT.BE.EMPTY"));
        }
        
        Vector<String> operationNames = new Vector<String>();
        WSDLReader reader = new WSDLReaderImpl();
        reader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
        Definition def = reader.readWSDL(wsdlUrl);
        Map<?, ?> mapServices = def.getServices();
        Iterator<?> iterServices = mapServices.entrySet().iterator();
        while (iterServices.hasNext()) {
            Map.Entry<?, ?> entryService = (Map.Entry<?, ?>) iterServices.next();
            Service currentService = def.getService((QName) entryService.getKey());
            Iterator<?> iterPorts = currentService.getPorts().entrySet().iterator();
            while (iterPorts.hasNext()) {
                Map.Entry<?, ?> entryPort = (Map.Entry<?, ?>) iterPorts.next();
                Port currentPort = (Port) entryPort.getValue();
                Binding currentBinding = currentPort.getBinding();
                List<?> bindingExtElems = currentBinding.getExtensibilityElements();
                for (int i = 0; i < bindingExtElems.size(); i++) {
                    ExtensibilityElement extElem = (ExtensibilityElement) bindingExtElems.get(i);
                    if (extElem instanceof SOAPBinding) {
                        List<?> listOperations = currentBinding.getBindingOperations();
                        for (int operIdx = 0; operIdx < listOperations.size(); operIdx++) {
                            BindingOperation operation = (BindingOperation) listOperations.get(operIdx);
                            operationNames.add(operation.getName());
                        }
                    }
                }
            }
        }
        
        return operationNames;
    }
    
    /**
     * Gets WSDL data from an Axis2 service.
     * 
     * @param service The Axis2 service
     * @param operationName The operation name
     * @return The WSDL data
     * @throws Exception if an error occurs
     */
    public static WSDLData getWsdlDataFromAxisService(AxisService service, String operationName) throws Exception {
        if (service == null || operationName == null || operationName.isEmpty()) {
            throw new Exception(resHash.getString("WSUTIL.WSDLDATA.PARAMETERS.MUST.NOT.BE.EMPTY"));
        }
        
        WSDLData wsdlData = new WSDLData();
        
        // In Axis2 1.7.6, getWSDL() requires parameters
        // We'll use a ByteArrayOutputStream to capture the WSDL
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        service.printWSDL(baos);
        
        // Parse the WSDL from the output stream
        WSDLReader reader = new WSDLReaderImpl();
        reader.setFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE, false);
        Definition def = reader.readWSDL(null, new org.xml.sax.InputSource(new java.io.ByteArrayInputStream(baos.toByteArray())));
        if (def == null) {
            throw new Exception(resHash.getString("WSUTIL.WSDLDATA.WSDL.NOT.AVAILABLE"));
        }
        
        // Set the target namespace
        String targetNamespace = def.getTargetNamespace();
        wsdlData.setTargetNamespace(targetNamespace);
        
        // In Axis2 1.7.6, getServiceQName() might not exist
        // We'll create the QName from the service name and target namespace
        QName serviceQName = new QName(targetNamespace, service.getName());
        wsdlData.setServiceQName(serviceQName);
        
        // Get the port name (endpoint name in Axis2)
        String portName = service.getEndpointName();
        wsdlData.setPortName(portName);
        
        // Get the location URL (endpoint address in Axis2)
        String locationUrl = service.getEPRs()[0];
        wsdlData.setLocationUrl(locationUrl);
        
        // Get the operation details
        org.apache.axis2.description.AxisOperation axisOperation = service.getOperation(new QName(operationName));
        if (axisOperation == null) {
            throw new Exception(resHash.getString("WSUTIL.WSDLDATA.SOAP.OPERATION.NOT.FOUND", 
                    new Object[] { operationName, service.getName() }));
        }
        
        // Set the SOAP action URI
        String soapAction = axisOperation.getSoapAction();
        wsdlData.setSoapActionURI(soapAction);
        
        // Set the style (document or rpc)
        String style = axisOperation.getStyle();
        wsdlData.setStyle(style != null ? style : DOCUMENT_STYLE);
        
        // Set the encoding (literal or encoded)
        String use = axisOperation.getMessageExchangePattern();
        wsdlData.setEncoding(use);
        
        // In Axis2 1.7.6, MEP_URI_IN_ONLY constant might have a different name
        // We'll use a string literal instead
        if (axisOperation.getMessageExchangePattern().equals("http://www.w3.org/ns/wsdl/in-only")) {
            wsdlData.operationType = OperationType.ONE_WAY;
        } else {
            wsdlData.operationType = OperationType.REQUEST_RESPONSE;
        }
        
        return wsdlData;
    }
    
    /**
     * Extracts parameter names from a WSDL operation.
     *
     * @param definition The WSDL definition
     * @param operation The WSDL operation
     * @return A list of parameter names in order
     */
    private static List<String> extractParameterNames(Definition definition, Operation operation) {
        List<String> paramNames = new ArrayList<String>();
        
        if (operation == null) {
            return paramNames;
        }
        
        try {
            Input input = operation.getInput();
            if (input != null) {
                Message message = input.getMessage();
                if (message != null) {
                    Map<?, ?> parts = message.getParts();
                    if (parts != null) {
                        // Iterate through parts in order
                        for (Object partObj : parts.values()) {
                            if (partObj instanceof Part) {
                                Part part = (Part) partObj;
                                String partName = part.getName();
                                if (partName != null && !partName.isEmpty()) {
                                    paramNames.add(partName);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (log != null) {
                log.logerror("Error extracting parameter names from WSDL operation", e);
            }
        }
        
        return paramNames;
    }
    /**
     * Extracts output parameter names from a WSDL operation.
     *
     * @param definition The WSDL definition
     * @param operation The WSDL operation
     * @return A list of output parameter names in order
     */
    private static List<String> extractOutputParameterNames(Definition definition, Operation operation) {
        List<String> paramNames = new ArrayList<String>();
        
        if (operation == null) {
            return paramNames;
        }
        
        try {
            javax.wsdl.Output output = operation.getOutput();
            if (output != null) {
                Message message = output.getMessage();
                if (message != null) {
                    Map<?, ?> parts = message.getParts();
                    if (parts != null) {
                        // Get parts in order - use getOrderedParts if available
                        java.util.Collection<?> orderedParts = null;
                        try {
                            // Try to get ordered parts (WSDL4J specific method)
                            java.lang.reflect.Method method = message.getClass().getMethod("getOrderedParts", (Class<?>[])null);
                            orderedParts = (java.util.Collection<?>) method.invoke(message, (Object[])null);
                        } catch (Exception e) {
                            // If getOrderedParts not available, use values()
                            orderedParts = parts.values();
                        }
                        
                        // Iterate through parts in order
                        for (Object partObj : orderedParts) {
                            if (partObj instanceof Part) {
                                Part part = (Part) partObj;
                                String partName = part.getName();
                                if (partName != null && !partName.isEmpty()) {
                                    paramNames.add(partName);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (log != null) {
                log.logerror("Error extracting output parameter names from WSDL operation", e);
            }
        }
        
        return paramNames;
    }
}
