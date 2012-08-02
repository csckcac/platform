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
package org.wso2.carbon.dataservices.sql.driver;

import com.google.gdata.client.spreadsheet.CellQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.carbon.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.carbon.dataservices.sql.driver.query.QueryFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TDriverUtil {

    public static ColumnInfo[] getHeaders(Connection connection,
                                          String tableName) throws SQLException {
        if (!(connection instanceof TConnection)) {
            throw new SQLException("Invalid connection type");
        }
        String connectionType = ((TConnection) connection).getType();
        QueryFactory.QueryTypes type =
                QueryFactory.QueryTypes.valueOf(connectionType.toUpperCase());
        switch (type) {
            case EXCEL:
                return getExcelHeaders(connection, tableName);
            case GSPREAD:
                return getGSpreadHeaders(connection, tableName);
            default:
                throw new SQLException("Invalid query type");
        }
    }

    private static ColumnInfo[] getExcelHeaders(Connection connection,
                                               String tableName) throws SQLException {
        if (!(connection instanceof TExcelConnection)) {
            throw new SQLException("Invalid connection type");
        }
        Workbook workbook = ((TExcelConnection) connection).getWorkbook();
        if (workbook == null) {
            throw new SQLException("TExcelConnection is not properly initialized");
        }
        Sheet sheet = workbook.getSheet(tableName);
        if (sheet == null) {
            throw new SQLException("Sheet '" + tableName + "' does not exist");
        }
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
        for (Cell header : sheet.getRow(0)) {
            ColumnInfo column = new ColumnInfo(header.getStringCellValue());
            column.setTableName(tableName);
            column.setSqlType(header.getCellType());
            column.setIndex(header.getColumnIndex());

            columns.add(column);
        }
        return columns.toArray(new ColumnInfo[columns.size()]);
    }

    private static ColumnInfo[] getGSpreadHeaders(Connection connection,
                                                 String sheetName) throws SQLException {
        WorksheetEntry currentWorksheet;
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();

        if (!(connection instanceof TGSpreadConnection)) {
            throw new SQLException("Invalid connection type");
        }
        currentWorksheet = getCurrentWorkSheetEntry(connection, sheetName);
        if (currentWorksheet == null) {
            throw new SQLException("Worksheet '" + sheetName + "' does not exist");
        }
        CellFeed cellFeed = getCellFeed(connection, currentWorksheet);
        for (CellEntry cell : cellFeed.getEntries()) {
            if (!getCellPosition(cell.getId()).startsWith("R1")) {
                break;
            }
            ColumnInfo column =
                    new ColumnInfo(cell.getTextContent().getContent().getPlainText());
            column.setTableName(sheetName);
            column.setSqlType(cell.getContent().getType());
            column.setIndex(getColumnIndex(cell.getId()));
            columns.add(column);
        }
        return columns.toArray(new ColumnInfo[columns.size()]);
    }

    public static int getColumnIndex(String id) {
        String tmp = getCellPosition(id);
        id = tmp.substring(tmp.indexOf("C"), tmp.length()).substring(1);
        return Integer.parseInt(id);
    }

    public static int getRowIndex(String id) {
        String tmp = getCellPosition(id);
        id = tmp.substring(tmp.indexOf("R") + 1, tmp.indexOf("C"));
        return Integer.parseInt(id);
    }

    public static String getCellPosition(String id) {
        return id.substring(id.lastIndexOf("/") + 1);
    }

    public static CellFeed getCellFeed(Connection connection,
                                       WorksheetEntry currentWorkSheet) throws SQLException {
        CellFeed cellFeed;
        try {
            SpreadsheetService service = ((TGSpreadConnection) connection).getSpreadSheetService();
            CellQuery cellQuery = new CellQuery(currentWorkSheet.getCellFeedUrl());
            cellFeed = service.query(cellQuery, CellFeed.class);
        } catch (IOException e) {
            throw new SQLException("Error occurred while retrieving the CellFeed", e);
        } catch (ServiceException e) {
            throw new SQLException("Error occurred while retrieving the CellFeed", e);
        }
        return cellFeed;
    }

    public static WorksheetEntry getCurrentWorkSheetEntry(Connection connection,
                                                          String sheetName) throws SQLException {
        WorksheetEntry currentWorkSheet = null;
        WorksheetFeed feed = ((TGSpreadConnection) connection).getWorkSheetFeed();
        if (feed == null) {
            throw new SQLException("TGSpreadConnection is not properly initialized");
        }
        List<WorksheetEntry> worksheets = feed.getEntries();
        for (WorksheetEntry entry : worksheets) {
            if (sheetName.equals(entry.getTitle().getPlainText())) {
                currentWorkSheet = entry;
            }
        }
        return currentWorkSheet;
    }


}
