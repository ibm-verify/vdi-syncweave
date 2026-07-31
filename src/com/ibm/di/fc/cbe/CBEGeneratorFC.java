/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.cbe;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.emf.common.util.EList;
import org.eclipse.hyades.logging.core.Guid;
import org.eclipse.hyades.logging.core.XmlUtility;
import org.eclipse.hyades.logging.events.cbe.AvailableSituation;
import org.eclipse.hyades.logging.events.cbe.CommonBaseEvent;
import org.eclipse.hyades.logging.events.cbe.ComponentIdentification;
import org.eclipse.hyades.logging.events.cbe.ConfigureSituation;
import org.eclipse.hyades.logging.events.cbe.ConnectSituation;
import org.eclipse.hyades.logging.events.cbe.CreateSituation;
import org.eclipse.hyades.logging.events.cbe.DependencySituation;
import org.eclipse.hyades.logging.events.cbe.DestroySituation;
import org.eclipse.hyades.logging.events.cbe.EventFactory;
import org.eclipse.hyades.logging.events.cbe.ExtendedDataElement;
import org.eclipse.hyades.logging.events.cbe.FeatureSituation;
import org.eclipse.hyades.logging.events.cbe.OtherSituation;
import org.eclipse.hyades.logging.events.cbe.ReportSituation;
import org.eclipse.hyades.logging.events.cbe.RequestSituation;
import org.eclipse.hyades.logging.events.cbe.Situation;
import org.eclipse.hyades.logging.events.cbe.SituationType;
import org.eclipse.hyades.logging.events.cbe.StartSituation;
import org.eclipse.hyades.logging.events.cbe.StopSituation;
import org.eclipse.hyades.logging.events.cbe.impl.EventFactoryContext;
import org.eclipse.hyades.logging.events.cbe.util.EventFormatter;
import org.eclipse.hyades.logging.java.CommonBaseEventLogRecord;

import com.ibm.di.entry.Attribute;
import com.ibm.di.entry.Entry;
import com.ibm.di.fc.Function;
import com.ibm.di.server.ResourceHash;

/**
 * This class generates objects of type
 * org.eclipse.hyades.logging.events.cbe.CommonBaseEvent from attributes mapped
 * by user to Entry object.
 * 
 */
public class CBEGeneratorFC extends Function {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "cbegeneratorfc";

	/**
	 * Indicates the mode - Entry to CBE
	 */
	private static final int ENTRY_TO_CBE = 0;

	/**
	 * Indicates the mode - CBE to Entry
	 */
	private static final int CBE_TO_ENTRY = 1;

	/**
	 * Used for retrieving the singleton instance of the
	 * {@link EventFactoryContext} and getting eventFactory
	 */
	private String EVENT_FACTORY = "org.eclipse.hyades.logging.events.cbe.impl.SimpleEventFactoryHomeImpl";

	/**
	 * Name of the logger
	 */
	private String LOGGERS_NAME;

	/**
	 * Utility parameter.
	 */
	private EventFactory eventFactory;

	/**
	 * {@link Situation}
	 */
	private Situation situation;

	/**
	 * Source component ID.
	 */
	private ComponentIdentification sourceComponentId;

	/**
	 * Reporter component ID.
	 */
	private ComponentIdentification reporterComponentId;

	/**
	 * IP address of the local host.
	 */
	private String localHostIP;

