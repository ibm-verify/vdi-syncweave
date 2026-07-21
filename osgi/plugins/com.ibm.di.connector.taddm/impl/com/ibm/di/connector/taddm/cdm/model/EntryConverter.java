/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm.model;

import static com.ibm.di.cdm.core.CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_CYCLE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_ID_SYSTEM_ATTRIBUTE;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.collation.platform.model.Guid;
import com.collation.platform.model.ModelObject;
import com.collation.proxy.api.util.ModelObjectFactory;
import com.ibm.di.cdm.core.CDMUtils;
import com.ibm.di.connector.taddm.TADDMConnector;
import com.ibm.di.connector.taddm.cdm.TADDMMetaData;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * This class is used for creating Model Objects from hierarchical entries.
 */
public class EntryConverter {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The meta-data.
	 */
	private TADDMMetaData metaData;

	/**
	 * The Log used for logging messages.
	 */
	private Log log;

	/**
	 * Constructor.
	 * 
	 * @param metaData
	 *            the meta-data.
	 * @param log
	 *            the log.
	 */
	public EntryConverter(TADDMMetaData metaData, Log log) {
		this.metaData = metaData;
		this.log = log;
	}

	/**
	 * Creates or update a Model Object from the provided entry.
	 * 
	 * @param classType
	 *            the type of the Model Object. It can be overwritten in from he
	 *            Entry.
	 * @param entry
	 *            the Entry containing the data.
	 * @param baseModelObject
	 *            the ModelObject that will be update, if is <b>null</b> the new
	 *            one will be created.
	 * @return the created Model Object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public ModelObject convert(String classType, Entry entry, ModelObject baseModelObject) throws Exception {
		Map<String, ModelObject> previousModelObjects = new HashMap<String, ModelObject>();
		return createModelObject(classType, entry, baseModelObject, previousModelObjects);
	}

	/**
	 * Creates or update a Model Object from the provided info.
	 * 
	 * @param classType
	 *            the type of the Model Object. It can be overwritten in from
	 *            the Entry.
	 * @param content
	 *            the node containing the data.
	 * @param baseModelObject
	 *            the ModelObject that will be update, if is <b>null</b> the new
	 *            one will be created.
	 * @param previousModelObjects
	 *            previously added ModelObjects on the same iteration. Used for
	 *            resolving cycle system attributes.
	 * @return the populated Model Object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private ModelObject createModelObject(String classType, Node content, ModelObject baseModelObject,
			Map<String, ModelObject> previousModelObjects) throws Exception {
		// handle cycles
		ModelObject cycleModelObject = getCycleModelObject(content, previousModelObjects);
		if (cycleModelObject != null) {
			return cycleModelObject;
		}

		// checks if the class type is overwritten
		classType = getClassType(content, classType);
		ModelObject thisModelObject = getNewModelObject(content, classType, baseModelObject, previousModelObjects);

		NodeList nodeList = content.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node attribute = nodeList.item(i);
			String attributeName = attribute.getNodeName();
			if (CDM_ID_SYSTEM_ATTRIBUTE.equals(attributeName) || CDM_CLASSTYPE_SYSTEM_ATTRIBUTE.equals(attributeName)
					|| CDM_CYCLE_SYSTEM_ATTRIBUTE.equals(attributeName)) {
				continue;
			}

			if (metaData.isImplicitAttribute(attributeName)) {
				for (Map.Entry<String, List<ModelObject>> entry : getImplicitAttributes(attribute, classType, thisModelObject,
						previousModelObjects).entrySet()) {
					setImplicitAttribute(thisModelObject, entry.getKey(), entry.getValue(), false);
				}
			} else {
				setExplicitAttribute(thisModelObject, attributeName, attribute.getNodeValue());
			}
		}
		return thisModelObject;
	}

	/**
	 * Return the model object that is a cycled connected or null if there is no
	 * such a object.
	 * 
	 * @param content
	 *            the node containing the data.
	 * @param previousModelObjects
	 *            previously added ModelObjects on the same iteration. Used for
	 *            resolving cycle system attributes.
	 * @return the model object that is a cycled connected or null if there is
	 *         no such a object.
	 * @throws Exception
	 *             if a problem occurs
	 */
	private ModelObject getCycleModelObject(Node content, Map<String, ModelObject> previousModelObjects) throws Exception {
		String cycle = getSystemAttribute(content, CDM_CYCLE_SYSTEM_ATTRIBUTE);
		if (cycle != null && cycle.length() > 0) {
			printDebugMessage("TADDM.CONN.CYCLE.ATTRIBUTE.FOUND", cycle);
			ModelObject cyclicModelObject = previousModelObjects.get(cycle);
			if (cyclicModelObject != null) {
				return cyclicModelObject;
			} else {
				throw new Exception(getMessage("TADDM.CONN.UNKNOWN.CYCLE.ATTRIBUTE.FOUND", CDMUtils.getAttributePath(content)));
			}
		}
		return null;
	}

