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

package org.wso2.carbon.broker.core;

import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;

/**
 * listener class to receive the events from the broker proxy
 */
public interface BrokerListener {

    /**
     * when an event definition is defined broker proxy call this method with the recived event.
     *
     * @param object - received event definition
     */
    void onEventDefinition(Object object) throws BrokerEventProcessingException;

    /**
     * when an event happens broker proxy call this method with the recived event.
     *
     * @param object - received event
     */
    void onEvent(Object object) throws BrokerEventProcessingException;

}