	/**
	 * Stores error message.
	 */
	private String tmsMessage = null;
	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	/**
	 * {@link CommonBaseEvent}
	 */
	private CommonBaseEvent commonBaseEvent;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * 
	 * The attributes of the Entry object passed as parameter to this method is
	 * mapped to/from a CommonBaseEvent.
	 * 
	 * @param obj
	 *            object of type Entry
	 * @return the {@link Entry}
	 * @exception Exception
	 *                if the user has passed a null or non Entry type object as
	 *                parameter
	 */
	public Object perform(Object obj) throws Exception {

		if (obj != null) {
			if (obj instanceof Entry) {
				Entry e = new Entry();

				Object mode = getParam("mode");
				int intMode = 0;

				if (mode != null) {
					intMode = Integer.parseInt(mode.toString());
				}

				// Entry -> CBE mode
				if (ENTRY_TO_CBE == intMode) {
					mapEntry((Entry) obj);
					e.setAttribute("event", commonBaseEvent);
					e.setAttribute("eventXml",
							convertCBEEventToXML(commonBaseEvent));
					return e;

					// CBE -> Entry mode
				} else if (CBE_TO_ENTRY == intMode) {
					Object cbeObj = ((Entry) obj).getObject("event");
					String cbeXML = ((Entry) obj).getString("eventXml");

					if (!(cbeObj instanceof CommonBaseEvent) && cbeXML == null) {

						// both attributes are not defined
						tmsMessage = sResHash
								.getString("FC.CBE_GENERATOR.INVALID_OR_UNDEFINED_CBE_ATTRIBUTE");
						throw new Exception(tmsMessage);
					}

					if (cbeObj instanceof CommonBaseEvent && cbeXML == null) {

						// only cbeObj is defined

						CommonBaseEvent cbe = (CommonBaseEvent) cbeObj;
						CBEGeneratorFC.mapCbeToEntry(cbe, e);

					} else if (cbeXML != null && cbeObj == null) {

						// only cbeXML is defined
						CommonBaseEvent[] cbes = null;

						try {

							boolean validateXML = Boolean
									.parseBoolean(getParam("validateXML")
											.toString());

							cbes = EventFormatter.eventsFromCanonicalXMLDoc(
									cbeXML, validateXML);

						} catch (Exception fe) {
							// the error to throw if invalid CBE XML is passed
							tmsMessage = sResHash.getString(
									"FC.CBE_GENERATOR.INVALID_CBE_XML", fe
											.getMessage());
							throw new Exception(tmsMessage);
						}
						if (cbes != null) {
							CommonBaseEvent cbe = cbes[0];
							CBEGeneratorFC.mapCbeToEntry(cbe, e);
						} else {
							// if we are here then we have gotten no CBE
							// object because the XML does no contain valid CBE
							// information so log the message in the catch block
							tmsMessage = sResHash
									.getString("FC.CBE_GENERATOR.NO_CBE_IN_XML");
							throw new Exception(tmsMessage);
						}
					} else {
						// throw message for CBE events in both attributes
						tmsMessage = sResHash
								.getString("FC.CBE_GENERATOR.CBEOBJ_AND_CBEXML_ARE_PRESENTED");
						throw new Exception(tmsMessage);
					}
					return e;
				}
			} else {
				tmsMessage = sResHash
						.getString("FC.CBE_GENERATOR.INVALID_ARGUMENT_FOR_PERFORM");
			}
		} else {
			tmsMessage = sResHash
					.getString("FC.CBE_GENERATOR.UNDEFINED_ARGUMENT_FOR_PERFORM");
		}
		return null;
	}

	/**
	 * Returns default value for "p" if not specified in entry object.
	 * 
	 * @param e
	 *            Entry
	 * @param p
	 *            String
	 * @param defval
	 *            String
	 * @return String
	 */
	private String param(Entry e, String p, String defval) {
		String str = e.getString(p);
		if (str != null && str.length() > 0)
			return str;
		else
			return defval;
	}

	/**
	 * Method checks if user has defined value for this attribute p
	 * 
	 * @param e
	 *            Entry
	 * @param p
	 *            String
	 * @return boolean
	 */
	private boolean isValueDefined(Entry e, String p) {
		boolean isDefined = false;
		String str = e.getString(p);
		if (str != null && str.length() > 0) {
			isDefined = true;
		}
		return isDefined;

	}

