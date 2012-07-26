package org.wso2.carbon.registry.reporting.test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.registry.ReportAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.automation.utils.registry.RegistryProviderUtil;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.reporting.stub.ReportingAdminServiceCryptoExceptionException;
import org.wso2.carbon.registry.reporting.stub.ReportingAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.reporting.stub.ReportingAdminServiceTaskExceptionException;
import org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

public class ReportingTestCase {

    private ManageEnvironment environment;
    private Registry governance;
    int userId = 1;
    UserInfo userInfo = UserListCsvReader.getUserInfo(userId);

    @BeforeClass
    public void initialize()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        WSRegistryServiceClient registry = registryProviderUtil.getWSRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = registryProviderUtil.getGovernanceRegistry(registry, userId);
    }

    /*
   * test case #1 : Add new report with valid name & verify report generation
   *
   * 1. Upload the TestGovernanceLC.jrxml to /_system/governance/repository/components/org.wso2.carbon.governance/templates
   * 2. Upload TestGovernanceCycle.rxt to any location you want
   * 3. Add extension TestingLCReportGenerator.jar
   * 4. Create necessary ReportConfigurationBean
   * 5. call saveReport method with bean as a parameter
   * */

    /**
     * Upload the TestGovernanceLC.jrxml
     *
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add templates")
    public void testAddTemplates()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" +
                              File.separator + "reports" + File.separator + "TestGovernanceLC.jrxml";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC.jrxml", "application/xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient.getResource("/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC.jrxml")[0].getAuthorUserName()
                           .contains(userInfo.getUserName()));

        resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" +
                       File.separator + "reports" + File.separator + "application_template.jrxml";

        dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/repository/components/org.wso2.carbon.governance/templates/application_template.jrxml", "application/xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient.getResource("/_system/governance/repository/components/org.wso2.carbon.governance/templates/application_template.jrxml")[0].getAuthorUserName()
                           .contains(userInfo.getUserName()));
    }

    /**
     * Upload TestGovernanceCycle.rxt
     *
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add RXT", dependsOnMethods = "testAddTemplates")
    public void testAddRXTs()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" +
                              File.separator + "reports" + File.separator + "TestGovernanceCycle.rxt";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/repository/components/org.wso2.carbon.governance/types/TestGovernanceCycle.rxt", "application/vnd.wso2.registry-ext-type+xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient.getResource("/_system/governance/repository/components/org.wso2.carbon.governance/types/TestGovernanceCycle.rxt")[0].getAuthorUserName()
                           .contains(userInfo.getUserName()));

        resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" +
                       File.separator + "reports" + File.separator + "application.rxt";

        dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt", "application/vnd.wso2.registry-ext-type+xml", "TstDec", dh);

        assertTrue(resourceAdminServiceClient.getResource("/_system/governance/repository/components/org.wso2.carbon.governance/types/application.rxt")[0].getAuthorUserName()
                           .contains(userInfo.getUserName()));
    }

    /**
     * Add extension TestingLCReportGenerator.jar
     *
     * @throws RemoteException
     * @throws MalformedURLException
     * @throws ResourceAdminServiceExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add JAR", dependsOnMethods = "testAddRXTs")
    public void testAddExtensionJARs()
            throws RemoteException, MalformedURLException, ResourceAdminServiceExceptionException {
        ResourceAdminServiceClient resourceAdminServiceClient = new ResourceAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());
        String resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" +
                              File.separator + "reports" + File.separator + "TestingLCReportGenerator.jar";

        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addExtension("TestingLCReportGenerator.jar", dh);

        resourcePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" + File.separator + "GREG" +
                       File.separator + "reports" + File.separator + "org.wso2.carbon.registry.samples.extensions.application-4.5.0.jar";

        dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addExtension("org.wso2.carbon.registry.samples.extensions.application-4.5.0.jar", dh);

        String[] extensions = resourceAdminServiceClient.listExtensions();

        boolean assertVal1 = false;
        boolean assertVal2 = false;

        for (int i = 0; i < extensions.length; i++) {
            if (extensions[i].equals("TestingLCReportGenerator.jar")) {
                assertVal1 = true;
            }
            if (extensions[i].equals("org.wso2.carbon.registry.samples.extensions.application-4.5.0.jar")) {
                assertVal2 = true;
            }
            if (assertVal1 && assertVal2) {
                break;
            }
        }

        assertTrue(assertVal1);
        assertTrue(assertVal2);
    }

    /**
     * Add report config and assert
     *
     * @throws Exception
     */

    @Test(groups = "wso2.greg", description = "Add Report", dependsOnMethods = "testAddExtensionJARs")
    public void testSaveReport()
            throws Exception {
        ReportAdminServiceClient reportAdminServiceClient = new ReportAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC.jrxml");
        configurationBean.setType("HTML");
        configurationBean.setReportClass("org.wso2.carbon.registry.samples.reporting.TestingLCReportGenerator");
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient.getSavedReport("TestGovernanceLCReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

    }

    /**
     * method to convert a
     * org.wso2.carbon.registry.reporting.stub.beans.xsd.ReportConfigurationBean
     * to a
     * org.wso2.carbon.registry.common.beans.ReportConfigurationBean
     *
     * @param toChange
     * @return
     */
    private org.wso2.carbon.registry.common.beans.ReportConfigurationBean createCommonBean(
            ReportConfigurationBean toChange) {
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retCommonBean = new org.wso2.carbon.registry.common.beans.ReportConfigurationBean();
        retCommonBean.setName(toChange.getName());
        retCommonBean.setTemplate(toChange.getTemplate());
        retCommonBean.setType(toChange.getType());
        retCommonBean.setReportClass(toChange.getReportClass());

        return retCommonBean;
    }


    @Test(groups = "wso2.greg", description = "Add Artifact", dependsOnMethods = "testSaveReportNameWithSpaces")
    public void testAddArtifact()
            throws RegistryException {
        GovernanceUtils.loadGovernanceArtifacts((UserRegistry) governance);
        GenericArtifactManager artifactManager = new GenericArtifactManager(governance, "testGovernance");
        GenericArtifact artifact = artifactManager.newGovernanceArtifact(new QName("testCycle1"));

        artifact.setAttribute("details_govCycleName", "G-reg testing");
        artifact.setAttribute("details_product", "governance registry");
        artifact.setAttribute("details_version", "4.5.0");
        artifact.setAttribute("details_addedby", "Evanthika Amarasiri");
        artifact.setAttribute("details_qa", "Amal Perera");
        artifact.setAttribute("details_qaa", "Krishantha Samaraweera");
        artifact.setAttribute("details_comments", "Smoke test");

        artifact.setAttribute("functionalTestCases_feature", "REST API");
        artifact.setAttribute("functionalTestCases_url", "https://10.200.3.57:9443/carbon/REST");
        artifact.setAttribute("functionalTestCases_comment", "Created by Sameera");

        artifactManager.addGenericArtifact(artifact);

        GenericArtifact recievedArtifact = artifactManager.getGenericArtifact(artifact.getId());

        assertTrue(artifact.getQName().toString().contains("testCycle1"), "artifact name not found");

    }

    /**
     * verifies report generation
     *
     * @throws AxisFault
     * @throws Exception
     */
    @Test(groups = "wso2.greg", dataProvider = "reportName", description = "Generate report bytes",
          dependsOnMethods = "testAddArtifact")
    public void testGetReportBytes(String reportName)
            throws AxisFault,
                   Exception {
        ReportAdminServiceClient reportAdminServiceClient = new ReportAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName(reportName);
        configurationBean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC.jrxml");
        configurationBean.setType("HTML");
        configurationBean.setReportClass("org.wso2.carbon.registry.samples.reporting.TestingLCReportGenerator");

        String[] attributes = {"responsibleQA|paramQA", "responsibleQAA|paramQAA"};

        configurationBean.setAttributes(attributes);

        DataHandler report = reportAdminServiceClient.getReportBytes(configurationBean);

        String result = readInputStreamAsString(report.getInputStream());

        assertTrue(result.contains("testCycle1"));
        assertTrue(result.contains("G-reg testing"));
        assertTrue(result.contains("4.5.0"));
        assertTrue(result.contains("Smoke test"));

    }

    public String readInputStreamAsString(InputStream in)
            throws IOException {

        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    /**
     * Add report config with a name with spaces and assert
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report config with a name with spaces",
          dependsOnMethods = "testSaveReport")
    public void testSaveReportNameWithSpaces()
            throws Exception {
        ReportAdminServiceClient reportAdminServiceClient = new ReportAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("Test Governance Life Cycle Report");
        configurationBean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC.jrxml");
        configurationBean.setType("HTML");
        configurationBean.setReportClass("org.wso2.carbon.registry.samples.reporting.TestingLCReportGenerator");
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient.getSavedReport("Test Governance Life Cycle Report");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

    }

    /**
     * Add report with a name which already exists and assert
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report with a name which already exists", dependsOnMethods = "testGetReportBytes")
    public void testSaveReportNameWhichExist()
            throws Exception {
        ReportAdminServiceClient reportAdminServiceClient = new ReportAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("TestGovernanceLCReport");
        configurationBean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/application_template.jrxml");
        configurationBean.setType("HTML");
        configurationBean.setReportClass("org.wso2.carbon.registry.samples.reporting.ApplicationReportGenerator");
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient.getSavedReport("TestGovernanceLCReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

    }

    /**
     * Add a report with a name which contains special characters  (~!@#;%^*+={}|<>,'"\)
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report", expectedExceptions = java.lang.Exception.class, dependsOnMethods = "testSaveReportNameWhichExist")
    public void testSaveReportNameSpecialCharacter()
            throws Exception {
        ReportAdminServiceClient reportAdminServiceClient = new ReportAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("Test~Governance@L#CReport");
        configurationBean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC.jrxml");
        configurationBean.setType("HTML");
        configurationBean.setReportClass("org.wso2.carbon.registry.samples.reporting.TestingLCReportGenerator");
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient.getSavedReport("Test~Governance@L#CReport");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

    }

    /**
     * Delete an existing report & search for the report
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report", dependsOnMethods = "testSaveReportNameSpecialCharacter")
    public void testDeleteReport()
            throws Exception

    {
        ReportAdminServiceClient reportAdminServiceClient = new ReportAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());

        reportAdminServiceClient.deleteSavedReport("TestGovernanceLCReport");

        ReportConfigurationBean retrievedBean[] = reportAdminServiceClient.getSavedReports();

        boolean assertVal = true; //assertVal remains "true" if deleted report config does not exist

        for (int i = 0; i < retrievedBean.length; i++) {
            ReportConfigurationBean reportConfigurationBean = retrievedBean[i];
            if (reportConfigurationBean.getName().equals("TestGovernanceLCReport")) {
                assertVal = false;
                break;
            }
        }

        assertTrue(assertVal);

    }

    /**
     * Delete an existing report, add a new report from the same name but with different report templates & classes and verify whether correct report is generated
     *
     * @throws Exception
     */
    @Test(groups = "wso2.greg", description = "Add Report", dependsOnMethods = "testSaveReportNameSpecialCharacter")
    public void testDeleteANDAddReport()
            throws Exception

    {
        ReportAdminServiceClient reportAdminServiceClient = new ReportAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());

        reportAdminServiceClient.deleteSavedReport("Test Governance Life Cycle Report");

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("Test Governance Life Cycle Report");
        configurationBean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/application_template.jrxml");
        configurationBean.setType("HTML");
        configurationBean.setReportClass("org.wso2.carbon.registry.samples.reporting.ApplicationReportGenerator");
        reportAdminServiceClient.saveReport(configurationBean);

        org.wso2.carbon.registry.common.beans.ReportConfigurationBean configurationCommonBean = createCommonBean(configurationBean);

        ReportConfigurationBean retrievedBean = reportAdminServiceClient.getSavedReport("Test Governance Life Cycle Report");
        org.wso2.carbon.registry.common.beans.ReportConfigurationBean retrievedCommonBean = createCommonBean(retrievedBean);

        assertEquals(configurationCommonBean, retrievedCommonBean);

    }

    /**
     * Add report without a name and assert
     *
     * @throws Exception
     * @throws RemoteException
     * @throws ReportingAdminServiceRegistryExceptionException
     *
     * @throws ReportingAdminServiceCryptoExceptionException
     *
     * @throws ReportingAdminServiceTaskExceptionException
     *
     */
    @Test(groups = "wso2.greg", description = "Add Report without a name", expectedExceptions = java.lang.Exception.class, dependsOnMethods = "testDeleteANDAddReport")
    public void testSaveReportWithoutName()
            throws Exception {
        ReportAdminServiceClient reportAdminServiceClient = new ReportAdminServiceClient(
                environment.getGreg().getProductVariables().getBackendUrl(), userInfo.getUserName(), userInfo.getPassword());

        ReportConfigurationBean configurationBean = new ReportConfigurationBean();
        configurationBean.setName("");
        configurationBean.setTemplate("/_system/governance/repository/components/org.wso2.carbon.governance/templates/TestGovernanceLC.jrxml");
        configurationBean.setType("HTML");
        configurationBean.setReportClass("org.wso2.carbon.registry.samples.reporting.TestingLCReportGenerator");
        reportAdminServiceClient.saveReport(configurationBean);

    }

    @DataProvider(name = "reportName")
    public Object[][] reportName() {
        return new Object[][]{
                {"TestGovernanceLCReport"},
                {"Test Governance Life Cycle Report"}
        };
    }
}


