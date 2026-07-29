/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// SearchCriteria.java
//
//
//
package com.ibm.di.server;

import java.util.Vector;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.script.ScriptEngine;
import com.ibm.di.util.ParameterSubstitutionCache;
import com.ibm.jscript.IValue;
import com.ibm.jscript.InterpretException;

/**
 * This class contains a list of generic search criteria and methods to generate
 * them. The search criteria is e.g. attr equals value. The class also has a
 * number of methods to convert the generic criteria to standard expressions
 * like LDAP search filters, SQL select statements etc.
 * <p>
 * The class also provides a means to define templates which can be expanded
 * using a com.ibm.di.entry.Entry object. The expanded templates can then be
 * accessed through the criteria methods.
 * <p>
 * The typical use of this class is to:
 * <ul>
 * <li>Create an instance of this class
 * <li>Populate the templates array (addTemplate method)
 * </ul>
 *
 * Then for every entry you can use the <i>buildCriteria</i> method to expand
 * the templates into the criteria array and use the getLDAPFilter etc to
 * construct a search filter usable with specific connectors. The connectors
 * themselves will use the get<i>XX</i>Filter methods to obtain the search
 * filter in order to search for entries. However, if you write your own
 * connector or for some other reason need to get hold of the expanded filters
 * you can always use the get<i>XX</i>>Filter methods. These methods only
 * build a string from the criteria array so no changes are done to the class
 * variables.
 * <p>
 * All get<i>XXX</i>Filter methods first check if the class was created with a
 * user defined script to return the search string. Thus, if you create an
 * instance of this class with a script/ScriptEngine and use for example the
 * LDAP connector, the search filter used by the LDAP connector will be the
 * result of the script evaluation rather than the getLDAPFilter method.
 * <p>
 * Here is an example Before Lookup Hook which displays all Link Criteria set.
 * <p>
 * <b>Example :</b>
 *
 * <pre>
 * var criteria = search.getCriteria();
 * var matchstr;
 *
 * task.logmsg(&quot;Link Criteria set: &quot;);
 * for (i = 0; i &lt; criteria.size(); i++) {
 * 	var crit = criteria.get(i);
 *
 *  if (crit.match == com.ibm.di.server.SearchCriteria.EXACT) {
 * 		matchstr = &quot;equals&quot;;
 * 	} else if (crit.match == com.ibm.di.server.SearchCriteria.LESS_THAN) {
 * 		matchstr = &quot;less than&quot;;
 * 	} else if (crit.match == com.ibm.di.server.SearchCriteria.LESS_THAN_OR_EQUAL) {
 * 		matchstr = &quot;less than or equal&quot;;
 * 	} else if (crit.match == com.ibm.di.server.SearchCriteria.GREATER_THAN) {
 * 		matchstr = &quot;greater than&quot;;
 * 	} else if (crit.match == com.ibm.di.server.SearchCriteria.GREATER_THAN_OR_EQUAL) {
 * 		matchstr = &quot;greater than or equal&quot;;
 * 	} else if (crit.match == com.ibm.di.server.SearchCriteria.SUBSTRING) {
 * 		matchstr = &quot;contains&quot;;
 * 	} else if (crit.match == com.ibm.di.server.SearchCriteria.INITIAL_STRING) {
 * 		matchstr = &quot;starts with&quot;;
 * 	} else if (crit.match == com.ibm.di.server.SearchCriteria.FINAL_STRING) {
 * 		matchstr = &quot;end with&quot;;
 * 	} else if (crit.match == com.ibm.di.server.SearchCriteria.NOT_STRING) {
 * 		matchstr = &quot;not equals&quot;;
 * 	}
 *
 * 	main.logmsg(&quot;name: &quot; + crit.name + &quot;\nmatch: &quot; + matchstr + &quot;\nvalue: &quot;
 * 			+ crit.value);
 * }
 * </pre>
 *
 * Instead of using the constant com.ibm.di.server.SearchCriteria.EXACT the value
 * 61 (the character code for '=') could have been used. Here is a list of the constants used for the match operator,
 * and their values:
 * <pre>
 *	EXACT = 61
 *	LESS_THAN = 60
 *	LESS_THAN_OR_EQUAL = 8804
 *	GREATER_THAN = 62
 *	GREATER_THAN_OR_EQUAL = 8805
 *	SUBSTRING = 126
 *	INITIAL_STRING = 94
 *	FINAL_STRING = 36
 *	NOT_STRING = 33
 * </pre>
 *
 */

