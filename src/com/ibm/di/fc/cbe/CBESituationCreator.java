/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.cbe;

import org.eclipse.hyades.logging.events.cbe.AvailableSituation;
import org.eclipse.hyades.logging.events.cbe.ConfigureSituation;
import org.eclipse.hyades.logging.events.cbe.ConnectSituation;
import org.eclipse.hyades.logging.events.cbe.CreateSituation;
import org.eclipse.hyades.logging.events.cbe.DependencySituation;
import org.eclipse.hyades.logging.events.cbe.DestroySituation;
import org.eclipse.hyades.logging.events.cbe.EventFactory;
import org.eclipse.hyades.logging.events.cbe.FeatureSituation;
import org.eclipse.hyades.logging.events.cbe.OtherSituation;
import org.eclipse.hyades.logging.events.cbe.ReportSituation;
import org.eclipse.hyades.logging.events.cbe.RequestSituation;
import org.eclipse.hyades.logging.events.cbe.Situation;
import org.eclipse.hyades.logging.events.cbe.SituationType;
import org.eclipse.hyades.logging.events.cbe.StartSituation;
import org.eclipse.hyades.logging.events.cbe.StopSituation;

import com.ibm.di.entry.Entry;
import com.ibm.di.server.ResourceHash;

/**
 * This is a helper class for the CBEGeneratorFC Function component. It creates
 * the Situation object for the CommonBaseEvent based on the
 * situation.CategoryName attribute entered by the user.
 * 
 */
public class CBESituationCreator {
	/**
	 * Component properties.
	 */
	private static final String PROPERTIES_FILE = "CBEGeneratorFC";

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * {@link Entry}
	 */
	private Entry entry;

	/**
	 * Error message.
	 */
	private String tmsMessage = null;

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash sResHash = null;

	static {
		sResHash = new ResourceHash(PROPERTIES_FILE);
	}

	/**
	 * Class constructor
	 * 
	 * @param p_entry
	 *            {@link Entry}
	 */
	public CBESituationCreator(Entry p_entry) {
		entry = p_entry;
	}

	/**
	 * Validates the value entered by the user to the predefined value of each
	 * situationType
	 * 
	 * @param categoryName
	 *            situation type
	 * @return boolean
	 * @throws Exception
	 */
	boolean validateSituationType(String categoryName) throws Exception {
		boolean valid = false;

		if (categoryName
				.equalsIgnoreCase(Situation.AVAILABLE_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.CONFIGURE_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.CONNECT_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.CREATE_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.DEPENDENCY_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.DESTROY_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.FEATURE_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.OTHER_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.REPORT_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.REQUEST_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.START_SITUATION_CATEGORY)) {
			valid = true;
		} else if (categoryName
				.equalsIgnoreCase(Situation.STOP_SITUATION_CATEGORY)) {
			valid = true;
		} else {
			tmsMessage = sResHash.getString(
					"FC.CBE_GENERATOR.SITUATION_CATEGORY_NAME.INVALID",
					categoryName);
			throw new Exception(tmsMessage);
		}

		return valid;
	}

	/**
	 * Populates situation object of type AvailableSituation
	 * 
	 * @param availSituation
	 *            {@link Entry}
	 * @return AvailableSituation
	 * @throws Exception :
	 *             never
	 */
	private AvailableSituation createSituation(AvailableSituation availSituation)
			throws Exception {

		availSituation.setOperationDisposition(entry
				.getString("availableSituation.operationDisposition"));
		availSituation.setProcessingDisposition(entry
				.getString("availableSituation.processingDisposition"));
		availSituation.setAvailabilityDisposition(entry
				.getString("availableSituation.availabilityDisposition"));
		return availSituation;

	}

	/**
	 * Populates situation object of type ConfigureSituation
	 * 
	 * @param configureSituation -
	 *            {@link Entry}
	 * @return ConfigureSituation
	 * @throws Exception :
	 *             never
	 */
	private ConfigureSituation createSituation(
			ConfigureSituation configureSituation) throws Exception {
		configureSituation.setSuccessDisposition(entry
				.getString("configureSituation.successDisposition"));
		return configureSituation;

	}

	/**
	 * Populates situation object of type ConnectSituation
	 * 
	 * @param connectSituation -
	 *            {@link Entry}
	 * @return ConnectSituation
	 * @throws Exception :
	 *             never
	 */
	private ConnectSituation createSituation(ConnectSituation connectSituation)
			throws Exception {
		connectSituation.setSuccessDisposition(entry
				.getString("connectSituation.successDisposition"));
		connectSituation.setSituationDisposition(entry
				.getString("connectSituation.situationDisposition"));
		return connectSituation;
	}

	/**
	 * Populates situation object of type CreateSituation
	 * 
	 * @param createSituation -
	 *            {@link Entry}
	 * @return CreateSituation
	 * @throws Exception :
	 *             never
	 */
	private CreateSituation createSituation(CreateSituation createSituation)
			throws Exception {
		createSituation.setSuccessDisposition(entry
				.getString("createSituation.successDisposition"));
		return createSituation;
	}

	/**
	 * Populates situation object of type DependencySituation
	 * 
	 * @param dependencySituation -
	 *            {@link Entry}
	 * @return DependencySituation
	 * @throws Exception :
	 *             never
	 * 
	 */
	private DependencySituation createSituation(
			DependencySituation dependencySituation) throws Exception {
		dependencySituation.setDependencyDisposition(entry
				.getString("dependencySituation.dependencyDisposition"));
		return dependencySituation;
	}

