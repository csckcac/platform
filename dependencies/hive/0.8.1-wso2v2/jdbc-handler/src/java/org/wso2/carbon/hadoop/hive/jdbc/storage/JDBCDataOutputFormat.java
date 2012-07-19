package org.wso2.carbon.hadoop.hive.jdbc.storage;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.FileSinkOperator;
import org.apache.hadoop.hive.ql.io.HiveOutputFormat;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.util.Progressable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.hadoop.hive.jdbc.storage.db.DBManager;
import org.wso2.carbon.hadoop.hive.jdbc.storage.db.DBRecordWriter;
import org.wso2.carbon.hadoop.hive.jdbc.storage.db.DatabaseProperties;
import org.wso2.carbon.hadoop.hive.jdbc.storage.utils.ConfigurationUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;


public class JDBCDataOutputFormat implements OutputFormat<NullWritable, MapWritable>,
                                             HiveOutputFormat<NullWritable, MapWritable> {

    private static final Logger log = LoggerFactory.getLogger(JDBCDataOutputFormat.class);

    public FileSinkOperator.RecordWriter getHiveRecordWriter(JobConf conf, Path path,
                                                             Class<? extends Writable> aClass,
                                                             boolean b, Properties properties,
                                                             Progressable progressable)
            throws IOException {
        try {

            DatabaseProperties dbProperties = new DatabaseProperties();
            dbProperties.setTableName(ConfigurationUtils.getOutputTableName(conf));
            dbProperties.setFieldsNames(ConfigurationUtils.getOutputFieldNames(conf));
            dbProperties.setUpdateOnDuplicate(ConfigurationUtils.isUpdateOnDuplicate(conf));
            dbProperties.setPrimaryFields(ConfigurationUtils.getPrimaryKeyFields(conf));
            dbProperties.setColumnMappingFields(ConfigurationUtils.getColumnMappingFields(conf));
            dbProperties.setDbSpecificUpsertQuery(ConfigurationUtils.getDbSpecificUpdateQuery(conf));
            dbProperties.setUpsertQueryValuesOrder(ConfigurationUtils.getUpsertQueryValuesOrder(conf));

            DBManager dbManager = new DBManager();
            dbManager.createConnection(conf);

            return new DBRecordWriter(dbProperties, dbManager);

        } catch (ClassNotFoundException e) {
            log.error("Failed to get connection", e);
        } catch (SQLException e) {
            log.error("Failed to get connection", e);
        }
        return null;
    }


    public RecordWriter<NullWritable, MapWritable> getRecordWriter(FileSystem fileSystem,
                                                                   JobConf entries, String s,
                                                                   Progressable progressable)
            throws IOException {
        throw new RuntimeException("Error: Hive should not invoke this method.");
    }

    public void checkOutputSpecs(FileSystem fileSystem, JobConf entries) throws IOException {

    }
}
