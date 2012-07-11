package org.wso2.carbon.hadoop.hive.jdbc.storage.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public class CarbonDataSourceFetcher {

    private static final Logger log = LoggerFactory.getLogger(CarbonDataSourceFetcher.class);

    private static final String CARBON_DATA_SOURCE_ACCESS_CLASS =
            "org.wso2.carbon.hive.data.source.access.util.DataSourceAccessUtil";
    private static final String GET_DATA_SOURCE_PROPERTIES_METHOD = "getDataSourceProperties";
    private Map<String, String> dataSource;

    public Map<String, String> getCarbonDataSource(String dataSourceName) {
        try {
            Class<?> dataSourceAccessUtilClass = Class.forName(CARBON_DATA_SOURCE_ACCESS_CLASS);
            Method getDataSourceMethod = dataSourceAccessUtilClass.getMethod(GET_DATA_SOURCE_PROPERTIES_METHOD, String.class);
            Object dataSourcePropertiesMap = getDataSourceMethod.invoke(null, dataSourceName);
            dataSource = (Map<String, String>) dataSourcePropertiesMap;
        } catch (ClassNotFoundException e) {
            log.error("Error occurred while getting carbon data-source", e);
        } catch (NoSuchMethodException e) {
            log.error("Error occurred while getting carbon data-source", e);
        } catch (InvocationTargetException e) {
            log.error("Error occurred while getting carbon data-source", e);
        } catch (IllegalAccessException e) {
            log.error("Error occurred while getting carbon data-source", e);
        }
        return dataSource;
    }
}
