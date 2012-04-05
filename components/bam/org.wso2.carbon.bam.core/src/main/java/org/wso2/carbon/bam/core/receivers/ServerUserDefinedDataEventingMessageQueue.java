/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.core.receivers;


import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.dataobjects.mediation.ServerUserDefinedDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationUserDefinedDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceDO;
import org.wso2.carbon.bam.core.admin.BAMDataServiceAdmin;
import org.wso2.carbon.bam.core.cache.CacheConstant;
import org.wso2.carbon.bam.core.cache.CacheData;
import org.wso2.carbon.bam.core.cache.CacheImpl;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMConstants;
import org.wso2.carbon.bam.util.BAMException;

import javax.xml.namespace.QName;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class ServerUserDefinedDataEventingMessageQueue extends AbstractQueue {

    private static final Log log = LogFactory.getLog(ServerUserDefinedDataEventingMessageQueue.class);

    public static final String SERVER_USER_DEFINED_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/server/user-defined/data";
    public static final String OPERATION_USER_DEFINED_DATA_NS_URI = "http://wso2.org/ns/2009/09/bam/operation/user-defined/data";

    public static final String ELEMENT_NAME_EVENT = "Event";
    public static final String ELEMENT_NAME_SERVER_USER_DEFINED_DATA = "ServerUserDefinedData";
    public static final String ELEMENT_NAME_SERVER_NAME = "ServerName";
    public static final String ELEMENT_NAME_TENANT_ID = "TenantID";
    public static final String ELEMENT_NAME_DATA = "Data";
    public static final String ELEMENT_NAME_KEY = "Key";
    public static final String ELEMENT_NAME_VALUE = "Value";
    public static final String ELEMENT_NAME_OPERATION_USER_DEFINED_DATA = "OperationUserDefinedData";
    public static final String ELEMENT_NAME_OPERATION_NAME = "OperationName";
    public static final String ELEMENT_NAME_SERVICE_NAME = "ServiceName";
    public static final String ELEMENT_NAME_TIMESTAMP = "Timestamp";

    private HashMap<String, CacheData> bamCache;
    private CacheImpl cacheImpl;

    public ServerUserDefinedDataEventingMessageQueue(int threadPoolSize) {
        super(threadPoolSize);
    }

    @Override
    protected void processEvents(MessageContext[] messageContexts) {
        BAMDataServiceAdmin dsAdmin = new BAMDataServiceAdmin();
        //Get cache object for later use
        bamCache = BAMUtil.getBAMCache();
        cacheImpl = new CacheImpl();

        for (int i = 0; i < messageContexts.length; i++) {
            try {
                handleServerUserDefinedData(messageContexts[i], dsAdmin);
                handleOperationUserDefinedData(messageContexts[i], dsAdmin);
            } catch (BAMException e) {
                log.error("BAM ServerUserDefined MessageReceiver invokeBusinessLogic " +
                          e.getLocalizedMessage());
                log.error("BAM MR invokeBusinessLogic SOAP Envelope causing the problem" +
                          messageContexts[i].getEnvelope().toString());
            }
        }
    }


    private void handleServerUserDefinedData(MessageContext mc, BAMDataServiceAdmin dsAdmin)
            throws BAMException {
        // Get event element
        QName eventQname = new QName(SERVER_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_EVENT);
        OMElement eventElement = mc.getEnvelope().getBody().getFirstChildWithName(eventQname);

        String messageFaultReason;

        if (eventElement != null) { // Event element is mandatory
            // Get service statistics data wrapper element, which is mandatory
            QName serviceStatisticsDataQname = new QName(SERVER_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_SERVER_USER_DEFINED_DATA);
            OMElement serverUserDefinedDataElement = eventElement.getFirstChildWithName(serviceStatisticsDataQname);
            if (serverUserDefinedDataElement != null) { // ServerUserDefinedData is mandatory
                // Get ServerName element
                QName serverQname = new QName(SERVER_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_SERVER_NAME);
                OMElement serverNameElement = serverUserDefinedDataElement.getFirstChildWithName(serverQname);
                QName tenantQName = new QName(SERVER_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_TENANT_ID);
                OMElement tenantOmElement = serverUserDefinedDataElement.getFirstChildWithName(tenantQName);

                QName timestampQname = new QName(SERVER_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_TIMESTAMP);
                OMElement timestampElement = serverUserDefinedDataElement.getFirstChildWithName(timestampQname);
                Calendar calendar = Calendar.getInstance();
                //Set timestamp given by the mediation stat publisher
                if (timestampElement != null) {
                    calendar.setTimeInMillis(Timestamp.valueOf(timestampElement.getText()).getTime());
                }

                int tenantId = 0;
                if (tenantOmElement != null) { // this can be null if it's not from a carbon server
                    String tenant = tenantOmElement.getText();
                    if (tenant == null) {
                        log.error("tenant not define of event payload");
                        return;
                    }
                    tenant = tenant.trim();
                    tenantId = Integer.parseInt(tenant);
                }
                if (serverNameElement != null) { // ServerName element is mandatory
                    String serverName = serverNameElement.getText().trim();

                    ServerDO server = null;

                    if (timestampElement != null) {
                        try {
                            server = cacheImpl.getServer(dsAdmin, serverName, tenantId,
                                                       BAMConstants.SERVER_TYPE_EVENTING, BAMConstants.MEDIATION_STAT_TYPE);
                            // check whether server is already in DB else add it(client side BAM).
                            if (server == null) {
                                if (serverName != null || serverName.length() > 0) {
                                    server = new ServerDO();
                                    server.setServerURL(serverName);
                                    server.setTenantID(tenantId);
                                    server.setServerType(BAMConstants.SERVER_TYPE_EVENTING);
                                    server.setCategory(BAMConstants.MEDIATION_STAT_TYPE);
                                    BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).addMonitoredServer(server);
                                }
                                server = cacheImpl.getServer(dsAdmin, serverName, tenantId,
                                                           BAMConstants.SERVER_TYPE_EVENTING,
                                                           BAMConstants.MEDIATION_STAT_TYPE);
                            }

                        } catch (Exception e) {
                            log.error("Error persisting information about the new server:  " + serverName
                                      + " from eventing message messagereceiver " + e.getLocalizedMessage(), e);
                        }
                    } else {
                        try {
                            server = cacheImpl.getServer(dsAdmin, serverName, tenantId,
                                                       BAMConstants.SERVER_TYPE_GENERIC,
                                                       BAMConstants.GENERIC_STAT_TYPE);
                            // check whether server is already in DB else add it(client side BAM).
                            if (server == null) {
                                if (serverName != null || serverName.length() > 0) {
                                    server = new ServerDO();
                                    server.setServerURL(serverName);
                                    server.setTenantID(tenantId);
                                    server.setServerType(BAMConstants.SERVER_TYPE_GENERIC);
                                    server.setCategory(BAMConstants.GENERIC_STAT_TYPE);
                                    BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).addMonitoredServer(server);
                                }
                                server = cacheImpl.getServer(dsAdmin, serverName, tenantId,
                                                             BAMConstants.SERVER_TYPE_GENERIC,
                                                             BAMConstants.GENERIC_STAT_TYPE);
                            }

                        } catch (Exception e) {
                            log.error("Error persisting information about the new server:  " + serverName
                                      + " from eventing message messagereceiver " + e.getLocalizedMessage(), e);
                        }
                    }

                    QName dataQname = new QName(SERVER_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_DATA);
                    Iterator dataElements = serverUserDefinedDataElement.getChildrenWithName(dataQname);

                    while (dataElements.hasNext()) {
                        OMElement dataElement = (OMElement) dataElements.next();
                        // pick the key
                        QName keyQname = new QName(SERVER_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_KEY);
                        OMElement keyElement = dataElement.getFirstChildWithName(keyQname);
                        if (keyElement == null) {
                            messageFaultReason = "Key element not found in the message";
                            throw new BAMException(messageFaultReason);
                        }
                        // pick the value
                        QName valueQname = new QName(SERVER_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_VALUE);
                        OMElement valueElement = dataElement.getFirstChildWithName(valueQname);
                        if (valueElement == null) {
                            messageFaultReason = "Value element not found in the message";
                            throw new BAMException(messageFaultReason);
                        }

                        ServerUserDefinedDO serverUserDefinedDO = new ServerUserDefinedDO(server.getId(), serverName,
                                                                                          calendar, keyElement.getText(), valueElement.getText());

                        try {
                            dsAdmin.addServerUserDefinedData(serverUserDefinedDO);
                        } catch (BAMException e) {
                            log.error("Error updating user defined data for server " + serverName
                                      + " from eventing message messagereceiver " + e.getLocalizedMessage(), e);
                        }
                       }
                } else {
                    // Invalid message, missing ServerName element
                    messageFaultReason = "ServerName element not found in the message";
                    throw new BAMException(messageFaultReason);
                }

            } else {
                // Invalid message, missing ServiceStatisticsData element
                messageFaultReason = "ServerUserDefinedData element not found in the message";
                throw new BAMException(messageFaultReason);
            }

        }


    }


    /**
     * Handles the user defined key value pairs regarding operation data. Server, service and operation
     * names must be present in the event. Expects following event format.
     *
     * @param mc      Axis2 MessgeContext
     * @param dsAdmin Wrapper class for Data service clients
     * @throws BAMException If message is not in expected format
     */
    private void handleOperationUserDefinedData(MessageContext mc, BAMDataServiceAdmin dsAdmin)
            throws BAMException {

        String messageFaultReason;
        // Get event element
        QName operationEventQname = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_EVENT);
        OMElement operationEventElement = mc.getEnvelope().getBody().getFirstChildWithName(operationEventQname);

        if (operationEventElement != null) {

            QName operationStatisticsDataQName = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_OPERATION_USER_DEFINED_DATA);
            OMElement operationUserDefinedDataElement = operationEventElement.getFirstChildWithName(operationStatisticsDataQName);
            if (operationUserDefinedDataElement != null) {
                QName tenantQName = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_TENANT_ID);
                OMElement tenantOmElement = operationUserDefinedDataElement.getFirstChildWithName(tenantQName);

                if (tenantOmElement == null) {
                    log.error("tenant not define of event payload");
                    return;
                }

                String tenant = tenantOmElement.getText().trim();
                int tenantId = Integer.parseInt(tenant);

                // Get OperationName element
                QName operationQName = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_OPERATION_NAME);
                OMElement operationNameElement = operationUserDefinedDataElement.getFirstChildWithName(operationQName);
                QName serviceQName = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_SERVICE_NAME);
                OMElement serviceNameElement = operationUserDefinedDataElement.getFirstChildWithName(serviceQName);
                QName serverQName = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_SERVER_NAME);
                OMElement serverNameElement = operationUserDefinedDataElement.getFirstChildWithName(serverQName);

                if (operationNameElement != null && serviceNameElement != null && serverNameElement != null) {
                    String operationName = operationNameElement.getText().trim();
                    String serverName = serverNameElement.getText().trim();
                    String serviceName = serviceNameElement.getText().trim();

                    // for all the key/value pairs in a single message, we will use the same time stamp
                    Calendar calendar = Calendar.getInstance();

                    QName dataQName = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_DATA);
                    Iterator dataElements = operationUserDefinedDataElement.getChildrenWithName(dataQName);

                    while (dataElements.hasNext()) {
                        OMElement dataElement = (OMElement) dataElements.next();
                        QName keyQName = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_KEY);
                        OMElement keyElement = dataElement.getFirstChildWithName(keyQName);
                        if (keyElement == null) {
                            messageFaultReason = "Key element not found in the message";
                            throw new BAMException(messageFaultReason);
                        }
                        QName valueQName = new QName(OPERATION_USER_DEFINED_DATA_NS_URI, ELEMENT_NAME_VALUE);
                        OMElement valueElement = dataElement.getFirstChildWithName(valueQName);
                        if (valueElement == null) {
                            messageFaultReason = "Value element not found in the message";
                            throw new BAMException(messageFaultReason);
                        }

                        OperationUserDefinedDO operationUserDefinedDO = new OperationUserDefinedDO(operationName, calendar,
                                                                                                   keyElement.getText(), valueElement.getText());

                        try {

                            int serverID = 0;
                            int serviceID = 0;
                            // check whether server is already in DB else add it
                            ServerDO server = dsAdmin.getServer(serverName, tenantId,
                                                                BAMConstants.SERVER_TYPE_EVENTING, BAMConstants.MEDIATION_STAT_TYPE);
                            if (server == null) {
                                if (serverName != null && serverName.length() > 0) {
                                    server = new ServerDO();
                                    server.setServerURL(serverName);
                                    server.setTenantID(tenantId);
                                    BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).syncServer(server);
                                }
                            } else {
                                serverID = server.getId();
                            }
                            ServiceDO monitoringService = dsAdmin.getService(serverID, serviceName);
                            // check whether service is already in DB else add it
                            if (monitoringService == null) {
                                if (serviceName != null && serviceName.length() > 0) {
                                    ServiceDO service = new ServiceDO();
                                    service.setServerID(serverID);
                                    service.setName(serviceName);
                                    BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).syncService(service);
                                    serviceID = monitoringService.getId();
                                }
                            } else {
                                serviceID = monitoringService.getId();
                            }

                            OperationDO monitoringOperation = dsAdmin.getOperation(serviceID, operationName);
                            // check whether operation is already in DB else add it
                            if (monitoringOperation == null) {

                                OperationDO operation = new OperationDO();
                                operation.setServiceID(serviceID);
                                operation.setName(operationName);
                                BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).syncOperation(operation);
                            }

                            operationUserDefinedDO.setOperationID(monitoringOperation.getOperationID());
                            dsAdmin.addUserDefinedOperationData(operationUserDefinedDO);
                        } catch (Exception e) {
                            log.error("Error updating user defined data for operation " + operationName
                                      + " from eventing message messagereceiver " + e.getLocalizedMessage(), e);
                        }
                    }
                } else {
                    // Invalid message, missing ServerName element
                    messageFaultReason = "OperationName/ServiceName/WSASServerName element not found in the message";
                    throw new BAMException(messageFaultReason);
                }

            } else {
                // Invalid message, missing ServiceStatisticsData element
                messageFaultReason = "OperationUserDefinedData element not found in the message";
                throw new BAMException(messageFaultReason);
            }
        }
    }
}
