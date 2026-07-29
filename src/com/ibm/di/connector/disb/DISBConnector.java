/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.disb;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.connector.JMSConnector;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;
import com.ibm.di.connector.disb.model.BaseOperation;
import com.ibm.di.connector.disb.model.ConfigurationItem;
import com.ibm.di.connector.disb.model.Create;
import com.ibm.di.connector.disb.model.Delete;
import com.ibm.di.connector.disb.model.Modify;
import com.ibm.di.connector.disb.model.OperationSet;
import com.ibm.di.connector.disb.model.Refresh;
import com.ibm.di.connector.disb.model.Reference;
import com.ibm.di.connector.disb.model.Relationship;
import com.ibm.dl.core.certification.IdMLCertification;

/**
 * The class DISBConnector facilitates the communication to the Data Integration
 * Services Bus that will be accessed by SyncWeave. It
 * extends the JMS connector class (JMSConnector) and overrides some of its
 * methods to implement DIS-specific functionality.
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1.1
 */
public class DISBConnector extends JMSConnector {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the component.
	 */
	private static final String connectorName = "DISB Connector";

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "disbconnector";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static final ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Holds the JSON/Entry type.
	 */
	private static final String ENTRY_ATTR_TYPE = "type";

	/**
	 * Holds the JSON/Entry type.
	 */
	private static final String ENTRY_ATTR_CLASSTYPE = "ClassType";

	/**
	 * Holds the JSON/Entry guid.
	 */
	private static final String ENTRY_ATTR_GUID = "guid";

	/**
	 * Holds the JSON/Entry ConfigurationItem.
	 */
	private static final String ENTRY_ATTR_CONFIGITEM = "configurationItem";

	/**
	 * Holds the JSON/Entry ModelItem.
	 */
	private static final String ENTRY_ATTR_MODELITEM = "modelItem";

	/**
	 * Holds the JSON message.
	 */
	private static final String ENTRY_ATTR_MESSAGE = "message";

	/**
	 * Holds the Managed Software System.
	 */
	private static final String ENTRY_ATTR_MSS = "Managed Software System";

	/**
	 * The name of topic type parameter from the Connector's configuration
	 * panel.
	 */
	private static final String DISB_TOPIC_TYPE_STR = "topicType";

	/**
	 * The name of Instance Topic parameter from the Connector's configuration
	 * panel.
	 */
	private static final String DISB_INSTANCE_TOPIC_STR = "Instance Topic";

	/**
	 * The name of Model Topic parameter from the Connector's configuration
	 * panel.
	 */
	private static final String DISB_MODEL_TOPIC_STR = "Model Topic";

	/**
	 * The name of GuidLifeCycle Topic parameter from the Connector's
	 * configuration panel.
	 */
	private static final String DISB_GUID_TOPIC_STR = "GuidLifeCycle Topic";

	/**
	 * The prefix for the JMS properties as required by the JMS Connector.
	 */
	private static final String JMS_PREFIX = "jms.";

	/**
	 * Flag to hold the Instance topic type from the Connector's configuration
	 * panel.
	 */
	private boolean isInstanceTopicMsg = false;

	/**
	 * Flag to hold the GuidLifeCycle topic type from the Connector's
	 * configuration panel.
	 */
	private boolean isGUIDTopicMsg = false;

	/**
	 * Flag to hold the Model topic type from the Connector's configuration
	 * panel.
	 */
	private boolean isModelTopicMsg = false;

	/**
	 * Holds the JMS Properties from the Instance Topic.
	 */
	private String[] instanceProperties = { "DISSourceMSSName", "DISSourceMSSGuid", "DISMessageVersion", "DISOperation",
			"DISDestinationMSS" };

	/**
	 * Holds the JMS Properties from the GuidLifeCycle Topic.
	 */
	private String[] guidProperties = { "DISMessageVersion", "DISOperation", "DISDestinationMSS" };

	/**
	 * Holds the JMS Properties from the GuidLifeCycle Topic.
	 */
	private String[] modelProperties = { "DISMessageVersion", "DISOperation", "DISClassNamespaces", "DISAttributeNamespaces",
			"DISMappingPolicyNamespaces", "DISRelationshipNamespaces", "DISNamingRuleNamespaces" };

