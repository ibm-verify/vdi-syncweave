/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.HookConfig;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.editors.BaseEditor;
import com.ibm.tdi.eclipse.extensions.ExtensionPointManager;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;
import com.ibm.tdi.eclipse.text.ColorManager;
import com.ibm.tdi.eclipse.util.TextEditorContextMenu;

public class LogViewer extends Composite {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Color linkBackground;
	private Color linkForeground;
	private int linkStyle;

	private BaseConfiguration editingConfig;

	private ProjectionViewer viewer;

	private final static Pattern COMPONENT_PATTERN = Pattern.compile("\\[(.+?)\\]");
	private final static Pattern FAILOVER_PATTERN = Pattern.compile("\\[(.+?) \\[(.+?)\\]\\]");
	private final static Pattern INTERPRETER_PATTERN = Pattern.compile("Script interpreter error, (line=(\\d+), col=\\d+)");
	private final static Pattern HOOK_PATTERN = Pattern.compile("\\[.+?\\] CTGDIS181E .+?\\((\\w+)\\.(\\w+)\\)");
	private final static Pattern MAPPING_PATTERN = Pattern.compile("\\[.+?\\] CTGDIS183E .+?\\((\\w+)\\.(\\w+)\\.(\\w+)\\)");

	public static final String EXCEPTION_ANNOTATION = "org.eclipse.ui.workbench.texteditor.error"; //$NON-NLS-1$

