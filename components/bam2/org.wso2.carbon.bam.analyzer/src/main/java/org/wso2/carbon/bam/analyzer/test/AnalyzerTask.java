/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.analyzer.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.analyzer.engine.Analyzer;
import org.wso2.carbon.bam.analyzer.engine.AnalyzerSequence;
import org.wso2.carbon.bam.analyzer.engine.DataContext;

public class AnalyzerTask implements Analyzer {
    private static final Log logger = LogFactory.getLog(AnalyzerTask.class);

    static final String KEYSPACE = "BAMKeyspace";
    static final String COLUMN_FAMILY = "WorkflowId_NodeId";

    static final String OUTPUT_REDUCER_VAR = "output_reducer";
    static final String OUTPUT_COLUMN_FAMILY = "output_words";
    private static final String OUTPUT_PATH_PREFIX = "/tmp/word_count";

    private static final String CONF_COLUMN_NAME = "columnname";

    private String sequenceName;

    private int positionInSequence;

    public static void main(String[] args) throws Exception {
        // Let ToolRunner handle generic command-line options
/*        ToolRunner.run(new Configuration(), new AnalyzerTask(), args);
        System.exit(0);*/
    }

    public void analyze(DataContext dataContext) {
/*        try {
            ToolRunner.run(new Configuration(), new AnalyzerTask(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void setAnalyzerSeqeunceName(String analyzerSequence) {
        this.sequenceName = analyzerSequence;
    }

    public String getAnalyzerSequenceName() {
        return this.sequenceName;
    }

    public void setPositionInSequence(int positionInSequence) {
        this.positionInSequence = positionInSequence;
    }

    public int getPositionInSequence() {
        return this.positionInSequence;
    }

    public AnalyzerSequence getAnalyzerSequence() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAnalyzerSequence(AnalyzerSequence analyzerSequence) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /*public static class TokenizerMapper
            extends Mapper<ByteBuffer, SortedMap<ByteBuffer, IColumn>, Text, Text> {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();
        private ByteBuffer sourceColumn;

        protected void setup(org.apache.hadoop.mapreduce.Mapper.Context context)
                throws IOException, InterruptedException {
            sourceColumn = ByteBufferUtil.bytes(context.getConfiguration().get(CONF_COLUMN_NAME));
        }

        public void map(ByteBuffer key, SortedMap<ByteBuffer, IColumn> columns, Context context)
                throws IOException, InterruptedException {
*//*            IColumn column = columns.get(sourceColumn);
            if (column == null)
                return;
            String value = ByteBufferUtil.string(column.value());
            logger.debug("read " + key + ":" + value + " from " + context.getInputSplit());

            StringTokenizer itr = new StringTokenizer(value);
            while (itr.hasMoreTokens())
            {
                word.set(itr.nextToken());
                context.write(word, one);
            }*//*
            String keyStr = ByteBufferUtil.string(key);
            String prefix = keyStr.substring(0, (keyStr.lastIndexOf("---")));

            List<String> lookupKeys = new ArrayList<String>();
            for (Map.Entry<ByteBuffer, IColumn> entry : columns.entrySet()) {
                IColumn column = entry.getValue();

                String lookupKey = ByteBufferUtil.string(column.name());
                lookupKeys.add(lookupKey);

            }

            List<ResultRow> results = QueryUtils.lookupColumnFamily("Event", lookupKeys);

            // rowkey | column1:value1 | column2:value2  ? rowKey | column1:value1 | column2:value2 ? ..
            StringBuffer sb = new StringBuffer();
            for (ResultRow row : results) {
                sb.append(row.getRowKey());
                sb.append("|");

                for (ResultColumn column : row.getColumns()) {
                    sb.append(column.getKey());
                    sb.append(":");
                    sb.append(column.getValue());
                }

                sb.append("?");
            }

            sb.deleteCharAt(sb.length() - 1);

            String result = sb.toString();

            context.write(new Text(prefix), new Text(result));
        }
    }

    public static class ReducerToFilesystem extends Reducer<Text, Text, Text, DoubleWritable> {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
*//*            int sum = 0;
            for (IntWritable val : values)
                sum += val.get();
            context.write(key, new IntWritable(sum));*//*

            double runningCount = 0;

            for (Text value : values) {
                String[] rows = value.toString().split("/?");

                for (String row : rows) {
                    String[] columns = row.split("|");

                    Map<String, String> columnMap = new HashMap<String, String>();
                    for (int i = 1; i < columns.length; i++) {
                        columnMap.put(columns[i].split(":")[0], columns[i].split(":")[1]);
                    }

                    String countStr = columnMap.get("requestCount");
                    double count = Double.parseDouble(countStr);
                    runningCount += count;
                }
            }

            context.write(key, new DoubleWritable(runningCount));
        }
    }

    public static class ReducerToCassandra
            extends Reducer<Text, IntWritable, ByteBuffer, List<Mutation>> {
        private ByteBuffer outputKey;

        protected void setup(org.apache.hadoop.mapreduce.Reducer.Context context)
                throws IOException, InterruptedException {
            outputKey = ByteBufferUtil.bytes(context.getConfiguration().get(CONF_COLUMN_NAME));
        }

        public void reduce(Text word, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            context.write(outputKey, Collections.singletonList(getMutation(word, sum)));
        }

        private static Mutation getMutation(Text word, int sum) {
            Column c = new Column();
            c.name = ByteBuffer.wrap(Arrays.copyOf(word.getBytes(), word.getLength()));
            c.value = ByteBufferUtil.bytes(String.valueOf(sum));
            c.timestamp = System.currentTimeMillis() * 1000;

            Mutation m = new Mutation();
            m.column_or_supercolumn = new ColumnOrSuperColumn();
            m.column_or_supercolumn.column = c;
            return m;
        }
    }

    public int run(String[] args) throws Exception {
        String outputReducerType = "filesystem";
        if (args != null && args.length > 0 && args[0].startsWith(OUTPUT_REDUCER_VAR)) {
            String[] s = args[0].split("=");
            if (s != null && s.length == 2) {
                outputReducerType = s[1];
            }
        }
        logger.info("output reducer type: " + outputReducerType);

        Job job = new Job(getConf(), "wordcount");
        job.setJarByClass(AnalyzerTask.class);
        job.setMapperClass(TokenizerMapper.class);

        if (outputReducerType.equalsIgnoreCase("filesystem")) {
            job.setCombinerClass(ReducerToFilesystem.class);
            job.setReducerClass(ReducerToFilesystem.class);
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(DoubleWritable.class);
            FileOutputFormat.setOutputPath(job, new Path(OUTPUT_PATH_PREFIX));
        } else {
            job.setReducerClass(ReducerToCassandra.class);

            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(IntWritable.class);
            job.setOutputKeyClass(ByteBuffer.class);
            job.setOutputValueClass(List.class);

            job.setOutputFormatClass(ColumnFamilyOutputFormat.class);

            ConfigHelper.setOutputColumnFamily(job.getConfiguration(), KEYSPACE, OUTPUT_COLUMN_FAMILY);
        }

        job.setInputFormatClass(ColumnFamilyInputFormat.class);


        ConfigHelper.setRpcPort(job.getConfiguration(), "9160");
        ConfigHelper.setInitialAddress(job.getConfiguration(), "localhost");
        ConfigHelper.setPartitioner(job.getConfiguration(), "org.apache.cassandra.dht.RandomPartitioner");
        ConfigHelper.setInputColumnFamily(job.getConfiguration(), KEYSPACE, COLUMN_FAMILY);
        SlicePredicate predicate = new SlicePredicate().setSlice_range(new SliceRange(
                ByteBufferUtil.bytes(""), ByteBufferUtil.bytes(""), false, Integer.MAX_VALUE));
        ConfigHelper.setInputSlicePredicate(job.getConfiguration(), predicate);

        job.waitForCompletion(true);

        return 0;
    }*/
}
