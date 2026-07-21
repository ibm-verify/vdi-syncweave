/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.util.HookTree;
import com.ibm.di.util.HookTree.Phase;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * This class is used to provide a TreeViewer with the contents of an
 * AssemblyLineConfig. The provider can be customized to include hooks,
 * attribute maps and schema.
 * 
 */
public class AssemblyLineStepProvider implements ITreeContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private AssemblyLineConfig alc;

	private boolean schemaShown = true;
	private boolean inActiveHooksIncluded = false;
	private boolean reply = false;

	private final static String[] lastHooks = new String[] { InternalSchema.AL_EPILOG2,
			InternalSchema.AL_ONSUCCESS, InternalSchema.AL_ONFAILURE, InternalSchema.AL_SHUTDOWN };

	/**
	 * Constructor
	 * 
	 */
	public AssemblyLineStepProvider() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.tdi.eclipse.providers.AbstractConfigProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.tdi.eclipse.providers.AbstractConfigProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		alc = null;
		if (newInput instanceof AssemblyLineConfig) {
			alc = (AssemblyLineConfig) newInput;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (! (inputElement instanceof AssemblyLineConfig))
			return null;
		
		ArrayList<Object> list = new ArrayList<Object>();

		alc = (AssemblyLineConfig) inputElement;
		boolean server = false;
		
		for (BaseConfiguration bc : alc.getEntryFeedComponents().getConfigurations(null)) {
			if ( !(bc instanceof ConnectorConfig))
				break; // Something is very wrong...
            ConnectorConfig cc = (ConnectorConfig) bc;
            if (ConnectorConfig.SERVER_MODE.equals(cc.getMode())
                            && cc.getEnabled()) {
                    server = true;
                    if (cc.getReplyRequired())
                    	reply = true;
            }
		}
		if (server)
			list.add(Phase.SERVER);
		addALHooks(list, InternalSchema.AL_PROLOG_INIT);
		list.add(Phase.INIT);
		addALHooks(list, InternalSchema.AL_PROLOG);
		list.add(Phase.LOOP);
		addALHooks(list, InternalSchema.AL_EPILOG);
		list.add(Phase.CLOSE);
		addALHooks(list, lastHooks);
		list.add(Phase.RECONNECT);
		return list.toArray();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		
		BaseConfiguration p = null;
		if (parent instanceof BaseConfiguration)
			p = (BaseConfiguration) parent;

		ArrayList<Object> list = new ArrayList<Object>();

		if (parent instanceof Phase) {
			addPhase(list, (Phase) parent);
		} else if (p instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) p;
				list.addAll( connectorHooks(HookTree.getHookTree(cc, Phase.LOOP)));
				//Add mapping for ALMappingConfig
				if (ConnectorConfig.MAPPING_MODE.equals(cc.getMode())
						&& isSchemaShown()) {
					list.add(cc.getAttributeMap(true));					
				}

		} else if (p instanceof ContainerConfig) {
			
			if (p instanceof LoopConfig
					&& ((LoopConfig)p).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
				try {
					ConnectorConfig cc = ((LoopConfig) p).getLoopConnector();
					if (cc != null) {
						list.addAll(connectorHooks(HookTree.getHookTree(cc, Phase.LOOP)));
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.getMessage(), e);
				}
			}
			ContainerConfig cc = (ContainerConfig) p;
			for (int i = 0; i < cc.size(); i++)
				list.add(cc.getConfig(i));

		} else if (parent instanceof HookTree) {
			return connectorHooks((HookTree) parent).toArray();
		} else if (p instanceof AttributeMapConfig) {
			AttributeMapConfig amc = (AttributeMapConfig) p;
			List<String> attrNames = amc.getAttributeNames();
			Collections.sort(attrNames);
			for (Object attr : attrNames)
				list.add(amc.getAttributeMapItem(attr));
		}

		return list.toArray();
	}

	private void addPhase(List<Object> list, Phase phase) {
		if (phase == Phase.LOOP) {
			addALHooks(list, InternalSchema.AL_STARTCYCLE);
			list.add(alc.getEntryFeedComponents());
			list.add(alc.getDataFlowComponents());
			if (reply)
				list.add(Phase.REPLY);
			return;
		}

		List<BaseConfiguration> components = alc.getEntryFeedComponents().getConfigurations(null);
		if (phase == Phase.INIT || phase == Phase.CLOSE)
			alc.getDataFlowComponents().getConfigurations(components);
		
		for (BaseConfiguration bc : components) {
			if (! (bc instanceof ConnectorConfig)) {
				continue;
			}
			HookTree ht = HookTree.getHookTree((ConnectorConfig)bc, phase);
			if (ht != null)
				list.add(ht);
		}				
	}
	
	/**
	 * Returns a list of connector hooks (flat view)
	 * 
	 */
	private ArrayList<Object> connectorHooks(HookTree ht) {
		ArrayList<Object> list = new ArrayList<Object>();
		if (ht != null && ht.hasChildren()) {
			ConnectorConfig cc = Utils.getParentConfig(ht.getHooksConfig(), ConnectorConfig.class);
			for (Object child : ht.getChildrenArray()) {
				HookTree ch = (HookTree) child;
				String name = ch.getName();
				if (ch.hasChildren()) {
					if (isInactiveHooksIncluded()) {
						list.add(ch);
					} else {
						List<Object> temp = connectorHooks(ch);
						if (temp.size() == 1)
							list.add(temp.get(0));
						else if (temp.size() > 1)
							list.add(ch);
					}
				} else if ("input_attribute_map".equals(name)) {
					list.add(cc.getAttributeMap(true));
				} else if ("output_attribute_map".equals(name)) {
					list.add(cc.getAttributeMap(false));
				} else if (isInactiveHooksIncluded()) {
					list.add(ch.getHookConfig(true));
				} else {
					HookConfig hc = ch.getHookConfig(false);
					if (hc != null && hc.getEnabled())
						list.add(hc);
				}
			}
		}
		return list;
	}

	private void addALHooks(List<Object> list, String... hookList) {
		HooksConfig hooks = alc.getHooks();
		for (String str : hookList) {
			if (isInactiveHooksIncluded()) {
				list.add(hooks.getHook(str));
			} else {
				HookConfig hook = hooks.getHook(str, false);
				if (hook != null && hook.getEnabled())
					list.add(hook);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof HookConfig)
			return false;
		if (element instanceof Phase)
			return true;
		if (element instanceof HookTree)
			return true;
		if (element instanceof AttributeMapConfig)
			return true;
		if (element instanceof AssemblyLineConfig)
			return true;
		if (element instanceof ConnectorConfig)
			return connectorHasChildren((ConnectorConfig) element);
		if (element instanceof LoopConfig)
			return true;
		if (element instanceof ContainerConfig)
			return containerHasChildren((ContainerConfig) element);

		return false;
	}

	/**
	 * Returns true if a connector has children based on the configuration of
	 * this provider.
	 * 
	 */
	private boolean containerHasChildren(ContainerConfig cc) {
		return cc.size() > 0;
	}

	/**
	 * Returns true if a connector has children based on the configuration of
	 * this provider.
	 * 
	 * @param cc
	 *            The connector
	 */
	private boolean connectorHasChildren(ConnectorConfig cc) {
		return isInactiveHooksIncluded() || 
			cc.getHooks().getActiveHooks().size() > 0 ||
			(isSchemaShown() && cc.getAttributeMap().size() > 0);
	}

	/**
	 * Locates the element in the tree that represents a config object. Elements
	 * in the tree can be a mix of HookTree objects and config objects.
	 * 
	 */
	public Object findElement(BaseConfiguration bc) {
		return findElement(getChildren(alc), bc);
	}

	/**
	 * Searches the list of elements for the one that holds the provided config
	 * object
	 * 
	 */
	public Object findElement(Object[] elements, BaseConfiguration bc) {
		for (Object obj : elements) {
			if (obj == bc)
				return obj;

			if (obj instanceof HookTree) {
				Object res = searchHookTree((HookTree) obj, bc);
				if (res != null)
					return res;
			}
		}
		return null;
	}

	/**
	 * Searches a hook tree for the element that holds the provided config
	 * object
	 * 
	 */
	private Object searchHookTree(HookTree ht, BaseConfiguration bc) {
		if (ht.getHookConfig(true) == bc)
			return ht;
		Object res = null;
		for (Object obj : ht.getChildrenArray()) {
			if (obj instanceof HookTree)
				res = searchHookTree((HookTree) obj, bc);
			if (res != null)
				return res;
		}
		return null;
	}

	/**
	 * Returns whether schema items are included in the content
	 * 
	 */
	public boolean isSchemaShown() {
		return schemaShown;
	}

	/**
	 * Sets the schemaIncluded flag
	 * 
	 * @param schemaShown
	 */
	public void setSchemaShown(boolean schemaShown) {
		this.schemaShown = schemaShown;
	}

	public void setInactiveHooksIncluded(boolean selection) {
		this.inActiveHooksIncluded = selection;
	}

	public boolean isInactiveHooksIncluded() {
		return inActiveHooksIncluded;
	}
}
