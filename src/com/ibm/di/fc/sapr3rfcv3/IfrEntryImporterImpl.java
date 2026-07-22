/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import com.sap.conn.jco.*;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.Attribute;

import java.util.Vector;

/**
 * Input Entry will be processed as a series of nested and multivalued
 * attributes representing the name and parameters of a given SAP RFC. The names
 * of the parameters must be encoded according to the rules for ABAP XML
 * serialization (i.e. names will not have characters that could result in badly
 * formed XML). The string at index 0 should be present representing the name of
 * the RFC. At index 1, the Attributes represent the names of the import,
 * export, and table parameters of the RFC. For simple ABAP parameter types, an
 * attribute with a single value should be present. For structure types, an
 * attribute with multiple values representing the fields of the structure. For
 * example, if the structure parameter name is Customer with fields Name and
 * Address, the Attribute syntax should be <br>
 * Customer[Name[Mr Smith], Address[3 High Street]] </br> For table parameters,
 * each row should be represented by an Attribute named <code>itemN</code>,
 * where N represents the row index. For example, if the table is named
 * Customers and represents a repeating structure containing the fields Name and
 * Address, the the Attribute syntax should be <br>
 * Customers[item0[Name[Mr Smith], Address[3 High Street]], item1[Name[Mr
 * Jones], Address[2 Low Street]]] </br>
 * 
 */
final class IfrEntryImporterImpl implements IfrImporter {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private AbapIfrEncoder abapEncoder;

	private Entry reqEntry;

	private IfrEntryImporterImpl() {
		super();
		setEncoder(IfrFunctionFactory.createEncoder());
	}

	IfrEntryImporterImpl(Entry e) {
		this();
		setEntry(e);
	}

	private void setEntry(Entry e) {
		reqEntry = e;
	}

	private Entry getEntry() {
		return reqEntry;
	}

	private AbapIfrEncoder getEncoder() {
		return abapEncoder;
	}

	private void setEncoder(AbapIfrEncoder encoder) {
		if (encoder == null) {
			throw new IllegalArgumentException();
		}
		abapEncoder = encoder;
	}

	/*
	 * The function name is retrieved from the Entry. We use the as the first
	 * value of the attributes. (non-Javadoc)
	 * 
	 * @see com.ibm.di.fc.sapr3rfcv3.IfrImporter#getFunctionName()
	 */
	public String getFunctionName() {
		String result = "";

		Attribute param = getEntry().getAttribute(SapR3RfcFCV3.PARAM_INPUT);
		if (param != null) {
			/* this should be the name of the RFC */
			Object funcObj = param.getValue(0);
			if (funcObj instanceof Attribute) {
				result = ((Attribute) funcObj).getName();
			}
		}

		return result;
	}

	public void importData(JCoFunction func) throws IfrImporterException {
		if (!func.getName().equals(getFunctionName())) {
			Object[] msgArgs = new Object[] { func.getName(), getFunctionName() };
			String msg = LogMessageHelper.getMsgResource().getMessage(
					LogMessageHelper.SAPR3_RFCFC_0011, msgArgs);
			throw new IfrImporterException(msg);
		}

		/*
		 * We want to get the parameter information as an additional value of
		 * the parameter. The first value is the RFC function name, the second
		 * value is the MVA with parameters.
		 */
		Attribute attr = (Attribute) getEntry().getAttribute(
				SapR3RfcFCV3.PARAM_INPUT).getValue(0);

		if (func.getImportParameterList() != null) {
			importData(func.getImportParameterList(), attr);
		}

		if (func.getTableParameterList() != null) {
			importData(func.getTableParameterList(), attr);
		}

	}

	private Attribute findChildAttr(String name, Attribute parent) {
		Attribute result = null;
		Vector attrValues = parent.getValuesVector();
		for (int i = 0; i < attrValues.size(); ++i) {
			Object o = attrValues.get(i);
			if (o instanceof com.ibm.di.entry.Attribute) {
				Attribute a = (Attribute) o;
				if (a.getName().equals(name)) {
					result = a;
					break;
				}
			}
		}

		return result;
	}

	void importData(JCoParameterList pList, Attribute a)
			throws IfrImporterException {
		JCoFieldIterator fi = pList.getFieldIterator();
		JCoListMetaData metadata = pList.getListMetaData();
		fi.reset();
		while (fi.hasNextField()) {
			JCoField f = fi.nextField();
			String fieldName = f.getName();
			String encodedName = getEncoder().encode(f.getName());
			Attribute paramAttr = findChildAttr(encodedName, a);
			if (paramAttr != null) {
				if (f.isStructure()) {
					importData(f.getStructure(), paramAttr);
					// import structure
				} else if (f.isTable()) {
					importData(f.getTable(), paramAttr); // import table
				} else {
					importData(f, paramAttr); // import simple field
				}
			} else {
				if (!metadata.isOptional(fieldName) && metadata.isImport(fieldName)) {
					Object[] msgArgs = new Object[] { f.getName() };
					String msg = LogMessageHelper.getMsgResource().getMessage(
							LogMessageHelper.SAPR3_RFCFC_0012, msgArgs);
					throw new IfrImporterException(msg);
				}
			}
		}
	}

	void importData(JCoField field, Attribute a) throws IfrImporterException {
		field.setValue(a.getValue());
	}

	void importData(JCoRecord rec, Attribute a) throws IfrImporterException {
		JCoFieldIterator fi = rec.getFieldIterator();
		fi.reset();
		while (fi.hasNextField()) {
			JCoField f = fi.nextField();
			String encodedName = getEncoder().encode(f.getName());
			Attribute attr = findChildAttr(encodedName, a);
			if (attr != null) {
				importData(f, attr); // call JCO.Field overload
			}
		}
	}

	void importData(JCoTable tab, Attribute a) throws IfrImporterException {
		int i = 0;
		Attribute itemAttr = null;
		while ((itemAttr = findChildAttr(ITEM + i, a)) != null) {
			tab.appendRow();
			importData((JCoRecord) tab, itemAttr);
			i++;
		}
	}

	void importData(JCoStructure struct, Attribute a)
			throws IfrImporterException {
		importData((JCoRecord) struct, a);
	}

}
