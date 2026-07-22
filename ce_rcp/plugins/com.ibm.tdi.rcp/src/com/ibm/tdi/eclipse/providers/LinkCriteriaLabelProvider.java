/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.ibm.di.config.interfaces.LinkCriteriaItem;
import com.ibm.tdi.eclipse.Messages;

public class LinkCriteriaLabelProvider extends LabelProvider implements ITableLabelProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	// List of what operators look like internally.
	// Actually, would have been better if operators were simply ints.
	public final static String[] internalOps = {
		LinkCriteriaItem.LC_EXACT,
		LinkCriteriaItem.LC_LESS_THAN, 
		LinkCriteriaItem.LC_LESS_THAN_OR_EQUAL, 
		LinkCriteriaItem.LC_GREATER_THAN, 
		LinkCriteriaItem.LC_GREATER_THAN_OR_EQUAL,
		
		LinkCriteriaItem.LC_SUBSTRING,
		LinkCriteriaItem.LC_INITIAL,
		LinkCriteriaItem.LC_FINAL,
		LinkCriteriaItem.LC_NOT,		
	};

	// And the external representation, translated
	public final static String[] externalOps = {
		Messages.getString("LinkCriteriaLabelProvider.Oper.0"),
		Messages.getString("BranchingConfig.Conditions.less"),
		Messages.getString("BranchingConfig.Conditions.lessequal"),
		Messages.getString("BranchingConfig.Conditions.greater"),
		Messages.getString("BranchingConfig.Conditions.greaterequal"),
		
		Messages.getString("LinkCriteriaLabelProvider.Oper.1"),
		Messages.getString("LinkCriteriaLabelProvider.Oper.2"),
		Messages.getString("LinkCriteriaLabelProvider.Oper.3"),
		Messages.getString("LinkCriteriaLabelProvider.Oper.4"),
	};
	

	@Override
	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	@Override
	public String getText(Object element) {
		return getColumnText(element, 0);
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if(!(element instanceof LinkCriteriaItem))
			return null;
		
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(!(element instanceof LinkCriteriaItem))
			return super.getText(element);
		
		LinkCriteriaItem item = (LinkCriteriaItem) element;
		String str = "";
		switch(columnIndex) {
		case 0:
			str = "" + item.getAttribute();
			break;
		case 1:
			str = externalOps[indexOf(item.getOper())];
			break;
		case 2:
			str =  "" + item.getValue();
			break;
		}
		
		return str;
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	/**
	 * Returns the index of the operation in the internalOps array
	 */
	public static int indexOf(Object op) {
		for (int i = 0; i < internalOps.length; i++)
			if (internalOps[i].equals(op))
				return i;
		return 0;
	}
}
