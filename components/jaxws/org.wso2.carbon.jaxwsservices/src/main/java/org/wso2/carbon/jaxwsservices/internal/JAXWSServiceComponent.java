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

package org.wso2.carbon.jaxwsservices.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.jaxwsservices.JAXWSAxis2ConfigurationObserver;
import org.wso2.carbon.utils.PreAxisConfigurationPopulationObserver;

/**
 * @scr.component name="jaxws.service.component" immediate="true"
 */

public class JAXWSServiceComponent {

    private static final Log log = LogFactory.getLog(JAXWSServiceComponent.class);

    private ServiceRegistration axisConfigCtxObserverServiceRegistration;

    protected void activate(ComponentContext ctxt) {
        try {
            //Registering StatisticsAdmin as an OSGi service.
            BundleContext bundleCtx = ctxt.getBundleContext();
            axisConfigCtxObserverServiceRegistration =
                    bundleCtx.registerService(PreAxisConfigurationPopulationObserver.class.getName(),
                            new JAXWSAxis2ConfigurationObserver(), null);
            if (log.isDebugEnabled()) {
                log.debug("JAXWS bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Failed to activate JAXWS bundle", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        axisConfigCtxObserverServiceRegistration.unregister();
        if (log.isDebugEnabled()) {
            log.debug("Statistics bundle is deactivated");
        }
    }
}
