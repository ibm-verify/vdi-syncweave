/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.actions;

import java.io.StringWriter;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.parser.ParserInterface;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * Copies the selected items from an SWT table (item.toString() or BaseConfiguration.getShortName()) to the Clipboard.
 *
 */
public class CopyTableContentsAction extends Action {
	/**
	 * 
	 */
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Table table;

	public CopyTableContentsAction(Table table) {
		this.table = table;
	}
	
	@Override
	public String getActionDefinitionId() {
		return "com.ibm.tdi.actions.copytable";
	}

	@Override
	public String getId() {
		return getActionDefinitionId();
	}

	@Override
	public String getText() {
		return Messages.getString("common.Copy.name");
	}

	@Override
	public void run() {
		StringBuffer buf = new StringBuffer();
		ParserInterface parser = null;
		StringWriter csv = new StringWriter();
		for(int sel : table.getSelectionIndices()) {
			TableItem item = table.getItem(sel);
			Object obj = item != null ? item.getData() : null;
			
			if(obj instanceof BaseConfiguration) {
				BaseConfiguration b = (BaseConfiguration) obj;
				if(buf.length() > 0)
					buf.append("\n");
				buf.append(b.getShortName());
				
			} else if (obj instanceof Entry) {
				if(parser == null) {
					try {
						parser = SystemFunctions.loadParser((ParserConfig)MetamergeConfigFactory.lookup(null, "system:/Parsers/ibmdi.CSV"));
						parser.initParser();
						parser.setOutputStream(csv);
					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e);
					}
				}
				if(parser != null) { 
					try {
						parser.writeEntry((Entry)obj);
					} catch (Exception e) {
						buf.append(obj.toString());
						EclipseAppender.logerror(e.toString(), e);
					}
				} else {
					buf.append(obj.toString());
				}
				
			} else if(obj != null) {
				buf.append(obj.toString());
			}
		}
		
		if(parser != null) {
			try {
				parser.flush();
				buf.append(csv.toString());
				parser.closeParser();
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
		
		try {
			Clipboard clipboard = new Clipboard(table.getDisplay());
			clipboard.setContents(new Object[]{buf.toString()}, new Transfer[]{TextTransfer.getInstance()});
			clipboard.dispose();
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}
}
