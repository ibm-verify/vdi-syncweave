/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.automation;

import java.lang.reflect.Array;
import java.util.Vector;

import com.ibm.di.server.RS;
import com.ibm.di.server.ResourceHash;

public class IDispatch {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	private int refCount = 0;

	public int m_pIDispatch = 0;

	// constructors
	/**
	 * Default c'tor This class is a wrapper for COM's IDispatch This class can
	 * be used in javascript, through a predefined javascript object, COMProxy.
	 */
	public IDispatch() {
		super();
		refCount = 0;
		m_pIDispatch = 0;
	}

	/**
	 * This class is a wrapper for COM's IDispatch It can be used in javascript,
	 * through a predefined javascript object, COMProxy. COMProxy is a
	 * predefined instance of com.ibm.di.automation.IDispatch usage :
	 * system.createCOMInstance(String progID) calls new IDispatch(progID)
	 * 
	 * @param progid
	 *            the progID (Programmatic IDentifier)is a string that uniquely
	 *            identifies the COM object, stored in the registry and is of
	 *            the form: Project.ClassName
	 */
	public IDispatch(String progid) {
		try {
			createInstance(progid);
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	/**
	 * Converts an object to VARIANT, so that it can be passed to various
	 * function calls
	 * 
	 * @param o
	 *            object to be converted to VARIANT
	 * @return VARIANT equivalent of o
	 * @throws COMError
	 *             throws COMError if o cannot be converted to VARIANT
	 */
	public static VARIANT objectToVariant(Object o) throws COMError {
		if (o == null)
			return new VARIANT();
		if (o instanceof VARIANT)
			return (VARIANT) o;
		if (o instanceof Integer)
			return new VARIANT(((Integer) o).intValue());
		if (o instanceof String)
			return new VARIANT((String) o);
		if (o instanceof Boolean)
			return new VARIANT(((Boolean) o).booleanValue());
		if (o instanceof Double)
			return new VARIANT(((Double) o).doubleValue());
		if (o instanceof Float)
			return new VARIANT(((Float) o).floatValue());
		if (o instanceof SafeArray)
			return new VARIANT((SafeArray) o);
		if (o instanceof BSTR)
			return new VARIANT((BSTR) o);
		if (o instanceof IDispatch) {
			VARIANT v = new VARIANT();
			v.putObject((IDispatch) o);
			return v;
		}
		// automatically convert arrays using reflection
		Class c1 = o.getClass();
		SafeArray sa = null;
		if (c1.isArray()) {
			int len1 = Array.getLength(o);
			Object first = Array.get(o, 0);
			if (first.getClass().isArray()) {
				int max = 0;
				for (int i = 0; i < len1; i++) {
					Object e1 = Array.get(o, i);
					int len2 = Array.getLength(e1);
					if (max < len2) {
						max = len2;
					}
				}
				sa = new SafeArray(COMConstants.VT_VARIANT, len1, max);
				for (int i = 0; i < len1; i++) {
					Object e1 = Array.get(o, i);
					for (int j = 0; j < Array.getLength(e1); j++) {
						sa.setVariant(i, j, objectToVariant(Array.get(e1, j)));
					}
				}
			} else {
				sa = new SafeArray(COMConstants.VT_VARIANT, len1);
				for (int i = 0; i < len1; i++) {
					sa.setVariant(i, objectToVariant(Array.get(o, i)));
				}
			}
			return new VARIANT(sa);
		}

		throw new ClassCastException(sResHash
				.getString("IDISPATCH.CANNOT.CONVERTTO.VARIANT.ERROR"));
	}

	/**
	 * Converts an array of objects to array of VARIANTs, so that it can be
	 * passed to various function calls
	 * 
	 * @param o
	 *            array of objects to be converted to VARIANT
	 * @return array of VARIANTs
	 * @throws COMError
	 *             throws COMError if o cannot be converted to VARIANT
	 */
	public static VARIANT[] objectToVariant(Object[] o) throws COMError {
		VARIANT vArg[] = new VARIANT[o.length];
		for (int i = 0; i < o.length; i++) {
			vArg[i] = objectToVariant(o[i]);
		}
		return vArg;
	}

	/**
	 * c'tor this only gets called from JNI
	 * 
	 * @param pDisp
	 *            pointer to pointer to COM's IDispatch
	 */
	protected IDispatch(int pDisp) {
		m_pIDispatch = pDisp;
	}

	/**
	 * Native call, calls contained COM's
	 * IDispatch's(m_pIDispatch)QueryInterface
	 * 
	 * @param iid
	 *            Identifier of the interface being requested
	 * @return Returns pointers to supported interfaces
	 * @throws COMError
	 */
	public native IDispatch QueryInterface(String iid) throws COMError;

	/**
	 * Creates a single uninitialized object of the class associated with a
	 * specified PROGID
	 * 
	 * @param progid
	 *            the progID (Programmatic IDentifier)
	 * @throws COMError
	 *             if instance cannot be created
	 */
	public native void createInstance(String progid) throws COMError;

	/**
	 * call this to explicitly release the com object before gc Native call,
	 * calls contained COM's IDispatch's(m_pIDispatch) Release Decrements
	 * reference count.
	 */
	public native void release();

	/**
	 * eliminate _Guid arg in the invoke call
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of method/property to be invoked
	 * @param dispID
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @param lcid
	 *            The locale context in which to interpret arguments, currently
	 *            only LOCALE_SYSTEM_DEFAULT is supported
	 * @param wFlags
	 *            Flags describing the context of the Invoke call, include:
	 *            Value Description 1>DISPATCH_METHOD 2>DISPATCH_PROPERTYGET
	 *            3>DISPATCH_PROPERTYPUT 4>DISPATCH_PROPERTYPUTREF
	 * @param vArg
	 *            array of arguments to be passed to the method invoked
	 * @param uArgErr
	 *            The index within rgvarg of the first argument that has an
	 *            error
	 * @throws COMError
	 *             if the method cannot be invoked
	 */
	public static void invokeSubv(Object disp, String name, int dispID,
			int lcid, int wFlags, VARIANT[] vArg, int[] uArgErr)
			throws COMError {
		invokev(disp, name, dispID, lcid, wFlags, vArg, uArgErr);
	}

	/**
	 * Native call, calls contained COM's IDispatch's(m_pIDispatch)
	 * GetIDsOfNames Maps a single member and an optional set of argument names
	 * to a corresponding set of integer DISPIDs, which can be used on
	 * subsequent calls to IDispatch::Invoke.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param lcid
	 *            The locale context in which to interpret arguments, currently
	 *            only LOCALE_SYSTEM_DEFAULT is supported
	 * @param names
	 *            array of method names
	 * @return Caller-allocated array, each element of which contains an
	 *         identifier (ID) corresponding to one of the names passed in the
	 *         names array. The first element represents the member name. The
	 *         subsequent elements represent each of the member's parameters.
	 * @throws COMError
	 */
	public static native int[] getIDsOfNames(Object disp, int lcid,
			String[] names) throws COMError;

	/**
	 * Calls invoke. Invokes the method specified.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param args
	 *            array of arguments to the method
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public static VARIANT call(Object disp, String name, Object[] args)
			throws COMError {
		return invokev(disp, name, COMConstants.DISPATCH_METHOD
				| COMConstants.DISPATCH_PROPERTYGET, objectToVariant(args),
				new int[args.length]);
	}

	/**
	 * Provides access to properties and methods exposed by an object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param dispID
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @param lcid
	 *            The locale context in which to interpret arguments, currently
	 *            only LOCALE_SYSTEM_DEFAULT is supported
	 * @param wFlags
	 *            Flags describing the context of the Invoke call, include:
	 *            Value Description 1>DISPATCH_METHOD 2>DISPATCH_PROPERTYGET
	 *            3>DISPATCH_PROPERTYPUT 4>DISPATCH_PROPERTYPUTREF
	 * @param oArg
	 *            array of arguments
	 * @param uArgErr
	 *            The index within rgvarg of the first argument that has an
	 *            error
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public static VARIANT invoke(Object disp, String name, int dispID,
			int lcid, int wFlags, Object[] oArg, int[] uArgErr) throws COMError {
		return invokev(disp, name, dispID, lcid, wFlags, objectToVariant(oArg),
				uArgErr);
	}

	/**
	 * Provides access to properties and methods exposed by an object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param wFlags
	 *            Flags describing the context of the Invoke call, include:
	 *            Value Description 1>DISPATCH_METHOD 2>DISPATCH_PROPERTYGET
	 *            3>DISPATCH_PROPERTYPUT 4>DISPATCH_PROPERTYPUTREF
	 * @param oArg
	 *            array of arguments
	 * @param uArgErr
	 *            The index within rgvarg of the first argument that has an
	 *            error
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public static VARIANT invoke(Object disp, String name, int wFlags,
			Object[] oArg, int[] uArgErr) throws COMError {
		return invokev(disp, name, wFlags, objectToVariant(oArg), uArgErr);
	}

	/**
	 * Provides access to properties and methods exposed by an object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param dispID
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @param wFlags
	 *            Flags describing the context of the Invoke call, include:
	 *            Value Description 1>DISPATCH_METHOD 2>DISPATCH_PROPERTYGET
	 *            3>DISPATCH_PROPERTYPUT 4>DISPATCH_PROPERTYPUTREF
	 * @param oArg
	 *            array of arguments
	 * @param uArgErr
	 *            The index within rgvarg of the first argument that has an
	 *            error
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public static VARIANT invoke(Object disp, int dispID, int wFlags,
			Object[] oArg, int[] uArgErr) throws COMError {
		return invokev(disp, dispID, wFlags, objectToVariant(oArg), uArgErr);
	}

	/**
	 * Native call, calls contained COM's IDispatch's(m_pIDispatch) invoke
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param dispID
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @param lcid
	 *            The locale context in which to interpret arguments, currently
	 *            only LOCALE_SYSTEM_DEFAULT is supported
	 * @param wFlags
	 *            Flags describing the context of the Invoke call, include:
	 *            Value Description 1>DISPATCH_METHOD 2>DISPATCH_PROPERTYGET
	 *            3>DISPATCH_PROPERTYPUT 4>DISPATCH_PROPERTYPUTREF
	 * @param vArg
	 *            array of arguments
	 * @param uArgErr
	 *            The index within rgvarg of the first argument that has an
	 *            error
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public static native VARIANT invokev(Object disp, String name, int dispID,
			int lcid, int wFlags, VARIANT[] vArg, int[] uArgErr)
			throws COMError;

	/**
	 * Calls invoke for the property/method exposed by the object
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param wFlags
	 *            Flags describing the context of the Invoke call, include:
	 *            Value Description 1>DISPATCH_METHOD 2>DISPATCH_PROPERTYGET
	 *            3>DISPATCH_PROPERTYPUT 4>DISPATCH_PROPERTYPUTREF
	 * @param vArg
	 *            array of arguments
	 * @param uArgErr
	 *            The index within rgvarg of the first argument that has an
	 *            error
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public static VARIANT invokev(Object disp, String name, int wFlags,
			VARIANT[] vArg, int[] uArgErr) throws COMError {
		if (!(disp instanceof IDispatch)) {
			throw new ClassCastException(sResHash
					.getString("IDISPATCH.OBJECT.EXPECTED.ERROR"));
		}

		// RS.getServer().getLog().dump(disp);
		return invokev(disp, name, 0, COMConstants.LOCALE_SYSTEM_DEFAULT,
				wFlags, vArg, uArgErr);
	}

	/*
	 * Calls invoke for the property/method exposed by the object @param disp
	 * java IDispatch(or COMProxy) object, obtained as a result of
	 * system.createCOMInstance(String progID) @param name name of the
	 * method/property to be invoked. @param wFlags Flags describing the context
	 * of the Invoke call, include: Value Description 1>DISPATCH_METHOD
	 * 2>DISPATCH_PROPERTYGET 3>DISPATCH_PROPERTYPUT 4>DISPATCH_PROPERTYPUTREF
	 * @param vArg @param uArgErr @param wFlagsEx @return return value of the
	 * method invoked @throws COMError
	 * 
	 * public static VARIANT invokev(Object disp, String name, int wFlags,
	 * VARIANT[] vArg, int[] uArgErr, int wFlagsEx) throws COMError { if (!(disp
	 * instanceof IDispatch)) throw new ClassCastException("IDispatch object
	 * expected"); // do not implement IIDispatchEx for now return invokev(disp,
	 * name, 0, COMConstants.LOCALE_SYSTEM_DEFAULT, wFlags, vArg, uArgErr); }
	 */

	/**
	 * Calls invoke for the property/method exposed by the object
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param dispID
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @param wFlags
	 *            Flags describing the context of the Invoke call, include:
	 *            Value Description 1>DISPATCH_METHOD 2>DISPATCH_PROPERTYGET
	 *            3>DISPATCH_PROPERTYPUT 4>DISPATCH_PROPERTYPUTREF
	 * @param vArg
	 *            array of arguments
	 * @param uArgErr
	 *            The index within rgvarg of the first argument that has an
	 *            error
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public static VARIANT invokev(Object disp, int dispID, int wFlags,
			VARIANT[] vArg, int[] uArgErr) throws COMError {
		if (!(disp instanceof IDispatch)) {
			throw new ClassCastException(sResHash
					.getString("IDISPATCH.OBJECT.EXPECTED.ERROR"));
		}

		return invokev(disp, null, dispID, COMConstants.LOCALE_SYSTEM_DEFAULT,
				wFlags, vArg, uArgErr);
	}

	/**
	 * Gets CLSID for the specified progID
	 * 
	 * @param progID
	 *            the progID (Programmatic Identifier) for which CLSID is
	 *            requested
	 * @return CLSID
	 * @throws COMError
	 */
	public static native String CLSIDfromProgID(String progID) throws COMError;

	/**
	 * enumerate methods/properties for the specified dispatch interface
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @return vector containing lest of methods/property names
	 * @throws COMError
	 */
	public static native Vector enumMethods(Object disp) throws COMError;

	static {
		System.loadLibrary("COMProxy");
	}

	/**
	 * 
	 */
	protected void finalize() {
		if (refCount != 0)
			release();
	}
}
