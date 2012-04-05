package org.wso2.carbon.bam.common.dataobjects.activity;

public class PropertyFilterDTO {

    private int id;
    private String serverName;
    private int serverId;
    private String expressionKey;
    private String alias;
    private String expression;
    private String[] namespaces;


    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getExpressionKey() {
        return expressionKey;
    }

    public void setExpressionKey(String expressionKey) {
        this.expressionKey = expressionKey;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String[] getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(String[] namespaces) {
        this.namespaces = namespaces;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

}
