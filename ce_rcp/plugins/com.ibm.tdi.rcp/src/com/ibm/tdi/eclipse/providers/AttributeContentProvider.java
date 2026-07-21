/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ibm.di.config.base.InternalSchema;
import com.ibm.di.config.base.SchemaItemConfigImpl;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.SchemaConfig;
import com.ibm.di.config.interfaces.SchemaItemConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Utils;

/**
 * This class provides a Structured
 *
 */
public class AttributeContentProvider implements IStructuredContentProvider, ITreeContentProvider, MetamergeConfigChangeListener, ITableLabelProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Viewer viewer;
	private boolean unmappedSchemaItemsIncluded;
	private AttributeMapConfig associatedAttributeMap;
	private boolean notificationsDisabled;
	private BaseConfiguration input;
	private BaseConfiguration schema;
	private boolean batchChange = false;
	private ArrayList<ILabelProviderListener> listeners = new ArrayList<ILabelProviderListener>();
	
	public void dispose() {
		if(schema != null)
			schema.removeListener(this);
		if(input != null)
			input.removeListener(this);
	}

	public void configurationChanged(final MetamergeConfigChange changeEvent) {
		if(isNotificationsDisabled()) {
			return;
		}
		int op = changeEvent.getOperation();
		if (op == MetamergeConfigChange.BEGIN_CHANGES) {
			batchChange = true;
			return;
		} else if (op == MetamergeConfigChange.END_CHANGES) {
			batchChange = false;
			if (viewer != null) {
				viewer.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						viewer.refresh();						
					}
				});
			}
			return;
		} else if (batchChange) {
			return;
		}
				
		if(viewer != null && !viewer.getControl().isDisposed()) {
			viewer.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					doUpdate(changeEvent);						
				}
			});
		}
	}

	private void doUpdate(MetamergeConfigChange changeEvent) {
		Object source = changeEvent.getSource();
		// -- Child schema item added/removed
		if(source instanceof ContainerConfig && changeEvent.getUserObject() instanceof Object[]) {
			BaseConfiguration parent = ((ContainerConfig)source).getParent();
			if(parent instanceof SchemaItemConfig) {
				Object[] user = (Object[]) changeEvent.getUserObject();
				if(user.length == 2) {
					int position = (Integer)user[0];
					SchemaItemConfig sic = (SchemaItemConfig) user[1];
					if(changeEvent.getOperation() == MetamergeConfigChange.MCC_SET)
						insertTreeElement(parent, sic, position);
					else
						deleteTreeElement(parent, sic);
					
					return;
				}
			}
		}
		
		
		if((source instanceof AttributeMapConfig || source instanceof SchemaConfig)) {
			if (InternalSchema.INHERITS_FROM.equals(changeEvent.getKey())) {
				((TreeViewer)viewer).refresh();
				return;
			}
			Object child;
			if(source instanceof AttributeMapConfig)
				child = ((AttributeMapConfig)source).getAttributeMapItem(changeEvent.getKey());
			else
				child = ((SchemaConfig)source).getItem(changeEvent.getKey());
			
			// Removed object no longer exists
			if(child == null) {
				BaseConfiguration conf = findConfigObject(source, changeEvent.getKey());
				if(conf != null)
					((TreeViewer)viewer).remove(source, new Object[]{conf});
				return;
			}
			
			switch(changeEvent.getOperation()) {
			case MetamergeConfigChange.MCC_ADD:
			case MetamergeConfigChange.MCC_SET:
				((TreeViewer)viewer).insert(source, child, findPosition(source, child));
				updateLabels(child);
				break;
				
			case MetamergeConfigChange.MCC_DELETE:
			case MetamergeConfigChange.MCC_REMOVE:
				((TreeViewer)viewer).remove(source, new Object[]{child});
				break;
			}
			
		} else if(source instanceof AttributeMapItem || source instanceof SchemaItemConfig) {
			updateLabels(source);
		}
	}
	
	private BaseConfiguration findConfigObject(Object source, Object key) {
		TreeItem[] items = ((TreeViewer)viewer).getTree().getItems();
		if(items == null)
			return null;
		
		for(TreeItem ti : items) {
			if(ti.getData() instanceof BaseConfiguration) {
				BaseConfiguration bc = (BaseConfiguration) ti.getData();
				if(key.equals(bc.getShortName()) && bc.getParent() == source)
					return bc;
			}
		}
		return null;
	}

	private void updateLabels(Object source) {
		LabelProviderChangedEvent event = new LabelProviderChangedEvent(this, source);
		for(ILabelProviderListener l : listeners) {
			l.labelProviderChanged(event);
		}
	}

	private int findPosition(Object source, Object child) {
		String str = ((BaseConfiguration)child).getShortName();
		List<String> cc;
		if(source instanceof AttributeMapConfig) {
			cc= ((AttributeMapConfig) source).getAttributeNames();
		} else {
			cc = ((SchemaConfig) source).getItemNames();
		}
		for(int i = 0; i < cc.size(); i++) {
			if(cc.get(i).compareTo(str) > -1)
				return i;
		}
		return 0;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		
		if(input != null) {
			input.removeListener(this);
		}
		if(schema != null) {
			schema.removeListener(this);
		}
		
		this.input = null;
		this.schema = null;
		
		if(newInput instanceof BaseConfiguration) {
			input = (BaseConfiguration) newInput;
			input.addListener(this);
			setUnmappedSchemaItemsIncluded(isUnmappedSchemaItemsIncluded());
		}
	}

	public Object[] getElements(Object inputElement) {
		setNotificationsDisabled(true);
		Object[] ret = getChildren(inputElement);
		setNotificationsDisabled(false);
		return ret;
	}

	public Object[] getChildren(Object parent) {
		BaseConfiguration p = (BaseConfiguration) parent;
		ArrayList<BaseConfiguration> list = new ArrayList<BaseConfiguration>();

		if (p instanceof AttributeMapConfig) {
			List<String> cc = ((AttributeMapConfig) p).getAttributeNames();
			for (String b : cc) {
				list.add(((AttributeMapConfig)p).getAttributeMapItem(b));
			}
			
			if(unmappedSchemaItemsIncluded)
				addUnmappedSchemaItems((AttributeMapConfig)p, list);
			
		} else if (p instanceof SchemaItemConfig) {
			SchemaItemConfig sic = (SchemaItemConfig)p;
			ContainerConfig cc = sic.getChildSchemaList();
			for(int i = 0; i < cc.size(); i++) {
				sic = (SchemaItemConfig) cc.getConfig(i);
				if(sic.isProperty())
					list.add(sic);
			}
			sic = (SchemaItemConfig)p;
			for(int i = 0; i < cc.size(); i++) {
				sic = (SchemaItemConfig) cc.getConfig(i);
				if(!sic.isProperty())
					list.add(sic);
			}
			
		} else if (p instanceof SchemaConfig) {
			SchemaConfig sc = (SchemaConfig) p;
			
			// -- Show the items that are targets for the attribute map
			// -- but only add the top-level node
			if(getAssociatedAttributeMap() != null) {
				List<String> am = getAssociatedAttributeMap().getAttributeNames();
				for(String a : am) {
					SchemaItemConfig sic = sc.getItem(a);
					if(sic != null) {
						while(!(sic.getParent() instanceof SchemaConfig)) {
							sic = (SchemaItemConfig) Utils.getParentConfig(sic.getParent(), SchemaItemConfig.class);
						}
						// sic should now be top level schema item
						if(!list.contains(sic))
							list.add(sic);
					} else {
						try {
							// Add a placeholder for missing schema attribute
							SchemaItemConfigImpl cc = new SchemaItemConfigImpl();
							cc.setAttributeName(a);
							cc.setName (MetamergeConfigFactory.parseName(a));
							cc.setPresenceFlag("Unknown");
							list.add(cc);
						} catch (Exception e) {}
					}
				}
			}
			
			// -- Add remainder of schema
			List<String> cc = sc.getItemNames();
			for (String b : cc) {
				SchemaItemConfig sic = sc.getItem(b);
				if(list.contains(sic))
					continue;
				list.add(((SchemaConfig)p).getItem(b));
			}
		}
		return list.toArray();
	}

	private void addUnmappedSchemaItems(AttributeMapConfig config, ArrayList<BaseConfiguration> list) {
		ConnectorConfig cc = (ConnectorConfig) Utils.getParentConfig(config, ConnectorConfig.class);
		if(cc == null)
			return;
		
		SchemaConfig sc = cc.getSchema(config.getShortName());
		if(sc == null) {
			return;
		}
		for(Object name : sc.getItemNames()) {
//			if(config.hasAttributeMapItem(name))
//				continue;

			list.add(sc.getItem(name));
		}
	}

	public Object getParent(Object element) {
		BaseConfiguration parent = ((BaseConfiguration) element).getParent();
		
		// -- We dont include the SchemaItemConfig.childSchemaList as an element in the
		// -- list returned from getChildren().
		if(parent instanceof ContainerConfig && element instanceof SchemaItemConfig)
			parent = parent.getParent();

		return parent;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof AssemblyLineConfig)
			return true;
		else if (element instanceof ContainerConfig && ((ContainerConfig) element).size() > 0)
			return true;
		else if (element instanceof SchemaConfig)
			return true;
		else if (element instanceof SchemaItemConfig)
			return ((SchemaItemConfig)element).getChildSchemaList().size() > 0; 
		else
			return false;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if(element instanceof AttributeMapItem) {
			AttributeMapItem item = (AttributeMapItem) element;
			switch(columnIndex) {
			case 0:
				if(item.isAdvanced())
					return Activator.getImage("Script_16");
				else if (item.isSubstitution())
					return Activator.getImage("Evaluate_16");
				else
					return Activator.getImage("Attribute_16");
			}
		} else if ((element instanceof SchemaItemConfig) && (columnIndex == 0)) {
			return Activator.getImage("Schema_16");
		} else if ((element instanceof ContainerConfig) && (columnIndex == 0)) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		}
		
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if(element instanceof AttributeMapItem) {
			AttributeMapItem item = (AttributeMapItem) element;
			switch(columnIndex) {
			case 0:
				return item.getShortName();
			case 1:
				if(item.isAdvanced())
					return item.getScript();
				else if (item.isSubstitution())
					return item.getSubstitution();
				else
					return oneLiner(item.getSimple());
			}
			
		} else if (element instanceof ContainerConfig) {
			if(columnIndex == 0)
				return ((ContainerConfig)element).getParent().getShortName();
			else
				return "";
			
		} else if (element instanceof SchemaItemConfig) {
			SchemaItemConfig sic = (SchemaItemConfig) element;
			switch(columnIndex) {
			case 0:
				return (sic.isProperty() ? "@" : "") + sic.getAttributeName();
			case 1:
				return sic.getSample() == null ? "" : sic.getSample().toString();
			case 2:
				String presence = sic.getPresenceFlag();
				if (presence == null || presence.equals(""))
					return SchemaItemConfig.PRESENCE_OPTIONAL;
				return presence;
			case 3:
				return sic.getJavaClass();
			case 4:
				return sic.getExternalSyntax();
			}

		}
		
		return "" + element;
	}

	public void addListener(ILabelProviderListener listener) {
		if(!listeners.contains(listener))
			listeners.add(listener);
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		listeners.remove(listener);
	}

	public Image getImage(Object element) {
		return getColumnImage(element, 0);
	}

	public String getText(Object element) {
		return getColumnText(element, 0);
	}

	public boolean isUnmappedSchemaItemsIncluded() {
		return unmappedSchemaItemsIncluded;
	}

	public void setUnmappedSchemaItemsIncluded(boolean unmappedSchemaItemsIncluded) {
		this.unmappedSchemaItemsIncluded = unmappedSchemaItemsIncluded;
		if(isUnmappedSchemaItemsIncluded()) {
			ConnectorConfig cc = (ConnectorConfig) Utils.getParentConfig(associatedAttributeMap, ConnectorConfig.class);
			if(cc != null) {
				boolean input = Utils.isInputMap(associatedAttributeMap);
				schema = cc.getSchema(input);
				schema.addListener(this);
			}
		}
	}

	public AttributeMapConfig getAssociatedAttributeMap() {
		return associatedAttributeMap;
	}

	public void setAssociatedAttributeMap(AttributeMapConfig associatedAttributeMap) {
		this.associatedAttributeMap = associatedAttributeMap;
	}

	public boolean isNotificationsDisabled() {
		return notificationsDisabled;
	}

	public void setNotificationsDisabled(boolean notificationsDisabled) {
		this.notificationsDisabled = notificationsDisabled;
	}

	private void insertTreeElement(Object parent, SchemaItemConfig child, int position) {
		// -- Believe or not, TreeViewer won't insert an item unless the parent already has children
		// -- Container already has the new child so we check accordingly
		if(child.getParent() instanceof ContainerConfig && ((ContainerConfig)child.getParent()).size() < 2) {
			((TreeViewer)viewer).refresh(parent, true);
		} else {
			((TreeViewer)viewer).insert(parent, child, position);
		}
		
		// -- Expand parent to reveal new item
		((TreeViewer)viewer).setExpandedState(parent, true);
	}
	
	private void deleteTreeElement(BaseConfiguration parent, SchemaItemConfig sic) {
		((TreeViewer)viewer).remove(parent, new Object[]{sic});
	}

	private String oneLiner(String s) {
		if (s == null)
			return "";
		return s.replaceAll("\n", ",");
	}
}
