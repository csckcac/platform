/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bpel.core.ode.integration.config;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpoint;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointFactory;

import java.io.File;

/**
 * The configuration for endpoints used by BPEL processes.
 * The main usage is to create/configure operation clients to invoke external services
 * @see EndpointConfigBuilder
 */
public class EndpointConfiguration {

    private String serviceName;

    private String servicePort;

    private String serviceNS;

    private String mexTimeout;

    private String basePath;

    private String unifiedEndPointReference;

    /* Keeps the inline definition of a unified endpoint */
    private OMElement uepOM;

    //Cache the unified endpoint
    private UnifiedEndpoint unifiedEndpoint = null;

    public void setUnifiedEndPointReference(String unifiedEndPointReference) {
        this.unifiedEndPointReference = unifiedEndPointReference;
    }

    public void setUepOM(OMElement uepOM) {
        this.uepOM = uepOM;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServicePort() {
        return servicePort;
    }

    public void setServicePort(String servicePort) {
        this.servicePort = servicePort;
    }

    public String getServiceNS() {
        return serviceNS;
    }

    public void setServiceNS(String serviceNS) {
        this.serviceNS = serviceNS;
    }

    public String getMexTimeout() {
        return mexTimeout;
    }

    public void setMexTimeout(String mexTimeout) {
        this.mexTimeout = mexTimeout;
    }

    public UnifiedEndpoint getUnifiedEndpoint() throws AxisFault {
        if (unifiedEndpoint == null) {
            UnifiedEndpointFactory uepFactory = new UnifiedEndpointFactory();
            if ((uepOM != null ||
                    unifiedEndPointReference != null)) {
                if (uepOM != null) {
                    unifiedEndpoint = uepFactory.createEndpoint(uepOM);
                } else {
                    String uepConfPath = unifiedEndPointReference;
                    if (!uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_GOV_REG) ||
                            !uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_CONF_REG) ||
                            !uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_REG)) {
                        if (uepConfPath.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
                            uepConfPath = uepConfPath.substring(UnifiedEndpointConstants.VIRTUAL_FILE.
                                    length());
                        }
                        if (isAbsoutePath(uepConfPath)) {
                            uepConfPath = UnifiedEndpointConstants.VIRTUAL_FILE + uepConfPath;
                        } else {
                            uepConfPath = getAbsolutePath(basePath, uepConfPath);
                        }
                    }
                    unifiedEndpoint = uepFactory.createVirtualEndpoint(uepConfPath);
                }
            } else {
                unifiedEndpoint = new UnifiedEndpoint();
                unifiedEndpoint.setUepId(serviceName);
                unifiedEndpoint.setAddressingEnabled(true);
                unifiedEndpoint.setAddressingVersion(UnifiedEndpointConstants.
                        ADDRESSING_VERSION_FINAL);
            }

            if (unifiedEndpoint.isSecurityEnabled()) {
                String secPolicyKey = unifiedEndpoint.getWsSecPolicyKey();
                if (secPolicyKey.startsWith(UnifiedEndpointConstants.VIRTUAL_FILE)) {
                    String secPolicyLocation = secPolicyKey.substring(
                            UnifiedEndpointConstants.VIRTUAL_FILE.length());
                    if (!isAbsoutePath(secPolicyLocation)) {
                        secPolicyKey = getAbsolutePath(basePath,
                                secPolicyLocation);
                    } else {
                        secPolicyKey = UnifiedEndpointConstants.VIRTUAL_FILE + secPolicyLocation;
                    }
                    unifiedEndpoint.setWsSecPolicyKey(secPolicyKey);
                }
            }
        }

        return unifiedEndpoint;
    }

    private String getAbsolutePath(String basePath, String filePath) {
        return UnifiedEndpointConstants.VIRTUAL_FILE + basePath + File.separator + filePath;
    }

    public static boolean isAbsoutePath(String filePath) {
        return filePath.startsWith("/") ||
                (filePath.length() > 1 && filePath.charAt(1) == ':');
    }
}
