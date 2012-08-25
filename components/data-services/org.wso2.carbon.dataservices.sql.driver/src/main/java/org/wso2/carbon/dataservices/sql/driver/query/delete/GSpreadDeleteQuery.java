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
package org.wso2.carbon.dataservices.sql.driver.query.delete;

import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;
import org.wso2.carbon.dataservices.sql.driver.TDriverUtil;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class GSpreadDeleteQuery extends DeleteQuery {

    public GSpreadDeleteQuery(Statement stmt) throws SQLException {
        super(stmt);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        this.executeSQL();
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return this.executeSQL();
    }

    @Override
    public boolean execute() throws SQLException {
        return (this.executeSQL() > 0);
    }

    private synchronized int executeSQL() throws SQLException {
        int count = 0;
        WorksheetEntry currentWorkSheet =
                TDriverUtil.getCurrentWorkSheetEntry(getConnection(), getTargetTableName());
        CellFeed cellFeed = TDriverUtil.getCellFeed(getConnection(), currentWorkSheet);

        Set<Integer> rowKeys = this.getProcessedRowKeySet(this.getResultantRows().keySet());
        for (CellEntry cell : cellFeed.getEntries()) {
            int rowId = TDriverUtil.getRowIndex(cell.getId());
            if (rowKeys.contains(rowId)) {
                try {
                    cell.delete();
                    count++;
                } catch (IOException e) {
                    throw new SQLException("Error occurred while deleting the row", e);
                } catch (ServiceException e) {
                    throw new SQLException("Error occurred while deleting the row", e);
                }
            }
        }
        return count;
    }

    //temporary
    private Set<Integer> getProcessedRowKeySet(Set<Integer> keys) {
        Set<Integer> processedKeys = new HashSet<Integer>();
        for (Integer key : keys) {
            processedKeys.add(key + 1);
        }
        return processedKeys;
    }

}
