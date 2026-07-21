/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * 
 */
package com.ibm.di.fc.sapr3rfcv3;

import java.util.Properties;
import java.util.Hashtable;
import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;

public class SapDestinationDataProvider {
	private static SapDDP destinationDataProvider;

	private static class SapDDP implements DestinationDataProvider {
		private DestinationDataEventListener eL;

		private Hashtable<String, Properties> propertiesTab = new Hashtable<String, Properties>();

		public Properties getDestinationProperties(String destinationName) {
//			System.out.println("Requesting props for " + destinationName);
			synchronized(propertiesTab) {
				if (propertiesTab.containsKey(destinationName)) {
					return propertiesTab.get(destinationName);
				}
			}

			throw new RuntimeException("Destination " + destinationName
					+ " is not available");
		}

		public void setDestinationDataEventListener(
				DestinationDataEventListener eventListener) {
			this.eL = eventListener;
		}

		public boolean supportsEvents() {
			return true;
		}

		void changeProperties(Properties pConProps) {
			if (pConProps.getProperty("ACTION").equalsIgnoreCase("CREATE")) {
				synchronized(propertiesTab) {
					propertiesTab.put(pConProps.getProperty("jco.client.dest"),
							pConProps);
				}
				if (eL != null) {
					eL.updated(pConProps.getProperty("jco.client.dest"));
				}
			} else if (pConProps.getProperty("ACTION").equalsIgnoreCase(
					"DELETE")) {
				synchronized(propertiesTab) {
					propertiesTab.remove(pConProps.getProperty("jco.client.dest"));
				}
				if (eL != null) {
					eL.deleted(pConProps.getProperty("jco.client.dest"));
				}
			}
		}
	}

	public SapDestinationDataProvider() {
		if (destinationDataProvider == null) {
			destinationDataProvider = new SapDDP();
			Environment
					.registerDestinationDataProvider(destinationDataProvider);
		}
	}

	public void changeProperties(String operation, String destinationName,
			Properties pConProps) {
		if (pConProps == null) {
			pConProps = new Properties();
		}
		pConProps.setProperty("jco.client.dest", destinationName);
		if (operation.toUpperCase().startsWith("DELETE")) {
			pConProps.setProperty("ACTION", "DELETE");
//			System.out.println("Deleting props for " + destinationName);
		} else {
			pConProps.setProperty("ACTION", "CREATE");
//			System.out.println("Changing props for " + destinationName);
		}
		destinationDataProvider.changeProperties(pConProps);
	}
	
	public void unregister(String destinationName) {
		changeProperties("DELETE", destinationName, null);
	}

}
