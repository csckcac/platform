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

package org.wso2.carbon.bam.core.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.clients.BAMConfigurationDSClient;
import org.wso2.carbon.bam.common.clients.BAMDataCollectionDSClient;
import org.wso2.carbon.bam.common.dataobjects.activity.*;
import org.wso2.carbon.bam.common.dataobjects.mediation.ServerUserDefinedDO;
import org.wso2.carbon.bam.common.dataobjects.service.*;
import org.wso2.carbon.bam.common.dataobjects.stats.StatisticsDO;
import org.wso2.carbon.bam.core.internal.BAMServiceComponent;
import org.wso2.carbon.bam.core.persistence.BAMPersistenceManager;
import org.wso2.carbon.bam.core.util.BAMConfigurationCache;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;

import javax.sql.DataSource;
import javax.wsdl.Operation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BAMDataServiceAdmin {
    private static final Log log = LogFactory.getLog(BAMDataServiceAdmin.class); //TODO: Improve logging in this class

    public void addServerStatistics(ServerStatisticsDO statisticsDO) throws BAMException {
        BAMDataCollectionDSClient client = null;
        try {
            client = BAMUtil.getBAMDataCollectionDSClient();
            client.addServerData(statisticsDO);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public void addServiceStatistics(ServiceStatisticsDO statisticsDO) throws BAMException {
        // Add the service if it doesn't exist
        BAMPersistenceManager pm = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
        int serviceId = statisticsDO.getServiceID();
        ServiceDO serviceDO;
        if (serviceId > 0) {
//            serviceDO = pm.getService(serviceId);
            serviceDO = getService(serviceId);
        } else {
            serviceDO = pm.getService(statisticsDO.getServerID(), statisticsDO.getServiceName());
            statisticsDO.setServiceID(serviceDO.getId());
        }
        addServiceData(statisticsDO);
//
//        BAMDataCollectionDSClient client = null;
//        try {
//            client = BAMUtil.getBAMDataCollectionDSClient();
//            client.addServiceData(statisticsDO);
//        } finally {
//            if (client != null) {
//                client.cleanup();
//            }
//        }
    }

    public void addServiceData(ServiceStatisticsDO serviceStatsDO) throws BAMException {
        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_SERVICE_DATA (BAM_SERVICE_ID, BAM_TIMESTAMP, " +
                         "BAM_AVG_RES_TIME, BAM_MAX_RES_TIME, BAM_MIN_RES_TIME, BAM_CUM_REQ_COUNT, " +
                         "BAM_CUM_RES_COUNT, BAM_CUM_FAULT_COUNT) VALUES (" + serviceStatsDO.getServiceID() + ",'" +
                         BAMCalendar.getInstance(serviceStatsDO.getTimestamp()).getBAMTimestamp() + "'," + serviceStatsDO.getAvgResTime() + "," + serviceStatsDO.getMaxResTime() + "," +
                         serviceStatsDO.getMinResTime() + "," + serviceStatsDO.getReqCount() + "," + serviceStatsDO.getResCount() + "," +
                         serviceStatsDO.getFaultCount() + ")";


            boolean success = statement.execute(sql);
        } catch (SQLException e) {
            String errorMsg = "Cannot add operation data to DB for service name : " + serviceStatsDO.getServiceName();
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }
    }

    public ServiceDO getService(int serviceID) throws BAMException {
        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * FROM BAM_SERVICE WHERE BAM_ID=" + serviceID;

            boolean success = statement.execute(sql);
            ServiceDO serviceDO = null;
            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    serviceDO = new ServiceDO();
                    serviceDO.setId(resultSet.getInt("BAM_ID"));
                    serviceDO.setServerID(resultSet.getInt("BAM_SERVER_ID"));
                    serviceDO.setName(resultSet.getString("BAM_SERVICE_NAME"));
                    serviceDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
                }
            }
            return serviceDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve service data from DB for service id : " + serviceID;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }
    }

    public OperationDO getOperation(int operationId) throws BAMException {
        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * FROM BAM_OPERATION WHERE BAM_ID=" + operationId;

            boolean success = statement.execute(sql);
            OperationDO operationDO = null;
            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    operationDO = new OperationDO();
                    operationDO.setOperationID(resultSet.getInt("BAM_ID"));
                    operationDO.setServiceID(resultSet.getInt("BAM_SERVICE_ID"));
                    operationDO.setName(resultSet.getString("BAM_OP_NAME"));
                    operationDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
                }
            }
            return operationDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve operation data from DB for operation id : " + operationId;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }
    }

    public void addOperationStatistics(OperationStatisticsDO statisticsDO) throws BAMException {
        BAMPersistenceManager pm = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry());
        int operationId = statisticsDO.getOperationID();
        OperationDO operationDO;
        ServiceDO service;

        int serviceID = statisticsDO.getServiceID();
        if (serviceID > 0) {
            service = getService(serviceID);
//            service = pm.getService(serviceID);
        } else {
            service = pm.getService(statisticsDO.getServerID(), statisticsDO.getServiceName());
            statisticsDO.setServiceID(service.getId());
        }

        if (operationId > 0) {
            operationDO = getOperation(operationId);
        } else {
            operationDO = pm.getOperation(statisticsDO.getServiceID(), statisticsDO.getOperationName());
            statisticsDO.setOperationID(operationDO.getOperationID());
        }

        addOperationData(statisticsDO);
