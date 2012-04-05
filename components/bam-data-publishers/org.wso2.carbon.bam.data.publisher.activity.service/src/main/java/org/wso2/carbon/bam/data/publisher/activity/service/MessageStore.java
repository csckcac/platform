package org.wso2.carbon.bam.data.publisher.activity.service;

import java.util.HashMap;
import java.util.Map;

/*
 * This class is used to store the messageId's of each message in order to keep track 
 * whether the message execution is success or not.
 */
public class MessageStore {
    public static Map<String, Map<String, String>> messageActivityMap = new HashMap<String, Map<String, String>>();

    /*
     * store msg with AID
     */
    public static void storeMessageAct(String messageId, String activityId, String activityName, String activityDes,
        String activityProperty, String propertyValue) {
        Map<String, String> act = new HashMap<String, String>();
        act.put("ActivityName", activityName);
        act.put("ActivityDescription", activityDes);
        act.put("ActivityID", activityId);
        act.put("ActivityProperty", activityProperty);
        act.put("ActivityPropertyValue", propertyValue);
        
        messageActivityMap.put(messageId, act);
    }

    /*
     * get AID for the poarticular messageID
     */
    public static Map<String, String> getMessageAct(String messageId) {
        return messageActivityMap.get(messageId);
    }
  
    /*
     * delete msg entry from map
     */
    public static void removeMessage(String messageId) {
        messageActivityMap.remove(messageId);
    }
}
