/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.disb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ibm.di.server.ResourceHash;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.OrderedJSONObject;
import com.ibm.json.xml.XMLToJSONTransformer;
import com.ibm.di.cdm.jar.JarMetaData;

/**
 * This class is used to transform the IDML to JSON Messages.
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1.1
 */
public class DISBIDMLMessageTransformer {
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
	 * Holds the IDML element from the IDML file.
	 */
	private static final String IDML_ELEMENT_IDML = "idml";

	/**
	 * Holds the source element from the IDML file.
	 */
	private static final String IDML_ELEMENT_SOURCE = "source";

	/**
	 * Holds the CDM-ER-Specification element from the IDML file.
	 */
	private static final String IDML_ELEMENT_CDMER = "CDM-ER-Specification";

	/**
	 * Holds the DISSourceMSSName property from the JMS properties.
	 */
	private static final String DIS_MSG_ATTR_MSSNAME = "DISSourceMSSName";

	/**
	 * Holds the DISSourceMSSGuid property from the JMS properties.
	 */
	private static final String DIS_MSG_ATTR_GUID = "DISSourceMSSGuid";

	/**
	 * Holds the DISMessageVersion property from the JMS properties.
	 */
	private static final String DIS_MSG_ATTR_VERSION = "DISMessageVersion";

	/**
	 * Holds the DISSourceMSSName property from the parsed JMS properties.
	 */
	private static final String DIS_MSG_ATTR_MSSNAME_VAL = "ibm-cdm:///CDMMSS/";

	/**
	 * Holds the DISMessageVersion property from the parsed JMS properties.
	 */
	private static final String DIS_MSG_ATTR_VERSION_VAL = "1.0";

	/**
	 * Holds the MSSName element from the IDML file.
	 */
	private static final String IDML_CDM_ATTR_MSSNAME = "MSSName";

	/**
	 * Holds the Host name element from the IDML file.
	 */
	private static final String IDML_CDM_ATTR_HOSTNAME = "Hostname";

	/**
	 * Holds the ManufacturerName element from the IDML file.
	 */
	private static final String IDML_CDM_ATTR_MANUFACTNAME = "ManufacturerName";

	/**
	 * Holds the ProductName element from the IDML file.
	 */
	private static final String IDML_CDM_ATTR_PRODNAME = "ProductName";

	/**
	 * Holds the Guid element from the IDML file.
	 */
	private static final String IDML_CDM_ATTR_GUID = "Guid";

	/**
	 * Holds the process.ManagementSoftwareSystem element from the IDML file.
	 */
	private static final String IDML_CDM_ATTR_MSS = "process.ManagementSoftwareSystem";

	/**
	 * The following constants are to form the Managed Software system element
	 * from the IDML file.
	 */
	private static final String STR_PLUS = "+";
	private static final String STR_EQUALS = "=";
	private static final String CDM_PREFIX = "cdm:";

	/**
	 * Holds the transformed JSON message.
	 */
	private String jSONMessage = null;

	/**
	 * Holds the transformed message properties.
	 */
	private Map<String, Object> msgProperties = null;

