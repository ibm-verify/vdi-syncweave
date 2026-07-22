/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.providers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.AttributeMapConfig;
import com.ibm.di.config.interfaces.AttributeMapItem;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.LoopConfig;
import com.ibm.tdi.eclipse.Utils;

public class WorkEntryAttributesProvider implements IStructuredContentProvider, ITreeContentProvider {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private String untilComponent;
	private Object[] workAttributes = new Object[]{};
	private List<String> sortedAttributes;
	
	private AssemblyLineConfig config;
	
	public WorkEntryAttributesProvider(String untilComponent) {
		super();
		this.untilComponent = untilComponent;
	}

	public String getUntilComponent() {
		return untilComponent;
	}

	public void setUntilComponent(String untilComponent) {
		this.untilComponent = untilComponent;
		build();
	}

	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof AssemblyLineConfig)
			return workAttributes;

		return null;
	}

	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof AssemblyLineConfig)
			return workAttributes;
		
		return null;
	}

	public Object getParent(Object element) {
		if(element instanceof BaseConfiguration)
			return ((BaseConfiguration)element).getParent();
		else
			return null;
	}

	public boolean hasChildren(Object element) {
		return false;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof AssemblyLineConfig) {
			config = (AssemblyLineConfig)newInput;
			build();
		}
	}

	private void build() {
		sortedAttributes = buildTable()[0];
		workAttributes = sortedAttributes.toArray(); // Unsorted, do we want to sort first?
		Collections.sort(sortedAttributes);
	}
	
	public List<String> getSortedAttributes() {
		return sortedAttributes;
	}
	
	/*
	 *  Description of the Method
	 *
	 * @param  allConnectors   Should all connectors be included, or only connectors in the Feed folder
	 * @param  untilComponent  Include all connectors up to, but not including, this connector
	 * @return                 an ArrayList[], first element is Attribute names, the other is where those names stem from
	 */
	public ArrayList<String>[] buildTable() {

		ArrayList<String> namelist = new ArrayList<String>();
		ArrayList<String> namelistic = new ArrayList<String>();
		ArrayList<String> source = new ArrayList<String>();
		ArrayList<String> consume = new ArrayList<String>();
		ArrayList<String> consumers = new ArrayList<String>();

		try {
			// Add input parameters from AL schema 
			AttributeMapConfig map = config.getAttributeMap(true);
			addToLists(namelist, namelistic, source, map.getAttributeNames(), map, "{IWE}", null);

			// Add attribute names from input connectors
			List<BaseConfiguration> items = config.getEntryFeedComponents().getConfigurations(null);
			config.getDataFlowComponents().getConfigurations(items);
			
			for (BaseConfiguration bc:items) {

				if (untilComponent != null && untilComponent.equals(bc.getShortName()))
					break;

				if (bc instanceof LoopConfig && bc.getEnabled()) {
					LoopConfig lc = (LoopConfig) bc;
					if (lc.getLoopType() == LoopConfig.LOOP_CONNECTOR_FC) {
						// Get loop connector
						bc = lc.getLoopConnector();
					} else if (lc.getLoopType() == LoopConfig.LOOP_COLLECTION){
						String compName = lc.getShortName();
						String name = lc.getLoopAttributeName();
						String icname = name.toLowerCase();
						int index = namelistic.indexOf(icname);

						if (index == -1) {
							source.add(compName);
							namelist.add(name);
							namelistic.add(icname);
						} else {
							String s = source.get(index);
							if (!s.equals(compName) && !s.endsWith(", " + compName))
								source.set(index, s + ", " + compName);
						}
						continue;
					}
				}

				if (!(bc instanceof ConnectorConfig && bc.getEnabled()))
					continue;

				ConnectorConfig cc = (ConnectorConfig) bc;

				if (Utils.isOutputConnector(cc)) {
					
					if (config.autoMapAllAttributes(cc.getName())) {
						// Consumes all work attrs 
						List<?> schemaItems = cc.getSchema(false).getItemNames();
						if (schemaItems.size() == 0)
							schemaItems = namelist;
						addToConsumeLists(consume, consumers, schemaItems,
								null, cc.getShortName(), cc);
					} else {
						// Consume work attrs from attr map
						map = cc.getAttributeMap(false);
						addToConsumeLists(consume, consumers, map.getAttributeNames(), map, cc.getShortName(), cc);
					}
				}
				
				if (Utils.isInputConnector(cc)) {

					if (config.autoMapAllAttributes(cc.getName())) {
						addToLists(namelist, namelistic, source, cc.getSchema(true).getItemNames(),
								null, cc.getShortName(), cc);
					} else {
						map = cc.getAttributeMap(true);
						addToLists(namelist, namelistic, source, map.getAttributeNames(), map, cc.getShortName(), cc);
					}
				}

			}

		} catch (Exception error) {
			error.printStackTrace();
		}

		return new ArrayList[] { namelist, source, consume, consumers };
	}

	private void addToConsumeLists(ArrayList<String> namelist, ArrayList<String> source, List<?> list,
			AttributeMapConfig map, String cName, ConnectorConfig cc) {

		for (int i = 0; i < list.size(); i++) {
			String name = list.get(i).toString();
			if (map != null) {
				AttributeMapItem ami = map.getAttributeMapItem(name);
				if (ami != null && !ami.getEnabled())
					continue;
			}

			if (name.equals("*") && cc != null) {
				addToConsumeLists(namelist, source, cc.getSchema(false).getItemNames(), null,
						cName, null);
				continue;
			}

			source.add(cName);
			namelist.add(name);
		}
	}
	
	private void addToLists(ArrayList<String> namelist, ArrayList<String> namelistic, ArrayList<String> source, List<?> list,
			AttributeMapConfig map, String cName, ConnectorConfig cc) {

		for (int i = 0; i < list.size(); i++) {
			String name = list.get(i).toString();
			if (map != null) {
				AttributeMapItem ami = map.getAttributeMapItem(name);
				if (ami != null && !ami.getEnabled())
					continue;
			}

			if (name.equals("*") && cc != null) {
				addToLists(namelist, namelistic, source, cc.getSchema(true).getItemNames(), null,
						cName, null);
				continue;
			}

			String icname = name.toLowerCase();
			int index = namelistic.indexOf(icname);

			if (index == -1) {
				source.add(cName);
				namelist.add(name);
				namelistic.add(icname);
			} else {
				String s = source.get(index);
				if (!s.equals(cName) && !s.endsWith(", " + cName))
					source.set(index, s + ", " + cName);
			}
		}
	}
}

