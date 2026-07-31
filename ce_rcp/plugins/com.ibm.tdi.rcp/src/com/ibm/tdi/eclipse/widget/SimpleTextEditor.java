/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.InstructionPointerAnnotation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IUndoManagerExtension;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.operations.UndoRedoActionGroup;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.swt.IFocusService;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import com.ibm.di.config.base.ScriptConfigImpl;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.script.ScriptEngineOptions;
import com.ibm.di.util.StringUtils;
import com.ibm.jscript.ParserResult;
import com.ibm.jscript.ScriptError;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.actions.TDIHelpMenuAction;
import com.ibm.tdi.eclipse.actions.TestScriptAction;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.extensions.ExtensionPointManager;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.preferences.JavaScriptPreferencePage;
import com.ibm.tdi.eclipse.preferences.PreferenceConstants;
import com.ibm.tdi.eclipse.text.ColorManager;
import com.ibm.tdi.eclipse.text.JavaScriptPartitionScanner;
import com.ibm.tdi.eclipse.text.JavaScriptPartitioner;
import com.ibm.tdi.eclipse.text.JavaScriptSourceViewerConfiguration;

public class SimpleTextEditor extends BaseWidget implements IDocumentListener {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final int RULER_WIDTH = 12;

	public static final String JAVASCRIPT_ANNOTATION = "org.eclipse.ui.workbench.texteditor.error"; //$NON-NLS-1$

	public static final String BREAKPOINT_ANNOTATION = "org.eclipse.debug.core.breakpoint";

	public static final String CURRENT_IP_ANNOTATION = "org.eclipse.debug.ui.currentIP";
    
	private ProjectionViewer editor;

	private ArrayList<Listener> listeners = new ArrayList<Listener>();

	// These labels are not used for the retargetable actions
	private final static String[] sourceLabels = new String[] {
		"common.Cut.name", 
		"common.Copy.name", 
		"common.Paste.name", 
		"general.delete.label",
		"TextEditorContextMenu.selectAll",
		"TextEditorContextMenu.findReplace",
		"SimpleTextEditor.format.source"};

	private final static int[] sourceActions = new int[] { 
		SourceViewer.CUT, 
		SourceViewer.COPY, 
		SourceViewer.PASTE, 
		SourceViewer.DELETE,
		SourceViewer.SELECT_ALL, 
		-1, 
		SourceViewer.FORMAT
		};

	private TextAction[] actions = new TextAction[sourceActions.length];

	private CompositeRuler ruler;

	private AnnotationRulerColumn annotationRulerColumn;

	private Menu menu;

	private ExtensionPointManager xpm = new ExtensionPointManager();

	private AnnotationModel annotationModel;

	private JavaScriptSourceViewerConfiguration svc;

	private Label title;

	/**
	 * These 3 variables define when to update the script in the config. If
	 * autoUpdate is true, we update for every change. Else, If
	 * updateOnFocusOut, we update when focus is lost. The variable modified
	 * tells if an update is needed.
	 */
	private boolean autoUpdate = false;

	private boolean updateOnFocusOut = true;

	private boolean modified = false;

	// -- Shared colors
	private ISharedTextColors sharedColors;

	// -- Overview ruler (right hand side)
	private OverviewRuler overViewRuler;

	// -- Decoration support (syntax coloring)
	private SourceViewerDecorationSupport svds;

	// -- Status line action to display current line:col
	private StatusLineContributionItem lineStatusAction;

	// -- Line number ruler
	protected LineNumberRulerColumn lineNumberRuler;

	private EditorActionBarContributor contributor;

	// -- True if we are editing javascript
	private boolean javascript;

	private MenuManager menuManager;

	// -- Partitioner for javascript segments (e.g. comments, code)
	private JavaScriptPartitioner jspart;

	// -- Painter to highlight current line
	private CursorLinePainter clp;

	private FunctionListWidget functionListWidget;
	
	// While we are setting the initial text, we should not flag or save modifications.
	private boolean settingText;

	private ConfigBinding cb;
	
	/**
	 * Invokes constructor(parent, flags, null)
	 * 
	 * @param parent
	 * @param flags
	 */
	public SimpleTextEditor(Composite parent, int flags) {
		this(parent, flags, null);
	}

	/**
	 * Invokes constructor (parent, style, config, true)
	 * 
	 * @param parent
	 * @param style
	 * @param config
	 */
	public SimpleTextEditor(Composite parent, int style, BaseConfiguration config) {
		this(parent, style, config, true);
	}

	/**
	 * @param parent
	 *            Parent container
	 * @param style
	 *            Style bits passed to Composite
	 * @param config
	 *            Config used for code completion (e.g. contextual objects)
	 * @param javascript
	 *            Specify true to activate javascript syntax checking and code
	 *            completion
	 */
	public SimpleTextEditor(Composite parent, int style, BaseConfiguration config, boolean javascript) {
		super(parent, style, config);

		this.javascript = javascript;

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		final SashForm sash = new SashForm(this, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));

		contributor = new EditorActionBarContributor() {
			@Override
			public void contributeToStatusLine(IStatusLineManager statusLineManager) {
				// in case we get called from editor contrib
				statusLineManager.remove(lineStatusAction.getId());
				statusLineManager.add(lineStatusAction);
			}
		};

		//
		// Annotation model contains all JavaScript errors/warnings
		//
		annotationModel = new AnnotationModel();

		ruler = new CompositeRuler();

