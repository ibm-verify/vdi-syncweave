
package com.ibm.di.util;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.util.Vector;

import org.junit.Test;

public class StringUtilsTest {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_toPrint_Method_For_Defect_14140() throws Exception {
		String result = StringUtils.toPrint("value\\with\\\\backslashes");
		assertEquals("value\\\\with\\\\\\\\backslashes", result);
	}

	@Test
	public void test_toPrint_Method_With_Whitespace_Characters() throws Exception {
		String result = StringUtils.toPrint("value\nwith\tspecial\rchars");
		assertEquals("value\\nwith\\tspecial\\rchars", result);
	}

	@Test
	public void test_fromPrint_Method_With_Whitespace_Characters() throws Exception {
		String result = StringUtils.fromPrint("value\\nwith\\tspecial\\rchars\\\\");
		assertEquals("value\nwith\tspecial\rchars\\", result);

		result = StringUtils.fromPrint("value\\\\with\\\\\\\\backslashes");
		assertEquals("value\\with\\\\backslashes", result);
	}

	@Test
	public void test_splitstirng_Method_with_String_Separator_Backslash() throws Exception {
		String str = "dolphin\\turtle\\rabbit\\newt";
		Vector<String> result = StringUtils.splitstring(str, "\\");

		assertNotNull(result);
		assertEquals(4, result.size());

		assertEquals("dolphin", result.get(0));
		assertEquals("turtle", result.get(1));
		assertEquals("rabbit", result.get(2));
		assertEquals("newt", result.get(3));
	}

	@Test
	public void test_splitstirng_Method_with_String_Separator_Comma() throws Exception {
		String str = "banana,apple,orange,lime";
		Vector<String> result = StringUtils.splitstring(str, ",");

		assertNotNull(result);
		assertEquals(4, result.size());

		assertEquals("banana", result.get(0));
		assertEquals("apple", result.get(1));
		assertEquals("orange", result.get(2));
		assertEquals("lime", result.get(3));
	}

	@Test
	public void test_splitStirng_Method_with_Char_Separator_Comma() throws Exception {
		String str = "banana,apple,orange,lime";
		String[] result = StringUtils.splitString(str, ',');

		assertNotNull(result);
		assertEquals(4, result.length);

		assertEquals("banana", result[0]);
		assertEquals("apple", result[1]);
		assertEquals("orange", result[2]);
		assertEquals("lime", result[3]);
	}

	@Test
	public void test_splitStirng_Method_with_Char_Separator_Backslash() throws Exception {
		String str = "dolphin\\turtle\\rabbit\\newt";
		String[] result = StringUtils.splitString(str, '\\');

		assertNotNull(result);
		assertEquals(4, result.length);

		assertEquals("dolphin", result[0]);
		assertEquals("turtle", result[1]);
		assertEquals("rabbit", result[2]);
		assertEquals("newt", result[3]);
	}

	@Test
	public void test_splitstringArr_Method_with_Comma_for_Separator() throws Exception {
		String str = "banana,apple,orange,lime";
		String[] result = StringUtils.splitstringArr(str, ",");

		assertNotNull(result);
		assertEquals(4, result.length);

		assertEquals("banana", result[0]);
		assertEquals("apple", result[1]);
		assertEquals("orange", result[2]);
		assertEquals("lime", result[3]);
	}

	@Test
	public void test_splitstringArr_Method_with_Empty_String() throws Exception {
		assertNull(StringUtils.splitstringArr("", ","));
	}

	@Test
	public void test_splitStringTokenCount_Method() {
		int result = StringUtils.splitStringTokenCount("banana,apple,orange,lime", ',');
		assertEquals(4, result);

		result = StringUtils.splitStringTokenCount("dolphin\\turtle\\rabbit\\newt\\monkey", '\\');
		assertEquals(5, result);
	}

	@Test
	public void test_splitStringTokenCount_Method_with_Empty_String() {
		assertEquals(0, StringUtils.splitStringTokenCount("", '\\'));
	}

	@Test
	public void test_nibble_Method_With_Numbers() {
		char[] numbers = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

		for (int i = 0; i < 10; i++) {
			assertEquals(i, StringUtils.nibble(numbers[i]));
		}
	}

	@Test
	public void test_nibble_Method_With_Letters() {
		char[] lowerLetters = { 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] upperLetters = { 'A', 'B', 'C', 'D', 'E', 'F' };
		int res = 10;

		for (int i = 0; i < 6; i++) {
			assertEquals(res, StringUtils.nibble(lowerLetters[i]));
			assertEquals(StringUtils.nibble(lowerLetters[i]), StringUtils.nibble(upperLetters[i]));
			res++;
		}
	}

