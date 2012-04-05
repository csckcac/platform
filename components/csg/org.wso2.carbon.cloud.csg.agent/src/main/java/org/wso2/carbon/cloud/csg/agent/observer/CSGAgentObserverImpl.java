/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.cloud.csg.agent.observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cloud.csg.agent.service.CSGAgentAdminService;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGException;

/**
 * Implementation for {@link CSGAgentObserver}
 */
public class CSGAgentObserverImpl implements CSGAgentObserver {

    private String host;
    
    private int port;

    private String serviceName;

    private Log log = LogFactory.getLog(CSGAgentObserverImpl.class);

    public CSGAgentObserverImpl(String host, String serviceName, int port) {
        this.host = host;
        this.serviceName = serviceName;
        this.port = port;
    }

    public void update(CSGAgentSubject subject) {
        try {
            // do the re-publishing of the service again
            boolean isAutomatic = true;
            CSGAgentAdminService service = new CSGAgentAdminService();
            String status = service.getServiceStatus(serviceName);
            if (!status.equals(CSGConstant.CSG_SERVICE_STATUS_AUTO_MATIC)) {
                isAutomatic = false;
            }
            String serverName = service.getPublishedServer(serviceName);
            service.unPublishService(serviceName, serverName);
            service.publishService(serviceName, serverName, isAutomatic);
        } catch (CSGException e) {
            log.error("Error while re-publishing the service '" + serviceName + "' via " +
                    "received publish notification. You may need to re-publish the service manually!");
        }
    }

    public String getHostName() {
        return host;
    }
    
    public int getPort(){
        return port;
    }
}