	/**
	 * Get the correct class type.
	 * 
	 * @param content
	 *            the node containing the data.
	 * @param currentClassType
	 *            the type of the Model Object.
	 * @return the correct class type.
	 * @throws Exception
	 *             if a problem occurs
	 */
	private String getClassType(Node content, String currentClassType) throws Exception {
		String entryClassType = getSystemAttribute(content, CDM_CLASSTYPE_SYSTEM_ATTRIBUTE);
		if (entryClassType != null && entryClassType.length() > 0) {
			printDebugMessage("TADDM.CONN.CLASS.TYPE.OVERWRITTEN", currentClassType, entryClassType);
			currentClassType = entryClassType;
		}

		if (currentClassType == null) {
			throw new Exception(getMessage("TADDM.CONN.NO.CLASS.TYPE"));
		}
		currentClassType = CDMUtils.removePrefix(currentClassType);
		currentClassType = CDMUtils.removeEscapeChars(currentClassType);
		return currentClassType;
	}

	/**
	 * Get the model object from TADDM if exist, otherwise create new one.
	 * 
	 * @param content
	 *            the node containing the data.
	 * @param classType
	 *            the type of the Model Object.
	 * @param baseModelObject
	 *            the ModelObject that will be update, if is <b>null</b> the new
	 *            one will be created.
	 * @param previousModelObjects
	 *            previously added ModelObjects on the same iteration. Used for
	 *            resolving cycle system attributes.
	 * @return the model object from TADDM if exist, otherwise create new one.
	 * @throws Exception
	 *             if a problem occurs
	 */
	private ModelObject getNewModelObject(Node content, String classType, ModelObject baseModelObject,
			Map<String, ModelObject> previousModelObjects) throws Exception {
		String id = getSystemAttribute(content, CDM_ID_SYSTEM_ATTRIBUTE);
		ModelObject modelObject = null;
		if (baseModelObject != null) {
			modelObject = baseModelObject;
			try {
				id = modelObject.getGuid().toString();
			} catch (Exception e) {
				// ignore (if we are updating the baseModelObject will always
				// have a Guid)
			}
		} else {
			// create new empty model object
			modelObject = createEmptyModelObject(classType);
		}

		previousModelObjects.put(id, modelObject);
		return modelObject;
	}

	/**
	 * The method constructs and returns map of implicit attribute names and
	 * list of its ModelObjects.
	 * 
	 * @param implicitAttributesNode
	 *            The node containing implicit attributes.
	 * @param classType
	 *            the type of the Model Object. It can be overwritten in from
	 *            the Entry.
	 * @param thisModelObject
	 *            parent model object.
	 * @param previousModelObjects
	 *            previously added ModelObjects on the same iteration. Used for
	 *            resolving cycle system attributes.
	 * @return map of implicit attribute names and list of its ModelObjects.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Map<String, List<ModelObject>> getImplicitAttributes(Node implicitAttributesNode, String classType,
			ModelObject thisModelObject, Map<String, ModelObject> previousModelObjects) throws Exception {
		Map<String, List<ModelObject>> implicitAttributes = new HashMap<String, List<ModelObject>>();
		for (Map.Entry<String, List<Node>> mapEntry : metaData.getTADDMImplicitAttributes(classType, implicitAttributesNode)
				.entrySet()) {
			String implicitAttrName = mapEntry.getKey();
			List<ModelObject> objects = implicitAttributes.get(implicitAttrName);
			if (objects == null) {
				objects = new ArrayList<ModelObject>();
				implicitAttributes.put(implicitAttrName, objects);
			}

			Class<?> relatedItemType = getSimpleClass(getPropertyType(thisModelObject, implicitAttrName));
			ModelObjectProperty existingModelObjects = new ModelObjectProperty(thisModelObject, implicitAttrName);
			for (Node node : mapEntry.getValue()) {
				ModelObject oldModelObject = null;
				if (existingModelObjects.isSingleValued()) {
					// use first
					oldModelObject = existingModelObjects.get();
				} else {
					String nodeId = getSystemAttribute(node, CDM_ID_SYSTEM_ATTRIBUTE);
					if (nodeId != null) {
						oldModelObject = existingModelObjects.get(nodeId);
					}
				}

				ModelObject newModelObject = createModelObject(relatedItemType.getCanonicalName(), node, oldModelObject,
						previousModelObjects);
				if (oldModelObject == null) {
					// For newly created model objects add a reverse
					// relationship (where applicable).
					addReverseRelationship(thisModelObject, newModelObject, classType, implicitAttrName);
				}
				objects.add(newModelObject);
			}
			objects.addAll(existingModelObjects.getUnmodified());
		}
		return implicitAttributes;
	}

	/**
	 * Check if the provided class is an array and return its element class. If
	 * this class is not array just return it.
	 * 
	 * @param clazz
	 *            to be checked.
	 * @return class of the provided class or of its elements.
	 */
	private Class<?> getSimpleClass(Class<?> clazz) {
		if (clazz.isArray()) {
			return clazz.getComponentType();
		}
		return clazz;
	}

