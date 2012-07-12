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

import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.parser.analyzer.InsertAnalyser;
import org.wso2.carbon.dataservices.sql.driver.parser.analyzer.SQLKeyWordAnalyser;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class TInsertQuery extends TQuery {

    private String targetTable;

    private SQLKeyWordAnalyser analyzer;

    public TInsertQuery(String type, Queue<String> tokens) {
        super(type, tokens);
        this.analyzer = new InsertAnalyser(tokens);
        try {
            this.getAnalyzer().analyze();
            Queue<String> processedTokens = this.getAnalyzer().getProcessedTokens();
            this.targetTable =
                    this.extractTargetTableName(processedTokens);
            System.out.println("Target Table : " + this.getTargetTable());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String extractTargetTableName(Queue<String> tokens) {
        while(tokens.size() > 0) {
            if (Constants.TABLE.equals(tokens.peek())) {
                tokens.poll();
                return tokens.poll();
            }
        }
        return null;
    }

    private Map<String, String> extractColumnValuePairs(Queue<String> tokens) {
        Map<String, String> params = new HashMap<String, String>();
        while(tokens.size() > 0) {
            if (Constants.COLUMNS.equals(tokens.peek())) {
                tokens.poll();

            }
        }
        return params;
    }

    public SQLKeyWordAnalyser getAnalyzer() {
        return analyzer;
    }

    @Override
    public void process() throws SQLException {
        
    }

    public String getTargetTable() {
        return targetTable;
    }

}
