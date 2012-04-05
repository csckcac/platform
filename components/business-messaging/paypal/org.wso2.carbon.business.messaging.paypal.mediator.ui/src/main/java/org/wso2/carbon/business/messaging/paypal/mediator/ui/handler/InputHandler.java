/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.business.messaging.paypal.mediator.ui.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.synapse.MessageContext;
import org.wso2.carbon.business.messaging.paypal.mediator.ui.Input;

public class InputHandler {

    private Map<String, Object> inputRegistry = new HashMap<String, Object>();

    public void handle(List<Input> inputs) {
        if (!inputRegistry.isEmpty()) {
            inputRegistry.clear();
        }

        for (Input input : inputs) {
            handle(input, inputRegistry);
        }
    }

    private void handle(Input input, Map<String, Object> inputMap) {

        if (null != input.getType()) {
            Map<String, Object> innerInputMap = new HashMap<String, Object>();
            for (Input subInput : input.getSubInputs()) {
                handle(subInput, innerInputMap);
            }
            inputMap.put(input.getType(), innerInputMap);
        } else {
            inputMap.put(input.getName(), input);
        }
    }

    /**
     * @param key
     * @param synCtx
     * @return
     */
    public String lookupValue(MessageContext synCtx, String... keys) {

        return (String) lookupObject(synCtx, inputRegistry, 0, keys);
    }

    /**
     * @param inputMap
     * @param keyIndex
     * @param keys
     * @return
     */
    @SuppressWarnings("unchecked")
    private Object lookupObject(MessageContext synCtx,
                                Map<String, Object> inputMap, int keyIndex, String... keys) {
        Object sourceValue = "?";

        String key = keys[keyIndex];

        if (inputMap.containsKey(key)) {

            Object obj = inputMap.get(key);
            if (obj instanceof Input) {

                sourceValue = ((Input) obj).evaluate(synCtx);
                sourceValue = (null != sourceValue && !"".equals(sourceValue) ? sourceValue
                                                                              : "?");
            } else if (obj instanceof Map) {
                sourceValue = lookupObject(synCtx, (Map) obj, ++keyIndex, keys);
            }
        }

        return sourceValue;
    }

    public void print() {
        for (Entry<String, Object> entry : inputRegistry.entrySet()) {
            System.out.println(entry.getKey() + "-->" + entry.getValue());
        }
    }
}
