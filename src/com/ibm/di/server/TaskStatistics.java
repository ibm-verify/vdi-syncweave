/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
//
// TaskStatistics.java
//
//
// 2002/08/22 09:24:30 jens Exp $
//
package com.ibm.di.server;

import com.ibm.di.entry.*;
import java.lang.Exception;
import java.io.Serializable;

public class TaskStatistics implements Serializable {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 2098518046376889585L;

	public int add;

	public int mod;

	public int del;

	public int get; // Number of accepted getnext's

	public int getTries; // Number of times we have tried a getnext

	public int getclient; // Number of accepted getnextclient's

	public int getclientTries; // Number of times we have tried a getnextclient

	public int callreply;

	public int err;

	public int nochange;

	public int lookup;

	public int skip;

	public int ignore;

	public int reply;

	public int branchtrue;

	public int branchfalse;

	public int switches;

	public int loopstart;

	public int loopcycles;

	public long start;

	public long end;

	public int reconnect;

	public long reconnectTime;

	public Exception ex;

	private TaskStatistics parent;
	
	public int bailoutStep;

	public TaskStatistics() {
		add = 0;
		mod = 0;
		del = 0;
		get = 0;
		getTries = 0;
		callreply = 0;
		err = 0;
		end = 0;
		skip = 0;
		start = System.currentTimeMillis();
		nochange = 0;
		lookup = 0;
		ignore = 0;
		reply = 0;
		branchtrue = 0;
		branchfalse = 0;
		switches = 0;
		loopstart = 0;
		loopcycles = 0;
		reconnect = 0;
		reconnectTime = 0L;
		ex = null;
		bailoutStep = ALState.MS_TERMINATE;
	}

	private static final String PROPERTIES_FILE = "miserver";

	private static ResourceHash res = ResourceHash.getHash(PROPERTIES_FILE);

	public void setParentStats(TaskStatistics parent) {
		this.parent = parent;
	}

	public void start() {
		// initialized in constructor
	}

	public void end() {
		end = System.currentTimeMillis();
	}

	public void exception(Throwable e) {
		if (e instanceof Exception)
			ex = (Exception) e;
		else
			ex = new Exception(e);
	}

	public void add() {
		add++;
		if (parent != null)
			parent.add();
	}

	public void del() {
		del++;
		if (parent != null)
			parent.del();
	}

	public void mod() {
		mod++;
		if (parent != null)
			parent.mod();
	}

	public void get() {
		get++;
		if (parent != null)
			parent.get();
	}

	public void getTries() {
		getTries++;
		if (parent != null)
			parent.getTries();
	}

	public void getclient() {
		getclient++;
		if (parent != null)
			parent.getclient();
	}

	public void getclientTries() {
		getclientTries++;
		if (parent != null)
			parent.getclientTries();
	}

	public void callreply() {
		callreply++;
		if (parent != null)
			parent.callreply();
	}

	public void err() {
		err++;
		if (parent != null)
			parent.err();
	}

	public void lookup() {
		lookup++;
		if (parent != null)
			parent.lookup();
	}

	public void nochange() {
		nochange++;
		if (parent != null)
			parent.nochange();
	}

	public void reply() {
		reply++;
		if (parent != null)
			parent.reply();
	}

	public void skip() {
		skip++;
		if (parent != null)
			parent.skip();
	}

	public void ignore() {
		ignore++;
		if (parent != null)
			parent.ignore();
	}

	public void branchfalse() {
		branchfalse++;
		if (parent != null)
			parent.branchfalse();
	}

	public void branchtrue() {
		branchtrue++;
		if (parent != null)
			parent.branchtrue();
	}

	public void switches() {
		switches++;
		if (parent != null)
			parent.switches();
	}

	public void loopstart() {
		loopstart++;
		if (parent != null)
			parent.loopstart();
	}

	public void loopcycles() {
		loopcycles++;
		if (parent != null)
			parent.loopcycles();
	}

	public void reconnect() {
		reconnect++;
		if (parent != null)
			parent.reconnect();
	}

	public void reconnectTime(long t) {
		reconnectTime += t;
		if (parent != null)
			parent.reconnectTime(t);
	}

	/**
	 * Increases the number of entries deleted with the numAffected value
	 * 
	 * @param numAffected
	 */
	public void addMultipleDel(int numAffected) {
		del += numAffected;
		if (parent != null)
			parent.addMultipleDel(numAffected);
	}

