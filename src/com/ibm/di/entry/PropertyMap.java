/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.entry;

import java.util.ArrayList;
import java.util.Collection;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ibm.di.exceptions.DOMException;

public class PropertyMap extends ArrayList<Property> implements NamedNodeMap {

	protected static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	private static final long serialVersionUID = -389712683134427893L;

	PropertyMap() {
		super();
	}

	/**
	 * @return the number of Properties in this PropertyMap
	 */
	public int getLength() {
		return size();
	}

	/**
	 * Looks up a Property using the specified qualified name.
	 * 
	 * @return the Property that is a result of the search operation.
	 */
	public Property getNamedItem(String name) {
		int pos = getNamedItemPos(name);
		return pos != -1 ? get(pos) : null;
	}

	/**
	 * Looks up a Property using the specified namespace URI and localName.
	 * 
	 * @return the first Property found that match the specified criterias.
	 */
	public Property getNamedItemNS(String namespaceURI, String localName) throws DOMException {
		int pos = getNamedItemNSPos(namespaceURI, localName);
		return pos != -1 ? get(pos) : null;
	}

	/**
	 * @return the property at the specified position
	 */
	public Property item(int index) {
		return get(index);
	}

	/**
	 * Removes the Property referenced by the given qualified name
	 * 
	 * @return the removed Property, or null if a Property with that qualified
	 *         name could not be found.
	 */
	public Property removeNamedItem(String name) throws DOMException {

		Property removed = getNamedItem(name);

		if (removed != null) {
			this.remove(removed);
			return removed;
		}

		return null;
	}

	/**
	 * Removes the Property referenced by the given namespace URI and localName
	 * 
	 * @return the removed Property, or null if a Property with that namespace
	 *         URI and localName could not be found.
	 */
	public Property removeNamedItemNS(String namespaceURI, String localName) throws DOMException {

		Property removed = (Property) getNamedItemNS(namespaceURI, localName);

		if (removed != null) {
			this.remove(removed);
			return removed;
		}

		return null;
	}

	/**
	 * Adds a Property using its nodeName attribute. As the nodeName attribute
	 * is used to derive the name which the Property must be stored under.
	 * 
	 * @return If the new Property replaces an existing property the replaced
	 *         Property is returned, otherwise null is returned.
	 * @param property
	 *            A node to store in a named node map.
	 */
	public Property setNamedItem(Node property) throws DOMException {

		int pos = getNamedItemPos(property.getNodeName());

		if (pos != -1) {
			Property temp = (Property) this.get(pos);
			this.set(pos, (Property) property);
			return temp;
		} else {
			this.add((Property) property);
			return null;
		}
	}

	/**
	 * Adds a node using its namespaceURI and localName.
	 * 
	 * @return If the new Property replaces an existing Property the replaced
	 *         Property is returned, otherwise null is returned.
	 * @param property
	 *            A node to store in a named node map.
	 */
	public Property setNamedItemNS(Node property) throws DOMException {

		int pos = getNamedItemNSPos(property.getNamespaceURI(), property.getLocalName());

		if (pos != -1) {
			Property temp = (Property) this.get(pos);
			this.set(pos, (Property) property);
			return temp;
		} else {
			this.add((Property) property);
			return null;
		}
	}

	private int getNamedItemPos(String name) {
		for (int i = 0; i < size(); i++) {
			if (get(i).getNodeName().equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private int getNamedItemNSPos(String namespaceURI, String localName) {

		if (localName == null)
			return -1;

		for (int i = 0; i < size(); i++) {
			Property prop = get(i);
			String aNamespaceURI = prop.getNamespaceURI();
			String aLocalName = prop.getLocalName();

			if (namespaceURI == null) {
				if (aNamespaceURI == null && localName.equals(aLocalName))
					return i;
			} else {
				if (namespaceURI.equals(aNamespaceURI) && localName.equals(aLocalName))
					return i;
			}
		}
		return -1;
	}

	/**
	 * Clones the PropertyMap and all the Property objects in it. The new
	 * Property objects's parent references are pointed to the Attribute passed
	 * as an argument.
	 * 
	 * @param owner
	 *            - the Attribute object that will be set as the owner of the
	 *            newly created Properties
	 * @return a deep clone of this PropertyMap object
	 */
	public PropertyMap cloneMap(Attribute owner) {
		PropertyMap result = new PropertyMap();
		for (int i = 0; i < size(); i++) {
			Property prop = (Property) get(i);

			Property propClone = (Property) prop.cloneNode(true);
			propClone.setParent(owner);

			result.add(propClone);
		}
		return result;
	}

	// *************************************************************************
	// just for protection, cause we do not want anything else int the arrayList

	/**
	 * Adds the prop object at the specified position if its type is Property
	 * otherwise nothing is added.
	 */
	@Override
	public void add(int pos, Property prop) {

		if (prop != null)
			super.add(pos, prop);
		else
			return;
	}

	/**
	 * Adds the prop object if its type is Property otherwise nothing is added
	 */
	@Override
	public boolean add(Property prop) {

		if (prop != null)
			return super.add(prop);
		else
			return false;
	}

	/**
	 * Adds only the Property objects to this PropertyMap
	 */
	@Override
	public boolean addAll(Collection<? extends Property> arg0) {
		return this.addAll(size(), arg0);
	}

	/**
	 * Adds only the Property objects to this PropertyMap, starting from the
	 * specified position.
	 */
	@Override
	public boolean addAll(int pos, Collection<? extends Property> colection) {
		return super.addAll(pos, colection);
	}
}
