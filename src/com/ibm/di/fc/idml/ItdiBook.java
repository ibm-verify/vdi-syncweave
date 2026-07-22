/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.idml;

import static com.ibm.di.cdm.core.CDMConstants.CDM_PREFIX;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

import org.xml.sax.InputSource;

import com.ibm.di.entry.Entry;
import com.ibm.di.fc.idml.IdMLConstants.Operations;
import com.ibm.di.server.ResourceHash;
import com.ibm.dl.core.certification.IdMLCertification;
import com.ibm.dl.production.IDMLBook;
import com.ibm.dl.production.IDMLInvalidOperationException;
import com.ibm.dl.production.IDMLManagedElement;
import com.ibm.dl.production.IDMLManagementSoftwareSystem;
import com.ibm.dl.production.IDMLRelationship;
import com.ibm.dl.production.interfaces.IDMLBookWriter;
import com.ibm.dl.production.utils.OutputStreamBookWriter;
import com.ibm.dl.production.utils.XMLUtilities;

/**
 * This wraps the functionality of an IDMLBook and enhances it with the
 * additional functionality needed by the IdML Components.
 */
public class ItdiBook {

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	/**
	 * The properties file containing messages.
	 */
	private static final String PROPERTIES_FILE = "openidmlfc";

	/**
	 * NLS Property set holding name-value pairs for the resource.
	 */
	private static ResourceHash resHash = ResourceHash.getHash(PROPERTIES_FILE);

	/**
	 * The version of the currently used IdML schema.
	 */
	private static final String IDML_SCHEMA_VERION = "0.8";

	/**
	 * One of the two modes supported by the IdML schema - delta.
	 */
	public static final String DELTA_TYPE_IDML = "DELTA";

	/**
	 * One of the two modes supported by the IdML schema - refresh.
	 */
	public static final String REFRESH_TYPE_IDML = "REFRESH";

	/**
	 * Determines that the generated IdML documents will be stored as a file.
	 */
	public static final int STORE_AS_FILE = 0;

	/**
	 * Determines that the generated IdML documents will be stored in the
	 * memory.
	 */
	private static final int STORE_IN_MEMORY = 1;

	/**
	 * The CDM class type of the MSS's name.
	 */
	private static final String MSS_NAME = "cdm:MSSName";

	/**
	 * The CDM class type of the MSS's manufacturer name.
	 */
	private static final String MSS_MANUFACTURER_NAME = "cdm:ManufacturerName";

	/**
	 * The CDM class type of the MSS's product name.
	 */
	private static final String MSS_PRODUCT_NAME = "cdm:ProductName";

	/**
	 * A prefix denoting that an attribute is extended.
	 */
	private static final String EXTENDED_ATTRIBUTE_PREFIX = "cdm:extattr:";

	/**
	 * The name of the IdML book. It is specified by the user (as opposed to the
	 * auto-generated file name) and is used for the books' static sharing.
	 */
	private String bookName;

	/**
	 * Determines whether this book will use the refresh mode as opposed to
	 * delta mode.
	 */
	private boolean refresh;

	/**
	 * Determines whether this book should be validated using the DL
	 * Certification tool, after its is generated.
	 */
	private boolean validateEnabled;

	/**
	 * The stream used for storing the IdML contents either to to file or in
	 * memory.
	 */
	private OutputStream bookStream;

	/**
	 * Determines if the book is currently opened. An opened book permits adding
	 * new CIs and Relationships to it. If the book is closed, it is
	 * irreversible (it cannot be opened again).
	 */
	private boolean opened = false;

	/**
	 * The IdMLBook object used for dealing with the book itself.
	 */
	private IDMLBook idmlBook;

	/**
	 * Determines how the book will be stored - as a file or in memory.
	 */
	private int storageType;

	/**
	 * The name of the directory where the IdML book will be stored. It is
	 * applicable only if the book will be stored as a file. For in-memory books
	 * this parameter is not used.
	 */
	private String directoryName;

	/**
	 * Holds the information of the MSS, which created this IdML book.
	 */
	private IDMLManagementSoftwareSystem mss;

	/**
	 * Holds the version of the Common Data Model used in the IdML book (e.g.
	 * 2.10.2).
	 */
	private String cdmVersion;

	/**
	 * A counter used to provide unique ids for each CI added to the book.
	 */
	private long idCounter;

	/**
	 * Holds the operation used for the last added CI/Relationship.
	 * 
	 */
	private Operations currentOperation;

