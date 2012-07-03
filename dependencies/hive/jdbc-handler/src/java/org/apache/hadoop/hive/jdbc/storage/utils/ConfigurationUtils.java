package org.apache.hadoop.hive.jdbc.storage.utils;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.jdbc.storage.datasource.BasicDataSourceConstants;
import org.apache.hadoop.hive.jdbc.storage.datasource.CarbonDataSourceFetcher;
import org.apache.hadoop.hive.metastore.api.Constants;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;

import java.util.Map;
import java.util.Properties;

public class ConfigurationUtils {

    public static final String HIVE_JDBC_UPDATE_ON_DUPLICATE = "hive.jdbc.update.on.duplicate";
    public static final String HIVE_JDBC_PRIMARY_KEY_FIELDS = "hive.jdbc.primary.key.fields";
    public static final String HIVE_JDBC_COLUMNS_MAPPING = "hive.jdbc.columns.mapping";

    public static final String HIVE_JDBC_TABLE_CREATE_QUERY = "hive.jdbc.table.create.query";
    public static final String HIVE_JDBC_OUTPUT_UPSERT_QUERY = "hive.jdbc.output.upsert.query";
    public static final String HIVE_JDBC_UPSERT_QUERY_VALUES_ORDER = "hive.jdbc.upsert.query.values.order";
    public static final String HIVE_PROP_CARBON_DS_NAME = "wso2.carbon.datasource.name";


    public static final String[] ALL_PROPERTIES = new String[]{
            DBConfiguration.DRIVER_CLASS_PROPERTY,
            DBConfiguration.USERNAME_PROPERTY,
            DBConfiguration.PASSWORD_PROPERTY,
            DBConfiguration.URL_PROPERTY,
            DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY,
            DBConfiguration.OUTPUT_FIELD_NAMES_PROPERTY,
            DBConfiguration.INPUT_TABLE_NAME_PROPERTY,
            Constants.META_TABLE_NAME,
            HIVE_JDBC_UPDATE_ON_DUPLICATE,
            HIVE_JDBC_PRIMARY_KEY_FIELDS,
            HIVE_JDBC_COLUMNS_MAPPING,
            HIVE_JDBC_TABLE_CREATE_QUERY,
            HIVE_JDBC_OUTPUT_UPSERT_QUERY,
            HIVE_JDBC_UPSERT_QUERY_VALUES_ORDER,
            HIVE_PROP_CARBON_DS_NAME};


    public static void copyJDBCProperties(Properties from, Map<String, String> to) {
        for (String key : ALL_PROPERTIES) {
            String value = from.getProperty(key);
            if (value != null) {
                to.put(key, value);
                // This is for supporting wso2 carbon datasources
                if (key.equals(HIVE_PROP_CARBON_DS_NAME)) {
                    addingProperties(value, to);
                }
            }
        }
    }

    private static void addingProperties(String dataSourceName, Map<String, String> to) {
        CarbonDataSourceFetcher carbonDataSourceFetcher = new CarbonDataSourceFetcher();
        Map<String, String> dataSource = carbonDataSourceFetcher.getCarbonDataSource(dataSourceName);
        to.putAll(dataSource);
    }

    public final static String getOutputTableName(Configuration configuration) {
        String tableName = configuration.get(DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY);
        if (tableName == null) {
            String createTableQuery = configuration.get(HIVE_JDBC_TABLE_CREATE_QUERY);
            if (createTableQuery != null) {
                tableName = Commons.extractingTableNameFromQuery(createTableQuery);
            } else {
                //assign the meta table name
                tableName = configuration.get(Constants.META_TABLE_NAME);
                if (tableName.contains("default.")) {
                    tableName = tableName.replace("default.", "");
                }
            }
        }
        return tableName;
    }

