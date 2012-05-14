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
package org.wso2.carbon.andes.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.andes.core.QueueManagerService;
import org.wso2.carbon.andes.core.internal.builder.QueueManagerServiceBuilder;

/**
 * @scr.component name="QueueManagerService.component" immediate="true"
 */

public class QueueManagerServiceDS {

    public static Log log = LogFactory.getLog(QueueManagerServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            QueueManagerService brokerService = QueueManagerServiceBuilder.createQueueManagerService();
            context.getBundleContext().registerService(QueueManagerService.class.getName(),
                    brokerService, null);
            log.info("Successfully created the queue manager service");
        } catch (RuntimeException e) {
            log.error("Can not create queue manager service ", e);
        }
    }

}
