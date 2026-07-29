/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.api.rest.internal.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;

import com.ibm.di.web.common.atom.AtomText;
import com.ibm.di.api.DIException;

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
public class InheritFromRewriter {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static void rewrite(Object binding, String internalUri) throws DIException {
		try {
			rewriteBasedOnClass(binding.getClass(), binding, internalUri);
		} catch (SecurityException e) {
			throw new DIException(e);
		} catch (IllegalArgumentException e) {
			throw new DIException(e);
		} catch (NoSuchFieldException e) {
			throw new DIException(e);
		} catch (IllegalAccessException e) {
			throw new DIException(e);
		}
	}

	private static void rewriteBasedOnClass(Class<?> c, Object o, String internalUri) throws SecurityException,
			NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		if (o == null) {
			return;
		}

		for (Field f : c.getDeclaredFields()) {
			if (f.getName().equals("inheritFrom") && f.getType() == String.class) {
				rewriteField(f, o, internalUri);
			} else if ((f.getModifiers() & Modifier.STATIC) != Modifier.STATIC && !f.getType().isPrimitive()
					&& f.getType() != String.class && !Number.class.isAssignableFrom(f.getType()) && f.getType() != Boolean.class
					&& f.getType() != Character.class && !f.getType().isEnum()) {
				boolean a = f.isAccessible();
				f.setAccessible(true);
				Object fieldObj = f.get(o);
				if (fieldObj != null) {
					if (f.getType().isArray()) {
						for (int i = 0; i < Array.getLength(fieldObj); i++) {
							Object elem = Array.get(fieldObj, i);
							if (elem != null) {
								rewriteBasedOnClass(elem.getClass(), elem, internalUri);
							}
						}
					} else {
						rewriteBasedOnClass(fieldObj.getClass(), fieldObj, internalUri);
					}
				}
				if (!a) {
					f.setAccessible(a);
				}
			}
		}

		Class<?> superclass = c.getSuperclass();
		if (superclass != null && superclass != Object.class) {
			rewriteBasedOnClass(superclass, o, internalUri);
		}
	}

	private static void rewriteField(Field f, Object o, String internalUri) throws IllegalArgumentException, IllegalAccessException {
		if (o == null) {
			return;
		}
		boolean a = f.isAccessible();
		f.setAccessible(true);
		String val = (String) f.get(o);

		if (val != null) {
			if (internalUri != null) {
				int colPos = val.indexOf(':');
				if (colPos > -1) {
					StringBuilder sb = new StringBuilder(internalUri.length() + 1 + val.length());
					sb.append(internalUri);
					if (internalUri.charAt(internalUri.length() - 1) != '/') {
						sb.append('/');
					}
					sb.append(val, 0, colPos);
					if (val.charAt(colPos + 1) != '/') {
						sb.append('/');
					}
					if (colPos < val.length() - 1) {
						sb.append(val, colPos + 1, val.length());
					}
					f.set(o, sb.toString());
				}
			} else if (val.startsWith("http:") || val.startsWith("https:")) {
				// rewriting back to something tdi understands
				URI u;
				try {
					u = new URI(val.replaceAll(" ", "%20")).normalize();
				} catch (URISyntaxException e) {
					return;
				}

				String path = u.getPath().replaceAll("%20", " ");
				int intStart = path.indexOf("internal/");
				int intEnd;
				if (intStart > -1 && (intEnd = intStart + 9) < path.length() - 1) {
					int nsEnd = path.indexOf('/', intEnd);
					if (nsEnd > -1) {
						StringBuilder sb = new StringBuilder(path.length() - (intEnd));
						sb.append(path, intEnd, nsEnd);
						sb.append(':');
						sb.append(path, nsEnd, path.length());
						f.set(o, sb.toString());
					}
				}
			}
		}

		if (!a) {
			f.setAccessible(a);
		}
	}
}
