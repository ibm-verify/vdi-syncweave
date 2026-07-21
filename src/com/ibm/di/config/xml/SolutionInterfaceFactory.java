/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.xml;

import org.w3c.dom.Element;

import com.ibm.di.config.interfaces.*;
import com.ibm.di.config.base.InternalSchema;

/**
 * Read/Write {@link SolutionInterface} elements in XML.
 */
public class SolutionInterfaceFactory extends Factories {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	public final static String SOLUTION_INTERFACE_TAG = "SolutionInterface";

	public final static String EXPOSED_PROPERTY_TAGS = "ExposedProperties";

	public final static String EXPOSED_PROPERTY_TAG = "ExposedProperty";

	private final static String EXPOSED_ASSEMBLYLINES_TAG = "ExposedAssemblylines";

	private final static String HEALTH_ASSEMBLYLINE_TAG = "HealthAssemblyLine";

	private static final String HEALTH_ASSEMBLYLINE_POLL_TAG = "PollInterval";

	private static final String INSTANCE_ID_TAG = "InstanceID";

	private static final String ENABLED_TAG = "enabled";

	/**
	 * {@inheritDoc}
	 */
	public void build(BaseConfiguration config, Element element)
			throws Exception {
		setBaseName(config, element);
		if (config instanceof ExposedProperty) {
			setParameters(element, config, null);
		} else {
			SolutionInterface si = (SolutionInterface) config;

			// -- Health AL
			setSingleElement(element, HEALTH_ASSEMBLYLINE_TAG, si,
					InternalSchema.SI_EXP_HEALTH);

			// -- Poll Interval
			setSingleElement(element, HEALTH_ASSEMBLYLINE_POLL_TAG, si,
					InternalSchema.SI_EXP_HEALTH_POLL);

			// -- Instance ID
			setSingleElement(element, INSTANCE_ID_TAG, si,
					InternalSchema.SI_INSTANCE_ID);

			// -- Enabled
			setSingleElement(element, ENABLED_TAG, si, InternalSchema.ENABLED);

			// -- Description
			setSingleElement(element, Factories.USER_COMMENT_ATTRIBUTE, si,
					InternalSchema.USER_COMMENT);

			// -- AssemblyLines
			ContainerConfig cc = si.getExposedAssemblyLines();
			if (cc != null && cc.size() > 0) {
				Element e = element.getOwnerDocument().createElement(
						EXPOSED_ASSEMBLYLINES_TAG);
				element.appendChild(e);
				Factories.getFactory(ContainerFactory.CONTAINER_TAG).build(cc,
						e);
			}

			// -- Properties
			cc = si.getExposedProperties();
			if (cc != null && cc.size() > 0) {
				Element e = element.getOwnerDocument().createElement(
						EXPOSED_PROPERTY_TAGS);
				element.appendChild(e);
				Factories.getFactory(ContainerFactory.CONTAINER_TAG).build(cc,
						e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void parse(BaseConfiguration config, Element element)
			throws Exception {
		Element e;

		if (config instanceof ExposedProperty) {
			getParameters(element, config);
		} else {
			SolutionInterface si = (SolutionInterface) config;

			getBaseName(config, element);

			// -- Health AL
			si.setHealthAssemblyLine(getNodeTextByName(element,
					HEALTH_ASSEMBLYLINE_TAG));

			// -- Poll Interval
			if (getNodeTextByName(element, HEALTH_ASSEMBLYLINE_POLL_TAG) != null)
				si.setHealthPollInterval(Integer
						.valueOf(
								getNodeTextByName(element,
										HEALTH_ASSEMBLYLINE_POLL_TAG))
						.intValue());

			// -- Instance ID
			si.setInstanceID(getNodeTextByName(element, INSTANCE_ID_TAG));

			// -- Enabled
			si.setEnabled(Boolean.valueOf(
					getNodeTextByName(element, ENABLED_TAG)).booleanValue());

			// -- Description
			if (getNodeTextByName(element, Factories.USER_COMMENT_ATTRIBUTE) != null)
				si.setUserComment(getNodeTextByName(element,
						Factories.USER_COMMENT_ATTRIBUTE));

			// -- AssemblyLines
			if ((e = getSingleElement(element, EXPOSED_ASSEMBLYLINES_TAG)) != null)
				Factories.getFactory(ContainerFactory.CONTAINER_TAG).parse(
						si.getExposedAssemblyLines(), e);

			// -- Properties
			if ((e = getSingleElement(element, EXPOSED_PROPERTY_TAGS)) != null)
				Factories.getFactory(ContainerFactory.CONTAINER_TAG).parse(
						si.getExposedProperties(), e);
		}
	}

}
