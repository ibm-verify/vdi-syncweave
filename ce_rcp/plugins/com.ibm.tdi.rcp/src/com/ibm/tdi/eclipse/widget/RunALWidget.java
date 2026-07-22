/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.ibm.di.api.DIException;
import com.ibm.di.api.remote.AssemblyLineListener;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.debugger.DebugClient;
import com.ibm.tdi.eclipse.debugger.DebugClientEvent;
import com.ibm.tdi.eclipse.debugger.DebugClientListener;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.editors.ConfigInstanceEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineEditor;
import com.ibm.tdi.eclipse.editors.RunAssemblyLineInput;
import com.ibm.tdi.eclipse.editors.RunRemoteAssemblyLineInput;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.server.RMILogger;
import com.ibm.tdi.eclipse.server.RMIServerAPI;
import com.ibm.tdi.eclipse.server.RestServerAPI;
import com.ibm.tdi.eclipse.stepper.StepperEvent;
import com.ibm.tdi.eclipse.stepper.StepperPanel;
import com.ibm.tdi.eclipse.views.EntryCollectorView;

/**
 * This widget hosts the StepperPanel widget and the LogViewer at the bottom
 * part in a SashForm. A progress bar for the debug session is kept in the
 * LogViewer toolbar.
 * 
 */
public class RunALWidget extends BaseWidget implements Runnable {
	/**
	 * 
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final String MEMENTO_EXPRESSIONS = "Expressions";
	
	private static final String MEMENTO_SASH_WEIGHTS = "RunALWidget.sash.weights";
	
	private RMILogger logger;
	private LogViewer text;
	private String str;
	private String configID;
	private String uniqueID;
	private RMIServerAPI api;
	private ToolItem rerunButton;
	private ToolItem stopConfigButton;
	private RunAssemblyLineInput runALInput;
	private SashForm sash;
	private StepperPanel stepPanel;
	private Composite stepPanelContainer;
	private ProgressBar progressBar;
	private File logFile;
	private BufferedWriter logStream;
	private ToolItem openLogFile;
	private boolean terminateOnDispose = true;
	private boolean debug;
	private int maxBufferLines;
	private boolean attached;

	// True if the user user pressed the stop button
	private boolean assemblyLineStopped;

	// -- Target for entries read/written
	protected EntryCollectorView collectorView;

	private DebugClient client;

	private SimpleTextEditor expression;

	// Saved expression list
	private ArrayList<String> expressionList = new ArrayList<String>();

	/**
	 * Used by the ConfigInstanceEditor upon receiving a debug session
	 * 
	 * @param parent
	 * @param api
	 * @param editor
	 */
	public RunALWidget(Composite parent, RestServerAPI api, ConfigInstanceEditor editor) {
		this(parent, null, null, editor);
		if (api instanceof RMIServerAPI)
			this.api = (RMIServerAPI) api;
		setAttached(true);
	}

	/**
	 * Used when the AL is started by the CE or attached by the server view
	 * 
	 * @param parent
	 * @param editingConfig
	 * @param ral
	 * @param editor
	 */
	public RunALWidget(Composite parent, BaseConfiguration editingConfig, RunAssemblyLineInput ral, BaseEditor editor) {
		super(parent, 0, editingConfig, editor);

		runALInput = ral;

		setLayout(new FillLayout());

		if (ral instanceof RunRemoteAssemblyLineInput) {
			RunRemoteAssemblyLineInput remote = (RunRemoteAssemblyLineInput) ral;
			if (remote.getApi() instanceof RMIServerAPI)
				api = (RMIServerAPI) remote.getApi();
			configID = remote.getCid();
			uniqueID = remote.getAlid();
		}

		if (ral != null) {
			debug = ral.isDebug() || ral.getBreakPoint() != null;
		} else if (editor != null) {
			debug = true;
		}

		maxBufferLines = Activator.getDefault().getPreferenceStore().getInt(PreferenceConstants.P_RUN_WINDOW_LINES);

		sash = new SashForm(this, SWT.VERTICAL);

		if (debug) {
			stepPanelContainer = new Composite(sash, SWT.NONE);
			stepPanelContainer.setLayout(new FillLayout());
			sash.setBackground(getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			createSteppePanel(stepPanelContainer);
		}

		Composite logc = new Composite(sash, SWT.NONE);
		logc.setLayout(new GridLayout(2, false));

		Composite tools = createRunALHeader(logc);
		tools.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false));

