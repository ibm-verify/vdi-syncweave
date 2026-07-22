/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.automation;

import com.ibm.di.server.ResourceHash;
import com.ibm.di.script.ScriptEngine;

/**
 * @author Vishakha
 * 
 * Filename : COMProxy.java
 */
public class COMProxy {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static COMProxy instance = null;

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	private COMProxy() {
	}

	/**
	 * Create singular instance of COMProxy
	 * 
	 * @return singular COMProxy instance null if running on a non windows
	 *         machine COMProxy does not make sense on a non windows machine
	 * @throws Exception
	 */
	public static COMProxy create() throws Exception {
		if (System.getProperty("os.name").startsWith("Windows")) {
			if (instance == null)
				instance = new COMProxy();

			return instance;
		}
		return null;
	}

	/**
	 * create separate instances of automation object IDispatch
	 * 
	 * @param progID
	 *            program identifier
	 * @return IDispatch object
	 */
	public IDispatch createInstance(String progID) {
		return new IDispatch(progID);
	}

	/**
	 * calls IDispatch object's QueryInterface
	 * 
	 * @param disp
	 *            IDispatch object
	 * @param iid
	 *            Identifier of the interface being requested
	 * @return Returns pointers to supported interfaces
	 * @throws COMError
	 */
	public Object QueryInterface(Object disp, String iid) throws COMError {
		if (disp instanceof IDispatch)
			return ((IDispatch) disp).QueryInterface(iid);
		if (disp instanceof IUnknown)
			return ((IUnknown) disp).QueryInterface(iid);

		throw new COMError(sResHash.getString("COMPROXY.UNDEF.TYPE.ERROR"));
	}

	/**
	 * call this to explicitly release the com object before gc
	 * 
	 * @param disp
	 *            IDispatch object that needs to be released
	 * @throws COMError
	 */
	public void release(Object disp) throws COMError {
		if (disp instanceof IDispatch)
			((IDispatch) disp).release();
		if (disp instanceof IUnknown)
			((IUnknown) disp).Release();

		throw new COMError(sResHash.getString("COMPROXY.UNDEF.TYPE.ERROR"));
	}

	/**
	 * Maps a single member and an optional set of argument names to a
	 * corresponding set of integer DISPIDs, which can be used on subsequent
	 * calls to IDispatch::Invoke.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param names
	 *            array of method names
	 * @return disp ID's of methods
	 * @throws COMError
	 */
	public int[] getIDsOfNames(Object disp, String[] names) throws COMError {
		return IDispatch.getIDsOfNames(disp,
				COMConstants.LOCALE_SYSTEM_DEFAULT, names);
	}

	/**
	 * Provides access to properties and methods exposed by an object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public VARIANT call(Object disp, String name) throws COMError {
		return IDispatch.call(disp, name, new VARIANT[0]);
	}

	/**
	 * Calls IDispatch::invoke for the property/method exposed by the object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param a1
	 * @return return value of the method invoked
	 * @throws COMError
	 */

	public VARIANT call(Object disp, String name, Object a1) throws COMError {
		return IDispatch.call(disp, name, new Object[] { a1 });
	}

	/**
	 * Calls IDispatch::invoke for the property/method exposed by the object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param a1
	 * @param a2
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public VARIANT call(Object disp, String name, Object a1, Object a2)
			throws COMError {
		return IDispatch.call(disp, name, new Object[] { a1, a2 });
	}

	/**
	 * Calls IDispatch::invoke for the property/method exposed by the object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param a1
	 * @param a2
	 * @param a3
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public VARIANT call(Object disp, String name, Object a1, Object a2,
			Object a3) throws COMError {
		return IDispatch.call(disp, name, new Object[] { a1, a2, a3 });
	}

	/**
	 * Calls IDispatch::invoke for the property/method exposed by the object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param a1
	 * @param a2
	 * @param a3
	 * @param a4
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public VARIANT call(Object disp, String name, Object a1, Object a2,
			Object a3, Object a4) throws COMError {
		return IDispatch.call(disp, name, new Object[] { a1, a2, a3, a4 });
	}

	/**
	 * Calls IDispatch::invokev for the property/method exposed by the object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param dispid
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public VARIANT call(Object disp, int dispid) throws COMError {
		return IDispatch.invokev(disp, dispid,
								 COMConstants.DISPATCH_METHOD | COMConstants.DISPATCH_PROPERTYGET,
								 new VARIANT[0], new int[0]);
	}

	/**
	 * Calls IDispatch::invoke for the property/method exposed by the object.
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param dispid
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @param a1
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public VARIANT call(Object disp, int dispid, Object a1) throws COMError {
		return IDispatch.invoke(disp, dispid, 
								COMConstants.DISPATCH_METHOD | COMConstants.DISPATCH_PROPERTYGET,
								new Object[] { a1 }, new int[1]);
	}

	/**
	 * Calls IDispatchinvoke for the property exposed by the object with wFlags =
	 * DISPATCH_PROPERTYPUT
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the property
	 * @param val
	 *            value to be set
	 * @throws COMError
	 */
	public void put(Object disp, String name, Object val) throws COMError {
		IDispatch.invoke(disp, name, COMConstants.DISPATCH_PROPERTYPUT,
				new Object[] { val }, new int[1]);
	}

