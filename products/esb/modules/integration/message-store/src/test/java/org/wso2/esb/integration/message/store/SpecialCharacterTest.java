/**
 *
 */
package org.wso2.esb.integration.message.store;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.http.RequestInterceptor;
import org.wso2.esb.integration.http.SimpleHttpClient;
import org.wso2.esb.integration.message.store.controller.JMSBrokerController;
import org.wso2.esb.integration.message.store.controller.config.JMSBrokerConfiguration;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.testng.Assert.assertTrue;

/**
 * @author wso2
 */
public class SpecialCharacterTest extends ESBIntegrationTestCase {


    private TestRequestInterceptor interceptor;
    private JMSBrokerController jmsBrokerController;

    @Override
    protected void init() throws Exception {
        this.interceptor = new TestRequestInterceptor();
        launchBackendHttpServer(interceptor);
        setUpJMSBroker();
        copyArtifacts();
    }

    @Test(groups = {"wso2.esb"})
    public void testSpecialCharacterMediation() throws Exception {
        testGracefulServerRestart();
        String filePath = System.getProperty("resource.dir") + File.separator + "special_character.xml";
        File configFile = new File(filePath);

        FileInputStream inputStream = new FileInputStream(configFile.getAbsolutePath());
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement documentElement = builder.getDocumentElement();
        updateESBConfiguration(documentElement);
        SimpleHttpClient httpClient = new SimpleHttpClient();
        String payload = "<test>This payload is üsed to check special character mediation</test>";
        try {

            HttpResponse response = httpClient.doPost(getProxyServiceURL("InOutProxy", false), null, payload, "application/xml");
        } catch (AxisFault e) {
            log.error("Response not expected here, Exception can be accepted ");
        }
        Thread.sleep(10000);
        assertTrue(interceptor.getPayload().contains(payload));
    }

    private void setUpJMSBroker() {
        jmsBrokerController = new JMSBrokerController("localhost", getJMSBrokerConfiguration());
        jmsBrokerController.start();
    }


    private JMSBrokerConfiguration getJMSBrokerConfiguration() {
        JMSBrokerConfiguration jmsBrokerConfiguration = new JMSBrokerConfiguration();
        jmsBrokerConfiguration.setInitialNamingFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        jmsBrokerConfiguration.setProviderURL("tcp://localhost:61616");
        return jmsBrokerConfiguration;
    }

    protected void copyArtifacts() throws IOException {
        copyFile("activemq-core.jar");
        copyFile("geronimo-j2ee-management_1.1_spec.jar");
        copyFile("geronimo-jms_1.1_spec.jar");
    }

    private void copyFile(String fileName) {
        copySampleFile(
                computeSourcePath(fileName),
                computeDestPath(fileName));
    }

    private void copySampleFile(String sourcePath, String destPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);

        try {
            FileManipulator.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            log.error("Error while copying the HelloWorld sample into AppServer", e);
        }
    }

    private String computeSourcePath(String fileName) {
        String samplesDir = System.getProperty("activeMQ.jar.location");
        return samplesDir + File.separator + fileName;

    }

    private String computeDestPath(String fileName) {
        String deploymentPathDir = System.getProperty("carbon.home");
        String deploymentPath = deploymentPathDir + File.separator + "repository" + File.separator
                + "components" + File.separator + "lib";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            log.error("Error while creating the deployment folder : " + deploymentPath);
        }

        return deploymentPath + File.separator + fileName;
    }


    private void testGracefulServerRestart() throws Exception {
        log.info("Testing server graceful restart...");
        int portOffset = 0;

        int httpsPort = 9443 + portOffset;
        ClientConnectionUtil.waitForPort(httpsPort);
        ServerAdminClient serverAdmin = LoginLogoutUtil.getServerAdminClient(portOffset);
        serverAdmin.restartGracefully();
        Thread.sleep(5000); //This sleep should be there, since we have to give some time for
        //the server to initiate restart. Otherwise, "waitForPort" call
        //might happen before server initiate restart.
        ClientConnectionUtil.waitForPort(httpsPort, 60000, true);

        Thread.sleep(5000);

    }

    private static class TestRequestInterceptor implements RequestInterceptor {

        private String payload;

        public void requestReceived(HttpRequest request) {
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                try {
                    InputStream in = entity.getContent();
                    String inputString = IOUtils.toString(in, "UTF-8");
                    payload = inputString;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        public String getPayload() {
            return payload;
        }
    }
}