/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;

import com.ibm.tdi.eclipse.Activator;

/**
 * This class provides a simple wrapper around the ToolBar that invokes a method
 * on the listener object when buttons are pressed, combos selected etc.
 * 
 * When an item is selected we first try to find a method having the same name
 * as the command (addXXX methods parameter). If not found we try to invoke the method
 * specified by get/setDefaultHandler (default is selectionEvent(SelectionEvent e)).
 *
 */
public class CommandBar extends Composite {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Object listener;
	private String defaultHandler = "selectionEvent";
	private ArrayList<Control> controlList = new ArrayList<Control>();
	private HashMap<String, Control> commands = new HashMap<String, Control>();
	private Label banner;

	/**
	 * Constructor
	 * 
	 * @param parent
	 * @param style
	 * @param listener
	 */
	public CommandBar(Composite parent, int style, Object listener) {
		super(parent, style);
		setBackground(parent.getBackground());
		this.listener = listener;
		setLayout(new FormLayout());
		setBanner("");
	}
	
	/**
	 * This method adds a Push button to the command bar. The method named
	 * by the <i>command</i>parameter is invoked when the button is pushed. The signature
	 * can either be name(SelectionEvent e) or name().
	 * @param str The button's label
	 * @param tooltip The tooltip text
	 * @param command The name of the method to invoke on selection
	 * @return The SWT Button object
	 */
	public Button addPushButton(String str, String tooltip, String command) {
		return addButton(str, tooltip, command, SWT.PUSH);
	}
	
	public Button addToggleButton(String str, String tooltip, String command) {
		return addButton(str, tooltip, command, SWT.TOGGLE);
	}
	
	public Button addCheckbox(String str, String tooltip, String command) {
		return addButton(str, tooltip, command, SWT.CHECK);
	}

	
	public Label addLabel(String string, String tooltip) {
		Label l = new Label(this, SWT.LEFT);
		l.setText(string);
		if(tooltip != null)
			l.setToolTipText(tooltip);
		
		l.setBackground(getBackground());
		setFormData(l);
		
		return l;
	}

	public void addSeparator() {
		Label l = new Label(this, SWT.LEFT);
		l.setText("  ");
		setFormData(l);
	}

	public Text addTextField(String str, String tooltip, String command, int size) {
		Text text = new Text(this, SWT.BORDER);
		text.setData("TDI_COMMAND", command);
		if(str != null)
			text.setText(str);
		text.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				fireCommand(e);
			}
		});
		
		FormData fd = new FormData();
		fd.left = attachLastControl();
		fd.bottom = new FormAttachment(100, -1);
		
		fd.width = size * 10; 
		text.setLayoutData(fd);
		
		return text;
	}
	
	private FormAttachment attachLastControl() {
		int offset;
		if(getLastControl() == banner && banner.getText().length() > 0)
			offset = 15;
		else if (getLastControl() == null)
			offset = 0;
		else
			offset = 3;
		return new FormAttachment(getLastControl(), offset);
	}

	public Button addButton(String str, String tooltip, String command, int style) {		
		Button item = new Button(this, style);
		
		Image image = Activator.getImage(str);
		if(image != null)
			item.setImage(image);
		else
			item.setText(str);
		
		if(tooltip != null)
			item.setToolTipText(tooltip);

		item.setData("TDI_COMMAND", command);

		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				fireCommand(e);
			}
		});
		
		item.setBackground(getBackground());
		
		setFormData(item);
		
		commands.put(command, item);
		
		return item;
	}
	
	public Combo addCombo(String tooltip, String command) {
		return addCombo(tooltip, command, 0);
	}

	public Combo addCombo(String tooltip, String command, int flags) {
		Combo combo = new Combo(this, SWT.DROP_DOWN|flags);
		combo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				fireCommand(e);
			}
		});
		if(tooltip != null)
			combo.setToolTipText(tooltip);
		combo.setData("TDI_COMMAND", command);
		setFormData(combo);
		return combo;
	}

	public ComboViewer addComboViewer(String tooltip, String command, int flags) {
		Combo combo = new Combo(this, SWT.DROP_DOWN|flags);
		ComboViewer viewer = new ComboViewer(combo);
		combo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				fireCommand(e);
			}
		});
		if(tooltip != null)
			combo.setToolTipText(tooltip);
		combo.setData("TDI_COMMAND", command);
		setFormData(combo);
		return viewer;
	}

	protected FormData setFormData(Control item) {
		FormData fd = new FormData();
		if(item instanceof Label || item instanceof Link) {
			fd.bottom = new FormAttachment(100,-3);
		} else {
			fd.top = new FormAttachment(0,5);
		}
		
		if( (item.getStyle() & SWT.CHECK) > 0)
			fd.bottom = new FormAttachment(100,-1);

		fd.left = attachLastControl();
		item.setLayoutData(fd);
		controlList.add(item);
		
		return fd;
	}
	
	private Control getLastControl() {
		if(controlList.size() > 0)
			return controlList.get(controlList.size()-1);
		else
			return banner;
	}

	protected void fireCommand(SelectionEvent event) {
		// try command specific method
		Control item = (Control) event.widget;
		if(invokeHandler((String) item.getData("TDI_COMMAND"), event))
			return;
		
		invokeHandler(getDefaultHandler(), event);
	}
	
	private boolean invokeHandler(String cmd, SelectionEvent event) {
		try {
			Method m = listener.getClass().getMethod(cmd, new Class[]{SelectionEvent.class});
			m.invoke(listener, new Object[]{event});
			return true;
		} catch (Exception nsm) {
			return invokeHandler(cmd);
		}
	}

	private boolean invokeHandler(String cmd) {
		try {
			Method m = listener.getClass().getMethod(cmd, (Class[])null);
			m.invoke(listener, (Object[])null);
			return true;
		} catch (Exception nsm) {
			return false;
		}
	}

	public String getDefaultHandler() {
		return defaultHandler;
	}

	public void setDefaultHandler(String defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	public void setBanner(String string) {
		if(banner == null) {
			banner = new Label(this, SWT.LEFT);
			banner.setFont(JFaceResources.getBannerFont());
			banner.setBackground(getBackground());
			FormData fd = new FormData();
			fd.bottom = new FormAttachment(100,-3);
			fd.left = new FormAttachment(3,0);
			banner.setLayoutData(fd);
		}
		banner.setText(string);
		layout();
	}

	public void setEnabled(String string, boolean b) {
		Control tmpControl = commands.get(string);
		if(tmpControl != null) {
			tmpControl.setEnabled(b);
		}
	}
	
	public Control getControl(String command) {
		return commands.get(command);
	}

	public Object getListener() {
		return listener;
	}

	public void setListener(Object listener) {
		this.listener = listener;
	}

	public Link addLink(String text, String command) {
		Link item = new Link(this, SWT.NULL);
		item.setData("TDI_COMMAND", command);
		item.setText(text);
		item.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			public void widgetSelected(SelectionEvent e) {
				fireCommand(e);
			}
		});
		
		setFormData(item);
		
		commands.put(command, item);
		
		return item;
	}

	public Label getBanner() {
		return banner;
	}

	public int numberControls() {
		return controlList.size();
	}

	public void removeControl(int i) {
		controlList.get(i).dispose();
		controlList.remove(i);
		layout();
	}

}
