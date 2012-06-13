/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.mashup.jsservices.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.Utils;
import org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * @scr.component name="mashup.jsservices.dscomponent"" immediate="true"
 * @scr.reference name="mashup.javascript.hostobjects.hostobjectservice"
 * interface="org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService"
 * cardinality="1..1" policy="dynamic" bind="setHostObjectService" unbind="unsetHostObjectService"
 */
public class JSServicesServiceComponent {
    private static Log log = LogFactory.getLog(JSServicesServiceComponent.class);
    private HostObjectService hostObjectService = null;
    List<String> hostObjectsThatNeedServices;

    public void activate(ComponentContext componentContext) {
       try {
            final BundleContext bundleContext = componentContext.getBundleContext();
            hostObjectsThatNeedServices = hostObjectService.getHostObjectsThatNeedServices();
            if (hostObjectsThatNeedServices.isEmpty()) {
                Utils.registerDeployerServices(bundleContext);
                log.info("JS Deployer initialized");
            } else {
                final HostObjectServiceListener listener = new HostObjectServiceListener(componentContext.getBundleContext(), hostObjectsThatNeedServices);
                bundleContext.addServiceListener(listener);
                listener.start();
                if (!hostObjectsThatNeedServices.isEmpty()) {
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            try {
                                if (!hostObjectsThatNeedServices.isEmpty()) {
                                    log.debug("JS Deployer initialization is delayed due to the following unsatisfied Host Objects..");
                                    for (String configItem : hostObjectsThatNeedServices) {
                                        log.debug("Waiting for required Host Object : " + configItem);
                                    }
                                } else {
                                    this.cancel();
                                    bundleContext.removeServiceListener(listener);
                                    Utils.registerDeployerServices(bundleContext);
                                    log.info("JS Deployer initialized");
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }, 2000, 2000);
                } else {
                    bundleContext.removeServiceListener(listener);
                    Utils.registerDeployerServices(bundleContext);
                    log.info("JS Deployer initialized");
                }
            }
            //JavaScriptEngineUtils.setHostObjectService(hostObjectService);
        } catch (Exception e) {
            String msg = "Failed to register JSDeployer as an OSGi service.";
            log.error(msg, e);
        }
    }

   protected void setHostObjectService(HostObjectService hostObjectService) {
        log.info("Setting hostobject services");
        this.hostObjectService = hostObjectService;
    }

    protected void unsetHostObjectService(HostObjectService hostObjectService) {
        this.hostObjectService = null;
    }
}
