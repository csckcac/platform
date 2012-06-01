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
package org.wso2.carbon.rssmanager.core;

import org.wso2.carbon.rssmanager.core.description.DatabaseInstance;
import org.wso2.carbon.rssmanager.core.description.DatabasePermissions;
import org.wso2.carbon.rssmanager.core.description.DatabaseUser;

import java.sql.SQLException;

public class OracleRSSManager implements RSSManager {
    
    public void createDatabase(DatabaseInstance db) throws RSSDAOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void dropDatabase(int dbInsId) throws RSSDAOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void createUser(DatabaseUser user, int privGroupId, int dbInsId) throws RSSDAOException,
            SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void dropUser(int userId, int dbInsId) throws RSSDAOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void editUserPrivileges(DatabasePermissions permissions, DatabaseUser user,
                                   int dbInsId) throws RSSDAOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
