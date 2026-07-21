/*
 * Copyright IBM Corp. 2025
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.internal.jaxrs;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

public class JaxRsInitializableServlet extends JaxRsServlet {

    private static final long serialVersionUID = 1L;
    private final Map<String, Object> pendingAttributes = new HashMap<>();

    public JaxRsInitializableServlet() {
        super();
    }

    public JaxRsInitializableServlet(Class<? extends Application> appClass) {
        super(appClass);
    }

    public JaxRsInitializableServlet(Application appInstance) {
        super(appInstance);
    }

    /**
     * Set an attribute before servlet initialization.
     * The attribute will be transferred to ServletContext after init.
     */
    public void setAttribute(String name, Object value) {
        pendingAttributes.put(name, value);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Transfer pending attributes to servlet context
        if (config != null && config.getServletContext() != null) {
            for (Map.Entry<String, Object> entry : pendingAttributes.entrySet()) {
                config.getServletContext().setAttribute(entry.getKey(), entry.getValue());
            }
            pendingAttributes.clear();
        }
        fireInitRequest();
    }

    private void fireInitRequest() throws ServletException {
        try {
            service(createRequestProxy(), createResponseProxy());
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private HttpServletRequest createRequestProxy() {
        return (HttpServletRequest) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{HttpServletRequest.class},
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getMethod": return "GET";
                    case "getRequestURI": return "/init";
                    case "getRequestURL":
                        return new StringBuffer("http://localhost/init");
                    case "getContextPath": return "";
                    case "getServletPath": return "";
                    case "getPathInfo": return "/init";
                    case "getQueryString": return null;
                    case "getHeader": return "application/json";
                    case "getHeaderNames": return Collections.enumeration(Collections.singleton("Content-Type"));
                    case "getHeaders": return Collections.enumeration(Collections.singleton("application/json"));
                    case "getParameterMap": return Collections.emptyMap();
                    case "getParameterNames": return Collections.emptyEnumeration();
                    case "getAttribute": return null;
                    case "getAttributeNames": return Collections.emptyEnumeration();
                }
                return null;
            }
        );
    }

    private HttpServletResponse createResponseProxy() {
        return (HttpServletResponse) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{HttpServletResponse.class},
            (proxy, method, args) -> {
                String methodName = method.getName();
                
                // Handle methods that return primitives or specific types
                switch (methodName) {
                    case "getOutputStream":
                        return new ServletOutputStream() {
                            public void write(int b) {}
                            public boolean isReady() { return true; }
                            public void setWriteListener(WriteListener l) {}
                        };
                    case "isCommitted":
                        return Boolean.FALSE;
                    case "getStatus":
                        return Integer.valueOf(200);
                    case "getContentType":
                        return MediaType.APPLICATION_JSON;
                    case "getCharacterEncoding":
                        return "UTF-8";
                    case "getBufferSize":
                        return Integer.valueOf(8192);
                    case "containsHeader":
                        return Boolean.FALSE;
                    // Void methods - return null is fine
                    case "setStatus":
                    case "setContentType":
                    case "setCharacterEncoding":
                    case "setContentLength":
                    case "setContentLengthLong":
                    case "setBufferSize":
                    case "flushBuffer":
                    case "reset":
                    case "resetBuffer":
                    case "setHeader":
                    case "addHeader":
                    case "setDateHeader":
                    case "addDateHeader":
                    case "setIntHeader":
                    case "addIntHeader":
                        return null;
                }
                
                // For other methods, return appropriate defaults
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class || returnType == Boolean.class) {
                    return Boolean.FALSE;
                } else if (returnType == int.class || returnType == Integer.class) {
                    return Integer.valueOf(0);
                } else if (returnType == long.class || returnType == Long.class) {
                    return Long.valueOf(0L);
                }
                
                return null;
            }
        );
    }
}