	/**
	 * A counter used to keep track of the count of artifacts (CIs and
	 * Relationships) added to the IdML document.
	 */
	private long artifactCount;

	/**
	 * Constructor.
	 * 
	 * @param bookName
	 *            the name of this book.
	 */
	public ItdiBook(String bookName) {
		this.bookName = bookName;
		opened = false;
		idCounter = 0;
		artifactCount = 0;
	}

	/**
	 * Returns the number of artifacts (CIs and Relationships) added to the IdML
	 * so far.
	 * 
	 * @return artifact count.
	 */
	public long getArtifactCount() {
		return artifactCount;
	}

	/**
	 * Returns the contents of the OutputStream used for storing the IdML book.
	 * If the book represents an in-memory IdML its contents will be returned as
	 * a String. Otherwise if the book is a file stored IdML <b>null</b> will be
	 * returned.
	 * 
	 * @return the contents of the IdML OutputStream.
	 * @throws UnsupportedEncodingException
	 *             if a problem occurs.
	 */
	public String getContents() throws UnsupportedEncodingException {
		String contents = null;
		if (isInMemory()) {
			contents = ((ByteArrayOutputStream) bookStream).toString("UTF-8");
		}
		return contents;
	}

	/**
	 * Returns the full name of the IdML book. The name of the IdML differs from
	 * the book name parameter. It is auto-generated during the books creation
	 * and contains meta informations, such as MSS data, IdML mode and a
	 * timestamp. If the IdML is an in-memory book only this string will be
	 * returned. For the file stored IdMLs it will contain the full path to the
	 * IdML, as well.
	 * 
	 * @return the auto-generated IdML name.
	 */
	public String getFileName() {
		String path = "";
		if (!isInMemory()) {
			path += directoryName;
		}
		path += File.separator + idmlBook.getBookName();

		return path;
	}

	/**
	 * Returns the size of the IdML book. This method is only applicable for
	 * IdMLs stored as files. For in-memory ones it returns 0.
	 * 
	 * @return size of the file used to store the IdML book..
	 */
	public long getFileSize() {
		long size = 0;
		if (!isInMemory()) {
			size = new File(directoryName, idmlBook.getBookName()).length();
		}
		return size;
	}

	/**
	 * Returns the name of the IdML book.
	 * 
	 * @return the IdML book name.
	 */
	public String getName() {
		return bookName;
	}

	/**
	 * Sets the name of the directory where the IdML book will be stored. It is
	 * applicable only for books stored as files.
	 * 
	 * @param directoryName
	 *            path to the folder where to store the IdML.
	 */
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	/**
	 * Sets the name of the IdML book.
	 * 
	 * @param bookName
	 *            the name to be used by this book.
	 */
	public void setName(String bookName) {
		this.bookName = bookName;
	}

	/**
	 * Sets the mode of this IdML book.
	 * 
	 * @param refresh
	 *            if <b>true</b> the generated IdML will use REFRESH mode,
	 *            otherwise it will use DELTA mode.
	 */
	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

	/**
	 * Sets the storage type of this IdML book.
	 * 
	 * @param storageType
	 *            {@link #STORE_AS_FILE} - the generated IdML will be stored as
	 *            a file, {@link #STORE_IN_MEMORY} - it will be kept in memory.
	 */
	public void setStorageType(int storageType) {
		this.storageType = storageType;
	}

	/**
	 * Sets whether the generated IdML will be validated.
	 * 
	 * @param validate
	 *            if <b>true</b> a validation of the book will be performed
	 *            after its completion.
	 */
	public void setValidate(boolean validate) {
		this.validateEnabled = validate;
	}

	/**
	 * Checks if this book is an in-memory IdML.
	 * 
	 * @return <b>true</b> if this book is stored in memory, otherwise false.
	 */
	public boolean isInMemory() {
		return storageType == STORE_IN_MEMORY;
	}

	/**
	 * Checks if this book is already opened. A book must be opened in order to
	 * add CIs and Relationships to it.
	 * 
	 * @return <b>true</b> if this book is already opened, otherwise false.
	 */
	public boolean isOpened() {
		return opened;
	}

	/**
	 * Checks if this book uses REFRESH mode.
	 * 
	 * @return <b>true</b> if this book uses REFRESH, otherwise false.
	 */
	public boolean isRefreshMode() {
		return refresh;
	}