	/**
	 * Increases the number of entries modified with the numAffected value
	 * 
	 * @param numAffected
	 */
	public void addMultipleMod(int numAffected) {
		mod += numAffected;
		if (parent != null)
			parent.addMultipleMod(numAffected);
	}

	public void addStats(TaskStatistics s) {
		Trace.entrymin(this, "addStats", s);
		add += s.add;
		mod += s.mod;
		del += s.del;
		get += s.get;
		getTries += s.getTries;
		callreply += s.callreply;
		reply += s.reply;
		// err += s.err;
		skip += s.skip;
		nochange += s.nochange;
		lookup += s.lookup;
		ignore += s.ignore;
		branchtrue += s.branchtrue;
		branchfalse += s.branchfalse;
		switches += s.switches;
		loopstart += s.loopstart;
		loopcycles += s.loopcycles;
		reconnect += s.reconnect;
		reconnectTime += s.reconnectTime;
		Trace.exitmin(this, "addStats");
	}

	public Entry getEntry() {
		Trace.entrymin(this, "getEntry");
		Entry e = new Entry();
		e.setAttribute("add", Integer.toString(add));
		e.setAttribute("mod", Integer.toString(mod));
		e.setAttribute("del", Integer.toString(del));
		e.setAttribute("get", Integer.toString(get));
		e.setAttribute("getTries", Integer.toString(getTries));
		e.setAttribute("getclient", Integer.toString(getclient));
		e.setAttribute("getclientTries", Integer.toString(getclientTries));
		e.setAttribute("callreply", Integer.toString(callreply));
		e.setAttribute("err", Integer.toString(err));
		e.setAttribute("skip", Integer.toString(skip));
		e.setAttribute("lookup", Integer.toString(lookup));
		e.setAttribute("ignore", Integer.toString(ignore));
		e.setAttribute("nochange", Integer.toString(nochange));
		e.setAttribute("branchtrue", Integer.toString(branchtrue));
		e.setAttribute("branchfalse", Integer.toString(branchfalse));
		e.setAttribute("switches", Integer.toString(switches));
		e.setAttribute("loopstart", Integer.toString(loopstart));
		e.setAttribute("loopcycles", Integer.toString(loopcycles));
		e.setAttribute("reconnect", Integer.toString(reconnect));
		e.setAttribute("reconnectTime", Long.toString(reconnectTime));

		if (ex != null)
			e.setAttribute("exception", ex.toString());
		Trace.exitmin(this, "getEntry", e);
		return e;
	}

	public void setStats(Entry e) throws Exception {
		Trace.entrymin(this, "setStats", e);
		add = getInt(e, "add");
		mod = getInt(e, "mod");
		del = getInt(e, "del");
		get = getInt(e, "get");
		getTries = getInt(e, "getTries");
		getclient = getInt(e, "getclient");
		getclientTries = getInt(e, "getclientTries");
		callreply = getInt(e, "callreply");
		err = getInt(e, "err");
		skip = getInt(e, "skip");
		lookup = getInt(e, "lookup");
		ignore = getInt(e, "ignore");
		nochange = getInt(e, "nochange");
		reply = getInt(e, "reply");
		branchtrue = getInt(e, "branchtrue");
		branchfalse = getInt(e, "branchfalse");
		switches = getInt(e, "switches");
		loopstart = getInt(e, "loopstart");
		loopcycles = getInt(e, "loopcycles");
		reconnect = getInt(e, "reconnect") + 1;
		Trace.exitmin(this, "setStats");
	}

	private int getInt(Entry e, String name) {
		try {
			return Integer.parseInt(e.getString(name));
		} catch (Exception ignore) {
		}
		return 0;
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(res.getString("get")).append(":").append(get).append(", ")
				.append(res.getString("getclient")).append(":").append(
						getclient).append(", ").append(res.getString("add"))
				.append(":").append(add).append(", ").append(
						res.getString("modify")).append(":").append(mod)
				.append(", ").append(res.getString("delete")).append(":")
				.append(del).append(", ").append(res.getString("error"))
				.append(":").append(err).append(", ").append(
						res.getString("skip")).append(":").append(skip).append(
						", ").append(res.getString("lookup")).append(":")
				.append(lookup).append(", ").append(res.getString("callreply"))
				.append(":").append(callreply).append(", ").append(
						res.getString("reply")).append(":").append(reply)
				.append(", ").append(res.getString("ignore")).append(":")
				.append(ignore).append(", ").append(res.getString("switches"))
				.append(":").append(switches).append(", ").append(
						res.getString("nochange")).append(":").append(nochange);
		return str.toString();
	}