public class SearchCriteria {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * This is the character that represents the binary operation equals.
	 */
	public final static int EXACT = LinkCriteriaItem.EXACT;

	/**
	 * Misspelling, kept for backwards compatibility
	 * @deprecated
	 */
	public final static int EXCACT = '=';

	/**
	 * This is the character that represents the binary operator that tells that
	 * the right operand should be contained somewhere in the left operand.
	 */
	public final static int SUBSTRING = LinkCriteriaItem.SUBSTRING;

	/**
	 * This is the character that represents the binary operator that tells that
	 * the left operand should start with the value of the right operand.
	 */
	public final static int INITIAL_STRING = LinkCriteriaItem.INITIAL_STRING;

	/**
	 * This is the character that represents the binary operator that tells that
	 * the left operand should end with the value of the right operand.
	 */
	public final static int FINAL_STRING = LinkCriteriaItem.FINAL_STRING;

	/**
	 * This is the character that represents the binary operator that tells that
	 * both operands should be different from each other.
	 */
	public final static int NOT_STRING = LinkCriteriaItem.NOT_STRING;

	/**
	 * The binary operator less than.
	 * @since 7.0
	 */
	public final static int LESS_THAN = LinkCriteriaItem.LESS_THAN;

	/**
	 * The binary operator less than or equal.
	 * @since 7.0
	 */
	public final static int LESS_THAN_OR_EQUAL = LinkCriteriaItem.LESS_THAN_OR_EQUAL;

	/**
	 * The binary operator greater than.
	 * @since 7.0
	 */
	public final static int GREATER_THAN = LinkCriteriaItem.GREATER_THAN;

	/**
	 * The binary operator greater than or equal.
	 * @since 7.0
	 */
	public final static int GREATER_THAN_OR_EQUAL = LinkCriteriaItem.GREATER_THAN_OR_EQUAL;

	/**
	 * This is the character that represents the binary operator AND.
	 */
	public final static int SEARCH_AND = LinkCriteriaItem.SEARCH_AND;

	/**
	 * This is the character that represents the binary operator OR.
	 */
	public final static int SEARCH_OR = LinkCriteriaItem.SEARCH_OR;

	/**
	 * This is the character that represents the unary operator NOT.
	 */
	public final static int SEARCH_NOT = LinkCriteriaItem.SEARCH_NOT;

	private int matchtype;

	private Vector<Object> criteria = new Vector<Object>();

	private Vector<rscSearch> template = new Vector<rscSearch>();

	private Entry currentEntry = null;

	private String criteriaScript;

	private ScriptEngine engine;

	public String filter;

	public ParameterSubstitutionCache psc = new ParameterSubstitutionCache();

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash res = ResourceHash.getHash(PROPERTIES_FILE);

	private Entry scriptObject = null;

	/**
	 * This class is used internally. <br>
	 * This class is a simple Java Bean object that represents the built search
	 * criteria or a template to build it.
	 */
	public static class rscSearch {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * The attribute name.
		 */
		public String name;

		/**
		 * This is the operator character:<br>
		 *
		 * @see SearchCriteria#EXACT
		 * @see SearchCriteria#INITIAL_STRING
		 * @see SearchCriteria#FINAL_STRING
		 * @see SearchCriteria#NOT_STRING
		 * @see SearchCriteria#LESS_THAN
		 * @see SearchCriteria#LESS_THAN_OR_EQUAL
		 * @see SearchCriteria#GREATER_THAN
		 * @see SearchCriteria#GREATER_THAN_OR_EQUAL
		 */
		public int match;

		/**
		 * This is the other attribute or its expanded value(s).
		 */
		public Object value;

		/**
		 * Tells whether the expression should be negated.
		 */
		public boolean negate;

		/**
		 * Creates s simple object of type {@link rscSearch}<br>
		 *
		 * @param name
		 *            is the attribute name
		 * @param match
		 *            is the operator.
		 * @param value
		 *            is the other attribute or its expanded value(s).
		 */
		public rscSearch(String name, int match, Object value) {
			this.name = name;
			this.match = match;
			this.value = value;
			this.negate = false;
		}
	}

	/**
	 * Default constructor for creating an object of type {@link SearchCriteria}.
	 * <br>
	 * The match type by default is set to {@link SearchCriteria#SEARCH_AND}
	 *
	 * @see #SearchCriteria(int)
	 */
	public SearchCriteria() {
		matchtype = SEARCH_AND;
	}