//        BAMDataCollectionDSClient client = null;
//        try {
//            client = BAMUtil.getBAMDataCollectionDSClient();
//            client.addOperationData(statisticsDO);
//        } finally {
//            if (client != null) {
//                client.cleanup();
//            }
//        }
    }

    public void addOperationData(OperationStatisticsDO opStatsDO) throws BAMException {
        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_OPERATION_DATA (BAM_OPERATION_ID, BAM_TIMESTAMP, " +
                         "BAM_AVG_RES_TIME, BAM_MAX_RES_TIME, BAM_MIN_RES_TIME, BAM_CUM_REQ_COUNT," +
                         " BAM_CUM_RES_COUNT, BAM_CUM_FAULT_COUNT) VALUES (" + opStatsDO.getOperationID() + ",'" +
                         BAMCalendar.getInstance(opStatsDO.getTimestamp()).getBAMTimestamp() + "'," + opStatsDO.getAvgResTime() + "," + opStatsDO.getMaxResTime() + "," +
                         opStatsDO.getMinResTime() + "," + opStatsDO.getReqCount() + "," + opStatsDO.getResCount() + "," +
                         opStatsDO.getFaultCount() + ")";


            boolean success = statement.execute(sql);
        } catch (SQLException e) {
            String errorMsg = "Cannot add operation data to DB for operation name : " + opStatsDO.getOperationName();
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }
    }

    public void addServerData(ServerStatisticsDO serverStatsDO) throws BAMException {
        Statement statement = null;
        Connection connection = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_SERVER_DATA (BAM_SERVER_ID ,BAM_TIMESTAMP ,BAM_AVG_RES_TIME ," +
                         " BAM_MAX_RES_TIME ,BAM_MIN_RES_TIME , BAM_CUM_REQ_COUNT , BAM_CUM_RES_COUNT, " +
                         "BAM_CUM_FAULT_COUNT) VALUES (" + serverStatsDO.getServerID() + ",'" +
                         BAMCalendar.getInstance(serverStatsDO.getTimestamp()).getBAMTimestamp() + "'," + serverStatsDO.getAvgResTime() + "," + serverStatsDO.getMaxResTime() + "," +
                         serverStatsDO.getMinResTime() + "," + serverStatsDO.getReqCount() + "," + serverStatsDO.getResCount() + "," +
                         serverStatsDO.getFaultCount() + ")";

            boolean success = statement.execute(sql);

        } catch (SQLException e) {
            String errorMsg = "Cannot add server data to DB for server url : " + serverStatsDO.getServerURL();
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

//        BAMDataCollectionDSClient client = null;
//        try {
//            client = BAMUtil.getBAMDataCollectionDSClient();
//            client.addServerData(statisticsDO);
//        } finally {
//            if (client != null) {
//                client.cleanup();
//            }
//        }
    }
//


    public void addServerUserDefinedData(ServerUserDefinedDO statisticsDO) throws BAMException {
        BAMDataCollectionDSClient client = null;
        try {
            addServerUserData(statisticsDO);
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    private void addServerUserData(ServerUserDefinedDO statisticsDO) throws BAMException {
        Connection connection = null;
        Statement statement = null;

        DataSource dataSource = BAMServiceComponent.getDataSource();
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_SERVER_USER_DATA " +
                         "(BAM_SERVER_ID, BAM_TIMESTAMP, BAM_KEY, BAM_VALUE) " +
                         "VALUES (" + statisticsDO.getServerID() + ",'" + BAMCalendar.getInstance(
                    statisticsDO.getTimestamp()).getBAMTimestamp() + "','"
                         + statisticsDO.getKey() + "','" + statisticsDO.getValue() + "')";
            boolean success = statement.execute(sql);
            if(success) System.out.print("ssss");
        } catch (SQLException e) {
            String errorMsg = "Cannot add data to BAM_SERVER_USER_DATA table for key: " + statisticsDO.getKey();
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }
    }


//    public boolean getServerUserData(int serverId) throws BAMException {
//      Connection connection = null;
//        Statement statement = null;
//        boolean found = false;
//        try {
//            DataSource dataSource = BAMServiceComponent.getDataSource();
//            connection = dataSource.getConnection();
//            statement = connection.createStatement();
//            String sql = "SELECT * FROM BAM_SERVICE";//" WHERE BAM_SERVER_ID=" + serverId;
//
//            boolean success = statement.execute(sql);
//            ServiceDO serviceDO = null;
//            if (success) {
//                ResultSet resultSet = statement.getResultSet();
//                boolean haveResults = resultSet.next();
//                if (haveResults) {
//                    found = true;
//                    serviceDO = new ServiceDO();
//                    serviceDO.setId(resultSet.getInt("BAM_ID"));
//                    serviceDO.setServerID(resultSet.getInt("BAM_SERVER_ID"));
//                    serviceDO.setName(resultSet.getString("BAM_SERVICE_NAME"));
//                    serviceDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
////                    serviceDO = new ServiceDO();
////                    serviceDO.setId(resultSet.getInt("BAM_ID"));
////                    serviceDO.setServerID(resultSet.getInt("BAM_SERVER_ID"));
////                    serviceDO.setName(resultSet.getString("BAM_SERVICE_NAME"));
////                    serviceDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
//                }
//            }
//            return false;
//        } catch (SQLException e) {
//            String errorMsg = "Cannot retrieve BAM_SERVER_USER_DATA table from DB for service id : " + serverId;
//            log.error(errorMsg, e);
//            throw new BAMException(errorMsg, e);
//        } finally {
//            try {
//                if (statement != null) {
//                    statement.close();
//                }
//                if (connection != null) {
//                    connection.close();
//                }
//            } catch (SQLException e) {
//                log.error("Cannot close connection to database.", e);
//            }
//        }
//    }

    /*
       * Add activity to the DB
       */

    public void addActivityData(ActivityDO activityDO) throws BAMException {

        Statement statement = null;
        Connection connection = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_ACTIVITY (BAM_NAME, BAM_DESCRIPTION, BAM_USER_DEFINED_ID)" +
                         " VALUES ('" + activityDO.getName() + "', '" + activityDO.getDescription() +
                         "', '" + activityDO.getActivityId() + "')";

            statement.execute(sql);

        } catch (SQLException e) {
            String errorMsg = "Cannot add activity data for activity :" + activityDO.getName();
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMDataCollectionDSClient client = null;
        try {
            client = BAMUtil.getBAMDataCollectionDSClient();
            client.addActivityData(activityDO);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }*/
    }

    public void addMessage(MessageDO messageDO) throws BAMException {
        BAMDataCollectionDSClient client = null;
        try {
            client = BAMUtil.getBAMDataCollectionDSClient();
            client.addMessageData(messageDO);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    /*
    * Add message to the DB.
    */

    public void addMessageData(MessageDO messageDO) throws BAMException {

        Statement statement = null;
        Connection connection = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_MESSAGE (BAM_OP_ID, BAM_MSG_ID, BAM_ACTIVITY_ID, " +
                         "BAM_TIMESTAMP, BAM_IP_ADDRESS, BAM_USER_AGENT) VALUES ('"+
                         messageDO.getOperationId()+"', '"+ messageDO.getMessageId() +"', '"+
                         messageDO.getActivityKeyId() +"', '"+ messageDO.getTimestamp() +"', '"+
                         messageDO.getIPAddress() +"', '"+ messageDO.getUserAgent() +"')";

            statement.execute(sql);

        } catch (SQLException e) {
            String errorMsg = "Cannot add activity data for messaege :" + messageDO.getMessageId();
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }
        
/*        BAMDataCollectionDSClient client = null;
        try {
            client = BAMUtil.getBAMDataCollectionDSClient();
            client.addMessageData(messageDO);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }*/
    }

    /*
    * Add message data to the DB.
    */

    public void addMessageDataDump(MessageDataDO messageDataDO, String direction)
            throws BAMException {

        Statement statement = null;
        Connection connection = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            String status;

            if (messageDataDO.getMsgStatus() == null) {
                status = "";
            } else {
                status = messageDataDO.getMsgStatus();
            }

            String sql = "INSERT INTO BAM_MESSAGE_DATA (BAM_MESSAGE_ID,  BAM_ACTIVITY_ID, " +
                         "BAM_TIMESTAMP, BAM_DIRECTION, BAM_MESSAGE, BAM_IP_ADDRESS, BAM_STATUS)" +
                         " VALUES ('"+ messageDataDO.getMessageKeyId() +"', '"+
                         messageDataDO.getActivityKeyId()+"', '"+ messageDataDO.getTimestamp() +"', '"+
                         direction +"','"+ messageDataDO.getMessageBody() +
                         "','"+ messageDataDO.getIpAddress() +"', '" + status+"')";

            statement.execute(sql);

        } catch (SQLException e) {
            String errorMsg = "Cannot add data for message :" + messageDataDO.getMessageKeyId();
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMDataCollectionDSClient client = null;
        try {
            client = BAMUtil.getBAMDataCollectionDSClient();
            client.addMessageDataDump(messageDataDO, direction);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }*/
    }

    /*
    * Add message Property data to the DB.
    */

    public void addMessageProperty(MessagePropertyDO messagePropertyDO)
            throws BAMException {
        BAMDataCollectionDSClient client = null;
        try {
            client = BAMUtil.getBAMDataCollectionDSClient();
            client.addMessageProperty(messagePropertyDO);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    /*
    * Add Operation user defined data.
    */

    public void addUserDefinedOperationData(OperationUserDefinedDO statisticsDO)
            throws BAMException {
        BAMDataCollectionDSClient client = null;
        try {
            addOperationUserData(statisticsDO);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    private void addOperationUserData(OperationUserDefinedDO statisticsDO) throws BAMException{
        Connection connection = null;
        Statement statement = null;

        DataSource dataSource = BAMServiceComponent.getDataSource();

        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_OPERATION_USER_DATA " +
                         "(BAM_OPERATION_ID, BAM_TIMESTAMP, BAM_KEY, BAM_VALUE) " +
                         "VALUES (" + statisticsDO.getOperationID() + ",'" + BAMCalendar.getInstance(
                    statisticsDO.getTimestamp()).getBAMTimestamp() + "','"
                         + statisticsDO.getKey() + "','" + statisticsDO.getValue() + "')";
            boolean success = statement.execute(sql);
        } catch (SQLException e) {
            String errorMsg = "Cannot add data to BAM_OPERATION_USER_DATA table for key: " + statisticsDO.getKey();
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }
    }


    public void addService(ServiceDO serviceDO) throws BAMException {

        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.addService(serviceDO);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public ServiceDO getService(int serverID, String serviceName) throws BAMException {
        Statement statement = null;
        Connection connection = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * FROM BAM_SERVICE WHERE BAM_SERVER_ID=" + serverID +
                         " AND BAM_SERVICE_NAME='" + serviceName + "'";

            boolean success = statement.execute(sql);
            ServiceDO serviceDO = null;
            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    serviceDO = new ServiceDO();
                    serviceDO.setId(resultSet.getInt("BAM_ID"));
                    serviceDO.setServerID(resultSet.getInt("BAM_SERVER_ID"));
                    serviceDO.setName(resultSet.getString("BAM_SERVICE_NAME"));
                    serviceDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
                }
            }
            return serviceDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve service data from DB for service name : " + serviceName;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

//          ServiceDO service = BAMConfigurationCache.getService(serverID, serviceName);
//          BAMConfigurationDSClient client = null;
//          try {
//              client = BAMUtil.getBAMConfigurationDSClient();
//              service = client.getService(serverID, serviceName);
//              if (service != null) {
//                  BAMConfigurationCache.addService(serverID,service);
//              }
//          } catch (BAMException e) {
//              throw e;
//          } finally {
//              if (client != null) {
//                  client.cleanup();
//              }
//          }
//          return service;
    }

    /**
     * @param serverUrl
     * @param tenantId
     * @return
     * @throws BAMException
     */
    public ServerDO getServer(String serverUrl, int tenantId, String serverType, int category)
            throws BAMException {
        Statement statement = null;
        Connection connection = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * FROM BAM_SERVER WHERE BAM_URL='" + serverUrl + "' and BAM_TENENT_ID=" + tenantId +
                         " and BAM_TYPE='" + serverType + "' and BAM_CATEGORY='" + category + "'";

            boolean success = statement.execute(sql);
            ServerDO serverDO = null;
            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    serverDO = new ServerDO();
                    serverDO.setId(resultSet.getInt("BAM_SERVER_ID"));
                    serverDO.setTenantID(resultSet.getInt("BAM_TENENT_ID"));
                    serverDO.setServerType(resultSet.getString("BAM_TYPE"));
                    serverDO.setServerURL(resultSet.getString("BAM_URL"));
                    serverDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
                    serverDO.setActive(resultSet.getBoolean("BAM_ACTIVE"));
                    serverDO.setCategory(resultSet.getInt("BAM_CATEGORY"));
                    serverDO.setSubscriptionEPR(resultSet.getString("BAM_EPR"));
                    serverDO.setSubscriptionID(resultSet.getString("BAM_SUBSCRIPTION_ID"));
                    serverDO.setUserName(resultSet.getString("USERNAME"));
                    serverDO.setPassword(resultSet.getString("PASSWORD"));
                }
            }
            return serverDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve server data from DB for server url : " + serverUrl + " and server type : " + serverType;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }


//          BAMConfigurationDSClient client = null;
//          try {
//              client = BAMUtil.getBAMConfigurationDSClient();
//              return client.getServer(serverUrl, tenantId,serverType,category);
//          } catch (BAMException e) {
//              throw e;
//          } finally {
//              if (client != null) {
//                  client.cleanup();
//              }
//          }
    }

    public ServerDO getServer(String serverUrl) throws BAMException {

        Statement statement = null;
        Connection connection = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * FROM BAM_SERVER WHERE BAM_URL='" + serverUrl + "'";

            boolean success = statement.execute(sql);
            ServerDO serverDO = null;
            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    serverDO = new ServerDO();
                    serverDO.setId(resultSet.getInt("BAM_SERVER_ID"));
                    serverDO.setTenantID(resultSet.getInt("BAM_TENENT_ID"));
                    serverDO.setServerType(resultSet.getString("BAM_TYPE"));
                    serverDO.setServerURL(resultSet.getString("BAM_URL"));
                    serverDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
                    serverDO.setActive(resultSet.getBoolean("BAM_ACTIVE"));
                    serverDO.setCategory(resultSet.getInt("BAM_CATEGORY"));
                    serverDO.setSubscriptionEPR(resultSet.getString("BAM_EPR"));
                    serverDO.setSubscriptionID(resultSet.getString("BAM_SUBSCRIPTION_ID"));
                    serverDO.setUserName(resultSet.getString("USERNAME"));
                    serverDO.setPassword(resultSet.getString("PASSWORD"));
                }
            }
            return serverDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve server data from DB for server url : " + serverUrl;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMConfigurationDSClient client = null;
try {
    client = BAMUtil.getBAMConfigurationDSClient();
    return client.getServer(serverUrl);
} catch (BAMException e) {
    throw e;
} finally {
    if (client != null) {
        client.cleanup();
    }
}*/
    }


    public void addOperation(OperationDO operation) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.addOperation(operation);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public OperationDO getOperation(int serviceID, String operationName) throws BAMException {
        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * FROM BAM_OPERATION WHERE BAM_SERVICE_ID=" + serviceID +
                         " AND BAM_OP_NAME='" + operationName + "'";

            boolean success = statement.execute(sql);
            OperationDO operationDO = null;
            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    operationDO = new OperationDO();
                    operationDO.setOperationID(resultSet.getInt("BAM_ID"));
                    operationDO.setServiceID(resultSet.getInt("BAM_SERVICE_ID"));
                    operationDO.setName(resultSet.getString("BAM_OP_NAME"));
                    operationDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
                }
            }
            return operationDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve operation data from DB for operation name : " + operationName;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }
//          BAMConfigurationDSClient client = null;
//          try {
//              client = BAMUtil.getBAMConfigurationDSClient();
//              return client.getOperation(serviceID, operationName);
//          } catch (BAMException e) {
//              throw e;
//          } finally {
//              if (client != null) {
//                  client.cleanup();
//              }
//          }
    }

    public ActivityDO getActivityForActivityID(String activityID) throws BAMException {
        ActivityDO activityDO = BAMConfigurationCache.getActivity(activityID);
        if (activityDO == null) {

            Connection connection = null;
            Statement statement = null;
            try {
                DataSource dataSource = BAMServiceComponent.getDataSource();
                connection = dataSource.getConnection();
                statement = connection.createStatement();
                String sql = "SELECT * FROM BAM_ACTIVITY WHERE BAM_USER_DEFINED_ID='" + activityID +
                             "'";

                boolean success = statement.execute(sql);

                if (success) {
                    ResultSet resultSet = statement.getResultSet();
                    boolean haveResults = resultSet.next();
                    if (haveResults) {
                        activityDO = new ActivityDO();
                        activityDO.setActivityKeyId(resultSet.getInt("BAM_ID"));
                        activityDO.setName(resultSet.getString("BAM_NAME"));
                        activityDO.setDescription(resultSet.getString("BAM_DESCRIPTION"));
                        activityDO.setActivityId(resultSet.getString("BAM_USER_DEFINED_ID"));
                    }
                }
                return activityDO;
            } catch (SQLException e) {
                String errorMsg = "Cannot retrieve data for activity with id : " + activityID;
                log.error(errorMsg, e);
                throw new BAMException(errorMsg, e);
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    log.error("Cannot close connection to database.", e);
                }
            }

            /*            BAMConfigurationDSClient client = null;
            try {
                client = BAMUtil.getBAMConfigurationDSClient();
                activityDO = client.getActivityForActivityID(activityID);
                if(activityDO != null){
                    BAMConfigurationCache.addActivity(activityID, activityDO);
                }
            } catch (BAMException e) {
                throw e;
            } finally {
                if (client != null) {
                    client.cleanup();
                }
            }*/
        }
        return activityDO;
    }

    public void addActivity(ActivityDO activity) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.addActivity(activity);
            BAMConfigurationCache.addActivity(activity.getActivityKeyId(), activity);
            BAMConfigurationCache.addActivity(activity.getActivityId(), activity);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public MessageDO getMessage(String messageId, int operationId, int activityKeyId)
            throws BAMException {

        Connection connection = null;
        Statement statement = null;

        MessageDO messageDO = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * FROM BAM_MESSAGE WHERE BAM_MSG_ID='"+ messageId +"' AND BAM_OP_ID="+
                         operationId +" AND BAM_ACTIVITY_ID="+ activityKeyId;

            boolean success = statement.execute(sql);

            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    messageDO = new MessageDO();
                    messageDO.setMessageKeyId(resultSet.getInt("BAM_ID"));
                    messageDO.setOperationId(resultSet.getInt("BAM_OP_ID"));
                    messageDO.setMessageId(resultSet.getString("BAM_MSG_ID"));
                    messageDO.setActivityKeyId(resultSet.getInt("BAM_ACTIVITY_ID"));
                    messageDO.setTimestamp(resultSet.getString("BAM_TIMESTAMP"));
                    messageDO.setIPAddress(resultSet.getString("BAM_IP_ADDRESS"));
                    messageDO.setUserAgent(resultSet.getString("BAM_USER_AGENT"));
                }
            }
            return messageDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve data for message with id : " + messageId;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMConfigurationDSClient client = null;
