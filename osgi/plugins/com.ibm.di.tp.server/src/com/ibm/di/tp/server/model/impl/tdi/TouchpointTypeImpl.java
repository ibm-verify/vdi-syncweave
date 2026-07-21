/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.tp.server.model.impl.tdi;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NameAlreadyBoundException;

import com.ibm.di.model.descriptor.ComponentDescriptor;
import com.ibm.di.model.descriptor.ConnectorDescriptor;
import com.ibm.di.model.descriptor.Label;
import com.ibm.di.model.descriptor.ModeOption;
import com.ibm.di.model.descriptor.Option;
import com.ibm.di.model.descriptor.ParameterDescriptor;
import com.ibm.di.tp.server.Constants;
import com.ibm.di.tp.server.model.TouchpointInstance;
import com.ibm.di.tp.server.model.TouchpointRole;
import com.ibm.di.tp.server.model.TouchpointType;
import com.ibm.di.tp.server.model.config.InstanceData;
import com.ibm.di.tp.server.model.config.PropertyDefinition;
import com.ibm.di.tp.server.model.config.PropertySheetDefinition;
import com.ibm.di.tp.server.model.exception.ErrorCode;
import com.ibm.di.tp.server.model.exception.SCMPException;

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
public class TouchpointTypeImpl implements TouchpointType {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.CopyRight.OBJECT_CODE;

	private final String id;

	private final ConnectivityProviderImpl cp;

	private final Map<String, TouchpointInstanceImpl> instances = new ConcurrentHashMap<String, TouchpointInstanceImpl>();

	private SoftReference<PropertySheetDefinition> sheetDefRef;

