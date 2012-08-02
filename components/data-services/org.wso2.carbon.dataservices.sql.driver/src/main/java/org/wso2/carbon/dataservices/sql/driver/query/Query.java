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
package org.wso2.carbon.dataservices.sql.driver.query;

import org.wso2.carbon.dataservices.sql.driver.TConnection;
import org.wso2.carbon.dataservices.sql.driver.TPreparedStatement;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;


public abstract class Query {

    private Statement stmt;

    private String queryType;

    private String connectionType;

    private Connection connection;

    private Queue<String> processedTokens;

    private ParamInfo[] parameters;
    
    public Query(Statement stmt) throws SQLException {
        this.stmt = stmt;
        this.connection = stmt.getConnection();
        this.connectionType = ((TConnection)getConnection()).getType();
        this.processedTokens = ((TPreparedStatement)getStatement()).getProcessedTokens();
        this.parameters = ((TPreparedStatement)getStatement()).getParameters();
        this.queryType = ((TPreparedStatement)getStatement()).getQueryType();
    }

    public abstract ResultSet executeQuery() throws SQLException;

    public abstract int executeUpdate() throws SQLException;

    public abstract boolean execute() throws SQLException;

    public Statement getStatement() {
        return stmt;
    }

    public String getQueryType() {
        return queryType;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public Connection getConnection() {
        return connection;
    }

    public Queue<String> getProcessedTokens() {
        return processedTokens;
    }

    public ParamInfo[] getParameters() {
        return parameters;
    }

}
