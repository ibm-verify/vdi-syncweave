/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.fc.webservice.axis2;

import com.ibm.di.server.Log;
import org.apache.axiom.soap.SOAPEnvelope;
import java.io.StringWriter;

/**
 * Utility class used for logging messages.
 * This class replaces the original WSLogUtil from Axis 1.x implementation.
 */
public class LogUtil {
    /**
     * Copyright.
     */
    @SuppressWarnings("unused")
    private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

    /**
     * Shared static logger object
     */
    public final static Log logger = new Log("miadmin", "com.ibm.di.admin");

    /**
     * Logs message.
     * 
     * @param msg
     *            message to log.
     */
    public static void logmsg(String msg) {
        logger.info(msg);
    }

    /**
     * Logs an error.
     * 
     * @param msg
     *            message to the error.
     * @param error
     *            error to log.
     */
    public static void logerror(String msg, Throwable error) {
        logger.error(msg, error);
    }

    /**
     * Logs debug information.
     * 
     * @param msg
     *            message to log
     */
    public static void logdebug(String msg) {
        logger.debug(msg);
    }

    /**
     * Logs warning message
     * 
     * @param msg
     *            message to log.
     */
    public static void logwarn(String msg) {
        logger.warn(msg);
    }
    
    /**
     * Logs a SOAP envelope at debug level.
     * 
     * @param envelope
     *            SOAP envelope to log
     * @param prefix
     *            prefix to add to the log message
     */
    public static void logSOAPEnvelope(SOAPEnvelope envelope, String prefix) {
        if (envelope != null) {
            try {
                StringWriter writer = new StringWriter();
                envelope.serialize(writer);
                logdebug(prefix + ":\n" + writer.toString());
            } catch (Exception e) {
                logerror("Error logging SOAP envelope", e);
            }
        }
    }
    
    /**
     * Logs a request SOAP envelope at debug level.
     * 
     * @param envelope
     *            SOAP envelope to log
     */
    public static void logRequestEnvelope(SOAPEnvelope envelope) {
        logSOAPEnvelope(envelope, "Request SOAP Envelope");
    }
    
    /**
     * Logs a response SOAP envelope at debug level.
     * 
     * @param envelope
     *            SOAP envelope to log
     */
    public static void logResponseEnvelope(SOAPEnvelope envelope) {
        logSOAPEnvelope(envelope, "Response SOAP Envelope");
    }
    
    /**
     * Logs method entry at debug level.
     * 
     * @param methodName
     *            name of the method being entered
     */
    public static void logMethodEntry(String methodName) {
        logdebug("Entering method: " + methodName);
    }
    
    /**
     * Logs method exit at debug level.
     * 
     * @param methodName
     *            name of the method being exited
     */
    public static void logMethodExit(String methodName) {
        logdebug("Exiting method: " + methodName);
    }
    
    /**
     * Logs method exit with result at debug level.
     * 
     * @param methodName
     *            name of the method being exited
     * @param result
     *            result to log
     */
    public static void logMethodExit(String methodName, Object result) {
        logdebug("Exiting method: " + methodName + " with result: " + result);
    }
    
    /**
     * Logs an Axis2 fault at error level.
     * 
     * @param msg
     *            message to log
     * @param fault
     *            Axis2 fault to log
     */
    public static void logAxisFault(String msg, org.apache.axis2.AxisFault fault) {
        logerror(msg + ": " + fault.getMessage(), fault);
    }
}
