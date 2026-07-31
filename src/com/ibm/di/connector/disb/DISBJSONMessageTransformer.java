/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.disb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.ibm.di.server.ResourceHash;
import com.ibm.di.connector.disb.model.BaseOperation;
import com.ibm.di.connector.disb.model.ConfigurationItem;
import com.ibm.di.connector.disb.model.Create;
import com.ibm.di.connector.disb.model.Delete;
import com.ibm.di.connector.disb.model.Modify;
import com.ibm.di.connector.disb.model.OperationSet;
import com.ibm.di.connector.disb.model.Reference;
import com.ibm.di.connector.disb.model.Refresh;
import com.ibm.di.connector.disb.model.Relationship;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.json.java.OrderedJSONObject;

/**
 * This class is used to parse and transform the JSON Messages.
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1.1
 */
public class DISBJSONMessageTransformer {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Resource Hash used to access DISB messages.
	 */
	private static ResourceHash resHash = com.ibm.di.connector.disb.DISBConnector.getResHash();

	/**
	 * Method returns the whole JSONObject after parsing the InputStream
	 * containing the JSON data.
	 * 
	 * @param data
	 *            The InputStream containing the JSON Message
	 * @return The converted JSONObject from the JSON data stream
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public JSONObject parseToJSONObject(InputStream data) throws Exception {
		try {
			JSONObject root = OrderedJSONObject.parse(data);
			return root;
		} catch (IOException JPE) {
			throw new Exception(resHash.getString("UNABLE.TO.PARSE.THE.JSON.MESSAGE"), JPE);
		}
	}

	/**
	 * Method returns the Instance or Model Topic's OperationSet object after
	 * parsing the JSON message.
	 * 
	 * @param jsonMessage
	 *            The JSON message.
	 * @return an OperationSet object containing the Instance or Model Topic's
	 *         Operation Set.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@SuppressWarnings("unchecked")
	public OperationSet getOperationSet(String jsonMessage) throws Exception {
		// Parse the raw JSON message
		OrderedJSONObject obj = (OrderedJSONObject) parseToJSONObject(new ByteArrayInputStream(jsonMessage.getBytes()));

		OperationSet opSet = new OperationSet();

		OrderedJSONObject opSetObject = (OrderedJSONObject) obj.get(DISBConstants.JSONMSG_ATTR_OPSET);
		Iterator<String> iterator = opSetObject.getOrder();

		while (iterator.hasNext()) {
			String key = iterator.next();
			if (key.equals(DISBConstants.JSONMSG_ATTR_OPSETID)) {
				String opid = (String) opSetObject.get(key);
				if (opid != null) {
					opSet.setOpId(opid);
				} else {
					throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS", DISBConstants.JSONMSG_ATTR_OPSETID));
				}
			} else {
				if (key.equals(DISBConstants.JSONMSG_ATTR_CREATE)) {
					OrderedJSONObject createJsonObj = (OrderedJSONObject) opSetObject.get(key);
					if (createJsonObj == null) {
						throw new Exception(
								resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS", DISBConstants.JSONMSG_ATTR_CREATE));
					}
					Create createObj = new Create();
					convertJsonToOperation(createJsonObj, createObj);
					if (createObj != null) {
						opSet.setCreate(createObj);
					}
				} else {
					if (key.equals(DISBConstants.JSONMSG_ATTR_MODIFY)) {
						OrderedJSONObject modifyJsonObj = (OrderedJSONObject) opSetObject.get(key);
						if (modifyJsonObj == null) {
							throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS",
									DISBConstants.JSONMSG_ATTR_MODIFY));
						}
						Modify modifyObj = new Modify();
						convertJsonToOperation(modifyJsonObj, modifyObj);
						if (modifyObj != null) {
							opSet.setModify(modifyObj);
						}
					} else {
						if (key.equals(DISBConstants.JSONMSG_ATTR_DELETE)) {
							OrderedJSONObject deleteJsonObj = (OrderedJSONObject) opSetObject.get(key);
							if (deleteJsonObj == null) {
								throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS",
										DISBConstants.JSONMSG_ATTR_DELETE));
							}
							Delete deleteObj = new Delete();
							convertJsonToOperation(deleteJsonObj, deleteObj);
							if (deleteObj != null) {
								opSet.setDelete(deleteObj);
							}
						} else {
							if (key.equals(DISBConstants.JSONMSG_ATTR_REFRESH)) {
								OrderedJSONObject refreshJsonObj = (OrderedJSONObject) opSetObject.get(key);
								if (refreshJsonObj == null) {
									throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS",
											DISBConstants.JSONMSG_ATTR_REFRESH));
								}
								Refresh refreshObj = new Refresh();
								String timeStamp = (String) refreshJsonObj.get(DISBConstants.JSONMSG_ATTR_TIMESTAMP);
								if (timeStamp != null) {
									refreshObj.setTimeStamp(timeStamp);
								}
								OrderedJSONObject createJsonObj = (OrderedJSONObject) refreshJsonObj
										.get(DISBConstants.JSONMSG_ATTR_CREATE);
								if (createJsonObj == null) {
									throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS",
											DISBConstants.JSONMSG_ATTR_CREATE));
								}
								Create createObj = new Create();
								convertJsonToOperation(createJsonObj, createObj);
								if (createObj != null) {
									refreshObj.setCreate(createObj);
								}
								opSet.setRefresh(refreshObj);
							} else {
								if (key.equals(DISBConstants.JSONMSG_ATTR_REFERENCE)) {
									OrderedJSONObject referenceJsonObj = (OrderedJSONObject) opSetObject.get(key);
									if (referenceJsonObj == null) {
										throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS",
												DISBConstants.JSONMSG_ATTR_REFERENCE));
									}
									Reference referenceObj = new Reference();
									convertJsonToOperation(referenceJsonObj, referenceObj);
									if (referenceObj != null) {
										opSet.setReference(referenceObj);
									}
								}
							}
						}
					}
				}
			}
		}
		return opSet;
	}

	/**
	 * Method returns the GUID Life Cycle Topic's ConfigurationItem object after
	 * parsing the JSON message.
	 * 
	 * @param jsonMessage
	 *            The JSON Message.
	 * @return ConfigurationItem object containing the GUIDLifeCycle Topic's
	 *         ConfigurationItem object.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@SuppressWarnings("unchecked")
	public ConfigurationItem getGuidConfigItem(String jsonMessage) throws Exception {

		// Parse the raw JSON message
		OrderedJSONObject obj = (OrderedJSONObject) parseToJSONObject(new ByteArrayInputStream(jsonMessage.getBytes()));

		// Will reuse the DIS model objects
		Iterator<String> iter = obj.getOrder();
		while (iter.hasNext()) {
			String key = iter.next();
			if (obj.get(key) instanceof OrderedJSONObject) {
				ConfigurationItem ci = new ConfigurationItem();
				ci.setClassNameType(key);
				// Using LinkedHashMap to preserve order of insertions while
				// iterating
				LinkedHashMap<String, Object> ciAttr = new LinkedHashMap<String, Object>();
				buildItemAttributes((OrderedJSONObject) obj.get(key), ciAttr);
				ci.setAttributes(ciAttr);
				return ci;
			}

		}
		return new ConfigurationItem();
	}

	/**
	 * Method to parse the Instance or Model Topic's JSON Operation contents.
	 * 
	 * @param operJsonObj
	 *            The OrderedJSONObject containing the ConfigurationItem objects
	 *            either in the form of OrderedJSONObject or in the form of
	 *            JSONArray.
	 * @param operModelObj
	 *            The BaseOperation object containing either the create,
	 *            modify,delete,refresh or reference objects.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@SuppressWarnings("unchecked")
	protected void convertJsonToOperation(OrderedJSONObject operJsonObj, BaseOperation operModelObj) throws Exception {
		// Reusing ConfigurationItem class to represent a ModelItem
		ArrayList<ConfigurationItem> ciList = new ArrayList<ConfigurationItem>();
		ArrayList<Relationship> relList = new ArrayList<Relationship>();

		Iterator<String> iterator = operJsonObj.getOrder();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (key.equals(DISBConstants.JSONMSG_ATTR_TIMESTAMP)) {
				String timeStamp = (String) operJsonObj.get(key);
				if (timeStamp != null) {
					operModelObj.setTimeStamp(timeStamp);
				}

			} else {
				if (operJsonObj.get(key) instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) operJsonObj.get(key);

					for (Object json : jsonArray) {
						ConfigurationItem ci = new ConfigurationItem();
						ci.setClassNameType(key);

						// Using LinkedHashMap to preserve order of insertions
						// while iterating
						LinkedHashMap<String, Object> ciAttr = new LinkedHashMap<String, Object>();
						buildItemAttributes((OrderedJSONObject) json, ciAttr);

						ci.setAttributes(ciAttr);
						ciList.add(ci);
					}
				} else {
					if (operJsonObj.get(key) instanceof OrderedJSONObject) {
						if (key.equals(DISBConstants.JSONMSG_ATTR_RELATIONSHIP)) {
							relList = convertJsonToRelationShipArray((OrderedJSONObject) operJsonObj.get(key));
						} else if (key.equals(DISBConstants.JSONMSG_ATTR_MODELOBJECT)) {
							OrderedJSONObject modelObjectJsonObj = (OrderedJSONObject) operJsonObj.get(key);
							Iterator<String> modelObjectIter = modelObjectJsonObj.getOrder();
							while (modelObjectIter.hasNext()) {

								String modelObjectKey = modelObjectIter.next();
								JSONArray jsonMoArray = (JSONArray) modelObjectJsonObj.get(modelObjectKey);

								for (Object jsonMo : jsonMoArray) {
									ConfigurationItem ci = new ConfigurationItem();
									ci.setClassNameType(modelObjectKey);

									// Using LinkedHashMap to preserve order of
									// insertions
									// while iterating
									LinkedHashMap<String, Object> ciAttr = new LinkedHashMap<String, Object>();
									buildItemAttributes((OrderedJSONObject) jsonMo, ciAttr);

									ci.setAttributes(ciAttr);
									ciList.add(ci);
								}
							}
						} else {
							ConfigurationItem ci = new ConfigurationItem();
							ci.setClassNameType(key);
							// Using LinkedHashMap to preserve order of
							// insertions
							// while iterating
							LinkedHashMap<String, Object> ciAttr = new LinkedHashMap<String, Object>();
							buildItemAttributes((OrderedJSONObject) operJsonObj.get(key), ciAttr);

							ci.setAttributes(ciAttr);
							ciList.add(ci);
						}
					}

				}

			}
		}
		operModelObj.setConfigurationItems(ciList.toArray(new ConfigurationItem[ciList.size()]));
		operModelObj.setRelationships(relList.toArray(new Relationship[relList.size()]));
	}

	/**
	 * Method to parse the CI attributes by making recursive calls to itself
	 * based on the OrderedJSONObject objects obtained. The contents may be the
	 * String,OrderedJSONObject or JSONArray objects.
	 * 
	 * @param jsonItem
	 *            The OrderedJSONObject
	 * @param ciAttr
	 *            an empty LinkedHashMap that needs to be populated.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@SuppressWarnings("unchecked")
	protected void buildItemAttributes(OrderedJSONObject jsonItem, HashMap<String, Object> ciAttr) throws Exception {
		Iterator<String> jsonItemIter = jsonItem.getOrder();
		while (jsonItemIter.hasNext()) {

			String itemAttrKey = jsonItemIter.next();

			// Attribute value can be a String, Object or Array
			if (jsonItem.get(itemAttrKey) instanceof String) {
				String miAttrValue = (String) jsonItem.get(itemAttrKey);
				ciAttr.put(itemAttrKey, miAttrValue);
			} else {
				if (jsonItem.get(itemAttrKey) instanceof OrderedJSONObject) {
					// Using LinkedHashMap to preserve order of insertions while
					// iterating
					LinkedHashMap<String, Object> hashMapAttrValue = new LinkedHashMap<String, Object>();
					// Recursive Call
					buildItemAttributes((OrderedJSONObject) jsonItem.get(itemAttrKey), hashMapAttrValue);
					ciAttr.put(itemAttrKey, hashMapAttrValue);
				} else {
					if (jsonItem.get(itemAttrKey) instanceof JSONArray) {
						JSONArray thisAttrArray = (JSONArray) jsonItem.get(itemAttrKey);

						int i = 1;
						for (Object thisJSONArrayElement : thisAttrArray) {
							// Array element can be an object or string
							if (thisJSONArrayElement instanceof OrderedJSONObject) {
								// Using LinkedHashMap to preserve order of
								// insertions while iterating
								LinkedHashMap<String, Object> hashMapAttrValue = new LinkedHashMap<String, Object>();
								// Recursive Call
								buildItemAttributes((OrderedJSONObject) thisJSONArrayElement, hashMapAttrValue);
								ciAttr.put(itemAttrKey + "[" + (i++) + "]", hashMapAttrValue);
							} else if (thisJSONArrayElement instanceof String) {
								ciAttr.put(itemAttrKey + "[" + (i++) + "]", (String) thisJSONArrayElement);
							}

						}
					}
				}
			}
		}
	}

	/**
	 * Method that converts a JSON object to {@link ArrayList} of 'Relationship'
	 * java model class.
	 * 
	 * @param jsonRelationshipObj
	 *            OrderedJSONObject that needs to be converted to an
	 *            {@link ArrayList} of Relationship.
	 * @return ArrayList An ArrayList of 'Relationship' Objects.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<Relationship> convertJsonToRelationShipArray(OrderedJSONObject jsonRelationshipObj) throws Exception {
		ArrayList<Relationship> managedElements = new ArrayList<Relationship>();

		Iterator<String> iterator = jsonRelationshipObj.getOrder();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();

			JSONArray jsonArrrayObj = (JSONArray) jsonRelationshipObj.get(key);
			if (jsonArrrayObj != null) {
				for (Object obj : jsonArrrayObj) {
					Relationship relationship = createRelationshipFromOrderedJSONObject(key, (OrderedJSONObject) obj);
					if (relationship.getSource() != null && relationship.getTarget() != null) {
						managedElements.add(relationship);
					} else {
						throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS",
								DISBConstants.JSONMSG_ATTR_RELATIONSHIP));
					}
				}
			}
		}
		return managedElements;
	}

	/**
	 * Method that will convert a JSON Object to 'Relationship' java model
	 * class.
	 * 
	 * @param key
	 *            The type of the relation. For eg.contains, runsOn,
	 *            installedOn.
	 * @param obj
	 *            OrderedJSONObject that needs to be converted to the class
	 *            'Relationship'.
	 * @return Relationship The java model class created from the
	 *         OrderedJSONObject.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@SuppressWarnings("unchecked")
	private Relationship createRelationshipFromOrderedJSONObject(String key, OrderedJSONObject obj) throws Exception {
		Relationship relationship = new Relationship();
		relationship.setRelationShipType(key);
		Iterator<String> it = obj.getOrder();
		while (it.hasNext()) {
			String objKey = it.next();
			if (objKey.equals(DISBConstants.JSONMSG_ATTR_SOURCE)) {
				relationship.setSource((String) obj.get(objKey));
			} else if (objKey.equals(DISBConstants.JSONMSG_ATTR_TARGET)) {
				relationship.setTarget((String) obj.get(objKey));
			}
		}
		return relationship;
	}

}
