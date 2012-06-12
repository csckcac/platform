package org.wso2.carbon.eventbridge.core.utils;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.wso2.carbon.eventbridge.core.beans.Event;
import org.wso2.carbon.eventbridge.core.exceptions.MalformedEventException;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class EventConverterUtils {

    private static Log log = LogFactory.getLog(EventConverterUtils.class);

    private static Gson gson = new Gson();
    /*
   Sample JSON string expected:
   [
     {
      "streamId" : "foo::1.0.0",
      "payloadData" : ["val1", "val2"] ,
      "metaData" : ["val1", "val2", "val3"] ,
      "correlationData" : ["val1", "val2"],
      "timeStamp" : 1312345432
     }
    ,
     {
      "streamId" : "bar::2.1.0",
      "payloadData" : ["val1", "val2"] ,
      "metaData" : ["val1", "val2", "val3"] ,
      "correlationData" : ["val1", "val2"]
     }

   ]
    */

//    public static List<Event> convertFromJson(String json) {
//
//
//        List<Event> eventList = new ArrayList<Event>();
//        try {
//            JSONArray eventObjects = new JSONArray(json);
//            for (int i = 0; i < eventObjects.length(); i++) {
////                Event event = gson.fromJson(json, Event.class);
//                Event event = new Event();
//                JSONObject eventObject = eventObjects.getJSONObject(i);
//                JSONArray metaJsonArray = eventObject.getJSONArray("meta");
//                JSONArray correlationJsonArray = eventObject.getJSONArray("correlation");
//                JSONArray eventJsonArray = eventObject.getJSONArray("event");
//
//                List<Object> metaEvents = new ArrayList<Object>();
//                List<Object> correlationEvents = new ArrayList<Object>();
//                List<Object> payloadEvents = new ArrayList<Object>();
//                for (int j = 0; j < metaJsonArray.length(); j++) {
//                    String value = metaJsonArray.getString(j);
//                    metaEvents.add(value);
//                }
//                for (int j = 0; j < correlationJsonArray.length(); j++) {
//                    String value = correlationJsonArray.getString(j);
//                    correlationEvents.add(value);
//                }
//                for (int j = 0; j < eventJsonArray.length(); j++) {
//                    String value = eventJsonArray.getString(j);
//                    payloadEvents.add(value);
//                }
//
//                long timestamp = 0;
//                try {
//                    timestamp = eventObject.getLong("timestamp");
//                } catch (JSONException e) {
//                    // ignore
//                }
//
//                String streamId = null;
//                try {
//                    streamId = eventObject.getString("streamId");
//                } catch (JSONException e) {
//
//                }
//                if (streamId == null || streamId.equals("")) {
//                    throw new MalformedEventException("Stream Id cannot be empty or null");
//                }
//
//                event.setCorrelationData(correlationEvents.toArray(new Object[correlationEvents.size()]));
//                event.setMetaData(metaEvents.toArray(new Object[metaEvents.size()]));
//                event.setPayloadData(payloadEvents.toArray(new Object[payloadEvents.size()]));
//                event.setTimeStamp(timestamp);
//                event.setStreamId(streamId);
//
//                eventList.add(event);
//            }
//        } catch (JSONException e) {
//            String errorMsg = "Error converting JSON to event, for JSON : " + json;
//            MalformedEventException malformedEventException = new MalformedEventException(errorMsg, e);
//            if (log.isDebugEnabled()) {
//                log.error(errorMsg, malformedEventException);
//            } else {
//                log.error(errorMsg);
//            }
//            throw malformedEventException;
//        }
//        return eventList;
//    }


    public static List<Event> convertFromJson(String json) {
        List<Event> eventList = new ArrayList<Event>();
        try {
            JSONArray eventObjects = new JSONArray(json);
            for (int i = 0; i < eventObjects.length(); i++) {
                Event event = gson.fromJson(eventObjects.get(i).toString(), Event.class);
                if (event.getStreamId() == null || event.getStreamId().equals("")) {
                    String errorMsg = "Stream Id cannot be null or empty, for JSON : " + eventObjects.get(i).toString();
                    MalformedEventException malformedEventException = new MalformedEventException();
                    if (log.isDebugEnabled()) {
                        log.error(errorMsg, malformedEventException);
                    } else {
                        log.error(errorMsg);
                    }
                    throw malformedEventException;
                }
                eventList.add(event);
            }
        } catch (JSONException e) {
            String errorMsg = "Error converting JSON to event, for JSON : " + json;
            MalformedEventException malformedEventException = new MalformedEventException(errorMsg, e);
            if (log.isDebugEnabled()) {
                log.error(errorMsg, malformedEventException);
            } else {
                log.error(errorMsg);
            }
            throw malformedEventException;
        }
        return eventList;
    }

    public static List<Event> convertFromJson(String json, String streamName, String version) {
        if ((streamName == null || streamName.equals("")) || ((version == null) || (version.equals("")))) {
            String errorMsg = "Stream name or version cannot be null or empty";
            MalformedEventException malformedEventException = new MalformedEventException();
            if (log.isDebugEnabled()) {
                log.error(errorMsg, malformedEventException);
            } else {
                log.error(errorMsg);
            }
            throw malformedEventException;
        }
        List<Event> eventList = new ArrayList<Event>();
        try {
            JSONArray eventObjects = new JSONArray(json);
            for (int i = 0; i < eventObjects.length(); i++) {
                Event event = gson.fromJson(eventObjects.get(i).toString(), Event.class);
                event.setStreamId(EventBridgeUtils.constructStreamKey(streamName, version));
                eventList.add(event);
            }
        } catch (JSONException e) {
            String errorMsg = "Error converting JSON to event, for JSON : " + json;
            MalformedEventException malformedEventException = new MalformedEventException(errorMsg, e);
            if (log.isDebugEnabled()) {
                log.error(errorMsg, malformedEventException);
            } else {
                log.error(errorMsg);
            }
            throw malformedEventException;
        }
        return eventList;

    }

}