	/**
	 * Creates an object of type {@link SearchCriteria}.
	 *
	 * @param matchtype -
	 *            this is one of the following: <br>
	 *            {@link SearchCriteria#SEARCH_AND} <br>
	 *            {@link SearchCriteria#SEARCH_OR} <br>
	 */
	public SearchCriteria(int matchtype) {
		this.matchtype = matchtype;
	}

	/**
	 * Creates an object of type {@link SearchCriteria} and adds an initial
	 * expression to criteria array.
	 *
	 * @param name
	 *            is the attribute name
	 * @param match
	 *            is the match operator
	 * @param value
	 *            is the matching value.
	 */
	public SearchCriteria(String name, int match, Object value) {
		addCriteria(name, match, value);
		matchtype = SEARCH_AND;
	}

	/**
	 * Creates an object of type {@link SearchCriteria} and prepares the object
	 * for interpreting a Criteria script.
	 *
	 * @param criteriaScript
	 *            is the script string.
	 * @param engine
	 *            is the ScriptEngine used for interpreting the script.
	 *
	 * @see #buildCriteriaScript(Entry)
	 */
	public SearchCriteria(String criteriaScript, ScriptEngine engine) {
		this.criteriaScript = (criteriaScript == null) ? "" : criteriaScript;
		this.engine = engine;
	}

	/**
	 * Return number of templates.
	 *
	 * @return Number of entries in the templates array
	 */
	public int size() {
		if (template != null)
			return template.size();
		else
			return 0;
	}

	/**
	 * Return criteria class at specified index.
	 *
	 * @param index
	 *            the position of the rscSearc object in the array.
	 * @return The rscSearch class at the specified index
	 */
	public rscSearch getCriteria(int index) {
		return (rscSearch) criteria.elementAt(index);
	}

	/**
	 * This method exposes the Vector object used for storing the criteria
	 * objects (usually of type {@link SearchCriteria.rscSearch}) which are
	 * result of the building the criteria templates. <br>
	 * <b>Note:</b> Some of the elements in the Vector could be of type
	 * {@link SearchCriteria}
	 *
	 * @see #buildCriteria(Entry)
	 * @return The Vector object in which the {@link SearchCriteria} object
	 *         stores the built criteria objects. <br>
	 *         <b> Note:</b> This is not an immutable object! On the next call
	 *         of the {@link #buildCriteria(Entry)} method all of its values
	 *         will be removed from the Vector.
	 */
	public Vector<?> getCriteria() {
		return criteria;
	}

	/**
	 * Add expression to criteria array.
	 *
	 * @param name
	 *            The attribute name
	 * @param match
	 *            The match operator (e.g. EXACT, FINAL_STRING ..)
	 * @param value
	 *            The matching value.
	 */
	public void addCriteria(String name, int match, Object value) {
		rscSearch rs = new rscSearch(name, match, value);
		criteria.addElement(rs);
	}

	/**
	 * Add expression to criteria array.
	 *
	 * @param name
	 *            The attribute name
	 * @param match
	 *            The match operator (e.g. EXACT, FINAL_STRING ..)
	 * @param value
	 *            The matching value.
	 * @param negate
	 *            Specify true to negate the expression
	 */
	public void addCriteria(String name, int match, Object value, boolean negate) {
		rscSearch rs = new rscSearch(name, match, value);
		rs.negate = negate;
		criteria.addElement(rs);
	}

	/**
	 * Add expression to criteria array, replacing all previous occurrences with
	 * the same attribute name. The match operator will always be EXACT.
	 *
	 * @param name
	 *            The attribute name
	 * @param value
	 *            The matching value.
	 */
	public void replaceCriteria(String name, Object value) {
		for (int i = criteria.size() - 1; i >= 0; i--) {
			Object obj = criteria.get(i);

			if (!(obj instanceof rscSearch))
				continue;

			if (((rscSearch) obj).name.equalsIgnoreCase(name))
				criteria.remove(i);
		}

		criteria.addElement(new rscSearch(name, EXACT, value));
	}

	/**
	 * Return the name of the first criteria entry.
	 *
	 * @return The name contained in the first criteria entry
	 */
	public String getFirstCriteriaName() {
		if (criteria.size() < 1)
			return null;

		Object obj = criteria.get(0);
		if (obj instanceof rscSearch)
			return ((rscSearch) obj).name;

		if (obj instanceof SearchCriteria)
			return ((SearchCriteria) obj).getFirstCriteriaName();

		return null;
	}

	/**
	 * Return the value of the first criteria entry.
	 *
	 * @return The value contained in the first criteria entry
	 */
	public String getFirstCriteriaValue() {
		if (criteria.size() < 1)
			return null;

		Object obj = criteria.get(0);
		if (obj instanceof rscSearch)
			return ((rscSearch) obj).value.toString();

		if (obj instanceof SearchCriteria)
			return ((SearchCriteria) obj).getFirstCriteriaValue();

		return null;
	}

