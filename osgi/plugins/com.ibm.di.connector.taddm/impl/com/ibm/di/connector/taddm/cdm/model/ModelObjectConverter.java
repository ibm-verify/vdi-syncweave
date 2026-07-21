/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.taddm.cdm.model;

import static com.ibm.di.cdm.core.CDMConstants.CDM_CLASSTYPE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_CYCLE_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.CDM_ID_SYSTEM_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_DOMAIN_ATTRIBUTE;
import static com.ibm.di.cdm.core.CDMConstants.TADDM_MSS_ATTRIBUTE;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.collation.platform.model.AttributeNotSetException;
import com.collation.platform.model.Guid;
import com.collation.platform.model.InterfaceIntrospector;
import com.collation.platform.model.ModelObject;
import com.collation.platform.model.domain.CMDBDomain;
import com.collation.platform.model.topology.core.Relationship;
import com.collation.platform.model.topology.meta.UserDataAttributeMeta;
import com.collation.platform.model.topology.meta.UserDataMeta;
import com.collation.platform.model.topology.process.ManagementSoftwareSystem;
import com.collation.proxy.api.client.ApiException;
import com.collation.proxy.api.client.CMDBApi;
import com.collation.proxy.api.client.CompatibilityApi;
import com.collation.proxy.api.common.AttrNameValue;
import com.ibm.di.cdm.core.CDMUtils;
import com.ibm.di.cdm.core.MetaData;
import com.ibm.di.connector.taddm.TADDMConnector;
import com.ibm.di.connector.taddm.cdm.TADDMMetaData;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;

/**
 * This class is used for creating hierarchical entries matching the provided
 * {@link ModelObject} and for hierarchical schema entries for some class type.
 * 
 */
public class ModelObjectConverter {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The TADDM API.
	 */
	private CMDBApi api;

	/**
	 * The {@link MetaData}.
	 */
	private TADDMMetaData metaData;

	/**
	 * Used to limit hierarchy's depth.
	 */
	private int depth;

	/**
	 * A buffer used when reading data from TADDM.
	 */
	private List<Object> fetchList;

	/**
	 * Cache of CMDBDomain objects.
	 */
	private Map<Guid, CMDBDomain> domainCache;

	/**
	 * Contains the related CIs which will not be returned for each iterated CI,
	 * if IdML Mode is enabled.
	 */
	private Entry skippedModelObjects;

	/**
	 * The Log used for logging messages.
	 */
	private Log log;

	/**
	 * The Guid of the MSS which items we want to read. Value of
	 * <code>null</code> returns all items disregarding their MSS..
	 */
	private Guid mssGuid;

	/**
	 * Comparator used for sorting the read implicit attributes.
	 */
	private Comparator<PropertyDescriptor> comparator;

	/**
	 * Determines if domain attributes should be returned. They are only
	 * available when the query is against an enterprise TADDM server. In this
	 * case they can be used to distinguish the TADDM server that provided the
	 * data from all the other servers in the enterprise architecture.
	 */
	private boolean domainAttributes;

	/**
	 * Determines if the management software system info for this MSS should be
	 * queried.
	 */
	private boolean managementSoftwareSystems;

	/**
	 * Determines if extended attributes should be returned if available.
	 */
	private boolean extendedAttributes;

	/**
	 * Determines if explicit relationships, without corresponding implicit ones
	 * should be present in the returned item.
	 */
	private boolean explicitRelationships;

	/**
	 * Constructor.
	 * 
	 * @param api
	 *            the TADDM API.
	 * @param metaData
	 *            the meta-data.
	 * @param depth
	 *            the iteration depth.
	 * @param fetchSize
	 *            the size of the buffer used when reading from TADDM.
	 * @param mssGuid
	 *            the GUID of the MSS of interest.
	 * @param log
	 *            the Log.
	 */
	public ModelObjectConverter(CMDBApi api, TADDMMetaData metaData, int depth, int fetchSize, Guid mssGuid, Log log) {
		this.api = api;
		this.metaData = metaData;
		this.depth = depth;
		this.log = log;
		this.mssGuid = mssGuid;

		fetchList = createFetchList(fetchSize);
		skippedModelObjects = new Entry();
		domainCache = new HashMap<Guid, CMDBDomain>();
		comparator = new Comparator<PropertyDescriptor>() {
			public int compare(PropertyDescriptor object1, PropertyDescriptor object2) {
				return object1.getName().compareToIgnoreCase(object2.getName());
			}
		};
	}

