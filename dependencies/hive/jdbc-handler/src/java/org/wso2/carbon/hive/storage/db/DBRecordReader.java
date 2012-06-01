package org.wso2.carbon.hive.storage.db;


import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.wso2.carbon.hive.storage.input.JDBCSplit;

import java.io.IOException;

public class DBRecordReader implements RecordReader<LongWritable, MapWritable> {

    public DBRecordReader(JDBCSplit split, JobConf job) {

    }

    public boolean next(LongWritable longWritable, MapWritable mapWritable) throws IOException {
        return false;
    }

    public LongWritable createKey() {
        return null;
    }

    public MapWritable createValue() {
        return null;
    }

    public long getPos() throws IOException {
        return 0;
    }

    public void close() throws IOException {

    }

    public float getProgress() throws IOException {
        return 0;
    }
}
