/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.ci.ps;

import java.rmi.RemoteException;
import java.util.Iterator;

import com.ibm.di.web.common.atom.AtomText;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.Properties;
import com.ibm.di.api.bind.Property;
import com.ibm.di.api.remote.ConfigInstance;
import com.ibm.di.api.remote.TDIProperties;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.entry.Entry;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class PropertyStoreEntry {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private final ConfigInstance ci;
	private String storeName;
	private TDIPropertyStore ps;

	public PropertyStoreEntry(ConfigInstance ci, String storeName) {
		this.ci = ci;
		this.storeName = storeName;
	}

	public PropertyStoreEntry(ConfigInstance ci, TDIPropertyStore ps) {
		this.ci = ci;
		this.ps = ps;
	}

	@GET
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public AtomEntry getSelfAsJson(@Context UriInfo uri) throws Exception {
		return getSelf(uri.getAbsolutePath().toString(), false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public AtomEntry getSelfAsXml(@Context UriInfo uri) throws Exception {
		return getSelf(uri.getAbsolutePath().toString(), true);
	}

	public AtomEntry getSelf(String absUrl, boolean isXml) throws Exception {
		AtomEntry e = new AtomEntry();
		e.setId(absUrl);
		e.setUpdated(System.currentTimeMillis());
		e.setTitle(createAtomText(getPsName()));

		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(e.getId());
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_RES_PROPERTY_STORE);

		TDIProperties props = ci.getTDIProperties();
		TDIPropertyStore def = props.getDefaultStore();
		TDIPropertyStore pass = props.getPasswordStore();

		if (def != null && getPS().getName().equals(def.getName())) {
			e.getCategories().add(AppConstants.CAT_PROPERTY_STORE_DEFAULT);
		}

		if (pass != null && getPS().getName().equals(pass.getName())) {
			e.getCategories().add(AppConstants.CAT_PROPERTY_STORE_PASSWORD);
		}

		if (getPS().isModified()) {
			e.getCategories().add(AppConstants.CAT_PROPERTY_STORE_MODIFIED);
		}

		l = new AtomLink();
		l.setRel(AppConstants.REL_PROPERTIES);
		l.setType(isXml ? AppConstants.MT_PROPERTY_STORE_XML : AppConstants.OBJ_JSON_Properties);
		l.setHref(absUrl + "/props");
		e.getLinks().add(l);

		return e;
	}

	private TDIPropertyStore getPS() throws Exception {
		if (ps == null) {
			ps = ci.getTDIProperties().getPropertyStore(storeName);
			if (ps == null) {
				throw new DIException(AppConstants.L10N.getString("REST.API.OBJECT.UNAVAILABLE"));
			}
		}
		return ps;
	}

	private String getPsName() {
		if (storeName == null) {
			storeName = ps.getName();
		}
		return storeName;
	}

	@PUT
	@Consumes( { AppConstants.OBJ_JSON_AtomEntry, MediaType.APPLICATION_ATOM_XML })
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public Response updateStoreAsJson(@Context UriInfo uri, AtomEntry store) throws RemoteException, Exception {
		return updateStore(uri, store, false);
	}

	@PUT
	@Consumes( { AppConstants.OBJ_JSON_AtomEntry, MediaType.APPLICATION_ATOM_XML })
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response updateStoreAsXml(@Context UriInfo uri, AtomEntry store) throws RemoteException, Exception {
		return updateStore(uri, store, true);
	}

	private Response updateStore(UriInfo uri, AtomEntry store, boolean isXml) throws Exception, RemoteException {
		boolean isDefault = hasCategory(store, AppConstants.CAT_PROPERTY_STORE_DEFAULT);
		boolean isPassword = hasCategory(store, AppConstants.CAT_PROPERTY_STORE_PASSWORD);

		if (isDefault || isPassword) {
			TDIProperties props = ci.getTDIProperties();
			TDIPropertyStore def = null;
			TDIPropertyStore pass = null;

			if (isDefault && (def = props.getDefaultStore()) != null && !getPS().getName().equals(def.getName())) {
				props.setDefaultStore(getPS());
			}

			if (isPassword && (pass = props.getPasswordStore()) != null && !getPS().getName().equals(pass.getName())) {
				props.setPasswordStore(getPS());
			}
		}

		return Response.ok(getSelf(uri.getAbsolutePath().toString(), isXml)).build();
	}

	private boolean hasCategory(AtomEntry e, AtomCategory cat) {
		for (AtomCategory c : e.getCategories()) {
			if (cat.getTerm().equals(c.getTerm()) && cat.getScheme().equals(c.getScheme())) {
				return true;
			}
		}
		return false;
	}

	@GET
	@Path("props")
	@Produces( { AppConstants.OBJ_JSON_Properties, AppConstants.MT_PROPERTY_STORE_XML })
	@SuppressWarnings("unchecked")
	public Response getProperties(@QueryParam("name") String name) throws Exception {
		Response res;
		if (name == null) {
			Properties props = new Properties();
			Iterator es = getPS().entries();
			while (es.hasNext()) {
				Entry e = (Entry) es.next();
				Property p = new Property();
				p.setName(e.getString(com.ibm.di.config.interfaces.TDIProperties.KEY_ATTRIBUTE));
				p.setValue(e.getString(com.ibm.di.config.interfaces.TDIProperties.VALUE_ATTRIBUTE));
				p.setEncrypt(Boolean.parseBoolean(e.getString(com.ibm.di.config.interfaces.TDIProperties.PROTECT_ATTRIBUTE)));

				props.getProperties().add(p);
			}
			res = Response.ok(props).build();
		} else {
			Entry e = getPS().getPropertyEntry(name);
			Property p = e != null ? new Property() : null;

			if (p != null) {
				p.setName(e.getString(com.ibm.di.config.interfaces.TDIProperties.KEY_ATTRIBUTE));
				p.setValue(e.getString(com.ibm.di.config.interfaces.TDIProperties.VALUE_ATTRIBUTE));
				p.setEncrypt(Boolean.parseBoolean(e.getString(com.ibm.di.config.interfaces.TDIProperties.PROTECT_ATTRIBUTE)));
			}
			res = p == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(p).build();
		}

		return res;
	}

	@PUT
	@Path("props")
	@Consumes( { AppConstants.OBJ_JSON_Properties, AppConstants.MT_PROPERTY_STORE_XML })
	public Response updateProperties(Properties props) throws Exception {

		for (Property p : props.getProperties()) {
			if (p.getName() != null) {
				if (p.getValue() == null) {
					// remove property
					getPS().removeProperty(p.getName());
				} else {
					getPS().setProperty(p.getName(), p.getValue(), p.isEncrypt());
				}
			}
		}
		if (props.isCommit()) {
			getPS().commit();
		}

		return Response.noContent().build();
	}

	@PUT
	@Path("props")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response updateProperty(String body, @QueryParam("name") String name, @QueryParam("commit") boolean commit,
			@QueryParam("encrypt") boolean encrypt) throws Exception {

		if (name == null) {
			throw new IllegalArgumentException(AppConstants.L10N.getString("REST.API.PS.NAME.MISSING"));
		}

		getPS().setProperty(name, body, encrypt);

		if (commit) {
			getPS().commit();
		}
		return Response.noContent().build();
	}

	@DELETE
	@Path("props")
	public Response deleteProperty(@QueryParam("name") String name, @QueryParam("commit") boolean commit) throws Exception {
		if (name == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		getPS().removeProperty(name);

		if (commit) {
			getPS().commit();
		}
		return Response.noContent().build();
	}

	@DELETE
	public Response delete() throws RemoteException, Exception {
		ci.getTDIProperties().removePropertyStore(storeName != null ? storeName : getPS().getName());
		return Response.ok().build();
	}

	/**
	 * Helper method to create AtomText with TEXT type.
	 */
	private AtomText createAtomText(String value) {
		AtomText text = new AtomText();
		text.setType("text");
		text.setValue(value);
		return text;
	}

}
