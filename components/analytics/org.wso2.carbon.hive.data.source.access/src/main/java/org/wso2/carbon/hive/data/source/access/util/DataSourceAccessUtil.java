package org.wso2.carbon.hive.data.source.access.util;


import org.apache.hadoop.mapred.lib.db.DBConfiguration;
import org.w3c.dom.Element;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceReader;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;
import org.apache.hadoop.hive.jdbc.storage.datasource.BasicDataSourceConstants;

import java.util.HashMap;
import java.util.Map;

public class DataSourceAccessUtil {


    private static DataSourceService carbonDataSourceService;

    public static DataSourceService getCarbonDataSourceService() {
        return carbonDataSourceService;
    }

    public static void setCarbonDataSourceService(
            DataSourceService dataSourceService) {
        carbonDataSourceService = dataSourceService;
    }

    public static Map<String, String> getDataSourceProperties(String dataSourceName) {

        int tenantId = CarbonContextHolder.getCurrentCarbonContextHolder().getTenantId();

        Map<String, String> dataSourceProperties = new HashMap<String, String>();
        try {

            SuperTenantCarbonContext.startTenantFlow();
            //TODO: Fix the hard coded value properly
            SuperTenantCarbonContext.getCurrentContext().setTenantId(-1234);

            Element element = (Element) carbonDataSourceService.getDataSource(dataSourceName).
                    getDSMInfo().getDefinition().getDsXMLConfiguration();
            RDBMSConfiguration rdbmsConfiguration = RDBMSDataSourceReader.loadConfig(
                    DataSourceUtils.elementToString(element));

            dataSourceProperties = setDataSourceProperties(dataSourceProperties,rdbmsConfiguration);

        } catch (DataSourceException e) {
            e.printStackTrace();
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
        return dataSourceProperties;
    }

    private static Map<String, String> setDataSourceProperties(
            Map<String, String> dataSourceProperties, RDBMSConfiguration rdbmsConfiguration) {
        setProperties(DBConfiguration.URL_PROPERTY,
                                 rdbmsConfiguration.getUrl(), dataSourceProperties);
        setProperties(DBConfiguration.DRIVER_CLASS_PROPERTY,
                                 rdbmsConfiguration.getDriverClassName(), dataSourceProperties);
        setProperties(DBConfiguration.USERNAME_PROPERTY,
                                 rdbmsConfiguration.getUsername(), dataSourceProperties);
        setProperties(DBConfiguration.PASSWORD_PROPERTY,
                                 rdbmsConfiguration.getPassword(), dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED,
                                 rdbmsConfiguration.isAccessToUnderlyingConnectionAllowed(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_DEFAULT_AUTO_COMMIT,
                                 rdbmsConfiguration.isDefaultAutoCommit(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_DEFAULT_CATALOG,
                                 rdbmsConfiguration.getDefaultCatalog(), dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_DEFAULT_READ_ONLY,
                                 rdbmsConfiguration.isDefaultReadOnly(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_DEFAULT_TRANSACTION_ISOLATION,
                                 rdbmsConfiguration.getDefaultTransactionIsolation(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_INITIAL_SIZE,
                                 rdbmsConfiguration.getInitialSize(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_LOG_ABANDONED,
                                 rdbmsConfiguration.isLogAbandoned(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_MAX_ACTIVE,
                                 rdbmsConfiguration.getMaxActive(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_MAX_IDLE,
                                 rdbmsConfiguration.getMaxIdle(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_MAX_WAIT,
                                 rdbmsConfiguration.getMaxWait(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_MIN_EVICTABLE_IDLE_TIME_MILLIS,
                                 rdbmsConfiguration.getMinEvictableIdleTimeMillis(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_MIN_IDLE,
                                 rdbmsConfiguration.getMinIdle(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_NUM_TESTS_PER_EVICTION_RUN,
                                 rdbmsConfiguration.getNumTestsPerEvictionRun(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_REMOVE_ABANDONED,
                                 rdbmsConfiguration.isRemoveAbandoned(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_REMOVE_ABANDONED_TIMEOUT,
                                 rdbmsConfiguration.getRemoveAbandonedTimeout(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_TEST_ON_BORROW,
                                 rdbmsConfiguration.isTestOnBorrow(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_TEST_ON_RETURN,
                                 rdbmsConfiguration.isTestOnReturn(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_TEST_WHILE_IDLE,
                                 rdbmsConfiguration.isTestWhileIdle(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_TIME_BETWEEN_EVICTION_RUNS_MILLIS,
                                 rdbmsConfiguration.getTimeBetweenEvictionRunsMillis(),
                                 dataSourceProperties);
        setProperties(BasicDataSourceConstants.PROP_VALIDATION_QUERY,
                                 rdbmsConfiguration.getValidationQuery(), dataSourceProperties);
        return  dataSourceProperties;
    }


    private static void setProperties(String propertyKey, Object value,
                               Map<String, String> dataSourceProperties){
        if(value!=null){
            if(value instanceof Boolean){
               dataSourceProperties.put(propertyKey, Boolean.toString((Boolean)value));
            }else if(value instanceof String){
               dataSourceProperties.put(propertyKey, (String)value);
            }else if(value instanceof Integer){
               dataSourceProperties.put(propertyKey, Integer.toString((Integer)value));
            } else if (value instanceof Long){
               dataSourceProperties.put(propertyKey, Long.toString((Long)value));
            }
        }
    }
}