		// -- Annotation ruler
		IAnnotationAccess annotationAccess = new DefaultMarkerAnnotationAccess();
		annotationRulerColumn = new AnnotationRulerColumn(RULER_WIDTH, annotationAccess);
		annotationRulerColumn.addAnnotationType(JAVASCRIPT_ANNOTATION);
		annotationRulerColumn.addAnnotationType(BREAKPOINT_ANNOTATION);
		annotationRulerColumn.addAnnotationType(CURRENT_IP_ANNOTATION);
		
		if ((style & SWT.SINGLE) == 0)
			ruler.addDecorator(0, annotationRulerColumn);

		// -- Overview ruler
		overViewRuler = new OverviewRuler(annotationAccess, RULER_WIDTH, getSharedColors());

		// -- Create source viewer and configure with JavaScript configuration
		if ((style & SWT.SINGLE) > 0)
			editor = new ProjectionViewer(sash, ruler, overViewRuler, true, style);
		else
			editor = new ProjectionViewer(sash, ruler, overViewRuler, true, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		svc = new JavaScriptSourceViewerConfiguration(config);
		if (isJavascript())
			editor.configure(svc);
		else
			editor.configure(new SourceViewerConfiguration());

		editor.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		editor.setDocument(new Document(), annotationModel);

		//
		// Drag/drop support
		//
		addDragDropSupport();

		//
		// Key listener to check JavaScript syntax, content assist and quick
		// close
		//
		editor.getDocument().addDocumentListener(this);
		editor.getTextWidget().addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				if ("\t".equals(e.text) && selectionSpansMultipleLines()) {
					e.doit = false;
				} else if (e.text != null && e.text.length() > 0 && 
						cb != null && !cb.mayModifyText()) {
					e.doit = false;
				}
			}
		});

		editor.getTextWidget().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 32 && (e.stateMask & SWT.MOD1) > 0) {
					// <command>+<space>
					if (isJavascript())
						editor.doOperation(SourceViewer.CONTENTASSIST_PROPOSALS);
				} else if (e.keyCode == '.' && (e.stateMask & SWT.MOD1) > 0) {
					// <command>+.
					if (isJavascript())
						editor.doOperation(SourceViewer.CONTENTASSIST_PROPOSALS);
				} else if (e.keyCode == '/' && (e.stateMask & SWT.MOD1) > 0) {
					// TODO: Fix the above test to be correct on all keyboards.
					// E.g. To get a control-slash on a Norwegian keyboard, the
					// user has to use CNTRL+7.
					toggleComment();

				} else if ((e.keyCode == SWT.TAB) && selectionSpansMultipleLines()) {
					if ((e.stateMask & SWT.SHIFT) != 0)
						outdentSelectedText();
					else
						indentSelectedText();

				} else if (e.keyCode == 'l' && (e.stateMask & SWT.MOD1) > 0) {
					int max = editor.getDocument().getNumberOfLines();
					InputDialog id = new InputDialog(getShell(), "Go to Line", "Enter line number: (1.." + max + "):", "",
							new IInputValidator() {
								public String isValid(String newText) {
									try {
										if ("".equals(newText))
											return null;
										int line = Integer.parseInt(newText);
										if (line >= 1 && line <= editor.getDocument().getNumberOfLines())
											return null;
										else
											return "";
									} catch (Exception e) {
										return e.toString();
									}
								}
							});
					if (id.open() == Window.OK) {
						try {
							gotoLine(Integer.parseInt(id.getValue()), false);
						} catch (Exception e1) {
							EclipseAppender.loginfo("parseInt: " + e1);
						}
					}
				}

				getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateStatusBar();
					}
				});
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		//
		// Source viewer decoration support is added to provide squiggles under
		// JavaScript
		// errors and warnings.
		//
		svds = new SourceViewerDecorationSupport(editor, overViewRuler, annotationAccess, getSharedColors());
		configureSourceViewerDecorationSupport(svds);
		svds.install(EditorsPlugin.getDefault().getPreferenceStore());

		//
		// Partitioning to decorate fragments of the script (e.g. syntax
		// coloring etc)
		//
		jspart = new JavaScriptPartitioner(new JavaScriptPartitionScanner());
		jspart.connect(editor.getDocument());
		editor.getDocument().setDocumentPartitioner(jspart);

		// -- Let sourceviewer be workbench selection provider
		addAsSelectionProvider(editor.getSelectionProvider());

		//
		// -- Create editor actions and related objects/actions
		//
		for (int i = 0; i < sourceActions.length; i++) {
			actions[i] = new TextAction(Messages.getString(sourceLabels[i]), sourceActions[i]);
		}

		// -- Update enabled state on selection change
		editor.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateStatusBar();
				for (TextAction a : actions)
					a.updateEnabled();
			}
		});

		editor.addViewportListener(new IViewportListener() {
			public void viewportChanged(int verticalOffset) {
				updateStatusBar();
			}
		});

		editor.getTextWidget().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				updateStatusBar();
			}
		});

		// -- Install global handlers
		editor.getTextWidget().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
				if (part instanceof EditorPart) {
					IActionBars bars = ((EditorPart) part).getEditorSite().getActionBars();
					if (bars != null) {
						installStatusAction(bars.getStatusLineManager());
					}
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (updateOnFocusOut && modified) {
					updateConfiguration();
				}
				IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
				if (part instanceof EditorPart) {
					IActionBars bars = ((EditorPart) part).getEditorSite().getActionBars();
					if (bars != null) {
						uninstallStatusAction(bars.getStatusLineManager());
					}
				}
			}
		});

		if ((style & SWT.SINGLE) == 0) {
			editor.getTextWidget().setFont(JFaceResources.getTextFont());
		}

		menuManager = new MenuManager();
		menu = menuManager.createContextMenu(editor.getTextWidget());
		editor.getTextWidget().setMenu(menu);

		// -- External editor
		Action extEditor = new Action() {

			@Override
			public String getText() {
				return Messages.getString("SimpleTextEditor.open.external");
			}

			@Override
			public void run() {
				String str = Utils.openEditorFor(editor.getTextWidget().getText(), true, false);
				if (str != null)
					editor.getTextWidget().setText(str);
			}

			@Override
			public String getActionDefinitionId() {
				return "com.ibm.tdi.action.openext.editor";
			}
		};
		menuManager.add(extEditor);

		if (javascript && (style&SWT.SINGLE) == 0) {

			functionListWidget = new FunctionListWidget(sash, this);

			// -- Test script as well
			TestScriptAction tsa = new TestScriptAction();
			tsa.setText(Messages.getString("JavaScriptView.7"));
			tsa.setImageDescriptor(Activator.getImageDescriptorRelative("Run"));
			tsa.setSource(this);
			menuManager.add(tsa);

			// -- Javascript help action
			Action jsHelp = new Action() {

				@Override
				public String getActionDefinitionId() {
					return "com.ibm.tdi.open.external.javascript";
				}

				@Override
				public String getText() {
					return Messages.getString("intro.section.learning.5.label");
				}

				@Override
				public void run() {
					new TDIHelpMenuAction().run(this);
				}

			};
			menuManager.add(jsHelp);
		}

		// -- separator
		menuManager.add(new Separator());

		// -- Add all actions to context menu
		for (IAction a : actions)
			menuManager.add(a);

		if (config != null) {
			init(config, config.getScript());
		}

		// -- TextEditHandler needs access to this for the undo/redo part
		editor.getTextWidget().setData("com.ibm.tdi.text.widget", this);
		IWorkbenchPartSite site = getSite();
		if (site != null) {
			Object o = site.getService(IFocusService.class);
			if (o instanceof IFocusService)
				((IFocusService) o).addFocusTracker(editor.getTextWidget(), "com.ibm.tdi.text.widget");
		}

		if ((style&SWT.SINGLE) == 0) {
			// -- Line number ruler
			if (EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER)) {
				showLineNumberRuler(true);
			}

			// -- Highlight line painter
			if (EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE)) {
				showLineNumberPainter(true);
			}
		}
		
		EditorsUI.getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {

				// -- toggle line number ruler
				if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER.equals(event.getProperty())) {
					showLineNumberRuler(Boolean.TRUE.equals(event.getNewValue()));

					// -- toggle current line painter
				} else if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE.equals(event.getProperty())) {
					showLineNumberPainter(Boolean.TRUE.equals(event.getNewValue()));

					// -- update color
				} else if (AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR.equals(event.getProperty())) {
					showLineNumberPainter(EditorsUI.getPreferenceStore().getBoolean(
							AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));

				}
			}
		});

		// Update function widget
		if (functionListWidget != null) {
			Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					if (PreferenceConstants.P_JS_FUNCTION_ENABLED.equals(event.getProperty())) {
						if (JavaScriptPreferencePage.isFunctionWidgetEnabled())
							sash.setWeights(new int[] { 80, 20 });
						else
							sash.setWeights(new int[] { 100, 0 });
					}
				}
			});
			if (JavaScriptPreferencePage.isFunctionWidgetEnabled())
				sash.setWeights(new int[] { 80, 20 });
			else
				sash.setWeights(new int[] { 100, 0 });
		}

		new ProjectionSupport(editor, annotationAccess, new ColorManager()).install();

		editor.doOperation(ProjectionViewer.TOGGLE);
	}

	/**
	 * Returns true if the current selection spans multiple lines
	 * 
	 * @return
	 */
	protected boolean selectionSpansMultipleLines() {
		String str = editor.getTextWidget().getSelectionText();
		return (str != null && str.indexOf("\n") != -1);
	}

	/**
	 * Indents selected text by inserting tabs at the beginning of each line.
	 */
	protected void indentSelectedText() {
		StringBuffer buf = new StringBuffer();
		buf.append("\t");

		Point p = editor.getTextWidget().getSelectionRange();

		String str = editor.getTextWidget().getSelectionText();
		char lastch = 0;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (lastch == SWT.LF)
				buf.append("\t");
			buf.append(ch);
			lastch = ch;
		}
		try {
			editor.getDocument().replace(p.x, p.y, buf.toString());
		} catch (BadLocationException e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
		p.y = buf.length();
		editor.getTextWidget().setSelection(p.x, p.x + p.y);
	}

	/**
	 * Auto formats indentation for the specified text range. 
	 * 
	 */
	protected void autoFormatScript() {
		Point sel = editor.getSelectedRange();
		boolean select = true;
		if (sel.y == 0) {
			sel.x = 0;
			sel.y = editor.getDocument().getLength();
			select = false;
		}

		try {
			StringBuffer buf = new StringBuffer();
			boolean iscomment = false;
			String indent = "";

			for(String s : editor.getDocument().get(sel.x, sel.y).split("\n", -1)) {
				if (buf.length() == 0) {
					for(int i = 0; i < s.length(); i++) {
						if(Character.isWhitespace(s.charAt(i)))
							indent += s.charAt(i);
						else
							break;
					}
				}

				String str = s.trim();
				
				//
				// Javascript multiline comments
				//
				if(str.startsWith("/*")) {
					buf.append(str);
					buf.append("\n");
					iscomment = !str.endsWith("*/");
					continue;
					
				} else if(str.startsWith("*") && iscomment) {
					buf.append(" ");
					buf.append(str);
					buf.append("\n");
					iscomment = !str.endsWith("*/");
					continue;
				}
				
				
				//
				// General indentation for code
				//
				if ((str.startsWith("}") || str.endsWith("}")) && indent.length() > 0)
					indent = indent.substring(1);
	
				buf.append(indent);

				if (str.endsWith("{"))
					indent = "\t" + indent;

				buf.append(str);
				buf.append("\n");
			}

			buf.deleteCharAt(buf.length() - 1); // Remove last linefeed

			editor.getDocument().replace(sel.x, sel.y, buf.toString());

			if (select) {
				editor.getTextWidget().setSelection(sel.x, sel.x + buf.length());
			}
		
		} catch (BadLocationException e) {
			SystemFunctions.doNothing();
		}
	}

	/**
	 * Reverse indents selected text by removing a tab at the beginning of each
	 * line. No indentation is done if any of the lines starts with a non
	 * whitespace character. Lines not starting with a tab is not modified.
	 */
	protected void outdentSelectedText() {
		StringBuffer buf = new StringBuffer();

		String str = editor.getTextWidget().getSelectionText();
		char lastch = SWT.LF;
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (lastch == SWT.LF && ch == SWT.TAB)
				SystemFunctions.doNothing();
			else if (lastch == SWT.LF && !Character.isWhitespace(ch))
				return;
			else
				buf.append(ch);
			lastch = ch;
		}

		Point p = editor.getTextWidget().getSelectionRange();
		try {
			// Modify document to bypass AutoIndentStrategy
			editor.getDocument().replace(p.x, p.y, buf.toString());
		} catch (BadLocationException e) {
			EclipseAppender.logerror(e.toString(), e, getShell());
		}
		p.y = buf.length();
		editor.getTextWidget().setSelection(p.x, p.x + p.y);
	}

	/**
	 * Add drag/drop support for the text editor
	 */
	private void addDragDropSupport() {
		DragSourceListener dsl = new DragSourceListener() {
			private Point selection;

			public void dragStart(DragSourceEvent event) {
				selection = editor.getTextWidget().getSelection();
				event.doit = selection.x != selection.y;
				editor.getTextWidget().setData("DRAG_START_DATA", selection);
			}

			public void dragSetData(DragSourceEvent event) {
				event.data = editor.getTextWidget().getText(selection.x, selection.y - 1);
			}

			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) {
					Point newSelection = editor.getTextWidget().getSelection();
					int length = selection.y - selection.x;
					int delta = 0;
					if (newSelection.x < selection.x)
						delta = length;
					editor.getTextWidget().replaceTextRange(selection.x + delta, length, "");
				}
				selection = null;
				editor.getTextWidget().setData("DRAG_START_DATA", null);
			}
		};
		DragSource ds = new DragSource(editor.getTextWidget(), DND.DROP_COPY | DND.DROP_MOVE);
		ds.addDragListener(dsl);
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });

		DropTarget target = new DropTarget(editor.getTextWidget(), DND.DROP_DEFAULT | DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK);
		target.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		target.addDropListener(new DropTargetAdapter() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if (!getSourceViewer().isEditable())
						event.detail = DND.DROP_NONE;
					else if (editor.getTextWidget().getData("DRAG_START_DATA") == null)
						event.detail = DND.DROP_COPY;
					else
						event.detail = DND.DROP_MOVE;
				}
			}

			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if (editor.getTextWidget().getData("DRAG_START_DATA") == null)
						event.detail = DND.DROP_COPY;
					else
						event.detail = DND.DROP_MOVE;
				}
			}

			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SCROLL | DND.FEEDBACK_SELECT;
			}

			public void drop(DropTargetEvent event) {
				if (event.detail != DND.DROP_NONE) {
					Point selection = (Point) editor.getTextWidget().getData("DRAG_START_DATA");
					int insertPos = editor.getTextWidget().getCaretOffset();
					if (event.detail == DND.DROP_MOVE && selection != null && selection.x <= insertPos && insertPos <= selection.y
							|| event.detail == DND.DROP_COPY && selection != null && selection.x < insertPos
							&& insertPos < selection.y) {
						editor.getTextWidget().setSelection(selection);
						event.detail = DND.DROP_COPY; // prevent source from
						// deleting selection
					} else {
						String string = (String) event.data;
						editor.getTextWidget().insert(string);
						if (selection != null)
							editor.getTextWidget().setSelectionRange(insertPos, string.length());
					}
				}
			}
		});
	}

	/**
	 * This method hooks the Undo/Redo handlers when the text widget gains
	 * focus. When the widget looses focus we revert back to the editor's
	 * undo/redo handlers. Never call this before the first change is made to
	 * the text widget since the UndoContext of the source viewer isn't
	 * available before then. A safe place to call this method is in the
	 * documentChanged method.
	 * 
	 */
	private UndoRedoActionGroup localUndoRedo = null;

	private void hookUndoRedo() {
		if (getEditor() == null)
			return;
		// -- if we already initialized return
		if (localUndoRedo != null)
			return;

		localUndoRedo = new UndoRedoActionGroup(getEditor().getSite(), ((IUndoManagerExtension) editor.getUndoManager())
				.getUndoContext(), true);

		editor.getTextWidget().addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				// -- revert back to editor's handlers
				BaseEditor editor = getEditor();
				if (editor == null || editor.getUndoRedo() == null || editor.getEditorSite() == null)
					return;
				editor.getUndoRedo().fillActionBars(editor.getEditorSite().getActionBars());
				editor.updateActionBars();
			}

			public void focusGained(FocusEvent e) {
				// -- use local handlers
				BaseEditor editor = getEditor();
				if (editor == null || editor.getEditorSite() == null)
					return;
				localUndoRedo.fillActionBars(editor.getEditorSite().getActionBars());
				editor.updateActionBars();
			}
		});

		localUndoRedo.fillActionBars(getEditor().getEditorSite().getActionBars());
		getEditor().updateActionBars();
	}

	protected void updateActions() {
		for (TextAction a : actions)
			a.updateEnabled();
	}

	private IWorkbenchPartSite getSite() {
		if (getEditor() != null) {
			return getEditor().getSite();
		}

		try {
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (part != null) {
				return part.getSite();
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		return null;
	}

	protected boolean isJavascript() {
		return javascript;
	}

	private void installStatusAction(IStatusLineManager statusLineManager) {
		if (lineStatusAction == null) {
			lineStatusAction = new StatusLineContributionItem("ocm.ibm.tdi.simple.text.id");
		}
		updateStatusBar();

		if (getEditor() != null) {
			getEditor().addContributor(contributor);
		}

		statusLineManager.add(lineStatusAction);
		statusLineManager.update(true);
	}

	private void uninstallStatusAction(IStatusLineManager statusLineManager) {
		if (lineStatusAction != null) {
			if (getEditor() != null) {
				getEditor().removeContributor(contributor);
			}
			statusLineManager.remove(lineStatusAction.getId());
			lineStatusAction = null;
			statusLineManager.update(true);
		}
	}

	public void registerActions(BaseEditor editor) {
	}

	/**
	 * This method configures the preferences needed by the source viewer
	 * decoration support class.
	 * 
	 * @param support
	 */
	@SuppressWarnings("unchecked")
	protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {

		List <AnnotationPreference> list =  EditorsPlugin.getDefault().getMarkerAnnotationPreferences().getAnnotationPreferences();
		for (AnnotationPreference info: list) {
			if (JAVASCRIPT_ANNOTATION.equals(info.getAnnotationType()) ||
					BREAKPOINT_ANNOTATION.equals(info.getAnnotationType()) ||
					CURRENT_IP_ANNOTATION.equals(info.getAnnotationType()))
				support.setAnnotationPreference(info);
		}
	}

	/**
	 * Shared color manager
	 * 
	 * @return The shared ColorManager
	 */
	private ISharedTextColors getSharedColors() {
		if (sharedColors == null)
			sharedColors = new ColorManager();

		return sharedColors;
	}

	/**
	 * Check JavaScript syntax of document and update the annotation model.
	 * 
	 */
	protected void checkJavaScriptSyntax() {
		if (!isJavascript())
			return;

		try {
			annotationModel.removeAllAnnotations();
			final ParserResult result = ScriptEngineOptions.get()
					.parseScript(StringUtils.toASCII(editor.getDocument().get()), true);
			for (int i = 0; i < result.getErrorCount(); i++) {
				ScriptError jserr = result.getError(i);
				addAnnotation(jserr.getMessage(), jserr.getErrorLine(), jserr.getErrorCol());
			}
			
			if (functionListWidget == null)
				return;

			// Note, the script is cached so there is not much overhead in
			// parsing again. Here
			// we just visit nodes to pull out relevant object/function
			// information.
			// If there are parsing errors we risk entering a hung state if we
			// call the
			// ibmjs parser again (not sure why). But, a script with errors is
			// an unreliable source
			// as to its contents so we keep the latest correct state info until
			// the script is correct.
			if (result.getErrorCount() > 0) {
				functionListWidget.setContentAssistProcessor(svc.getCAP());
				return;
			}

			new Job("") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						svc.getCAP().parseJavascriptSource(editor.getDocument().get(), result);
						getDisplay().syncExec(new Runnable() {
							public void run() {
								functionListWidget.setContentAssistProcessor(svc.getCAP());
							}
						});
					} catch (Throwable e) {
						SystemFunctions.doNothing();
					}
					return Status.OK_STATUS;
				}

			}.schedule();

		} catch (Exception e) {
			EclipseAppender.logerror(e.toString(), e);
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		editor.getTextWidget().setEditable(enabled);
		if (enabled)
			editor.getTextWidget().setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		else
			editor.getTextWidget().setBackground(getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
	}

	/**
	 * Adds a JavaScript annotation to the annotation model
	 * 
	 * @param text
	 * @param line
	 * @param column
	 */
	public void addAnnotation(String text, int line, int column) {
		// Seems like lexical errors have no line number.
		// Try to parse the text
		if (line == -1) {
			Pattern p = Pattern.compile("Lexical error at line ([0-9]+), column ([0-9]+)"); //$NON-NLS-1$
			Matcher m = p.matcher(text);
			if (!m.find())
				return;
			try {
				line = Integer.valueOf(m.group(1)).intValue();
				column = Integer.valueOf(m.group(2)).intValue();
			} catch (Exception e) {
				return;
			}
		}
		if (column > 0)
			column--; // The JSEngine is 1-based, Eclipse is 0-based
		int offset;
		try {
			offset = editor.getTextWidget().getOffsetAtLine(line - 1) + column;
		} catch (Exception e) {
			return;
		}
		Annotation a = new Annotation(JAVASCRIPT_ANNOTATION, false, text);
		IAnnotationModel model = editor.getAnnotationModel();
		if (model != null)
			model.addAnnotation(a, new Position(offset, 1));
	}

	public void removeAnnotations() {
		annotationModel.removeAllAnnotations();
	}

	@SuppressWarnings("unchecked")
	public boolean toggleBreakpointAnnotation(String text, int line, boolean enable) {
		try {
			if (enable) {
				annotationModel.addAnnotation(new Annotation(BREAKPOINT_ANNOTATION, false, text), 
											  new Position(editor.getTextWidget().getOffsetAtLine(line), 0));
			} else {
				for (Iterator<Annotation> i = annotationModel.getAnnotationIterator(); i.hasNext();) {
					Annotation a = i.next();
					if (text.equals(a.getText())) {
						annotationModel.removeAnnotation(a);
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void addToolbarItem(String text, SelectionListener listener) {
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(text);
		item.addSelectionListener(listener);
	}

	protected void fireCloseEvent() {
		Event event = new Event();
		event.widget = this;
		event.type = SWT.Close;
		for (Listener l : listeners)
			l.handleEvent(event);
	}

	public void setText(String text) {
		if (isUpdating())
			return;

		if (text == null)
			text = "";
		if (editor.getDocument() != null) {
			// -- Remove the initial change we do when initializing the text
			// widget.
			boolean mustReset = (localUndoRedo == null);
			settingText = true;
			editor.getDocument().set(text);
			settingText = false;

			// -- After we create the source viewer we do a setText which causes
			// the
			// -- first undoable change. This change is what we are
			// resetting/removing.

			if (mustReset)
				editor.getUndoManager().reset();
		}
	}

	public void addCloseListener(Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public IDocument getDocument() {
		return editor.getDocument();
	}

	public void removeCloseListener(Listener listener) {
		listeners.remove(listener);
	}

	public String getText() {
		return editor.getDocument().get();
	}

	@Override
	public boolean setFocus() {
		if (editor != null)
			return editor.getControl().setFocus();
		else
			return super.setFocus();
	}

	private class TextAction extends Action {
		private int operation;

		public TextAction(String text, int operation) {
			super(text);
			this.operation = operation;
		}

		@Override
		public String getActionDefinitionId() {
			if(operation == SourceViewer.FORMAT)
				return null;
			else if (operation == -1)
				return "org.eclipse.ui.edit.findReplace";
			else
				return super.getActionDefinitionId();
		}

		public void updateEnabled() {
			switch (operation) {
			case -1:
			case SourceViewer.FORMAT:
				super.setEnabled(true);
				break;
			default:
				super.setEnabled(editor.canDoOperation(operation));
			}
		}

		@Override
		public void run() {
			switch (operation) {
			case -1:
				findReplace();
				break;
			case SourceViewer.FORMAT:
				autoFormatScript();
				break;
			default:
				editor.doOperation(operation);
			}
		}

	}

	public ICommandService getCS() {
		return (ICommandService) getSite().getService(ICommandService.class);
	}

	/**
	 * Toggles the comment of the selected range of lines
	 */
	public void toggleComment() {
		try {
			if (getSourceViewer().getUndoManager() != null)
				getSourceViewer().getUndoManager().beginCompoundChange();

			Point p = getSourceViewer().getTextWidget().getSelectionRange();
			int firstLine = getSourceViewer().getDocument().getLineOfOffset(p.x);
			int lastLine = getSourceViewer().getDocument().getLineOfOffset(p.x + (p.y > 0 ? p.y - 1 : 0));
			boolean nocomments = false;

			// -- Check if any of the selected lines isn't commented
			for (int i = lastLine; i >= firstLine; i--) {
				IRegion line = getDocument().getLineInformation(i);
				String str = getDocument().get(line.getOffset(), line.getLength());
				if (!str.startsWith("//"))
					nocomments = true;
			}

			// -- If selection has lines with no comments then we comment the
			// whole block, otherwise we remove comments
			for (int i = lastLine; i >= firstLine; i--) {
				IRegion line = getDocument().getLineInformation(i);
				String str = getDocument().get(line.getOffset(), line.getLength());
				if (nocomments) {
					str = "//" + str;
					if (line.getOffset() > p.x)
						p.y += 2;
					else
						p.x += 2;
				} else {
					str = str.substring(2);
					if (line.getOffset() > p.x)
						p.y -= 2;
					else
						p.x -= 2;
				}
				getDocument().replace(line.getOffset(), line.getLength(), str);
			}

			// -- reselect what we just changed
			getSourceViewer().setSelectedRange(p.x, p.y);

		} catch (BadLocationException e) {
			e.printStackTrace();
		} finally {
			if (getSourceViewer().getUndoManager() != null)
				getSourceViewer().getUndoManager().endCompoundChange();
		}
	}

	public IHandlerService getHS() {
		return (IHandlerService) getSite().getService(IHandlerService.class);
	}

	public SourceViewer getSourceViewer() {
		return editor;
	}

	public void findReplace() {
		IFindReplaceTarget target = editor.getFindReplaceTarget();
		if (target == null)
			return;

		// Dummy bundle to make FRA happy
		ResourceBundle resbundle = new ResourceBundle() {
			public Enumeration<String> getKeys() {
				return Collections.emptyEnumeration();
			}

			protected Object handleGetObject(String key) {
				return null;
			}
		};

		// Make sure we don't send updates on focus out (new dialog causes
		// focusLost)
		boolean oldUOF = updateOnFocusOut;
		updateOnFocusOut = false;

		// Run the standard find/replace dialog
		FindReplaceAction fra = new FindReplaceAction(resbundle, null, getShell(), target);
		fra.run();

		// Restore the update on focus flag
		updateOnFocusOut = oldUOF;
	}

	/**
	 * Move to the specified line, 1-based. If mark is true, also mark the line
	 * as selected.
	 * 
	 * @param line
	 *            Line to move to, 1-based.
	 * @param mark
	 *            If true, mark line as selected.
	 */
	public void gotoLine(final int line, final boolean mark) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					int offset = editor.getTextWidget().getOffsetAtLine(line - 1);
					editor.revealRange(offset, 1);
					int end = offset;
					if (mark) {
						int length = editor.getDocument().getLineLength(line - 1);
						end += length;
						Annotation b = new InstructionPointerAnnotation(new StackFrame(line-1, offset, end),
								CURRENT_IP_ANNOTATION, "Current Line", null);
						annotationModel.addAnnotation(b, new Position(offset, length));
					}
					editor.getTextWidget().setSelection(offset, end);
					annotationRulerColumn.redraw(); // TODO: only if visible ?
				} catch (Exception e) {
					EclipseAppender.logerror(e.getMessage(), e);
				}
			}
		});
	}

	public void setAnnotationsVisible(boolean visible) {
		if (visible)
			ruler.addDecorator(0, annotationRulerColumn);
		else
			ruler.removeDecorator(annotationRulerColumn);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		xpm.setTargetConfigObjects(null);
		try {
			BaseConfiguration b = new ScriptConfigImpl();
			b.init();
			b.setScript(trim(getText()));
			xpm.setEditingConfigObject(b);
			xpm.invokeActionHandler(e.getSource());
		} catch (Exception err) {
			EclipseAppender.logerror(err.toString(), err, getShell());
		}
	}

	/**
	 * Update configuration from editor document
	 */
	public void updateConfiguration() {
		setUpdating(true);
		BaseConfiguration b = getEditingConfig();
		if (!isJavascript() && b instanceof AttributeMapItem)
			((AttributeMapItem) b).setSubstitution(getText());
		else if (b != null)
			b.setScript(trim(getText()));
		modified = false;
		setUpdating(false);
	}

	private String trim(String s) {
		if (s == null)
			return "";
		return s.trim();
	}

	/**
	 * Appends a string to the document and scrolls the document to show the
	 * last 20 lines.
	 * 
	 * @param string
	 */
	public void append(String string) {
		editor.getTextWidget().append(string);
		int count = editor.getTextWidget().getLineCount();
		if (count > 20)
			editor.getTextWidget().setTopIndex(count - 19);
	}

	/**
	 * Returns the configuration being used for editing
	 */
	public BaseConfiguration getConfig() {
		return svc.getConfig();
	}

	/**
	 * Sets the configuration being used for editing
	 * 
	 * @param config
	 */
	public void setConfig(BaseConfiguration config) {
		svc.setConfig(config);
	}

	/**
	 * Reinitialize the editor with the new configuration and script.
	 * 
	 * @param bc
	 * @param script
	 */
	public void init(BaseConfiguration bc, String script) {
		setEditingConfig(bc);
		setConfig(bc);
		editor.getDocument().removeDocumentListener(this);
		editor.getDocument().set(trim(script));
		if (editor.getUndoManager() != null)
			editor.getUndoManager().reset();
		if (title != null) {
			BaseConfiguration parent = (BaseConfiguration) Utils.getParentConfig(bc, ConnectorConfig.class);
			if (parent != null)
				title.setText(parent.getShortName() + "." + bc.getShortName()); //$NON-NLS-1$
		}
		editor.getDocument().addDocumentListener(this);

		checkJavaScriptSyntax();
	}

	private List<IDocumentListener> docListeners = Collections.synchronizedList(new ArrayList<IDocumentListener>());

	public void addDocumentListener(IDocumentListener listener) {
		if (!docListeners.contains(listener)) {
			synchronized (docListeners) {
				docListeners.add(listener);
			}
		}
	}

	public void removeDocumentListener(IDocumentListener listener) {
		synchronized (docListeners) {
			docListeners.remove(listener);
		}
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
		if (!settingText)
			modified = true;
	}

	public void documentChanged(DocumentEvent event) {
		if (!settingText) {
			if (modified && autoUpdate) {
				updateConfiguration();
			} else if (cb != null) {
				cb.setValue(getText());
			} else if (getEditingConfig() != null) {
					getEditingConfig().setModified(true);
			}
		}
		hookUndoRedo();
		updateActions();

		// TODO: This should really be part of a reconciler that runs in the
		// background.
		checkJavaScriptSyntax();

		if (!settingText)
			fireDocChanged(event);
	}

	private synchronized void fireDocChanged(DocumentEvent event) {
		IDocumentListener[] array = docListeners.toArray(new IDocumentListener[0]);
		for (IDocumentListener dl : array) {
			dl.documentChanged(event);
		}
	}

	/**
	 * This method sets the updateOnFocusOut variable. When enabled, this method
	 * will enable auto update but defer the actual update to the point where
	 * this text widget loses focus.
	 * 
	 * @param updateOnFocusOut
	 */
	public void setUpdateOnFocusOut(boolean updateOnFocusOut) {
		this.updateOnFocusOut = updateOnFocusOut;
	}

	public boolean isUpdateOnFocusOut() {
		return updateOnFocusOut;
	}

	/**
	 * Returns true if document text has been modified.
	 * 
	 * @return true if document text has been modified.
	 */
	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	/**
	 * If set to true, this class will update the configuration every time the
	 * document text changes.
	 * 
	 * @param update
	 */
	public void setAutoUpdate(boolean update) {
		this.autoUpdate = update;
	}

	public boolean isAutoUpdate() {
		return autoUpdate;
	}

	public CompositeRuler getRuler() {
		return ruler;
	}

	protected void updateStatusBar() {
		if (lineStatusAction == null)
			return;
		try {
			StyledText styledText = editor.getTextWidget();
			int caret = styledText.getCaretOffset();

			IDocument document = editor.getDocument();
			int line = document.getLineOfOffset(caret);
			int lineOffset = document.getLineOffset(line);
			int tabWidth = styledText.getTabs();
			int column = 0;
			for (int i = lineOffset; i < caret; i++)
				if ('\t' == document.getChar(i))
					column += tabWidth - (tabWidth == 0 ? 0 : column % tabWidth);
				else
					column++;

			lineStatusAction.setText((line + 1) + ":" + (column + 1));
		} catch (BadLocationException e) {
			SystemFunctions.doNothing();
		}
	}

	public String getSelected() {
		ISelection sel = editor.getSelection();
		if (sel instanceof TextSelection)
			return ((TextSelection) sel).getText();
		return null;
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	/**
	 * Toggle the display of line numbers
	 * 
	 * @param show
	 */
	protected void showLineNumberRuler(boolean show) {
		if (!show && lineNumberRuler != null) {
			ruler.removeDecorator(lineNumberRuler);
			lineNumberRuler = null;
		} else if (show && lineNumberRuler == null) {
			lineNumberRuler = new LineNumberRulerColumn();
			lineNumberRuler
					.setForeground(getDefaultColor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
			ruler.addDecorator(1, lineNumberRuler);
		}
	}

	/**
	 * Toggle the line number background painter or update the color.
	 * 
	 * @param show
	 */
	public void showLineNumberPainter(boolean show) {
		if (!show) {
			if (clp != null)
				editor.removePainter(clp);
			clp = null;
		} else {
			// -- change color only?
			if (clp == null)
				clp = new CursorLinePainter(editor);
			clp.setHighlightColor(getDefaultColor(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
			editor.addPainter(clp);
		}
	}

	/**
	 * Returns the color for a preference in the EditorsUI pref store.
	 * 
	 * @param pref
	 * @return
	 */
	private Color getDefaultColor(String pref) {
		RGB color = PreferenceConverter.getColor(EditorsUI.getPreferenceStore(), pref);
		return EditorsUI.getSharedTextColors().getColor(color);
	}

	/**
	 * Returns the source viewer configuration for this text editor.
	 * 
	 * @return JavaScriptSourceViewerConfiguration
	 */
	public JavaScriptSourceViewerConfiguration getSVC() {
		return svc;
	}

	public void setConfigBinding(ConfigBinding cb) {
		this.cb = cb;
	}
	
	private static class StackFrame implements IStackFrame {

		private int line;
		private int start;
		private int end;

		public StackFrame(int line, int offset, int end) {
			this.line = line;
			this.start = offset;
			this.end = end;
		}

		public boolean canTerminate() {
			return false;
		}

		public boolean isTerminated() {
			return false;
		}

		public void terminate() throws DebugException {		
		}

		public boolean canResume() {
			return true;
		}

		public boolean canSuspend() {
			return false;
		}

		public boolean isSuspended() {
			return false;
		}

		public void resume() throws DebugException {		
		}

		public void suspend() throws DebugException {	
		}

		public IDebugTarget getDebugTarget() {
			return null;
		}

		public ILaunch getLaunch() {
			return null;
		}

		public String getModelIdentifier() {
			return null;
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class arg0) {
			return null;
		}

		public boolean canStepInto() {
			return false;
		}

		public boolean canStepOver() {
			return false;
		}

		public boolean canStepReturn() {
			return false;
		}

		public boolean isStepping() {
			return false;
		}

		public void stepInto() throws DebugException {	
		}

		public void stepOver() throws DebugException {		
		}

		public void stepReturn() throws DebugException {
		}

		public int getCharEnd() throws DebugException {
			return end;
		}

		public int getCharStart() throws DebugException {
			return start;
		}

		public int getLineNumber() throws DebugException {
			return line;
		}

		public String getName() throws DebugException {
			return "Current Line";
		}

		public IRegisterGroup[] getRegisterGroups() throws DebugException {
			return null;
		}

		public IThread getThread() {
			return null;
		}

		public IVariable[] getVariables() throws DebugException {
			return null;
		}

		public boolean hasRegisterGroups() throws DebugException {
			return false;
		}

		public boolean hasVariables() throws DebugException {
			return false;
		}
	}
}
