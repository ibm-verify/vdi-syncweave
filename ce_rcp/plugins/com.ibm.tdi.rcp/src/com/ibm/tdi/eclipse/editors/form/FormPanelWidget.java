/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.editors.form;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.swt.IFocusService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.di.config.base.FormItemConfigImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.FormConfig;
import com.ibm.di.config.interfaces.FormItemConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.script.ScriptEngine;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.util.TextEditorContextMenu;
import com.ibm.tdi.eclipse.util.UndoRedoSupport;
import com.ibm.tdi.eclipse.widget.BaseWidget;
import com.ibm.tdi.eclipse.widget.FormWidget2;
import com.ibm.tdi.eclipse.widget.ParameterSubstitutionWidget;
import com.ibm.tdi.eclipse.wizards.NewParserWizard;

/**
 * This class implements the the Panel syntax as defined in the TDI Forms
 * specification. It is not intended to be used outside a FormWidget context.
 * 
 */
public class FormPanelWidget extends BaseWidget {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/*
	 * These strings are used to tag field names with source information
	 * Tooltip should reveal the property/script used
	 */
	private static final String JAVASCRIPT_PREFIX = "{javascript ";
	private static final String PROPERTY_PREFIX = "{property.";
	private static final String propertyTag = Messages.getString("FormWidget.tag.property");
	private static final String substitutionTag = Messages.getString("FormWidget.tag.substitution");
	private static final String javaScriptTag = Messages.getString("FormWidget.tag.javascript");
	
	/*
	 * Definition of Tag, types and attributes names and values
	 */
	private static final String BUTTON_RADIO = "radio";

	private static final String BUTTON_CHECKBOX = "checkbox";

	private static final String BUTTON_PUSH = "push";

	private static final String ATTTR_TYPE = "type";

	private static final String ATTR_BINDING = "binding";

	private static final String ATTR_BEAN = "bean";

	private static final String ATTR_TOOLTIP = "tooltip";

	private static final String ATTR_SELECTED = "selected";

	private static final String ATTR_ENABLED = "enabled";

	private static final String ATTR_VISIBLE = "visible";

	private static final String ATTR_NAME = "name";

	private static final String ATTR_FILL = "fill";

	private static final String ATTR_EQUAL_SIZE = "equalSize";

	private static final String ATTR_COLUMNS = "columns";

	private static final String ATTR_LAYOUT = "layout";

	private static final String ATTR_SOURCE = "source";

	private static final String ATTR_TEXT = "text";

	private static final String ATTR_SIZE = "size";

	private static final String ATTR_PASSWORD = "password";

	private static final String ATTR_READONLY = "readonly";
	
	private static final String ATTR_MULTI_LINE = "multiline";

	private static final String ATTR_BUTTON_TYPE = "type";
	
	private static final String ATTR_LINKLABEL = "linklabel";

	private static final String ATTR_REQUIRED = "required";

	/*
	 * Event names
	 */
	private static final String EVENT_ONFOCUSOUT = "onfocusout";

	private static final String EVENT_ONFOCUS = "onfocus";

	private static final String EVENT_ONCLICK = "onclick";

	private static final String EVENT_ONCHANGE = "onchange";

	private static final String EVENT_ON_LOAD = "on_load";

	private static final String EVENT_ON_UNLOAD = "on_unload";
	
	/*
	 * Valid layout tags and values
	 */
	public static final String LAYOUT_FILL = "Fill";

	public static final String LAYOUT_GRID = "Grid";
	
	public static final String LAYOUT_ROW = "Row";
	
	public static final String LAYOUT_STACK = "Stack";
	
	/*
	 * Element tags
	 */
	private static final String TAG_VALUE = "Value";
	
	private static final String TAG_SCRIPT = "Script";
	
	private static final String TAG_BUTTON = "Button";
	
	private static final String TAG_TEXT = "Text";
	
	private static final String TAG_LABEL = "Label";
	
	private static final String TAG_GROUP = "Group";
	
	private static final String TAG_COMPOSITE = "Composite";
	
	private static final String TAG_COMBO = "Combo";
	
	private static final String TAG_TAB_FOLDER = "TabFolder";
	
	private static final String TAG_TAB_ITEM = "TabItem";
	
	private static final String TAG_FORM_ITEM = "FormItem";

	private static final String TAG_LAYOUT_DATA = "LayoutData";

	/*
	 * Values
	 */
	private static final String VALUE_FILL_BOTH = "both";

	private static final String VALUE_FILL_HORIZONTAL = "horizontal";

	private static final String VALUE_FILL_VERTICAL = "vertical";

	/*
	 * Variables
	 */
	private FormItemConfig formItem;
	
	private FormConfig formConfig;
	
	private FormWidget2 widget;
	
	private ScriptEngine se;
	
	private Hashtable<String, Control> controls = new Hashtable<String, Control>();
	
	private boolean eventsEnabled;

	/*
	 * This is the parsed panel markup
	 */
	private Document doc;

	public FormPanelWidget(Composite parent, BaseConfiguration config, FormItemConfig formItem, FormWidget2 formWidget2)
			throws Exception {
		super(parent, 0, config);
		this.formItem = formItem;
		formConfig = ((FormItemConfigImpl) formItem).getForm();
		this.widget = formWidget2;

		createUI();
	}

