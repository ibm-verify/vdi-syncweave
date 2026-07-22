/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.text;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Display;

import com.ibm.di.function.SystemFunctions;
import com.ibm.jscript.ASTTree.ASTFunction;
import com.ibm.jscript.ASTTree.ASTNode;
import com.ibm.jscript.ASTTree.ASTProgram;
import com.ibm.jscript.ASTTree.DefaultNodeVisitor;

/**
 * Called by the source viewer after document changes. The folding structure is computed
 * based on the current JavaScript node tree and the document is also scanned for multi
 * line comments. Annotations are updated in the UI thread after computing the changes.
 * 
 */
public class JavaScriptFoldingReconciler implements IReconcilingStrategy, IReconcilingStrategyExtension {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private Annotation[] oldAnnotations;
	private IDocument document;
	private ProjectionViewer viewer;

	private JavaScriptSourceViewerConfiguration svc;
	
	public JavaScriptFoldingReconciler(ProjectionViewer viewer, JavaScriptSourceViewerConfiguration svc) {
		this.viewer = viewer;
		this.svc = svc;
	}

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

	protected int computeLastLine(ASTNode parent) {
		if(parent == null)
			return 0;
		
		int line = parent.getEndLine();
		for(int i = 0; i < parent.getSlotCount(); i++) {
			ASTNode node = parent.readSlotAt(i);
			line = Math.max(computeLastLine(node), line);
		}
		return line;
	}
	
	private void computePositions() {
		
		ASTProgram main = svc.getCAP().getJavaScriptMainNode();
		if(main == null)
			return;
		
		final ArrayList<Position> positions = new ArrayList<Position>();
		final ArrayList<Boolean> collapsed = new ArrayList<Boolean>();
		
		main.visitAllNodes(new DefaultNodeVisitor() {

			@Override
			public Object visitFunction(ASTFunction func, Object param) {
				int start = func.getBeginLine();
				if (func.getName() == null)
					start--; // Correction for anonymous functions
				int end = computeLastLine(func);
				try {
					start = document.getLineOffset(start > 0 ? (start-1) : start);
					end = document.getLineOffset(end + 1);
					positions.add(new Position(start, end - start));
					collapsed.add(Boolean.FALSE);
				} catch (Exception e) {
					SystemFunctions.doNothing();
				}
				return super.visitFunction(func, param);
			}
			
		});
		
		// Find multi-line comments
		int pos = 0;
		try {
			int start = -1;
			int end = -1;
			
			while(pos < document.getLength()) {
				int line = document.getLineOfOffset(pos);
				int length = document.getLineLength(line);
				if(length > 0) {
					String str = document.get(pos, length);
					if(str.trim().startsWith("/**")) {
						start = pos;
					} else if(start != -1 && str.trim().endsWith("*/")) {
						end = pos + length;
						positions.add(new Position(start, end - start));
						collapsed.add(Boolean.FALSE);
					}
					pos += length;
				} else {
					pos = document.getLength() + 1;
				}
			}
		} catch (Exception e) {
			SystemFunctions.doNothing();
		}
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				updateFoldingStructure(positions, collapsed);
			}
		});
	}

	public void updateFoldingStructure(ArrayList<Position> positions, ArrayList<Boolean> collapsed) {
		Annotation[] annotations = new Annotation[positions.size()];

		// -- This will hold the new annotations along with their corresponding
		// positions
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
