/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.aleidoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Trace;
import com.sap.mw.idoc.IDoc;
import com.sap.mw.idoc.jco.JCoIDoc;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.IRepository;

/**
 * Class SapIDocServerImpl is for receiving IDoc and function requests. This is
 * the actual JCo IDoc Server implementation for the Connector and manages the
 * receiving of IDoc and Remote Function Module requests, and also the TID
 * Management for the received requests if applicable.
 */
public class SapIDocServerImpl extends JCoIDoc.Server {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	protected final com.ibm.icu.text.SimpleDateFormat timestampFormat = new com.ibm.icu.text.SimpleDateFormat(
			"yyyyMMddHHmmss");

	private String currentTID = "";

	private SapALEIDocConnector conn = null;

	/**
	 * Constructor of JCoIDoc.Server that takes RFC Server connection parameters
	 * directly.
	 */
	public SapIDocServerImpl(SapALEIDocConnector conn, String gwhost,
			String gwserv, String progid, IRepository jcoRepository,
			IDoc.Repository idocRepository) {
		super(gwhost, gwserv, progid, jcoRepository, idocRepository);
		this.conn = conn;
	}

	/**
	 * Constructor of JCoIDoc.Server that takes RFC Server connection parameters
	 * contained within a properties object.
	 */
	public SapIDocServerImpl(SapALEIDocConnector conn,
			java.util.Properties properties, IRepository jcoRepository,
			IDoc.Repository idocRepository) {
		super(properties, jcoRepository, idocRepository);
		this.conn = conn;
	}

	/**
	 * Overridden method of JCoIDoc.Server. Forms the initial call made on the
	 * Server for the TID management cycle. Expected to follow are calls on
	 * handleRequest() and then onConfirmTID() to complete the TID management
	 * life cycle.
	 * 
	 * @param tid -
	 *            The SAP client transaction identifier.
	 */
	protected boolean onCheckTID(String tid) {
		Trace.entrymin(this, "onCheckTID");
		Object[] args = new Object[] { tid };
		if (conn.getTidIDocMap().containsKey(tid)) {
			if (conn.mDebugEnabled)
				conn.debug(
						LogMessageHelper.getMessage(
								LogMessageHelper.SAP_ALEIDOC_0024, args));
			// TID already received and is being processed.
			Trace.exitmin(this, "onCheckTID");
			return false;
		} else {
			if (conn.mDebugEnabled) {
				conn.debug(
						LogMessageHelper.getMessage(
								LogMessageHelper.SAP_ALEIDOC_0025, args));
			}
			currentTID = tid;
			Trace.exitmin(this, "onCheckTID");
			return true;
		}
	}

	/**
	 * Overridden method of JCoIDoc.Server. Forms the final call made on the
	 * Server for the TID management cycle. Expected that calls on onCheckTID()
	 * and handleRequest() have already been processed as part of the TID
	 * management life cycle.
	 * 
	 * @param tid -
	 *            The SAP client transaction identifier.
	 */
	protected void onConfirmTID(String tid) {
		Trace.entrymin(this, "onConfirmTID");
		if (conn.getTidIDocMap().containsKey(tid)) {
			TIDManager tidMgr = (TIDManager) conn.getTidIDocMap().get(tid);
			if (null != tidMgr) {
				// Pole time in milli-secs
				long pollTime = 20000;
				if (conn.isConnParamValueValid(
						SapALEIDocConnector.CONFIG_PARAM_QUEUE_POLE_TIME, true))
					pollTime = new Long(
							conn
									.getParam(SapALEIDocConnector.CONFIG_PARAM_QUEUE_POLE_TIME))
							.longValue() * 1000;
				if (pollTime < 1) {
					pollTime = 20000;
				}
				while (tidMgr.getTidStatus() != TIDManager.TID_STAT_CONFIRM) {
					if (conn.mDebugEnabled) {
						Object[] args = new Object[] { tid,
								tidMgr.getTidStatusAsString() };
						if (conn.mDebugEnabled) {
							conn
									.debug(
											LogMessageHelper
													.getMessage(
															LogMessageHelper.SAP_ALEIDOC_0026,
															args));
						}
					}
					try {
						Thread.sleep(pollTime);
					} catch (InterruptedException e) {
						conn
								.logmsg(
										LogMessageHelper
												.getMessage(LogMessageHelper.SAP_ALEIDOC_0011));
					}
				}
				Object[] args = new Object[] { tid };
				conn.logmsg(
						LogMessageHelper.getMessage(
								LogMessageHelper.SAP_ALEIDOC_0027, args));
				conn.getTidIDocMap().remove(tid);
			} else {
				Object[] args = new Object[] { tid };
				conn.logmsg(
						LogMessageHelper.getMessage(
								LogMessageHelper.SAP_ALEIDOC_0007, args));
				conn.getTidIDocMap().remove(tid);
			}
		}
		// clear the current TID now for next IDoc Client request.
		currentTID = "";
		Trace.exitmin(this, "onConfirmTID");
	}

