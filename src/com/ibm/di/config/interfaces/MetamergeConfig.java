/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.interfaces;

import java.util.Enumeration;

/**
 * The MetamergeConfig interface specifies the methods used by the IBM Security
 * Directory Integrator to obtain configuration information. The configuration
 * interface is a hierarchical tree of objects and each object's unique name is
 * composed of a number of strings separated by a slash (see
 * MetamergeConfigFactory). If the implementor chooses another naming schema it
 * should perform a name mapping between the slash separated naming and its own
 * local naming schema.
 * 
 * Instantiation of a MetamergeConfig object should always performed by the
 * MetamergeConfigFactory. The latter builds a Hashtable of parameters which is
 * passed to the implementation of this interface.
 */

public interface MetamergeConfig extends MetamergeFolder {

	/**
	 * The following String constants can be used to lookup or create default
	 * folders using lookup or newInstanceOf(String).
	 */
	public final static String DEFAULT_LIBS_FOLDER = "Libs";

	public final static String DEFAULT_ASSEMBLYLINE_FOLDER = "AssemblyLines";

	public final static String DEFAULT_CONNECTOR_FOLDER = "Connectors";

	public final static String DEFAULT_PARSER_FOLDER = "Parsers";

	public final static String DEFAULT_SCRIPT_FOLDER = "Scripts";

	public final static String DEFAULT_LIBRARY_FOLDER = "JavaLibraries";

	public final static String DEFAULT_PROPERTY_FOLDER = "JavaProperties";

	public final static String DEFAULT_NAMESPACE_FOLDER = "Includes";

	public final static String DEFAULT_FORM_FOLDER = "Forms";

	public final static String DEFAULT_EXTPROP_FOLDER = "ExternalProperties";

	public final static String DEFAULT_SERVER_FOLDER = "Config";

	public final static String DEFAULT_CONFIG_FOLDER = "Config";

	public final static String DEFAULT_FUNCTION_FOLDER = "Functions";

	public final static String DEFAULT_ATTRIBUTEMAP_FOLDER = "AttributeMaps";

	public final static String DEFAULT_PROPSTORE_FOLDER = "Properties";

	public final static String DEFAULT_LOGGER_FOLDER = "Loggers";

	public final static String DEFAULT_SCHEDULER_FOLDER = "Schedules";
	
	public final static String DEFAULT_SEQUENCE_FOLDER = "Sequences";

	public final static String DEFAULT_SOLUTION_INTERFACE = "SolutionInterface";

	/**
	 * the following constants name the reserved names in various folders.
	 */
	public final static String DEFAULT_EXTPROP_NAME = " Default";

	public final static String DEFAULT_SERVER_LOG = "Logging";

	public final static String DEFAULT_SERVER_AUTOSTART = "AutoStart";

	public final static String DEFAULT_SERVER_TOMBSTONES = "Tombstones";

	public final static String DEFAULT_SERVER_INSTANCE_PROPERTIES = "Properties";

	/**
	 * The following constants are used to lookup or create folders using lookup
	 * or newInstanceOf(int)
	 */
	public final static int LIBS_FOLDER = 0;

	public final static int ASSEMBLYLINE_FOLDER = 1;

	public final static int CONNECTOR_FOLDER = 2;

	public final static int PARSER_FOLDER = 3;

	public final static int SCRIPT_FOLDER = 4;

	public final static int LIBRARY_FOLDER = 5;

	public final static int PROPERTY_FOLDER = 6;

	public final static int NAMESPACE_FOLDER = 7;

	public final static int FORM_FOLDER = 8;

	public final static int EXTPROP_FOLDER = 9;

	public final static int CONFIG_FOLDER = 10;

	public final static int LOGGING_FOLDER = 10;

	public final static int FUNCTION_FOLDER = 11;

	public final static int ATTRIBUTEMAP_FOLDER = 12;

	public final static int PROPSTORE_FOLDER = 13;

	public final static int LOGGER_FOLDER = 14;

	public final static int SCHEDULER_FOLDER = 15;

	public final static int SEQUENCE_FOLDER = 16;

	/**
	 * Returns an enumeration of the immediate children of this node
	 * 
	 * @param name
	 *            The folder name to list
	 * 
	 * @return Enumeration object of javax.naming.Binding objects with names and
	 *         objects.
	 * @exception Exception
	 */
	public Enumeration list(Object name) throws Exception;

