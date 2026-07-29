/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.views;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.ViewPart;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.xml.MetamergeConfigXML;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.UserFunctions;
import com.ibm.di.script.ScriptEngineOptions;
import com.ibm.jscript.JSContext;
import com.ibm.jscript.JSInterpreter;
import com.ibm.jscript.types.FBSGlobalObject;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.widget.SimpleTextEditor;

/**
 */

public class JavaScriptView extends ViewPart implements ISelectionListener {
	
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	public final static String VIEW_ID = "com.ibm.tdi.eclipse.views.JavaScriptView";
	
	private static final String AUTO_VARIABLES = "AutoVariables";

	private static final String SCRAP_SCRIPT = "Scrap";

	private TreeViewer viewer;

	private Action executeScriptAction;

	private Action resetScriptEngineAction;

	private JSInterpreter js;

	private ViewContentProvider treeContentProvider;
	
	private SimpleTextEditor editor;
	private String editorText = "";

	private Text autoVars;
	private String autoText = "";
	
	private Text output;

	private JSContext jsOptions;
	
	private UndoRedoActionGroup localUndoRedo;
	
	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */

	class ViewContentProvider implements ITreeContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return getChildren(js);
		}

		public Object[] getChildren(Object parentElement) {
			ArrayList<Object> children = new ArrayList<Object>();
			if(parentElement instanceof JSInterpreter) {
				try {
					FBSGlobalObject g = js.getGlobalObject();
					for (Iterator<String> i = g.getPropertyKeys(); i.hasNext();) {
						String prop = i.next();
						Object value = g.getProperty(prop);
						if (value != null && g.getProperty(prop).toJavaObject() != null)
							value = g.getProperty(prop).toJavaObject();

						if(value instanceof Entry || value instanceof Attribute)
							children.add(value);
						else
							children.add(prop + "=" + value);
						
					}
				} catch (Exception e) {
					children.add(e);
				}
			} else if (parentElement instanceof Entry) {
				Entry e = (Entry) parentElement;
				for(String key : e.getAttributeCollection()) {
					children.add(e.getAttribute(key));
				}
			}
			return children.toArray();
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return (element == js || element instanceof Entry);
		}
	}

	static class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			if(obj instanceof Entry)
				return "Entry";
			else if (obj instanceof Attribute)
				return ((Attribute)obj).getName() + "=" + obj;
			else
				return super.getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}

		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
	}

	/**
	 * The constructor.
	 */
	public JavaScriptView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		
		TabFolder tabs = new TabFolder(sash, SWT.BOTTOM);
		
		Font font = JFaceResources.getTextFont();
		
		MetamergeConfigXML mc = new MetamergeConfigXML();
		BaseConfigurationImpl config = new BaseConfigurationImpl();
		try {
			mc.initializeConfig();
			config.init();
		} catch(Exception ignore){}
		config.setMetamergeConfig(mc);
		
		editor = new SimpleTextEditor(tabs, SWT.BORDER, null);
		editor.setText(editorText);
		editor.getSourceViewer().getTextWidget().setFont(font);
		editor.setEditingConfig(config);

		localUndoRedo = new UndoRedoActionGroup(getSite(), 
				((IUndoManagerExtension) editor.getSourceViewer().getUndoManager()).getUndoContext(), true);
		localUndoRedo.fillActionBars(getViewSite().getActionBars());
		
		TabItem item = new TabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("JavaScriptView.1"));
		item.setControl(editor);
		
		output = new Text(tabs, SWT.BORDER|SWT.V_SCROLL);
		output.setText("");
		output.setFont(font);
		item = new TabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("JavaScriptView.2"));
		item.setControl(output);
		
		autoVars = new Text(tabs, SWT.BORDER|SWT.V_SCROLL);
		autoVars.setText(autoText);
		autoVars.setFont(font);
		item = new TabItem(tabs, SWT.LEFT);
		item.setText(Messages.getString("JavaScriptView.3"));
		item.setControl(autoVars);
		
		ViewForm form = new ViewForm(sash, SWT.FLAT); 
		
		treeContentProvider = new ViewContentProvider();
		viewer = new TreeViewer(form, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(treeContentProvider);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(js);
		makeActions();
		hookContextMenu();
		contributeToActionBars();
		
		form.setContent(viewer.getControl());
		Label title = new Label(form, SWT.LEFT);
		title.setText(Messages.getString("JavaScriptView.4"));
		form.setTopLeft(title);
		
		sash.setWeights(new int[]{50,50});
		
		resetEngine();
		
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				JavaScriptView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(executeScriptAction);
		manager.add(new Separator());
		manager.add(resetScriptEngineAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(executeScriptAction);
		manager.add(resetScriptEngineAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(executeScriptAction);
		manager.add(resetScriptEngineAction);
	}

	private void makeActions() {
		executeScriptAction = new Action() {
			public void run() {
				try {
					testScript(editor.getText());
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getSite().getShell());
				}
			}
		};
		executeScriptAction.setText(Messages.getString("JavaScriptView.5"));
		executeScriptAction.setImageDescriptor(Activator.getImageDescriptor("/icons/Run.gif"));

		resetScriptEngineAction = new Action() {
			public void run() {
				resetEngine();
			}
		};
		resetScriptEngineAction.setText(Messages.getString("JavaScriptView.6"));
		resetScriptEngineAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_OBJS_INFO_TSK));
	}

	protected void resetEngine() {
		jsOptions = ScriptEngineOptions.get();
		js = new JSInterpreter(jsOptions);
		try {
			js.getGlobalObject().put("task", FBSUtility.wrapAsObject(jsOptions, this));
			js.interpret(autoVars.getText());
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getSite().getShell());
		}
		viewer.setInput(js);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		if(memento != null) {
			String str = memento.getString(AUTO_VARIABLES);
			if(str != null)
				autoText = str;
			
			str = memento.getString(SCRAP_SCRIPT);
			if(str != null)
				editorText = str;
		}
		
		site.getWorkbenchWindow().getSelectionService().addSelectionListener(this);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString(AUTO_VARIABLES, autoVars.getText());
		memento.putString(SCRAP_SCRIPT, editor.getText());
	}

	public void testScript(String script) throws Exception {
		editor.setText(script);
		js.getGlobalObject().put("system", FBSUtility.wrap(jsOptions, new UserFunctions()));
		js.interpret(script);
		viewer.setInput(js);
	}
	
	public void logmsg(Object str) {
		output.append("" + str + "\n");
	}
	
	public void dumpEntry(Entry entry) {
		logmsg(entry.toDeltaString());
	}

	@Override
	public String toString() {
		return "JavaScriptView";
	}

}