	/**
	 * Overridden method of JCoIDoc.Server. Forms the intermediate call made on
	 * the Server for the TID management cycle. Expected that calls on
	 * onCheckTID() has already been processed as part of the TID management
	 * life cycle. Therefore the list of IDocs received by this invokation
	 * belong to the last received TID stored when onCheckTID() was called.
	 * 
	 * @param documentList -
	 *            The list of IDocs for a particular SAP client transaction.
	 */
	protected void handleRequest(IDoc.DocumentList documentList) {
		Trace.entrymin(this, "handleRequest(IDocList)");
		if (conn.mDebugEnabled) {
			Object[] args = new Object[] { currentTID,
					Integer.valueOf(documentList.getNumDocuments()).toString() };
			if (conn.mDebugEnabled) {
				conn.debug(
						LogMessageHelper.getMessage(
								LogMessageHelper.SAP_ALEIDOC_0027, args));
			}
		}
		// Store the IDocs against their TID as TDI Entries in the Connector's
		// TID management
		// hash table. Key is the TID, Value is the constructed TIDManager
		// object.
		IDoc.DocumentIterator iterator = documentList.iterator();
		IDoc.Document jcoIDoc = null;
		ArrayList iDocEntries = new ArrayList();
		while (iterator.hasNext()) {
			jcoIDoc = iterator.nextDocument();
			Entry idocEntry = writeIDocToTDIEntry(currentTID, jcoIDoc);
			if (null == idocEntry) {
				conn.logmsg(
						LogMessageHelper
								.getMessage(LogMessageHelper.SAP_ALEIDOC_0029));
			} else {
				iDocEntries.add(idocEntry);
			}
		}
		if (currentTID.length() == 0) {
			conn.logmsg(
					LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0030));
		} else if (conn.getTidIDocMap().containsKey(currentTID)) {
			Object[] args = new Object[] { currentTID };
			conn.logmsg(
					LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0031, args));
		} else if (iDocEntries.size() < 1) {
			Object[] args = new Object[] { currentTID };
			conn.logmsg(
					LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0032, args));
		} else {
			TIDManager tidMgr = new TIDManager(currentTID, iDocEntries);
			conn.getTidIDocMap().put(currentTID, tidMgr);
			Object[] args = new Object[] { currentTID,
					Integer.valueOf(iDocEntries.size()).toString() };
			conn.logmsg(
					LogMessageHelper.getMessage(
							LogMessageHelper.SAP_ALEIDOC_0033, args));
		}
		Trace.exitmin(this, "handleRequest(IDocList)");
	}

	/**
	 * Processes a particular IDoc for a particular SAP client transaction. The
	 * result is a TDI Entry that represents the SAP ALE IDoc.
	 * 
	 * @param tid -
	 *            The SAP client transaction identifier.
	 * @param jcoIDoc -
	 *            IDoc to be processed for a particular SAP client transaction.
	 * @return - A TDI Entry that represents the SAP ALE IDoc.
	 */
	protected Entry writeIDocToTDIEntry(String tid, IDoc.Document jcoIDoc) {
		Trace.entrymin(this, "writeIDocToTDIEntry");
		Entry result = null;
		if (null == jcoIDoc) {
			conn.logmsg(
					LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0034));
			Trace.exitmin(this, "writeIDocToTDIEntry");
			return result;
		} else {
			result = new Entry();
			// Create the required attributes in the entry to represent the
			// IDoc.
			if (null != jcoIDoc.toXML() && jcoIDoc.toXML().length() > 0) {
				result.addAttributeValue(SapALEIDocConnector.ATTR_IDOC_AS_XML,
						jcoIDoc.toXML());
			}
			if ((conn.isConnParamValueValid(
					SapALEIDocConnector.CONFIG_PARAM_IDOC_XMLONLY, true)) &&
					(conn
						.getParam(SapALEIDocConnector.CONFIG_PARAM_IDOC_XMLONLY)
						.equalsIgnoreCase(SapALEIDocConnector.NO))) {
				if (null != tid && tid.length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_IDOC_TID, tid);
				if (null != jcoIDoc.getArchiveKey()
						&& jcoIDoc.getArchiveKey().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_ARCKEY, jcoIDoc
									.getArchiveKey());
				if (null != jcoIDoc.getClient()
						&& jcoIDoc.getClient().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_MANDT, jcoIDoc
									.getClient());
				if (null != jcoIDoc.getCreationDateAsString()
						&& jcoIDoc.getCreationDateAsString().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_CREDAT, jcoIDoc
									.getCreationDateAsString());
				if (null != jcoIDoc.getCreationTimeAsString()
						&& jcoIDoc.getCreationTimeAsString().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_CRETIM, jcoIDoc
									.getCreationTimeAsString());
				if (null != jcoIDoc.getDirection()
						&& jcoIDoc.getDirection().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_DIRECT, jcoIDoc
									.getDirection());
				if (null != jcoIDoc.getEDIMessage()
						&& jcoIDoc.getEDIMessage().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_REFMES, jcoIDoc
									.getEDIMessage());
				if (null != jcoIDoc.getEDIMessageGroup()
						&& jcoIDoc.getEDIMessageGroup().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_REFGRP, jcoIDoc
									.getEDIMessageGroup());
				if (null != jcoIDoc.getEDIMessageType()
						&& jcoIDoc.getEDIMessageType().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_STDMES, jcoIDoc
									.getEDIMessageType());
				if (null != jcoIDoc.getEDIStandardFlag()
						&& jcoIDoc.getEDIStandardFlag().length() > 0)
					result.addAttributeValue(SapALEIDocConnector.ATTR_STD,
							jcoIDoc.getEDIStandardFlag());
				if (null != jcoIDoc.getEDIStandardVersion()
						&& jcoIDoc.getEDIStandardVersion().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_STDVRS, jcoIDoc
									.getEDIStandardVersion());
				if (null != jcoIDoc.getEDITransmissionFile()
						&& jcoIDoc.getEDITransmissionFile().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_REFINT, jcoIDoc
									.getEDITransmissionFile());
				if (null != jcoIDoc.getExpressFlag()
						&& jcoIDoc.getExpressFlag().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_EXPRSS, jcoIDoc
									.getExpressFlag());
				if (null != jcoIDoc.getIDocCompoundType()
						&& jcoIDoc.getIDocCompoundType().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_DOCTYP, jcoIDoc
									.getIDocCompoundType());
				if (null != jcoIDoc.getIDocNumber()
						&& jcoIDoc.getIDocNumber().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_DOCNUM, jcoIDoc
									.getIDocNumber());
				if (null != jcoIDoc.getIDocSAPRelease()
						&& jcoIDoc.getIDocSAPRelease().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_DOCREL, jcoIDoc
									.getIDocSAPRelease());
				if (null != jcoIDoc.getIDocType()
						&& jcoIDoc.getIDocType().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_IDOCTYP, jcoIDoc
									.getIDocType());
				if (null != jcoIDoc.getIDocTypeExtension()
						&& jcoIDoc.getIDocTypeExtension().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_CIMTYP, jcoIDoc
									.getIDocTypeExtension());
				if (null != jcoIDoc.getMessageCode()
						&& jcoIDoc.getMessageCode().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_MESCOD, jcoIDoc
									.getMessageCode());
				if (null != jcoIDoc.getMessageFunction()
						&& jcoIDoc.getMessageFunction().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_MESFCT, jcoIDoc
									.getMessageFunction());
				if (null != jcoIDoc.getMessageType()
						&& jcoIDoc.getMessageType().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_MESTYP, jcoIDoc
									.getMessageType());
				if (null != jcoIDoc.getOutputMode()
						&& jcoIDoc.getOutputMode().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_OUTMOD, jcoIDoc
									.getOutputMode());
				if (null != jcoIDoc.getRecipientAddress()
						&& jcoIDoc.getRecipientAddress().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_RCVSAD, jcoIDoc
									.getRecipientAddress());
				if (null != jcoIDoc.getRecipientLogicalAddress()
						&& jcoIDoc.getRecipientLogicalAddress().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_RCVLAD, jcoIDoc
									.getRecipientLogicalAddress());
				if (null != jcoIDoc.getRecipientPartnerFunction()
						&& jcoIDoc.getRecipientPartnerFunction().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_RCVPFC, jcoIDoc
									.getRecipientPartnerFunction());
				if (null != jcoIDoc.getRecipientPartnerNumber()
						&& jcoIDoc.getRecipientPartnerNumber().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_RCVPRN, jcoIDoc
									.getRecipientPartnerNumber());
				if (null != jcoIDoc.getRecipientPartnerType()
						&& jcoIDoc.getRecipientPartnerType().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_RCVPRT, jcoIDoc
									.getRecipientPartnerType());
				if (null != jcoIDoc.getRecipientPort()
						&& jcoIDoc.getRecipientPort().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_RCVPOR, jcoIDoc
									.getRecipientPort());
				if (null != jcoIDoc.getSenderAddress()
						&& jcoIDoc.getSenderAddress().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_SNDSAD, jcoIDoc
									.getSenderAddress());
				if (null != jcoIDoc.getSenderLogicalAddress()
						&& jcoIDoc.getSenderLogicalAddress().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_SNDLAD, jcoIDoc
									.getSenderLogicalAddress());
				if (null != jcoIDoc.getSenderPartnerFunction()
						&& jcoIDoc.getSenderPartnerFunction().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_SNDPFC, jcoIDoc
									.getSenderPartnerFunction());
				if (null != jcoIDoc.getSenderPartnerNumber()
						&& jcoIDoc.getSenderPartnerNumber().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_SNDPRN, jcoIDoc
									.getSenderPartnerNumber());
				if (null != jcoIDoc.getSenderPartnerType()
						&& jcoIDoc.getSenderPartnerType().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_SNDPRT, jcoIDoc
									.getSenderPartnerType());
				if (null != jcoIDoc.getSenderPort()
						&& jcoIDoc.getSenderPort().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_SNDPOR, jcoIDoc
									.getSenderPort());
				if (null != jcoIDoc.getSerialization()
						&& jcoIDoc.getSerialization().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_SERIAL, jcoIDoc
									.getSerialization());
				if (null != jcoIDoc.getStatus()
						&& jcoIDoc.getStatus().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_STATUS, jcoIDoc
									.getStatus());
				if (null != jcoIDoc.getTableStructureName()
						&& jcoIDoc.getTableStructureName().length() > 0)
					result.addAttributeValue(
							SapALEIDocConnector.ATTR_TABNAM, jcoIDoc
									.getTableStructureName());
				if (null != jcoIDoc.getTestFlag()
						&& jcoIDoc.getTestFlag().length() > 0)
					result.addAttributeValue(SapALEIDocConnector.ATTR_TEST,
							jcoIDoc.getTestFlag());
				// Now get the segment data and populate as attributes
				// We won't add the root segment as it's not
				// real segment data.
				IDoc.Segment root = jcoIDoc.getRootSegment();
				IDoc.SegmentIterator childSegIter = root
						.getChildrenIterator();
				while (childSegIter.hasNext()) {
					IDoc.Segment nextSeg = childSegIter.nextSegment();
					result
							.addAttributeValue(
									SapALEIDocConnector.ATTR_CHILD_SEG_PREFIX
											+ nextSeg.getType(), nextSeg
											.toString());
				}
				IDoc.SegmentIterator descSegIter = root
						.getDescendantsIterator();
				while (descSegIter.hasNext()) {
					IDoc.Segment nextSeg = descSegIter.nextSegment();
					result
							.addAttributeValue(
									SapALEIDocConnector.ATTR_DESC_SEG_PREFIX
											+ nextSeg.getType(), nextSeg
											.toString());
				}
			}
		}
		Trace.exitmin(this, "writeIDocToTDIEntry");
		return result;
	}

	/**
	 * Overridden method of JCoIDoc.Server. Function requests that do not
	 * contain IDocs will be handled here. These requests will be stored as XML
	 * file in the incoming path. No other action will be done. The return
	 * parameters won't be filled and no exception will be thrown to the caller.
	 * Future releases of the connector may implement certain function modules
	 * as required within this methods internal method invokations.
	 * 
	 * @param function -
	 *            The remote function call to be processed.
	 */
	protected void handleRequest(JCO.Function function) {
		Trace.entrymin(this, "handleRequest(function)");
		if (conn.mDebugEnabled) {
			Object[] args = new Object[] { currentTID, function.getName() };
			if (conn.mDebugEnabled) {
				conn.debug(
						LogMessageHelper.getMessage(
								LogMessageHelper.SAP_ALEIDOC_0035, args));
			}
		}
		if (currentTID.length() == 0) {
			conn.logmsg(
					LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0036));
			currentTID = SapALEIDocConnector.UNKNOWN_TID;
		}
		Entry funcEntry = writeRFMToTDIEntry(currentTID, function);
		if (conn.isConnParamValueValid(
				SapALEIDocConnector.CONFIG_PARAM_RFM_XML, true)
				&& (conn.getParam(SapALEIDocConnector.CONFIG_PARAM_RFM_XML)
						.equalsIgnoreCase(SapALEIDocConnector.NO))) {
			if (null != funcEntry) {
				Attribute attr = funcEntry
						.getAttribute(SapALEIDocConnector.ATTR_RFM_AS_XML);
				if ((null != attr) && (conn.mDebugEnabled)) {
					Object[] args = new Object[] { attr.getValue() };
					if (conn.mDebugEnabled) {
						conn
								.debug(
										LogMessageHelper
												.getMessage(
														LogMessageHelper.SAP_ALEIDOC_0037,
														args));
					}
				}
			}
			Trace.exitmin(this, "handleRequest(function)");
			return;
		}
		ArrayList funcEntries = new ArrayList();
		if (null == funcEntry) {
			conn.logmsg(
					LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0019));
		} else {
			funcEntries.add(funcEntry);
		}
		if (conn.getTidIDocMap().containsKey(currentTID)) {
			conn.logmsg(
					LogMessageHelper
							.getMessage(LogMessageHelper.SAP_ALEIDOC_0018));
		} else if (funcEntries.size() < 1) {
			conn.logmsg(LogMessageHelper.SAP_ALEIDOC_0038);
		} else {
			TIDManager tidMgr = new TIDManager(currentTID, funcEntries);
			conn.getTidIDocMap().put(currentTID, tidMgr);
		}
		Trace.exitmin(this, "handleRequest(function)");
	}

	/**
	 * Processes a particular RFM for a particular SAP client transaction. The
	 * result is a TDI Entry that represents the SAP RFM. If a TID is not known
	 * then a default TID will have been assigned to fit within the TID
	 * Management functionality.
	 * 
	 * @param tid -
	 *            The SAP client transaction identifier.
	 * @param function -
	 *            RFM to be processed for a particular SAP client transaction.
	 * @return - A TDI Entry that represents the SAP ALE IDoc.
	 */
	protected Entry writeRFMToTDIEntry(String tid, JCO.Function function) {
		Trace.entrymin(this, "writeRFMToTDIEntry");
		Entry result = null;
		if (null == function) {
			conn.logmsg(LogMessageHelper.SAP_ALEIDOC_0039);
			Trace.exitmin(this, "writeRFMToTDIEntry");
			return result;
		} else {
			String filename;
			if (conn.isConnParamValueValid(SapALEIDocConnector.JCO_TRACE_PATH,
					true)) {
				filename = conn.getParam(SapALEIDocConnector.JCO_TRACE_PATH)
						+ File.separator + function.getName() + "_"
						+ timestampFormat.format(new java.util.Date()) + ".xml";
			} else {
				filename = function.getName() + "_"
						+ timestampFormat.format(new java.util.Date()) + ".xml";
			}
			if (conn.mDebugEnabled) {
				Object[] args = new Object[] { filename };
				if (conn.mDebugEnabled) {
					conn.debug(
							LogMessageHelper.getMessage(
									LogMessageHelper.SAP_ALEIDOC_0040, args));
				}
			}
			function.writeXML(filename);
			BufferedReader xmlReader;
			try {
				xmlReader = new BufferedReader(new FileReader(filename));
				StringBuffer funcXMLStr = new StringBuffer();
				String line;
				try {
					while ((line = xmlReader.readLine()) != null) {
						funcXMLStr.append(line);
					}
					xmlReader.close();
					if (!conn.mDebugEnabled) {
						File rfmXML = new File(filename);
						boolean deleted = rfmXML.delete();
						if (!deleted)
						{
							Object[] args = new Object[] { filename };
							//print error message
							conn.logmsg(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0047, args));
						}
					}
					if (funcXMLStr.toString().length() > 0) {
						result = new Entry();
						result.addAttributeValue(
								SapALEIDocConnector.ATTR_RFM_AS_XML, funcXMLStr
										.toString());
						Trace.exitmin(this, "writeRFMToTDIEntry");
						return result;
					} else {
						Object[] args = new Object[] { filename };
						conn
								.logmsg(
										LogMessageHelper
												.getMessage(
														LogMessageHelper.SAP_ALEIDOC_0041,
														args));
						Trace.exitmin(this, "writeRFMToTDIEntry");
						return null;
					}
				} catch (IOException e1) {
					Object[] args = new Object[] { e1.getMessage() };
					conn.logmsg(
							LogMessageHelper.getMessage(
									LogMessageHelper.SAP_ALEIDOC_0042, args));
					if (conn.mDebugEnabled) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e1.printStackTrace(pw);
						if (conn.mDebugEnabled) {
							conn.debug(sw.toString());
						}
					}
					Trace.exitmin(this, "writeRFMToTDIEntry");
					return null;
				}
			} catch (FileNotFoundException e) {
				Object[] args = new Object[] { e.getMessage() };
				conn.logmsg(
						LogMessageHelper.getMessage(
								LogMessageHelper.SAP_ALEIDOC_0042, args));
				if (conn.mDebugEnabled) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					if (conn.mDebugEnabled) {
						conn.debug(sw.toString());
					}
				}
				Trace.exitmin(this, "writeRFMToTDIEntry");
				return null;
			}
		}
	}

	/**
	 * The displayFunction() methods have been left in for debug purposes only.
	 * They demonstrate how to access and process and RFMs input, export and
	 * table parameters. The log messages will not be part of translation.
	 * 
	 * @param function -
	 *            RFM to be displayed.
	 */
	private void displayFunction(JCO.Function function) {
		// Set values for the rquired import parameter values
		JCO.ParameterList input = function.getImportParameterList();
		if (null != input && null != input.fields()) {
			for (JCO.FieldIterator e = input.fields(); e.hasMoreElements();) {
				JCO.Field field = e.nextField();
				if (conn.mDebugEnabled) {
					conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0048, new Object[] { field.getName() }));
				}
				if (field.isStructure()) {
					// import structure
					if (conn.mDebugEnabled) {
						conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0049));
					}
					displayImportValue(field.getStructure());
				} else if (field.isTable()) {
					// import table
					if (conn.mDebugEnabled) {
						conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0050));
					}
					displayImportValue(field.getTable());
				} else {
					// import simple field
					if (conn.mDebugEnabled) {
						conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0051));
					}
					displayImportValue(field);
				}
			}
		} else {
			if (conn.mDebugEnabled) {
				conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0052));
			}
		}
		// Print out the returned export parameters and their
		// values
		if (conn.mDebugEnabled) {
			conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0053));
		}
		JCO.ParameterList export = function.getExportParameterList();
		if (null != export && null != export.fields()) {
			for (JCO.FieldIterator e = export.fields(); e.hasMoreElements();) {
				JCO.Field exportField = e.nextField();
				if (exportField.isStructure()) {
					// import structure
					if (conn.mDebugEnabled) {
						conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0054));
					}
					displayImportValue(exportField.getStructure());
				} else {
					// import simple field
					if (conn.mDebugEnabled) {
						conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0055));
					}
					displayImportValue(exportField);
				}
			}
			if (conn.mDebugEnabled) {
				conn.debug("\n");
			}
		} else {
			if (conn.mDebugEnabled) {
				conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0056));
			}
		}
		if (conn.mDebugEnabled) {
			conn.debug("\n\n");
			// Print table results
			conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0057));
		}
		JCO.ParameterList tables = function.getTableParameterList();
		for (JCO.FieldIterator te = tables.fields(); te.hasMoreElements();) {
			JCO.Field tableField = te.nextField();
			JCO.Table table = function.getTableParameterList().getTable(
					tableField.getName());
			if (table.getNumRows() > 0) {
				if (conn.mDebugEnabled) {
					conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0058, new Object[] { tableField.getName() }));
				}
				// Loop over all rows
				do {
					conn.debug("-----------------------------------------");
					// Loop over all columns in the current row
					for (JCO.FieldIterator e = table.fields(); e
							.hasMoreElements();) {
						JCO.Field field = e.nextField();
						if (conn.mDebugEnabled) {
							conn.debug(field.getName() + ":\t" + field.getString());
						}
					}// for
				} while (table.nextRow());
			} else {
				if (conn.mDebugEnabled) {
					conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0059, new Object[] { tableField.getName() }));
				}
			}
			if (conn.mDebugEnabled) {
				conn.debug("\n");
			}
		}
	}

	/**
	 * The displayImportValue() methods have been left in for debug purposes
	 * only. They demonstrate how to access and process and RFMs input, export
	 * and table parameters. The log messages will not be part of translation.
	 * 
	 * @param field -
	 *            Import field to be displayed.
	 */
	private void displayImportValue(JCO.Field field) {
		if (null != field && field.getName().length() > 0) {
			if (conn.mDebugEnabled) {
				conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0060,new Object[] { field.getName(), field.getString() }));
				conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0062,new Object[] { field.getName(), field.getValue() }));
			}
		} else {
			if (conn.mDebugEnabled) {
				conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0064));
			}
		}
	}

	/**
	 * The displayImportValue() methods have been left in for debug purposes
	 * only. They demonstrate how to access and process and RFMs input, export
	 * and table parameters. The log messages will not be part of translation.
	 * 
	 * @param field -
	 *            Import Record to be displayed.
	 */
	private void displayImportValue(JCO.Record rec) {
		JCO.FieldIterator fi = rec.fields();
		fi.reset();
		if (conn.mDebugEnabled) {
			conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0065));
		}
		int fCount = 0;
		while (fi.hasMoreFields()) {
			fCount++;
			if (conn.mDebugEnabled) {
				conn.debug(LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0066,new Object[] { ""+ fCount }));
			}
			JCO.Field f = fi.nextField();
			displayImportValue(f);
		}
	}

	/**
	 * The displayImportValue() methods have been left in for debug purposes
	 * only. They demonstrate how to access and process and RFMs input, export
	 * and table parameters. The log messages will not be part of translation.
	 * 
	 * @param field -
	 *            Import Table to be displayed.
	 */
	private void displayImportValue(JCO.Table tab) {
		if (conn.mDebugEnabled) {
			conn.debug(
					LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0067));
		}
		displayImportValue((JCO.Record) tab);
	}

	/**
	 * The displayImportValue() methods have been left in for debug purposes
	 * only. They demonstrate how to access and process and RFMs input, export
	 * and table parameters. The log messages will not be part of translation.
	 * 
	 * @param field -
	 *            Import Structure to be displayed.
	 */
	private void displayImportValue(JCO.Structure struct) {
		if (conn.mDebugEnabled) {
			conn.debug(
					LogMessageHelper.getMessage(LogMessageHelper.SAP_ALEIDOC_0068));
		}
		displayImportValue((JCO.Record) struct);
	}
}
