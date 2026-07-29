/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.ReconnectRuleConfig;
import com.ibm.di.connector.ConnectorInterface;

/**
 * 
 * <p>
 * This class decides what to do when a connector raises an error. Possible
 * actions are to attempt a reconnect or leave the error unhandled and let
 * further mechanisms process it.
 * </p>
 * 
 * <p>
 * The class makes decisions based on configured rules. Each rule describes what
 * should be done when an error occurs. Each rule applies to certain connectors
 * and certain errors. In a rule the connectors are described by their base Java
 * class and their name in the current configuration. (there are match-all
 * options for both the class and the name of the connector). A rule describes
 * applicable errors by their base Java class and a regular expression that
 * matches their messages (the regular expression is optional).
 * </p>
 * 
 * <p>
 * The reconnect engine holds two collections of rules: in-built rules and
 * user-defined rules.
 * </p>
 * 
 * <p>
 * The in-built rules are defined in an '.inf' file in the jar file of the
 * corresponding connector. They are specific to that connector class. In order
 * to preserver the reconnect behaviour, which existed before the introduction
 * of ReconnectRuleEngine, when the engine is created, it implicitly adds to the
 * in-built rules rules, which prescribe to attempt reconnect on all
 * IOException-s and all CommunicationException-s.
 * </p>
 * 
 * <p>
 * The user defined rules reside in an external text file.
 * </p>
 */
public class ReconnectRuleEngine {

	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/** invalid reconnect action */
	public static final int INVALID_RECONNECT_ACTION = 0;

	/** the situation is a fatal error which no reconnect could fix */
	public static final int ERROR = 1;

	/** reconnect should be attempted to fix the error situation */
	public static final int RECONNECT = 2;

	/** the delimiter of the parts of a rule in the config file */
	public static final char RECONNECT_RULE_PARTS_DELIMETER = ':';

	/*
	 * the following are the reconnect actions in string format in the way that
	 * they should appear in the configuration file with user defined reconnect
	 * rules
	 */

	/** error action - no reconnect will be attempted */
	public static final String ERROR_STRING = "error";

	/** reconnect action - if an error occurs reconnect will be attempted */
	public static final String RECONNECT_STRING = "reconnect";

	/** The rules from the external file with user-defined rules. */
	private List<ReconnectRule> userDefinedRules = new ArrayList<ReconnectRule>();

	/**
	 * If no matching rule is found, the default action is applied
	 */
	private static final int defaultAction = ReconnectRuleEngine.ERROR;

	/**
	 * The maximum depth in the error chain that will be considered. This
	 * constant guards against endless loops if the chain has cycles.
	 */
	private static final int MAX_ERROR_CHAIN_DEPTH = 128;
	
	/**
	 * Log object used to log messages to log files.
	 */
	private final Log log;

	/**
	 * TMS Filename used for info, error and debug messages.
	 */
	private static final String PROPERTIES_FILE = "miserver";

	/**
	 * Message Resource Hash used to access the TMS messages.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * The following class represents a single rule.
	 */
	static private class ReconnectRule {

		/**
		 * Copyright information.
		 */
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * Copyright information.
		 */
		public static final Class CONNECTOR_CLASS_MATCH_ALL = Object.class;

		/**
		 * Match all option for the name of the connector instance.
		 */
		public static final String CONNECTOR_NAME_MATCH_ALL = "";

		/**
		 * Match all option for the exception classes this rule applies.
		 */
		public static final Class EXCEPTION_CLASS_MATCH_ALL = Throwable.class;

		/**
		 * Match all option for the exception message.
		 */
		public static final String EXCEPTION_MESSAGE_REGEXP_MATCH_ALL = "";

		/**
		 * The class of the connector this rule applies to.
		 */
		private Class connectorClass;

		/**
		 * The name of the connector this rule applies to.
		 */
		private String connectorName;

		/**
		 * The class of the exception this rule applies to.
		 */
		private Class exceptionClass;

		/**
		 * Regular expression that matches the message of the exception this
		 * rule applies to.
		 */
		private String exceptionMessageRegExp;

		/**
		 * The action prescribed by this rule.
		 */
		private int action;

