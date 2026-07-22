/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.codec.binary.Base64;

/**
 * Configuration class for web service clients.
 * This class provides methods for applying configuration to Axis2 Options
 * and supports common web service configuration options.
 */
public class WebServiceConfig {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;
    
    /**
     * Default connection timeout in milliseconds (30 seconds).
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 30000;
    
    /**
     * Default socket timeout in milliseconds (60 seconds).
     */
    public static final int DEFAULT_SOCKET_TIMEOUT = 60000;
    
    /**
     * Default maximum connections per host.
     */
    public static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 20;
    
    /**
     * Default maximum total connections.
     */
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 100;
    
    /**
     * Connection timeout in milliseconds.
     */
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
    
    /**
     * Socket timeout in milliseconds.
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
    
    /**
     * Maximum connections per host.
     */
    private int maxConnectionsPerHost = DEFAULT_MAX_CONNECTIONS_PER_HOST;
    
    /**
     * Maximum total connections.
     */
    private int maxTotalConnections = DEFAULT_MAX_TOTAL_CONNECTIONS;
    
    /**
     * SOAP action.
     */
    private String soapAction = null;
    
    /**
     * Endpoint URL.
     */
    private String endpointURL = null;
    
    /**
     * Username for basic authentication.
     */
    private String username = null;
    
    /**
     * Password for basic authentication.
     */
    private String password = null;
    
    /**
     * Flag indicating whether to use MTOM.
     */
    private boolean useMTOM = false;
    
    /**
     * Flag indicating whether to use REST.
     */
    private boolean useREST = false;
    
    /**
     * Flag indicating whether to use SOAP 1.2.
     */
    private boolean useSOAP12 = false;
    
    /**
     * Flag indicating whether to use chunking.
     */
    private boolean useChunking = true;
    
    /**
     * Flag indicating whether to use preemptive authentication.
     */
    private boolean usePreemptiveAuthentication = true;
    
    /**
     * Creates a new WebServiceConfig with default settings.
     */
    public WebServiceConfig() {
        // Use default settings
    }
    
    /**
     * Creates a new WebServiceConfig with the specified endpoint URL.
     * 
     * @param endpointURL The endpoint URL
     */
    public WebServiceConfig(String endpointURL) {
        this.endpointURL = endpointURL;
    }
    
    /**
     * Creates a new WebServiceConfig with the specified endpoint URL and authentication.
     * 
     * @param endpointURL The endpoint URL
     * @param username The username for basic authentication
     * @param password The password for basic authentication
     */
    public WebServiceConfig(String endpointURL, String username, String password) {
        this.endpointURL = endpointURL;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Applies this configuration to an Axis2 Options object.
     * 
     * @param options The Axis2 Options object
     */
    public void applyTo(Options options) {
        if (options == null) {
            return;
        }
        
        // Set endpoint URL
        if (endpointURL != null) {
            options.setTo(new EndpointReference(endpointURL));
        }
        
        // Set timeouts
        options.setTimeOutInMilliSeconds(socketTimeout);
        options.setProperty("CONNECTION_TIMEOUT", connectionTimeout);
        
        // Set SOAP action
        if (soapAction != null) {
            options.setAction(soapAction);
        }
        
        // Set HTTP client parameters
        options.setProperty("REUSE_HTTP_CLIENT", Boolean.TRUE);
        options.setProperty("AUTO_RELEASE_CONNECTION", Boolean.TRUE);
        
        // Set connection limits
        // In Axis2 1.7.6, these constants are defined in HttpConnectionManager
        options.setProperty("MaxConnectionsPerHost", maxConnectionsPerHost);
        options.setProperty("MaxTotalConnections", maxTotalConnections);
        
        // Set chunking
        if (!useChunking) {
            options.setProperty("CHUNKED", Boolean.FALSE);
        }
        
        // Set MTOM
        if (useMTOM) {
            options.setProperty(Constants.Configuration.ENABLE_MTOM, Boolean.TRUE);
        }
        
        // Set REST
        if (useREST) {
            options.setProperty(Constants.Configuration.ENABLE_REST, Boolean.TRUE);
        }
        
        // Set SOAP version
        if (useSOAP12) {
            options.setSoapVersionURI(Constants.URI_SOAP12_ENV);
        } else {
            options.setSoapVersionURI(Constants.URI_SOAP11_ENV);
        }
        
        // Set authentication
        if (username != null && password != null) {
            // In Axis2 1.7.6, we need to use a different approach for authentication
            // Create a basic authentication HTTP header
            String authString = username + ":" + password;
            String encodedAuth = org.apache.commons.codec.binary.Base64.encodeBase64String(authString.getBytes());
            String authHeader = "Basic " + encodedAuth;
            
            // Set the authorization header
            options.setProperty("Authorization", authHeader);
        }
    }
    
    /**
     * Gets the connection timeout in milliseconds.
     * 
     * @return The connection timeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    
    /**
     * Sets the connection timeout in milliseconds.
     * 
     * @param connectionTimeout The connection timeout
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    
    /**
     * Gets the socket timeout in milliseconds.
     * 
     * @return The socket timeout
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }
    
    /**
     * Sets the socket timeout in milliseconds.
     * 
     * @param socketTimeout The socket timeout
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }
    
    /**
     * Gets the maximum connections per host.
     * 
     * @return The maximum connections per host
     */
    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }
    
