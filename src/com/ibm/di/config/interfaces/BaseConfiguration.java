/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.*;
import java.io.*;
import javax.naming.*;

import com.ibm.di.entry.Entry;

/**
 * This interface provides the basic methods used by all configuration
 * sub-classes. It provides a number of getter/setter methods for parameters as
 * well as methods for change notification. Every instance of BaseConfiguration
 * also has a pointer to the MetamergeConfig object that created it. When
 * changes are made, the MetamergeConfig object is also notified by calling the
 * setModified() method. Other objects are notified only if they have registered
 * for change notification.
 * <p>
 * Inheritance from other objects is only configured when the
 * setupInheritanceChain() method is called. The latter method retrieves the
 * <i>inherit from</i> attribute which names the object from which this object
 * inherits values. It is also possible to call the <i>setInheritsFrom()<i>
 * method to setup a temporary (non persistent) object for inheritance.
 * <p>
 * When inherited data is returned it should be cloned so that the object from
 * which we inherit is not tampered with. Configuration objects that return
 * inherited data (such as AttributeMapConfig) typically returns an orphaned
 * object. The class that returns such an orphaned object typically register
 * itself as a listener for change event. When a change event occurs, the
 * orphaned object is added to the local store for the object.
 * <p>
 * One example of such behavior is the AttributeMapConfig. It will return an
 * orphaned AttributeMapItem object and register itself for change notification.
 * When a change occurs in the AttributeMapItem object, the AttributeMapConfig
 * call its own <i>setAttribute()</i> method to add the object to the local
 * attribute map items list (and unregisters for notifications of course).
 */
public interface BaseConfiguration extends Serializable {

	/**
	 * Explicitly disregards inheritance
	 */
	public final static String INHERIT_NONE = "[no inheritance]";

	/**
	 * Inherit from parent
	 */
	public final static String INHERIT_PARENT = "[parent]";

	/**
	 * Return keys of this object
	 */
	public final static int ONE_LEVEL = 1;

	/**
	 * Return child entries's keys of this object
	 */
	public final static int SUBTREE = 2;

	/**
	 * Flag that determines if getKeys() should recurse to inherited object
	 */
	public final static int RECURSIVE = 4;

	/**
	 * Convenience for RECURSIVE + SUBTREE
	 */
	public final static int RECURSIVE_SUBTREE = 6;

	/**
	 * Convenience for RECURSIVE + ONE_LEVEL
	 */
	public final static int RECURSIVE_ONELEVEL = 5;

	/**
	 * Flag to disable inherited access (getParameter)
	 */
	public final static int DISABLE_INHERITANCE = 1;

	/**
	 * Flag to disable expansion of external properties (getParameter)
	 */
	public final static int DISABLE_EXTPROPS = 2;

	/**
	 * Separator between path components
	 * 
	 * @since 7.0
	 */
	public final static char CHILD_PATH_SEPARATOR = '.';

	/**
	 * The object's full name
	 * 
	 * @return The name value
	 */
	public Name getName();

	/**
	 * Gets the shortName attribute of the BaseConfiguration object. The short
	 * name is the last component in a multi component name. E.g.:
	 * AssemblyLines/MyName --> "MyName"
	 * 
	 * @return The shortName value
	 */
	public String getShortName();

	/**
	 * Sets the name attribute of the BaseConfiguration object
	 * 
	 * @param name
	 *            The new name value
	 */
	public void setName(Name name);

	/**
	 * Sets the name attribute of the BaseConfiguration object
	 * 
	 * @param name
	 *            The new name value
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public void setName(String name) throws Exception;

	/**
	 * Gets the namespace attribute of the BaseConfiguration object
	 * 
	 * @return The namespace value
	 */
	public Object getNamespace();

	/**
	 * Modified state
	 * 
	 * @return The modified value
	 */
	public boolean getModified();

	/**
	 * Sets the modified attribute of the BaseConfiguration object
	 * 
	 * @param modified
	 *            The new modified value
	 */
	public void setModified(boolean modified);

	/**
	 * Controls how the getParameter behaves in terms of inheritance and
	 * external properties.
	 * 
	 * @param flags
	 *            new value for the flags
	 */
	public void setFlags(int flags);

	/**
	 * Returns the flags which determines how the getParameter behaves in terms
	 * of inheritance and external properties.
	 * 
	 * @return value of the flags
	 */
	public int getFlags();

	/**
	 * Returns the value for a given config item without expanding the property
	 * value.
	 * 
	 * @param name
	 *            The parameter name
	 * @return The parameter value
	 */
	public Object getParameterRaw(Object name);