		/**
		 * Creates a reconnect rule.
		 * 
		 * @param connClass
		 *            the class of the connector to which the rule applies or
		 *            <code>CONNECTOR_CLASS_MATCH_ALL</code> if the rule
		 *            applies to all connectors
		 * 
		 * @param connName
		 *            the name of the connector instance (as specified in the
		 *            configuration of the current solution) or
		 *            <code>CONNECTOR_NAME_MATCH_ALL</code> if the rule
		 *            applies to all connectors
		 * 
		 * @param exClass
		 *            the class/superclass of exceptions to which this rule
		 *            applies or <code>EXCEPTION_CLASS_MATCH_ALL</code> to
		 *            match all exceptions
		 * 
		 * @param regExp
		 *            a regular expression that matches the message of the
		 *            exceptions to which this rule applies or
		 *            <code>EXCEPTION_MESSAGE_REGEXP_MATCH_ALL</code> to match
		 *            all messages
		 * 
		 * @param action
		 *            the action prescribed by this rule, must be
		 *            ReconnectRuleEngine.ERROR or ReconnectRuleEngine.RECONNECT
		 * 
		 */
		public ReconnectRule(Class connClass, String connName, Class exClass,
				String regExp, int action) {

			this.connectorClass = connClass;
			if (connectorClass == null) {
				connectorClass = CONNECTOR_CLASS_MATCH_ALL;
			}

			this.connectorName = connName;
			if (connectorName == null || connectorName.trim().length() == 0) {
				connectorName = CONNECTOR_NAME_MATCH_ALL;
			}

			this.exceptionClass = exClass;
			if (exceptionClass == null) {
				exceptionClass = EXCEPTION_CLASS_MATCH_ALL;
			}

			this.exceptionMessageRegExp = regExp;
			if (exceptionMessageRegExp == null
					|| exceptionMessageRegExp.trim().length() == 0) {
				exceptionMessageRegExp = EXCEPTION_MESSAGE_REGEXP_MATCH_ALL;
			}

			this.action = action;
			if (action == INVALID_RECONNECT_ACTION) {
				this.action = ERROR;
			}
		}

		/**
		 * Returns the action prescribed by this rule.
		 * 
		 * @return the action prescribed by this rule
		 */
		public int getAction() {
			return action;
		}

		/**
		 * This method decides whether the current rule is more general than
		 * another rule. The regular expression parts of the rules are ignored
		 * in the generality test. If the rules are equal (ignoring the regular
		 * expression), none is considered more general.
		 * 
		 * @param other
		 *            the other rule
		 * @return whether the current rule is more general than the other rule
		 */
		public boolean isGeneralizationOf(ReconnectRule other) {

			/*
			 * whether this rule covers all connectors and exceptions covered by
			 * the other rule (ignoring regular expressions) eventually the
			 * rules may be equal (ignoring regular expressions)
			 */
			boolean matchOtherRule = matchConnector(other.connectorClass,
					other.connectorName)
					&& this.exceptionClass
							.isAssignableFrom(other.exceptionClass);

			// whether the rules are equal (ignoring regular expressions)
			boolean equalToOtherRule = this.connectorClass
					.equals(other.connectorClass)
					&& this.connectorName.equals(other.connectorName)
					&& this.exceptionClass.equals(other.exceptionClass);

			// Analogy : '>' is equivalent to ('>=' && not '==')
			return matchOtherRule && !equalToOtherRule;
		}

		/**
		 * This method decides whether the current rule matches the specified
		 * connector.
		 * 
		 * @param connectorClass
		 *            the class of the connector
		 * @param connectorName
		 *            the name of the connector
		 * @return whether the current rule is more general than the other rule
		 */
		public boolean matchConnector(Class connectorClass, String connectorName) {

			// connector subclasses are also matched
			boolean isConnectorClassGeneralization = this.connectorClass
					.equals(CONNECTOR_CLASS_MATCH_ALL)
					|| this.connectorClass.isAssignableFrom(connectorClass);

			// component names in TDI are case sensitive, so mind the case
			boolean isConnectorNameGeneralization = this.connectorName
					.equals(CONNECTOR_NAME_MATCH_ALL)
					|| this.connectorName.equals(connectorName);

			return isConnectorClassGeneralization
					&& isConnectorNameGeneralization;

		}

