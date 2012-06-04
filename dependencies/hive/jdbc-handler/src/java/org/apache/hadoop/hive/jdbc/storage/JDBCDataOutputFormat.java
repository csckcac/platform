package org.apache.hadoop.hive.jdbc.storage;

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
import org.apache.hadoop.hive.jdbc.storage.utils.ConfigurationUtils;
import org.apache.hadoop.hive.jdbc.storage.db.DBRecordWriter;
import org.apache.hadoop.hive.jdbc.storage.db.DatabaseProperties;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;


public class JDBCDataOutputFormat implements OutputFormat<NullWritable, MapWritable>,
                                             HiveOutputFormat<NullWritable, MapWritable> {
    public FileSinkOperator.RecordWriter getHiveRecordWriter(JobConf conf, Path path,
                                                             Class<? extends Writable> aClass,
                                                             boolean b, Properties properties,
                                                             Progressable progressable)
            throws IOException {
        try {

            DatabaseProperties dbProperties = new DatabaseProperties();
            dbProperties.setConnectionUrl(ConfigurationUtils.getConnectionUrl(conf));
            dbProperties.setDriverClass(ConfigurationUtils.getDriverClass(conf));
            dbProperties.setUserName(ConfigurationUtils.getDatabaseUserName(conf));
            dbProperties.setPassword(ConfigurationUtils.getDatabasePassword(conf));
            dbProperties.setTableName(ConfigurationUtils.getOutputTableName(conf));
            dbProperties.setFieldsNames(ConfigurationUtils.getOutputFieldNames(conf));
            dbProperties.setUpdateOnDuplicate(ConfigurationUtils.isUpdateOnDuplicate(conf));
            dbProperties.setPrimaryFields(ConfigurationUtils.getPrimaryKeyFields(conf));
            dbProperties.setColumnMappingFields(ConfigurationUtils.getColumnMappingFields(conf));
            dbProperties.setDbSpecificUpsertQuery(ConfigurationUtils.getDbSpecificUpdateQuery(conf));
            dbProperties.setUpsertQueryValuesOrder(ConfigurationUtils.getUpsertQueryValuesOrder(conf));

            return new DBRecordWriter(dbProperties);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
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
