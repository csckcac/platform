/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.registry.subscription.test.util;

import java.rmi.RemoteException;
import java.util.Date;

import org.wso2.carbon.automation.api.clients.governance.HumanTaskAdminClient;
import org.wso2.carbon.automation.api.clients.governance.WorkItem;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;

public class WorkItemClient {

    public static WorkItem[] getWorkItems(HumanTaskAdminClient humanTaskAdminClient)
            throws RemoteException, IllegalStateFault, IllegalAccessFault, IllegalArgumentFault {
        long startTime = new Date().getTime();
        long endTime = startTime + 2 * 60 * 1000;
        WorkItem[] workItems = null;
        while ((new Date().getTime()) < endTime) {
            workItems = humanTaskAdminClient.getWorkItems();
            if (workItems.length > 0) {
                break;
            }
        }

        return workItems;
    }

}