		// -- progress bar (remote is running / we are waiting)
		progressBar = new ProgressBar(logc, SWT.HORIZONTAL | SWT.INDETERMINATE | SWT.SMOOTH);

		// -- Log control
		text = new LogViewer(logc, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		text.setDocument(new Document());
		text.setEditingConfig(editingConfig);
		text.getControl().setFont(JFaceResources.getTextFont());
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = 2;
		text.setLayoutData(gd);

		if (debug)
			sash.setWeights(new int[] { 80, 20 });
		else
			sash.setWeights(new int[] { 100 });
	}

	private void createSteppePanel(Composite parent) {
		if (stepPanel != null) {
			if (!stepPanel.isDisposed()) {
				stepPanel.saveState(getMemento());
				stepPanel.dispose();
			}
			stepPanel = null;
		}

		// -- Stepper panel
		stepPanel = new StepperPanel(stepPanelContainer, SWT.NONE, getEditor());

		stepPanelContainer.layout();

	}

	public void startAssemblyLine(RestServerAPI api, RunAssemblyLineInput inp) throws Exception {

		if (! (api instanceof RMIServerAPI))
			return;
		
		this.api = (RMIServerAPI) api;
		
		client = new DebugClient(this.api, inp);
		client.addDebugListener(new DebugClientListener() {
			public void handleEvent(final DebugClientEvent event) {
				if (isDisposed()) {
					return;
				}
				
				switch (event.getCommand()) {
				case DebugClientEvent.STATE_CHANGE:
					getDisplay().syncExec(new Runnable() {
						public void run() {
							if (progressBar != null && !progressBar.isDisposed()) {
								if (client.isWaiting() || client.isIdle()) {
									progressBar.setVisible(false);
								} else {
									progressBar.setVisible(true);
								}
								if(client.isIdle())
									setStopped(true);
							}
						}
					});
					break;
					
				case DebugClientEvent.EVAL_MESSAGE:
					safeAppend(event.getEval());
					break;
				}
			}
		});

		if (stepPanel != null) {
			stepPanel.setDebugClient(client);
			stepPanel.setRunUntil(inp.getBreakPoint());
			inp.setBreakPoint(null); // If restart, we start from the beginning.
		}
		
		inp.setCollectingWork(inp.isDebug());

		logger = client.startAssemblyLine(createLogWriter(), maxBufferLines);

		setStopped(false);
		
		new Thread(this).start();

		if(inp.isCollectingWork()) {
			if(collectorView == null) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						collectorView = (EntryCollectorView) getEditor().getSite().getPage().findView(EntryCollectorView.VIEW_ID);
					}
				});
			}
			
			if(collectorView != null) {
				collectorView.clearAll(getEditor());
				((RMILogger)logger).setCycleDoneListener(new AssemblyLineListener() {
					private long cycleNo = 0;
					public void messageLogged(String arg0) throws DIException, RemoteException {
					}
					public void assemblyLineFinished() throws DIException, RemoteException {
						//((RMILogger)logger).setCycleDoneListener(null);
					}
					public void assemblyLineCycleDone(Entry arg0) throws DIException, RemoteException {
						try {
							collectorView.addEntry(getEditor(), arg0, cycleNo++);
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				});
			}
		}
	}

	public void handleExternalDebugSession(StepperEvent event, final CTabItem tabItem) throws Exception {
		
		RunAssemblyLineInput input = new RunAssemblyLineInput();
		input.setDebug(true);
		
		client = new DebugClient((RMIServerAPI) api, input);
		client.addDebugListener(new DebugClientListener() {
			private String alName;
			
			public void handleEvent(final DebugClientEvent event) {
				if (isDisposed()) {
					return;
				}
				
				switch (event.getCommand()) {
				case DebugClientEvent.STATE_CHANGE:
					getDisplay().syncExec(new Runnable() {
						public void run() {
							if (progressBar != null && !progressBar.isDisposed()) {
								if (client.isWaiting() || client.isIdle()) {
									progressBar.setVisible(false);
								} else {
									progressBar.setVisible(true);
								}
								if(client.isIdle())
									setStopped(true);
							}
						}
					});
					break;
					
				case DebugClientEvent.EVAL_MESSAGE:
					safeAppend(event.getEval());
					break;
					
				case DebugClientEvent.HELLO:
					alName = event.getData().toString();
					alName = alName.substring(alName.indexOf('/') + 1);
					break;
					
				case DebugClientEvent.UNIQUE_ID:
					try {
						String id = event.getData().toString();
						String cid = id.substring(0, id.lastIndexOf(":"));
						final String al = id.substring(id.lastIndexOf(":")+1);
						logger = client.attachAssemblyLineLog(cid, al);
						if (logger != null) {
							logger.setLogWriter(createLogWriter());
							new Thread(RunALWidget.this).start();
						}
						
						if (alName != null && tabItem != null) {
							tabItem.getDisplay().syncExec(new Runnable() {
								public void run() {
									tabItem.setText(alName + "." + al);
								}
							});
						}

					} catch (Exception e) {
						EclipseAppender.logerror(e.toString(), e);
					}
					break;
					
				}
			}
		});
		
		if (stepPanel != null)
			stepPanel.setDebugClient(client);
		
		setStopped(false);

		client.acceptDebugConnection(event);
}

	/**
	 * Appends the string to the text viewer and logfile. Checks if any of the
	 * widgets are disposed before doing so.
	 * 
	 * @param string
	 *            Message to append
	 */
	private void safeAppend(final String string) {
		if (isDisposed())
			return;

		getDisplay().syncExec(new Runnable() {
			public void run() {
				if (text == null || text.getControl() == null || text.getControl().isDisposed())
					return;

				try {
					text.append(string, maxBufferLines);
					revealEndOfDocument();
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
			}
		});
	}

	/**
	 * Creates the log file writer.
	 * 
	 */
	private BufferedWriter createLogWriter() {
		try {
			if(getEditingConfig() == null)
				logFile = Utils.getALLogFile(null, null);
			else
				logFile = Utils.getALLogFile(Utils.getProjectFor(getEditingConfig()), getEditingConfig().getShortName());
			logStream = new BufferedWriter(new FileWriter(logFile));
			if (openLogFile != null && ! openLogFile.isDisposed()) {
				//This method may have been called from a Non-UI Thread...
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (! openLogFile.isDisposed())
							openLogFile.setEnabled(true);
					}
				});
			}
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
		return logStream;
	}

	/**
	 * Updates UI elements based on run al state
	 * 
	 * @param stopped
	 */
	public void setStopped(boolean stopped) {
		assemblyLineStopped = false;
		if (stopped)
			saveState(getMemento());
		if (isDisposed())
			return;
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (rerunButton != null && !rerunButton.isDisposed())
					rerunButton.setEnabled(isStopped() && !debug && !isAttached());
				if (stopConfigButton != null && !stopConfigButton.isDisposed())
					stopConfigButton.setEnabled(isRunning());
				if (!progressBar.isDisposed())
					progressBar.setVisible(isRunning());
				if(expression != null && !expression.isDisposed())
					expression.setEnabled(isRunning());
				if (getEditor() instanceof RunAssemblyLineEditor)
					getEditor().setModified(isRunning());
			}
		});
	}

	public boolean isStopped() {
		return client == null || client.isIdle();
	}

	public boolean isRunning() {
		return !isStopped();
	}

	/*
	 * Runs in its own thread to receive and write the log messages to the text
	 * viewer and log file.
	 */
	public void run() {

		final ArrayList<String> buf = new ArrayList<String>();
		try {
			while (text != null && text.getControl() != null && !text.getControl().isDisposed()) {
				try {
					str = logger.getNextMessage();
					if (str == null)
						break;
					else if (assemblyLineStopped || client.isIdle())
						buf.add(str);
					else
						safeAppend(str + "\n");

				} catch (SocketTimeoutException tmo) {
					// No data received for 3 seconds. Just continue waiting for
					// more data.
					SystemFunctions.doNothing();
				}
			}

			// -- Print out the last lines in case AL was stopped while we were getting log messages
			if (!isDisposed() && buf.size() > 0) {
				getDisplay().syncExec(new Runnable() {
					public void run() {
						if (!text.getControl().isDisposed()) {
							try {
								int start = buf.size() > maxBufferLines ? buf.size() - maxBufferLines : 0;
								StringBuffer sb = new StringBuffer();
								if(start > 0)
									text.getDocument().set("");
								for(int i = start; i < buf.size(); i++) {
									sb.append(buf.get(i));
									sb.append("\n");
								}
								text.append(sb.toString(), maxBufferLines);
							} catch (Exception e) {
								SystemFunctions.doNothing();
							}
							revealEndOfDocument();
						}
					}
				});
			}

		} catch (Exception e) {
			EclipseAppender.logerror(e.getMessage(), e);
		} finally {
			if(logger != null)
				logger.close();
		}
	}

	/**
	 * Creates the toolbar at the top
	 * 
	 */
	public Composite createRunALHeader(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new FillLayout());

		// -- Toolbar
		final ToolBar tools = new ToolBar(c, SWT.NONE);

		// Do not add stop or rerun button in debug mode.
		// We do not want two stop buttons, and cannot rerun debug (for now).

		if (!debug) {
			// -- Stop
			stopConfigButton = new ToolItem(tools, SWT.PUSH);
			stopConfigButton.setImage(Activator.getImage("Stop")); //$NON-NLS-1$
			stopConfigButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					stopAssemblyLine();
					stopConfigInstance();
				}
			});
			stopConfigButton.setToolTipText(Messages.getString("RunAL.stop.tooltip")); //$NON-NLS-1$
			stopConfigButton.setEnabled(false);

			// -- Rerun
			rerunButton = new ToolItem(tools, SWT.PUSH);
			rerunButton.setImage(Activator.getImage("Run")); //$NON-NLS-1$
			rerunButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					try {
						stopConfigInstance();
						startAssemblyLine(api, runALInput);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			});
			rerunButton.setToolTipText(Messages.getString("RunAL.restart.tooltip")); //$NON-NLS-1$
			rerunButton.setEnabled(false);
		}

		// -- Clear buffer
		ToolItem item = new ToolItem(tools, SWT.PUSH);
		item.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
		item.setToolTipText(Messages.getString("Debugger.toolbar.Clear.tooltip")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setDocument(new Document());
			}
		});

		// -- Open log file
		openLogFile = new ToolItem(tools, SWT.PUSH);
		openLogFile.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE));
		openLogFile.setToolTipText(Messages.getString("RunAL.logfile.tooltip")); //$NON-NLS-1$
		openLogFile.setEnabled(false);
		openLogFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (logFile == null || logFile.isDirectory() || ! logFile.exists())
					return;
				try {
					if (logStream != null)
						logStream.flush();
					IFileStore fileStore = EFS.getLocalFileSystem().fromLocalFile(logFile);
					if (fileStore == null)
						return;
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IDE.openEditorOnFileStore(page, fileStore);
				} catch (Exception err) {
					EclipseAppender.logerror(err.toString(), err, getShell());
				}
			}
		});

		if (debug) {
			final ToolItem evalExpression = new ToolItem(tools, SWT.SEPARATOR);
			expression = new SimpleTextEditor(tools, SWT.SINGLE);
			expression.setData("placeholder", "true");
			expression.getSourceViewer().getTextWidget().addFocusListener(new FocusListener() {
				public void focusGained(FocusEvent e) {
					if ("true".equals(expression.getData("placeholder")))
						expression.setText("");
				}

				public void focusLost(FocusEvent e) {
					if (expression.getText().length() == 0) {
						expression.setData("placeholder", "true");
						expression.setText(Messages.getString("StepperPanel.placeholder"));
					} else {
						expression.setData("placeholder", "false");
					}
				}
			});
			expression.setText(Messages.getString("StepperPanel.placeholder"));
			expression.getSourceViewer().getTextWidget().addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == SWT.CR) {
						try {
							if (!expressionList.contains(expression.getText()))
								expressionList.add(expression.getText());
							client.evaluateExpression(expression.getText(), false);
						} catch (Exception e1) {
							EclipseAppender.logerror(e1.toString(), e1, getShell());
						}
					} else if (e.keyCode == SWT.ARROW_DOWN &&
							!expression.getSVC().isPopupProposalActive())
						showExpressionPopup(expression, tools);
				}
			});
			expression.showLineNumberPainter(false);
			expression.showLineNumberRuler(false);
			evalExpression.setControl(expression);

			final ToolItem clearHistory = new ToolItem(tools, SWT.PUSH);
			clearHistory.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
			clearHistory.setToolTipText(Messages.getString("RunALWidget.clear.history.tooltip")); //$NON-NLS-1$
			clearHistory.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					expressionList.clear();
				}
			});

			updateExpressionWidth(tools, evalExpression, clearHistory);

			tools.addControlListener(new ControlListener() {
				public void controlResized(ControlEvent e) {
					updateExpressionWidth(tools, evalExpression, clearHistory);
				}

				public void controlMoved(ControlEvent e) {
				}
			});

		}

		return c;
	}

	protected void showExpressionPopup(Control widget, Composite toolbar) {
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		for (String str : expressionList) {
			MenuItem item = new MenuItem(menu, SWT.PUSH);
			item.setText(str);
			item.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event e) {
					expression.setText(((MenuItem) e.widget).getText());
					expression.setData("placeholder", "false"); // defect 13181
				}
			});
		}
		Rectangle b = widget.getBounds();
		Point p = toolbar.toDisplay(b.x, b.y + b.height);
		menu.setLocation(p);
		menu.setVisible(true);
		while (!menu.isDisposed() && menu.isVisible()) {
			if (!getDisplay().readAndDispatch())
				getDisplay().sleep();
		}
		menu.dispose();
	}

	private boolean updating = false;
	protected void updateExpressionWidth(ToolBar tools, ToolItem evalExpression, ToolItem clearHistory) {
		if (updating || tools.isDisposed() || tools.getItemCount() < 3)
			return;

		Rectangle exp = evalExpression.getBounds();
		Rectangle area = tools.getClientArea();
		int width = area.width - (exp.x + clearHistory.getWidth());
		if (width > 0 && width != evalExpression.getWidth()) {
			updating = true;
			evalExpression.setWidth(width);
			tools.pack(true);
			updating = false;
		}
	}

	@Override
	public void dispose() {
		try {
			saveState(getMemento());
		} catch (Exception e) {
			// We may fail to get a Memento if parent is disposed
			SystemFunctions.doNothing();
		}
		// Run the cleanup in a thread
		new Job("") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// -- Terminate the config instance
				if (client != null) {
					if (isTerminateOnDispose()) {
						try {
							if(!client.isIdle() && !client.isPending())
								client.stopAssemblyLine();
						} catch (Exception e) {
							EclipseAppender.logerror(e.toString(), e);
						}
						client.stopConfigInstance();
					} else if (isStopped()) {
						client.stopConfigInstance();
					} else if (debug) {
						client.stopDebugging();
					}
				}

				// -- Close/Delete the log file
				if (logStream != null) {
					try {
						if (logger != null)
							logger.setLogWriter(null);
						logStream.close();
					} catch (IOException e) {
						EclipseAppender.logerror(e.toString(), e);
					}
					logStream = null;
				}

				if (logFile != null) {
					Utils.removeALLogFile(logFile);
					logFile = null;
				}

				// -- Terminate the debug thread
				return Status.OK_STATUS;
			}

		}.schedule();

		if (stepPanel != null)
			stepPanel.dispose();
		super.dispose();
	}

	public boolean stopAssemblyLine() {
		if (client == null)
			return false;

		try {
			if(!client.isIdle() && !client.isPending())
				client.stopAssemblyLine();
			assemblyLineStopped = true;
			return true;
		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
			return false;
		}
	}

	public boolean stopConfigInstance() {
		// TODO: Add some code to see if there are any AssemblyLines running
		// in the config Instance, and if so, ask the user if the Config
		// Instance
		// should be stopped.
		if(runALInput instanceof RunRemoteAssemblyLineInput)
			return false;
		
		client.stopConfigInstance();
		return true;
	}

	@Override
	public void restoreState(IMemento memento) {
		setMemento(memento);
		
		IMemento expr = memento.getChild(MEMENTO_EXPRESSIONS);
		if (expr != null) {
			String str = expr.getTextData();
			if (str != null && str.length() > 0) {
				for (String expression : str.trim().split("\n"))
					expressionList.add(expression);
			}
		}
		
		if (debug)
			getSashSettings(sash, MEMENTO_SASH_WEIGHTS);
	}

	@Override
	public void saveState(IMemento memento) {
		if (stepPanel != null && !stepPanel.isDisposed())
			stepPanel.saveState(memento);
		
		IMemento expr = memento.getChild(MEMENTO_EXPRESSIONS);
		if (expr == null)
			expr = memento.createChild(MEMENTO_EXPRESSIONS);
		StringBuilder buf = new StringBuilder();
		if(expressionList != null) {
			for (String str : expressionList) {
				buf.append(str);
				buf.append("\n");
			}
		}
		expr.putTextData(buf.toString());

		if (debug)
			saveSashSettings(sash, MEMENTO_SASH_WEIGHTS);
	}

	/**
	 * Scrolls the text viewer to the bottom making the last line visible
	 */
	protected void revealEndOfDocument() {
		IDocument doc = text.getDocument();
		int lines = doc.getNumberOfLines();
		if (lines == 0)
			return;
		try {
			// lines are 0-based
			int lineStartOffset = doc.getLineOffset(lines - 1);
			StyledText widget = text.getTextWidget();
			if (lineStartOffset > 0) {
				widget.setCaretOffset(lineStartOffset);
				widget.showSelection();
			}
			int lineEndOffset = lineStartOffset + doc.getLineLength(lines - 1);
			if (lineEndOffset > 0) {
				widget.setCaretOffset(lineEndOffset);
			}
		} catch (BadLocationException e) {
			// This should not happen.
			// Just catch it here, to avoid having to declare that this method
			// throws an Exception.
			SystemFunctions.doNothing();
		}
	}

	/**
	 * Returns the unique identifier for the assemblyline we started.
	 * 
	 * @return Unique AL identifier
	 */
	public String getUniqueID() {
		return uniqueID;
	}

	public void setUniqueID(String uniqueID) {
		this.uniqueID = uniqueID;
	}

	public void setConfigID(String configID) {
		this.configID = configID;
	}

	/**
	 * Returns the identifier of the config instance we have started .
	 * 
	 * @return Config ID
	 */
	public String getConfigID() {
		return configID;
	}

	/**
	 * Returns true if config and AL instance are terminated on dispose
	 * 
	 */
	public boolean isTerminateOnDispose() {
		return terminateOnDispose;
	}

	/**
	 * Sets the terminate on dispose flag.
	 * 
	 * @param terminateOnDispose
	 *            True if config and AL instance are terminated on dispose
	 */
	public void setTerminateOnDispose(boolean terminateOnDispose) {
		this.terminateOnDispose = terminateOnDispose;
	}

	/**
	 * Returns true if this widget attached to a running AL.
	 * 
	 */
	public boolean isAttached() {
		return attached;
	}

	public void setAttached(boolean attached) {
		this.attached = attached;
	}

	/**
	 * Creates a logger for the cid:al and starts a thread to display the log output
	 * 
	 * @param alid
	 * @throws Exception
	 */
	public void createLoggerObject(RunAssemblyLineInput input) throws Exception {
		client = new DebugClient((RMIServerAPI) api, input);
		client.addDebugListener(new DebugClientListener() {
			public void handleEvent(final DebugClientEvent event) {
				if (isDisposed()) {
					return;
				}
				
				switch (event.getCommand()) {
				case DebugClientEvent.STATE_CHANGE:
					getDisplay().syncExec(new Runnable() {
						public void run() {
							if (progressBar != null && !progressBar.isDisposed()) {
								if (client.isWaiting() || client.isIdle()) {
									progressBar.setVisible(false);
								} else {
									progressBar.setVisible(true);
								}
								if(client.isIdle())
									setStopped(true);
							}
						}
					});
					break;
					
				case DebugClientEvent.EVAL_MESSAGE:
					safeAppend(event.getEval());
					break;
				}
			}
		});

		if (stepPanel != null)
			stepPanel.setDebugClient(client);
		
		logger = client.attachAssemblyLine(input.isDebug());
		new Thread(this).start();
		
		setStopped(false);
	}
	
	/**
	 * Get weights from a memento and use in a SashFrom.
	 * This is used to remember window sizes.
	 * @param sash
	 * @param memento
	 * @param key
	 */
	public static void getSashSettings(SashForm sash, String key) {
		if (sash == null)
			return;
		
		String values = Activator.getDefault().getPreferenceStore().getString(key);
		if (values == null)
			return;

		String[] vals = values.split(",");
		if (vals.length >= 2) {
			int[] w = new int[vals.length];
			for (int i = 0; i< vals.length; i++) {
				w[i] = Integer.valueOf(vals[i]);
			}
			sash.setWeights(w);
		}
	}

	/**
	 * Gets the weights from a SashForm and save in a memento.
	 * Used to remember window sizes.
	 * @param sash
	 * @param memento
	 * @param key
	 */
	public static void saveSashSettings(SashForm sash, String key) {
		if (sash == null || sash.isDisposed())
			return;
		int[] w = sash.getWeights();
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < w.length; i++){
			if (i > 0)
				s.append(",");
			s.append(w[i]);
		}
		Activator.getDefault().getPreferenceStore().setValue(key, s.toString());
	}
}
