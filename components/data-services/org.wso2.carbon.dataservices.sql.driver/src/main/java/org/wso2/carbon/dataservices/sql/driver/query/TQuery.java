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
package org.wso2.carbon.dataservices.sql.driver.query;

import org.wso2.carbon.dataservices.sql.driver.parser.SQLParserUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Queue;

public abstract class TQuery {

    private String type;

    private Queue<String> tokens;

    private List<String> expectedColumns;

    private List<String> tablesInvolved;

    public TQuery(String type, Queue<String> tokens) {
        this.type = type;
        this.tokens = tokens;
        init();
    }

    public void init() {
        this.expectedColumns = SQLParserUtil.getSelectedColumns(this.getTokens());
        this.tablesInvolved = SQLParserUtil.getSelectedTables(this.getTokens());
    }
    
    public String getType() {
        return type;
    }

    public Queue<String> getTokens() {
        return tokens;
    }

    public List<String> getExpectedColumns() {
        return expectedColumns;
    }

    public List<String> getTablesInvolved() {
        return tablesInvolved;
    }
    
    public abstract void process() throws SQLException;

}
