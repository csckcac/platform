/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.bam.integration.test.datacollection.mediation;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.aspects.ComponentType;
import org.bouncycastle.ocsp.OCSPReqGenerator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.test.datacollection.mediation.mockobjects.MockBAMMediationStatsPublisherAdmin;
import org.wso2.bam.integration.test.datacollection.mediation.mockobjects.MockLwEventBroker;
import org.wso2.bam.integration.test.datacollection.mediation.mockobjects.MockMediationStatisticsSnapshot;
import org.wso2.bam.integration.test.datacollection.mediation.mockobjects.MockStatistics;
import org.wso2.carbon.bam.data.publisher.mediationstats.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.mediationstats.ServiceHolder;
import org.wso2.carbon.bam.data.publisher.mediationstats.config.MediationStatConfig;
import org.wso2.carbon.bam.data.publisher.mediationstats.observer.BAMMediationStatisticsObserver;
import org.wso2.carbon.bam.services.stub.bamconfigurationds.types.Server;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceStub;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceBAMException;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceStub;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.statquery.BAMStatQueryDSStub;
import org.wso2.carbon.bam.stub.statquery.Endpoint;
import org.wso2.carbon.bam.stub.statquery.ProxyService;
import org.wso2.carbon.bam.stub.statquery.Sequence;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;

import java.rmi.RemoteException;

/**
 * This test case verifies whether the mediation events are received successfully by BAM.
 */
public class MediationEventReceiverTestCase {

    private static final Log log = LogFactory.getLog(MediationEventReceiverTestCase.class);

    private static final int SERVER_SUCCESSFULLY_ADDED = 0;
    private static final int SERVER_ALREADY_EXIST = 1;

    private static final String TEST_ENDPOINT_NAME = "TestEndpoint";
    private static final String TEST_PROXY_NAME = "TestProxy";
    private static final String TEST_SEQUENCE_NAME = "TestSequence";

    private static final String ENDPOINT = "Endpoint";
    private static final String PROXY = "Proxy";
    private static final String SEQUENCE = "Sequence";

    private static final String AVG_PROCESSING_TIME = "AvgProcessingTime";
    private static final String MIN_PROCESSING_TIME = "MinProcessingTime";
    private static final String MAX_PROCESSING_TIME = "MaxProcessingTime";
    private static final String CUMULATIVE_COUNT = "CumulativeCount";
    private static final String FAULT_COUNT = "FaultCount";

    private static final String LOCALHOST = "https://localhost:9443";

    private static final String CONFIG_ADMIN_SERVICE_URL = "https://localhost:9443/services/BAMConfigAdminService";
    private static final String LIST_ADMIN_SERVICE_URL = "https://localhost:9443/services/BAMListAdminService";
    private static final String STAT_QUERY_DS_SERVICE_URL = "https://localhost:9443/services/BAMStatQueryDS";

    private LoginLogoutUtil util = new LoginLogoutUtil();

    private static enum Direction {
        IN("In"), OUT("Out");

        private final String direction;

        Direction(String direction) {
            this.direction = direction;
        }

        public String getDirection() {
            return this.direction;
        }
    }

    private BAMConfigAdminServiceStub configAdminStub;
    private BAMListAdminServiceStub listAdminStub;
    private BAMStatQueryDSStub statQueryStub;

    private ServerDO server;


