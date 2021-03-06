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

import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataTable;

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
        //writeRecord(workbook, ((TExcelConnection) getConnection()).getPath());
        return rowCount;
    }

    private int findColumn(DataTable table, String columnName) throws SQLException {
        return table.getHeaders().get(columnName);
    }


}
