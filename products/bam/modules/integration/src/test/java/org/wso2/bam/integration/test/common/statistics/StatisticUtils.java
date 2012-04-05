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

package org.wso2.bam.integration.test.common.statistics;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.bam.integration.test.common.events.EventException;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceBAMException;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceStub;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.OperationDO;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.ServiceDO;
import org.wso2.carbon.bam.stub.statquery.BAMStatQueryDSStub;
import org.wso2.carbon.bam.stub.statquery.Data;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

public class StatisticUtils {

    private static final Log log = LogFactory.getLog(StatisticUtils.class);

    private static final String STATISTICS_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/service/statistics/data";

    private static final String STATISTICS_DATA_ELEMENT_NAME_TENANT_ID = "TenantID";

    // / OM element names
    private static final String STATISTICS_DATA_ELEMENT_NAME_EVENT = "Event";
    private static final String STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA = "ServiceStatisticsData";
    private static final String STATISTICS_DATA_ELEMENT_NAME_SERVER_NAME = "ServerName";
    private static final String STATISTICS_DATA_ELEMENT_NAME_AVG_RESPONSE_TIME = "AverageResponseTime";
    private static final String STATISTICS_DATA_ELEMENT_NAME_MIN_RESPONSE_TIME = "MinimumResponseTime";
    private static final String STATISTICS_DATA_ELEMENT_NAME_MAX_RESPONSE_TIME = "MaximumResponseTime";
    private static final String STATISTICS_DATA_ELEMENT_NAME_REQUEST_COUNT = "RequestCount";
    private static final String STATISTICS_DATA_ELEMENT_NAME_RESPONSE_COUNT = "ResponseCount";
    private static final String STATISTICS_DATA_ELEMENT_NAME_FAULT_COUNT = "FaultCount";
    private static final String STATISTICS_DATA_ELEMENT_NAME_SERVICE_NAME = "ServiceName";
    private static final String STATISTICS_DATA_ELEMENT_NAME_OPERATION_NAME = "OperationName";

    public static StatisticsData getStatisticsData(OMElement eventData)
            throws EventException {

        // Event attributes
        String serverName;
        String serviceName;
        String operationName;

        int tenantID;
        long minResTime;
        double avgResTime;
        long maxResTime;
        int requestCount;
        int responseCount;
        int faultCount;

        StatisticsData data = new StatisticsData();

        if (!eventData.getQName().equals(new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_EVENT))) {
            throw new EventException("Event element not found in the message");
        }

