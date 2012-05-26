package org.wso2.carbon.hive.storage;


import com.google.common.collect.ImmutableSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;

import java.util.*;

public class ConfigurationUtils {

    public static final String HIVE_JDBC_UPDATE_ON_DUPLICATE = "hive.jdbc.update.on.duplicate";
    public static final String HIVE_JDBC_PRIMARY_KEY_FIELDS = "hive.jdbc.primary.key.fields";
    public static final String HIVE_JDBC_COLUMNS_MAPPING = "hive.jdbc.columns.mapping";

    public static final String HIVE_JDBC_TABLE_CREATE_QUERY = "hive.jdbc.table.create.query";
    public static final String HIVE_JDBC_OUTPUT_UPSERT_QUERY = "hive.jdbc.output.upsert.query";
    public static final String HIVE_JDBC_UPSERT_QUERY_VALUES_ORDER = "hive.jdbc.upsert.query.values.order";


    public static final Set<String> ALL_PROPERTIES = ImmutableSet.of(
            DBConfiguration.DRIVER_CLASS_PROPERTY,
            DBConfiguration.USERNAME_PROPERTY,
            DBConfiguration.PASSWORD_PROPERTY,
            DBConfiguration.URL_PROPERTY,
            DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY,
            DBConfiguration.OUTPUT_FIELD_NAMES_PROPERTY,
            HIVE_JDBC_UPDATE_ON_DUPLICATE,
            HIVE_JDBC_PRIMARY_KEY_FIELDS,
            HIVE_JDBC_COLUMNS_MAPPING,
            HIVE_JDBC_TABLE_CREATE_QUERY,
            HIVE_JDBC_OUTPUT_UPSERT_QUERY,
            HIVE_JDBC_UPSERT_QUERY_VALUES_ORDER
    );


    public static void copyJDBCProperties(Properties from, Map<String, String> to) {
        for (String key : ALL_PROPERTIES) {
            String value = from.getProperty(key);
            if (value != null) {
                to.put(key, value);
            }
        }
    }

    public final static String getTableName(Configuration conf) {
        String tableName = conf.get(DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY);
        if (tableName == null) {
            String createTableQuery = conf.get(HIVE_JDBC_TABLE_CREATE_QUERY);
            tableName = extractingTableNameFromQuery(tableName, createTableQuery);
        }
        return tableName;
    }

    public final static String getConnectionUrl(Configuration conf) {
        return conf.get(DBConfiguration.URL_PROPERTY);
    }

    public final static String getDriverClass(Configuration conf) {
        return conf.get(DBConfiguration.DRIVER_CLASS_PROPERTY);
    }

    public final static String getDatabaseUserName(Configuration conf) {
        return conf.get(DBConfiguration.USERNAME_PROPERTY);
    }

    public final static String getDatabasePassword(Configuration conf) {
        return conf.get(DBConfiguration.PASSWORD_PROPERTY);
    }

    public final static String[] getFieldNames(Configuration conf) {
        String[] fieldNames = null;
        String outputFieldNames = conf.get(DBConfiguration.OUTPUT_FIELD_NAMES_PROPERTY);
        if (outputFieldNames != null) {
            fieldNames = outputFieldNames.split(",");
        }
        return fieldNames;
    }

    public final static boolean isUpdateOnDuplicate(Configuration conf) {
        return Boolean.parseBoolean(conf.get(HIVE_JDBC_UPDATE_ON_DUPLICATE));
    }

    public final static String[] getPrimaryKeyFields(Configuration conf) {
        String primaryKeyFields = conf.get(HIVE_JDBC_PRIMARY_KEY_FIELDS);
        if (primaryKeyFields != null) {
            return primaryKeyFields.split(",");
        }
        return null;
    }

    public final static String[] getColumnMappingFields(Configuration conf) {
        String mappingFields = conf.get(HIVE_JDBC_COLUMNS_MAPPING);
        if (mappingFields != null) {
            return mappingFields.split(",");
        }
        return null;
    }


    //If user has given the query for creating table, we don't need to ask the table name again. Get the table name from the query
    public final static String extractingTableNameFromQuery(String tableName,
                                                            String createTableQuery) {
        if (createTableQuery != null) {
            List<String> queryList = Arrays.asList(createTableQuery.split(" "));
            Iterator<String> iterator = queryList.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equalsIgnoreCase("create")) {
                    while (iterator.hasNext()) {
                        if (iterator.next().equalsIgnoreCase("table")) {
                            while (iterator.hasNext()) {
                                String nextString = iterator.next();
                                if (!nextString.equalsIgnoreCase("")) {
                                    tableName = nextString;
                                    return tableName;
                                }
                            }
                        }
                    }
                }

            }
        } else {
            throw new IllegalArgumentException("You should provide at least " +
                                               DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY + " or " +
                                               ConfigurationUtils.HIVE_JDBC_TABLE_CREATE_QUERY + " property.");
        }
        return tableName;
    }

    public static String getDbSpecificUpdateQuery(JobConf conf) {
        return conf.get(ConfigurationUtils.HIVE_JDBC_OUTPUT_UPSERT_QUERY);
    }

    public static String[] getUpsertQueryValuesOrder(JobConf conf) {
        String valuesOrder = conf.get(ConfigurationUtils.HIVE_JDBC_UPSERT_QUERY_VALUES_ORDER);
        String[] order = null;
        if (valuesOrder != null) {
            valuesOrder= valuesOrder.trim();
            order = valuesOrder.split(",");
        }
        return order;
    }
}
