/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchCondition;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.server.ResourceHash;

/**
 * Read/write {@link BranchingConfig} and {@link BranchCondition} elements in XML format.
 */
public class BranchingFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Name of the component.
	 */
	public final static String BRANCH_TAG = "Branch";

	/**
	 * Name of the component.
	 */
	public final static String BRANCH_CONDITION_TAG = "BranchCondition";

	/**
	 * Name of the enabled tag.
	 */
	public final static String ENABLED_TAG = "Enabled";

	/**
	 * Name of the script condition tag.
	 */
	public final static String SCRIPT_TAG = "ScriptCondition";

	/**
	 * Name of the conditions tag.
	 */
	public final static String CONDITION_TAG = "Conditions";

	/**
	 * Name of the left hand tag.
	 */
	public final static String LEFT_HAND_TAG = "LeftHand";

	/**
	 * Name of the right hand tag.
	 */
	public final static String RIGHT_HAND_TAG = "RightHand";

	/**
	 * Name of the operator tag.
	 */
	public final static String OPERATOR_TAG = "Operator";

	/**
	 * Name of the negate tag.
	 */
	public final static String NEGATE_TAG = "Negate";

	/**
	 * Name of the case sensitive tag.
	 */
	public final static String CASESENSITIVE_TAG = "CaseSensitive";

	/**
	 * Name of the match any tag.
	 */
	public final static String MATCH_ANY_TAG = "MatchAny";

	/**
	 * Name of the type tag.
	 */
	public final static String TYPE_TAG = "Type";

	/**
	 * Tag indicating the script is deleted, but kept to allow it to be revived
	 */
	public final static String SCRIPT_DELETED = "ScriptDeleted";
	
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration bconfig, Element elem) throws Exception {
		if (bconfig instanceof BranchingConfig)
			parse((BranchingConfig) bconfig, elem);
		else if (bconfig instanceof BranchCondition)
			parse((BranchCondition) bconfig, elem);
		else {
			throw new Exception(sResHash.getString(
					"MMCONFIG.BRANCHINGFACT.CANNOT.PARSE", bconfig.getClass()
							.getName()));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration bconfig, Element elem) throws Exception {
		if (bconfig instanceof BranchingConfig)
			build((BranchingConfig) bconfig, elem);
		else if (bconfig instanceof BranchCondition)
			build((BranchCondition) bconfig, elem);
		else {
			throw new Exception(sResHash.getString(
					"MMCONFIG.BRANCHINGFACT.CANNOT.SERIALIZE", bconfig
							.getClass().getName()));
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// BRANCHING CONFIG
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Parse a XML Branch element into a configuration object.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @throws Exception
	 *             in case the configuration object the provided XML element
	 *             represents is not recognized.
	 */
	public void parse(BranchingConfig config, Element elem) throws Exception {
		config.init();
		getBaseName(config, elem);

		NodeList list = elem.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (!(node instanceof Element))
				continue;

			String tag = list.item(i).getNodeName();

			if (tag.equalsIgnoreCase("#text")) {
				continue;
			} else if (tag.equals(SCRIPT_TAG)) {
				config.setScript(getNodeText(node));
			} else if (tag.equals(CONDITION_TAG)) {
				Factories.getFactory(ContainerFactory.CONTAINER_TAG).parse(
						config.getConditions(), (Element) list.item(i));
			} else if (tag.equals(MATCH_ANY_TAG)) {
				config.setMatchAny(Boolean.valueOf(getNodeText(node))
						.booleanValue());
			} else if (tag.equals(ENABLED_TAG)) {
				config.setEnabled(Boolean.valueOf(getNodeText(node))
						.booleanValue());
			} else if (tag.equals(TYPE_TAG)) {
				config.setBranchType(Integer.valueOf(getNodeText(node))
						.intValue());
			} else if (tag.equals(Factories.USER_COMMENT_ATTRIBUTE)) {
				config.setUserComment(getNodeText(node));
			} else if (tag.equals(SCRIPT_DELETED)) {
				config.setParameter(SCRIPT_DELETED, getNodeText(node));
			} else if (!tag.equals(MOD_TS_TAG)) {
				BaseConfiguration impl = Factories.getImpl(tag);
				config.addConfig(impl);
				Factories.getFactory(tag).parse(impl, (Element) list.item(i));
				// impl.init();
				impl.setupInheritanceChain();
			}
		}

	}

	/**
	 * Generate a XML Branch Condition element from a configuration object.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will be represented as a XML sub-tree.
	 * @param elem
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @throws Exception
	 *             in case the tag name of the provided XML element is not
	 *             recognized as a valid name which is used for configuration
	 *             object representation as a XML.
	 */
	public void build(BranchingConfig config, Element elem) throws Exception {
		setBaseName(config, elem);
		for (int i = 0; i < config.size(); i++) {
			BaseConfiguration bc = config.getConfig(i);
			String tag = Factories.getClassTag(bc);
			if (tag == null) {
				throw new Exception(sResHash.getString(
						"MMCONFIG.BRANCHINGFACT.CANNOT.SERIALIZE2", bc
								.getClass().getName()));
			}
			Element e = elem.getOwnerDocument().createElement(tag);
			elem.appendChild(e);
			Factories.getFactory(tag).build(bc, e);
		}

		// Script condition
		setSingleElement(elem, SCRIPT_TAG, config.getScript());

		// Conditions
		if (config.getConditions() != null) {
			Element e = elem.getOwnerDocument().createElement(CONDITION_TAG);
			elem.appendChild(e);
			Factories.getFactory(ContainerFactory.CONTAINER_TAG).build(
					config.getConditions(), e);
		}

		// Match any flag
		setSingleElement(elem, MATCH_ANY_TAG, "" + config.getMatchAny());

		// Enabled
		setSingleElement(elem, ENABLED_TAG, "" + config.getEnabled());

		// Type
		setSingleElement(elem, TYPE_TAG, "" + config.getBranchType());

		// Script deleted
		if (config.hasParameter(SCRIPT_DELETED))
			setSingleElement(elem, SCRIPT_DELETED, config.getStringParameter(SCRIPT_DELETED));
	}

	// //////////////////////////////////////////////////////////////////////////
	//
	// BRANCH CONDITION
	//
	// //////////////////////////////////////////////////////////////////////////

	/**
	 * Generate a XML Branch element from a configuration object.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will be represented as a XML sub-tree.
	 * @param elem
	 *            this is the part of the XML tree where the config object's XML
	 *            representation will be attached to.
	 * @throws Exception
	 *             in case the tag name of the provided XML element is not
	 *             recognized as a valid name which is used for configuration
	 *             object representation as a XML.
	 */
	public void build(BranchCondition config, Element elem) throws Exception {
		setSingleElement(elem, LEFT_HAND_TAG, config,
				InternalSchema.BRANCH_CONDITION_LEFT);
		setSingleElement(elem, OPERATOR_TAG, config,
				InternalSchema.BRANCH_CONDITION_OPER);
		setSingleElement(elem, RIGHT_HAND_TAG, config,
				InternalSchema.BRANCH_CONDITION_RIGHT);
		setSingleElement(elem, NEGATE_TAG, config,
				InternalSchema.BRANCH_CONDITION_NEGATE);
		setSingleElement(elem, CASESENSITIVE_TAG, config,
				InternalSchema.BRANCH_CONDITION_CASE_SENSITIVE);
		setSingleElement(elem, MATCH_ANY_TAG, ""+config.getMatchAny());
	}

	/**
	 * Parse a XML Branch Condition element into a configuration object.
	 * 
	 * @param config
	 *            an instance of the {@link BaseConfiguration} class which
	 *            internal parameters will get set based on the information from
	 *            the provided XML Element.
	 * @param elem
	 *            this is a part of the XML tree that represents the
	 *            {@link BaseConfiguration} object.
	 * @throws Exception
	 *             in case the configuration object the provided XML element
	 *             represents is not recognized.
	 */
	public void parse(BranchCondition config, Element elem) throws Exception {

		config.init();
		getBaseName(config, elem);

		String str;

		if ((str = getNodeTextByName(elem, LEFT_HAND_TAG)) != null)
			config.setLeftHand(str);

		if ((str = getNodeTextByName(elem, RIGHT_HAND_TAG)) != null)
			config.setRightHand(str);

		if ((str = getNodeTextByName(elem, OPERATOR_TAG)) != null)
			config.setOperator(str);

		if ((str = getNodeTextByName(elem, NEGATE_TAG)) != null)
			config.setNegate(str.equalsIgnoreCase("true"));

		if ((str = getNodeTextByName(elem, CASESENSITIVE_TAG)) != null)
			config.setCaseSensitive(str.equalsIgnoreCase("true"));
		
		if ((str = getNodeTextByName(elem, MATCH_ANY_TAG)) != null)
			config.setMatchAny(str.equalsIgnoreCase("true"));
	}

}
