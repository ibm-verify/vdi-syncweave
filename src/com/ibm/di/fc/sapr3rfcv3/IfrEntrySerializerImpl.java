/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfcv3;

import com.sap.conn.jco.*;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.Attribute;

/**
 * Serializes a JCO.Function into RFC Entry/Attribute request data.
 * 
 * The result entry will processed as a series of nested and multivalued
 * attributes representing the name and parameters of a given SAP RFC. The names
 * of the parameters will be encoded according to the rules for ABAP XML
 * serialization (i.e. names will not have characters that could result in badly
 * formed XML). A single attribute will be present representing the name of the
 * RFC. Its values are Attributes representing the names of the import, export,
 * and table parameters of the RFC. For simple ABAP parameter types, an
 * attribute with a single value will be present. For structure types, an
 * attributue with mulitple values representing the fields of the struture. For
 * example, if the structure parameter name is Customer with fields Name and
 * Address, the Attribute syntax should be <br>
 * Customer[Name[Mr Smith], Address[3 High Street]] </br> For table parameters,
 * each row should be represented by an Attribute named <code>itemN</code>,
 * where N represents the row index. For example, if the table is named
 * Customers and represents a repeating structure containing the fields Name and
 * Address, the the Attribute syntax should be <br>
 * Customers[item0[Name[Mr Smith], Address[3 High Street]], item1[Name[Mr
 * Jones], Address[2 Low Street]]] </br>
 * </p>
 */
/*
 * @modelguid {360E3486-8E17-42A0-820F-6535587BBA2E}
 */
final class IfrEntrySerializerImpl implements IfrSerializer {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/* @modelguid {151FF6DF-ECB4-43F8-8051-3D6A1BE55B12} */
	private AbapIfrEncoder abapEncoder;

	/**
	 * The result output target
	 */
	/*
	 * @modelguid {4A970EF4-CE52-4D65-9E45-07ABC0B9772D}
	 */
	private Entry output;

	/* @modelguid {960BB18F-8669-4EC4-AA2F-1766457F4BF2} */
	IfrEntrySerializerImpl(Entry e) {
		super();
		setEntry(e);
		setEncoder(IfrFunctionFactory.createEncoder());
	}

	/* @modelguid {694D8820-DBB0-4FFA-AB54-E530572F0BA9} */
	private void setEntry(Entry e) {
		output = e;
	}

	/* @modelguid {7648153E-99B5-43B9-9002-399E37D246F1} */
	private Entry getEntry() {
		return output;
	}

	/* @modelguid {CAEA4755-1B65-4971-9C41-06E53D8A8F44} */
	private AbapIfrEncoder getEncoder() {
		return abapEncoder;
	}

	/* @modelguid {90E471AC-CC7D-4A45-9E6A-41545F8E2F2A} */
	private void setEncoder(AbapIfrEncoder encoder) {
		if (encoder == null) {
			throw new IllegalArgumentException();
		}
		abapEncoder = encoder;
	}

	/* @modelguid {5BD9BA84-3AE8-46B1-976D-CB8158CBBA7F} */
	public void serialize(JCoFunction func) {
		/* Store the results in a multivalued attribute that has the name output */

		Attribute respAttr = new Attribute(SapR3RfcFCV3.PARAM_OUTPUT);

		Attribute rfcAttr = new Attribute(getEncoder().encode(func.getName())
				+ RESPONSE_SUFFIX);
		JCoParameterList pList = func.getImportParameterList();
		if (pList != null) {
			serialize(rfcAttr, pList);
		}
		pList = func.getExportParameterList();
		if (pList != null) {
			serialize(rfcAttr, pList);
		}
		pList = func.getTableParameterList();
		if (pList != null) {
			serialize(rfcAttr, pList);
		}
		respAttr.addValue(rfcAttr);
		getEntry().setAttribute(respAttr);

	}

	/* @modelguid {7A198E71-FEDA-4403-AC36-854EBBBD150E} */
	void serialize(Attribute a, JCoParameterList pList) {
		JCoFieldIterator fi = pList.getFieldIterator();
		fi.reset();
		while (fi.hasNextField()) {
			JCoField f = fi.nextField();
			Attribute attr = new Attribute(getEncoder().encode(f.getName()));
			if (f.isStructure()) {
				serialize(attr, f.getStructure());
			} else if (f.isTable()) {
				serialize(attr, f.getTable());
			} else {
				serialize(attr, f);
			}
			a.addValue(attr);
		}
	}

	/* @modelguid {6F6C2568-BEE3-4888-AA9F-14AFA9FE179A} */
	void serialize(Attribute a, JCoField field) {
		if (field.getType() == JCoMetaData.TYPE_DATE
				|| field.getType() == JCoMetaData.TYPE_TIME) {
			// strip out formatting characters.
			String rawVal = field.getString();
			StringBuffer result = new StringBuffer();
			for (int i = 0; i < rawVal.length(); ++i) {
				if (Character.isDigit(rawVal.charAt(i))) {
					result.append(rawVal.charAt(i));
				}
			}
			a.addValue(result.toString());
		} else {
			a.addValue(field.getString());
		}
	}

	/* @modelguid {E9EC2069-BE44-4E6E-A0C8-29556B51DC9B} */
	void serialize(Attribute a, JCoRecord rec) {
		JCoFieldIterator fi = rec.getFieldIterator();
		fi.reset();
		while (fi.hasNextField()) {
			JCoField f = fi.nextField();
			Attribute newAttr = new Attribute(getEncoder().encode(f.getName()));
			serialize(newAttr, f);
			a.addValue(newAttr);
		}

	}

	/* @modelguid {48688902-63F0-42F6-B697-8667753256A1} */
	void serialize(Attribute a, JCoStructure struct) {
		serialize(a, (JCoRecord) struct);
	}

	/* @modelguid {41C869FB-27EA-44EB-BD80-B516DC104A0A} */
	void serialize(Attribute a, JCoTable tab) {
		for (int i = 0; i < tab.getNumRows(); ++i) {
			tab.setRow(i);
			Attribute newAttr = new Attribute("item" + i);
			serialize(newAttr, (JCoRecord) tab);
			a.addValue(newAttr);
		}
	}
}
