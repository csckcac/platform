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
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementException;
import org.wso2.carbon.appfactory.events.notification.Constants;
import org.wso2.carbon.appfactory.events.notification.internal.AppFactoryEventNotificationComponent;
import org.wso2.carbon.appfactory.events.notification.internal.EventRepository;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.ArrayList;
import java.util.Iterator;

public class EventNotificationService extends AbstractAdmin{

    private static Log log = LogFactory.getLog(EventNotificationService.class);
    private Gson gson = new Gson();
    
    public void publishEvent(JsonObject eventJson) {
        EventRepository.getInstance().addEvent(gson.fromJson(eventJson,EventBean.class));
    }
    
    public JsonObject[] getEventsForApplications(String[] appIDs, String userName) {
        ArrayList<JsonObject> events = new ArrayList<JsonObject>();
        ArrayList userApps = getAppsOfUser(userName);
        for(String appID : appIDs) {
            if(userApps.contains(appID)) {
                events.addAll(getEventsForID(appID));
            }
        }
        return events.toArray(new JsonObject[events.size()]);
    }

    private ArrayList getAppsOfUser(String userName) {
        ArrayList appList = new ArrayList();
        try {
            String[] userApps = AppFactoryEventNotificationComponent.getApplicationManagementService().getAllApplications(userName);
            for (String app : userApps) {
                appList.add(app);
            }
        } catch (ApplicationManagementException e) {
            log.error("Error while retrieving the application of user "+userName);
        }
        return appList;
    }

    private ArrayList<JsonObject> getEventsForID(String appID) {
        ArrayList<JsonObject> events = new ArrayList<JsonObject>();
        Iterator iterator = EventRepository.getInstance().getEventBuffer().iterator();
        while(iterator.hasNext()) {
            EventBean eventBean = (EventBean)iterator.next();
            if(appID.equals(eventBean.getApplicationId())) {
                events.add(getJsonObject(eventBean));
            }
        }
        return events;
    }

    private JsonObject getJsonObject(EventBean eventBean) {
        JsonObject eventJson = new JsonObject();
        eventJson.addProperty(Constants.APPLICATION_ID,eventBean.getApplicationId());
        eventJson.addProperty(Constants.EVENT,eventBean.getEvent());
        eventJson.addProperty(Constants.RESULT,eventBean.getResult());
        return eventJson;
    }
}