		/**
		 * This method decides whether the current rule matches a given error
		 * situation.
		 * 
		 * @param connectorClass
		 *            the class of the connector, which raised the error
		 * @param connectorName
		 *            the name of the connector, which raised the error
		 * @param error
		 *            the error object
		 * 
		 * @return whether the current rule applies to the specified error
		 *         situation
		 */
		public boolean matchErrorSituation(Class connectorClass,
				String connectorName, Throwable error) {

			if (matchConnector(connectorClass, connectorName)
					&& this.exceptionClass.isInstance(error)) {

				if (exceptionMessageRegExp
						.equals(EXCEPTION_MESSAGE_REGEXP_MATCH_ALL))
					return true;

				String msg = error.getMessage() != null ? error.getMessage()
						: "";
				return Pattern.matches(exceptionMessageRegExp, msg);
			}
			return false;
		}

	} // end of ReconnectRule

	/**
	 * Constructor, which loads the in-built rules and the user-defined rules.
	 * 
	 * @param log
	 *            a log object to be used during the lifetime of the engine
	 */
	public ReconnectRuleEngine(Log log) {

		this.log = log;

		/*
		 * In order to preserve the behavior, which was before the
		 * ReconnectRuleEngine was introduced, add rules to reconnect on
		 * IOException-s and CommunicationException-s.
		 */
		userDefinedRules.add(new ReconnectRule(
				ReconnectRule.CONNECTOR_CLASS_MATCH_ALL,
				ReconnectRule.CONNECTOR_NAME_MATCH_ALL,
				java.io.IOException.class,
				ReconnectRule.EXCEPTION_MESSAGE_REGEXP_MATCH_ALL, RECONNECT));
		userDefinedRules.add(new ReconnectRule(
				ReconnectRule.CONNECTOR_CLASS_MATCH_ALL,
				ReconnectRule.CONNECTOR_NAME_MATCH_ALL,
				javax.naming.CommunicationException.class,
				ReconnectRule.EXCEPTION_MESSAGE_REGEXP_MATCH_ALL, RECONNECT));
	}

	/**
	 * Loads the in-built and the user-defined rules.
	 * 
	 * @param userDefinedRulesFilePath
	 *            the file with user-defined rules
	 * @param sysConfig
	 *            system configuration object, which has the in-built rules of
	 *            all connectors
	 * @exception Exception
	 *                problem while parsing the configured rules
	 *                ClassNotFoundException problem while loading an
	 *                exception/connector class, specified in a rule
	 */
	public void loadRules(String userDefinedRulesFilePath,
			MetamergeConfig sysConfig) throws Exception {
		try {
			parseUserDefinedReconnectRules(userDefinedRules,
					userDefinedRulesFilePath);
		} finally {
			// log the loaded rules anyway
			dumpRules(userDefinedRules,
					"MISERVER.RECONNECTENGINE.USERDEFINED.RULES.TITLE");
		}
	}

	/**
	 * <p>
	 * This method prescribes a response action for a given error situation.
	 * </p>
	 * <p>
	 * First searches the user-defined rules and if no matching rule is found,
	 * the method searches the Connector own rules. If a rule cannot be found in
	 * either collection, the default action (<code>ERROR</code>) is
	 * prescribed.
	 * </p>
	 * <p>
	 * When a collection (in-built or user-defined) is searched for a matching
	 * rule, first is sought a match for the error itself. If no match is found
	 * for the error and the error has a specified cause (<code>getCause()</code>
	 * returns non-null) then a match for the cause is searched. Only when both
	 * searched fail, another collection of rules is considered.
	 * </p>
	 * 
	 * @param connector
	 *            the connector, which raised the error
	 * @param error
	 *            the error object
	 * @return the prescribed action - it is one of the following:
	 *         <code>ERROR</code> or <code>RECONNECT</code>
	 * 
	 */
	public int getReconnectChoice(ConnectorInterface connector, Throwable error) {

		Class connectorClass = connector.getClass();
		String connectorName = connector.getName();

		// First search a matching rule for the error within the user defined
		// rules
		ReconnectRule rule = findMatchingRuleForErrorChain(userDefinedRules,
				connectorClass, connectorName, error);

		List<ReconnectRule> connectorRules = null;
		if (rule == null) {
			connectorRules = getReconnectRules(connector);
		}

		// If no matching rule is found, then search a matching rule for the
		// error within the Connector rules
		if (rule == null) {
			rule = findMatchingRuleForErrorChain(connectorRules,
					connectorClass, connectorName, error);
		}

		if (rule != null) {
			log.debug(
					"MISERVER.RECONNECTENGINE.RECONNECT.CHOICE.RULE.SELECTED",
					new Object[] { connectorClass, connectorName, error,
							rule.connectorClass, rule.connectorName,
							rule.exceptionClass, rule.exceptionMessageRegExp,
							reconnectActionToText(rule.getAction()) });
		} else {
			log.debug("MISERVER.RECONNECTENGINE.RECONNECT.CHOICE.NO.RULE",
					new Object[] { connectorClass, connectorName, error,
							reconnectActionToText(defaultAction) });
		}

		return (rule != null) ? rule.getAction() : defaultAction;
	}
	
