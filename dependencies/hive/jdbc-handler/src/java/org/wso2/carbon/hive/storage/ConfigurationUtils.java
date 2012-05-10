package org.wso2.carbon.hive.storage;


import com.google.common.collect.ImmutableSet;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ConfigurationUtils {

    public static final String UPDATE_ON_DUPLICATE =  "hive.jdbc.update.on.duplicate";
    
    public static final Set<String> ALL_PROPERTIES = ImmutableSet.of(
            DBConfiguration.DRIVER_CLASS_PROPERTY,
            DBConfiguration.USERNAME_PROPERTY,
            DBConfiguration.PASSWORD_PROPERTY,
            DBConfiguration.URL_PROPERTY,
            DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY,
            DBConfiguration.OUTPUT_FIELD_NAMES_PROPERTY,
            UPDATE_ON_DUPLICATE
    );
    
    public static void copyJDBCProperties(Properties from, Map<String, String> to){
        for (String key : ALL_PROPERTIES) {
            String value = from.getProperty(key);
            if (value != null) {
                to.put(key, value);
            }
        }
    }

}
