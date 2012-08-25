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

import org.wso2.carbon.dataservices.sql.driver.TDriverUtil;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.parser.ParserUtil;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataReaderFactory;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.carbon.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.carbon.dataservices.sql.driver.query.ConditionalQuery;
import org.wso2.carbon.dataservices.sql.driver.query.ParamInfo;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;

public abstract class UpdateQuery extends ConditionalQuery {

    private String targetTableName;

    private DataTable targetTable;

    private ParamInfo[] targetColumns;

    private ColumnInfo[] columns;

    public UpdateQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.targetColumns = new ParamInfo[getParameters().length];
        preprocessTokens(getProcessedTokens());
        this.targetTable = 
                DataReaderFactory.createDataReader(getConnection()).getData().get(
                        getTargetTableName());
        this.columns = TDriverUtil.getHeaders(stmt.getConnection(), getTargetTableName());
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
        this.targetTableName = tokens.poll();
        //Dropping SET token
        tokens.poll();
        processUpdatedColumns(tokens, 0);
        if (Constants.WHERE.equals(tokens.peek())) {
            throw new SQLException("'WHERE' keyword is expected");
        }
        processUpdatedColumns(tokens, 0);
    }

    private void processUpdatedColumns(Queue<String> tokens,
                                       int targetColCount) throws SQLException {
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        this.targetColumns[targetColCount] = new ParamInfo(targetColCount, tokens.poll());
        //getTargetColumns().put(targetColCount, tokens.poll());
        processColumnValues(tokens, 0, false, false, true);
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
                //this.findParam(valCount).setValue(tokens.poll());
                this.findTargetParam(valCount).setValue(tokens.poll());
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
                this.findTargetParam(valCount).setValue(tokens.poll());
                //this.findParam(valCount).setValue(tokens.poll());
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
                this.findTargetParam(valCount).setValue(tokens.poll());
                //this.findParam(valCount).setValue(tokens.poll());
            }
            if (!Constants.PARAM_VALUE.equals(tokens.peek())) {
                isEnd = true;
            } else {
                tokens.poll();
            }
            processColumnValues(tokens, valCount + 1, isParameterized, isEnd, isInit);
        }
    }
    
    public DataTable getTargetTable() {
        return targetTable;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    public ParamInfo[] getTargetColumns() {
        return targetColumns;
    }

    public ColumnInfo[] getColumns() {
        return columns;
    }

    public ParamInfo findTargetParam(int index) {
        ParamInfo param = null;
        for (ParamInfo paramInfo : getTargetColumns()) {
            if (paramInfo.getOrdinal() == index) {
                param = paramInfo;
                break;
            }
        }
        return param;
    }

    public int extractColumnId(String column) {
        int columnId = -1;
        for (ColumnInfo columnInfo : getColumns()) {
            if (columnInfo.getName().equals(column)) {
                columnId = columnInfo.getId();
            }
        }
        return columnId;
    }

}
