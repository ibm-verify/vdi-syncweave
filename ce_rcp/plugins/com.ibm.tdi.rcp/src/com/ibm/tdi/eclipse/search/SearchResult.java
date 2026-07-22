/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.search;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;

import com.ibm.tdi.eclipse.Activator;
import com.ibm.tdi.eclipse.Messages;

public class SearchResult implements ISearchResult {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private SearchQuery query;
	
	private ArrayList<ISearchResultListener> searchListeners = new ArrayList<ISearchResultListener>();
	
	private HashMap<IFile, Match> matchMap = new HashMap<IFile, Match>(); 
	
	public SearchResult(SearchQuery searchQuery) {
		query = searchQuery;
	}

	public void addListener(ISearchResultListener l) {
		if(!searchListeners.contains(l))
			searchListeners.add(l);
	}

	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor("Neo");
	}

	public String getLabel() {
		return Messages.getString("perspective.name.0") + ": " + query.getLabel();
	}

	public ISearchQuery getQuery() {
		return query;
	}

	public String getTooltip() {
		return null;
	}

	public void removeListener(ISearchResultListener l) {
		searchListeners.remove(l);
	}

	public void add(IFile file, String location, String value) {
		Match m = matchMap.get(file);
		if(m == null) {
			m = new Match(file, location, value);
			matchMap.put(file, m);
		}
		m.addMatch(new Match(location, value));
		fireSearchResultChanged();
	}

	private void fireSearchResultChanged() {
		SRE sre = new SRE(this);
		for(ISearchResultListener sl : searchListeners)
			sl.searchResultChanged(sre);
	}

	public Match[] getMatches() {
		return matchMap.values().toArray(new Match[0]);
	}

	public void clear() {
		matchMap.clear();
		fireSearchResultChanged();
	}
	
	private static class SRE extends SearchResultEvent {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SRE(ISearchResult searchResult) {
			super(searchResult);
		}
	}

}
