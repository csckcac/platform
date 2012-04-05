/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.lwevent.core.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.lwevent.core.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/* This class extends AbstractAxis2ConfigurationContextObserver to engage Servicestat module,
 * when a new tenant is created.
 * 
 */
public class ServiceStatisticsAxis2ConfigurationContextObserver  extends AbstractAxis2ConfigurationContextObserver {
    private static final Log log = LogFactory.getLog(ServiceStatisticsAxis2ConfigurationContextObserver.class);

    public void createdConfigurationContext(ConfigurationContext configContext) {

        AxisConfiguration axisConfiguration = configContext.getAxisConfiguration();
        AxisModule serviceStatisticsModule = axisConfiguration
            .getModule(ServiceStatisticsPublisherConstants.BAM_SERVICE_STATISTISTICS_PUBLISHER_MODULE_NAME);
        int tenantId = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        if (serviceStatisticsModule != null) {
            try {
                axisConfiguration
                    .engageModule(ServiceStatisticsPublisherConstants.BAM_SERVICE_STATISTISTICS_PUBLISHER_MODULE_NAME);
            } catch (AxisFault e) {
                log.error("Cannot  engage ServiceStatistics module for teh tenant :" + tenantId, e);
            }
        }
    }


    public void terminatedConfigurationContext(ConfigurationContext configCtx) {

    }

    public void terminatingConfigurationContext(ConfigurationContext configCtx) {

    }
    
}
