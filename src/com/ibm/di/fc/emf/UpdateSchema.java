/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.emf;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;

import com.ibm.di.config.base.SchemaItemConfigImpl;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.server.ResourceHash;

/**
 * Provides a discover schema functionality for the EMF XMLToSDO and SDOToXML
 * Function Components.
 * 
 * <br />
 * <br />
 * <b> THIS CLASS HAS BEEN DEPRECATED SINCE THE IBM Tivoli Directory Integrator 7.0
 * RELEASE AND WILL BE COMPLETELY REMOVED IN A FUTURE RELEASE! </b>
 */
@Deprecated
public class UpdateSchema {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String mXSDFile = null;

	private Map mURIToPrefixMap = null;

	private boolean mAlwaysPrefix = false;

	private boolean mIsSDOToXML = false;

	private Set mClassSet = null;

	private List mPropertyNameList = null;

	private List mPropertyTypeList = null;

	private ResourceHash mResHash = null;

	private ModelParser mModelParser = null;

	/**
	 * Initialize the UpdateSchema instance.
	 * 
	 * @param xsdFile
	 *            The XML Schema file location.
	 * @param uriToPrefixMap
	 *            The namespace prefix to namespace URI map.
	 * @param alwaysPrefix
	 *            If <code>true</code> all XML elements are namespace
	 *            prefixed.
	 * @param isSDOToXML
	 *            If <code>true</code> specifies that the object will execute
	 *            to discover schema functionality for the EMF XMLToSDO Function
	 *            Component. If <code>false</code> discover function for EMF
	 *            SDOToXML Function Component will be executed.
	 */
	@Deprecated
	public UpdateSchema(String xsdFile, Map uriToPrefixMap,
			boolean alwaysPrefix, boolean isSDOToXML, ResourceHash resHash) {
		mXSDFile = xsdFile;
		mURIToPrefixMap = uriToPrefixMap;
		mAlwaysPrefix = alwaysPrefix;
		mIsSDOToXML = isSDOToXML;
		mResHash = resHash;

		mModelParser = new ModelParser(mResHash);
	}

	/**
	 * Adds items to the function configuration corresponding to the XML
	 * elements and attributes defined in the XML Schema file.
	 * 
	 * @param config
	 *            the function configuration object.
	 * @throws Exception
	 *             if the XML Schema file passed to the constructor during
	 *             object creation is not found or is an invalid XML Schema
	 *             file.
	 */
	@Deprecated
	public void updateSchema(FunctionConfig config) throws Exception {
		guessSchema(config, true);
		guessSchema(config, false);
	}

	/**
	 * Implements the discover schema functionality. The function reads the XML
	 * Schema file, creates an Ecore model and traverses it to build the Entry
	 * Attribute names that are added as items to the function configuration.
	 * 
	 * @param config
	 *            the function configuration object.
	 * @param isInput
	 *            specifies weather we discover the input or the output schema.
	 * @throws Exception
	 *             if the XML Schema file passed to the constructor during
	 *             object creation is not found or is an invalid XML Schema
	 *             file.
	 */
	private void guessSchema(FunctionConfig config, boolean isInput)
			throws Exception {
		SchemaConfig schema = config.getSchema(isInput);

		/*
		 * The input schema of the XMLToSDO Function component is the same as
		 * the output schema of the SDOToXML
		 */
		if (isInput ^ mIsSDOToXML) {
			Collection packageCollection = mModelParser
					.getPackageCollection(mXSDFile);
			mModelParser.checkPackagesForConformity(packageCollection,
					mAlwaysPrefix);

			for (Iterator i = packageCollection.iterator(); i.hasNext();) {
				EPackage epackage = (EPackage) i.next();
				traversePackage(epackage);

				if (mPropertyNameList != null) {
					Iterator nameIterator = mPropertyNameList.iterator();
					Iterator typeIterator = mPropertyTypeList.iterator();
					while (nameIterator.hasNext()) {
						String propertyName = (String) nameIterator.next();
						String propertyType = (String) typeIterator.next();

						setSchemaItem(schema, propertyName, propertyType);
					}
				}
			}
		}
	}

	/**
	 * Adds a single item to the schema configuration.
	 * 
	 * @param schema
	 *            the schema configuration object.
	 * @param propertyName
	 *            the name of the item to be added.
	 * @param propertyType
	 *            the type of the item to be added.
	 */
	private void setSchemaItem(SchemaConfig schema, String propertyName,
			String propertyType) {
		SchemaItemConfig itemConfig = new SchemaItemConfigImpl();
		itemConfig.setAttributeName(propertyName);
		if (propertyType != null) {
			itemConfig.setJavaClass(propertyType);
		}
		schema.setItem(propertyName, itemConfig);
	}

	/**
	 * Traverses an EPackage object from the Ecore model created from the XML
	 * Schema file. The function creates a list of Entry Attribute names
	 * corresponding to the XML elements and attributes defined in the XML
	 * Schema file.
	 * 
	 * @param pack
	 *            EPackage object from the Ecore model created from the XML
	 *            Schema file.
	 */
	private void traversePackage(EPackage pack) throws Exception {
		EClass docRoot = (EClass) pack
				.getEClassifier(ModelParser.DOCROOT_CLASSNAME);
		if (docRoot != null) {
			mClassSet = new HashSet();
			mPropertyNameList = new LinkedList();
			mPropertyTypeList = new LinkedList();

			mPropertyNameList.add(ModelParser.ROOT_NAME);
			mPropertyTypeList.add(docRoot.getInstanceClassName());

			traverseClass(docRoot, ModelParser.ROOT_NAME);
		} else {
			mPropertyNameList = null;
			mPropertyTypeList = null;
		}
	}

	/**
	 * Traverses <code>eclass</code>'s features and adds them to the lists
	 */
	private void traverseClass(EClass eclass, String path) throws Exception {
		String className = eclass.getName();
		if (!mClassSet.contains(className)) {
			mClassSet.add(className);

			List featureList = eclass.getEAllStructuralFeatures();
			for (Iterator iter = featureList.iterator(); iter.hasNext();) {
				EStructuralFeature feature = (EStructuralFeature) iter.next();
				String featureName = ModelParser.getFeatureName(feature);

				if (!ModelParser.isIgnored(featureName)) {
					String featurePath = ModelParser.makeFeaturePath(feature,
							path, mURIToPrefixMap, mAlwaysPrefix);
					traverseFeature(feature, featurePath);
				}
			}

			mClassSet.remove(className);
		}
	}

	/**
	 * Adds the feature's path and type to the lists and continue traversing if
	 * the feature is a complex type
	 */
	private void traverseFeature(EStructuralFeature feature, String featurePath)
			throws Exception {
		EClassifier featureType = feature.getEType();
		mPropertyNameList.add(featurePath);
		mPropertyTypeList.add(featureType.getInstanceClassName());

		if (featureType instanceof EClass) {
			traverseClass((EClass) featureType, featurePath);
		}
	}
}
