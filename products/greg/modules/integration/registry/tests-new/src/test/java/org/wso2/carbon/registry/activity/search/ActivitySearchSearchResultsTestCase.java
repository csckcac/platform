package org.wso2.carbon.registry.activity.search;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.FileAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.automation.api.clients.registry.ResourceAdminServiceClient;
import org.wso2.carbon.automation.api.clients.reporting.ReportResourceSupplierClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.UserInfo;
import org.wso2.carbon.automation.core.utils.UserListCsvReader;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.ManageEnvironment;
import org.wso2.carbon.registry.activities.services.ActivityService;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activity.search.utils.ActivitySearchUtil;
import org.wso2.carbon.registry.common.beans.ActivityBean;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.reporting.api.ReportingException;
import org.wso2.carbon.reporting.stub.ReportingResourcesSupplierReportingExceptionException;
import org.wso2.carbon.reporting.ui.BeanCollectionReportData;
import org.wso2.carbon.reporting.util.JasperPrintProvider;
import org.wso2.carbon.reporting.util.ReportParamMap;
import org.wso2.carbon.reporting.util.ReportStream;


import javax.activation.DataHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.FileAssert.fail;

/**
 * A test case which tests registry activity search operation
 */
public class ActivitySearchSearchResultsTestCase {
    private static final Log log = LogFactory.getLog(ActivitySearchSearchResultsTestCase.class);
    private String wsdlPath = "/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/";
    private String resourceName = "sample.wsdl";

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private ReportResourceSupplierClient reportResourceSupplierClient;
    private ActivityAdminServiceClient activityAdminServiceClient;

    private ManageEnvironment environment;
    UserInfo userInfo;


    @BeforeClass(groups = {"wso2.greg"})
    public void init() throws Exception {
        log.info("Initializing Tests for Activity Search");
        log.debug("Activity Search Tests Initialised");

        int userId = 0;
        userInfo = UserListCsvReader.getUserInfo(userId);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
        log.debug("Running SuccessCase");

        resourceAdminServiceClient =
                new ResourceAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        activityAdminServiceClient =
                new ActivityAdminServiceClient(environment.getGreg().getBackEndUrl(),
                                               userInfo.getUserName(), userInfo.getPassword());
        UserManagementClient userManagementClient = new UserManagementClient(environment.getGreg().getBackEndUrl(),
                                                                             environment.getGreg().getSessionCookie());
        reportResourceSupplierClient =
                new ReportResourceSupplierClient(environment.getGreg().getBackEndUrl(),
                                                 userInfo.getUserName(), userInfo.getPassword());
    }


    @Test(groups = {"wso2.greg"})

    public void addResource() throws InterruptedException, MalformedURLException,
                                     ResourceAdminServiceExceptionException, RemoteException {
        String resource = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + "artifacts" +
                          File.separator + "GREG" + File.separator +
                          "wsdl" + File.separator + "sample.wsdl";

        resourceAdminServiceClient.addResource(wsdlPath + resourceName,
                                               "application/wsdl+xml", "test resource",
                                               new DataHandler(new URL("file:///" + resource)));


        // wait for sometime until the resource has been added. The activity logs are written
        // every 10 seconds, so you'll need to wait until that's done.
        // Thread.sleep(20000);
        // assertTrue(resourceAdminServiceClient.getResource("/_system/governance/trunk/wsdls/eu/dataaccess/footballpool/"+
        // resourceName )[0].getAuthorUserName().contains(userInfo.getUserName()));

    }

    @Test(groups = {"wso2.greg"})
    public void verifySearchResultsPage() throws InterruptedException, MalformedURLException,
                                                 ResourceAdminServiceExceptionException,
                                                 RemoteException, RegistryExceptionException {

        if (null == activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                             "", 0).getActivity()) {
            FileAssert.fail();

        }

    }


    @Test(groups = {"wso2.greg"})
    public void verifySearchResultsPagination() throws InterruptedException, MalformedURLException,
                                                       ResourceAdminServiceExceptionException,
                                                       RemoteException, RegistryExceptionException {
        for (int k = 0; k < 100; k++) {
            addResource();
            resourceAdminServiceClient.deleteResource(wsdlPath + resourceName);
        }
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               "", 0).getActivity());
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               "", 1).getActivity());
    }

    @Test(groups = {"wso2.greg"})
    public void pdfReportTest()
            throws Exception, MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException, ReportingResourcesSupplierReportingExceptionException,
                   ReportingException, JRException, RegistryExceptionException {
        assertNotNull(getReportOutputStream("pdf") == null);
    }

    @Test(groups = {"wso2.greg"})
    public void htmlReportTest()
            throws Exception, MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException {
        assertNotNull(getReportOutputStream("html"));
    }

    @Test(groups = {"wso2.greg"})
    public void excelReportTest()
            throws Exception, MalformedURLException, ResourceAdminServiceExceptionException,
                   RemoteException {
        assertNotNull(getReportOutputStream("excel"));
    }

    @Test(groups = {"wso2.greg"})
    public void searchAgainLink() throws InterruptedException, MalformedURLException,
                                         ResourceAdminServiceExceptionException, RemoteException,
                                         RegistryExceptionException {
        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               "", 0).getActivity());

        assertNotNull(activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "",
                                                               "", 0).getActivity());
    }

    private ByteArrayOutputStream getReportOutputStream(String type)
            throws Exception, RemoteException,
                   ReportingResourcesSupplierReportingExceptionException,
                   ReportingException,
                   JRException,
                   RegistryException {
        ActivityService service = new ActivityService();

        activityAdminServiceClient.getActivities(environment.getGreg().getSessionCookie(), "", "", "", "", "", 0);

        ActivityBean beanOne = service.getActivities(userInfo.getUserName(), "", "", "", "", "",
                                                     environment.getGreg().getSessionCookie());

        List<ActivityBean> beanList = new ArrayList<ActivityBean>();
        beanList.add(beanOne);
        String reportResource = reportResourceSupplierClient.getReportResource(ActivitySearchUtil.COMPONENT,
                                                                               ActivitySearchUtil.TEMPLATE);
        JRDataSource jrDataSource = new BeanCollectionReportData().getReportDataSource(beanList);
        JasperPrintProvider jasperPrintProvider = new JasperPrintProvider();
        JasperPrint jasperPrint = jasperPrintProvider.createJasperPrint(jrDataSource, reportResource, new ReportParamMap[0]);

        ReportStream reportStream = new ReportStream();
        return reportStream.getReportStream(jasperPrint, type);
    }

    public void queryResourceInRootTestCase() throws RegistryException {
        Collection collection = registry.newCollection();
        collection.setDescription("Testing Collection");
        collection.setMediaType("application/vnd.wso2.xpath.query");
        collection.setChildren(new String[]{"test"});

        registry.put("/", collection);
        String[] paths =XpathQueryUtil.searchFromXpathQuery(registry,"/", "/");
        boolean pass =false;
        for(String path:paths){
            System.out.println(path);
            if(path.equals("test")||path.equals("/_system")){
                pass = true;
            }
        }
        if(!pass){
            fail("");
        }

}