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
package org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.*;
import org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.HostObjectRegistry;
import org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService;

import java.util.Hashtable;

public class Activator implements BundleActivator, BundleListener {

    private static final Log log = LogFactory.getLog(Activator.class);
    BundleContext context;
    HostObjectRegistry hostObjectRegistry = new HostObjectRegistry();

    public void start(BundleContext bundleContext) throws Exception {
        this.context = bundleContext;
        context.addBundleListener(this);
        Bundle bundles[] = context.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getState() >= Bundle.RESOLVED) {
                hostObjectRegistry.register(bundle);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Starting Mozilla Rhino bundle");
        }
        context.registerService(HostObjectService.class.getName(), HostObjectService.instance(), new Hashtable());
    }

    public void stop(BundleContext bundleContext) throws Exception {
        this.context.removeBundleListener(this);
        hostObjectRegistry.close();
        if (log.isDebugEnabled()) {
            log.debug("Stoping Mozilla Rhino bundle");
        }
    }

    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STARTED:
                hostObjectRegistry.register(event.getBundle());
                break;

            case BundleEvent.STOPPED:
                hostObjectRegistry.unregister(event.getBundle());
                break;
        }
    }
}
