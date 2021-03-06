/**
 *  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 **/

package org.wso2.carbon.mediator.throttle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.mediator.service.MediatorService;

import java.util.Properties;

/**
 *
 */
public class ThrottleMediatorActivator implements BundleActivator {

    private static final Log log = LogFactory.getLog(ThrottleMediatorActivator.class);

    public void start(BundleContext bundleContext) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug("Starting the throttle mediator component ...");
        }

        Properties props = new Properties();
        bundleContext.registerService(
                MediatorService.class.getName(), new ThrottleMediatorService(), props);

        bundleContext.registerService(
                MediatorService.class.getName(), new OnAcceptMediatorService(), props);

        bundleContext.registerService(
                MediatorService.class.getName(), new OnRejectMediatorService(), props);

        if (log.isDebugEnabled()) {
            log.debug("Successfully registered the throttle mediator service");
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Stopped the throttle mediator component ...");
        }
    }
}
