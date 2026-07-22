/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.ibm.icu.text.MessageFormat;

public class Messages {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static ResourceBundle RESOURCE_BUNDLE;
	static {
		RESOURCE_BUNDLE = Platform.getResourceBundle(Activator.getDefault().getBundle());
	}
	
	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return null;
		}
	}

	public static Label getIconLabel(Composite parent, String resource) {
		String label = getString(resource + ".label");
		String image = getString(resource + ".icon");
		String tip = getString(resource + ".tooltip");
		Label l = new Label(parent, SWT.LEFT);
		if (label != null)
			l.setText(label);
		if (image != null)
			l.setImage(Activator.getImage(image));
		if (tip != null)
			l.setToolTipText(tip);

		return l;
	}

	public static Button getButton(Composite parent, String resource) {
		String label = getString(resource + ".label");
		String image = getString(resource + ".icon");
		String tip = getString(resource + ".tooltip");
		Button l = new Button(parent, SWT.PUSH);
		if (label != null)
			l.setText(label);
		if (image != null)
			l.setImage(Activator.getImage(image));
		if (tip != null)
			l.setToolTipText(tip);

		return l;
	}

	public static String getMessage(String message, Object[] params) {
		String msg = getString(message);
		if (msg == null)
			return message + params[0];
		return MessageFormat.format(FixMessageFormat.fixPattern(msg), params);
	}

	public static String getMessage(String message, Object param) {
		String msg = getString(message);
		if (msg == null)
			return message + param;
		return MessageFormat.format(FixMessageFormat.fixPattern(msg), new Object[] { param });
	}

	public static String getMessage(String message, Object param1, Object param2) {
		String msg = getString(message);
		if (msg == null)
			return message + ": " + param1 + ", " + param2;
		return MessageFormat.format(FixMessageFormat.fixPattern(msg), new Object[] { param1, param2 });
	}

	public static class FixMessageFormat {

		protected MessageFormat format_ = null;

		public FixMessageFormat(String pattern, Locale locale) {
			format_ = new MessageFormat(pattern);
			format_.setLocale(locale);
			format_.applyPattern(fixPattern(pattern));
		}

		public final String format(Object[] args) {
			return format_.format(args);
		}

		private static final char SINGLE_QUOTE = '\''; // @01A3
		private static final char CURLY_BRACE_LEFT = '{';
		private static final char CURLY_BRACE_RIGHT = '}';

		private static final int STATE_INITIAL = 0; // @01A4
		private static final int STATE_SINGLE_QUOTE = 1;
		private static final int STATE_LITHERAL_BRACE = 2;
		private static final int STATE_MSG_ELEMENT = 3;

		public static String fixPattern(String pattern) { // @01
			StringBuffer buf = new StringBuffer(pattern.length() * 2);
			int state = STATE_INITIAL;
			for (int i = 0, j = pattern.length(); i < j; ++i) {
				char c = pattern.charAt(i);
				switch (state) {
				case STATE_INITIAL:
					switch (c) {
					case SINGLE_QUOTE:
						state = STATE_SINGLE_QUOTE;
						break;
					case CURLY_BRACE_LEFT:
						state = STATE_MSG_ELEMENT;
						break;
					}
					break;
				case STATE_SINGLE_QUOTE:
					switch (c) {
					case SINGLE_QUOTE:
						state = STATE_INITIAL;
						break;
					case CURLY_BRACE_LEFT:
					case CURLY_BRACE_RIGHT:
						state = STATE_LITHERAL_BRACE;
						break;
					default:
						buf.append(SINGLE_QUOTE);
						state = STATE_INITIAL;
					}
					break;
				case STATE_LITHERAL_BRACE:
					switch (c) {
					case SINGLE_QUOTE:
						state = STATE_INITIAL;
						break;
					}
					break;
				case STATE_MSG_ELEMENT:
					switch (c) {
					case CURLY_BRACE_RIGHT:
						state = STATE_INITIAL;
						break;
					}
					break;
				default: // This will not going to be happen.
				}
				buf.append(c);
			}
			// End of scan
			if (state == STATE_SINGLE_QUOTE) {
				buf.append(SINGLE_QUOTE);
			}
			return new String(buf);
		}
	}
}
