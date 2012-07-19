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

package org.wso2.bps.integration.tests.security;

import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.security.utils.SecurityClientUtils;

/**
 * Apply security to BPEL process using the service descriptor - service.xml file
 */
public class SecurityWithServiceDescriptorTestCase {

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario - secure BPEL process with service.xml file")
    public void securityWithServiceDescriptorTest() throws Exception {
        SecurityClientUtils.sendReceiveSecurity("SWSDPService", "urn:swsdp",
                "securityWithService.xml", "<p:swsdp xmlns:p=\"http://wso2.org/bpel/sample.wsdl\">\n" +
                "      <TestPart>ww</TestPart>\n" +
                "   </p:swsdp>", "ww World");
    }

    public static void main(String[] args) {
        System.setProperty("bps.test.resource.location", "/home/waruna/WSO2/projects/src/trunk/platform/products/bps/modules/integration/org.wso2.bps.integration.tests/src/test/resources/");
        System.setProperty("user.dir", "/home/waruna/WSO2/projects/src/trunk/platform/products/bps/modules/distribution/target/wso2bps-3.0.0-SNAPSHOT");
        SecurityWithServiceDescriptorTestCase d = new SecurityWithServiceDescriptorTestCase();
        try {
            d.securityWithServiceDescriptorTest();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
