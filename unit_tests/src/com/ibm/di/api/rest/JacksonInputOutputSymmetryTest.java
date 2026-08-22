package com.ibm.di.api.rest;

import org.junit.Ignore;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.text.StringContainsInOrder.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.providers.jackson.WinkJacksonJaxbJsonProvider;
import org.junit.Test;

import com.ibm.di.api.bind.CreateConfig;
import com.ibm.di.api.bind.StartAL;
import com.ibm.di.api.rest.internal.provider.CustomMediaTypeToJaxbJSONProviderDelegator;
import com.ibm.di.config.bind.BindUtil;
import com.ibm.di.test.utils.ConfigUtils;
import com.ibm.di.web.common.internal.atom.StringAtomText;

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
@Ignore("All test methods are pending implementation")
public class JacksonInputOutputSymmetryTest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// @Test
	// public void test_JacksonJaxbProvider_Is_Symmetric_For_Custom_Types() throws Exception {
	// 	WinkJacksonJaxbJsonProvider p = new WinkJacksonJaxbJsonProvider();

	// 	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// 	p.writeTo(new StartAL(), StartAL.class, StartAL.class, null, MediaType.APPLICATION_JSON_TYPE, null, bos);

	// 	System.out.println(bos);

	// 	ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
	// 	StartAL s = (StartAL) p.readFrom(Object.class, StartAL.class, null, MediaType.APPLICATION_JSON_TYPE, null, bis);
	// }

	// @Test
	// public void test_JacksonJaxbProvider_Is_Symmetric_For_Atom_Type() throws Exception {
	// 	WinkJacksonJaxbJsonProvider p = new WinkJacksonJaxbJsonProvider();

	// 	AtomEntry e = new AtomEntry();
	// 	e.setId("testEntry");

	// 	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// 	p.writeTo(e, AtomEntry.class, AtomEntry.class, null, MediaType.APPLICATION_JSON_TYPE, null, bos);

	// 	System.out.println(bos);

	// 	ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
	// 	AtomEntry r = (AtomEntry) p.readFrom(Object.class, AtomEntry.class, null, MediaType.APPLICATION_JSON_TYPE, null, bis);
	// 	assertThat(e.getId(), is(r.getId()));
	// }

	// @Test
	// public void test_Output_Contains_No_otherAttributes() throws Exception {
	// 	WinkJacksonJaxbJsonProvider p = new WinkJacksonJaxbJsonProvider();

	// 	AtomEntry e = new AtomEntry();
	// 	e.setId("testEntry");

	// 	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// 	p.writeTo(e, AtomEntry.class, AtomEntry.class, null, MediaType.APPLICATION_JSON_TYPE, null, bos);

	// 	System.out.println(bos.toString("UTF-8"));

	// 	assertThat(bos.toString("UTF-8"), not(stringContainsInOrder(Arrays.asList("otherAttributes"))));
	// }

	// @Test
	// public void test_Output_Contains_Title_Value() throws Exception {
	// 	WinkJacksonJaxbJsonProvider p = new WinkJacksonJaxbJsonProvider();

	// 	AtomEntry e = new AtomEntry();
	// 	e.setId("testEntry");
	// 	e.setTitle(new StringAtomText("vvv"));

	// 	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// 	p.writeTo(e, AtomEntry.class, AtomEntry.class, null, MediaType.APPLICATION_JSON_TYPE, null, bos);

	// 	System.out.println(bos.toString("UTF-8"));

	// 	assertThat(bos.toString("UTF-8"), stringContainsInOrder(Arrays.asList("vvv")));
	// }

	// @Test
	// public void test_TDI_Config_Is_Correctly_Serialized_And_Then_Deserialized() throws Exception {
	// 	CustomMediaTypeToJaxbJSONProviderDelegator p = new CustomMediaTypeToJaxbJSONProviderDelegator();

	// 	CreateConfig cc = new CreateConfig();
	// 	cc.setSolution(BindUtil.fromMetamergeConfig(ConfigUtils.deserializeConfig(new File(
	// 			"resources/api/rest/jackson/Bluepages.xml"))));

	// 	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// 	p.writeTo(cc, CreateConfig.class, CreateConfig.class, null, MediaType.APPLICATION_JSON_TYPE, null, bos);

	// 	System.out.println(bos.toString("UTF-8"));

	// 	ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
	// 	p.readFrom(Object.class, CreateConfig.class, null, MediaType.APPLICATION_JSON_TYPE, null, bis);
	// }
}
