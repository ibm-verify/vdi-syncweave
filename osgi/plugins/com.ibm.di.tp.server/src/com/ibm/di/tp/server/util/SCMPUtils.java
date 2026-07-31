/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.util;

import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;
import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.common.model.atom.AtomEntry;
import org.w3c.dom.Element;

import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.ServerActivator;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.TouchpointType;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;
import com.ibm.di.util.DOMUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class SCMPUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	/**
	 * @param id
	 */
	public static String escapeId(String id) {
		return id == null ? null : id.replaceAll("[\\W&&[^\\.]]", "_");
	}

	public static String getUniqueKey(Set<String> keys, String escapedId) {
		int idx = 0;
		String tempId = escapedId;

		while (keys.contains(tempId)) {
			tempId = escapedId + idx;
		}

		return tempId;
	}

	public static Element createConnectivityProviderElement(String type, String location, String organization, String contact) {
		Element data = DOMUtils.doc.createElementNS(Constants.NS_SCMP, "scmp:data");
		data.setAttributeNS(Constants.NS_SCHEMA_INSTANCE, "xsi:schemaLocation",
				"http://www.ibm.com/xmlns/prod/scmp tdi-connectivity-provider.xsd");

		Element cp = DOMUtils.doc.createElementNS(Constants.NS_SCMP, "scmp:connectivity-provider");
		data.appendChild(cp);

		Element temp = DOMUtils.doc.createElementNS(Constants.NS_SCMP, "scmp:type");
		temp.appendChild(DOMUtils.doc.createTextNode(type));
		cp.appendChild(temp);

		if (location != null) {
			Element t = DOMUtils.doc.createElementNS(Constants.NS_SCMP, "scmp:location");
			t.appendChild(DOMUtils.doc.createTextNode(location));
			cp.appendChild(t);
		}

		if (organization != null) {
			Element t = DOMUtils.doc.createElementNS(Constants.NS_SCMP, "scmp:organization");
			t.appendChild(DOMUtils.doc.createTextNode(organization));
			cp.appendChild(t);
		}

		if (contact != null) {
			Element t = DOMUtils.doc.createElementNS(Constants.NS_SCMP, "scmp:contact");
			t.appendChild(DOMUtils.doc.createTextNode(contact));
			cp.appendChild(t);
		}

		return data;
	}

	/**
	 * Finds the configuration data element in the provided list of elements.
	 * 
	 * @param any
	 * @return
	 */
	public static Element getDataElement(List<Element> any) throws SCMPException {
		for (Element elem : any) {
			if ("data".equals(elem.getLocalName()) && Constants.NS_SCMP.equals(elem.getNamespaceURI())) {
				return elem;
			}
		}

		SCMPException e = new SCMPException(ErrorCode.GENERIC_MISSING_DATA, ServerActivator.L10N.getString(
				"TP.SERVER.RESOURCE.MISSING.CONFIG.DATA"), Response.Status.EXPECTATION_FAILED.getStatusCode());
		e.setDetail("qname", "{" + Constants.NS_SCMP + "}data");
		throw e;
	}

	/**
	 * Finds the tp role category in the provided {@link AtomEntry} and maps it
	 * to the {@link TouchpointRole} values. Note this method also validates the
	 * number of categories in an entry.
	 * 
	 * @param payload
	 *            the {@link AtomEntry} to search for tp role categories
	 * @param tt
	 *            the context Touchpoint Type to check if it supports the found
	 *            role.
	 * @return the {@link TouchpointRole} representation of the tp role category
	 *         found.
	 * @throws SCMPException
	 *             in one of the following cases:
	 *             <ul>
	 *             <li>No roles are found in the provided entry</li>
	 *             <li>More than one roles are found in the provided entry</li>
	 *             <li>The context {@link TouchpointType} does not support the
	 *             found role</li>
	 *             </ul>
	 * @throws Exception
	 *             if there is a problem when checking the supported TP roles.
	 */
	public static TouchpointRole getTPRole(AtomEntry payload, TouchpointType tt) throws SCMPException {
		TouchpointRole tr = null;
		// get the role
		int trCount = 0;
		AtomCategory lastCat = null;
		for (AtomCategory cat : payload.getCategories()) {
			if (Constants.SCHEME_TP_ROLE.equals(cat.getScheme())) {
				trCount++;
				if (Constants.CAT_ROLE_INITIATOR.getTerm().equals(cat.getTerm())) {
					tr = TouchpointRole.INITIATOR;
				} else if (Constants.CAT_ROLE_INTERMEDIARY.getTerm().equals(cat.getTerm())) {
					tr = TouchpointRole.INTERMEDIARY;
				} else if (Constants.CAT_ROLE_PROVIDER.getTerm().equals(cat.getTerm())) {
					tr = TouchpointRole.PROVIDER;
				}
				lastCat = cat;
			}
		}

		SCMPException e = null;
		if (trCount == 0) {
			e = new SCMPException(ErrorCode.GENERIC_MISSING_CATEGORY, ServerActivator.L10N
					.getString("TP.SERVER.RESOURCE.MISSING.TP.ROLE"), Response.Status.EXPECTATION_FAILED.getStatusCode());
			e.setDetail("schema", Constants.SCHEME_TP_ROLE);
		} else if (trCount > 1) {
			e = new SCMPException(ErrorCode.GENERIC_TOO_MANY_CATEGORIES, ServerActivator.L10N
					.getString("TP.SERVER.RESOURCE.MORE.THAT.ONE.TP.ROLE.PROVIDED"), Response.Status.EXPECTATION_FAILED.getStatusCode());
			e.setDetail("schema", Constants.SCHEME_TP_ROLE);
		} else if (!tt.getSupportedRoles().contains(tr)) {
			e = new SCMPException(ErrorCode.GENERIC_INVALID_CATEGORY, ServerActivator.L10N.getString(
					"TP.SERVER.RESOURCE.UNSUPPORTED.TP.ROLE", new Object[] { tt.getId(), tr.toString() }),
					Response.Status.EXPECTATION_FAILED.getStatusCode());
			e.setDetail("schema", Constants.SCHEME_TP_ROLE);
			e.setDetail("term", lastCat.getTerm());
		}

		if (e != null) {
			throw e;
		}

		return tr;
	}
}
