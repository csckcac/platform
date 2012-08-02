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
package org.wso2.carbon.dataservices.sql.driver.query.update;

import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.parser.ParserUtil;
import org.wso2.carbon.dataservices.sql.driver.query.Query;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public abstract class UpdateQuery extends Query {

    private String targetTable;

    private Map<Integer, String> targetColumns;

    private Map<Integer, Object> targetColumnValues;

    private Map<String, Object> conditions;

    public UpdateQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.targetColumns = new HashMap<Integer, String>();
        this.targetColumnValues = new HashMap<Integer, Object>();
        this.conditions = conditions;

        preprocessTokens(getProcessedTokens());
    }

    private void preprocessTokens(Queue<String> tokens) throws SQLException {
        //Dropping UPDATE token
        tokens.poll();
        if (!Constants.TABLE.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : 'TABLE' keyword is expected");
        }
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        this.targetTable = tokens.poll();
        //Dropping SET token
        tokens.poll();
        processUpdatedColumns(tokens, 0);
        if (Constants.WHERE.equals(tokens.peek())) {
            throw new SQLException("'WHERE' keyword is expected");
        }
        processUpdatedColumns(tokens, 0);
    }

    private void processUpdatedColumns(Queue<String> tokens, int targetColCount) throws SQLException {
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        getTargetColumns().put(targetColCount, tokens.poll());
        processColumnValues(tokens, 0, false, false, true);
    }

    private void processConditions(Queue<String> tokens, int targetCount) {
        
    }

    private void processColumnValues(Queue<String> tokens,
                                     int valCount,
                                     boolean isParameterized,
                                     boolean isEnd, boolean isInit) throws SQLException {
        if (!isEnd) {
            if (!Constants.PARAM_VALUE.equals(tokens.peek())) {
                throw new SQLException("Syntax Error : 'PARAM_VALUE' is expected");
            }
            tokens.poll();
            if (!ParserUtil.isStringLiteral(tokens.peek())) {
                throw new SQLException("Syntax Error : String literal expected");
            }
            if ("?".equals(tokens.peek())) {
                if (isInit) {
                    isParameterized = true;
                    isInit = false;
                }
                if (!isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                isParameterized = true;
                this.getTargetColumnValues().put(valCount, tokens.poll());
            } else if (Constants.SINGLE_QUOTATION.equals(tokens.peek())) {
                if (isInit) {
                    isInit = false;
                    isParameterized = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                tokens.poll();
                StringBuilder b = new StringBuilder();
                while (Constants.SINGLE_QUOTATION.equals(tokens.peek()) || tokens.isEmpty()) {
                    b.append(tokens.poll());
                }
                this.getTargetColumnValues().put(valCount, b.toString());
                tokens.poll();
            } else if (ParserUtil.isStringLiteral(tokens.peek())) {
                if (isInit) {
                    isInit = false;
                    isParameterized = false;
                }
                if (isParameterized) {
                    throw new SQLException("Both parameters and inline parameter values are not " +
                            "allowed to exist together");
                }
                this.getTargetColumnValues().put(valCount, tokens.poll());
            }
            if (!Constants.PARAM_VALUE.equals(tokens.peek())) {
                isEnd = true;
            } else {
                tokens.poll();
            }
            processColumnValues(tokens, valCount + 1, isParameterized, isEnd, isInit);
        }
    }

    public String getTargetTable() {
        return targetTable;
    }

    public Map<Integer, String> getTargetColumns() {
        return targetColumns;
    }

    public Map<Integer, Object> getTargetColumnValues() {
        return targetColumnValues;
    }

}