    @BeforeClass(groups = {"wso2.bam"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();
        initStubs(sessionCookie);
    }


    private void init() throws Exception {
        server = new ServerDO();
        server.setServerURL(LOCALHOST);
        server.setUserName("admin");
        server.setPassword("admin");
        server.setCategory(4);
        server.setServerType("EventingServer");
        server.setSubscriptionEPR("");
        server.setDescription("");
        server.setSubscriptionID(null);
        addEventingServer(server);
    }

    @Test(groups = {"wso2.bam.test"}, description = "Test for mediation event reception")
    public void recieveMediationEvent() throws Exception {
        this.init();
        runEndpointTestScenario(server);
        runProxyTestScenario(server);
        runSequenceTestScenario(server);
        cleanup();
    }


    private void cleanup() throws AxisFault {
        configAdminStub._getServiceClient().cleanupTransport();
        configAdminStub._getServiceClient().cleanup();
        configAdminStub.cleanup();

        listAdminStub._getServiceClient().cleanupTransport();
        listAdminStub._getServiceClient().cleanup();
        listAdminStub.cleanup();

        statQueryStub._getServiceClient().cleanupTransport();
        statQueryStub._getServiceClient().cleanup();
        statQueryStub.cleanup();
    }

    @AfterClass(groups = {"wso2.bam"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }


    /**
     * ********************* Test scenarios ***********************
     */

    private void runEndpointTestScenario(ServerDO server) throws BAMListAdminServiceBAMException, RemoteException {

        MockStatistics statistics = new MockStatistics(TEST_ENDPOINT_NAME);
        statistics.setAvgProcessingTime(15.3);
        statistics.setMaxProcessingTime(40);
        statistics.setMinProcessingTime(10);
        statistics.setCount(32);
        statistics.setFaultCount(4);

        publishEvent(LOCALHOST, ComponentType.ENDPOINT, Direction.IN, statistics);

        log.info("Successfully updated mediation statistics for " + TEST_ENDPOINT_NAME);

        verifyEndpointData(server, Direction.IN, statistics);

        log.info("Successfully verified mediation statistics for " + TEST_ENDPOINT_NAME);

    }

    private void runSequenceTestScenario(ServerDO server) throws BAMListAdminServiceBAMException, RemoteException {

        MockStatistics statistics = new MockStatistics(TEST_SEQUENCE_NAME);
        statistics.setAvgProcessingTime(30.34);
        statistics.setMaxProcessingTime(50);
        statistics.setMinProcessingTime(20);
        statistics.setCount(35);
        statistics.setFaultCount(2);

        publishEvent(LOCALHOST, ComponentType.SEQUENCE, Direction.IN, statistics);
        publishEvent(LOCALHOST, ComponentType.SEQUENCE, Direction.OUT, statistics);

        log.info("Successfully updated mediation statistics for" + TEST_SEQUENCE_NAME);

        verifySequenceData(server, Direction.IN, statistics);
        verifySequenceData(server, Direction.OUT, statistics);

        log.info("Successfully verified mediation statistics for " + TEST_SEQUENCE_NAME);

    }

    private void runProxyTestScenario(ServerDO server) throws BAMListAdminServiceBAMException, RemoteException {

        MockStatistics statistics = new MockStatistics(TEST_PROXY_NAME);
        statistics.setAvgProcessingTime(22432432233233423.03423423432);
        statistics.setMaxProcessingTime(3333333242353455555L);
        statistics.setMinProcessingTime(20);
        statistics.setCount(324223424);
        statistics.setFaultCount(0);

        publishEvent(LOCALHOST, ComponentType.PROXYSERVICE, Direction.IN, statistics);
        publishEvent(LOCALHOST, ComponentType.PROXYSERVICE, Direction.OUT, statistics);

        log.info("Successfully updated mediation statistics for" + TEST_PROXY_NAME);

        verifyProxyData(server, Direction.IN, statistics);
        verifyProxyData(server, Direction.OUT, statistics);

        log.info("Successfully verified mediation statistics for " + TEST_PROXY_NAME);

    }

    /**
     * ******************** Private helper methods *************************
     */

    private void initStubs(String sessionCookie) throws AxisFault {

        configAdminStub = new BAMConfigAdminServiceStub(CONFIG_ADMIN_SERVICE_URL);
        listAdminStub = new BAMListAdminServiceStub(LIST_ADMIN_SERVICE_URL);
        statQueryStub = new BAMStatQueryDSStub(STAT_QUERY_DS_SERVICE_URL);

        setSessionCookie(configAdminStub, sessionCookie);
        setSessionCookie(listAdminStub, sessionCookie);
        setSessionCookie(statQueryStub, sessionCookie);
    }

    private void setSessionCookie(Stub stub, String sessionCookie) {
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
    }


    private boolean isEqualServers(MonitoredServerDTO first, ServerDO second) {
        if (first.getServerURL().equals(second.getServerURL())) {
            if (first.getServerType() != null && second.getServerType() != null) {
                if (first.getServerType().equals(second.getServerType())) {
                    return (first.getCategory() == second.getCategory());
                }
            } else {
                return (first.getCategory() == second.getCategory());
            }
        }

        return false;
    }

    private MockStatistics getEndpointDataFromDB(MonitoredServerDTO server, Direction directionType,
                                                 Endpoint endpoint) throws RemoteException {

        MockStatistics statistics = new MockStatistics(endpoint.getEndpoint());
        // Only Direction.IN data is present for endpoints
        String mediationString = createMediationKeyString(
                ENDPOINT, directionType.getDirection(), AVG_PROCESSING_TIME, endpoint.getEndpoint());
        String avgTimeStr = statQueryStub.getLatestInAverageProcessingTimeForEndpoint(
                server.getServerId(), mediationString)[0].getAverageTime();

        mediationString = createMediationKeyString(
                ENDPOINT, directionType.getDirection(), MIN_PROCESSING_TIME, endpoint.getEndpoint());
        String minTimeStr = statQueryStub.getLatestInMinimumProcessingTimeForEndpoint(
                server.getServerId(), mediationString)[0].getMinimumTime();

        mediationString = createMediationKeyString(
                ENDPOINT, directionType.getDirection(), MAX_PROCESSING_TIME, endpoint.getEndpoint());
        String maxTimeStr = statQueryStub.getLatestInMaximumProcessingTimeForEndpoint(
                server.getServerId(), mediationString)[0].getMaximumTime();

        mediationString = createMediationKeyString(
                ENDPOINT, directionType.getDirection(), CUMULATIVE_COUNT, endpoint.getEndpoint());
        String cumulativeCountStr = statQueryStub.getInCumulativeCountsForEndpoint(
                server.getServerId(), mediationString)[0].getCumulativeCount();

        mediationString = createMediationKeyString(
                ENDPOINT, directionType.getDirection(), FAULT_COUNT, endpoint.getEndpoint());
        String faultCountStr = statQueryStub.getLatestInFaultCountForEndpoint(
                server.getServerId(), mediationString)[0].getFaultCount();

        statistics.setAvgProcessingTime(Double.parseDouble(avgTimeStr));
        statistics.setMinProcessingTime(Long.parseLong(minTimeStr));
        statistics.setMaxProcessingTime(Long.parseLong(maxTimeStr));
        statistics.setCount(Integer.parseInt(cumulativeCountStr));
        statistics.setFaultCount(Integer.parseInt(faultCountStr));

        return statistics;
    }

    private MockStatistics getProxyDataFromDB(MonitoredServerDTO server, Direction directionType,
                                              ProxyService proxy) throws RemoteException {

        MockStatistics statistics = new MockStatistics(proxy.getProxyService());

        String mediationString = createMediationKeyString(
                PROXY, directionType.getDirection(), AVG_PROCESSING_TIME, proxy.getProxyService());
        String avgTimeStr;
        if (directionType == Direction.IN) {
            avgTimeStr = statQueryStub.getLatestInAverageProcessingTimeForProxy(
                    server.getServerId(), mediationString)[0].getAverageTime();
        } else {
            avgTimeStr = statQueryStub.getLatestOutAverageProcessingTimeForProxy(
                    server.getServerId(), mediationString)[0].getAverageTime();
        }

        mediationString = createMediationKeyString(
                PROXY, directionType.getDirection(), MIN_PROCESSING_TIME, proxy.getProxyService());
        String minTimeStr;
        if (directionType == Direction.IN) {
            minTimeStr = statQueryStub.getLatestInMinimumProcessingTimeForProxy(
                    server.getServerId(), mediationString)[0].getMinimumTime();
        } else {
            minTimeStr = statQueryStub.getLatestOutMinimumProcessingTimeForProxy(
                    server.getServerId(), mediationString)[0].getMinimumTime();
        }

        mediationString = createMediationKeyString(
                PROXY, directionType.getDirection(), MAX_PROCESSING_TIME, proxy.getProxyService());
        String maxTimeStr;
        if (directionType == Direction.IN) {
            maxTimeStr = statQueryStub.getLatestInMaximumProcessingTimeForProxy(
                    server.getServerId(), mediationString)[0].getMaximumTime();
        } else {
            maxTimeStr = statQueryStub.getLatestOutMaximumProcessingTimeForProxy(
                    server.getServerId(), mediationString)[0].getMaximumTime();
        }

        mediationString = createMediationKeyString(
                PROXY, directionType.getDirection(), CUMULATIVE_COUNT, proxy.getProxyService());
        String cumulativeCountStr;
        if (directionType == Direction.IN) {
            cumulativeCountStr = statQueryStub.getLatestInCumulativeCountForProxy(
                    server.getServerId(), mediationString)[0].getCumulativeCount();
        } else {
            cumulativeCountStr = statQueryStub.getLatestOutCumulativeCountForProxy(
                    server.getServerId(), mediationString)[0].getCumulativeCount();
        }

        mediationString = createMediationKeyString(
                PROXY, directionType.getDirection(), FAULT_COUNT, proxy.getProxyService());
        String faultCountStr;
        if (directionType == Direction.IN) {
            faultCountStr = statQueryStub.getLatestInFaultCountForProxy(
                    server.getServerId(), mediationString)[0].getFaultCount();
        } else {
            faultCountStr = statQueryStub.getLatestOutFaultCountForProxy(
                    server.getServerId(), mediationString)[0].getFaultCount();
        }

        statistics.setAvgProcessingTime(Double.parseDouble(avgTimeStr));
        statistics.setMinProcessingTime(Long.parseLong(minTimeStr));
        statistics.setMaxProcessingTime(Long.parseLong(maxTimeStr));
        statistics.setCount(Integer.parseInt(cumulativeCountStr));
        statistics.setFaultCount(Integer.parseInt(faultCountStr));

        return statistics;
    }

    private MockStatistics getSequenceDataFromDB(MonitoredServerDTO server, Direction directionType,
                                                 Sequence sequence) throws RemoteException {

        MockStatistics statistics = new MockStatistics(sequence.getSequence());

        String mediationString = createMediationKeyString(
                SEQUENCE, directionType.getDirection(), AVG_PROCESSING_TIME, sequence.getSequence());
        String avgTimeStr;
        if (directionType == Direction.IN) {
            avgTimeStr = statQueryStub.getLatestInAverageProcessingTimeForSequence(
                    server.getServerId(), mediationString)[0].getAverageTime();
        } else {
            avgTimeStr = statQueryStub.getLatestOutAverageProcessingTimeForSequence(
                    server.getServerId(), mediationString)[0].getAverageTime();
        }

        mediationString = createMediationKeyString(
                SEQUENCE, directionType.getDirection(), MIN_PROCESSING_TIME, sequence.getSequence());
        String minTimeStr;
        if (directionType == Direction.IN) {
            minTimeStr = statQueryStub.getLatestInMinimumProcessingTimeForSequence(
                    server.getServerId(), mediationString)[0].getMinimumTime();
        } else {
            minTimeStr = statQueryStub.getLatestOutMinimumProcessingTimeForSequence(
                    server.getServerId(), mediationString)[0].getMinimumTime();
        }

        mediationString = createMediationKeyString(
                SEQUENCE, directionType.getDirection(), MAX_PROCESSING_TIME, sequence.getSequence());
        String maxTimeStr;
        if (directionType == Direction.IN) {
            maxTimeStr = statQueryStub.getLatestInMaximumProcessingTimeForSequence(
                    server.getServerId(), mediationString)[0].getMaximumTime();
        } else {
            maxTimeStr = statQueryStub.getLatestOutMaximumProcessingTimeForSequence(
                    server.getServerId(), mediationString)[0].getMaximumTime();
        }

        mediationString = createMediationKeyString(
                SEQUENCE, directionType.getDirection(), CUMULATIVE_COUNT, sequence.getSequence());
        String cumulativeCountStr;
        if (directionType == Direction.IN) {
            cumulativeCountStr = statQueryStub.getLatestInCumulativeCountForSequence(
                    server.getServerId(), mediationString)[0].getCumulativeCount();
        } else {
            cumulativeCountStr = statQueryStub.getLatestOutCumulativeCountForSequence(
                    server.getServerId(), mediationString)[0].getCumulativeCount();
        }

        mediationString = createMediationKeyString(
                SEQUENCE, directionType.getDirection(), FAULT_COUNT, sequence.getSequence());
        String faultCountStr;
        if (directionType == Direction.IN) {
            faultCountStr = statQueryStub.getLatestInFaultCountForSequence(
                    server.getServerId(), mediationString)[0].getFaultCount();
        } else {
            faultCountStr = statQueryStub.getLatestOutFaultCountForSequence(
                    server.getServerId(), mediationString)[0].getFaultCount();
        }

        statistics.setAvgProcessingTime(Double.parseDouble(avgTimeStr));
        statistics.setMinProcessingTime(Long.parseLong(minTimeStr));
        statistics.setMaxProcessingTime(Long.parseLong(maxTimeStr));
        statistics.setCount(Integer.parseInt(cumulativeCountStr));
        statistics.setFaultCount(Integer.parseInt(faultCountStr));

        return statistics;
    }

    private String createMediationKeyString(String mediationType, String direction, String dataType,
                                            String name) {
        return mediationType + direction + dataType + "-" + name;
    }

    private void addEventingServer(ServerDO addedServer) throws Exception {
        int stateOfAddedServer;

        stateOfAddedServer = configAdminStub.addServer(addedServer);


        if (stateOfAddedServer == SERVER_SUCCESSFULLY_ADDED) {
            log.info("Added " + addedServer.getServerType() + " with URL " + addedServer.getServerURL());
        } else if (stateOfAddedServer == SERVER_ALREADY_EXIST) {
            log.info("Skip adding the server. Server already exists..");
        } else {
            log.error("Unknown return code while adding server..");
        }
    }

    private void publishEvent(String server, ComponentType type, Direction direction,
                              MockStatistics statistics) throws AxisFault {
        MediationStatConfig config = new MediationStatConfig();
        config.setEnableEventing("ON");
        config.setEndpointRequestCountThreshold(5);
        config.setProxyRequestCountThreshold(5);
        config.setSequenceRequestCountThreshold(5);

        MockBAMMediationStatsPublisherAdmin admin = new MockBAMMediationStatsPublisherAdmin();
        admin.configureEventing(config);

        MockLwEventBroker broker = new MockLwEventBroker();
        PublisherUtils.setServerName(server);
        PublisherUtils.setMediationStatPublisherAdmin(admin);
        PublisherUtils.setEventBroker(broker);

        ServiceHolder.setLWEventBroker(broker);

        StatisticsRecord update = null;
        if (direction == Direction.IN) {
            update = new StatisticsRecord(statistics.getId(), type, true, statistics);
        } else if (direction == Direction.OUT) {
            update = new StatisticsRecord(statistics.getId(), type, false, statistics);
        }

        MockMediationStatisticsSnapshot snapshot = new MockMediationStatisticsSnapshot();
        snapshot.setUpdate(update);

        BAMMediationStatisticsObserver observer = new BAMMediationStatisticsObserver();
        observer.updateStatistics(snapshot);

    }

    private void verifyEndpointData(ServerDO addedServer, Direction direction,
                                    MockStatistics statistics) throws BAMListAdminServiceBAMException, RemoteException {

        MonitoredServerDTO[] servers = null;
        servers = listAdminStub.getServerList();

        Assert.assertTrue((servers != null), "Added server non existent in database..");

        boolean serverFound = false;
        for (MonitoredServerDTO server : servers) {
            if (isEqualServers(server, addedServer)) {
                serverFound = true;
                Endpoint[] endpoints = null;
                endpoints = statQueryStub.getEndpoints(server.getServerId());

                Assert.assertTrue((endpoints != null), "Endpoint data non existent in database for server: " +
                        server.getServerURL());

                boolean endpointFound = false;
                for (Endpoint endpoint : endpoints) {
                    MockStatistics dbStatistics = getEndpointDataFromDB(server, direction, endpoint);
                    if (TEST_ENDPOINT_NAME.equals(endpoint.getEndpoint())) {
                        endpointFound = true;
                        Assert.assertTrue(statistics.equals(dbStatistics), "Endpoint statistics does not match");
                        break;
                    }
                }

                if (!endpointFound) {
                    Assert.assertFalse(true, "Endpoint data non existent in database for server: "
                            + server.getServerURL());
                }
            }

            if (!serverFound) {
                Assert.assertFalse(true, "Added service not present in the database..");
            }
        }
    }

    private void verifyProxyData(ServerDO addedServer, Direction direction,
                                 MockStatistics statistics) throws BAMListAdminServiceBAMException, RemoteException {

        MonitoredServerDTO[] servers = null;
        servers = listAdminStub.getServerList();

        Assert.assertTrue((servers != null), "Added server non existent in database..");

        boolean serverFound = false;
        for (MonitoredServerDTO server : servers) {
            if (isEqualServers(server, addedServer)) {
                serverFound = true;
                ProxyService[] proxies = null;
                proxies = statQueryStub.getProxyServices(server.getServerId());

                Assert.assertTrue((proxies != null), "Proxy data non existent in database for server: "
                        + server.getServerURL());

                boolean proxyFound = false;
                for (ProxyService proxy : proxies) {
                    MockStatistics dbStatistics = getProxyDataFromDB(server, direction, proxy);
                    if (TEST_PROXY_NAME.equals(proxy.getProxyService())) {
                        proxyFound = true;
                        Assert.assertTrue(statistics.equals(dbStatistics), "Proxy statistics does not match");
                        break;
                    }
                }

                if (!proxyFound) {
                    Assert.assertFalse(true, "Proxy data non existent in database for server: "
                            + server.getServerURL());
                }
            }

            if (!serverFound) {
                Assert.assertFalse(true, "Added service not present in the database..");
            }
        }
    }

    private void verifySequenceData(ServerDO addedServer, Direction direction,
                                    MockStatistics statistics) throws BAMListAdminServiceBAMException, RemoteException {

        MonitoredServerDTO[] servers = null;
        servers = listAdminStub.getServerList();

        Assert.assertTrue((servers != null), "Added server non existent in database..");

        boolean serverFound = false;
        for (MonitoredServerDTO server : servers) {
            if (isEqualServers(server, addedServer)) {
                serverFound = true;
                Sequence[] sequences = null;
                sequences = statQueryStub.getSequences(server.getServerId());

                Assert.assertTrue((sequences != null), "Sequence data non existent in database for server: "
                        + server.getServerURL());

                boolean sequenceFound = false;
                for (Sequence sequence : sequences) {
                    MockStatistics dbStatistics = getSequenceDataFromDB(server, direction, sequence);
                    if (TEST_SEQUENCE_NAME.equals(sequence.getSequence())) {
                        sequenceFound = true;
                        Assert.assertEquals(statistics, dbStatistics, "Sequence statistics does not match");
                        break;
                    }
                }

                Assert.assertTrue(sequenceFound, "Sequence data non existent in database for server: " +
                        server.getServerURL());
            }
            Assert.assertTrue(serverFound, "Added service not present in the database..");
        }
    }

}
