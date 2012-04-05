package org.wso2.carbon.messagebox;


import java.util.HashMap;
import java.util.Map;

public class SQSMessage {

    private String body;

    private String md5ofMessageBody;

    private String messageId;

    private String receiptHandle;

    private Map<String, String> attribute;

    private long defaultVisibilityTimeout;

    private long receivedTimeStamp;

    public SQSMessage() {
        attribute = new HashMap<String, String>();
    }

    public void setReceivedTimeStamp(long receivedTimeStamp) {
        this.receivedTimeStamp = receivedTimeStamp;
    }

    public long getDefaultVisibilityTimeout() {

        return defaultVisibilityTimeout;
    }

    public long getReceivedTimeStamp() {
        return receivedTimeStamp;
    }

    public void setDefaultVisibilityTimeout(long defaultVisibilityTimeout) {
        this.defaultVisibilityTimeout = defaultVisibilityTimeout;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMd5ofMessageBody() {
        return md5ofMessageBody;
    }

    public void setMd5ofMessageBody(String md5ofMessageBody) {
        this.md5ofMessageBody = md5ofMessageBody;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public void setReceiptHandle(String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }

    public Map<String, String> getAttribute() {
        return attribute;
    }

    public void setAttribute(Map<String, String> attribute) {
        this.attribute = attribute;
    }

    public void setFirstReceivedTimestamp() {
        if (!attribute.containsKey(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_FIRST_RECEIVE_TIMESTAMP)) {
            attribute.put(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_FIRST_RECEIVE_TIMESTAMP, Long.toString(System.currentTimeMillis()));
        }
    }

    public void setReceiveCount() {
        int receivedCount;
        if (attribute.get(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT) != null) {
            receivedCount = Integer.parseInt(attribute.get(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT));
            receivedCount++;
            attribute.put(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT, Integer.toString(receivedCount));
        } else {
            attribute.put(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_RECEIVE_COUNT, "1");
        }
    }

    public void setSentTimestamp() {
        attribute.put(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENT_TIMESTAMP, Long.toString(System.currentTimeMillis()));
    }

    public void setSenderId(String sender) {
        attribute.put(MessageBoxConstants.SQS_MESSAGE_ATTRIBUTE_SENDER_ID, sender);
    }
}