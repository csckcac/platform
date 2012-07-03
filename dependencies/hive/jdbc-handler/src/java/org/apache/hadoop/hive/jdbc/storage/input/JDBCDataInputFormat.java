package org.apache.hadoop.hive.jdbc.storage.input;

import org.apache.hadoop.hive.jdbc.storage.db.DBManager;
import org.apache.hadoop.hive.jdbc.storage.db.DBRecordReader;
import org.apache.hadoop.hive.jdbc.storage.db.DatabaseProperties;
import org.apache.hadoop.hive.jdbc.storage.utils.ConfigurationUtils;
import org.apache.hadoop.hive.ql.io.HiveInputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;


public class JDBCDataInputFormat extends HiveInputFormat<LongWritable, MapWritable> {


    @Override
    public RecordReader getRecordReader(InputSplit split, JobConf conf, Reporter reporter)
            throws IOException {
        DatabaseProperties dbProperties = new DatabaseProperties();
        dbProperties.setTableName(ConfigurationUtils.getInputTableName(conf));
        dbProperties.setFieldsNames(ConfigurationUtils.getInputFieldNames(conf));
        dbProperties.setDataSourceName(ConfigurationUtils.getWso2CarbonDataSourceName(conf));

        DBManager dbManager = new DBManager();
        dbManager.configureDB(conf);

        return new DBRecordReader((JDBCSplit)split,dbProperties, dbManager);
    }


    @Override
    public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
        return JDBCSplit.getSplits(job, numSplits);
    }
}