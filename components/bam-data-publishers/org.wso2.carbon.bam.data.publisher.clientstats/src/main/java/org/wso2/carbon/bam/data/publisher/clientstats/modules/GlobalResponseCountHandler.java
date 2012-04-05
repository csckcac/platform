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

import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;

import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.SystemFilter;
import org.wso2.carbon.bam.data.publisher.clientstats.Counter;
import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;

/**
 * Handler to count all responses from services
 */
public class GlobalResponseCountHandler extends AbstractHandler {
    private static Log log = LogFactory.getLog(GlobalResponseCountHandler.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        AxisService axisService = msgContext.getAxisService();
        Counter counter = new Counter();
        if (axisService == null
                || SystemFilter.isFilteredOutService(axisService.getAxisServiceGroup())) {
            return InvocationResponse.CONTINUE;
        }

        Object globalResponseCountObj = msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_RESPONSE_COUNTER_PROPERTY);

        if (globalResponseCountObj != null) {
            if (globalResponseCountObj instanceof Counter) {
                counter.increment(((Counter) globalResponseCountObj).getCount());
            }
        } else {
            log
                    .warn(ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_RESPONSE_COUNTER_PROPERTY
                            + " is null");
        }
        return InvocationResponse.CONTINUE;
    }
}
