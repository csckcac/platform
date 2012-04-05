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

import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.collections.map.UnmodifiableMap;

import java.util.Map;

/**
 * A bean that holds the raw event body and other mandatory fields
 */
public class RawEvent {

    private Map<String, String> mandatoryDataMap;

    private SOAPBody soapBody;

    public RawEvent(Map<String, String> mandatoryDataMap, SOAPBody soapBody) {
        this.mandatoryDataMap = UnmodifiableMap.decorate(mandatoryDataMap);
        this.soapBody = soapBody;
    }

    public Map<String, String> getMandatoryDataMap() {
        return mandatoryDataMap;
    }

    public SOAPBody getSOAPBody() {
        return soapBody;
    }

}