	/**
	 * Creates the reading buffer.
	 * 
	 * @param fetchSize
	 *            the buffers size.
	 * @return the buffer.
	 */
	private List<Object> createFetchList(int fetchSize) {
		List<Object> fetchList = null;
		if (fetchSize >= 0) {
			fetchList = new ArrayList<Object>(fetchSize);
		} else {
			fetchList = new ArrayList<Object>();
		}
		return fetchList;
	}

	/**
	 * Sets whether domain attributes should be read.
	 * 
	 * @param enable
	 *            if this option is enabled.
	 */
	public void setDomainAttributes(boolean enable) {
		domainAttributes = enable;
	}

	/**
	 * Sets whether MSS attributes should be read.
	 * 
	 * @param enable
	 *            if this option is enabled.
	 */
	public void setManagementSoftwareSystems(boolean enable) {
		managementSoftwareSystems = enable;
	}

	/**
	 * Sets whether extended attributes should be read.
	 * 
	 * @param enable
	 *            if this option is enabled.
	 */
	public void setExtendedAttributes(boolean enable) {
		extendedAttributes = enable;
	}

	/**
	 * Sets whether explicit attributes should be read.
	 * 
	 * @param enable
	 *            if this option is enabled.
	 */
	public void setExplicitRelationships(boolean enable) {
		explicitRelationships = enable;
	}

