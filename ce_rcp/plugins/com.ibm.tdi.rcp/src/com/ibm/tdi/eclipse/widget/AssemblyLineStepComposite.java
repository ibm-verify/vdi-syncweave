/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.ibm.di.config.interfaces.ALMappingConfig;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.config.interfaces.ParserConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.RawFunctionConfig;
import com.ibm.di.config.interfaces.ScriptConfig;
import com.ibm.di.util.AssemblyLineScripts;
import com.ibm.di.util.Breakpoint;
import com.ibm.di.util.DebugServer;
import com.ibm.icu.util.StringTokenizer;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.providers.AssemblyLineContentProvider3;
import com.ibm.tdi.eclipse.providers.ConfigLabelProvider;
import com.ibm.tdi.eclipse.stepper.StepperPanel;

public class AssemblyLineStepComposite extends BaseWidget implements ICheckable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private CheckboxTreeViewer stepTree;
	private AssemblyLineContentProvider3 alcp;
	private Hashtable<String, Breakpoint> breakpoints;

	private BaseConfiguration currentBreak;
	private Object currentElem;

	private StepperPanel myStepperPanel;

	public AssemblyLineStepComposite(Composite parent, int style, BaseConfiguration editingConfig, StepperPanel sp) {
		super(parent, style, editingConfig);
		myStepperPanel = sp;
		createUI();
	}

	private void createUI() {
		setLayout(new FillLayout());

		stepTree = new CheckboxTreeViewer(this, SWT.NULL);
		alcp = new AssemblyLineContentProvider3();
		alcp.setHooksIncluded(true);
		alcp.setAttributeMapsShown(true);
		alcp.setShowChecked(true);
		alcp.setIncludeLoopPlaceHolders(false);
		alcp.setShowScriptComponents(true);
		alcp.setShowIncludedScripts(true);
		stepTree.setContentProvider(alcp);
		stepTree.setCheckStateProvider(alcp);
		stepTree.addCheckStateListener(alcp);
		alcp.setFeedFlowShown(true);

		// -- Set a customized ConfigLabelProvider that prefixes each item with a "*"
		// -- if there is a custom breakpoint condition set on it. Also, the tooltip
		// -- shows the custom breakpoint code as well as the first 10 lines of script
		// -- for attmaps, hooks and script configs.
		stepTree.setLabelProvider(new ConfigLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof RawConnectorConfig ||
						element instanceof ParserConfig ||
						element instanceof RawFunctionConfig)
					return Activator.getImage("Script_16");
				if (element instanceof AssemblyLineScripts)
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

				return super.getImage(element);
			}

			@Override
			public String getToolTipText(Object element) {
				if(element instanceof BaseConfiguration) {
					StringBuffer tooltip = new StringBuffer();

					BaseConfiguration b = (BaseConfiguration) element;
					if(b.getUserComment() != null && b.getUserComment().length() > 0) {
						tooltip.append(Messages.getString("StepperPanel.4"));
						tooltip.append("\n-------------------------------------------------\n");
						tooltip.append(b.getUserComment());
					}

					if((b instanceof AttributeMapItem || b instanceof ScriptConfig || b instanceof HookConfig)
							&& b.getScript() != null && b.getScript().length() > 0) {
						if(tooltip.length() > 0)
							tooltip.append("\n\n");
						tooltip.append(Messages.getString("StepperPanel.3"));
						tooltip.append("\n-------------------------------------------------\n");
						// append at most 10 lines so we dont clutter the UI
						StringTokenizer st = new StringTokenizer(b.getScript(), "\r\n");
						int max = 10;
						while(st.hasMoreTokens() && max > 0) {
							tooltip.append(st.nextToken() + "\n");
							max--;
						}
						if(st.hasMoreTokens())
							tooltip.append("...");
					} else if ( b instanceof AttributeMapItem ) {
						AttributeMapItem ami = (AttributeMapItem) b;
						String s = null;
						if (ami.isSubstitution()) {
							s = ami.getSubstitution();
						} else if (ami.isSimple()) {
							AttributeMapConfig map = Utils.getParentConfig(ami, AttributeMapConfig.class);
							boolean isALMap = (map != null && map.getParent() instanceof ALMappingConfig);
							s = Utils.getScript(!isALMap && Utils.isInputMap(map) ? "conn":"work", ami.getSimple());
						}
						if (s != null && s.length() > 0) {
							if(tooltip.length() > 0)
								tooltip.append("\n\n");
							tooltip.append(Messages.getString("AttributeMap.3.label"));
							tooltip.append("\n");
							tooltip.append(s);
						}
					}
					if(tooltip.length() > 0)
						return tooltip.toString();
				}
				return super.getToolTipText(element);
			}

			@Override
			public String getText(Object element) {
				String str = super.getText(element);
				if (element instanceof RawConnectorConfig)
					str = "ConnectorScript";
				if (element instanceof ParserConfig)
					str = "ParserScript";
				if (element instanceof RawFunctionConfig)
					str = "FunctionScript";

				if (breakpoints != null && element instanceof BaseConfiguration) {
					int n = 0;
					String compName = myStepperPanel.getCompNameFromConf((BaseConfiguration) element);
					if (compName.contains("/Scripts/")) {
						str = compName.replace("/Scripts/", "");
					}
					for (Map.Entry<String, Breakpoint> me:breakpoints.entrySet()) {
						if (me.getValue().isEnabled() &&
								(me.getKey().startsWith(compName + "#") || me.getKey().startsWith(compName + ".")))
							n++;
					}
					if (n > 0)
						str = "("+n+") " + str;
				}
				if(element instanceof BaseConfiguration) {
					BaseConfiguration b = (BaseConfiguration) element;
					if(b.getUserComment() != null && b.getUserComment().length() > 0)
						str = "*" + str;

					if(element == currentElem)
						str = ">" + str;
				}
				if (element instanceof AssemblyLineScripts) {
					return Messages.getString("miadmin.foldernames.Scripts");
				}
				return str;
			}

		});

		if(getEditingConfig() != null) {
			stepTree.setInput(getEditingConfig());
			stepTree.expandAll();
		}

		// Enable per item tool-tips for this tree
		ColumnViewerToolTipSupport.enableFor(stepTree);
	}

	public void setInput(AssemblyLineConfig config) {
		setEditingConfig(config);
		getDisplay().syncExec(new Runnable() {
			public void run() {
				stepTree.setInput(getEditingConfig());
				stepTree.expandAll();
			}
		});
	}

	public void setBreak(BaseConfiguration b) {
		setBreak(b, false);
	}
	
	public void setBreak(BaseConfiguration b, boolean force) {
		if (currentBreak == b && !force)
			return;
		currentBreak = b;

		if (b instanceof HookConfig)
			alcp.setCurrentHook((HookConfig) b);
		else
			alcp.setCurrentHook(null);

		currentElem = alcp.findElement(b);
		if (currentElem == null) {
			stepTree.setSelection(StructuredSelection.EMPTY);
		} else {
			stepTree.setSelection(new StructuredSelection(currentElem), true);
		}
		stepTree.refresh();
	}

	public Breakpoint getBreakpoint(String name) {
		return breakpoints.get(name);
	}

	public void setBreakpoints(Hashtable<String, Breakpoint> bpoint) {
		this.breakpoints = bpoint;
		getDisplay().syncExec(new Runnable() {
			public void run() {
				for (Enumeration<String> en = breakpoints.keys(); en.hasMoreElements();) {
					String str = en.nextElement();
					if (str.indexOf('#')>0)
						continue; //Ignore script line breaks for now
					Breakpoint bp = (Breakpoint) breakpoints.get(str);
					BaseConfiguration b = myStepperPanel.getConfigForPath(str);
					if (b != null && (b instanceof HookConfig || b.getEnabled())) {
						alcp.setChecked(b, bp.isEnabled());
						// -- Set the user comment to the BP expression (used by tree to render and show tooltip)
						if (bp.getExpression() != null)
							b.setUserComment(bp.getExpression());
					} else {
						breakpoints.remove(str);
					}
				}
				stepTree.refresh();
				breakpoints.put(DebugServer.INIT_BREAK, new Breakpoint(DebugServer.INIT_BREAK, false, null));
			}
		});
	}

	public void addCheckStateListener(ICheckStateListener listener) {
		stepTree.addCheckStateListener(listener);
	}

	public boolean getChecked(Object element) {
		return alcp.isChecked(element);
	}

	public void removeCheckStateListener(ICheckStateListener listener) {
		stepTree.removeCheckStateListener(listener);
	}

	public boolean setChecked(Object element, boolean state) {
		return alcp.setChecked(element, state);
	}

	public CheckboxTreeViewer getStepTree() {
		return stepTree;
	}

	public void setShowAllHooks(boolean selection) {
		alcp.setDisabledHooksIncluded(selection);
		alcp.setAlHooksIncluded(selection);
		refreshTree();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if(stepTree != null)
			stepTree.getTree().setEnabled(false);
	}

	public void setShowAttributes(boolean show) {
		alcp.setAttributeMapsShown(show);
		refreshTree();
	}

	private void refreshTree() {
		BaseConfiguration b = currentBreak;
		currentBreak = null;
		if (b == null)
			stepTree.refresh();
		else
			setBreak(b, true);
	}

	public BaseConfiguration getBreak() {
		return currentBreak;
	}


}
