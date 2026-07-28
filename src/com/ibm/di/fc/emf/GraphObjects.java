/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.emf;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.sdo.EDataObject;

import com.ibm.di.entry.Attribute;
import com.ibm.di.server.ResourceHash;

/**
 * This class is used for traversing Data Object structures. It can create Entry
 * Attributes corresponding to the Data Objects in the Data Graph and to add new
 * Data Object to it.
 * 
 * <br />
 * <br />
 * <b> THIS CLASS HAS BEEN DEPRECATED SINCE THE SyncWeave 7.0 RELEASE AND WILL BE COMPLETELY
 * REMOVED IN A FUTURE RELEASE! </b>
 */
@Deprecated
public class GraphObjects {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private List mAttributeList = null;

	private Map mPrefixToURIMap = null;

	private Map mURIToPrefixMap = null;

	private boolean mAlwaysPrefix = false;

	private ResourceHash mResHash = null;

	private ModelParser mModelParser = null;

	/**
	 * Initializes the object.
	 * 
	 * @param resHash
	 *            Specifies the logger used for logging information and errors
	 *            messages.
	 */
	@Deprecated
	public GraphObjects(ResourceHash resHash) {
		mResHash = resHash;
		mModelParser = new ModelParser(mResHash);
	}

	/**
	 * Traverse the data graph rooted at <code>root</code> and creates Entry
	 * Attributes for the Data Objects.
	 * 
	 * @param root
	 *            Data Object specifying the root element of the data graph.
	 * @param uriToPrefixMap
	 *            namespace URI to prefix map.
	 * @param alwaysPrefix
	 *            if <code>true</code> the XML element or attribute names are
	 *            prefixed with namespace URI if no prefix is present.
	 * 
	 * @return list with Entry Attributes containing Data Objects from the data
	 *         graph.
	 */
	@Deprecated
	public List getAttributes(EDataObject root, Map uriToPrefixMap,
			boolean alwaysPrefix) {
		mURIToPrefixMap = uriToPrefixMap;
		mAlwaysPrefix = alwaysPrefix;

		mAttributeList = new LinkedList();
		mAttributeList.add(new Attribute(ModelParser.ROOT_NAME, root));

		getObject(root, ModelParser.ROOT_NAME);

		return mAttributeList;
	}

	/**
	 * Traverses <code>vex</code>'s properties and adds them to the attribute
	 * list
	 */
	private void getObject(EDataObject vex, String path) {
		EClass eclass = vex.eClass();
		List featureList = eclass.getEAllStructuralFeatures();
		for (Iterator iter = featureList.iterator(); iter.hasNext();) {
			EStructuralFeature feature = (EStructuralFeature) iter.next();
			String featureName = ModelParser.getFeatureName(feature);

			if (!ModelParser.isIgnored(featureName)) {
				Object obj = vex.eGet(feature);
				String featurePath = ModelParser.makeFeaturePath(feature, path,
						mURIToPrefixMap, mAlwaysPrefix);
				getValue(obj, featurePath);
			}
		}
	}

	/**
	 * Adds an object to the attribute list
	 */
	private void getValue(Object obj, String path) {
		if (obj != null) {
			if (obj instanceof List) {
				List values = (List) obj;

				Attribute attribute = new Attribute(path);
				attribute.setValues(values.toArray());
				mAttributeList.add(attribute);

				if (values.size() == 1) {
					Object value = values.get(0);
					if (value instanceof EDataObject) {
						getObject((EDataObject) value, path);
					}
				}
			} else {
				Attribute attribute = new Attribute(path, obj);
				mAttributeList.add(attribute);

				if (obj instanceof EDataObject) {
					getObject((EDataObject) obj, path);
				}
			}
		}
	}

	/**
	 * Adds the value(s) of the Entry Attribute to the data graph rooted at
	 * <code>root</code>. The Entry Attribute's name specifies the position
	 * of the value(s) in the data graph. If part of the intermediate elements
	 * from the root to the values' position do not exists they are also
	 * created.
	 * 
	 * @param root
	 *            Data Object specifying the root element of the data graph.
	 * @param attribute
	 *            an Entry Attribute which name specifies the position in the
	 *            data graph where the Entry Attribute's value(s) have to be
	 *            inserted.
	 * @param prefixToURIMap
	 *            prefix to namespace URI map.
	 * 
	 * @throws Exception
	 *             if the Entry Attribute's name contains a non-existing XML
	 *             element or attribute name according to the XML Schema. if the
	 *             Entry Attribute's name specifies an XML element or attribute
	 *             that.
	 */
	@Deprecated
	public void applyAttribute(EDataObject root, Attribute attribute,
			Map prefixToURIMap) throws Exception {
		mPrefixToURIMap = prefixToURIMap;

		String path = attribute.getName();
		int pos = ModelParser.getSeparatorPosition(path);
		path = path.substring(pos + 1);

		setObject(root, path, attribute);
	}

