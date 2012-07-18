/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.carbon.appfactory.events.notification.service;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.events.notification.internal.EventRepository;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.ArrayList;
import java.util.Iterator;

public class EventNotificationService extends AbstractAdmin{

    private static Log log = LogFactory.getLog(EventNotificationService.class);

    public void publishEvent(EventBean event) {
        EventRepository.getInstance().addEvent(event);
    }
    
    public String[] getJSONEventsForAppIDs(String[] appIDs) {
        ArrayList<EventBean> events = new ArrayList<EventBean>();
        for(String appID : appIDs) {
            events.addAll(getEventForID(appID));
        }
        return getJSONStringArray(new EventBean[events.size()]);
    }
    
    private ArrayList<EventBean> getEventForID(String appID) {
        ArrayList<EventBean> events = new ArrayList<EventBean>();
        Iterator iterator = EventRepository.getInstance().getEventBuffer().iterator();
        while(iterator.hasNext()) {
            EventBean eventBean = (EventBean)iterator.next();
            if(appID.equals(eventBean.getApplicationId())) {
                events.add(eventBean);
            }
        }
        return events;
    }
    
    private String[] getJSONStringArray(EventBean[] events) {
        ArrayList<String> jsonEvents = new ArrayList<String>();
        for (EventBean event : events) {
            Gson gson = new Gson();
            jsonEvents.add(gson.toJson(event));
        }
        return jsonEvents.toArray(new String[jsonEvents.size()]);
    }
}
