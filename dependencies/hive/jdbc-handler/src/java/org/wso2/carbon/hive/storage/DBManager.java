package org.wso2.carbon.hive.storage;


import org.apache.hadoop.mapred.lib.db.DBConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBManager {

    private Map<String, String> conf = new HashMap<String, String>();

    public void configureDB(String connectionUrl, String username, String password,
                            String driverClass) {

        conf.put(DBConfiguration.DRIVER_CLASS_PROPERTY, driverClass);
        conf.put(DBConfiguration.URL_PROPERTY, connectionUrl);
        if (username != null) {
            conf.put(DBConfiguration.USERNAME_PROPERTY, username);
        }
        if (password != null) {
            conf.put(DBConfiguration.PASSWORD_PROPERTY, password);
        }
    }


    public Connection getConnection() throws ClassNotFoundException, SQLException {

        Class.forName(conf.get(DBConfiguration.DRIVER_CLASS_PROPERTY));

        if (conf.get(DBConfiguration.USERNAME_PROPERTY) == null) {
            return DriverManager.getConnection(
                    conf.get(DBConfiguration.URL_PROPERTY));
        } else {
            return DriverManager.getConnection(
                    conf.get(DBConfiguration.URL_PROPERTY),
                    conf.get(DBConfiguration.USERNAME_PROPERTY),
                    conf.get(DBConfiguration.PASSWORD_PROPERTY));
        }
    }

    public boolean isTableExist(String tableName, Connection connection) throws SQLException {
        ResultSet tables = connection.getMetaData().getTables(null, null, tableName.toString(), null);
        if (tables.next()) {
            return true;
        } else {
            return false;
        }
    }


    protected String constructInsertQuery(String table, String[] fieldNames) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("Field names may not be null");
        }

        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(table);

        if (fieldNames[0] != null || fieldNames.length > 0) {
            query.append(" (");
            for (int i = 0; i < fieldNames.length; i++) {
                query.append(fieldNames[i]);
                if (i != fieldNames.length - 1) {
                    query.append(",");
                }
            }
            query.append(")");
        }
        query.append(" VALUES (");

        for (int i = 0; i < fieldNames.length; i++) {
            query.append("?");
            if (i != fieldNames.length - 1) {
                query.append(",");
            }
        }
        query.append(")");

        return query.toString();
    }


    public String constructSelectQuery(String tableName, List<String> fieldNames,
                                       String[] primaryFields) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("Field names may not be null");
        }
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ").append(tableName).append(" WHERE ");

        if (primaryFields != null && primaryFields.length > 0) {
            for (int i = 0; i < primaryFields.length; i++) {
                if (i > 0) {
                    query.append(" AND ").append(primaryFields[i]).append("=").append("?");
                } else {
                    query.append(primaryFields[i]).append("=").append("?");
                }
            }
        } else {
            query.append(fieldNames.get(0)).append("=").append("?");
        }
        return query.toString();
    }


    //When constructing the query we try to preserve the order
    public String constructUpdateQuery(String tableName, List<String> fieldNames,
                                       String[] primaryFields) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("Field names may not be null");
        }
        StringBuilder query = new StringBuilder();
        query.append("UPDATE ").append(tableName).append(" SET ");

        //If primary key fields are null, then we treat 1 field as the primary key
        if (primaryFields == null || primaryFields.length == 0) {
            primaryFields = new String[1];
            primaryFields[0] = fieldNames.get(0);
        }

        boolean isFirstFieldAppended = false;

        for (int fieldCount = 0; fieldCount < fieldNames.size(); fieldCount++) {
            boolean isPrimaryField = false;
            for (int primaryFieldCount = 0; primaryFieldCount < primaryFields.length; primaryFieldCount++) {
                if (fieldNames.get(fieldCount).equals(primaryFields[primaryFieldCount])) {
                    isPrimaryField = true;
                    break;
                }
            }
            if (!isPrimaryField) {
                if (!isFirstFieldAppended) {
                    query.append(fieldNames.get(fieldCount)).append("=").append("?");
                    isFirstFieldAppended = true;
                } else {
                    query.append(",").append(fieldNames.get(fieldCount)).append("=").append("?");
                }
            }
        }
        query.append(" WHERE ");
        for (int primaryKeyCount = 0; primaryKeyCount < primaryFields.length; primaryKeyCount++) {
            if (primaryKeyCount == 0) {
                query.append(primaryFields[primaryKeyCount]).append("=").append("?");
            } else {
                query.append(" AND ").append(primaryFields[primaryKeyCount]).append("=").append("?");
            }
        }
        return query.toString();
    }
}
