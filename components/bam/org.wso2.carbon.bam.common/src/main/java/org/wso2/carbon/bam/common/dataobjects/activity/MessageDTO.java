package org.wso2.carbon.bam.common.dataobjects.activity;

import java.util.Calendar;

/*
 * Message class
 */
public class MessageDTO {
    private int messageKeyId;
    private int operationId;
    private String messageId;
    private int activityKeyId;
    private Calendar timestamp;
    private String ipaddress;
    private String useragent;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getMessageKeyId() {
        return messageKeyId;
    }

    public void setMessageKeyId(int messageKeyId) {
        this.messageKeyId = messageKeyId;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public int getActivityKeyId() {
        return activityKeyId;
    }

    public void setActivityKeyId(int activityKeyId) {
        this.activityKeyId = activityKeyId;
    }

    public Calendar getTimeStamp() {
        return timestamp;
    }

    public void setTimeStamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public String getIPAddress() {
        return ipaddress;
    }

    public void setIPAddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getUserAgent() {
        return useragent;
    }

    public void setUserAgent(String useragent) {
        this.useragent = useragent;
    }
}