	/**
	 * Return the match operator of the first criteria entry.
	 *
	 * @return The match operator contained in the first criteria entry
	 */
	public int getFirstCriteriaMatch() {
		if (criteria.size() < 1)
			return 0;

		Object obj = criteria.get(0);
		if (obj instanceof rscSearch)
			return ((rscSearch) obj).match;

		if (obj instanceof SearchCriteria)
			return ((SearchCriteria) obj).getFirstCriteriaMatch();

		return 0;
	}

	/**
	 * Set type (SEARCH_AND, SEARCH_OR) for the search criteria. This governs
	 * how multiple criteria objects are constructed.
	 *
	 * @param type
	 *            SEARCH_AND, SEARCH_OR
	 */
	public void setType(int type) {
		matchtype = type;
	}

	/**
	 * Returns the search type.
	 *
	 * @see #setType(int)
	 *
	 * @return SEARCH_AND or SEARCH_OR
	 */
	public int getType() {
		return matchtype;
	}

	/**
	 * Returns the criteria template at a specific location.
	 *
	 * @param index
	 *            The index into the templates array
	 * @return The {@link SearchCriteria.rscSearch} class at location index
	 */
	public rscSearch getTemplate(int index) {
		return template.elementAt(index);
	}

	/**
	 * Add a criteria to the templates array.
	 *
	 * @param name
	 *            The attribute name
	 * @param match
	 *            The match operator (e.g. EXACT, FINAL_STRING ..)
	 * @param defvalue
	 *            The value to match
	 */
	public void addTemplate(String name, int match, String defvalue) {
		rscSearch rs = new rscSearch(name, match, defvalue);
		template.addElement(rs);
	}

	/**
	 * Add a criteria to the templates array.
	 *
	 * @param name
	 *            The attribute name
	 * @param match
	 *            The match operator (e.g. EXACT, FINAL_STRING ..)
	 * @param defvalue
	 *            The value to match
	 * @param negate
	 *            Specify true to negate the expression
	 * @since 7.2
	 */
	public void addTemplate(String name, int match, String defvalue, boolean negate) {
		rscSearch rs = new rscSearch(name, match, defvalue);
		rs.negate = negate;
		template.addElement(rs);
	}

	/**
	 * Add a criteria to the templates array.
	 *
	 * @param name
	 *            The attribute name
	 * @param match
	 *            The match operator as a String, e.g. '=','~', '^', '$' or '!'.
	 * @param defvalue
	 *            The value to match
	 */
	public void addTemplate(String name, String match, String defvalue) {
		addTemplate(name, match.charAt(0), defvalue);
	}

	/**
	 * Format the LDAP search string according to RFC 2254
	 *
	 * @param str
	 *            The String containing the LDAP search string.
	 */
	private String formatLDAPFilter(String str) {
		StringBuffer tmpstr = new StringBuffer();
		String shrtstr = "";
		for (int i = 0; i < str.length(); i++) {
			shrtstr = str.substring(i, i + 1);
			switch ((int) shrtstr.charAt(0)) {
			case 40: // System.out.println(" FOUND '('");
				shrtstr = "\\28";
				break;
			case 41: // System.out.println(" FOUND ')'");
				shrtstr = "\\29";
				break;
			case 92: // System.out.println(" FOUND '\\'");
				shrtstr = "\\5c";
				break;
			case 0: // System.out.println(" FOUND NUL");
				shrtstr = "\\00";
				break;
			}
			tmpstr.append(shrtstr);
		}
		return tmpstr.toString();
	}

	/**
	 * Constructs an LDAP search filter from an {@link SearchCriteria.rscSearch}
	 * class.
	 *
	 * @param rs
	 *            the {@link SearchCriteria.rscSearch} object which properties
	 *            to use during the filter construction process.
	 *
	 * @return The LDAP search filter
	 */
	public String buildLdapFilter(rscSearch rs) {
		String val;
		if (rs.value instanceof byte[])
			val = UserFunctions.encodeToHexstring((byte[]) rs.value);
		else if (rs.value != null)
			val = formatLDAPFilter(rs.value.toString());
		else
			val = "";

		switch (rs.match) {
		case EXACT:
			return "(" + rs.name + "=" + val + ")";
		case LESS_THAN:
			return "(!(" + rs.name + ">=" + val + "))";
		case LESS_THAN_OR_EQUAL:
			return "(" + rs.name + "<=" + val + ")";
		case GREATER_THAN:
			return "(!(" + rs.name + "<=" + val + "))";
		case GREATER_THAN_OR_EQUAL:
			return "(" + rs.name + ">=" + val + ")";
		case SUBSTRING:
			return "(" + rs.name + "=*" + val + "*)";
		case INITIAL_STRING:
			return "(" + rs.name + "=" + val + "*)";
		case FINAL_STRING:
			return "(" + rs.name + "=*" + val + ")";
		case NOT_STRING:
			return "(!(" + rs.name + "=" + val + "))";
		}
		return "";
	}