	private void createUI() {
		String xml = formItem.getStringParameter("panel");

		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
			Element root = doc.getDocumentElement();
			addScriptEngine(root);
			internalSetLayout(this, root);
			addUIElements(this, root);
			callOnLoad(root);
			installUndoRedo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void installUndoRedo() {
		IOperationHistory operationHistory = null;
		IUndoContext undoContext = null;
		if(widget != null && widget.getSite() != null) {
			operationHistory = widget.getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getOperationHistory();
			undoContext = widget.getSite().getWorkbenchWindow().getWorkbench().getOperationSupport().getUndoContext();
		}
		for(Control c : controls.values()) {
			if(c instanceof Text)
				new UndoRedoSupport((Text)c, formItem.getShortName(), operationHistory, undoContext);
			else if(c instanceof StyledText)
				new UndoRedoSupport((StyledText)c, formItem.getShortName(), operationHistory, undoContext);
		}
 	}
	
	public void callOnLoad(Element root) {
		try {
			setEventsEnabled(false);
			if (root != null)
				se.call(EVENT_ON_LOAD, new Object[] { root }, true);
			else
				se.call(EVENT_ON_LOAD, null, true);
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		} finally {
			setEventsEnabled(true);
		}
	}

	@Override
	public void dispose() {
		try {
			se.call(EVENT_ON_UNLOAD, null, true);
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		super.dispose();
	}

	private void addScriptEngine(Element root) throws Exception {
		se = new ScriptEngine(null);
		NodeList list = root.getElementsByTagName(TAG_SCRIPT);
		for (int index = 0; index < list.getLength(); index++) {
			Node node = list.item(index);
			try {
				if (node.getParentNode() == root)
					executeScript((Element) node);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}
		BaseConfiguration bc = getEditingConfig();
		if (bc instanceof FormConfig) {
			bc = Utils.getParentConfig(bc, ConnectorConfig.class);
			if (bc == null)
				bc = getEditingConfig();
		}
		se.declareStaticBean("config", getEditingConfig());
		se.declareStaticBean("form", widget);
		se.declareStaticBean("panel", this);
		se.declareStaticBean("formItem", formItem);
	}

	private void executeScript(Element node) throws Exception {
		String str = getNodeText(node);
		if (node.hasAttribute(ATTR_SOURCE)) {
			String src = node.getAttribute(ATTR_SOURCE);
			try {
				se.includeScript(src);
				return;
			} catch (Exception e) {
				BufferedReader inp = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(src)));
				StringBuilder buf = new StringBuilder();
				while ((str = inp.readLine()) != null) {
					buf.append(str);
					buf.append("\n");
				}
				inp.close();
				str = buf.toString();
			}
		}
		se.exec(str);
	}

	/**
	 * Call the named JavaScript function.
	 * @param func The name of the function.
	 * @param params Parameters to the function.
	 * @throws Exception If an error occurs.
	 */
	public void call(String func, Object[] params) throws Exception {
		se.call(func, params, true);
	}
	
	/**
	 * Sets the layout on a Composite control
	 * 
	 * @param composite
	 * @param elem
	 */
	private void internalSetLayout(Composite composite, Element elem) {
		String layout = elem.getAttribute(ATTR_LAYOUT);

		if (layout.equals("") || LAYOUT_FILL.equalsIgnoreCase(layout)) {
			composite.setLayout(new FillLayout());
		} else if (LAYOUT_GRID.equalsIgnoreCase(layout)) {
			String cols = elem.getAttribute(ATTR_COLUMNS);
			if (cols.length() == 0)
				cols = "2";

			String equalSize = elem.getAttribute(ATTR_EQUAL_SIZE);
			if (equalSize.length() == 0)
				equalSize = "false";

			composite.setLayout(new GridLayout(Integer.valueOf(cols), Boolean.valueOf(equalSize)));
		} else if (LAYOUT_ROW.equalsIgnoreCase(layout)) {
			RowLayout rl = new RowLayout();
			setValues(rl, elem);
			composite.setLayout(rl);
		} else if (LAYOUT_STACK.equalsIgnoreCase(layout)) {
			StackLayout sl = new StackLayout();
			setValues(sl, elem);
			composite.setLayout(sl);
		}

		internalSetLayoutData(composite, elem);
	}

	private void setValues(Object o, Element elem) {
		for (Field f : o.getClass().getFields()) {
			String str = elem.getAttribute(f.getName());
			if (str.length() > 0) {
				try {
					if (f.getType() == Integer.class || f.getType() == int.class) {
						f.setInt(o, getIntValue(str, o.getClass()));
					} else if (f.getType() == String.class) {
						f.set(o, str);
					} else if (f.getType() == Boolean.class || f.getType() == boolean.class) {
						f.setBoolean(o, Boolean.valueOf(str));
					}
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e);
				}
			}
		}

		// -- shortcut: fill=both|horizontal|vertical
		if (elem.hasAttribute(ATTR_FILL)) {
			if (elem.getAttribute(ATTR_FILL).equals(VALUE_FILL_BOTH)) {
				((GridData) o).grabExcessHorizontalSpace = true;
				((GridData) o).grabExcessVerticalSpace = true;
				((GridData) o).horizontalAlignment = SWT.FILL;
				((GridData) o).verticalAlignment = SWT.FILL;
			} else if (elem.getAttribute(ATTR_FILL).equals(VALUE_FILL_HORIZONTAL)) {
				((GridData) o).grabExcessHorizontalSpace = true;
				((GridData) o).horizontalAlignment = SWT.FILL;
			} else if (elem.getAttribute(ATTR_FILL).equals(VALUE_FILL_VERTICAL)) {
				((GridData) o).grabExcessVerticalSpace = true;
				((GridData) o).verticalAlignment = SWT.FILL;
			}
		}
	}

