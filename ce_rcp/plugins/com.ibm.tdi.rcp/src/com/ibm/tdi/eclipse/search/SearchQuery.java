/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.search;

import java.util.List;

import javax.naming.Binding;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

import com.ibm.di.config.eclipse.TDIConfigurationFile;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.function.SystemFunctions;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.natures.TDINature;

public class SearchQuery implements ISearchQuery {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String searchText;
	private SearchResult searchResult = new SearchResult(this);
	private int searchScope;
	private StructuredSelection selection;
	private boolean caseSensitive;
	private boolean regex;
	
	public SearchQuery(String text, int searchScope, boolean caseSensitive, boolean regex, IStructuredSelection selection) {
		this.searchText = text;
		this.searchScope = searchScope;
		this.selection = (StructuredSelection) selection;
		this.caseSensitive = caseSensitive;
		this.regex = regex;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#canRerun()
	 */
	public boolean canRerun() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
	 */
	public boolean canRunInBackground() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#getLabel()
	 */
	public String getLabel() {
		return searchText;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
	 */
	public ISearchResult getSearchResult() {
		return searchResult; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
		searchResult.clear();
		if(searchScope == ISearchPageContainer.WORKSPACE_SCOPE) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			for(IProject p : root.getProjects()) {
				if(!p.isOpen())
					continue;
				try {
					if(p.isNatureEnabled(TDINature.TDI_NATURE_ID)) {
						searchProject(p);
					}
				} catch (CoreException e) {
					return EclipseAppender.statusException(e);
				}
			}
			
		} else if (searchScope == ISearchPageContainer.SELECTION_SCOPE) {
			for(Object obj : ((IStructuredSelection)selection).toArray()) {
				if(obj instanceof IFolder) {
					searchProjectFolder((IFolder) obj);
				} else if(obj instanceof IFile) {
					searchProjectFile((IFile) obj);
				} else if(obj instanceof IProject) {
					searchProject((IProject) obj);
				} else if(obj instanceof TDIConfigurationFile) {
					TDIConfigurationFile tdi = (TDIConfigurationFile) obj;
					try {
						searchConfig(tdi.getFile(), tdi.getDefaultConfigObject());
					} catch (Exception e) {
						return EclipseAppender.statusException(e);
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	private void searchProject(IProject p) {
		try {
			for(IResource res : p.members()) {
				if(res instanceof IFile)
					searchProjectFile((IFile) res);
				else if(res instanceof IFolder)
					searchProjectFolder((IFolder) res);
			}
		} catch (CoreException e) {
			SystemFunctions.doNothing();
		}
	}

	private void searchProjectFolder(IFolder folder) {
		if(!folder.exists())
			return;
		try {
			for(IResource file : folder.members()) {
				if(file instanceof IFile)
					searchProjectFile((IFile)file);
				else if(file instanceof IFolder)
					searchProjectFolder((IFolder) file);
			}
		} catch (CoreException e) {
			SystemFunctions.doNothing();
		}
	}

	private void searchProjectFile(IFile file) {
		try {
			// -- only search TDI files
			if(TDIConfigurationFile.getFolderForExtension(file.getFileExtension()) == null)
				return;
			TDIConfigurationFile cfg = TDIConfigurationFile.loadFile(file);
			BaseConfiguration bc = cfg.getDefaultConfigObject();
			searchConfig(file, bc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void searchConfig(IFile file, BaseConfiguration bc) {
		int options = (regex ? BaseConfiguration.SEARCH_REGEX : BaseConfiguration.SEARCH_SUBSTRING);
		if(caseSensitive)
			options |= BaseConfiguration.SEARCH_EXACTCASE;
		List<Binding> list = bc.search(searchText, options, 0);
		for(Binding b : list) {
			searchResult.add(file, b.getName(), b.getObject().toString());
		}
	}

}