	/**
	 * Checks if validation has been enabled for this book..
	 * 
	 * @return <b>true</b> if this book will be validated, otherwise false.
	 */
	public boolean isValidateEnabled() {
		return validateEnabled;
	}

	/**
	 * Opens the IdML book.
	 * 
	 * @param applicationCode
	 *            the MSS's application code. It is used in the header of the
	 *            IdML document, when describing the MSS that contains the
	 *            artifacts described in this IdML.
	 * @param mssHostname
	 *            the MSS's hostname. It is used in the header of the IdML
	 *            document, when describing the MSS that contains the artifacts
	 *            described in this IdML.
	 * 
	 * @param cdmVersion
	 *            the version of the Common Data Model used for defining the
	 *            artifacts in the IdML.
	 * @param id
	 *            the id used for the MSS in the IdML document. If provided it
	 *            overrides the default value formed using the MSS's application
	 *            code and hostname.
	 * @param entry
	 *            an Entry object containing the attributes for the MSS
	 * @return the id used for the MSS.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public String open(String applicationCode, String mssHostname, String cdmVersion, String id, Entry entry) throws Exception {
		this.cdmVersion = cdmVersion;

		if (applicationCode == null || applicationCode.equals("") || mssHostname == null || mssHostname.equals("")
				|| cdmVersion == null || cdmVersion.equals("") || !isCdmVersionValid(cdmVersion)) {
			throw new Exception(resHash.getString("ITDI.BOOK.APPLCODE.HOSTNAME.CDMVERSION.REQUIRED"));
		}

		// create and populate the MSS object.
		String mssId = id;
		if (mssId == null || mssId.equals("")) {
			mssId = applicationCode + "." + mssHostname;
		}

		mss = new IDMLManagementSoftwareSystem(mssId);

		String mssName = entry.getString(MSS_NAME);
		String mssProductName = entry.getString(MSS_PRODUCT_NAME);
		String mssManufacturerName = entry.getString(MSS_MANUFACTURER_NAME);

		if (mssName == null) {
			if (mssManufacturerName != null && mssProductName != null) {
				mssName = "ibm-cdm:///CDMMSS/Hostname=" + mssHostname + "+Manufacturer=" + mssManufacturerName + "+ProductName="
						+ mssProductName;
				mss.addAttribute(MSS_NAME, mssName);
			} else {
				throw new Exception(resHash.getString("ITDI.BOOK.INSUFFICIENT.DATA.FOR.MSS"));
			}
		}
		populateManagedElement(mss, entry);

		// open the book
		idmlBook = openBook();
		opened = true;

		return mssId;
	}

	/**
	 * Adds a Configuration Item (CI) to the IdML book.
	 * 
	 * @param classType
	 *            the class type of the configuration item. It is determined by
	 *            the Common Data Model.
	 * @param operationType
	 *            the type of operation under which this item should be added.
	 *            This will determine what action should be performed with this
	 *            CI when the IdML book is later loaded to a CMDB. It can be
	 *            either created, modified or deleted.
	 * @param entry
	 *            the TDI entry where the attributes needed for this CI are
	 *            stored.
	 * @return the id with which this CI was added in the IdML book. Ids are
	 *         book-wide unique identifiers given to each CI.
	 * 
	 * @throws IDMLInvalidOperationException
	 *             if a problem with the operation associated with this CI
	 *             occurs.
	 * @throws IOException
	 *             if a problem occurs when trying to store the IdML data
	 *             (either in memory or to a file).
	 */
	public String addConfigurationItem(String classType, Operations operationType, Entry entry)
			throws IDMLInvalidOperationException, IOException {
		setCurrentOperation(operationType);

		// determine the id for this CI
		// it can be an auto-generated number, or an user-provided string
		String id = null;
		String attribute = entry.getString(IdMLConstants.ID_ATTR);
		if (attribute != null) {
			// escape the id
			id = XMLUtilities.escapeXmlCharacters(attribute);
		} else {
			id = String.valueOf(idCounter++);
		}

		// create the CI (ME) object
		IDMLManagedElement me = new IDMLManagedElement(classType, id);
		populateManagedElement(me, entry);

		idmlBook.addManagedElement(me);
		bookStream.flush();
		artifactCount++;
		return id;
	}

