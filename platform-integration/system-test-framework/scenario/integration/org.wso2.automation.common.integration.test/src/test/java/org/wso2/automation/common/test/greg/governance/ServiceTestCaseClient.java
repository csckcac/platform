/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.automation.common.test.greg.governance;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceFilter;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;

public class ServiceTestCaseClient {
    private Registry governance;
    private String configPath;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registryWS, userId);

        configPath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator +
                     "artifacts" + File.separator + "GREG" + File.separator + "services" +
                     File.separator + "service.metadata.xml";

    }

    @Test(groups = {"wso2.greg"})
    public void testAddService() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);

        Service service = serviceManager.newService(new QName("http://bang.boom.com/mnm/beep", "MyService"));
        service.addAttribute("testAttribute", "somevalue");
        serviceManager.addService(service);

        String serviceId = service.getId();
        Service newService = serviceManager.getService(serviceId);

        Assert.assertEquals(newService.getAttribute("testAttribute"), "somevalue");

        service.addAttribute("testAttribute", "somevalue2");
        serviceManager.updateService(service);

        newService = serviceManager.getService(serviceId);

        String[] values = newService.getAttributes("testAttribute");

        Assert.assertEquals(values.length, 2);
    }

    @Test(groups = {"wso2.greg"})
    public void testServiceSearch() throws Exception {
        File file = new File(configPath);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileContents = new byte[(int) file.length()];
        fileInputStream.read(fileContents);

        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(contentElement);

        service.addAttribute("custom-attribute", "custom-value");
        serviceManager.addService(service);


        // so retrieve it back
        String serviceId = service.getId();
        Service newService = serviceManager.getService(serviceId);

        Assert.assertEquals(newService.getAttribute("custom-attribute"), "custom-value");
        Assert.assertEquals(newService.getAttribute("interface_wsdlURL"),
                            "/_system/governance/trunk/wsdls/com/foo/abc.wsdl");
        Assert.assertEquals(newService.getQName(), service.getQName());

        Service service2 = serviceManager.newService(new QName("http://baps.paps.mug/done", "meep"));
        service2.addAttribute("custom-attribute", "custom-value2");
        serviceManager.addService(service2);

        Service service3 = serviceManager.newService(new QName("http://baps.paps.mug/jug", "peem"));
        service3.addAttribute("custom-attribute", "not-custom-value");
        serviceManager.addService(service3);

        Service service4 = serviceManager.newService(new QName("http://baps.dadan.mug/jug", "doon"));
        service4.addAttribute("not-custom-attribute", "custom-value3");
        serviceManager.addService(service4);

        Service[] services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("custom-attribute");
                if (attributeVal != null && attributeVal.startsWith("custom-value")) {
                    return true;
                }
                return false;
            }
        });

        Assert.assertEquals(services.length, 2);
        Assert.assertTrue(services[0].getQName().equals(service.getQName()) ||
                          services[0].getQName().equals(service2.getQName()));
        Assert.assertTrue(services[1].getQName().equals(service.getQName()) ||
                          services[1].getQName().equals(service2.getQName()));

        // update the service2
        service2.setQName(new QName("http://bom.bom.com/baaaang", "bingo"));
        serviceManager.updateService(service2);

        newService = serviceManager.getService(service2.getId());
        Assert.assertEquals(service2.getAttribute("custom-attribute"), "custom-value2");


        // do the test again
        services = serviceManager.findServices(new ServiceFilter() {
            public boolean matches(Service service) throws GovernanceException {
                String attributeVal = service.getAttribute("custom-attribute");
                if (attributeVal != null && attributeVal.startsWith("custom-value")) {
                    return true;
                }
                return false;
            }
        });

        Assert.assertEquals(services.length, 2);
        Assert.assertTrue(services[0].getQName().equals(service.getQName()) ||
                          services[0].getQName().equals(service2.getQName()));
        Assert.assertTrue(services[1].getQName().equals(service.getQName()) ||
                          services[1].getQName().equals(service2.getQName()));
    }

    @Test(groups = {"wso2.greg"})
    public void testServiceAttachments() throws Exception {
        // first put a WSDL
        WsdlManager wsdlManager = new WsdlManager(governance);

        Wsdl wsdl = wsdlManager.newWsdl("http://svn.wso2.org/repos/wso2/trunk/graphite/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl");
        wsdlManager.addWsdl(wsdl);

        ServiceManager serviceManager = new ServiceManager(governance);
        Service service = serviceManager.newService(new QName("http://test/org/bang", "myservice"));
        serviceManager.addService(service);

        service.attachWSDL(wsdl);

        Wsdl[] wsdls = service.getAttachedWsdls();

        Assert.assertEquals(wsdls.length, 1);
        Assert.assertEquals(wsdls[0].getQName(), new QName("http://foo.com", "BizService.wsdl"));

        Schema[] schemas = wsdls[0].getAttachedSchemas();

        Assert.assertEquals(schemas.length, 1);
        Assert.assertEquals(schemas[0].getQName(),
                            new QName("http://bar.org/purchasing", "purchasing.xsd"));
        Assert.assertNotNull(schemas[0].getId());

        // now add a policy.
        PolicyManager policyManager = new PolicyManager(governance);

        Policy policy = policyManager.newPolicy(
                                                       "http://svn.wso2.org/repos/wso2/tags/wsf/php/2.1.0/samples/security/" +
                                                       "complete/policy.xml");
        policy.setName("mypolicy.xml");
        policyManager.addPolicy(policy);

        service.attachPolicy(policy);

        Policy[] policies = service.getAttachedPolicies();
        Assert.assertEquals(policies.length, 1);
        Assert.assertEquals(policies[0].getQName(), new QName("mypolicy.xml"));

        File file = new File(configPath);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileContents = new byte[(int) file.length()];
        fileInputStream.read(fileContents);

        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);

        service = serviceManager.newService(contentElement);
        //serviceManager.addService(service);

        String[] serviceIds = serviceManager.getAllServiceIds();
        for (String serviceId : serviceIds) {
            Service servicex = serviceManager.getService(serviceId);
            Policy[] policiesx = servicex.getAttachedPolicies();
            if (policiesx != null && policiesx.length != 0) {
                Assert.assertEquals(policiesx[0].getQName(), new QName("mypolicy.xml"));
            }
        }

    }

    @Test(groups = {"wso2.greg"})
    public void testServiceRename() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);

        Service service = serviceManager.newService(new QName("http://banga.queek.queek/blaa", "sfosf"));
        serviceManager.addService(service);

        service.setQName(new QName("http://doc.x.ge/yong", "almdo"));
        serviceManager.updateService(service);

        Service exactServiceCopy = serviceManager.getService(service.getId());
        QName qname = exactServiceCopy.getQName();

        Assert.assertEquals(new QName("http://doc.x.ge/yong", "almdo"), qname);
        Assert.assertEquals(exactServiceCopy.getPath(), "/trunk/services/ge/x/doc/yong/almdo");


        // doing the same for a meta data service
        File file = new File(configPath);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] fileContents = new byte[(int) file.length()];
        fileInputStream.read(fileContents);

        OMElement contentElement = GovernanceUtils.buildOMElement(fileContents);

        service = serviceManager.newService(contentElement);
        serviceManager.addService(service);

        service.setQName(new QName("http://doc.x.ge/yong", "almdo"));
        serviceManager.updateService(service);

        exactServiceCopy = serviceManager.getService(service.getId());
        qname = exactServiceCopy.getQName();

        Assert.assertEquals(qname, new QName("http://doc.x.ge/yong", "almdo"));
        Assert.assertEquals(exactServiceCopy.getPath(), "/trunk/services/ge/x/doc/yong/almdo");

    }

    @Test(groups = {"wso2.greg"})
    public void testServiceDelete() throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);

        Service service = serviceManager.newService(new QName("http://banga.doom.queek/blaa", "lmnop"));
        serviceManager.addService(service);

        Service newService = serviceManager.getService(service.getId());
        Assert.assertNotNull(newService);

        serviceManager.removeService(newService.getId());
        newService = serviceManager.getService(service.getId());
        Assert.assertNull(newService);


        service = serviceManager.newService(new QName("http://banga.bang.queek/blaa", "basss"));
        serviceManager.addService(service);

        newService = serviceManager.getService(service.getId());
        Assert.assertNotNull(newService);

        governance.delete(newService.getPath());
        newService = serviceManager.getService(service.getId());
        Assert.assertNull(newService);
    }
}
