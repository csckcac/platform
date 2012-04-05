/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 
  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/


package org.wso2.carbon.registry.resource.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

public class ResourceAdminServiceTest extends TestTemplate {
    /**
     * @goal testing ResourceAdmin service in registry
     */

    private static final Log log = LogFactory.getLog(ResourceAdminServiceTest.class);

    @Override
    public void init() {

    }

    @Override
    public void runSuccessCase(){
        try{
        ResourceAdminServiceStub resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);

            String collectionPath = resourceAdminServiceStub.addCollection("/","Test","","");
            log.debug("collection added to " + collectionPath);
           // resourceAdminServiceStub.addResource("/Test/echo_back.xslt", "application/xml", "xslt files", null,null);
            resourceAdminServiceStub.addTextResource("/Test","Hello","text/plain","sample","Hello world");

        }
        catch (Exception e){}

    }

    @Override
    public void runFailureCase() {


    }

    @Override
    public void cleanup() {

    }
}
