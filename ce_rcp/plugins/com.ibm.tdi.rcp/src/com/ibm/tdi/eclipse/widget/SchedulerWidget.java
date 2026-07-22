/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.forms.widgets.Form;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.SchedulerConfig;
import com.ibm.tdi.eclipse.ConfigUtils;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * This widget allows the user to create a Scheduler.
 */

public class SchedulerWidget extends BaseWidget {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Composite body;       // The body content
	final private SchedulerConfig config; //The SchedulerConfig we are editing
	private FormWidget2 formWidget;     // The current FormWidget2 that is being used
	private MetamergeConfigChangeListener listener;
	
	/**
	 * The standard constructor
	 * @param parent
	 * @param style
	 * @param editingConfig
	 */
	public SchedulerWidget(Composite parent, int style,	BaseConfiguration editingConfig) {
		super(parent, style, editingConfig);
		config = (SchedulerConfig) editingConfig;
		createUI();
	}

	/**
	 * Creates the UI
	 */
	private void createUI() {

		setLayout(new FillLayout());
		final Form form = createForm(this, null);
		form.setText(getEditingConfig().getShortName());
		listener = new MetamergeConfigChangeListener() {
			public void configurationChanged(MetamergeConfigChange changeEvent) {
				if (isDisposed())
					return;
				if (changeEvent.getSource() == config &&
						BaseConfigurationImpl.NAME.equals(changeEvent.getKey()))
					form.setText(config.getShortName());
			}			
		};
		config.addListener(listener);

        Composite header = new Composite(form.getHead(), SWT.NONE);
        createHeader(header);
        form.setHeadClient(header);

        body = form.getBody();
        body.setLayout(new FillLayout());
 
		selectType(config.getType());
	}

	/**
	 * Create the header
	 * @param header
	 */
	private void createHeader(Composite header) {
		GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 10;
		header.setLayout(layout);

		Group typeGroup = new Group(header, SWT.SHADOW_NONE|SWT.TRANSPARENT);
		typeGroup.setText(Messages.getString("SchedulerWidget.Type"));
		typeGroup.setLayout(new RowLayout());

		final Button keepAlive = new Button(typeGroup, SWT.RADIO);
		keepAlive.setText(Messages.getString("SchedulerWidget.KeepAlive"));
		keepAlive.setSelection(config.getType() == SchedulerConfig.KEEP_ALIVE);

		final Button timer = new Button(typeGroup, SWT.RADIO);
		timer.setText(Messages.getString("SchedulerWidget.Timer"));
		timer.setSelection(config.getType() == SchedulerConfig.TIMER);

		keepAlive.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (keepAlive.getSelection())
					selectType(SchedulerConfig.KEEP_ALIVE);
			}			
		});

		timer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (timer.getSelection())
					selectType(SchedulerConfig.TIMER);
			}			
		});
		
		final Button enableButton = new Button(header, SWT.CHECK);
		enableButton.setText(Messages.getString("Localized.Enabled"));
		enableButton.setSelection(config.getEnabled());
		enableButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				config.setEnabled(enableButton.getSelection());
			}
		});

		final Button helpButton = new Button(header, SWT.PUSH);
		helpButton.setText(Messages.getString("general.help.label"));
		helpButton.setToolTipText(Messages.getString("general.help.tooltip"));
		helpButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						ConfigUtils.showHelp("SCHEDULER");
					}
				});
			}			
		});
		helpButton.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
	}

	private int oldType = -1; // Remember what the old type was, no need to do double changes.

	/**
	 * Changes type of scheduler, Timer or KeepAlive
	 * @param type
	 */
	private void selectType(int type) {
		if (type == oldType)
			return;
		if (oldType != -1)
			config.setType(type);
		oldType = type;

		if (formWidget != null)
			formWidget.dispose();

		try {
			if (type == SchedulerConfig.TIMER) {
				formWidget = new FormWidget2(body, SWT.NONE, config, "Scheduler"); //$NON-NLS-1$
			} else if (type == SchedulerConfig.KEEP_ALIVE) {
				formWidget = new FormWidget2(body, SWT.NONE, config, "KeepAlive"); //$NON-NLS-1$			
			}
		} catch (Exception e) {
			EclipseAppender.logerror("SchedulerWidget.selectType", e); // Cannot happen
		}

		body.layout(true, true);
	}

	@Override
	public void dispose() {
		if (formWidget != null)
			formWidget.dispose();
		config.removeListener(listener);
		super.dispose();
	}

}
