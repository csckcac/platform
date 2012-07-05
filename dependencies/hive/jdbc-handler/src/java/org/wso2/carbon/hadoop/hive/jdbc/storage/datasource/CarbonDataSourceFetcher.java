package org.wso2.carbon.hadoop.hive.jdbc.storage.datasource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;


public class CarbonDataSourceFetcher {

    private static final String CARBON_DATA_SOURCE_ACCESS_CLASS =
            "org.wso2.carbon.hive.data.source.access.util.DataSourceAccessUtil";
    private static final String GET_DATA_SOURCE_PROPERTIES_METHOD = "getDataSourceProperties";
    private Map<String,String> dataSource;

    public Map<String,String> getCarbonDataSource(String dataSourceName) {
        try {
            Class<?> dataSourceAccessUtilClass =  Class.forName(CARBON_DATA_SOURCE_ACCESS_CLASS);
            Method getDataSourceMethod = dataSourceAccessUtilClass.getMethod(GET_DATA_SOURCE_PROPERTIES_METHOD, String.class);
            Object dataSourcePropertiesMap = getDataSourceMethod.invoke(null, dataSourceName);
            dataSource = (Map<String,String>) dataSourcePropertiesMap;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return dataSource;
    }
}
