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
package org.wso2.carbon.dataservices.sql.driver.query.insert;

import org.wso2.carbon.dataservices.sql.driver.TConnection;
import org.wso2.carbon.dataservices.sql.driver.query.ParamInfo;
import org.wso2.carbon.dataservices.sql.driver.query.Query;
import org.wso2.carbon.dataservices.sql.driver.query.QueryFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Queue;

public class InsertQueryFactory implements QueryFactory {

    public enum InsertQueryTypes {
        EXCEL, GSPREAD
    }

    public Query createQuery(Connection connection, Queue<String> processedTokens,
                             ParamInfo[] parameters) throws SQLException {
        if (!(connection instanceof TConnection)) {
            throw new SQLException("Unsupported connection type");
        }
        String connectionType = ((TConnection) connection).getType();
        InsertQueryTypes types = InsertQueryTypes.valueOf(connectionType.toUpperCase());
        switch (types) {
            case EXCEL:
                return new ExcelInsertQuery(connection, processedTokens, parameters);
            case GSPREAD:
                return new GSpreadInsertQuery(connection, processedTokens, parameters);
            default:
                throw new SQLException("Unsupported type");
        }
    }

}
