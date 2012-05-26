package org.wso2.carbon.hive.storage;


import org.apache.hadoop.hive.ql.exec.FileSinkOperator.RecordWriter;
import org.apache.hadoop.hive.serde2.io.ByteWritable;
import org.apache.hadoop.hive.serde2.io.ShortWritable;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCWriter implements RecordWriter {

    Connection connection = null;
    DatabaseProperties dbProperties;
    DBManager dbManager;
    List<String> fieldNames;
    List<Object> values;
    Map<String,Object> fieldNamesAndValuesMap = new HashMap<String,Object>();

    public JDBCWriter(DatabaseProperties databaseProperties)
            throws ClassNotFoundException, SQLException {
        dbProperties = databaseProperties;
        dbManager = new DBManager();
        dbManager.configureDB(databaseProperties.getConnectionUrl(), databaseProperties.getUserName(),
                              databaseProperties.getPassword(), databaseProperties.getDriverClass());
        connection = dbManager.getConnection();
    }

    public void write(Writable writable) throws IOException {
        MapWritable map = (MapWritable) writable;

        fieldNames = new ArrayList<String>();
        values = new ArrayList<Object>();

        for (final Map.Entry<Writable, Writable> entry : map.entrySet()) {
            //If there is no mapping database table and metadata table are same
            if (dbProperties.getColumnMappingFields() == null) {
                fieldNames.add(entry.getKey().toString());
            }
            values.add(getObjectFromWritable(entry.getValue()));
        }
        if (dbProperties.getColumnMappingFields() != null) {
            fieldNames.addAll(Arrays.asList(dbProperties.getColumnMappingFields()));
        }
        for (int i=0; i<fieldNames.size();i++){
            fieldNamesAndValuesMap.put(fieldNames.get(i),values.get(i));
        }
        try {
            writeToDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void writeToDB() throws SQLException {

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


    private PreparedStatement updateData(PreparedStatement statement) throws SQLException {
        statement = connection.prepareStatement(dbManager.constructUpdateQuery(dbProperties.getTableName(),
                                                                               fieldNames, dbProperties.getPrimaryFields()));
        statement = setValuesForUpdateStatement(statement);
        statement.executeUpdate();
        return statement;
    }

    private ResultSet selectData(PreparedStatement statement) throws SQLException {
        statement = connection.prepareStatement(dbManager.constructSelectQuery(dbProperties.getTableName(),
                                                                               fieldNames, dbProperties.getPrimaryFields()));
        statement = setValuesForWhereClause(statement);
        ResultSet resultSet = statement.executeQuery();
        return resultSet;
    }

    private PreparedStatement insertData(PreparedStatement statement) throws SQLException {
        statement = connection.prepareStatement(dbManager.constructInsertQuery(dbProperties.getTableName(),
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
            statement = assignCorrectObjectType(value,valuesOrderCount+1,statement);
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
                    statement = assignCorrectObjectType(values.get(fieldsCount), primaryFieldsCount + 1, statement);
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
                    statement = assignCorrectObjectType(values.get(fieldCount), fieldNames.size() - (primaryKeyFields.length - primaryFieldCount - 1),
                                                        statement);  //Primary key fields add in the where clause.
                    isPrimaryField = true;
                    break;
                }
            }
            if (!isPrimaryField) {
                counter++;
                statement = assignCorrectObjectType(values.get(fieldCount), counter, statement);
            }
        }
        return statement;
    }

    private PreparedStatement setValues(PreparedStatement statement) {
        for (int i = 0; i < values.size(); i++) {
            statement = assignCorrectObjectType(values.get(i), i + 1, statement);
        }
        return statement;
    }

    /**
     * Set values for the prepared statement
     *
     * @param value     value
     * @param position
     * @param statement prepared statement
     * @return
     */
    private PreparedStatement assignCorrectObjectType(Object value, int position,
                                                      PreparedStatement statement) {
        try {
            if (value instanceof Integer) {
                statement.setInt(position, (Integer) value);
            } else if (value instanceof Short) {
                statement.setShort(position, (Short) value);
            } else if (value instanceof Byte) {
                statement.setByte(position, (Byte) value);
            } else if (value instanceof Boolean) {
                statement.setBoolean(position, (Boolean) value);
            } else if (value instanceof Long) {
                statement.setLong(position, (Long) value);
            } else if (value instanceof Float) {
                statement.setFloat(position, (Float) value);
            } else if (value instanceof Double) {
                statement.setDouble(position, (Double) value);
            } else {
                statement.setString(position, value.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return statement;

    }

    private Object getObjectFromWritable(Writable w) {
        if (w instanceof IntWritable) {
            // int
            return ((IntWritable) w).get();
        } else if (w instanceof ShortWritable) {
            // short
            return ((ShortWritable) w).get();
        } else if (w instanceof ByteWritable) {
            // byte
            return ((ByteWritable) w).get();
        } else if (w instanceof BooleanWritable) {
            // boolean
            return ((BooleanWritable) w).get();
        } else if (w instanceof LongWritable) {
            // long
            return ((LongWritable) w).get();
        } else if (w instanceof FloatWritable) {
            // float
            return ((FloatWritable) w).get();
        } else if (w instanceof org.apache.hadoop.hive.serde2.io.DoubleWritable) {
            // double
            return ((org.apache.hadoop.hive.serde2.io.DoubleWritable) w).get();
        } else if (w instanceof NullWritable) {
            //null
            return null;
        } else {
            // treat as string
            return w.toString();
        }

    }

    public void close(boolean b) throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
