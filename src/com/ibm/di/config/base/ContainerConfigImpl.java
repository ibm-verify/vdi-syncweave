/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.config.base;

import java.util.*;

import com.ibm.di.config.interfaces.*;

/**
 * Implements a Container containing BaseConfiguration objects.
 */
public class ContainerConfigImpl extends BaseConfigurationImpl implements
		ContainerConfig {
	/**
	 * Copyright
	 */
	@SuppressWarnings("unused")
	private final static String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	static final long serialVersionUID = -4134004409592694052L;

	/**
	 * Collection of config objects in the container
	 */
	protected Vector<BaseConfiguration> items;

	/**
	 * Constructor for the ContainerConfigImpl object
	 */
	public ContainerConfigImpl() {
		super();
		createChildrenProp();
//		items = (Vector) getParameter("Children", new Vector());
	}

	/**
	 * Constructor for the ContainerConfigImpl object
	 * 
	 * @param data
	 *            TreeMap of attribute/value pairs
	 */
	public ContainerConfigImpl(Object data) {
		super(data);
		createChildrenProp();
//		items = (Vector) getParameter("Children", new Vector());
	}
	
	@SuppressWarnings("unchecked")
	private void createChildrenProp() {
		Object obj = getParameter(InternalSchema.CONTAINER_CHILDREN);
		if(obj == null || (obj instanceof String && obj.equals("[]")))
			items = new Vector<BaseConfiguration>();
		else if (obj instanceof Vector)
			items = (Vector)obj;
		else
			System.out.println("Bad value for Children: " + obj);
		
		if(items == null)
			items = new Vector<BaseConfiguration>();

		setParameter(InternalSchema.CONTAINER_CHILDREN, items, false);
	}

	/**
	 * This method returns the number of config objects in the container
	 * 
	 * @return Number of config objects
	 */
	public int size() {
		return items.size();
	}

	/**
	 * Returns a list of config items from this object and inherited containers
	 */
	public List<BaseConfiguration> getInheritedConfigurations(List<BaseConfiguration> list) {
		List<BaseConfiguration> dest = (list == null ? new ArrayList<BaseConfiguration>() : list);
		if (getInheritsFrom() instanceof ContainerConfig) {
			((ContainerConfig) getInheritsFrom()).getConfigurations(dest);
			((ContainerConfig) getInheritsFrom()).getInheritedConfigurations(dest);
		}

		return dest;
	}

	/**
	 * Returns a flattened list of the entire tree of config items.
	 * 
	 * @param list
	 *            If not null, config items are added to this list
	 * @return The provided list or a new list object with the contents of this
	 *         container and child containers
	 */
	public List<BaseConfiguration> getConfigurations(List<BaseConfiguration> list) {
		List<BaseConfiguration> dest = (list == null ? new ArrayList<BaseConfiguration>() : list);
		for (int i = 0; i < size(); i++) {
			BaseConfiguration c = getConfig(i);
			dest.add(c);
			if (c instanceof ContainerConfig)
				((ContainerConfig) c).getConfigurations(dest);
		}
		return dest;
	}

	/**
	 * Returns the position of a configuration object in a container
	 * 
	 * @param config
	 *            Configuration object in the container
	 * @return The position of the Configuration object in the collection of
	 *         configuration objects in the container
	 */
	public int indexOf(BaseConfiguration config) {
		if (config == null) {
			return -1;
		} else {
			return items.indexOf(config);
		}
	}

	/**
	 * Returns the index of the config
	 * 
	 * @param name
	 *            The name of the configuration object
	 * @return The position of the configuration object in the collection of
	 *         configuration objects
	 */
	public int indexOf(String name) {
		if (name == null)
			return -1;
		for (int i = 0; i < size(); i++) {
			BaseConfiguration bc = items.get(i);
			if (name.equals(bc.getShortName()))
				return i;
		}
		return -1;
	}

	/**
	 * This method returns the configuration object, which is at position index
	 * 
	 * @param index
	 *            The position of the configuration object
	 * @return Class representing the configuration of the object
	 */
	public BaseConfiguration getConfig(int index) {
		if (index < 0 || index >= size()) {
			return null;
		}
		BaseConfiguration bc = items.get(index);
		bc.setParent(this);
		return bc;
	}

	/**
	 * Returns the config with the specified name
	 * 
	 * @param name
	 *            The config's name
	 * @return The connector configuration object or null if config wasn't found
	 */
	public BaseConfiguration getConfig(String name) {
		return getConfig(name, false);
	}

	/**
	 * Returns the config with the specified name
	 * 
	 * @param name
	 *            The config's name
	 * @param recursive
	 *            If true, searches through all the components in a container
	 * 
	 * @return The connector configuration object or null if config wasn't found
	 */
	public BaseConfiguration getConfig(String name, boolean recursive) {
		for (int i = 0; i < size(); i++) {
			BaseConfiguration bc = getConfig(i);
			if (name.equals(bc.getShortName())) {
				return bc;
			} else if (recursive && bc instanceof ContainerConfig) {
				bc = ((ContainerConfig) bc).getConfig(name, recursive);
				if (bc != null) {
					return bc;
				}
			}
		}
		return null;
	}

	/**
	 * This method adds a configuration object to the container
	 * 
	 * @param config
	 *            The configuration object
	 * @return Index of the new configuration object
	 */
	public int addConfig(BaseConfiguration config) {
		items.add(config);
		int index = items.indexOf(config);
		config.setParent(this);
		notifyChange(this, "ComponentList", MetamergeConfigChange.MCC_SET,
				new Object[] { Integer.valueOf(index), config });
		return size() - 1;
	}

	/**
	 * This method inesrts a configuration object at the specified index
	 * 
	 * @param config
	 *            The configuration object
	 * @param position
	 *            Position of the new object
	 * @return Index of the new configuration object
	 */
	public int insertConfig(BaseConfiguration config, int position) {
		if (position >= size() || position < 0) {
			return addConfig(config);
		} else {
			config.setParent(this);
			items.insertElementAt(config, position);
			notifyChange(this, "ComponentList", MetamergeConfigChange.MCC_SET,
					new Object[] { Integer.valueOf(position), config });
			return indexOf(config);
		}
	}

	/**
	 * This method removes a configuration object from the container
	 * 
	 * @param index
	 *            Index of the configuration object
	 * @return The configuration object that is removed
	 */
	public BaseConfiguration removeConfig(int index) {
		BaseConfiguration c = items.remove(index);
		notifyChange(this, "ComponentList", MetamergeConfigChange.MCC_REMOVE,
				new Object[] { Integer.valueOf(index), c });
		return c;
	}

	/**
	 * This method removes a configuration object from the container
	 * 
	 * @param config
	 *            The configuration object to remove
	 * @return true if the remove operation is successful. Otherwise, returns
	 *         false.
	 */
	public boolean removeConfig(BaseConfiguration config) {
		int index = indexOf(config);
		if (index == -1) {
			return false;
		}

		removeConfig(index);
		return true;
	}

	/**
	 * This method removes a configuration object from the container
	 * 
	 * @param name
	 *            The name of the configuration to remove
	 * @param recursive
	 *            If true, searches through all the components in a container
	 * @return true, if the remove operation is successful. Otherwise returns
	 *         false.
	 */
	public boolean removeConfig(String name, boolean recursive) {
		for (int i = 0; i < items.size(); i++) {
			BaseConfiguration bc = items.get(i);
			if (name.equals(bc.getShortName())) {
				removeConfig(i);
				return true;
			} else if (recursive && bc instanceof ContainerConfig
					&& ((ContainerConfig) bc).removeConfig(name, recursive)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Moves a connector one position up or down
	 * 
	 * @param position
	 *            Current connector position
	 * @param up
	 *            Up (true) or down (false)
	 * @return true if the operation succeeded
	 */
	public boolean moveConfig(int position, boolean up) {
		if (position < 0 || position >= size()) {
			return false;
		}

		if (up && position == 0) {
			return false;
		}
		if (!up && position + 1 >= size()) {
			return false;
		}

		BaseConfiguration obj = getConfig(position);
		if (obj == null) {
			return false;
		}

		removeConfig(position);

		if (up) {
			items.insertElementAt(obj, position - 1);
		} else {
			items.insertElementAt(obj, position + 1);
		}

		notifyChange(this, "ComponentList", MetamergeConfigChange.MCC_SET, obj);
		return true;
	}

	/**
	 * Moves a config one position up or down
	 * 
	 * @param up
	 *            Up (true) or down (false)
	 * @param config
	 *            The configuration object to move.
	 * @return true if the operation succeeded
	 */
	public boolean moveConfig(BaseConfiguration config, boolean up) {
		return moveConfig(indexOf(config), up);
	}

	/**
	 * Moves a config from one position to another
	 * 
	 * @param position
	 *            New position
	 * @param config
	 *            The configuration object to move
	 * @return The current position of the configuration
	 */
	public int moveConfig(BaseConfiguration config, int position) {
		int current = indexOf(config);
		if (current == position) {
			return current;
		} else if (current == -1) {
			return -1;
		}

		removeConfig(current);
		// items.removeElementAt(current);
		if (current < position) {
			position--;
		}

		if (position == -1 || position > items.size())
			position = items.size();

		items.insertElementAt(config, position);

		notifyChange(this, "ComponentList", MetamergeConfigChange.MCC_SET,
				new Object[] { Integer.valueOf(position), config });

		return position;
	}

	/**
	 * Returns true if there is a config having the provided name.
	 * 
	 * @param name
	 *            The name of the config item
	 * @param recursive
	 *            If true, a tree walk will be performed when checking for the
	 *            name
	 * @return TRUE if there is an object with the provided name
	 */
	public boolean containsConfig(String name, boolean recursive) {
		for (BaseConfiguration bc: items) {
			if (name.equalsIgnoreCase(bc.getShortName())) {
				return true;
			} else if ((recursive && bc instanceof ContainerConfig) &&
					(((ContainerConfig) bc).containsConfig(name, recursive))) {
				return true;
			}
		}
		return false;
	}

	public BaseConfiguration getChild(Object name) {
		BaseConfiguration child = super.getChild(name);
		if(child == null)
			child = getConfig(name.toString());
		return child;
	}

	public List<String> getChildNames() {
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(super.getChildNames());
		for (BaseConfiguration b: items)
			list.add(b.getShortName());
		return list;
	}

	public void setupInheritanceChain() throws Exception {
		super.setupInheritanceChain();
		for (int i = 0; i < size(); i++)
			getConfig(i).setupInheritanceChain();
	}

	public Object getClone() throws Exception {
		ContainerConfigImpl cc = new ContainerConfigImpl();
		cc.setName(getName());
		cc.init();
		for(BaseConfiguration obj : items) {
			Object clone = obj.getClone();
			cc.addConfig((BaseConfiguration)clone);
		}
		cc.setModTS(getModTS());
		return cc;
	}

}