	/**
	 * Returns the Entry representation of the provided Model Object. If in
	 * "IdML Mode" and no naming rules are matched, an empty entry is returned.
	 * 
	 * @param modelObject
	 *            the source Model Object.
	 * @return the generated hierarchical entry.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Entry convert(ModelObject modelObject) throws Exception {
		Entry entry = new Entry(true);
		skippedModelObjects.removeAllAttributes();

		boolean isAdded = recursiveAddAttributes(modelObject, entry);
		if (isAdded) {
			if (domainAttributes) {
				addDomainAttributes(modelObject, entry);
			}

			if (managementSoftwareSystems) {
				addMSSAttributes(modelObject, entry);
			}
		} else {
			entry.removeAllAttributes();
		}

		return entry;
	}

	/**
	 * Adds the ModelObject's domain attributes to the provided node.
	 * 
	 * @param modelObject
	 *            the ModelObejct.
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void addDomainAttributes(ModelObject modelObject, Node currentNode) throws Exception {
		Guid moGuid = modelObject.getGuid();
		printDebugMessage("TADDM.CONN.GETTING.DOMAIN.ATTRIBUTES", moGuid);

		CMDBDomain domain = null;
		if (modelObject.hasCmdbSource()) {
			Guid sourceGuid = modelObject.getCmdbSource();
			if (domainCache.containsKey(sourceGuid)) {
				domain = domainCache.get(sourceGuid);
			} else {
				try {
					domain = (CMDBDomain) api.find(sourceGuid, 1, null);
				} catch (ApiException ae) {
					printDebugMessage("TADDM.CONN.ERROR.GETTING.DOMAIN.ATTRIBUTES", moGuid, ae.getMessage());
				}
			}
		} else {
			domain = api.getDomain(moGuid);
		}

		if (domain != null && domain.hasGuid()) {
			// cache domain
			domainCache.put(domain.getGuid(), domain);
			currentNode = currentNode.appendChild(new Attribute(TADDM_DOMAIN_ATTRIBUTE));
			Node child = currentNode.appendChild(new Attribute(InterfaceIntrospector.getCollationType(domain)));
			recursiveAddAttributes(domain, child);
		} else {
			printDebugMessage("TADDM.CONN.NO.DOMAIN.ATTRIBUTES", moGuid);
		}
	}

	/**
	 * Adds the ModelObject's MSS attributes to the provided node.
	 * 
	 * @param modelObject
	 *            the ModelObejct.
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void addMSSAttributes(ModelObject modelObject, Node currentNode) throws Exception {
		Guid moGuid = modelObject.getGuid();
		printDebugMessage("TADDM.CONN.GETTING.MSS.ATTRIBUTES", moGuid);
		try {
			ManagementSoftwareSystem[] mssArray = api.getManagementSoftwareSystems(modelObject.getGuid(), null);
			if (mssArray != null && mssArray.length > 0) {
				currentNode = currentNode.appendChild(new Attribute(TADDM_MSS_ATTRIBUTE));
				for (ManagementSoftwareSystem mss : mssArray) {
					String cdmClassName = metaData.getMSSClassName();
					recursiveAddAttributes(mss, currentNode.appendChild(new Attribute(cdmClassName)));
				}
			} else {
				printDebugMessage("TADDM.CONN.NO.MSS.ATTRIBUTES", moGuid);
			}
		} catch (ApiException ae) {
			printDebugMessage("TADDM.CONN.ERROR.GETTING.MSS.ATTRIBUTES", moGuid, ae.getMessage());
		}
	}

	/**
	 * Recursively adds the all attributes of the Model Object to the provided
	 * node.
	 * 
	 * @param modelObject
	 *            the ModelObejct.
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @return whether the addition was successful.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private boolean recursiveAddAttributes(ModelObject modelObject, Node currentNode) throws Exception {
		Guid moGuid = modelObject.getGuid();
		if (isCyclicAttribute(currentNode, moGuid)) {
			// add it as a 'cycle' system attribute
			Node sysAttr = createSystemAttribute(currentNode, CDM_CYCLE_SYSTEM_ATTRIBUTE, moGuid.toString());
			printDebugMessage("TADDM.CONN.CYCLIC.ATTRIBUTE.DETECTED", currentNode.getNodeName(), moGuid, CDMUtils
					.getAttributePath(sysAttr));
			return true;
		}

		// set the cdmClassType system attribute
		String cdmClassType = getCDMClassType(modelObject);
		printDebugMessage("TADDM.CONN.ADDING.ATTRIBUTES.RECURSIVELY", cdmClassType);
		createSystemAttribute(currentNode, CDM_CLASSTYPE_SYSTEM_ATTRIBUTE, cdmClassType);

		// set the GUID system attribute
		createSystemAttribute(currentNode, CDM_ID_SYSTEM_ATTRIBUTE, moGuid.toString());

		// add all CDM attributes (both implicit and explicit)
		Set<String> addedAttributes = new HashSet<String>();

		Map<?, ?> map = getTADDMAttributes(modelObject);
		for (Map.Entry<?, ?> me : map.entrySet()) {
			String key = me.getKey().toString();

			Object value = me.getValue();
			if (value == null || !metaData.isSupportedAttribute(key, cdmClassType)) {
				continue;
			}
			boolean isAttributeAdded = false;
			Attribute attribute = null;
			if (isModelObject(value)) { // implicit attribute
				ModelObject mo = (ModelObject) value;
				attribute = metaData.createImplicitAttributeByName(currentNode, cdmClassType, key);
				if (attribute == null) {
					printDebugMessage("TADDM.CONN.UNKNOWN.IMPLICIT.ATTRIBUTE", key, cdmClassType);
				} else {
					isAttributeAdded = recursiveAddAttributes(mo, attribute);
				}
			} else if (value instanceof ModelObject[]) { // array of attributes
				ModelObject[] internalArray = (ModelObject[]) value;
				for (ModelObject mo : internalArray) {
					attribute = metaData.createImplicitAttributeByName(currentNode, cdmClassType, key);
					if (attribute == null) {
						printDebugMessage("TADDM.CONN.UNKNOWN.IMPLICIT.ATTRIBUTE", key, cdmClassType);
					} else {
						isAttributeAdded |= recursiveAddAttributes(mo, attribute);
					}
				}
			} else {
				Attribute newAttribute = metaData.createExplicitAttribute(currentNode, key);
				newAttribute.setValue(value);
				isAttributeAdded = true;
			}

			if (isAttributeAdded) {
				addedAttributes.add(key);
			}
		}

		boolean isAdded = true;
		// check naming rules
		if (!metaData.matchesNamingRules(cdmClassType, addedAttributes)) {
			isAdded = false;
			printDebugMessage("TADDM.CONN.SKIPPING.ATTRIBUTE", cdmClassType, moGuid);
			removeNode(currentNode);
			Attribute tempAttr = new Attribute(modelObject.getGuid().toString());
			simpleAddAttributes(modelObject, tempAttr);
			skippedModelObjects.setAttribute(tempAttr);
		} else {
			isAdded = true;

			// add extended attributes
			if (extendedAttributes) {
				addExtendedAttributes(modelObject, currentNode);
			}

			// add CIs connected to the current CI with explicit relationships
			if (explicitRelationships) {
				addExplicitRelationships(modelObject, currentNode);
			}
		}
		return isAdded;
	}

	/**
	 * This method returns all attributes of the provided TADDM Model Object. To
	 * do this we first try using the method recommended by TADDM 7.2 and, if
	 * missing, we switch to the older version (the older method is deprecated
	 * in TADDM 7.2 and can be dropped in future versions).
	 * 
	 * @param modelObject
	 *            the object which attributes will be listed.
	 * @return a map with the attributes of the model object.
	 * @throws Exception
	 *             if the required Java methods are not supported by the used
	 *             TADDM SDK.
	 */
	private Map<?, ?> getTADDMAttributes(ModelObject modelObject) throws Exception {
		final String METHOD_NAME = "getAllAttributes";
		Method method = null;
		Map<?, ?> map = null;
		try {
			method = modelObject.getClass().getMethod(METHOD_NAME, new Class[] { Collection.class });
			map = (Map<?, ?>) method.invoke(modelObject, new Object[] { fetchList });
		} catch (NoSuchMethodException nsme) {
			method = modelObject.getClass().getMethod(METHOD_NAME, new Class[] {});
			map = (Map<?, ?>) method.invoke(modelObject, new Object[] {});
		}
		if (map == null) {
			map = new HashMap<Object, Object>();
		}
		return map;
	}

