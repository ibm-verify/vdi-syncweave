/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.search;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;

public class Match {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private IFile file;
	private String location;
	private String matchedValue;
	private ArrayList<Match> children = new ArrayList<Match>();
	private Match parent;
	
	public Match(IFile file, String location, String matchedValue) {
		super();
		this.file = file;
		this.location = location;
		this.matchedValue = matchedValue;
	}
	
	public Match(String location, String value) {
		this.location = location;
		this.matchedValue = value;
	}

	public IFile getFile() {
		if(getParent() != null)
			return getParent().getFile();
		else
			return file;
	}

	public String getLocation() {
		return location;
	}

	public String getMatchedValue() {
		return matchedValue;
	}

	public String toString() {
		if(location != null && matchedValue != null)
			return file.getProject().getName() + "/" + file.getName() + " (" + location + " = " + matchedValue + ")";
		else
			return file.getName();
	}

	public ArrayList<Match> getChildren() {
		return children;
	}

	public void addMatch(Match match) {
		// -- Add match but first check if location is already recorded
		String loc = match.getLocation();
		if(loc == null)
			return;
		
		for(Match m : children) {
			if(loc.equals(m.getLocation()))
				return;
		}
		
		children.add(match);
		match.setParent(this);
	}

	public boolean hasChildren() {
		// This object has all details of the first child so no need to say we have 
		// children unless there are more than one.
		return children.size() > 1;
	}

	public Match getParent() {
		return parent;
	}

	public void setParent(Match parent) {
		this.parent = parent;
	}

}