try {
    client = BAMUtil.getBAMConfigurationDSClient();
    return client.getMessage(messageId, operationId, activityKeyId);
} catch (BAMException e) {
    throw e;
} finally {
    if (client != null) {
        client.cleanup();
    }
}*/
    }

    public MessageDataDO getMessageDataForActivityKeyIDandMessageKeyID(int messageKeyID,
                                                                       int activityKeyID)
            throws BAMException {

        Connection connection = null;
        Statement statement = null;

        MessageDataDO messageDataDO = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT * FROM BAM_MESSAGE_DATA WHERE BAM_MESSAGE_ID="+ messageKeyID +
                         " AND BAM_ACTIVITY_ID=" + activityKeyID ;

            boolean success = statement.execute(sql);

            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    messageDataDO = new MessageDataDO();
                    messageDataDO.setMessageDataKeyId(resultSet.getInt("BAM_ID"));
                    messageDataDO.setMessageKeyId(resultSet.getInt("BAM_MESSAGE_ID"));
                    messageDataDO.setActivityKeyId(resultSet.getInt("BAM_ACTIVITY_ID"));
                    messageDataDO.setTimestamp(resultSet.getString("BAM_TIMESTAMP"));
                    messageDataDO.setMessageDirection(resultSet.getString("BAM_DIRECTION"));
                    messageDataDO.setMessageBody(resultSet.getString("BAM_MESSAGE"));
                    messageDataDO.setIpAddress(resultSet.getString("BAM_IP_ADDRESS"));
                }
            }
            return messageDataDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve data for message with id : " + messageKeyID;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            return client.getMessageDataForActivityKeyIDandMessageKeyID(messageKeyID, activityKeyID);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }*/
    }

    public void updateMessageStatus(String messageStatus, int messageDataKeyId)
            throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.updateMessageStatus(messageStatus, messageDataKeyId);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public void updateActivity(String name, String description, int activityKeyId)
            throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.updateActivity(name, description, activityKeyId);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public void updateMessageDump(String messageBody, String messageDir, String ipAddress,
                                  int messageDataKeyId)
            throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.updateMessageDump(messageBody, messageDir, ipAddress, messageDataKeyId);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public MessagePropertyDO getPropertyofMessage(int messageKeyId, int activityKeyId, String key)
            throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            return client.getPropertyofMessage(messageKeyId, activityKeyId, key);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

    public PropertyFilterDO getXpathConfiguration(String xpathKey, int serverId)
            throws BAMException {

        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "SELECT BAM_ID,BAM_ALIAS,BAM_XPATH FROM BAM_XPATH WHERE BAM_NAME='" +
                         xpathKey + "' AND BAM_SERVER_ID=" + serverId;

            boolean success = statement.execute(sql);
            PropertyFilterDO propertyFilterDO = null;
            if (success) {
                ResultSet resultSet = statement.getResultSet();
                boolean haveResults = resultSet.next();
                if (haveResults) {
                    propertyFilterDO = new PropertyFilterDO();
                    propertyFilterDO.setId(resultSet.getInt("BAM_ID"));
                    propertyFilterDO.setAlias(resultSet.getString("BAM_ALIAS"));
                    propertyFilterDO.setExpression(resultSet.getString("BAM_XPATH"));
                }
            }
            return propertyFilterDO;
        } catch (SQLException e) {
            String errorMsg = "Cannot retrieve operation data from DB for xpath key : " + xpathKey;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMConfigurationDSClient client = null;
try {
    client = BAMUtil.getBAMConfigurationDSClient();
    return client.getXpathData(xpathKey, serverId);
} catch (BAMException e) {
    throw e;
} finally {
    if (client != null) {
        client.cleanup();
    }
}*/
    }

    public void addXpathConfiguration(String alias, String xpathKey, String expression,
                                      int serverId)
            throws BAMException {

        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_XPATH(BAM_ALIAS,BAM_NAME,BAM_XPATH,BAM_SERVER_ID) VALUES ('" +
                         alias + "','" + xpathKey + "','" + expression + "','" + serverId + "')";

            statement.execute(sql);

        } catch (SQLException e) {
            String errorMsg = "Cannot add xpath key : " + xpathKey;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMConfigurationDSClient client = null;
try {
    client = BAMUtil.getBAMConfigurationDSClient();
    client.addXpathData(alias, xpathKey, expression, serverId);
} catch (BAMException e) {
    throw e;
} finally {
    if (client != null) {
        client.cleanup();
    }
}*/
    }

    public void updateXpathConfiguration(String alias, String xpathKey, String expression,
                                         int serverId, int bamId) throws BAMException {

        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "UPDATE BAM_XPATH SET BAM_ALIAS='" + alias + "', BAM_NAME='" + xpathKey + "', " +
                         "BAM_XPATH='" + expression + "', BAM_SERVER_ID=" + serverId + " WHERE BAM_ID=" +
                         bamId;

            statement.execute(sql);

        } catch (SQLException e) {
            String errorMsg = "Cannot update DB data for xpath key : " + xpathKey;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.updateXpathData(alias, xpathKey, expression, serverId, bamId);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }*/
    }

    public void addNamespaceData(int xpathId, String prefix, String uri) throws BAMException {

        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "INSERT INTO BAM_NAMESPACE(BAM_XPATH_ID,BAM_PREFIX, BAM_URI) VALUES('" +
                         xpathId + "','" + prefix + "','" + uri + "')";

            statement.execute(sql);

        } catch (SQLException e) {
            String errorMsg = "Cannot add namespace for xpath id : " + xpathId;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.addNamespaceData(xpathId, prefix, uri);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }*/
    }

    public void deleteNamespaceData(int xpathId) throws BAMException {

        Connection connection = null;
        Statement statement = null;
        try {
            DataSource dataSource = BAMServiceComponent.getDataSource();
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            String sql = "DELETE FROM BAM_NAMESPACE WHERE BAM_XPATH_ID=" + xpathId;

            statement.execute(sql);

        } catch (SQLException e) {
            String errorMsg = "Cannot delete DB data for xpath id : " + xpathId;
            log.error(errorMsg, e);
            throw new BAMException(errorMsg, e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("Cannot close connection to database.", e);
            }
        }

/*        BAMConfigurationDSClient client = null;
try {
    client = BAMUtil.getBAMConfigurationDSClient();
    client.deleteNamespaceData(xpathId);
} catch (BAMException e) {
    throw e;
} finally {
    if (client != null) {
        client.cleanup();
    }
}*/
    }

    public void updateMessageProperty(String value, int messagePropertyKeyId) throws BAMException {
        BAMConfigurationDSClient client = null;
        try {
            client = BAMUtil.getBAMConfigurationDSClient();
            client.updateMessageProperty(value, messagePropertyKeyId);
        } catch (BAMException e) {
            throw e;
        } finally {
            if (client != null) {
                client.cleanup();
            }
        }
    }

}
