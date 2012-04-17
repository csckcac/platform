package org.wso2.carbon.bam.client;


import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.bam.analyzer.stub.AnalyzerAdminServiceAnalyzerException;
import org.wso2.carbon.bam.analyzer.stub.AnalyzerAdminServiceStub;
import org.wso2.carbon.bam.core.stub.ConnectionAdminServiceConfigurationException;
import org.wso2.carbon.bam.core.stub.ConnectionAdminServiceStub;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceConfigurationException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceIndexingException;
import org.wso2.carbon.bam.index.stub.IndexAdminServiceStub;
import org.wso2.carbon.bam.index.stub.service.types.IndexDTO;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.NetworkUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class Client {

    private static String ANALYZER_ADMIN_SERVICE_URL = "AnalyzerAdminService";
    private static String INDEX_ADMIN_SERVICE_URL = "IndexAdminService";
    
    private static AnalyzerAdminServiceStub analyzerAdminServiceStub;
    private static IndexAdminServiceStub indexAdminServiceStub;
    public static final String CONNECTION_ADMIN_SERVICE = "ConnectionAdminService";


    public static void main(String[] args)
            throws IOException, AnalyzerAdminServiceAnalyzerException,
                   IndexAdminServiceConfigurationException, IndexAdminServiceIndexingException,
                   LoginAuthenticationExceptionException {

        String trustStore = System.getProperty("carbon.home") + "/repository/resources/security";
        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");


        String authenticationServiceURL = getProperty("bamUrl") + "AuthenticationAdmin";
        AuthenticationAdminStub authenticationAdminStub = new AuthenticationAdminStub(authenticationServiceURL);
        ServiceClient client = authenticationAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        authenticationAdminStub.login("admin", "admin", NetworkUtils.getLocalHostname());

        ServiceContext serviceContext = authenticationAdminStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);


        String analyzerAdminServiceURL = getProperty("bamUrl") + ANALYZER_ADMIN_SERVICE_URL;
        analyzerAdminServiceStub =  new AnalyzerAdminServiceStub(analyzerAdminServiceURL);

        ServiceClient analyzerAdminServiceStubClient = analyzerAdminServiceStub._getServiceClient();
        Options analyzerAdminServiceStubOption = analyzerAdminServiceStubClient.getOptions();
        analyzerAdminServiceStubOption.setManageSession(true);
        analyzerAdminServiceStubOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
        
        String indexAdminServiceURL =  getProperty("bamUrl") + INDEX_ADMIN_SERVICE_URL;
        indexAdminServiceStub = new IndexAdminServiceStub(indexAdminServiceURL);

        ServiceClient indexAdminServiceStubClient =  indexAdminServiceStub._getServiceClient();
        Options  indexAdminServiceStubOption = indexAdminServiceStubClient.getOptions();
        indexAdminServiceStubOption.setManageSession(true);
        indexAdminServiceStubOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);

        String ConnectionMgtServiceURL = getProperty("bamUrl") + CONNECTION_ADMIN_SERVICE;
        ConnectionAdminServiceStub connectionAdminServiceStub = new ConnectionAdminServiceStub(ConnectionMgtServiceURL);

        ServiceClient connectionAdminServiceStubClient =  connectionAdminServiceStub._getServiceClient();
        Options  connectionAdminServiceStubOption = connectionAdminServiceStubClient.getOptions();
        connectionAdminServiceStubOption.setManageSession(true);
        connectionAdminServiceStubOption.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);


        String classAnalyzerContent = FileUtil.readFileToString(System.getProperty("configFilePath") + "ClassAnalyzer.xml");
        String apiVersionSummarySequenceContent = FileUtil.readFileToString( System.getProperty("configFilePath") +
                                                                             "APIVersionSummarySequence.xml");


        try {

            connectionAdminServiceStub.configureConnectionParameters("admin","admin");

            System.out.println("Connection parameters have been added");

            APIMgtUsageBAMDataPublisher apiMgtUsageBAMDataPublisher = new APIMgtUsageBAMDataPublisher();

            apiMgtUsageBAMDataPublisher.publishEvent();

            System.out.println("Adding class analyzer");

            analyzerAdminServiceStub.addTask(classAnalyzerContent);

            System.out.println("Waiting 1:30 min until analyzer creates the BASEEnriched CF.....");

            //Wait for 1:30 min
            Thread.sleep(1000*60 + 30000);


            System.out.println("Adding API version summary analyzer");

            analyzerAdminServiceStub.addTask(apiVersionSummarySequenceContent);

            System.out.println("Waiting 1:30 min until analyzer creates the APIVersionSummary CF.......");

            //Wait for 1:30 min
            Thread.sleep(1000*60 + 30000);

            IndexDTO index = new IndexDTO();
            index.setIndexName("APIVersionSummaryIndex");
            index.setIndexedTable("APIVersionSummary");
            index.setDataSourceType("CASSANDRA");
            String[] indexColumns = {"api"};
            index.setIndexedColumns(indexColumns);
            index.setCron("1 * * * * ? *");
            index.setGranularity(null);

            System.out.println("Adding API Version Summary Index");

            indexAdminServiceStub.createIndex(index);

            System.out.println("BAM configured successfully for collecting API stats");

            return;

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (ConnectionAdminServiceConfigurationException e) {
            e.printStackTrace();
        }

    }

    private static String getProperty(String bamUrl) {
        String defaultVal = "https://localhost:9444/services/";
        String result = System.getProperty(bamUrl);
        if (result == null || result.length() == 0) {
            result = defaultVal;
        }
        return result;
    }
}