	/**
	 * Maps an Entry object to a CBE object
	 * 
	 * @param entry
	 *            Entry object that has the user defined attributes to create
	 *            CBE .
	 * @exception Exception -
	 *                ValidationException
	 */
	private void mapEntry(Entry entry) throws Exception {

		commonBaseEvent = eventFactory.createCommonBaseEvent();

		CBESituationCreator cbeSituationCreator = new CBESituationCreator(entry);
		// Create a new instance of a situation:
		situation = eventFactory.createSituation();
		String categoryName;

		if (!isValueDefined(entry, "situation.CategoryName")) {
			tmsMessage = sResHash
					.getString("FC.CBE_GENERATOR.SITUATION_CATEGORY_NAME.UNDEFINED");
			throw new Exception(tmsMessage);
		} else {
			categoryName = entry.getString("situation.CategoryName");
			// Validate that the user has entered defined value for
			// situationType
			cbeSituationCreator.validateSituationType(categoryName);
			situation.setCategoryName(categoryName);
		}
		situation.setSituationType(cbeSituationCreator
				.getSituation(eventFactory));
		commonBaseEvent.setSituation(situation);

		// Create source component ID
		sourceComponentId = eventFactory.createComponentIdentification();
		sourceComponentId
				.setLocation(param(entry, "SCI.location", localHostIP));
		sourceComponentId.setLocationType(param(entry, "SCI.locationType",
				"Unknown"));
		sourceComponentId.setExecutionEnvironment(entry
				.getString("SCI.executionEnvironment"));
		sourceComponentId.setComponentIdType(param(entry,
				"SCI.componentIdType", "Unknown"));
		sourceComponentId.setComponent(entry.getString("SCI.component"));
		sourceComponentId.setSubComponent(entry.getString("SCI.subcomponent"));
		sourceComponentId
				.setComponentType(entry.getString("SCI.componentType"));
		commonBaseEvent.setSourceComponentId(sourceComponentId);

		// Set the event's globalInstanceId property with a new GUID:
		commonBaseEvent.setGlobalInstanceId(param(entry, "GlobalInstanceId",
				Guid.generate()));

		// Set the Creation time
		long creationVal;
		String time = param(entry, "CreationTime", ""
				+ System.currentTimeMillis());
		try {
			creationVal = Long.parseLong(time);
			commonBaseEvent.setCreationTimeAsLong(creationVal);
		} catch (NumberFormatException err) {
			commonBaseEvent.setCreationTime(time);
		}

		String prefix = "RCI.";
		List list = getMatchingAttrs(entry, prefix);
		if (!list.isEmpty()) {
			// Create reporter component ID
			reporterComponentId = eventFactory.createComponentIdentification();
			reporterComponentId.setLocation(param(entry, "RCI.location",
					localHostIP));
			reporterComponentId.setLocationType(param(entry,
					"RCI.locationType", "Unknown"));
			reporterComponentId.setExecutionEnvironment(entry
					.getString("RCI.executionEnvironment"));
			reporterComponentId.setComponentIdType(param(entry,
					"RCI.componentIdType", "Unknown"));
			reporterComponentId.setComponent(entry.getString("RCI.component"));
			reporterComponentId.setSubComponent(entry
					.getString("RCI.subcomponent"));
			reporterComponentId.setComponentType(entry
					.getString("RCI.componentType"));
			commonBaseEvent.setReporterComponentId(reporterComponentId);
		}
		// Set the repeatCount -no. of occurrences of event within specified
		// time
		boolean repeatCountPresent = false;
		if (isValueDefined(entry, "repeatCount")) {
			repeatCountPresent = true;
			short repeatCountVal;
			try {
				repeatCountVal = Short.parseShort(entry
						.getString("repeatCount"));
			} catch (NumberFormatException e) {
				tmsMessage = sResHash
						.getString("FC.CBE_GENERATOR.REPEAT_COUNT_INVALID");
				throw new Exception(tmsMessage);
			}
			commonBaseEvent.setRepeatCount(repeatCountVal);
		}

		// Set elapsed time - specified time for the repeatCount
		if (isValueDefined(entry, "elapsedTime")) {
			long elapsedTimeVal;
			try {
				elapsedTimeVal = Long.parseLong(entry.getString("elapsedTime"));
			} catch (NumberFormatException e) {
				tmsMessage = sResHash
						.getString("FC.CBE_GENERATOR.ELAPSED_TIME_INVALID");
				throw new Exception(tmsMessage);
			}
			commonBaseEvent.setElapsedTime(elapsedTimeVal);
		}
		// Set the event's severity
		if (isValueDefined(entry, "Severity")) {
			String sev = entry.getString("Severity");
			short severityVal;
			try {
				severityVal = Short.parseShort(sev);
			} catch (NumberFormatException e) {
				tmsMessage = sResHash
						.getString("FC.CBE_GENERATOR.SEVERITY_INVALID");
				throw new Exception(tmsMessage);
			}
			commonBaseEvent.setSeverity(severityVal);
		}
		// Set the event's priority
		if (isValueDefined(entry, "Priority")) {
			String priority = entry.getString("Priority");
			short priorityVal;
			try {
				priorityVal = Short.parseShort(priority);
			} catch (NumberFormatException e) {
				tmsMessage = sResHash
						.getString("FC.CBE_GENERATOR.PRIORITY_INVALID");
				throw new Exception(tmsMessage);
			}
			commonBaseEvent.setPriority(priorityVal);
		}

		if (isValueDefined(entry, "sequenceNumber")) {
			String seqNumber = entry.getString("sequenceNumber");
			long seqVal;
			try {
				seqVal = Long.parseLong(seqNumber);
			} catch (NumberFormatException e) {
				tmsMessage = sResHash
						.getString("FC.CBE_GENERATOR.SEQUENCE_NUMBER_INVALID");
				throw new Exception(tmsMessage);
			}

			commonBaseEvent.setSequenceNumber(seqVal);
		}
		commonBaseEvent.setExtensionName(entry.getString("extensionName"));
		commonBaseEvent.setMsg(entry.getString("Message"));

		createExtendedDataElements(commonBaseEvent, entry);

		/*
		 * CBE.validate() validates the created event for all required values
		 * etc. and throws exception otherwise.
		 */
		commonBaseEvent.validate();
	}

