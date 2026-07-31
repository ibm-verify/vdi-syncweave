/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.naming.Binding;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * This class is used to provide a TreeViewer with the contents of an
 * AssemblyLineConfig. The provider can be customized to include hooks,
 * attribute maps and schema.
 * 
 */
public class AssemblyLineContentProvider extends AbstractConfigProvider implements ITreeContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private boolean alHooksIncluded = false;

	private boolean hooksIncluded = false;

	private boolean schemaShown = false;

	private AssemblyLineConfig alc;

	private boolean includeLoopPlaceHolders = true;

	private final static String[] epilogHooks = new String[] { InternalSchema.AL_EPILOG, InternalSchema.AL_EPILOG2,
			InternalSchema.AL_ONSUCCESS, InternalSchema.AL_ONFAILURE, InternalSchema.AL_SHUTDOWN };

	private final static String[] prologHooks = new String[] { InternalSchema.AL_PROLOG_INIT, InternalSchema.AL_PROLOG,
		InternalSchema.AL_STARTCYCLE };

	public AssemblyLineContentProvider() {
		super();
	}

	/**
	 * Returns the list of epilog hooks in their execution order
	 * 
	 */
	public static String[] getEpilogHooks() {
		return epilogHooks;
	}

	/**
	 * Returns the list of prolog hooks in their execution order
	 * 
	 */
	public static String[] getPrologHooks() {
		return prologHooks;
	}

	/**
	 * Constructor
	 * 
	 * @param includeALHooks
	 *            true if provider should include active AL hooks
	 */
	public AssemblyLineContentProvider(boolean includeALHooks) {
		super();
		this.alHooksIncluded = includeALHooks;
	}

	/**
	 * Constructor
	 * 
	 * @param includeALHooks
	 *            true if provider should include active AL hooks
	 * @param includeHooks
	 *            true if provider should include active component hooks
	 */
	public AssemblyLineContentProvider(boolean includeALHooks, boolean includeHooks) {
		super();
		this.alHooksIncluded = includeALHooks;
		this.hooksIncluded = includeHooks;
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
		if (alc != null) {
			alc.removeListener(this);
		}
		alc = null;
		if (newInput instanceof AssemblyLineConfig) {
			alc = (AssemblyLineConfig) newInput;
			alc.addListener(this);
		}
		super.inputChanged(viewer, oldInput, newInput);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
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

		if (p instanceof ConnectorConfig) {
			ConnectorConfig cc = (ConnectorConfig) p;
			if (isHooksIncluded()) {
				list.addAll(connectorHooks(cc, HookTree.getHookTree(cc)));
			}
			if (isSchemaShown() ) {
				if (Utils.isOutputConnector(cc))
					list.add(cc.getAttributeMap(false));

				if (Utils.isInputConnector(cc))
					list.add(cc.getAttributeMap(true));
			}

		} else if (p instanceof ContainerConfig) {
			
			if (p instanceof LoopConfig
					&& ((LoopConfig)p).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
				try {
					ConnectorConfig cc = ((LoopConfig) p).getLoopConnector();
					if (cc != null && isHooksIncluded()) {
						list.addAll(connectorHooks(cc, HookTree.getHookTree(cc)));
					}
					if (cc != null && isSchemaShown() ) {
						list.add(cc.getAttributeMap(true));
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.getMessage(), e);
				}
			}
			ContainerConfig cc = (ContainerConfig) p;
			for (int i = 0; i < cc.size(); i++)
				list.add(cc.getConfig(i));

			// -- Add place holder for empty branches
			if (cc instanceof BranchingConfig && cc.size() == 0 && isIncludeLoopPlaceHolders()) {
				BranchingConfig bc = (BranchingConfig) cc;
				switch (bc.getBranchType()) {
				case BranchingConfig.BRANCH_IF:
				case BranchingConfig.BRANCH_ELSE:
				case BranchingConfig.BRANCH_ELSEIF:
				case BranchingConfig.BRANCH_CASE:
					list.add(createPlaceHolder(cc));
				}
			}

		} else if (parent instanceof HookTree) {
			return ((HookTree) parent).getChildrenArray();

		} else if (p instanceof HooksConfig) {
			HooksConfig hooks = (HooksConfig) p;
			list.addAll(hooks.getActiveHooks());

		} else if (p instanceof AttributeMapConfig) {
			AttributeMapConfig amc = (AttributeMapConfig) p;
			List<String> attrNames = amc.getAttributeNames();
			Collections.sort(attrNames);
			for (Object attr : attrNames)
				list.add(amc.getAttributeMapItem(attr));

		} else if (p instanceof AssemblyLineConfig) {
			AssemblyLineConfig alc = (AssemblyLineConfig) p;

			addALHooks(list, prologHooks);

			if (alc.getEntryFeedComponents().size() > 0 || isFeedFlowShown()) {
				list.add(alc.getEntryFeedComponents());
				list.add(alc.getDataFlowComponents());
			} else {
				ContainerConfig cc = alc.getDataFlowComponents();
				for (int i = 0; i < cc.size(); i++)
					list.add(cc.getConfig(i));
			}
			addALHooks(list, epilogHooks);

		} else if (parent instanceof HookTree) {
			return ((HookTree) parent).getChildrenArray();

		} else if (p instanceof MetamergeFolder) {
			try {
				for (Enumeration<Binding> e = ((MetamergeFolder) p).list(); e.hasMoreElements();)
					list.add((BaseConfiguration) e.nextElement().getObject());
			} catch (Exception err) {
				err.printStackTrace();
			}

		}

		return list.toArray();
	}

	/**
	 * Creates a place holder object used by the AL editor to create a new
	 * component
	 * 
	 * @param parent
	 *            The container in which the replaced object should appear
	 */
	private Object createPlaceHolder(BaseConfiguration parent) {
		BaseConfiguration bc = new BaseConfigurationImpl();
		try {
			bc.init();
			bc.setName(Messages.getString("AssemblyLineContentProvider.placeholder"));
			bc.setParameter("%%PLACEHOLDER%%", parent);
		} catch (Exception e) {
		}
		return bc;
	}

	/**
	 * Returns a list of connector hooks (flat view)
	 * 
	 */
	private ArrayList<BaseConfiguration> connectorHooks(ConnectorConfig cc, HookTree ht) {
		ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
		if (ht != null && ht.hasChildren()) {
			for (Object child : ht.getChildrenArray()) {
				HookTree ch = (HookTree) child;
				if (ch.hasChildren()) {
					list.addAll(connectorHooks(cc, ch));
				} else {
					HookConfig hc = ch.getHookConfig(false);
					if (hc != null && hc.getEnabled())
						list.add(hc);
				}
			}
		}
		return list;
	}

	private void addALHooks(List<Object> list, String[] hookList) {
		if (isHooksIncluded()) {
			HooksConfig hooks = alc.getHooks();
			for (String str : hookList) {
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
		return ((BaseConfiguration) element).getParent();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof AssemblyLineConfig)
			return true;
		else if (element instanceof ConnectorConfig)
			return connectorHasChildren((ConnectorConfig) element);
		else if (element instanceof LoopConfig)
			return true;
		else if (element instanceof ContainerConfig)
			return containerHasChildren((ContainerConfig) element);
		else if (element instanceof MetamergeFolder)
			return true;
		else if (element instanceof HooksConfig)
			return isHooksIncluded() && ((HooksConfig) element).getActiveHooks().size() > 0;
		else if (element instanceof HookTree)
			return ((HookTree) element).hasChildren();
		else if (element instanceof AttributeMapConfig)
			return true;

		return false;
	}

	/**
	 * Returns true if a connector has children based on the configuration of
	 * this provider.
	 * 
	 */
	private boolean containerHasChildren(ContainerConfig cc) {
		if (cc instanceof BranchingConfig && cc.size() == 0 && isIncludeLoopPlaceHolders())
			return true;
		else
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
		return (isHooksIncluded() && cc.getHooks().getActiveHooks().size() > 0)
				|| (isSchemaShown() && cc.getAttributeMap().size() > 0);
	}

	/**
	 * Returns whether active AL hooks are included in the content
	 * 
	 */
	public boolean isAlHooksIncluded() {
		return alHooksIncluded;
	}

	/**
	 * Sets the alHooksIncluded flag
	 * 
	 * @param alHooksIncluded
	 */
	public void setAlHooksIncluded(boolean alHooksIncluded) {
		this.alHooksIncluded = alHooksIncluded;
	}

	/**
	 * Returns whether active component hooks are included in the content
	 * 
	 */
	public boolean isHooksIncluded() {
		return hooksIncluded;
	}

	/**
	 * Sets the hooksIncluded flag
	 * 
	 */
	public void setHooksIncluded(boolean hooksIncluded) {
		this.hooksIncluded = hooksIncluded;
	}

	/**
	 * Locates the element in the tree that represents a config object. Elements
	 * in the tree can be a mix of HookTree objects and config objects.
	 * 
	 * @param bc
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

	public boolean isIncludeLoopPlaceHolders() {
		return includeLoopPlaceHolders;
	}

	public void setIncludeLoopPlaceHolders(boolean includeLoopPlaceHolders) {
		this.includeLoopPlaceHolders = includeLoopPlaceHolders;
	}

	@Override
	public void configurationChanged(MetamergeConfigChange changeEvent) {
		int operation = changeEvent.getOperation();
		Object source = changeEvent.getSource();
		Object key = changeEvent.getKey();
		TreeViewer tree = (TreeViewer) getViewer();

		if (operation == MetamergeConfigChange.BEGIN_CHANGES) {
			batchChange = true;
			return;
		} else if (operation == MetamergeConfigChange.END_CHANGES) {
			batchChange = false;
			new RefreshThread(tree, source);
			return;
		} else if (batchChange) {
			return;
		}

		if (source instanceof HooksConfig) {
			HooksConfig hc = (HooksConfig) source;
			BaseConfiguration parent = hc.getParent();
			switch (operation) {
			case MetamergeConfigChange.MCC_DELETE:
			case MetamergeConfigChange.MCC_REMOVE:
			case MetamergeConfigChange.MCC_ADD:
			case MetamergeConfigChange.MCC_SET:
				new RefreshThread(tree, parent);
			}
		} else if (source instanceof HookConfig) {
			HookConfig hc = (HookConfig) source;
			if (InternalSchema.HC_ENABLED.equals(key)) {
				BaseConfiguration parent = hc.getParent();
				if (parent != null && parent.getParent() instanceof AssemblyLineConfig) {
					new RefreshThread(tree, parent.getParent());
				} else if (parent != null && parent.getParent() instanceof ConnectorConfig) {
					if (parent.getParent().getParent() instanceof LoopConfig)
						new RefreshThread(tree, parent.getParent().getParent());
					else
						new RefreshThread(tree, parent.getParent());
				} else {
					new RefreshThread(tree, parent);
				}
			} else {
				new RefreshThread(tree, hc);
			}
		} else if (source instanceof BaseConfiguration) {
			new RefreshThread(tree, source);
		}
	}

	/**
	 * Class to update the viewer in a UI thread. A thread is created to run
	 * this class in the constructor.
	 */
	private class RefreshThread implements Runnable {
		private Object target;
		private TreeViewer viewer;

		public RefreshThread(TreeViewer viewer, Object target) {
			super();
			this.target = target;
			this.viewer = viewer;
			viewer.getTree().getDisplay().asyncExec(this);
		}

		public void run() {
			//TODO: If we inherit from the target, we should find out
			// which component does the inheriting, and then use that as 
			// the target for refresh.
			if (alc == Utils.getParentConfig(target, AssemblyLineConfig.class))
				viewer.refresh(target, true);
			else
				viewer.refresh();
		}
	}
}
