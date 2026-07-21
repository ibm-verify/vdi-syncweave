/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.eclipse;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.di.config.base.BaseConfigurationImpl;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;
import com.ibm.di.config.interfaces.PropertyManager;
import com.ibm.di.config.interfaces.PropertyStoreConfig;
import com.ibm.di.config.interfaces.RawConnectorConfig;
import com.ibm.di.config.interfaces.TDIProperties;
import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.entry.Entry;
import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;
import com.ibm.tdi.eclipse.editors.PropertiesEditor;
import com.ibm.tdi.eclipse.log.EclipseAppender;

/**
 * This class replaces the normal TDIPropertyStore to override access to the store. In the CE
 * the property stores are always special configuration files with the key/value pairs in them.
 * 
 */
public final class TDIPropertyStoreCE extends TDIPropertyStore {
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = 198251115520372634L;

	/**
	 * This is the logger object used by this class and other configuration
	 * drivers.
	 */
	public final static Log logger = new Log("com.ibm.di.TDIProperties");

	public final static String PROTECT_VAL_PREFIX = "{encr}";

	private PropertyStoreConfig psc;

	private boolean modified = false;

	private Map<String, Boolean> encrypted = new Hashtable<String, Boolean>();

	private TDIProperties context;

	private TDIConfigurationFile storeData;
	
	private ContainerConfig propData;

	transient private IFile file;

	private final static ResourceHash sResHash = MetamergeConfigFactory
			.getResHash();

	/**
	 * Constructor for the TDIPropertyStore object
	 * 
	 * @param psc
	 *            Configuration of the property store
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public TDIPropertyStoreCE(PropertyStoreConfig psc) throws Exception {
		this(psc, null);
	}

	/**
	 * Constructor for the TDIPropertyStore object
	 * 
	 * @param psc
	 *            The PropertyStoreConfig
	 * @param context
	 *            The TDIProperties context
	 * @throws Exception
	 */
	public TDIPropertyStoreCE(PropertyStoreConfig psc, TDIProperties context)
			throws Exception {
		this.psc = psc;
		try {
			initialize(context);
		} catch (Exception e) {
			logerror(sResHash.getString(
					"MICONFIG.TDIPROPERTYSTORE.CONSTRUCTOR.ERROR",
					new Object[] { psc.toString(), e.getMessage() }), e);
		}
	}

