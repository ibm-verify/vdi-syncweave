/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.cdm.jar;

import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.di.cdm.core.CDMConstants;
import com.ibm.di.cdm.core.NamingRule;
import com.ibm.di.cdm.core.NamingRuleIdentifier;
import com.ibm.di.cdm.core.NamingRulesReader;
import com.ibm.dl.core.certification.Utils;

/**
 * The reader for JAR Naming Rule meta-data.
 * 
 */
public class JarNamingRulesReader extends NamingRulesReader {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The location of the XML containing the Naming Rule meta-data in the JAR
	 * file.
	 */
	private static final String NAMING_RULES_LOCATION = "/NamingRules.xml";

	/**
	 * The parser used for reading the XML file.
	 */
	private SAXParser parser;

	/**
	 * Maps the policies (sets of rules) corresponding to each class type.
	 */
	private Map<String, Set<String>> classToPolicies;

	/**
	 * Contains the Naming Rules for each Policy.
	 */
	private Map<String, List<NamingRule>> namingPolicies;

	/**
	 * Constructor.
	 * 
	 * @throws ParserConfigurationException
	 *             problem parsing the XML meta-data.
	 * @throws SAXException
	 *             problem parsing the XML meta-data.
	 * @throws IOException
	 *             problem parsing the XML meta-data.
	 */
	public JarNamingRulesReader() throws ParserConfigurationException, SAXException, IOException {
		classToPolicies = new HashMap<String, Set<String>>();
		namingPolicies = new HashMap<String, List<NamingRule>>();
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		saxParserFactory.setNamespaceAware(true);
		saxParserFactory.setValidating(true);
		parser = saxParserFactory.newSAXParser();
		parser.parse(this.getClass().getResourceAsStream(NAMING_RULES_LOCATION), new NamingRulesParserHandler());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<NamingRule> getAllNamingRules(String cdmClassName) {
		List<NamingRule> rules = new ArrayList<NamingRule>();
		Set<String> policies = classToPolicies.get(cdmClassName);
		if (policies != null) {
			for (String policy : policies) {
				List<NamingRule> policyRules = namingPolicies.get(policy);
				if (policyRules != null) {
					rules.addAll(policyRules);
				}
			}
		}
		// sort by priority
		Collections.sort(rules);
		return rules;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getFullyQualifiedName(String className) {
		if (isFullyQualifiedName(className)) {
			return className;
		}
		return Utils.convertCdmNameToDLSchemaName(className);
	}

	/**
	 * Checks if this type is fully qualified.
	 * 
	 * @param classType
	 *            input type.
	 * @return <code>true</code> if the name is fully qualified, otherwise
	 *         <code>false</code>.
	 */
	private boolean isFullyQualifiedName(String classType) {
		return classType.startsWith(CDMConstants.JAR_CDM_NAMEPACE) && classType.contains(".IDML_");
	}

	/**
	 * The handler providing the logic for the Naming Rule-s parser.
	 * 
	 */
	private class NamingRulesParserHandler extends DefaultHandler {

		/**
		 * The name of the Policy tag in the Naming Rule-s XML.
		 */
		private static final String POLICY_TAG = "NamingPolicy";

		/**
		 * The name of the Rule tag in the Naming Rule-s XML.
		 */
		private static final String RULE_TAG = "NamingRule";

		/**
		 * The name of the Identifier tag in the Naming Rule-s XML.
		 */
		private static final String IDENTIFIER_TAG = "Identifier";

		/**
		 * The name of the Omitted Identifier tag in the Naming Rule-s XML.
		 */
		private static final String OMITTED_IDENTIFIER_TAG = "OmittedIdentifier";

		/**
		 * The name of the Mapping tag in the Naming Rule-s XML.
		 */
		private static final String MAPPING_TAG = "Mapping";

		/**
		 * The policy list.
		 */
		private List<NamingRule> policy = null;

		/**
		 * The currently read Naming Rule.
		 */
		private JarNamingRule rule = null;

		/**
		 * {@inheritDoc}
		 */
		public void startElement(String namespaceURI, String lname, String qname, Attributes attrs) {
			if (qname.equals(POLICY_TAG)) {
				policy = new ArrayList<NamingRule>();
				String name = attrs.getValue("name");
				namingPolicies.put(name, policy);
			} else if (qname.equals(RULE_TAG)) {
				String name = attrs.getValue("name");
				int priority = Integer.valueOf(attrs.getValue("priority"));
				rule = new JarNamingRule(name, priority);
				policy.add(rule);
			} else if (qname.equals(IDENTIFIER_TAG) || qname.equals(OMITTED_IDENTIFIER_TAG)) {
				NamingRuleIdentifier identifier = null;
				boolean isRequired = qname.equals(IDENTIFIER_TAG);
				String attributeName = attrs.getValue("keyword");
				String relationshipType = attrs.getValue("relationship");
				if (relationshipType != null && relationshipType.length() > 0) {
					// implicit attribute
					String sourceClass = attrs.getValue("relationshipSource");
					String targetClass = attrs.getValue("relationshipTarget");
					boolean isSource = false;
					String relatedClass = null;
					if (sourceClass != null && sourceClass.trim().length() > 0) {
						isSource = false;
						relatedClass = sourceClass.trim();
					} else if (targetClass != null && targetClass.trim().length() > 0) {
						isSource = true;
						relatedClass = targetClass.trim();
					}
					identifier = new JarNamingRuleIdentifier(attributeName, isRequired, relationshipType, relatedClass,
							isSource);
				} else {
					identifier = new JarNamingRuleIdentifier(attributeName, isRequired);
				}
				rule.addNamingIdentifier(identifier);
			} else if (qname.equals(MAPPING_TAG)) {
				String policyName = attrs.getValue("policy");
				String className = attrs.getValue("class");
				if (className.startsWith(CDM_PREFIX)) {
					className = className.substring(CDM_PREFIX.length());
				}
				className = className.replace('/', '.');
				Set<String> policies = classToPolicies.get(className);
				if (policies == null) {
					policies = new HashSet<String>();
					classToPolicies.put(className, policies);
				}
				policies.add(policyName);
			}
		}
	}

}