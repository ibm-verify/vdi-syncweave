/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.List;

import java.io.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.swt.IFocusService;
import org.eclipse.ui.texteditor.FindReplaceAction;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.handlers.TextControlHandler;
import com.ibm.tdi.eclipse.handlers.TextEditorHandler;
import com.ibm.tdi.eclipse.providers.TDIPropertiesContentProvider;
import com.ibm.tdi.eclipse.providers.WorkEntryAttributesProvider;
import com.ibm.tdi.eclipse.text.ColorManager;
import com.ibm.tdi.eclipse.text.CompletionProposal;
import com.ibm.tdi.eclipse.text.SingleTokenScanner;
import com.ibm.tdi.eclipse.util.TextEditorContextMenu;

/**
 * This class provides a code completion text editor that provides properties. Completion
 * list is displayed when the user types a '{' or hits ctrl-<space> The class is
 * intended for writing TDIExpression statements.
 *
 */
public class TDIExpressionEditor extends Composite implements IDocumentListener{

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected BaseConfiguration config;
	private SourceViewer sv;
	private boolean multiLine;

	/**
	 * Constructor
	 *
	 * @param parent
	 *            The parent Composite for this widget
	 * @param cfg
	 *            The Configuration where widget is editing a parameter
	 * @param param
	 *            The name of the parameter in the configuration
	 */
	public TDIExpressionEditor(Composite parent, BaseConfiguration cfg, boolean multiLine) {
		super(parent, SWT.NONE);
		this.config = cfg;
		this.multiLine = multiLine;

		setLayout(new FillLayout());

		// -- always use SWT.MULTI so we don't pass up the Enter key (dismissing a dlg box etc) during
		// -- content assist
		sv = new SourceViewer(this, null, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER);

		// -- provide custom SV config
		sv.configure(new TDIExpressionSourceViewerConfiguration(config));

		// -- set the default document (we'll get the text later)
		sv.setDocument(new Document(""));

		// -- This is for syntax highlighting (if sv.setDocument() is called again, this code must also be called again
		TDIExpressionPartitioner partitioner = new TDIExpressionPartitioner(new TDIExpressionPartitionScanner());
		partitioner.connect(sv.getDocument());
		sv.getDocument().setDocumentPartitioner(partitioner);

		// -- Hook the content assist command
		sv.getTextWidget().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 32 && (e.stateMask & SWT.MOD1) > 0) {
					sv.doOperation(SourceViewer.CONTENTASSIST_PROPOSALS);
					return;
				}
				// Listen for Ctrl+F in case there is no associated editor
				if (e.keyCode == 'f' && e.stateMask == SWT.CONTROL) {
					findReplace();
					return;
				}
			}
			public void keyReleased(KeyEvent e) {
			}
		});

		new TextEditorContextMenu(sv.getTextWidget(), sv.getFindReplaceTarget());

		// -- TextEditHandler needs access to this TDIExpressionEditor
		sv.getTextWidget().setData(TextEditorHandler.TDI_TEXT_WIDGET, this);
		IWorkbenchPartSite site = getSite();
		if (site != null) {
			Object o = site.getService(IFocusService.class);
			if (o instanceof IFocusService)
				((IFocusService) o).addFocusTracker(sv.getTextWidget(), "com.ibm.tdi.text.widget");
		}
	}


	public void init(BaseConfiguration config, String text) {
		sv.getDocument().removeDocumentListener(this);
		this.config = config;
		setText(text != null ? text : "");
		sv.getDocument().addDocumentListener(this);

	}


	public void documentChanged(DocumentEvent event) {
		if(config instanceof AttributeMapItem) {
			((AttributeMapItem)config).setSubstitution(getText());
		}
	}

	/**
	 * Returns the current text in the editor
	 *
	 * @return
	 */
	public String getText() {
		String str = sv.getDocument().get();
		if(!multiLine) {
			while(str.endsWith("\n") || str.endsWith("\r"))
				str = str.substring(0, str.length()-1);
		}
		return str;
	}

	/**
	 * Sets the text of the editor
	 */
	public void setText(String text) {
		sv.getDocument().set(text);
		sv.getUndoManager().reset();
	}

	/**
	 * {@inheritDoc}
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point p = sv.getTextWidget().computeSize(wHint, hHint, changed);
		if (p.y < 96)
			p.y = 96;
		return p;
	}

	/**
	 * This class provides a SourceViewerConfiguration for the
	 * TDIExpressionEditor
	 */
	private class TDIExpressionSourceViewerConfiguration extends SourceViewerConfiguration {

		private TDIExpressionContentAssistProcessor proc;
		private ContentAssistant contentAssistant;

		/**
		 * Constructor
		 *
		 * @param config
		 *            The configuration we are editing.
		 */
		public TDIExpressionSourceViewerConfiguration(BaseConfiguration config) {
			proc = new TDIExpressionContentAssistProcessor(config);

			contentAssistant = new ContentAssistant();
			contentAssistant.setStatusLineVisible(true);
			contentAssistant.setProposalPopupOrientation(ContentAssistant.PROPOSAL_REMOVE);
			contentAssistant.setContentAssistProcessor(proc, IDocument.DEFAULT_CONTENT_TYPE);
			contentAssistant.setInformationControlCreator(new IInformationControlCreator() {
				public IInformationControl createInformationControl(Shell parent) {
					return new DefaultInformationControl(parent, "SyncWeave");
				}
			});
			contentAssistant.setAutoActivationDelay(500);
			contentAssistant.enableAutoActivation(true);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
			return contentAssistant;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getTabWidth(ISourceViewer sourceViewer) {
			return 4;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
			return new TDIExpressionPresentationReconciler();
		}
	}

	/**
	 * This class provides an IContentAssistProcessor for the
	 * TDIExpressionEditor
	 *
	 */
	private class TDIExpressionContentAssistProcessor implements IContentAssistProcessor {

		//private BaseConfiguration config;

		/**
		 * Constructor
		 *
		 * @param config
		 *            The BaseConfiguration we are editing
		 */
		public TDIExpressionContentAssistProcessor(BaseConfiguration config) {
			super();
			//this.config = config;
		}

		/**
		 * {@inheritDoc}
		 */
		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int docoffset) {

			ArrayList<ICompletionProposal> props = new ArrayList<ICompletionProposal>();

			// -- Connector
			ConnectorConfig cc = Utils.getParentConfig(config, ConnectorConfig.class);

			// -- Get current string for proposal
			StringBuffer prefix = new StringBuffer();
			int offset = docoffset - 1;
			while (offset >= 0) {
				try {
					String str = viewer.getDocument().get(offset, 1);
					// -- break on eol and end of expression
					if(str.equals("}") || str.equals("\n"))
						break;
					prefix.insert(0, str);
					if (str.equals("{")) {
						break;
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				offset--;
			}

			if(offset < 0) {
				offset = 0;
			}

			// -- match against this prefix (if it's empty or just the "{" everything matches)
			String match = prefix.toString().trim();
			if(match.startsWith("{"))
				match = match.substring(1);
			if(match.equals(""))
				match = null;

			// -- Output map
			if (prefixMatches(match, "conn") && Utils.isOutputConnector(cc)) {
				List<String> list = cc.getAttributeMap(false).getAttributeNames();
				for (int i = 0; i < list.size(); i++) {
					String str = "conn." + list.get(i);
					if (prefixMatches(match, str))
						props.add(newCompletionProposal("{" + str + "}", offset, prefix.length(), str.length()+2, str));
				}
			}

			// -- Work attributes
			AssemblyLineConfig alc = Utils.getParentConfig(cc, AssemblyLineConfig.class);
			if(alc != null) {
				WorkEntryAttributesProvider wp = new WorkEntryAttributesProvider(cc.getShortName());
				wp.inputChanged(null, null, alc);
				for(Object obj : wp.getChildren(alc)) {
					String str = "work." + obj;
					if (prefixMatches(match, str))
						props.add(newCompletionProposal("{" + str + "}", offset, prefix.length(), str.length()+2, str));
				}
			}

			// -- Link criteria
			if (prefixMatches(match, "link") && Utils.hasLinkRequirements(cc)) {
				LinkCriteriaConfig lcc = cc.getLinkCriteria();
				List<String> crits = lcc.getCriteriaNames();
				for (int i = 0; i < crits.size(); i++) {
					String str = "link[" + i + "].name";
					if (prefixMatches(match, str)) {
						props.add(newCompletionProposal("{" + str + "}", offset, prefix.length(), str.length()+2, str));
						str = "link[" + i + "].value";
						props.add(newCompletionProposal("{" + str + "}", offset, prefix.length(), str.length()+2, str));
					}
				}
			}

			// -- Fixed expressions
			String[] fixed = new String[] { "javascript return new java.util.Date()",
			"javascript<<EOF\n return new java.util.Date();\nEOF\n" };

			for (int i = 0; i < fixed.length; i++) {
				if (prefixMatches(match, fixed[i]))
					props.add(newCompletionProposal("{" + fixed[i] + "}", offset, prefix.length(), fixed[i].length()+2, fixed[i]));
			}

			// -- Config params
			if(config != null) {
				for (String key : config.getKeys(BaseConfiguration.ONE_LEVEL)) {
					String str = "config." + key;
					if (prefixMatches(match, str))
						props.add(newCompletionProposal("{" + str + "}", offset, prefix.length(), str.length()+2, str));
				}

				// -- add all property stores
				try {
					TDIPropertiesContentProvider tcp = new TDIPropertiesContentProvider();
					tcp.inputChanged(null, null, config.getMetamergeConfig());
					for (Object element : tcp.getChildren(config.getMetamergeConfig())) {
						String val = null;
						if (element instanceof IFile) {
							IFile file = (IFile) element;
							val = file.getName();
							if (val.endsWith(".tdiproperties"))
								val = val.substring(0, val.lastIndexOf("."));
						} else if (element instanceof File) {
							File file = (File) element;
							val = file.getName();
							if ("solution.properties".equals(val))
								val = PropertyManager.STDCOLL_SOLUTION;
							else if ("global.properties".equals(val))
								val = PropertyManager.STDCOLL_GLOBAL;
						}

						if(val == null)
							continue;

						String ext = "property:" + val + ".";

						// -- if prefix extends beyond a store show the properties
						// from that store
						if (match != null && prefixMatches(ext, match)) {
							for(Object bc : tcp.getChildren(element)) {
								String prop = ((BaseConfiguration)bc).getShortName();
								String str = "property:" + val + "." + prop;
								String display = prop; // store == null ? prop : store + "."
								if (prefixMatches(match, str))
									props.add(newCompletionProposal("{" + str + "}", offset, prefix.length(), str.length() + 2, display));
							}
						} else if (match != null && prefixMatches("property.", match)) {
							for(Object bc : tcp.getChildren(element)) {
								String prop = ((BaseConfiguration)bc).getShortName();
								String str = "property." + prop;
								String display = prop; // store == null ? prop : store + "."
								if (prefixMatches(match, str))
									props.add(newCompletionProposal("{" + str + "}", offset, prefix.length(), str.length() + 2, display));
							}
						} else {
							String str = "property:" + val;
							if (prefixMatches(match, str))
								props.add(newCompletionProposal("{" + str, offset, prefix.length(), str.length() + 1, str));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return props.toArray(new ICompletionProposal[props.size()]);
		}

		private ICompletionProposal newCompletionProposal(String str, int offset, int preflen, int replen, String display) {
			return new CompletionProposal(str, offset, preflen, replen, null, display, null, null);
		}

		private boolean prefixMatches(String match, String str) {
			if (match == null || match.length() == 0)
				return true;
			else
				return str.startsWith(match);
		}

		/**
		 * {@inheritDoc}
		 */
		public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public char[] getCompletionProposalAutoActivationCharacters() {
			return new char[] { '{', '.' };
		}

		/**
		 * {@inheritDoc}
		 */
		public char[] getContextInformationAutoActivationCharacters() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public IContextInformationValidator getContextInformationValidator() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getErrorMessage() {
			return null;
		}

	}

	/**
	 * This class provides a PresentationReconciler for the TDIExpressionEditor
	 */
	private static class TDIExpressionPresentationReconciler extends PresentationReconciler {
		public static final RGB TDI_PS = new RGB(0, 0, 128);
		public static final RGB DEFAULT = new RGB(0, 0, 0);
		public static final RGB STRING = new RGB(0, 128, 0);

		private ColorManager colorManager = new ColorManager();

		/**
		 * Constructor.
		 */
		public TDIExpressionPresentationReconciler() {
			super();

			DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager
					.getColor(TDI_PS))));
			setDamager(dr, TDIExpressionPartitionScanner.TDIExpression_DOUBLE_PS);
			setRepairer(dr, TDIExpressionPartitionScanner.TDIExpression_DOUBLE_PS);

			dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager.getColor(STRING))));
			setDamager(dr, TDIExpressionPartitionScanner.TDIExpression_STRING);
			setRepairer(dr, TDIExpressionPartitionScanner.TDIExpression_STRING);

			dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager.getColor(DEFAULT))));
			setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
			setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		}

	}

	/**
	 * This class provides a Scanner for the TDIExpressionEditor
	 */
	private static class TDIExpressionPartitionScanner extends RuleBasedPartitionScanner implements IWordDetector {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		public static final String TDIExpression_DOUBLE_PS = "TDIExpression.double.ps";
		public static final String TDIExpression_STRING = "TDIExpression.string";
		public static final String TDIExpression_JSCRIPT = "TDIExpression.jscript";

		/**
		 * Constructor.
		 */
		public TDIExpressionPartitionScanner() {
			ArrayList<IRule> rules = new ArrayList<IRule>();

			IToken doublePS = new Token(TDIExpression_DOUBLE_PS);
			IToken stringPS = new Token(TDIExpression_STRING);
			IToken jscriptPS = new Token(TDIExpression_JSCRIPT);

			// Add rule for TDI prepared statement expression
			rules.add(new MultiLineRule("?{", "}", doublePS, '\\'));

			// Add rules for TDIExpression strings (single quotes, no double
			// quotes)
			rules.add(new SingleLineRule("'", "'", stringPS, '\\'));
			rules.add(new MultiLineRule("{javascript<<EOF", "EOF\n}", jscriptPS, '\\'));

			setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c);
		}

	}

	/**
	 * This class provides a Partitioner for the TDIExpressionEditor
	 */
	private static class TDIExpressionPartitioner extends FastPartitioner {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * Constructor.
		 *
		 * @param scanner
		 *            The Scanner to use.
		 */
		public TDIExpressionPartitioner(IPartitionTokenScanner scanner) {
			super(scanner, new String[] { IDocument.DEFAULT_CONTENT_TYPE, TDIExpressionPartitionScanner.TDIExpression_DOUBLE_PS,
					TDIExpressionPartitionScanner.TDIExpression_STRING });
		}

	}

	public void documentAboutToBeChanged(DocumentEvent arg0) {
		// TODO Auto-generated method stub
	}

	private IWorkbenchPartSite getSite() {
		try {
			IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
			if (part instanceof EditorPart) {
				return part.getSite();
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		return null;
	}


	/**
	 * TextEditHandler needs this method
	 */
	public SourceViewer getSourceViewer() {
		return sv;
	}
	
	/**
	 * Runs the standard Find/Replace action
	 */
	public void findReplace() {
		if (isDisposed() || sv == null || sv.getTextWidget() == null || sv.getFindReplaceTarget() == null)
			return;
		new FindReplaceAction(TextControlHandler.DUMMY_BUNDLE, null, sv.getTextWidget().getShell(), sv.getFindReplaceTarget()).run();			
	}

}