	/**
	 * Returns list of names with matching prefix.
	 * 
	 * @param e
	 *            {@link Entry}
	 * @param prefix
	 *            prefix
	 * @return List
	 */
	private List getMatchingAttrs(Entry e, String prefix) {
		String[] names = e.getAttributeNames();
		ArrayList list = new ArrayList();
		for (int i = 0; i < names.length; i++) {
			if (names[i].startsWith(prefix)) {
				list.add(names[i].substring(prefix.length()));
			}
		}

		Collections.sort(list);

		return list;
	}

	/**
	 * Creates ExtendedDataElements from attribute mapped as X.Attributename
	 * X.AttributeName.Value
	 * 
	 * @param cbe
	 *            commonBaseEvent to which EDE will be added
	 * @param e
	 *            Entry object that has the user defined EDE attributes
	 * @throws Exception
	 */
	private void createExtendedDataElements(CommonBaseEvent cbe, Entry e)
			throws Exception {

		String prefix = "X.";
		List list = getMatchingAttrs(e, prefix);
		List extendedDataElements = new ArrayList();
		boolean edePresent;
		for (int i = 0; i < list.size(); i++) {
			String str = (String) list.get(i);
			String[] arr = str.split("\\.");

			// Create noValue type elements for all but the last
			ExtendedDataElement ede = null;
			for (int j = 0; j < (arr.length - 1); j++) {
				if (ede == null) {
					/*
					 * Checks if EDE of same name has already been added to cbe,
					 * is yes, then add this as child element to same cbe eg.
					 * X.ede.firstChild, X.ede.secondChild - both belong to same
					 * EDE
					 */
					edePresent = extendedDataElements.contains(arr[j]);
					if (edePresent) {
						EList edeList = cbe.getExtendedDataElements(arr[j]);
						ede = (ExtendedDataElement) edeList.get(0);
					} else {
						ede = cbe.addExtendedDataElementWithNoValue(arr[j]);
						extendedDataElements.add(arr[j]);
					}
				} else {
					ede = ede.addChild(arr[j]);
				}
			}

			// Create final EDE with name, type and values
			// Currently only String supported
			String name = arr[arr.length - 1];
			Attribute a = e.getAttribute(prefix + str);
			if (a.size() > 1) {
				String[] av = new String[a.size()];
				for (int j = 0; j < a.size(); j++)
					av[j] = "" + a.getValue(j);
				if (ede == null)
					ede = cbe.addExtendedDataElement(name, av);
				else
					ede = ede.addChild(name, av);
			} else {
				if (ede == null)
					ede = cbe.addExtendedDataElement(name, a.getValue());
				else
					ede = ede.addChild(name, a.getValue());
			}
		}
	}

	/**
	 * Version information.
	 * 
	 * @return version information
	 */
	public String getVersion() {
		return "2.2-di7.1.1 %I%, 20%E%";
	}

	/**
	 * This method initializes the eventFactory object used to create the
	 * CommonBaseEvent
	 * 
	 * @param obj
	 * @throws Exception:
	 *             never.
	 * 
	 */
	public void initialize(Object obj) throws Exception {
		super.initialize(obj);
		String param;

		try {
			localHostIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException u) {
			localHostIP = InetAddress.getByName("localhost").getHostAddress();
		}

		param = (String) getParam("loggers_name");
		if (param != null && param.length() > 0) {
			LOGGERS_NAME = param;
		} else {
			LOGGERS_NAME = localHostIP;
		}

		// Retrieve the singleton instance of the Event Factory Context and get
		// eventFactory
		eventFactory = EventFactoryContext.getInstance().getEventFactoryHome(
				EVENT_FACTORY).getEventFactory(LOGGERS_NAME);
	}

