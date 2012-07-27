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
package org.wso2.carbon.dataservices.sql.driver;

import com.google.gdata.client.GoogleAuthTokenFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dataservices.sql.driver.parser.Constants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

public class TGSpreadConnection extends TConnection {

    private static final Log log = LogFactory.getLog(TExcelConnection.class);

    private WorksheetFeed worksheetFeed;

    private String visibility = "private";

    private SpreadsheetService service;

    public TGSpreadConnection(Properties props) throws SQLException {
        super(props);
        this.visibility = props.getProperty(Constants.VISIBILITY);
        this.service = new SpreadsheetService(Constants.SPREADSHEET_SERVICE_NAME);
        this.service.setCookieManager(null);
        try {
            this.service.setUserCredentials(this.getUsername(), this.getPassword());
            this.service.setUserToken(((GoogleAuthTokenFactory.UserToken)
                    service.getAuthTokenFactory().
                    getAuthToken()).getValue());
        } catch (AuthenticationException e) {
            throw new SQLException("Error occurred while authenicating user to access the " +
                    "spread sheet");
        }
        String key = extractKey(this.getPath());
        this.worksheetFeed = generateWorksheetFeedURL(key);
    }

    public SpreadsheetService getSpreadSheetService() {
        return service;
    }

    public String getVisibility() {
        return visibility;
    }

    public WorksheetFeed getWorkSheetFeed() {
        return worksheetFeed;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String s) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String s) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i, int i1) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String s, int i, int i1) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i, int i1, int i2) throws SQLException {
        return null;
    }

    @Override
    public CallableStatement prepareCall(String s, int i, int i1, int i2) throws SQLException {
        throw new SQLFeatureNotSupportedException("CallableStatements are not supported");
    }

    @Override
    public PreparedStatement prepareStatement(String s, int i) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String s, int[] ints) throws SQLException {
        return null;
    }

    @Override
    public PreparedStatement prepareStatement(String s, String[] strings) throws SQLException {
        return null;
    }

    private static String extractKey(String documentURL) throws SQLException {
        URI documentURI;
        try {
            documentURI = new URI(documentURL);
        } catch (URISyntaxException e) {
            String msg = "Document URL Syntax error:" + documentURL;
            log.warn(msg, e);
            throw new SQLException(msg, e);
        }
        String extractedQuery = documentURI.getQuery();
        int i1 = extractedQuery.lastIndexOf("key=");
        int i2 = extractedQuery.indexOf("&", i1);
        if (i2 < 0) {
            return extractedQuery.substring(i1 + 4);
        } else {
            return extractedQuery.substring(i1 + 4, i2);
        }
    }

    private WorksheetFeed generateWorksheetFeedURL(String key) throws SQLException {
        WorksheetFeed worksheetFeed;
        try {
            URL worksheetFeedUrl = new URL(Constants.BASE_WORKSHEET_URL + key + "/" +
                    this.getVisibility() + "/basic");
            worksheetFeed =
                    this.getSpreadSheetService().getFeed(worksheetFeedUrl, WorksheetFeed.class);
        } catch (MalformedURLException e) {
            throw new SQLException("Error occurred while generating the worksheet feed URL");
        } catch (ServiceException e) {
            throw new SQLException("Error occurred while retrieving the worksheet feed");
        } catch (IOException e) {
            throw new SQLException("Error occurred while retrieving the worksheet feed");
        }
        return worksheetFeed;
    }


}
