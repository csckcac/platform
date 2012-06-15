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
package org.wso2.carbon.eventbridge.receiver.thrift;

import org.wso2.carbon.agent.server.EventBridgeReceiverService;
import org.wso2.carbon.eventbridge.receiver.thrift.conf.ThriftEventReceiverConfiguration;
import org.wso2.carbon.eventbridge.receiver.thrift.internal.ThriftEventReceiver;

/**
 * The falconry method that is used to create Agent server
 */
public class ThriftEventReceiverFactory {
    public ThriftEventReceiver createAgentServer(ThriftEventReceiverConfiguration thriftEventReceiverConfiguration,
                                                 EventBridgeReceiverService eventBridgeReceiverService) {
        return new ThriftEventReceiver(thriftEventReceiverConfiguration, eventBridgeReceiverService);
    }

    public ThriftEventReceiver createAgentServer(int secureReceiverPort, int receiverPort,
                                                 EventBridgeReceiverService eventBridgeReceiverService) {
        return new ThriftEventReceiver(secureReceiverPort, receiverPort, eventBridgeReceiverService);
    }


    public ThriftEventReceiver createAgentServer(int receiverPort,
                                                 EventBridgeReceiverService eventBridgeReceiverService) {
        return new ThriftEventReceiver(receiverPort, eventBridgeReceiverService);
    }

}