    /**
     * Sets the maximum connections per host.
     * 
     * @param maxConnectionsPerHost The maximum connections per host
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
        return this;
    }
    
    /**
     * Gets the maximum total connections.
     * 
     * @return The maximum total connections
     */
    public int getMaxTotalConnections() {
        return maxTotalConnections;
    }
    
    /**
     * Sets the maximum total connections.
     * 
     * @param maxTotalConnections The maximum total connections
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setMaxTotalConnections(int maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
        return this;
    }
    
    /**
     * Gets the SOAP action.
     * 
     * @return The SOAP action
     */
    public String getSoapAction() {
        return soapAction;
    }
    
    /**
     * Sets the SOAP action.
     * 
     * @param soapAction The SOAP action
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setSoapAction(String soapAction) {
        this.soapAction = soapAction;
        return this;
    }
    
    /**
     * Gets the endpoint URL.
     * 
     * @return The endpoint URL
     */
    public String getEndpointURL() {
        return endpointURL;
    }
    
    /**
     * Sets the endpoint URL.
     * 
     * @param endpointURL The endpoint URL
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setEndpointURL(String endpointURL) {
        this.endpointURL = endpointURL;
        return this;
    }
    
    /**
     * Gets the username for basic authentication.
     * 
     * @return The username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Sets the username for basic authentication.
     * 
     * @param username The username
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setUsername(String username) {
        this.username = username;
        return this;
    }
    
    /**
     * Gets the password for basic authentication.
     * 
     * @return The password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Sets the password for basic authentication.
     * 
     * @param password The password
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setPassword(String password) {
        this.password = password;
        return this;
    }
    
    /**
     * Checks if MTOM is enabled.
     * 
     * @return true if MTOM is enabled, false otherwise
     */
    public boolean isUseMTOM() {
        return useMTOM;
    }
    
    /**
     * Sets whether to use MTOM.
     * 
     * @param useMTOM true to use MTOM, false otherwise
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setUseMTOM(boolean useMTOM) {
        this.useMTOM = useMTOM;
        return this;
    }
    
    /**
     * Checks if REST is enabled.
     * 
     * @return true if REST is enabled, false otherwise
     */
    public boolean isUseREST() {
        return useREST;
    }
    
    /**
     * Sets whether to use REST.
     * 
     * @param useREST true to use REST, false otherwise
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setUseREST(boolean useREST) {
        this.useREST = useREST;
        return this;
    }
    
    /**
     * Checks if SOAP 1.2 is enabled.
     * 
     * @return true if SOAP 1.2 is enabled, false otherwise
     */
    public boolean isUseSOAP12() {
        return useSOAP12;
    }
    
    /**
     * Sets whether to use SOAP 1.2.
     * 
     * @param useSOAP12 true to use SOAP 1.2, false to use SOAP 1.1
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setUseSOAP12(boolean useSOAP12) {
        this.useSOAP12 = useSOAP12;
        return this;
    }
    
    /**
     * Checks if chunking is enabled.
     * 
     * @return true if chunking is enabled, false otherwise
     */
    public boolean isUseChunking() {
        return useChunking;
    }
    
    /**
     * Sets whether to use chunking.
     * 
     * @param useChunking true to use chunking, false otherwise
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setUseChunking(boolean useChunking) {
        this.useChunking = useChunking;
        return this;
    }
    
    /**
     * Checks if preemptive authentication is enabled.
     * 
     * @return true if preemptive authentication is enabled, false otherwise
     */
    public boolean isUsePreemptiveAuthentication() {
        return usePreemptiveAuthentication;
    }
    
    /**
     * Sets whether to use preemptive authentication.
     * 
     * @param usePreemptiveAuthentication true to use preemptive authentication, false otherwise
     * @return This WebServiceConfig instance for method chaining
     */
    public WebServiceConfig setUsePreemptiveAuthentication(boolean usePreemptiveAuthentication) {
        this.usePreemptiveAuthentication = usePreemptiveAuthentication;
        return this;
    }
    
    /**
     * Creates a default configuration.
     * 
     * @return A default WebServiceConfig instance
     */
    public static WebServiceConfig createDefault() {
        return new WebServiceConfig();
    }
    
    /**
     * Creates a configuration with the specified endpoint URL.
     * 
     * @param endpointURL The endpoint URL
     * @return A WebServiceConfig instance with the specified endpoint URL
     */
    public static WebServiceConfig createWithEndpoint(String endpointURL) {
        return new WebServiceConfig(endpointURL);
    }
    
    /**
     * Creates a configuration with the specified endpoint URL and authentication.
     * 
     * @param endpointURL The endpoint URL
     * @param username The username for basic authentication
     * @param password The password for basic authentication
     * @return A WebServiceConfig instance with the specified endpoint URL and authentication
     */
    public static WebServiceConfig createWithAuthentication(String endpointURL, String username, String password) {
        return new WebServiceConfig(endpointURL, username, password);
    }
}
