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
package org.wso2.carbon.dataservices.sql.driver.query.select;

import org.wso2.carbon.dataservices.sql.driver.TDriverUtil;
import org.wso2.carbon.dataservices.sql.driver.TResultSet;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;
import org.wso2.carbon.dataservices.sql.driver.parser.ParserUtil;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataReaderFactory;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataRow;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.DataTable;
import org.wso2.carbon.dataservices.sql.driver.processor.reader.FixedDataTable;
import org.wso2.carbon.dataservices.sql.driver.query.ColumnInfo;
import org.wso2.carbon.dataservices.sql.driver.query.ConditionalQuery;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public abstract class SelectQuery extends ConditionalQuery {

    private List<ColumnInfo> targetColumns;

    private String targetTableName;

    private DataTable targetTable;

    private ColumnInfo[] columns;

    public SelectQuery(Statement stmt) throws SQLException {
        super(stmt);
        this.targetColumns = new ArrayList<ColumnInfo>();
        this.preProcessTokens(getProcessedTokens());
        this.targetTable = DataReaderFactory.createDataReader(getConnection()).getDataTable(
                getTargetTableName());
        this.columns = TDriverUtil.getHeaders(stmt.getConnection(), getTargetTableName());
        this.preProcessColumns(getColumns(), getTargetColumns());
    }

    @Override
    public int executeUpdate() throws SQLException {
        throw new SQLException("'executeUpdate() is only allowed to be used with DML statements " +
                "such as INSERT, UPDATE and DELETE");
    }

    public synchronized ResultSet executeSQL() throws SQLException {
        Map<Integer, DataRow> result;
        DataTable table = new FixedDataTable(getTargetTableName(), getTargetTable().getHeaders());
        if (getCondition().getLhs() == null && getCondition().getRhs() == null) {
            result = getTargetTable().getRows();
        } else {
            result = getCondition().process(getTargetTable());
        }
        table.setData(result);
        return new TResultSet(getStatement(), table, getTargetColumns());
    }

    private void preProcessColumns(ColumnInfo[] columns, ColumnInfo[] targetColumns) {
        int columnIndex = 0;
        for (ColumnInfo column : columns) {
            if (columnIndex < getTargetColumns().length) {
                for (ColumnInfo targetColumn : targetColumns) {
                    if (column.getName().equals(targetColumn.getName())) {
                        targetColumn.setSqlType(column.getSqlType());
                        columnIndex++;
                    }
                }
            }
        }
    }

    private void preProcessTokens(Queue<String> tokens) throws SQLException {
        //Drops SELECT keyword
        tokens.poll();
        if (Constants.ASTERISK.equalsIgnoreCase(tokens.peek())) {
            this.targetColumns = Arrays.asList(getColumns()); 
        } else {
            processTargetColumns(tokens, 0);
        }
        //Drops FROM keyword
        tokens.poll();
        if (!Constants.TABLE.equals(tokens.peek())) {
            throw new SQLException("'TABLE' keyword is expected");
        }
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        targetTableName = tokens.poll();
        if (tokens.isEmpty()) {
            return;
        }
        if (!Constants.WHERE.equalsIgnoreCase(tokens.peek())) {
            throw new SQLException("Syntax Error : 'WHERE' keyword is expected");
        }
        //Removing WHERE keyword
        tokens.poll();
        this.processConditions(tokens, getCondition());
    }

    private void processTargetColumns(Queue<String> tokens, int paramCount) throws SQLException {
        if (!Constants.COLUMN.equals(tokens.peek())) {
            throw new SQLException("Syntax Error : 'COLUMN' keyword is expected");
        }
        tokens.poll();
        if (!ParserUtil.isStringLiteral(tokens.peek())) {
            throw new SQLException("Syntax Error : String literal is expected");
        }
        targetColumns.add(new ColumnInfo(tokens.poll()));
        if (Constants.COLUMN.equals(tokens.peek())) {
            processTargetColumns(tokens, paramCount + 1);
        }
    }

    public ColumnInfo[] getTargetColumns() {
        return targetColumns.toArray(new ColumnInfo[targetColumns.size()]);
    }

    public DataTable getTargetTable() {
        return targetTable;
    }

    public String getTargetTableName() {
        return targetTableName;
    }

    private ColumnInfo[] getColumns() {
        return columns;
    }

}
