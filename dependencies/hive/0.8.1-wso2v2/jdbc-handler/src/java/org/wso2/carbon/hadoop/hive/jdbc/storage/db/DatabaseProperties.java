package org.wso2.carbon.hadoop.hive.jdbc.storage.db;


import java.util.Map;

public class DatabaseProperties {

    private String connectionUrl;
    private String userName;
    private String password;
    private String driverClass;
    private String tableName;
    private String dbSpecificUpsertQuery;
    private String[] upsertQueryValuesOrder;
    private String[] fieldsNames;
    private String[] primaryFields;
    private String[] columnMappingFields;
    private Map<String,String> inputColumnMappingFields;
    private boolean updateOnDuplicate;
    private String dataSourceName;

    public Map<String, String> getInputColumnMappingFields() {
        return inputColumnMappingFields;
    }

    public void setInputColumnMappingFields(Map<String, String> inputColumnMappingFields) {
        this.inputColumnMappingFields = inputColumnMappingFields;
    }

    public String getDbSpecificUpsertQuery() {
        return dbSpecificUpsertQuery;
    }

    public void setDbSpecificUpsertQuery(String dbSpecificUpsertQuery) {
        this.dbSpecificUpsertQuery = dbSpecificUpsertQuery;
    }

    public String[] getUpsertQueryValuesOrder() {
        return upsertQueryValuesOrder;
    }

    public void setUpsertQueryValuesOrder(String[] upsertQueryValuesOrder) {
        this.upsertQueryValuesOrder = upsertQueryValuesOrder;
    }

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
        if(connectionUrl!=null){
           connectionUrl=  connectionUrl.replaceAll(" ", "");
        }
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

    public void setDataSourceName(String wso2CarbonDataSourceName) {
        dataSourceName = wso2CarbonDataSourceName;
    }

    public String getDataSourceName(){
        return dataSourceName;
    }
}
