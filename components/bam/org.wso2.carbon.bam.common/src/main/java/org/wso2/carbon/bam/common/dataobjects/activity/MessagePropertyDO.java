package org.wso2.carbon.bam.common.dataobjects.activity;

public class MessagePropertyDO {
    private int messagePropertyKeyId; // <-- primaryKey
    private String value;
    private String key;

    private String keyArray[]; 
    private String valueArray[];
    
    // we need these variables when we store the data @ DB.
    private int serverId;
    private int serviceId;
    private int operationId;
    private int messageKeyId;
    private int activityKeyId;

    public MessagePropertyDO() {
        this.messagePropertyKeyId = -1;
        this.messageKeyId = -1;
        this.operationId = -1;
        this.activityKeyId = -1;
        this.serverId = -1;
        this.serviceId = -1;
    }

    public MessagePropertyDO(String key, String value) {

        this.key = key;
        this.value = value;

    }

    public int getMessagePropertyKeyId() {
        return messagePropertyKeyId;
    }

    public void setMessagePropertyKeyId(int messagePropertyKeyId) {
        this.messagePropertyKeyId = messagePropertyKeyId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public int getMessageKeyId() {
        return messageKeyId;
    }

    public void setMessageKeyId(int messageKeyId) {
        this.messageKeyId = messageKeyId;
    }

    public int getActivityKeyId() {
        return activityKeyId;
    }

    public void setActivityKeyId(int activityKeyId) {
        this.activityKeyId = activityKeyId;
    }
    public String[] getKeyArray() {
        return keyArray;
    }

    public void setKeyArray(String[] keyArray) {
        this.keyArray = keyArray;
    }

    public String[] getValueArray() {
        return valueArray;
    }

    public void setValueArray(String[] valueArray) {
        this.valueArray = valueArray;
    }
}
