/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.MetamergeConfig;

/**
 * This implementation provides access to attribute maps via the file system.
 * Only property style attribute map files are supported by this driver.
 * <p/>
 * If the file extension is ".map" it is interpreted as a property style attribute map.
 * The property style attribute map is a simple file with a one liner for each mapping,
 *  where the left hand is the attribute name and the right hand is the assignment.
 * Lines not containing an equal sign are ignored, except while scanning JavaScript.
 * <pre>
 *  attrname=
 *  attrname=JavaScript
 *  attrname=[
 *    multiple lines of JavaScript
 *  ]
 * </pre>
 * If the value of an attribute is blank a simple map is created. In all other cases an advanced map
 * is created using the value as the JavaScript expression. 
 * For JavaScript spanning multiple lines, there should be a single [ after
 * the equal sign, and the end is signaled by a single ]<p/>
 * 
 * You can also use flags after the attribute name.
 * <pre>
 *  attrname{SAM}=...
 * </pre>
 * where <br>
 * S = Use text with substitution instead of JavaScript<br>
 * A = Only use this mapping for Add operation, not for Modify<br>
 * M = Only use this mapping for Modify operation, not for Add<br>
 */
public class FileNamespace {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 1L;
	
	public static final String EXTERNAL_ATTRIBUTE_MAP_EXTENSION = ".map";

	/**
	 * Reads a property-style attribute map configuration from the specified file.
	 * 
	 * @param file The input file
	 * @return The attribute map config object
	 * @throws Exception
	 */
	public static AttributeMapConfig createMap(String name, MetamergeConfig mc) throws Exception {
		
		File file = new File(name);
		if (!file.exists())
			return null;
		
		AttributeMapConfig amc = null;

//		// -- Check cache
//		amc = attmapCache.get(file);
//		if(amc != null) {
//			Long lastMod = (Long)amc.getParameter("file-timestamp");
//			if(lastMod != null && lastMod == file.lastModified()) {
//				return amc;
//			}
//			
//			// -- Clear current list of attributes
//			for(String str : amc.getAttributeNames())
//				amc.removeAttributeMapItem(str);
//		}
//		

		
		// -- Create and cache new object
		if(amc == null) {
			amc = new AttributeMapConfigImpl();
			amc.setMetamergeConfig(mc);
			amc.init();
			amc.setName(file.getName());
//			attmapCache.put(file, amc);
		}

		// -- Update last mod
//		amc.setParameter("file-timestamp", file.lastModified());

		BufferedReader inp = null;
		try {
			// -- Read in file
			inp = new BufferedReader(new FileReader(file));
			String str;
			while ((str = inp.readLine()) != null) {
				int index = str.indexOf("=");
				if (index == -1)
					continue;

				// attribute=value
				String attr = str.substring(0, index).trim();
				String value = str.substring(index + 1).trim();

				// attribute= [
				// .... script ....
				// ]
				if (value.equals("[")) {
					StringBuffer buf = new StringBuffer();
					while ((str = inp.readLine()) != null) {
						if (str.trim().equals("]")) {
							break;
						}
						buf.append(str + "\n");
					}
					value = buf.toString();
				}

				// Curly braces are used for flags.
				// A = Add
				// M - Modify
				// S - Substitution text
				// ! - Map item disabled
				boolean mod = false;
				boolean add = false;
				boolean subst = false;
				boolean disabled = false;
				if (attr.contains("{")) {
					index = attr.indexOf('{');
					String flags = attr.substring(index+1).toUpperCase();
					attr = attr.substring(0, index);
					subst = flags.contains("S");
					mod = flags.contains("M");
					add = flags.contains("A");
					disabled = flags.contains("!");
				}

				AttributeMapItem ami = amc.newAttributeMapItem(attr);
				if (value.length() == 0) {
					ami.setSimple(attr);
				} else if (subst) {
					ami.setSubstitution(value);
				} else {
					ami.setScript(value);
				}

				if (mod && !add) {
					ami.setAdd(false);
				} else if (add && !mod) {
					ami.setModify(false);
				}
				
				ami.setEnabled(!disabled);
			}
		} finally {
			if (inp != null) {
				inp.close();
			}
		}
		return amc;
	}
	
}