	/**
	 * Gets the CDM class type of the provided Model Object.
	 * 
	 * @param modelObject
	 *            the Model Object
	 * @return the CDM class type.
	 */
	private String getCDMClassType(ModelObject modelObject) {
		String taddmClassType = metaData.getModelObjectClass(modelObject);
		return metaData.getCDMClassType(taddmClassType);
	}

	/**
	 * Adds the ModelObject's explicit attributes to the provided node. This
	 * method will not follow and add its implicit attributes.
	 * 
	 * @param modelObject
	 *            the Model Object
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void simpleAddAttributes(ModelObject modelObject, Node currentNode) throws Exception {
		Map<?, ?> map = getTADDMAttributes(modelObject);
		for (Map.Entry<?, ?> me : map.entrySet()) {
			String key = me.getKey().toString();
			Object value = me.getValue();
			if (value == null || !metaData.isSupportedAttribute(key, getCDMClassType(modelObject))) {
				continue;
			}

			if (!isModelObject(value) && !(value instanceof ModelObject[])) {
				Attribute newAttribute = metaData.createExplicitAttribute(currentNode, key);
				newAttribute.setValue(value);
			}
		}
	}

	/**
	 * Adds the ModelObject's extended attributes to the provided node.
	 * 
	 * @param modelObject
	 *            the ModelObejct.
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @throws AttributeNotSetException
	 *             if a problem occurs.
	 */
	private void addExtendedAttributes(ModelObject modelObject, Node currentNode) throws AttributeNotSetException {
		Guid guid = modelObject.getGuid();
		printDebugMessage("TADDM.CONN.GETTING.EXTENDED.ATTRIBUTES", guid);
		try {
			AttrNameValue[] extAttrs = api.getExtendedAttributes(guid);
			if (extAttrs != null && extAttrs.length > 0) {
				for (AttrNameValue extAttr : extAttrs) {
					String name = metaData.getExtendedAttributeName(extAttr.name);
					currentNode.appendChild(new Attribute(name, extAttr.value));
				}
			} else {
				printDebugMessage("TADDM.CONN.NO.EXTENDED.ATTRIBUTES", guid);
			}
		} catch (ApiException ae) {
			printDebugMessage("TADDM.CONN.ERROR.GETTING.EXTENDED.ATTRIBUTES", guid, ae.getMessage());
		}
	}

