package org.wso2.carbon.hive.storage;


public class DatabaseProperties {
    
    private String connectionUrl;
    private String userName;
    private String password;
    private String driverClass;
    private String tableName;
    private String[] fieldsNames;
    private String[] primaryFields;
    private String[] columnMappingFields;
    private boolean updateOnDuplicate;

    public String[] getColumnMappingFields() {
        return columnMappingFields;
    }

    public void setColumnMappingFields(String[] columnMappingFields) {
        this.columnMappingFields = columnMappingFields;
    }

    public String[] getPrimaryFields() {
        return primaryFields;
    }

    public void setPrimaryFields(String[] primaryFields) {
        this.primaryFields = primaryFields;
    }

    public boolean isUpdateOnDuplicate() {
        return updateOnDuplicate;
    }

    public void setUpdateOnDuplicate(boolean updateOnDuplicate) {
        this.updateOnDuplicate = updateOnDuplicate;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String[] getFieldsNames() {
        return fieldsNames;
    }

    public void setFieldsNames(String[] fieldsNames) {
        this.fieldsNames = fieldsNames;
    }
}
