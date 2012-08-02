/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.sql.driver.query.update;

import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataReader;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataReaderFactory;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataTable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ExcelUpdateQuery extends UpdateQuery {

    public ExcelUpdateQuery(Statement stmt) throws SQLException {
        super(stmt);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        executeSQL();
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return executeSQL();
    }

    @Override
    public boolean execute() throws SQLException {
        return (executeSQL() > 0);
    }

    private int executeSQL() throws SQLException {
        int rowCount = 0;

        DataReader reader = DataReaderFactory.createDataReader(getConnection());
        DataTable table = reader.getData().get(getTargetTable());
        for (DataRow row : table.getRows().values()) {
            for (String columnName : getTargetColumns().values()) {}
            //row.getCells().get(findColumn(table, columnName)).setCellValue();
        }

        //writeRecord(workbook, ((TExcelConnection) getConnection()).getPath());
        return rowCount;
    }

    private synchronized void writeRecord(Workbook workbook, String filePath) throws SQLException {
        try {
            FileOutputStream out = new FileOutputStream(filePath);
            workbook.write(out);
            out.close();
        } catch (FileNotFoundException e) {
            throw new SQLException("Error occurred while locating the EXCEL datasource", e);
        } catch (IOException e) {
            throw new SQLException("Error occurred while writing the records to the EXCEL " +
                    "datasource", e);
        }
    }

    private int findColumn(DataTable table, String columnName) {
        return table.getHeaders().get(columnName);
    }


}
