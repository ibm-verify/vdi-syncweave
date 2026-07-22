/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.loader.IDILoader;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.text.DateFormat;

/**
 * This is a function component that calls a generic java method.
 */
public class JavaClassFC extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties
	 */
	private static final String PROPERTIES_FILE = "javaclassfc";

	/**
	 * Parameter name
	 */
	private static final String JARFILE = "jarFile";
	/**
	 * Parameter name
	 */
	private static final String JAVACLASS = "javaClass";
	/**
	 * Parameter name
	 */
	private static final String METHOD = "method";

	/**
	 * The Java Class containing the method that is to be called.
	 */
	private Class<?> theClass = null;

	/**
	 * Instance of {@link #theClass}
	 */
	private Object instance = null;

	/**
	 * The method that is to be called
	 */
	private Method theMethod = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Called once to initialize the function
	 * 
	 * @param obj
	 *            Additional information
	 * @exception Exception
	 *                If the configuration is not complete, or the class or
	 *                method cannot be found
	 */
	public void initialize(Object obj) throws Exception {

		String javaClass = getConfiguration().getStringParameter(JAVACLASS);
		if (javaClass == null || javaClass.length() == 0) {
			throw new Exception(sResHash.getString("FC.JAVACLASS.JAVACLASS.EXCEPTION"));
		}

		String method = getConfiguration().getStringParameter(METHOD);
		if (method == null || method.length() == 0) {
			throw new Exception(sResHash.getString("FC.JAVACLASS.METHOD.EXCEPTION"));
		}
		
		try {
			theClass = Class.forName(javaClass);
		} catch (Exception e) {
			String jarFile = getConfiguration().getStringParameter(JARFILE);
			if (jarFile != null && jarFile.length() > 0) {
				if (jarFile.endsWith(".class")) {
					theClass = IDILoader.getInstance().loadClassFromFile(jarFile);
				} else {
					theClass = IDILoader.getInstance().loadClassFromFile(jarFile, javaClass);
				}
			}
		}

		if (theClass == null) {
			// Should use another Message here
			throw new Exception(sResHash.getString(
					"FC.JAVACLASS.UNABLE.TO.LOCATE.METHOD", new Object[] {
							"<init>", javaClass }));
		}

		Method[] methods = theClass.getMethods();
		theMethod = null;

		for (int i = 0; i < methods.length; i++) {
			if (method.equals(method2String(methods[i]))) {
				theMethod = methods[i];
				break;
			}
		}

		if (theMethod == null) {
			throw new Exception(sResHash.getString("FC.JAVACLASS.UNABLE.TO.LOCATE.METHOD", 
					new Object[] { method, javaClass }));
		}

		super.initialize(obj);
	}

	/**
	 * Sets the instance that will be used when invoking the method. By default
	 * an instance created using the empty constructor will be used.
	 * 
	 * @param obj
	 *            The instance to use
	 */
	public void setInstance(Object obj) {
		instance = obj;
	}

	/**
	 * Return the instance that is used when invoking the method.
	 * 
	 * @return The instance used
	 */
	public Object getInstance() {
		return instance;
	}

	/**
	 * Makes a call to the java method.
	 * 
	 * @param obj
	 *            An Entry containing the values of the parameters
	 * 
	 * @return The result of calling the method
	 * @throws Exception
	 *             If no method is found. If parameters are needed, and obj is
	 *             not an Entry.
	 */
	public Object perform(Object obj) throws Exception {

		if (theMethod == null) {
			throw new Exception(sResHash.getString(
					"FC.JAVACLASS.UNABLE.TO.LOCATE.METHOD.2", new Object[] {
							getConfiguration().getStringParameter(METHOD),
							getConfiguration().getStringParameter(JAVACLASS) }));
		}

		Class<?>[] params = theMethod.getParameterTypes();
		int n = params.length;
		Object[] args = new Object[n];

		if (n > 0) {
			if (!(obj instanceof Entry)) {
				throw new Exception(sResHash
						.getString("FC.JAVACLASS.INVALIDENTRY.EXCEPTION"));
			}

			Entry e = (Entry) obj;

			for (int i = 0; i < n; i++)
				args[i] = convertClass(e.getAttribute("p" + (i + 1)), params[i]);
		}

		// Create an instance if needed
		if (instance == null && !Modifier.isStatic(theMethod.getModifiers()))
			instance = theClass.newInstance();

		Object ret = theMethod.invoke(instance, args);

		// Maybe return an empty Entry if the method is supposed to return void,
		// since null is supposed to mean that something went wrong....
		if (ret == null && theMethod.getReturnType() == Void.TYPE)
			ret = new Entry();

		return ret;
	}

	/**
	 * Convert a Method to a nice display String. Method.toString() does not
	 * look nice
	 * 
	 * @param m
	 *            Method name
	 * @return formated method
	 */
	private String method2String(Method m) {
		StringBuffer s = new StringBuffer();
		s.append(m.getReturnType().getName());
		s.append(" ");
		s.append(m.getName());
		s.append("(");
		Class<?>[] params = m.getParameterTypes();
		for (int i = 0; i < params.length; i++) {
			if (i > 0)
				s.append(", ");
			s.append(getClassName(params[i]));
			s.append(" p" + (i + 1));
		}
		s.append(")");
		return s.toString();
	}

	/**
	 * Convert a Class to a nice display String. Class.getName() does not look
	 * nice for arrays
	 * 
	 * @param c
	 *            Class
	 * @return formated class
	 */
	private String getClassName(Class<?> c) {
		if (c.isArray())
			return getClassName(c.getComponentType()) + "[]";
		else
			return c.getName();
	}

	/**
	 * Try to convert the argument to the given class
	 * 
	 * @param a -
	 *            An Object, possible an Attribute that should be unwrapped
	 * @param c -
	 *            The class to convert to
	 * @return - an Object of the correct class, or null if not possible
	 * @throws Exception
	 *             never.
	 */
	@SuppressWarnings("unchecked")
	private Object convertClass(Object a, Class<?> c) throws Exception {
		if (a == null)
			return a;
		Object obj = a;

		// Unwrap Attribute
		if (a instanceof Attribute) {
			Attribute att = (Attribute) a;
			if (c == Attribute.class)
				return att;

			if (att.size() == 0 && !c.isArray())
				return null;
			if (att.size() == 1)
				obj = att.getValue(0);
			else
				obj = att.getValues();
		}

		// Are we lucky?
		if (c.isInstance(obj))
			return obj;

		// Handle String
		if (c == String.class) {
			if (obj instanceof byte[])
				return new String((byte[]) obj);
			if (obj instanceof char[])
				return new String((char[]) obj);
			return obj.toString();
		}

		// Handle array...
		Object[] val;
		if (obj instanceof Object[])
			val = (Object[]) obj;
		else
			val = new Object[] { obj };

		if (c.isArray()) {
			Object array = Array.newInstance(c.getComponentType(), val.length);
			for (int i = 0; i < val.length; i++)
				Array.set(array, i, convertClass(val[i], c.getComponentType()));
			return array;
		}

		if (Collection.class.isAssignableFrom(c)) {
			Collection ret = (Collection) c.newInstance();
			for (int i = 0; i < val.length; i++)
				ret.add(val[i]);
			return ret;
		}

		obj = val[0];

		// Lucky?
		if (c.isInstance(obj))
			return obj;

		if (c == Integer.class || c == Integer.TYPE) {
			if (obj instanceof Integer)
				return obj;
			int i;
			if (obj instanceof Number)
				i = ((Number) obj).intValue();
			else
				i = Integer.parseInt(obj.toString());
			return Integer.valueOf(i);
		}

		if (c == Long.class || c == Long.TYPE) {
			if (obj instanceof Long)
				return obj;
			long l;
			if (obj instanceof Number)
				l = ((Number) obj).longValue();
			else
				l = Long.parseLong(obj.toString());
			return Long.valueOf(l);
		}

		if (c == Short.class || c == Short.TYPE) {
			if (obj instanceof Short)
				return obj;
			short l;
			if (obj instanceof Number)
				l = ((Number) obj).shortValue();
			else
				l = Short.parseShort(obj.toString());
			return Short.valueOf(l);
		}

		if (c == Byte.class || c == Byte.TYPE) {
			if (obj instanceof Byte)
				return obj;
			byte l;
			if (obj instanceof Number)
				l = ((Number) obj).byteValue();
			else
				l = Byte.parseByte(obj.toString());
			return Byte.valueOf(l);
		}

		if (c == Float.class || c == Float.TYPE) {
			if (obj instanceof Float)
				return obj;
			return new Float(obj.toString());
		}

		if (c == Double.class || c == Double.TYPE) {
			if (obj instanceof Double)
				return obj;
			return new Double(obj.toString());
		}

		if (c == Boolean.class || c == Boolean.TYPE) {
			if (obj instanceof Boolean)
				return obj;
			return Boolean.valueOf(obj.toString());
		}

		if (c == Character.class || c == Character.TYPE) {
			if (obj instanceof Character)
				return obj;
			return Character.valueOf(obj.toString().charAt(0));
		}

		if (c == Date.class) {
			try {
				return DateFormat.getInstance().parse(obj.toString());
			} catch (Exception e) {
				logger.info(sResHash.getString(
						"FC.JAVACLASS.DATEBAD.EXCEPTION", new Object[] { obj,
								e.toString() }));
			}
		}

		logger.info(sResHash.getString("FC.JAVACLASS.CANNOTCONVERT.EXCEPTION",
				new Object[] { obj, obj.getClass().getName(), c.getName() }));
		return null;

	}

	/**
	 * @return version information
	 */
	public String getVersion() {
		return "2.0-di11.0.0.1 1.27 2018/08/08";
	}

	/**
	 * Return a list of method names in our javaClass. Used by the Config
	 * Editor.
	 * 
	 * @return a String[] containing names of all methods.
	 */
	public String[] getMethods() {
		String javaClass = getConfiguration().getStringParameter(JAVACLASS);

		if (javaClass == null || javaClass.trim().length() == 0)
			return new String[0];

		ArrayList<String> list = new ArrayList<String>();
		try {
			Class<?> c = null;

			String jarFile = getConfiguration().getStringParameter(JARFILE);
			if (jarFile != null && jarFile.length() > 0) {

				if (jarFile.endsWith(".class")) {
					c = IDILoader.getInstance().loadClassFromFile(jarFile);
				} else {
					c = IDILoader.getInstance().loadClassFromFile(jarFile, javaClass);
				}
			}

			if (c == null || !c.getName().equals(javaClass))
				c = Class.forName(javaClass);

			Method[] methods = c.getMethods();

			for (int i = 0; i < methods.length; i++)
				list.add(method2String(methods[i]));

		} catch (Exception e) {
			list.add(e.toString());
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * This method modifies the schema in the provided configuration. The intent
	 * is to allow the FC to provide a schema definition dynamically based on a
	 * given configuration. Note: changes the configuration of this component
	 * 
	 * @param config -
	 *            The new FunctionConfig to use
	 * @return - true if the schema was successfully updated
	 * @throws Exception :
	 *             never
	 */
	public boolean updateSchema(FunctionConfig config) throws Exception {
		setConfiguration(config.getFunctionConfig());
		initialize(null);
		String defaultAttribute = getConfiguration().getStringParameter(
				"defaultAttribute");

		// Input (from the JavaClassFC to the AssemblyLine )
		SchemaConfig schema = config.getSchema(true);
		SchemaItemConfig sic = schema.getItem(defaultAttribute);
		if (sic == null)
			sic = schema.newItem(defaultAttribute);
		sic.setPresenceFlag("Always");
		sic.setJavaClass(getClassName(theMethod.getReturnType()));

		// Output (from the AssemblyLine to the JavaClassFC )
		schema = config.getSchema(false);
		List<String> oldList = schema.getItemNames();

		for (int i = 0; i < oldList.size(); i++)
			schema.removeItem(oldList.get(i));

		Class<?>[] params = theMethod.getParameterTypes();
		for (int i = 0; i < params.length; i++) {
			sic = schema.newItem("p" + (i + 1));
			sic.setPresenceFlag("Required");
			sic.setJavaClass(getClassName(params[i]));
		}

		return true;
	}

}
