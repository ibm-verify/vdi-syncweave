package com.ibm.di.test.utils.atom;

import com.ibm.di.web.common.atom.AtomCategory;
import com.ibm.di.web.common.atom.AtomContent;
import com.ibm.di.web.common.atom.AtomEntry;
import com.ibm.di.web.common.atom.AtomLink;
import com.ibm.di.web.common.atom.AtomPerson;
import com.ibm.di.web.common.atom.AtomText;
import org.w3c.dom.Element;

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
public class AtomEntryBuilder {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	private AtomEntry e;

	public AtomEntryBuilder() {
		e = new AtomEntry();
	}

	public static AtomEntryBuilder newBuilder() {
		return new AtomEntryBuilder();
	}

	public AtomEntryBuilder id(String id) {
		e.setId(id);
		return this;
	}

	public AtomEntryBuilder base(String base) {
		e.setBase(base);
		return this;
	}

	public AtomEntryBuilder lang(String lang) {
		e.setLang(lang);
		return this;
	}

	public AtomEntryBuilder pub(long pub) {
		e.setPublished(pub);
		return this;
	}

	public AtomEntryBuilder sum(AtomText sum) {
		e.setSummary(sum);
		return this;
	}

	public AtomEntryBuilder title(AtomText title) {
		e.setTitle(title);
		return this;
	}

	public AtomEntryBuilder title(String title) {
		return title(new AtomText(title));
	}

	public AtomEntryBuilder upd(long upd) {
		e.setUpdated(upd);
		return this;
	}

	public AtomEntryBuilder cat(AtomCategory cat) {
		e.getCategories().add(cat);
		return this;
	}

	public AtomEntryBuilder author(AtomPerson author) {
		e.getAuthors().add(author);
		return this;
	}

	public AtomEntryBuilder link(AtomLink link) {
		e.getLinks().add(link);
		return this;
	}

	public AtomEntryBuilder link(String rel) {
		return link(rel, null);
	}

	public AtomEntryBuilder link(String rel, String type) {
		AtomLink al = new AtomLink();
		al.setRel(rel);
		al.setType(type);
		return link(al);
	}

	public AtomEntryBuilder any(Element any) {
		e.getAny().add(any);
		return this;
	}

	public AtomEntryBuilder content(AtomContent content) {
		e.setContent(content);
		return this;
	}

	public AtomEntryBuilder content(String type) {
		AtomContent c = new AtomContent();
		c.setType(type);
		return content(c);
	}

	public AtomEntry build() {
		return e;
	}
}
