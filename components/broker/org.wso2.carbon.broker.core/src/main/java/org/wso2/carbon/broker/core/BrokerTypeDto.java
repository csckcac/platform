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

import java.util.ArrayList;
import java.util.List;

/**
 * this class is used to transfer the broker proxy type details to the UI. UI renders the
 * properties according to the properties specified here.
 */
public class BrokerTypeDto {

    /**
     * logical name of this type
     */
    private String name;

    /**
     * properties which are needed to connect to actual broker instance.
     */
    private List<Property> propertyList;

    public BrokerTypeDto() {
        this.propertyList = new ArrayList<Property>();
    }

    public void addProperty(Property property) {
        this.propertyList.add(property);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Property> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }


}