	/**
	 * Populates situation object of type DestroySituation
	 * 
	 * @param destroySituation -
	 *            {@link Entry}
	 * @return DestroySituation
	 * @throws Exception :
	 *             never
	 */
	private DestroySituation createSituation(DestroySituation destroySituation)
			throws Exception {
		destroySituation.setSuccessDisposition(entry
				.getString("destroySituation.successDisposition"));
		return destroySituation;
	}

	/**
	 * Populates situation object of type FeatureSituation
	 * 
	 * @param featureSituation -
	 *            {@link Entry}
	 * @return FeatureSituation
	 * @throws Exception :
	 *             never
	 */
	private FeatureSituation createSituation(FeatureSituation featureSituation)
			throws Exception {
		featureSituation.setFeatureDisposition(entry
				.getString("featureSituation.featureDisposition"));
		return featureSituation;
	}

	/**
	 * Populates situation object of type ReportSituation
	 * 
	 * @param reportSituation -
	 *            {@link Entry}
	 * @return ReportSituation
	 * @throws Exception :
	 *             never
	 */
	private ReportSituation createSituation(ReportSituation reportSituation)
			throws Exception {
		reportSituation.setReportCategory(entry
				.getString("reportSituation.reportCategory"));
		return reportSituation;
	}

	/**
	 * Populates situation object of type RequestSituation
	 * 
	 * @param requestSituation -
	 *            {@link Entry}
	 * @return RequestSituation
	 * @throws Exception :
	 *             never
	 */
	private RequestSituation createSituation(RequestSituation requestSituation)
			throws Exception {
		requestSituation.setSuccessDisposition(entry
				.getString("requestSituation.successDisposition"));
		requestSituation.setSituationQualifier(entry
				.getString("requestSituation.situationQualifier"));
		return requestSituation;
	}

	/**
	 * Populates situation object of type StartSituation
	 * 
	 * @param startSituation -
	 *            {@link Entry}
	 * @return StartSituation
	 * @throws Exception :
	 *             never
	 */
	private StartSituation createSituation(StartSituation startSituation)
			throws Exception {
		startSituation.setSuccessDisposition(entry
				.getString("startSituation.successDisposition"));
		startSituation.setSituationQualifier(entry
				.getString("startSituation.situationQualifier"));
		return startSituation;
	}

	/**
	 * Populates situation object of type StopSituation
	 * 
	 * @param stopSituation -
	 *            {@link Entry}
	 * @return StopSituation
	 * @throws Exception :
	 *             never
	 */
	private StopSituation createSituation(StopSituation stopSituation)
			throws Exception {
		stopSituation.setSuccessDisposition(entry
				.getString("stopSituation.successDisposition"));
		stopSituation.setSituationQualifier(entry
				.getString("stopSituation.situationQualifier"));
		return stopSituation;
	}

	/**
	 * Populates situation object of type OtherSituation
	 * 
	 * @param otherSituation -
	 *            {@link Entry}
	 * @return OtherSituation
	 * @throws Exception :
	 *             never
	 */
	private OtherSituation createSituation(OtherSituation otherSituation)
			throws Exception {
		otherSituation.setAny(entry.getString("otherSituation.any"));
		return otherSituation;
	}

	/**
	 * This method creates object of SituationType depending on the value of
	 * situation.CategoryName given by the user.
	 * 
	 * @param eventFactory -
	 *            {@link Entry}
	 * @return {@link SituationType} instance
	 * @throws Exception
	 */
	SituationType getSituation(EventFactory eventFactory) throws Exception {

		SituationType situationType = null;
		String situationVal = entry.getString("situation.CategoryName");

		if (situationVal
				.equalsIgnoreCase(Situation.AVAILABLE_SITUATION_CATEGORY)) {
			AvailableSituation availSituation = eventFactory
					.createAvailableSituation();
			situationType = createSituation(availSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.CONFIGURE_SITUATION_CATEGORY)) {
			ConfigureSituation configureSituation = eventFactory
					.createConfigureSituation();
			situationType = createSituation(configureSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.CONNECT_SITUATION_CATEGORY)) {
			ConnectSituation connectSituation = eventFactory
					.createConnectSituation();
			situationType = createSituation(connectSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.CREATE_SITUATION_CATEGORY)) {
			CreateSituation createSituation = eventFactory
					.createCreateSituation();
			situationType = createSituation(createSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.DEPENDENCY_SITUATION_CATEGORY)) {
			DependencySituation dependencySituation = eventFactory
					.createDependencySituation();
			situationType = createSituation(dependencySituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.DESTROY_SITUATION_CATEGORY)) {
			DestroySituation destroySituation = eventFactory
					.createDestroySituation();
			situationType = createSituation(destroySituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.FEATURE_SITUATION_CATEGORY)) {
			FeatureSituation featureSituation = eventFactory
					.createFeatureSituation();
			situationType = createSituation(featureSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.OTHER_SITUATION_CATEGORY)) {
			OtherSituation otherSituation = eventFactory.createOtherSituation();
			situationType = createSituation(otherSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.REPORT_SITUATION_CATEGORY)) {
			ReportSituation reportSituation = eventFactory
					.createReportSituation();
			situationType = createSituation(reportSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.REQUEST_SITUATION_CATEGORY)) {
			RequestSituation requestSituation = eventFactory
					.createRequestSituation();
			situationType = createSituation(requestSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.START_SITUATION_CATEGORY)) {
			StartSituation startSituation = eventFactory.createStartSituation();
			situationType = createSituation(startSituation);
		} else if (situationVal
				.equalsIgnoreCase(Situation.STOP_SITUATION_CATEGORY)) {
			StopSituation stopSituation = eventFactory.createStopSituation();
			situationType = createSituation(stopSituation);
		}

		if (situationType != null) {
			situationType.setReasoningScope(entry
					.getString("situation.reasoningScope"));
		}

		return situationType;
	}

}
