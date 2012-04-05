package org.wso2.automation.common.test.greg.wsapi;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.testng.annotations.*;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.*;

import java.util.List;


public class TestAssociation {
    private static final Log log = LogFactory.getLog(TestAssociation.class);
    private static WSRegistryServiceClient registry = null;


    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int tenantId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(tenantId, ProductConstant.GREG_SERVER_NAME);
        removeResource();
    }

    @Test(groups = {"wso2.greg"}, description = "Add an association to resource", priority = 1)
    private void testAddAssociationToResource() throws Exception {
        Resource r2 = registry.newResource();
        String path = "/testk12/testa/testbsp/test.txt";

        try {
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
            log.info("**************WS-API Add Association To Resource test - Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Add Association To Resource test - Fail:" + e.getMessage());
            throw new RegistryException("WS-API Add Association To Resource test - Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Add Association To Resource test-Fail:" + e.getMessage());
            throw new Exception("WS-API Add Association To Resource test - Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Add an association to collection", priority = 2)
    private void testAddAssociationToCollection() throws Exception {
        Resource r2 = registry.newCollection();
        String path = "/assocol1/assocol2/assoclo3";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        try {
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
            log.info("**************WS-API Add an Association To Collection test-Passed***************");
        } catch (RegistryException e) {
            log.error("WS-API Add an Association To Collection test - Fail:" + e.getMessage());
            throw new RegistryException("WS-API Add an Association To Collection test - Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Add an Association To Collection test - Fail:" + e.getMessage());
            throw new Exception("WS-API Add an Association To Collection test- Fail:" + e.getMessage());
        }
    }


    @Test(groups = {"wso2.greg"}, description = "Add an association to root", priority = 3)
    private void testAddAssociationToRoot() throws Exception {
        Resource r2 = registry.newCollection();
        String path = "/";

        try {
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
            log.info("**************WS-API Add an Association To Root test - Passed");
        } catch (RegistryException e) {
            log.error("WS-API Add an Association To Root test - Fail:" + e.getMessage());
            throw new RegistryException("WS-API Add an Association To Root test- Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Add an Association To Root test - Fail:" + e.getMessage());
            throw new Exception("WS-API Add an Association To Root test - Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Get an association from resource", priority = 4)
    private void testGetResourceAssociation() throws Exception {
        Resource r2 = registry.newResource();
        String path = "/testk1234/testa/testbsp/test.txt";
        try {

            r2.setContent(new String("this is the content").getBytes());
            r2.setDescription("this is test desc");
            r2.setMediaType("plain/text");
            r2.setProperty("test2", "value2");

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
            log.info("*************WS-API Get Resource Association test - Passed **************");
        } catch (RegistryException e) {
            log.error("WS-API Get Resource Association test-Fail:" + e.getMessage());
            throw new RegistryException("WS-API Get Resource Association test- Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Get Resource Association test-Fail:" + e.getMessage());
            throw new Exception("WS-API Get Resource Association test- Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Get an association from resource", priority = 5)
    private void testGetCollectionAssociation() throws Exception {
        Resource r2 = registry.newCollection();
        String path = "/getcol1/getcol2/getcol3";
        r2.setDescription("this is test desc");
        r2.setProperty("test2", "value2");

        try {
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
            log.info("*****************WS-API Get Collection Association test - Passed *************************");
        } catch (RegistryException e) {
            log.error("WS-API Get Collection Association test-Fail:" + e.getMessage());
            throw new RegistryException("WS-API Get Collection Association test- Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Get Collection Association test - Fail:" + e.getMessage());
            throw new Exception("WS-API Get Collection Association test Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Remove an association from resource", priority = 6)
    private void testRemoveResourceAssociation() throws Exception {
        Resource r2 = registry.newResource();
        String path = "/testk123456/testa/testbsp/test.txt";
        try {

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
            deleteResources("/testk123456");
            log.info("*************WS-API Remove an association from resource test - Passed ***************");
        } catch (RegistryException e) {
            log.error("WS-API Remove an association from resource test - Fail:" + e.getMessage());
            throw new RegistryException("WS-API Remove an association from resource test- Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Remove an association from resource test - Fail:" + e.getMessage());
            throw new Exception("WS-API Remove an association from resource test- Fail:" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Remove an association from Collection", priority = 7)
    private void testRemoveCollectionAssociation() throws Exception {
        Resource r2 = registry.newCollection();
        String path = "/assoColremove1/assoColremove2/assoColremove3";
        r2.setDescription("this is test desc");
        r2.setMediaType("plain/text");
        r2.setProperty("test2", "value2");

        try {
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
            log.info("*******************WS-API Remove an association from Collection test - Passed ******************");
        } catch (RegistryException e) {
            log.error("WS-API Remove an association from Collection test-Fail" + e.getMessage());
            throw new RegistryException("WS-API Remove an association from Collection test- Fail:" + e.getMessage());
        } catch (Exception e) {
            log.error("WS-API Remove an association from Collection test -Fail:" + e.getMessage());
            throw new Exception("WS-API Remove an association from Collection test- Fail:" + e.getMessage());
        }
    }

    private void removeResource() throws RegistryException {
        deleteResources("/testk12");
        deleteResources("/assocol1");
        deleteResources("/testk123456");
        deleteResources("/testk1234");
        deleteResources("/getcol1");
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


    public static boolean resourceExists(RemoteRegistry registry, String fileName) throws Exception {
        boolean value = registry.resourceExists(fileName);
        return value;
    }

    public boolean associationPathExists(String path, String assoPath)
            throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            if (assoPath.equals(association[i].getDestinationPath()))
                value = true;
        }
        return value;
    }

    public boolean associationTypeExists(String path, String assoType) throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            association[i].getAssociationType();
            if (assoType.equals(association[i].getAssociationType()))
                value = true;
        }
        return value;
    }

    public boolean associationSourcepathExists(String path, String sourcePath) throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            association[i].getAssociationType();
            if (sourcePath.equals(association[i].getSourcePath()))
                value = true;
        }
        return value;
    }

    public boolean getAssocitionbyType(String path, String type) throws Exception {
        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;
        if (asso == null) return assoFound;
        for (Association a2 : asso) {

            if (a2.getAssociationType().equals(type)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbySourceByType(String path, String type) throws Exception {
        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;
        if (asso == null) return assoFound;
        for (Association a2 : asso) {

            if (a2.getSourcePath().equals(path)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean getAssocitionbyDestinationByType(String path, String type, String destinationPath) throws Exception {
        Association[] asso;
        asso = registry.getAssociations(path, type);

        boolean assoFound = false;

        if (asso == null) return assoFound;
        for (Association a2 : asso) {

            if (a2.getDestinationPath().equals(destinationPath)) {
                assoFound = true;
                break;
            }
        }
        return assoFound;
    }

    public boolean associationNotExists(String path) throws Exception {
        Association association[] = registry.getAllAssociations(path);
        boolean value = true;
        if (association.length > 0)
            value = false;
        return value;
    }

    public boolean getProperty(String path, String key, String value) throws Exception {
        Resource r3 = registry.newResource();
        try {
            r3 = registry.get(path);
        }
        catch (Exception e) {
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
