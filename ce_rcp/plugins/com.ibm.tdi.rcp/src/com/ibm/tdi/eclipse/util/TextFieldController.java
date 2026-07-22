/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.util;

import java.lang.reflect.Method;

import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Text;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * This class listens for keyboard events on a Text control and sets the configuration value
 * whenever an event is sent. Conversely, it listens on changes to the configuration object to set
 * the Text controls value on change.
 *  
 * @author NO010186
 *
 */
public class TextFieldController implements KeyListener, MetamergeConfigChangeListener, ITextListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private boolean updatingConfig = false;
	private Text text;
	private BaseConfiguration config;
	private String paramName;
	private TextViewer textViewer;
	private boolean expression;

	public TextFieldController(Text text, BaseConfiguration config, String paramName) {
		super();
		this.text = text;
		this.config = config;
		this.paramName = paramName;
		
		updateTextControl();
		
		config.addListener(this);
		text.addKeyListener(this);
	}

	public TextFieldController(TextViewer textViewer, BaseConfiguration config, String paramName) {
		super();
		this.textViewer = textViewer;
		this.config = config;
		this.paramName = paramName;
		
		updateTextControl();
		
		config.addListener(this);
		textViewer.addTextListener(this);
	}

	public boolean isExpression() {
		return expression;
	}

	public void setExpression(boolean expression) {
		this.expression = expression;
		updateTextControl();
	}

	public boolean isUpdatingConfig() {
		return updatingConfig;
	}

	public void setUpdatingConfig(boolean updatingConfig) {
		this.updatingConfig = updatingConfig;
	}

	public void configurationChanged(MetamergeConfigChange changeEvent) {
		if(isUpdatingConfig())
			return;
		if(changeEvent.getSource() == config && paramName.equals(changeEvent.getUserObject()))
			updateTextControl();
	}

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {
		updateConfig();
	}

	public void textChanged(TextEvent event) {
		updateConfig();
	}

	private void updateTextControl() {
		
		config.setFlags(config.getFlags() | BaseConfiguration.DISABLE_EXTPROPS);
		
		String value = null;
		if(paramName.endsWith("()")) {
			value = getConfigValueByMethod();
		} else if (isExpression()) {
			value = config.getParameterPropertySource(paramName);
		} else {
			value = config.getStringParameter(paramName);
		}
		
		if(value == null)
			value = "";

		if(text != null)
			text.setText(value);
		else if (textViewer != null)
			textViewer.getDocument().set(value);
	}

	private void updateConfig() {
		String value = (text != null ? text.getText() : textViewer.getDocument().get());
		setUpdatingConfig(true);
		if(paramName.endsWith("()"))
			updateConfigByMethod(value);
		else
			config.setStringParameter(paramName, value);
		
		setUpdatingConfig(false);
	}

	private void updateConfigByMethod(String value) {
		try {
			String name = paramName.substring(0, paramName.length() - 2);
			if(paramName.startsWith("get") || paramName.startsWith("set"))
				name = "set" + name.substring(3);
			else
				name = "set" + name;
			
			Method m = config.getClass().getMethod(name, new Class[]{String.class});
			m.invoke(config, new Object[]{value});
		} catch (Exception e) {
			EclipseAppender.logerror("SetConfigParam: " + paramName, e);
		}
	}

	private String getConfigValueByMethod() {
		try {
			String name = paramName.substring(0, paramName.length() - 2);
			if(paramName.startsWith("get") || paramName.startsWith("set"))
				name = "get" + name.substring(3);
			else
				name = "get" + name;
			
			Method m = config.getClass().getMethod(name, (Class[])null);
			Object obj = m.invoke(config, (Object[])null);
			if(obj instanceof String)
				return (String)obj;
			else if (obj == null)
				return null;
			else
				return obj.toString();
			
		} catch (Exception e) {
			EclipseAppender.logerror("GetConfigParam: " + paramName, e);
		}
		return null;
	}

}
