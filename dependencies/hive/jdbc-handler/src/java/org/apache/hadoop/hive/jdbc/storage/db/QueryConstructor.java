package org.apache.hadoop.hive.jdbc.storage.db;


import org.apache.hadoop.hive.jdbc.storage.input.JDBCSplit;

import java.util.List;

public class QueryConstructor {



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

    public String constructInsertQuery(String table, String[] fieldNames) {
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

    public String constructSelectQueryForReading(DatabaseProperties dbProperties, JDBCSplit split) {
        StringBuilder query = new StringBuilder();

        query.append("SELECT ");

        String[] fieldNames = dbProperties.getFieldsNames();
        for (int i = 0; i < fieldNames.length; i++) {
            query.append(fieldNames[i]);
            if (i != fieldNames.length - 1) {
                query.append(", ");
            }
        }

        query.append(" FROM ").append(dbProperties.getTableName());
        query.append(" AS ").append(dbProperties.getTableName()); //in hsqldb this is necessary

        query.append(" LIMIT ").append(split.getLength());
        query.append(" OFFSET ").append(split.getStart());
        return query.toString();
    }

    public String constructCountQuery(DatabaseProperties dbProperties) {

        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(");
        if(dbProperties.getFieldsNames()!=null){
            query.append(dbProperties.getFieldsNames()[0]);
        } else {
             query.append("*");
        }
        query.append(") FROM ").append(dbProperties.getTableName());
        return query.toString();
    }
}
