/*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.dataservices.sql.driver.query.insert.ExcelInsertQuery;
import org.wso2.carbon.dataservices.sql.driver.query.insert.GSpreadInsertQuery;
import org.wso2.carbon.dataservices.sql.driver.query.select.ExcelSelectQuery;
import org.wso2.carbon.dataservices.sql.driver.query.select.GSpreadSelectQuery;
import org.wso2.carbon.dataservices.sql.driver.query.update.ExcelUpdateQuery;
import org.wso2.carbon.dataservices.sql.driver.query.update.GSpreadUpdateQuery;

import java.sql.SQLException;
import java.sql.Statement;

public class QueryFactory {

    public enum QueryFactoryTypes {
        SELECT, INSERT, UPDATE, DELETE, CREATE
    }

    public enum QueryTypes {
        EXCEL, GSPREAD
    }

    public static Query createQuery(Statement stmt) throws SQLException {
        String queryType = ((TPreparedStatement)stmt).getQueryType();
        QueryFactoryTypes types = QueryFactoryTypes.valueOf(queryType);
        switch (types)  {
            case SELECT:
                return createSelectQuery(stmt);
            case INSERT:
                return createInsertQuery(stmt);
            case UPDATE:
                return createUpdateQuery(stmt);
            case DELETE:
                break;
            case CREATE:
                break;
            default:
                break;
        }
        return null;
    }

    public static Query createInsertQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection)(((TPreparedStatement)stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelInsertQuery(stmt);
            case GSPREAD:
                return new GSpreadInsertQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

    private static Query createSelectQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection)(((TPreparedStatement)stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelSelectQuery(stmt);
            case GSPREAD:
                return new GSpreadSelectQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

    private static Query createUpdateQuery(Statement stmt) throws SQLException {
        String connectionType =
                ((TConnection)(((TPreparedStatement)stmt).getConnection())).getType();
        QueryTypes types = QueryTypes.valueOf(connectionType);
        switch (types) {
            case EXCEL:
                return new ExcelUpdateQuery(stmt);
            case GSPREAD:
                return new GSpreadUpdateQuery(stmt);
            default:
                throw new SQLException("Unsupported type");
        }
    }

}
