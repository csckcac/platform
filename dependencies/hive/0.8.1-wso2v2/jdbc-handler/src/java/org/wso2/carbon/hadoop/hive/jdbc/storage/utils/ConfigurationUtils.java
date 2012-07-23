package org.wso2.carbon.hadoop.hive.jdbc.storage.utils;


import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.api.Constants;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;
import org.wso2.carbon.hadoop.hive.jdbc.storage.datasource.CarbonDataSourceFetcher;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigurationUtils {

    public static final String HIVE_JDBC_UPDATE_ON_DUPLICATE = "hive.jdbc.update.on.duplicate";
    public static final String HIVE_JDBC_PRIMARY_KEY_FIELDS = "hive.jdbc.primary.key.fields";
    public static final String HIVE_JDBC_INPUT_COLUMNS_MAPPING = "hive.jdbc.input.columns.mapping";
    public static final String HIVE_JDBC_OUTPUT_COLUMNS_MAPPING = "hive.jdbc.output.columns.mapping";



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
            HIVE_JDBC_INPUT_COLUMNS_MAPPING,
            HIVE_JDBC_OUTPUT_COLUMNS_MAPPING,
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

    public final static Map<String,String> mapTableFieldNamesAgainstHiveFieldNames(
            Configuration conf) {
        Map<String,String> mapFields= new HashMap<String,String>();
        String[] fieldNames = null;
        String[] dbTableFieldName = null;
        String inputFieldNames = conf.get(HIVE_JDBC_INPUT_COLUMNS_MAPPING);

        //Field name getting from hive meta table
        fieldNames = conf.get(org.apache.hadoop.hive.serde.Constants.LIST_COLUMNS).split(",");
        fieldNames = (String[])ArrayUtils.removeElement(fieldNames,Commons.BLOCK_OFFSET_INSIDE_FILE);
        fieldNames = (String[])ArrayUtils.removeElement(fieldNames, Commons.INPUT_FILE_NAME);
        fieldNames = trim(fieldNames);

        if (inputFieldNames != null) {
            dbTableFieldName = inputFieldNames.split(",");
            dbTableFieldName = trim(dbTableFieldName);
            if(dbTableFieldName.length!=fieldNames.length){
                throw new IllegalArgumentException("hive.jdbc.input.columns.mapping size " + dbTableFieldName.length
                + " doesn't match with no of hive meta table columns, which is " + fieldNames.length);
            }
            for(int i=0;i<dbTableFieldName.length;i++){
                  mapFields.put(dbTableFieldName[i],fieldNames[i]);
            }
        } else {
            for(int i=0;i<fieldNames.length;i++){
                mapFields.put(fieldNames[i],fieldNames[i]);
            }
        }
        return mapFields;
    }


    /**
     * Trim the white spaces, new lines from the input array.
     *
     * @param input a input string array
     * @return a trimmed string array
     */
    protected static String[] trim(String[] input) {
        String[] trimmed = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            trimmed[i] = input[i].trim();
        }
        return trimmed;
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
        String mappingFields = conf.get(HIVE_JDBC_OUTPUT_COLUMNS_MAPPING);
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
            order = trim(valuesOrder.split(","));
        }
        return order;
    }

    /// getting wso2 carbon data source name

    public static String getWso2CarbonDataSourceName(JobConf conf){
        return conf.get(ConfigurationUtils.HIVE_PROP_CARBON_DS_NAME);
    }

}
