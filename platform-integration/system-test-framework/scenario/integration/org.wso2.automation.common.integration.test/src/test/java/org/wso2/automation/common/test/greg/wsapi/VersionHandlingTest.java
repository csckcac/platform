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
package org.wso2.automation.common.test.greg.wsapi;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

public class VersionHandlingTest {
    private static final Log log = LogFactory.getLog(VersionHandlingTest.class);
    private static WSRegistryServiceClient registry = null;
    private FrameworkProperties properties;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }


//    public void runSuccessCase() {
//        log.info("Running WS-API VersionHandlingTest Test Cases............................ ");
//
//        if (FrameworkSettings.getStratosTestStatus()) {
//            createVersions();
//            resourceContentVersioning();
//            resourcePropertyVersioning();
//            simpleCollectionVersioning();
//        }
    //
// Do not execute Below Test Scenarios -- Features are unavailable in SLive
//
//        try {
//            resourceRestore();

    //            simpleCollectionRestore();
//            advancedCollectionRestore();
//            permaLinksForCollections();
//            rootLevelVersioning();
//            permaLinksForResources();
//            GetContentOfOldData();
//        } catch (Exception e) {
////            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
////        }
////
//        log.info("Completed Running WS-API VersionHandlingTest Test Cases............................ ");
//    }
//
//  
    private void removeResource() throws RegistryException {
        deleteResources("/ver1");
        deleteResources("/v2");
        deleteResources("/ver4");
        deleteResources("/v3");
    }

    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e);
            throw new RegistryException("deleteResources RegistryException thrown:" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test Resource Hierarchy", priority = 1)
    private void testCreateVersions() throws RegistryException {
        properties = FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);
        String path = "/ver1/r1";
        int before_creating_ver = 0;
        int after_creating_ver = 0;
        if (properties.getEnvironmentSettings().is_enableSelenium()) {
            Resource r1 = registry.newResource();
            try {
                r1.setContent("some content");
                registry.put(path, r1);
                String[] r1Versions = registry.getVersions(path);

                if (r1Versions != null) {
                    before_creating_ver = r1Versions.length;

                }
                //Create version
                registry.createVersion(path);
                String[] r1Versions1 = registry.getVersions(path);
                after_creating_ver = r1Versions1.length;
                assertEquals(after_creating_ver, before_creating_ver + 1, "/ver1/r1 should have  versions.");
                Resource r1v2 = registry.get(path);
                r1v2.setContent("another content");
                registry.put(path, r1v2);
                //Create anothe version
                registry.createVersion(path);
                String[] r1Versions2 = registry.getVersions(path);
                assertEquals(r1Versions2.length, before_creating_ver + 2, "/ver1/r1 should have additional 2 version.");
                deleteResources("/ver1");
                log.info("************WS-API Create Versions test - Passed **********************");
            } catch (RegistryException e) {
                log.error("WS-API Create Versions test -Failed :" + e);
                throw new RegistryException("WS-API Create Versions test -Failed:" + e);
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test Versioning on Resource Content ", priority = 2)
    private void testResourceContentVersioning() throws RegistryException {
        String path = "/v2/r1";
        int after_creating_ver = 0;
        if (properties.getEnvironmentSettings().is_enableSelenium()) {
            Resource r1 = registry.newResource();

            try {
                r1.setContent("content 1".getBytes());
                registry.put(path, r1);
                Resource r12 = registry.get(path);
                r12.setContent("content 2".getBytes());
                registry.put(path, r12);

                //Create version
                registry.createVersion(path);

                String[] r1Versions = registry.getVersions(path);

                if (r1Versions != null) {
                    after_creating_ver = r1Versions.length;

                }

                Resource r1vv2 = registry.get(r1Versions[after_creating_ver - 1]);
                assertEquals(new String((byte[]) r1vv2.getContent()), "content 2", "r1's second version's content should be 'content 2'");
                deleteResources("/v2");
                log.info("*************WS-API Resource Content Versioning test- Passed*************");
            } catch (RegistryException e) {
                log.error("resourceContentVersioning RegistryException thrown :" + e);
                throw new RegistryException("WS-API Create Versions test -Failed:" + e);
            }
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test Versioning on Resource property Change ", priority = 3)
    private void testResourcePropertyVersioning() throws RegistryException {
        String path = "/ver4/r1";
        int after_creating_ver = 0;
        if (properties.getEnvironmentSettings().is_enableSelenium()) {
            Resource r1 = registry.newResource();
            try {
                r1.setContent("content 1");
                r1.addProperty("p1", "v1");
                registry.put(path, r1);

                Resource r1v2 = registry.get(path);
                r1v2.addProperty("p2", "v2");
                registry.put(path, r1v2);

                //Create Version
                registry.createVersion(path);
                String[] r1Versions = registry.getVersions(path);

                if (r1Versions != null) {
                    after_creating_ver = r1Versions.length;
                }

                Resource r1vv2 = registry.get(r1Versions[after_creating_ver - 1]);
                assertEquals(r1vv2.getProperty("p1"), "v1", "r1's second version should contain a property p1 with value v1");
                assertEquals(r1vv2.getProperty("p2"), "v2", "r1's second version should contain a property p2 with value v2");
                deleteResources("/ver4");
                log.info("**************WS-API Resource Property Versioning test- Passed ************");
            } catch (RegistryException e) {
                log.error("resourcePropertyVersioning RegistryException thrown :" + e);
                throw new RegistryException("WS-API Create Versions test -Failed:" + e);
            }
        }
    }

        @Test(groups = {"wso2.greg"}, description = "Test Versioning on simple collection ", priority = 4)
            private void testSimpleCollectionVersioning() throws RegistryException {
            Collection c1 = registry.newCollection();
            if (properties.getEnvironmentSettings().is_enableSelenium()) {
                try {
                    registry.put("/v3/c1", c1);
                    registry.createVersion("/v3/c1");

                    Collection c2 = registry.newCollection();
                    registry.put("/v3/c1/c2", c2);
                    registry.createVersion("/v3/c1");

                    Collection c3 = registry.newCollection();
                    registry.put("/v3/c1/c3", c3);
                    registry.createVersion("/v3/c1");

                    Collection c4 = registry.newCollection();
                    registry.put("/v3/c1/c2/c4", c4);
                    registry.createVersion("/v3/c1");

                    Collection c5 = registry.newCollection();
                    registry.put("/v3/c1/c2/c5", c5);

                    registry.createVersion("/v3/c1");
                    String[] c1Versions = registry.getVersions("/v3/c1");
                    registry.get(c1Versions[0]);
                    registry.get(c1Versions[1]);
                    registry.get(c1Versions[2]);
                    deleteResources("/v3");
                    log.info("**************WS-API Simple Collection Versioning test - Passed ***************");
                } catch (RegistryException e) {
                    log.error("WS-API Simple Collection Versioning test - Failed:" + e);
                    throw new RegistryException("WS-API Simple Collection Versioning test -Failed:" + e);
                }
            }

        }
//
//    private void resourceRestore() throws Exception {
//
//        Resource r1 = registry.newResource();
//        r1.setContent("content 1".getBytes());
//        registry.put("/test/v10/r1", r1);
//
//        Resource r1e1 = registry.get("/test/v10/r1");
//        r1e1.setContent("content 2".getBytes());
//        registry.put("/test/v10/r1", r1e1);
//        registry.put("/test/v10/r1", r1e1);
//
//        String[] r1Versions = registry.getVersions("/test/v10/r1");
//        registry.restoreVersion(r1Versions[1]);
//
//        Resource r1r1 = registry.get("/test/v10/r1");
//
//        assertEquals("Restored resource should have content 'content 1'",
//                "content 1", new String((byte[]) r1r1.getContent()));
//        log.info("resourceRestore - Passed");
//    }
//
//    private void simpleCollectionRestore() throws Exception {
//
//        Collection c1 = registry.newCollection();
//        registry.put("/test/v11/c1", c1);
//
//        registry.createVersion("/test/v11/c1");
//
//        Resource r1 = registry.newResource();
//        r1.setContent("r1c1");
//        registry.put("/test/v11/c1/r1", r1);
//
//        registry.createVersion("/test/v11/c1");
//
//        Resource r2 = registry.newResource();
//        r2.setContent("r1c1");
//        registry.put("/test/v11/c1/r2", r2);
//
//        registry.createVersion("/test/v11/c1");
//
//        String[] c1Versions = registry.getVersions("/test/v11/c1");
//        assertEquals("/test/v11/c1 should have 3 versions.", c1Versions.length, 3);
//
//        registry.restoreVersion(c1Versions[2]);
//        Collection c1r1 = (Collection) registry.get("/test/v11/c1");
//        assertEquals("version 1 of c1 should not have any children", 0, c1r1.getChildren().length);
//
//        try {
//            registry.get("/test/v11/c1/r1");
//            fail("Version 1 of c1 should not have child r1");
//        } catch (Exception e) {
//        }
//
//        try {
//            registry.get("/test/v11/c1/r2");
//            fail("Version 1 of c1 should not have child r2");
//        } catch (Exception e) {
//        }
//
//        registry.restoreVersion(c1Versions[1]);
//        Collection c1r2 = (Collection) registry.get("/test/v11/c1");
//        assertEquals("version 2 of c1 should have 1 child", 1, c1r2.getChildren().length);
//
//        try {
//            registry.get("/test/v11/c1/r1");
//        } catch (Exception e) {
//            fail("Version 2 of c1 should have child r1");
//        }
//
//        try {
//            registry.get("/test/v11/c1/r2");
//            fail("Version 2 of c1 should not have child r2");
//        } catch (Exception e) {
//
//        }
//
//        registry.restoreVersion(c1Versions[0]);
//        Collection c1r3 = (Collection) registry.get("/test/v11/c1");
//        assertEquals("version 3 of c1 should have 2 children", 2, c1r3.getChildren().length);
//
//        try {
//            registry.get("/test/v11/c1/r1");
//        } catch (Exception e) {
//            fail("Version 3 of c1 should have child r1");
//        }
//
//        try {
//            registry.get("/test/v11/c1/r2");
//        } catch (Exception e) {
//            fail("Version 3 of c1 should have child r2");
//        }
//
//        log.info("simpleCollectionRestore() -Passed");
//    }
//
//    private void advancedCollectionRestore() throws Exception {
//
//        Collection c1 = registry.newCollection();
//        registry.put("/test/v12/c1", c1);
//
//        registry.createVersion("/test/v12/c1");
//
//        Resource r1 = registry.newResource();
//        r1.setContent("r1c1".getBytes());
//        registry.put("/test/v12/c1/c11/r1", r1);
//
//        registry.createVersion("/test/v12/c1");
//
//        Collection c2 = registry.newCollection();
//        registry.put("/test/v12/c1/c11/c2", c2);
//
//        registry.createVersion("/test/v12/c1");
//
//        Resource r1e1 = registry.get("/test/v12/c1/c11/r1");
//        r1e1.setContent("r1c2".getBytes());
//        registry.put("/test/v12/c1/c11/r1", r1e1);
//
//        registry.createVersion("/test/v12/c1");
//
//        String[] c1Versions = registry.getVersions("/test/v12/c1");
//        assertEquals("c1 should have 4 versions", c1Versions.length, 4);
//
//        registry.restoreVersion(c1Versions[3]);
//
//        try {
//            registry.get("/test/v12/c1/c11");
//            fail("Version 1 of c1 should not have child c11");
//        } catch (Exception e) {
//        }
//
//        registry.restoreVersion(c1Versions[2]);
//
//        try {
//            registry.get("/test/v12/c1/c11");
//        } catch (Exception e) {
//            fail("Version 2 of c1 should have child c11");
//        }
//
//        try {
//            registry.get("/test/v12/c1/c11/r1");
//        } catch (Exception e) {
//            fail("Version 2 of c1 should have child c11/r1");
//        }
//
//        registry.restoreVersion(c1Versions[1]);
//
//        Resource r1e2 = null;
//        try {
//            r1e2 = registry.get("/test/v12/c1/c11/r1");
//        } catch (Exception e) {
//            fail("Version 2 of c1 should have child c11/r1");
//        }
//
//        try {
//            registry.get("/test/v12/c1/c11/c2");
//        } catch (Exception e) {
//            fail("Version 2 of c1 should have child c11/c2");
//        }
//
//        String r1e2Content = new String((byte[]) r1e2.getContent());
//        assertEquals("c11/r1 content should be 'r1c1", r1e2Content, "r1c1");
//
//        registry.restoreVersion(c1Versions[0]);
//
//        Resource r1e3 = registry.get("/test/v12/c1/c11/r1");
//        String r1e3Content = new String((byte[]) r1e3.getContent());
//        assertEquals("c11/r1 content should be 'r1c2", r1e3Content, "r1c2");
//    }
//
//    private void permaLinksForResources() throws Exception {
//
//        Resource r1 = registry.newResource();
//        r1.setContent("r1c1");
//        registry.put("/test/v13/r1", r1);
//        registry.put("/test/v13/r1", r1);
//
//        String[] r1Versions = registry.getVersions("/test/v13/r1");
//
//        Resource r1e1 = registry.get(r1Versions[0]);
//        assertEquals("Permalink incorrect", r1e1.getPermanentPath(), r1Versions[0]);
//
//        r1e1.setContent("r1c2");
//        registry.put("/test/v13/r1", r1e1);
//
//        r1Versions = registry.getVersions("/test/v13/r1");
//
//        Resource r1e2 = registry.get(r1Versions[0]);
//        assertEquals("Permalink incorrect", r1e2.getPermanentPath(), r1Versions[0]);
//
//        registry.restoreVersion(r1Versions[1]);
//
//        Resource r1e3 = registry.get(r1Versions[1]);
//        assertEquals("Permalink incorrect", r1e3.getPermanentPath(), r1Versions[1]);
//    }
//
//    private void permaLinksForCollections() throws Exception {
//
//        Collection c1 = registry.newCollection();
//        registry.put("/test/v14/c1", c1);
//
//        registry.createVersion("/test/v14/c1");
//
//        String[] c1Versions = registry.getVersions("/test/v14/c1");
//        Resource c1e1 = registry.get(c1Versions[0]);
//        assertEquals("Permalink incorrect", c1e1.getPermanentPath(), c1Versions[0]);
//
//        Resource r1 = registry.newResource();
//        r1.setContent("r1c1");
//        registry.put("/test/v14/c1/r1", r1);
//
//        registry.createVersion("/test/v14/c1");
//
//        c1Versions = registry.getVersions("/test/v14/c1");
//        Resource c1e2 = registry.get(c1Versions[0]);
//        assertEquals("Permalink incorrect", c1e2.getPermanentPath(), c1Versions[0]);
//
//        registry.restoreVersion(c1Versions[1]);
//
//        Resource c1e3 = registry.get(c1Versions[1]);
//        assertEquals("Permalink incorrect", c1e3.getPermanentPath(), c1Versions[1]);
//    }
//
//    private void rootLevelVersioning() throws Exception {
//
//        Resource r1 = registry.newResource();
//        r1.setContent("r1c1");
//        registry.put("/vtr1", r1);
//
//        registry.createVersion("/");
//
//        Collection c2 = registry.newCollection();
//        registry.put("/vtc2", c2);
//
//        registry.createVersion("/");
//
//        String[] rootVersions = registry.getVersions("/");
//
//        Collection rootv0 = (Collection) registry.get(rootVersions[0]);
//        String[] rootv0Children = (String[]) rootv0.getContent();
//        assertTrue("Root should have child vtr1",
//                RegistryUtils.containsAsSubString("/vtr1", rootv0Children));
//        assertTrue("Root should have child vtc2",
//                RegistryUtils.containsAsSubString("/vtc2", rootv0Children));
//
//        Collection rootv1 = (Collection) registry.get(rootVersions[1]);
//        String[] rootv1Children = (String[]) rootv1.getContent();
//        assertTrue("Root should have child vtr1",
//                RegistryUtils.containsAsSubString("/vtr1", rootv1Children));
//        assertFalse("Root should not have child vtc2",
//                RegistryUtils.containsAsSubString("/vtc2", rootv1Children));
//    }
//
////    public void testGetContentOfOldData() throws Exception {
////        Resource resource = registry.newResource();
////        String content = "Hello Out there!";
////        resource.setContent(content);
////
////        String resourcePath = "/abc2";
////        registry.put(resourcePath, resource);
////
////        Resource getResource = registry.get("/abc2");
////        String contentRetrieved = new String((byte[]) getResource.getContent());
////        assertEquals("Content does not match", content, contentRetrieved);
////
////        resource = registry.newResource();
////        String newContent = "new content";
////        resource.setContent(newContent);
////
////        registry.put(resourcePath, resource);
////
////        String[] versions = registry.getVersions(resourcePath);
////        getResource = registry.get(versions[versions.length - 1]);
////
////        contentRetrieved = new String((byte[]) getResource.getContent());
////
////        assertEquals("Content does not match", content, contentRetrieved);
////
////    }
    }
