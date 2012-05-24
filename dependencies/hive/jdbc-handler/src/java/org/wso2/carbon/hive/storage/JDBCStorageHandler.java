package org.wso2.carbon.hive.storage;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaHook;
import org.apache.hadoop.hive.metastore.MetaStoreUtils;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.ql.metadata.HiveStorageHandler;
import org.apache.hadoop.hive.ql.plan.TableDesc;
import org.apache.hadoop.hive.serde2.SerDe;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class JDBCStorageHandler implements HiveStorageHandler , HiveMetaHook {

    private Configuration conf;

    public Class<? extends org.apache.hadoop.mapred.InputFormat> getInputFormatClass() {
        return JDBCDataInputFormat.class;
    }

    public Class<? extends org.apache.hadoop.mapred.OutputFormat> getOutputFormatClass() {
        return JDBCDataOutputFormat.class;
    }

    public Class<? extends SerDe> getSerDeClass() {
        return JDBCDataSerDe.class;
    }

    public HiveMetaHook getMetaHook() {
        return this;
    }

    public void configureTableJobProperties(final TableDesc tableDesc, final Map<String, String> jobProperties) {
        Properties properties = tableDesc.getProperties();
        ConfigurationUtils.copyJDBCProperties(properties,jobProperties);
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getConf() {
        return conf;
    }

    public void preCreateTable(Table table) throws MetaException {
        boolean isExternal = MetaStoreUtils.isExternalTable(table);

        if (!isExternal) {
            throw new MetaException("Tables must be external.");
        }

        Map<String,String> tableParameters = table.getParameters();

        String tableName = tableParameters.get(DBConfiguration.OUTPUT_TABLE_NAME_PROPERTY);
        String databaseUserName = tableParameters.get(DBConfiguration.USERNAME_PROPERTY);
        String databasePassword = tableParameters.get(DBConfiguration.PASSWORD_PROPERTY);
        String connectionUrl = tableParameters.get(DBConfiguration.URL_PROPERTY);
        String driverClass = tableParameters.get(DBConfiguration.DRIVER_CLASS_PROPERTY);
        String createTableQuery = tableParameters.get(ConfigurationUtils.HIVE_JDBC_TABLE_CREATE_QUERY);

        if(tableName == null){
            tableName = ConfigurationUtils.extractingTableNameFromQuery(tableName, createTableQuery);
        }

        DBManager dbManager = new DBManager();
        dbManager.configureDB(connectionUrl,databaseUserName,databasePassword,driverClass);
        Connection connection = null;
        try {
            connection = dbManager.getConnection();
            if(!dbManager.isTableExist(tableName, connection)){
                Statement statement = connection.createStatement();
                statement.executeUpdate(createTableQuery);
                statement.close();
                connection.close();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void rollbackCreateTable(Table table) throws MetaException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void commitCreateTable(Table table) throws MetaException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void preDropTable(Table table) throws MetaException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void rollbackDropTable(Table table) throws MetaException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void commitDropTable(Table table, boolean b) throws MetaException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
