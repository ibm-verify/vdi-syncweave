/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import com.ibm.di.config.interfaces.*;
import java.util.*;

/**
 * Implementation of one item in a {@link FormConfigImpl} 
 *
 */
public class FormItemConfigImpl extends BaseConfigurationImpl implements
		FormItemConfig {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -7825109041707716857L;

	private static final String EXPRESSION = "expression";
	private static final String READONLY = "readonly";
	private static final String DONTPROTECT = "dontprotect";
	private static final String COMPONENT = "component";
	private static final String REFLECT = "reflect";
	private static final String MINVALUE = "minValue";
	private static final String MAXVALUE = "maxValue";
	private static final String INDEXBASED = "indexbased";
	private static final String SIZE = "size";
	private static final String HELP = "help";
	private static final String REQUIRED = "Required";
	private static final String LEAD_TEXT= "LeadText";
	private static final String MODES = "modes";

	private FormConfig form = null;

	public FormItemConfigImpl() {
		super();
	}

	public FormItemConfigImpl(Object config) {
		super(config);
	}

	public void setForm(FormConfig form) {
		this.form = form;
	}

	public FormConfig getForm() {
		return form;
	}

	@SuppressWarnings("unchecked")
	public List<String> getValues() {
		Object obj = getParameter(InternalSchema.FORM_VALUES, new Vector<String>());
		if(obj instanceof String) {
			Vector<String> v = new Vector<String>();
			v.add((String)obj);
			setParameter(InternalSchema.FORM_VALUES, v, false);
			return v;
		}

		if ( obj instanceof List)
			return (List) obj;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public List<String> getLocalizedValues() {
		Object o = getParameter(InternalSchema.FORM_LOCALIZEDVALUES);
		if (o instanceof Map)
			return mapTranslate((Map<String,String>) o);

		TreeMap<String,String> t = new TreeMap<String,String>();
		if (o instanceof List) {
			List<String> list = (List) o;
			for (String s:list) {
				int i = s.indexOf(':');
				if (i > 0)
					t.put(s.substring(0, i), s.substring(i + 1));
			}
		}

		// Save the parsed values
		setParameter(InternalSchema.FORM_LOCALIZEDVALUES, t);
		return mapTranslate(t);
	}

	@SuppressWarnings("unchecked")
	public void setLocalizedValues(Map map) {
		setParameter(InternalSchema.FORM_LOCALIZEDVALUES, map);
	}

	@SuppressWarnings("unchecked")
	public void setValues(Vector values) {
		setParameter(InternalSchema.FORM_VALUES, values);
	}

	public String getSyntax() {
		return getStringParameter(InternalSchema.FORM_SYNTAX);
	}

	public void setSyntax(String str) {
		setStringParameter(InternalSchema.FORM_SYNTAX, str);
	}

	public String getLabel() {
		return translate(getStringParameter(InternalSchema.FORM_LABEL));
	}

	public void setLabel(String str) {
		setStringParameter(InternalSchema.FORM_LABEL, str);
	}

	public String getToolTip() {
		return translate(getStringParameter(InternalSchema.FORM_TOOLTIP));
	}

	public void setToolTip(String str) {
		setStringParameter(InternalSchema.FORM_TOOLTIP, str);
	}

	public String getDefaultValue() {
		String translateDefault = getStringParameter("translatedefault");
		if ( translateDefault != null )
			return translate(translateDefault);

		String defval = getStringParameter(InternalSchema.FORM_DEFAULT_VALUE);
		if (defval != null && "static".equalsIgnoreCase(getSyntax()) )
			return translate(defval);
		return defval;
	}

	public void setDefaultValue(String str) {
		setStringParameter(InternalSchema.FORM_DEFAULT_VALUE, str);
	}

	public String getScript() {
		return getStringParameter(InternalSchema.FORM_SCRIPT);
	}

	public void setScript(String str) {
		setStringParameter(InternalSchema.FORM_SCRIPT, str);
	}

	public String getScriptLabel() {
		return translate(getStringParameter(InternalSchema.FORM_SCRIPT_LABEL));
	}

	public void setScriptLabel(String str) {
		setStringParameter(InternalSchema.FORM_SCRIPT_LABEL, str);
	}

	public String getScriptToolTip() {
		return translate(getStringParameter(InternalSchema.FORM_SCRIPT_TOOLTIP));
	}

	public void setScriptToolTip(String str) {
		setStringParameter(InternalSchema.FORM_SCRIPT_TOOLTIP, str);
	}

	public String getScript2() {
		return getStringParameter(InternalSchema.FORM_SCRIPT + "2");
	}

	public void setScript2(String str) {
		setStringParameter(InternalSchema.FORM_SCRIPT + "2", str );
	}

	public String getScriptLabel2() {
		return translate(getStringParameter(InternalSchema.FORM_SCRIPT_LABEL + "2"));
	}

	public void setScriptLabel2(String str) {
		setStringParameter(InternalSchema.FORM_SCRIPT_LABEL + "2", str);
	}

	public String getScriptToolTip2() {
		return translate(getStringParameter(InternalSchema.FORM_SCRIPT_TOOLTIP + "2"));
	}

	public void setScriptToolTip2(String str) {
		setStringParameter(InternalSchema.FORM_SCRIPT_TOOLTIP + "2", str);
	}

	public boolean isExpression() {
		return getBooleanParameter(EXPRESSION, false);
	}

	public boolean isReadOnly() {
		return getBooleanParameter(READONLY, false);
	}

	public boolean isIndexBased() {
		return getBooleanParameter(INDEXBASED, false);
	}

	public void setIndexBased( boolean value) {
		setBooleanParameter(INDEXBASED, value);
	}
	
	public boolean getDontProtect() {
		return getBooleanParameter(DONTPROTECT, false);
	}

	public String getComponentClass() {
		return getStringParameter(COMPONENT);
	}

	public String getReflect() {
		return getStringParameter(REFLECT);
	}

	public int getMinValue() {
		return getIntegerParameter(MINVALUE, Integer.MIN_VALUE);
	}

	public int getMaxValue() {
		return getIntegerParameter(MAXVALUE, Integer.MAX_VALUE);
	}

	public Object get(String name) {
		return getParameter(name);
	}

	public int getSize() {
		return getIntegerParameter(SIZE, 0);
	}

	public boolean isHelp() {
		return hasParameter(HELP);
	}

	public boolean isRequired() {
		return getBooleanParameter(REQUIRED, false);
	}

	public void setRequired( boolean value ) {
		setBooleanParameter(REQUIRED, value);
	}

	public String getLeadText() {
		return getStringParameter(LEAD_TEXT);
	}

	public void setLeadText(String text) {
		setParameter(LEAD_TEXT, text);
	}

	/**
	 * Translate all values using the Map and the form's translate method 
	 */
	private List<String> mapTranslate( Map<String,String> map ) {
		List<String> values = getValues();
		if ( values == null )
			return null;

		List<String> ret = new ArrayList<String>(values.size());

		for (String s:values) {
			String m = map.get(s);
			if ( m!=null ) 
				ret.add( translate(m) );
			else 
				ret.add( translate(s) );
		}
		return ret;
	}

	/**
	 * Translate a String using the form's translate method 
	 */
	private String translate( String str ) {
		if ( form == null )
			return str;
		return form.translate( str );
	}

	public boolean isValidForMode(String mode) {
		String validModes = getStringParameter(MODES);
		if (validModes == null || validModes.length() == 0)
			return true;

		boolean minus = false; // Set to true if one of the tokens starts with minus
		StringTokenizer modes = new StringTokenizer(validModes, ",");
		while (modes.hasMoreTokens()) {
			String s=modes.nextToken().trim();
			if (s.startsWith("-")) {
				minus = true;
				s = s.substring(1);
			}
			if (s.equals(mode))
				return !minus;			
		}
		return minus;
	}

}