	/**
	 * Constructs an SQL where expression from an
	 * {@link SearchCriteria.rscSearch} class.
	 *
	 * @param rs
	 *            the {@link SearchCriteria.rscSearch} object which properties
	 *            to use during the filter construction process.
	 *
	 * @return The SQL where expression
	 */
	public String buildSQLFilter(rscSearch rs) {
		Trace.entrymax(this, "buildSQLFilter", rs);
		String neg = "";
		if (rs.negate)
			neg = " NOT ";

		switch (rs.match) {
		case EXACT:
			Trace.exitmax(this, "buildSQLFilter");
			return (neg + rs.name + "='" + rs.value + "'");
		case LESS_THAN:
			Trace.exitmax(this, "buildSQLFilter");
			return (neg + rs.name + "<'" + rs.value + "'");
		case LESS_THAN_OR_EQUAL:
			Trace.exitmax(this, "buildSQLFilter");
			return (neg + rs.name + "<='" + rs.value + "'");
		case GREATER_THAN:
			Trace.exitmax(this, "buildSQLFilter");
			return (neg + rs.name + ">'" + rs.value + "'");
		case GREATER_THAN_OR_EQUAL:
			Trace.exitmax(this, "buildSQLFilter");
			return (neg + rs.name + ">='" + rs.value + "'");
		case SUBSTRING:
			Trace.exitmax(this, "buildSQLFilter");
			return (rs.name + neg + " LIKE '%" + rs.value + "%'");
		case INITIAL_STRING:
			Trace.exitmax(this, "buildSQLFilter");
			return (rs.name + neg + " LIKE '" + rs.value + "%'");
		case FINAL_STRING:
			Trace.exitmax(this, "buildSQLFilter");
			return (rs.name + neg + " LIKE '%" + rs.value + "'");
		case NOT_STRING:
			Trace.exitmax(this, "buildSQLFilter");
			return ("NOT " + rs.name + " = '" + rs.value + "'");
		}
		return "";
	}

