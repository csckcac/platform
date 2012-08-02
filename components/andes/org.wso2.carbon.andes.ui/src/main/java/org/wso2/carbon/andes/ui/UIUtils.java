/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.andes.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.andes.stub.AndesAdminServiceStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;
import org.wso2.carbon.andes.stub.admin.types.Queue;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

public class UIUtils {

    public static String getHtmlString(String message) {
        return message.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

    }
      public static AndesAdminServiceStub getAndesAdminServiceStub(ServletConfig config,
                                                                           HttpSession session,
                                                                           HttpServletRequest request)
            throws AxisFault {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        backendServerURL = backendServerURL + "AndesAdminService";
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        AndesAdminServiceStub stub = new AndesAdminServiceStub(configContext,backendServerURL);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        if (cookie != null) {
            Options option = stub._getServiceClient().getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }

        return stub;
    }

    /**
     * filter the full queue list to suit the range
     * @param fullList
     * @param startingIndex
     * @param maxQueueCount
     * @return  Queue[]
     */
    public static Queue[] getFilteredQueueList(Queue[] fullList ,int startingIndex, int maxQueueCount) {
        Queue[] queueDetailsArray;
        int resultSetSize = maxQueueCount;

        ArrayList<Queue> resultList = new ArrayList<Queue>();
        for(Queue aQueue : fullList) {
            resultList.add(aQueue);
        }

        if ((resultList.size() - startingIndex) < maxQueueCount) {
            resultSetSize = (resultList.size() - startingIndex);
        }
        queueDetailsArray = new Queue[resultSetSize];
        int index = 0;
        int queueDetailsIndex = 0;
        for (Queue queueDetail : resultList) {
            if (startingIndex == index || startingIndex < index) {
                queueDetailsArray[queueDetailsIndex] = new Queue();

                queueDetailsArray[queueDetailsIndex].setQueueName(queueDetail.getQueueName());
                queueDetailsArray[queueDetailsIndex].setMessageCount(queueDetail.getMessageCount());
                queueDetailsIndex++;
                if (queueDetailsIndex == maxQueueCount) {
                    break;
                }
            }
            index++;
        }

        return queueDetailsArray;
    }
}
