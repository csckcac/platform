package org.wso2.carbon.hadoop.hive.jdbc.storage.db;


import org.apache.hadoop.hive.ql.exec.FileSinkOperator.RecordWriter;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DBRecordWriter implements RecordWriter {

    private static final Logger log = LoggerFactory.getLogger(DBRecordWriter.class);

    Connection connection = null;
    DatabaseProperties dbProperties;
    DBManager dbManager;

    public DBRecordWriter(DatabaseProperties databaseProperties, DBManager dbManager)
            throws ClassNotFoundException, SQLException {
        dbProperties = databaseProperties;
        this.dbManager = dbManager;
        connection = dbManager.getConnection();
    }

    public void write(Writable writable) throws IOException {
        MapWritable map = (MapWritable) writable;

        DBOperation operation = null;
        try {
            operation = new DBOperation(dbProperties, connection);
            operation.writeToDB(map);
        } catch (SQLException e) {
            log.error("Failed to write data to the table: " + dbProperties.getTableName(), e);
        }
    }

    public void close(boolean b) throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Failed to close the connection", e);
            }
        }
    }
}
