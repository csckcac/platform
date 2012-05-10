package org.wso2.carbon.hive.storage;

import org.apache.hadoop.hive.ql.io.HiveInputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;


public class JDBCDataInputFormat extends HiveInputFormat<LongWritable, MapWritable> {

}
