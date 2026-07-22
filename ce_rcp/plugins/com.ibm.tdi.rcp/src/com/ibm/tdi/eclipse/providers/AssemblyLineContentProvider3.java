/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Binding;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.BranchingConfig;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.FunctionConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.HooksConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeFolder;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.util.AssemblyLineScripts;
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
public class AssemblyLineContentProvider3 extends AbstractConfigProvider 
implements ITreeContentProvider, ICheckStateProvider, ICheckStateListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private boolean alHooksIncluded = false;

	private boolean hooksIncluded = false;

	private boolean showChecked;

	private boolean attributeMapsShown = false;

	private AssemblyLineConfig alc;

	private boolean includeLoopPlaceHolders = true;

	private boolean disabledHooksIncluded;

	private boolean showScriptComponents;

	private boolean showIncludedScripts;

	private AssemblyLineScripts scripts;

	private HookConfig currentHook;

	private final static String[] epilogHooks = new String[] { InternalSchema.AL_EPILOG, InternalSchema.AL_EPILOG2,
		InternalSchema.AL_ONSUCCESS, InternalSchema.AL_ONFAILURE, InternalSchema.AL_SHUTDOWN };

	private final static String[] prologHooks = new String[] { InternalSchema.AL_PROLOG_INIT, InternalSchema.AL_PROLOG,
		InternalSchema.AL_STARTCYCLE };

	public AssemblyLineContentProvider3() {
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
		if (alc != null) {
			alc.removeListener(this);
		}
		alc = null;
		if (newInput instanceof AssemblyLineConfig) {
			alc = (AssemblyLineConfig) newInput;
			alc.addListener(this);
			scripts = new AssemblyLineScripts(alc);
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

			if (showScriptComponents)
				addScriptedComponents(cc, list);

			if (isHooksIncluded()) {
				list.addAll(connectorHooks(cc, HookTree.getHookTree(cc)));
			}

			if (isAttributeMapsShown() ) {
				if (Utils.isOutputConnector(cc))
					list.add(cc.getAttributeMap(false));

				if (Utils.isInputConnector(cc))
					list.add(cc.getAttributeMap(true));	
			} else if (showChecked) {
				if (Utils.isOutputConnector(cc) && !getCheckedChildren(cc.getAttributeMap(false)).isEmpty())
					list.add(cc.getAttributeMap(false));

				if (Utils.isInputConnector(cc) && !getCheckedChildren(cc.getAttributeMap(true)).isEmpty())
					list.add(cc.getAttributeMap(true));					
			}

		} else if (p instanceof ContainerConfig) {

			if (p instanceof LoopConfig
					&& ((LoopConfig)p).getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
				try {
					ConnectorConfig cc = ((LoopConfig) p).getLoopConnector();
					if (cc != null) {
						if (showScriptComponents)
							addScriptedComponents(cc, list);

						if (isHooksIncluded())
							list.addAll(connectorHooks(cc, HookTree.getHookTree(cc)));

						if (isAttributeMapsShown())
							list.add(cc.getAttributeMap(true));
						else if	(showChecked && !getCheckedChildren(cc.getAttributeMap(true)).isEmpty())
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
			for(String str : hooks.getKeys(BaseConfiguration.RECURSIVE_SUBTREE)) {
				HookConfig hc = hooks.getHook(str);
				if(isHookEnabled(hc) || (showChecked && isChecked(hc)))
					list.add(hc);
			}
			//list.addAll(hooks.getActiveHooks());

		} else if (p instanceof AttributeMapConfig) {
			if (isAttributeMapsShown()) {
				AttributeMapConfig amc = (AttributeMapConfig) p;
				List<String> attrNames = amc.getAttributeNames();
				Collections.sort(attrNames);
				for (Object attr : attrNames)
					list.add(amc.getAttributeMapItem(attr));
			} else if (showChecked) {
				List<AttributeMapItem> children = getCheckedChildren((AttributeMapConfig) p);
				Collections.sort(children, new Comparator<AttributeMapItem>() {
					public int compare(AttributeMapItem a, AttributeMapItem b) {
						return a.getShortName().compareTo(b.getShortName());
					}			
				});
				list.addAll(children);
			}
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
			if (showIncludedScripts && scripts != null && ! scripts.isEmpty())
				list.add(scripts);

		} else if (p instanceof MetamergeFolder) {
			try {
				for (Enumeration<Binding> e = ((MetamergeFolder) p).list(); e.hasMoreElements();)
					list.add((BaseConfiguration) e.nextElement().getObject());
			} catch (Exception err) {
				err.printStackTrace();
			}
		} else if (p instanceof LinkCriteriaConfig) {
			LinkCriteriaConfig lc = (LinkCriteriaConfig) p;
			for(Object str : lc.getCriteriaNames()) {
				list.add(lc.getCriteria(str));
			}
		} else if (parent == scripts && scripts != null) {
			for (String key:scripts.getAllNames()) {
				list.add(scripts.getScript(key));
			}
		}

		return list.toArray();
	}

	private void addScriptedComponents(ConnectorConfig cc, List<Object> list) {
		RawConnectorConfig rcc = cc.getConnectionConfig();
		if (rcc != null && "com.ibm.di.connector.ScriptConnector".equals(rcc.getJavaClass()))
			list.add(rcc);
		ParserConfig pc = cc.getParserConfig();
		if (pc != null && "com.ibm.di.parser.ScriptParser".equals(pc.getJavaClass()))
			list.add(pc);
		if (cc instanceof FunctionConfig) {
			RawFunctionConfig rfc = ((FunctionConfig)cc).getFunctionConfig();
			if (rfc != null && "com.ibm.di.fc.ScriptedFC".equals(rfc.getJavaClass()))
				list.add(rfc);
		}
	}

	/**
	 * Creates a place holder object used by the AL editor to create a new
	 * component
	 * 
	 * @param parent
	 *            The container in which the replaced object should appear
	 * @return
	 */
	private Object createPlaceHolder(BaseConfiguration parent) {
		BaseConfiguration bc = new BaseConfigurationImpl();
		try {
			bc.init();
			bc.setName(Messages.getString("AssemblyLineContentProvider.placeholder"));
			bc.setParameter("%%PLACEHOLDER%%", parent);
		} catch (Exception e) {
			SystemFunctions.doNothing(); //Cannot happen
		}
		return bc;
	}

	/**
	 * Returns a list of connector hooks (flat view)
	 * 
	 * @param cc
	 * @param ht
	 * @return
	 */
	private ArrayList<BaseConfiguration> connectorHooks(ConnectorConfig cc, HookTree ht) {
		ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();
		if (ht != null && ht.hasChildren()) {
			for (Object child : ht.getChildrenArray()) {
				HookTree ch = (HookTree) child;
				if (ch.hasChildren()) {
					list.addAll(connectorHooks(cc, ch));
				} else {
					HookConfig hc = ch.getHookConfig(true);
					if ( disabledHooksIncluded || hc == currentHook || isHookEnabled(hc) || 
							(showChecked && isChecked(hc)))
						list.add(hc);
				}
			}
		}
		return list;
	}

	private boolean isHookEnabled(HookConfig hc) {
		if(hc == null)
			return false;
		if(hc.getScript() != null && hc.getScript().length() > 0)
			return true;
		else
			return hc.getEnabled();
	}

	private void addALHooks(List<Object> list, String[] hookList) {
		HooksConfig hooks = alc.getHooks();
		for (String str : hookList) {
			HookConfig hook = hooks.getHook(str, false);
			if (hook != null && (hook == currentHook || isHookEnabled(hook) || isAlHooksIncluded() || (showChecked && isChecked(hook))))
				list.add(hook);
			else if (isAlHooksIncluded())
				list.add(hooks.getHook(str));
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
		if (element instanceof AssemblyLineConfig){
			return true;
		} else if (element instanceof ConnectorConfig){
			return connectorHasChildren((ConnectorConfig) element);
		} else if (element instanceof LoopConfig){
			return true;
		} else if (element instanceof ContainerConfig){
			return containerHasChildren((ContainerConfig) element);
		} else if (element instanceof MetamergeFolder){
			return true;
		} else if (element instanceof HooksConfig) {
			return isHooksIncluded() && ((HooksConfig) element).getActiveHooks().size() > 0;
		} else if (element instanceof HookTree)
			return ((HookTree) element).hasChildren();
		else if (element instanceof AttributeMapConfig) {
			return ((AttributeMapConfig)element).getAttributeNames().size() > 0;
		} else if (element == scripts) {
			return true;
		}

		//		else if (element instanceof LinkCriteriaConfig)
		//			return ((LinkCriteriaConfig)element).getCriteriaNames().size() > 0;

		return false;
	}

	/**
	 * Returns true if a connector has children based on the configuration of
	 * this provider.
	 * 
	 * @param element
	 * @return
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
	 * @return
	 */
	private boolean connectorHasChildren(ConnectorConfig cc) {

		if (isDisabledHooksIncluded())
			return true;

		// -- Check hooks
		if (isHooksIncluded()) {
			for(BaseConfiguration bc : connectorHooks(cc, HookTree.getHookTree(cc))) {
				if(isHookEnabled((HookConfig) bc) || (showChecked && isChecked(bc)) || bc == currentHook)
					return true;
			}
		}

		if (isAttributeMapsShown()) {
			if(Utils.isInputConnector(cc) && cc.getAttributeMap(true).size() > 0)
				return true;
			if(Utils.isOutputConnector(cc) && cc.getAttributeMap(false).size() > 0)
				return true;
		} else if (showChecked) {
			if (Utils.isInputConnector(cc) && !getCheckedChildren(cc.getAttributeMap(true)).isEmpty())
				return true;					
			if (Utils.isOutputConnector(cc) && !getCheckedChildren(cc.getAttributeMap(false)).isEmpty())
				return true;
		}

		if (showScriptComponents) {
			List<Object> list = new ArrayList<Object>();
			addScriptedComponents(cc, list);
			if (list.size() > 0)
				return true;
		}
		return false;
	}

	/**
	 * Returns whether inactive AL hooks are included in the content (active are always shown)
	 * 
	 * @return
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
	 * @return
	 */
	public boolean isHooksIncluded() {
		return hooksIncluded;
	}

	/**
	 * Sets the hooksIncluded flag
	 * 
	 * @param alHooksIncluded
	 *            true if active component hooks should be included
	 */
	public void setHooksIncluded(boolean hooksIncluded) {
		this.hooksIncluded = hooksIncluded;
	}

	/**
	 * Locates the element in the tree that represents a config object.
	 * 
	 * @param bc
	 * @return
	 */
	public Object findElement(BaseConfiguration bc) {
		while (bc != null) {
			Object o = findElement(getChildren(alc), bc);
			if (o!= null)
				return o;
			bc = bc.getParent();
		}
		return null;
	}

	/**
	 * Searches the list of elements for the one that holds the provided config
	 * object
	 * 
	 * @param elements
	 * @param bc
	 * @return
	 */
	public Object findElement(Object[] elements, BaseConfiguration bc) {
		for (Object obj : elements) {
			if (obj == bc)
				return obj;

			Object res = findElement(getChildren(obj), bc);
			if (res != null)
				return res;
		}
		return null;
	}

	/**
	 * Returns whether schema items are included in the content
	 * 
	 * @return
	 */
	public boolean isAttributeMapsShown() {
		return attributeMapsShown;
	}

	/**
	 * Sets the schemaIncluded flag
	 * 
	 * @param schemaShown
	 */
	public void setAttributeMapsShown(boolean schemaShown) {
		this.attributeMapsShown = schemaShown;
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
			if (! viewer.getTree().isDisposed())
				viewer.getTree().getDisplay().asyncExec(this);
		}

		public void run() {
			if (viewer.getTree().isDisposed())
				return;
			//TODO: If we inherit from the target, we should find out
			// which component does the inheriting, and then use that as 
			// the target for refresh.
			if (alc == Utils.getParentConfig(target, AssemblyLineConfig.class))
				viewer.refresh(target, true);
			else
				viewer.refresh();
		}
	}

	public void setDisabledHooksIncluded(boolean selection) {
		disabledHooksIncluded = selection;
	}

	public boolean isDisabledHooksIncluded() {
		return disabledHooksIncluded;
	}

	private Set<Object> checked = new HashSet<Object>();
	public boolean setChecked (Object bc, boolean value) {
		if (value)
			checked.add(bc);
		else
			checked.remove(bc);
		return true;
	}

	public boolean isChecked(Object element) {
		return checked.contains(element);
	}

	public boolean isGrayed(Object element) {
		// We could say that simple AttributeMapItems are grayed, but maybe it is better to not do that
		//		if (element instanceof AttributeMapItem)
		//			return ! ((AttributeMapItem)element).isAdvanced();
		return false;
	}

	public void checkStateChanged(CheckStateChangedEvent event) {
		setChecked(event.getElement(), event.getChecked());
	}

	public void setShowChecked(boolean value) {
		showChecked = value;
	}

	public List<AttributeMapItem> getCheckedChildren(AttributeMapConfig map) {
		List<AttributeMapItem> ret = new ArrayList<AttributeMapItem>();
		for (Object o:checked) {
			if (o instanceof AttributeMapItem && 
					map == Utils.getParentConfig(o, AttributeMapConfig.class))
				ret.add((AttributeMapItem) o);
		}
		return ret;
	}

	public void setCurrentHook(HookConfig currentHook) {
		this.currentHook = currentHook;
	}

	public void setShowScriptComponents(boolean showScriptComponents) {
		this.showScriptComponents = showScriptComponents;
	}

	public void setShowIncludedScripts(boolean value) {
		showIncludedScripts = value;
	}
}
