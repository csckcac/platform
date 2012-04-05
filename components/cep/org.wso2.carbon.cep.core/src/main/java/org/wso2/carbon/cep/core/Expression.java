package org.wso2.carbon.cep.core;

/**
 * This Class contains query text
 * */
public class Expression {
    /**
     * Text of the expression
     * */
    private String text;
    /**
     * Type of the expression
     * type : inline | registry
     * */
    private String type;

    private String listenerName;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getListenerName() {
        return listenerName;
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }
}
