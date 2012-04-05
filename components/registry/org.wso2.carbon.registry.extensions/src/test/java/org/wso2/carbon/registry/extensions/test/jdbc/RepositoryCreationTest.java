/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.extensions.test.jdbc;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryRealm;
import org.wso2.carbon.registry.core.jdbc.EmbeddedRegistryService;
import org.wso2.carbon.registry.extensions.test.utils.BaseTestCase;


public class RepositoryCreationTest extends BaseTestCase {

    protected static Registry registry = null;
    protected static RegistryRealm realm = null;

    public void setUp() throws RegistryException {
        super.setUp();
        if (registry == null) {
            super.setUp();
            EmbeddedRegistryService embeddedRegistry = ctx.getEmbeddedRegistryService();
            registry = embeddedRegistry.getGovernanceUserRegistry("admin", "admin");
        }
    }

    public void testAxis2Repository() throws RegistryException {

        String axis2Path = "/axis2test";
        Collection axis2 = registry.newCollection();
        axis2.setMediaType("application/vnd.apache.axis2");
        registry.put(axis2Path, axis2);

        assertTrue("axis2 repo conf collection not found.", registry.resourceExists(axis2Path + "/conf"));
        assertTrue("axis2 repo modles collection not found.", registry.resourceExists(axis2Path + "/modules"));
        assertTrue("axis2 repo services collection not found.", registry.resourceExists(axis2Path + "/services"));
    }

    public void testEsbRepository() throws RegistryException {

        String esbPath = "/esbtest";
        Collection esb = registry.newCollection();
        esb.setMediaType("application/vnd.wso2.esb");
        registry.put(esbPath, esb);

        assertTrue("esb repo conf collection not found.", registry.resourceExists(esbPath + "/conf"));
        assertTrue("esb repo endpoints collection not found.", registry.resourceExists(esbPath + "/endpoints"));
        assertTrue("esb repo entries collection not found.", registry.resourceExists(esbPath + "/entries"));
        assertTrue("esb repo proxy-services collection not found.", registry.resourceExists(esbPath + "/proxy-services"));
        assertTrue("esb repo sequences collection not found.", registry.resourceExists(esbPath + "/sequences"));
        assertTrue("esb repo tasks collection not found.", registry.resourceExists(esbPath + "/tasks"));
    }

    public void testWsasRepository() throws RegistryException {

        String wsasPath = "/wsastest";
        Collection wsas = registry.newCollection();
        wsas.setMediaType("application/vnd.wso2.wsas");
        registry.put(wsasPath, wsas);

        assertTrue("wsas repo conf collection not found.", registry.resourceExists(wsasPath + "/conf"));
        assertTrue("wsas repo modles collection not found.", registry.resourceExists(wsasPath + "/modules"));
        assertTrue("wsas repo services collection not found.", registry.resourceExists(wsasPath + "/services"));
    }

    public void testSynapseRepository() throws RegistryException {

        String esbPath = "/synapsetest";
        Collection esb = registry.newCollection();
        esb.setMediaType("application/vnd.apache.synapse");
        registry.put(esbPath, esb);

        assertTrue("synapse repo conf collection not found.", registry.resourceExists(esbPath + "/conf"));
        assertTrue("synapse repo endpoints collection not found.", registry.resourceExists(esbPath + "/endpoints"));
        assertTrue("synapse repo entries collection not found.", registry.resourceExists(esbPath + "/entries"));
        assertTrue("synapse repo proxy-services collection not found.", registry.resourceExists(esbPath + "/proxy-services"));
        assertTrue("synapse repo sequences collection not found.", registry.resourceExists(esbPath + "/sequences"));
        assertTrue("synapse repo tasks collection not found.", registry.resourceExists(esbPath + "/tasks"));
    }
}