	/**
	 * Returns the value for a given config item. This method will call
	 * inherited object's getParameter if this object does not contain the named
	 * value. In case of returning an inherited value, the value from the
	 * inherited object will be cloned. It is up to the user to create a
	 * key/value local to this object by calling one of the setParameter
	 * methods.
	 * 
	 * @param name
	 *            The parameter name
	 * @return The parameter value
	 */
	public Object getParameter(Object name);

	/**
	 * Gets the parameter attribute of the BaseConfiguration object. If the
	 * parameter is not present a default value is assigned to the parameter
	 * using createValue.
	 * 
	 * @param name
	 *            The parameter name
	 * @param createValue
	 *            The value added to this object in case the parameter does not
	 *            exist
	 * @return The parameter value
	 */
	public Object getParameter(Object name, Object createValue);

	/**
	 * Returns the value for a given config item. This method will call
	 * inherited object's getParameter if this object does not contain the named
	 * value. In case of returning an inherited value, the value from the
	 * inherited object will be cloned. It is up to the user to create a
	 * key/value local to this object by calling one of the setParameter
	 * methods.
	 * <p>
	 * This method also conveys the List object to ParameterSubstitution calls
	 * to resolve circular references. If the parameter is not present even
	 * after calling inherited object's getParameter, a default value is
	 * assigned to the parameter using createValue.
	 * 
	 * @param name
	 *            The parameter name
	 * @param createValue
	 *            The value added to this object in case the parameter does not
	 *            exist
	 * @param cref
	 *            The circular references list of previously used config object
	 *            expressions
	 * @return The parameter value
	 */
	public Object getParameter(Object name, Object createValue,
			List<String> cref);

	/**
	 * Converts value from getParameter call to a string
	 * 
	 * @param name
	 *            The parameter name
	 * @return The stringParameter value
	 */
	public String getStringParameter(Object name);

	/**
	 * Converts value from getParameter call to a boolean
	 * 
	 * @param name
	 *            The parameter name
	 * @param defval
	 *            The default value if parameter does not exist
	 * @return The boolean value
	 */
	public boolean getBooleanParameter(Object name, boolean defval);

	/**
	 * Converts value from getParameter call to an int
	 * 
	 * @param name
	 *            The parameter name
	 * @param defval
	 *            The default value if parameter does not exist
	 * @return The integerParameter value
	 */
	public int getIntegerParameter(Object name, int defval);

	/**
	 * Sets the value for a given config item
	 * 
	 * @param name
	 *            Parameter name
	 * @param value
	 *            Parameter value
	 */
	public void setParameter(Object name, Object value);

	/**
	 * Stores the parameter value in the default password store if one is
	 * configured. Retrieval of the parameter is done through the standard
	 * getParameter methods.
	 * 
	 * @param name
	 *            Parameter name
	 * @param value
	 *            Parameter value
	 * @throws Exception
	 *             if error occurs while setting property to the property store
	 *             chosen by TDIProperties object associated with this config
	 */
	public void setProtectedParameter(Object name, Object value)
			throws Exception;

	/**
	 * Calls setParameter converting value to a Boolean object.
	 * 
	 * @param name
	 *            Parameter name
	 * @param value
	 *            Parameter value
	 */
	public void setBooleanParameter(Object name, boolean value);

	/**
	 * Calls setParameter converting value to an Integer object.
	 * 
	 * @param name
	 *            Parameter name
	 * @param value
	 *            Parameter value
	 */
	public void setIntegerParameter(Object name, int value);

	/**
	 * Calls setParameter.
	 * 
	 * @param name
	 *            Parameter name
	 * @param value
	 *            Parameter value
	 */
	public void setStringParameter(Object name, String value);

	/**
	 * Sets the value for a given config item, optionally notifying the change.
	 * If notify is <code>true</code>, the behavior is identical to
	 * setParameter( name, value ).
	 * 
	 * @param name
	 *            Parameter name
	 * @param value
	 *            Parameter value
	 * @param notify
	 *            If <code>true</code>, notify of the change.
	 * @since 6.1.1
	 */
	public void setParameter(Object name, Object value, boolean notify);

	/**
	 * Removes a local parameter from this object.
	 * 
	 * @param name
	 *            Parameter name
	 */
	public void removeParameter(Object name);

	/**
	 * Returns <code>true</code> if parameter name is local to this object.
	 * 
	 * @param name
	 *            Parameter name
	 * @return <code>true</code> if name is local.
	 */
	public boolean hasParameter(Object name);