	public LogViewer(Composite parent, int styles) {
		super(parent, SWT.READ_ONLY);
		// super(parent, null, styles|SWT.READ_ONLY);
		setLayout(new FillLayout());

		CompositeRuler ruler = new CompositeRuler();

		viewer = new ProjectionViewer(this, ruler, null, true, styles);

		new TextEditorContextMenu(viewer.getTextWidget(), viewer.getFindReplaceTarget());

		linkBackground = parent.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		linkForeground = parent.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		linkStyle = SWT.NORMAL;

		viewer.getTextWidget().addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent event) {
				event.styles = getLineStyles(event.lineText, event.lineOffset);
			}
		});

		viewer.configure(new SourceViewerConfiguration() {
			@Override
			public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
				return new IHyperlinkDetector[] { new URLHyperlinkDetector(), new LinkDetector() };
			}

			@Override
			public int getHyperlinkStateMask(ISourceViewer sourceViewer) {
				return SWT.CONTROL;
			}

			@Override
			public IReconciler getReconciler(ISourceViewer sourceViewer) {
				ReconcilerStrat rs = new ReconcilerStrat();
				MonoReconciler reconciler = new MonoReconciler(rs, false);
				return reconciler;
			}

		});

		viewer.getTextWidget().setFont(JFaceResources.getTextFont());
		viewer.setDocument(new Document(), new AnnotationModel(), -1, -1);

		ProjectionSupport projectionSupport = new ProjectionSupport(viewer, new DefaultMarkerAnnotationAccess(), new ColorManager());
		projectionSupport.addSummarizableAnnotationType(EXCEPTION_ANNOTATION);
		projectionSupport.install();

		viewer.doOperation(ProjectionViewer.TOGGLE);
	}

	public void append(String msg, int maxBufferLines) throws BadLocationException {
		// -- Remove first line if we are beyond the limit
		IDocument doc = viewer.getDocument();
		if (doc.getNumberOfLines() > maxBufferLines) {
			int offset = doc.get().indexOf("\n");
			doc.replace(0, offset + 1, "");
		}

		// -- Append text
		doc.replace(doc.getLength(), 0, msg.replace("\r", ""));
	}

	protected StyleRange[] getLineStyles(String lineText, int lineOffset) {
		ArrayList<StyleRange> styles = new ArrayList<StyleRange>();

		// -- Result of an expression
		markEvalExpr(lineOffset, lineText, styles);
		if (styles.size() > 0)
			return styles.toArray(new StyleRange[styles.size()]);

		markComponent(lineOffset, lineText, styles);
		if (styles.size() == 0)
			return null; // Only mark if we can identify the component

		StyleRange lineNumbers = markScriptError(null, lineOffset, lineText);
		if (lineNumbers != null)
			styles.add(lineNumbers);

		StyleRange sr = markHookError(null, lineOffset, lineText);
		if (sr != null)
			styles.add(sr);

		sr = markMappingError(null, lineOffset, lineText);
		if (sr != null)
			styles.add(sr);

		if (styles.size() == 0)
			return null;
		else
			return styles.toArray(new StyleRange[styles.size()]);
	}

	@Override
	public void setFont(Font font) {
		super.setFont(font);
		if (viewer != null)
			viewer.getControl().setFont(font);
	}

	private void markEvalExpr(int offset, String line, ArrayList<StyleRange> styles) {
		if (line.startsWith("--> "))
			styles.add(new StyleRange(offset, line.length(), linkForeground, linkBackground, linkStyle));
	}

	private void markComponent(int offset, String line, ArrayList<StyleRange> styles) {
		Matcher m = FAILOVER_PATTERN.matcher(line);
		while (m.find()) {
			//Failover Connector
			styles.add(new StyleRange(offset + m.start(2), m.end(2) - m.start(2), 
					linkForeground, linkBackground, linkStyle));
		}
		if (styles.size() > 0)
			return;
		
		m = COMPONENT_PATTERN.matcher(line);
		while (m.find()) {
			String comp = m.group(1);
			if (editingConfig instanceof AssemblyLineConfig) {
				BaseConfiguration bc = ((AssemblyLineConfig) editingConfig).getComponent(comp);
				if (bc != null)
					styles.add(new StyleRange(offset + m.start(1), m.end(1) - m.start(1), linkForeground, linkBackground,
									linkStyle));
			}
		}
	}

	private StyleRange markScriptError(StyledText textWidget, int offset, String msg) {
		// Mark script error messages on this line
		Matcher m = INTERPRETER_PATTERN.matcher(msg);
		if (m.find())
			return new StyleRange(offset + m.start(1), m.end(1) - m.start(1), linkForeground, linkBackground, linkStyle);
		return null;
	}

	private StyleRange markHookError(StyledText textWidget, int offset, String msg) {
		// [FileSystemConnector] CTGDIS181E Error while evaluating Hook
		// FileSystemConnector.after_getnext.
		// com.ibm.jscript.InterpretException: Script interpreter error, line=1,
		// col=6: Unknown member 'lxmsg' in Java class
		// 'com.ibm.di.server.AssemblyLine'
		Matcher m = HOOK_PATTERN.matcher(msg);
		if (m.find() && m.groupCount() == 2)
			return new StyleRange(offset + m.start(1), m.end(2) - m.start(1), linkForeground, linkBackground, linkStyle);
		return null;
	}

	private StyleRange markMappingError(StyledText textWidget, int offset, String msg) {
		// [FileSystemConnector] CTGDIS181E Error while evaluating Hook
		// FileSystemConnector.after_getnext.
		// com.ibm.jscript.InterpretException: Script interpreter error, line=1,
		// col=6: Unknown member 'lxmsg' in Java class
		// 'com.ibm.di.server.AssemblyLine'
		Matcher m = MAPPING_PATTERN.matcher(msg);
		if (m.find() && m.groupCount() == 3)
			return new StyleRange(offset + m.start(1), m.end(3) - m.start(1), linkForeground, linkBackground, linkStyle);
		return null;
	}

	public void setEditingConfig(BaseConfiguration editingConfig) {
		this.editingConfig = editingConfig;
	}

	public class LinkDetector implements IHyperlinkDetector {

		public LinkDetector() {
		}

		/*
		 * @see
		 * org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks
		 * (org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion,
		 * boolean)
		 */
		public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks) {
			if (region == null || textViewer == null)
				return null;

			IDocument document = textViewer.getDocument();

			int offset = region.getOffset();
			if (document == null)
				return null;

			IRegion lineInfo;
			String line;
			try {
				lineInfo = document.getLineInformationOfOffset(offset);
				line = document.get(lineInfo.getOffset(), lineInfo.getLength());
			} catch (BadLocationException ex) {
				return null;
			}

			ArrayList<TDIHyperlink> list = new ArrayList<TDIHyperlink>();
			//
			// CTGDIS077I Failed with error: Script interpreter error, line=2,
			// col=6: Unknown member 'lxmsg' in Java class
			// 'com.ibm.di.server.AssemblyLine'.
			//
			linkComponents(line, lineInfo.getOffset(), list);
			linkScriptErrors(line, lineInfo.getOffset(), list);
			linkHookErrors(line, lineInfo.getOffset(), list);
			linkMappingErrors(line, lineInfo.getOffset(), list);

			if (list.size() > 0)
				return list.toArray(new TDIHyperlink[0]);
			else
				return null;
		}

		private void linkScriptErrors(String line, int offset, ArrayList<TDIHyperlink> list) {
			// No components no script errors to point to
			if (list.size() == 0)
				return;

			// Script interpreter error, line=2, col=6
			Matcher m = INTERPRETER_PATTERN.matcher(line);
			if (!m.find())
				return;

			if (m.groupCount() == 2) {
				IRegion urlRegion = new Region(offset + m.start(1), m.start(1) - m.end(1));
				TDIHyperlink link = new TDIHyperlink(editingConfig, urlRegion, list.get(0).getURLString());
				link.setLine(m.group(2));
				list.add(0, link);
			}

		}

		private void linkHookErrors(String line, int offset, ArrayList<TDIHyperlink> list) {
			Matcher m = HOOK_PATTERN.matcher(line);
			if (m.find() && m.groupCount() == 2) {
				String comp = m.group(1);
				String hook = m.group(2);
				if (editingConfig instanceof AssemblyLineConfig) {
					BaseConfiguration bc = ((AssemblyLineConfig) editingConfig).getComponent(comp);
					if (bc instanceof ConnectorConfig) {
						ConnectorConfig cc = (ConnectorConfig) bc;
						HookConfig hc = cc.getHooks().getHook(hook, false);
						if (hc != null) {
							IRegion urlRegion = new Region(offset + m.start(1), m.end(2) - m.start(1));
							list.add(0, new TDIHyperlink(editingConfig, urlRegion, hc.getPath()));
						}
					}
				}
			}
		}

		private void linkMappingErrors(String line, int offset, ArrayList<TDIHyperlink> list) {
			Matcher m = MAPPING_PATTERN.matcher(line);
			if (m.find() && m.groupCount() == 3) {
				String comp = m.group(1);
				String mapName = m.group(2);
				String mapItem = m.group(3);
				if (editingConfig instanceof AssemblyLineConfig) {
					BaseConfiguration bc = ((AssemblyLineConfig) editingConfig).getComponent(comp);
					if (bc instanceof ConnectorConfig) {
						ConnectorConfig cc = (ConnectorConfig) bc;
						AttributeMapConfig map = cc.getAttributeMap(mapName);
						if (map == null)
							return;
						AttributeMapItem ami = map.getAttributeMapItem(mapItem);
						if (ami != null) {
							IRegion urlRegion = new Region(offset + m.start(1), m.end(2) - m.start(1));
							list.add(0, new TDIHyperlink(editingConfig, urlRegion, ami.getPath()));
						}
					}
				}
			}
		}

		private void linkComponents(String line, int offset, ArrayList<TDIHyperlink> list) {
			Matcher m = FAILOVER_PATTERN.matcher(line);
			if (m.find()) {
				IRegion urlRegion = new Region(offset + m.start(2), m.end(2) - m.start(2));					
				list.add(new TDIHyperlink(Utils.getProjectFor(editingConfig), urlRegion, m.group(2)));				
				return;
			}
			
			m = COMPONENT_PATTERN.matcher(line);
			if (m.find()) {
				String comp = m.group(1);
				if (editingConfig instanceof AssemblyLineConfig) {
					BaseConfiguration bc = ((AssemblyLineConfig) editingConfig).getComponent(comp);
					if (bc != null) {
						IRegion urlRegion = new Region(offset + m.start(1), m.end(1) - m.start(1));
						list.add(new TDIHyperlink(editingConfig, urlRegion, bc.getPath()));
					}
				}
			}
		}
	}

	private static class TDIHyperlink extends URLHyperlink {
		private BaseConfiguration editingConfig;
		private String line = null;
		private IProject project;

		public TDIHyperlink(BaseConfiguration editingConfig, IRegion region, String urlString) {
			super(region, urlString);
			this.editingConfig = editingConfig;
		}
		public TDIHyperlink(IProject project, IRegion region, String urlString) {
			super(region, urlString);
			this.project = project;
		}


		@Override
		public void open() {
			if (project != null) {
				// Failover Connector
				IFile path = project.getFile(TDINature.RESOURCES_FOLDER + "/" + getURLString() + "." + ExtensionPointManager.XP_CONNECTOR);
				try {
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), path);
				} catch (Exception e) {
					EclipseAppender.logerror("LogViewer.open", e);
				}
			}
			if (editingConfig == null)
				return;
			
			if (editingConfig.getMetamergeConfig() instanceof TDIConfigurationFile) {
				IFile path = ((TDIConfigurationFile) editingConfig.getMetamergeConfig()).getFile();
				try {
					IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), path);
					if (editor instanceof BaseEditor) {
						IMarker m = path.createMarker(IMarker.MARKER);
						m.setAttribute(IMarker.LOCATION, getURLString());
						if (getLine() != null)
							m.setAttribute(IMarker.LINE_NUMBER, getLine());
						((BaseEditor) editor).gotoMarker(m);
						m.delete();
					}
				} catch (Exception e) {
					e.printStackTrace();
					SystemFunctions.doNothing();
				}
			}
		}

		public String getLine() {
			return line;
		}

		public void setLine(String line) {
			this.line = line;
		}

	}

	public Control getControl() {
		return viewer.getControl();
	}

	public StyledText getTextWidget() {
		return viewer.getTextWidget();
	}

	public void setDocument(IDocument doc) {
		viewer.getDocument().set(doc.get());
	}

	public IDocument getDocument() {
		return viewer.getDocument();
	}

	public class ReconcilerStrat implements IReconcilingStrategy, IReconcilingStrategyExtension {

		private IDocument document;
		private int offset;
		private IRegion pos;

		public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
			computePositions();
		}

		public void reconcile(IRegion partition) {
			computePositions();
		}

		public void setDocument(IDocument document) {
			this.document = document;
		}

		public void initialReconcile() {
			computePositions();
		}

		public void setProgressMonitor(IProgressMonitor monitor) {
		}

		private void computePositions() {
			offset = 0;
			final ArrayList<Position> positions = new ArrayList<Position>();
			final ArrayList<Boolean> collapsed = new ArrayList<Boolean>();
			String str;
			try {
				int start = -1;
				while ((str = readLine()) != null) {
					String[] arr = str.split(" ");
					
					// <date> ERROR <errmsg>
					// java.lang.Exception: <msg>
					// 		at .....
					if (arr.length > 1 && "ERROR".equals(arr[1])) {
						// -- collect stack trace
						start = pos.getOffset();
						int nextline = offset;
						while ((str = readLine()) != null) {
							if(isDateString(str)) {
								offset = pos.getOffset();
								break;
							}
						}
						// -- don't collapse one liners
						if(nextline != offset) {
							positions.add(new Position(start, offset-start));
							collapsed.add(Boolean.TRUE);
						}
						
					} else if (str.indexOf("CTGDIS003I") != -1) {
						// -- Collect dumpEntry output
						start = pos.getOffset();
						while ((str = readLine()) != null) {
							if (str.indexOf("CTGDIS004I") != -1) {
								break;
							}
						}
						positions.add(new Position(start, offset-start));
						collapsed.add(Boolean.FALSE);
						
					}
				}

				getDisplay().syncExec(new Runnable() {
					public void run() {
						updateFoldingStructure(positions, collapsed);
					}
				});

			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		private boolean isDateString(String str) {
			if(str.length() > 2)
				return (Character.isDigit(str.charAt(0)) && Character.isDigit(str.charAt(1)));
			else
				return false;
		}

		private String readLine() throws BadLocationException {
			if(offset >= document.getLength())
				return null;

			pos = document.getLineInformationOfOffset(offset);
			if (pos.getOffset() + pos.getLength() < offset) {
				//Handle lines with double delimiter (CR+LF)
				if(offset+1 >= document.getLength())
					return null;
				pos = document.getLineInformationOfOffset(offset+1);
			}
			
			offset = pos.getOffset() + pos.getLength() + 1;
			return document.get(pos.getOffset(), pos.getLength());
		}
	}

	private Annotation[] oldAnnotations;
	
	public void updateFoldingStructure(ArrayList<Position> positions, ArrayList<Boolean> collapsed) {
		Annotation[] annotations = new Annotation[positions.size()];

		// -- This will hold the new annotations along with their corresponding positions
		HashMap<ProjectionAnnotation, Position> newAnnotations = new HashMap<ProjectionAnnotation, Position>();

		for (int i = 0; i < positions.size(); i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation(collapsed.get(i));
			newAnnotations.put(annotation, positions.get(i));
			annotations[i] = annotation;
		}

		if (viewer != null && viewer.getProjectionAnnotationModel() != null)
			viewer.getProjectionAnnotationModel().modifyAnnotations(oldAnnotations, newAnnotations, null);

		oldAnnotations = annotations;

	}
}
