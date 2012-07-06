package org.wso2.carbon.hadoop.hive.jdbc.storage.db;


import org.wso2.carbon.hadoop.hive.jdbc.storage.input.JDBCSplit;

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

    public String constructSelectQueryForReading(DatabaseProperties dbProperties, JDBCSplit split,
                                                 DatabaseType databaseType) {
        String query = null;
        switch (databaseType) {

            case MYSQL:
            case H2:
            case POSTGRESQL:
                query = getQueryForMySql(dbProperties,split);
                break;
            case ORACLE:
                query = getQueryForOracle(dbProperties,split);
                break;
            case SQLSERVER:
                query = getQueryForMsSql(dbProperties,split);
                break;
        }
        return query;
    }

    /**
     * Sample query is like this -
     SELECT * FROM (
     SELECT TOP 150 * FROM (
     SELECT TOP 200 fields
     FROM table
     ORDER BY column1  ASC)
     ORDER by column1 DESC)
     ORDER by column1 ASC
     * @param dbProperties
     * @param split
     * @return
     */
    private String getQueryForMsSql(DatabaseProperties dbProperties, JDBCSplit split) {
        String[] fieldNames = dbProperties.getFieldsNames();
        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM ( ");
        query.append("SELECT TOP ").append(split.getLength()).append(" * FROM ( ");
        query.append("SELECT TOP ").append(split.getEnd()).append(" ");
        for (int i = 0; i < fieldNames.length; i++) {
            query.append(fieldNames[i]);
            if (i != fieldNames.length - 1) {
                query.append(", ");
            }
        }
        query.append(" FROM ");
        query.append(dbProperties.getTableName());
        query.append(" ORDER BY ").append(fieldNames[0]).append(" ASC ").append(")");
        query.append(" ORDER BY ").append(fieldNames[0]).append(" DESC ").append(")");
        query.append(" ORDER BY ").append(fieldNames[0]).append(" ASC");
        return query.toString();
    }

    private String getQueryForMySql(DatabaseProperties dbProperties, JDBCSplit split) {
        StringBuilder query = new StringBuilder();
        String[] fieldNames = dbProperties.getFieldsNames();

        query.append("SELECT ");

        for (int i = 0; i < fieldNames.length; i++) {
            query.append(fieldNames[i]);
            if (i != fieldNames.length - 1) {
                query.append(", ");
            }
        }

        query.append(" FROM ").append(dbProperties.getTableName());
/*        query.append(" AS ").append(dbProperties.getTableName()); //in hsqldb this is necessary*/

        query.append(" LIMIT ").append(split.getLength());
        query.append(" OFFSET ").append(split.getStart());
        return query.toString();
    }

    /**
     * sample query is like this -
     SELECT * FROM (
     SELECT ROW_NUMBER() OVER(ORDER BY column1) LINENUM, column1, column2
     FROM MyTable
     ORDER BY column1
     )
     WHERE LINENUM BETWEEN 100 AND 200;
     * @param dbProperties
     * @param split
     * @return
     */
    private String getQueryForOracle(DatabaseProperties dbProperties, JDBCSplit split){
        String[] fieldNames = dbProperties.getFieldsNames();
        StringBuilder query = new StringBuilder();

        query.append("SELECT * FROM ( SELECT ROW_NUMBER() OVER( ORDER BY ");
        query.append(fieldNames[0]);
        query.append(" ) LINENUM, ");
        for (int i = 0; i < fieldNames.length; i++) {
            query.append(fieldNames[i]);
            if (i != fieldNames.length - 1) {
                query.append(", ");
            }
        }
        query.append(" FROM ");
        query.append(dbProperties.getTableName());
        query.append(" ORDER BY ");
        query.append(fieldNames[0]);
        query.append(" )");
        query.append(" WHERE LINENUM BETWEEN ");
        query.append(split.getStart());
        query.append(" AND ");
        query.append(split.getEnd());
        return query.toString();
    }
}