	/**
	 * Returns <code>true</code> if the parameter is local and
	 * <code>false</code> if the parameter does not exist either in this
	 * object or in any inherited object.
	 * 
	 * @param name
	 *            Parameter name
	 * @return <code>true</code> if name is local; otherwise
	 *         <code>false</code>.
	 */
	public boolean isParameterLocal(Object name);

	/**
	 * Returns the user property name for a parameter.
	 * 
	 * @param name
	 *            Parameter name
	 * @return The user property source value.
	 */
	public String getParameterPropertySource(Object name);

	/**
	 * Sets the user property name to use for an attribute.
	 * 
	 * @param name
	 *            Parameter name
	 * @param propertySource
	 *            User property name
	 */
	public void setParameterPropertySource(Object name, String propertySource);

	/**
	 * Number of key/value pairs.
	 * 
	 * @return A count of local items in this object.
	 */
	public int size();

	/**
	 * Returns a TreeMap with key/value pairs for all local parameters.
	 * 
	 * @return The TreeMap object.
	 */
	public TreeMap getData();

	/**
	 * Sets the data to be used by this object. The data is passed by a TreeMap
	 * object containing key/value pairs.
	 * <p>
	 * Note: The TreeMap data is not cloned.
	 * 
	 * @param data
	 *            The new data value.
	 */
	public void setData(TreeMap data);

	/**
	 * Called to initialize to object.
	 * 
	 * @exception Exception
	 *                if the initialization fails.
	 */
	public void init() throws Exception;

	/**
	 * Returns an Iterator for the keys in the data object.
	 * 
	 * @return The dataIterator value.
	 */
	public Iterator<String> getDataIterator();

	/**
	 * Gets the keys attribute of the BaseConfiguration object
	 * 
	 * @param level
	 *            The level of keys returned.
	 * @return The list of keys
	 */
	public List<String> getKeys(int level);

	/**
	 * Checks whether event listeners are used.
	 * 
	 * @return <code>true</code> if event listeners are used; otherwise
	 *         <code>false</code>.
	 * @deprecated use the
	 *             {@link MetamergeConfigFactory#getUseConfigListeners()} method
	 *             instead
	 */
	@Deprecated
	public boolean getUseListeners();

	/**
	 * Specifies whether event listeners are used.
	 * 
	 * @param value
	 *            <code>true</code> indicates using event listeners;
	 *            <code>false</code> - not using
	 * @deprecated use the
	 *             {@link MetamergeConfigFactory#setUseConfigListeners(boolean)}
	 *             method instead
	 */
	@Deprecated
	public void setUseListeners(boolean value);

	/**
	 * Registers an object to be notified of change events in this object.
	 * 
	 * @param listener
	 *            The listener object to notify.
	 */
	public void addListener(MetamergeConfigChangeListener listener);

	/**
	 * Removes a listener from the notification list.
	 * 
	 * @param listener
	 *            The listener object to remove.
	 */
	public void removeListener(MetamergeConfigChangeListener listener);

	/**
	 * Notifies listeners for a change event.
	 * <p>
	 * Note: This method is typically called by setParameter method but not from
	 * user code.
	 * 
	 * @param source
	 *            The caller initiating the change event
	 * @param key
	 *            The key that changed
	 * @param operation
	 *            The operation performed on key
	 */
	public void notifyChange(Object source, Object key, int operation);

	/**
	 * Notifies listeners for a change event.
	 * <p>
	 * Note: This method is typically called by setParameter method but not from
	 * user code.
	 * 
	 * @param source
	 *            The caller initiating the change event
	 * @param key
	 *            The key that changed
	 * @param operation
	 *            The operation performed on key
	 * @param userObject
	 *            Arbitrary object provided by caller
	 */
	public void notifyChange(Object source, Object key, int operation,
			Object userObject);

	/**
	 * This method returns a {@link Entry} object constructed from the internal
	 * data structure.
	 * 
	 * @return The entry representing the internal data.
	 * @exception Exception
	 *                Any exception thrown by the implementing class.
	 */
	public Object toEntry() throws Exception;

	/**
	 * This method populates this object's internal data with the data from a
	 * {@link Entry} object.
	 * 
	 * @param entry
	 *            The entry containing data.
	 * @exception Exception
	 *                Any exception thrown by the implementing class.
	 */
	public void fromEntry(Object entry) throws Exception;

