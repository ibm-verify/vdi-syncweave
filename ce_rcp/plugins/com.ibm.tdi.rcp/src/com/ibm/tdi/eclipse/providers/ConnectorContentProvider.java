/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.Utils;

/**
 * This class provides the contents of a ConnectorConfig object in a structured tree.
 *
 */
public class ConnectorContentProvider extends AbstractConfigProvider implements ITreeContentProvider{
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private HooksContentProvider hooks;

	public ConnectorContentProvider() {
		super();
		hooks = new HooksContentProvider();
	}

	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	public Object getParent(Object element) {
		if(element instanceof BaseConfiguration)
			return ((BaseConfiguration)element).getParent();
		else
			return null;
	}

	public boolean hasChildren(Object element) {
		if(element instanceof ConnectorConfig ||
//				element instanceof AttributeMapConfig ||
//				element instanceof SchemaConfig ||
				element instanceof LinkCriteriaConfig ||
				element instanceof Object[] ||
				element instanceof HooksConfig)
			return true;
		else if (element instanceof HookTree)
			return ((HookTree)element).hasChildren();
		else
			return false;
	}

	public Object[] getElements(Object inputElement) {
		ArrayList<Object> children = new ArrayList<Object>();
		if(inputElement instanceof ConnectorConfig) {
			
			ConnectorConfig cc = (ConnectorConfig)inputElement;

			children.add(cc.getConnectionConfig());
			
			if(cc.getConnectionConfig().getParserOption() != RawConnectorConfig.PARSER_USELESS)
				children.add(cc.getParserConfig());
			
			/*
			Action action = new Action() {
				public void run() {}
				public String toString() { return getText(); }
			};
			action.setText("Discover Data");
			action.setId("discoverData");
			children.add(action);
			*/
			
			if(Utils.isInputConnector(cc)) {
//				children.add(cc.getSchema(true));
				children.add(cc.getAttributeMap(true));
			}
			
			if(Utils.isOutputConnector(cc)) {
//				children.add(cc.getSchema(false));
				children.add(cc.getAttributeMap(false));
			}
			
			if(Utils.hasLinkRequirements(cc)) {
				children.add(cc.getLinkCriteria());
			}
			
			children.add(HookTree.getHookTree(cc));
			
//		} else if(inputElement instanceof AttributeMapConfig) {
//			for(Object str : ((AttributeMapConfig)inputElement).getAttributeNames())
//				children.add(((AttributeMapConfig)inputElement).getAttributeMapItem(str));
//		} else if(inputElement instanceof SchemaConfig) {
//			for(Object str : ((SchemaConfig)inputElement).getItemNames())
//				children.add(((SchemaConfig)inputElement).getItem(str));
		} else if(inputElement instanceof LinkCriteriaConfig) {
			for(Object str : ((LinkCriteriaConfig)inputElement).getCriteriaNames())
				children.add(((LinkCriteriaConfig)inputElement).getCriteria(str));
		} else if(inputElement instanceof HooksConfig) {
			return hooks.getElements(((HooksConfig)inputElement).getParent());
		} else if(inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		} else if(inputElement instanceof HookTree) {
			return ((HookTree)inputElement).getChildrenArray();
		}
		return children.toArray();
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		hooks.inputChanged(viewer, oldInput, newInput);
		super.inputChanged(viewer, oldInput, newInput);
	}

}
