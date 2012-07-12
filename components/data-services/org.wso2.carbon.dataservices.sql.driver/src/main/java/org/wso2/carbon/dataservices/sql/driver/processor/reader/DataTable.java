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

import java.util.HashMap;
import java.util.Map;

public class DataTable {

    private int noOfColumns;

    private Map<Integer, DataRow> rows;

    private String tableName;

    private Map<Integer, String> headers;

    public DataTable(String tableName, Map<Integer, String> headers) {
        this.tableName = tableName;
        this.headers = headers;
        this.rows = new HashMap<Integer, DataRow>();
        this.noOfColumns = this.getheaders().size();
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<Integer, String> getheaders() {
        return headers;
    }

    public void setheaders(Map<Integer, String> headers) {
        this.headers = headers;
    }

    public int getNoOfColumns() {
        return noOfColumns;
    }

    public void setNoOfColumns(int noOfColumns) {
        this.noOfColumns = noOfColumns;
    }

    public Map<Integer, DataRow> getRows() {
        return rows;
    }

    public void setData(Map<Integer, DataRow> rows) {
        this.rows = rows;
    }

    public void addRowToTable(DataRow dataRow) {
        this.getRows().put(dataRow.getRowID(), dataRow);
    }

}
