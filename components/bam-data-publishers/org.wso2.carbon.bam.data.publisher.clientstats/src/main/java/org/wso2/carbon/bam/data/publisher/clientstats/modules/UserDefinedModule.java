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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.modules.Module;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.wso2.carbon.bam.data.publisher.clientstats.Counter;
import org.wso2.carbon.bam.data.publisher.clientstats.ClientStatisticsPublisherConstants;

public class UserDefinedModule implements Module {

    public void init(ConfigurationContext configurationContext, AxisModule axisModule)
                                                                                      throws AxisFault {

        {
           Counter globalRequestCounter = new Counter();
            configurationContext
                    .setProperty(
                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_REQUEST_COUNTER_PROPERTY,
                            globalRequestCounter);
        }

        {
            Counter globalResponseCounter = new Counter();
            configurationContext
                    .setProperty(
                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_RESPONSE_COUNTER_PROPERTY,
                            globalResponseCounter);
        }

        {
            Counter globalFaultCounter = new Counter();
            configurationContext
                    .setProperty(
                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_GLOBAL_FAULT_COUNTER_PROPERTY,
                            globalFaultCounter);
        }

        {

            ResponseTimeProcessor responseTimeProcessor = new ResponseTimeProcessor();
            configurationContext
                    .setProperty(
                            ClientStatisticsPublisherConstants.BAM_USER_DEFINED_RESPONSE_TIME_PROCESSOR_PROPERTY,
                            responseTimeProcessor);
        }
    }

    public void applyPolicy(Policy arg0, AxisDescription arg1) throws AxisFault {
        // TODO Auto-generated method stub

    }

    public boolean canSupportAssertion(Assertion arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    public void engageNotify(AxisDescription arg0) throws AxisFault {
        // TODO Auto-generated method stub

    }

    public void shutdown(ConfigurationContext arg0) throws AxisFault {
        // TODO Auto-generated method stub

    }

}
