/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.ui.webui.internal.handler.ts;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.di.api.DIException;
import com.ibm.di.api.Tombstone;
import com.ibm.di.api.remote.Session;
import com.ibm.di.entry.Entry;
import com.ibm.di.ui.webui.bind.Statistics;
import com.ibm.di.ui.webui.bind.Tombstones;
import com.ibm.di.ui.webui.internal.SessionUtils;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.2
 */
@Path(TombstoneHandler.URL)
public class TombstoneHandler {
	
	private int maxTombstones = 100;
	
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	public final static String URL = "ts";
	
	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllTombstones(@Context HttpServletRequest req, @PathParam("id") String id, @PathParam("al") String al) throws RemoteException, DIException, NotBoundException, DatatypeConfigurationException {
		Tombstone[] tslist = getTombstoneList(req, id, "*", -1);
		return Response.ok(fromTombstones(tslist)).build(); 
	}	

	@GET
	@Path("{id}/{al}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTombstones(@Context HttpServletRequest req, @PathParam("id") String id, @PathParam("al") String al) throws RemoteException, DIException, NotBoundException, DatatypeConfigurationException {
		Tombstone[] tslist = getTombstoneList(req, id, al, -1);
		return Response.ok(fromTombstones(tslist)).build(); 
	}	

	@GET
	@Path("{id}/{al}/{index}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTombstones(@Context HttpServletRequest req, @PathParam("id") String id, @PathParam("al") String al, @PathParam("index") int index) throws RemoteException, DIException, NotBoundException, DatatypeConfigurationException {
		Tombstone[] tslist = getTombstoneList(req, id, al, index);
		return Response.ok(fromTombstones(tslist)).build(); 
	}
	
	private Tombstone[] getTombstoneList(HttpServletRequest req, String id, String al, int index) throws RemoteException, DIException, NotBoundException, DatatypeConfigurationException {
		String alname = "*".equals(al) ? "" : "AssemblyLines/" + al;
		String configid = "*".equals(id) ? "" : id;
		Session sess = SessionUtils.getServerApiSession(req);
		if(sess.getTombstoneManager() == null)
			return null;
		
		Tombstone[] tslist = sess.getTombstoneManager().getAssemblyLineTombstones(alname, configid);
		if(tslist == null || tslist.length == 0)
			return tslist;
		
		if(index != -1) {
			if(index > -1 && index < tslist.length)
				return new Tombstone[] {tslist[index]};
			else if(index == -2 && tslist.length > 0)
				return new Tombstone[] {tslist[tslist.length-1]};
			else
				return tslist;
		}
		return tslist;
	}

	private Tombstones fromTombstones(Tombstone[] tslist) {
		Tombstones list = new Tombstones();
		if(tslist != null) {
			for(Tombstone ts : tslist) {
				com.ibm.di.ui.webui.bind.Tombstone t = new com.ibm.di.ui.webui.bind.Tombstone();
				t.setComponent(ts.getComponentName());
				t.setConfiguration(ts.getConfiguration());
				t.setErrorDescription(ts.getErrorDescription());
				t.setExitCode(ts.getExitCode());
				t.setGuuid(ts.getGUID());
				try {
					t.setStarted(toGregorian(ts.getStartTime()));
					t.setTerminated(toGregorian(ts.getTombstoneCreateTime()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				t.setUserMessage(ts.getUserMessage());
				t.setStatistics(convertStats(ts.getStatistics()));
				list.getTombstone().add(t);
				if(list.getTombstone().size() >= maxTombstones) {
					list.getTombstone().remove(0);
				}
			}
		}
		return list;
	}

	private Statistics convertStats(Entry ts) {
		if(ts == null)
			return null;
		Statistics s = new Statistics();
		s.setAdd(parseInt(ts.getString("add")));
		s.setMod(parseInt(ts.getString("mod")));
		s.setDel(parseInt(ts.getString("del")));
		s.setGet(parseInt(ts.getString("get")));
		s.setRequest(parseInt(ts.getString("request")));
		s.setCallReply(parseInt(ts.getString("callrepy")));
		s.setErr(parseInt(ts.getString("err")));
		s.setSkip(parseInt(ts.getString("skip")));
		s.setLookup(parseInt(ts.getString("lookup")));
		s.setIgnore(parseInt(ts.getString("ignore")));
		s.setReconnect(parseInt(ts.getString("reconnect")));
		return s;
	}

	private int parseInt(String string) {
		if(string != null)
			return Integer.parseInt(string);
		else
			return 0;
	}

	public static  XMLGregorianCalendar toGregorian(Date date) throws Exception {
		DatatypeFactory factory = DatatypeFactory.newInstance();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(date.getTime());
		return factory.newXMLGregorianCalendar(cal);
	}

}
