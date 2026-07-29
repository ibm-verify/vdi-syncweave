/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import com.sap.mw.jco.JCO;

import java.io.PrintWriter;

/**
 * <p>
 * Serializes JCO.Functions into XML streams. This implementation requires a
 * PrintWriter at construction. The Printwriter is then used as the output
 * target for the serialized RFC parameter data.
 * </p>
 * <p>
 * This class serilizes functions into XML conforming to the specifications for
 * XML ABAP serialization, {@see http://ifr.sap.com}. An AbapIfrEncoder ensures
 * well formed XML.
 * </p>
 * 
 * @modelguid {9FB32384-1444-47D6-B139-3A19C5A42D2A}
 */
final class IfrXmlSerializerImpl implements IfrSerializer {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/* @modelguid {5D2588AE-26B6-4EC1-AA26-1BF0B2AC0BC7} */
	public static final String NAMESPACE_PREFIX = "doc:";

	/* @modelguid {A38FA933-2400-49CF-87CD-610E918FAC71} */
	public static final String NAMESPACE_URI = "xmlns:doc=\"urn:sap-com:document:sap:business:rfc\"";

	/* @modelguid {9128B465-6E05-4834-9D0A-4FE5C4F04D1F} */
	private static final String BEGIN_CDATA = "<![CDATA[";

	/* @modelguid {C3D140C4-D5FB-44DB-8BDB-0CFB7F997551} */
	private static final String END_CDATA = "]]>";

	/*
	 * Encode XML tag names to IFR spec. Ensure well formed XML @modelguid
	 * {B1457000-03E3-4B5B-9ED1-5E036E557CD3}
	 */
	private AbapIfrEncoder abapEncoder;

	/*
	 * The output target stream @modelguid
	 * {F569B0D7-2A5E-447B-840D-67982268D951}
	 */
	private PrintWriter output;

	/* @modelguid {03559651-C95F-435C-8DAE-78AF43A5B015} */
	IfrXmlSerializerImpl(PrintWriter pw) {
		super();
		setWriter(pw);
		setEncoder(IfrFunctionFactory.createEncoder());
	}

	/* @modelguid {C081C1D5-44C9-40E7-84D0-FF898C0409CF} */
	private void setWriter(PrintWriter pw) {
		output = pw;
	}

	/* @modelguid {F0ACA307-3DB4-4178-82E5-007B95727EF9} */
	private PrintWriter getWriter() {
		return output;
	}

	/* @modelguid {85D52E37-9BBA-4490-9D7D-AE5160AD278C} */
	private AbapIfrEncoder getEncoder() {
		return abapEncoder;
	}

	/* @modelguid {44E04F49-19BB-4778-B3B1-670A61F55074} */
	private void setEncoder(AbapIfrEncoder encoder) {
		if (encoder == null) {
			throw new IllegalArgumentException();
		}
		abapEncoder = encoder;
	}

	/* @modelguid {E687E118-818D-4BE4-89B1-1DAEC4C44428} */
	private void emmitStartDocument(PrintWriter pw, String name) {
		pw.print("<");
		pw.print(NAMESPACE_PREFIX);
		pw.print(getEncoder().encode(name));
		pw.print(RESPONSE_SUFFIX);
		pw.print(" ");
		pw.print(NAMESPACE_URI);
		pw.print(">");
	}

	/* @modelguid {B3110BF5-95D8-4B59-A9C0-D6EA0CDFA7B6} */
	private void emmitEndDocument(PrintWriter pw, String name) {
		pw.print("</");
		pw.print(NAMESPACE_PREFIX);
		pw.print(getEncoder().encode(name));
		getWriter().print(RESPONSE_SUFFIX);
		getWriter().print(">");
	}

	/* @modelguid {BA30C1C2-5EDE-4EFA-9701-319715DB07E5} */
	private void emmitStartTag(String name) {
		getWriter().print("<");
		getWriter().print(getEncoder().encode(name));
		getWriter().print(">");
	}

	/* @modelguid {8F1FD30A-1003-4FCB-9ADE-D6F83F8C6880} */
	private void emmitEndTag(String name) {
		getWriter().print("</");
		getWriter().print(getEncoder().encode(name));
		getWriter().print(">");
	}

	/* @modelguid {04D7666E-261F-4F38-9D8A-06B965D7C502} */
	public void serialize(JCO.Function func) {
		emmitStartDocument(getWriter(), func.getName());
		if (func.getImportParameterList() != null) {
			serialize(func.getImportParameterList());
		}

		if (func.getExportParameterList() != null) {
			serialize(func.getExportParameterList());
		}

		if (func.getTableParameterList() != null) {
			serialize(func.getTableParameterList());
		}
		emmitEndDocument(getWriter(), func.getName());
	}

	/* @modelguid {1DC15AE0-0933-48BB-8976-3B1F966E6803} */
	void serialize(JCO.ParameterList pList) {
		JCO.FieldIterator fi = pList.fields();
		fi.reset();
		while (fi.hasMoreFields()) {
			JCO.Field f = fi.nextField();
			if (f.isStructure()) {
				emmitStartTag(f.getName());
				serialize(f.getStructure());
				emmitEndTag(f.getName());
			} else if (f.isTable()) {
				emmitStartTag(f.getName());
				serialize(f.getTable());
				emmitEndTag(f.getName());
			} else {
				serialize(f);
			}
		}
	}

	/* @modelguid {1511C08F-AAF7-4E10-B97A-34AAFD65E70A} */
	void serialize(JCO.Field field) {
		emmitStartTag(field.getName());
		getWriter().print(BEGIN_CDATA);
		if (field.getType() == JCO.TYPE_DATE
				|| field.getType() == JCO.TYPE_TIME) {
			// strip out formatting characters.
			String rawVal = field.getString();
			StringBuffer result = new StringBuffer();
			for (int i = 0; i < rawVal.length(); ++i) {
				if (Character.isDigit(rawVal.charAt(i))) {
					result.append(rawVal.charAt(i));
				}
			}
			getWriter().print(result.toString());
		} else {
			getWriter().print(field.getString());
		}
		getWriter().print(END_CDATA);
		emmitEndTag(field.getName());
	}

	/* @modelguid {048CF2BC-C67D-4ED0-B651-84F2A8727BB1} */
	void serialize(JCO.Record rec) {
		JCO.FieldIterator fi = rec.fields();
		fi.reset();
		while (fi.hasMoreFields()) {
			JCO.Field f = fi.nextField();
			serialize(f);
		}

	}

	/* @modelguid {0DDD3305-5522-43E7-8B9B-43056595D90B} */
	void serialize(JCO.Structure struct) {
		serialize((JCO.Record) struct);
	}

	/* @modelguid {FCB174B7-9877-4D9D-91C9-E20C970A84EA} */
	void serialize(JCO.Table tab) {
		for (int i = 0; i < tab.getNumRows(); ++i) {
			tab.setRow(i);
			emmitStartTag(ITEM);
			serialize((JCO.Record) tab);
			emmitEndTag(ITEM);
		}
	}

}
