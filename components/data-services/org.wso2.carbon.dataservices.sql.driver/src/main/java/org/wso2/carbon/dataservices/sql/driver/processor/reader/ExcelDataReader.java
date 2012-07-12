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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.wso2.carbon.dataservices.sql.driver.TExcelConnection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExcelDataReader implements DataReader {

    private Map<String, DataTable> data;

    private static final Log log = LogFactory.getLog(ExcelDataReader.class);

    public ExcelDataReader(TExcelConnection con) {
        this.data = new HashMap<String, DataTable>();
        this.populateData(con);
    }

    /**
     * Populates config resides in the Excel sheet to the config map
     *
     * @param con Connection to the Excel document
     */
    private void populateData(TExcelConnection con) {
        Workbook workbook = con.getWorkbook();
        int noOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < noOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);

            String sheetName = sheet.getSheetName();
            Map<Integer, String> headers = this.getColumnHeaders(sheet);
            DataTable dataTable = new DataTable(sheetName, headers);

            Iterator<Row> rowItr = sheet.rowIterator();
            while (rowItr.hasNext()) {
                Row row = rowItr.next();
                DataRow dataRow = new DataRow(row.getRowNum());
                Iterator<Cell> cellItr = row.cellIterator();
                while (cellItr.hasNext()) {
                    Cell cell = cellItr.next();
                    DataCell dataCell =
                            new DataCell(cell.getColumnIndex(), cell.getCellType(),
                                    this.extractCellValue(cell));
                    dataRow.addCellToRow(dataCell);
                }
                dataTable.addRowToTable(dataRow);
            }
            this.addTableToDatabase(dataTable);
        }
    }

    /**
     * Extracts the value of a particular cell depending on its type
     *
     * @param cell  A populated Cell instance
     * @return      Value of the cell
     */
    private Object extractCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                return cell.getNumericCellValue();
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();
            default:
                return cell.getStringCellValue();
        }
    }

    /**
     * Adds a populated config table to the config map
     *
     * @param dataTable A populated instance of DataTable
     */
    public void addTableToDatabase(DataTable dataTable) {
        this.getData().put(dataTable.getTableName(), dataTable);
    }

    /**
     * Extracts out the columns in the given excel sheet
     *
     * @param sheet Sheet instance corresponding to the desired Excel sheet
     * @return Map containing the column indices and names
     */
    private Map<Integer, String> getColumnHeaders(Sheet sheet) {
        Map<Integer, String> headers = new HashMap<Integer, String>();
        // Retrieving the first row of the sheet as the header row.
        Row row = sheet.getRow(0);
        if (row != null) {
            Iterator<Cell> itr = row.cellIterator();
            while (itr.hasNext()) {
                Cell c = itr.next();
                if (c.getStringCellValue() != null) {
                    headers.put(c.getColumnIndex(), c.getStringCellValue());
                }
            }
        }
        return headers;
    }

    public Map<String, DataTable> getData() {
        return data;
    }

}
