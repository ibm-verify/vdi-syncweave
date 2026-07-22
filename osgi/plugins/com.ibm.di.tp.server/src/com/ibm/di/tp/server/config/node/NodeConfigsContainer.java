/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.config.node;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.ibm.di.tp.server.Constants;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> This class is for internal usage only. Any dependency from the
 * end-user will not be supported. Changes to this class will happen without a
 * warning.
 * 
 * @since 7.1
 */
@XmlType(namespace = Constants.NS_TDI_71_TP)
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeConfigsContainer implements Cloneable {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	@XmlElement(name = "nodeConfig", namespace = Constants.NS_TDI_71_TP)
	private List<NodeConfig> nodeConfigs;

	@XmlElement(name = "tdiNodeConfig", namespace = Constants.NS_TDI_71_TP)
	private List<TdiNodeConfig> tdiNodeConfigs;

	/**
	 * @return the nodeConfigs
	 */

	public List<NodeConfig> getNodeConfigs() {
		if (nodeConfigs == null) {
			nodeConfigs = new ArrayList<NodeConfig>();
		}
		return nodeConfigs;
	}

	/**
	 * @return the tdiNodeConfigs
	 */

	public List<TdiNodeConfig> getTdiNodeConfigs() {
		if (tdiNodeConfigs == null) {
			tdiNodeConfigs = new ArrayList<TdiNodeConfig>();
		}
		return tdiNodeConfigs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public NodeConfigsContainer clone() {
		try {
			NodeConfigsContainer clone = (NodeConfigsContainer) super.clone();
			clone.nodeConfigs = cloneList(nodeConfigs);
			clone.tdiNodeConfigs = cloneList(tdiNodeConfigs);
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T extends NodeConfig> List<T> cloneList(List<T> src) {
		List<T> clone = null;
		if (src != null) {
			clone = new ArrayList<T>(src.size());
			for (T elem : src) {
				clone.add((T) elem.clone());
			}
		}

		return clone;
	}
}