	/**
	 * Sets the parent object to which this object belongs. Many configuration
	 * objects are part of another object and cannot be addressed by the
	 * MetamergeConfig object. These objects should notify their parents of any
	 * changes made.
	 * 
	 * @param parent
	 *            The new parent value
	 */
	public void setParent(BaseConfiguration parent);

	/**
	 * Returns the parent object to which this object belongs.
	 * 
	 * @return The parent value.
	 */
	public BaseConfiguration getParent();

	/**
	 * Sets the MetamergeConfig object to which this object belongs.
	 * 
	 * @param mc
	 *            The new metamergeConfig object
	 */
	public void setMetamergeConfig(MetamergeConfig mc);

	/**
	 * Gets the MetamergeConfig object to which this object belongs.
	 * 
	 * @return The metamergeConfig object
	 */
	public MetamergeConfig getMetamergeConfig();

	/**
	 * Called to initialize the inheritance chain for this object.
	 * 
	 * @exception Exception
	 *                if the operation does not succeed
	 */
	public void setupInheritanceChain() throws Exception;

	/**
	 * Returns the object from which this object inherits data.
	 * 
	 * @return The inherited object.
	 */
	public BaseConfiguration getInheritsFrom();

	/**
	 * Gets the name of the object this object should inherit its values.
	 * 
	 * @return The name of the inherited object.
	 */
	public String getInheritsFromRef();

	/**
	 * Sets the name of the object from which this object should inherit its
	 * values.
	 * 
	 * @param ref
	 *            The new inheritsFromRef value
	 */
	public void setInheritsFromRef(String ref);

	/**
	 * Sets the name of the object from which this object should inherit its
	 * values and calls the setupInheritanceChain to activate the change.
	 * 
	 * @param ref
	 *            The new inheritsFromRef value
	 * @throws Exception
	 *             if the operation does not succeed
	 */
	public void updateInheritsFrom(String ref) throws Exception;

	/**
	 * Sets the object from which this object inherits data.
	 * <p>
	 * Note: Call this method to setup a temporary (non persistent) object for
	 * inheritance.
	 * 
	 * @param inherit
	 *            The new inheritsFrom value.
	 */
	public void setInheritsFrom(BaseConfiguration inherit);

	/**
	 * Returns the debug flag for the object.
	 * 
	 * @return The debug value.
	 */
	public boolean getDebug();

	/**
	 * Returns the debug flag for the object.
	 * 
	 * @param defval
	 *            The value to be returned, if the debug parameter cannot be
	 *            found
	 * 
	 * @return The debug value.
	 */
	public boolean getDebug(boolean defval);

	/**
	 * Sets the debug flag for the object.
	 * 
	 * @param debug
	 *            The new debug value
	 */
	public void setDebug(boolean debug);

	/**
	 * Gets the debugBreak attribute of the BaseConfiguration object.
	 * 
	 * @param defval
	 *            The value to be returned, if the debugBreak parameter cannot
	 *            be found
	 * 
	 * @return The debugBreak value.
	 */
	public boolean getDebugBreak(boolean defval);

	/**
	 * Sets the debugBreak attribute of the BaseConfiguration object.
	 * 
	 * @param debug
	 *            The new debugBreak value
	 */
	public void setDebugBreak(boolean debug);

	/**
	 * Gets the script attribute of the BaseConfiguration object.
	 * 
	 * @return The script value.
	 */
	public String getScript();

	/**
	 * Sets the script attribute of the BaseConfiguration object.
	 * 
	 * @param script
	 *            The new script value
	 */
	public void setScript(String script);

	/**
	 * Gets the ScriptEngine attribute of the BaseConfiguration object.
	 * 
	 * @return The ScriptEngine value.
	 */
	public String getScriptEngine();

	/**
	 * Sets the ScriptEngine attribute of the BaseConfiguration object.
	 * 
	 * @param engine
	 *            The new ScriptEngine value
	 */
	public void setScriptEngine(String engine);

	/**
	 * Gets the nullBehavior attribute of the BaseConfiguration object.
	 * 
	 * @return The nullBehavior value.
	 */
	public String getNullBehavior();

	/**
	 * Gets the nullBehaviorValue attribute of the BaseConfiguration object.
	 * 
	 * @return The nullBehaviorValue value.
	 */
	public String getNullBehaviorValue();

	/**
	 * Sets the nullBehavior attribute of the BaseConfiguration object.
	 * 
	 * @param nb
	 *            The new nullBehavior value
	 */
	public void setNullBehavior(String nb);