	/**
	 * returns the CBE event as a XMLDocString using
	 * org.eclipse.hyades.logging.events.cbe.util.EventFormatter
	 * 
	 * @param event
	 *            CommonBaseEvent
	 * @return String
	 * @throws Exception
	 */
	public static String convertCBEEventToXML(CommonBaseEvent event)
			throws Exception {

		String eventXml = EventFormatter.toCanonicalXMLDocString(event);
		return eventXml;

	}

	/**
	 * Method uses org.eclipse.hyades.logging.java.CommonBaseEventLogRecord to
	 * generate log records
	 * 
	 * @param event
	 *            CommomnBaseEvent
	 * @param isXMLComplete
	 *            returns a complete xml string if true
	 * @return xmlDocString
	 * 
	 * @throws Exception
	 */
	public static String getCBELogXml(CommonBaseEvent event,
			boolean isXMLComplete) throws Exception {

		CommonBaseEventLogRecord commonBaseEventLogRecord = new CommonBaseEventLogRecord(
				event);
		String xmlOutput;

		if (isXMLComplete) {
			xmlOutput = commonBaseEventLogRecord
					.externalizeCanonicalXmlDocString();
		} else {
			xmlOutput = commonBaseEventLogRecord
					.externalizeCanonicalXmlString();
		}
		return xmlOutput;

	}

