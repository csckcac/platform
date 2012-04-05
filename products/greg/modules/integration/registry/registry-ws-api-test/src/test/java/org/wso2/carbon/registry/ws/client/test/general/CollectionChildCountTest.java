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

package org.wso2.carbon.registry.ws.client.test.general;

import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class CollectionChildCountTest extends TestSetup {
    @Override
    public void runSuccessCase() {
       // super.runSuccessCase();
        try {
            getChildCountForCollection();
        } catch (RegistryException e) {
            e.printStackTrace();
            fail("Get child count for collection test failed ");

        }
    }

    @Override
    public void runFailureCase() {

    }

    public void getChildCountForCollection() throws RegistryException {
        String path = "/";
        Resource resource = registry.get(path);
        assertTrue("resource is not a collection", (resource instanceof Collection));
        Collection collection = (Collection) resource;
        assertTrue("Child count is " + collection.getChildCount(), true);
    }
}
