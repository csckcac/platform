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

import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import org.wso2.carbon.dataservices.sql.driver.TDriverUtil;
import org.wso2.carbon.dataservices.sql.driver.TGSpreadConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class GSpreadDataReader extends DataReader {

    public GSpreadDataReader(Connection connection) throws SQLException {
        super(connection);
    }

    public void populateData() throws SQLException {
        int tmp = -1;
        DataRow dataRow = null;

        TGSpreadConnection gsConnection = (TGSpreadConnection) getConnection();
        WorksheetFeed workSheetFeed = gsConnection.getWorksheetFeed();
        if (workSheetFeed == null) {
            throw new SQLException("Work sheet feed it not initialized properly and is null");
        }
        List<WorksheetEntry> workSheets = workSheetFeed.getEntries();
        for (WorksheetEntry workSheet : workSheets) {
            CellFeed cellFeed = TDriverUtil.getCellFeed(gsConnection, workSheet);
            List<CellEntry> cells = cellFeed.getEntries();

            Map<String, Integer> headers = this.extractHeaders(workSheet);
            DataTable result = new FixedDataTable(workSheet.getTitle().getPlainText(), headers);

            for (CellEntry cell : cells) {
                int rowId = TDriverUtil.getRowIndex(cell.getId());
                if (tmp != rowId && rowId != 1) {
                    if (dataRow != null) {
                        result.addRow(this.fillUpEmptyCells(dataRow, headers.values()));
                    }
                    dataRow = new DataRow(rowId - 1);
                    tmp = rowId;
                }
                int columnId = TDriverUtil.getColumnIndex(cell.getId());
                if (columnId > headers.size()) {
                    continue;
                }
                if (rowId != 1 && dataRow != null) {
                    DataCell dataCell =
                            new DataCell(TDriverUtil.getColumnIndex(cell.getId()) - 1,
                                    cell.getContent().getType(),
                                    cell.getTextContent().getContent().getPlainText());

                    dataRow.addCell(dataCell);
                }
            }
            this.addTable(result);
        }
    }

    /**
     * Google gdata-client spreadsheet API only returns the non-empty cells that exist in the
     * spreadsheet document that is being queried. This method fills up the data rows with the
     * dummy cells containing null as cell value in place of the missing empty rows.
     * @param row       Data row to be modified
     * @param columns   Column indices of the header row
     * @return          Processed data row to add empty data cells
     */
    private DataRow fillUpEmptyCells(DataRow row, Collection<Integer> columns) {
        List<Integer> existingColumns = new ArrayList<Integer>();
        for (DataCell cell : row.getCells()) {
            existingColumns.add(cell.getColumnId());
        }
        for (Integer column : columns) {
            if (!existingColumns.contains(column - 1)) {
                row.addCell(new DataCell(column - 1, -1, null));
            }
        }
        return row;
    }

    /**
     * Extracts out the header elements of the spreadsheet entry that is being queried.
     * @param currentWorkSheet  Worksheet being queried
     * @return                  Map containing the header names and their indices
     * @throws SQLException     Is thrown if an error occurs while extracting the spreadsheet
     *                          cell feed 
     */
    private Map<String, Integer> extractHeaders(WorksheetEntry currentWorkSheet) throws
            SQLException {
        Map<String, Integer> headers = new HashMap<String, Integer>();

        CellFeed cellFeed = TDriverUtil.getCellFeed(getConnection(), currentWorkSheet);
        for (CellEntry cell : cellFeed.getEntries()) {
            if (!TDriverUtil.getCellPosition(cell.getId()).startsWith("R1")) {
                break;
            }
            headers.put(cell.getTextContent().getContent().getPlainText(),
                    TDriverUtil.getColumnIndex(cell.getId()) - 1);
        }
        return headers;
    }

}
