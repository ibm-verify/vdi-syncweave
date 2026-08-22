package com.ibm.di.tp.server.handler.base;

import static com.ibm.di.test.utils.atom.AtomUtils.atomEntryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.createNodeEntryFor;
import static com.ibm.di.test.utils.atom.AtomUtils.createReferenceAtomEntry;
import static com.ibm.di.test.utils.atom.AtomUtils.deserializeEntry;

import java.io.ByteArrayOutputStream;
import java.net.URI;

import javax.ws.rs.core.UriInfo;

import com.ibm.di.web.common.atom.AtomEntry;
import org.junit.Test;

import com.ibm.di.test.tp.TpAppHelper;
import com.ibm.di.test.tp.UnitTestTPClientContext;

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
public class EntryTest {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static class TestableEntry extends Entry {

		@SuppressWarnings("unchecked")
		protected void expandLinks(Object payload, UriInfo uriInfo) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.ibm.di.tp.server.handler.base.Entry#getEscapedId()
		 */
		@Override
		public String getEscapedId() {
			return null;
		}
	}

	@Test
	public void test_check_Reference_Entry_Creation_Makes_An_Entry_That_Has_Only_Id_Self_Link_Updated_And_Title_Tags()
			throws Exception {
		TestableEntry act = new TestableEntry();

		AtomEntry expFull = createNodeEntryFor(UnitTestTPClientContext.CONTEXT_ROOT_USED_BY_WINK, TpAppHelper
				.createTdiNodeConfg("0"));

		// set the template of the TestableEntry
		act.setEntryTemplate(null);

		String selfHref = "/link/to/entry";
		// createReferenceEntry returns a Wink AtomEntry; bridge to internal via marshal/unmarshal.
		org.apache.wink.common.model.atom.AtomEntry winkRef =
				(org.apache.wink.common.model.atom.AtomEntry) act.createReferenceEntry(URI.create(selfHref));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		org.apache.wink.common.model.atom.AtomEntry.marshal(winkRef, baos);
		AtomEntry actRef = deserializeEntry(baos.toString("UTF-8"));
		AtomEntry expRef = createReferenceAtomEntry(expFull, selfHref);

		atomEntryComparator.assertEquals(actRef, expRef, false);
	}
}
