/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;
import javax.naming.*;
import com.ibm.di.config.interfaces.*;
import com.ibm.di.server.ResourceHash;

/**
 * Implements a container for other folders and leaf configuration objects.
 */
public class MetamergeFolderImpl extends BaseConfigurationImpl implements
		MetamergeFolder {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 6107586753523140220L;

	private final static ResourceHash sResHash = BaseConfigurationImpl
			.getResHash();

	public MetamergeFolderImpl() {
		super();
	}

	public MetamergeFolderImpl(Object config) {
		super(config);
	}

	/**
	 * This method creates a folder
	 */
	public MetamergeFolder createFolder(Object name) throws Exception {
		Name fullName = (Name) getName().clone();
		Name folderName = MetamergeConfigFactory.parseName(name);
		if (folderName.size() != 1) {
			throw new Exception(
					sResHash
							.getString("MMCONFIG.METAMFOLDERIMPL.CANNOT.RECURSIVELY.CREATE.FOLDERS"));
		} else if (folderName.size() == 0) {
			throw new Exception(
					sResHash
							.getString("MMCONFIG.METAMFOLDERIMPL.SINGLE.NAME.COMPONENT.REQUIRED"));
		}

		fullName.add(folderName.get(0));

		System.out.println(sResHash.getString(
				"MMCONFIG.METAMFOLDERIMPL.CREATE.FOLDER", fullName));

		MetamergeFolder folder = new MetamergeFolderImpl();
		folder.setName(fullName);
		getMetamergeConfig().bind(fullName, folder);
		return folder;
	}

	public Enumeration list() throws Exception {
		return getMetamergeConfig().list(getName());
	}

	public String[] getNames() throws Exception {
		Vector<String> v = new Vector<String>();
		synchronized(this){
			for (Enumeration<Binding> e = list(); e.hasMoreElements();) {
				Binding b = e.nextElement();
				v.add(b.getName());
			}
		}

		String[] str = new String[v.size()];
		for (int i = 0; i < v.size(); i++)
			str[i] = v.elementAt(i);

		return str;
	}

	/*
	 * 
	 * public Enumeration list () throws Exception { return new
	 * TreeMapEnumeration ( getMetamergeConfig(), getName(), this ); }
	 * 
	 * public String[] getNames() throws Exception { String[] str = new String [
	 * size() ]; int i = 0; for ( Iterator e = getDataIterator(); e.hasNext(); ) {
	 * str[i++] = e.next().toString(); } return str; }
	 */

	/**
	 * Return self clone
	 */
	public Object getClone() throws Exception {
		MetamergeFolder fld = new MetamergeFolderImpl(deepClone(null));
		fld.setName(getName());
		fld.init();
		fld.setMetamergeConfig(getMetamergeConfig());
		setModTS(getModTS());
		return fld;
	}
}
