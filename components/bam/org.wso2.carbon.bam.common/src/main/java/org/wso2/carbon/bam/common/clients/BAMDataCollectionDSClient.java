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

package org.wso2.carbon.bam.common.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.common.ClientUtil;
import org.wso2.carbon.bam.common.dataobjects.activity.ActivityDO;
import org.wso2.carbon.bam.common.dataobjects.activity.MessageDO;
import org.wso2.carbon.bam.common.dataobjects.activity.MessageDataDO;
import org.wso2.carbon.bam.common.dataobjects.activity.MessagePropertyDO;
import org.wso2.carbon.bam.common.dataobjects.mediation.ServerUserDefinedDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationStatisticsDO;
import org.wso2.carbon.bam.common.dataobjects.service.OperationUserDefinedDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServerStatisticsDO;
import org.wso2.carbon.bam.common.dataobjects.service.ServiceStatisticsDO;
import org.wso2.carbon.bam.services.stub.bamdatacollectionds.BAMDataCollectionDSStub;
import org.wso2.carbon.bam.services.stub.bamdatacollectionds.types.*;
import org.wso2.carbon.bam.util.BAMCalendar;
import org.wso2.carbon.bam.util.BAMException;

public class BAMDataCollectionDSClient {

    private static final String BAM_DATA_COLLECTION_DS = "BAMDataCollectionDS";
    private BAMDataCollectionDSStub bamDataCollectionDSStub;
    private static final Log log = LogFactory.getLog(BAMDataCollectionDSClient.class);

    public BAMDataCollectionDSClient(String backendServerURL, ConfigurationContext configCtx)
            throws BAMException {

        try {
            String serviceURL = ClientUtil.getBackendEPR(backendServerURL, BAM_DATA_COLLECTION_DS);
            bamDataCollectionDSStub = new BAMDataCollectionDSStub(configCtx, serviceURL);
        } catch (Exception e) {
            throw new BAMException(e.getMessage(), e);
        }
    }

    public void cleanup() {
        try {
            bamDataCollectionDSStub._getServiceClient().cleanupTransport();
            bamDataCollectionDSStub._getServiceClient().cleanup();
            bamDataCollectionDSStub.cleanup();
        } catch (AxisFault axisFault) {
            if (log.isErrorEnabled()) {
                log.error("Stub cleanup failed: " + this.getClass().getName(), axisFault);
            }
        }
    }

    public void addServerData(ServerStatisticsDO statisticsDO) throws BAMException {

        try {
            // stub.addServerData(statisticsDO.getServerID(),
            // BAMCalendar.getInstance(statisticsDO.getTimestamp()).getBAMTimestamp(),
            // statisticsDO.getAvgResTime(),
            // statisticsDO.getMaxResTime(),
            // statisticsDO.getMinResTime(),
            // statisticsDO.getReqCount(),
            // statisticsDO.getResCount(),
            // statisticsDO.getFaultCount());

            AddServerData adddata = new AddServerData();
            AddServerData_type0 type = new AddServerData_type0();
            type.setServerID(statisticsDO.getServerID());
            type.setTimestamp(BAMCalendar.getInstance(statisticsDO.getTimestamp())
                    .getBAMTimestamp());
            type.setAvgResTime(statisticsDO.getAvgResTime());
            type.setCumFaultCount(statisticsDO.getFaultCount());
            type.setCumReqCount(statisticsDO.getReqCount());
            type.setCumResCount(statisticsDO.getResCount());
            type.setMaxResTime(statisticsDO.getMaxResTime());
            type.setMinResTime(statisticsDO.getMinResTime());

            adddata.setAddServerData(type);
            bamDataCollectionDSStub.addServerData(adddata);

        } catch (Exception e) {
            throw new BAMException("addServerData failed", e);
        }
    }

    public void addServiceData(ServiceStatisticsDO statisticsDO) throws BAMException {

        try {
            // stub.addServiceData(statisticsDO.getServiceID(),
            // BAMCalendar.getInstance(statisticsDO.getTimestamp()).getBAMTimestamp(),
            // statisticsDO.getAvgResTime(),
            // statisticsDO.getMaxResTime(),
            // statisticsDO.getMinResTime(),
            // statisticsDO.getReqCount(),
            // statisticsDO.getResCount(),statisticsDO.getFaultCount());
            AddServiceData adddata = new AddServiceData();
            AddServiceData_type0 type = new AddServiceData_type0();
            type.setServiceID(statisticsDO.getServiceID());
            type.setAvgResTime(statisticsDO.getAvgResTime());
            type.setCumFaultCount(statisticsDO.getFaultCount());
            type.setCumReqCount(statisticsDO.getReqCount());
            type.setCumResCount(statisticsDO.getResCount());
            type.setMaxResTime(statisticsDO.getMaxResTime());
            type.setMinResTime(statisticsDO.getMinResTime());
            type.setTimestamp(BAMCalendar.getInstance(statisticsDO.getTimestamp())
                    .getBAMTimestamp());

            adddata.setAddServiceData(type);
            bamDataCollectionDSStub.addServiceData(adddata);

        } catch (Exception e) {
            throw new BAMException("addServiceData failed", e);
        }

    }

