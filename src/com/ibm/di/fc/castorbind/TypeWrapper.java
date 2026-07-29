/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.castorbind;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class provides information for types supported by the Castor Function
 * Component. <b> String , Date , Boolean , Integer , Long , Double , Float ,
 * Big decimal , Byte , Short , Character , String array , Byte array , Char
 * array</b>
 * 
 * 
 */
public class TypeWrapper {
	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * An {@link ArrayList} holding the supported types
	 */
	public static final List mSupportedTypes = new ArrayList();

	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link String} object.
	 */
	public static final String TYPE_STRING = "string";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Date} object.
	 */

	public static final String TYPE_DATE = "date";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Boolean} object.
	 */

	public static final String TYPE_BOOLEAN = "boolean";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Integer} object.
	 */

	public static final String TYPE_INTEGER = "integer";

	// public static final String TYPE_OTHER = "other";

	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Long} object.
	 */
	public static final String TYPE_LONG = "long";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Double} object.
	 */

	public static final String TYPE_DOUBLE = "double";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Float} object.
	 */

	public static final String TYPE_FLOAT = "float";

	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link BigDecimal} object.
	 */

	public static final String TYPE_BIGDECIMAL = "big-decimal";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Byte} object.
	 */

	public static final String TYPE_BYTE = "byte";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Short} object.
	 */

	public static final String TYPE_SHORT = "short";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link Character} object.
	 */

	public static final String TYPE_CHAR = "character";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link StringArrayWrapper} object.
	 */

	public static final String TYPE_STRINGS = "strings";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link CharArrayWrapper} object.
	 */

	public static final String TYPE_CHARS = "chars";
	/**
	 * The key name passed to the {@link #getObject(String)} method that will
	 * return the inner {@link ByteArrayWrapper} object.
	 */

	public static final String TYPE_BYTES = "bytes";

	/**
	 * String to wrap.
	 */
	private String mString = null;
	/**
	 * Date to wrap.
	 */
	private Date mDate = null;
	/**
	 * Boolean object to wrap.
	 */
	private Boolean mBoolean = null;
	/**
	 * Integer object to wrap.
	 */
	private Integer mInteger = null;
	/**
	 * Long to wrap.
	 */
	private Long mLong = null;

	/**
	 * Double
	 */
	private Double mDouble = null;
	/**
	 * Float object to wrap.
	 */
	private Float mFloat = null;

	/**
	 * BigDecimal object to wrap.
	 */
	private BigDecimal mBigDecimal = null;
	/**
	 * Byte object to wrap.
	 */
	private Byte mByte = null;
	/**
	 * Short object to wrap.
	 */
	private Short mShort = null;
	/**
	 * Character object to wrap.
	 */
	private Character mChar = null;

	/**
	 * {@link StringArrayWrapper} to wrap.
	 */
	private StringArrayWrapper mSAWrapper = null;
	/**
	 * {@link CharArrayWrapper} to wrap.
	 */
	private CharArrayWrapper mCAWrapper = null;
	/**
	 * {@link ByteArrayWrapper} to wrap.
	 */
	private ByteArrayWrapper mBAWrapper = null;

	static {
		mSupportedTypes.add(TYPE_STRING);
		mSupportedTypes.add(TYPE_DATE);
		mSupportedTypes.add(TYPE_BOOLEAN);
		mSupportedTypes.add(TYPE_INTEGER);
		mSupportedTypes.add(TYPE_LONG);
		mSupportedTypes.add(TYPE_DOUBLE);
		mSupportedTypes.add(TYPE_FLOAT);
		mSupportedTypes.add(TYPE_BIGDECIMAL);
		mSupportedTypes.add(TYPE_BYTE);
		mSupportedTypes.add(TYPE_SHORT);
		mSupportedTypes.add(TYPE_CHAR);
		mSupportedTypes.add(TYPE_STRINGS);
		mSupportedTypes.add(TYPE_CHARS);
		mSupportedTypes.add(TYPE_BYTES);
	}

	/**
	 * Class constructor
	 */

	public TypeWrapper() {
	}

	/**
	 * Checks whether a type is supported by the FC
	 * 
	 * @param aType
	 *            type of parameter
	 * @return true if the type is supported
	 */
	public static boolean isSupported(String aType) {
		return mSupportedTypes.contains(aType);
	}

	/**
	 * This method returns {@link String} representation of the member variable
	 * 
	 * @return String
	 */
	public String getString() {
		return mString;
	}

	/**
	 * Sets member variable mString
	 * 
	 * @param aString
	 *            {@link String}
	 */
	public void setString(String aString) {
		mString = aString;
	}

	/**
	 * This method returns {@link Date} representation of the member variable
	 * 
	 * @return Date
	 */
	public Date getDate() {
		return mDate;
	}

	/**
	 * Sets member variable mDate
	 * 
	 * @param aDate
	 *            {@link Date}
	 */
	public void setDate(Date aDate) {
		mDate = aDate;
	}

	/**
	 * This method returns {@link Boolean} representation of the member variable
	 * 
	 * @return Boolean
	 */
	public Boolean getBoolean() {
		return mBoolean;
	}

	/**
	 * Sets member variable mBooolean
	 * 
	 * @param aBoolean
	 *            {@link Boolean}
	 */
	public void setBoolean(Boolean aBoolean) {
		mBoolean = aBoolean;
	}

	/**
	 * This method returns {@link Integer} representation of the member variable
	 * 
	 * @return Integer
	 */
	public Integer getInteger() {
		return mInteger;
	}

	/**
	 * Sets member variable mInteger
	 * 
	 * @param aInteger
	 *            {@link Integer}
	 */
	public void setInteger(Integer aInteger) {
		mInteger = aInteger;
	}

	/**
	 * This method returns {@link Long} representation of the member variable
	 * 
	 * @return Long
	 */
	public Long getLong() {
		return mLong;
	}

	/**
	 * Sets member variable mLong
	 * 
	 * @param aLong
	 *            {@link Long}
	 */
	public void setLong(Long aLong) {
		mLong = aLong;
	}

	/**
	 * This method returns {@link Double} representation of the member variable
	 * 
	 * @return Double
	 */
	public Double getDouble() {
		return mDouble;
	}

	/**
	 * Sets member variable mDouble
	 * 
	 * @param aDouble
	 *            {@link Double}
	 */
	public void setDouble(Double aDouble) {
		mDouble = aDouble;
	}

	/**
	 * This method returns {@link Float} representation of the member variable
	 * 
	 * @return Float
	 */
	public Float getFloat() {
		return mFloat;
	}

	/**
	 * Sets member variable mFloat
	 * 
	 * @param aFloat
	 *            {@link Float}
	 */
	public void setFloat(Float aFloat) {
		mFloat = aFloat;
	}

	/**
	 * This method returns {@link BigDecimal} representation of the member
	 * variable
	 * 
	 * @return BigDecimal
	 */
	public BigDecimal getBigDecimal() {
		return mBigDecimal;
	}

	/**
	 * Sets member variable mBigDecimal
	 * 
	 * @param aBigDecimal
	 *            {@link BigDecimal}
	 */
	public void setBigDecimal(BigDecimal aBigDecimal) {
		mBigDecimal = aBigDecimal;
	}

	/**
	 * This method returns {@link Byte} representation of the member variable
	 * 
	 * @return Byte
	 */
	public Byte getByte() {
		return mByte;
	}

	/**
	 * Sets member variable mByte
	 * 
	 * @param aByte
	 *            {@link Byte}
	 */
	public void setByte(Byte aByte) {
		mByte = aByte;
	}

	/**
	 * This method returns {@link Short} representation of the member variable
	 * 
	 * @return Short
	 */
	public Short getShort() {
		return mShort;
	}

	/**
	 * Sets member variable mShort
	 * 
	 * @param aShort
	 *            {@link Short}
	 */
	public void setShort(Short aShort) {
		mShort = aShort;
	}

	/**
	 * This method returns {@link Character} representation of the member
	 * variable
	 * 
	 * @return Character
	 */
	public Character getCharacter() {
		return mChar;
	}

	/**
	 * Sets member variable mChar
	 * 
	 * @param aChar
	 *            {@link Character}
	 */
	public void setCharacter(Character aChar) {
		mChar = aChar;
	}

	/**
	 * This method returns {@link StringArrayWrapper} representation of the
	 * member variable
	 * 
	 * @return StringArrayWrapper
	 */
	public StringArrayWrapper getStrings() {
		return mSAWrapper;
	}

	/**
	 * Sets member variable mSAWrapper
	 * 
	 * @param aStringArrayWrapper
	 *            {@link StringArrayWrapper}
	 */
	public void setStrings(StringArrayWrapper aStringArrayWrapper) {
		mSAWrapper = aStringArrayWrapper;
	}

	/**
	 * This method returns {@link CharArrayWrapper} representation of the member
	 * variable
	 * 
	 * @return CharArrayWrapper
	 */
	public CharArrayWrapper getChars() {
		return mCAWrapper;
	}

	/**
	 * Sets member variable mCAWrapper
	 * 
	 * @param aCharArrayWrapper
	 *            {@link CharArrayWrapper}
	 */
	public void setChars(CharArrayWrapper aCharArrayWrapper) {
		mCAWrapper = aCharArrayWrapper;
	}

	/**
	 * This method returns {@link ByteArrayWrapper} representation of the member
	 * variable
	 * 
	 * @return ByteArrayWrapper
	 */
	public ByteArrayWrapper getBytes() {
		return mBAWrapper;
	}

	/**
	 * Sets member variable mBAWrapper
	 * 
	 * @param aByteArrayWrapper
	 *            {@link ByteArrayWrapper}
	 */
	public void setBytes(ByteArrayWrapper aByteArrayWrapper) {
		mBAWrapper = aByteArrayWrapper;
	}

	/**
	 * The method accepts a String name of the specified type and returns an
	 * object of this type if it is supported , else it returns
	 * <code>null</code>
	 * 
	 * @see #TYPE_STRINGS
	 * @see #TYPE_BIGDECIMAL
	 * @see #TYPE_BOOLEAN
	 * @see #TYPE_BYTE
	 * @see #TYPE_BYTES
	 * @see #TYPE_CHAR
	 * @see #TYPE_CHARS
	 * @see #TYPE_DATE
	 * @see #TYPE_DOUBLE
	 * @see #TYPE_FLOAT
	 * @see #TYPE_INTEGER
	 * @see #TYPE_LONG
	 * @see #TYPE_SHORT
	 * @see #TYPE_STRINGS
	 * @param aType
	 *            the name of the Type
	 * @return the actual object, or null if the key name is not recognized.
	 */
	public Object getObject(String aType) {
		if (aType.equalsIgnoreCase(TYPE_STRING)) {
			return mString;
		}

		if (aType.equalsIgnoreCase(TYPE_DATE)) {
			return mDate;
		}

		if (aType.equalsIgnoreCase(TYPE_BOOLEAN)) {
			return mBoolean;
		}

		if (aType.equalsIgnoreCase(TYPE_INTEGER)) {
			return mInteger;
		}

		if (aType.equalsIgnoreCase(TYPE_LONG)) {
			return mLong;
		}

		if (aType.equalsIgnoreCase(TYPE_DOUBLE)) {
			return mDouble;
		}

		if (aType.equalsIgnoreCase(TYPE_FLOAT)) {
			return mFloat;
		}

		if (aType.equalsIgnoreCase(TYPE_BIGDECIMAL)) {
			return mBigDecimal;
		}

		if (aType.equalsIgnoreCase(TYPE_BYTE)) {
			return mByte;
		}

		if (aType.equalsIgnoreCase(TYPE_SHORT)) {
			return mShort;
		}

		if (aType.equalsIgnoreCase(TYPE_CHAR)) {
			return mChar;
		}

		if (aType.equalsIgnoreCase(TYPE_STRINGS)) {
			return mSAWrapper.getStrings();
		}

		if (aType.equalsIgnoreCase(TYPE_CHARS)) {
			return mCAWrapper.getChars();
		}

		if (aType.equalsIgnoreCase(TYPE_BYTES)) {
			return mBAWrapper.getBytes();
		}

		return null;
	}

}
