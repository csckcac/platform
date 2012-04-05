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

import org.wso2.carbon.cep.core.mapping.input.Input;
import org.wso2.carbon.cep.core.exception.CEPEventProcessingException;
import org.wso2.carbon.cep.core.internal.CEPBucket;

/**
 * this class listens to the events comes from the topics
 * and do adaptation and send them to CEP
 */
public class TopicEventListener {

    private CEPBucket cepBucket;


    private Input input;


    public TopicEventListener(CEPBucket cepBucket, Input input) {
        this.cepBucket = cepBucket;
        this.input = input;
    }

    /**
     * forward the message to bucket so that it can send the message to real cep engine
     *
     * @param event
     * @throws CEPEventProcessingException
     */
    public void onEvent(Object event) throws CEPEventProcessingException {
        if (this.input.getInputMapping() !=  null){
            event = this.input.getInputMapping().convert(event);
        }
        this.cepBucket.insertEvent(event, this.input.getInputMapping());
    }

    /**
     * Defines the event definition to map the outputs accordingly
     * @param eventDef
     */
    public void onEventDefinition(Object eventDef) {
        input.getInputMapping().setEventDefinition(eventDef);
    }

    public CEPBucket getCepBucket() {
        return cepBucket;
    }

    public void setCepBucket(CEPBucket cepBucket) {
        this.cepBucket = cepBucket;
    }
}
