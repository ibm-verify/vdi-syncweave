/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.model.descriptor;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the com.ibm.di.model.descriptor package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _ParameterDescriptor_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/71/core",
			"parameterDescriptor");
	private final static QName _ParameterMapDescriptor_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/71/core",
			"parameterMapDescriptor");
	private final static QName _ModeParameterDescriptor_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/71/core",
			"modeParameterDescriptor");
	private static final QName _ConnectorDescriptor_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/71/core",
			"connectorDescriptor");
	private static final QName _FunctionComponentDescriptor_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/71/core",
			"functionComponentDescriptor");
	private static final QName _ParserDescriptor_QNAME = new QName("http://www.ibm.com/xmlns/prod/tdi/71/core", "parserDescriptor");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: com.ibm.di.model.descriptor
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link ConnectorDescriptor.SupportedModes }
	 * 
	 */
	public ConnectorDescriptor.SupportedModes createConnectorDescriptorSupportedModes() {
		return new ConnectorDescriptor.SupportedModes();
	}

	/**
	 * Create an instance of {@link BaseDescriptor }
	 * 
	 */
	public BaseDescriptor createBaseDescriptor() {
		return new BaseDescriptor();
	}

	/**
	 * Create an instance of {@link Option }
	 * 
	 */
	public Option createOption() {
		return new Option();
	}

	/**
	 * Create an instance of {@link FunctionComponentDescriptor }
	 * 
	 */
	public FunctionComponentDescriptor createFunctionComponentDescriptor() {
		return new FunctionComponentDescriptor();
	}

	/**
	 * Create an instance of {@link ParserDescriptor }
	 * 
	 */
	public ParserDescriptor createParserDescriptor() {
		return new ParserDescriptor();
	}

	/**
	 * Create an instance of {@link ModeOption }
	 * 
	 */
	public ModeOption createModeOption() {
		return new ModeOption();
	}

	/**
	 * Create an instance of {@link Label }
	 * 
	 */
	public Label createLabel() {
		return new Label();
	}

    /**
     * Create an instance of {@link SectionDescriptor }
     * 
     */
    public SectionDescriptor createSectionDescriptor() {
        return new SectionDescriptor();
    }

	/**
	 * Create an instance of {@link ConnectorDescriptor }
	 * 
	 */
	public ConnectorDescriptor createConnectorDescriptor() {
		return new ConnectorDescriptor();
	}

	/**
	 * Create an instance of {@link ModeParameterDescriptor }
	 * 
	 */
	public ModeParameterDescriptor createModeParameterDescriptor() {
		return new ModeParameterDescriptor();
	}

	/**
	 * Create an instance of {@link ParameterDescriptor }
	 * 
	 */
	public ParameterDescriptor createParameterDescriptor() {
		return new ParameterDescriptor();
	}

	/**
	 * Create an instance of {@link ParameterMapDescriptor }
	 * 
	 */
	public ParameterMapDescriptor createParameterMapDescriptor() {
		return new ParameterMapDescriptor();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ParameterDescriptor }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", name = "parameterDescriptor")
	public JAXBElement<ParameterDescriptor> createParameterDescriptor(ParameterDescriptor value) {
		return new JAXBElement<ParameterDescriptor>(_ParameterDescriptor_QNAME, ParameterDescriptor.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ParameterMapDescriptor }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", name = "parameterMapDescriptor")
	public JAXBElement<ParameterMapDescriptor> createParameterMapDescriptor(ParameterMapDescriptor value) {
		return new JAXBElement<ParameterMapDescriptor>(_ParameterMapDescriptor_QNAME, ParameterMapDescriptor.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ModeParameterDescriptor }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", name = "modeParameterDescriptor", substitutionHeadNamespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", substitutionHeadName = "parameterDescriptor")
	public JAXBElement<ModeParameterDescriptor> createModeParameterDescriptor(ModeParameterDescriptor value) {
		return new JAXBElement<ModeParameterDescriptor>(_ModeParameterDescriptor_QNAME, ModeParameterDescriptor.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ConnectorDescriptor }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", name = "connectorDescriptor")
	public JAXBElement<ConnectorDescriptor> createConnectorDescriptor(ConnectorDescriptor value) {
		return new JAXBElement<ConnectorDescriptor>(_ConnectorDescriptor_QNAME, ConnectorDescriptor.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ModeParameterDescriptor }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", name = "functionComponentDescriptor")
	public JAXBElement<FunctionComponentDescriptor> createFunctionComponentDescriptor(FunctionComponentDescriptor value) {
		return new JAXBElement<FunctionComponentDescriptor>(_FunctionComponentDescriptor_QNAME, FunctionComponentDescriptor.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ModeParameterDescriptor }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://www.ibm.com/xmlns/prod/tdi/71/core", name = "parserDescriptor")
	public JAXBElement<ParserDescriptor> createParserDescriptor(ParserDescriptor value) {
		return new JAXBElement<ParserDescriptor>(_ParserDescriptor_QNAME, ParserDescriptor.class, null, value);
	}
}
