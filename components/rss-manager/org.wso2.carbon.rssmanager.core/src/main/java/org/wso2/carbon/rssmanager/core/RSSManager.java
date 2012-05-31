package org.wso2.carbon.rssmanager.core;

import org.wso2.carbon.rssmanager.core.RSSDAOException;
import org.wso2.carbon.rssmanager.core.description.DatabaseInstance;
import org.wso2.carbon.rssmanager.core.description.DatabasePermissions;
import org.wso2.carbon.rssmanager.core.description.DatabaseUser;

import java.sql.SQLException;

public interface RSSManager {

    public void createDatabase(DatabaseInstance db) throws RSSDAOException;

    public void dropDatabase(int dbInsId) throws RSSDAOException;

    public void createUser(DatabaseUser user, int privGroupId, int dbInsId) throws RSSDAOException,
            SQLException;

    public void dropUser(int userId, int dbInsId) throws RSSDAOException;

    public void editUserPrivileges(DatabasePermissions permissions, DatabaseUser user, int dbInsId)
            throws RSSDAOException;

}