	/**
	 * Holds the JMS Properties for the DIS In bound Queue.
	 */
	private String[] dDISInboundQProperties = { "DISSourceMSSName", "DISSourceMSSGuid", "DISMessageVersion" };

	/**
	 * The following constants are to identify the Instance or GuidLifeCycle
	 * Topic's JSON message contents.
	 */
	private static final String INSTANCEMSG_KEY = "\"modelObject\":";
	private static final String GUIDMSG_KEY_1 = "\"new\":";
	private static final String GUIDMSG_KEY_2 = "\"old\":";

	/**
	 * JMS Property for DIS Message Version from the DIS Topics.
	 */
	private static final String DISMESSAGEVER = "DISMessageVersion";

	/**
	 * JMS Property for DIS Operation from the DIS Topics.
	 */
	private static final String DISOPERATION = "DISOperation";

	/**
	 * Holds the JMS Property for DIS Operation from the DIS Topics.
	 */
	private String disOperation = null;

	/**
	 * Holds the DIS Message Version for the message received from DIS Topics.
	 */
	private String disMessageVersion = null;

	/**
	 * JMS Properties for DIS Model Topic in the Entry.
	 */
	private static final String ENTRY_ATTR_MODEL_INFO = "Model Item Information";

	/**
	 * Holds the IDML Message Flag value from the Configuration.
	 */
	private String idmlMsgFlag = "";

	/**
	 * Holds the validate IDML Message Flag from the configuration.
	 */
	private String validateIdmlFlag = "";

	/**
	 * Holds the JSON Message Flag value from the Configuration.
	 */
	private String jsonMsgFlag = "";

	/**
	 * Holds the parameter name for the IDML flag from the Configuration.
	 */
	private static final String CONFIG_ATTR_IDML_MODE = "idmlMsgFlag";

	/**
	 * Holds the parameter name for the JSON flag from the Configuration.
	 */
	private static final String CONFIG_ATTR_JSON_MODE = "jsonMsgFlag";

	/**
	 * Holds the parameter name for the Validate IDML flag from the
	 * Configuration.
	 */
	private static final String CONFIG_ATTR_IDML_VALIDATE = "validateIdmlFlag";

	/**
	 * Holds the IDML message in Entry.
	 */
	private static final String ENTRY_ATTR_DISIDMLMSG = "DISIDMLMessage";

