/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.migration;

import java.util.Arrays;
import java.util.List;

/**
 * This class is used to describe the changes that need to be performed over a
 * particular property.
 * 
 * @since TDI 7.1
 */
public final class ChangeDescription {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * Specifies that the property should be commented.
	 */
	public static final int TYPE_COMMENT = 1 << 0;

	/**
	 * Specifies that the property should be uncommented.
	 */
	public static final int TYPE_UNCOMMENT = 1 << 1;

	/**
	 * Specifies that the property should be added.
	 */
	public static final int TYPE_ADD = 1 << 2;

	/**
	 * Specifies that the property should be modified.
	 */
	public static final int TYPE_MODIFY = 1 << 3;

	/**
	 * Specifies that the property should be deleted.
	 */
	public static final int TYPE_DELETE = 1 << 4;

	/**
	 * Holds the type of change that will be performed.
	 */
	private final int type;

	/**
	 * Holds the new value to add/modify
	 */
	private final String value;

	/**
	 * Specifies the key name to add the new property after
	 */
	private final String addAfterKey;

	/**
	 * Specifies whether the delete change should also delete the comments along
	 * with the property.
	 */
	private final boolean deleteComments;

	/**
	 * These are the lines of comments that will be put before the property that
	 * is just added. Each line will be prefixed with comment character and will
	 * be postfixed with an End-Of-Line character.
	 */
	private final List<String> addCommentLines;

	/**
	 * Specifies the number of End-Of-Line characters to add before the property
	 */
	private final int addNewLinesBefore;

	/**
	 * Specifies the number of End-Of-Line characters to add after the property
	 */
	private final int addNewLinesAfter;

	private final String propertyKey;

	/**
	 * Constructs an instance by providing the type of change only. This is
	 * useful for changes that don't need value operand - {@link #TYPE_COMMENT},
	 * {@link #TYPE_UNCOMMENT}, {@link #TYPE_DELETE}.
	 * 
	 * @param type
	 *            the type of change to create. The changes could be logically
	 *            OR'd to build a complex type of change.
	 * 
	 */
	public ChangeDescription(String propertyKey, int type) {
		this(propertyKey, type, null);
	}

	/**
	 * Constructs an instance by providing the type of change. This is useful
	 * for changes that don't need any other operands but a value -
	 * {@link #TYPE_ADD}, {@link #TYPE_MODIFY}. It could also be used with
	 * <code>null</code> for the following changes - {@link #TYPE_COMMENT},
	 * {@link #TYPE_UNCOMMENT}, {@link #TYPE_DELETE}
	 * 
	 * @param type
	 *            the type of change to create. The changes could be logically
	 *            OR'd to build a complex type of change.
	 * @param value
	 *            the value of the added/modified property or <code>null</code>
	 */
	public ChangeDescription(String propertyKey, int type, String value) {
		this(propertyKey, type, value, null, null, 0, 0, false);
	}

	/**
	 * Constructs an instance by providing the type of change. This is useful
	 * for changes describing addition of a property - {@link #TYPE_ADD}. It
	 * could also be used with nullified values for the following changes -
	 * {@link #TYPE_COMMENT}, {@link #TYPE_UNCOMMENT}, {@link #TYPE_DELETE},
	 * {@link #TYPE_MODIFY}
	 * 
	 * @param type
	 *            the type of change to create. The changes could be logically
	 *            OR'd to build a complex type of change.
	 * @param value
	 *            the value of the added/modified property or <code>null</code>
	 * @param addAfterKey
	 *            specifies the name of the key to add this property after. May
	 *            be <code>null</code>
	 * @param addCommentLines
	 *            specifies an array of lines of comments to add before this
	 *            property. Each string will be commented and put on a separate
	 *            line in the output configuration file. May be
	 *            <code>null</code>
	 * @param addNewLinesBefore
	 *            specifies the number of lines to add before the property (or
	 *            the comments if there are some)
	 * @param addNewLinesAfter
	 *            specifies the number of lines to add after the property
	 */
	public ChangeDescription(String propertyKey, int type, String value, String addAfterKey) {
		this(propertyKey, type, value, addAfterKey, null, 0, 0, false);
	}
	
	/**
	 * Constructs an instance by providing the type of change. This is useful
	 * for changes describing addition of a property - {@link #TYPE_ADD}. It
	 * could also be used with nullified values for the following changes -
	 * {@link #TYPE_COMMENT}, {@link #TYPE_UNCOMMENT}, {@link #TYPE_DELETE},
	 * {@link #TYPE_MODIFY}
	 * 
	 * @param type
	 *            the type of change to create. The changes could be logically
	 *            OR'd to build a complex type of change.
	 * @param value
	 *            the value of the added/modified property or <code>null</code>
	 * @param addAfterKey
	 *            specifies the name of the key to add this property after. May
	 *            be <code>null</code>
	 */
	public ChangeDescription(String propertyKey, int type, String value, String addAfterKey, String[] addCommentLines,
			int addNewLinesBefore, int addNewLinesAfter) {
		this(propertyKey, type, value, addAfterKey, addCommentLines, addNewLinesBefore, addNewLinesAfter, false);
	}

