/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.util.HookTree;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.RunReportAction;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.widget.BaseWidget;

public class UserCommentView extends ViewPart implements ModifyListener, IObjectActionDelegate {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private StyledText text;
	private BaseConfiguration config;
	private BaseWidget base;

	public UserCommentView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		base = new BaseWidget(parent, SWT.NONE);
		base.createForm(base, null);
		base.setLayout(new FillLayout());
		base.getForm().getBody().setLayout(new FillLayout());
		
		base.getForm().getToolBarManager().add(new Action() {

			@Override
			public String getText() {
				return Messages.getString("action.label.26");
			}

			@Override
			public void run() {
				if(config != null && config.getMetamergeConfig() instanceof TDIConfigurationFile) {
					String template = Activator.getInstallPath() + "/XSLT/ConfigReports/UserCommentsReport.xsl";
					RunReportAction rra = new RunReportAction();
					try {
						rra.create(((TDIConfigurationFile)config.getMetamergeConfig()).getFile(), template);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		});
		base.getForm().getToolBarManager().update(true);
		
		text = new StyledText(base.getForm().getBody(), SWT.V_SCROLL|SWT.H_SCROLL);

		getViewSite().getWorkbenchWindow().getSelectionService().addSelectionListener(new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				if(selection instanceof IStructuredSelection) {
					Object obj = ((IStructuredSelection)selection).getFirstElement();
					if(obj instanceof BaseConfiguration) {
						setBaseConfig((BaseConfiguration) obj);
					} else {
						setBaseConfig(null);
					}	
				} else {
					setBaseConfig(null);
				}	
			}
		});
		
		// Set initial text
		ISelection selected = getViewSite().getWorkbenchWindow().getSelectionService().getSelection();
		if (selected instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection)selected).getFirstElement();
			if(obj instanceof BaseConfiguration)
				setBaseConfig((BaseConfiguration) obj);
			else if(obj instanceof HookTree)
				setBaseConfig(((HookTree)obj).getHookConfig(false));
		}
	}
	
	protected void setBaseConfig(BaseConfiguration config) {
		if (config != null && ("EntryFeedContainer".equals(config.getShortName()) || "DataFlowContainer".equals(config.getShortName()))) {
			config = Utils.getParentConfig(config, AssemblyLineConfig.class);
		}
		this.config = config;
		
		if (text == null || text.isDisposed())
			return;

		text.removeModifyListener(this);
		if(config == null) {
			base.getForm().setText("");
			text.setText("");
		} else {
			base.getForm().setText(config.getShortName());
			String str = config.getUserComment();
			if(str == null)
				str = "";
			text.setText(str);
			text.addModifyListener(this);
		}
	}

	@Override
	public void setFocus() {
		if(text != null)
			text.setFocus();
	}

	public void modifyText(ModifyEvent e) {
		if(config != null) {
			config.setUserComment(text.getText());
		}
	}

	// Object action delegate methods
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("com.ibm.tdi.eclipse.views.UserCommentView");
		} catch (PartInitException e) {
			EclipseAppender.logerror(e.getMessage(), e);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
