/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.List;

/**
 * The configuration for a Link Criteria in a Connector.
 *
 */
public interface LinkCriteriaConfig extends BaseConfiguration {

	public List<String> getCriteriaNames();

	public BaseConfiguration getCriteria();

	public LinkCriteriaItem getCriteria(Object name);

	public void removeCriteria(Object name);

	public void setCriteria(LinkCriteriaItem item);

	public LinkCriteriaItem newCriteria(Object name) throws Exception;

	public boolean isCriteriaLocal(Object name);

	public String getAdvancedLinkCriteria();

	public void setAdvancedLinkCriteria(String script);

	public boolean getAdvancedLinkMode();

	public void setAdvancedLinkMode(boolean advanced);

	public boolean getMatchAny();

	public void setMatchAny(boolean value);
}
