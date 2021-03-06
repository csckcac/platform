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
package org.wso2.carbon.dataservices.sql.driver.processor.reader;

import java.sql.SQLException;
import java.util.Map;

/**
 * This interface represents a data table the parser will work on.
 */
public interface DataTable {

	String getTableName();
	
	Map<String, Integer> getHeaders() throws SQLException;
	
	Map<String, Integer> getHeaderTypes() throws SQLException;
	
	Map<Integer, DataRow> getRows() throws SQLException;
		
	void addRow(DataRow dataRow) throws SQLException;
	
	void updateRows(DataRow... dataRows) throws SQLException;
	
	void deleteRows(int... rowIds) throws SQLException;
	
	Map<Integer, DataRow> applyCondition(String column, String value, 
			String operator) throws SQLException;
	
}
