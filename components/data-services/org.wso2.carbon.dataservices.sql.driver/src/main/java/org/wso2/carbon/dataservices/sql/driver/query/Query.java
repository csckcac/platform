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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Queue;

public abstract class Query {

    private Connection connection;

    private String type;

    private Queue<String> processedSQL;

    private ParamInfo[] parameters;

    public Query(Connection connection, Queue<String> processedTokens,
                        ParamInfo[] parameters) {
        this.connection = connection;
        this.type = processedTokens.peek();
        this.processedSQL = processedTokens;
        this.parameters = parameters;
    }

    public abstract ResultSet executeQuery() throws SQLException;

    public abstract int executeUpdate() throws SQLException;

    public abstract boolean execute() throws SQLException;

    public Connection getConnection() {
        return connection;
    }

    public String getType() {
        return type;
    }

    public Queue<String> getProcessedSQL() {
        return processedSQL;
    }

    public ParamInfo[] getParameters() {
        return parameters;
    }

}