	/**
	 * Constructs an Notes search filter from an
	 * {@link SearchCriteria.rscSearch} class.
	 *
	 * @param rs
	 *            the {@link SearchCriteria.rscSearch} object which properties
	 *            to use during the filter construction process.
	 *
	 * @return The Notes search filter
	 */
	public String buildNotesFilter(rscSearch rs) {
		Trace.entrymax(this, "buildNotesFilter", rs);
		// System.out.println ("buildNotesFilter: " + rs.name + "/" + rs.value);

		String value = (rs.value == null) ? "" : rs.value.toString();

		switch (rs.match) {
		case EXACT:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + "=\"" + value + "\"");
		case LESS_THAN:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + "<\"" + value + "\"");
		case LESS_THAN_OR_EQUAL:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + "<=\"" + value + "\"");
		case GREATER_THAN:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + ">\"" + value + "\"");
		case GREATER_THAN_OR_EQUAL:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + ">=\"" + value + "\"");
		case SUBSTRING:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + "=\"*" + value + "*\"");
		case INITIAL_STRING:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + "=\"" + value + "*\"");
		case FINAL_STRING:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + "=\"*" + value + "\"");
		case NOT_STRING:
			Trace.exitmax(this, "buildNotesFilter");
			return (rs.name + "!=\"" + value + "\"");
		}
		Trace.exitmax(this, "buildNotesFilter");
		return "";
	}

	/**
	 * Constructs an Notes FullText search filter from an
	 * {@link SearchCriteria.rscSearch} class.
	 *
	 * @param rs
	 *            the {@link SearchCriteria.rscSearch} object which properties
	 *            to use during the filter construction process.
	 *
	 * @return The Notes FullText search filter
	 */
	public String buildNotesFTFilter(rscSearch rs) {
		Trace.entrymax(this, "buildNotesFTFilter");
		switch (rs.match) {
		case EXACT:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("[" + rs.name + "] = \"" + rs.value + "\"");
		case LESS_THAN:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("[" + rs.name + "] < \"" + rs.value + "\"");
		case LESS_THAN_OR_EQUAL:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("[" + rs.name + "] <= \"" + rs.value + "\"");
		case GREATER_THAN:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("[" + rs.name + "] > \"" + rs.value + "\"");
		case GREATER_THAN_OR_EQUAL:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("[" + rs.name + "] >= \"" + rs.value + "\"");
		case SUBSTRING:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("[" + rs.name + "] CONTAINS \"" + rs.value + "\"");
		case INITIAL_STRING:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("[" + rs.name + "] = \"" + rs.value + "*\"");
		case FINAL_STRING:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("[" + rs.name + "] = \"*" + rs.value + "\"");
		case NOT_STRING:
			Trace.exitmax(this, "buildNotesFTFilter");
			return ("NOT [" + rs.name + "] = \"" + rs.value + "\"");
		}
		Trace.exitmax(this, "buildNotesFTFilter");
		return "";
	}

	/**
	 * Return complete LDAP filter from criteria array.
	 *
	 * @return The complete LDAP filter from the values in the criteria array.
	 */
	public String getLDAPFilter() {
		Trace.entrymax(this, "getLDAPFilter");
		if (criteriaScript != null) {
			Trace.exitmax(this, "getLDAPFilter");
			return getScriptFilter();
		}

		StringBuffer flt = new StringBuffer();

		if (criteria.size() > 1) {
			flt.append("(");
			flt.append(matchtype == SEARCH_AND ? "&" : "|");
		}

		for (int i = 0; i < criteria.size(); i++) {

			Object obj = criteria.get(i);
			if (obj instanceof SearchCriteria)
				flt.append(((SearchCriteria) obj).getLDAPFilter());
			if (obj instanceof rscSearch)
				flt.append(buildLdapFilter((rscSearch) obj));

		}

		if (criteria.size() > 1) {
			flt.append(")");
		}

		Trace.exitmax(this, "getLDAPFilter");
		return flt.toString();
	}

	/**
	 * Return complete SQL search expression from criteria array.
	 *
	 * @return The complete SQL search expression from the values in the
	 *         criteria array.
	 */
	public String getSQLFilter() {
		Trace.entrymax(this, "getSQLFilter");
		StringBuffer flt = new StringBuffer();
		Object obj;

		if (criteriaScript != null) {
			Trace.exitmax(this, "getSQLFilter");
			return getScriptFilter();
		}
		for (int i = 0; i < criteria.size(); i++) {

			if (flt.length() > 0)
				flt.append((matchtype == SEARCH_AND ? " AND " : " OR "));

			obj = criteria.get(i);

			if (obj instanceof rscSearch) {
				flt.append(buildSQLFilter((rscSearch) obj));
			} else if (obj instanceof SearchCriteria) {
				flt.append("( ");
				flt.append(((SearchCriteria) obj).getSQLFilter());
				flt.append(" )");
			} else {
				Trace.exitmax(this, "getSQLFilter");
				return (res
						.getString("MISERVER.SEARCHCRITERIA.HIERARCHICAL.SEARCH.CRITERIA.NOT.SUPPORTED.FOR.SQL"));
			}

		}
		Trace.exitmax(this, "getSQLFilter");
		return flt.toString();
	}

	/**
	 *
	 * Get a string representation of the Criteria array.
	 *
	 * @return a simple string representation of the search criteria objects in
	 *         the criteria array, or an error message if all of the objects in
	 *         the criteria array are not of type
	 *         {@link SearchCriteria.rscSearch}.
	 */
	public String getSimpleFilter() {
		Trace.entrymax(this, "getSimpleFilter");

		if (criteriaScript != null) {
			Trace.exitmax(this, "getSimpleFilter");
			return getScriptFilter();
		}

		StringBuffer flt = new StringBuffer();

		for (int i = 0; i < criteria.size(); i++) {
			Object obj = criteria.get(i);
			rscSearch rs;

			if (obj instanceof rscSearch) {
				rs = (rscSearch) obj;
			} else {
				Trace.exitmax(this, "getSimpleFilter");
				return (res
						.getString("MISERVER.SEARCHCRITERIA.HIERARCHICAL.SEARCH.CRITERIA.NOT.SUPPORTED.FOR.SIMPLEFILTER"));
			}

			flt.append((matchtype == SEARCH_AND ? "AND:" : "OR:"));
			flt.append(rs.name);
			flt.append(":");
			flt.append(rs.value);
			flt.append("\n");
		}
		Trace.exitmax(this, "getSimpleFilter");
		return flt.toString();
	}

	/**
	 * Return complete Notes search expression from criteria array.
	 *
	 * @return The complete Notes search expression from the values in the
	 *         criteria array.
	 */
	public String getNotesFilter() {
		Trace.entrymax(this, "getNotesFilter");

		if (criteriaScript != null) {
			Trace.exitmax(this, "getNotesFilter");
			return getScriptFilter();
		}

		StringBuffer flt = new StringBuffer();

		for (int i = 0; i < criteria.size(); i++) {
			if (flt.length() > 0)
				flt.append((matchtype == SEARCH_AND ? " & " : " | "));

			Object obj = criteria.get(i);

			if (obj instanceof rscSearch) {
				flt.append(buildNotesFilter((rscSearch) obj));
			} else {
				Trace.exitmax(this, "getNotesFilter");
				return (res
						.getString("MISERVER.SEARCHCRITERIA.HIERARCHICAL.SEARCH.CRITERIA.NOT.SUPPORTED.FOR.NOTES"));
			}
		}
		Trace.exitmax(this, "getNotesFilter");
		return flt.toString();
	}

	/**
	 * Return complete Notes FullText search expression from criteria array.
	 *
	 * @return The complete Notes FullText search expression from the values in
	 *         the criteria array.
	 */
	public String getNotesFTFilter() {
		Trace.entrymax(this, "getNotesFTFilter");

		if (criteriaScript != null) {
			Trace.exitmax(this, "getNotesFTFilter");
			return getScriptFilter();
		}

		StringBuffer flt = new StringBuffer();

		for (int i = 0; i < criteria.size(); i++) {
			if (flt.length() > 0)
				flt.append((matchtype == SEARCH_AND ? " & " : " | "));

			Object obj = criteria.get(i);

			if (obj instanceof rscSearch) {
				flt.append(buildNotesFTFilter((rscSearch) obj));
			} else {
				Trace.exitmax(this, "getNotesFTFilter");
				return ("Hierarchical search criteria not supported for Notes");
			}
		}
		Trace.exitmax(this, "getNotesFTFilter");
		return flt.toString();
	}

	/**
	 * Get the string representation of the search criteria in the criteria
	 * array.<br>
	 *
	 * <b>Note: </b>This method will throw an exception of type
	 * {@link ClassCastException} if the criteria array contains objects of type
	 * other than {@link SearchCriteria.rscSearch}. <br>
	 *
	 * @param i
	 *            the position of the {@link SearchCriteria.rscSearch} object in
	 *            the criteria array.
	 * @return the string representation of the search criteria object or
	 *         <code>null</code> if the specified index is out of the criteria
	 *         array bounds.
	 */
	public String getSearchString(int i) {
		if (i >= criteria.size() || i < 0)
			return null;
		rscSearch rs = (rscSearch) criteria.get(i);
		return rs.name + "\t" + (char) rs.match + "\t" + rs.value;
	}

	/**
	 * Return the working entry used to generate the criteria array from the
	 * templates array. <br>
	 * This is the entry passed to the {@link #buildCriteria(Entry)} method.
	 *
	 * @return The working entry
	 * @see com.ibm.di.entry.Entry
	 * @see #buildCriteria(Entry)
	 */
	public Entry getCurrentEntry() {
		return currentEntry;
	}

	/**
	 * Build the criteria array using provided entry. This method uses the
	 * templates array to generate the criteria array. Each criteria in the
	 * template array holds an attribute name, a matching operator and a value.
	 * The value, if prefixed by a dollar sign, causes this method to substitute
	 * the value with the corresponding attribute value in the work entry. The
	 * attribute name is derived from the value by removing the first dollar
	 * sign using the remainder as the attribute name. For example, "$test"
	 * becomes "test".
	 * <p>
	 * When the method completes the criteria array has been built so it
	 * contains a copy of the templates array with expanded values.
	 * <p>
	 * You typically create an instance of this class and then add the
	 * templates. Then for each entry you are using in a search you can call the
	 * buildCriteria method to populate the criteria array. Then, having done
	 * that you can use the getLDAPFilter, getNotesFilter etc to obtain the
	 * search filter as a string.
	 *
	 * <br>
	 * This method reuses the functionality of the
	 * {@link #buildCriteria(Entry, BaseConfiguration, Object)} method.
	 *
	 * @param e
	 *            The work entry used in parameter substitution.
	 *
	 * @throws InterpretException
	 *             If the provided script could not be interpreted correctly.
	 * @throws Exception
	 *             If the current {@link SearchCriteria} object does not have
	 *             any templates to build from.
	 */

	public void buildCriteria(Entry e) throws Exception {
		buildCriteria(e, null, null);
	}

	/**
	 *
	 * @see #buildCriteria(Entry) for general information. <br>
	 *      This method will also check if the user have provided a script
	 *      string and will attempt to interpret it instead of building any
	 *      templates.
	 * @param e
	 *            the work entry used in parameter substitution.
	 * @param config
	 *            the component's config object, could be null.
	 * @param task
	 *            usually a reference to the {@link AssemblyLine} instance
	 *            calling this method, could be null.
	 * @throws InterpretException
	 *             If the provided script could not be interpreted correctly.
	 * @throws Exception
	 *             If the current {@link SearchCriteria} object does not have
	 *             any templates to build from.
	 */
	public void buildCriteria(Entry e, BaseConfiguration config, Object task)
			throws Exception {
		Trace.entrymax(this, "buildCriteria");
		criteria.clear();

		currentEntry = e;

		if (criteriaScript != null) {
			engine.declareBean(AttributeMapping.SCRIPT_OBJECT,
					getScriptObject(config, task));
			buildCriteriaScript(e);
			Trace.exitmax(this, "buildCriteria");
			return;
		}

		for (int i = 0; i < template.size(); i++) {
			rscSearch s = template.get(i);

			String name = s.name;
			if (ParameterSubstitutionCache.isExpression(name)) {
				psc.put("config", config);
				if (task instanceof AssemblyLine)
					psc.put("op-entry", ((AssemblyLine) task).getOpEntry());
				name = psc.substitute(name, "work", e);
			}

			String value = s.value.toString();
			Object o;
			char fc = value.length() > 0 ? value.charAt(0) : 0;

			if (fc == '$' || fc == '@') {
				Attribute a = e.getAttribute(value.substring(1));
				if (a == null || a.size() < 1)
					continue;

				if (fc == '@' && a.size() > 1) {
					SearchCriteria rsc = new SearchCriteria();
					rsc.setType(SEARCH_OR);
					for (int j = 0; j < a.size(); j++) {
						rsc.addCriteria(name, s.match, a.getValue(j));
					}
					criteria.addElement(rsc);
					continue;
				}

				o = a.getValue(0);
			} else if (ParameterSubstitutionCache.isExpression(value)) {
				psc.put("config", config);
				psc.put("task", task);
				o = psc.substitute(value, "work", e);
			} else {
				o = value;
			}

			criteria.addElement(new rscSearch(name, s.match, o));
		}

		if (criteria.size() == 0 && template.size() > 0) {
			throw new Exception(res.getString("no.criteria.can.be.built"));
		}
		Trace.exitmax(this, "buildCriteria");
	}

	/**
	 * Call user defined script to build the search filter. The filter has
	 * access to the <i>entry</i> object as a scriptable object called <i>work</i>.
	 * The class instance (this) is available as <i>ret</i>. The script is
	 * expected at some point to assign a value to the <i>ret.filter</i>
	 * property. The value of that property will be returned to the caller.
	 *
	 * @param entry
	 *            The work entry used in parameter substitution
	 * @throws InterpretException
	 *             If the provided script could not be interpreted correctly.
	 */

	public void buildCriteriaScript(Entry entry) throws Exception {

		engine.declareBean("work", entry);
		engine.declareBean("ret", this);
		filter = "";

		IValue value = engine.interpret(criteriaScript, false);
		if ("".equals(filter) && value != null) {
			Object o = value.toJavaObject();
			if ( o!= null )
				filter = o.toString();
		}
	}

	/**
	 * This method executes a script that returns a user-defined search filter.
	 *
	 * @return The search filter
	 */

	public String getScriptFilter() {
		return this.filter;
	}

	/**
	 * Check if the object has some kind of configuration set.
	 *
	 * @return true if the current {@link SearchCriteria} object has templates
	 *         to build from or has a script string to interpret, returns false
	 *         otherwise.
	 */
	public boolean isConfigured() {
		return (size() > 0 || criteriaScript != null);
	}

	private Entry getScriptObject(BaseConfiguration config, Object task) {
		if (scriptObject == null) {
			scriptObject = new Entry();
			if (config != null)
				scriptObject.setAttribute("Component", config.getShortName());
			if (task instanceof AssemblyLine)
				scriptObject.setAttribute("AssemblyLine", ((AssemblyLine)task).getName());
		}
		return scriptObject;
	}
}
