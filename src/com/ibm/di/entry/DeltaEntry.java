/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.ibm.di.server.Compare;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements static methods to perform Delta operations on Entries
 * and Attributes.
 */
public class DeltaEntry {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static int COMPARE_ATTRIBUTE_KEY = 1;

	public final static int COMPARE_ATTRIBUTE_NAMES = 2;

	public final static int COMPARE_ATTRIBUTE_VALUES = 3;

	private static Compare compare = new Compare();

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);
	
	private static final boolean newValuesFirst = Boolean.getBoolean("com.ibm.di.applyDelta.newValuesFirst");

	/**
	 * This method is a convenience method that calls
	 * <code>compareEntries(source, target, false, null);</code>
	 *
	 * @param source
	 *            The source entry
	 * @param target
	 *            The target entry
	 *
	 * @return Entry with delta information
	 */
	public static Entry getDeltaEntry(Entry source, Entry target) throws Exception {
		return compareEntries(source, target, false, COMPARE_ATTRIBUTE_VALUES, null);
	}

	/**
	 * This method returns an Entry that contains the delta changes needed to
	 * make target equal to source. If the <i>unchanged</i> parameter is true,
	 * then unchanged attributes and values are returned as part of the
	 * resulting entry. The <i>level</i> parameter determines how deep the
	 * comparison goes.
	 *
	 * <h3>Level 1 Comparison</h3> A level 1 comparison requires the <i>key</i>
	 * parameter and sets the result Entry's operation code to OP_ADD, OP_DEL,
	 * OP_MOD or OP_UNCHANGED. The result Entry will contain no Attributes.
	 *
	 * <h3>Level 2 Comparison</h3> A level 2 comparison (includes level 1 if key
	 * parameter is specified) compares attribute names in the two entries. Each
	 * attribute in the result entry has its operation code set to ATTRIBUTE_ADD
	 * or ATTRIBUTE_DELETE. If the <i>unchanged</i> parameter is true, then all
	 * unchanged attributes are returned as well with the operation code set to
	 * ATTRIBUTE_UNCHANGED. The Attributes in the result Entry will contain no
	 * values.
	 *
	 * <h3>Level 3 Comparison</h3> Level 3 comparison implicitly includes level
	 * 2 and compares values for those attribute whose operation code is
	 * ATTRIBUTE_UNCHANGED (e.g. named attribute exists in both entries). In
	 * this case, the result Entry will have Attributes with values. Each value
	 * in the resulting attribute has its operation code set to AV_ADD,
	 * AV_UNCHANGED or AV_DELETE. If there are changes in the value sets of the
	 * two entries, the resulting attribute's operation code is changed to
	 * ATTRIBUTE_MOD. If the Attribute name is "$dn", logic will be applied to
	 * compare the values as LDAP Distinguished Names.
	 * <p>
	 * After comparing two entries at level 3, you can apply the returned delta
	 * entry to make target equal to source by calling the <i>applyDelta (
	 * target, delta )</i> method.
	 * <p>
	 * If no changes are detected an empty Entry is returned.
	 *
	 * @param source
	 *            The source entry
	 * @param target
	 *            The target entry
	 * @param unchanged
	 *            If true, returned entry contains unmodified attributes and
	 *            values as well
	 * @param level
	 *            The number of levels to compare: 1 entry (requires key), 2
	 *            attribute names, 3 attribute values
	 * @param key
	 *            The name of the key attribute. This parameter must be
	 *            specified if level == 1.
	 * @return Entry with delta information
	 */
	public static Entry compareEntries(Entry source, Entry target,
			boolean unchanged, int level, String key) throws Exception {

		Entry result = new Entry();

		if (source == null || target == null) {
			result = (source != null ? source.clone() : target.clone());
			result.setOp(source != null ? Entry.OP_DEL : Entry.OP_ADD);
			return result;
		}

		// Level 1 - Compare key attribute values and set Entry operation
		if (key == null && level == COMPARE_ATTRIBUTE_KEY) {
			throw new Exception(sResHash.getString(
					"MISERVER.DELTAENTRY.CANNOT.COMPARE.AT.LEVEL.ONE"));
		}

		char resultOp = Entry.OP_UNCHANGED;

		if (key != null) {
			String a = source.getString(key);
			String b = target.getString(key);
			if (a == null && b == null)
				throw new Exception(sResHash.getString(
						"MISERVER.DELTAENTRY.KEY.ATTRIBUTES.AT.LEVEL.ARE.NULL"));

			if (a == null)
				resultOp = Entry.OP_DEL;
			else if (b == null)
				resultOp = Entry.OP_ADD;
			else if (a.equals(b))
				resultOp = Entry.OP_UNCHANGED;
			else
				resultOp = Entry.OP_MOD;
		}

		// Level 2 - Compare attribute names
		if (level >= COMPARE_ATTRIBUTE_NAMES) {
			Collection<String> a = source.getAttributeCollection();
			Collection<String> b = target.getAttributeCollection();

			// Added and Unchanged attribute names
			for (Iterator<String> i = a.iterator(); i.hasNext();) {
				String an = i.next();
				if (target.getAttribute(an) == null) {
					result.newAttribute(an).setOper(Attribute.ATTRIBUTE_ADD);
					resultOp = Entry.OP_MOD;
				} else if (unchanged || level > 2) {
					result.newAttribute(an).setOper(
							Attribute.ATTRIBUTE_UNCHANGED);
				}
			}

			// Deleted attribute names
			for (Iterator<String> i = b.iterator(); i.hasNext();) {
				String bn = i.next();
				if (source.getAttribute(bn) == null) {
					result.newAttribute(bn).setOper(Attribute.ATTRIBUTE_DELETE);
					resultOp = Entry.OP_MOD;
				}
			}
		}

		// Level 3 - Compare attribute values
		if (level >= 3) {

			Collection<String> src = result.getAttributeCollection();

			for (Iterator<String> i = src.iterator(); i.hasNext();) {

				String av = i.next();
				Attribute attr = result.getAttribute(av);
				Attribute srca = source.getAttribute(av);
				Attribute dsta = target.getAttribute(av);
				Vector<Object> srcv = null;
				Vector<Object> dstv = null;
				boolean dn = av.equalsIgnoreCase("$dn");

				if (srca != null)
					srcv = srca.getValuesVector();
				if (dsta != null)
					dstv = dsta.getValuesVector();

				switch (attr.getOper()) {
				case Attribute.ATTRIBUTE_UNCHANGED:

					// Find added and unchanged values
					for (int index = 0; index < srcv.size(); index++) {
						if (compare.vectorContains(srcv.get(index), dstv, dn)) {
							if (unchanged)
								attr.addValue(srcv.get(index),
										AttributeValue.AV_UNCHANGED);
						} else {
							attr.addValue(srcv.get(index),
									AttributeValue.AV_ADD);
							attr.setOper(Attribute.ATTRIBUTE_MOD);
							resultOp = Entry.OP_MOD;
						}
					}

					// Remaining values in DSTV are deleted
					if (dstv.size() > 0) {
						for (int index = 0; index < dstv.size(); index++) {
							attr.addValue(dstv.get(index),
									AttributeValue.AV_DELETE);
						}
						attr.setOper(Attribute.ATTRIBUTE_MOD);
						resultOp = Entry.OP_MOD;
					}

					if (!unchanged
							&& attr.getOper() == Attribute.ATTRIBUTE_UNCHANGED)
						result.removeAttribute(attr.getName());

					break;

				case Attribute.ATTRIBUTE_ADD:
					for (int j = 0; j < srcv.size(); j++)
						attr.addValue(srcv.get(j), AttributeValue.AV_ADD);
					resultOp = Entry.OP_MOD;
					break;

				case Attribute.ATTRIBUTE_DELETE:
					if (dstv != null) {
						for (int j = 0; j < dstv.size(); j++)
							attr
									.addValue(dstv.get(j),
											AttributeValue.AV_DELETE);
					}
					resultOp = Entry.OP_MOD;
					break;
				}
			}
		}

		result.setOp(resultOp);

		return result;
	}

	/**
	 * This method returns an Entry that contains delta changes for specific
	 * attributes.
	 *
	 * @param source
	 *            The source entry
	 * @param target
	 *            The target entry
	 * @param unchanged
	 *            If <code>true</code>, returned entry contains unmodified
	 *            Attributes and values
	 * @param level
	 *            The number of levels to compare: 1 entry (requires key), 2
	 *            attribute names, 3 attribute values
	 * @param key
	 *            The name of the key attribute. This parameter must be
	 *            specified if level == 1.
	 * @param attributeList
	 *            List of attributes whose changes will be either detected or
	 *            ignored
	 * @param isInvertedIgnore
	 *            If <code>true</code> changes only in attributes listed in
	 *            <code>attributeList</code> parameter will be detected; else
	 *            changes in these attributes will be ignored.
	 * @return Entry with delta information
	 *
	 * @see #compareEntries(Entry, Entry, boolean, int, String)
	 * @since TDI 7.1
	 */
	public static Entry compareEntries(Entry source, Entry target, boolean unchanged, int level, String key,
			List<String> attributeList, boolean isInvertedIgnore) throws Exception {
		Entry result = compareEntries(source, target, unchanged, level, key);

		// No attributes to ignore changes in.
		if (attributeList == null || (attributeList.isEmpty() && !isInvertedIgnore)) {
			return result;
		}

		if (level >= COMPARE_ATTRIBUTE_NAMES) {
			Collection<String> resultAttrNames = result.getAttributeCollection();
			boolean hasOtherModifiedAttr = false;

			for (Iterator<String> i = resultAttrNames.iterator(); i.hasNext();) {
				String resultAttrName = i.next();
				char resultOper = result.getAttribute(resultAttrName).getOper();

				// When isInvertedIgnore is true we are ignoring changes in all
				// attributes EXCEPT those in attributeList variable
				boolean ignoreChangesInAttr = (isInvertedIgnore ^ attributeList.contains(resultAttrName));

				switch (resultOper) {
				case Attribute.ATTRIBUTE_ADD:
				case Attribute.ATTRIBUTE_MOD:
				case Attribute.ATTRIBUTE_REPLACE:
					if (ignoreChangesInAttr) {
						Attribute attr = source.getAttribute(resultAttrName).clone();
						attr.setOper(Attribute.ATTRIBUTE_UNCHANGED);
						result.setAttribute(attr);
						continue;
					} else {
						hasOtherModifiedAttr = true;
					}
					break;
				case Attribute.ATTRIBUTE_DELETE:
					if (ignoreChangesInAttr) {
						result.removeAttribute(resultAttrName);
					} else {
						hasOtherModifiedAttr = true;
					}
				}
			}

			// The source entry is not changed so return it
			if (!hasOtherModifiedAttr) {
				return source;
			}
		}
		return result;
	}

	/**
	 * Forwards the call to applyDelta(target, delta, false).
	 */
	public static void applyDelta(Entry target, Entry delta) throws Exception {
		applyDelta(target, delta, false);
	}

	/**
	 * This method applies the delta operations specified in <i>delta</i> to the
	 * <i>target</i> entry. After completing the operation, the <i>target</i> is
	 * modified to reflect the delta operations. Attributes from the delta are
	 * referenced and not cloned so subsequent changes to the delta entry may
	 * affect the target entry as well.
	 * <p>
	 * Only attributes and values with explicit change operations are processed.
	 * Attributes with the ATTRIBUTE_UNCHANGED flag and values with either
	 * AV_UNDEFINED or AV_UNCHANGED are not processed.
	 * <p>
	 *
	 * @param target
	 *            The entry to which changes are applied
	 * @param delta
	 *            The entry that contains the changes to apply
	 * @param removeUnchanged
	 *            If true, the unmodified attributes are removed from the target
	 *            entry leaving only modified attributes
	 */

	public static void applyDelta(Entry target, Entry delta,
			boolean removeUnchanged)throws Exception {

		String[] names = delta.getAttributeNames();

		for (int i = 0; i < names.length; i++) {
			Attribute attr = delta.getAttribute(names[i]);
			switch (attr.getOper()) {
			case Attribute.ATTRIBUTE_ADD:
				target.setAttribute((Attribute) attr.clone());
				break;
			case Attribute.ATTRIBUTE_DELETE:
				target.setAttribute(attr);
				break;
			case Attribute.ATTRIBUTE_UNCHANGED:
				if (removeUnchanged)
					target.removeAttribute(names[i]);
				break;
			case Attribute.ATTRIBUTE_MOD:
				applyAttributeDelta(target.newAttribute(names[i]), attr);
				break;
			case Attribute.ATTRIBUTE_REPLACE:
				//L3 changes Defect # 11923.
				if( removeUnchanged && !compare.differs(attr, target.getAttribute(names[i])))
					target.removeAttribute(names[i]);
				else
					target.setAttribute(attr);
				break;
			}
		}

	}

		/**
		 * Removes one instance of a value from an Attribute
		 * @param target - The Attribute where we remove one value
		 * @param value - The value to remove
		 */
		private static void removeOneValue(Attribute target, Object value) {
			for (int i = 0; i < target.size(); i++) {
				Object val = target.getValue(i);
				if (val != null ? val.equals(value) : value == null) {
					target.removeValueAt(i);
					return;
				}
			}
			// We did not find the value with the specified casing, try ignoring case
			if (value instanceof String || value instanceof Number) {
				String delValue = value.toString();
				for (int i = 0; i < target.size(); i++) {
					Object val = target.getValue(i);
					if (val != null && delValue.equalsIgnoreCase(val.toString())) {
						target.removeValueAt(i);
						return;
					}
				}
			}
		}

	/**
	 * Method removed for defect # 11923.
	 * This method has been added for specific case when source does not do
	 * delta tagging at attribute level and we need to compare it to

	private static boolean isEqualAttribute(Attribute source, Attribute target) {
		if (source == null || target == null)
			return source == target;
		int i = source.size();
		if (target.size() != i)
			return false;
		for(int j = 0; j < i; j++)
     		{
           	  if(source.getValueOper(j) == AttributeValue.AV_DELETE)
                     	return false;
         	  if(!source.getValue(j).equals(target.getValue(j)))
             		return false;
     		}

		return true;
	}
	*/

	/**
	 * This method applies the change operations in <code>delta</code> to the
	 * <code>target</code> attribute. Value operations with either AV_UNDEFINED
	 * or AV_UNCHANGED are ignored.
	 *
	 * @param target
	 *            The attribute to modify
	 * @param delta
	 *            The attribute containing value operations
	 */
	public static void applyAttributeDelta(Attribute target, Attribute delta) {

		if (target == null)
			return;

		for (int i = 0; i < delta.size(); i++) {
			switch (delta.getValueOper(i)) {
			case AttributeValue.AV_UNDEFINED:
				break;
			case AttributeValue.AV_ADD:
				if (newValuesFirst)
					target.addValue(0, delta.getValue(i));
				else
					target.addValue(delta.getValue(i));
				break;
			case AttributeValue.AV_DELETE:
				//target.removeValue(delta.getValue(i));
				removeOneValue(target, delta.getValue(i));
				break;
			case AttributeValue.AV_UNCHANGED:
				break;
			}
		}
	}

}
