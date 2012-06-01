package org.wso2.carbon.hive.storage;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.metastore.HiveMetaHook;
import org.apache.hadoop.hive.metastore.MetaStoreUtils;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.ql.metadata.HiveStorageHandler;
import org.apache.hadoop.hive.ql.plan.TableDesc;
import org.apache.hadoop.hive.serde2.SerDe;
import org.wso2.carbon.hive.storage.db.DBOperation;
import org.wso2.carbon.hive.storage.utils.ConfigurationUtils;
import org.wso2.carbon.hive.storage.input.JDBCDataInputFormat;
import java.util.Map;
import java.util.Properties;

public class JDBCStorageHandler implements HiveStorageHandler, HiveMetaHook {

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

    public void configureTableJobProperties(final TableDesc tableDesc,
                                            final Map<String, String> jobProperties) {
        Properties properties = tableDesc.getProperties();
        ConfigurationUtils.copyJDBCProperties(properties, jobProperties);
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

        Map<String, String> tableParameters = table.getParameters();
        DBOperation operation = new DBOperation();
        operation.createTableIfNotExist(tableParameters);
    }


    public void rollbackCreateTable(Table table) throws MetaException {

    }

    public void commitCreateTable(Table table) throws MetaException {

    }

    public void preDropTable(Table table) throws MetaException {

    }

    public void rollbackDropTable(Table table) throws MetaException {

    }

    public void commitDropTable(Table table, boolean b) throws MetaException {

    }
}
