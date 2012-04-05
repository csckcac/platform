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

package org.wso2.carbon.cep.core.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;

/**
 * This listener is passed to broker component to receive the messages.
 */
public class BrokerEventListener implements BrokerListener {

    private static final Log log = LogFactory.getLog(BrokerEventListener.class);
    private TopicEventListener topicEventListener;

    public BrokerEventListener(TopicEventListener topicEventListener) {
        this.topicEventListener = topicEventListener;
    }

    @Override
    public void onEventDefinition(Object eventDef) throws BrokerEventProcessingException {
        if (log.isDebugEnabled()) {
            log.debug("Received Event Def : " + eventDef.toString());
        }
        this.topicEventListener.onEventDefinition(eventDef);
    }

    public void onEvent(Object event) throws BrokerEventProcessingException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Received Event : " + event.toString());
            }
            this.topicEventListener.onEvent(event);
        } catch (CEPEventProcessingException e) {
            String errorMessage = "can not process the message at cep level";
            log.error(errorMessage, e);
            throw new BrokerEventProcessingException(errorMessage, e);
        }
    }
}
