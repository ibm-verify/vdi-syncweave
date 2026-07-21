
package com.ibm.di.api.syslog;

import static org.hamcrest.beans.HasProperty.*;
import static org.hamcrest.beans.HasPropertyWithValue.*;
import static org.hamcrest.beans.SamePropertyValuesAs.*;
import static org.hamcrest.collection.IsArray.*;
import static org.hamcrest.collection.IsArrayContaining.*;
import static org.hamcrest.collection.IsArrayContainingInAnyOrder.*;
import static org.hamcrest.collection.IsArrayContainingInOrder.*;
import static org.hamcrest.collection.IsArrayWithSize.*;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.hamcrest.collection.IsEmptyCollection.*;
import static org.hamcrest.collection.IsEmptyIterable.*;
import static org.hamcrest.collection.IsIn.*;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.*;
import static org.hamcrest.collection.IsIterableContainingInOrder.*;
import static org.hamcrest.collection.IsIterableWithSize.*;
import static org.hamcrest.collection.IsMapContaining.*;
import static org.hamcrest.core.AllOf.*;
import static org.hamcrest.core.AnyOf.*;
import static org.hamcrest.core.DescribedAs.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsAnything.*;
import static org.hamcrest.core.IsEqual.*;
import static org.hamcrest.core.IsInstanceOf.*;
import static org.hamcrest.core.IsNot.*;
import static org.hamcrest.core.IsNull.*;
import static org.hamcrest.core.IsSame.*;
import static org.hamcrest.number.IsCloseTo.*;
import static org.hamcrest.number.OrderingComparison.*;
import static org.hamcrest.object.HasToString.*;
import static org.hamcrest.object.IsCompatibleType.*;
import static org.hamcrest.object.IsEventFrom.*;
import static org.hamcrest.text.IsEmptyString.*;
import static org.hamcrest.text.IsEqualIgnoringCase.*;
import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.*;
import static org.hamcrest.text.StringContainsInOrder.*;
import static org.hamcrest.xml.HasXPath.*;
import static org.junit.Assert.*;

import org.junit.Test;

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
public class LogUtilsTest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_Clean_Config_Id() throws Exception {
		assertThat(LogUtils.getCleanConfigId("<c/o:n\\f*i?g\">|"), is("_c_o_n_f_i_g___"));
	}
}
