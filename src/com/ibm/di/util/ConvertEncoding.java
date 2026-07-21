/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

public class ConvertEncoding {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String PROPERTIES_FILE = "miserver";

	private static ResourceHash sResHash = ResourceHash.getHash(PROPERTIES_FILE);

	public static void main(String[] args) throws Exception {
		if (invalidArgs(args)) {
			printUsage();
			return;
		}

		String srcFile = args[0];
		String destFile = args[1];
		String srcEnc = args[2];
		String destEnc = null;
		if (args.length > 3) {
			destEnc = args[3];
		}
		convertEncoding(srcFile, destFile, srcEnc, destEnc);
	}

	private static boolean invalidArgs(String[] args) {
		if (args.length < 3) {
			return true;
		}

		return false;
	}

	private static void printUsage() {
		System.out
				.println(sResHash.getString("MISERVER.CONVERTENCODING.USAGE"));
	}

	private static void convertEncoding(String aSrcFile, String aDestFile,
			String aSrcEncoding, String aDestEncoding) throws IOException {
		String[] srcFileArray = generateFileNameArray(aSrcFile);
		String[] destFileArray = generateFileNameArray(aDestFile);

		if (srcFileArray.length != destFileArray.length) {
			pringmsg(sResHash
					.getString("MISERVER.CONVERTENCODING.THE.NUMBER.OF.SOURCE.FILES.DOES.NOT.MATCH"));
			return;
		}
		for (int iCount = 0; iCount < srcFileArray.length; iCount++) {
			File srcFile = new File(srcFileArray[iCount]);
			int srcLen = (int) srcFile.length();
			byte[] srcBytes = new byte[srcLen];

			FileInputStream srcFIS = new FileInputStream(srcFile);
			srcFIS.read(srcBytes);
			srcFIS.close();
			String content = new String(srcBytes, aSrcEncoding);
			FileOutputStream destFOS = new FileOutputStream(
					destFileArray[iCount]);
			byte[] destBytes = null;
			if (aDestEncoding != null) {
				destBytes = content.getBytes(aDestEncoding);
			} else {
				destBytes = content.getBytes();
			}
			destFOS.write(destBytes);
			destFOS.close();
		}
	}

	private static String[] generateFileNameArray(String aFileList) {
		String[] fileNameArray = null;
		int tokenIdx = 0;

		if (aFileList.indexOf(",") < 0) {
			fileNameArray = new String[] { aFileList };
		} else {
			StringTokenizer strToken = new StringTokenizer(aFileList, ",");
			fileNameArray = new String[strToken.countTokens()];
			while (strToken.hasMoreElements()) {
				fileNameArray[tokenIdx++] = strToken.nextToken();
			}
		}
		return fileNameArray;
	}

	protected static void pringmsg(Object msg) {
		System.out.println(msg);
	}
}