	/**
	 * Maps the fields of a Common Base Event object into the attributes of a
	 * SyncWeave Entry. This process is the reverse of what the CBE Generator FC's
	 * 'perform' method does. All attributes in the resulting Entry are of type
	 * java.lang.String.
	 * 
	 * @param aCbe
	 *            the Common Base Event to be mapped into the Entry
	 * @param aEntry
	 *            the Entry, where will be mapped the fields of the Common Base
	 *            Event
	 * 
	 * @since 6.1.1
	 */
	public static void mapCbeToEntry(CommonBaseEvent aCbe, Entry aEntry) {

		if (null == aCbe || null == aEntry) {
			return;
		}

		aEntry.setAttribute("CreationTime", "" + aCbe.getCreationTime());
		aEntry.setAttribute("GlobalInstanceId", ""
				+ XmlUtility.denormalize(aCbe.getGlobalInstanceId()));
		aEntry.setAttribute("Message", ""
				+ XmlUtility.denormalize(aCbe.getMsg()));
		if (aCbe.isSetSeverity()) {
			aEntry.setAttribute("Severity", "" + aCbe.getSeverity());
		}
		aEntry.setAttribute("ExtensionName", XmlUtility.denormalize(aCbe
				.getExtensionName()));
		if (aCbe.isSetSequenceNumber()) {
			aEntry
					.setAttribute("SequenceNumber", ""
							+ aCbe.getSequenceNumber());
		}
		if (aCbe.isSetRepeatCount()) {
			aEntry.setAttribute("RepeatCount", "" + aCbe.getRepeatCount());
		}
		if (aCbe.isSetElapsedTime()) {
			aEntry.setAttribute("ElapsedTime", "" + aCbe.getElapsedTime());
		}
		if (aCbe.isSetPriority()) {
			aEntry.setAttribute("Priority", "" + aCbe.getPriority());
		}

		ComponentIdentification sourceCompId = aCbe.getSourceComponentId();
		if (null != sourceCompId) {
			aEntry.setAttribute("SCI.location", ""
					+ XmlUtility.denormalize(sourceCompId.getLocation()));
			aEntry.setAttribute("SCI.locationType", ""
					+ XmlUtility.denormalize(sourceCompId.getLocationType()));
			aEntry.setAttribute("SCI.executionEnvironment", ""
					+ XmlUtility.denormalize(sourceCompId
							.getExecutionEnvironment()));
			aEntry.setAttribute("SCI.component", ""
					+ XmlUtility.denormalize(sourceCompId.getComponent()));
			aEntry.setAttribute("SCI.subcomponent", ""
					+ XmlUtility.denormalize(sourceCompId.getSubComponent()));
			aEntry
					.setAttribute("SCI.componentIdType", ""
							+ XmlUtility.denormalize(sourceCompId
									.getComponentIdType()));
			aEntry.setAttribute("SCI.componentType", ""
					+ XmlUtility.denormalize(sourceCompId.getComponentType()));
		}

		ComponentIdentification reporterCompId = aCbe.getReporterComponentId();
		if (null != reporterCompId) {
			aEntry.setAttribute("RCI.location", ""
					+ XmlUtility.denormalize(reporterCompId.getLocation()));
			aEntry.setAttribute("RCI.locationType", ""
					+ XmlUtility.denormalize(reporterCompId.getLocationType()));
			aEntry.setAttribute("RCI.executionEnvironment", ""
					+ XmlUtility.denormalize(reporterCompId
							.getExecutionEnvironment()));
			aEntry.setAttribute("RCI.component", ""
					+ XmlUtility.denormalize(reporterCompId.getComponent()));
			aEntry.setAttribute("RCI.subcomponent", ""
					+ XmlUtility.denormalize(reporterCompId.getSubComponent()));
			aEntry.setAttribute("RCI.componentIdType", ""
					+ XmlUtility.denormalize(reporterCompId
							.getComponentIdType()));
			aEntry
					.setAttribute("RCI.componentType", ""
							+ XmlUtility.denormalize(reporterCompId
									.getComponentType()));
		}

		/* see what kind of situation we have */
		Situation situation = aCbe.getSituation();
		SituationType situationType = null;

		if (null != situation) {
			aEntry.setAttribute("situation.CategoryName", ""
					+ situation.getCategoryName());
			situationType = situation.getSituationType();
		}

		if (null != situationType) {
			aEntry
					.setAttribute("situation.reasoningScope", ""
							+ XmlUtility.denormalize(situationType
									.getReasoningScope()));

			if (situationType instanceof AvailableSituation) {

				AvailableSituation s = (AvailableSituation) situationType;
				aEntry.setAttribute("availableSituation.operationDisposition",
						""
								+ XmlUtility.denormalize(s
										.getOperationDisposition()));
				aEntry.setAttribute("availableSituation.processingDisposition",
						""
								+ XmlUtility.denormalize(s
										.getProcessingDisposition()));
				aEntry.setAttribute(
						"availableSituation.availabilityDisposition", ""
								+ XmlUtility.denormalize(s
										.getAvailabilityDisposition()));

			} else if (situationType instanceof ConfigureSituation) {

				ConfigureSituation s = (ConfigureSituation) situationType;
				aEntry.setAttribute("configureSituation.successDisposition", ""
						+ XmlUtility.denormalize(s.getSuccessDisposition()));

			} else if (situationType instanceof ConnectSituation) {

				ConnectSituation s = (ConnectSituation) situationType;
				aEntry.setAttribute("connectSituation.successDisposition", ""
						+ XmlUtility.denormalize(s.getSuccessDisposition()));
				aEntry.setAttribute("connectSituation.situationDisposition", ""
						+ XmlUtility.denormalize(s.getSituationDisposition()));

			} else if (situationType instanceof CreateSituation) {

				CreateSituation s = (CreateSituation) situationType;
				aEntry.setAttribute("createSituation.successDisposition", ""
						+ XmlUtility.denormalize(s.getSuccessDisposition()));

			} else if (situationType instanceof DependencySituation) {

				DependencySituation s = (DependencySituation) situationType;
				aEntry.setAttribute(
						"dependencySituation.dependencyDisposition", ""
								+ XmlUtility.denormalize(s
										.getDependencyDisposition()));

			} else if (situationType instanceof DestroySituation) {

				DestroySituation s = (DestroySituation) situationType;
				aEntry.setAttribute("destroySituation.successDisposition", ""
						+ XmlUtility.denormalize(s.getSuccessDisposition()));

			} else if (situationType instanceof FeatureSituation) {

				FeatureSituation s = (FeatureSituation) situationType;
				aEntry.setAttribute("featureSituation.featureDisposition", ""
						+ XmlUtility.denormalize(s.getFeatureDisposition()));

			} else if (situationType instanceof ReportSituation) {

				ReportSituation s = (ReportSituation) situationType;
				aEntry.setAttribute("reportSituation.reportCategory", ""
						+ XmlUtility.denormalize(s.getReportCategory()));

			} else if (situationType instanceof RequestSituation) {

				RequestSituation s = (RequestSituation) situationType;
				aEntry.setAttribute("requestSituation.successDisposition", ""
						+ XmlUtility.denormalize(s.getSuccessDisposition()));
				aEntry.setAttribute("requestSituation.situationQualifier", ""
						+ XmlUtility.denormalize(s.getSituationQualifier()));

			} else if (situationType instanceof StartSituation) {

				StartSituation s = (StartSituation) situationType;
				aEntry.setAttribute("startSituation.successDisposition", ""
						+ XmlUtility.denormalize(s.getSuccessDisposition()));
				aEntry.setAttribute("startSituation.situationQualifier", ""
						+ XmlUtility.denormalize(s.getSituationQualifier()));

			} else if (situationType instanceof StopSituation) {

				StopSituation s = (StopSituation) situationType;
				aEntry.setAttribute("stopSituation.successDisposition", ""
						+ XmlUtility.denormalize(s.getSuccessDisposition()));
				aEntry.setAttribute("stopSituation.situationQualifier", ""
						+ XmlUtility.denormalize(s.getSituationQualifier()));

			} else if (situationType instanceof OtherSituation) {

				OtherSituation s = (OtherSituation) situationType;
				aEntry.setAttribute("otherSituation.any", ""
						+ XmlUtility.denormalize(s.getAny()));
			}
		}

		// Map all Extended Data Elements (EDEs) to the entry.
		List edeList = aCbe.getExtendedDataElements();
		if (null != edeList) {
			Iterator it = edeList.iterator();
			while (it.hasNext()) {
				ExtendedDataElement ede = (ExtendedDataElement) it.next();
				mapEDEToEntry(ede, aEntry, new Stack()); // call the
				// auxiliary
				// recursive routine
			}
		}

	}

