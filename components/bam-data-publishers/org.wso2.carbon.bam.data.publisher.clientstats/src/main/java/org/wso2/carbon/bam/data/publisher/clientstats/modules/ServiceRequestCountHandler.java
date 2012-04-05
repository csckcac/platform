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

import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;
import org.wso2.carbon.bam.data.publisher.clientstats.Counter;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handler to count the number of request to a certain service
 */
public class ServiceRequestCountHandler extends AbstractHandler {
    private static Log log = LogFactory.getLog(ServiceRequestCountHandler.class);

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        Counter counter = new Counter();
        String service = "";
        if (msgContext.getConfigurationContext().getProperty(
                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY) != null) {
            service = msgContext.getConfigurationContext().getProperty(
                    ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_PROPERTY)
                    .toString();
        } else {
            log.error("ServiceName property not found");
        }

        if (service != null) {
            Object serviceCountObj = msgContext
                    .getConfigurationContext()
                    .getProperty(
                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_REQUEST_COUNTER_PROPERTY);

            if (serviceCountObj != null) {
                if (serviceCountObj instanceof Counter) {
                    counter.increment(((Counter) serviceCountObj).getCount());
                }
            } else {

                counter.increment();
                msgContext
                        .getConfigurationContext()
                        .setProperty(
                                ClientStatisticsPublisherConstants.BAM_USER_DEFINED_SERVICE_REQUEST_COUNTER_PROPERTY,
                                counter);
            }
        }

        return InvocationResponse.CONTINUE;
    }
}