	/**
	 * Searches this configuration for a named node.
	 * 
	 * @param name
	 *            The name to lookup
	 * @return The configuration object found
	 * @exception Exception
	 */
	public Object lookup(Object name) throws Exception;

	/**
	 * Removes a named object from this configuration.
	 * 
	 * @param name
	 *            The object name to remove
	 * @exception Exception
	 */
	public void unbind(Object name) throws Exception;

	/**
	 * Adds a new object to the configuration at the location specified by name
	 * 
	 * @param name
	 *            Name
	 * @param obj
	 *            Configuration Object
	 * @exception Exception
	 */
	public void bind(Object name, Object obj) throws Exception;

	/**
	 * Adds or replaces a new object to the configuration.
	 * 
	 * @param name
	 *            Name
	 * @param obj
	 *            Configuration Object
	 * @exception Exception
	 */
	public void rebind(Object name, Object obj) throws Exception;

	/**
	 * Renames an object. If new name is a simple name then location is
	 * unchanged, otherwise the object is moved.
	 * 
	 * @param name
	 *            Current Name
	 * @param newName
	 *            New name
	 * @exception Exception
	 */
	public void rename(Object name, Object newName) throws Exception;

	/**
	 * Tells the configuration driver to save all modified objects. Not all
	 * config drivers support this method. Typically drivers that work with
	 * streams (e.g. files) will implement this method to flush its cache to
	 * disk. The output parameter may be any object understood by the driver and
	 * serves as a temporary output destination. Use the setOutput() to
	 * permanently change the output URL for the driver.
	 * 
	 * @param output
	 *            Null or any object supported by the underlying driver.
	 * @param isSave
	 *            true if this is a permanent save, false if this just saves to
	 *            e.g. a command window.
	 * 
	 * @exception Exception
	 */
	public void commitChanges(Object output, boolean isSave) throws Exception;

	/**
	 * Tells the configuration driver to save all modified objects. This has the
	 * same effect as commitChanges( output, true)
	 * 
	 * @param output
	 *            Null or any object supported by the underlying driver.
	 * 
	 * @exception Exception
	 */
	public void commitChanges(Object output) throws Exception;

	/**
	 * Returns true if the configuration driver implements the commitChanges
	 * method and the current value for PROVIDER_URL is writable.
	 * 
	 * @return The committable state
	 */
	public boolean isCommittable();

	/**
	 * Returns true if this configuration cannot be modified.
	 * 
	 * @return The readOnly value
	 */
	public boolean isReadOnly();

	/**
	 * Changes the output to wich the configuration driver writes its
	 * configuration. This is different from calling commitChanges(output,
	 * false) where the output parameter is temporary (e.g. SaveCopyAs).
	 * 
	 * @param output
	 *            The new output value
	 * @exception Exception
	 */
	public void setOutput(Object output) throws Exception;

	/**
	 * Initialize the driver
	 * 
	 * @exception Exception
	 */
	public void initializeConfig() throws Exception;

	/**
	 * Close the driver
	 * 
	 * @exception Exception
	 */
	public void closeConfig() throws Exception;

	/**
	 * Copies a configuration object into this configuration. This differs from
	 * the bind/rebind method in that all copied objects are stored in a
	 * different branch than the original objects. Also, the copied objects'
	 * referenced objects may be copied in as well.
	 * 
	 * @param input
	 *            Configuration object to be copied
	 * @param destination
	 *            The name of the destination object (javax.naming.Name or
	 *            String)
	 * @param copyRefs
	 *            If true, copy referenced objects as well (e.g. all inherited
	 *            objects)
	 * 
	 * @exception Exception
	 */
	public void copy(BaseConfiguration input, Object destination, boolean copyRefs) throws Exception;

	/**
	 * Returns the external properties delegator object for this configuration.
	 * 
	 * @return The ExternalPropertiesConfig value
	 * @exception Exception
	 * @deprecated use {@link #getTDIProperties()} instead
	 */
	@Deprecated
	public ExternalPropertiesConfig getExternalProperties() throws Exception;

