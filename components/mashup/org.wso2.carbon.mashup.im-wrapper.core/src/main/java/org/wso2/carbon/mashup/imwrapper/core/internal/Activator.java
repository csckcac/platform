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
package org.wso2.carbon.mashup.imwrapper.core.internal;

import org.osgi.framework.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class Activator implements BundleActivator, BundleListener {

    private static final Log log = LogFactory.getLog(Activator.class);
    private BundleContext context;
    private static IMImplementationRegistry registry = new IMImplementationRegistry();

    public void start(BundleContext bundleContext) throws Exception {
        this.context = bundleContext;
        context.addBundleListener(this);
        Bundle bundles[] = context.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getState() >= Bundle.RESOLVED) {
                registry.register(bundle);
            }
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        this.context.removeBundleListener(this);
        registry.close();
    }

    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STARTED:
                registry.register(event.getBundle());
                break;

            case BundleEvent.STOPPED:
                registry.unregister(event.getBundle());
                break;
        }
    }

    public static Map<String, String> getIMImplementatios() {
        return registry.getImplementatiomMap();
    }
}