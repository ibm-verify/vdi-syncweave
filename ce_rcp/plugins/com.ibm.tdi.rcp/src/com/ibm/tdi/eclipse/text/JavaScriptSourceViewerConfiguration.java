/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.tdi.eclipse.Utils;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class JavaScriptSourceViewerConfiguration extends TextSourceViewerConfiguration {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private TDIContentAssistant contentAssistant;
	private JavaScriptContentAssistProcessor proc;
	private JavaScriptDocParser scriptContext;

	public JavaScriptSourceViewerConfiguration(BaseConfiguration config) {
		super();
		
		AssemblyLineConfig alc = Utils.getParentConfig(config, AssemblyLineConfig.class);
		if(alc != null) {
			try {
				scriptContext = JavaScriptDocParser.getDocParserFor(alc);
			} catch (Exception e) {
				EclipseAppender.logerror(e.toString(), e);
			}
		}
		
		proc = new JavaScriptContentAssistProcessor(config, this);

		contentAssistant = new TDIContentAssistant();
		contentAssistant.enableAutoActivation(true);
		contentAssistant.setAutoActivationDelay(500);
		contentAssistant.setStatusLineVisible(true);
		contentAssistant.setProposalPopupOrientation(ContentAssistant.PROPOSAL_REMOVE);
		contentAssistant.setContentAssistProcessor(proc, IDocument.DEFAULT_CONTENT_TYPE);		
		contentAssistant.setInformationControlCreator(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, "SyncWeave");
			}
		});
		
	}

	/**
	 * Returns the doc parser if the editing config is an assemblyline.
	 * 
	 * @return
	 */
	public JavaScriptDocParser getScriptContext() {
		return scriptContext;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		return contentAssistant;
	}

	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return EditorsUI.getPreferenceStore().getInt(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH);
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if(JavaScriptPartitionScanner.JAVASCRIPT_MULTILINE_COMMENT.equals(contentType)) {
			return new IAutoEditStrategy[] { new AutoIndentJSComment() };
		}
		return new IAutoEditStrategy[] { new AutoIndentStrategy() };
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		return new JavaScriptPresentationReconciler();
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[]{
				IDocument.DEFAULT_CONTENT_TYPE,
				JavaScriptPartitionScanner.JAVASCRIPT_COMMENT,
				JavaScriptPartitionScanner.JAVASCRIPT_MULTILINE_COMMENT,
				JavaScriptPartitionScanner.JAVASCRIPT_CONST_STRING,
				JavaScriptPartitionScanner.JAVASCRIPT_CONST_KEYWORD,
			};
	}

	public BaseConfiguration getConfig() {
		return proc.getConfig();
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if(sourceViewer instanceof ProjectionViewer) {
			MonoReconciler reconciler= new MonoReconciler(new JavaScriptFoldingReconciler((ProjectionViewer)sourceViewer, this), false);
			reconciler.setDelay(500);
			return reconciler;
		} else {
			return super.getReconciler(sourceViewer);
		}
	}

	public void setConfig(BaseConfiguration config) {
		proc.setConfig(config);
	}

	public JavaScriptContentAssistProcessor getCAP() {
		return proc;
	}
	
	public boolean isPopupProposalActive() {
		return contentAssistant.isPopupProposalActive();
	}
	
	private static class TDIContentAssistant extends ContentAssistant {
		public boolean isPopupProposalActive() {
			return super.isProposalPopupActive();
		}
	}

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer, String contentType) {
		return new DoubleClickStrategy();
	}
}
