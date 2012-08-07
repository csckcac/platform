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
package org.wso2.carbon.identity.scim.consumer.config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.scim.consumer.utils.IdentitySCIMException;
import org.wso2.carbon.identity.scim.consumer.utils.SCIMConsumerConstants;

/*
 * Class that represents scim-consumer-config.xml
 */

public class SCIMConsumersConfig {

    private static Map<String, SCIMConsumerConfig> scimConsumers;
    private static Log logger = LogFactory.getLog(SCIMConsumerConfig.class);

    /**
     * Returns a map of SCIM Consumer configurations.
     *
     * @return
     * @throws IdentitySCIMException
     */
    public static Map<String, SCIMConsumerConfig> getScimConsumers() throws IdentitySCIMException {
        if (scimConsumers != null) {
            return scimConsumers;
        } else {
            String error = "SCIM Consumer Configuration Map is not initialized..";
            logger.error(error);
            throw new IdentitySCIMException(error);
        }
    }

    public static void setScimConsumers(List<Map<String, String>> scimConsumersList) {
        for (Map<String, String> scimConsumerMap : scimConsumersList) {
            SCIMConsumerConfig scimConsumerConfig = new SCIMConsumerConfig();
            scimConsumerConfig.setConsumerConfigProperties(scimConsumerMap);
            if (scimConsumers == null) {
                scimConsumers = new ConcurrentHashMap<String, SCIMConsumerConfig>();
                scimConsumers.put(scimConsumerMap.get(SCIMConsumerConstants.TENANT_DOMAIN), scimConsumerConfig);
            } else {
                scimConsumers.put(scimConsumerMap.get(SCIMConsumerConstants.TENANT_DOMAIN), scimConsumerConfig);
            }
        }
    }

    //represents a single SCIMConsumerConfig element

    public static class SCIMConsumerConfig {
        private Map<String, String> consumerConfigProperties;

        public SCIMConsumerConfig() {

        }

        public Map<String, String> getConsumerConfigProperties() {
            return consumerConfigProperties;
        }

        public void setConsumerConfigProperties(Map<String, String> consumerConfigProperties) {
            this.consumerConfigProperties = consumerConfigProperties;
        }

        public String getProperty(String propertyName) {
            return consumerConfigProperties.get(propertyName);
        }
    }

}
