/*
 * Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.data.publisher.servicestats;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBroker;
import org.wso2.carbon.bam.lwevent.core.LightWeightEventBrokerInterface;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ServiceHolder {

    private static ConfigurationContext configurationContext;
     private static LightWeightEventBrokerInterface lightWeightEventBrokerInterface;


    public static void setConfigurationContextService(ConfigurationContext context) {
        configurationContext = context;

    }

    public static void unsetConfigurationContextService(ConfigurationContext context) {
        configurationContext = null;
    }

    public static void setLWEventBroker(LightWeightEventBrokerInterface lwEventBrokerInterface)  {
         lightWeightEventBrokerInterface = lwEventBrokerInterface;
    }

    public static void unsetLWEventBroker(LightWeightEventBrokerInterface lwEventBrokerInterface) {
        lightWeightEventBrokerInterface = null;
    }

     public static LightWeightEventBrokerInterface getLWEventBroker(){
         return lightWeightEventBrokerInterface;
     }
}
