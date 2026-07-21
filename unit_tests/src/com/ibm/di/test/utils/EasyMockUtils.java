package com.ibm.di.test.utils;

import static org.easymock.EasyMock.aryEq;
import static org.easymock.EasyMock.eq;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
public class EasyMockUtils {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public static Object getEqualsMatcher(Object obj) {
		if (obj != null && obj.getClass().isArray()) {
			if (obj instanceof boolean[]) {
				return aryEq((boolean[]) obj);
			} else if (obj instanceof byte[]) {
				return aryEq((byte[]) obj);
			} else if (obj instanceof char[]) {
				return aryEq((char[]) obj);
			} else if (obj instanceof double[]) {
				return aryEq((double[]) obj);
			} else if (obj instanceof float[]) {
				return aryEq((float[]) obj);
			} else if (obj instanceof int[]) {
				return aryEq((int[]) obj);
			} else if (obj instanceof long[]) {
				return aryEq((long[]) obj);
			} else if (obj instanceof short[]) {
				return aryEq((short[]) obj);
			} else {
				return aryEq((Object[]) obj);
			}
		} else {
			return eq(obj);
		}
	}
}