	public TDIPropertyStoreCE(IFile file, TDIProperties context) throws Exception {
		this.file = file;
		reloadFile();
		PropertiesEditor.synchWithLocalFile(propData, psc);
		
		//
		// -- Reload contents if file data changes
		//
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					IResourceDelta resDelta = delta.findMember(getFile().getFullPath());
					if (resDelta == null)
						return;
					if (resDelta.getKind() == IResourceDelta.CHANGED) {
						try {
							reloadFile();
						} catch (Exception e) {
							EclipseAppender.logerror(e.toString(), e);
						}
					}
				}
			}
		});
	}
	
	/**
	 * Returns the file object or null if store was created with a config only
	 * 
	 * @return the IFile object 
	 */
	public IFile getFile() {
		return file;
	}
	
	/**
	 * Initializes data structures. Load and initialize the connector, and read
	 * data if they should be read initially.
	 * 
	 * @param context
	 *            The TDIProperties context. Not used.
	 * @throws Exception
	 *             If any problem occurs during loading or initializing the
	 *             connector.
	 */
	public synchronized void initialize(TDIProperties context) throws Exception {
		// Trace.entrymin(this, "initialize", context);
		this.context = context;
		storeData = TDIPropertiesCE.loadPropertyStore(psc, psc.getShortName() + ".tdiproperties");
		if(storeData == null)
			return;
		
		propData = (ContainerConfig) ((ContainerConfig)storeData.getDefaultConfigObject()).getConfig("Data");
		// Trace.exitmin(this, "initialize");
	}

	/**
	 * Logs a message.
	 * 
	 * @param msg
	 *            The message to log
	 */
	public void log(String msg) {
		if (logger != null)
			logger.logdebug("[" + psc.getName() + "] " + msg);
	}

	/**
	 * Logs an error message.
	 * 
	 * @param msg
	 *            The message to log
	 * @param err
	 *            The Exception
	 */
	public void logerror(String msg, Exception err) {
		if (logger != null)
			logger.error("[" + psc.getName() + "] " + msg, err);
	}

	public synchronized void reload() {
		// override to avoid nasty errors
	}
	
	/**
	 * Reloads the information from the connector
	 * @throws Exception 
	 */
	public synchronized void reloadFile() throws Exception {
		storeData = TDIConfigurationFile.loadFile(file);
		PropertyManager pm = (PropertyManager) ((ContainerConfig) storeData.getDefaultConfigObject()).getConfig("Config"); //$NON-NLS-1$
		String name = file.getName().substring(0, file.getName().indexOf("."));
		psc = (PropertyStoreConfig) pm.getPropertyStores().getConfig(0);
		psc.setName(name);
		propData = (ContainerConfig) ((ContainerConfig)storeData.getDefaultConfigObject()).getConfig("Data");
	}

	/**
	 * Reinitializes the connector.
	 * 
	 * @throws Exception
	 *             If there is any problem with initializing the connector
	 */
	public synchronized void reconnect() throws Exception {
		// Trace.entrymax(this, "reconnect");
		// Trace.exitmax(this, "reconnect");
	}

	/**
	 * Terminates the connector
	 * 
	 * @throws Exception
	 *             If there is a problem with terminating the connector
	 */
	public synchronized void terminate() throws Exception {
		storeData = null;
	}

	/**
	 * Flushes cached changes to target system
	 */
	public synchronized void commit() throws Exception {
		// Trace.entrymax(this, "commit");
		// Trace.exitmax(this, "commit");
	}

	/**
	 * Returns true if the connector supports Iterator mode
	 * 
	 * @return true if the connector supports Iterator mode
	 */
	public boolean canIterate() {
		return supportsMode("Iterator");
	}

	/**
	 * Returns true if the connector supports Update mode and is not readonly
	 * 
	 * @return true if the connector supports Update mode and is not readonly
	 */
	public boolean canWrite() {
		return (supportsMode("Update") && !psc.getReadOnly());
	}

	/**
	 * Returns true if the connector supports Delete mode and is not readonly
	 * 
	 * @return true if the connector supports Delete mode and is not readonly
	 */
	public boolean canDelete() {
		return (supportsMode("Delete") && !psc.getReadOnly());
	}

	/**
	 * Returns true if the connector supports Lookup mode
	 * 
	 * @return true if the connector supports Lookup mode
	 */
	public boolean canRead() {
		return supportsMode("Lookup");
	}

	/**
	 * Returns true if we accept this propertyname.
	 * 
	 * @param propertyName
	 *            Property name to check
	 * @return true if we accept this propertyname
	 */
	public boolean accepts(String propertyName) {
		String str = psc.getNameFilters();
		if (str != null && str.trim().length() > 0) {
			for (String s: com.ibm.di.util.StringUtils.splitstring(str, "\r\n")) {
				if (propertyName.startsWith(s))
					return true;
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the short name of this TDIPropertyStore
	 * 
	 * @return The name value
	 */
	public String getName() {
		return psc.getShortName();
	}

	/**
	 * Returns true if the connector supports this mode
	 * 
	 * @param mode
	 *            The mode to check
	 * @return true if the connector supports this mode
	 */
	public synchronized boolean supportsMode(String mode) {
		return true;
	}

	/**
	 * Returns the named property
	 * 
	 * @param key
	 *            The property name to find
	 * @return The property value or null if not found
	 * @exception Exception
	 *                Any Exception thrown by the connector
	 */
	public synchronized Object getProperty(String key) throws Exception {
		// try reinit in case file exists now
		if(propData == null)
			initialize(context);
		
		if(propData == null)
			return null;
		
		BaseConfiguration p = propData.getConfig(key);
		if(p == null)
			return null;

		return PropertiesEditor.getLocalPropertyValue(p);
	}

	/**
	 * Returns the Entry object for a key. The Entry object has at least two
	 * Attributes, a "key" Attribute and a "value" Attribute. If the value is
	 * protected, there will also be a "protect" Attribute.
	 * 
	 * @param key
	 *            The name of the attribute to find
	 * @return The Entry containing the name and value
	 * @throws Exception
	 *             Any Exception thrown by the connector
	 */
	public synchronized Entry getPropertyEntry(String key) throws Exception {
		return null;
	}

	/**
	 * Sets the named property to the given value
	 * 
	 * @param key
	 *            The name of the property
	 * @param value
	 *            The new property value, null means delete
	 * @exception Exception
	 *                If key is missing, or any Exception thrown by the
	 *                connector
	 */
	public void setProperty(String key, Object value) throws Exception {
		// Trace.entrymax(this, "setProperty", key, value);
		if (key == null || key.length() == 0) {
			throw new Exception(
					sResHash.getString("MICONFIG.TDIPROPERTYSTORE.NO.KEY.ATTRIBUTE.SPECIFIED"));
		}
		Entry e = new Entry();
		e.setAttribute(TDIProperties.KEY_ATTRIBUTE, key);
		e.setAttribute(TDIProperties.VALUE_ATTRIBUTE, value);
		setProperty(e);
		// Trace.exitmax(this, "setProperty");
	}

	/**
	 * Sets the named property to the given value, with optional protection
	 * 
	 * @param key
	 *            The name of the property
	 * @param value
	 *            The new property value, null means delete
	 * @param protect
	 *            True if the value should be protected (encrypted)
	 * @exception Exception
	 *                If key is missing, or any Exception thrown by the
	 *                connector
	 */
	public void setProperty(String key, Object value, boolean protect)
			throws Exception {
		if (key == null || key.length() == 0) {
			throw new Exception(sResHash.getString("MICONFIG.TDIPROPERTYSTORE.NO.KEY.ATTRIBUTE.SPECIFIED"));
		}

		Entry e = new Entry();
		e.setAttribute(TDIProperties.KEY_ATTRIBUTE, key);
		e.setAttribute(TDIProperties.VALUE_ATTRIBUTE, value, protect);
		if (protect)
			e.setAttribute(TDIProperties.PROTECT_ATTRIBUTE, "true");
		encrypted.put(key, Boolean.valueOf(protect));
		setProperty(e);
	}

	/**
	 * Sets a named property to the given value. The Entry object must contains
	 * the "key" and "value" attributes and optionally "protect" (boolean)
	 * 
	 * @param entry
	 *            The Entry object containing property name and value
	 * @throws Exception
	 *             If key is missing, or any Exception thrown by the connector
	 */
	public synchronized void setProperty(Entry entry) throws Exception {
		// Trace.entrymax(this, "setProperty", entry);
		if (psc.getReadOnly()) {
			throw new Exception(
					sResHash.getString("MICONFIG.TDIPROPERTYSTORE.PROPERTY.STORE.IS.CONFIGURED.AS.READ.ONLY",
									getName()));
		}

		String key = entry.getString(TDIProperties.KEY_ATTRIBUTE);
		if (key == null || key.length() == 0) {
			throw new Exception(
					sResHash.getString("MICONFIG.TDIPROPERTYSTORE.NO.KEY.ATTRIBUTE.SPECIFIED"));
		}

		Object value = entry.getObject(TDIProperties.VALUE_ATTRIBUTE);
		
		//
		// -- All stores in the CE are local files with a fixed layout
		//
		
		// If there is an editor open for the store we set the value through
		// the editor in case other mods have been done to the store before updating the file.
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findEditor(new FileEditorInput(file));
		if(editor instanceof PropertiesEditor) {
			((PropertiesEditor)editor).setProperty(key, value);
		}
		
		// Update the local file store
		if(value == null) {
			propData.removeConfig(key, false);
		} else {
			BaseConfiguration b = propData.getConfig(key);
			if(b == null) {
				b = new BaseConfigurationImpl();
				b.setName(key);
				propData.addConfig(b);
			} else {
				b.removeParameter(PropertiesEditor.DELETED);
			}
			b.setParameter(PropertiesEditor.LOCAL_VALUE, value.toString());
			b.setParameter(PropertiesEditor.PROPERTY, "true");
			PropertiesEditor.verifyEncrypted(b);
		}

		storeData.commitChanges(null, true);
	}

	/**
	 * Removes a named property.
	 * 
	 * @param key
	 *            The name of the property to remove
	 * @throws Exception
	 *             If key is missing, or the Property store is readonly, or any
	 *             Exception thrown by the connector
	 */
	public synchronized void removeProperty(String key) throws Exception {
		// Trace.entrymax(this, "removeProperty", key);
		if (key == null || key.length() == 0) {
			throw new Exception(sResHash.getString(
					"MICONFIG.TDIPROPERTYSTORE.NO.KEY.ATTRIBUTE.SPECIFIED"));
		}

		if (psc.getReadOnly()) {
			throw new Exception(sResHash.getString(
					"MICONFIG.TDIPROPERTYSTORE.PROPERTY.STORE.IS.CONFIGURED.AS.READ.ONLY",
					getName()));
		}
		BaseConfiguration bc = propData.getConfig(key);
		if (bc != null)
			bc.setParameter(PropertiesEditor.DELETED, "true");
		setModified(true);
		// Trace.exitmax(this, "removeProperty");
	}

	/**
	 * Returns an Iterator over all property keys
	 * 
	 * @return an Iterator over all property keys
	 */
	public synchronized Iterator keys() {
		return this;
	}

	/**
	 * Returns an Iterator over all property values
	 * 
	 * @return an Iterator over all property values
	 */
	public synchronized Iterator values() {
		return this;
	}

	/**
	 * Returns an Iterator over all property Entries
	 * 
	 * @return an Iterator over all property Entries
	 */
	public synchronized Iterator entries() {
		return this;
	}

	/**
	 * Returns true if the Iterator has more values. Only call this method if
	 * you have previously called keys(), values() or entries().
	 * 
	 * @return true if the Iterator has a next
	 */
	public boolean hasNext() {
		return false;
	}

	/**
	 * Throws a NoSuchElementException.
	 * 
	 * @return Always throws a NoSuchElementException
	 * @exception NoSuchElementException is always thrown
	 */
	public synchronized Object next() throws NoSuchElementException {
		throw new NoSuchElementException();
	}

	/**
	 * Dummy method to implement Iterator. This method does nothing.
	 */
	public void remove() {
	}

	/**
	 * Returns null
	 * 
	 * @return null
	 */
	public Exception getException() {
		return null;
	}

	/**
	 * Returns true if the properties have been modified, but not saved yet
	 * 
	 * @return true if the properties have been modified, but not saved yet
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Sets the modified flag. This can be useful if you want to force a save,
	 * or avoid a save
	 */
	public void setModified(boolean modified) {
		this.modified = modified;
	}

    /**
     * {@inheritDoc}
     */
	public RawConnectorConfig getConnectionConfig() {
		return psc.getConnectionConfig();
	}
}
