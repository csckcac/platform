package org.wso2.carbon.hive.storage.input;

import org.apache.hadoop.hive.ql.io.HiveInputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.wso2.carbon.hive.storage.db.DBRecordReader;
import org.wso2.carbon.hive.storage.db.DatabaseProperties;
import org.wso2.carbon.hive.storage.utils.ConfigurationUtils;

import java.io.IOException;


public class JDBCDataInputFormat extends HiveInputFormat<LongWritable, MapWritable> {


    @Override
    public RecordReader getRecordReader(InputSplit split, JobConf conf, Reporter reporter)
            throws IOException {
        DatabaseProperties dbProperties = new DatabaseProperties();
        dbProperties.setTableName(ConfigurationUtils.getInputTableName(conf));
        dbProperties.setUserName(ConfigurationUtils.getDatabaseUserName(conf));
        dbProperties.setPassword(ConfigurationUtils.getDatabasePassword(conf));
        dbProperties.setConnectionUrl(ConfigurationUtils.getConnectionUrl(conf));
        dbProperties.setDriverClass(ConfigurationUtils.getDriverClass(conf));
        dbProperties.setFieldsNames(ConfigurationUtils.getInputFieldNames(conf));

        return new DBRecordReader((JDBCSplit)split,conf,dbProperties);
    }


    @Override
    public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
        return JDBCSplit.getSplits(job, numSplits);
    }
}