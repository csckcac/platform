/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dataservices.sql.driver.processor.reader;

import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import org.wso2.carbon.dataservices.sql.driver.TDriverUtil;
import org.wso2.carbon.dataservices.sql.driver.TGSpreadConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GSpreadDataReader extends DataReader {

    public GSpreadDataReader(Connection connection) throws SQLException {
        super(connection);
    }

    public void populateData() throws SQLException {
        TGSpreadConnection gsConnection = (TGSpreadConnection) getConnection();
        WorksheetFeed workSheetFeed = gsConnection.getWorkSheetFeed();
        if (workSheetFeed == null) {
            throw new SQLException("Work sheet feed it not initialized properly and is null");
        }
        try {
            List<WorksheetEntry> workSheets = workSheetFeed.getEntries();
            for (WorksheetEntry workSheet : workSheets) {
                CellQuery cellQuery = new CellQuery(workSheet.getCellFeedUrl());
                CellFeed cellFeed =
                        gsConnection.getSpreadSheetService().query(cellQuery, CellFeed.class);
                List<CellEntry> cells = cellFeed.getEntries();
                int tmp = -1;
                DataRow dataRow = null;
                String workSheetName = workSheet.getTitle().getPlainText();
                DataTable dataTable = new DataTable(workSheetName, extractHeaders(workSheet));
                for (CellEntry cell : cells) {
                    int rowId = TDriverUtil.getRowIndex(cell.getId());
                    if (tmp != rowId) {
                        if (dataRow != null) {
                            dataTable.addRow(dataRow);
                        }
                        dataRow = new DataRow(rowId);
                        tmp = rowId;
                    }
                    if (rowId != 1) {
                        DataCell dataCell = new DataCell(TDriverUtil.getColumnIndex(cell.getId()),
                                cell.getTextContent().getContent().getPlainText());
                        if (dataRow != null) {
                            dataRow.addCell(dataCell);
                        }
                    }
                }
                addTable(dataTable);
            }
        } catch (IOException e) {
            throw new SQLException("Error occurred while retrieving the CellFeed", e);
        } catch (ServiceException e) {
            throw new SQLException("Error occurred while retrieving the CellFeed", e);
        }
    }

    private Map<String, Integer> extractHeaders(WorksheetEntry currentWorkSheet) throws SQLException {
        Map<String, Integer> headers = new HashMap<String, Integer>();

        CellFeed cellFeed = TDriverUtil.getCellFeed(getConnection(), currentWorkSheet);
        for (CellEntry cell : cellFeed.getEntries()) {
            if (!TDriverUtil.getCellPosition(cell.getId()).startsWith("R1")) {
                break;
            }
            headers.put(cell.getTextContent().getContent().getPlainText(),
                    TDriverUtil.getColumnIndex(cell.getId()));
        }
        return headers;
    }

}
