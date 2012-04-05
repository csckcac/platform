/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.receiver.event;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.collections.map.UnmodifiableMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventProcessor {

    private RawEvent rawEvent;

    public EventProcessor(RawEvent rawEvent) {
        this.rawEvent = rawEvent;
    }

    public Map<String, String> processEvent() {
        Map<String, String> processedMap = new HashMap<String, String>();

        SOAPBody soapBody = rawEvent.getSOAPBody();
        Iterator eventChildren = soapBody.getFirstElement().getChildren();

        while (eventChildren.hasNext()) {
            Object object = eventChildren.next();
            if (object instanceof OMElement) {
                OMElement elem = (OMElement) object;

                // map event elements, to column keys and values
                // <key1>value1</key1> ----> { AnyRowKey : { key1 : value1 }
                processedMap.put(elem.getLocalName(), elem.getText());
            }
        }

        Map<String, String> mandatoryDataMap = rawEvent.getMandatoryDataMap();
        insertMandatoryFields(processedMap, mandatoryDataMap);
        return UnmodifiableMap.decorate(processedMap);
    }

    private void insertMandatoryFields(Map<String, String> processedMap, Map<String, String> mandatoryDataMap) {
        if (mandatoryDataMap == null) {
            return;
        }
        for (Map.Entry<String, String> mandatoryEntry : mandatoryDataMap.entrySet()) {
            if (!processedMap.containsKey(mandatoryEntry.getKey())) {
                processedMap.put(mandatoryEntry.getKey(), mandatoryEntry.getValue());
            }
        }
    }

}