	/**
	 * Finds the most specific matching rule for an error situation. Returns
	 * null if no rule is found. The whole error chain
	 * (java.lang.Throwable.getCause()) is considered during the matching. The
	 * next error in the chain is considered only if no matching rule is found
	 * for the current error.
	 * 
	 * @param rules
	 *            A list of rules against which the error will be matched.
	 * @param connectorClass
	 *            The class of the Connector that threw the error.
	 * @param connectorName
	 *            The name of the Connector that threw the error.
	 * @param error
	 *            The error object.
	 * @return The first rule from the list that matches the error. Null if no
	 *         rule matches the error.
	 */
	private ReconnectRule findMatchingRuleForErrorChain(
			List<ReconnectRule> rules, Class connectorClass,
			String connectorName, Throwable error) {

		ReconnectRule rule = null;
		Throwable t = error;
		int depth = 0;

		while (rule == null && t != null && depth < MAX_ERROR_CHAIN_DEPTH) {

			rule = findMatchingRule(rules, connectorClass, connectorName, t);
			t = t.getCause();
			++depth;
		}

		return rule;
	}

	/**
	 * Finds the most specific matching rule for an error situation. Returns
	 * null if no rule is found.
	 * 
	 * @param rules
	 *            a list of rules in which to search for a match.
	 * @param connectorClass
	 *            the class of the connector involved.
	 * @param connectorName
	 *            the name of the connector involved.
	 * @param error
	 *            the error situation occurred.
	 * @return a specific matching rule.
	 */
	private ReconnectRule findMatchingRule(List<ReconnectRule> rules,
			Class connectorClass, String connectorName, Throwable error) {

		ReconnectRule mostSpecificMatchingRule = null;

		for (Iterator<ReconnectRule> it = rules.iterator(); it.hasNext();) {

			ReconnectRule rule = it.next();

			if (rule.matchErrorSituation(connectorClass, connectorName, error)
					&& (mostSpecificMatchingRule == null || mostSpecificMatchingRule
							.isGeneralizationOf(rule))) {

				mostSpecificMatchingRule = rule;
			}
		}

		return mostSpecificMatchingRule;
	}

	/**
	 * Parses the specified file to a list of reconnect rules. The file must be
	 * UTF-8 encoded. When a problem rule is encountered in the file, an error
	 * message is logged but parsing continues until all rules are parsed. When
	 * parsing finishes, an Exception is thrown if there were any problem lines.
	 * 
	 * @param rules
	 *            the list that will hold the rules parsed.
	 * @param filePath
	 *            the path to the UTF-8 file containing the rules.
	 * @throws Exception
	 *             if an error during the parsing occurs.
	 */
	private void parseUserDefinedReconnectRules(List<ReconnectRule> rules,
			String filePath) throws Exception {

		File rulesFile = new File(filePath);
		if (!rulesFile.isFile()) {
			return;
		}

		BufferedReader input = new BufferedReader(new InputStreamReader(
				new FileInputStream(filePath), "UTF-8"));

		String line = null;

		int errorCount = 0;

		while ((line = input.readLine()) != null) {

			// left trim
			int firstNonWhiteSpace = -1;
			for (int i = 0; i < line.length(); ++i) {
				if (line.charAt(i) != ' ' && line.charAt(i) != '\t') {
					firstNonWhiteSpace = i;
					break;
				}
			}
			if (firstNonWhiteSpace != -1) {
				line = line.substring(firstNonWhiteSpace);
			} else {
				line = "";
			}

			// if not empty and not a comment, parse the reconnect rule out of
			// the line and add the rule to the list
			if (line.length() > 0 && !line.startsWith("#")) {
				try {
					ReconnectRule rule = parseSingleUserDefinedReconnectRule(line);
					rules.add(rule);
				} catch (Exception ex) {
					log.error("MISERVER.RECONNECTENGINE.ERROR.IN.RULE", ex
							.toString());
					++errorCount;
				}
			}
		}

		input.close();

		// errors found during parsing - raise an error
		if (errorCount > 0) {
			throw new Exception(sResHash.getString(
					"MISERVER.RECONNECTENGINE.ERRORS.IN.USERDEFINED.RULES",
					Integer.valueOf(errorCount)));
		}
	}

