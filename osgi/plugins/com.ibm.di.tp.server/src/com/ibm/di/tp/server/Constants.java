/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.common.model.synd.SyndCategory;

import com.ibm.di.tp.server.model.config.Label;

/**
 * A utility class containing all the constants used throughout the application. <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public final class Constants {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * The NLS properties file containing the TP Server labels.
	 */
	private static final String TP_SERVER_LABELS = "NLS/idi_tp_server";

	/** XSI namespace URI */
	public static final String NS_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";

	/** SCMP Namespace URI */
	public static final String NS_SCMP = "http://www.ibm.com/xmlns/prod/scmp";

	/** Atom Syndication Namespace URI */
	public static final String NS_ATOM = "http://www.w3.org/2005/Atom";

	/** Atom Publishing Namespace URI */
	public static final String NS_APP = "http://www.w3.org/2007/app";

	/** The namespace of the configuration elements used by TDI */
	public static final String NS_TDI_71_TP = "http://www.ibm.com/xmlns/prod/tdi/71/tp";

	/** The Resource category schema */
	public static final String SCHEME_RESOURCE = "http://www.ibm.com/xmlns/prod/scmp#resource";

	/** The Aspect category schema */
	public static final String SCHEMA_ASPECT = "http://www.ibm.com/xmlns/prod/scmp#aspect";

	public static final String SCHEME_TP_ROLE = "http://www.ibm.com/xmlns/prod/scmp#touchpoint-role";

	public static final String SCHEME_TP_TYPE = "http://www.ibm.com/xmlns/prod/scmp#touchpoint-type";

	/** The "tp-node" feed category object */
	public static final AtomCategory CAT_CONN_PROVIDER = new AtomCategory();
	static {
		CAT_CONN_PROVIDER.setTerm("connectivity-provider");
		CAT_CONN_PROVIDER.setScheme(SCHEME_RESOURCE);
		CAT_CONN_PROVIDER.setLabel("Connectivity Provider Role");
	}
	public static final SyndCategory CAT_CONN_PROVIDER_SYND = new SyndCategory();
	static {
		CAT_CONN_PROVIDER_SYND.setTerm(CAT_CONN_PROVIDER.getTerm());
		CAT_CONN_PROVIDER_SYND.setScheme(CAT_CONN_PROVIDER.getScheme());
		CAT_CONN_PROVIDER_SYND.setLabel(CAT_CONN_PROVIDER.getLabel());
	}

	/** The "tp-type" feed category object */
	public static final AtomCategory CAT_TOUCHPOINT = new AtomCategory();
	static {
		CAT_TOUCHPOINT.setTerm("touchpoint");
		CAT_TOUCHPOINT.setScheme(SCHEME_RESOURCE);
	}
	public static final SyndCategory CAT_TOUCHPOINT_SYND = new SyndCategory();
	static {
		CAT_TOUCHPOINT_SYND.setTerm(CAT_TOUCHPOINT.getTerm());
		CAT_TOUCHPOINT_SYND.setScheme(CAT_TOUCHPOINT.getScheme());
	}

	/** The "tp-type" entry category object */
	public static final AtomCategory CAT_RES_TYPE_ENTRY = new AtomCategory();
	static {
		CAT_RES_TYPE_ENTRY.setTerm("resource-type");
		CAT_RES_TYPE_ENTRY.setScheme(SCHEMA_ASPECT);
	}
	public static final SyndCategory CAT_RES_TYPE_ENTRY_SYND = new SyndCategory();
	static {
		CAT_RES_TYPE_ENTRY_SYND.setTerm(CAT_RES_TYPE_ENTRY.getTerm());
		CAT_RES_TYPE_ENTRY_SYND.setScheme(CAT_RES_TYPE_ENTRY.getScheme());
	}

	/** The "status" entry category object */
	public static final AtomCategory CAT_STATUS_ENTRY = new AtomCategory();
	static {
		CAT_STATUS_ENTRY.setTerm("status");
		CAT_STATUS_ENTRY.setScheme(SCHEMA_ASPECT);
	}
	public static final SyndCategory CAT_STATUS_ENTRY_SYND = new SyndCategory();
	static {
		CAT_STATUS_ENTRY_SYND.setTerm(CAT_STATUS_ENTRY.getTerm());
		CAT_STATUS_ENTRY_SYND.setScheme(CAT_STATUS_ENTRY.getScheme());
	}

	/** The "status" entry category object */
	public static final AtomCategory CAT_DESTINATION_ENTRY = new AtomCategory();
	static {
		CAT_DESTINATION_ENTRY.setTerm("tp-destination");
		CAT_DESTINATION_ENTRY.setScheme(SCHEME_RESOURCE);
	}
	public static final SyndCategory CAT_DESTINATION_ENTRY_SYND = new SyndCategory();
	static {
		CAT_DESTINATION_ENTRY_SYND.setTerm(CAT_DESTINATION_ENTRY.getTerm());
		CAT_DESTINATION_ENTRY_SYND.setScheme(CAT_DESTINATION_ENTRY.getScheme());
	}

	/** The "initiator-tp" role for a TP */
	public static final AtomCategory CAT_ROLE_INITIATOR = new AtomCategory();
	static {
		CAT_ROLE_INITIATOR.setTerm("initiator-tp");
		CAT_ROLE_INITIATOR.setScheme(SCHEME_TP_ROLE);
	}

	/** The "intermediary-tp" role for a TP */
	public static final AtomCategory CAT_ROLE_INTERMEDIARY = new AtomCategory();
	static {
		CAT_ROLE_INTERMEDIARY.setTerm("intermediary-tp");
		CAT_ROLE_INTERMEDIARY.setScheme(SCHEME_TP_ROLE);
	}

	/** The "provider-tp" role for a TP */
	public static final AtomCategory CAT_ROLE_PROVIDER = new AtomCategory();
	static {
		CAT_ROLE_PROVIDER.setTerm("provider-tp");
		CAT_ROLE_PROVIDER.setScheme(SCHEME_TP_ROLE);
	}

	public static final String TYPE_APPLICATION_ATOM_XML_ENTRY = MediaType.APPLICATION_ATOM_XML + ";type=entry";

	public static final String TYPE_APPLICATION_ATOM_XML_FEED = MediaType.APPLICATION_ATOM_XML + ";type=feed";

	public static final String CONNECTIVITY_PROVIDER_TDI_TYPE = "tdi";

	public static final String REL_TOUCHPOINT = "http://www.ibm.com/xmlns/prod/scmp#touchpoint";

	public static final String REL_INSTANCE_FEED = "http://www.ibm.com/xmlns/prod/scmp#instance-feed";

	public static final String REL_DESTINATION_FEED = "http://www.ibm.com/xmlns/prod/scmp#tp-destination";

	public static final String REL_RESOURCE_TYPE = "http://www.ibm.com/xmlns/prod/scmp#resource-type";

	public static final String REL_STATUS = "http://www.ibm.com/xmlns/prod/scmp#status";

	public static final String REL_SELF = "self";

	public static final String REL_EDIT = "edit";

	public static final String REL_PROPSHEET_DEF = "http://www.ibm.com/xmlns/prod/scmp#property-sheet-definition";

	/**
	 * An immutable map between the supported by the TPServer {@link Locale}s
	 * and their corresponding string representations (in the syntax specified
	 * by RFC 1766).
	 */
	public static final Map<Locale, String> SUPPORTED_LOCALES;
	static {
		Map<Locale, String> map = new HashMap<Locale, String>();
		map.put(Locale.ENGLISH, "en");
		map.put(Locale.GERMAN, "de");
		map.put(new Locale("es"), "es");
		map.put(Locale.FRENCH, "fr");
		map.put(Locale.ITALIAN, "it");
		map.put(Locale.JAPANESE, "ja");
		map.put(Locale.KOREAN, "ko");
		map.put(new Locale("pt", "BR"), "pt-BR");
		map.put(Locale.SIMPLIFIED_CHINESE, "zh-CN");
		map.put(Locale.TRADITIONAL_CHINESE, "zh-TW");

		SUPPORTED_LOCALES = Collections.unmodifiableMap(map);
	}

	/**
	 * The prefix used before system properties.
	 */
	public static final String SYSTEM_PROPERTY_PREFIX = "$";

	/**
	 * A special property used for specifying the Connector mode.
	 */
	public static final String PROP_INIT_MODE = SYSTEM_PROPERTY_PREFIX + "initMode";

	/**
	 * The key for the {@link #PROP_INIT_MODE} label in the NLS properties file.
	 */
	public static final String PROP_INIT_MODE_LABEL = "TP_SERVER_CONN_INIT_MODE";

	/**
	 * This is the contextDir which TP Server is registering with in the
	 * com.ibm.di.schema bundle.
	 */
	public static final String SCHEMA_CONTEXT_DIR = "tp";

	/**
	 * An immutable list containing the {@link Label}s for the
	 * {@link #PROP_INIT_MODE} parameter.
	 */
	public static final List<Label> LABELS_INIT_MODE;

	static {
		List<Label> temp = new LinkedList<Label>();
		for (Entry<Locale, String> loc : SUPPORTED_LOCALES.entrySet()) {
			try {
				temp.add(new Label(ResourceBundle.getBundle(TP_SERVER_LABELS, loc.getKey()).getString(PROP_INIT_MODE_LABEL), loc
						.getValue()));
			} catch (MissingResourceException m) {
				// when running outside the standard context (e.g. when unit
				// testing...) the resource might not be available.
				temp.add(new Label(PROP_INIT_MODE_LABEL, loc.getValue()));
			}
		}
		LABELS_INIT_MODE = Collections.unmodifiableList(temp);
	}

	private Constants() {
	}
}
