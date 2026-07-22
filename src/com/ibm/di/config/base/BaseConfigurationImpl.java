/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
/**
 * The BaseConfigurationImpl is the class from which all other classes descend. This class provides all the
 * necessary methods for event notification, parameter handling.
 */
package com.ibm.di.config.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import java.security.SecureRandom;

import javax.naming.Binding;
import javax.naming.Name;
import javax.naming.NameNotFoundException;

import com.ibm.di.config.interfaces.AssemblyLineConfig;
import com.ibm.di.config.interfaces.BaseConfiguration;
import com.ibm.di.config.interfaces.ConnectorConfig;
import com.ibm.di.config.interfaces.ContainerConfig;
import com.ibm.di.config.interfaces.DefaultConfigChangeListener;
import com.ibm.di.config.interfaces.InheritanceLoopException;
import com.ibm.di.config.interfaces.MetamergeConfig;
import com.ibm.di.config.interfaces.MetamergeConfigChange;
import com.ibm.di.config.interfaces.MetamergeConfigChangeListener;
import com.ibm.di.config.interfaces.MetamergeConfigFactory;

import com.ibm.di.config.interfaces.TDIPropertyStore;
import com.ibm.di.entry.Entry;
import com.ibm.di.function.SystemFunctions;
import com.ibm.di.server.ResourceHash;
import com.ibm.icu.util.StringTokenizer;

/**
 * This class provides the basic methods for all the configuration classes.
 */
