/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// Using fully qualified name for javax.wsdl.xml.WSDLLocator to avoid conflict

import org.apache.commons.codec.binary.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HttpTransportProperties;
import java.net.HttpURLConnection;
import org.xml.sax.InputSource;

import com.ibm.di.server.Log;
import com.ibm.di.server.ResourceHash;

/**
 * This class implements the WSDLLocator interface for custom authentication
 * when retrieving WSDL documents. It uses Axis2's HTTP client for HTTP operations.
 */
public class WSDLLocator implements javax.wsdl.xml.WSDLLocator {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * Latin-1 encoding.
     */
    public static final String ENCODING_LATIN_1 = "ISO-8859-1";
    
    /**
     * Username for authentication.
     */
    private String username;
    
    /**
     * Password for authentication.
     */
    private String password;
    
    /**
     * Base URI of the WSDL document.
     */
    private String baseURI;
    
    /**
     * Latest import URI.
     */
    private String latestImportURI;
    
    /**
     * Map of imported documents.
     */
    private Map<String, InputSource> importedDocs = new HashMap<String, InputSource>();
    
    /**
     * Connection timeout in milliseconds.
     */
    private int connectionTimeout = 30000;
    
    /**
     * Logger.
     */
    private Log log;
    
    /**
     * Resource hash.
     */
    private static ResourceHash resHash;
    
    static {
        try {
            resHash = new ResourceHash("webserviceutil");
        } catch (Exception e) {
            // Cannot log here since log is not initialized
            e.printStackTrace();
        }
    }
    
    /**
     * Default constructor.
     */
    public WSDLLocator() {
        try {
            log = new Log("WSDLLocator");
            initHttpClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Constructor with authentication credentials and WSDL URL.
     * 
     * @param username The username
     * @param password The password
     * @param wsdlUrl The WSDL URL
     */
    public WSDLLocator(String username, String password, String wsdlUrl) {
        this();
        this.username = username;
        this.password = password;
        this.baseURI = wsdlUrl;
    }
    
    /**
     * Initializes the HTTP client.
     * 
     * Initializes HTTP connection settings.
     */
    private void initHttpClient() {
        // No initialization needed for HttpURLConnection
        // We'll create connections as needed
    }
    
    /**
     * Returns an InputSource "pointed at" the base document.
     * 
     * @return The InputSource for the base document
     */
    @Override
    public InputSource getBaseInputSource() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseURI);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectionTimeout);
            connection.setRequestMethod("GET");
            
            // Add authentication if credentials are provided
            if (username != null && password != null) {
                String authHeader = "Basic " + b64Encode(username + ":" + password, ENCODING_LATIN_1);
                connection.setRequestProperty("Authorization", authHeader);
            }
            
            // Execute the HTTP request
            connection.connect();
            
            // Create the InputSource from the response
            InputStream inputStream = connection.getInputStream();
            InputSource inputSource = new InputSource(inputStream);
            inputSource.setSystemId(url.toString());
            
            return inputSource;
        } catch (Exception e) {
            if (log != null) {
                log.logerror(resHash.getString("WSUTIL.WSDLLOCATOR.FAILED.TO.GET.BASE.INPUT.SOURCE"), e);
            }
            return null;
        }
    }
    
    /**
     * Returns an InputSource pointed at an imported WSDL document.
     * 
     * @param parentLocation The location of the parent document
     * @param importLocation The location of the imported document relative to the parent
     * @return The InputSource for the imported document
     */
    @Override
    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        HttpURLConnection connection = null;
        try {
            // Check if we've already loaded this import
            String importKey = parentLocation + "#" + importLocation;
            if (importedDocs.containsKey(importKey)) {
                return importedDocs.get(importKey);
            }
            
            // Resolve the import URL
            URL parentUrl = (parentLocation != null) ? new URL(parentLocation) : null;
            URL importUrl = (parentUrl != null) ? new URL(parentUrl, importLocation) : new URL(importLocation);
            
            // Set the latest import URI
            latestImportURI = importUrl.toString();
            
            // Create the HTTP connection
            connection = (HttpURLConnection) importUrl.openConnection();
            connection.setConnectTimeout(connectionTimeout);
            connection.setRequestMethod("GET");
            
            // Add authentication if credentials are provided
            if (username != null && password != null) {
                String authHeader = "Basic " + b64Encode(username + ":" + password, ENCODING_LATIN_1);
                connection.setRequestProperty("Authorization", authHeader);
            }
            
            // Execute the HTTP request
            connection.connect();
            
            // Create the InputSource from the response
            InputStream inputStream = connection.getInputStream();
            InputSource inputSource = new InputSource(inputStream);
            inputSource.setSystemId(importUrl.toString());
            
            // Cache the imported document
            importedDocs.put(importKey, inputSource);
            
            return inputSource;
        } catch (Exception e) {
            if (log != null) {
                log.logerror(resHash.getString("WSUTIL.WSDLLOCATOR.FAILED.TO.GET.IMPORT.INPUT.SOURCE"), e);
            }
            return null;
        }
    }
    
    /**
     * Closes any resources used by this WSDLLocator.
     */
    @Override
    public void close() {
        // Nothing to close
    }
    
    /**
     * Returns a URI representing the location of the base document.
     * 
     * @return The base URI
     */
    @Override
    public String getBaseURI() {
        return baseURI;
    }
    
    /**
     * Returns a URI representing the location of the last import document to be resolved.
     * 
     * @return The latest import URI
     */
    @Override
    public String getLatestImportURI() {
        return latestImportURI != null ? latestImportURI : baseURI;
    }
    
    /**
     * Encodes a string using Base64.
     * 
     * @param auth The string to encode
     * @param encoding The character encoding to use
     * @return The Base64-encoded string
     * @throws UnsupportedEncodingException if the encoding is not supported
     */
    private String b64Encode(String auth, String encoding) throws UnsupportedEncodingException {
        return new String(Base64.encodeBase64(auth.getBytes(encoding)));
    }
    
    /**
     * Sets the username for authentication.
     * 
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Sets the password for authentication.
     * 
     * @param password The password
     */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /**
     * Sets the base URI.
     * 
     * @param baseURI The base URI
     */
    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }
}
