package org.wso2.carbon.hive.data.source.access.util;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveContext;
import org.apache.hadoop.hive.metastore.hooks.JDOConnectionURLHook;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;
import org.w3c.dom.Element;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.ndatasource.core.utils.DataSourceUtils;
import org.wso2.carbon.ndatasource.rdbms.RDBMSConfiguration;
import org.wso2.carbon.ndatasource.rdbms.RDBMSDataSourceReader;
import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.entity.DatabaseMetaData;
import org.wso2.carbon.rssmanager.core.service.RSSManagerService;

import java.util.HashMap;
import java.util.Map;

public class DataSourceAccessUtil implements JDOConnectionURLHook {

    private static final String HIVE_METASTORE_DB = "metastore_db";

    private static DataSourceService carbonDataSourceService;

    private static RSSManagerService rssManagerService;

    public static DataSourceService getCarbonDataSourceService() {
        return carbonDataSourceService;
    }

    public static void setCarbonDataSourceService(
            DataSourceService dataSourceService) {
        carbonDataSourceService = dataSourceService;
    }

    public static void setRSSManagerService(RSSManagerService rssMgrService) {
        rssManagerService = rssMgrService;
    }

    public static RSSManagerService getRSSManagerService() {
        return rssManagerService;
    }

    public static Map<String, String> getDataSourceProperties(String dataSourceName) {

        int tenantId = HiveContext.getCurrentContext().getTenantId();

        Map<String, String> dataSourceProperties = new HashMap<String, String>();
        try {

            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);

            Element element = (Element) carbonDataSourceService.getDataSource(dataSourceName).
                    getDSMInfo().getDefinition().getDsXMLConfiguration();
            RDBMSConfiguration rdbmsConfiguration = RDBMSDataSourceReader.loadConfig(
                    DataSourceUtils.elementToString(element));

            dataSourceProperties = setDataSourceProperties(dataSourceProperties, rdbmsConfiguration);

        } catch (DataSourceException e) {
            e.printStackTrace();
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }
        return dataSourceProperties;
    }

    public static String getMetaStoreConnectionURL(int tenantId) {

        try {
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);

            DatabaseMetaData[] databaseEntries = rssManagerService.getDatabases();
            for (DatabaseMetaData databaseEntry : databaseEntries) {
                if (databaseEntry.getName().contains(HIVE_METASTORE_DB)) {
                    return databaseEntry.getUrl();
                }
            }

        } catch (RSSManagerException e) {
            e.printStackTrace();
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }

        return null;

    }

    public String getJdoConnectionUrl(Configuration configuration) throws Exception {

        int tenantId = configuration.getInt("hive.current.tenant", -1234);
        return getMetaStoreConnectionURL(tenantId);

    }

    public void notifyBadConnectionUrl(String s) {
        // Do nothing
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
        return dataSourceProperties;
    }


    private static void setProperties(String propertyKey, Object value,
                                      Map<String, String> dataSourceProperties) {
        if (value != null) {
            if (value instanceof Boolean) {
                dataSourceProperties.put(propertyKey, Boolean.toString((Boolean) value));
            } else if (value instanceof String) {
                dataSourceProperties.put(propertyKey, (String) value);
            } else if (value instanceof Integer) {
                dataSourceProperties.put(propertyKey, Integer.toString((Integer) value));
            } else if (value instanceof Long) {
                dataSourceProperties.put(propertyKey, Long.toString((Long) value));
            }
        }
    }
}
