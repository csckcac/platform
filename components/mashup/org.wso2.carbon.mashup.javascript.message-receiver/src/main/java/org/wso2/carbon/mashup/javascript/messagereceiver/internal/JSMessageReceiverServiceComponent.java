/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mashup.javascript.messagereceiver.internal;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.scriptengine.cache.CacheManager;
import org.jaggeryjs.scriptengine.engine.RhinoEngine;
import org.jaggeryjs.scriptengine.security.RhinoSecurityController;
import org.jaggeryjs.scriptengine.security.RhinoSecurityDomain;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService;
import org.wso2.carbon.mashup.javascript.messagereceiver.JavaScriptEngineUtils;
import org.wso2.carbon.mashup.utils.MashupConstants;
import org.wso2.carbon.mashup.utils.MashupSecurityDomain;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.security.PermissionCollection;


/**
 * This class is used to get OSGI services and register OSGI services.
 *
 * @scr.component name="mashup.javascript.messagereceiver.dscomponent"" immediate="true"
 * @scr.reference name="mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService"
 * interface="org.wso2.carbon.mashup.javascript.hostobjects.hostobjectservice.service.HostObjectService"
 * cardinality="1..1" policy="dynamic" bind="setHostObjectService" unbind="unsetHostObjectService"
 */
public class JSMessageReceiverServiceComponent {

    private static Log log = LogFactory.getLog(JSMessageReceiverServiceComponent.class);
    private HostObjectService hostObjectService = null;

    /**
     * This method is called when the component is being activating.
     *
     * @param componentContext  <tt>ComponentContext</tt> of the OSGI bundle
     */
    public void activate(ComponentContext componentContext) {
        try {
            JavaScriptEngineUtils.setHostObjectService(hostObjectService);
            JavaScriptEngineUtils.setEngine(new RhinoEngine(new CacheManager(null), new RhinoSecurityController() {
                @Override
                protected void updatePermissions(PermissionCollection permissions, RhinoSecurityDomain securityDomain) {
                    MashupSecurityDomain domain = (MashupSecurityDomain) securityDomain;
                    AxisService service = domain.getService();
                    File resourcesDir = (File) service.getParameterValue(MashupConstants.RESOURCES_FOLDER);
                    try {
                        String resourcesURI = resourcesDir.getCanonicalPath();
                        // Create a file read permission for web app context directory
                        if (!resourcesURI.endsWith(File.separator)) {
                            permissions.add(new FilePermission(resourcesURI, "read"));
                            resourcesURI = resourcesURI + File.separator;
                        } else {
                            permissions.add(new FilePermission(resourcesURI.substring(0, resourcesURI.length() - 1), "read"));
                        }
                        resourcesURI = resourcesURI + "-";
                        permissions.add(new FilePermission(resourcesURI, "read"));
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }));
        } catch (Exception e) {
            log.error("Failed setting the OSGI servce, HostObjectService", e);
        }
    }

    /**
     * Set method for the OSGI service, <tt>HostObjectService</tt>.
     *
     * @param hostObjectService  <tt>HostObjectService</tt> instance being set
     */
    protected void setHostObjectService(HostObjectService hostObjectService) {
        this.hostObjectService = hostObjectService;
    }

    /**
     * Unset method for the OSGI service, <tt>HostObjectService</tt>.
     *
     * @param hostObjectService  <tt>HostObjectService</tt> instance being unset
     */
    protected void unsetHostObjectService(HostObjectService hostObjectService) {
        this.hostObjectService = null;
    }
}
