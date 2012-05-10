package org.wso2.carbon.hive.storage;

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

import java.io.IOException;
import java.util.Properties;


public class JDBCDataOutputFormat implements OutputFormat<NullWritable, MapWritable>,
        HiveOutputFormat<NullWritable, MapWritable> {
    public FileSinkOperator.RecordWriter getHiveRecordWriter(JobConf entries, Path path, Class<? extends Writable> aClass, boolean b, Properties properties, Progressable progressable) throws IOException {
        return null;
    }

    public RecordWriter<NullWritable, MapWritable> getRecordWriter(FileSystem fileSystem, JobConf entries, String s, Progressable progressable) throws IOException {
        return null;
    }

    public void checkOutputSpecs(FileSystem fileSystem, JobConf entries) throws IOException {

    }
}
