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
package org.wso2.carbon.bam.data.publisher.clientstats.modules;


import org.wso2.carbon.core.util.SystemFilter;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.wso2.carbon.bam.data.publisher.clientstats.Counter;
import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;

/**
 * Handler to count all requests to WSO2 WSAS
 */
public class GlobalRequestCountHandler extends AbstractHandler {

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        AxisService axisService = msgContext.getAxisService();
        Counter counter = new Counter();
        if (axisService == null ||
            SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup())) {
            return InvocationResponse.CONTINUE;
        }
        msgContext.getConfigurationContext()
                .setProperty(
                             ClientStatisticsPublisherConstants.BAM_USER_DEFINED_REQUEST_RECEIVED_TIME_PROPERTY,
                               "" + System.currentTimeMillis());
     
        Object globalrequestCountObj = msgContext.getConfigurationContext()
                .getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_REQUEST_COUNTER_PROPERTY);
        if (globalrequestCountObj != null) {
            if (globalrequestCountObj instanceof Counter) {
            counter.increment(((Counter) globalrequestCountObj).getCount());
        }
        }
        return InvocationResponse.CONTINUE;
    }
}