	/**
	 * Method transforms the IDML input stream to JSON data.
	 * 
	 * @param data
	 *            The InputStream containing the IDML Message.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void transformIDML(InputStream idmlInpStrm) throws Exception {
		ByteArrayOutputStream jJSONoutStream = new ByteArrayOutputStream();
		try {
			XMLToJSONTransformer.transform(idmlInpStrm, jJSONoutStream);
			OrderedJSONObject jsa = (OrderedJSONObject) OrderedJSONObject.parse(new ByteArrayInputStream(jJSONoutStream
					.toByteArray()));
			OrderedJSONObject idmlObject = (OrderedJSONObject) jsa.get(IDML_ELEMENT_IDML);
			OrderedJSONObject opSetObject = (OrderedJSONObject) idmlObject.get(DISBConstants.JSONMSG_ATTR_OPSET);
			OrderedJSONObject sourceObject = (OrderedJSONObject) idmlObject.get(IDML_ELEMENT_SOURCE);
			jSONMessage = parseJSONMessage(opSetObject);
			msgProperties = parseProperties(sourceObject);
		} catch (Exception e) {
			throw new Exception(resHash.getString("UNABLE.TO.PARSE.THE.JSON.MESSAGE"), e);
		} finally {
			if (null != idmlInpStrm) {
				idmlInpStrm.close();
			}
			if (null != jJSONoutStream) {
				jJSONoutStream.close();
			}
		}
	}

	/**
	 * Method returns a map containing the JMS properties as required by the DIS
	 * Subscription Queue.
	 * 
	 * @param sourceObject
	 *            The OrderedJSON object containing the IDML Message Properties.
	 * @return a Map object containing the parsed JMS properties as required by
	 *         the DIS Subscription Queue.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	protected Map<String, Object> parseProperties(OrderedJSONObject sourceObject) throws Exception {
		Map<String, Object> propMap = new HashMap<String, Object>();
		String dDISSourceMSSName = DIS_MSG_ATTR_MSSNAME_VAL;
		OrderedJSONObject mss = (OrderedJSONObject) sourceObject.get(IDML_CDM_ATTR_MSS);
		if (mss.containsKey(IDML_CDM_ATTR_MSSNAME)) {
			String mssName = (String) mss.get(IDML_CDM_ATTR_MSSNAME);
			if (mssName.indexOf(dDISSourceMSSName) != -1) {
				dDISSourceMSSName = mssName;
			} else {
				dDISSourceMSSName = dDISSourceMSSName.concat(mssName);
			}
		} else if (mss.containsKey(IDML_CDM_ATTR_HOSTNAME) && mss.containsKey(IDML_CDM_ATTR_MANUFACTNAME)
				&& mss.containsKey(IDML_CDM_ATTR_PRODNAME)) {
			dDISSourceMSSName = dDISSourceMSSName.concat(IDML_CDM_ATTR_HOSTNAME).concat(STR_EQUALS)
					.concat((String) mss.get(IDML_CDM_ATTR_HOSTNAME)).concat(STR_PLUS).concat(IDML_CDM_ATTR_MANUFACTNAME)
					.concat(STR_EQUALS).concat((String) mss.get(IDML_CDM_ATTR_MANUFACTNAME)).concat(STR_PLUS)
					.concat(IDML_CDM_ATTR_PRODNAME).concat(STR_EQUALS).concat((String) mss.get(IDML_CDM_ATTR_PRODNAME));
		}
		if (mss.containsKey(IDML_CDM_ATTR_GUID)) {
			propMap.put(DIS_MSG_ATTR_GUID, (String) mss.get(IDML_CDM_ATTR_GUID));
		}
		if (!dDISSourceMSSName.equals(DIS_MSG_ATTR_MSSNAME_VAL)) {
			propMap.put(DIS_MSG_ATTR_MSSNAME, dDISSourceMSSName);
		}
		propMap.put(DIS_MSG_ATTR_VERSION, DIS_MSG_ATTR_VERSION_VAL);
		return propMap;
	}

	/**
	 * Method returns a String containing the JSON message as required by the
	 * DIS Subscription Queue.
	 * 
	 * @param OrderedJSONObject
	 *            The object containing the containing DIS Operation Set.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@SuppressWarnings("unchecked")
	protected String parseJSONMessage(OrderedJSONObject opSetObject) throws Exception {
		OrderedJSONObject finalJSON = new OrderedJSONObject();
		OrderedJSONObject finaloperSet = new OrderedJSONObject();
		Iterator<String> iterator = opSetObject.getOrder();

		while (iterator.hasNext()) {

			String key = iterator.next();
			if (key.equals(DISBConstants.JSONMSG_ATTR_OPSETID)) {
				String opid = (String) opSetObject.get(key);
				if (opid != null) {
					finaloperSet.put(DISBConstants.JSONMSG_ATTR_OPSETID, opid);
				} else {
					throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS", DISBConstants.JSONMSG_ATTR_OPSETID));
				}
			} else {
				if (key.equals(DISBConstants.JSONMSG_ATTR_REFRESH)) {
					OrderedJSONObject refreshJsonObj = (OrderedJSONObject) opSetObject.get(key);
					if (refreshJsonObj == null) {
						throw new Exception(resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS",
								DISBConstants.JSONMSG_ATTR_REFRESH));
					}
					OrderedJSONObject newRefreshJsonObj = new OrderedJSONObject();
					String timeStamp = (String) refreshJsonObj.get(DISBConstants.JSONMSG_IDML_ATTR_TIMESTAMP);
					if (timeStamp != null) {
						newRefreshJsonObj.put(DISBConstants.JSONMSG_ATTR_TIMESTAMP, timeStamp);
					}
					OrderedJSONObject createJsonObj = (OrderedJSONObject) refreshJsonObj.get(DISBConstants.JSONMSG_ATTR_CREATE);
					if (createJsonObj == null) {
						throw new Exception(
								resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS", DISBConstants.JSONMSG_ATTR_CREATE));
					}
					OrderedJSONObject newCreateJsonObj = new OrderedJSONObject();
					constructCiAndRelationshipJson(createJsonObj, newCreateJsonObj);
					if (newCreateJsonObj != null) {
						newRefreshJsonObj.put(DISBConstants.JSONMSG_ATTR_CREATE, newCreateJsonObj);
					}
					if (newRefreshJsonObj != null) {
						finaloperSet.put(DISBConstants.JSONMSG_ATTR_REFRESH, newRefreshJsonObj);
					}
					refreshJsonObj = null;
					createJsonObj = null;
				} else if (key.equals(DISBConstants.JSONMSG_ATTR_CREATE)) {
					OrderedJSONObject createJsonObj = (OrderedJSONObject) opSetObject.get(key);
					if (createJsonObj == null) {
						throw new Exception(
								resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS", DISBConstants.JSONMSG_ATTR_CREATE));
					}
					OrderedJSONObject newCreateJsonObj = new OrderedJSONObject();
					constructCiAndRelationshipJson(createJsonObj, newCreateJsonObj);
					if (newCreateJsonObj != null) {
						finaloperSet.put(DISBConstants.JSONMSG_ATTR_CREATE, newCreateJsonObj);
					}
					createJsonObj = null;
				} else if (key.equals(DISBConstants.JSONMSG_ATTR_MODIFY)) {
					OrderedJSONObject modifyJsonObj = (OrderedJSONObject) opSetObject.get(key);
					if (modifyJsonObj == null) {
						throw new Exception(
								resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS", DISBConstants.JSONMSG_ATTR_MODIFY));
					}
					OrderedJSONObject newModifyJsonObj = new OrderedJSONObject();
					constructCiAndRelationshipJson(modifyJsonObj, newModifyJsonObj);
					if (newModifyJsonObj != null) {
						finaloperSet.put(DISBConstants.JSONMSG_ATTR_MODIFY, newModifyJsonObj);
					}
					modifyJsonObj = null;
				} else if (key.equals(DISBConstants.JSONMSG_ATTR_DELETE)) {
					OrderedJSONObject deleteJsonObj = (OrderedJSONObject) opSetObject.get(key);
					if (deleteJsonObj == null) {
						throw new Exception(
								resHash.getString("UNABLE.TO.GET.THE.OPSET.CONTENTS", DISBConstants.JSONMSG_ATTR_DELETE));
					}
					OrderedJSONObject newDeleteJsonObj = new OrderedJSONObject();
					constructCiAndRelationshipJson(deleteJsonObj, newDeleteJsonObj);
					if (newDeleteJsonObj != null) {
						finaloperSet.put(DISBConstants.JSONMSG_ATTR_DELETE, newDeleteJsonObj);
					}
					deleteJsonObj = null;
				}
			}
		}
		finalJSON.put(DISBConstants.JSONMSG_ATTR_OPSET, finaloperSet);
		return finalJSON.serialize(true);
	}

	/**
	 * Method populates a new OrderedJSONObject with the CI and Relationships as
	 * required by the DIS Subscription Queue.
	 * 
	 * @param operJsonObj
	 *            The OrderedJSONObject object containing the JSON Message from
	 *            IDML.
	 * @param newOperJsonObj
	 *            The new OrderedJSONObject that needs to be populated with the
	 *            JSON message as required by the DIS Subscription Queue.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	@SuppressWarnings("unchecked")
	protected void constructCiAndRelationshipJson(OrderedJSONObject operJsonObj, OrderedJSONObject newOperJsonObj) throws Exception {
		JarMetaData jarmetadata = new JarMetaData();
		Map<String, Object> relMap = jarmetadata.getTypes(DISBConstants.JSONMSG_ATTR_RELATIONSHIP);
		Iterator<String> iterator = operJsonObj.getOrder();
		while (iterator.hasNext()) {
			String key = iterator.next();
			if (key.equals(DISBConstants.JSONMSG_IDML_ATTR_TIMESTAMP)) {
				String timeStamp = (String) operJsonObj.get(key);
				if (timeStamp != null) {
					newOperJsonObj.put(DISBConstants.JSONMSG_ATTR_TIMESTAMP, timeStamp);
				}
			} else {
				if (key.equals(IDML_ELEMENT_CDMER)) {
					OrderedJSONObject arrOperObj = (OrderedJSONObject) operJsonObj.get(key);
					OrderedJSONObject newArrOperObj = new OrderedJSONObject();
					OrderedJSONObject newRelObj = new OrderedJSONObject();
					Iterator<String> miterator = arrOperObj.getOrder();
					while (miterator.hasNext()) {
						String mkey = miterator.next();
						if (relMap.containsKey(CDM_PREFIX + mkey)) {
							if (arrOperObj.get(mkey) instanceof JSONArray) {
								newRelObj.put(mkey, arrOperObj.get(mkey));
							} else {
								JSONArray newJSONArray = new JSONArray();
								newJSONArray.add(arrOperObj.get(mkey));
								newRelObj.put(mkey, newJSONArray);
								newJSONArray = null;
							}
						} else {
							if (arrOperObj.get(mkey) instanceof JSONArray) {
								newArrOperObj.put(mkey, arrOperObj.get(mkey));
							} else {
								JSONArray newJSONArray = new JSONArray();
								newJSONArray.add(arrOperObj.get(mkey));
								newArrOperObj.put(mkey, newJSONArray);
								newJSONArray = null;
							}
						}
					}
					if (newArrOperObj.size() > 0) {
						newOperJsonObj.put(DISBConstants.JSONMSG_ATTR_MODELOBJECT, newArrOperObj);
					}
					if (newRelObj.size() > 0) {
						newOperJsonObj.put(DISBConstants.JSONMSG_ATTR_RELATIONSHIP, newRelObj);
					}
				}
			}
		}
	}

	/**
	 * @return the jSONMessage
	 */
	public String getjSONMessage() {
		return this.jSONMessage;
	}

	/**
	 * @return the msgProperties
	 */
	public Map<String, Object> getMsgProperties() {
		return this.msgProperties;
	}
}