    public void addOperationData(OperationStatisticsDO statisticsDO) throws BAMException {

        try {
            // stub.addOperationData(statisticsDO.getOperationID(),
            // BAMCalendar.getInstance(statisticsDO.getTimestamp()).getBAMTimestamp(),
            // statisticsDO.getAvgResTime(),
            // statisticsDO.getMaxResTime(),
            // statisticsDO.getMinResTime(),
            // statisticsDO.getReqCount(),
            // statisticsDO.getResCount(),
            // statisticsDO.getFaultCount());
            AddOperationData adddata = new AddOperationData();
            AddOperationData_type0 type = new AddOperationData_type0();
            type.setOperationID(statisticsDO.getOperationID());
            type.setAvgResTime(statisticsDO.getAvgResTime());
            type.setCumFaultCount(statisticsDO.getFaultCount());
            type.setCumReqCount(statisticsDO.getReqCount());
            type.setCumResCount(statisticsDO.getResCount());
            type.setMaxResTime(statisticsDO.getMaxResTime());
            type.setMinResTime(statisticsDO.getMinResTime());
            type.setTimestamp(BAMCalendar.getInstance(statisticsDO.getTimestamp())
                    .getBAMTimestamp());

            adddata.setAddOperationData(type);
            bamDataCollectionDSStub.addOperationData(adddata);
        } catch (Exception e) {
            throw new BAMException("addOperationData failed", e);
        }

    }

    /**
     * Adds user defined sever specific data to the data service.
     *
     * @param serverUserDefinedDO Server level user defined data object, that contains key/value
     *                            pair.
     * @throws org.wso2.carbon.bam.util.BAMException
     *          If adding data to the database failed.
     */
    public void addServerUserDefinedData(ServerUserDefinedDO serverUserDefinedDO)
            throws BAMException {
        try {
            // stub.addServerUserData(serverUserDefinedDO.getServerID(),BAMCalendar.getInstance(serverUserDefinedDO.getTimestamp()).getBAMTimestamp(),
            // serverUserDefinedDO.getKey(),
            // serverUserDefinedDO.getValue());
            AddServerUserData adddata = new AddServerUserData();
            AddServerUserData_type0 type = new AddServerUserData_type0();
            type.setServerID(serverUserDefinedDO.getServerID());
            type
                    .setTimestamp(BAMCalendar
                            .getInstance(
                                    serverUserDefinedDO
                                            .getTimestamp())
                            .getBAMTimestamp());
            type.setKey(serverUserDefinedDO.getKey());
            type.setValue(serverUserDefinedDO.getValue());

            adddata.setAddServerUserData(type);
            bamDataCollectionDSStub.addServerUserData(adddata);
        } catch (Exception e) {
            throw new BAMException("addServerUserDefinedData failed", e);
        }
    }

    /*
     * Add activity Details to the DB
     */

    public synchronized void addActivityData(ActivityDO activityDO) throws BAMException {
        try {
            // stub.addActivityData(activityDO.getName(),
            // activityDO.getDescription(), activityDO
            // .getActivityId());
            AddActivityData data = new AddActivityData();
            AddActivityData_type0 type = new AddActivityData_type0();
            type.setName(activityDO.getName());
            type.setDescription(activityDO.getDescription());
            type.setUserDefinedID(activityDO.getActivityId());

            data.setAddActivityData(type);
            bamDataCollectionDSStub.addActivityData(data);

        } catch (Exception e) {
            throw new BAMException("addActivityData failed", e);
        }
    }

    /*
     * Add Message Details to the DB
     */

    public synchronized void addMessageData(MessageDO messageDO) throws BAMException {
        try {
            // stub.addMessageData(messageDO.getOperationId(),
            // messageDO.getMessageId(),
            // messageDO.getActivityKeyId(),
            // messageDO.getTimestamp(), messageDO.getIPAddress(),
            // messageDO.getUserAgent());
            AddMessageData data = new AddMessageData();
            AddMessageData_type0 type = new AddMessageData_type0();

            type.setOperationID(messageDO.getOperationId());
            type.setMsgID(messageDO.getMessageId());
            type.setActivityID(messageDO.getActivityKeyId());
            type.setIpAddress(messageDO.getIPAddress());
            type.setTimestamp(messageDO.getTimestamp());
            type.setUserAgent(messageDO.getUserAgent());

            data.setAddMessageData(type);
            bamDataCollectionDSStub.addMessageData(data);

        } catch (Exception e) {
            throw new BAMException("addMessageData failed", e);
        }
    }

    /*
     * Add Message Data (body,timestamp)Details to the DB
     */

