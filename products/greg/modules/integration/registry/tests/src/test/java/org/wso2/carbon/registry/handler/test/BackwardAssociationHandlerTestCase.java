package org.wso2.carbon.registry.handler.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.testng.Assert.*;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.handler.stub.ExceptionException;
import org.wso2.carbon.registry.handler.stub.HandlerManagementServiceStub;
import org.wso2.carbon.registry.relations.stub.AddAssociationRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.GetAssociationTreeRegistryExceptionException;
import org.wso2.carbon.registry.relations.stub.RelationAdminServiceStub;
import org.wso2.carbon.registry.relations.stub.beans.xsd.AssociationTreeBean;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * A test case which tests backwardAssociation handler add test
 */

public class BackwardAssociationHandlerTestCase {

    private static final Log log = LogFactory.getLog(BackwardAssociationHandlerTestCase.class);
    private String loggedInSessionCookie = "";
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private HandlerManagementServiceStub handlerManagementServiceStub;
    private String frameworkPath = "";

    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing backward association handler Test");
        loggedInSessionCookie = util.login();

        frameworkPath = FrameworkSettings.getFrameworkPath();
    }

  @Test(groups = {"wso2.greg"}, description = "Add handler configuration to registry", priority = 1)
  public void testCopyHandler() throws ExceptionException, RemoteException {
      log.debug("Running SuccessCase");
      String sampleHandlerName = "backwardAssociationHandler.xml";
      String handlerName = "org.wso2.carbon.registry.backward.association.handler.BackwardAssociationHandler";
      handlerManagementServiceStub =
              TestUtils.getHandlerManagementServiceStub(loggedInSessionCookie);

      String handlerResource = TestUtils.getTestResourcesDir(frameworkPath);
      handlerResource = handlerResource + File.separator + "handler" + File.separator + sampleHandlerName;
      handlerManagementServiceStub.createHandler(fileReader(handlerResource));
      String handlerConfig = handlerManagementServiceStub.getHandlerConfiguration(handlerName);
      assertNotNull(handlerConfig, "Handler config cannot be null - " + handlerName);
  }  

    @Test(groups = {"wso2.greg"}, description = "Test backward association handler", priority = 1)
    public void testBackwardAssociation()
            throws ResourceAdminServiceExceptionException, RemoteException,
            AddAssociationRegistryExceptionException,
            GetAssociationTreeRegistryExceptionException {
        String sourceResource = "/_system/associationResource/testFileSource";
        String targetResource = "/_system/associationResource/testFileTarget";
        ResourceAdminServiceStub resourceAdminServiceStub =
                TestUtils.getResourceAdminServiceStub(loggedInSessionCookie);
        RelationAdminServiceStub relationAdminServiceStub =
                TestUtils.getRelationAdminServiceStub(loggedInSessionCookie);

        resourceAdminServiceStub.addTextResource("/_system/associationResource",
                "testFileSource", "plain/text", "testDESC", "testContent");
        assertNotNull(resourceAdminServiceStub.getResourceData(new String[]{sourceResource}),
                "Resource data is null");

        resourceAdminServiceStub.addTextResource("/_system/associationResource",
                "testFileTarget", "plain/text", "testDESC", "testContent");
        assertNotNull(resourceAdminServiceStub.getResourceData(new String[]{targetResource}),
                "Resource data is null");

        //set association type as resource
        relationAdminServiceStub.addAssociation(sourceResource, "calls", targetResource, "add");

        AssociationTreeBean associationTreeBeanSourceFile = relationAdminServiceStub.getAssociationTree(
                sourceResource, "calls");

        assertTrue(associationTreeBeanSourceFile.getAssociationTree().contains(targetResource),
                "Association not available");
        AssociationTreeBean associationTreeBeanTargetFile = relationAdminServiceStub.getAssociationTree(
                targetResource, "calledBy");
        assertTrue(associationTreeBeanTargetFile.getAssociationTree().contains(targetResource),
                "Association not available");
        assertEquals(associationTreeBeanTargetFile.getAssoType(), "calledBy", "Association type mismatch");

        //remove resources after test execution
        resourceAdminServiceStub.delete(sourceResource);
        resourceAdminServiceStub.delete(targetResource);
        resourceAdminServiceStub.delete("/_system/associationResource");
    }

    @AfterClass(groups = {"wso2.greg"})
    public void testCleanup() throws Exception {
//        handlerManagementServiceStub.deleteHandler(handlerName);
        util.logout();

    }

    public static String fileReader(String fileName) {
        String fileContent = "";
        try {
            // Open the file that is the first
            // command line parameter
            FileInputStream fstream = new
                    FileInputStream(fileName);

            // Convert our input stream to a
            // DataInputStream
            DataInputStream in =
                    new DataInputStream(fstream);

            // Continue to read lines while
            // there are still some left to read

            while (in.available() != 0) {
                fileContent = fileContent + (in.readLine());
            }

            in.close();
        } catch (Exception e) {
            System.err.println("File input error");
        }
        return fileContent;

    }

}
