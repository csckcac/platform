/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mashup.jsservices.internal;

import org.osgi.framework.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mashup.utils.HostObjectServiceInterface;
import org.wso2.carbon.utils.Utils;

import java.util.*;

public class HostObjectServiceListener implements ServiceListener {
    private static Log log = LogFactory.getLog(HostObjectServiceListener.class);

    private Map<String, Bundle> requiredHostObjectsMap = new HashMap<String, Bundle>();
    private List<String> hostObjectsThatNeedServices;

    private BundleContext bundleContext;
    private boolean listenerRegistered;

    public HostObjectServiceListener(BundleContext bundleContext, List<String> hostObjectsThatNeedServices) {
        this.bundleContext = bundleContext;
        this.hostObjectsThatNeedServices = hostObjectsThatNeedServices;
    }

    /**
     * Registering HostObjectServiceListener as a ServiceListener
     *
     * @return boolean : whether the listener is registered
     */
    boolean registerServiceListener() {
        if (requiredHostObjectsMap.isEmpty()) {
            //There are no required Host Objects
            listenerRegistered = false;
        } else {
            //Registering HostObjectServiceListener as a ServiceListener
            bundleContext.addServiceListener(this);
            listenerRegistered = true;
        }
        return listenerRegistered;
    }

    void unregisterServiceListener() {
        if (listenerRegistered) {
            bundleContext.removeServiceListener(this);
        }
    }

    synchronized void start() {
        try {
            //Getting the registered required OSGi services
            ServiceReference[] references = bundleContext.getServiceReferences(HostObjectServiceInterface.class.getName(), null);
            if (references != null && references.length > 0) {
                for (ServiceReference reference : references) {
                    HostObjectServiceInterface hostObjectServiceInterface = (HostObjectServiceInterface) bundleContext.getService(reference);
                    String className = hostObjectServiceInterface.getHostObjectClassName();
                    hostObjectsThatNeedServices.remove(className);
                }
            }
        } catch (InvalidSyntaxException e) {
            //SyntaxError Occured. Ignoring
            log.error(e.getCause(), e);
        }
    }

    void addRequiredServiceBundle(Bundle bundle, String hostObjectName) {
        requiredHostObjectsMap.put(hostObjectName, bundle);
    }

    public synchronized void serviceChanged(ServiceEvent event) {
        if (event.getType() == ServiceEvent.REGISTERED) {
            Object service = bundleContext.getService(event.getServiceReference());
            if (service instanceof HostObjectServiceInterface) {
                HostObjectServiceInterface hostObjectServiceInterface = (HostObjectServiceInterface) service;
                hostObjectsThatNeedServices.remove(hostObjectServiceInterface.getHostObjectClassName());
            }
            if (hostObjectsThatNeedServices.isEmpty()) {
                try {
                    bundleContext.removeServiceListener(this);
                    Utils.registerDeployerServices(bundleContext);
                } catch (Exception e) {
                    String msg = "Failed to register JSDeployer as an OSGi service.";
                    log.error(msg, e);
                }
            }
        }
    }

    public List<String> getHostObjectsThatNeedServices() {
        return hostObjectsThatNeedServices;
    }
}
