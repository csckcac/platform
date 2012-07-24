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
package org.wso2.carbon.rssmanager.core.internal.manager;

import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.entity.Database;
import org.wso2.carbon.rssmanager.core.entity.DatabasePrivilegeSet;
import org.wso2.carbon.rssmanager.core.entity.DatabaseUser;

public class OracleRSSManager extends RSSManager {

    private static RSSManager rssManager = new OracleRSSManager();

    public static synchronized RSSManager getRSSManager() {
        return rssManager;
    }

    @Override
    public void createDatabase(Database db) throws RSSManagerException {
        
    }

    @Override
    public void dropDatabase(String rssInstanceName, String name) throws RSSManagerException {
        
    }

    @Override
    public void createDatabaseUser(DatabaseUser databaseUser) throws
            RSSManagerException {

    }

    @Override
    public void dropDatabaseUser(String rssInstanceName, String username) throws
            RSSManagerException {

    }

    @Override
    public void editDatabaseUserPrivileges(DatabasePrivilegeSet privileges, DatabaseUser user,
                                   String databaseName) throws RSSManagerException {
        
    }

    @Override
    public void attachUserToDatabase(String rssInstanceName, String databaseName, String username,
                                     String templateName) throws RSSManagerException {

    }

    @Override
    public void detachUserFromDatabase(String rssInstanceName, String databaseName,
                                       String username) throws RSSManagerException {

    }
    
}
