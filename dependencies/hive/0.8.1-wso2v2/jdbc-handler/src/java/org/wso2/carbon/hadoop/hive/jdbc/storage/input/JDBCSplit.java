package org.wso2.carbon.hadoop.hive.jdbc.storage.input;


import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.hadoop.hive.jdbc.storage.db.DBManager;
import org.wso2.carbon.hadoop.hive.jdbc.storage.db.DBOperation;
import org.wso2.carbon.hadoop.hive.jdbc.storage.db.DatabaseProperties;
import org.wso2.carbon.hadoop.hive.jdbc.storage.db.QueryConstructor;
import org.wso2.carbon.hadoop.hive.jdbc.storage.utils.ConfigurationUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class JDBCSplit extends FileSplit implements InputSplit {

    private static final Logger log = LoggerFactory.getLogger(JDBCSplit.class);

    private static final String[] EMPTY_ARRAY = new String[]{};
    private long start, end;
    private boolean isLastSplit = false;

    public JDBCSplit() {
        super((Path) null, 0, 0, EMPTY_ARRAY);
    }

    public JDBCSplit(long start, long end, Path dummyPath) {
        super(dummyPath, 0, 0, EMPTY_ARRAY);
        this.start = start;
        this.end = end;
    }

    @Override
    public void readFields(final DataInput input) throws IOException {
        super.readFields(input);
        start = input.readLong();
        end = input.readLong();
    }

    @Override
    public void write(final DataOutput output) throws IOException {
        super.write(output);
        output.writeLong(start);
        output.writeLong(end);
    }

    /* Data is remote for all nodes. */
    @Override
    public String[] getLocations() throws IOException {
        return EMPTY_ARRAY;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isLastSplit() {
        return this.isLastSplit;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public long getLength() {
        return end - start;
    }


    public static JDBCSplit[] getSplits(JobConf conf, int numSplits) {

        DatabaseProperties dbProperties = new DatabaseProperties();
        dbProperties.setTableName(ConfigurationUtils.getInputTableName(conf));
        dbProperties.setUserName(ConfigurationUtils.getDatabaseUserName(conf));
        dbProperties.setPassword(ConfigurationUtils.getDatabasePassword(conf));
        dbProperties.setConnectionUrl(ConfigurationUtils.getConnectionUrl(conf));
        dbProperties.setDriverClass(ConfigurationUtils.getDriverClass(conf));
        Map<String,String> mapFieldNames = ConfigurationUtils.mapTableFieldNamesAgainstHiveFieldNames(conf);
        dbProperties.setInputColumnMappingFields(mapFieldNames);
        dbProperties.setFieldsNames(mapFieldNames.keySet().toArray(new String[mapFieldNames.size()]));

        QueryConstructor queryConstructor = new QueryConstructor();
        String sql = queryConstructor.constructCountQuery(dbProperties);

        DBManager dbManager = new DBManager();
        dbManager.createConnection(conf);
        JDBCSplit[] splits = null;
        try {
            Connection connection = dbManager.getConnection();
            DBOperation operation = new DBOperation();
            long total = operation.getTotalCount(sql, connection);
            final long splitSize = total / numSplits;
            splits = new JDBCSplit[numSplits];
            final Path[] tablePaths = FileInputFormat.getInputPaths(conf);
            for (int i = 0; i < numSplits; i++) {
                if ((i + 1) == numSplits) {
                    splits[i] = new JDBCSplit(i * splitSize, total, tablePaths[0]);
                    splits[i].setLastSplit();
                } else {
                    splits[i] = new JDBCSplit(i * splitSize, (i + 1) * splitSize, tablePaths[0]);
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Failed to get sql connection", e);
        } catch (SQLException e) {
            log.error("Failed to get total rows count", e);
        }


        return splits;
    }

    private void setLastSplit() {
        this.isLastSplit = true;
    }
}
