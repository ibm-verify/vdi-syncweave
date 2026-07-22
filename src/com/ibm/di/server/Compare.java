/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// Compare.java
//
//
//
package com.ibm.di.server;

import java.util.*;
import com.ibm.di.entry.*;

/**
 * This class is used for making comparisons of entries and attributes.
 * 
 */
public class Compare {
	/**
	 * Copyright information.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * A constant indicating a String.
	 */
	final static int SYNTAX_STRING = 0;

	/**
	 * A constant indicating a dn.
	 */
	final static int SYNTAX_DN = 1;

	/**
	 * A constant indicating a Date.
	 */
	final static int SYNTAX_DATE = 2;

	/**
	 * Adds all values from the second Attribute object to the first one (if
	 * they are not already present).
	 * 
	 * @param a
	 *            the attribute that will accept additional values
	 * @param b
	 *            the attribute whose values will be added to the first one
	 * @throws Exception
	 *             if a problem occurs
	 */
	public void join(Attribute a, Attribute b) throws Exception {
		for (int i = 0; i < b.size(); i++) {
			Object obj = b.getValue(i);
			if (!a.contains(obj))
				a.addValue(obj);
		}
	}

	/**
	 * Compares two Attribute objects and returns if they are different.
	 * 
	 * @param a
	 *            first Attribute object
	 * @param b
	 *            second Attribute object
	 * @return <code>true</code> if the attributes are different, otherwise
	 *         <code>false</code>
	 * @throws Exception
	 *             if a problem occurs
	 */
	public boolean differs(Attribute a, Attribute b) throws Exception {
		if (a == null && b == null)
			return false;

		if (a == null || b == null)
			return true;

		int size = a.size();

		if (b.size() != size)
			return true;

		// Some special cases
		if (size == 0)
			return false;

		for (int i = 0; i < size; i++) {
			if (a.getValueOper(i) == AttributeValue.AV_DELETE)
				return true;
		}

		boolean dn = a.getName().equalsIgnoreCase("$dn");

		if (size == 1)
			return !equals(a.getValue(0), b.getValue(0), dn);

		// more than one value but both attributes have same size()
		Vector<Object> bvalues = b.getValuesVector();

		for (int i = 0; i < size; i++) {
			if (!vectorContains(a.getValue(i), bvalues, dn))
				return true;
		}

		return false;
	}