	/**
	 * Constructs an instance by providing the type of change. This is useful
	 * for changes describing deletion of a property - {@link #TYPE_DELETE}.
	 * 
	 * @param type
	 *            the type of change to create. The changes could be logically
	 *            OR'd to build a complex type of change.
	 * @param deleteCommentsBefore
	 *            specifies whether the comments right before the property
	 *            should be deleted.
	 */
	public ChangeDescription(String propertyKey, int type, boolean deleteCommentsBefore) {
		this(propertyKey, type, null, null, null, 0, 0, deleteCommentsBefore);
	}

	/**
	 * Called by the rest of the constructors to set the instance variables.
	 * 
	 * @param type
	 * @param value
	 * @param addAfterKey
	 * @param addCommentLines
	 * @param addNewLinesBefore
	 * @param addNewLinesAfter
	 * @param deleteComments
	 */
	private ChangeDescription(String propertyKey, int type, String value, String addAfterKey, String[] addCommentLines,
			int addNewLinesBefore, int addNewLinesAfter, boolean deleteComments) {
		this.propertyKey = propertyKey;
		this.type = type;
		this.value = value;
		this.addAfterKey = addAfterKey;
		this.addCommentLines = (addCommentLines != null) ? Arrays.asList(addCommentLines) : null;
		this.addNewLinesBefore = addNewLinesBefore;
		this.addNewLinesAfter = addNewLinesAfter;
		this.deleteComments = deleteComments;
	}

	/**
	 * @return the key name of the property that will be changed.
	 */
	public String getPropertyKey() {
		return propertyKey;
	}

	/**
	 * Answers <code>true</code> if the change type contains {@link #TYPE_ADD}
	 * 
	 * @return <code>true</code> if {@link #TYPE_ADD} is present,
	 *         <code>false</code> otherwise.
	 */
	public boolean isAdded() {
		return (type & TYPE_ADD) == TYPE_ADD;
	}

	/**
	 * Answers <code>true</code> if the change type contains
	 * {@link #TYPE_MODIFY}
	 * 
	 * @return <code>true</code> if {@link #TYPE_MODIFY} is present,
	 *         <code>false</code> otherwise.
	 */
	public boolean isModifyed() {
		return (type & TYPE_MODIFY) == TYPE_MODIFY;
	}

	/**
	 * Answers <code>true</code> if the change type contains
	 * {@link #TYPE_DELETE}
	 * 
	 * @return <code>true</code> if {@link #TYPE_DELETE} is present,
	 *         <code>false</code> otherwise.
	 */
	public boolean isDeleted() {
		return (type & TYPE_DELETE) == TYPE_DELETE;
	}

	/**
	 * Answers <code>true</code> if the change type contains
	 * {@link #TYPE_COMMENT}
	 * 
	 * @return <code>true</code> if {@link #TYPE_COMMENT} is present,
	 *         <code>false</code> otherwise.
	 */
	public boolean isCommented() {
		return (type & TYPE_COMMENT) == TYPE_COMMENT;
	}

	/**
	 * Answers <code>true</code> if the change type contains
	 * {@link #TYPE_UNCOMMENT}
	 * 
	 * @return <code>true</code> if {@link #TYPE_UNCOMMENT} is present,
	 *         <code>false</code> otherwise.
	 */
	public boolean isUncommented() {
		return (type & TYPE_UNCOMMENT) == TYPE_UNCOMMENT;
	}

	/**
	 * @return the value of the new property to add/modify
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the key name of the property to add this after
	 */
	public String getAddAfterKey() {
		return addAfterKey;
	}

	/**
	 * @return <code>true</code> if the interpreter should delete the comments
	 *         before this property.
	 */
	public boolean isDeletingComments() {
		return deleteComments;
	}

	/**
	 * @return the strings of the block of comments to put before this property.
	 */
	public List<String> getAddComment() {
		return addCommentLines;
	}

	/**
	 * @return the number of End-Of-Lines characters to put before this
	 *         property.
	 */
	protected int getAddNewLinesBefore() {
		return addNewLinesBefore;
	}

	/**
	 * @return the number of End-Of-Lines characters to put after this property.
	 */
	protected int getAddNewLinesAfter() {
		return addNewLinesAfter;
	}
}
