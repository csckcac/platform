/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.core.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.core.deployment.BAMDeploymentInterceptor;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class Activator implements BundleActivator {

    private static final Log log = LogFactory.getLog(BundleActivator.class);

    public void start(BundleContext bundleContext) throws Exception {
        log.info("BAM Core bundle activator started.");
        if (log.isDebugEnabled()) {
            log.debug("BAM Core bundle activator started.");
        }

        BAMDeploymentInterceptor interceptor = new BAMDeploymentInterceptor();
        Dictionary ss = new Hashtable();
        ss.put(CarbonConstants.AXIS2_CONFIG_SERVICE, AxisObserver.class.getName());
        bundleContext.registerService(BAMDeploymentInterceptor.class.getName(), interceptor, ss);

        log.info("BAM Core bundle activator started.");
        if (log.isDebugEnabled()) {
            log.debug("BAMDeploymentInterceptor registered successfully.");
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
