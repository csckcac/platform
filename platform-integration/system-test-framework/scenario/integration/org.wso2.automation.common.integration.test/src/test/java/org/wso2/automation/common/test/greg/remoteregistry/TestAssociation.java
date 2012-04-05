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

package org.wso2.automation.common.test.greg.remoteregistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.testng.annotations.*;
import org.wso2.platform.test.core.utils.gregutils.GregRemoteRegistryProvider;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;

import static org.testng.Assert.*;


import java.net.MalformedURLException;
import java.util.List;


public class TestAssociation {
    private static final Log log = LogFactory.getLog(ResourceHandling.class);
    public RemoteRegistry registry;

    @BeforeClass(alwaysRun = true)
    public void init() throws MalformedURLException, RegistryException {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new GregRemoteRegistryProvider().getRegistry(tenantId);
        removeResource();

    }


    @Test(groups = {"wso2.greg"}, description = "test add an association to resource ", priority = 1)
    public void testAddAssociationToResourceTest() throws RegistryException {
        Resource r2;
        try {
            r2 = registry.newResource();
            String path = "/testk12/testa/testbsp/test.txt";
            r2.setContent(new String("this is the content").getBytes());
            r2.setDescription("this is test desc");
            r2.setMediaType("plain/text");
            r2.setProperty("test2", "value2");

            registry.put(path, r2);
            registry.addAssociation(path, "/vtr2121/test", "testasstype1");
            registry.addAssociation(path, "/vtr2122/test", "testasstype2");
            registry.addAssociation(path, "/vtr2123/test", "testasstype3");
            assertTrue(associationPathExists(path, "/vtr2121/test"), "association Destination path not exist");
            assertTrue(associationPathExists(path, "/vtr2122/test"), "association Destination path not exist");
            assertTrue(associationPathExists(path, "/vtr2123/test"), "association Destination path not exist");
            assertTrue(associationTypeExists(path, "testasstype1"), "association Type not exist");
            assertTrue(associationTypeExists(path, "testasstype2"), "association Type not exist");
            assertTrue(associationTypeExists(path, "testasstype3"), "association Type not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            deleteResources("/testk12");
            log.info("***************Registry API Add Association To Resource Test - Passed ****************");
        } catch (RegistryException e) {
            log.error("Registry API Add Association To Resource Test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API Add Association To Resource Test -Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add an association to collection ", priority = 2)
    public void testAddAssociationToCollectionTest() throws RegistryException {
        Resource r2;
        try {
            r2 = registry.newCollection();
            String path = "/assocol1/assocol2/assoclo3";
            r2.setDescription("this is test desc");
            r2.setMediaType("plain/text");
            r2.setProperty("test2", "value2");

            registry.put(path, r2);
            registry.addAssociation(path, "/vtr2121/test", "testasstype1");
            registry.addAssociation(path, "/vtr2122/test", "testasstype2");
            registry.addAssociation(path, "/vtr2123/test", "testasstype3");
            assertTrue(associationPathExists(path, "/vtr2121/test"), "association Destination path not exist");
            assertTrue(associationPathExists(path, "/vtr2122/test"), "association Destination path not exist");
            assertTrue(associationPathExists(path, "/vtr2123/test"), "association Destination path not exist");
            assertTrue(associationTypeExists(path, "testasstype1"), "association Type not exist");
            assertTrue(associationTypeExists(path, "testasstype2"), "association Type not exist");
            assertTrue(associationTypeExists(path, "testasstype3"), "association Type not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            deleteResources("/assocol1");
            log.info("******************** REgistry API Add Association To Collection Test - Passed ***********************");
        } catch (RegistryException e) {
            log.error("REgistry API Add Association To Collection Test -Failed :" + e.getMessage());
            throw new RegistryException("REgistry API Add Association To Collection Test -Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add an association to root ", priority = 3)
    public void testAddAssociationToRootTest() throws RegistryException {
        try {
            String path = "/";
            registry.addAssociation(path, "/vtr2121/test", "testasstype1");
            registry.addAssociation(path, "/vtr2122/test", "testasstype2");
            registry.addAssociation(path, "/vtr2123/test", "testasstype3");
            assertTrue(associationPathExists(path, "/vtr2121/test"), "association Destination path not exist");
            assertTrue(associationPathExists(path, "/vtr2122/test"), "association Destination path not exist");
            assertTrue(associationPathExists(path, "/vtr2123/test"), "association Destination path not exist");
            assertTrue(associationTypeExists(path, "testasstype1"), "association Type not exist");
            assertTrue(associationTypeExists(path, "testasstype2"), "association Type not exist");
            assertTrue(associationTypeExists(path, "testasstype3"), "association Type not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            assertTrue(associationSourcepathExists(path, path), "association Source path not exist");
            registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
            registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
            registry.removeAssociation(path, "/vtr2123/test", "testasstype3");
            log.info("*************Registry API Add Association To Root Test - Passed***************");
        } catch (RegistryException e) {
            log.error("Registry API Add Association To Root Test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Add Association To Root Test -Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test add an association to collection ", priority = 4)
    public void GetCollectionAssociationTest() throws RegistryException {
        Resource r2;
        try {
            r2 = registry.newCollection();
            String path = "/getcol1/getcol2/getcol3";
            r2.setDescription("this is test desc");
            r2.setProperty("test2", "value2");

            registry.put(path, r2);
            registry.addAssociation(path, "/vtr2121/test", "testasstype1");
            registry.addAssociation(path, "/vtr2122/test", "testasstype2");
            registry.addAssociation(path, "/vtr2123/test", "testasstype3");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");
            deleteResources("/getcol1");
            log.info("***************Registry API Get Collection Association Test - Passed *******************");
        } catch (RegistryException e) {
            log.error("Registry API Get Collection Association Test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Get Collection Association Tes-Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test get an association from root ", priority = 5)
    public void GetRootAssociationTest() throws RegistryException {
        Resource r2;
        try {
            r2 = registry.newCollection();
            String path = "/";
            r2.setDescription("this is test desc");
            r2.setProperty("test2", "value2");

            registry.put(path, r2);
            registry.addAssociation(path, "/vtr21211/test", "testasstype1");
            registry.addAssociation(path, "/vtr21221/test", "testasstype2");
            registry.addAssociation(path, "/vtr21231/test", "testasstype3");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr21211/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr21221/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr21231/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");
            registry.removeAssociation(path, "/vtr21211/test", "testasstype1");
            registry.removeAssociation(path, "/vtr21221/test", "testasstype2");
            registry.removeAssociation(path, "/vtr21231/test", "testasstype3");
            log.info("***************Registry API Get Root Association Test - Passed*****************");
        } catch (RegistryException e) {
            log.error("Registry API Get Root Association Test - Failed :" + e.getMessage());
            throw new RegistryException("Registry API Get Root Association Test-Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test remove an association ", priority = 6)
    public void RemoveResourceAssociationTest() throws RegistryException {
        Resource r2;
        try {
            r2 = registry.newResource();
            String path = "/testk1234/testa/testbsp/test.txt";
            r2.setContent(new String("this is the content").getBytes());
            r2.setDescription("this is test desc");
            r2.setMediaType("plain/text");
            r2.setProperty("test2", "value2");

            registry.put(path, r2);
            registry.addAssociation(path, "/vtr2121/test", "testasstype1");
            registry.addAssociation(path, "/vtr2122/test", "testasstype2");
            registry.addAssociation(path, "/vtr2123/test", "testasstype3");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");
            registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
            registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
            registry.removeAssociation(path, "/vtr2123/test", "testasstype3");
            assertFalse(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path exists");
            assertFalse(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path exists");
            assertFalse(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path exists");
            assertFalse(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
            assertFalse(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
            assertFalse(getAssocitionbyType(path, "testasstype3"), "association Type not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");
            deleteResources("/testk1234/");
            log.info("***************Registry API Remove Resource Association Test - Passed ****************");
        } catch (RegistryException e) {
            log.error("Registry API Remove Resource Association Test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API Remove Resource Association Test-Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test remove an association ", priority = 7)
    public void RemoveCollectionAssociationTest() throws RegistryException {
        Resource r2;
        try {
            r2 = registry.newCollection();
            String path = "/assoColremove1/assoColremove2/assoColremove3";
            r2.setDescription("this is test desc");
            r2.setMediaType("plain/text");
            r2.setProperty("test2", "value2");

            registry.put(path, r2);
            registry.addAssociation(path, "/vtr2121/test", "testasstype1");
            registry.addAssociation(path, "/vtr2122/test", "testasstype2");
            registry.addAssociation(path, "/vtr2123/test", "testasstype3");

            assertTrue(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype3"), "association Type not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");

            registry.removeAssociation(path, "/vtr2121/test", "testasstype1");
            registry.removeAssociation(path, "/vtr2122/test", "testasstype2");
            registry.removeAssociation(path, "/vtr2123/test", "testasstype3");

            assertFalse(getAssocitionbyDestinationByType(path, "testasstype1", "/vtr2121/test"), "association Destination path exists");
            assertFalse(getAssocitionbyDestinationByType(path, "testasstype2", "/vtr2122/test"), "association Destination path exists");
            assertFalse(getAssocitionbyDestinationByType(path, "testasstype3", "/vtr2123/test"), "association Destination path exists");
            assertFalse(getAssocitionbyType(path, "testasstype1"), "association Type not exist");
            assertFalse(getAssocitionbyType(path, "testasstype2"), "association Type not exist");
            assertFalse(getAssocitionbyType(path, "testasstype3"), "association Type not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype1"), "association Source path not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype2"), "association Source path not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype3"), "association Source path not exist");
            deleteResources("/assoColremove1");
            log.info("****************Registry API Remove Collection Association Test - Passed *****************");
        } catch (RegistryException e) {
            log.error("Registry API Remove Collection Association Test -Failed :" + e.getMessage());
            throw new RegistryException("Registry API Remove Collection Association Test-Failed :" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "test remove an association ", priority = 8)
    public void testRemoveRootAssociationTest() throws RegistryException {
        Resource r2;
        try {
            r2 = registry.newCollection();
            String path = "/";
            r2.setDescription("this is test desc");
            r2.setMediaType("plain/text");
            r2.setProperty("test2", "value2");

            registry.put(path, r2);
            registry.addAssociation(path, "/vtr21212/test", "testasstype11");
            registry.addAssociation(path, "/vtr21222/test", "testasstype21");
            registry.addAssociation(path, "/vtr21232/test", "testasstype31");


            assertTrue(getAssocitionbyDestinationByType(path, "testasstype11", "/vtr21212/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype21", "/vtr21222/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyDestinationByType(path, "testasstype31", "/vtr21232/test"), "association Destination path not exist");
            assertTrue(getAssocitionbyType(path, "testasstype11"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype21"), "association Type not exist");
            assertTrue(getAssocitionbyType(path, "testasstype31"), "association Type not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype11"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype21"), "association Source path not exist");
            assertTrue(getAssocitionbySourceByType(path, "testasstype31"), "association Source path not exist");

            registry.removeAssociation(path, "/vtr21212/test", "testasstype11");
            registry.removeAssociation(path, "/vtr21222/test", "testasstype21");
            registry.removeAssociation(path, "/vtr21232/test", "testasstype31");

            assertFalse(getAssocitionbyDestinationByType(path, "testasstype11", "/vtr21212/test"), "association Destination path exists");
            assertFalse(getAssocitionbyDestinationByType(path, "testasstype21", "/vtr21222/test"), "association Destination path exists");
            assertFalse(getAssocitionbyDestinationByType(path, "testasstype31", "/vtr21232/test"), "association Destination path exists");
            assertFalse(getAssocitionbyType(path, "testasstype11"), "association Type not exist");
            assertFalse(getAssocitionbyType(path, "testasstype21"), "association Type not exist");
            assertFalse(getAssocitionbyType(path, "testasstype31"), "association Type not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype11"), "association Source path not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype21"), "association Source path not exist");
            assertFalse(getAssocitionbySourceByType(path, "testasstype31"), "association Source path not exist");
            log.info("****************Registry API Remove Root Association Test - Passed ******************");
        } catch (RegistryException e) {
            log.error("Registry API Remove Root Association Test -Failed:" + e.getMessage());
            throw new RegistryException("Registry API Remove Root Association Test-Failed :" + e.getMessage());
        }
    }


    private void removeResource() throws RegistryException {
        deleteResources("/testk12");
        deleteResources("/assocol1");
        deleteResources("/getcol1");
        deleteResources("/testk1234");
        deleteResources("/assoColremove1");

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


    public static boolean resourceExists(RemoteRegistry registry, String fileName)
            throws RegistryException {
        boolean value = registry.resourceExists(fileName);
        return value;
    }

    public boolean associationPathExists(String path, String assoPath)
            throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            //System.out.println(association[i].getDestinationPath());
            if (assoPath.equals(association[i].getDestinationPath())) {
                value = true;
            }
        }


        return value;
    }

    public boolean associationTypeExists(String path, String assoType) throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            association[i].getAssociationType();
            if (assoType.equals(association[i].getAssociationType())) {
                value = true;
            }
        }


        return value;
    }

    public boolean associationSourcepathExists(String path, String sourcePath)
            throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            association[i].getAssociationType();
            if (sourcePath.equals(association[i].getSourcePath())) {
                value = true;
            }
        }

        return value;
    }

    public boolean getAssocitionbyType(String path, String type) throws RegistryException {

        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;

        for (Association a2 : asso) {

            if (a2.getAssociationType().equals(type)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbySourceByType(String path, String type) throws RegistryException {

        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;

        for (Association a2 : asso) {

            if (a2.getSourcePath().equals(path)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbyDestinationByType(String path, String type,
                                                    String destinationPath)
            throws RegistryException {

        Association[] asso;
        asso = registry.getAssociations(path, type);


        boolean assoFound = false;

        for (Association a2 : asso) {

            if (a2.getDestinationPath().equals(destinationPath)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean associationNotExists(String path) throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = true;
        if (association.length > 0) {
            value = false;
        }
        return value;
    }

    public boolean getProperty(String path, String key, String value) throws RegistryException {
        Resource r3 = registry.newResource();
        try {
            r3 = registry.get(path);
        }
        catch (RegistryException e) {
            fail((new StringBuilder()).append("Couldn't get file from the path :").append(path).toString());
        }
        List propertyValues = r3.getPropertyValues(key);
        Object valueName[] = propertyValues.toArray();
        boolean propertystatus = containsString(valueName, value);
        return propertystatus;
    }

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
