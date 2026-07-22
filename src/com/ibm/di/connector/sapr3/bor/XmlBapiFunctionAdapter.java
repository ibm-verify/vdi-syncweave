/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.sapr3.bor;

/**
 * Adatper for processing BAPI RFC methods. The main extension functionaity
 * added is the processing of the "RETURN" parameter present on all BAPI
 * compliant RFC methods defiend in SAP.
 * 
 */
interface XmlBapiFunctionAdapter extends XmlFunctionAdapter {

	/**
	 * Get the error level messages present in the RETURN parameter of the RFC.
	 * 
	 * @return The error messages. Length == zero if no errors.
	 */
	AbapErrorInfo[] getErrorMessages();

	/**
	 * Get the warning level messages present in the RETURN parameter of the
	 * RFC.
	 * 
	 * @return The warning messages. Length == zero if no warnings.
	 */
	AbapErrorInfo[] getWarningMessages();

}
