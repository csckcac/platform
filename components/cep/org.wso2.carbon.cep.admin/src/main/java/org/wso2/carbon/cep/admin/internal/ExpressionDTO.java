package org.wso2.carbon.cep.admin.internal;

/**
 * This Class contains query text
 * */
public class ExpressionDTO {
    /**
     * Text of the expression
     * */
    private String text;

    /**
     * Type of the expression
     * inline|registry*/
    private String type;


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
}