	/**
	 * Sets the nullBehaviorValue attribute of the BaseConfiguration object.
	 * 
	 * @param nbv
	 *            The new nullBehaviorValue value
	 */
	public void setNullBehaviorValue(String nbv);

	/**
	 * Gets the nullDefinition attribute of the BaseConfiguration object.
	 * 
	 * @return The nullDefinition value.
	 */
	public String getNullDefinition();

	/**
	 * Gets the nullDefinitionValue attribute of the BaseConfiguration object.
	 * 
	 * @return The nullDefinitionValue value.
	 */
	public String getNullDefinitionValue();

	/**
	 * Sets the nullDefinition attribute of the BaseConfiguration object.
	 * 
	 * @param nb
	 *            The new nullBehavior value
	 */
	public void setNullDefinition(String nb);

	/**
	 * Sets the nullDefinitionValue attribute of the BaseConfiguration object.
	 * 
	 * @param nbv
	 *            The new nullBehaviorValue value
	 */
	public void setNullDefinitionValue(String nbv);

	/**
	 * Gets the enabled attribute of the BaseConfiguration object.
	 * 
	 * @return The nullBehaviorValue value.
	 */
	public boolean getEnabled();

	/**
	 * Sets the enabled attribute of the BaseConfiguration object.
	 * 
	 * @param enabled
	 *            <code>true</code> for enabling; <code>false</code>
	 *            otherwise.
	 */
	public void setEnabled(boolean enabled);

	/**
	 * @return a cloned version of this object.
	 * @throws Exception
	 */
	public Object getClone() throws Exception;

	/**
	 * Gets the log enabled attribute of the BaseConfiguration object.
	 * 
	 * @return The nullBehaviorValue value.
	 */
	public boolean getLogEnabled();

	/**
	 * Sets the log enabled attribute of the BaseConfiguration object.
	 * 
	 * @param enabled
	 *            <code>true</code> for enabled log; <code>false</code>
	 *            otherwise.
	 */
	public void setLogEnabled(boolean enabled);

	/**
	 * Sets the user defined comment string.
	 * 
	 * @param comment
	 *            new value for user comment
	 */
	public void setUserComment(String comment);

	/**
	 * @return the user defined comment string.
	 */
	public String getUserComment();

	/**
	 * @return the Map object containing available objects for param
	 *         substitution.
	 */
	public Map<String, Object> getSubstitutionMap();

	/**
	 * Sets the substitution map for this configuration object.
	 * 
	 * @param map
	 *            the new value for the substitution map
	 */
	public void setSubstitutionMap(Map<String, Object> map);

	/**
	 * @param excludedNS
	 *            List of namespaces to exclude from flattening
	 * @return <code>true</code> if this object will attempt flattening given
	 *         the provided excluded namespaces.
	 */
	public boolean willFlatten(List<String> excludedNS);

	/**
	 * flatten - combines all values from this object and its inherited objects
	 * into one single config object. After flattening, the object is a complete
	 * object with no inherited values except those from the excludedNS list.
	 * 
	 * @param excludedNS
	 *            List of namespaces to exclude from flattening
	 * 
	 * @return <code>false</code> if this object did not flatten because of
	 *         inheritance from excludedNS; <code>true</code> otherwise.
	 * @throws Exception
	 */
	public boolean flatten(List<String> excludedNS) throws Exception;

	/**
	 * @param list
	 *            If non-null, the object must use the provided list rather than
	 *            creating its own
	 * @return a list of all references to other object from which this object
	 *         or its children inherit from.
	 */
	public List<String> getReferences(List<String> list);

	/**
	 * Search option. Indicates that only keys of this object will be searched.
	 */
	public static int SEARCH_ONELEVEL = 1;

	/**
	 * Search option. Indicates objects matching the exact case will be
	 * returned.
	 */
	public static int SEARCH_EXACTCASE = 2;

	/**
	 * Search option. Indicates objects matching a given regular expression will
	 * be returned.
	 */
	public static int SEARCH_REGEX = 4;

	/**
	 * Search option. Indicates that the parameter name will be used as a value
	 * to search for.
	 */
	public static int SEARCH_PARAMNAME = 8;

	/**
	 * Search option. Indicates that the user property name of the parameter or
	 * its value will be used as a value to search for.
	 */
	public static int SEARCH_PROPERTY = 16;

	/**
	 * Search option. Indicates that the value is checked for presence of the search string.
	 */
	public static int SEARCH_SUBSTRING  = 32;

