/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.connector.WSReceiverServerConnector;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A replacement for the deprecated NonBlockingBufferedInputStream from Axis2.
 * This class provides similar functionality with a modern implementation.
 */
public class BufferedNonBlockingInputStream extends BufferedInputStream {
    
    /**
     * The content length of the stream.
     */
    private int contentLength = -1;
    
    /**
     * Default constructor.
     */
    public BufferedNonBlockingInputStream() {
        super(null);
    }
    
    /**
     * Constructor with an input stream.
     * 
     * @param in The input stream to buffer
     */
    public BufferedNonBlockingInputStream(InputStream in) {
        super(in);
    }
    
    /**
     * Sets the input stream for this buffered stream.
     * 
     * @param in The input stream to buffer
     */
    public void setInputStream(InputStream in) {
        this.in = in;
        this.count = 0;
        this.pos = 0;
        this.markpos = -1;
    }
    
    /**
     * Sets the content length for this stream.
     * 
     * @param contentLength The content length
     */
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
    
    /**
     * Returns the content length for this stream.
     * 
     * @return The content length
     */
    public int getContentLength() {
        return contentLength;
    }
    
    /**
     * Peeks at the next byte in the stream without consuming it.
     *
     * @return The next byte, or -1 if the end of the stream has been reached
     * @throws IOException If an I/O error occurs
     */
    public int peek() throws IOException {
        // Mark the current position
        mark(1);
        
        // Read the next byte
        int nextByte = read();
        
        // Reset to the marked position
        reset();
        
        // Return the byte we read
        return nextByte;
    }
}
