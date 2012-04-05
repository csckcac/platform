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

package org.wso2.carbon.bam.common.dataobjects.service;

import java.util.Calendar;

/*
 * Operation Statistics Data class
 */
public class OperationStatisticsDO extends ServiceStatisticsDO {
    private String operationName;

    private int operationID;

    public OperationStatisticsDO() {
        this.operationID = -1;
    }

    public OperationStatisticsDO(String serverURL, Calendar timestamp, double avgResTime, double maxResTime, double minResTime,
                                 int reqCount, int resCount, int faultCount,String serviceName, String operationName) {

        super(serverURL, timestamp, avgResTime, maxResTime, minResTime, reqCount, resCount, faultCount, serviceName);
        this.operationName = operationName;
    }

    public int getOperationID() {
        return operationID;
    }

    public void setOperationID(int id) {
        this.operationID = id;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

//    public OperationDO getOperation() throws BAMException {
//        OperationDO operation;
//        if (getOperationID() > 0) {
//            operation = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getOperation(getOperationID());
//        } else {
//            operation = BAMPersistenceManager.getPersistenceManager(BAMUtil.getRegistry()).getOperation(getServiceID(),
//                    getOperationName());
//        }
//
//        if (operation != null) {
//            this.setOperationID(operation.getOperationID());
//            // The ID is auto generated at DB level.
//            // Hence we need to pick it up for future use
//        }
//
//        return operation;
//    }
}
