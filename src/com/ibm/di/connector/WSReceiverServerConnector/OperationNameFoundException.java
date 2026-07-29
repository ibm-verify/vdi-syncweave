/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.WSReceiverServerConnector;

import org.xml.sax.SAXException;

/**
 * Encapsulate a general SAX error or warning.
 * Used to signal when an operation name is found in SOAP parsing.
 */
public class OperationNameFoundException extends SAXException {

    /**
     * Helps the JVM to recognize class and class instances
     */
    private static final long serialVersionUID = 2347841159163487059L;

    /**
     * Encapsulate a general SAX error or warning.
     * 
     * @param operationName
     *            String
     */
    public OperationNameFoundException(String operationName) {
        super(operationName);
    }
}
