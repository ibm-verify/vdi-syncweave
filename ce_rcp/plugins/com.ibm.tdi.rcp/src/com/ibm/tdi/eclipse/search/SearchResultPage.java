/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.search;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.ISearchResultPage;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.progress.UIJob;

import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.log.EclipseAppender;

public class SearchResultPage implements ISearchResultPage, ISearchResultListener {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SearchResult search;
	private ISearchResultViewPart part;
	private IPageSite site;
	private TreeViewer table;
	private String id;

	public SearchResultPage() {
	}

	public String getID() {
		return id;
	}

	public String getLabel() {
		if(search != null)
			return search.getQuery().getLabel();
		else
			return Messages.getString("perspective.name.0");
	}

	public Object getUIState() {
		return null;
	}

	public void restoreState(IMemento memento) {
	}

	public void saveState(IMemento memento) {
	}

	public void setID(String id) {
		this.id = id;
	}

	public void setInput(ISearchResult search, Object uiState) {
		
		if(this.search != null)
			this.search.removeListener(this);
		
		this.search = (SearchResult) search;
		if(this.search != null)
			this.search.addListener(this);
		
		if(table != null)
			table.setInput(search);
	}

	public void setViewPart(ISearchResultViewPart part) {
		this.part = part;
	}

	public IPageSite getSite() {
		return site;
	}

	public void init(IPageSite site) throws PartInitException {
		this.site = site;
	}

	public void createControl(Composite parent) {
		table = new TreeViewer(parent,SWT.V_SCROLL|SWT.H_SCROLL|SWT.FULL_SELECTION);
		
		TreeViewerColumn tvc = new TreeViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("SearchPage.file"));
		tvc.getColumn().setWidth(300);

		tvc = new TreeViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("SearchPage.matched"));
		tvc.getColumn().setWidth(300);
		
		tvc = new TreeViewerColumn(table, SWT.LEFT);
		tvc.getColumn().setText(Messages.getString("SearchPage.location"));
		tvc.getColumn().setWidth(300);
		
		table.getTree().setHeaderVisible(true);
		table.setContentProvider(new SearchContentProvider());
		table.setLabelProvider(new SearchLabelProvider());
		table.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		table.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				Match match = (Match) ((IStructuredSelection)table.getSelection()).getFirstElement();
				IMarker marker;
				try {
					marker = match.getFile().createMarker("goto");
					String loc = match.getLocation().substring(0, match.getLocation().lastIndexOf("/"));
					marker.setAttribute(IMarker.LOCATION, loc);
					IEditorPart editor = IDE.openEditor(part.getViewSite().getPage(), match.getFile());
					if(editor != null)
						IDE.gotoMarker(editor, marker);
					marker.delete();
				} catch (CoreException e) {
					EclipseAppender.logerror(e.toString(), e, part.getSite().getShell());
				}
			}
		});
		
		table.expandAll();
	}

	public void dispose() {
	}

	public Control getControl() {
		return (table != null ? table.getTree() : null);
	}

	public void setActionBars(IActionBars actionBars) {
	}

	public void setFocus() {
		if(table != null)
			table.getTree().setFocus();
	}
	
	public void searchResultChanged(SearchResultEvent e) {
		if(table != null) {
			new UIJob("") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					table.refresh();
					table.expandAll();
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	private static class SearchContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof SearchResult)
				return ((SearchResult)inputElement).getMatches();
			else if (inputElement instanceof Match)
				return ((Match)inputElement).getChildren().toArray();
			else
				return null;
		}
		public void dispose() {}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		
		public Object[] getChildren(Object parentElement) {
			return getElements(parentElement);
		}
		public Object getParent(Object element) {
			return null;
		}
		public boolean hasChildren(Object element) {
			Match m = (Match) element;
			return m.hasChildren();
		}
	}

	private static class SearchLabelProvider implements ITableLabelProvider {

		public void addListener(ILabelProviderListener listener) {
		}
		public void dispose() {
		}
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}
		public void removeListener(ILabelProviderListener listener) {
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		public String getColumnText(Object element, int columnIndex) {
			Match m = (Match) element;
			switch(columnIndex) {
			case 0:
				if(m.getFile() != null)
					return m.getFile().getName() + " (" + m.getFile().getProject().getName() + ")";
				break;
			case 1:
				if(!m.hasChildren())
					return m.getMatchedValue();
				break;
			case 2:
				if(!m.hasChildren())
					return m.getLocation();
				break;
			default:
				if(!m.hasChildren())
					return m.toString();
				break;
			}
			return "";
		}
	}
}