	public String getMsg() {
		StringBuffer msg = new StringBuffer("");

		if (get > 0)
			msg.append(", ").append(res.getString("get")).append(":").append(
					get);
		if (getclient > 0)
			msg.append(", ").append(res.getString("getclient")).append(":")
					.append(getclient);
		if (lookup > 0)
			msg.append(", ").append(res.getString("lookup")).append(":")
					.append(lookup);
		if (add > 0)
			msg.append(", ").append(res.getString("add")).append(":").append(
					add);
		if (mod > 0)
			msg.append(", ").append(res.getString("modify")).append(":")
					.append(mod);
		if (del > 0)
			msg.append(", ").append(res.getString("delete")).append(":")
					.append(del);
		if (callreply > 0)
			msg.append(", ").append(res.getString("callreply")).append(":")
					.append(callreply);
		if (reply > 0)
			msg.append(", ").append(res.getString("reply")).append(":").append(
					reply);
		if (switches > 0)
			msg.append(", ").append(res.getString("switches")).append(":")
					.append(switches);
		if (skip > 0)
			msg.append(", ").append(res.getString("skip")).append(":").append(
					skip);
		if (ignore > 0)
			msg.append(", ").append(res.getString("ignore")).append(":")
					.append(ignore);
		if (nochange > 0)
			msg.append(", ").append(res.getString("nochange")).append(":")
					.append(nochange);
		if (reconnect > 0)
			msg.append(", ").append(res.getString("reconnect")).append(":")
					.append(reconnect);
		if (reconnectTime > 0)
			msg.append(", ").append(res.getString("reconnectTime")).append(":")
					.append(reconnectTime).append("ms");
		if (err > 0)
			msg.append(", ").append(res.getString("error")).append(":").append(
					err);

		if (msg.length() > 0)
			return msg.substring(2);
		else
			return res.getString("unused");
	}

	public String getBranchStats() {
		Trace.entrymax(this, "getBranchStats");
		Trace.exitmax(this, "getBranchStats");
		return res.getString("branchtrue") + ":" + branchtrue + ", "
				+ res.getString("branchfalse") + ":" + branchfalse;
	}

	public String getSwitchStats() {
		Trace.entrymax(this, "getSwitchStats");
		if (branchtrue + branchfalse == 0) {
			Trace.exitmax(this, "getSwitchStats");
			return res.getString("switches") + ":" + switches;
		} else {
			Trace.exitmax(this, "getSwitchStats");
			return res.getString("match") + ":" + branchtrue + ", "
					+ res.getString("nomatch") + ":" + branchfalse;
		}
	}

	public String getLoopStats() {
		Trace.entrymax(this, "getLoopStats");
		Trace.exitmax(this, "getLoopStats");
		return res.getString("loopstart") + ":" + loopstart + ", "
				+ res.getString("loopcycles") + ":" + loopcycles;
	}

	public String getScriptStats() {
		if (err > 0)
			return res.getString("TaskStatistics.script", new Object[]{add, err});
		else
			return res.getString("TaskStatistics.script.noerror", add);
	}

	public int numErrors() {
		return this.err;
	}

	public int numAdd() {
		return this.add;
	}

	public int numModify() {
		return this.mod;
	}

	public int numDelete() {
		return this.del;
	}

	public int numGet() {
		return this.get;
	}

	public int numGetTries() {
		return this.getTries;
	}

	public int numGetClient() {
		return this.getclient;
	}

	public int numGetClientTries() {
		return this.getclientTries;
	}

	public int numCallReply() {
		return this.callreply;
	}

	public int numLookup() {
		return this.lookup;
	}

	public int numNoChange() {
		return this.nochange;
	}

	public int numSkipped() {
		return this.skip;
	}

	public int numIgnored() {
		return this.ignore;
	}

	public Exception getError() {
		return this.ex;
	}

	public long getStart() {
		return this.start;
	}

	public long getEnd() {
		return this.end;
	}
	
	/**
	 * Returns the step where the AssemblyLine had a problem.
	 * @see ALState.mainStep
	 * @return The step where the AssemblyLine had a problem.
	 */
	public int getBailoutStep(){
		return bailoutStep;
	}
}