    public void addMessageDataDump(MessageDataDO messageDataDO, String direction)
            throws BAMException {
        try {
            String status;

            AddMessageDataDump data = new AddMessageDataDump();
            AddMessageDataDump_type0 type = new AddMessageDataDump_type0();

            if ("Request".equals(direction)) {
                status = messageDataDO.getRequestMessageStatus();
                if (status == null) {
                    status = "";
                }
//                       stub.addMessageDataDump(messageDataDO.getMessageKeyId(), messageDataDO.getActivityKeyId(), messageDataDO
//                                               .getTimestamp(), "Request", messageDataDO.getMessageBody(), messageDataDO
//                                               .getIpAddress(), status);

                type.setActivityKeyID(messageDataDO.getActivityKeyId());
                type.setMessageKeyID(messageDataDO.getMessageKeyId());
                type.setIpAddress(messageDataDO.getIpAddress());
                type.setMessageBody(messageDataDO.getMessageBody());
                type.setMessageDirection("Request");
                type.setStatus(status);
                type.setTimeStamp(messageDataDO.getTimestamp());

                data.setAddMessageDataDump(type);
                bamDataCollectionDSStub.addMessageDataDump(data);

            } else if ("Response".equals(direction)) {
                status = messageDataDO.getResponseMessageStatus();
                if (status == null) {
                    status = "";
                }
//                       stub.addMessageDataDump(messageDataDO.getMessageKeyId(), messageDataDO.getActivityKeyId(), messageDataDO
//                                               .getTimestamp(),"Response", messageDataDO.getMessageBody(), messageDataDO
//                                               .getIpAddress(), status);
                type.setActivityKeyID(messageDataDO.getActivityKeyId());
                type.setMessageKeyID(messageDataDO.getMessageKeyId());
                type.setIpAddress(messageDataDO.getIpAddress());
                type.setMessageBody(messageDataDO.getMessageBody());
                type.setMessageDirection("Response");
                type.setStatus(status);
                type.setTimeStamp(messageDataDO.getTimestamp());

                data.setAddMessageDataDump(type);
                bamDataCollectionDSStub.addMessageDataDump(data);
            }

        } catch (Exception e) {
            throw new BAMException("addMessageData failed", e);
        }
    }
    /*
     * Add Message Property Details to the DB
     */

    public synchronized void addMessageProperty(MessagePropertyDO messagePropertyDO)
            throws BAMException {
        try {
            // stub.addMessageProperty(messagePropertyDO.getMessageKeyId(),
            // messagePropertyDO.getActivityKeyId(),
            // messagePropertyDO.getKey(),
            // messagePropertyDO.getValue());

            String keyArray[] = messagePropertyDO.getKeyArray();
            String valueArray[] = messagePropertyDO.getValueArray();
            AddMessageProperty_batch_req batch_req = new AddMessageProperty_batch_req();
            AddMessageProperty_type0 properties[] =
                    new AddMessageProperty_type0[keyArray.length];
            for (int i = 0; i < properties.length; i++) {
                properties[i] = new AddMessageProperty_type0();
                properties[i].setActiivtyID(messagePropertyDO.getActivityKeyId());
                properties[i].setMessageID(messagePropertyDO.getMessageKeyId());
                properties[i].setKey(keyArray[i]);
                properties[i].setValue(valueArray[i]);
            }
            batch_req.setAddMessageProperty(properties);
            bamDataCollectionDSStub.addMessageProperty_batch_req(batch_req);

            // stub.addMessageProperty( messagePropertyDO);
        } catch (Exception e) {
            throw new BAMException("addMessageProperty failed", e);
        }
    }
    // adding JMX server data

//    public void addJMXServerUserData(ServerUserDefinedDO serverUserDefinedDO) throws BAMException {
//        try {
//            bamDataCollectionDSStub.addServerUserData(serverUserDefinedDO.getServerID(), BAMCalendar.getInstance(serverUserDefinedDO.getTimestamp()).getBAMTimestamp(),
//                    serverUserDefinedDO.getKey(), serverUserDefinedDO.getValue());
//        } catch (Exception e) {
//            throw new BAMException("addJMXServerUserData failed", e);
//        }
//    }

    /*
     * Add Operation user defined data.
     */

    public void addUserDefinedOperationData(OperationUserDefinedDO operationUserDefinedDO)
            throws BAMException {
        try {
            // stub.addOperationUserData(operationUserDefinedDO.getOperationID(),
            // BAMCalendar
            // .getInstance(operationUserDefinedDO.getTimestamp()).getBAMTimestamp(),
            // operationUserDefinedDO.getKey(),
            // operationUserDefinedDO
            // .getValue());

            AddOperationUserData data = new AddOperationUserData();
            AddOperationUserData_type0 type = new AddOperationUserData_type0();

            type.setOperationID(operationUserDefinedDO.getOperationID());
            type.setKey(operationUserDefinedDO.getKey());
            type.setValue(operationUserDefinedDO.getValue());
            type
                    .setTimestamp(BAMCalendar
                            .getInstance(
                                    operationUserDefinedDO
                                            .getTimestamp())
                            .getBAMTimestamp());

            data.setAddOperationUserData(type);
            bamDataCollectionDSStub.addOperationUserData(data);

        } catch (Exception e) {
            throw new BAMException("addServerUserDefinedData failed", e);
        }
    }

}