        QName serviceStatisticsDataQName =
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_SERVICE_STATISTICS_DATA);
        OMElement serviceStatisticsDataElement = eventData.getFirstChildWithName(serviceStatisticsDataQName);

        if (serviceStatisticsDataElement == null) {
            throw new EventException("ServiceStatisticsData element not found in the message");
        }

        tenantID = getIntDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_TENANT_ID));

        if (tenantID == -1) {
            tenantID = CarbonConstants.SUPER_TENANT_ID;
        }

        data.setTenantID(tenantID);

        serverName = getStringDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_SERVER_NAME));

        if (serverName == null) {
            throw new EventException("ServerName element not found in the message");
        }

        avgResTime = getDoubleDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_AVG_RESPONSE_TIME));
        minResTime = getLongDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_MIN_RESPONSE_TIME));
        maxResTime = getLongDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_MAX_RESPONSE_TIME));
        requestCount = getIntDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_REQUEST_COUNT));
        responseCount = getIntDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_RESPONSE_COUNT));
        faultCount = getIntDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_FAULT_COUNT));
        serviceName = getStringDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_SERVICE_NAME));
        operationName = getStringDataValueWithQName(serviceStatisticsDataElement,
                new QName(STATISTICS_DATA_NS_URI, STATISTICS_DATA_ELEMENT_NAME_OPERATION_NAME));

        data.setServerName(serverName);
        data.setAvgResTime(avgResTime);
        data.setMinResTime(minResTime);
        data.setMaxResTime(maxResTime);
        data.setRequestCount(requestCount);
        data.setResponseCount(responseCount);
        data.setFaultCount(faultCount);
        data.setServiceName(serviceName);
        data.setOperationName(operationName);

        return data;
    }


    public static void checkDataAgainstDB(StatisticsData eventData, ServerDO addedServer,
                                          BAMListAdminServiceStub listAdminStub,
                                          BAMStatQueryDSStub statQueryStub) throws BAMListAdminServiceBAMException, RemoteException {
        MonitoredServerDTO[] servers = null;
        servers = listAdminStub.getServerList();

        Assert.assertTrue((servers != null), "Added server non existent in database..");
        boolean serverFound = false;
        for (MonitoredServerDTO server : servers) {
            ServiceDO[] services = null;

            if (isEqualServers(server, addedServer)) {

                serverFound = true;
                services = listAdminStub.getServiceList(server.getServerId());

                Assert.assertTrue((services != null), "Service non existent in database..");

                boolean serviceFound = false;
                for (ServiceDO service : services) {
                    OperationDO[] operations = null;

                    if (service.getName().equals(eventData.getServiceName())) {

                        serviceFound = true;
                        operations = listAdminStub.getOperationList(service.getId());
                        Assert.assertTrue((operations != null), "Operation non existent in database..");

                        boolean operationFound = false;
                        for (OperationDO operation : operations) {

                            if (operation.getName().equals(eventData.getOperationName())) {

                                operationFound = true;
                                Data[] data = null;
                                data = statQueryStub.getLatestDataForOperation(
                                        operation.getOperationID());

                                Assert.assertTrue((data != null), "Operation data non existent in database..");

                                verifyStatisticsData(data[0], eventData);

                                Assert.assertTrue(server.getServerURL().trim().
                                        equals(eventData.getServerName().trim()));
                                Assert.assertTrue(service.getName().trim().
                                        equals(eventData.getServiceName().trim()));
                                Assert.assertTrue(operation.getName().trim().
                                        equals(eventData.getOperationName().trim()));
                                break;
                            }
                        }

                        Assert.assertTrue(operationFound, "Added operation not present in the database..");
                    }
                    break;
                }
                Assert.assertTrue(serviceFound, "Added service not present in the database..");
                break;
            }
        }
        Assert.assertTrue(serverFound, "Added server not present in the database..");
    }

    private static boolean isEqualServers(MonitoredServerDTO first, ServerDO second) {
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

    private static void verifyStatisticsData(Data dbData, StatisticsData eventData) {
        Assert.assertTrue((int) Double.parseDouble(dbData.getAvgTime()) == (int) eventData.getAvgResTime());
        Assert.assertTrue(Long.parseLong(dbData.getMinTime()) == eventData.getMinResTime());
        Assert.assertTrue(Long.parseLong(dbData.getMaxTime()) == eventData.getMaxResTime());
        Assert.assertTrue(Integer.parseInt(dbData.getReqCount()) == eventData.getRequestCount());
        Assert.assertTrue(Integer.parseInt(dbData.getResCount()) == eventData.getResponseCount());
        Assert.assertTrue(Integer.parseInt(dbData.getFaultCount()) == eventData.getFaultCount());
    }

    public static int getIntDataValueWithQName(OMElement serviceStatisticsDataElement,
                                               QName qName) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        int value = -1;

        if (element != null) {
            value = Integer.parseInt(element.getText().trim());
        }
        return value;
    }

    public static long getLongDataValueWithQName(OMElement serviceStatisticsDataElement,
                                                 QName qName) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        long value = -1;

        if (element != null) {
            value = Long.parseLong(element.getText().trim());
        }
        return value;
    }

    public static Double getDoubleDataValueWithQName(OMElement serviceStatisticsDataElement,
                                                     QName qName) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        double value = -1;

        if (element != null) {
            value = Double.parseDouble(element.getText().trim());
        }
        return value;
    }

    public static String getStringDataValueWithQName(OMElement serviceStatisticsDataElement,
                                                     QName qName) {
        OMElement element = serviceStatisticsDataElement.getFirstChildWithName(qName);
        String value = null;

        if (element != null) {
            value = element.getText().trim();
        }
        return value;
    }
}