	@Test
	public void test_nibble_Method_With_Invalid_Characters() {
		boolean hasException = false;
		try {
			StringUtils.nibble('\n');
		} catch (NumberFormatException e) {
			hasException = true;
		}
		assertTrue(hasException);

		hasException = false;
		try {
			StringUtils.nibble('g');
		} catch (NumberFormatException e) {
			hasException = true;
		}
		assertTrue(hasException);
	}

	@Test
	public void test_fromHex_Method() {
		assertEquals(1, StringUtils.fromHex("01"));
		assertEquals(10, StringUtils.fromHex("0A"));
		assertEquals(64, StringUtils.fromHex("40"));
	}

	@Test
	public void test_toHex_Method_With_Empty_String() {
		assertEquals("", StringUtils.toHex(""));
	}

	@Test
	public void test_toHex_Method_With_String() {
		String str = "~!@#$%^&*()_+\"\\[]{}:;/?|,.'<>";
		String result = StringUtils.toHex(str);
		assertEquals(29, StringUtils.splitStringTokenCount(result, ' '));
		assertEquals("7E 21 40 23 24 25 5E 26 2A 28 29 5F 2B 22 5C 5B 5D 7B 7D 3A 3B 2F 3F 7C 2C 2E 27 3C 3E", result);

		str = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
		result = StringUtils.toHex(str);
		assertEquals(62, StringUtils.splitStringTokenCount(result, ' '));
		assertEquals(
				"71 77 65 72 74 79 75 69 6F 70 61 73 64 66 67 68 6A 6B 6C 7A 78 63 76 62 6E 6D 51 57 45 52 54 59 55 49 4F 50 41 53 44 46 47 48 4A 4B 4C 5A 58 43 56 42 4E 4D 31 32 33 34 35 36 37 38 39 30",
				result);
	}

	@Test
	public void test_toHex_Method_With_Char() {
		char[] symbols = { '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '"', '\\', '[', ']', '{', '}', ':',
				';', '/', '?', '|', ',', '.', '<', '>' };
		String[] expected = { "007E", "0021", "0040", "0023", "0024", "0025", "005E", "0026", "002A", "0028", "0029", "005F",
				"002B", "0022", "005C", "005B", "005D", "007B", "007D", "003A", "003B", "002F", "003F", "007C", "002C", "002E",
				"003C", "003E" };
		String result = null;

		for (int i = 0; i < symbols.length; i++) {
			result = StringUtils.toHex(symbols[i]);
			assertEquals(expected[i], result);
		}

		char[] alphanumeric = { 'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z',
				'x', 'c', 'v', 'b', 'n', 'm', 'Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P', 'A', 'S', 'D', 'F', 'G', 'H', 'J',
				'K', 'L', 'Z', 'X', 'C', 'V', 'B', 'N', 'M', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' };
		String[] expected1 = { "0071", "0077", "0065", "0072", "0074", "0079", "0075", "0069", "006F", "0070", "0061", "0073",
				"0064", "0066", "0067", "0068", "006A", "006B", "006C", "007A", "0078", "0063", "0076", "0062", "006E", "006D",
				"0051", "0057", "0045", "0052", "0054", "0059", "0055", "0049", "004F", "0050", "0041", "0053", "0044", "0046",
				"0047", "0048", "004A", "004B", "004C", "005A", "0058", "0043", "0056", "0042", "004E", "004D", "0031", "0032",
				"0033", "0034", "0035", "0036", "0037", "0038", "0039", "0030" };

		for (int i = 0; i < alphanumeric.length; i++) {
			result = StringUtils.toHex(alphanumeric[i]);
			assertEquals(expected1[i], result);
		}
	}

	@Test
	public void test_toASCII_Method_With_ASCII_String() {
		String str = "some ASCII sring";
		String result = StringUtils.toASCII(str);
		assertEquals(str, result);
	}
	
	
	@Test
	public void test_toASCII_Method_With_Non_ASCII_String() {
		String str = "some \u00ed\u00e5 ASCII \u00ed\u00e8\u00e7";
		String result = StringUtils.toASCII(str);
		assertEquals("some \\u043D\\u0435 ASCII \\u043D\\u0438\\u0437", result);
	}
	
	@Test
	public void test_toBase64_Method() {
		String str = "qwertyuiopasdfghjklzxcvbnm";
		String result = (new StringUtils()).toBase64(str);
		assertEquals("dQBAGUdhBAHleQBAG9dABAHMaABAGdbABAGtcABAHhZxBAGIchBAA==", result);
	}
	
	@Test
	public void test_toBase64_Method_With_Empty_String() {
		String str = "";
		String result = (new StringUtils()).toBase64(str);
		assertEquals("", result);
	}
	
	@Test
	public void test_getLastError_Method_After_Initialization() {
		Exception ex = (new StringUtils()).getLastError();
		assertNull(ex);
	}
}
