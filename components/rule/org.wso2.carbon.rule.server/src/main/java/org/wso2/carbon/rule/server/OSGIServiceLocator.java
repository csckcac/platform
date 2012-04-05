/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rule.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.rule.core.LoggedRuntimeException;

/**
 * OSGIServiceLocator is a singleton class proving capability to access an OSGI service by giving
 * the name of the service
 */
public class OSGIServiceLocator {

    private static final Log log = LogFactory.getLog(OSGIServiceLocator.class);

    private static OSGIServiceLocator ourInstance = new OSGIServiceLocator();

    /**
     * Context object to access bundles
     */
    private BundleContext bundleContext;

    /**
     * The state of this component
     */
    private boolean initialized = false;

    public static OSGIServiceLocator getInstance() {
        return ourInstance;
    }

    private OSGIServiceLocator() {
    }

    /**
     * Initializes the component. BundleContext must not be null.
     *
     * @param bundleContext the context object to access bundles
     */
    public void init(BundleContext bundleContext) {
        assertBundleContextNull(bundleContext);

        this.bundleContext = bundleContext;
        this.initialized = true;
    }

    /**
     * Finds a the OSGI service with the given name and returns it. The provided name must be valid
     *
     * @param name the name of the service to be located
     * @return <code>null<code> if there is no service with the given name, otherwise ,
     *         returns the service
     */
    public Object locateService(String name) {
        assertInitialized();
        assertNameNull(name);

        Object service = null;
        ServiceReference serviceReference = bundleContext.getServiceReference(name);
        if (serviceReference != null) {
            service = bundleContext.getService(serviceReference);
        }
        if (service == null) {
            if (log.isDebugEnabled()) {
                log.debug("There is no service for the given service name :" + name);
            }
        }
        return service;
    }

    /**
     * Checks the state of this component
     *
     * @return <code>true</code>
     */
    public boolean isInitialized() {
        return initialized;
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new LoggedRuntimeException("OSGIServiceLocator has not been initialized, " +
                    "it requires to be initialized, with the required " +
                    "configurations before starting", log);
        }
    }

    private void assertBundleContextNull(BundleContext bundleContext) {
        if (bundleContext == null) {
            throw new LoggedRuntimeException("Given BundleContext is null", log);
        }
    }

    private void assertNameNull(String name) {
        if (name == null || "".equals(name)) {
            throw new LoggedRuntimeException("Name argument cannot be null", log);
        }
    }

}
