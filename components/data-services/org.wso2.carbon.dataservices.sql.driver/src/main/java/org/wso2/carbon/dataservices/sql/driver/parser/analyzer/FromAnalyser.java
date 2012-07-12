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
package org.wso2.carbon.dataservices.sql.driver.parser.analyzer;

import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.parser.SQLParserUtil;

import java.sql.SQLException;
import java.util.Queue;

public class FromAnalyser extends SQLKeyWordAnalyser {

    public FromAnalyser(Queue<String> tokens) {
        super(tokens);
    }

    public void analyze() throws SQLException {
        StringBuilder sb;
        if (SQLParserUtil.isStringLiteral(getTokens().peek())) {
            throw new SQLException("Invalid string literal");
        }

        sb = new StringBuilder();
        sb.append(getTokens().poll());

        if (!getTokens().isEmpty()) {
            if (getTokens().peek().equals(Constants.COMMA)) {
                getProcessedTokens().add(Constants.TABLE);
                getProcessedTokens().add(sb.toString());
                getTokens().poll();
                analyze();
            } else if (getTokens().peek().equals(Constants.DOT)) {
                getTokens().poll();
                analyze();
            } else {
                throw new SQLException("Error-while analysing for DOT or COMMA");
            }
        } else {
            getProcessedTokens().add(Constants.TABLE);
            getProcessedTokens().add(sb.toString());
        }
    }
    
}

