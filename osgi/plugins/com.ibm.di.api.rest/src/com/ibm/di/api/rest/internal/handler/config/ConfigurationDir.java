/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.handler.config;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.di.web.common.atom.AtomText;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import javax.ws.rs.Path;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomFeed;
import com.ibm.di.web.common.atom.AtomLink;

import com.ibm.di.api.DIException;
import com.ibm.di.api.bind.CreateConfig;
import com.ibm.di.api.remote.Session;
import com.ibm.di.api.rest.internal.util.AtomFeedFactory;
import com.ibm.di.api.rest.internal.AppConstants;
import com.ibm.di.api.rest.internal.handler.NotFound;
import com.ibm.di.api.rest.internal.util.ConfigConvertor;
import com.ibm.di.api.rest.internal.util.EnvUtils;
import com.ibm.di.function.SystemFunctions;

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
@Path(ConfigurationDir.URL)
public class ConfigurationDir {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static final String URL = "config";

	private final String apiConfigDir;

	public ConfigurationDir() {
		this(".");
	}

	public ConfigurationDir(String apiConfigDir) {
		this.apiConfigDir = apiConfigDir;
	}
	@GET
	@Produces(AppConstants.OBJ_JSON_AtomFeed)
	public Response getFeedAsJson(@Context UriInfo uri, @Context HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException, UnsupportedEncodingException {
		return getFeed(uri, req, false);
	}

	@GET
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response getFeedAsXml(@Context UriInfo uri, @Context HttpServletRequest req) throws RemoteException, DIException,
			NotBoundException, UnsupportedEncodingException {
		return getFeed(uri, req, true);
	}

	private Response getFeed(UriInfo uri, HttpServletRequest req, boolean isXml) throws DIException, RemoteException,
			NotBoundException, UnsupportedEncodingException {
		Session s = EnvUtils.getServerApiSession(req);
		try {
			s.listFolders(apiConfigDir);
		} catch (DIException e) {
			return Response.status(Status.NOT_FOUND).build();
		}

		String absPath = uri.getAbsolutePath().normalize().toString();
		String parentUrl = normalizeDirPath(absPath);

		AtomFeed feed = AtomFeedFactory.createAtomFeed();
		feed.setId(absPath);
		feed.setUpdated(System.currentTimeMillis());
		feed.getCategories().add(AppConstants.CAT_RES_CONFIG);
		if (!".".equals(apiConfigDir)) {
			feed.setTitle(createAtomText(apiConfigDir));
		}

		for (Object childFile : s.listAllConfigurations()) {
			String lastFile = getLastPath(childFile.toString());
			feed.getEntries().add(
					new ConfigurationFile(childFile.toString()).getSelf(parentUrl + "/"
							+ URLEncoder.encode("e:" + lastFile, "UTF-8"), s, req.getSession(), isXml));
		}

		return Response.ok(feed).build();
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_CreateConfig, AppConstants.MT_API_CONFIG_XML })
	@Produces(AppConstants.OBJ_JSON_AtomEntry)
	public Response createConfigurationAsJson(@Context HttpServletRequest req, CreateConfig cc, @Context UriInfo uri)
			throws Exception {
		return createConfiguration(req, cc, uri, false);
	}

	@POST
	@Consumes( { AppConstants.OBJ_JSON_CreateConfig, AppConstants.MT_API_CONFIG_XML })
	@Produces(MediaType.APPLICATION_ATOM_XML)
	public Response createConfigurationAsXml(@Context HttpServletRequest req, CreateConfig cc, @Context UriInfo uri)
			throws Exception {
		return createConfiguration(req, cc, uri, true);
	}

	private Response createConfiguration(HttpServletRequest req, CreateConfig cc, UriInfo uri, boolean isXml) throws DIException,
			RemoteException, NotBoundException, Exception, UnsupportedEncodingException {
		String configFile = apiConfigDir + "/" + cc.getName();

		Session s = EnvUtils.getServerApiSession(req);
		s.createNewConfiguration(configFile, cc.isOverwrite());

		if (cc.getSolution() == null) {
			throw new DIException(AppConstants.L10N.getString("REST.API.CONFIG.MISSING"));
		}

		if (cc.isLeaveCheckOut()) {
			s.checkInAndLeaveCheckedOut(ConfigConvertor.toConfig(cc.getSolution()), configFile, cc.isEncrypt());
		} else {
			s.checkInConfiguration(ConfigConvertor.toConfig(cc.getSolution()), configFile, cc.isEncrypt());
		}

		URI absPath = URI.create(uri.getAbsolutePath().normalize() + "/" + URLEncoder.encode(cc.getName(), "UTF-8"));
		return Response.created(absPath).entity(
				new ConfigurationFile(configFile).getSelf(absPath.toString(), s, req.getSession(), isXml)).build();
	}

