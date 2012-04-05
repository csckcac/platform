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
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;


import java.util.Date;
import java.util.List;

public class TestResources {
    private static final Log log = LogFactory.getLog(TestResources.class);
    private static WSRegistryServiceClient registry = null;
    String password = "administrator";

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        password = UserListCsvReader.getUserInfo(tenantId).getPassword();
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Test Resource Hierarchy", priority = 1)
    private void testHierachicalResource() throws RegistryException {
        Resource r1 = registry.newResource();
        String content = "this is my content1";

        try {
            r1.setContent(content.getBytes());
            r1.setDescription("This is r1 file description");
            String path = "/d1/d2/d3/r1";

            try {
                registry.put(path, r1);
            } catch (RegistryException e) {
                fail("Couldn't put content to path /d1/d2/d3/r1");
            }

            Resource r1_actual = registry.newResource();
            try {
                r1_actual = registry.get("/d1/d2/d3/r1");
            } catch (RegistryException e) {
                fail("Couldn't get content from path /d1/d2/d3/r1");
            }

            assertEquals(new String((byte[]) r1.getContent()),
                    new String((byte[]) r1_actual.getContent()), "Content is not equal.");

            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals("/d1/d2/d3/r1", r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/d1/d2/d3", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(),
                    r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Resource description is not equal");

            deleteResources("/d1");
            log.info("*************WS-API Hierachical Resource test- Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Hierachical Resource test-Fail:" + e.getMessage());
            throw new RegistryException("WS-API Hierachical Resource test -Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test Update Resource content", priority = 2)
    private void testUpdateResourceContent() throws RegistryException {
        Resource r1 = registry.newResource();
        String content = "this is my content1";

        try {
            r1.setContent(content.getBytes());
            r1.setDescription("This is r1 file description");
            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            String path = "/d1/d2/d3/d4/r1";
            try {
                registry.put(path, r1);
            } catch (RegistryException e) {
                fail("Couldn't put content to path /d1/d2/d3/d4/r1");
            }

            Resource r1_actual = registry.get("/d1/d2/d3/d4/r1");
            assertEquals(new String((byte[]) r1.getContent()),
                    new String((byte[]) r1_actual.getContent()), "Content is not equal.");
            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals("/d1/d2/d3/d4/r1", r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/d1/d2/d3/d4", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(),
                    r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Author is not equal");
            assertEquals(r1.getProperty("key1"),
                    r1_actual.getProperty("key1"), "Resource properties are equal");
            assertEquals(r1.getProperty("key2"),
                    r1_actual.getProperty("key2"), "Resource properties are equal");
            assertEquals(r1.getProperty("key3_update"),
                    r1_actual.getProperty("key3_update"), "Resource properties are equal");

            String contentUpdated = "this is my content updated";
            r1.setContent(contentUpdated.getBytes());
            r1.setDescription("This is r1 file description updated");
            r1.setProperty("key1", "value1_update");
            r1.setProperty("key2", "value2_update");
            r1.setProperty("key3_update", "value3_update");

            registry.put(path, r1);
            Resource r2_actual = registry.get("/d1/d2/d3/d4/r1");
            assertEquals(new String((byte[]) r1.getContent()),
                    new String((byte[]) r2_actual.getContent()), "Content is not equal.");
            assertEquals(password, r2_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals("/d1/d2/d3/d4/r1", r2_actual.getPath(), "Can not get Resource path");
            assertEquals("/d1/d2/d3/d4", r2_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(),
                    r2_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r2_actual.getAuthorUserName(), "Author is not equal");
            assertEquals(r1.getProperty("key1"),
                    r2_actual.getProperty("key1"), "Resource properties are equal");
            assertEquals(r1.getProperty("key2"),
                    r2_actual.getProperty("key2"), "Resource properties are equal");
            assertEquals(r1.getProperty("key3_update"),
                    r2_actual.getProperty("key3_update"), "Resource properties are equal");

            deleteResources("/d1");
            log.info("************WS-API Update Resource Content test - Passed****************");
        } catch (RegistryException e) {
            log.error("WS-API Update Resource Content test-Fail:" + e.getMessage());
            throw new RegistryException("WS-API Update Resource Content test -Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test Get meta Data", priority = 3)
    private void testGetMetaData() throws RegistryException {
        Resource r1 = registry.newResource();
        String content = "this is my content1";
        try {
            r1.setContent(content.getBytes());
            r1.setDescription("This is r1 file description");
            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            String path = "/d1/d2/d3/d4/r1";
            registry.put(path, r1);

            Resource r1_actual = registry.getMetaData("/d1/d2/d3/d4/r1");
            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(r1_actual.getPath(), "/d1/d2/d3/d4/r1", "Can not get Resource path");
            assertEquals("/d1/d2/d3/d4", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(),
                    r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Author is not equal");
            assertEquals(r1_actual.getProperty("key1"), null, "Resource properties cannot be equal");
            assertEquals(r1_actual.getProperty("key2"), null, "Resource properties cannot be equal");
            assertEquals(r1_actual.getProperty("key3_update"), null, "Resource properties cannot be equal");
            deleteResources("/d1");
            log.info("*************WS-API Get MetaData test - Passed************");
        } catch (RegistryException e) {
            log.error("WS-API Get MetaData test -Failed" + e.getMessage());
            throw new RegistryException("WS-API Get MetaData test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test add another resource", priority = 4)
    private void testAddAnotherResource() throws RegistryException {
        Resource r1 = registry.newResource();
        String content = "this is my content2";

        try {
            r1.setContent(content.getBytes());
            r1.setDescription("r2 file description");
            String path = "/d1/d2/r2";
            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");

            try {
                registry.put(path, r1);
            } catch (RegistryException e) {
                fail("Couldn't put content to path /d1/d2/r2");
            }

            Resource r1_actual = registry.newResource();
            try {
                r1_actual = registry.get("/d1/d2/r2");
            } catch (RegistryException e) {
                fail("Couldn't get content from path /d1/d2/r2");
            }

            assertEquals(new String((byte[]) r1.getContent()),
                    new String((byte[]) r1_actual.getContent()), "Content is not equal.");
            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals("/d1/d2/r2", r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/d1/d2", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(),
                    r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Author is not equal");
            assertEquals(r1.getProperty("key1"),
                    r1_actual.getProperty("key1"), "Resource properties are equal");
            assertEquals(r1.getProperty("key2"),
                    r1_actual.getProperty("key2"), "Resource properties are equal");
            deleteResources("/d1");
            log.info("*************WS-API Add an Another Resource test - Passed**************");
        } catch (RegistryException e) {
            log.error("WS-API Add an Another Resource test - Failed:" + e.getMessage());
            throw new RegistryException("WS-API Add an Another Resource test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "set resource details", priority = 5)
    private void testSetResourceDetails() throws RegistryException {
        Resource r1 = registry.newResource();
        r1.setDescription("R4 collection description");
        r1.setMediaType("jpg/image");

        try {
            r1.setContent(new byte[]{(byte) 0xDE, (byte) 0xDE, (byte) 0xDE, (byte) 0xDE});

            r1.setProperty("key1", "value5");
            r1.setProperty("key2", "value3");

            String path_collection = "/c11/c12/c13/c14/r4";

            try {
                registry.put(path_collection, r1);
            } catch (RegistryException e) {
                fail("Couldn't put content to path /c11/c12/c13/c14/r4");
            }

            Resource r1_actual = null;
            try {
                r1_actual = registry.get("/c11/c12/c13/c14/r4");
            } catch (RegistryException e) {
                fail("Couldn't get content from path /c11/c12/c13/c14/r4");
            }

            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(path_collection, r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/c11/c12/c13/c14", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(), r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Author is not equal");
            assertEquals(r1.getProperty("key1"),
                    r1_actual.getProperty("key1"), "Resource properties are not equal");
            assertEquals(r1.getProperty("key2"),
                    r1_actual.getProperty("key2"), "Resource properties are not equal");
            assertEquals(r1.getMediaType(), r1_actual.getMediaType(), "Media Types are not equal");
            deleteResources("/c11");
            log.info("**************WS-API Set Resource Details test - Passed*******************");
        } catch (RegistryException e) {
            log.error("WS-API Set Resource Details test-Failed:" + e.getMessage());
            throw new RegistryException("WS-API Set Resource Details test-Failed:" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Test Collection details", priority = 6)
    private void testCollectionDetails() throws RegistryException {
        Resource r1 = registry.newResource();
        String content = "this is my content4";

        try {
            r1.setContent(content.getBytes());
            r1.setDescription("r3 file description");
            String path = "/c1/c2/c3/c4/r3";

            try {
                registry.put(path, r1);
            } catch (Exception e) {
                fail("Couldn't put Collection to path /c1/c2/c3/c4/r3");
            }

            try {
                registry.get("/c1/c2/c3");
            } catch (Exception e) {
                fail("Couldn't get content from path /c1/c2/c3");
            }

            String path_delete = "/c1/c2/c3";
            try {
                deleteResources(path_delete);
            } catch (Exception e) {
                fail("Couldn't delete content resource " + path_delete);
            }
            assertFalse(registry.resourceExists(path), "Deleted resource /r1 is returned on get.");
            deleteResources("/c1");
            log.info("*************WS-API Collection Details test - Passed**************");
        } catch (RegistryException e) {
            log.error("WS-API Collection Details test-Failed:" + e.getMessage());
            throw new RegistryException("WS-API Collection Details test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test set up Collection details", priority = 7)
    private void testSetCollectionDetails() throws RegistryException {
        Collection r1 = registry.newCollection();
        r1.setDescription("C3 collection description");
        r1.setProperty("key1", "value5");
        r1.setProperty("key2", "value3");

        String path_collection = "/c1/c2/c3";
        try {
            registry.put(path_collection, r1);
            Resource r1_actual = registry.get("/c1/c2/c3");
            assertTrue(r1_actual instanceof Collection);
            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(path_collection, r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/c1/c2", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(),
                    r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Authour is not equal");
            assertEquals(r1.getProperty("key1"),
                    r1_actual.getProperty("key1"), "Resource properties are not equal");
            assertEquals(r1.getProperty("key2"),
                    r1_actual.getProperty("key2"), "Resource properties are not equal");
            deleteResources("/c1");
            log.info("**************WS-API Set Collection Details test - Passed ********************");
        } catch (RegistryException e) {
            log.error("WS-API Set Collection Details test-Fail:" + e.getMessage());
            throw new RegistryException("WS-API Set Collection Details test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test Delete Resource", priority = 8)
    private void testDeleteResource() throws RegistryException {
        Resource r1 = registry.newResource();

        try {
            r1.setContent("this is file for deleting");
            r1.setDescription("this is the description of deleted file");
            r1.setMediaType("text/plain");
            r1.setProperty("key1", "value1");
            r1.setProperty("key2", "value2");
            String path = "/c11/c12/c13/r4";
            registry.put(path, r1);

            String path_delete = "/c11/c12/c13/r4";
            registry.delete(path_delete);
            assertFalse(registry.resourceExists("/c11/c12/c13/r4"), "Delted resource /c11/c12/c13/r4 is returned on get.");

            /*Add deleted resource again in to same path*/
            Resource r2 = registry.newResource();
            r2.setContent("This is new contenet added after deleting");
            r2.setDescription("this is desc for new resource");
            r2.setMediaType("text/plain");
            r2.setProperty("key1", "value5");
            r2.setProperty("key2", "value3");
            String path_new = "/c11/c12/c13/r4";

            try {
                registry.put(path_new, r2);
            } catch (Exception e) {
                fail("Couldn't put content to path /c11/c12/c13/r4");
            }

            Resource r1_actual = null;
            try {
                r1_actual = registry.get(path_new);
            } catch (Exception e) {
                fail("Couldn't get content of path /c11/c12/c13/r4");
            }
            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(path_new, r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/c11/c12/c13", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r2.getDescription(), r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Authour is not equal");
            assertEquals(r2.getProperty("key1"), r1_actual.getProperty("key1"), "Resource properties are equal");
            assertEquals(r2.getProperty("key2"), r1_actual.getProperty("key2"), "Resource properties are equal");
            assertEquals(r2.getMediaType(), r1_actual.getMediaType(), "Media Types is not equal");
            deleteResources("/c11");
            log.info("************WS-API Delete Resource test - Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Delete Resource test -Failed:" + e.getMessage());
            throw new RegistryException("WS-API Delete Resource test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test Delete Collection", priority = 9)
    private void testDeleteCollection() throws RegistryException {
        Resource r1 = registry.newCollection();
        r1.setDescription("this is a collection for deleting");
        r1.setMediaType("text/plain");
        r1.setProperty("key1", "value1");
        r1.setProperty("key2", "value2");
        String path = "/c20/c21/c22";

        try {
            registry.put(path, r1);
            String path_delete = "/c20/c21/c22";

            registry.delete(path_delete);
            assertFalse(registry.resourceExists("/c20/c21/c22"), "Deleted collection /c20/c21/c22 is returned on get.");

            /*Add deleted resource again in to same path*/
            Resource r2 = registry.newCollection();
            r2.setDescription("this is desc for new collection");
            r2.setProperty("key1", "value5");
            r2.setProperty("key2", "value3");
            String path_new = "/c20/c21/c22";

            try {
                registry.put(path_new, r2);
            } catch (Exception e) {
                fail("Couldn't put content to path /c20/c21/c22");
            }

            Resource r1_actual = registry.newCollection();
            try {
                r1_actual = registry.get(path_new);
            } catch (Exception e) {
                fail("Couldn't get content of path /c20/c21/c22");
            }
            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(path_new, r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/c20/c21", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r2.getDescription(), r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Authour is not equal");
            assertEquals(r2.getProperty("key1"), r1_actual.getProperty("key1"), "Resource properties are equal");
            assertEquals(r2.getProperty("key2"), r1_actual.getProperty("key2"), "Resource properties are equal");
            deleteResources("/c20");
            log.info("**************WS-API Delete Collection test - Passed ******************");
        } catch (RegistryException e) {
            log.error("WS-API Delete Collection test-Failed:" + e.getMessage());
            throw new RegistryException("WS-API Delete Collection test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test add a space in a resource name", priority = 10)
    private void testAddSpacesInResName() throws RegistryException {
        Resource r1 = registry.newResource();

        try {
            r1.setContent("this is file file content");
            r1.setDescription("this is a file name with spaces");
            r1.setMediaType("text/plain");
            r1.setProperty("key1", "value5");
            r1.setProperty("key2", "value3");

            String path = "/d11/d12/d13/r1 space";
            String actualPath = null;
            try {
                actualPath = registry.put(path, r1);
            } catch (Exception e) {
                fail("Couldn't put content to path /d11/d12/d13/r1 space");
            }

            Resource r1_actual = null;
            try {
                r1_actual = registry.get(actualPath);
            } catch (Exception e) {
                fail("Couldn't get content of path /d11/d12/d13/r1 space");
            }

            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(path, r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/d11/d12/d13", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(), r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Authour is not equal");
            assertEquals(r1.getProperty("key1"), r1_actual.getProperty("key1"), "Resource properties are not equal");
            assertEquals(r1.getProperty("key2"), r1_actual.getProperty("key2"), "Resource properties are not equal");
            assertEquals(r1.getMediaType(), r1_actual.getMediaType(), "Media Types are not equal");
            deleteResources("/d11");
            log.info("************WS-API Add Spaces to a Resource Name test- Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Add Spaces to a Resource Name test -Failed:" + e.getMessage());
            throw new RegistryException("WS-API Add Spaces to a Resource Name test -Failed:" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Test add a space in a collection name", priority = 11)
    private void testAddSpacesInCollName() throws RegistryException {
        Collection c1 = registry.newCollection();
        c1.setDescription("this is a collection name with spaces");
        c1.setProperty("key1", "value5");
        c1.setProperty("key2", "value3");
        String path = "/col1/col2/col30 space45";
        Resource r1_actual = null;
        String actualPath = null;

        try {
            actualPath = registry.put(path, c1);
            r1_actual = registry.get(path);
            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(actualPath, r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/col1/col2", r1_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(c1.getDescription(), r1_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r1_actual.getAuthorUserName(), "Authour is not equal");
            assertEquals(c1.getProperty("key1"), r1_actual.getProperty("key1"), "Resource properties are equal");
            assertEquals(c1.getProperty("key2"), r1_actual.getProperty("key2"), "Resource properties are equal");
            deleteResources("/col1");
            log.info("**************WS-APIS Add Spaces to a Collection name test - Passed**************");
        } catch (Exception e) {
            log.error("WS-APIS Add Spaces to a Collection name test -Fail:" + e.getMessage());
            throw new RegistryException("WS-APIS Add Spaces to a Collection name test -Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test add a space in a collection name", priority = 12)
    private void testAddResourceFromURL() throws RegistryException {
        String path = "/d25/d21/d23/d24/r1";
        String url = "http://shortwaveapp.com/waves.txt";

        Resource r1 = registry.newResource();
        r1.setDescription("this is a file imported from url");
        r1.setMediaType("java");
        r1.setProperty("key1", "value5");
        r1.setProperty("key2", "value3");

        try {
            registry.importResource(path, url, r1);
            Resource r1_actual = registry.newResource();
            r1_actual = registry.get(path);

            boolean content = true;
            if (r1_actual == null) {
                content = false;
            }
            assertTrue(content, "Imported file is empty");
            assertEquals(password, r1_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(path, r1_actual.getPath(), "Can not get Resource path");
            assertEquals("/d25/d21/d23/d24", r1_actual.getParentPath(), "Can not get Resource parent path");
            //assertEquals("Resource description is not equal", r1.getDescription(), r1_actual.getDescription());
            assertEquals(password, r1_actual.getAuthorUserName(), "Authour is not equal");
            assertEquals(r1.getProperty("key1"), r1_actual.getProperty("key1"), "Resource properties are equal");
            assertEquals(r1.getProperty("key2"), r1_actual.getProperty("key2"), "Resource properties are equal");
            deleteResources("/d25");
            log.info("************WS-API Add Resource From URL test - Passed**************");
        } catch (Exception e) {
            log.error("WS-API Add Resource From URL test -Failed:" + e.getMessage());
            throw new RegistryException("WS-API Add Resource From URL test -Failed:" + e.getMessage());
        }


    }

    @Test(groups = {"wso2.greg"}, description = "Test rename a resource", priority = 13)
    private void testRenameResource() throws RegistryException {
        Resource r1 = registry.newResource();
        String content = "this is my content";

        try {
            r1.setContent(content.getBytes());
            r1.setDescription("This is r1 file description");
            String path = "/d30/d31/r1";

            try {
                registry.put(path, r1);
            } catch (Exception e) {
                fail("Couldn't put content to path" + path);
            }

            Resource r1_actual = registry.newResource();
            try {
                r1_actual = registry.get(path);
            } catch (Exception e) {
                fail("Couldn't get content from path" + path);
            }

            assertEquals(new String((byte[]) r1.getContent()),
                    new String((byte[]) r1_actual.getContent()), "Content is not equal.");

            /*rename the resource*/

            String new_path = "/d33/d34/r1";
            try {
                registry.rename(path, new_path);
            } catch (Exception e) {
                fail("Can not rename the path from" + path + "to" + new_path);
            }

            Resource r2_actual = registry.newResource();
            try {
                r2_actual = registry.get(new_path);
            } catch (Exception e) {
                fail("Couldn't get content from path" + new_path);
            }
            assertEquals(password, r2_actual.getLastUpdaterUserName(), "LastUpdatedUser is not Equal");
            assertEquals(new_path, r2_actual.getPath(), "Can not get Resource path");
            assertEquals("/d33/d34", r2_actual.getParentPath(), "Can not get Resource parent path");
            assertEquals(r1.getDescription(), r2_actual.getDescription(), "Resource description is not equal");
            assertEquals(password, r2_actual.getAuthorUserName(), "Authour is not equal");
            assertEquals(r1.getProperty("key1"), r2_actual.getProperty("key1"), "Resource properties are equal");
            assertEquals(r1.getProperty("key2"), r2_actual.getProperty("key2"), "Resource properties are equal");
            deleteResources("/d33");
            deleteResources("/d30");
            log.info("*************WS-API Rename Resource test - Passed************");
        } catch (RegistryException e) {
            log.error("WS-API Rename Resource test - Failed:" + e.getMessage());
            throw new RegistryException("WS-API Rename Resource test -Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test Delete & Update a resource", priority = 14)
    private void testDeleteAndUpdateResource() throws RegistryException {
        Resource r1 = registry.newResource();
        String content = "this is my content";

        try {
            r1.setContent(content.getBytes());
            r1.setDescription("This is r1 file description");
            String path = "/d40/d43/r1";

            try {
                registry.put(path, r1);
            } catch (Exception e) {
                fail("Couldn't put content to path" + path);
            }

            Resource r1_actual = registry.newResource();
            try {
                r1_actual = registry.get(path);
            } catch (Exception e) {
                fail("Couldn't get content from path" + path);
            }

            assertEquals(new String((byte[]) r1.getContent()),
                    new String((byte[]) r1_actual.getContent()), "Content is not equal.");

            boolean deleted = true;
            try {
                registry.delete(path);
            } catch (Exception e) {
                fail("Couldn't delete the resource from path" + path);
                deleted = false;
            }

            assertTrue(deleted, "Resource not deleted");

            /*add the same resource again*/

            Resource r2 = registry.newResource();
            String content2 = "this is my content updated";
            r2.setContent(content2.getBytes());
            r2.setDescription("This is r1 file description");

            String path_new = "/d40/d43/r1";
            try {
                registry.put(path_new, r2);
            } catch (Exception e) {
                fail("Couldn't put content to path" + path_new);
            }

            Resource r1_actual2 = registry.newResource();
            try {
                r1_actual2 = registry.get(path_new);
            } catch (Exception e) {
                fail("Couldn't get content from path" + path_new);
            }

            assertEquals(new String((byte[]) r2.getContent()),
                    new String((byte[]) r1_actual2.getContent()), "Content is not equal.");
            deleteResources("/d40");
            log.info("************WS-API Delete And Update a Resource test - Passed*************");
        } catch (RegistryException e) {
            log.error("WS-API Delete And Update a Resource test - Failed:" + e.getMessage());
            throw new RegistryException("WS-API Delete And Update a Resource test -Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test mulitple properties of a resource", priority = 15)
    private void testResourceMultipleProperties() throws RegistryException {

        try {
            String path = "/m11/m12/r1";
            Resource r1 = registry.newResource();
            String content = "this is my content";
            r1.setContent(content.getBytes());
            r1.setDescription("This is r1 file description");

            r1.addProperty("key1", "value1");
            r1.addProperty("key1", "value2");
            r1.addProperty("key1", "value3");
            r1.addProperty("key2", "value1");
            r1.addProperty("key2", "value2");

            registry.put(path, r1);

            Resource r1_actual2 = registry.get(path);

            assertEquals(new String((byte[]) r1.getContent()),
                    new String((byte[]) r1_actual2.getContent()), "Content is not equal.");

            List propertyValues = r1_actual2.getPropertyValues("key1");
            Object[] valueName = propertyValues.toArray();

            List propertyValuesKey2 = r1_actual2.getPropertyValues("key2");
            Object[] valueNameKey2 = propertyValuesKey2.toArray();

            assertTrue(containsString(valueName, "value1"), "value1 is not associated with key1");
            assertTrue(containsString(valueName, "value2"), "value2 is not associated with key1");
            assertTrue(containsString(valueName, "value3"), "value3 is not associated with key1");
            assertTrue(containsString(valueNameKey2, "value1"), "value1 is not associated with key2");
            assertTrue(containsString(valueNameKey2, "value2"), "value2 is not associated with key2");
            deleteResources("/m11");
            log.info("*************WS-API Resource Multiple Properties test - Passed ************");
        } catch (Exception e) {
            log.error("WS-API Resource Multiple Properties test -Failed:" + e.getMessage());
            throw new RegistryException("WS-API Resource Multiple Properties test-Failed:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test mulitple properties of a resource", priority = 16)
    private void testCollectionMultipleProperties() throws RegistryException {
        try {
            String path = "/m15/m16/m17";
            Resource r1 = registry.newCollection();

            r1.setDescription("This m17 description");
            r1.addProperty("key1", "value1");
            r1.addProperty("key1", "value2");
            r1.addProperty("key1", "value3");
            r1.addProperty("key2", "value1");
            r1.addProperty("key2", "value2");

            registry.put(path, r1);

            Resource r1_actual2 = registry.get(path);

            List propertyValues = r1_actual2.getPropertyValues("key1");
            Object[] valueName = propertyValues.toArray();


            List propertyValuesKey2 = r1_actual2.getPropertyValues("key2");
            Object[] valueNameKey2 = propertyValuesKey2.toArray();
            assertTrue(containsString(valueName, "value1"), "value1 is not associated with key1");
            assertTrue(containsString(valueName, "value2"), "value2 is not associated with key1");
            assertTrue(containsString(valueName, "value3"), "value3 is not associated with key1");
            assertTrue(containsString(valueNameKey2, "value1"), "value1 is not associated with key2");
            assertTrue(containsString(valueNameKey2, "value2"), "value2 is not associated with key2");
            deleteResources("/m15");
            log.info("*************WS-API Collection Multiple Properties test - Passed**************");
        } catch (Exception e) {
            log.error("WS-API Collection Multiple Properties test -Failed:" + e.getMessage());
            throw new RegistryException("WS-API Collection Multiple Properties test-Failed:" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Test Last Modified Date Change", priority = 17)
    private void testLastModifiedDateChange() throws InterruptedException, RegistryException {
        Resource resource = registry.newResource();
        String content = "Hello Out there!";
        try {
            resource.setContent(content);
            String resourcePath = "/lastModTest2";
            registry.put(resourcePath, resource);

            Resource getResource = registry.get(resourcePath);
            Date lastMod = getResource.getLastModified();

            //wait for some times
            log.info("Sleeping for 5000 milliseconds...");
            Thread.sleep(5000);
            log.info("Woke-up after 5000 milliseconds..");

            getResource = registry.get(resourcePath);

            assertEquals(lastMod, getResource.getLastModified(), "Invalid lastModified time.");
            deleteResources("/lastModTest2");
            log.info("*************WS-API Last Modified Date Change test - Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Last Modified Date Change test -Failed:" + e.getMessage());
            throw new RegistryException("WS-API Last Modified Date Change test -Failed:" + e.getMessage());
        } catch (InterruptedException e) {
            log.error("WS-API Last Modified Date Change test -Failed:" + e.getMessage());
            throw new InterruptedException("WS-API Last Modified Date Change test -Failed:" + e.getMessage());
        }
    }


    private void removeResource() throws RegistryException {
        deleteResources("/d1");
        deleteResources("/col1");
        deleteResources("/lastModTest2");
        deleteResources("/m15");
        deleteResources("/m11");
        deleteResources("d40");
        deleteResources("/c1");
        deleteResources("/c11");
        deleteResources("/c20");
        deleteResources("/d11");
        deleteResources("/d25");
        deleteResources("/d33");
        deleteResources("/d30");
        deleteResources("/d40");
    }


    public void deleteResources(String resourceName) throws RegistryException {
        try {
            if (registry.resourceExists(resourceName)) {
                registry.delete(resourceName);
            }
        } catch (RegistryException e) {
            log.error("deleteResources RegistryException thrown:" + e.getMessage());
            throw new RegistryException("deleteResources RegistryException thrown:" + e.getMessage());
        }
    }

    //  The below method is used in the above methods.
    private boolean containsString(Object[] array, String value) {

        boolean found = false;
        for (Object anArray : array) {
            String s = anArray.toString();
            if (s.startsWith(value)) {
                found = true;
                break;
            }
        }

        return found;
    }

}
