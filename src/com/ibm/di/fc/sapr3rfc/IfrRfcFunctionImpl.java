/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.sapr3rfc;

import com.sap.mw.jco.JCO;
import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.IRepository;

/**
 * 
 */
/*
 * @modelguid {8BE8CE55-40AF-43E0-8FB4-96F980D2B29D}
 */
final class IfrRfcFunctionImpl implements IfrRfcFunction {

	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/* @modelguid {969B2EA5-5921-4483-A658-B0C1D3029B3C} */
	private JCO.Function jcoFunc;

	/* @modelguid {3520A107-892F-4ECD-B9E2-F56B61013362} */
	private IRepository funcRepos;

	/* @modelguid {50CA3712-B783-4F5A-9DD3-9E842D5E19AA} */
	IfrRfcFunctionImpl(IRepository repos) {
		super();
		setRepository(repos);
	}

	/* @modelguid {95C07852-68A5-49BF-ADE2-BFBA8D10651E} */
	private void setRepository(IRepository repos) {
		funcRepos = repos;
	}

	/* @modelguid {ECFCB561-9286-471A-83F8-AF5387717383} */
	private IRepository getRepository() {
		return funcRepos;
	}

	/* @modelguid {A4F681D0-65D1-413A-817C-C0C61918DB06} */
	private void setFunctionImpl(JCO.Function f) {
		jcoFunc = f;
	}

	/* @modelguid {9B894408-50E7-4708-9638-F83C11AD2DD6} */
	private JCO.Function getFunctionImpl() {
		return jcoFunc;
	}

	/* @modelguid {50D1CD50-8928-4660-9872-5094EFFC5D4A} */
	public void importRequestData(IfrImporter importer)
			throws SapRfcFunctionException {

		try {
			IFunctionTemplate funcTemplate = getRepository()
					.getFunctionTemplate(importer.getFunctionName());
			if (funcTemplate == null) {
				Object[] msgArgs = new Object[] { importer.getFunctionName() };
				String msg = LogMessageHelper.getMsgResource().getMessage(
						LogMessageHelper.SAPR3_RFCFC_0013, msgArgs);
				throw new SapRfcFunctionException(msg);
			}
			setFunctionImpl(funcTemplate.getFunction());
			importer.importData(getFunctionImpl());
		} catch (IfrImporterException x) {
			throw new SapRfcFunctionException(x.getMessage(), x);
		}
	}

	/* @modelguid {E6401D8B-867C-47C4-8D8F-EFCBE7816419} */
	public void execute(JCO.Client conn) {
		conn.execute(getFunctionImpl());
	}

	/* @modelguid {2BCCD403-A54C-47C5-B0CA-A4ACBAD18937} */
	public void exportResponseData(IfrSerializer serializer)
			throws SapRfcFunctionException {
		serializer.serialize(getFunctionImpl());
	}
}
