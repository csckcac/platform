package org.wso2.bam.integration.tests.sample;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.bam.toolbox.deployer.stub.BAMToolboxDepolyerServiceStub;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import javax.sql.DataSource;
import java.net.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Random;

import static org.testng.Assert.assertTrue;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class KPISampleTestCase {
    private static final Log log = LogFactory.getLog(KPISampleTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    private static final String TOOLBOX_DEPLOYER_SERVICE = "/services/BAMToolboxDepolyerService";

    private BAMToolboxDepolyerServiceStub toolboxStub;

    private BasicDataSource dataSource;

    private Connection connection;

    boolean installed = false;
    private String deployedToolBox = "";

    @BeforeClass(groups = {"wso2.bam"})
    public void init() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(null);

        String loggedInSessionCookie = util.login();

        String EPR = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + TOOLBOX_DEPLOYER_SERVICE;
        toolboxStub = new BAMToolboxDepolyerServiceStub(configContext, EPR);
        ServiceClient client = toolboxStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }


    @Test(groups = {"wso2.bam"})
    public void kpiToolBoxDeployment() throws Exception {
        deployedToolBox = getToolBoxName();

        //Sample Id of KPI sample id - 1
        toolboxStub.deployBasicToolBox(1);

        log.info("Installing toolbox...");
        Thread.sleep(20000);
        //get List of deployed toolboxes
        BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO statusDTO = toolboxStub.getDeployedToolBoxes("1", "");
        String[] deployed = statusDTO.getDeployedTools();

        assertTrue(deployed != null, "Status of Toolbox is null");

        String toolBoxname = deployedToolBox.replaceAll(".bar", "");

        for (String aTool : deployed) {
            aTool = aTool.replaceAll(".bar", "");
            if (aTool.equalsIgnoreCase(toolBoxname)) {
                installed = true;
                break;
            }
        }
        assertTrue(installed, "Installation of toolbox :" + toolBoxname + " failed!!");
    }

    @Test(groups = {"wso2.bam"}, dependsOnMethods = "kpiToolBoxDeployment")
    public void runKPIAgent() throws AgentException, MalformedURLException, AuthenticationException, MalformedStreamDefinitionException, SocketException, StreamDefinitionException, TransportException, NoStreamDefinitionExistException, DifferentStreamDefinitionAlreadyDefinedException {
        KPIAgent.publish();
    }

    @Test(groups = {"wso2.bam"}, dependsOnMethods = "runKPIAgent")
    public void validateData() throws SQLException {
        try {
            log.info("Waiting to run the hive analysis");
            Thread.sleep(80000);
        } catch (InterruptedException e) {
        }
        log.info("Finished waiting....");

        dataSource = (BasicDataSource) initDataSource("org.h2.Driver",
                "jdbc:h2:repository/database/samples/WSO2CARBON_DB;AUTO_SERVER=TRUE",
                "wso2carbon",
                "wso2carbon");
        connection = dataSource.getConnection();

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery("SELECT * FROM brandSummary");
        assertTrue(result.next(), "No data in the summarized table Brand Summary");

        result = statement.executeQuery("SELECT * FROM UserSummary");
        assertTrue(result.next(), "No data in the summarized table Brand Summary");
    }

    private DataSource initDataSource(String driverName, String url, String username, String password) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }


    public void undeployDefaultToolbox() throws Exception {
        String toolBoxname = deployedToolBox.replaceAll(".bar", "");
        toolboxStub.undeployToolBox(new String[]{toolBoxname});

        Thread.sleep(15000);

        BAMToolboxDepolyerServiceStub.ToolBoxStatusDTO statusDTO = toolboxStub.getDeployedToolBoxes("1", "");
        String[] deployedTools = statusDTO.getDeployedTools();
        String[] undeployingTools = statusDTO.getToBeUndeployedTools();


        boolean unInstalled = true;
        if (null != undeployingTools) {
            for (String aTool : undeployingTools) {
                if (aTool.equalsIgnoreCase(toolBoxname)) {
                    unInstalled = false;
                    break;
                }
            }
        }

        if (null != deployedTools && unInstalled) {
            for (String aTool : deployedTools) {
                if (aTool.equalsIgnoreCase(toolBoxname)) {
                    unInstalled = false;
                    break;
                }
            }
        }

        assertTrue(unInstalled, "Un installing toolbox" + deployedToolBox + " is not successful");
    }

    @AfterClass(groups = {"wso2.bam"})
    public void cleanUp() throws Exception {
        if (installed) undeployDefaultToolbox();
        if (null != connection) {
            connection.close();
        }
    }

    private String getToolBoxName() throws Exception {
        BAMToolboxDepolyerServiceStub.BasicToolBox[] toolBoxes = toolboxStub.getBasicToolBoxes();
        if (null == toolBoxes || toolBoxes.length == 0) {
            throw new Exception("No default toolboxes available..");
        }

        return toolBoxes[0].getToolboxName();
    }


    private static class KPIAgent {
        private static final Log log = LogFactory.getLog(KPISampleTestCase.class);
        public static final String PHONE_RETAIL_STREAM = "org.wso2.bam.phone.retail.store.kpi";
        public static final String VERSION = "1.0.0";

        public static final String[] phoneModels = {"Nokia", "Apple", "Samsung", "Sony-Ericson", "LG"};
        public static final String[] users = {"James", "Mary", "John", "Peter", "Harry", "Tom", "Paul"};
        public static final int[] quantity = {2, 5, 3, 4, 1};
        public static final int[] price = {50000, 55000, 90000, 80000, 70000};


        public static void publish() throws AgentException,
                MalformedStreamDefinitionException,
                StreamDefinitionException,
                DifferentStreamDefinitionAlreadyDefinedException,
                MalformedURLException,
                AuthenticationException,
                NoStreamDefinitionExistException,
                TransportException, SocketException,
                org.wso2.carbon.databridge.commons.exception.AuthenticationException {
            System.out.println("Starting BAM Phone Reatil Shop KPI Agent");

            String host;

            AgentConfiguration agentConfiguration = new AgentConfiguration();
            String carbonHome = System.getProperty("carbon.home");
            System.setProperty("javax.net.ssl.trustStore", carbonHome + "/repository/resources/security/client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

            Agent agent = new Agent(agentConfiguration);

            host = getLocalHostAddress().getHostAddress();
            //create data publisher

            DataPublisher dataPublisher = new DataPublisher("tcp://" + host + ":7611", "admin", "admin", agent);
            String streamId = null;

            try {
                streamId = dataPublisher.findStream(PHONE_RETAIL_STREAM, VERSION);
                System.out.println("Stream already defined");

            } catch (NoStreamDefinitionExistException e) {
                streamId = dataPublisher.defineStream("{" +
                        "  'name':'" + PHONE_RETAIL_STREAM + "'," +
                        "  'version':'" + VERSION + "'," +
                        "  'nickName': 'Phone_Retail_Shop'," +
                        "  'description': 'Phone Sales'," +
                        "  'metaData':[" +
                        "          {'name':'clientType','type':'STRING'}" +
                        "  ]," +
                        "  'payloadData':[" +
                        "          {'name':'brand','type':'STRING'}," +
                        "          {'name':'quantity','type':'INT'}," +
                        "          {'name':'total','type':'INT'}," +
                        "          {'name':'user','type':'STRING'}" +
                        "  ]" +
                        "}");
//            //Define event stream
            }


            //Publish event for a valid stream
            if (!streamId.isEmpty()) {
                System.out.println("Stream ID: " + streamId);

                for (int i = 0; i < 100; i++) {
                    publishEvents(dataPublisher, streamId, i);
                    System.out.println("Events published : " + i);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }

                dataPublisher.stop();
            }
        }

        private static void publishEvents(DataPublisher dataPublisher, String streamId, int i) throws AgentException {
            int quantity = getRandomQuantity();
            Event eventOne = new Event(streamId, System.currentTimeMillis(), new Object[]{"external"}, null,
                    new Object[]{getRandomProduct(), quantity, quantity * getRandomPrice(), getRandomUser()});
            dataPublisher.publish(eventOne);
        }

        private static String getRandomProduct() {
            return phoneModels[getRandomId(5)];
        }

        private static String getRandomUser() {
            return users[getRandomId(7)];
        }

        private static int getRandomQuantity() {
            return quantity[getRandomId(5)];
        }

        private static int getRandomPrice() {
            return price[getRandomId(5)];
        }


        private static int getRandomId(int i) {
            Random randomGenerator = new Random();
            return randomGenerator.nextInt(i);
        }

        private static InetAddress getLocalHostAddress() throws SocketException {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                Enumeration<InetAddress> addresses = iface.getInetAddresses();

                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        return addr;
                    }
                }
            }

            return null;
        }
    }
}
