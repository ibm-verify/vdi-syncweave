/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.*;
import org.apache.xerces.impl.dv.util.Base64;
import com.ibm.di.entry.Entry;
import com.ibm.di.entry.Attribute;
import com.ibm.di.server.ResourceHash;

import java.util.ArrayList;
import java.util.Vector;

import com.ibm.icu.util.StringTokenizer;

public class Base64EntryConverter {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String[] biAttrNames = null;

	private static final String[] preDefinedBiAttrNames = { "photo",
			"personalSignature", "audio", "jpegPhoto", "javaSerializedData",
			"thumbnailPhoto", "thumbnailLogo", "userPassword",
			"userCertificate", "authorityRevocationList",
			"certificateRevocationList", "crossCertificatePair",
			"x500UniqueIdentifier", "objectGUID", "objectSid" };

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * Constructor for Base64Entry.
	 */
	public Base64EntryConverter() {
		init(null, null, null);
	}

	/**
	 * Constructor for Base64Entry.
	 */
	public Base64EntryConverter(String[] userDefinedBiAttributeNames) {
		init(null, null, userDefinedBiAttributeNames);
	}

	/**
	 * Constructor for Base64Entry.
	 * 
	 * @param userDefinedBiAttributeNames :
	 *            user defined binary attribute names delimited by ","
	 */
	public Base64EntryConverter(String userDefinedBiAttributeNames) {
		init(userDefinedBiAttributeNames, ",", null);
	}

	/**
	 * Constructor for Base64Entry.
	 * 
	 * @param userDefinedBiAttributeNames
	 *            User defined binary attribute names delimited by the second
	 *            parameter
	 * @param delimiter
	 *            Delimiter of the binary attribute names
	 */
	public Base64EntryConverter(String userDefinedBiAttributeNames,
			String delimiter) {
		init(userDefinedBiAttributeNames, delimiter, null);
	}

	/**
	 * Initialize the object. It parse the binary attributes to a array list.
	 */
	private void init(String userDefinedBiAttributeNames, String delimiter,
			String[] userDefinedBiAttributeNameArray) {
		ArrayList biAttrNameList = new ArrayList();
		for (int i = 0; i < preDefinedBiAttrNames.length; i++) {
			biAttrNameList.add(preDefinedBiAttrNames[i]);
		}

		if (userDefinedBiAttributeNameArray == null) {
			if (userDefinedBiAttributeNames != null) {
				StringTokenizer tokenizer = new StringTokenizer(
						userDefinedBiAttributeNames, delimiter);
				while (tokenizer.hasMoreTokens()) {
					biAttrNameList.add(tokenizer.nextToken());
				}
			}
		} else {
			for (int i = 0; i < userDefinedBiAttributeNameArray.length; i++) {
				biAttrNameList.add(userDefinedBiAttributeNameArray[i]);
			}
		}

		biAttrNames = new String[biAttrNameList.size()];
		biAttrNameList.toArray(biAttrNames);
	}

	/**
	 * Encode the entry. All binary value attribute values will be encoded.
	 * 
	 * @param entry :
	 *            Entry of which attributs to encoded
	 */
	public void base64encode(Entry entry) throws Exception {
		String[] attrNames = getBinaryAttributeNames(entry);

		for (int i = 0; i < attrNames.length; i++) {
			Attribute biAttr = entry.getAttribute(attrNames[i]);
			Object[] biValues = biAttr.getValues();
			Vector encodedValueVector = new Vector();

			for (int j = 0; j < biValues.length; j++) {
				if ( biValues[j] instanceof String ) {
					encodedValueVector.add(biValues[j]);
				} else if ( biValues[j] instanceof byte[] ) {
					encodedValueVector.add(Base64.encode((byte[])biValues[j]));
				}
			}
			biAttr.setValues(encodedValueVector);
		}
	}

	/**
	 * Encode the entry. All binary value attribute values will be encoded.
	 * 
	 * @param entry :
	 *            Entry of which attributs to decoded
	 */
	public void base64decode(Entry entry) throws Exception {
		String[] attrNames = getBinaryAttributeNames(entry);

		for (int i = 0; i < attrNames.length; i++) {
			Attribute encodedAttr = entry.getAttribute(attrNames[i]);
			Object[] encodedValues = encodedAttr.getValues();
			Vector biValueVector = new Vector();

			for (int j = 0; j < encodedValues.length; j++) {
				if (encodedValues[j] instanceof String) {
					byte[] biBytes = Base64.decode((String) encodedValues[j]);
					biValueVector.add(biBytes);
				} else {
					biValueVector.add(encodedValues[j]);
				}
			}
			encodedAttr.setValues(biValueVector);
		}
	}

	/**
	 * Get BASE64 encoding attributes
	 * 
	 * @param entry :
	 *            Entry of which the function will get the attribute names in
	 *            Binary
	 * @return The Base 64 encoding attribute names in this entry as an
	 *         ArrayList
	 */
	public String[] getBinaryAttributeNames(Entry entry) {
		String[] entryAttrNames = entry.getAttributeNames();
		ArrayList entryBiAttrNameList = new ArrayList();

		for (int i = 0; i < entryAttrNames.length; i++) {
			for (int j = 0; j < biAttrNames.length; j++) {
				if (entryAttrNames[i].equals(biAttrNames[j])) {
					entryBiAttrNameList.add(entryAttrNames[i]);
					break;
				}
			}
		}

		String[] entryBiAttrNames = new String[entryBiAttrNameList.size()];

		entryBiAttrNameList.toArray(entryBiAttrNames);

		return entryBiAttrNames;
	}

	public static void main(String args[]) {
		String biFile = args[0];
		int index = biFile.lastIndexOf(".");
		String biFileBody = biFile.substring(0, index);

		String tag = biFile.substring(index + 1);

		String encodedFile = biFileBody + ".txt";
		String outBiFile = biFileBody + "1." + tag;

		String[] userBiAttribs = { "biattr1", "biattr2" };

		Base64EntryConverter base64 = new Base64EntryConverter(userBiAttribs);

		try {
			Entry entry = new Entry();

			FileInputStream fis = new FileInputStream(biFile);
			int byteNum = 0;
			while (fis.read() != -1) {
				byteNum++;
			}
			fis.close();

			fis = new FileInputStream(biFile);
			byte[] byteArray = new byte[byteNum];
			byte biByte = (byte) 0;
			for (int i = 0; i < byteNum; i++) {
				biByte = (byte) fis.read();
				byteArray[i] = biByte;
			}
			fis.close();

			entry.setAttribute("biattr2", byteArray);
			base64.base64encode(entry);

			String encodedString = (String) entry.getObject("biattr2");

			BufferedWriter fileOut = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(encodedFile)));
			fileOut.write(encodedString, 0, encodedString.length());
			fileOut.close();

			base64.base64decode(entry);

			byte[] decodedBytes = (byte[]) entry.getObject("biattr2");

			FileOutputStream decodedFileOut = new FileOutputStream(outBiFile);
			decodedFileOut.write(decodedBytes);
			decodedFileOut.close();

		} catch (Exception e) {
			System.out.println(sResHash.getString(
					"MISERVER.BASE64ENTRYCONVERTER.ERROR", e));
		}
	}
}
