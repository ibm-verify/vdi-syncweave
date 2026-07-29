/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import com.sap.mw.jco.JCO;

/**
 * Serializes a JCO.Function object into a persistent format. Typically used
 * after the function has been executed so that response data can be extracted.
 * 
 * Known implementations: {@link IfrXmlSerializerImpl},
 * {@link IfrEntrySerializerImpl}
 */
interface IfrSerializer {

	String RESPONSE_SUFFIX = ".Response";

	String ITEM = "item";

	/**
	 * Serialize the function.
	 * 
	 * @param func
	 * @modelguid {2E25F363-761E-4485-A6DE-7363CDF4889A}
	 */
	void serialize(JCO.Function func);

}