	@Path("{pathElem}")
	public Object getChild(@PathParam("pathElem") String pathElem, @Context HttpServletRequest req) throws RemoteException,
			DIException, NotBoundException {

		Session s = EnvUtils.getServerApiSession(req);
		if (pathElem.startsWith("e:")) {
			// looking for end element to represent as AtomEntry
			String actPath = pathElem.substring(2);
			try {
				String decoded = URLDecoder.decode(actPath, "UTF-8");
				actPath = decoded;
			} catch (Exception e) {
				SystemFunctions.doNothing();
			}

			String apiName = findResourceIn(actPath, s.listConfigurations(apiConfigDir));
			if (apiName != null) {
				return new ConfigurationFile(apiName);
			}
			
			apiName = findResourceIn(actPath, s.listFolders(apiConfigDir));
			if (apiName != null) {
				return new EntryRepresentation(actPath, apiName);
			}
			
			// Allow use of solution identifiers (e.g. dir structure ignored)
			for(Object name : s.listAllConfigurations()) {
				if(actPath.equals(name)) {
					return new ConfigurationFile(actPath);
				}
			}

		} else if (pathElem.startsWith("f:")) {
			// looking for an end element to represent as AtomFeed
			String actPath = pathElem.substring(2);
			String apiName = findResourceIn(actPath, s.listFolders(apiConfigDir));
			if (apiName != null) {
				return new ConfigurationDir(apiName);
			}
		} else {
			// an intermediary path element so just navigate through
			String apiName = findResourceIn(pathElem, s.listFolders(apiConfigDir));
			if (apiName != null) {
				return new ConfigurationDir(apiName);
			}
		}

		// in some rare situations when the server is on a Unix box the file
		// might contain colons.
		if (pathElem.indexOf(':') > -1 && !s.getServerInfo().getOperatingSystem().toLowerCase().contains("windows")) {
			String apiName = findResourceIn(pathElem, s.listConfigurations(apiConfigDir));
			if (apiName != null) {
				return new ConfigurationFile(apiName);
			}

			apiName = findResourceIn(pathElem, s.listFolders(apiConfigDir));
			if (apiName != null) {
				return new ConfigurationDir(apiName);
			}
		}

		return NotFound.getInstance();
	}

	public String findResourceIn(String resource, List<String> apiConfigNames) {
		String parentDir = ".".equals(apiConfigDir) ? null : apiConfigDir;
		for (String c : apiConfigNames) {
			if (c != null && equalsIgnoreLastSeparator(c, parentDir, resource)) {
				return c;
			}
		}
		return null;
	}

	private boolean equalsIgnoreLastSeparator(String complete, String parentDir, String childFile) {
		if (parentDir == null) {
			return childFile.regionMatches(0, complete, 0, childFile.length()) && childFile.length() == complete.length();
		} else if (parentDir.regionMatches(0, complete, 0, parentDir.length())) {
			int childStart = parentDir.length();
			for (; childStart < complete.length() && (complete.charAt(childStart) == '\\' || complete.charAt(childStart) == '/'); childStart++)
				;
			return childFile.regionMatches(0, complete, childStart, childFile.length())
					&& childStart + childFile.length() == complete.length();
		}

		return false;
	}

	static String getLastPath(String relPath) {
		int last = Math.max(relPath.lastIndexOf('/'), relPath.lastIndexOf('\\'));
		return last > 0 && last < relPath.length() - 1 ? relPath.substring(last + 1) : relPath;
	}

	public class EntryRepresentation {
		private final String simpleName;
		private final String title;

		public EntryRepresentation(String simpleName, String title) {
			this.simpleName = simpleName;
			this.title = title;
		}

		@GET
		@Produces(AppConstants.OBJ_JSON_AtomEntry)
		public AtomEntry getDirEntryAsJson(@Context UriInfo uri) throws UnsupportedEncodingException {
			return getDirEntry(uri, false);
		}

		@GET
		@Produces(MediaType.APPLICATION_ATOM_XML)
		public AtomEntry getDirEntryAsXml(@Context UriInfo uri) throws UnsupportedEncodingException {
			return getDirEntry(uri, true);
		}

		private AtomEntry getDirEntry(UriInfo uri, boolean isXml) throws UnsupportedEncodingException {
			String path = uri.getPath(false);
			path = path.substring(0, path.lastIndexOf('/'));
			return ConfigurationDir.this.getDirEntry(URI.create(uri.getBaseUri() + path).normalize().toString(), simpleName, title,
					isXml);
		}
	}

	private AtomEntry getDirEntry(String parentDirUrl, String dirName, String title, boolean isXml)
			throws UnsupportedEncodingException {
		dirName = URLEncoder.encode(":" + dirName, "UTF-8");
		StringBuilder urlBuilder = new StringBuilder(parentDirUrl.length() + 3 + dirName.length());
		urlBuilder.append(parentDirUrl);
		urlBuilder.append('/');
		int markerPos = urlBuilder.length();
		urlBuilder.append('e');
		urlBuilder.append(dirName);

		String entryUrl = urlBuilder.toString();
		urlBuilder.setCharAt(markerPos, 'f');
		String feedUrl = urlBuilder.toString();

		AtomEntry e = new AtomEntry();
		e.setId(entryUrl);
		e.setUpdated(System.currentTimeMillis());
		e.setTitle(createAtomText(title));

		AtomLink l = new AtomLink();
		l.setRel(AppConstants.REL_SELF);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomEntry);
		l.setHref(e.getId());
		e.getLinks().add(l);

		l = new AtomLink();
		l.setRel(AppConstants.REL_CONTENT);
		l.setType(isXml ? MediaType.APPLICATION_ATOM_XML : AppConstants.OBJ_JSON_AtomFeed);
		l.setHref(feedUrl);
		e.getLinks().add(l);

		e.getCategories().add(AppConstants.CAT_RES_CONFIG);
		e.getCategories().add(AppConstants.CAT_CONFIG_DIR);
		return e;
	}

	private String normalizeDirPath(String absPath) {
		return ".".equals(apiConfigDir) ? absPath : (getParentPath(absPath) + "/" + getLastPath(apiConfigDir));
	}

	static String getParentPath(String path) {
		// JAX-RS guarantees URI paths don't end with slashes
		int lastSlash = path.lastIndexOf('/');
		return lastSlash > 0 ? path.substring(0, lastSlash) : null;
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
