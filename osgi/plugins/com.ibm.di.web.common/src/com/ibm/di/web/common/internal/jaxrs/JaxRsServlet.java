/*
 * Copyright contributors to the SyncWeave project
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.di.web.common.internal.jaxrs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
// import org.glassfish.hk2.api.ServiceLocatorGenerator;

public class JaxRsServlet extends HttpServlet {

    private final Class<? extends Application> appClass;
    private final Application appInstance;
    private final Map<String, Object> pendingAttributes = new HashMap<>();
    private Runnable postInitCallback;

    private ServletContainer delegate;

    public JaxRsServlet() {
        this(null, null);
    }

    public JaxRsServlet(Class<? extends Application> appClass) {
        this(appClass, null);
    }

    public JaxRsServlet(Application appInstance) {
        this(null, appInstance);
    }

    private JaxRsServlet(Class<? extends Application> appClass, Application appInstance) {
        this.appClass = appClass;
        this.appInstance = appInstance;
    }

    /**
     * Set an attribute before servlet initialization.
     * The attribute will be transferred to ServletContext after init.
     */
    public void setAttribute(String name, Object value) {
        pendingAttributes.put(name, value);
    }

    /**
     * Set a callback to be executed after servlet initialization.
     * This allows activators to complete registrations that require ServletContext.
     */
    public void setPostInitCallback(Runnable callback) {
        this.postInitCallback = callback;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // MUST call super.init() first to set ServletConfig in parent HttpServlet
        super.init(config);
        
        ResourceConfig resourceConfig = null;

        if (appInstance != null) {
            resourceConfig = (appInstance instanceof ResourceConfig)
                    ? (ResourceConfig) appInstance
                    : ResourceConfig.forApplication(appInstance);
        } else if (appClass != null) {
            try {
                Application app = appClass.newInstance();
                resourceConfig = (app instanceof ResourceConfig)
                        ? (ResourceConfig) app
                        : ResourceConfig.forApplication(app);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        // if (resourceConfig != null) {
        //     resourceConfig.property(
        //         ServletProperties.SERVICE_LOCATOR_GENERATOR_CLASS,
        //         ServiceLocatorGenerator.class.getName()
        //     );
        // }

        delegate = new ServletContainer(resourceConfig);
        delegate.init(config);
        
        // Transfer pending attributes to servlet context
        if (config != null && config.getServletContext() != null) {
            for (Map.Entry<String, Object> entry : pendingAttributes.entrySet()) {
                config.getServletContext().setAttribute(entry.getKey(), entry.getValue());
            }
            pendingAttributes.clear();
            
            // Execute post-init callback if set
            if (postInitCallback != null) {
                postInitCallback.run();
                postInitCallback = null;
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        delegate.service(req, resp);
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }
}