	/**
	 * Search option. Convenience for SEARCH_PARAMNAME + SEARCH_REGEX.
	 */
	public static int SEARCH_PARAMNAME_RE = SEARCH_PARAMNAME | SEARCH_REGEX;

	/**
	 * Searches a configuration object and optionally its children for a
	 * specific key or value.
	 * 
	 * @param text
	 *            The search text
	 * @param options
	 *            Search options (oneLevel=1, exactCase=2, regExp=4,
	 *            paramName=8)
	 * @param sizelimit
	 *            Max number of hits returned
	 * @return List of {@link Binding} objects containing results from the
	 *         performed search.
	 */
	public List<Binding> search(String text, int options, int sizelimit);

	/**
	 * Searches a configuration object and optionally its children for a
	 * specific key or value.
	 * 
	 * @param text
	 *            The search text
	 * @param options
	 *            Search options (oneLevel=1, exactCase=2, regExp=4,
	 *            paramName=8)
	 * @param sizelimit
	 *            Max number of hits returned
	 * @param results
	 *            A list of results.
	 * @return the results from the performed search added into the provided
	 *         <code>results</code> parameter.
	 */
	public List<Binding> search(String text, int options, int sizelimit,
			List<Binding> results);

	/**
	 * Named children support
	 */

	/**
	 * @param name
	 *            The name of the configuration object
	 * @return the configuration object named <i>name</i>.
	 */
	public BaseConfiguration getChild(Object name);

	/**
	 * Associates a configuration object with a name in this configuration
	 * 
	 * @param name
	 *            The name
	 * @param config
	 *            The configuration object
	 */
	public void setChild(Object name, BaseConfiguration config);

	/**
	 * Returns the name for a configuration object
	 * 
	 * @param config
	 *            The configuration object
	 * @return The name associated with config or null if not found
	 */
	public String nameForChild(BaseConfiguration config);

	/**
	 * Returns a list of names for all child configuration object
	 * 
	 * @return list of names
	 */
	public List<String> getChildNames();

	/**
	 * Returns the complete path to this object. The complete path is built by
	 * concatenating the names (child names, not necessarily the actual object
	 * names as returned by getName()) for all objects in the parent chain. The
	 * parent chain typically starts with the major objects like
	 * AssemblyLineConfig etc.
	 * 
	 * @return The full path to this object
	 */
	public String getPath();

	/**
	 * This method traverses the child hierarchy (from the top) and returns the
	 * configuration object associated with the path. This method will in effect
	 * split the path in its components and recursively call getChild() to drill
	 * down to the last object in the path.
	 * 
	 * @param path
	 *            The complete path to an object (as returned by getPath())
	 * @return The configuration object associated with the path
	 * @throws NameNotFoundException
	 *             if the child could not be found.
	 */
	public BaseConfiguration getChildForPath(String path)
			throws NameNotFoundException;

	/**
	 * This method removes this objects from its parent container. If this
	 * cannot be removed from the parent the contents should be cleared (default
	 * implementation) and true be returned as if it were removed. If the object
	 * cannot be cleared or removed it should return false.
	 * 
	 * @return <code>true</code> if this object was removed;
	 *         <code>false</code> if it could not be removed.
	 * @since 7.0
	 */
	public boolean detachFromParent();

	/**
	 * This method reattaches this object to its former parent.
	 * 
	 * @param position
	 * 
	 * @return <code>true</code> if this object was reattached;
	 *         <code>false</code> if not;
	 * @since 7.0
	 */
	public boolean reattachToParent(int position);

	/**
	 * @param parameter name of the parameter
	 * @return <code>true</code> if the parameter is an expression.
	 * 
	 * @since 7.0
	 */
	public boolean isExpression(Object parameter);
	
	/**
	 * Signal a change event to listeners. This method is typically called by
	 * setParameter but not user code.
	 * 
	 * @param event
	 *            The event describing the change
	 * 
	 * @since 7.0
	 */
	public void notifyChange(MetamergeConfigChange event);
	
	/**
	 * Returns the last time this BaseConfiguration was changed.
	 * For internal use.
	 * @since 7.1.1
	 */
	public long getModTS();
	
	/**
	 * Sets the last time this BaseConfiguration was changed
	 * For internal use.
	 * @since 7.1.1
	 */
	public void setModTS(long time);
	
	/**
	 * Marks a parameter as needing protection (a password)
	 * @since 7.2
	 */
	public void setProtectedParameter(String name);
	
	/**
	 * Returns true if this is a parameter that needs to be protected (a password)
	 * @since 7.2
	 */
	public boolean isProtectedParameter(String name);
}
