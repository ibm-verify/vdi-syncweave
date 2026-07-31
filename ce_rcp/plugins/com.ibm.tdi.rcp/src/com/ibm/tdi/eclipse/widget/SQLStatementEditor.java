/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.operations.IWorkbenchOperationSupport;

import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LinkCriteriaConfig;
import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.text.ColorManager;
import com.ibm.tdi.eclipse.text.CompletionProposal;
import com.ibm.tdi.eclipse.text.SingleTokenScanner;
import com.ibm.tdi.eclipse.util.TextEditorContextMenu;
import com.ibm.tdi.eclipse.util.UndoRedoSupport;
import com.ibm.tdi.eclipse.util.WordPartDetector;

/**
 * This class provides a code completion text editor that provides output attributes, link criteria and a few
 * other common TDI expressions.
 * Completion list is displayed when the user hits ctrl-<space> or types a question mark.
 * The class is intended for writing SQL statements.
 * 
 */
public class SQLStatementEditor extends Composite {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
	
	private String paramName;
	private BaseConfiguration config;
	private SourceViewer sv;
	
	private boolean isUpdating;
	
	/**
	 * SQL Key Word list.
	 */
	private Set<String> sqlKeywords = null;

	/**
	 * SQL Key Word string array.
	 */
	private static final String[] DEFAULT_SQL_KEYWORDS = new String[] { "SELECT", "FROM", "WHERE" };