	/**
	 * Return true if and only if a Vector contains a value, and remove that
	 * value from the Vector. Look into the values if they are complex. If dn is
	 * true, also compact Strings (by removing some spaces).
	 * 
	 * @param val
	 *            an Object that will be searched
	 * @param v
	 *            the vector whose contents will be checked
	 * @param dn
	 *            determines whether strings need to be compact or not
	 * @return <code>true</code> if the vector contains the given Object,
	 *         otherwise <code>false</code>
	 */
	public boolean vectorContains(Object val, Vector<Object> v, boolean dn) {
		for (int i = 0; i < v.size(); i++) {
			if (equals(v.get(i), val, dn)) {
				v.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if and only if two Objects are equal. Look into the Objects
	 * if they are complex. If dn is true, also compact Strings (by removing
	 * some spaces).
	 * 
	 * @param a
	 *            first Object
	 * @param b
	 *            second Object
	 * @param dn
	 *            determines whether strings need to be compact or not
	 * @return <code>true</code> if Objects are equal, otherwise
	 *         <code>false</code>
	 */
	private boolean equals(Object a, Object b, boolean dn) {

		// Same object?
		if (a == b)
			return true;

		if (a == null || b == null)
			return false;

		if (dn) {
			// Comparing $dn, both must be a String.
			return equalsDN(a.toString(), b.toString());
		}

		// Two strings?
		if (a instanceof String && b instanceof String)
			return a.equals(b);

		// Special case for arrays ( as we need to look inside them )
		if (a instanceof byte[])
			return compareByteArray((byte[]) a, b);
		else if (a instanceof char[])
			return compareCharArray((char[]) a, b);
		else if (a instanceof int[])
			return compareIntArray((int[]) a, b);
		else if (a instanceof long[])
			return compareLongArray((long[]) a, b);
		// If both objects are of same object class (and not an array) then it
		// should be ok to call equals method?
		else if (a.getClass() == b.getClass())
			return a.equals(b);
		else if (b instanceof byte[])
			return compareByteArray((byte[]) b, a);
		else if (b instanceof char[])
			return compareCharArray((char[]) b, a);
		else
		// Catch all: compare using toString()
		if (a.toString() != null) {
			return a.toString().equals(b.toString());
		} else {
			return false;
		}
	}

	/**
	 * Compares two byte arrays.
	 * 
	 * @param a
	 *            a byte array used for the comparison
	 * @param o
	 *            an Object that will be compared to the byte array
	 * @return <code>true</code> if the two byte[] are equal, otherwise
	 *         <code>false</code>
	 */
	private boolean compareByteArray(byte[] a, Object o) {
		if (o instanceof String)
			return new String(a).equals(o);

		if (!(o instanceof byte[]))
			return false;

		byte[] b = (byte[]) o;

		if (a.length != b.length)
			return false;

		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;

		return true;
	}

	/**
	 * Compares two int arrays.
	 * 
	 * @param a
	 *            int array used for the comparison
	 * @param o
	 *            an Object that will be compared to the int array
	 * @return <code>true</code> if the two int[] are equal, otherwise
	 *         <code>false</code>
	 */
	private boolean compareIntArray(int[] a, Object o) {
		if (!(o instanceof int[]))
			return false;

		int[] b = (int[]) o;

		if (a.length != b.length)
			return false;

		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;

		return true;
	}

	/**
	 * Compares two long arrays.
	 * 
	 * @param a
	 *            long array used for the comparison
	 * @param o
	 *            an Object that will be compared to the long array
	 * @return <code>true</code> if the two long[] are equal, otherwise
	 *         <code>false</code>
	 */
	private boolean compareLongArray(long[] a, Object o) {
		if (!(o instanceof long[]))
			return false;

		long[] b = (long[]) o;

		if (a.length != b.length)
			return false;

		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;

		return true;
	}

	/**
	 * Compares two char arrays.
	 * 
	 * @param a
	 *            char array used for the comparison
	 * @param o
	 *            an Object that will be compared to the char array
	 * @return <code>true</code> if the two char[] are equal, otherwise
	 *         <code>false</code>
	 */
	private boolean compareCharArray(char[] a, Object o) {
		if (o instanceof String)
			return new String(a).equals(o);

		if (!(o instanceof char[]))
			return false;

		char[] b = (char[]) o;

		if (a.length != b.length)
			return false;

		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;

		return true;
	}

	/**
	 * Compares two given Entry objects and returns an ArrayList with the
	 * differences (attributes that were in one of the entries and not the
	 * other).
	 * 
	 * @param a
	 *            first Entry object
	 * @param b
	 *            second Entry object
	 * @return a list of the differences found during the comparison
	 * @throws Exception
	 *             if a problem occurs
	 */
	public ArrayList<ModificationItem> compareEntries(Entry a, Entry b)
			throws Exception {

		ArrayList<ModificationItem> mdList = new ArrayList<ModificationItem>();

		/*
		 * Find all attributes in both a and b with different values
		 */
		for (String name : a.getAttributeNames()) {
			Attribute aa = a.getAttribute(name);
			Attribute bb = b.getAttribute(name);

			if (!name.equals(aa.getName())) {
				aa = aa.clone();
				aa.setName(name);
			}
			
			if (bb == null) {
				if (aa.size() > 0) {
					mdList.add(new ModificationItem(
							ModificationItem.ADD_ATTRIBUTE, aa));
				}
				continue;
			}

			if (differs(aa, bb)) {
				if (aa.size() < 1) {
					mdList.add(new ModificationItem(
							ModificationItem.REMOVE_ATTRIBUTE, aa));
				} else {
					mdList.add(new ModificationItem(
							ModificationItem.REPLACE_ATTRIBUTE, aa));
				}
			}
		}

		/*
		 * Find all attributes in B which is not in A (deleted attributes)
		 */
		for (String name : b.getAttributeNames()) {
			if (a.getAttribute(name) == null) {
				mdList.add(new ModificationItem(
								ModificationItem.REMOVE_ATTRIBUTE, b
										.getAttribute(name)));
			}
		}

		return mdList;
	}

	/**
	 * Applies a given ArrayList of modifications to an Entry object and returns
	 * it.
	 * 
	 * @param source
	 *            a source Entry
	 * @param mods
	 *            ArrayList of modifications
	 * @return the updated Entry
	 */
	public static Entry applyMods(Entry source, ArrayList<ModificationItem> mods) {
		Entry entry = new Entry();

		// entry = source.duplicate();

		for (ModificationItem m : mods) {

			String attr = m.getAttribute().getName();

			switch (m.getModificationOp()) {
			case ModificationItem.REMOVE_ATTRIBUTE:
				entry.removeAttribute(attr);
				entry.newAttribute(attr);
				break;
			default:
				entry.setAttribute(m.getAttribute());
				break;
			}
		}

		return entry;
	}

	/**
	 * Checks if two strings are equal. Insignificant space characters are
	 * ignored.
	 * 
	 * @param p1
	 *            first string
	 * @param p2
	 *            second string
	 * @return <code>true<code> it the strings are equal, otherwise <code>false</code>
	 */
	public boolean equalsDN(String p1, String p2) {
		// Quick comparison
		if (p1.compareToIgnoreCase(p2) == 0)
			return true;

		// Trim insignificant spaces
		String d1 = compactDN(p1);
		String d2 = compactDN(p2);

		return (d1.compareToIgnoreCase(d2) == 0);
	}

	/**
	 * Removes spaces in a given string, including those at the string
	 * beginning, end and those following a comma character.
	 * 
	 * @param p1
	 *            the String to be processed
	 * @return the comacted String
	 */
	public String compactDN(String p1) {
		StringBuffer str = new StringBuffer();
		int i = 0;
		while (i < p1.length()) {
			str.append(p1.charAt(i));
			if (p1.charAt(i) == ',') {
				while (p1.charAt(i + 1) == ' ') {
					i++;
				}
			}
			i++;
		}
		return str.toString().trim();
	}

}
