/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.list.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ListServiceUtil {
    private static final Log log = LogFactory.getLog(ListServiceUtil.class);
    public static String[] filterServices(String criteria, Registry governanceRegistry) throws RegistryException {
        try {
            ServiceManager serviceManger = new ServiceManager(governanceRegistry);
            final Service referenceService;
            if (criteria != null) {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(criteria));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement referenceServiceElement = builder.getDocumentElement();
                referenceService = serviceManger.newService(referenceServiceElement);

            //ListServiceFilter listServiceFilter = new ListServiceFilter(referenceService);
            ServiceFilter listServiceFilter = new ServiceFilter() {
                GovernanceArtifactFilter filter = new GovernanceArtifactFilter(referenceService);
                public boolean matches(Service service) throws GovernanceException {
                    return filter.matches(service);
                }
            };
            Service[] services = serviceManger.findServices(listServiceFilter);

            List<String> servicePaths = new ArrayList<String>();
            if (services != null) {
                GovernanceUtils.setTenantGovernanceSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
                for (Service service: services) {
                    String path = service.getPath();
                    if (path != null) {
                        servicePaths.add(path);
                    }
                }
                GovernanceUtils.unsetTenantGovernanceSystemRegistry();
            }
            return servicePaths.toArray(new String[servicePaths.size()]);
            }else {
                   return  serviceManger.getAllServicePaths();
            }
        } catch (Exception ignore) {
            String msg = "Error in filtering the services. " + ignore.getMessage();
            throw new RegistryException(msg);
        }
    }
}