	private int getIntValue(String s, Class<?> clazz) throws Exception {
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException e) {
			// If this is not a number, we will try to lookup a field.
			SystemFunctions.doNothing();
		}
		Field f = null;
		try {
			f = clazz.getField(s);
		} catch (NoSuchFieldException nsfe) {
			f = SWT.class.getField(s);
		}
		return (Integer) f.get(null);
	}

	private void internalSetLayoutData(Control control, Element elem) {
		Object ld = control.getLayoutData();
		if (ld == null) {
			if (control.getParent().getLayout() instanceof GridLayout)
				ld = new GridData(SWT.DEFAULT, SWT.DEFAULT);
			else if (control.getParent().getLayout() instanceof RowLayout)
				ld = new RowData();
			else
				return;

			control.setLayoutData(ld);
		}

		// -- permit layout settings on main element as well as the child
		// LayoutData tag
		setValues(ld, elem);

		NodeList list = elem.getElementsByTagName(TAG_LAYOUT_DATA);
		for (int i = 0; i < list.getLength(); i++) {
			Element e = (Element) list.item(i);
			setValues(ld, e);
		}
	}

	public Control getControl(String name) {
		if (name == null)
			return null;
		else
			return controls.get(name);
	}

	private void recordControl(Control control, Element node) {
		String name = node.getAttribute(ATTR_NAME);
		if (name.length() > 0)
			controls.put(name, control);

		// -- set layout on container
		if (control instanceof Composite)
			internalSetLayout((Composite) control, node);

		// -- set layout data
		internalSetLayoutData(control, node);

		// -- add change/focus/etc handlers
		addHandler(control, node);

		// -- general control props

		// -- visible
		if (node.hasAttribute(ATTR_VISIBLE))
			control.setVisible(Boolean.valueOf(node.getAttribute(ATTR_VISIBLE)));

		// -- enabled
		if (node.hasAttribute(ATTR_ENABLED))
			control.setEnabled(Boolean.valueOf(node.getAttribute(ATTR_ENABLED)));

		// -- selection
		if (node.hasAttribute(ATTR_SELECTED) && control instanceof Button)
			((Button) control).setSelection(Boolean.valueOf(node.getAttribute(ATTR_SELECTED)));

		// -- tooltip
		if (node.hasAttribute(ATTR_TOOLTIP))
			control.setToolTipText(formConfig.translate(node.getAttribute(ATTR_TOOLTIP)));

		// -- register as bean?
		if (node.hasAttribute(ATTR_BEAN)) {
			try {
				se.declareStaticBean(node.getAttribute(ATTR_BEAN), control);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e, getShell());
			}
		}
		
		// -- Update link label for those controls that link their status to a label
		if (node.hasAttribute(ATTR_LINKLABEL) && node.hasAttribute(ATTR_NAME))
			updateLinkedLabel(node.getAttribute(ATTR_NAME));
		
		// -- Set background if it is required and add asterisk to link label
		if ("true".equals(node.getAttribute(ATTR_REQUIRED)))
			updateRequiredField(node);
		
	}

	/**
	 * Add listener for control. We always add the handlers so we can filter and
	 * bubble later and also dynamically.
	 * 
	 * @param control
	 * @param node
	 */
	private void addHandler(Control control, final Element node) {
		if (control instanceof Text) {
			((Text) control).addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					executeOnChange(node, e);
				}
			});
		} else if (control instanceof Button) {
			((Button) control).addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if ((((Button) e.widget).getStyle() & SWT.PUSH) > 0)
						executeOnClick(node, e);
					else
						executeOnChange(node, e);
				}
			});
		} else if (control instanceof Combo) {
			((Combo) control).addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					executeOnChange(node, e);
				}
			});
		}

		control.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				executeOnFocus(true, node, e);
			}

			public void focusLost(FocusEvent e) {
				executeOnFocus(false, node, e);
			}
		});

		control.setData("tdi.form.element", node);
	}

	/**
	 * Executes the named event if child has a handler defined for it.
	 * 
	 * @param eventName
	 * @param child
	 * @param event
	 */
	protected void executeEvent(String eventName, Element child, Object event) {
		if(!isEventsEnabled() || child == null)
			return;
		
		String name = child.getAttribute(ATTR_NAME);
		Widget control = getControl(name);
		if (control == null && event instanceof TypedEvent)
			control = ((TypedEvent) event).widget;

		String func = getEventHandler(eventName, child);
		try {
			if (func != null)
				se.call(func, new Object[] { name, control, child, event });
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
		
		NodeList list = child.getElementsByTagName(TAG_SCRIPT);
		for (int i = 0; i < list.getLength(); i++) {
			Element node = (Element) list.item(i);
			if (eventName.equals(node.getAttribute(ATTR_NAME))) {
				try {
					executeScript(node);
				} catch (Exception e) {
					EclipseAppender.logerror(e.toString(), e, getShell());
				}
			}
		}
		
		if(name != null && name.length() > 0)
			updateLinkedLabel(name);
	}

	private boolean isEventsEnabled() {
		return eventsEnabled;
	}
	
	private void setEventsEnabled(boolean enabled) {
		eventsEnabled = enabled;
	}

	/**
	 * Execute the onclick event.
	 * 
	 * @param child
	 * @param event
	 */
	protected void executeOnClick(Element child, Object event) {
		executeEvent(EVENT_ONCLICK, child, event);
	}

	/**
	 * Execute the onfocus/onfocusout events.
	 * 
	 * @param gained
	 * @param child
	 * @param event
	 */
	protected void executeOnFocus(boolean gained, Element child, Object event) {
		String func = gained ? EVENT_ONFOCUS : EVENT_ONFOCUSOUT;
		executeEvent(func, child, event);
	}

	/**
	 * Update configuration if child has a binding attribute and then execute
	 * the onchange event.
	 * 
	 * @param child
	 * @param event
	 */
	protected void executeOnChange(Element child, Object event) {
		if(isEventsEnabled() && child.hasAttribute(ATTR_BINDING)) {
			getEditingConfig().setParameter(child.getAttribute(ATTR_BINDING), getControlValue(((TypedEvent) event).widget));
		}
		executeEvent(EVENT_ONCHANGE, child, event);
	}

	/**
	 * Returns the value for the control.
	 * 
	 * @param control
	 * @return
	 */
	private Object getControlValue(Widget control) {
		if (control instanceof Button)
			return "" + ((Button) control).getSelection();
		else if (control instanceof Combo)
			return ((Combo) control).getText();
		else if (control instanceof Text)
			return ((Text) control).getText();
		else
			return null;
	}

	/**
	 * Returns the handler (e.g. script function name) for a specific event. If
	 * the node doesn't have a handler defined we bubble up the hierarchy for a
	 * Group/Composite/TabFolder/TabItem element that has this attribute. This
	 * makes it possible to define the event handler at a container level to
	 * handle all events for child nodes that does not override the event.
	 * 
	 * @param handler
	 * @param node
	 * @return Script function name or null if there are no handlers defined
	 */
	private String getEventHandler(String handler, Element node) {
		// -- Get onchange attribute ... allow the onchange param to be set on a
		// Group/Composite so that many controls
		// -- share the same handler.
		String func = node.hasAttribute(handler) ? node.getAttribute(handler) : null;
		if (func == null && node.getParentNode() instanceof Element) {
			return getEventHandler(handler, (Element) node.getParentNode());
		}
		return func;
	}

	/**
	 * Adds UI controls from the element
	 * 
	 * @param parent
	 * @param elem
	 */
	private void addUIElements(Composite parent, Element elem) {
		NodeList list = elem.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				addUIControl((Element) node, parent);
			}
		}
	}

	/**
	 * Adds controls based on the named FormItem configuration.
	 * 
	 * @param formItemName
	 */
	private void addUIFormItem(Element node, Composite parent) {
		String formItemName = node.getAttribute(ATTR_NAME);
		FormItemConfig fic = formConfig.getFormItem(formItemName);
		if (fic == null)
			return;
		
		Element newnode = convertFormItem(fic, node);
		if(newnode != null)
			addUIElements(parent, newnode);
	}
	

	/**
	 * Converts a FormItem to an Element matching the panel ui markup
	 * 
	 * @param fic
	 * @param node
	 * @return element or null if FormItem was not converted
	 */
	private Element convertFormItem(FormItemConfig fic, Element node) {
		String syntax = (fic.getSyntax() == null ? "string" : fic.getSyntax().toLowerCase());
		Element newnode = null;
		if("string".equals(syntax) || "password".equals(syntax) || "textarea".equals(syntax)) {
			newnode = node.getOwnerDocument().createElement(TAG_TEXT);
			newnode.setAttribute(ATTR_PASSWORD, "" + "password".equals(syntax));
			newnode.setAttribute(ATTR_MULTI_LINE, "" + "textarea".equals(syntax));
		} else if("boolean".equals(syntax)) {
			newnode = node.getOwnerDocument().createElement(TAG_BUTTON);
			newnode.setAttribute(ATTR_BUTTON_TYPE, BUTTON_CHECKBOX);
		} else if("droplist".equals(syntax) || "dropedit".equals(syntax)) {
			newnode = node.getOwnerDocument().createElement(TAG_COMBO);
			newnode.setAttribute("readonly", ""+"droplist".equals(syntax));
		}
		
		if(newnode == null)
			return null;
		
		newnode.setAttribute("name", fic.getShortName());
		// -- add label
		if(fic.getLabel() != null) {
			Element label = node.getOwnerDocument().createElement(TAG_LABEL);
			label.setAttribute("text", fic.getLabel());
			node.appendChild(label);
		}
		
		// -- add the control
		newnode.setAttribute(ATTR_FILL, VALUE_FILL_HORIZONTAL);
		node.appendChild(newnode);

		// -- add buttons
		if(fic.getScript() != null && fic.getScript().length() > 0) {
			Element button = node.getOwnerDocument().createElement(TAG_BUTTON);
			button.setAttribute(ATTR_NAME, fic.getShortName() + "_button1");
			button.setAttribute(EVENT_ONCLICK, fic.getScript());
			button.setAttribute(ATTR_TEXT, fic.getScriptLabel());
			if(fic.getScriptToolTip() != null)
				button.setAttribute(ATTR_TOOLTIP, fic.getScriptToolTip());
			node.appendChild(button);
		}
		
		if(fic.getScript2() != null && fic.getScript2().length() > 0) {
			Element button = node.getOwnerDocument().createElement(TAG_BUTTON);
			button.setAttribute(ATTR_NAME, fic.getShortName() + "_button2");
			button.setAttribute(EVENT_ONCLICK, fic.getScript2());
			button.setAttribute(ATTR_TEXT, fic.getScriptLabel2());
			if(fic.getScriptToolTip2() != null)
				button.setAttribute(ATTR_TOOLTIP, fic.getScriptToolTip2());
			node.appendChild(button);
		}
		
		return node;
	}

	/**
	 * Adds a UI control from the element definition
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUIControl(Element node, Composite parent) {
		String type = node.getNodeName();
		if (TAG_BUTTON.equalsIgnoreCase(type))
			addUIButton(node, parent);
		else if (TAG_TEXT.equalsIgnoreCase(type))
			addUIText(node, parent);
		else if (TAG_LABEL.equalsIgnoreCase(type))
			addUILabel(node, parent);
		else if (TAG_GROUP.equalsIgnoreCase(type))
			addUIGroup(node, parent);
		else if (TAG_COMPOSITE.equalsIgnoreCase(type))
			addUIComposite(node, parent);
		else if (TAG_COMBO.equalsIgnoreCase(type))
			addUICombo(node, parent);
		else if (TAG_TAB_FOLDER.equalsIgnoreCase(type))
			addUITabFolder(node, parent);
		else if (TAG_TAB_ITEM.equalsIgnoreCase(type))
			addUITabItem(node, parent);
		else if (TAG_FORM_ITEM.equalsIgnoreCase(type))
			addUIFormItem(node, parent);
	}

	/**
	 * Adds a Composite control to the parent
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUIComposite(Element node, Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		recordControl(composite, node);
		addUIElements(composite, node);
	}

	/**
	 * Adds a Group control to the parent
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUIGroup(Element node, Composite parent) {
		Group group = new Group(parent, SWT.SHADOW_IN);
		group.setText(formConfig.translate(node.getAttribute(ATTR_TEXT)));
		recordControl(group, node);
		addUIElements(group, node);
	}

	/**
	 * Adds a TabFolder control to the parent
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUITabFolder(Element node, Composite parent) {
		String placement = (node.hasAttribute("placement") ? node.getAttribute("placement") : "TOP");
		int flags = SWT.TOP;
		try {
			flags = SWT.class.getField(placement).getInt(null);
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}

		TabFolder tabFolder = new TabFolder(parent, flags);
		internalSetLayout(tabFolder, node);
		recordControl(tabFolder, node);

		addUIElements(tabFolder, node);
	}

	/**
	 * Adds a TabItem to the TabFolder parent
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUITabItem(Element node, Composite parent) {
		if (!(parent instanceof TabFolder)) {
			MessageDialog.openError(getShell(), "Panel", "TabItem tags can only appear as immediate children of TabFolder");
			return;
		}
		int flags = SWT.LEFT;
		TabFolder tabs = (TabFolder) parent;
		TabItem item = new TabItem(tabs, flags);
		item.setText(node.hasAttribute(ATTR_TEXT) ? node.getAttribute(ATTR_TEXT) : "(no text attribute)");

		Composite composite = new Composite(tabs, SWT.NONE);
		internalSetLayout(composite, node);
		item.setControl(composite);

		addUIElements(composite, node);
	}

	/**
	 * Adds a Label control to the parent
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUILabel(Element node, Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(formConfig.translate(node.getAttribute(ATTR_TEXT)));
		recordControl(label, node);
	}

	/**
	 * Adds a Text control to the parent
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUIText(Element node, Composite parent) {
		int flags = SWT.BORDER;
		if (Boolean.valueOf(node.getAttribute(ATTR_READONLY)))
			flags |= SWT.READ_ONLY;
		if (Boolean.valueOf(node.getAttribute(ATTR_PASSWORD)))
			flags |= SWT.PASSWORD;
		if (Boolean.valueOf(node.getAttribute(ATTR_MULTI_LINE)))
			flags |= SWT.MULTI;

		Text text = new Text(parent, flags);
		String size = node.getAttribute(ATTR_SIZE);
		if (size.length() > 0) {
			int hint = Integer.valueOf(size) * 15;
			if (parent.getLayout() instanceof GridLayout) {
				GridData gd = new GridData();
				gd.widthHint = hint;
				text.setLayoutData(gd);
			} else if (parent.getLayout() instanceof RowLayout) {
				RowData rd = new RowData();
				rd.width = hint;
				text.setLayoutData(rd);
			}
		}

		// -- Set value if bound
		String binding = node.getAttribute(ATTR_BINDING);
		if (binding != null && binding.length() > 0) {
			Object value = getEditingConfig().getParameter(binding);
			if(value != null && !(value instanceof Throwable)) {
				text.setText(value.toString());
			}
		}

		new TextEditorContextMenu(text);
		recordControl(text, node);
	}

	/**
	 * Adds a Button control to the parent
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUIButton(Element node, Composite parent) {
		String type = node.getAttribute(ATTTR_TYPE);
		int flags = SWT.PUSH;

		if (type.equals("") || type.equals(BUTTON_PUSH))
			flags = SWT.PUSH;
		else if (type.equals(BUTTON_CHECKBOX))
			flags = SWT.CHECK;
		else if (type.equals(BUTTON_RADIO))
			flags = SWT.RADIO;

		Button button = new Button(parent, flags);
		button.setText(formConfig.translate(node.getAttribute(ATTR_TEXT)));

		recordControl(button, node);
	}

	/**
	 * Adds a Combo control to the parent
	 * 
	 * @param node
	 * @param parent
	 */
	private void addUICombo(Element node, Composite parent) {
		int flags = SWT.DROP_DOWN;
		if (Boolean.valueOf(node.getAttribute(ATTR_READONLY)))
			flags |= SWT.READ_ONLY;

		Combo combo = new Combo(parent, flags);
		boolean sort = Boolean.valueOf(node.getAttribute("sort"));

		// -- CCP for editable combo
		if ((flags & SWT.READ_ONLY) == 0 && getEditor() != null && getEditor().getSite() != null) {
			Object o = getEditor().getSite().getService(IFocusService.class);
			if (o instanceof IFocusService)
				((IFocusService) o).addFocusTracker(combo, "com.ibm.tdi.text.control");
		}

		ArrayList<String> values = new ArrayList<String>();
		NodeList list = node.getElementsByTagName(TAG_VALUE);
		for (int i = 0; i < list.getLength(); i++) {
			String s;
			try {
				Element value = (Element) list.item(i);
				s = getNodeText(value);
				if (s == null || s.length() == 0)
					s = value.getAttribute(ATTR_TEXT);
				s = formConfig.translate(s);
				values.addAll(expandValue(s));
			} catch (Exception e) {
				s = e.toString();
				values.add(s);
			}
		}

		if (sort)
			Collections.sort(values);

		for (String str : values)
			combo.add(str);

		// -- Set value if bound
		String binding = node.getAttribute(ATTR_BINDING);
		if (binding != null && binding.length() > 0) {
			binding = getEditingConfig().getStringParameter(binding);
			if (binding != null)
				combo.select(combo.indexOf(binding));
		}

		new TextEditorContextMenu(combo);
		recordControl(combo, node);
	}

	/**
	 * Expands the str parameter based on standard keyword expansion values
	 * (e.g. @PARSERS@ etc)
	 * 
	 * @param str
	 * @return
	 */
	private List<String> expandValue(String str) {
		ArrayList<String> list = new ArrayList<String>();
		if ("@PARSERS@".equals(str)) {
			list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.PARSER_FOLDER));

		} else if ("@CONNECTORS@".equals(str)) {
			list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.CONNECTOR_FOLDER));

		} else if ("@FUNCTIONS@".equals(str)) {
			list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.FUNCTION_FOLDER));

		} else if ("@ATTRIBUTEMAPS@".equals(str)) {
			list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.ATTRIBUTEMAP_FOLDER));

		} else if ("@REUSE_CONNECTORS@".equals(str)) {
			list.addAll(ConfigUtils.getReuseConnectors(getEditingConfig()));

		} else if ("@ASSEMBLY_LINES@".equals(str) || "@ASSEMBLYLINES@".equals(str)) {
			list.addAll(ConfigUtils.getComponentsAsStrings(getEditingConfig(), MetamergeConfig.ASSEMBLYLINE_FOLDER));

		} else if ("@ATTRS".equals(str)) {
			ConnectorConfig cc = Utils.getParentConfig(getEditingConfig(), ConnectorConfig.class);
			if (cc != null)
				list.addAll(cc.getAttributeMap(true).getAttributeNames());
			Collections.sort(list);
		} else {
			list.add(str);
		}
		return list;

	}

	/**
	 * Returns the node text
	 * 
	 * @param node
	 * @return
	 * @throws Exception
	 */
	private String getNodeText(Node node) throws Exception {
		StringBuffer buf = new StringBuffer();
		Node n = node.getFirstChild();
		while (n != null) {
			switch (n.getNodeType()) {
			case Node.TEXT_NODE:
			case Node.CDATA_SECTION_NODE:
				buf.append(n.getNodeValue());
				break;
			case Node.ELEMENT_NODE:
				buf.append(getNodeText(n));
				break;
			default:
				break;
			}
			n = n.getNextSibling();
		}

		return buf.toString();
	}

	/**
	 * Returns the string value for a control
	 * 
	 * @param ctrl
	 * @return
	 */
	public String getText(String ctrl) {
		Control c = controls.get(ctrl);
		if (c instanceof Text)
			return ((Text) c).getText();
		else if (c instanceof Combo)
			return ((Combo) c).getText();
		else if (c instanceof Label)
			return ((Label) c).getText();
		else if (c instanceof Button)
			return ((Button) c).getText();
		else
			return null;
	}

	/**
	 * Updates the named control with the provided value. Notifications are turned off during update to prevent
	 * script handlers to execute and overwrite the new value.
	 *  
	 * @param ctrl
	 * @param value
	 */
	public void updateControl(String ctrl, String value) {
		setEventsEnabled(false);
		Control c = controls.get(ctrl);
		if(c instanceof Button)
			setChecked(ctrl, Boolean.valueOf(value));
		else if (c instanceof Combo)
			setComboSelection(ctrl, value, true);
		else
			setText(ctrl, value);
		setEventsEnabled(true);
	}
	
	/**
	 * Sets the selection of the combo control to the index of value. If value is not in the list of values it
	 * is added if append is true.
	 * 
	 * @param ctrl Name of control
	 * @param value The new selection
	 * @param append true if non-existing value is added
	 * @return the selection index after updating
	 */
	public int setComboSelection(String ctrl, String value, boolean append) {
		Control c = controls.get(ctrl);
		if(c instanceof Combo) {
			int sel = ((Combo)c).indexOf(value);
			if(sel == -1 && append) {
				((Combo)c).add(value, 0);
				sel = 0;
			}
			((Combo)c).select(sel);
			return ((Combo)c).getSelectionIndex();
		}
		return -1;
	}
	
	/**
	 * Sets the text property on a Button, Label or Text control
	 * 
	 * @param ctrl
	 * @param text
	 */
	public void setText(String ctrl, String text) {
		Control c = controls.get(ctrl);
		if (c != null) {
			try {
				Method m = c.getClass().getMethod("setText", new Class[] { String.class });
				m.invoke(c, new Object[] { text });
			} catch (Exception e) {
				return;
			}
		}
	}

	/**
	 * Sets the selection status on a Button
	 * 
	 * @param ctrl
	 * @param selected
	 */
	public void setChecked(String ctrl, boolean selected) {
		Control c = controls.get(ctrl);
		if (c instanceof Button)
			((Button) c).setSelection(selected);
	}

	/**
	 * Returns the checked status for a button
	 * 
	 * @param ctrl
	 * @return
	 */
	public boolean isChecked(String ctrl) {
		Control c = controls.get(ctrl);
		if (c instanceof Button)
			return ((Button) c).getSelection();
		else
			return false;
	}

	/**
	 * Loads a Form into the parent based on the formName and configuration
	 * 
	 * @param formName
	 * @param parent
	 * @param config
	 * @return
	 * @throws Exception
	 */
	public FormWidget2 loadForm(String formName, Composite parent, BaseConfiguration config) throws Exception {
		return new FormWidget2(parent, 0, config, formName);
	}

	/**
	 * This method calls into the script engine of the panel to update the value
	 * for the parameter.
	 * 
	 * @param value
	 * @throws Exception
	 * @throws Exception
	 */
	public void setValue(Object value) throws Exception {
		getEditingConfig().setParameter(formItem.getShortName(), value);
		se.call("setValue", new Object[] { value }, false);
	}

	/**
	 * This method calls into the script engine of the panel to get the value
	 * for the parameter.
	 * 
	 * @param value
	 * @throws Exception
	 */
	public Object getValue() {
		try {
			return se.call("getValue", null, true);
		} catch (Exception e) {
			return getEditingConfig().getParameter(formItem.getShortName());
		}
	}

	/**
	 * This method set the topControl field of the container's stack layout to
	 * control. Either parameter can be a string (name of the component) or the
	 * actual control.
	 * 
	 * When a parameter is a string it is used with getControl() to obtain the
	 * actual composite or control.
	 * 
	 * After the topControl is set the layout(true, true) is called on the
	 * container.
	 * 
	 * @param container
	 *            String or Composite object
	 * @param control
	 *            String or Control object
	 */
	public void setTopControl(Object container, Object control) {
		Composite c = (Composite) (container instanceof Composite ? container : getControl(container.toString()));
		Control ctrl = null;
		if (control != null)
			ctrl = (Control) (control instanceof Control ? control : getControl(control.toString()));
		((StackLayout) c.getLayout()).topControl = ctrl;
		c.layout(true, true);
	}

	/**
	 * Returns the XML element that was the source for the specified control.
	 * 
	 * @param control
	 * @return
	 */
	public Element getElement(String control) {
		return getElement(control, -1);
	}

	/**
	 * Returns the XML element that was the source for the specified control. If
	 * index greater than -1 the child element of the control is returned.
	 * 
	 * @param control
	 * @param index
	 *            The index (Combo value)
	 */
	public Element getElement(String control, int index) {
		if(control == null)
			return null;
		
		Control ctl = getControl(control);
		Element top = ctl == null ? null : (Element) ctl.getData("tdi.form.element");
		if (top == null || index < 0)
			return top;

		NodeList list = top.getElementsByTagName(TAG_VALUE);
		if (list == null)
			return null;

		if (list.getLength() > index)
			return (Element) list.item(index);

		return null;
	}

	/**
	 * Returns a map of system component names. The key is the path to the
	 * component relative to this configuration and the value is the title for
	 * the same component.
	 */
	public Map<String, String> getSystemComponents(int type) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (String str : ConfigUtils.getAvailableSystemComponents(getEditingConfig(), type)) {
			try {
				String name = Utils.getFormName((BaseConfiguration) getEditingConfig().getMetamergeConfig().lookup(str));
				if (name != null)
					map.put(str, name);
				else
					map.put(str, str);
			} catch (Exception e) {
				map.put(str, str);
			}
		}
		return map;
	}

	/**
	 * Selects a parser and returns the path to the parser or null if dialog was
	 * cancelled.
	 * 
	 */
	public String selectParser() {
		NewParserWizard wiz = new NewParserWizard();
		wiz.init(null, new StructuredSelection(getEditingConfig()));
		wiz.setChooseFileName(false);
		wiz.setIncludeNullSelection(true);

		WizardDialog dlg = new WizardDialog(getShell(), wiz);
		if (dlg.open() == Window.OK) {
			return wiz.getConfigObject().getInheritsFromRef();
		}
		return null;
	}

	/**
	 * This method is used from a panel script to provide the param subst editor dialog. The return value is the
	 * new (expanded) value from the configuration.
	 * 
	 * @param config The config to update
	 * @param param The param to read/write
	 * @param toolTip The dialog title
	 * @param label The prompt label
	 * @param multiline true if multiline input is permitted in expression editor
	 * @param update if true this method updates the control assuming the name of the control and param is the same
	 */
	public String showParamSubstEditor(BaseConfiguration config, String param, String toolTip, String label, boolean multiline, boolean update) {
		String str = ParameterSubstitutionWidget.openPSDialog(getShell(), config, param, toolTip, label, multiline);
		if (str != null) {
			// If user chooses no property just clear the value with an
			// empty string
			if (str.equals("")) {
				config.setParameter(param, "");
			} else {
				config.setParameterPropertySource(param, str);
			}
		}
		
		Object value = config.getParameter(param);
		if(value instanceof Throwable) {
			str = "";
		} else if (value != null) {
			str = value.toString();
		} else {
			str = null;
		}
		
		if(update)
			updateControl(param, str);
		
		updateLinkedLabel(param);
		
		return str;
	}
	
	/**
	 * If a control has a linked label we update the label to reflect the value source (e.g. property, javascript etc).
	 * 
	 * @param param The id of the control 
	 */
	private void updateLinkedLabel(String param) {
		String ext = getEditingConfig().getParameterPropertySource(param);
		Element source = getElement(param);
		String labelCtl = source.getAttribute(ATTR_LINKLABEL);
		Element elem = null;
		Control ctl = null;
		if(labelCtl != null && labelCtl.length() > 0) {
			elem = getElement(labelCtl);
			ctl = getControl(labelCtl);
		}
		
		if(elem == null) {
			widget.updateRequiredFieldsMessages();
			return;
		}
		
		String org = formConfig.translate(elem.getAttribute(ATTR_TEXT));
		if("true".equals(source.getAttribute(ATTR_REQUIRED)))
			org += " *";
		
		if (ext != null && ctl instanceof Label) {
			String tooltip = "";
			if (ext.startsWith(PROPERTY_PREFIX)) {
				org += "\n" + propertyTag;
				tooltip = ext.substring(PROPERTY_PREFIX.length(), ext.length() - 1);
			} else if (ext.startsWith(JAVASCRIPT_PREFIX)) {
				org += "\n" + javaScriptTag;
				tooltip = ext.substring(JAVASCRIPT_PREFIX.length(), ext.length() - 1);
			} else {
				org += "\n" + substitutionTag;
			}
			
			if(tooltip != null)
				ctl.setToolTipText(tooltip);
			((Label)ctl).setText(org);
		} else if (ctl instanceof Label) {
			Label l = (Label) ctl;
			if(elem != null) {
				l.setToolTipText(null);
				l.setText(org);
			}
		}
		
		if(ctl != null) {
			ctl.getParent().layout(true, true);
		}
				
		widget.updateRequiredFieldsMessages();
	}

	/**
	 * Sets the background on the required UI control and tags the link label with an asterisk.
	 * @param node
	 */
	private void updateRequiredField(Element node) {
		Control control = getControl(node.getAttribute(ATTR_NAME));
		if(control == null)
			return;
		
		control.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		control.setForeground(getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		
		if(node.hasAttribute(ATTR_LINKLABEL)) {
			Control link = getControl(node.getAttribute(ATTR_LINKLABEL));
			if(link instanceof Label) {
				Label label = (Label) link;
				if(!label.getText().endsWith("*"))
					label.setText(label.getText() + " *");
			}
		}
		
	}

	/**
	 * This method is called from the FormWidget2 to let the panel provide error messages
	 * for parameters with invalid or missing values.
	 * 
	 * @param msgs
	 */
	public void updateRequiredFieldsMessages(ArrayList<IMessage> msgs) {
		Element root = doc.getDocumentElement();
		updateRequiredFieldsMessages(msgs, root);
	}

	/**
	 * Generate error message for each UI control that has a direct binding to a config value. If the config
	 * value is invalid (e.g. typically a bad javascript expression) we add an error message. For required fields without
	 * a binding we add a field missing error if the control is visible and has no value.
	 * 
	 * @param msgs
	 * @param elem
	 */
	private void updateRequiredFieldsMessages(ArrayList<IMessage> msgs, Element elem) {
		NodeList list = elem.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (node instanceof Element) {
				Element e = (Element) node;
				String name = e.getAttribute(ATTR_NAME);
				if(e.hasAttribute(ATTR_BINDING)) {
					// Field with a direct config binding
					String param = e.getAttribute(ATTR_BINDING);
					Object value = getEditingConfig().getParameter(param);
					
					if(value instanceof Throwable) {
						Element link = getElement(e.getAttribute(ATTR_LINKLABEL));
						addErrorField(name, link, value, msgs);
					} else if ( (value == null || value.equals("")) && "true".equals(e.getAttribute(ATTR_REQUIRED))) {
						Element link = getElement(e.getAttribute(ATTR_LINKLABEL));
						addMissingField(name, link, msgs);
					}
					
				} else if ("true".equals(e.getAttribute(ATTR_REQUIRED))) {
					// Required field without a binding, check for visibility
					Control ctl = getControl(name);
					if(ctl != null && ctl.isVisible()) {
						Object value = getControlValue(ctl);
						if(value == null || value.equals("")) {
							Element link = getElement(e.getAttribute(ATTR_LINKLABEL));
							addMissingField(name, link, msgs);
						}
					}
				}
				updateRequiredFieldsMessages(msgs, e);
			}
		}
	}
	
	private void addErrorField(final String name, final Element link, final Object value, ArrayList<IMessage> msgs) {
		msgs.add(new IMessage() {
			public int getMessageType() {
				return IMessage.ERROR;
			}
			
			public String getMessage() {
				if(link != null)
					return widget.translate(link.getAttribute(ATTR_TEXT)) + ": " + value.toString();
				else
					return name + ": " + value.toString(); 
			}
			
			public String getPrefix() {
				return null;
			}
			
			public Object getKey() {
				return name;
			}
			
			public Object getData() {
				return null;
			}
			
			public Control getControl() {
				return controls.get(name);
			}
		});
	}
	
	private void addMissingField(final String name, final Element link, ArrayList<IMessage> msgs) {
		msgs.add(new IMessage() {
			public int getMessageType() {
				return IMessage.ERROR;
			}
			
			public String getMessage() {
				String label = name;
				if(link != null)
					label = widget.translate(link.getAttribute(ATTR_TEXT));
				return Messages.getMessage("FormWidget2.required", label);
			}
			
			public String getPrefix() {
				return null;
			}
			
			public Object getKey() {
				return name;
			}
			
			public Object getData() {
				return null;
			}
			
			public Control getControl() {
				return controls.get(name);
			}
		});
	}
}
