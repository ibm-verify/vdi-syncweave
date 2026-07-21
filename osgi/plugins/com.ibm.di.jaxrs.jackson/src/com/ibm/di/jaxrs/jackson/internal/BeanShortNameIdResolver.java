/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.jaxrs.jackson.internal;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;

/**
 * Used to convert {@link XmlRootElement} name() to JAXB Class and vice versa. <br>
 * <br>
 * Name -> Class:
 * <ul>
 * <li>Use each class in the baseClasses list, to find out the concrete
 * sub-class which is referred to using {@link XmlSeeAlso} annotation.</li>
 * <li>If above does not find out the exact class, cycle all the ObjectFactory
 * objects and look for a method that returns a {@link JAXBElement} and has an
 * {@link XmlElementDecl} with the exact name. If true the single parameter type
 * is returned.</li>
 * </ul>
 * <br>
 * Instance -> Name:
 * <ul>
 * <li>For the provided POJO instance find the {@link XmlRootElement} and use
 * the name() as identifier.</li>
 * <li>If above does not find out the exact class, use one of the specified
 * ObjectFactory objects and a method that returns a {@link JAXBElement} and
 * accepts a single parameter assignable from the specified class and uses
 * {@link XmlElementDecl} name()</li>
 * </ul>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class BeanShortNameIdResolver implements TypeIdResolver, ResolvableTypesFilter {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private static ReadWriteLock cacheLock = new ReentrantReadWriteLock();

	private static Map<String, JavaType> idToType = new HashMap<String, JavaType>();
	private static Map<JavaType, String> typeToId = new HashMap<JavaType, String>();
	private static Set<JavaType> notProcessed = new HashSet<JavaType>();

	private final Class<?>[] baseClasses;
	private final Class<?>[] objectFactories;

	public BeanShortNameIdResolver(final Class<?>[] baseClasses, final Class<?>[] objectFactories) {
		this.baseClasses = baseClasses;
		this.objectFactories = objectFactories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.fasterxml.jackson.databind.jsontype.TypeIdResolver#getMechanism()
	 */
	public Id getMechanism() {
		return Id.CUSTOM;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fasterxml.jackson.databind.jsontype.TypeIdResolver#idFromValue(java.lang
	 * .Object)
	 */
	public String idFromValue(Object inst) {
		JavaType jt = TypeFactory.defaultInstance().constructType(inst.getClass());
		String id = idFromType(jt);
		if (id == null) {
			throw new InternalError(inst.getClass().getName());
		}
		return id;
	}

	private String idFromType(JavaType jt) {
		cacheLock.readLock().lock();
		String id = typeToId.get(jt);
		boolean processed = id != null || !notProcessed.contains(jt);
		cacheLock.readLock().unlock();

		if (id == null && processed) {
			cacheLock.writeLock().lock();
			// some other thread might has already found it until this one
			// received the writer lock so do the check again.
			id = typeToId.get(jt);
			if (id == null) {
				id = idForType(jt.getRawClass());
				if (id != null) {
					typeToId.put(jt, id);
					idToType.put(id, jt);
				} else {
					notProcessed.add(jt);
				}
			}
			cacheLock.writeLock().unlock();
		}
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fasterxml.jackson.databind.jsontype.TypeIdResolver#init(com.fasterxml.jackson
	 * .databind.JavaType)
	 */
	public void init(JavaType arg0) {
	}

	public String getDescForKnownTypeIds() {
		return null;
	}

	public String idFromBaseType() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fasterxml.jackson.databind.jsontype.TypeIdResolver#typeFromId(com.fasterxml.jackson
	 * .databind.DatabindContext, java.lang.String)
	 */
	public JavaType typeFromId(DatabindContext paramDatabindContext, String id) throws IOException {
		cacheLock.readLock().lock();
		JavaType jt = idToType.get(id);
		cacheLock.readLock().unlock();

		if (jt == null) {
			cacheLock.writeLock().lock();
			// some other thread might has already found it until this one
			// received the writer lock so do the check again.
			jt = idToType.get(jt);
			if (jt == null) {
				Class<?> t = typeForId(id);
				if (t != null) {
					jt = TypeFactory.defaultInstance().constructType(t);
					typeToId.put(jt, id);
					idToType.put(id, jt);
				} else {
					throw new IllegalArgumentException("Invalid type id '" + id + "' no mapping class found");
				}
			}
			cacheLock.writeLock().unlock();
		}

		return jt;
	}

	private String idForType(Class<?> type) {
		// try the preferred way using class annotation.
		String id = getNameFromXmlRootElement(type);
		if (id != null) {
			return id;
		}

		// No! Then use one of the ObjectFactories
		Class<?> of = null;

		for (Class<?> fact : objectFactories) {
			if (type.getPackage().getName().equals(fact.getPackage().getName())) {
				of = fact;
				break;
			}
		}

		if (of != null && of.getAnnotation(XmlRegistry.class) != null) {
			for (Method m : of.getMethods()) {
				XmlElementDecl decl = m.getAnnotation(XmlElementDecl.class);
				if (decl != null && decl.scope() == XmlElementDecl.GLOBAL.class && m.getParameterTypes().length == 1
						&& JAXBElement.class.isAssignableFrom(m.getReturnType()) && type.isAssignableFrom(m.getParameterTypes()[0])) {
					return decl.name();
				}
			}
		}

		return null;
	}

	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		return idForType(suggestedType);
	}

	private Class<?> typeForId(String id) {
		// use class annotations
		for (Class<?> c : baseClasses) {
			Class<?> result = getClassById(c, id);
			if (result != null) {
				return result;
			}
		}

		// fallback to ObjectFactory
		for (Class<?> of : objectFactories) {
			if (of.getAnnotation(XmlRegistry.class) != null) {
				for (Method m : of.getMethods()) {
					XmlElementDecl decl = m.getAnnotation(XmlElementDecl.class);
					if (decl != null && decl.scope() == XmlElementDecl.GLOBAL.class && m.getParameterTypes().length == 1
							&& JAXBElement.class.isAssignableFrom(m.getReturnType()) && id.equals(decl.name())) {
						return m.getParameterTypes()[0];
					}
				}
			}
		}

		return null;
	}

	private Class<?> getClassById(Class<?> c, String id) {
		// try XmlRootElement first
		String name = getNameFromXmlRootElement(c);
		if (id.equals(name)) {
			return c;
		}

		// none... try XmlSeeAlso
		XmlSeeAlso see = c.getAnnotation(XmlSeeAlso.class);
		if (see != null && see.value() != null) {
			for (Class<?> s : see.value()) {
				Class<?> result = getClassById(s, id);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	private String getNameFromXmlRootElement(Class<?> type) {
		XmlRootElement root = type.getAnnotation(XmlRootElement.class);
		if (root != null && root.name() != null) {
			return root.name();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.jaxrs.jackson.ResolvableTypesFilter#isTypeResolvable(com.fasterxml.jackson
	 * .databind.JavaType)
	 */
	public boolean isTypeResolvable(JavaType jt) {
		if (jt.isArrayType()) {
			jt = jt.getContentType();
		}
		
		// Objects are always not resolvable
		if (Object.class == jt.getRawClass()) {
			return false;
		}
		
		for (Class<?> c : baseClasses) {
			if (jt.getRawClass() != null && c.isAssignableFrom(jt.getRawClass())) {
				// if the class is abstract it must have a sub-class with root
				// element defined, so just consider it not resolvable
				return (jt.getRawClass().getModifiers() & Modifier.ABSTRACT) != Modifier.ABSTRACT
				// it may inherit a base class but still not be a global
						// element, i.e. has no substitutionGroup and thus no
						// id, then it is considered resolvable.
						&& idFromType(jt) == null;
			}
		}

		// instance not configured to resolve that class, then it must be
		// resolvable
		return true;
	}
}