	/**
	 * Returns the ExternalPropertiesConfig object for the named external
	 * properties object. Name can either be a simple name or a fully qualified
	 * name. For simple names, the config driver will lookup the object in the
	 * default folder for external properties.
	 * 
	 * @param name
	 *            The external property object name
	 * 
	 * @return The ExternalPropertiesConfig object
	 * @deprecated use {@link #getTDIProperties()} instead
	 */
	@Deprecated
	public ExternalPropertiesConfig getExternalProperties(Object name) throws Exception;

	/**
	 * Adds a name component to the NameComponent attribute of the
	 * MetamergeConfig object
	 * 
	 * @param name
	 *            Null or existing javax.naming.Name object
	 * @param component
	 *            The component to add to name
	 * @param prefix
	 *            If true, component is inserted at beginning of name
	 * 
	 * @return The provided name or a new instance of javax.naming.Name
	 * @exception Exception
	 */
	public Object addNameComponent(Object name, String component, boolean prefix) throws Exception;

	/**
	 * Returns the AssemblyLineConfig object for the named assemblyline. Name
	 * can either be a simple name or a fully qualified name. For simple names,
	 * the config driver will lookup the assemblyline in its default folder for
	 * assemblylines.
	 * 
	 * @param name
	 *            Name
	 * 
	 * @return The configuration object
	 * @exception Exception
	 */
	public AssemblyLineConfig getAssemblyLine(Object name) throws Exception;

	/**
	 * Returns the SequenceConfig object for the named sequence. Name
	 * can either be a simple name or a fully qualified name.
	 * 
	 * @param name The name of the sequence
	 * 
	 * @return The configuration object, or null if no match is found
	 * @exception Exception
	 * @since 7.1.1
	 */
	public SequenceConfig getSequence(Object name) throws Exception;

	/**
	 * Returns the ConnectorConfig object for the named connector. Name can
	 * either be a simple name or a fully qualified name. For simple names, the
	 * config driver will lookup the connector in its default folder for
	 * connectors.
	 * 
	 * @param name
	 *            Name
	 * @return The configuration object
	 * @exception Exception
	 */
	public ConnectorConfig getConnector(Object name) throws Exception;

	/**
	 * Returns the ParserConfig object for the named parser. Name can either be
	 * a simple name or a fully qualified name. For simple names, the config
	 * driver will lookup the parser in its default folder for parsers.
	 * 
	 * @param name
	 *            Name
	 * 
	 * @return The configuration object
	 * @exception Exception
	 */
	public ParserConfig getParser(Object name) throws Exception;

	/**
	 * Returns the ScriptConfig object for the named script. Name can either be
	 * a simple name or a fully qualified name. For simple names, the config
	 * driver will lookup the script in its default folder for scripts.
	 * 
	 * @param name
	 *            Name
	 * 
	 * @return The configuration object
	 * @exception Exception
	 */
	public ScriptConfig getScript(Object name) throws Exception;

	/**
	 * Returns the FunctionConfig object for the named function. Name can either
	 * be a simple name or a fully qualified name. For simple names, the config
	 * driver will lookup the script in its default folder for functions.
	 * 
	 * @param name
	 *            Name
	 * 
	 * @return The configuration object
	 * @exception Exception
	 */
	public FunctionConfig getFunction(Object name) throws Exception;

	/**
	 * Returns the AttributeMapConfig object for the named map. Name can either
	 * be a simple name or a fully qualified name. For simple names, the config
	 * driver will lookup the map in its default folder for maps.
	 * 
	 * @param name
	 *            Name
	 * 
	 * @return The configuration object
	 * @exception Exception
	 * @since 7.1.1
	 */
	public AttributeMapConfig getAttributeMap(Object name) throws Exception;

	/**
	 * Returns the NamespaceConfig object for the named namespace. Name can
	 * either be a simple name or a fully qualified name. For simple names, the
	 * config driver will lookup the namespace in its default folder for
	 * namespaces.
	 * 
	 * @param name
	 *            Name
	 * 
	 * @return The configuration object
	 * @exception Exception
	 */
	public NamespaceConfig getNamespace(Object name) throws Exception;

