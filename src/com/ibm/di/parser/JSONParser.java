/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.parser;

import java.util.List;

import com.ibm.commons.util.io.json.JsonGenerator;
import com.ibm.commons.util.io.json.parser.Json;
import com.ibm.di.entry.Entry;

public class JSONParser extends ParserImpl {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String PARAM_COMPACT = "compact";
	private final static String PARAM_WRAP_AS_ARRAY = "wrapAsArray";

	private Json json;
	private JsonTdiFactory factory = new JsonTdiFactory();

	private List<Entry> myList;
	
	private boolean wrapAsArray = false;
	private boolean hasWrittenRecord;
	private boolean compact = true;
	
	@Override
	public void initParser() throws Exception {
		super.initParser();
		json = null;
		compact = myConfiguration == null || myConfiguration.getBooleanParameter(PARAM_COMPACT, true);
		wrapAsArray = myConfiguration != null && myConfiguration.getBooleanParameter(PARAM_WRAP_AS_ARRAY, false);
		if (wrapAsArray && getWriter() != null) 
			getWriter().write(compact ? "[" : "[\n");
		hasWrittenRecord = false;
	}

	@SuppressWarnings("unchecked")
	public Entry readEntry() throws Exception {
		if (json == null) {
			json = new Json(getReader());
			json.factory = factory;
		}
		
		if (myList != null && myList.size() > 0)
			return myList.remove(0);
		
		//
		// We may be at EOF right now but parseJson throws an exception even though
		// the stream is at EOF. So we save the last mark after a successful read
		// and compare that to the location when an EOF exception occurs. If there are
		// tokens between the last entry read and when the read fails it means it has actually
		// encountered a problem with the JSON data.
		//
		int markLine = json.token.beginLine;
		int markCol = json.token.beginColumn;
		
		try {
			Object o = json.parseJson();
			if (o instanceof Entry)
				return (Entry) o;
			if (o instanceof List) {
				myList = (List<Entry>) o;
				if (myList.size() > 0)
					return myList.remove(0);
			}
		} catch (Exception e) {
			if(e.getMessage().indexOf("<EOF>") != -1 &&
					markLine == json.token.beginLine &&
					markCol == json.token.beginColumn) {
				return null;
			}
			throw e;
		} catch (Error err) {
			// An Exception is correct, this is not an Error.
			throw new Exception(err);
		}
		return null;
	}

	public void writeEntry(Entry entry) throws Exception {
		if (wrapAsArray && hasWrittenRecord)
				getWriter().write(compact ? "," : ",\n");
		String result = JsonGenerator.toJson(factory, entry, compact);
		getWriter().write(result);
		hasWrittenRecord = true;
		getWriter().flush();
	}

	public String getVersion() {
		return "1.0-di11.0.0.1 2020-17-05";
	}

	public void closeParser() throws Exception {
		if (wrapAsArray && getWriter() != null) 
			getWriter().write(compact ? "]" : "]\n");
		super.closeParser();
	}
}
