package org.apache.hadoop.hive.jdbc.storage.db;


import org.apache.commons.dbcp.BasicDataSource;
import org.apache.hadoop.hive.jdbc.storage.datasource.CarbonDataSourceFetcher;
import org.apache.hadoop.hive.jdbc.storage.utils.Commons;
import org.apache.hadoop.hive.jdbc.storage.utils.ConfigurationUtils;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBOperation {

    DatabaseProperties dbProperties;
    List<String> fieldNames;
    List<Object> values;
    Map<String,Object> fieldNamesAndValuesMap = new HashMap<String,Object>();
    Connection connection = null;

    public DBOperation(DatabaseProperties databaseProperties, Connection con) {
        dbProperties = databaseProperties;
        connection = con;
    }

    public DBOperation() {
    }

    public void writeToDB(MapWritable map) throws SQLException {

        fillFieldNamesAndValueObj(map);

        PreparedStatement statement = null;
        try {
            if (!dbProperties.isUpdateOnDuplicate()) { //Insert every record
                statement = insertData(statement);
            } else { //upsert
                if (dbProperties.getDbSpecificUpsertQuery() == null) {   //User haven't given the db specific upsert query
                    ResultSet resultSet = selectData(statement);
                    if (resultSet.next()) {     // If result is zero, then update
                        statement = updateData(statement);
                    } else {
                        statement = insertData(statement);
                    }
                } else {
                    String upsertQuery = dbProperties.getDbSpecificUpsertQuery();
                    statement = connection.prepareStatement(upsertQuery);
                    statement = setValuesForUpsertStatement(statement);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            statement.close();
        }
    }

    private void fillFieldNamesAndValueObj(MapWritable map) {

        fieldNames = new ArrayList<String>();
        values = new ArrayList<Object>();

        for (final Map.Entry<Writable, Writable> entry : map.entrySet()) {
            //If there is no mapping database table and metadata table are same
            if (dbProperties.getColumnMappingFields() == null) {
                fieldNames.add(entry.getKey().toString());
            }
            values.add(Commons.getObjectFromWritable(entry.getValue()));
        }
        if (dbProperties.getColumnMappingFields() != null) {
            fieldNames.addAll(Arrays.asList(dbProperties.getColumnMappingFields()));
        }
        for (int i = 0; i < fieldNames.size(); i++) {
            fieldNamesAndValuesMap.put(fieldNames.get(i), values.get(i));
        }

    }


    private PreparedStatement updateData(PreparedStatement statement) throws SQLException {
        QueryConstructor queryConstructor = new QueryConstructor();
        statement = connection.prepareStatement(queryConstructor.constructUpdateQuery(dbProperties.getTableName(),
                                                                                      fieldNames, dbProperties.getPrimaryFields()));
        statement = setValuesForUpdateStatement(statement);
        statement.executeUpdate();
        return statement;
    }

    private ResultSet selectData(PreparedStatement statement) throws SQLException {
        QueryConstructor queryConstructor = new QueryConstructor();
        statement = connection.prepareStatement(queryConstructor.constructSelectQuery(dbProperties.getTableName(),
                                                                                      fieldNames, dbProperties.getPrimaryFields()));
        statement = setValuesForWhereClause(statement);
        ResultSet resultSet = statement.executeQuery();
        return resultSet;
    }

    private PreparedStatement insertData(PreparedStatement statement) throws SQLException {
        QueryConstructor queryConstructor = new QueryConstructor();
        statement = connection.prepareStatement(queryConstructor.constructInsertQuery(dbProperties.getTableName(),
                                                                                      fieldNames.toArray(new String[fieldNames.size()])));
        statement = setValues(statement);
        statement.executeUpdate();
        return statement;
    }


    private PreparedStatement setValuesForUpsertStatement(PreparedStatement statement) {
        String[] valuesOrder = dbProperties.getUpsertQueryValuesOrder();
        if(valuesOrder==null){
            throw new IllegalArgumentException("You must supply both " +
                                               ConfigurationUtils.HIVE_JDBC_UPSERT_QUERY_VALUES_ORDER +
                                               " and " + ConfigurationUtils.HIVE_JDBC_OUTPUT_UPSERT_QUERY);
        }

        for (int valuesOrderCount = 0; valuesOrderCount < valuesOrder.length; valuesOrderCount++) {
            Object value=fieldNamesAndValuesMap.get(valuesOrder[valuesOrderCount].toLowerCase()); //Hive use lower case
            statement = Commons.assignCorrectObjectType(value, valuesOrderCount + 1, statement);
        }
        return statement;
    }

    private PreparedStatement setValuesForWhereClause(PreparedStatement statement) {

        String[] primaryFields = dbProperties.getPrimaryFields();
        if (primaryFields == null || primaryFields.length == 0) {
            primaryFields = new String[1];
            primaryFields[0] = fieldNames.get(0);
        }
        for (int fieldsCount = 0; fieldsCount < fieldNames.size(); fieldsCount++) {
            for (int primaryFieldsCount = 0; primaryFieldsCount < primaryFields.length; primaryFieldsCount++) {
                if (fieldNames.get(fieldsCount).equals(primaryFields[primaryFieldsCount])) {
                    statement = Commons.assignCorrectObjectType(values.get(fieldsCount), primaryFieldsCount + 1, statement);
                }
            }
        }
        return statement;
    }

    private PreparedStatement setValuesForUpdateStatement(PreparedStatement statement) {
        String[] primaryKeyFields = dbProperties.getPrimaryFields();
        if (primaryKeyFields == null) {
            primaryKeyFields = new String[1];
            primaryKeyFields[0] = fieldNames.get(0);
        }
        int counter = 0;
        for (int fieldCount = 0; fieldCount < fieldNames.size(); fieldCount++) {
            boolean isPrimaryField = false;
            for (int primaryFieldCount = 0; primaryFieldCount < primaryKeyFields.length; primaryFieldCount++) {
                if (fieldNames.get(fieldCount).equals(primaryKeyFields[primaryFieldCount])) {
                    statement = Commons.assignCorrectObjectType(values.get(fieldCount), fieldNames.size() - (primaryKeyFields.length - primaryFieldCount - 1),
                                                                statement);  //Primary key fields add in the where clause.
                    isPrimaryField = true;
                    break;
                }
            }
            if (!isPrimaryField) {
                counter++;
                statement = Commons.assignCorrectObjectType(values.get(fieldCount), counter, statement);
            }
        }
        return statement;
    }

    private PreparedStatement setValues(PreparedStatement statement) {
        for (int i = 0; i < values.size(); i++) {
            statement = Commons.assignCorrectObjectType(values.get(i), i + 1, statement);
        }
        return statement;
    }

    public boolean isTableExist(String tableName, Connection connection) throws SQLException {
        //This return all tables, we use this because it is not db specific, Passing table name doesn't
        //work with every database
        ResultSet tables = connection.getMetaData().getTables(null, null, "%" , null);
        while (tables.next()) {
            if(tables.getString(3).equalsIgnoreCase(tableName)){
                return true;
            }
        }
        return false;
    }


    public void createTableIfNotExist(Map<String, String> tableParameters) {
        String inputTable = tableParameters.get(DBConfiguration.INPUT_TABLE_NAME_PROPERTY);
        String outputTable = tableParameters.get(DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY);
        String createTableQuery = tableParameters.get(ConfigurationUtils.HIVE_JDBC_TABLE_CREATE_QUERY);
        /*If inputTable=null, then most probably it should be a output table.
         In input table table must already exist.
          */
        if(inputTable==null && (outputTable !=null || createTableQuery!=null)){
            DatabaseProperties dbProperties = new DatabaseProperties();
            dbProperties.setTableName(outputTable);
            dbProperties.setUserName(tableParameters.get(DBConfiguration.USERNAME_PROPERTY));
            dbProperties.setPassword(tableParameters.get(DBConfiguration.PASSWORD_PROPERTY));
            dbProperties.setConnectionUrl(tableParameters.get(DBConfiguration.URL_PROPERTY));
            dbProperties.setDriverClass(tableParameters.get(DBConfiguration.DRIVER_CLASS_PROPERTY));
            dbProperties.setDataSourceName(tableParameters.get(ConfigurationUtils.HIVE_PROP_CARBON_DS_NAME));

            if (dbProperties.getTableName() == null) {
                dbProperties.setTableName(Commons.extractingTableNameFromQuery(createTableQuery));
            }

            if(dbProperties.getConnectionUrl()==null && dbProperties.getDataSourceName()!=null){
                CarbonDataSourceFetcher carbonDataSourceFetcher = new CarbonDataSourceFetcher();
                Map<String, String> dataSource = carbonDataSourceFetcher.getCarbonDataSource(
                        dbProperties.getDataSourceName());
                dbProperties.setConnectionUrl(dataSource.get(DBConfiguration.URL_PROPERTY));
                dbProperties.setDriverClass(dataSource.get(DBConfiguration.DRIVER_CLASS_PROPERTY));
                dbProperties.setUserName(dataSource.get(DBConfiguration.USERNAME_PROPERTY));
                dbProperties.setPassword(dataSource.get(DBConfiguration.PASSWORD_PROPERTY));
                // We are not getting connection pool parameters,
                // because this is just for creating a table.
            }

            DBManager dbManager = new DBManager();
            BasicDataSource basicDataSource = createBasicDataSource(dbProperties);
            dbManager.setDataSource(basicDataSource);
            Connection connection = null;
            Statement statement = null;
            try {
                connection = dbManager.getConnection();
                if (!isTableExist(dbProperties.getTableName(), connection)) {
                    statement = connection.createStatement();
                    statement.executeUpdate(createTableQuery);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private BasicDataSource createBasicDataSource(DatabaseProperties dbProperties) {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setUrl(dbProperties.getConnectionUrl());
        basicDataSource.setDriverClassName(dbProperties.getDriverClass());
        basicDataSource.setUsername(dbProperties.getUserName());
        basicDataSource.setPassword(dbProperties.getPassword());
        return basicDataSource;
    }


    public int getTotalCount(String sql, Connection connection) throws SQLException {
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        int noOfRows=0;
        try {
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                noOfRows = resultSet.getInt(1);
            } else {
                throw new SQLException("Can't get total rows count using sql " + sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            resultSet.close();
            statement.close();
            connection.close();
        }
        return noOfRows;
    }

}
