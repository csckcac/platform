/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.lwevent.core;

import org.wso2.carbon.bam.lwevent.core.notify.WSEventDispatcher;
import org.wso2.carbon.event.core.EventBroker;
import org.wso2.carbon.event.core.util.EventBrokerConstants;

public class WSEventBrokerHolder {

    private static WSEventBrokerHolder wsEventBrokerHolder = new WSEventBrokerHolder();

    private EventBroker eventBroker;

    public static WSEventBrokerHolder getInstance(){
        return wsEventBrokerHolder;
    }

    public EventBroker getEventBroker() {
        return eventBroker;
    }

    public void registerEventBroker(EventBroker eventBroker) {
        this.eventBroker = eventBroker;
    }

    public void registerWSEventDispatcher(){
        WSEventDispatcher wsEventDispatcher = new WSEventDispatcher();
        this.eventBroker.registerEventDispatcher(
                EventBrokerConstants.WS_EVENT_DISPATCHER_NAME, wsEventDispatcher);
    }
}
