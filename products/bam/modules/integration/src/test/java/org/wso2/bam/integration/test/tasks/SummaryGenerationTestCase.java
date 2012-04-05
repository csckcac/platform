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
package org.wso2.bam.integration.test.tasks;

import org.apache.axis2.AxisFault;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.h2.store.Data;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.test.BAMTestServerManager;
import org.wso2.bam.integration.test.common.events.EventException;
import org.wso2.bam.integration.test.common.events.EventUtils;
import org.wso2.bam.integration.test.common.publisher.DataPublisher;
import org.wso2.bam.integration.test.common.publisher.JDBCPublisher;
import org.wso2.bam.integration.test.common.publisher.QueryTemplate;
import org.wso2.bam.integration.test.common.publisher.ServiceEvent;
import org.wso2.bam.integration.test.common.statistics.CumulativeStatisticsData;
import org.wso2.bam.integration.test.common.statistics.ServiceEventData;
import org.wso2.bam.integration.test.common.statistics.StatisticsData;
import org.wso2.bam.integration.test.common.utils.CommonUtils;
import org.wso2.carbon.bam.services.stub.bamsummarygenerationds.BAMSummaryGenerationDSStub;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceBAMException;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceStub;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceBAMException;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceStub;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.OperationDO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ServerDO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO;
import org.wso2.carbon.bam.stub.summaryquery.BAMSummaryQueryDSStub;
import org.wso2.carbon.bam.stub.summaryquery.SummaryStat;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.bam.core.internal.BAMServiceComponent;

import javax.sql.DataSource;
import javax.wsdl.Operation;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SummaryGenerationTestCase {

    /**
     * This test case verifies whether the summary is generated successfully
     */
    private static final Log log = LogFactory.getLog(SummaryGenerationTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();
    BAMConfigAdminServiceStub configAdminStub;
    BAMListAdminServiceStub listAdminStub;
    BAMSummaryQueryDSStub summaryQueryStub;
    BasicDataSource datasource;
    Connection connection;
    DataPublisher publisher;
    private static final String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @BeforeClass(groups = {"wso2.bam"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();
        initStubs(sessionCookie);
    }


    private void init() throws IOException, SQLException {
        datasource = initDataSource();
        connection = datasource.getConnection();
    }

    @Test(groups = {"wso2.bam.summary"}, description = "Testing summary generation", dependsOnGroups = {"wso2.bam.restart"}, enabled = true)
    public void generateSummary() throws SQLException, EventException, IOException, XMLStreamException, BAMConfigAdminServiceBAMException, BAMListAdminServiceBAMException {
        init();
        runServiceStatSummaryGenerationScenario();
    }

    @AfterClass(groups = {"wso2.bam"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }


    private void initStubs(String sessionCookie) throws AxisFault {
        configAdminStub = new BAMConfigAdminServiceStub("https://localhost:9443/services/BAMConfigAdminService");
        listAdminStub = new BAMListAdminServiceStub("https://localhost:9443/services/BAMListAdminService");
        summaryQueryStub = new BAMSummaryQueryDSStub("https://localhost:9443/services/BAMSummaryQueryDS");

        CommonUtils.setSessionCookie(configAdminStub, sessionCookie);
        CommonUtils.setSessionCookie(listAdminStub, sessionCookie);
        CommonUtils.setSessionCookie(summaryQueryStub, sessionCookie);
    }

    private void runServiceStatSummaryGenerationScenario() throws EventException, SQLException, XMLStreamException, IOException, BAMConfigAdminServiceBAMException, BAMListAdminServiceBAMException {
        ServiceEventData systemEventData = publishServiceStatEvents("ServiceStatSystemEvent.xml");
        ServiceEventData serviceEventData = publishServiceStatEvents("ServiceStatServiceEvent.xml");
        ServiceEventData operationEventData = publishServiceStatEvents("ServiceStatOperationEvent.xml");

        connection.close();
        ((JDBCPublisher) publisher).closeConnection();
        datasource.close();
        try {
            Thread.sleep(100 * 1000);       // Wait on a known field and timeout
        } catch (InterruptedException ignored) {

        }
        verifySummaryGeneration();
    }

    private ServiceEventData publishServiceStatEvents(String baseFile) throws EventException, XMLStreamException {
        publisher = new JDBCPublisher(connection);
        ServiceEventData eventData;
        eventData = EventUtils.generateServiceStatEvents(baseFile, 1);

        publisher.beginBatch();

        for (StatisticsData event : eventData.getEvents()) {
            publisher.publishServiceEvent(event, DataPublisher.BackDate.HOUR);
        }
        publisher.endBatch();
        return eventData;

    }

    private void verifySummaryGeneration() throws RemoteException, BAMConfigAdminServiceBAMException, BAMListAdminServiceBAMException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, -4);

        DateFormat dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.ENGLISH);
        String startTime = dateFormat.format(cal.getTime());

        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 1);

        dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.ENGLISH);
        String endTime = dateFormat.format(cal.getTime());

        MonitoredServerDTO[] serverDTOs = listAdminStub.getServerList();
        Assert.assertTrue(serverDTOs != null, "Server is not in the BAM Database..");

        ServiceDO[] service = listAdminStub.getServiceList(serverDTOs[0].getServerId());
        Assert.assertTrue(service != null, "Service is not in the BAM Database..");

        OperationDO[] operation = listAdminStub.getOperationList(serverDTOs[0].getServerId());
        Assert.assertTrue(operation != null, "Operation is not in the BAM Database..");

        SummaryStat[] serviceStats = summaryQueryStub.getServiceStatHourlySummaries(serverDTOs[0].getServerId(), startTime, endTime);
        SummaryStat[] operationStats = summaryQueryStub.getOperationStatHourlySummaries(serverDTOs[0].getServerId(), startTime, endTime);
        SummaryStat[] serverStats = summaryQueryStub.getServerStatHourlySummaries(serverDTOs[0].getServerId(), startTime, endTime);

        boolean success = false;
        if (serviceStats != null & operationStats != null & serverStats != null) {
            success = true;
        }
        Assert.assertTrue(success, "Summary generation is not success..");
    }


    private BasicDataSource initDataSource() throws IOException {

        Properties props = new Properties();

        props.load(JDBCPublisher.class.getResourceAsStream("/" + "datasource.properties"));
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(props.getProperty("driver"));
        dataSource.setUrl(props.getProperty("url"));
        dataSource.setUsername(props.getProperty("username"));
        dataSource.setPassword(props.getProperty("password"));
        String validationQuery = props.getProperty("validationQuery");
        if (validationQuery != null) {
            dataSource.setValidationQuery(validationQuery);
        }

        String maxActive = props.getProperty("maxActive");
        if (maxActive != null) {
            dataSource.setMaxActive(Integer.parseInt(maxActive));
        }

        String initialSize = props.getProperty("initialSize");
        if (initialSize != null) {
            dataSource.setInitialSize(Integer.parseInt(initialSize));
        }

        String maxIdle = props.getProperty("maxIdle");
        if (maxIdle != null) {
            dataSource.setMaxIdle(Integer.parseInt(maxIdle));
        }

        log.info("Created new data source to: " + dataSource.getUrl());
        return dataSource;
    }

}