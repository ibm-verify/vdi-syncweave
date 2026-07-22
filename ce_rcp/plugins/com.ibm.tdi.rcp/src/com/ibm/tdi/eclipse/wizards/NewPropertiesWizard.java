/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.tdi.eclipse.wizards;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.ui.INewWizard;

import com.ibm.di.config.base.ContainerConfigImpl;
import com.ibm.di.config.base.PropertyManagerImpl;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.tdi.eclipse.Messages;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;
import com.ibm.tdi.eclipse.validators.IllegalCharValidator;

public class NewPropertiesWizard extends NewComponentBaseWizard implements
		INewWizard {
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private final static String ILLEGAL_NAME_CHARS = "[]\\/.:{}*?|";
	
	/**
	 * Validator for property store names
	 */
	public final static IInputValidator VALIDATOR = new IllegalCharValidator(ILLEGAL_NAME_CHARS);
	
	public NewPropertiesWizard() {
		super("properties", MetamergeConfig.DEFAULT_PROPERTY_FOLDER, "miadmin.menu.Object.NewPropertyStore.label"); //$NON-NLS-1$ //$NON-NLS-2$
		setExtension(".tdiproperties"); //$NON-NLS-1$
		setShowTypes(false);
		setNameValidator(VALIDATOR);
	}

	@Override
	public void createConfigObject() {
		try {
			ContainerConfigImpl cc = new ContainerConfigImpl();
			cc.init();

			ContainerConfigImpl data = new ContainerConfigImpl();
			data.init();
			data.setName("Data"); //$NON-NLS-1$
			cc.addConfig(data);

			PropertyManager pm = new PropertyManagerImpl();
			pm.init();
			pm.setName("Config"); //$NON-NLS-1$
			cc.addConfig(pm);

			PropertyStoreConfig psc = new com.ibm.di.config.base.PropertyStoreConfigImpl();
			psc.init();

			RawConnectorConfig rcc = psc.getConnectionConfig();
			rcc.setParent(psc);
			rcc.setParameter("collectionType", "Default"); //$NON-NLS-1$ //$NON-NLS-2$
			rcc.setInheritsFromRef("system:/Connectors/ibmdi.Properties"); //$NON-NLS-1$

			psc.setName("Default"); //$NON-NLS-1$
			psc.setKeyAttribute("key"); //$NON-NLS-1$
			psc.setValueAttribute("value"); //$NON-NLS-1$
			psc.setInitialLoad(true);

			pm.addPropertyStore(psc);

			setConfigObject(cc);
		} catch (Exception e) {
			EclipseAppender.logerror("Error", e, getShell()); //$NON-NLS-1$
		}
	}

	@Override
	protected void updateConfigObject() {
		String name = super.getName();

		ContainerConfig cc = (ContainerConfig) super.getConfigObject();
		RawConnectorConfig rcc = null;

		if (cc != null) {
			PropertyManager pm = (PropertyManager) cc.getConfig("Config");
			if (pm != null) {
				PropertyStoreConfig psc = pm.getPropertyStore("Default");
				if (psc != null) {
					rcc = psc.getConnectionConfig();
				}
			}
		}

		if (rcc != null) {
			rcc.setParameterPropertySource("collection", PropertiesEditor.CONFIG_DIR + "/" + name + ".properties");
			for (String stdName : PropertyManager.STDCOLL_PROPERTY_NAMES) {
				if (name.equalsIgnoreCase(stdName)) {
					rcc.setParameter("collectionType", stdName);
					break;
				}
			}
		}
	}
}