	/**
	 * Creates an empty Model Object from the provided class type.
	 * 
	 * @param classType
	 *            the object's class type.
	 * @return the created object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private ModelObject createEmptyModelObject(String classType) throws Exception {
		String taddmClassName = metaData.getTADDMClassType(classType);
		if (taddmClassName == null) {
			throw new IllegalArgumentException(getMessage("TADDM.CONN.UNKNOWN.TADDM.CLASS.TYPE", classType));
		}

		Class<?> clazz = Class.forName(taddmClassName);
		return (ModelObject) ModelObjectFactory.newInstance(clazz);
	}

	/**
	 * Gets the type of the provided attribute.
	 * 
	 * @param modelObject
	 *            the Model Object containing this property.
	 * @param attributeName
	 *            the attribute name.
	 * @return the attribute's type.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private Class<?> getPropertyType(ModelObject modelObject, String attributeName) throws Exception {
		Class<?> type = PropertyUtils.getPropertyType(modelObject, attributeName);
		if (type == null) {
			throw new Exception(getMessage("TADDM.CONN.UNKNOWN.ATTRIBUTE.TYPE", attributeName, metaData
					.getModelObjectClass(modelObject)));
		}
		return type;
	}

	/**
	 * In TADDM when we add a Model Object (child) to another Model Object
	 * (parent), we also need to update the child setting the parent to its
	 * implicit attribute.
	 * 
	 * @param firstObject
	 *            the 'parent' Model Object.
	 * @param secondObject
	 *            the 'child' Model Object.
	 * @param firstClassType
	 *            the first object's class type.
	 * @param firstImplicitAttributeName
	 *            the name of the implicit attribute in the first object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void addReverseRelationship(ModelObject firstObject, ModelObject secondObject, String firstClassType,
			String firstImplicitAttributeName) throws Exception {
		String secondImplicitAttributeName = metaData.getTADDMReversedImplicitAttributeName(firstClassType,
				firstImplicitAttributeName);
		if (secondImplicitAttributeName != null) {
			List<ModelObject> temp = new ArrayList<ModelObject>();
			temp.add(firstObject);
			setImplicitAttribute(secondObject, secondImplicitAttributeName, temp, true);
		} else {
			printDebugMessage("TADDM.CONN.ERROR.GETTING.REVERSE.IMPLICIT.ATTRIBUTE", firstImplicitAttributeName, firstClassType,
					metaData.getModelObjectClass(secondObject));
		}
	}

	/**
	 * Adds an explicit attribute to the provided Model Object.
	 * 
	 * @param modelObject
	 *            the Model Object.
	 * @param attributeName
	 *            the explicit attribute's name.
	 * @param attributeValue
	 *            the explicit attribute'd value.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void setExplicitAttribute(ModelObject modelObject, String attributeName, String attributeValue) throws Exception {
		attributeName = CDMUtils.removePrefix(attributeName);
		attributeName = metaData.getTADDMExplicitAttributeName(attributeName);
		Class<?> type = getPropertyType(modelObject, attributeName);
		Object value = null;
		if (Guid.class.isAssignableFrom(type)) {
			value = new Guid(attributeValue);
		} else {
			value = ConvertUtils.convert(attributeValue, type);
		}
		try {
			PropertyUtils.setSimpleProperty(modelObject, attributeName, value);
		} catch (NoSuchMethodException nsme) {
			printDebugMessage("TADDM.CONN.SKIP.READONLY.ATTRIBUTE", attributeName);
		}
	}

	/**
	 * Gets the system attribute with the provided name from the node.
	 * 
	 * @param currentNode
	 *            the node where attributes are.
	 * @param name
	 *            the system attribute's name.
	 * @return the value.
	 */
	private String getSystemAttribute(Node currentNode, String name) {
		NodeList list = currentNode.getChildNodes();
		String value = null;
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (name.equals(node.getNodeName())) {
				value = node.getNodeValue();
				break;
			}
		}
		return value;
	}

	/**
	 * Sets the provided related objects to the Model Object as an implicit
	 * attribute.
	 * 
	 * @param modelObject
	 *            the base Model Object.
	 * @param implicitAttributeName
	 *            the implicit attribute in the base object where the other
	 *            objects will be added.
	 * @param relatedObjects
	 *            the related Model Objects.
	 * @param accumulate
	 *            flag show if the new values will be appended or overwrite.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void setImplicitAttribute(ModelObject modelObject, String implicitAttributeName, List<ModelObject> relatedObjects,
			boolean accumulate) throws Exception {
		if (relatedObjects == null) {
			return;
		}
		if (accumulate) {
			ModelObject[] objects = null;
			try {
				// existing model objects
				objects = (ModelObject[]) PropertyUtils.getProperty(modelObject, implicitAttributeName);
				for (ModelObject object : objects) {
					relatedObjects.add(object);
				}
			} catch (Exception ex) {
				// ignore
			}
		}

		if (!relatedObjects.isEmpty()) {
			Class<?> attributeType = getPropertyType(modelObject, implicitAttributeName);
			try {
				if (attributeType.isArray()) {
					PropertyUtils.setProperty(modelObject, implicitAttributeName, relatedObjects.toArray((ModelObject[]) Array
							.newInstance(attributeType.getComponentType(), 0)));
				} else {
					PropertyUtils.setProperty(modelObject, implicitAttributeName, (relatedObjects.size() > 0) ? relatedObjects
							.get(0) : null);
				}
			} catch (NoSuchMethodException nsme) {
				printDebugMessage("TADDM.CONN.SKIP.READONLY.ATTRIBUTE", implicitAttributeName);
			}
		}
	}

	/**
	 * Prints a debug message, if debug mode for the owning Components is
	 * enabled.
	 * 
	 * @param msgKey
	 *            message key
	 * @param params
	 *            the message;s parameters.
	 */
	private void printDebugMessage(String msgKey, Object... params) {
		log.debug(getMessage(msgKey, params));
	}

	/**
	 * Gets a localized message using the provided key and adding the available
	 * values.
	 * 
	 * @param key
	 *            the message's key.
	 * @param values
	 *            the values to be added to the message.
	 * @return the formatted localized string.
	 */
	private String getMessage(String key, Object... values) {
		return TADDMConnector.L10N.getString(key, values);
	}

	/**
	 * Class representing an implicit attribute (single or multi-valued) of a
	 * Model Object.
	 */
	private static class ModelObjectProperty {

		/**
		 * Map used for storing Guid ModelObject pairs.
		 */
		private Map<String, ModelObject> map = null;

		/**
		 * The name of the property.
		 */
		private String propertyName = null;

		/**
		 * Hold ModelObject if the property is single value.
		 */
		private ModelObject singleValue = null;

		/**
		 * Store ModelObject's properties.
		 * 
		 * @param modelObject
		 *            The ModelObject that will be scanned.
		 * @param name
		 *            The name of the property.
		 */
		ModelObjectProperty(ModelObject modelObject, String name) {
			propertyName = name;
			map = new HashMap<String, ModelObject>();
			try {
				Object obj = PropertyUtils.getSimpleProperty(modelObject, propertyName);
				if (obj instanceof ModelObject) {
					singleValue = (ModelObject) obj;
				} else if (obj instanceof ModelObject[]) {
					singleValue = null;
					ModelObject[] mos = (ModelObject[]) obj;
					for (ModelObject mo : mos) {
						map.put(mo.getGuid().toString(), mo);
					}
				}
			} catch (Exception ex) {
				// ignore
			}
		}

		/**
		 * Check if this property has a single value.
		 * 
		 * @return true if the property has a single value, else false.
		 */
		public boolean isSingleValued() {
			return singleValue != null;
		}

		/**
		 * Return the corresponding ModeltObject for given Guid.
		 * 
		 * @param guid
		 *            of the searching ModelObject
		 * @return The corresponding ModeltObject for given Guid.
		 */
		public ModelObject get(String guid) {
			return map.remove(guid);
		}

		/**
		 * Return the single value ModelObject.
		 * 
		 * @return The ModelObject if it is a single value.
		 */
		public ModelObject get() {
			return singleValue;
		}

		/**
		 * Return the collection of all unused ModelObjects from multi-value
		 * property.
		 * 
		 * @return The collection of all unused ModelObjects from multi-value
		 *         property.
		 */
		public Collection<ModelObject> getUnmodified() {
			return map.values();
		}
	}
}
