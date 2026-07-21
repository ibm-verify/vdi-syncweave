package com.ibm.di.tp.server.handler.base;

import static com.ibm.di.test.utils.atom.AtomUtils.atomEntryComparator;
import static com.ibm.di.test.utils.atom.AtomUtils.createNodeEntryFor;
import static com.ibm.di.test.utils.atom.AtomUtils.createReferenceAtomEntry;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.atom.AtomEntry;
import org.apache.wink.common.model.synd.SyndEntry;
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.ibm.di.tp.server.handler.base.Entry#expandLinks(org.apache.wink
		 * .common.model.atom.AtomEntry, javax.ws.rs.core.UriInfo)
		 */
		@Override
		protected void expandLinks(AtomEntry payload, UriInfo uriInfo) {
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
		act.setEntryTemplate(expFull.toSynd(new SyndEntry()));

		String selfHref = "/link/to/entry";
		AtomEntry actRef = act.createReferenceEntry(URI.create(selfHref));
		AtomEntry expRef = createReferenceAtomEntry(expFull, selfHref);

		atomEntryComparator.assertEquals(actRef, expRef, false);
	}
}
