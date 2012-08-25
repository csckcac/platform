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
package org.wso2.carbon.dataservices.sql.driver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;

import java.sql.*;
import java.util.Enumeration;
import java.util.Properties;

public class TDriver implements Driver {

    private boolean isFilePath;

    private static final Log log = LogFactory.getLog(Driver.class);

    public boolean isFilePath() {
        return isFilePath;
    }

    static {
        try {
            DriverManager.registerDriver(new TDriver());
        } catch (SQLException e) {
            log.error("Error in registering the driver", e);
        }
    }

    public Connection connect(String url, Properties info) throws SQLException {
        Properties props = getProperties(url, info);
        String conType = props.getProperty(Constants.DATA_SOURCE_TYPE);
        return TConnectionFactory.createConnection(conType, props);
    }

    public boolean acceptsURL(String url) throws SQLException {
        return false;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return 0;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        return false;
    }

    private Properties getProperties(String url, Properties info) throws SQLException {
        if (url == null) {
            throw new SQLException("JDBC URL cannot be NULL");
        }
        Properties props = new Properties();
        for (Enumeration e = info.propertyNames(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = info.getProperty(key);
            if (value != null) {
                props.setProperty(key.toUpperCase(), value);
            }
        }
        int pos = 0;
        StringBuffer token = new StringBuffer();
        pos = getNextTokenPos(url, pos, token);
        if (!Constants.JDBC_PREFIX.equalsIgnoreCase(token.toString())) {
            throw new SQLException("Malformed URL");
        }
        pos = getNextTokenPos(url, pos, token);
        if (!Constants.PROVIDER_PREFIX.equalsIgnoreCase(token.toString())) {
            throw new SQLException("Malformed URL");
        }
        pos = getNextTokenPos(url, pos, token);
        if (!Constants.EXCEL_PREFIX.equalsIgnoreCase(token.toString()) &&
                !Constants.GSPRED_PREFIX.equalsIgnoreCase(token.toString())) {
            throw new SQLException("Malformed URL");
        }
        props.setProperty(Constants.DATA_SOURCE_TYPE, token.toString());
        pos = getNextTokenPos(url, pos, token);
        if (Constants.FILE_PATH.equals(token.toString())) {
            isFilePath = true;
            pos = getNextTokenPos(url, pos, token);
            String propValue = token.toString();
            if (propValue == null || "".equals(propValue)) {
                throw new SQLException("File path attribute is missing");
            }
            props.setProperty(Constants.FILE_PATH, propValue);
        }
        pos = getNextTokenPos(url, pos, token);
        if (Constants.SHEET_NAME.equals(token.toString())) {
             pos = getNextTokenPos(url, pos, token);
            String propValue = token.toString();
            if (propValue == null || "".equals(propValue)) {
                throw new SQLException("File path attribute is missing");
            }
            props.setProperty(Constants.SHEET_NAME, propValue);
        }
        pos = getNextTokenPos(url, pos, token);
        if (Constants.VISIBILITY.equals(token.toString())) {
            pos = getNextTokenPos(url, pos, token);
            props.setProperty(Constants.VISIBILITY, token.toString());
        }
        getNextTokenPos(url, pos, token);
        return props;
    }

    private int getNextTokenPos(String url, int pos, StringBuffer token) {
        token.setLength(0);
        while (pos < url.length()) {
            char c = url.charAt(pos++);
            if (c == ':') {
                if (!isFilePath()) {
                    break;
                }
            }
            if (c == ';') {
                if (isFilePath()) {
                    isFilePath = false;
                    break;
                } else {
                    break;
                }
            }
            if (c == '/') {
                if (!isFilePath()) {
                    break;
                }
            }
            if (c == '=') {
                if (!isFilePath()) {
                    break;
                }
            }
            token.append(c);
        }
        if ("".equals(token.toString()) && pos < url.length()) {
            return getNextTokenPos(url, pos, token);
        }
        return pos;
    }

}
