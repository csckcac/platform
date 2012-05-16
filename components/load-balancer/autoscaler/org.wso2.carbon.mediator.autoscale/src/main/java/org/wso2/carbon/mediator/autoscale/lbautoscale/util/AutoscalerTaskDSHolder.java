/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mediator.autoscale.lbautoscale.util;

import org.wso2.carbon.lb.common.conf.LoadBalancerConfiguration;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * Singleton class to hold Agent Management Service
 */
public class AutoscalerTaskDSHolder {
    
    private ConfigurationContextService configurationContextService;
    private LoadBalancerConfiguration lbConfig;

    private static AutoscalerTaskDSHolder instance = new AutoscalerTaskDSHolder();

    private AutoscalerTaskDSHolder(){

    }

    public static AutoscalerTaskDSHolder getInstance(){
        return instance;
    }

    public ConfigurationContextService getConfigurationContextServiceService(){
        return this.configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService cCtxService){
        this.configurationContextService = cCtxService;
    }

    public void setLoadBalancerConfig(LoadBalancerConfiguration lbConf) {
        this.lbConfig = lbConf;
    }
    
    public LoadBalancerConfiguration getLoadBalancerConfig() {
        return lbConfig;
    }

}
