/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.dpa.provider;

/**
 * 
 * @author yavor.gologanov
 *
 */
public class DeployedAssetsSchema {

	public static final String CLASS_PROPERTY_NAME = "Assetclass";	
	public static final String CLASS_COLUMN_NAME = "ASSETCLASS";	
	
	public static final String CLASS_NAME_COMPUTER = "COMPUTER";
	public static final String CLASS_NAME_NETDEVICE = "NETDEVICE";
	public static final String CLASS_NAME_NETPRINTER = "NETPRINTER";	
	
	public static final String MANUFACTURER = "Manufacturer";
	public static final String DEPLOYED_ASSET = "DeployedAsset";	
	public static final String COMPUTER = "Computer";
	public static final String NETWORK_DEVICE = "NetworkDevice";
	public static final String NETWORK_PRINTER = "NetworkPrinter";	
	public static final String COMMUNICATION = "Communication";
	public static final String DPAANCESTOR = "DPAAncestor";
	public static final String DISPLAY = "Display";
	public static final String FILE = "File";
	public static final String IPX = "IPX";
	public static final String IMAGE_DEVICE = "ImageDevice";
	public static final String LOGICAL_DRIVE = "LogicalDrive";
	public static final String MEDIA_ADAPTER = "MediaAdapter";
	public static final String NETWORK_ADAPTER = "NetworkAdapter";
	public static final String OPERATING_SYSTEM = "OperatingSystem";
	public static final String PVUDATA = "PVUData";
	public static final String PHISYCAL_DISK = "PhysicalDisk";
	public static final String PROCESSOR = "Processor";
	public static final String SOFTWARE = "Software";
	public static final String TCPIP = "TCPIP";
	public static final String USER = "User";
	public static final String NETWORK_CARD = "NetworkCard";
	public static final String ADAPTER_VARIANT = "AdapterVariant";
	public static final String OPERATING_SYSTEM_VARIANT = "OperatingSystemVariant";
	public static final String SOFTWARE_PRODUCT = "SoftwareProduct";
	public static final String PROCESSOR_VARIANT = "ProcessorVariant";

	/**
	 * 
	 * @param className
	 * @return String
	 */
	public static String getAssetClass(String className) {
		if (CLASS_NAME_COMPUTER.equals(className)) {
			return DeployedAssetsSchema.COMPUTER;
		} else if (CLASS_NAME_NETDEVICE.equals(className)) {
			return DeployedAssetsSchema.NETWORK_DEVICE;
		} else if (CLASS_NAME_NETPRINTER.equals(className)) {
			return DeployedAssetsSchema.NETWORK_PRINTER;
		}
		
		return DeployedAssetsSchema.DEPLOYED_ASSET;
	}
	
	/**
	 * 
	 * @param assetClass
	 * @return String
	 */
	public static String getClassName(String assetClass) {
		if (DeployedAssetsSchema.COMPUTER.equals(assetClass)) {
			return CLASS_NAME_COMPUTER;
		} else if (DeployedAssetsSchema.NETWORK_DEVICE.equals(assetClass)) {
			return CLASS_NAME_NETDEVICE;
		} else if (DeployedAssetsSchema.NETWORK_PRINTER.equals(assetClass)) {
			return CLASS_NAME_NETPRINTER;
		}
		
		return null;
	}		
	
}
