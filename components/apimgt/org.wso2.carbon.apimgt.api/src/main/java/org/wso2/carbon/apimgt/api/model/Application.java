/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class represent the Application in api model
 */
@SuppressWarnings("unused")
public class Application {
    private int id;
    private String name;
    private Subscriber subscriber;
    private Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();

    public Application(String name, Subscriber subscriber) {
        this.name = name;
        this.subscriber = subscriber;
    }

    public String getName() {
        return name;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public Set<SubscribedAPI> getSubscribedAPIs() {
        return subscribedAPIs;
    }

    public void addSubscribedAPIs(Set<SubscribedAPI> subscribedAPIs) {
        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            subscribedAPI.setApplication(this);
        }
        this.subscribedAPIs.addAll(subscribedAPIs);
    }

    public void removeSubscribedAPIs(Set<SubscribedAPI> subscribedAPIs) {
        this.subscribedAPIs.removeAll(subscribedAPIs);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;
        return id == that.id && name.equals(that.name) && subscriber.equals(that.subscriber);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Integer.valueOf(id).hashCode() ;
        result = 31 * result + subscriber.hashCode();
        return result;
    }
}