	/**
	 * Returns the MetamergeFolder object for the default folder named by
	 * folder.
	 * 
	 * @param folder
	 *            The default folder constant
	 * @return The Folder object
	 * @exception Exception
	 */
	public MetamergeFolder getDefaultFolder(int folder) throws Exception;

	/**
	 * This method returns a new instance of the object type specified by
	 * typeName. Use the default folder names constants to denote the type. The
	 * new object is not added to the configuration.
	 * 
	 * @param typeName
	 *            The object type to create.
	 * 
	 * @return The newly created object.
	 * @exception Exception
	 */
	public BaseConfiguration newInstanceOf(Object typeName) throws Exception;

	/**
	 * This method returns a new instance of the object type specified by type.
	 * Use the default folder names constants to denote the type. The new object
	 * is not added to the configuration.
	 * 
	 * @param type
	 *            The object type to create
	 * 
	 * @return The newly created object.
	 * @exception Exception
	 */
	public BaseConfiguration newInstanceOf(int type) throws Exception;

	/**
	 * This method returns a new instance of the object type. Instead of using
	 * folder names, use interfaces to specify what you need. The new object is
	 * not added to the configuration.
	 * 
	 * @param type
	 *            The object type to create
	 * 
	 * @return The newly created instance.
	 * @exception Exception
	 */
	public <T extends BaseConfiguration> T newInstanceOf(Class<T> cls) throws Exception;

	/**
	 * This method returns the driver parameters as a BaseConfiguration object.
	 * 
	 * @return The newly created object.
	 * @exception Exception
	 */
	public BaseConfiguration getDriverParameters() throws Exception;

	/**
	 * This method sets the driver parameters from a BaseConfiguration object.
	 * 
	 * @param driverParams
	 *            The driver parameters
	 * @exception Exception
	 */
	public void setDriverParameters(BaseConfiguration driverParams) throws Exception;

	/**
	 * This method returns the value for a given driver parameter.
	 * 
	 * @param name
	 *            The name of the driver parameter ot get.
	 * 
	 * @return The driver parameter value
	 * @exception Exception
	 */
	public Object getDriverParameter(Object name) throws Exception;

	/**
	 * This method sets a driver parameter.
	 * 
	 * @param name
	 *            The driver parameter name
	 * @param value
	 *            The driver parameter value
	 * 
	 * @exception Exception
	 */
	public void setDriverParameter(Object name, Object value) throws Exception;

	public boolean isRemote();

	/**
	 * This method iterates the entire configuration to create java objects from
	 * the config drivers underlying store. This is needed when all references
	 * to other namespaces must be resolved (system and others).
	 * 
	 * @exception Exception
	 */
	public void instantiateAllObjects() throws Exception;

	/**
	 * This method returns the associated TDIProperties object
	 */
	public TDIProperties getTDIProperties() throws Exception;

	/**
	 * This method returns the SolutionInterface object for this configuration.
	 * 
	 */
	public SolutionInterface getSolutionInterface();

	/**
	 * @return The version of the configuration. e.g. "6.1.1", "7.0", "7.1",
	 *         etc.
	 * @since 7.0
	 */
	public String getConfigVersion();

	/**
	 * Returns the directory this config is placed in. If nothing can be
	 * determined, returns "."
	 * 
	 * @return The directory this config is placed in.
	 * @since 7.0
	 */
	public String getDirectory();
	
	/**
	 * Sets the value of modTSEnabled
	 * For internal use.
	 * @param value - If true, allows time stamps to be automatically modified
	 * @since 7.1.1
	 */
	public void setModTSEnabled(boolean value);
	
	/**
	 * Returns the value of modTSEnabled
	 * For internal use.
	 * @return true if time stamps are currently allowed to be automatically modified
	 * @since 7.1.1
	 */
	public boolean isModTSEnabled();
	
	/**
	 * Return true if protected parameters should be encrypted with server key.
	 * @since 7.2
	 */
	public boolean shouldEncryptProtected();
	
	/**
	 * Writes the configuration to output without server encryption of protected parameters.
	 * The output parameter may be any object understood by the driver and
	 * should be a temporary output destination, e.g. on another server.
	 * 
	 * @param output
	 *            Any object supported by the underlying driver.
	 * 
	 * @exception Exception
	 * @since 7.2
	 */
	public void commitChangesNoEncryption(Object output) throws Exception;
}
