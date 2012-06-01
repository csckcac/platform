package org.wso2.carbon.hive.storage.input;

import org.apache.hadoop.hive.ql.exec.TableScanOperator;
import org.apache.hadoop.hive.ql.io.HiveInputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.wso2.carbon.hive.storage.db.DBRecordReader;

import java.io.IOException;


public class JDBCDataInputFormat extends HiveInputFormat<LongWritable, MapWritable> {


    @Override
    public void configure(JobConf job) {
        super.configure(job);
    }

    @Override
    public RecordReader getRecordReader(InputSplit split, JobConf job, Reporter reporter)
            throws IOException {
        return new DBRecordReader((JDBCSplit)split,job);
    }

    @Override
    protected void init(JobConf job) {
        super.init(job);
    }

    @Override
    public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
        return JDBCSplit.getSplits(job, numSplits);
    }

    @Override
    public void validateInput(JobConf job) throws IOException {
        super.validateInput(job);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void pushFilters(JobConf jobConf, TableScanOperator tableScan) {
        super.pushFilters(jobConf, tableScan);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void pushProjectionsAndFilters(JobConf jobConf, Class inputFormatClass,
                                             String splitPath, String splitPathWithNoSchema) {
        super.pushProjectionsAndFilters(jobConf, inputFormatClass, splitPath, splitPathWithNoSchema);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void pushProjectionsAndFilters(JobConf jobConf, Class inputFormatClass,
                                             String splitPath, String splitPathWithNoSchema,
                                             boolean nonNative) {
        super.pushProjectionsAndFilters(jobConf, inputFormatClass, splitPath, splitPathWithNoSchema, nonNative);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
