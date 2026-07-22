/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomCategory;
import org.apache.wink.common.model.atom.AtomLink;

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
public class AtomUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public static EntityTag increaseIntegerValue(EntityTag intEntityTag) {
		return new EntityTag(Integer.toString(Integer.parseInt(intEntityTag.getValue()) + 1));
	}

	public static URI getParentURI(URI thisURI) {
		String resourceURIString = thisURI.toString();
		int pos = resourceURIString.lastIndexOf('/');
		return pos > 0 ? URI.create(resourceURIString.substring(0, pos)) : null;
	}

	/**
	 * Constructs a synthetic implementation of the {@link UriInfo} that
	 * provides valid responses for the following methods:
	 * <p>
	 * {@link UriInfo#getRequestUri()} and {@link UriInfo#getAbsolutePath()} -
	 * returns the concatenated value of <code>rootCtx</code> and
	 * <code>path</code>
	 * <p>
	 * {@link UriInfo#getBaseUri()} - returns the URI representation of the
	 * provided <code>rootCtx</code>
	 * <p>
	 * {@link UriInfo#getPath()} - returns the value of the provided
	 * <code>path</code>
	 * 
	 * @param rootCtx
	 * @param path
	 * @return
	 */
	public static UriInfo getSyntethicUriInfo(final String rootCtx, final String path) {
		return (UriInfo) Proxy.newProxyInstance(AtomUtils.class.getClassLoader(), new Class[] { UriInfo.class },
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						try {
							if ("getAbsolutePath".equals(method.getName()) || "getRequestUri".equals(method.getName())) {
								return new URI(rootCtx + path);
							} else if ("getBaseUri".equals(method.getName())) {
								return new URI(rootCtx);
							} else if ("getPath".equals(method.getName())) {
								return path;
							}
						} catch (URISyntaxException e) {
							return null;
						}
						return null;
					}
				});
	}

	public static List<AtomLink> findLinksByLitteralRelValue(List<AtomLink> links, String rel) {
		List<AtomLink> list = new LinkedList<AtomLink>();

		for (AtomLink link : links) {
			if (link.getRel().equals(rel)) {
				list.add(link);
			}
		}

		return list;
	}

	public static List<AtomCategory> findCategoriesByTermAndScheme(List<AtomCategory> cats, AtomCategory cat) {
		List<AtomCategory> list = new LinkedList<AtomCategory>();

		for (AtomCategory c : cats) {
			if (cat.getScheme().equals(c.getScheme()) && cat.getTerm().equals(c.getTerm())) {
				list.add(c);
			}
		}

		return list;
	}

	public static List<AtomCategory> findCategoriesByScheme(List<AtomCategory> cats, String schema) {
		List<AtomCategory> list = new LinkedList<AtomCategory>();

		for (AtomCategory c : cats) {
			if (schema.equals(c.getScheme())) {
				list.add(c);
			}
		}

		return list;
	}
}