	/**
	 * Constructor
	 * @param form The FormWidget2 that displays this widget
	 * @param parent The paremt Composite for this widget
	 * @param cfg The Configuration where widget is editing a parameter
	 * @param param The name of the parameter in the configuration
	 */
	public SQLStatementEditor(FormWidget2 form, Composite parent, BaseConfiguration cfg, String param) {
		super(parent, SWT.NONE);
		sqlKeywords = new HashSet<String>();
		sqlKeywords.addAll(Arrays.asList(DEFAULT_SQL_KEYWORDS));
		this.paramName = param;
		this.config = cfg;

		setLayout(new FillLayout());
		
		sv = new SourceViewer(this, null, SWT.V_SCROLL|SWT.H_SCROLL|SWT.MULTI|SWT.BORDER);
		sv.configure(new SQLSourceViewerConfiguration(config));
		
		String text = config.getStringParameter(param);
		if(text == null)
			text = "";
		
		sv.setDocument(new Document(text));
		
		SQLPartitioner partitioner = new SQLPartitioner(new SQLPartitionScanner());
		partitioner.connect(sv.getDocument());
		sv.getDocument().setDocumentPartitioner(partitioner);
		
		sv.getDocument().addDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(DocumentEvent event) {
			}
			public void documentChanged(DocumentEvent event) {
				if (!isUpdating)
					config.setStringParameter(paramName, sv.getDocument().get());
			}
		});
		
		sv.getTextWidget().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 32 && (e.stateMask & SWT.CONTROL) > 0) {
					sv.doOperation(SourceViewer.CONTENTASSIST_PROPOSALS);
				}
			}
			public void keyReleased(KeyEvent e) {
			}
		});
		
		new TextEditorContextMenu(sv.getTextWidget(), sv.getFindReplaceTarget());
		if(form.getSite() != null) {
			IWorkbenchOperationSupport support = form.getSite().getWorkbenchWindow().getWorkbench().getOperationSupport();
			new UndoRedoSupport(sv.getTextWidget(), paramName, support.getOperationHistory(), support.getUndoContext());
		}

	}
	
	/**
	 * Constructor
	 * 
	 * @param form
	 *            The {@link FormWidget2} that displays this widget
	 * @param parent
	 *            The parent Composite for this widget
	 * @param cfg
	 *            The Configuration where widget is editing a parameter
	 * @param param
	 *            The name of the parameter in the configuration
	 * @param keywords
	 *            The custom string array to be add.
	 */
	public SQLStatementEditor(FormWidget2 form, Composite parent, BaseConfiguration cfg, String param, String[] keywords) {
		this(form, parent, cfg, param);
		sqlKeywords.addAll(Arrays.asList(keywords));
	}

	/**
	 * Replaces the text in the current text widget
	 * 
	 * @param text
	 */
	public void setText(String text) {
		String str = text == null ? "" : text;
		isUpdating = true;
		if(sv != null && sv.getTextWidget() != null && !sv.getTextWidget().isDisposed())
			sv.getTextWidget().setText(str);
		isUpdating = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Point computeSize (int wHint, int hHint, boolean changed) {
		checkWidget();
		Point p = sv.getTextWidget().computeSize(wHint, hHint, changed);
		if(p.y < 96)
			p.y = 96;
		return p;
	}
	
	/**
	 * This class provides a SourceViewerConfiguration for the SQLStatementEditor
	 */
	private class SQLSourceViewerConfiguration extends SourceViewerConfiguration {

		private SQLContentAssistProcessor proc;
		private ContentAssistant contentAssistant;

		/**
		 * Constructor
		 * @param config The configuration we are editing.
		 */
		public SQLSourceViewerConfiguration(BaseConfiguration config) {
			proc = new SQLContentAssistProcessor(config);

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
			return new SQLPresentationReconciler();
		}		
	}

	/**
	 * This class provides an IContentAssistProcessor for the SQLStatementEditor
	 *
	 */
	private class SQLContentAssistProcessor implements IContentAssistProcessor {
		
		private BaseConfiguration config;

		/**
		 * Constructor
		 * @param config The BaseConfiguration we are editing
		 */
		public SQLContentAssistProcessor(BaseConfiguration config) {
			super();
			this.config = config;
		}

		/**
		 * {@inheritDoc}
		 */
		public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
			
			ArrayList<ICompletionProposal> props = new ArrayList<ICompletionProposal>();

			// -- Connector
			ConnectorConfig cc = Utils.getParentConfig(config, ConnectorConfig.class);

			// -- Detect word
			WordPartDetector wordPart = new WordPartDetector(viewer, offset);

			// -- Output map 
			if(Utils.isOutputConnector(cc)) {
				List<String> list = cc.getAttributeMap(false).getAttributeNames();
				for(int i = 0; i < list.size(); i++) {
					String str = "{conn." + list.get(i) + "}";
					if (str.startsWith(wordPart.getString())) {
						props.add(createCompletionProposal(str, wordPart, "Script"));
					}
				}
			}
			
			// -- Link criteria
			if(Utils.hasLinkRequirements(cc)) {
				LinkCriteriaConfig lcc = cc.getLinkCriteria();
				List<String> crits = lcc.getCriteriaNames();
				for(int i = 0; i < crits.size(); i++) {
					String str = "{link[" + i + "].name}";
					if (str.startsWith(wordPart.getString())) {
						props.add(createCompletionProposal(str, wordPart, "Script"));
					}
					str = "{link[" + i + "].value}";
					if (str.startsWith(wordPart.getString())) {
						props.add(createCompletionProposal(str, wordPart, "Script"));
					}
				}
			}
			
			// -- Always available
			String[] fixed = new String[]{
					"{property.*}",
					"{javascript return new java.util.Date()}",
					"{javascript<<EOF\n return new java.util.Date();\nEOF\n}",
				};
			
			for(int i = 0; i < fixed.length; i++) {
				if (fixed[i].startsWith(wordPart.getString())) {
					props.add(createCompletionProposal(fixed[i], wordPart, "Script"));
				}
			}
			
			// -- Config params
			for(String key : config.getKeys(BaseConfiguration.ONE_LEVEL)) {
				String str = "{config." + key + "}";
				if (str.startsWith(wordPart.getString())) {
					props.add(createCompletionProposal(str, wordPart, "Script"));
				}
			}

			// -- Add custom list
			for (String keyword : sqlKeywords) {
				if (keyword.startsWith(wordPart.getString())) {
					props.add(createCompletionProposal(keyword, wordPart, null));
				}
			}

			// -- Add Input schema
			List<String> schemaList = cc.getSchema("Input").getItemNames();
			for (String schameItem : schemaList) {
				if (schameItem.startsWith(wordPart.getString())) {
					props.add(createCompletionProposal(schameItem, wordPart, "Schema"));
				}
			}

			// -- Add mapped attributes
			List<String> attributsMap = cc.getAttributeMap().getAttributeNames();
			for (String attributMap : attributsMap) {
				if (attributMap.startsWith(wordPart.getString())) {
					props.add(createCompletionProposal(attributMap, wordPart, null));
				}
			}

			return props.toArray(new ICompletionProposal[props.size()]);
		}

		/**
		 * Create new Completion Proposal by provided parameters.
		 * 
		 * @param string
		 *            the actual string to be inserted into the document
		 * 
		 * @param wordPart
		 *            the part of the word that will be replaced.
		 * @param iconName
		 *            the image to display for this proposal.
		 * 
		 * @return the Completion Proposal.
		 */
		private ICompletionProposal createCompletionProposal(String string, WordPartDetector wordPart, String iconName) {
			Image image = null;
			if (iconName != null) {
				image = Activator.getImage(iconName);
			}
			ICompletionProposal result = new CompletionProposal(string, wordPart.getOffset(), wordPart.getString().length(), string
					.length(), image, null, null, null);
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		public IContextInformation[] computeContextInformation(
				ITextViewer viewer, int offset) {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public char[] getCompletionProposalAutoActivationCharacters() {
			return new char[]{'?'};
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
	 * This class provides a PresentationReconciler for the SQLStatementEditor
	 */
	private static class SQLPresentationReconciler extends PresentationReconciler {

		/**
		 * Define script default color.
		 */
		public static final RGB TDI_PS = new RGB(0, 0, 255);

		/**
		 * Define default color.
		 */
		public static final RGB DEFAULT = new RGB(0, 0, 0);

		/**
		 * Define string default color.
		 */
		public static final RGB STRING = new RGB(0, 128, 0);

		/**
		 * Create new color manager.
		 */
		private ColorManager colorManager = new ColorManager();
		
		/**
		 * Constructor.
		 */
		public SQLPresentationReconciler() {
			super();
			
			DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager
					.getColor(TDI_PS))));
			setDamager(dr, SQLPartitionScanner.SQL_DOUBLE_PS);
			setRepairer(dr, SQLPartitionScanner.SQL_DOUBLE_PS);
			
			dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager.getColor(STRING))));
			setDamager(dr, SQLPartitionScanner.SQL_STRING);
			setRepairer(dr, SQLPartitionScanner.SQL_STRING);
			
			dr = new DefaultDamagerRepairer(new SingleTokenScanner(new TextAttribute(colorManager.getColor(DEFAULT))));
			setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
			setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		}
		
	}
	
	/**
	 * This class provides a Scanner for the SQLStatementEditor
	 */
	private static class SQLPartitionScanner extends RuleBasedPartitionScanner implements IWordDetector {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		public static final String SQL_DOUBLE_PS = "sql.double.ps";
		public static final String SQL_STRING = "sql.string";
		public static final String SQL_JSCRIPT = "sql.jscript";

		/**
		 * Constructor.
		 */
		public SQLPartitionScanner() {
			ArrayList<IRule> rules = new ArrayList<IRule>();

			IToken doublePS = new Token(SQL_DOUBLE_PS);
			IToken stringPS = new Token(SQL_STRING);
			IToken jscriptPS = new Token(SQL_JSCRIPT);
			
			// Add rule for TDI prepared statement expression
			rules.add(new MultiLineRule("{", "}", doublePS, '\\'));
			
			// Add rules for SQL strings (single quotes, no double quotes)
			rules.add(new SingleLineRule("'", "'", stringPS, '\\'));
			rules.add(new MultiLineRule("{javascript<<EOF", "EOF\n}", jscriptPS, '\\'));
			
			setPredicateRules(rules.toArray(new IPredicateRule[rules.size()]));
		}

		/**
		 *  {@inheritDoc}
		 */
		public boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}

		/**
		 *  {@inheritDoc}
		 */
		public boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c);
		}

	}
	
	/**
	 * This class provides a Partitioner for the SQLStatementEditor
	 */
	private static class SQLPartitioner extends FastPartitioner {
		@SuppressWarnings("unused")
		private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

		/**
		 * Constructor.
		 * @param scanner The Scanner to use.
		 */
		public SQLPartitioner(IPartitionTokenScanner scanner) {
			super(scanner, 
					new String[]{
						IDocument.DEFAULT_CONTENT_TYPE,
						SQLPartitionScanner.SQL_DOUBLE_PS,
						SQLPartitionScanner.SQL_STRING
						}
			);
		}

	}

}