	/**
	 * Recursively maps an Extended Data Element (EDE) into an Entry.
	 * 
	 * @param aEde
	 *            the Extended Data Element
	 * @param aEntry
	 *            the Entry, where the EDE will be mapped
	 * @param aEdeAncestors
	 *            the ancestors of 'ede'; pass an empty stack, if it is a
	 *            topmost EDE
	 * 
	 * @since 6.1.1
	 */
	private static void mapEDEToEntry(ExtendedDataElement aEde, Entry aEntry,
			Stack aEdeAncestors) {

		if (null == aEde) {
			return;
		}

		/*
		 * Calculate the name of the EDE attribute in the Entry by including the
		 * names of its ancestors and its own name.
		 */
		StringBuffer edeAttributeNameBuffer = new StringBuffer(64);

		// add the names of EDE's ancestors, separated by a dot (the final dot
		// is placed after the last ancestor)
		Iterator ancestorsIter = aEdeAncestors.iterator();
		while (ancestorsIter.hasNext()) {

			ExtendedDataElement ancestor = (ExtendedDataElement) ancestorsIter
					.next();
			edeAttributeNameBuffer.append(ancestor.getName());
			edeAttributeNameBuffer.append(".");
		}

		// finally add the name of the current EDE to the attribute name
		edeAttributeNameBuffer.append(aEde.getName());

		String edeAttributeName = edeAttributeNameBuffer.toString();

		/*
		 * Map the current EDE node to the Entry - add each value of the EDE to
		 * the EDE's attribute in the Entry.
		 */
		List edeValues = aEde.getValues();

		if (null != edeValues) {

			Iterator valuesIter = edeValues.iterator();
			while (valuesIter.hasNext()) {

				aEntry.addAttributeValue(edeAttributeName, valuesIter.next());
			}
		}

		/*
		 * Map into the Entry each of the EDE's children.
		 */
		List edeChildren = aEde.getChildren();

		if (null != edeChildren) {

			aEdeAncestors.push(aEde); // add this EDE to the ancestor list,
			// before recursing over the EDE's
			// children

			Iterator childrenIter = edeChildren.iterator();
			while (childrenIter.hasNext()) {

				ExtendedDataElement edeChild = (ExtendedDataElement) childrenIter
						.next();
				mapEDEToEntry(edeChild, aEntry, aEdeAncestors);
			}

			aEdeAncestors.pop(); // restore the ancestors list to its initial
			// state
		}

	}

}