	/**
	 * Adds the ModelObject's explicit relationships to the provided node.
	 * 
	 * @param modelObject
	 *            the ModelObejct.
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void addExplicitRelationships(ModelObject modelObject, Node currentNode) throws Exception {
		int currentDepth = getAttributeDepth(currentNode);
		if (hasReachedDepthLimit(currentDepth)) {
			return;
		}

		String className = metaData.getModelObjectClass(modelObject);
		Guid moGuid = modelObject.getGuid();
		try {
			printDebugMessage("TADDM.CONN.GETTING.EXPLICITRELN.ATTRIBUTES", moGuid);
			// forward relationships
			Relationship[] relns = api.findRelationships(moGuid,// 
					CompatibilityApi.CDB_DIRECTION_FORWARD, null /* all types */, 1, null);
			printDebugMessage("TADDM.CONN.FOUND.FORWARD.EXPLICIT.RELNS", relns.length, moGuid);
			addExplicitRelationships(className, relns, true, currentNode, currentDepth);

			// backward relationships
			relns = api.findRelationships(moGuid,// 
					CompatibilityApi.CDB_DIRECTION_BACKWARD, null /* all types */, 1, null);
			printDebugMessage("TADDM.CONN.FOUND.BACKWARD.EXPLICIT.RELNS", relns.length, moGuid);
			addExplicitRelationships(className, relns, false, currentNode, currentDepth);
		} catch (ApiException ae) {
			printDebugMessage("TADDM.CONN.ERROR.GETTING.EXPLICIT.RELNS", moGuid, ae.getMessage());
		}
	}

	/**
	 * Adds the ModelObject's explicit relationships to the provided node.
	 * 
	 * @param classType
	 *            the owning class type.
	 * @param relationships
	 *            the relationships that will be added.
	 * @param areForward
	 *            if the relationships are forward (from this item to another
	 *            one).
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @param currentDepth
	 *            the current depth in the hierarchy.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void addExplicitRelationships(String classType, Relationship[] relationships, boolean areForward, Node currentNode,
			int currentDepth) throws Exception {
		currentDepth++;
		for (Relationship reln : relationships) {
			if (!reln.hasType() || !reln.hasTarget() || !reln.hasSource()) {
				continue;
			}
			ModelObject modelObject = null;
			if (areForward) {
				modelObject = reln.getTarget();
			} else {
				modelObject = reln.getSource();
			}
			if (!modelObject.hasGuid()) {
				continue;
			}
			Guid guid = modelObject.getGuid();
			try {
				// TODO: TADDM says it cannot find some related CI. Is this
				// their defect/limitation or an error in our setup.
				ModelObject mo = api.find(guid, getRemainingDepth(currentDepth), mssGuid, null);
				String relatedClass = metaData.getModelObjectClass(mo);
				Node implicitAttribute = metaData.createImplicitAttribute(currentNode, classType, reln.getType(), relatedClass,
						areForward, guid.toString());
				if (implicitAttribute != null) {
					printDebugMessage("TADDM.CONN.EXPLICIT.RELN.ADDED.SUCCESSFULLY", getCDMClassType(reln), reln.getGuid(),
							getCDMClassType(mo));
					recursiveAddAttributes(mo, implicitAttribute);
				} else {
					printDebugMessage("TADDM.CONN.EXPLICIT.RELN.ALREADY.ADDED", getCDMClassType(reln), reln.getGuid(),
							getCDMClassType(mo));
				}
			} catch (ApiException ae) {
				printDebugMessage("TADDM.CONN.ERROR.GETTING.RELATED.ITEM", modelObject.getGuid(), ae.getMessage());
			}
		}
	}

	/**
	 * Sets the name-value pair as an attribute of the provided node.
	 * 
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @param name
	 *            the system attribute's name.
	 * @param value
	 *            the system attribute's value.
	 * @return the created system node.
	 */
	private Node createSystemAttribute(Node currentNode, String name, String value) {
		return currentNode.appendChild(new Attribute(name, value));
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
	 * Checks if the provided attribute is cyclic. If it can be found higher in
	 * the hierarchy path.
	 * 
	 * @param currentNode
	 *            the node to be checked.
	 * @param guid
	 *            the Guid of the item to be checked.
	 * @return <code>true</code> if cyclic, otherwise <code>false</code>.
	 */
	private boolean isCyclicAttribute(Node currentNode, Guid guid) {
		String currentGuidString = getSystemAttribute(currentNode, CDM_ID_SYSTEM_ATTRIBUTE);
		boolean isCyclic = guid.toString().equals(currentGuidString);
		if (!isCyclic) {
			Node parent = getParentNode(currentNode);
			if (parent != null) {
				isCyclic = isCyclicAttribute(parent, guid);
			}
		}
		return isCyclic;
	}

	/**
	 * Returns the depth in the hierarchy of the provided node.
	 * 
	 * @param node
	 *            the node to be checked.
	 * @return the depth.
	 */
	private int getAttributeDepth(Node node) {
		Node parent = getParentNode(node);
		if (parent == null) {
			return 0;
		}
		String name = parent.getNodeName();
		// In the case of relationships we do not consider $source and $target
		// implicit attributes. This method will return false, because their
		// parent is an Entry object, and its name is '#document' (which does
		// not match the conditions for implicit attribute).
		if (metaData.isImplicitAttribute(name)) {
			parent = getParentNode(parent);
		}
		return 1 + getAttributeDepth(parent);
	}

	/**
	 * Gets the parent of the provided node. If it is an Attribute directly in
	 * the TDI Entry, the Entry will be returned (instead of null).
	 * 
	 * @param node
	 *            the node to be checked.
	 * @return the parent node, or <code>null</code> if none is found.
	 */
	private Node getParentNode(Node node) {
		if (node == null) {
			return null;
		}

		Node parent = node.getParentNode();
		if (parent == null && node instanceof Attribute) {
			parent = node.getOwnerDocument();
		}
		return parent;
	}

	/**
	 * Removes a node from the hierarchy. If its parent node is empty it is also
	 * removed.
	 * 
	 * @param node
	 *            the node to be removed.
	 */
	private void removeNode(Node node) {
		Node parent = getParentNode(node);
		if (parent != null) {
			parent.removeChild(node);

			// check if parent is not empty
			if (parent.getChildNodes().getLength() == 0) {
				removeNode(parent);
			}
		}
	}

	/**
	 * Returns the remaining depth before reaching the configured limit. Takes
	 * into account unlimited depth.
	 * 
	 * @param currentDepth
	 *            the current depth.
	 * @return the remaining depth or -1 (infinity).
	 */
	private int getRemainingDepth(int currentDepth) {
		int remaining = 0;
		if (depth == CMDBApi.DEPTH_INFINITE) {
			remaining = depth;
		} else {
			remaining = depth - currentDepth;
			remaining = remaining < 0 ? 0 : remaining;
		}
		return remaining;
	}

	/**
	 * Checks if the depth limit has been reached. Takes into account unlimited
	 * depth.
	 * 
	 * @param currentDepth
	 *            the current depth.
	 * @return whether the limit is reached.
	 */
	private boolean hasReachedDepthLimit(int currentDepth) {
		if (depth == CMDBApi.DEPTH_INFINITE) {
			return false;
		} else {
			return currentDepth > depth;
		}
	}

	/**
	 * Checks if the provided object is a Model Object.
	 * 
	 * @param attributeValue
	 *            object to be checked.
	 * @return whether this is a Model Object.
	 */
	private boolean isModelObject(Object attributeValue) {
		return attributeValue instanceof ModelObject;
	}

	/**
	 * Returns a list of the Model Objects which were skipped when building the
	 * last entry. Objects are skipped only if IdML mode is enabled.
	 * 
	 * @return an Entry containing the GUID of each skipped item and a one-level
	 *         list of its explicit attributes.
	 */
	public Entry getSkippedModelObjects() {
		return skippedModelObjects;
	}

	/**
	 * Builds a hierarchical schema Entry to be used then the schema of the
	 * TADDM Connector is queried.
	 * 
	 * @param classType
	 *            the type of item which is queried.
	 * @return the schema entry.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public Entry convertClassType(String classType) throws Exception {
		Entry entry = new Entry(true);

		classType = CDMUtils.removePrefix(classType);
		classType = CDMUtils.removeEscapeChars(classType);
		String taddmClassName = metaData.getTADDMClassType(classType);
		recursiveAddSchemaAttributes(Class.forName(taddmClassName), entry);

		if (domainAttributes) {
			Node domainNodeBase = entry.appendChild(new Attribute(TADDM_DOMAIN_ATTRIBUTE));
			Node domainNode = domainNodeBase.appendChild(new Attribute("domain." + CMDBDomain.class.getSimpleName()));
			recursiveAddSchemaAttributes(CMDBDomain.class, domainNode);
		}

		if (managementSoftwareSystems) {
			Node mssNodeBase = entry.appendChild(new Attribute(TADDM_MSS_ATTRIBUTE));
			Node mssNode = mssNodeBase.appendChild(new Attribute(metaData.getMSSClassName()));
			recursiveAddSchemaAttributes(ManagementSoftwareSystem.class, mssNode);
		}
		return entry;
	}

	/**
	 * Recursively adds attributes to the schema Entry. Implicit attributes are
	 * followed.
	 * 
	 * @param clazz
	 *            the class which attributes are added.
	 * @param currentNode
	 *            the node where they will be added.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void recursiveAddSchemaAttributes(Class<?> clazz, Node currentNode) throws Exception {
		createSystemAttribute(currentNode, CDM_CLASSTYPE_SYSTEM_ATTRIBUTE, String.class.getCanonicalName());
		createSystemAttribute(currentNode, CDM_ID_SYSTEM_ATTRIBUTE, Object.class.getCanonicalName());
		createSystemAttribute(currentNode, CDM_CYCLE_SYSTEM_ATTRIBUTE, Object.class.getCanonicalName());

		if (hasReachedDepthLimit(getAttributeDepth(currentNode) + 1)) {
			return;
		}

		PropertyDescriptor[] descriptors = InterfaceIntrospector.getPropertyDescriptors(clazz);
		Arrays.sort(descriptors, comparator);
		for (PropertyDescriptor descriptor : descriptors) {
			String attributeName = descriptor.getName();

			if (!metaData.isSupportedAttribute(attributeName, clazz.getCanonicalName())) {
				continue;
			}
			Class<?> attributeType = descriptor.getPropertyType();
			if (isModelObject(attributeType) || attributeType.isArray()) {
				if (attributeType.isArray()) {
					// TODO: this can be enhanced so that the schema notifies
					// users which implicit attributes are arrays and which are
					// simple types
					attributeType = attributeType.getComponentType();
				}
				try {
					Attribute child = metaData.createImplicitAttributeByName(currentNode, clazz.getCanonicalName(), attributeName);
					if (child == null) {
						printDebugMessage("TADDM.CONN.UNKNOWN.IMPLICIT.ATTRIBUTE", attributeName, clazz.getCanonicalName());
					} else {
						recursiveAddSchemaAttributes(attributeType, child);
					}
				} catch (Exception ex) {
					// ignore, this attribute is present in TADDM classes, but
					// is not returned by its MetaData API
				}
			} else {
				// TODO: we can enhance it to recognize enums and
				// display only their supported values.
				Attribute child = metaData.createExplicitAttribute(currentNode, attributeName);
				child.setNodeValue(attributeType.getCanonicalName());
			}
		}

		if (extendedAttributes) {
			addExtendedSchemaAttributes(clazz, currentNode);
		}
	}

	/**
	 * Adds the extended attributes of the provided class to the node.
	 * 
	 * @param clazz
	 *            class to be checked.
	 * @param currentNode
	 *            the node where attributes will be added.
	 * @throws AttributeNotSetException
	 *             if a problem occurs.
	 */
	private void addExtendedSchemaAttributes(Class<?> clazz, Node currentNode) throws AttributeNotSetException {
		try {
			UserDataMeta[] dataMeta = api.getExtendedAttributeMeta(clazz.getCanonicalName());
			if (dataMeta == null) {
				return;
			}

			for (UserDataMeta udm : dataMeta) {
				if (udm.hasAttrMeta()) {
					UserDataAttributeMeta[] attributeMeta = udm.getAttrMeta();
					for (UserDataAttributeMeta meta : attributeMeta) {
						if (!meta.hasAttrName() && !meta.hasAttrType()) {
							continue;
						}
						String name = metaData.getExtendedAttributeName(meta.getAttrName());
						currentNode.appendChild(new Attribute(name, meta.getAttrType()));
					}
				}
			}
		} catch (ApiException ae) {
			printDebugMessage("TADDM.CONN.ERROR.GETTING.ATTRIBUTE.METADATA", clazz, ae.getMessage());
		}
	}

	/**
	 * Checks if the provided class is of a Model Object.
	 * 
	 * @param clazz
	 *            the class to be checked.
	 * @return whether this is a Model Object class.
	 */
	private boolean isModelObject(Class<?> clazz) {
		return ModelObject.class.isAssignableFrom(clazz);
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
}
