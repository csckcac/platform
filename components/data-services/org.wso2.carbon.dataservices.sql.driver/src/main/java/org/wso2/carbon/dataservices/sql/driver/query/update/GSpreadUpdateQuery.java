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

import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataCell;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.FixedDataTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class GSpreadUpdateQuery extends UpdateQuery {
    
    public GSpreadUpdateQuery(Statement stmt) throws SQLException {
        super(stmt);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return null;
    }

    @Override
    public int executeUpdate() throws SQLException {
        return 0;
    }

    @Override
    public boolean execute() throws SQLException {
        return false;
    }

    private int executeSQL() throws SQLException {
        Map<Integer, DataRow> result;
        FixedDataTable table = new FixedDataTable(getTargetTableName(), 
        		getTargetTable().getHeaders());
        if (getCondition().getLhs() == null && getCondition().getRhs() == null) {
            result = getTargetTable().getRows();
        } else {
            result = getCondition().process(getTargetTable());
        }
        table.setData(result);

        for (Map.Entry<Integer, DataRow> row : result.entrySet()) {
            List<DataCell> cells = row.getValue().getCells();
            for (DataCell cell : cells) {
                if (cell.getColumnId() == this.extractColumnId(cell.getColumnName())) {
                    //cell.setCellValue();
                }
            }
        }
        return 0;
    }

    
}
