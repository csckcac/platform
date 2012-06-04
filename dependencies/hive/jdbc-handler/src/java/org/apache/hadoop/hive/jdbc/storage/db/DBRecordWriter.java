package org.apache.hadoop.hive.jdbc.storage.db;


import org.apache.hadoop.hive.ql.exec.FileSinkOperator.RecordWriter;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Writable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DBRecordWriter implements RecordWriter {

    Connection connection = null;
    DatabaseProperties dbProperties;
    DBManager dbManager;

    public DBRecordWriter(DatabaseProperties databaseProperties)
            throws ClassNotFoundException, SQLException {
        dbProperties = databaseProperties;
        dbManager = new DBManager();
        dbManager.configureDB(databaseProperties);
        connection = dbManager.getConnection();
    }

    public void write(Writable writable) throws IOException {
        MapWritable map = (MapWritable) writable;

        DBOperation operation = null;
        try {
            operation = new DBOperation(dbProperties,connection);
            operation.writeToDB(map);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close(boolean b) throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
