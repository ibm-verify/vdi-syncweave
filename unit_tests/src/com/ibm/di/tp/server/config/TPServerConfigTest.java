
package com.ibm.di.tp.server.config;

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

import java.io.File;

import org.junit.Test;

import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.config.persistence.PersistenceConfig;
import com.ibm.di.tp.server.config.security.EncryptionConfig;
import com.ibm.di.tp.server.config.template.TemplateConfig;

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
public class TPServerConfigTest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	public void test_Deserialize_TP_XML() throws Exception {
		TPServerConfigFile file = new TPServerConfigFile(new File("resources/tp/server/tp.xml"));

		TPServerConfig cfg = file.getTPServerConfig();
		assertThat(cfg.getVersion(), is(equalTo("1.0")));

		EncryptionConfig enc = cfg.getEncryptionConfig();
		assertThat(enc.getStash(), is("idisrv.sth"));
		assertThat(enc.getKeyStore(), is("testserver.jks"));
		assertThat(enc.getKeyStoreType(), is("jks"));
		assertThat(enc.getKeyAlias(), is("server"));
		assertThat(enc.getTransformation(), is("RSA"));

		TemplateConfig tmpl = cfg.getTemplateConfig();
		assertThat(tmpl.getBaseTemplate(), is("etc/TouchpointTemplate.xml"));
		assertThat(tmpl.getCustomTemplatesDir(), is("templates"));

		PersistenceConfig pcfg = cfg.getPersistenceConfig();
		assertThat(pcfg.isEnabled(), is(true));
		assertThat(pcfg.getLocation(), is("tp_state"));

		assertThat(cfg.getNodeConfigs().getTdiNodeConfigs().size(), is(1));

		TdiNodeConfig def = cfg.getNodeConfigs().getTdiNodeConfigs().get(0);
		assertThat(def.isLocal(), is(true));
		assertThat(def.getId(), is("default"));
		assertThat(def.getProviderHost(), is("localhost"));
		assertThat(def.getProviderPort(), is(1097));
		assertThat(def.getTitle(), is("Example Touchpoint Provider"));
		assertThat(def.getAuthor(), is("John Doe"));
		assertThat(def.getEmail(), is("jdoe@example.org"));
		assertThat(def.getSummary(), is("Example Touchpoinet Provider Atom Entry"));
		assertThat(def.getContact(), is("Local Administrator"));
		assertThat(def.getLocation(), is("Main building, 5th fl."));
		assertThat(def.getOrganization(), is("Example Organization"));
	}
}