	/**
	 * Populates the provided managed element with the data form the Entry.
	 * 
	 * @param me
	 *            the managed element.
	 * @param entry
	 *            source of the data for the managed element.
	 */
	private void populateManagedElement(IDMLManagedElement me, Entry entry) {
		// set the sourceToken, superiorId and sourceContactInfo, if present
		me.setSourceToken(entry.getString(IdMLConstants.ARTIFACT_SOURCE_TOKEN_ATTR));
		me.setSuperiorId(entry.getString(IdMLConstants.SUPERIOR_ID_ATTR));
		me.setSourceContactInfo(entry.getString(IdMLConstants.SOURCE_CONTACT_INFO_ATTR));

		// Convert the passed Entry to a CI (ME)
		String[] attributeNames = entry.getAttributeNames();
		for (String attributeName : attributeNames) {
			// filter out the "$" attributes and "cdm:SourceToken"
			// keep only attributes starting with 'cdm:'
			if (!attributeName.startsWith("$") && attributeName.startsWith(CDM_PREFIX)
					&& !attributeName.equals(IdMLConstants.ARTIFACT_SOURCE_TOKEN_ATTR)) {
				String attributeValue = entry.getString(attributeName);
				if (attributeValue != null) {
					attributeValue = attributeValue.trim();
					if (!"".equals(attributeValue)) {
						if (attributeName.startsWith(EXTENDED_ATTRIBUTE_PREFIX)) {
							// remove prefix for extended attributes
							attributeName = attributeName.substring(EXTENDED_ATTRIBUTE_PREFIX.length());
							if (!"".equals(attributeName)) {
								me.addExtendedAttribute(attributeName, attributeValue);
							}
						} else {
							me.addAttribute(attributeName, attributeValue);
						}
					}
				}
			}
		}
	}

	/**
	 * Adds a Relationship to the IdML book.
	 * 
	 * @param relationshipType
	 *            the type of the relationship. It is determined by the Common
	 *            Data Model.
	 * @param operationType
	 *            the type of operation under which this relationship should be
	 *            added. This will determine what action should be performed
	 *            with this Relationship when the IdML book is later loaded to a
	 *            CMDB. It can be either created, modified or deleted.
	 * @param entry
	 *            the TDI entry where the attributes needed for this
	 *            Relationship are stored.
	 * 
	 * @throws IDMLInvalidOperationException
	 *             if a problem with the operation associated with this CI
	 *             occurs.
	 * @throws Exception
	 *             if a problem occurs when trying to store the IdML data
	 *             (either in memory or to a file).
	 */
	public void addRelationship(String relationshipType, Operations operationType, Entry entry) throws Exception {
		setCurrentOperation(operationType);

		String sourceId = entry.getString(IdMLConstants.RELATIONSHIP_SOURCE_ATTR);
		String targetId = entry.getString(IdMLConstants.RELATIONSHIP_TARGET_ATTR);
		if (sourceId != null && targetId != null) {
			sourceId = XMLUtilities.escapeXmlCharacters(sourceId);
			targetId = XMLUtilities.escapeXmlCharacters(targetId);
			IDMLRelationship relation = new IDMLRelationship(relationshipType, sourceId, targetId);
			idmlBook.addRelationship(relation);
			bookStream.flush();
			artifactCount++;
		} else {
			throw new Exception(resHash.getString("ITDI.BOOK.RELATIONSHIP.NEEDS.TARGET.AND.SOURCE"));
		}
	}

	/**
	 * Closes this IdML book.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public void close() throws Exception {
		opened = false;
		closeBook();
	}

	/**
	 * Empties the contents of the buffer.
	 */
	public void reset() {
		if (bookStream instanceof ByteArrayOutputStream) {
			((ByteArrayOutputStream) bookStream).reset();
		}
	}

