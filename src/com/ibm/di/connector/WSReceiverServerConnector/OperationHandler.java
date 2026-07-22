/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.WSReceiverServerConnector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.di.server.ResourceHash;

/**
 * Used for parsing input stream to extract operation name from SOAP messages.
 */
public class OperationHandler extends DefaultHandler {

    /**
     * Pattern of the prefix - a combination of letters, digits and underscore
     * ending with ':'.
     */
    private static final String PREFIX_PATTERN = "\\w*:|\\w*\\-\\w*:";

    /**
     * Envelope element name.
     */
    private static final String SOAP_ENVELOPE_ELEMENT = "Envelope";

    /**
     * Body element name.
     */
    private static final String SOAP_BODY_ELEMENT = "Body";

    /**
     * NLS Property set holding name-value pairs for the resource.
     */
    private static final ResourceHash sResHash = new ResourceHash("wsreceiverserverconnector");

    /**
     * In SOAP envelope.
     */
    boolean inEnvelope = false;

    /**
     * In SOAP body.
     */
    boolean inBody = false;

    /**
     * Receive notification of the start of an element.
     * 
     * @param uri
     *            The Namespace URI.
     * @param localName
     *            The local name (without prefix).
     * @param qName
     *            The qualified name (with prefix).
     * @param attributes
     *            specified Attributes.
     * @throws SAXException
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String name = qName.replaceFirst(PREFIX_PATTERN, "");

        if (inEnvelope) {
            if (inBody) {
                throw new OperationNameFoundException(name);
            } else {
                if (name.equals(SOAP_BODY_ELEMENT)) {
                    inBody = true;
                }
            }
        } else {
            if (name.equals(SOAP_ENVELOPE_ELEMENT)) {
                inEnvelope = true;
            }
        }
    }

    /**
     * Receive notification of the end of an element.
     * 
     * @param uri
     *            The Namespace URI.
     * @param localName
     *            The local name (without prefix).
     * @param qName
     *            The qualified XML 1.0 name (with prefix).
     * @throws SAXException
     *             - Any SAX exception, possibly wrapping another exception.
     */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String name = qName.replaceFirst(PREFIX_PATTERN, "");

        if (name.equals(SOAP_ENVELOPE_ELEMENT) || name.equals(SOAP_BODY_ELEMENT)) {
            throw new SAXException(sResHash.getString("CONNECTOR.WSRECSERVER.CHILDNOTFOUND.EXCEP"));
        }
    }
}