public class BaseConfigurationImpl implements BaseConfiguration,
		MetamergeConfigChangeListener {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Unique ID used for deserialization.
	 */
	static final long serialVersionUID = -7316979979253125005L;

	/**
	 * Property prefix.
	 */
	private String _propertyPrefix = "@PROPERTY{";

	/**
	 * Property suffix.
	 */
	private String _propertySuffix = "}";

	/**
	 * Substitute prefix.
	 */
	private static String _substitutePrefix = "@SUBSTITUTE";

	/**
	 * Name used for notify events.
	 */
	public final static String NAME = "name";

	/**
	 * Full name of this config.
	 */
	private Name _name;

	/**
	 * Parent container.
	 */
	private BaseConfiguration _parent;

	/**
	 * Container for the local data.
	 */
	private TreeMap<String, Object> _data;

	/**
	 * Container for Event Listeners.
	 * <p>
	 * Note: listeners are not serialized.
	 */
	private transient Vector<Object> _listeners = new Vector<Object>();

	/**
	 * Flag determining the use of listeners.
	 */
	private static boolean useListeners = true;

	/**
	 * Modified flag.
	 */
	private boolean _modified = false;

	/**
	 * Creating MetamergeConfig.
	 */
	private MetamergeConfig _mc;

	/**
	 * Inherited configuration.
	 */
	private BaseConfiguration _inherited;

	/**
	 * Child items
	 */
	private TreeMap<String, BaseConfiguration> _children = new TreeMap<String, BaseConfiguration>();

	/**
	 * Flags (like ignore, inheritance)
	 */
	private int _flags = _static_flags;

	/**
	 * Static flags.
	 * <p>
	 * Note: Eclipse CE disables property expansion for all configs.
	 */
	private static int _static_flags = Integer.getInteger(
			"com.ibm.di.config.static_flags", 0).intValue();

	/**
	 * Substitution map
	 */
	private transient Map<String, Object> _paramMap = null;

	/**
	 * Package
	 */
	transient boolean batchChange = false;
	
	transient int batchChanges = 0;

	/**
	 * TMS Filename used for retrieving info, error and debug messages
	 */
	private static final String PROPERTIES_FILE = "mmconfig";

	/**
	 * Name of the event of setting inherit from.
	 */
	static final String SETINHERITSFROM = "setInheritsFrom";

	/**
	 * ResourceHash used for access of the TMS messages.
	 */
	private static ResourceHash sResHash = ResourceHash
			.getHash(PROPERTIES_FILE);

	/**
	 * Random number generator.
	 */
	private static final Random randGen = new SecureRandom();

	/**
	 * The last time this was modified
	 */
	private long timeStamp;
	
	/**
	 * @return ResourceHash object used for access of the TMS messages.
	 */
	public static ResourceHash getResHash() {
		return sResHash;
	}

	/**
	 * Empty constructor
	 */
	public BaseConfigurationImpl() {
		setData(new TreeMap<String, Object>());
	}

	/**
	 * Constructor providing a TreeMap of attribute/value pairs
	 * 
	 * @param data
	 *            TreeMap of attribute/value pairs
	 */
	@SuppressWarnings("rawtypes")
	public BaseConfigurationImpl(Object data) {
		if (data instanceof TreeMap) {
			setData((TreeMap) data);
		} else {
			MetamergeConfigImpl.logger.error(sResHash.getString(
					"MMCONFIG.BASECONFIMPL.FATAL.CONSTRUCTED.WITH.NON.TREEMAP.OBJECT",
					data));
			if (_data == null)
				setData(new TreeMap());
		}
	}

	/**
	 * Established the inheritance chain for this object.
	 * 
	 * @throws Exception
	 *             if the operation could not be performed
	 */
	public void setupInheritanceChain() throws Exception {
		String inheritFrom = getStringParameter(InternalSchema.INHERITS_FROM);
		try {
			MetamergeConfigFactory.verifyInheritanceChain(this, inheritFrom);
		} catch (InheritanceLoopException ile) {
			ile.printStackTrace();
			MetamergeConfigImpl.logger.error(sResHash.getString(
					"MMCONFIG.BASECONFIMPL.CANNOT.SETUP.INHERITANCE",
					new Object[] { getLongName(), inheritFrom }), ile);
			inheritFrom = null;
			setInheritsFromRef(null);
		}
		if (inheritFrom != null) {
			if (getName() != null && inheritFrom.equals(getName().toString())) {
				throw new Exception(sResHash.getString(
						"MMCONFIG.BASECONFIMPL.CANNOT.INHERIT.FROM.SELF",
						inheritFrom));
			} else if (inheritFrom.equals(BaseConfiguration.INHERIT_PARENT)) {
				if (getParent() != null)
					setInheritsFrom(getParent().getInheritsFrom());
				else
					setInheritsFrom(null);
			} else if (inheritFrom.equals(BaseConfiguration.INHERIT_NONE)) {
				setInheritsFrom(null);
			} else {
				if (!inheritFrom.contains("/")) {
					// This could be inheriting from a Connector in the same AL.
					String connectorName = inheritFrom;
					if (connectorName.startsWith("@"))
						connectorName = connectorName.substring(1);

					for (BaseConfiguration bc = getParent(); bc != null; bc = bc.getParent() ) {
						try {
							ConnectorConfig cc = null;
							if (bc instanceof AssemblyLineConfig) {
								cc = ((AssemblyLineConfig) bc).getConnectorByName(connectorName);
							} else if (bc instanceof ContainerConfig) {
								cc = (ConnectorConfig) ((ContainerConfig)bc).getConfig(connectorName, true);
							}
							if (cc != null) {
								setInheritsFrom(cc);
								return;
							}
						} catch (Exception e) {
							SystemFunctions.doNothing();
						}					
					}
				}

				try {
					MetamergeConfig mc = getMetamergeConfig();
					if (mc == null)
						setInheritsFrom((BaseConfiguration) MetamergeConfigFactory
								.lookup(null, inheritFrom));
					else
						setInheritsFrom((BaseConfiguration) mc
								.lookup(inheritFrom));
				} catch (Exception nnf) {
					setInheritsFrom(null);
					String msg = sResHash.getString(
							"MMCONFIG.BASECONFIMPL.CANNOT.SETUP.INHERITANCE",
							new Object[] { getLongName(), inheritFrom });
					MetamergeConfigImpl.logger.warn(nnf.toString());
					MetamergeConfigImpl.logger.error(msg);
				}
			}
		} else {
			setInheritsFrom(null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getInheritsFrom() {
		return _inherited;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getInheritsFromRef() {
		Object obj = getParameterRaw(InternalSchema.INHERITS_FROM);
		//TODO: Is this needed?
		if (obj != null)
			obj = expandPropertyValue(InternalSchema.INHERITS_FROM, obj, null);
		return obj != null ? obj.toString() : null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInheritsFrom(BaseConfiguration inherit) {

		if (_inherited == inherit)
			return;

		if ((inherit != null) && (inherit.equals(this))) {
			MetamergeConfigImpl.logger.error(sResHash.getString(
					"MMCONFIG.BASECONFIMPL.CANNOT.INHERIT.FROM.SELF2",
					getName()));
			return;
		}

		if (_inherited != null)
			_inherited.removeListener(this);
		_inherited = inherit;
		if (_inherited != null)
			_inherited.addListener(this);

		notifyChange(this, InternalSchema.INHERITS_FROM,
				MetamergeConfigChange.MCC_REPLACE, SETINHERITSFROM);
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateInheritsFrom(String ref) throws Exception {
		if (ref.length() == 0)
			ref = null;

		MetamergeConfigFactory.verifyInheritanceChain(this, ref);

		setInheritsFromRef(ref);
		setupInheritanceChain();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInheritsFromRef(String ref) {
		if (ref == null || ref.equals(""))
			removeParameter(InternalSchema.INHERITS_FROM);
		else
			setStringParameter(InternalSchema.INHERITS_FROM, ref);
	}

	/**
	 * Returns the modified state of this object.
	 * 
	 * @return the modified state.
	 */
	public boolean getModified() {
		return _modified;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setModified(boolean modified) {
		_modified = modified;
		if (modified) {
			if (_parent != null) {
				_parent.setModified(modified);
			} else if (_mc != null) {
				performNotifyChange(new MetamergeConfigChange(this, "", MetamergeConfigChange.MCC_MODIFY, null));
				_mc.setModified(modified);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public void setUseListeners(boolean value) {
		setUseConfigListeners(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Deprecated
	public boolean getUseListeners() {
		return getUseConfigListeners();
	}
	
	/**
	 * Enable/disable the use of configuration listeners in the JVM. This is an
	 * optimization to turn off listeners which the CE uses, but the Server does
	 * not need.
	 * 
	 * @param value
	 *            False to disable configuration listeners.
	 * @since 7.0
	 */
	public static void setUseConfigListeners(boolean value) {
		useListeners = value;
	}

	/**
	 * @return Whether configuration listeners are enabled or disabled in this
	 *         JVM.
	 * @see #setUseConfigListeners(boolean)
	 * @since 7.0
	 */
	public static boolean getUseConfigListeners() {
		return useListeners;
	}
	
	/**
	 * Check whether a particular event listener is available.
	 * 
	 * @param listener
	 * @return <code>true</code> if the listener is available; otherwise
	 *         <code>false</code>;
	 */
	private boolean hasListener(Object listener) {
		for (int i = 0; i < _listeners.size(); i++) {
			if (getListener(i) == listener)
				return true;
		}
		return false;
	}

	/**
	 * @param index
	 *            number of the listener
	 * @return MetamergeConfigChangeListener object containing the wanted
	 *         listener
	 */
	@SuppressWarnings("unchecked")
	private MetamergeConfigChangeListener getListener(int index) {
		if (index >= _listeners.size())
			return null;
		Object obj = _listeners.get(index);
		if (obj instanceof WeakReference<?>)
			return ((WeakReference<MetamergeConfigChangeListener>) obj).get();
		else
			return (MetamergeConfigChangeListener) obj;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListener(MetamergeConfigChangeListener listener) {
		if (!(listener instanceof DefaultConfigChangeListener || useListeners))
			return;

		if (_listeners == null)
			_listeners = new Vector<Object>();

		// Already in list?
		if (hasListener(listener))
			return;
		else if (listener instanceof DefaultConfigChangeListener)
			_listeners.add(listener);
		else
			_listeners.add(new WeakReference<MetamergeConfigChangeListener>(
					listener));
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(MetamergeConfigChangeListener listener) {
		if (_listeners == null)
			return;

		for (int i = 0; i < _listeners.size(); i++) {
			if (getListener(i) == listener) {
				_listeners.remove(i);
				break;
			}
		}
		// _listeners.remove ( listener );
		if (getInheritsFrom() != null)
			getInheritsFrom().removeListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void configurationChanged(MetamergeConfigChange mcc) {
		performNotifyChange(mcc);
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyChange(Object source, Object key, int operation) {
		if (operation == MetamergeConfigChange.END_CHANGES)
			batchChange = false;
		
		notifyChange(new MetamergeConfigChange(source, key, operation, null));
		
		if (operation == MetamergeConfigChange.BEGIN_CHANGES) {
			batchChange = true;
			batchChanges = 0;
		} else if(operation != MetamergeConfigChange.END_CHANGES && batchChange) {
			batchChanges++;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyChange(Object source, Object key, int operation,
			Object userObject) {
		notifyChange(new MetamergeConfigChange(source, key, operation,
				userObject));
	}

	/**
	 * {@inheritDoc}
	 */
	public void notifyChange(MetamergeConfigChange event) {
		if (batchChange)
			return;
		
		// -- Only set the modified flag if we got notifyChange calls between BEGIN/END changes
		// -- and the user object is not SETINHERITSFROM (all special notifications).
		if (event.getUserObject() != SETINHERITSFROM) {
			switch(event.getOperation()) {
			case MetamergeConfigChange.BEGIN_CHANGES:
				break;
			case MetamergeConfigChange.END_CHANGES:
				if(batchChanges == 0)
					break;
			default:
				setModified(true);
			}
		}

		if (_parent != null)
			_parent.notifyChange(event);

		performNotifyChange(event);

	}

	/**
	 * Notifies all registered listeners for a specified config for a change
	 * event.
	 * 
	 * @param mcc
	 *            the config object
	 */
	@SuppressWarnings("unchecked")
	protected void performNotifyChange(MetamergeConfigChange mcc) {

		if (_listeners == null || _listeners.size() == 0)
			return;

		boolean cleanup = false;

		// Make a copy of _listeners to avoid problems if a listener is removed.
		for (Object ref: _listeners.toArray()) {
			MetamergeConfigChangeListener listener = ref instanceof WeakReference ? 
					((WeakReference<MetamergeConfigChangeListener>)ref).get()
					: (MetamergeConfigChangeListener) ref;

			if (listener == null) {
				cleanup = true;
			} else if (listener != mcc.getSource()) {
				listener.configurationChanged(mcc);
			}
		}
		// Remove GC'ed listeners
		if (cleanup) {
			for (int i = _listeners.size() - 1; i > -1; i--) {
				if (getListener(i) == null) {
					_listeners.removeElementAt(i);
				}
			}
		}
	}

	/**
	 * @return the total number of keys in this configuration object.
	 */
	public int size() {
		return _data.size();
	}

	/**
	 * @return the name of this object.
	 */
	public Name getName() {
		return _name;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getShortName() {
		Name name = getName();
		if (name == null)
			return null;
		if (name.size() == 0)
			return "";
		return name.get(name.size() - 1);
	}

	/**
	 * @return a string giving the complete name of this object, including the
	 *         name of all parents.
	 */
	public String getLongName() {
		StringBuilder s = new StringBuilder();
		if (_parent instanceof BaseConfigurationImpl)
			s.append(((BaseConfigurationImpl) _parent).getLongName());
		String name = null;
		if (_name != null && _name.size() > 1)
			name = _name.toString();
		else if (_parent != null)
			name = _parent.nameForChild(this);
		if (name == null && _name != null)
			name = _name.toString();
		if (name != null) {
			if (s.length() > 0)
				s.append(".");
			s.append(name);
		}
		return s.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getNamespace() {
		Object obj = MetamergeConfigFactory.getNamespaceFor(this);
		try {
			return MetamergeConfigFactory.parseName("").add(obj.toString());
		} catch (Exception error) {
			return null;
		}
	}

	/**
	 * Sets the name of this object.
	 * 
	 * @param name
	 *            The new name value
	 */
	public void setName(Name name) {
		if (name == null || name.equals(_name))
			return;
		Object userObject = _name;
		_name = (Name) name.clone();
		notifyChange(this, NAME, MetamergeConfigChange.MCC_SET, userObject);
		updateTimeStamp();
	}

	/**
	 * Sets the name attribute of this object.
	 * 
	 * @param name
	 *            The new name value
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public void setName(String name) throws Exception {
		setName(MetamergeConfigFactory.parseName(name));
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public TreeMap getData() {
		return _data;
	}

	/**
	 * Clones the local data structure.
	 * 
	 * @param inputMap
	 *            TreeMap object to be cloned
	 * @return clone of the <code>inputMap</code> parameter.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TreeMap deepClone(TreeMap inputMap) {
		TreeMap<Object, Object> n = new TreeMap<Object, Object>();
		TreeMap map;
		if (inputMap == null)
			map = getData();
		else
			map = inputMap;

		for (Iterator<Map.Entry> e = map.entrySet().iterator(); e.hasNext();) {
			Map.Entry mapEntry = e.next();
			Object name = mapEntry.getKey();
			Object obj = mapEntry.getValue();
			if (obj instanceof Vector) {
				Vector<Object> cv = new Vector<Object>();
				Vector<Object> v = (Vector) obj;
				for (int i = 0; i < v.size(); i++) {
					Object o = v.get(i);
					try {
						if (o instanceof TreeMap)
							o = deepClone((TreeMap) o);
						else if (o instanceof BaseConfigurationImpl)
							o = ((BaseConfigurationImpl) o).getClone();
					} catch (Exception ignore) {
					}
					cv.add(o);
				}
				n.put(name, cv);
			} else if (obj instanceof TreeMap) {
				n.put(name, deepClone((TreeMap) obj));
			} else {
				n.put(name, obj);
			}
		}
		return n;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setData(TreeMap obj) {
		// TODO: deep clone????
		_data = obj;
	}

	/**
	 * Called after internal data structure is set.
	 * 
	 * @throws Exception
	 */
	public void init() throws Exception {
	}

	/**
	 * Returns an Iterator for all keys in this object.
	 * 
	 * @return The dataIterator value.
	 */
	public Iterator<String> getDataIterator() {
		if (_data != null)
			return _data.keySet().iterator();
		else
			return null;
	}

	/**
	 * Returns a list of keys in this object.
	 * 
	 * @param level
	 *            can have these values: <li>ONE_LEVEL - only simple key/value
	 *            items are returned.</li> <li>SUBTREE - all keys are returned
	 *            regardless of whether they are complex (TreeMap) or simple
	 *            (String, Boolean, Integer, Vector ).</li> <li>RECURSIVE -
	 *            inherited keys are returned as well.</li>
	 * @return The list of keys.
	 */
	public List<String> getKeys(int level) {

		ArrayList<String> list = new ArrayList<String>();

		boolean tmonly = ((level & SUBTREE) > 0);
		// Retrieve local keys

		for (Iterator<String> i = getDataIterator(); i != null && i.hasNext();) {
			String key = i.next();
			Object keyData = getData().get(key);
			if (keyData instanceof TreeMap<?, ?> && tmonly)
				list.add(key);
			else if (!tmonly && !(keyData instanceof TreeMap<?, ?>))
				list.add(key);
		}

		// Retrieve keys from inherited object
		if ((level & RECURSIVE) > 0 && getInheritsFrom() != null) {
			List<String> plist = getInheritsFrom().getKeys(level);
			for (int i = 0; i < plist.size(); i++) {
				if (!list.contains(plist.get(i)))
					list.add(plist.get(i));
			}
		}

		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getParent() {
		return _parent;
	}

	/**
	 * @return a string giving the complete name of the parent, followed by a
	 *         dot; if no parent, return empty string
	 */
	public String getParentName() {
		if (_parent instanceof BaseConfigurationImpl)
			return ((BaseConfigurationImpl) _parent).getParentName()
					+ _parent.getName() + ".";
		else
			return "";
	}

	/**
	 * Sets the parent object.
	 * 
	 * @param parent
	 *            the new parent
	 */
	public void setParent(BaseConfiguration parent) {
		_parent = parent;
	}

	/**
	 * Sets flags. ( like ignore inheritance )
	 * 
	 * @param flags
	 *            the new flags to set
	 */
	public void setFlags(int flags) {
		_flags = flags;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFlags() {
		return _flags;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeParameter(Object name) {
		if (name != null && _data.remove(name.toString()) != null) {
			notifyChange(this, name, MetamergeConfigChange.MCC_REMOVE);
			updateTimeStamp();
		}
	}

	private void updateTimeStamp() {
		MetamergeConfig mc = getMetamergeConfig();
		if (mc == null || ! mc.isModTSEnabled())
			return;
		
		long time = new java.util.Date().getTime();
		for (BaseConfiguration bc = this; bc != null; bc = bc.getParent()) {
			bc.setModTS(time);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isParameterLocal(Object name) {
		if (name == null)
			return true;
		if (_data.containsKey(name.toString()))
			return true;
		BaseConfiguration inh = getInheritsFrom();
		while (inh != null) {
			if (inh.getInheritsFrom() == null && !InternalSchema.SCRIPT.equals(name))
				break;
			Object o = inh.getParameterRaw(name);
			if (o != null)
				return o.toString().length() == 0;
			inh = inh.getInheritsFrom();
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasParameter(Object name) {
		return name != null && _data.containsKey(name.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParameterRaw(Object name) {
		if (name != null && _data != null)
			return _data.get(name.toString());
		else
			return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParameter(Object name) {
		return getParameter(name, null, null);
	}

	/**
	 * Gets the parameter attribute of this config object. If the parameter is
	 * not present a default value is assigned to the parameter using
	 * <code>createValue</code>.
	 * 
	 * @param name
	 *            The parameter name
	 * @param createValue
	 *            The value added to this object in case the parameter does not
	 *            exist
	 * @return The parameter value
	 */
	public Object getParameter(Object name, Object createValue) {
		return getParameter(name, createValue, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParameter(Object name, Object createValue,
			List<String> cref) {
		if (name == null)
			return null;
		Object obj = (_data != null) ? _data.get(name.toString()) : null;

		if (obj == null && _inherited != null && willUseInherited()) {
			for (BaseConfiguration i = _inherited; i != null && obj == null; i = i.getInheritsFrom()) {
				obj = i.getParameterRaw(name);			
			}

			// Return a copy of structures we inherit from another component
			if (obj instanceof Vector<?>)
				return ((Vector<?>) obj).clone();
			if (obj instanceof TreeMap<?,?>)
				return deepClone((TreeMap<?,?>) obj);
		}

		if (obj != null && willExpandProps())
			return expandPropertyValue(name, obj, cref);

		if (obj == null && createValue != null) {
			obj = createValue;
			_data.put(name.toString(), obj);
		}

		return obj;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getStringParameter(Object name) {
		Object obj = getParameter(name);
		if (obj == null)
			return null;
		else
			return obj.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBooleanParameter(Object key, boolean defval) {
		Object obj = getParameter(key);
		if (obj == null)
			return defval;

		if (obj instanceof Boolean)
			return ((Boolean) obj).booleanValue();

		return Boolean.valueOf(obj.toString()).booleanValue();
	}

	/**
	 * {@inheritDoc}
	 */
	public int getIntegerParameter(Object key, int defval) {
		Object obj = getParameter(key);
		if (obj == null)
			return defval;

		if (obj instanceof Integer)
			return ((Integer) obj).intValue();
		String s = obj.toString().trim();
		if (s.length() == 0)
			return defval;

		return Integer.parseInt(s);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParameter(Object name, Object value) {
		if (name == null)
			return;
		if (value == null) {
			removeParameter(name);
			return;
		}
		Object oldVal = _data.get(name.toString());
		if (value.equals(oldVal))
			return;
		_data.put(name.toString(), value);
		updateTimeStamp();
		notifyChange(this, name, (oldVal == null ? MetamergeConfigChange.MCC_SET
				: MetamergeConfigChange.MCC_REPLACE));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParameter(Object name, Object value, boolean notify) {
		if (notify)
			setParameter(name, value);
		else if (value == null)
			_data.remove(name.toString());
		else
			_data.put(name.toString(), value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProtectedParameter(Object name, Object value)
			throws Exception {
		TDIPropertyStore def = null;
		MetamergeConfig mc = getMetamergeConfig();
		if(mc != null)
			def = mc.getTDIProperties().getPasswordStore();
		
		if (def != null) {
			String propname = getParameterPropertySource(name);
			if (propname != null && propname.startsWith("{property.")
					&& propname.endsWith("}")) {
				propname = propname.substring(10, propname.length() - 1);
				getMetamergeConfig().getTDIProperties().setProperty(propname,
						value, true);
			} else {
				propname = def.getName() + ":" + name + "-"
						+ Long.toHexString(Math.abs(randGen.nextLong()));
				getMetamergeConfig().getTDIProperties().setProperty(propname,
						value, true);
				setParameterPropertySource(name, "{property." + propname + "}");
			}
		} else {
			setParameter(name, value);
			setProtectedParameter(name.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void setProtectedParameter(String name) {
		Set<String> protectedParameters = (Set<String>) getParameterRaw(InternalSchema.PROTECTED_PARAMETERS);
		if (protectedParameters == null) {
			protectedParameters = new HashSet<String>();
			setParameter(InternalSchema.PROTECTED_PARAMETERS, protectedParameters, false);
		}
		protectedParameters.add(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public boolean isProtectedParameter(String name) {
		Set<String> protectedParameters = (Set<String>) getParameterRaw(InternalSchema.PROTECTED_PARAMETERS);
		return protectedParameters != null && protectedParameters.contains(name);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBooleanParameter(Object name, boolean value) {
		setParameter(name, "" + value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIntegerParameter(Object name, int value) {
		setParameter(name, "" + value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setStringParameter(Object name, String value) {
		setParameter(name, value);
	}

	/**
	 * @param name
	 *            the parameter name
	 * @return the property name source for a given parameter name. If the
	 *         parameter name is not associated with a user property then null
	 *         is returned.
	 */
	public String getParameterPropertySource(Object name) {
		Object value = getParameterRaw(name);
		String s = getParameterPropertySourceFromValue(value);
		if (s == null && value == null && _inherited != null && willUseInherited())
			s = _inherited.getParameterPropertySource(name);
		return s;
	}

	/**
	 * @param value
	 *            parameter value
	 * @return the parameter property source. Also converts the old property
	 *         source to the new Parameter substitution format.
	 */
	public String getParameterPropertySourceFromValue(Object value) {

		if (value == null)
			return null;

		String str = value.toString();
		/* defect 13374, toString() returns null for some Objects */
		if (str == null)
			return null;

		if (str.startsWith(_substitutePrefix)) {
			return str.substring(_substitutePrefix.length());
		} else if (str.startsWith(_propertyPrefix)
				&& str.endsWith(_propertySuffix)) {
			// On the fly conversion of old prop source to new Parameter subst
			// format
			return "{property." + str.substring(_propertyPrefix.length(),
					str.length() - _propertySuffix.length()) + "}";
		} else {
			return null;
		}
	}

	/**
	 * Associates a parameter name with a user property.
	 * 
	 * @param name
	 *            parameter name
	 * @param propertySource
	 *            parameter property source
	 */
	public void setParameterPropertySource(Object name, String propertySource) {
		setParameter(name, _substitutePrefix + propertySource);
	}

	/**
	 * Substitutes the parameter value with a user property value.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @return expanded property value.
	 */
	public Object expandPropertyValue(Object name, Object value) {
		return expandPropertyValue(name, value, null);
	}

	/**
	 * Substitutes the parameter value with a user property value.
	 * 
	 * @param name
	 *            parameter name
	 * @param value
	 *            parameter value
	 * @param cref
	 *            The circular references list of previously used config object
	 *            expressions
	 * @return a valid substituted value; if error occurs returns an Exception
	 *         object.
	 */
	public Object expandPropertyValue(Object name, Object value,
			List<String> cref) {

		String source = getParameterPropertySourceFromValue(value);
		if (source == null)
			return value;

		try {
			Object retValue = com.ibm.di.util.ParameterSubstitution.substitute(
					source, getSubstitutionMap(), cref);
			if (retValue == null)
				MetamergeConfigImpl.logger.warn(sResHash.getString(
						"MMCONFIG.BASECONFIMPL.EXPANDED.NO.VALUE",
						new Object[] { name, getLongName(), source }));
			return retValue;
		} catch (Exception error) {
			MetamergeConfigImpl.logger.error(sResHash.getString(
					"MMCONFIG.BASECONFIMPL.EXPANDING.PROPERTY.ERROR",
					new Object[] { name, getLongName(), source }), error);
			return error;
		}
	}

	/**
	 * This method returns a {@link Entry} object constructed from the internal
	 * data structure. Default implementation simply copies every attribute
	 * to/from entry using String values.
	 * 
	 * @return The entry representing the internal data.
	 * @exception Exception
	 */
	public Object toEntry() throws Exception {
		Entry e = new Entry();
		for (Iterator<String> iter = getDataIterator(); iter.hasNext();) {
			String key = iter.next();
			e.setAttribute(key, getParameter(key));
		}
		return e;
	}

	/**
	 * This method populates this object's internal data with the data from a
	 * {@link Entry} object.
	 * 
	 * @param entry
	 *            The entry containing data.
	 * @exception Exception
	 */
	public void fromEntry(Object entry) throws Exception {
		boolean mod = getModified();
		String[] names = ((Entry) entry).getAttributeNames();
		for (int i = 0; i < names.length; i++) {
			setParameter(names[i], ((Entry) entry).getString(names[i]));
		}
		setModified(mod);
	}

	/**
	 * Answers a string containing the name of the config.
	 * 
	 * @return a printable representation of the name
	 */
	public String toString() {
		String str = getShortName();
		if (str == null)
			str = super.toString();
		return str;
		/*
		 * String str = "["; for ( Iterator i = getDataIterator(); i.hasNext();
		 * ) { if ( str.length() > 1 ) str += ", "; Object key = i.next(); str
		 * += key; str += "=" + getStringParameter(key); } str += "]"; return
		 * str;
		 */
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMetamergeConfig(MetamergeConfig mc) {
		_mc = mc;
	}

	/**
	 * {@inheritDoc}
	 */
	public MetamergeConfig getMetamergeConfig() {
		if (_parent != null)
			return _parent.getMetamergeConfig();
		else
			return _mc;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getDebug() {
		return getDebug(false);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getDebug(boolean defval) {
		return getBooleanParameter(InternalSchema.DEBUG, defval);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDebug(boolean debug) {
		setBooleanParameter(InternalSchema.DEBUG, debug);
	}

	/**
	 * Gets the debugBreak attribute of this object.
	 * 
	 * @param defval
	 *            The value to be returned, if the debugBreak parameter cannot
	 *            be found
	 * 
	 * @return The debugBreak value.
	 */
	public boolean getDebugBreak(boolean defval) {
		return getBooleanParameter(InternalSchema.DEBUG_BREAK, defval);
	}

	/**
	 * Sets the debugBreak attribute of this object.
	 * 
	 * @param debug
	 *            The new debugBreak value.
	 */
	public void setDebugBreak(boolean debug) {
		setBooleanParameter(InternalSchema.DEBUG_BREAK, debug);
	}

	/**
	 * Gets the script attribute of this object.
	 * 
	 * @return The script value.
	 */
	public String getScript() {
		return getStringParameter(InternalSchema.SCRIPT);
	}

	/**
	 * Sets the script attribute of this object.
	 * 
	 * @param script
	 *            The new script value
	 */
	public void setScript(String script) {
		setStringParameter(InternalSchema.SCRIPT, script);
	}

	/**
	 * Gets the ScriptEngine attribute of this object.
	 * 
	 * @return The ScriptEngine value.
	 */
	public String getScriptEngine() {
		String str = getStringParameter(InternalSchema.SCRIPT_ENGINE);
		if (str != null)
			return str.toLowerCase(Locale.ENGLISH);
		else
			return "javascript";
	}

	/**
	 * Sets the ScriptEngine attribute of this object.
	 * 
	 * @param engine
	 *            The new ScriptEngine value
	 */
	public void setScriptEngine(String engine) {
		setStringParameter(InternalSchema.SCRIPT_ENGINE, engine);
	}

	/**
	 * Gets the nullBehavior attribute of this object.
	 * 
	 * @return The nullBehavior value.
	 */
	public String getNullBehavior() {
		return getStringParameter(InternalSchema.NULL_BEHAVIOR);
	}

	/**
	 * Gets the nullBehaviorValue attribute of this object.
	 * 
	 * @return The nullBehaviorValue value.
	 */
	public String getNullBehaviorValue() {
		return getStringParameter(InternalSchema.NULL_BEHAVIOR_VALUE);
	}

	/**
	 * Sets the nullBehavior attribute of this object.
	 * 
	 * @param nb
	 *            The new nullBehavior value
	 */
	public void setNullBehavior(String nb) {
		setStringParameter(InternalSchema.NULL_BEHAVIOR, nb);
	}

	/**
	 * Sets the nullBehaviorValue attribute of this object.
	 * 
	 * @param nbv
	 *            The new nullBehaviorValue value
	 */
	public void setNullBehaviorValue(String nbv) {
		setStringParameter(InternalSchema.NULL_BEHAVIOR_VALUE, nbv);
	}

	/**
	 * Gets the nullDefinition attribute of the BaseConfiguration object
	 * 
	 * @return The nullDefinition value
	 */
	public String getNullDefinition() {
		return getStringParameter(InternalSchema.NULL_DEFINITION);
	}

	/**
	 * Gets the nullDefinitionValue attribute of the BaseConfiguration object
	 * 
	 * @return The nullDefinitionValue value
	 */
	public String getNullDefinitionValue() {
		return getStringParameter(InternalSchema.NULL_DEFINITION_VALUE);
	}

	/**
	 * Sets the nullDefinition attribute of the BaseConfiguration object
	 * 
	 * @param nb
	 *            The new nullBehavior value
	 */
	public void setNullDefinition(String nb) {
		setStringParameter(InternalSchema.NULL_DEFINITION, nb);
	}

	/**
	 * Sets the nullDefinitionValue attribute of the BaseConfiguration object
	 * 
	 * @param nbv
	 *            The new nullBehaviorValue value
	 */
	public void setNullDefinitionValue(String nbv) {
		setStringParameter(InternalSchema.NULL_DEFINITION_VALUE, nbv);
	}

	/**
	 * Gets the enabled attribute of this object.
	 * 
	 * @return The nullBehaviorValue value.
	 */
	public boolean getEnabled() {
		return getBooleanParameter(InternalSchema.ENABLED, false);
	}

	/**
	 * Sets the enabled attribute of this object.
	 * 
	 * @param autoStart
	 *            <code>true</code> for enabling; <code>false</code> otherwise.
	 */
	public void setEnabled(boolean autoStart) {
		setBooleanParameter(InternalSchema.ENABLED, autoStart);
	}

	/**
	 * @return <code>true</code> if this object will expand the external
	 *         properties; <code>false</code> otherwise.
	 */
	public boolean willExpandProps() {
		return ((_flags & BaseConfiguration.DISABLE_EXTPROPS) == 0);
	}

	/**
	 * @return <code>true</code> if this object will enable inherited access;
	 *         <code>false</code> otherwise.
	 */
	public boolean willUseInherited() {
		return ((_flags & BaseConfiguration.DISABLE_INHERITANCE) == 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getClone() throws Exception {
		Constructor<? extends BaseConfigurationImpl> c = getClass().getConstructor(Object.class);
		BaseConfigurationImpl bc = c.newInstance(deepClone(null));
		bc.setName(getName());
		bc.init();
		bc.setModTS(timeStamp);
		return bc;
	}

	/**
	 * Gets the log enabled attribute of this object.
	 * 
	 * @return The nullBehaviorValue value.
	 */
	public boolean getLogEnabled() {
		return getBooleanParameter(InternalSchema.LOG_ENABLED, true);
	}

	/**
	 * Sets the log enabled attribute of this object.
	 * 
	 * @param enabled
	 *            <code>true</code> for enabled log; <code>false</code>
	 *            otherwise.
	 */
	public void setLogEnabled(boolean enabled) {
		setBooleanParameter(InternalSchema.LOG_ENABLED, enabled);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setUserComment(String comment) {
		setStringParameter(InternalSchema.USER_COMMENT, comment);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserComment() {
		return getStringParameter(InternalSchema.USER_COMMENT);
	}

	/**
	 * @return the Map object containing available objects for param
	 *         substitution.
	 */
	public Map<String, Object> getSubstitutionMap() {
		Map<String, Object> map = _paramMap;
		if (map == null && getParent() != null) {
			map = getParent().getSubstitutionMap();
		}

		if (map == null) {
			map = new SubstMap(getParent());
		}
		map.put("config", this);

		return map;
	}

	/**
	 * Sets the substitution map for this configuration object
	 * 
	 * @param map
	 *            the new substitution map
	 */
	public void setSubstitutionMap(Map<String, Object> map) {
		_paramMap = map;
	}

	/**
	 * This method combines all values from this object and its inherited
	 * objects into one single config object. After flattening, the object is a
	 * complete object with no inherited values except those from the excludedNS
	 * list.
	 * 
	 * @param excludedNS
	 *            List of namespaces to exclude from flattening
	 * @return <code>true</code> if object attempted flattening, FALSE is this
	 *         object inherits from an excluded namespace or has no inheritance
	 * @throws Exception
	 */
	public boolean flatten(List<String> excludedNS) throws Exception {

		boolean didFlatten = false;

		// Flatten children (may inherit from other objects)
		for (BaseConfiguration child : _children.values()) {
			if (child.flatten(excludedNS))
				didFlatten = true;
		}

		String inheritRef = getInheritsFromRef();
		BaseConfiguration inheritFrom = getInheritsFrom();

		while (inheritFrom != null && willFlatten(excludedNS, inheritRef)) {

			didFlatten = true;

			// Copy all simple values from inherited object
			List<String> simple = inheritFrom
					.getKeys(BaseConfiguration.ONE_LEVEL);
			for (int i = 0; i < simple.size(); i++) {
				String param = simple.get(i);
				if (!hasParameter(param))
					setParameter(param, inheritFrom.getParameter(param));
			}

			// Update inheritance reference if references anything but PARENT.
			// If A inherits from B inherits from C; then after flattening
			// references
			// to B we update our own (A) inheritsFrom to C and continue
			// flattening
			// until we have reached excluded or no-inheritance

			inheritRef = inheritFrom.getInheritsFromRef();
			BaseConfiguration parent = inheritFrom.getParent();

			// get the "real" inheritance reference
			while (parent != null
					&& BaseConfiguration.INHERIT_PARENT.equals(inheritRef)) {
				inheritRef = parent.getInheritsFromRef();
				parent = parent.getParent();
			}

			// This is now the new inheritance reference (e.g. See the above
			// example)
			if (inheritRef != null
					&& !BaseConfiguration.INHERIT_PARENT.equals(inheritRef))
				updateInheritsFrom(inheritRef);

			// Set inheritFrom to the next in the chain
			inheritFrom = inheritFrom.getInheritsFrom();
		}

		return didFlatten;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willFlatten(List<String> excludedNS) {
		return willFlatten(excludedNS, getInheritsFromRef());
	}

	/**
	 * @param ref
	 *            name of the object this object should inherit its values
	 * @param excludedNS
	 *            List of namespaces to exclude from flattening
	 * @return <code>true</code> if this object will attempt flattening given
	 *         the provided excluded namespaces.
	 */
	private boolean willFlatten(List<String> excludedNS, String ref) {
		if (ref != null && ref.contains(":")) {
			for (int i = 0; i < excludedNS.size(); i++) {
				if (ref.startsWith(excludedNS.get(i) + ":"))
					return false;
			}
		}
		return (ref != null && !BaseConfiguration.INHERIT_NONE.equals(ref));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getReferences(List<String> list) {
		List<String> refs = (list == null ? new ArrayList<String>() : list);
		String str = getInheritsFromRef();
		if (str != null && str.indexOf('/') >= 0) {
			if (!refs.contains(str))
				refs.add(str);
			try {
				((BaseConfiguration) getMetamergeConfig().lookup(str))
						.getReferences(refs);
			} catch (Exception err) {
				err.printStackTrace();
			}
		}

		if (getInheritsFrom() != null)
			getInheritsFrom().getReferences(refs);

		for (String key : getChildNames()) {
			BaseConfiguration child = getChild(key);
			if (child != null)
				child.getReferences(refs);
		}

		return refs;
	}

	/**
	 * @param bit
	 * @param value
	 * @return <code>true</code> if the and operation of bit and value is
	 *         greater than zero; <code>false</code> otherwise
	 */
	private boolean isBitSet(int bit, int value) {
		return (value & bit) > 0;
	}

	/**
	 * This method tell whether <code>value</code> matches <code>match</code>.
	 * Options include {@value BaseConfiguration#SEARCH_REGEX},
	 * {@value BaseConfiguration#SEARCH_EXACTCASE} and
	 * {@value BaseConfiguration#SEARCH_SUBSTRING}.
	 * 
	 * @param value
	 * @param match
	 * @param options
	 * @return <code>true</code> if "value" matches "match"; <code>false</code> otherwise
	 */
	private boolean valueMatches(String value, String match, int options) {
		String val = value;
		String m = match;
		if (!isBitSet(BaseConfiguration.SEARCH_EXACTCASE, options)) {
			val = (value != null ? value.toLowerCase() : null);
			m = (m != null ? m.toLowerCase() : null);
		}

		if (val == null || m == null)
			return false;

		if (isBitSet(BaseConfiguration.SEARCH_REGEX, options))
			return val.matches(m);
		else if (isBitSet(BaseConfiguration.SEARCH_SUBSTRING, options))
			return val.contains(m);
		else
			return val.equals(m);

	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Binding> search(String text, int options, int sizelimit) {
		return search(text, options, sizelimit, new ArrayList<Binding>());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Binding> search(String text, int options, int sizelimit,
			List<Binding> results) {
		if (results == null)
			results = new ArrayList<Binding>();
		List<String> keys = getKeys(BaseConfiguration.RECURSIVE_ONELEVEL);
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = null;
			if (isBitSet(BaseConfiguration.SEARCH_PARAMNAME, options)) {
				value = key;
			} else {
				value = getParameterPropertySource(key);
				// System.out.println ( "-- key=" + key + ", pps=" + value);
				if (value == null) {
					value = getStringParameter(key);
				} else if (value != null
						&& isBitSet(BaseConfiguration.SEARCH_PROPERTY, options)) {
					if (valueMatches(value, text, options))
						results.add(new Binding(getPath() + "/" + key, value));
					return results;
				}

			}

			// System.out.println ( "search: " + text + "=" + value + ": " +
			// value.matches(text));
			if (valueMatches(value, text, options))
				results.add(new Binding(getPath() + "/" + key, value));
		}

		// -- Search child objects
		for (String str : getChildNames()) {
			BaseConfiguration child = getChild(str);
			if (child != null)
				child.search(text, options, sizelimit, results);
		}

		return results;
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getChild(Object name) {
		return _children.get(name.toString());
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChild(Object name, BaseConfiguration config) {
		_children.put(name.toString(), config);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getChildNames() {
		return Arrays.asList(_children.keySet().toArray(new String[0]));
	}

	/**
	 * {@inheritDoc}
	 */
	public String nameForChild(BaseConfiguration config) {
		for (Map.Entry<String, BaseConfiguration> entry : _children.entrySet()) {
			if (entry.getValue() == config)
				return entry.getKey();
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public BaseConfiguration getChildForPath(String path)
			throws NameNotFoundException {
		BaseConfiguration top = this;

		// Get the top level path
		while (top.getParent() != null)
			top = top.getParent();

		StringTokenizer st = new StringTokenizer(path, "/");

		// First token is always top level object (e.g. top)
		if (!st.hasMoreTokens())
			throw new NameNotFoundException(path);
		st.nextToken();

		BaseConfiguration child = top;
		while (st.hasMoreTokens()) {
			String childName = st.nextToken();
			child = top.getChild(childName);
			if (child == null)
				throw new NameNotFoundException(top.getPath() + ": "
						+ childName);
			else
				top = child;
		}

		return child;
	}

	/**
	 * This method returns the path to this object. The path can be used to
	 * traverse a hierarchy of configuration objects/configuration values. The
	 * preferred name is the parent's name for this configuration object since
	 * we use the getChild() method to map between names and config objects. We
	 * do this since the name of this object can be different than the parent's
	 * name for the object, which basically happens when the parent container
	 * has several objects with the same name.
	 * 
	 * @return The path to this configuration object
	 */
	public String getPath() {
		String name = null;

		// Use parent containers name for the path (in case parent uses a
		// different name than this)
		if (getParent() != null) {
			name = getParent().nameForChild(this);
		}

		// If parent has no name then use shortname
		if (name == null) {
			name = getShortName();
		}

		if (name == null)
			name = "unnamed";

		// Ask parent for its path and append our own
		if (getParent() != null)
			name = getParent().getPath() + "/" + name;

		return name;

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean detachFromParent() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean reattachToParent(int position) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExpression(Object parameter) {
		Object value = getParameterRaw(parameter);
		if (value != null && value.toString().startsWith(_substitutePrefix))
			return true;
		else if (value != null && value.toString().startsWith(_propertyPrefix))
			return true;
		else
			return false;
	}

	/**
	 * Internal class extending HashMap used to contain available objects for
	 * param substitution.
	 */
	private static class SubstMap extends HashMap<String, Object> implements
			Serializable {

		/**
		 * Unique ID used for deserialization.
		 */
		static final long serialVersionUID = -7316979979253125006L;

		/**
		 * Parent configuration object.
		 */
		private BaseConfiguration parent;

		/**
		 * Constructor.
		 * 
		 * @param parent
		 *            parent configuration object
		 */
		public SubstMap(BaseConfiguration parent) {
			super();
			this.parent = parent;
		}

		/**
		 * @param key
		 * @return the value corresponding
		 */
		public Object get(Object key) {
			Object val = super.get(key);
			if ((val == null) && (parent != null)) {
				val = parent.getSubstitutionMap().get(key);
			}
			return val;
		}
	}

	/**
	 * Read the non-static and non-transient fields of the SubstMap class from
	 * specified stream and initialize some member variables of the config.
	 * 
	 * @param in
	 *            ObjectInputStream object
	 * @throws ClassNotFoundException
	 *             if the class of a serialized object could not be found.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		// our "pseudo-constructor"
		in.defaultReadObject();
		_listeners = new Vector<Object>();
		batchChange = false;
	}

	public long getModTS() {
		return timeStamp;
	}

	public void setModTS(long time) {
		timeStamp = time;
	}
}