	/**
	 * Constructor. Initializes the connector to work in AddOnly and Iterator
	 * Modes.
	 */
	public DISBConnector() {
		setName(connectorName);
		setModes(new String[] { ConnectorConfig.ADDONLY_MODE, ConnectorConfig.ITERATOR_MODE });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(Object o) throws Exception {
		super.initialize(o);

		// This parameter is used for the iterator mode.
		String disbTopicType = (String) getParam(DISB_TOPIC_TYPE_STR);

		if (disbTopicType != null) {
			if (disbTopicType.equalsIgnoreCase(DISB_INSTANCE_TOPIC_STR)) {
				isInstanceTopicMsg = true;
			} else if (disbTopicType.equalsIgnoreCase(DISB_MODEL_TOPIC_STR)) {
				isModelTopicMsg = true;
			} else if (disbTopicType.equalsIgnoreCase(DISB_GUID_TOPIC_STR)) {
				isGUIDTopicMsg = true;
			}
			if (debugMode()) {
				debug(sResHash.getString("DISB.CONN.PARAMETER.INITIALIZED", new Object[] { DISB_TOPIC_TYPE_STR, disbTopicType }));
			}
		}
		jsonMsgFlag = getParam(CONFIG_ATTR_JSON_MODE);
		idmlMsgFlag = getParam(CONFIG_ATTR_IDML_MODE);
		validateIdmlFlag = getParam(CONFIG_ATTR_IDML_VALIDATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Entry getNextEntry() throws Exception {

		// Call parent class method
		Entry entry = super.getNextEntry();

		if (entry == null) {
			return entry;
		}

		// Get the raw message
		String msg = entry.getAttribute(ENTRY_ATTR_MESSAGE).getValue();

		// DISB specific JSON Message transformer
		DISBJSONMessageTransformer jsonMsgTransformer = new DISBJSONMessageTransformer();

		// Entry that will be returned
		Entry retEntry = new Entry();

		OperationSet opSet = null;

		boolean isAttribute = false;
		// Get the JMS properties from the entry properties
		String[] props = entry.getPropertyNames();
		if (props.length > 0) {
			buildEntryForProperties(entry, retEntry, props, isAttribute);
		} else {
			// Get the JMS properties from the entry attributes
			props = entry.getAttributeNames();
			// Populate the new Entry Object with the JMS Properties
			if (props.length > 0) {
				isAttribute = true;
				buildEntryForProperties(entry, retEntry, props, isAttribute);
			}
		}

		if (isGUIDTopicMsg) {
			if (msg.indexOf(GUIDMSG_KEY_1) == -1 || msg.indexOf(GUIDMSG_KEY_2) == -1) {
				throw new Exception(sResHash.getString("INVALID.JSON.MESSAGE", DISB_GUID_TOPIC_STR));
			} else {
				if (disMessageVersion != null) {
					retEntry.setAttribute(DISMESSAGEVER, disMessageVersion);
				}
				if (disOperation != null) {
					retEntry.setAttribute(DISOPERATION, disOperation);
				}
				ConfigurationItem ci = jsonMsgTransformer.getGuidConfigItem(msg);
				Attribute ciAttr = retEntry.createElement(ci.getClassNameType());
				HashMap<String, Object> attr = (HashMap<String, Object>) ci.getAttributes();
				if (ciAttr != null) {
					buildEntryCiAttributes(retEntry, attr, ciAttr);
				}
				retEntry.appendChild(ciAttr);
			}
		} else if (isInstanceTopicMsg) {
			if (msg.indexOf(INSTANCEMSG_KEY) == -1) {
				throw new Exception(sResHash.getString("INVALID.JSON.MESSAGE", DISB_INSTANCE_TOPIC_STR));
			} else {
				opSet = jsonMsgTransformer.getOperationSet(msg);
			}
		} else if (isModelTopicMsg) {
			opSet = jsonMsgTransformer.getOperationSet(msg);
		}

		// Applies only for Instance and Model Topic messages

		if (opSet != null) {
			if (disMessageVersion != null) {
				retEntry.setAttribute(DISMESSAGEVER, disMessageVersion);
			}
			if (disOperation != null) {
				retEntry.setAttribute(DISOPERATION, disOperation);
			}

			retEntry.setAttribute(DISBConstants.JSONMSG_ATTR_OPSETID, opSet.getOpId());

			Create create = opSet.getCreate();
			Delete delete = opSet.getDelete();
			Modify modify = opSet.getModify();
			Refresh refresh = opSet.getRefresh();
			Reference reference = opSet.getReference();
			boolean isRefresh = false;

			if (reference != null) {
				Attribute referenceAttr = retEntry.createElement(DISBConstants.JSONMSG_ATTR_REFERENCE);
				buildEntryForOperation(retEntry, reference, referenceAttr, isRefresh);
			}

			if (create != null) {
				Attribute createAttr = retEntry.createElement(DISBConstants.JSONMSG_ATTR_CREATE);
				buildEntryForOperation(retEntry, create, createAttr, isRefresh);
			}

			if (delete != null) {
				Attribute deleteAttr = retEntry.createElement(DISBConstants.JSONMSG_ATTR_DELETE);
				buildEntryForOperation(retEntry, delete, deleteAttr, isRefresh);
			}

			if (modify != null) {
				Attribute modifyAttr = retEntry.createElement(DISBConstants.JSONMSG_ATTR_MODIFY);
				buildEntryForOperation(retEntry, modify, modifyAttr, isRefresh);
			}

			if (refresh != null) {
				isRefresh = true;
				Attribute createAttr = retEntry.createElement(DISBConstants.JSONMSG_ATTR_CREATE);
				buildEntryForOperation(retEntry, refresh.getCreate(), createAttr, isRefresh);
			}

		}

		return retEntry;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putEntry(Entry entry) throws Exception {
		Entry toPushEntry = new Entry();
		boolean isAttribute = false;
		List<String> dDISInboundQPropertiesList = Arrays.asList(dDISInboundQProperties);

		if (idmlMsgFlag.equalsIgnoreCase("true")) {
			String iIDMLMessage = entry.getString(ENTRY_ATTR_DISIDMLMSG);
			if (iIDMLMessage == null || iIDMLMessage.equals("")) {
				throw new Exception(sResHash.getString("INSUFFICIENT.DATA.TO.PROCEED", "DISIDMLMessage"));
			}
			createJSONFromIDMLInputEntry(iIDMLMessage, dDISInboundQPropertiesList, toPushEntry);
		} else if (jsonMsgFlag.equalsIgnoreCase("true")) {
			String jJSONMessage = entry.getString(DISBConstants.JSONMSG_ATTR_MESSAGE);
			if (jJSONMessage == null || jJSONMessage.equals("")) {
				throw new Exception(sResHash.getString("INSUFFICIENT.DATA.TO.PROCEED", DISBConstants.JSONMSG_ATTR_MESSAGE));
			}
			toPushEntry.setAttribute(DISBConstants.JMSMSG_ATTR_MSG, jJSONMessage);
			// Get the JMS properties from the entry Properties
			String[] props = entry.getPropertyNames();
			// Populate the Entry Object with the JMS Properties after the
			// prefix "jms."
			if (props.length > 0) {
				List<String> properties = Arrays.asList(props);
				for (String instProp : dDISInboundQPropertiesList) {
					if (!properties.contains(instProp)) {
						throw new Exception(sResHash.getString("INSUFFICIENT.DATA.TO.PROCEED", instProp));
					}
				}
				for (String entryProp : props) {
					if (entryProp.indexOf(DISBConstants.JSONMSG_ATTR_MESSAGE) == -1) {
						createPropertyInputEntry(entry, dDISInboundQPropertiesList, entryProp, toPushEntry, isAttribute);
					}
				}
			} else {
				// Get the JMS properties from the entry attributes
				props = entry.getAttributeNames();
				// Populate the Entry Object with the JMS Properties after the
				// prefix "jms."
				if (props.length > 0) {
					isAttribute = true;
					List<String> properties = Arrays.asList(props);
					for (String instProp : dDISInboundQPropertiesList) {
						if (!properties.contains(instProp)) {
							throw new Exception(sResHash.getString("INSUFFICIENT.DATA.TO.PROCEED", instProp));
						}
					}
					for (String entryAttr : props) {
						if (entryAttr.indexOf(DISBConstants.JSONMSG_ATTR_MESSAGE) == -1) {
							createPropertyInputEntry(entry, dDISInboundQPropertiesList, entryAttr, toPushEntry, isAttribute);
						}
					}
				}
			}
		}
		super.putEntry(toPushEntry);
	}

	/**
	 * Populates the Entry Attribute with the JMS properties for the In bound
	 * Queue in AddOnly mode.
	 * 
	 * @param rcvdEntry
	 *            The Entry that contains the property value.
	 * @param dDISInboundQPropertiesList
	 *            The List containing the JMS property names.
	 * @param entryProperty
	 *            The Entry that will contain the message properties that needs
	 *            to be prefixed with ".jms".
	 * @param toBePushedEntry
	 *            The Entry that needs to be populated with the property with
	 *            the prefixed value.
	 * @param isAttribute
	 *            Flag that tells whether the property is part of Entry Property
	 *            or Attribute.
	 */
	private void createPropertyInputEntry(Entry rcvdEntry, List<String> dDISInboundQPropertiesList, String entryProperty,
			Entry toBePushedEntry, boolean isAttribute) throws Exception {
		if (dDISInboundQPropertiesList.contains(entryProperty)) {
			if (isAttribute) {
				toBePushedEntry.setAttribute(JMS_PREFIX + entryProperty, rcvdEntry.getAttribute(entryProperty).getValue());
			} else {
				toBePushedEntry.setProperty(JMS_PREFIX + entryProperty, rcvdEntry.getProperty(entryProperty));
			}
		}
	}

	/**
	 * Builds the Entry with the JMS properties based on the relevant DISB Topic
	 * Types in Iterator mode.
	 * 
	 * @param rcvdEntry
	 *            The Entry containing the JMS properties with the prefix ".jms"
	 *            for the attribute names
	 * @param toBeBuiltEntry
	 *            The Entry that will contain the message properties with the
	 *            prefix ".jms" for the attribute names removed.
	 * @param props
	 *            The String array containing the attribute names of the JMS
	 *            properties
	 * @param isAttribute
	 *            Flag that tells whether the property is part of Entry Property
	 *            or Attribute.
	 * @throws Exception
	 *             If a problem occurs.
	 */
	protected void buildEntryForProperties(Entry rcvdEntry, Entry toBeBuiltEntry, String[] props, boolean isAttribute)
			throws Exception {
		List<String> instancePropertiesList = Arrays.asList(instanceProperties);
		List<String> guidPropertiesList = Arrays.asList(guidProperties);
		List<String> modelPropertiesList = Arrays.asList(modelProperties);
		Attribute propAttr = null;
		if (isModelTopicMsg) {
			propAttr = new Attribute(ENTRY_ATTR_MODEL_INFO);
		} else {
			propAttr = new Attribute(ENTRY_ATTR_MSS);
		}

		for (String entryProp : props) {

			if (entryProp.indexOf(JMS_MESSAGE_PROPERTY) != -1 && debugMode()) {
				// This displays the actual raw message as received on the Topic
				debug(sResHash.getString("MESSAGE.RECIEVED.FROM.TOPIC", (isAttribute ? rcvdEntry.getAttribute(entryProp).getValue()
						: rcvdEntry.getProperty(entryProp))));
			}
			if (entryProp.startsWith(JMS_PREFIX)) {
				if (isInstanceTopicMsg) {
					createPropertyEntryAttribute(instancePropertiesList, entryProp, propAttr, rcvdEntry, isAttribute);
				} else if (isGUIDTopicMsg) {
					createPropertyEntryAttribute(guidPropertiesList, entryProp, propAttr, rcvdEntry, isAttribute);
				} else if (isModelTopicMsg) {
					createPropertyEntryAttribute(modelPropertiesList, entryProp, propAttr, rcvdEntry, isAttribute);
				}
			}
		}
		NodeList nodes = propAttr.getChildNodes();
		if (nodes.getLength() > 0) {
			toBeBuiltEntry.appendChild(propAttr);
		} else {
			// Log if none of the DIS Message properties were present in the
			// message
			if (debugMode()) {
				debug(sResHash.getString("JMS.PROPERTIES.OF.JSON.PAYLOAD.MISSING"));
			}
		}
	}

	/**
	 * Populates the Entry Attribute with the JMS properties in Iterator mode.
	 * 
	 * @param properties
	 *            The String Array containing the JMS property names.
	 * @param entryProperty
	 *            The Entry that will contain the message properties with the
	 *            prefix ".jms".
	 * @param prop
	 *            The Attribute that needs to be populated with the property
	 *            value.
	 * @param rcvdEntry
	 *            The Entry that needs to be populated with the property value.
	 * @param isAttribute
	 *            Flag that tells whether the property is part of Entry Property
	 *            or Attribute.
	 */
	private void createPropertyEntryAttribute(List<String> properties, String entryProperty, Attribute Prop, Entry rcvdEntry,
			boolean isAttribute) {
		for (String instProp : properties) {
			if (entryProperty.indexOf(instProp) != -1) {
				if (entryProperty.indexOf(DISMESSAGEVER) != -1) {
					disMessageVersion = (isAttribute ? (String) rcvdEntry.getAttribute(entryProperty).getValue()
							: (String) rcvdEntry.getProperty(entryProperty));
				} else if (entryProperty.indexOf(DISOPERATION) != -1) {
					disOperation = isAttribute ? (String) rcvdEntry.getAttribute(entryProperty).getValue() : (String) rcvdEntry
							.getProperty(entryProperty);
				} else {
					Attribute mssAttr = new Attribute(entryProperty.substring(4));
					mssAttr.addValue(isAttribute ? rcvdEntry.getAttribute(entryProperty).getValue() : rcvdEntry
							.getProperty(entryProperty));
					Prop.appendChild(mssAttr);
				}
			}
		}
	}

	/**
	 * Method populates the Entry with the configuration items based on the
	 * Topic types along with the relationships if existing. If it's Instance
	 * Topic, builds the "configurationItem". If its Model Topic, builds the
	 * "modelItem". If its GuidLifeCycleTopic, builds the "guid". If
	 * relationships exist then the "relationship" is built.
	 * 
	 * @param entry
	 *            The Entry that needs to be populated.
	 * @param operation
	 *            The DIS model BaseOperation object.
	 * @param operAttr
	 *            The Attribute that needs to be populated.
	 * @param instanceTopicMsg
	 *            The flag to identify the DIS instance topic.
	 * @param isRefresh
	 *            The flag to identify the refresh instance topic object.
	 */
	@SuppressWarnings("unchecked")
	protected void buildEntryForOperation(Entry entry, BaseOperation operation, Attribute operAttr, boolean isRefresh) {
		String timeStamp = operation.getTimeStamp();
		if (timeStamp != null) {
			Attribute tsAttr = entry.createElement(DISBConstants.JSONMSG_ATTR_TIMESTAMP);
			tsAttr.setValue(timeStamp);
			operAttr.appendChild(tsAttr);
		}

		ConfigurationItem[] configItems = operation.getConfigurationItems();
		if (configItems != null) {
			for (ConfigurationItem ci : configItems) {
				if (ci != null) {

					Attribute ciAttr = isInstanceTopicMsg ? entry.createElement(ENTRY_ATTR_CONFIGITEM) : entry
							.createElement(ENTRY_ATTR_MODELITEM);
					if (ci.getGuid() != null) {
						Attribute guid = entry.createElement(ENTRY_ATTR_GUID);
						guid.setNodeValue(ci.getGuid().toString());
						ciAttr.appendChild(guid);
					}
					if (ci.getClassNameType() != null) {
						Attribute ciType = entry.createElement(ENTRY_ATTR_CLASSTYPE);
						ciType.setNodeValue(ci.getClassNameType());
						ciAttr.appendChild(ciType);
					}
					if (!isInstanceTopicMsg) {
						// If message is from Model Topic, need to include type
						// as attribute
						// to identify the model item
						Attribute ciType = entry.createElement(ENTRY_ATTR_TYPE);
						ciType.setNodeValue(ci.getClassNameType());
						ciAttr.appendChild(ciType);
					}

					HashMap<String, Object> attr = ci.getAttributes();
					if (attr != null) {
						buildEntryCiAttributes(entry, attr, ciAttr);
						operAttr.appendChild(ciAttr);
					}
				}
			}
		}

		Relationship[] rel = operation.getRelationships();

		if (rel != null) {
			for (Relationship thisRel : rel) {
				Attribute relationship = entry.createElement(DISBConstants.JSONMSG_ATTR_RELATIONSHIP);

				Attribute relType = entry.createElement(ENTRY_ATTR_TYPE);
				relType.setNodeValue(thisRel.getRelationShipType());

				Attribute relSrc = entry.createElement(DISBConstants.JSONMSG_ATTR_SOURCE);
				relSrc.setNodeValue(thisRel.getSource());

				Attribute relTarget = entry.createElement(DISBConstants.JSONMSG_ATTR_TARGET);
				relTarget.setNodeValue(thisRel.getTarget());

				relationship.appendChild(relType);
				relationship.appendChild(relSrc);
				relationship.appendChild(relTarget);

				operAttr.appendChild(relationship);
			}
		}

		if (isRefresh) {
			Attribute refreshAttr = entry.createElement(DISBConstants.JSONMSG_ATTR_REFRESH);
			if (timeStamp != null) {
				Attribute tsAttr = entry.createElement(DISBConstants.JSONMSG_ATTR_TIMESTAMP);
				tsAttr.setValue(timeStamp);
				refreshAttr.appendChild(tsAttr);
			}
			refreshAttr.appendChild(operAttr);
			entry.appendChild(refreshAttr);
		} else {
			entry.appendChild(operAttr);
		}
	}

	/**
	 * Method populates the Entry with the Configuration items. The method
	 * recursively calls itself to build the entry.
	 * 
	 * @param entry
	 *            The Entry that needs to be populated.
	 * @param attr
	 *            The HashMap containing the CI Attributes
	 * @param entryAttr
	 *            The Attribute that will contain the built attributes.
	 */
	@SuppressWarnings("unchecked")
	protected void buildEntryCiAttributes(Entry entry, HashMap<String, Object> attr, Attribute entryAttr) {
		for (Map.Entry<String, Object> mapEntry : attr.entrySet()) {
			String key = mapEntry.getKey();
			String entryAttrKey = key;
			// The attributes in the HashMap can have attribute keys
			// with key names containing array like index representation.
			// However
			// we do not want entry attribute key names to have this
			// array like index representation.
			if (key.contains("[")) {
				entryAttrKey = key.substring(0, key.indexOf("["));
			}
			Attribute thisAttr = entry.createElement(entryAttrKey);
			if (attr.get(key) instanceof String) {
				String value = (String) attr.get(key);
				thisAttr.setNodeValue(value);
			} else if (attr.get(key) instanceof HashMap) {
				// Recursive Call
				buildEntryCiAttributes(entry, (HashMap<String, Object>) attr.get(key), thisAttr);
			}
			entryAttr.appendChild(thisAttr);
		}
	}

	/**
	 * Method receives the Entry with the IDML message and then validates if
	 * configured to validate the IdML and parses the IDML Message to JSON
	 * Message for the DIS Inbound Queue in AddOnly mode.
	 * 
	 * @param rcvdEntry
	 *            The Entry that contains the IDML message.
	 * @param dDISInboundQPropertiesList
	 *            The List containing the JMS property names.
	 * @param toBePushedEntry
	 *            The Entry that needs to be populated with the transformed JSON
	 *            message.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void createJSONFromIDMLInputEntry(String iIDMLMessage, List<String> dDISInboundQPropertiesList, Entry toBePushedEntry)
			throws Exception {
		boolean isValid = true;
		if (validateIdmlFlag.equalsIgnoreCase("true")) {
			IdMLCertification idmlCertificator = new IdMLCertification();
			InputSource inputSource = new InputSource(new StringReader(iIDMLMessage));
			isValid = idmlCertificator.validate(ENTRY_ATTR_DISIDMLMSG, inputSource);
		}
		if (isValid) {
			InputStream in = new ByteArrayInputStream(iIDMLMessage.getBytes());
			DISBIDMLMessageTransformer disbIdmlTrns = new DISBIDMLMessageTransformer();
			disbIdmlTrns.transformIDML(in);
			String jJSONMessage = disbIdmlTrns.getjSONMessage();
			if (jJSONMessage != null) {
				toBePushedEntry.setAttribute(DISBConstants.JMSMSG_ATTR_MSG, jJSONMessage);
			} else {
				throw new Exception(
						sResHash.getString("INSUFFICIENT.DATA.TO.PROCEED", "Transformed JSON Message from IDML is null"));
			}
			HashMap<String, Object> props = (HashMap<String, Object>) disbIdmlTrns.getMsgProperties();
			if (props != null) {
				for (String instProp : dDISInboundQPropertiesList) {
					if (!props.containsKey(instProp) && debugMode()) {
						debug(sResHash.getString("JMS.PROPERTIES.OF.IDML.PAYLOAD.MISSING", instProp));
					}
				}
				for (Map.Entry<String, Object> mapEntry : props.entrySet()) {
					String key = mapEntry.getKey();
					if (dDISInboundQPropertiesList.contains(key)) {
						toBePushedEntry.setAttribute(JMS_PREFIX + key, props.get(key));
					}
				}
			}
		} else {
			throw new Exception(sResHash.getString("IDML.DATA.FAILED.CERTIFICATION"));
		}
	}

	/**
	 * @return The container with Connector's messages.
	 */
	public static ResourceHash getResHash() {
		return sResHash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVersion() {
		return "1.0-di7.1.1 %I%, 20%E%";
	}

}