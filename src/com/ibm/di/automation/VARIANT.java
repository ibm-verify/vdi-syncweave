/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.automation;

import com.ibm.di.server.RS;

public class VARIANT implements java.io.Serializable {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public int m_pVARIANT = 0;

	public native int toInt() throws COMError;

	public native double toDate() throws COMError;

	public native boolean toBoolean() throws COMError;

	public native VARIANT cloneIndirect() throws COMError;

	public native double toDouble() throws COMError;

	public native long toCurrency() throws COMError;

	public native String toString();

	public native IDispatch toDispatch() throws COMError;

	public native byte toByte() throws COMError;

	public native int toError() throws COMError;

	public Object toObject() {
		try {
			return toDispatch();
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
			return null;
		}
	}

	public native float toFloat() throws COMError;

	public SafeArray toSafeArray() throws COMError {
		return toSafeArray(true);
	}

	public native SafeArray toSafeArray(boolean deepCopy) throws COMError;

	public native void putShortRef(short in) throws COMError;

	public native void putIntRef(int in) throws COMError;

	public native void putDoubleRef(double in) throws COMError;

	public native void putDateRef(double in) throws COMError;

	public native void putStringRef(String in) throws COMError;

	public native void putShort(short in) throws COMError;

	public native void putInt(int in) throws COMError;

	public native void putLong(long in) throws COMError;

	public native void putDate(double in) throws COMError;

	public void putDispatch(Object in) throws COMError {
		putObject(in);
	}

	public native void putBoolean(boolean in) throws COMError;

	public native void putByte(byte in) throws COMError;

	public native void putEmpty() throws COMError;

	public native void putError(int in) throws COMError;

	public native void putNoParam() throws COMError;

	public native void putCurrency(long in) throws COMError;

	public native void putObject(Object in) throws COMError;

	public native void putDouble(double in) throws COMError;

	public native void putFloatRef(float in) throws COMError;

	public native void putCurrencyRef(long in) throws COMError;

	public native void putErrorRef(int in) throws COMError;

	public native void putBooleanRef(boolean in) throws COMError;

	public void putObjectRef(Object in) throws COMError {
		putObject(in);
	}

	public native void putByteRef(byte in) throws COMError;

	public native void putString(String in) throws COMError;

	public native void putNull() throws COMError;

	public native void getNull() throws COMError;

	public native short getShortRef() throws COMError;

	public native int getIntRef() throws COMError;

	public native short getShort() throws COMError;

	public native double getDoubleRef() throws COMError;

	public native double getDateRef() throws COMError;

	public native String getStringRef() throws COMError;

	public native int getInt() throws COMError;

	public native double getDate() throws COMError;

	public Object getDispatch() throws COMError {
		return toDispatch();
	}

	public native boolean getBoolean() throws COMError;

	public native byte getByte() throws COMError;

	public native void getEmpty() throws COMError;

	public native int getError() throws COMError;

	public native double getDouble() throws COMError;

	public Object getObject() throws COMError {
		return toDispatch();
	}

	public native long getCurrency() throws COMError;

	public native String getString() throws COMError;

	public native float getFloatRef() throws COMError;

	public native long getCurrencyRef() throws COMError;

	public native int getErrorRef() throws COMError;

	public native boolean getBooleanRef() throws COMError;

	public native Object getObjectRef() throws COMError;

	public native byte getByteRef() throws COMError;

	public native void putSafeArrayRef(SafeArray in) throws COMError;

	public native void putSafeArray(SafeArray in) throws COMError;

	public native void putBSTR(BSTR in) throws COMError;

	public native float getFloat() throws COMError;

	public native void putFloat(float in) throws COMError;

	public void putDispatchRef(Object in) throws COMError {
		putDispatch(in);
	}

	public Object getDispatchRef() throws COMError {
		return getDispatch();
	}

	public VARIANT() {
		try {
			init();
			putEmpty();
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public VARIANT(int in) {
		try {

			init();
			putInt(in);
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public VARIANT(double in) {
		try {
			init();
			putDouble(in);
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public VARIANT(boolean in) {
		try {
			init();
			putBoolean(in);
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public VARIANT(String in) {
		try {
			init();
			putString(in);
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public VARIANT(SafeArray in, boolean fByRef) {
		try {
			init();
			if (fByRef) {
				putSafeArrayRef(in);
			} else {
				putSafeArray(in);
			}
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public VARIANT(Object in) throws COMError {
		this(in, false);
	}

	public VARIANT(Object o, boolean fByRef) throws COMError {
		init();
		if (o == null) {
			putEmpty();
		} else if (o instanceof Integer) {
			if (fByRef)
				putIntRef(((Integer) o).intValue());
			else
				putInt(((Integer) o).intValue());
		} else if (o instanceof String) {
			if (fByRef)
				putStringRef((String) o);
			else
				putString((String) o);
		} else if (o instanceof Boolean) {
			if (fByRef)
				putBooleanRef(((Boolean) o).booleanValue());
			else
				putBoolean(((Boolean) o).booleanValue());
		} else if (o instanceof Double) {
			if (fByRef)
				putDoubleRef(((Double) o).doubleValue());
			else
				putDouble(((Double) o).doubleValue());
		} else if (o instanceof Float) {
			if (fByRef)
				putFloatRef(((Float) o).floatValue());
			else
				putFloat(((Float) o).floatValue());
		} else if (o instanceof BSTR) {
			putBSTR((BSTR) o);
		} else if (!(o instanceof SafeArray)) {
			if (fByRef)
				putObjectRef(o);
			else
				putObject(o);
		}
	}

	public native short getvt() throws COMError;

	public native short toShort() throws COMError;

	// call this to explicitly release the com object before gc
	public native void release() throws COMError;

	protected native void init() throws COMError;

	// serialization support
	private void writeObject(java.io.ObjectOutputStream oos) {
		try {
			Save(oos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readObject(java.io.ObjectInputStream ois) {
		try {
			Load(ois);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// is the VARIANT null or empty or error or null disp
	public native boolean isNull() throws COMError;

	public native void Save(java.io.OutputStream os) throws java.io.IOException;

	public native void Load(java.io.InputStream is) throws java.io.IOException;

	static {
		System.loadLibrary("COMProxy");
	}

}