    public final static String getInputTableName(Configuration configuration) {
        String inputTableName = configuration.get(DBConfiguration.INPUT_TABLE_NAME_PROPERTY);
        if (inputTableName == null) {
            //assign the meta table name
            inputTableName = configuration.get(Constants.META_TABLE_NAME);
            if (inputTableName.contains("default.")) {
                inputTableName = inputTableName.replace("default.", "");
            }
        }
        return inputTableName.trim();
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

    public final static String[] getInputFieldNames(Configuration conf) {
        String[] fieldNames = null;
        String inputFieldNames = conf.get(DBConfiguration.INPUT_FIELD_NAMES_PROPERTY);
        if (inputFieldNames != null) {
            fieldNames = inputFieldNames.split(",");
        } else {

            String selectQuery = conf.get("hive.query.string");
            fieldNames = Commons.extractFieldNames(selectQuery).split(",");
        }
        return fieldNames;
    }

    public final static String[] getOutputFieldNames(Configuration conf) {
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
            //Hive keeps column names in lower case letters.
            return convertingToLowerCase(primaryKeyFields.split(","));
        }
        return null;
    }

    private static String[] convertingToLowerCase(String[] primaryKeyFields) {
        if (primaryKeyFields != null) {
            String[] fields = new String[primaryKeyFields.length];
            for (int i = 0; i < primaryKeyFields.length; i++) {
                fields[i] = primaryKeyFields[i].toLowerCase();
            }
            return fields;
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


    public static String getDbSpecificUpdateQuery(JobConf conf) {
        return conf.get(ConfigurationUtils.HIVE_JDBC_OUTPUT_UPSERT_QUERY);
    }

    public static String[] getUpsertQueryValuesOrder(JobConf conf) {
        String valuesOrder = conf.get(ConfigurationUtils.HIVE_JDBC_UPSERT_QUERY_VALUES_ORDER);
        String[] order = null;
        if (valuesOrder != null) {
            valuesOrder = valuesOrder.trim();
            order = valuesOrder.split(",");
        }
        return order;
    }

    /// getting wso2 carbon data source name

    public static String getWso2CarbonDataSourceName(JobConf conf){
        return conf.get(ConfigurationUtils.HIVE_PROP_CARBON_DS_NAME);
    }

    //For getting connection pool properties
    public static String isDefaultAutoCommit(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_DEFAULT_AUTO_COMMIT);
    }

    public static String isDefaultReadOnly(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_DEFAULT_READ_ONLY);
    }

    public static String getDefaultCatalog(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_DEFAULT_CATALOG);
    }

    public static String getDefaultTransactionIsolation(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_DEFAULT_TRANSACTION_ISOLATION);
    }

    public static String isTestOnBorrow(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_TEST_ON_BORROW);
    }

    public static String isTestOnReturn(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_TEST_ON_RETURN);
    }

    public static String getTimeBetweenEvictionRunsMillis(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
    }

    public static String getNumTestsPerEvictionRun(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_NUM_TESTS_PER_EVICTION_RUN);
    }

    public static String getMinEvictableIdleTimeMillis(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_MIN_EVICTABLE_IDLE_TIME_MILLIS);
    }

    public static String isTestWhileIdle(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_TEST_WHILE_IDLE);
    }

    public static String getValidationQuery(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_VALIDATION_QUERY);
    }

    public static String getMaxActive(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_MAX_ACTIVE);
    }

    public static String getMaxIdle(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_MAX_IDLE);
    }

    public static String getMaxWait(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_MAX_WAIT);
    }

    public static String getMinIdle(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_MIN_IDLE);
    }

    public static String getInitialSize(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_INITIAL_SIZE);
    }

    public static String isAccessToUnderlyingConnectionAllowed(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED);
    }

    public static String isRemoveAbandoned(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_REMOVE_ABANDONED);
    }

    public static String getRemoveAbandonedTimeout(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_REMOVE_ABANDONED_TIMEOUT);
    }

    public static String isLogAbandoned(JobConf conf) {
        return conf.get(BasicDataSourceConstants.PROP_LOG_ABANDONED);
    }

}