	/**
	 * Construct a reconnect rule out of a non-comment line of text. The rule
	 * parts must be in the following order : connector class , connector name ,
	 * exception class , action , regular expression. The parts are delimited by
	 * one RECONNECT_RULE_PARTS_DELIMETER. All whitespace is considered
	 * belonging to a corresponding rule part - no whitespace is trimmed.
	 * 
	 * @param line
	 *            the used for the rule construction.
	 * @return the constructed rule.
	 * @throws Exception
	 *             is a problem occurs.
	 * 
	 */
	private ReconnectRule parseSingleUserDefinedReconnectRule(String line)
			throws Exception {

		Class connectorClass = null;
		String connectorName = null;
		Class exceptionClass = null;
		int action = INVALID_RECONNECT_ACTION;
		String regularExpression = null;

		// read connector class name
		int delimAfterConnectorClass = line
				.indexOf(RECONNECT_RULE_PARTS_DELIMETER);
		if (delimAfterConnectorClass != -1) {
			String connectorClassName = line.substring(0,
					delimAfterConnectorClass).trim();

			connectorClass = (connectorClassName.length() > 0) ? Class
					.forName(connectorClassName)
					: ReconnectRule.CONNECTOR_CLASS_MATCH_ALL;
		} else {
			throw new Exception(sResHash.getString(
					"MISERVER.RECONNECTENGINE.CONNECTOR.CLASS.MISSING", line));
		}

		// read connector name
		int delimAfterConnectorName = line.indexOf(
				RECONNECT_RULE_PARTS_DELIMETER, delimAfterConnectorClass + 1);
		if (delimAfterConnectorName != -1) {
			connectorName = line.substring(delimAfterConnectorClass + 1,
					delimAfterConnectorName).trim();
			if (connectorName.length() == 0) {
				connectorName = ReconnectRule.CONNECTOR_NAME_MATCH_ALL;
			}
		} else {
			throw new Exception(sResHash.getString(
					"MISERVER.RECONNECTENGINE.CONNECTOR.NAME.MISSING", line));
		}

		// read exception class name
		int delimAfterExceptionClass = line.indexOf(
				RECONNECT_RULE_PARTS_DELIMETER, delimAfterConnectorName + 1);
		if (delimAfterExceptionClass != -1) {
			String exceptionClassName = line.substring(
					delimAfterConnectorName + 1, delimAfterExceptionClass)
					.trim();

			exceptionClass = (exceptionClassName.length() > 0) ? Class
					.forName(exceptionClassName)
					: ReconnectRule.EXCEPTION_CLASS_MATCH_ALL;
		} else {
			throw new Exception(sResHash.getString(
					"MISERVER.RECONNECTENGINE.EXCEPTION.CLASS.MISSING", line));
		}

		// read action name
		int delimAfterAction = line.indexOf(RECONNECT_RULE_PARTS_DELIMETER,
				delimAfterExceptionClass + 1);
		if (delimAfterAction != -1) {
			String actionName = line.substring(delimAfterExceptionClass + 1,
					delimAfterAction).trim();
			action = reconnectActionFromText(actionName);
			if (action == INVALID_RECONNECT_ACTION) {

				// cannot recognize action text
				throw new Exception(sResHash.getString(
						"MISERVER.RECONNECTENGINE.INVALID.ACTION", line));
			}
		} else {
			throw new Exception(sResHash.getString(
					"MISERVER.RECONNECTENGINE.ACTION.MISSING", line));
		}

		regularExpression = line.substring(delimAfterAction + 1);
		if (regularExpression.length() == 0) {
			regularExpression = ReconnectRule.EXCEPTION_MESSAGE_REGEXP_MATCH_ALL;
		}

		return new ReconnectRule(connectorClass, connectorName, exceptionClass,
				regularExpression, action);
	}

