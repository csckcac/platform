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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.wso2.carbon.dataservices.sql.driver.parser.Constants;

/**
 * Fixed data table implementation.
 */
public class FixedDataTable implements DataTable {

    private Map<Integer, DataRow> rows;

    private String tableName;

    private Map<String, Integer> headers;

    public FixedDataTable(String tableName, Map<String, Integer> headers) {
        this.tableName = tableName;
        this.headers = headers;
        this.rows = new HashMap<Integer, DataRow>();
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    @Override
    public Map<String, Integer> getHeaders() {
        return headers;
    }

    @Override
    public Map<Integer, DataRow> getRows() {
        return rows;
    }

    public void setData(Map<Integer, DataRow> rows) {
        this.rows = rows;
    }

    @Override
    public void addRow(DataRow dataRow) {
        getRows().put(dataRow.getRowId(), dataRow);
    }
    
    private void handleEqualCondition(Map<Integer, DataRow> dataRows, int cellId, String value, 
    		String operator) {
    	DataRow tmpRow;
    	double tmpNumberLhs, tmpNumberRhs = 0;
    	boolean isNumber = true;
    	try {
    		tmpNumberRhs = Double.parseDouble(value);
    	} catch (NumberFormatException e) {
    		isNumber = false;
		}
    	for (Iterator<Map.Entry<Integer, DataRow>> itr = dataRows.entrySet().iterator(); 
    	            itr.hasNext();) {
			tmpRow = itr.next().getValue();
			try {
				if (isNumber) {
				    tmpNumberLhs = Double.parseDouble(tmpRow.getCell(
						    cellId).getCellValue().toString());
				    if (!(tmpNumberLhs == tmpNumberRhs)) {
					    itr.remove();
				    }
				    continue;
				}
			} catch (NumberFormatException e) {
				isNumber = false;
			}	
			if (!value.equals(tmpRow.getCell(cellId).getCellValue())) {
				itr.remove();
			}
		}
    }
    
    private void handleLessThanCondition(Map<Integer, DataRow> dataRows, int cellId, String value, 
    		String operator) {
    	double tmpNumberLhs, tmpNumberRhs;
    	try {
    		tmpNumberRhs = Double.parseDouble(value);
    	} catch (NumberFormatException e) {
			dataRows.clear();
			return;
		}
    	DataRow tmpRow;
		for (Iterator<Map.Entry<Integer, DataRow>> itr = dataRows.entrySet().iterator(); 
		            itr.hasNext();) {
			tmpRow = itr.next().getValue();
			try {
				tmpNumberLhs = Double.parseDouble(tmpRow.getCell(
						cellId).getCellValue().toString());
				if (!(tmpNumberLhs < tmpNumberRhs)) {
					itr.remove();
				}
			} catch (NumberFormatException e) {
				itr.remove();
			}
		}
    }
    
    private void handleGreaterThanCondition(Map<Integer, DataRow> dataRows, int cellId, String value, 
    		String operator) {
    	double tmpNumberLhs, tmpNumberRhs;
    	try {
    		tmpNumberRhs = Double.parseDouble(value);
    	} catch (NumberFormatException e) {
			dataRows.clear();
			return;
		}
    	DataRow tmpRow;
		for (Iterator<Map.Entry<Integer, DataRow>> itr = dataRows.entrySet().iterator(); 
		            itr.hasNext();) {
			tmpRow = itr.next().getValue();
			try {
				tmpNumberLhs = Double.parseDouble(tmpRow.getCell(
						cellId).getCellValue().toString());
				if (!(tmpNumberLhs > tmpNumberRhs)) {
					itr.remove();
				}
			} catch (NumberFormatException e) {
				itr.remove();
			}
		}
    }
    
	@Override
	public Map<Integer, DataRow> applyCondition(String column,
			String value, String operator) {
		Map<Integer, DataRow> dataRows = new HashMap<Integer, DataRow>(this.getRows());
		int cellId = this.getHeaders().get(column);
		if (Constants.EQUAL.equals(operator)) {
			this.handleEqualCondition(dataRows, cellId, value, operator);
		} else if (Constants.GREATER_THAN.equals(operator)) {
			this.handleGreaterThanCondition(dataRows, cellId, value, operator);
		} else if (Constants.LESS_THAN.equals(operator)) {
			this.handleLessThanCondition(dataRows, cellId, value, operator);
		} else {
			throw new RuntimeException("Unsupported operator: " + operator);
		}
		return dataRows;
	}

	@Override
	public void updateRows(DataRow... dataRows) {
		for (DataRow dataRow : dataRows) {
			this.getRows().put(dataRow.getRowId(), dataRow);
		}
	}

	@Override
	public void deleteRows(int... rowIds) {
		for (int rowId : rowIds) {
			this.getRows().remove(rowId);
		}
	}

	@Override
	public Map<String, Integer> getHeaderTypes() throws SQLException {
		throw new SQLException("getHeaderTypes() not implemented in FixedDataTable");
	}

}