	/**
	 * Splits this IdML book. This action closes the current book (validating
	 * it, if this option is enabled) and reopens it with the same meta
	 * attributes (MSS data, CDM version, etc.). Splitting is applicable only
	 * for IdML books stored as files.
	 * 
	 * @return the filename of the IdML book that was closed.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	public String split() throws Exception {
		String idmlFileName = getFileName();
		if (!isInMemory()) {
			closeBook();
			idmlBook = openBook();
		}
		return idmlFileName;
	}

	/**
	 * This private method closes the IdML book.
	 * 
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private void closeBook() throws Exception {
		if (idmlBook != null) {
			if (artifactCount > 0) {
				idmlBook.closeOperation();
			}
			if (refresh) {
				// close the refresh mode tag
				idmlBook.closeOperation();
			}
			idmlBook.closeOperationSet();
			idmlBook.closeBook();

			if (bookStream != null) {
				bookStream.flush();
				bookStream.close();
			}

			// validate the book
			String idmlFileName = getFileName();
			if (validateEnabled) {
				boolean passedCertification = validate();
				if (!passedCertification) {
					throw new Exception(resHash
							.getString("ITDI.BOOK.FAILED.CERTIFICATION", new Object[] { bookName, idmlFileName }));
				}
			}
		}
	}

	/**
	 * Checks if the passed string is a valid CDM version identifier, It should
	 * consist of three dot-separated integer numbers.
	 * 
	 * @param cdmVersion
	 *            the String to be verified.
	 * @return whether the String is a valid CDM version identifier.
	 */
	private boolean isCdmVersionValid(String cdmVersion) {
		boolean isValid = true;
		String[] tokens = cdmVersion.split("\\.");
		if (tokens.length == 3) {
			for (String token : tokens) {
				try {
					Integer.parseInt(token);
				} catch (NumberFormatException nfe) {
					isValid = false;
					break;
				}
			}
		} else {
			isValid = false;
		}
		return isValid;
	}

	/**
	 * A private method that performs the activities needed for
	 * opening/reopening (when splitting) an IdML book.
	 * 
	 * @return the generated IdML book.
	 * @throws IDMLInvalidOperationException
	 *             if a problem with the operation associated with this CI
	 *             occurs.
	 * @throws IOException
	 *             if a problem occurs when trying to store the IdML data
	 *             (either in memory or to a file).
	 */
	private IDMLBook openBook() throws IDMLInvalidOperationException, IOException {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		IDMLBook book = IDMLBook.create(mss, timestamp, IDMLBook.DEFAULT_SCHEMAURI, cdmVersion, refresh, IDML_SCHEMA_VERION);
		book.setIndent(true);

		IDMLBookWriter bookWriter = null;
		switch (storageType) {
		case STORE_AS_FILE:
			bookStream = new FileOutputStream(new File(directoryName, book.getBookName()));
			break;
		case STORE_IN_MEMORY:
			bookStream = new ByteArrayOutputStream();
			break;
		}
		bookWriter = OutputStreamBookWriter.create(bookStream);
		book.openBook(bookWriter);
		book.openOperationSet("1");
		if (refresh) {
			book.openRefreshOperation(timestamp);
		}
		bookStream.flush();

		// reset variables
		currentOperation = null;
		artifactCount = 0;
		idCounter = 0;

		return book;
	}

	/**
	 * Sets the operation to be used by the next added CI/Relationship (a tag is
	 * opened in the IdML file). If another operation is already opened it will
	 * be closed. If the needed operation is currently opened no action will be
	 * taken.
	 * 
	 * @param operationType
	 *            the new operation we want to open in the IdML file.
	 * @throws IDMLInvalidOperationException
	 *             if the wanted operation is not supported at the current
	 *             location in the IdML file.
	 * @throws IOException
	 *             if a problem occurs.
	 */
	private void setCurrentOperation(Operations operationType) throws IDMLInvalidOperationException, IOException {
		if (!operationType.equals(currentOperation)) {
			if (artifactCount > 0) {
				idmlBook.closeOperation();
			}
			currentOperation = operationType;
			if (currentOperation.equals(Operations.CREATE)) {
				idmlBook.openCreateOperation(idmlBook.getTimestamp());
			} else if (currentOperation.equals(Operations.MODIFY)) {
				idmlBook.openModifyOperation(idmlBook.getTimestamp());
			} else if (currentOperation.equals(Operations.DELETE)) {
				idmlBook.openDeleteOperation(idmlBook.getTimestamp());
			}
		}
	}

	/**
	 * Validates this IdML book.
	 * 
	 * @return <b>true</b> if the book passed validation successfully, otherwise
	 *         false.
	 * @throws Exception
	 *             if a problem occurs.
	 */
	private boolean validate() throws Exception {
		IdMLCertification idmlCertificator = new IdMLCertification();

		String fileName = getFileName();
		InputSource inputSource = null;
		switch (storageType) {
		case STORE_AS_FILE:
			inputSource = new InputSource(new FileInputStream(fileName));
			break;
		case STORE_IN_MEMORY:
			inputSource = new InputSource(new StringReader(getContents()));
			break;
		}
		return idmlCertificator.validate(fileName, inputSource);
	}

}
