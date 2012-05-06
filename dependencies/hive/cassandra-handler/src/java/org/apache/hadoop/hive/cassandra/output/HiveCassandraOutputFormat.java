package org.apache.hadoop.hive.cassandra.output;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.cassandra.auth.IAuthenticator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.cassandra.CassandraException;
import org.apache.hadoop.hive.cassandra.CassandraProxyClient;
import org.apache.hadoop.hive.cassandra.serde.AbstractColumnSerDe;
import org.apache.hadoop.hive.ql.exec.FileSinkOperator.RecordWriter;
import org.apache.hadoop.hive.ql.io.HiveOutputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.util.Progressable;

@SuppressWarnings("deprecation")
public class HiveCassandraOutputFormat implements HiveOutputFormat<Text, CassandraPut>,
    OutputFormat<Text, CassandraPut> {

  static final Log LOG = LogFactory.getLog(HiveCassandraOutputFormat.class);

  @Override
  public RecordWriter getHiveRecordWriter(final JobConf jc, Path finalOutPath,
      Class<? extends Writable> valueClass, boolean isCompressed, Properties tableProperties,
      Progressable progress) throws IOException {

    final String cassandraKeySpace = jc.get(AbstractColumnSerDe.CASSANDRA_KEYSPACE_NAME);
    final String cassandraHost = jc.get(AbstractColumnSerDe.CASSANDRA_HOST);
    final int cassandraPort = Integer.parseInt(jc.get(AbstractColumnSerDe.CASSANDRA_PORT));

    final String username = jc.get(AbstractColumnSerDe.CASSANDRA_INPUT_KEYSPACE_USERNAME_CONFIG);
    final String password = jc.get(AbstractColumnSerDe.CASSANDRA_INPUT_KEYSPACE_PASSWD_CONFIG);

    Map<String, String> credentials = null;
    if (username != null) {
       credentials = new HashMap<String, String>();
       credentials.put(IAuthenticator.USERNAME_KEY, username);
       credentials.put(IAuthenticator.PASSWORD_KEY, password);
    }

    final CassandraProxyClient client;
    try {
      client = new CassandraProxyClient(
        cassandraHost, cassandraPort, cassandraKeySpace, credentials, true, true);
    } catch (CassandraException e) {
      throw new IOException(e);
    }

    return new RecordWriter() {

      @Override
      public void close(boolean abort) throws IOException {
        if (client != null) {
          client.close();
        }
      }

      @Override
      public void write(Writable w) throws IOException {
        Put put = (Put) w;
        put.write(cassandraKeySpace, client, jc);
      }

    };
  }

  @Override
  public void checkOutputSpecs(FileSystem arg0, JobConf jc) throws IOException {

  }

  @Override
  public org.apache.hadoop.mapred.RecordWriter<Text, CassandraPut> getRecordWriter(FileSystem arg0,
      JobConf arg1, String arg2, Progressable arg3) throws IOException {
    throw new RuntimeException("Error: Hive should not invoke this method.");
  }
}
