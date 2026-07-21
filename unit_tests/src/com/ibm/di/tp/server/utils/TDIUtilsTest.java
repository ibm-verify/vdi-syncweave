package com.ibm.di.tp.server.utils;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.ibm.di.tp.server.util.TDIUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
public class TDIUtilsTest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_escapeRunName_Escapes_Collons_And_Any_Kind_Of_Slashes() throws Exception {
		String str = "system:/Connectors/ibmdi\\LDAP_Id0";
		String escStr = TDIUtils.escapeRunName(str);

		assertThat(escStr, is(not(equalTo(str))));
		assertThat(escStr, is(not(equalToIgnoringCase(str))));
		assertThat(escStr, is(equalTo("system__Connectors_ibmdi_LDAP_Id0")));
	}
}
