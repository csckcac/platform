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

package org.wso2.carbon.statistics.transport.services.util;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.TransportListener;
import org.apache.axis2.transport.TransportSender;

import java.util.Map;
import java.util.Iterator;

/**
 * Singleton class which is used to store ConfigurationContext of the bundle.
 */
public class ConfigHolder {
    private static ConfigHolder configHolderSingleton = new ConfigHolder();

    private ConfigurationContext configContext = null;

    private ConfigHolder() {
    }

    public static ConfigHolder getInstance() {
        return configHolderSingleton;
    }

    public void setConfigurationContext(ConfigurationContext configContext) {
        this.configContext = configContext;
    }

    public String[] getAllTransports() {
        Map map = configContext.getAxisConfiguration().getTransportsIn();
        Iterator iterator = map.keySet().iterator();
        String array[] = new String[map.size()];
        int count = 0;

        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            array[count] = key;
            count++;
        }

        return array;
    }

    public TransportListener getHttpTransportListener() {
        return configContext.getAxisConfiguration().getTransportIn("http").getReceiver();
    }

    public TransportListener getTransportListener(String transportName) {
        return configContext.getAxisConfiguration().getTransportIn(transportName).getReceiver();
    }

    public TransportSender getTransportSender(String transportName) {
        return configContext.getAxisConfiguration().getTransportOut(transportName).getSender();
    }

    public String getTransportListenerClassName(String transportName) {
        return configContext.getAxisConfiguration().getTransportIn(transportName).getReceiver().
                getClass().getName();
    }

    public String getTransportSenderClassName(String transportName) {
        return configContext.getAxisConfiguration().getTransportOut(transportName).getSender().
                getClass().getName();
    }

}