	/**
	 * Calls IDispatch::invoke for the property exposed by the object with
	 * wFlags = DISPATCH_PROPERTYPUT
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param dispid
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @param val
	 * @throws COMError
	 */
	public void put(Object disp, int dispid, Object val) throws COMError {
		IDispatch.invoke(disp, dispid, COMConstants.DISPATCH_PROPERTYPUT,
				new Object[] { val }, new int[1]);
	}

	/**
	 * Calls IDispatch::invoke for the property exposed by the object, with
	 * wFlags=DISPATCH_PROPERTYGET
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @return return value of the method invoked
	 * @throws COMError
	 */
	public VARIANT get(Object disp, String name) throws COMError {
		return IDispatch.invokev(disp, name, COMConstants.DISPATCH_PROPERTYGET,
				new VARIANT[0], new int[0]);
	}

	/**
	 * Calls IDispatch::invoke for the property exposed by the object, with
	 * wFlags=DISPATCH_PROPERTYGET
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param dispid
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @return value of the property
	 * @throws COMError
	 */
	public VARIANT get(Object disp, int dispid) throws COMError {
		return IDispatch.invokev(disp, dispid,
				COMConstants.DISPATCH_PROPERTYGET, new VARIANT[0], new int[0]);
	}

	/**
	 * Calls IDispatch::invoke for the property exposed by the object, with
	 * wFlags=DISPATCH_PROPERTYPUTREF
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param name
	 *            name of the method/property to be invoked.
	 * @param val
	 *            value of the property
	 * @throws COMError
	 */
	public void putRef(Object disp, String name, Object val) throws COMError {
		IDispatch.invoke(disp, name, COMConstants.DISPATCH_PROPERTYPUTREF,
				new Object[] { val }, new int[1]);
	}

	/**
	 * Calls IDispatch::invoke for the property exposed by the object, with
	 * wFlags=DISPATCH_PROPERTYPUTREF
	 * 
	 * @param disp
	 *            java IDispatch(or COMProxy) object, obtained as a result of
	 *            system.createCOMInstance(String progID)
	 * @param dispid
	 *            Identifies the member. Use GetIDsOfNames or the object's
	 *            documentation to obtain the dispatch identifier
	 * @param val
	 *            value of the property
	 * @throws COMError
	 */
	public void putRef(Object disp, int dispid, Object val) throws COMError {
		IDispatch.invoke(disp, dispid, COMConstants.DISPATCH_PROPERTYPUTREF,
				new Object[] { val }, new int[1]);
	}

	/**
	 * create VARIANT with specific type
	 * 
	 * @param type
	 *            refer to the COMConstant types
	 * @param data
	 *            data
	 * @return VARIANT object
	 * @throws COMError
	 */
	public VARIANT newVariant(int type, Object data) throws COMError {
		VARIANT v = new VARIANT();
		switch (type) {
		case COMConstants.VT_INT:
		case COMConstants.VT_I2:
			v.putInt(((Integer) data).intValue());
			break;
		case COMConstants.VT_I4:
			v.putLong(((Integer) data).longValue());
			break;
		case COMConstants.VT_R4:
			v.putFloat(((Float) data).floatValue());
			break;
		case COMConstants.VT_R8:
			v.putDouble(((Double) data).doubleValue());
			break;
		case COMConstants.VT_DATE:
			v.putDate(((Double) data).doubleValue());
			break;
		default:
			return new VARIANT(data);
		}
		return v;
	}

	/**
	 * create new BSTR object
	 * 
	 * @param data
	 *            string
	 * @return BSTR object
	 * @throws COMError
	 */
	public BSTR newBSTR(String data) throws COMError {
		return new BSTR(data);
	}

}
