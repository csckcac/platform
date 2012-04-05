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
package org.wso2.salesforce.webapp.salesforce;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.salesforce.webapp.constants.SalesForceWebAppConstants;
import org.wso2.salesforce.webapp.exception.SalesForceWebAppException;

/**
 * Handles loggin in and out functionalities offered by salesforce api.
 */
public class SalesForceLoginManager {

    private PartnerConnection conn;
    private static final Log log = LogFactory.getLog(SalesForceLoginManager.class);

    /**
     * Logs in to sales force and obtain a partner connection
     *
     * @param username salesforce username
     * @param password salesforce password
     * @throws SalesForceWebAppException webapp exception
     */
    public void login(String username, String password) throws SalesForceWebAppException {
        if (conn != null) {
            try {
                conn.logout();
            } catch (ConnectionException e) {
                log.error(e);
                throw new SalesForceWebAppException("Unable to login", e);
            }
        }
        conn = createConnection(username, password);
    }

    /**
     * Logs out from the sales force connection
     *
     * @throws SalesForceWebAppException webapp exception
     */
    public void logout() throws SalesForceWebAppException {
        try {
            conn.logout();
        } catch (ConnectionException e) {
            log.error(e);
            throw new SalesForceWebAppException("Unable to logout", e);
        }
    }

    /**
     * Creates a partner connection to query data from salesforce.
     *
     * @param username salesforce username
     * @param password salesforce password
     * @return a partner connection obtained from salesforce
     * @throws SalesForceWebAppException webapp exception
     */
    private PartnerConnection createConnection(String username, String password) throws
            SalesForceWebAppException {
        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(username);
        config.setPassword(password);
        config.setAuthEndpoint(SalesForceWebAppConstants.AUTH_END_POINT);
        config.setCompression(true);
        try {
            return Connector.newConnection(config);
        } catch (ConnectionException e) {
            throw new SalesForceWebAppException("Unable to create partner connection", e);
        }
    }

    /**
     * Getter for the partner connection object
     *
     * @return partner connection obtained from salesforce
     */
    public PartnerConnection getConnection() {
        return conn;
    }

}