	private void setObject(EDataObject obj, String path, Attribute attribute)
			throws Exception {
		int pos = ModelParser.getSeparatorPosition(path);

		if (pos == -1) {
			EStructuralFeature feature = getFeature(obj.eClass(), path);
			setFeature(obj, feature, attribute);
		} else {
			String featureName = path.substring(0, pos);
			EStructuralFeature feature = getFeature(obj.eClass(), featureName);
			if (feature.isMany()) {
				EList propertyList = (EList) obj.eGet(feature);
				if (propertyList.size() > 1) {
					String attributeName = attribute.getName();
					String errorMessage = mResHash.getString(
							"ELEMENT.ANCESTOR.SIBLINGS", new Object[] {
									attributeName, featureName });
					throw new Exception(errorMessage);
				} else {
					path = path.substring(pos + 1);

					EDataObject property = null;
					if (propertyList.size() == 1) {
						property = (EDataObject) propertyList.get(0);
					} else {
						property = createObject(feature);
						propertyList.add(property);
					}

					setObject(property, path, attribute);
				}
			} else {
				path = path.substring(pos + 1);

				EDataObject property = (EDataObject) obj.eGet(feature);
				if (property == null) {
					property = createObject(feature);
					obj.eSet(feature, property);
				}

				setObject(property, path, attribute);
			}
		}
	}

	/**
	 * Creates and returns an object
	 */
	private EDataObject createObject(EStructuralFeature feature) {
		EClass featureType = (EClass) feature.getEType();
		EPackage featurePackage = featureType.getEPackage();
		EDataObject dataObject = (EDataObject) featurePackage
				.getEFactoryInstance().create(featureType);

		return dataObject;
	}

	/**
	 * Sets the specified feature of the Data Object
	 */
	private void setFeature(EDataObject obj, EStructuralFeature feature,
			Attribute attribute) throws Exception {
		if (feature.isMany()) {
			EList values = new BasicEList(attribute.getValuesVector());
			obj.eSet(feature, values);
		} else {
			Object value = attribute.getValue(0);
			if (feature instanceof EAttribute) {
				EDataType dataType = ((EAttribute) feature).getEAttributeType();
				if (dataType instanceof EEnum) {
					EEnumLiteral val = ((EEnum) dataType)
							.getEEnumLiteral((String) value);
					if (val == null) {
						String errorMessage = mResHash.getString(
								"VALUE.ENUMERATION.NOT.ALLOWED", new Object[] {
										feature.getName(), (String) value });
						throw new Exception(errorMessage);
					}
					obj.eSet(feature, val);
					return;

				}

			}
			obj.eSet(feature, value);
		}
	}

	/**
	 * The method changes the type of a feature in the model specified by path
	 * expression defining the nesting of the XML elements.
	 * 
	 * @param rootClass
	 *            the root class in the Ecore model.
	 * @param featurePath
	 *            path expression representing the path from to root class to
	 *            the feature.
	 * @param dataType
	 *            the feature's new data type.
	 * @param prefixToURIMap
	 *            prefix to namespace URI map
	 * 
	 * @throws Exception
	 *             if the Entry Attribute's name contains a non-existing XML
	 *             element or attribute name according to the model.
	 */
	@Deprecated
	public void applyClassToModel(EClass rootClass, String featurePath,
			EClass dataType, Map prefixToURIMap) throws Exception {
		mPrefixToURIMap = prefixToURIMap;
		int pos = ModelParser.getSeparatorPosition(featurePath);
		featurePath = featurePath.substring(pos + 1);
		applyClassToFeature(rootClass, featurePath, dataType);
	}

	/**
	 * Changes the type of a feature in the model specified by path expression
	 * relative to the <code>eclass</code> class.
	 */
	private void applyClassToFeature(EClass eclass, String featurePath,
			EClass dataType) throws Exception {
		int pos = ModelParser.getSeparatorPosition(featurePath);

		if (pos == -1) {
			EStructuralFeature feature = getFeature(eclass, featurePath);
			feature.setEType(dataType);
		} else {
			String featureName = featurePath.substring(0, pos);
			featurePath = featurePath.substring(pos + 1);
			EStructuralFeature feature = getFeature(eclass, featureName);

			applyClassToFeature((EClass) feature.getEType(), featurePath,
					dataType);
		}
	}

	/**
	 * Gets a feature representing XML element or attribute with the specified
	 * name. In case of error the method logs the error and throws an exception.
	 * 
	 * @param eclass
	 *            the EClass containg the searched feature
	 * @param featureName
	 *            the name of the XML element or attribute.
	 * 
	 * @return the feature representing the XML element or attribute with the
	 *         specified name.
	 * 
	 * @throws Exception
	 *             if the <code>eclass</code> does not contain a feature
	 *             representing XML element or attribute with the specified
	 *             name.
	 */
	private EStructuralFeature getFeature(EClass eclass, String featureName)
			throws Exception {
		try {
			return mModelParser
					.getFeature(eclass, featureName, mPrefixToURIMap);
		} catch (Exception e) {
			String errorMessage = mResHash.getString("INVALID.XML.ELEMENT",
					featureName);
			throw new Exception(errorMessage);
		}
	}
}
