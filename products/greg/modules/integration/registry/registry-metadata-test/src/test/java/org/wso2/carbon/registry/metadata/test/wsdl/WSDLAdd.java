/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
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
package org.wso2.carbon.registry.metadata.test.wsdl;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.metadata.test.util.RegistryConsts;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.registry.resource.stub.beans.xsd.ResourceTreeEntryBean;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

/**
 * This class used to add WSDL files in to the governance registry using resource-admin command.
 */
public class WSDLAdd extends TestTemplate {
    private static final Log log = LogFactory.getLog(WSDLAdd.class);
    private boolean isFound;
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private RelationAdminServiceStub relationAdminServiceStub;


    @Override
    public void init() {
    }

    /**
     * runSuccessCase having two different of test-cases.adding wsdl from local file system and adding wsdl from global url.
     */
    @Override
    public void runSuccessCase() {

        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);
        relationAdminServiceStub = TestUtils.getRelationAdminServiceStub(sessionCookie);
        try {
            addWSDL();
        } catch (Exception e) {
            Assert.fail("Community feature test failed : " + e);
            log.error("Community feature test failed: " + e.getMessage());
        }
    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }

    private void addWSDL() throws Exception {
        String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                    + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator + "sample.wsdl";

        resourceAdminServiceStub.addResource("/_system/governance/wsdls/sample.wsdl",
                RegistryConsts.APPLICATION_WSDL_XML, "txtDesc", new DataHandler(new URL("file:///" + resource)), null);
        resourceAdminServiceStub.importResource("/_system/governance/wsdls", "WeatherForecastService.wsdl",
                RegistryConsts.APPLICATION_WSDL_XML, "txtDesc",
                "http://www.restfulwebservices.net/wcf/WeatherForecastService.svc?wsdl", null);
        ResourceTreeEntryBean searchFile1 = resourceAdminServiceStub.getResourceTreeEntry
                ("/_system/governance/wsdls/eu/dataaccess/footballpool");
        ResourceTreeEntryBean searchFile2 = resourceAdminServiceStub.getResourceTreeEntry
                ("/_system/governance/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01");
        String[] resourceChild1 = searchFile1.getChildren();
        String[] resourceChild2 = searchFile2.getChildren();
        for (int childCount = 0; childCount <= resourceChild1.length; childCount++) {
            if (resourceChild1[childCount].equalsIgnoreCase("/_system/governance/wsdls/eu/dataaccess/footballpool/sample.wsdl")) {
                isFound = true;
                break;
            }
        }
        if (isFound = false) {
            Assert.fail("uploaded resource not found in /_system/governance/wsdls/eu/dataaccess/footballpool/sample.wsdl");
        }
        for (int childCount = 0; childCount <= resourceChild2.length; childCount++) {
            if (resourceChild2[childCount].equalsIgnoreCase("/_system/governance/wsdls/net/restfulwebservices/www/servicecontracts/_2008/_01/WeatherForecastService.wsdl")) {
                isFound = true;
                break;
            }
        }
        if (isFound = false) {
            Assert.fail("uploaded resource not found in /_system/governance/wsdls/eu/dataaccess/footballpool/sample.wsdl");
        }
    }
}
