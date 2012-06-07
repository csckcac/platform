/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.automation.common.test.greg.governance;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;

import static org.testng.Assert.assertNull;

public class MetaDataConcurrentTestClient {
    private static final Log log = LogFactory.getLog(MetaDataConcurrentTestClient.class);
    private static Registry governance;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS =
                new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registryWS, userId);
    }

    @Test(groups = {"wso2.greg"}, threadPoolSize = 40, invocationCount = 100,
          description = "Update the service concurrently", priority = 1)
    public void testServiceConcurrentUpdate() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        long id = Thread.currentThread().getId();
        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep",
                                                              "WSO2AutomationActiveServiceUpdate" + id));

        service.addAttribute("overview_description", "serviceAttr");
        serviceManager.addService(service);
        service.setAttribute("overview_description", "serviceAttrUpdated");
        serviceManager.addService(service);
    }

    @Test(groups = {"wso2.greg"}, description = "Update the service concurrently", priority = 2)
    public void testDeleteUpdatedServices() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        ServiceFilter filter = new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                return service.getQName().toString().contains("WSO2AutomationActiveServiceUpdate");
            }
        };

        Service[] services = serviceManager.findServices(filter);
        for (Service service : services) {
            serviceManager.removeService(service.getId());
            assertNull(serviceManager.getService(service.getId()));
        }
    }

    @Test(threadPoolSize = 40, invocationCount = 100, groups = {"wso2.greg"}, description =
            "Concurrent wsdl update", priority = 3)
    public void testWsdlConcurrentUpdate() throws GovernanceException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        try {
            for (Wsdl w : wsdlList) {
                if (w.getQName().getLocalPart().contains("WithInlinePolicyAndSchema.wsdl")) {
                    w.addAttribute("version", "0.02");
                    wsdlManager.addWsdl(w);
                    wsdlManager.updateWsdl(w);
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception thrown while updating wsdl concurrently"
                                          + e);
        }
    }
}