	/**
	 * Convert the given reconnect action to a string.
	 * 
	 * @param action
	 *            the reconnect action.
	 * @return an action string corresponding to the received action.
	 */
	private static String reconnectActionToText(int action) {

		String actionString = null;

		switch (action) {
		case ReconnectRuleEngine.ERROR:
			actionString = ERROR_STRING;
			break;
		case ReconnectRuleEngine.RECONNECT:
			actionString = RECONNECT_STRING;
			break;
		default:
			// unknown reconnect action
			break;
		}

		return actionString;
	}

	/**
	 * Suggests suitable reconnect action based on the action string received.
	 * 
	 * @param actionString
	 *            the action string received.
	 * @return a reconnect action depending on the string received.
	 */
	private static int reconnectActionFromText(String actionString) {

		if (ERROR_STRING.equalsIgnoreCase(actionString)) {
			return ReconnectRuleEngine.ERROR;
		}

		if (RECONNECT_STRING.equalsIgnoreCase(actionString)) {
			return ReconnectRuleEngine.RECONNECT;
		}

		// reconnect action string not recognized
		return INVALID_RECONNECT_ACTION;
	}

	/**
	 * Dumps all rules from a collection with rules to the engine's log. The
	 * logged text has a specified title line (to eventually identify the
	 * collection of rules in the log file).
	 * 
	 * @param rules
	 *            a collection of rules to be logged.
	 * @param titleMessageID
	 *            a specific title for a collection of rules.
	 */
	private void dumpRules(List<ReconnectRule> rules, String titleMessageID) {
		log
				.debug("-----------------------------------------------------------------");
		log.debug(titleMessageID);

		for (Iterator<ReconnectRule> it = rules.iterator(); it.hasNext();) {

			ReconnectRule rule = it.next();
			log.debug("MISERVER.RECONNECTENGINE.RECONNECT.RULE", new Object[] {
					rule.connectorClass, rule.connectorName,
					rule.exceptionClass, rule.exceptionMessageRegExp,
					reconnectActionToText(rule.getAction()) });
		}

		log
				.debug("-----------------------------------------------------------------");
	}

	/**
	 * Get a list of reconnect rules, which corresponds to the Connector
	 * configuration. If a rule in the configuration references a not existing
	 * class, that rule will be silently skipped.
	 * 
	 * @param connector
	 *            A Connector.
	 * @return List of the Connector's reconnect rules.
	 */
	private static List<ReconnectRule> getReconnectRules(
			ConnectorInterface connector) {

		List<ReconnectRule> reconnectRules = new ArrayList<ReconnectRule>();

		ConnectorConfig cc = (ConnectorConfig) connector.getConfiguration();
		ContainerConfig ccReconnectRules = cc.getReconnectConfig()
				.getReconnectRules();

		List allRules = ccReconnectRules.getConfigurations(null);
		ccReconnectRules.getInheritedConfigurations(allRules);

		for (Iterator i = allRules.iterator(); i.hasNext();) {

			ReconnectRuleConfig config = (ReconnectRuleConfig) i.next();

			int action = reconnectActionFromText(config.getAction());
			if (action == INVALID_RECONNECT_ACTION) {
				action = ERROR;
			}

			Class exClass;
			try {
				exClass = Class.forName(config.getExceptionClass());
			} catch (ClassNotFoundException ignore) {
				exClass = null;
			}

			if (exClass != null) {

				ReconnectRule rule = new ReconnectRule(connector.getClass(),
						connector.getName(), exClass, config
								.getExceptionMessageRegExp(), action);
				reconnectRules.add(rule);
			}/*
				 * else skip the rule, if the exception class is not available
				 * in the JVM
				 */
		}

		return reconnectRules;
	}

}
