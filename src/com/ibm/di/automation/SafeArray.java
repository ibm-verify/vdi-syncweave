/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.automation;

import com.ibm.di.server.RS;

/**
 * @author Vishakha
 * 
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SafeArray {
	public SafeArray() {
	}

	public SafeArray(int vt) {
		try {
			init(vt, new int[] { 0 }, new int[] { -1 });
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public SafeArray(int vt, int celems) {
		try {
			init(vt, new int[] { 0 }, new int[] { celems });
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public SafeArray(int vt, int celems1, int celems2) {
		try {
			init(vt, new int[] { 0, 0 }, new int[] { celems1, celems2 });
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	public SafeArray(int vt, int lbounds[], int celems[]) {
		try {
			init(vt, lbounds, celems);
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	// convert a string to a VT_UI1 array
	public SafeArray(String s) {
		try {

			char[] ca = s.toCharArray();
			init(COMConstants.VT_UI1, new int[] { 0 }, new int[] { ca.length });
			fromCharArray(ca);
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
		}
	}

	protected native void init(int vt, int lbounds[], int celems[])
			throws COMError;

	// not impl
	public int getNumLocks() {
		return 0;
	}

	// convert a VT_UI1 array to string
	public String asString() throws COMError {
		if (getvt() != COMConstants.VT_UI1)
			return null;
		char ja[] = toCharArray();
		return new String(ja);
	}

	// not impl
//	public native Object clone();

	// call this to explicitly release the com object before gc

	public void release() throws COMError {
		destroy();
	}

	public native void destroy() throws COMError;

	public native int getvt() throws COMError;

	public native void reinit(SafeArray sa) throws COMError;

	public native void reinterpretType(int vt) throws COMError;

	public native int getLBound() throws COMError;

	public native int getLBound(int dim) throws COMError;

	public native int getUBound() throws COMError;

	public native int getUBound(int dim) throws COMError;

	public native int getNumDim() throws COMError;

	public native int getFeatures() throws COMError;

	public native int getElemSize();

	public native void fromCharArray(char ja[]) throws COMError;

	public native void fromIntArray(int ja[]) throws COMError;

	public native void fromShortArray(short ja[]) throws COMError;

	public native void fromDoubleArray(double ja[]) throws COMError;

	public native void fromStringArray(String ja[]) throws COMError;

	public native void fromByteArray(byte ja[]) throws COMError;

	public native void fromFloatArray(float ja[]) throws COMError;

	public native void fromBooleanArray(boolean ja[]) throws COMError;

	public native void fromVariantArray(VARIANT ja[]) throws COMError;

	public native char[] toCharArray() throws COMError;

	public native int[] toIntArray() throws COMError;

	public native short[] toShortArray() throws COMError;

	public native double[] toDoubleArray() throws COMError;

	public native String[] toStringArray() throws COMError;

	public native byte[] toByteArray() throws COMError;

	public native float[] toFloatArray() throws COMError;

	public native boolean[] toBooleanArray() throws COMError;

	public native VARIANT[] toVariantArray() throws COMError;

	// char access
	public native char getChar(int sa_idx) throws COMError;

	public native char getChar(int sa_idx1, int sa_idx2) throws COMError;

	public native void setChar(int sa_idx, char c) throws COMError;

	public native void setChar(int sa_idx1, int sa_idx2, char c)
			throws COMError;

	public native void getChars(int sa_idx, int nelems, char ja[], int ja_start)
			throws COMError;

	public native void setChars(int sa_idx, int nelems, char ja[], int ja_start)
			throws COMError;

	// int access
	public native int getInt(int sa_idx) throws COMError;

	public native int getInt(int sa_idx1, int sa_idx2) throws COMError;

	public native void setInt(int sa_idx, int c) throws COMError;

	public native void setInt(int sa_idx1, int sa_idx2, int c) throws COMError;

	public native void getInts(int sa_idx, int nelems, int ja[], int ja_start)
			throws COMError;

	public native void setInts(int sa_idx, int nelems, int ja[], int ja_start)
			throws COMError;

	// short access
	public native short getShort(int sa_idx) throws COMError;

	public native short getShort(int sa_idx1, int sa_idx2) throws COMError;

	public native void setShort(int sa_idx, short c) throws COMError;

	public native void setShort(int sa_idx1, int sa_idx2, short c)
			throws COMError;

	public native void getShorts(int sa_idx, int nelems, short ja[],
			int ja_start) throws COMError;

	public native void setShorts(int sa_idx, int nelems, short ja[],
			int ja_start) throws COMError;

	// double access
	public native double getDouble(int sa_idx) throws COMError;

	public native double getDouble(int sa_idx1, int sa_idx2) throws COMError;

	public native void setDouble(int sa_idx, double c) throws COMError;

	public native void setDouble(int sa_idx1, int sa_idx2, double c)
			throws COMError;

	public native void getDoubles(int sa_idx, int nelems, double ja[],
			int ja_start) throws COMError;

	public native void setDoubles(int sa_idx, int nelems, double ja[],
			int ja_start) throws COMError;

	// string access
	public native String getString(int sa_idx) throws COMError;

	public native String getString(int sa_idx1, int sa_idx2) throws COMError;

	public native void setString(int sa_idx, String c) throws COMError;

	public native void setString(int sa_idx1, int sa_idx2, String c)
			throws COMError;

	public native void getStrings(int sa_idx, int nelems, String ja[],
			int ja_start) throws COMError;

	public native void setStrings(int sa_idx, int nelems, String ja[],
			int ja_start) throws COMError;

	// byte access
	public native byte getByte(int sa_idx) throws COMError;

	public native byte getByte(int sa_idx1, int sa_idx2) throws COMError;

	public native void setByte(int sa_idx, byte c) throws COMError;

	public native void setByte(int sa_idx1, int sa_idx2, byte c)
			throws COMError;

	public native void getBytes(int sa_idx, int nelems, byte ja[], int ja_start)
			throws COMError;

	public native void setBytes(int sa_idx, int nelems, byte ja[], int ja_start)
			throws COMError;

	// float access
	public native float getFloat(int sa_idx) throws COMError;

	public native float getFloat(int sa_idx1, int sa_idx2) throws COMError;

	public native void setFloat(int sa_idx, float c) throws COMError;

	public native void setFloat(int sa_idx1, int sa_idx2, float c)
			throws COMError;

	public native void getFloats(int sa_idx, int nelems, float ja[],
			int ja_start) throws COMError;

	public native void setFloats(int sa_idx, int nelems, float ja[],
			int ja_start) throws COMError;

	// boolean access
	public native boolean getBoolean(int sa_idx) throws COMError;

	public native boolean getBoolean(int sa_idx1, int sa_idx2) throws COMError;

	public native void setBoolean(int sa_idx, boolean c) throws COMError;

	public native void setBoolean(int sa_idx1, int sa_idx2, boolean c)
			throws COMError;

	public native void getBooleans(int sa_idx, int nelems, boolean ja[],
			int ja_start) throws COMError;

	public native void setBooleans(int sa_idx, int nelems, boolean ja[],
			int ja_start) throws COMError;

	// VARIANT access
	public native VARIANT getVariant(int sa_idx) throws COMError;

	public native VARIANT getVariant(int sa_idx1, int sa_idx2) throws COMError;

	public native void setVariant(int sa_idx, VARIANT c) throws COMError;

	public native void setVariant(int sa_idx1, int sa_idx2, VARIANT c)
			throws COMError;

	public native void getVariants(int sa_idx, int nelems, VARIANT ja[],
			int ja_start) throws COMError;

	public native void setVariants(int sa_idx, int nelems, VARIANT ja[],
			int ja_start) throws COMError;

	public String toString() {
		try {

			StringBuffer s = new StringBuffer("");
			int ndim = getNumDim();
			if (ndim == 1) {
				int ldim = getLBound();
				int udim = getUBound();
				for (int i = ldim; i <= udim; i++) {
					VARIANT v = getVariant(i);

					if (((v.getvt() & COMConstants.VT_TYPEMASK) | COMConstants.VT_ARRAY) == v
							.getvt()) {
						s.append("[");
						s.append(v.toSafeArray().toString());
						s.append("]");
						return s.toString();
					} else {
						s.append(" ");
						s.append(v.toString());
					}
				}
			} else if (ndim == 2) {
				int ldim1 = getLBound(1);
				int udim1 = getUBound(1);

				int ldim2 = getLBound(2);
				int udim2 = getUBound(2);

				for (int i = ldim1; i <= udim1; i++) {
					for (int j = ldim2; j <= udim2; j++) {
						VARIANT v = getVariant(i, j);
						s.append(" ");
						s.append(v.toString());
					}
					s.append("\n");
				}
			}
			return s.toString();
		} catch (COMError e) {
			if (RS.getServer() != null)
				RS.getServer().getLog().logerror(e.getMessage(), e);
			return "";
		}
	}

	static {
		System.loadLibrary("COMProxy");
	}

}