	/**
	 * @param typeId
	 * @param connType
	 * @param cp
	 */
	public TouchpointTypeImpl(String typeId, ConnectivityProviderImpl cp) {
		this.id = typeId;
		this.cp = cp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointType#createInstance(java.util.Map)
	 */
	public TouchpointInstance createInstance(String instId, TouchpointRole role, InstanceData cfg) throws SCMPException {
		if (!getSupportedRoles().contains(role)) {
			throw new IllegalArgumentException(role.toString());
		}
		if (instances.containsKey(instId)) {
			throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, "", -1, new NameAlreadyBoundException());
		}

		TouchpointInstanceImpl inst = new TouchpointInstanceImpl(instId, role, cfg, this);
		instances.put(instId, inst);
		return inst;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointType#disposeInstance(com.ibm.di.
	 * tp.server.model.TouchpointInstance)
	 */
	public void disposeInstance(String instId) throws SCMPException {
		TouchpointInstanceImpl inst = instances.remove(instId);
		if (inst != null) {
			try {
				inst.stopTouchpoint();
			} catch (Exception e) {
				throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, e.getMessage(), -1, e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointType#getId()
	 */
	public String getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointType#getInstances()
	 */
	public Collection<TouchpointInstance> getInstances() {
		Set<TouchpointInstance> set = new HashSet<TouchpointInstance>();
		for (TouchpointInstanceImpl inst : instances.values()) {
			set.add(inst);
		}
		return set;
	}

	ConnectivityProviderImpl getConnectivityProviderImpl() {
		return cp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.ibm.di.tp.server.model.TouchpointType#getSupportedRoles()
	 */
	public Collection<TouchpointRole> getSupportedRoles() throws SCMPException {
		TemplateConfigLoader tcl = ConnectivityProviderImpl.getConfigLoader();
		List<TouchpointRole> supportedRoles;
		try {
			supportedRoles = tcl.getSupportedRolesByTemplate(getConfigTemplate());
		} catch (Exception e) {
			throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, e.getMessage(), 500, e);
		}

		switch (TouchpointTypeScheme.fromType(this)) {
		case SYSTEM:
			// no system type should allow Intermediary Role
			supportedRoles.remove(TouchpointRole.INTERMEDIARY);
			break;
		case FILE:
			// all TP roles are supported for custom templates
			break;
		case VIRTUAL:
			// for now, only Intermediary role is supported for virtual type
			supportedRoles.remove(TouchpointRole.PROVIDER);
			supportedRoles.remove(TouchpointRole.INITIATOR);
			break;
		default:
			throw new RuntimeException();
		}

		return Collections.unmodifiableList(supportedRoles);
	}

	File getConfigTemplate() {
		return cp.getTypeLocator().getConfigTemplateForType(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointType#hasPropertySheetDefinition()
	 */
	public boolean hasPropertySheetDefinition() {
		switch (TouchpointTypeScheme.fromType(this)) {
		case FILE:
		case SYSTEM:
			return true;
		default:
			return false;
		}
	}

	private String getServiceConnectorName() throws Exception {
		switch (TouchpointTypeScheme.fromType(this)) {
		case FILE:
			return ConnectivityProviderImpl.getConfigLoader().getServiceConnectorInheritanceRef(
					cp.getTypeLocator().getConfigTemplateForType(this));
		case SYSTEM:
			return getId();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ibm.di.tp.server.model.TouchpointType#getPropertySheetDefinition()
	 */
	public PropertySheetDefinition getPropertySheetDefinition() throws SCMPException {
		PropertySheetDefinition hardRef = null;
		if (sheetDefRef == null || (hardRef = sheetDefRef.get()) == null) {
			try {
				hardRef = generatePropertySheetDefinition();
			} catch (Exception e) {
				throw new SCMPException(ErrorCode.CONNECTIVITY_UNKNOWN, e.getMessage(), -1, e);
			}
			if (hardRef != null) {
				sheetDefRef = new SoftReference<PropertySheetDefinition>(hardRef);
			}
		}

		return hardRef;
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private PropertySheetDefinition generatePropertySheetDefinition() throws Exception {
		String srvcConnName = getServiceConnectorName();
		if (srvcConnName != null) {
			ComponentDescriptor cDef = cp.getSession().getServerInfo().getInstalledComponentDescriptor(srvcConnName);

			PropertySheetDefinition psd = new PropertySheetDefinition();
			List<PropertyDefinition> props = psd.getPropertyDefinition();
			PropertyDefinition prop = null;
			com.ibm.di.tp.server.model.config.Option opt = null;
			for (ParameterDescriptor param : cDef.getParameterMapDescriptor().getParameterDescriptors()) {
				prop = new PropertyDefinition();
				prop.setHidden(param.isHidden());
				prop.setMultiple(false);
				prop.setPropertyName(param.getKey());
				prop.setPropertyType(param.getType());
				prop.setReadonly(false);
				prop.setRequired(param.isRequired());
				
				for (Label paramLbl : param.getLabels()) {
					prop.getLabel().add(new com.ibm.di.tp.server.model.config.Label(paramLbl.getValue(), paramLbl.getLang()));
				}
				
				if (param.getDefaultValue() != null) {
					prop.getDefaultValue().add(param.getDefaultValue());
				}
				for (Option paramOpt : param.getOptions()) {
					opt = new com.ibm.di.tp.server.model.config.Option();
					opt.setValue(paramOpt.getValue());
					for (Label paramLbl : paramOpt.getLabels()) {
						opt.getLabel().add(new com.ibm.di.tp.server.model.config.Label(paramLbl.getValue(), paramLbl.getLang()));
					}
					prop.getOption().add(opt);
				}
				props.add(prop);
			}

			if (cDef instanceof ConnectorDescriptor) {
				ConnectorDescriptor connDesc = (ConnectorDescriptor) cDef;

				if (connDesc.getSupportedModes().size() > 0) {
					prop = new PropertyDefinition();
					prop.setRequired(false);
					prop.setMultiple(false);
					prop.setReadonly(false);
					prop.setPropertyName(Constants.PROP_INIT_MODE);
					prop.setPropertyType("string");

					for (com.ibm.di.tp.server.model.config.Label l : Constants.LABELS_INIT_MODE) {
						prop.getLabel().add(new com.ibm.di.tp.server.model.config.Label(l.getLabel(), l.getLang()));
					}

					for (ModeOption mode : connDesc.getSupportedModes()) {
						opt = new com.ibm.di.tp.server.model.config.Option();
						opt.setValue(mode.getValue().value());
						for (Label modeLbl : mode.getLabels()) {
							opt.getLabel().add(new com.ibm.di.tp.server.model.config.Label(modeLbl.getValue(), modeLbl.getLang()));
						}
						prop.getOption().add(opt);
					}
					props.add(prop);
				}
			}
			return psd;
		}

		return null;
	}
}
