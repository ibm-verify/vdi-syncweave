/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.util.DOMUtils;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the com.ibm.di.tp.server.model.config package.
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

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static final JAXBContext ctx;
	private static final Unmarshaller unmarsh;
	static {
		try {
			ctx = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), ObjectFactory.class.getClassLoader());
			unmarsh = ctx.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	private final static QName _Data_QNAME = new QName(Constants.NS_SCMP, "data");
	private final static QName _OpState_QNAME = new QName(Constants.NS_SCMP, "op-state");
	private final static QName _RequestIn_QNAME = new QName(Constants.NS_SCMP, "request-in");
	private final static QName _AdminState_QNAME = new QName(Constants.NS_SCMP, "admin-state");
	private final static QName _RequestOut_QNAME = new QName(Constants.NS_SCMP, "request-out");
	private final static QName _OpStatus_QNAME = new QName(Constants.NS_SCMP, "op-status");
	private final static QName _PropertySheetDefinition_QNAME = new QName(Constants.NS_SCMP, "propertySheetDefinition");
	private final static QName _PropertySheet_QNAME = new QName(Constants.NS_SCMP, "propertySheet");

	private static Marshaller constructMarshaller(String schemaLocation) throws JAXBException, NullPointerException {
		// JAXB causes problems if this method is named "createMarshaller"
		if (schemaLocation == null) {
			throw new NullPointerException();
		}
		Marshaller m = ctx.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
		return m;
	}

	public static <T> Element toElement(T data, Class<T> clazz, String schemaLocation) throws JAXBException {
		JAXBElement<T> jaxbElement = new JAXBElement<T>(_Data_QNAME, clazz, data);

		Element temp = DOMUtils.doc.createElement("temp");

		Marshaller m = constructMarshaller(schemaLocation);
		m.marshal(jaxbElement, temp);

		Element elem = (Element) temp.getFirstChild();
		if (elem != null) {
			// Detach from the parent
			elem = (Element) temp.removeChild(elem);
		}

		return elem;
	}

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: com.ibm.di.tp.server.model.config
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link InstanceData }
	 * 
	 */
	public InstanceData createInstanceData() {
		return new InstanceData();
	}

	public static Element toElement(InstanceData cfg) throws JAXBException {
		return toElement(cfg, InstanceData.class, Constants.NS_SCMP + " touchpoint.xsd");
	}

	/**
	 * Create an instance of {@link InstanceData }
	 * 
	 * @param elem
	 *            the xml element to read from
	 * @throws JAXBException
	 * 
	 */
	public static InstanceData createInstanceData(Element elem) throws JAXBException {
		return ((JAXBElement<InstanceData>) unmarsh.unmarshal(elem, InstanceData.class)).getValue();
	}

	/**
	 * Create an instance of {@link DestinationData }
	 * 
	 */
	public DestinationData createDestinationData() {
		return new DestinationData();
	}

	public static Element toElement(DestinationData cfg) throws JAXBException {
		return toElement(cfg, DestinationData.class, Constants.NS_SCMP + " touchpoint.xsd");
	}

	/**
	 * Create an instance of {@link DestinationData }
	 * 
	 * @param elem
	 *            the xml element to read from
	 * @throws JAXBException
	 * 
	 */
	public static DestinationData createDestinationData(Element elem) throws JAXBException {
		return ((JAXBElement<DestinationData>) unmarsh.unmarshal(elem, DestinationData.class)).getValue();
	}

	/**
	 * Create an instance of {@link StatusData }
	 * 
	 */
	public StatusData createStatusData() {
		return new StatusData();
	}

	public static Element toElement(StatusData status) throws JAXBException {
		return toElement(status, StatusData.class, Constants.NS_SCMP + " touchpoint.xsd");
	}

	public static Element toElement(PropertySheetDefinition def) throws JAXBException {
		return toElement(def, PropertySheetDefinition.class, Constants.NS_SCMP + " propertysheet.xsd");
	}

	/**
	 * Create an instance of {@link StatusData }
	 * 
	 * @param elem
	 *            the xml element to read from
	 * @throws JAXBException
	 * 
	 */
	public static StatusData createStatusData(Element elem) throws JAXBException {
		return ((JAXBElement<StatusData>) unmarsh.unmarshal(elem, StatusData.class)).getValue();
	}

	/**
	 * Create an instance of {@link Property }
	 * 
	 */
	public Property createPropertyType() {
		return new Property();
	}

	/**
	 * Create an instance of {@link TouchpointStatus }
	 * 
	 */
	public TouchpointStatus createTouchpointStatus() {
		return new TouchpointStatus();
	}

	/**
	 * Create an instance of {@link PropertySheet }
	 * 
	 */
	public PropertySheet createPropertySheetType() {
		return new PropertySheet();
	}

	/**
	 * Create an instance of {@link Destination }
	 * 
	 */
	public Destination createDestination() {
		return new Destination();
	}

	/**
	 * Create an instance of {@link Label }
	 * 
	 */
	public Label createLabelType() {
		return new Label();
	}

	/**
	 * Create an instance of {@link PropertyDefinition }
	 * 
	 */
	public PropertyDefinition createPropertyDefinitionType() {
		return new PropertyDefinition();
	}

	/**
	 * Create an instance of {@link StatusOpState }
	 * 
	 */
	public StatusOpState createStatusOpState() {
		return new StatusOpState();
	}

	/**
	 * Create an instance of {@link Option }
	 * 
	 */
	public Option createOptionType() {
		return new Option();
	}

	/**
	 * Create an instance of {@link Touchpoint }
	 * 
	 */
	public Touchpoint createTouchpoint() {
		return new Touchpoint();
	}

	/**
	 * Create an instance of {@link PropertySheetDefinition }
	 * 
	 */
	public PropertySheetDefinition createPropertySheetDefinitionType() {
		return new PropertySheetDefinition();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link EnumOpState }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = Constants.NS_SCMP, name = "op-state")
	public JAXBElement<EnumOpState> createOpState(EnumOpState value) {
		return new JAXBElement<EnumOpState>(_OpState_QNAME, EnumOpState.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = Constants.NS_SCMP, name = "request-in")
	public JAXBElement<String> createRequestIn(String value) {
		return new JAXBElement<String>(_RequestIn_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link EnumAdminState }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = Constants.NS_SCMP, name = "admin-state")
	public JAXBElement<EnumAdminState> createAdminState(EnumAdminState value) {
		return new JAXBElement<EnumAdminState>(_AdminState_QNAME, EnumAdminState.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = Constants.NS_SCMP, name = "request-out")
	public JAXBElement<String> createRequestOut(String value) {
		return new JAXBElement<String>(_RequestOut_QNAME, String.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link StatusOpState }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = Constants.NS_SCMP, name = "op-status")
	public JAXBElement<StatusOpState> createOpStatus(StatusOpState value) {
		return new JAXBElement<StatusOpState>(_OpStatus_QNAME, StatusOpState.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link PropertySheetDefinition }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = Constants.NS_SCMP, name = "propertySheetDefinition")
	public JAXBElement<PropertySheetDefinition> createPropertySheetDefinition(PropertySheetDefinition value) {
		return new JAXBElement<PropertySheetDefinition>(_PropertySheetDefinition_QNAME, PropertySheetDefinition.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <} {@link PropertySheet }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = Constants.NS_SCMP, name = "propertySheet")
	public JAXBElement<PropertySheet> createPropertySheet(PropertySheet value) {
		return new JAXBElement<PropertySheet>(_PropertySheet_QNAME, PropertySheet.class, null, value);
	}

	public JAXBContext getSchemaLocationAwareJaxContext() throws JAXBException {
		return new JAXBContext() {

			private Marshaller m = (Marshaller) Proxy.newProxyInstance(ObjectFactory.class.getClassLoader(),
					new Class<?>[] { Marshaller.class }, new MultiHomedSchemaAwareMarshaller(ctx.createMarshaller()));

			@Override
			public Validator createValidator() throws JAXBException {
				return ctx.createValidator();
			}

			@Override
			public Unmarshaller createUnmarshaller() throws JAXBException {
				return ctx.createUnmarshaller();
			}

			@Override
			public Marshaller createMarshaller() throws JAXBException {
				return m;
			}
		};
	}

	private static class MultiHomedSchemaAwareMarshaller implements InvocationHandler {

		private final Marshaller delegate;

		public MultiHomedSchemaAwareMarshaller(Marshaller delegate) {
			this.delegate = delegate;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (method.getName().equals("marshal")) {
				PropertySheetDefinition sheetDef = null;
				if (args != null && args.length > 0) {
					Object tmp = args[0];

					if (tmp instanceof JAXBElement<?>) {
						tmp = ((JAXBElement<?>) tmp).getValue();
					}

					if (tmp instanceof PropertySheetDefinition && ((PropertySheetDefinition) tmp).getSchemaLocation() != null) {
						sheetDef = (PropertySheetDefinition) tmp;
					}
				}

				if (sheetDef != null) {
					// make sure the properties don't change if this instance is
					// shared (Wink code shares Marshallers)
					synchronized (delegate) {
						Object old = null;
						try {
							old = delegate.getProperty(Marshaller.JAXB_SCHEMA_LOCATION);
						} catch (PropertyException e) {
							// we couldn't get it... try to set it anyway
						}

						try {
							delegate.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, sheetDef.getSchemaLocation());
							return method.invoke(delegate, args);
						} finally {
							if (old != null) {
								delegate.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, old);
							}
						}
					}
				}
			}

			return method.invoke(delegate, args);
		}
	}
}
