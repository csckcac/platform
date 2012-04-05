package org.wso2.carbon.bam.data.publisher.activity.service;

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.bam.data.publisher.activity.service.data.MessageData;

/**
 * Store XPath values
 * 
 */
public class XPathStore {
  
    public static Map<String, MessageData> xpMap = new HashMap<String, MessageData>();

    public static void storeMessageXPathData(String messageId, MessageData messageData) {
        xpMap.put(messageId, messageData);
    }

    public static MessageData getMessageXPathData(String messageId) {
        return xpMap.get(messageId);
    }

    public static void removeXPath(int key) {
        removeKey(key);
    }

    private static void removeKey(int key) {
        xpMap.remove(key);
    }
}
