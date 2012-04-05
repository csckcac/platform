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
package org.wso2.carbon.rssmanager.core.dao;

import org.wso2.carbon.rssmanager.core.description.*;
import org.wso2.carbon.rssmanager.core.exception.RSSDAOException;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for WSO2 RSS based database operations.
 */
public interface RSSDAO {

	public void addRSSInstance(RSSInstance rssInst) throws RSSDAOException;
	
	public List<RSSInstanceEntry> getAllTenantSpecificRSSInstances() throws RSSDAOException;
	
	public List<RSSInstance> getAllServiceProviderHostedRSSInstances() throws RSSDAOException;
	
	public void deleteRSSInstance(int rssInstanceId) throws RSSDAOException;
	
	public void updateRSSInstance(RSSInstance rssInst) throws RSSDAOException;

	public void addDatabaseInstance(DatabaseInstance dbInst) throws RSSDAOException;
	
	public void updateDatabaseInstance(DatabaseInstance dbInst) throws RSSDAOException;
	
	public List<DatabaseInstanceEntry> getAllDatabaseInstanceEntries() throws RSSDAOException;

	public List<DatabaseInstance> getAllDatabaseInstancesByRSSInstanceId(
            int rssInsId) throws RSSDAOException;

	public void deleteDatabaseInstance(int databaseInstanceId) throws RSSDAOException;

	public int addUser(DatabaseUser user) throws RSSDAOException;

	public void deleteUser(int userId) throws RSSDAOException;
	
	public void updateUser(DatabaseUser user) throws RSSDAOException;
	
	public void addUserDatabaseEntry(UserDatabaseEntry userDBEntry) throws RSSDAOException;
	
	public void updateUserDatabaseEntry(UserDatabaseEntry userDBEntry) throws RSSDAOException;
	
	public void deleteUserDatabaseEntry(int userId, int databaseInstanceId) throws RSSDAOException;
	
	public void incrementServiceProviderHostedRSSDatabaseInstanceCount() throws RSSDAOException;
	
	public int getServiceProviderHostedRSSDatabaseInstanceCount() throws RSSDAOException;

    public List<RSSInstanceEntry> getAllRSSInstances() throws RSSDAOException;

    public RSSInstanceEntry getRSSInstanceEntry(int rssInstId) throws RSSDAOException;

    public RSSInstance getRSSInstanceById(int rssInstId) throws RSSDAOException;

    public List<DatabaseUserEntry> getUsersByDatabaseInstanceId(int dbInstId) throws RSSDAOException;

    public DatabaseInstanceEntry getDatabaseInstanceEntryById(int dbInsId) throws RSSDAOException;

    public DatabaseInstance getDatabaseInstanceById(int dbInsId) throws RSSDAOException;

    public DatabaseUserEntry getUserEntry(int userId) throws RSSDAOException;

    public DatabaseUser getUserById(int userId) throws RSSDAOException;

    public List<UserDatabaseEntry> getUserDatabaseEntriesByDatabaseInstanceId(
            int dbInsId) throws RSSDAOException;

    public Map<String, Object> getUserDatabasePermissions(
            int userId, int databaseInstanceId) throws RSSDAOException;

    public void updateUser(DatabasePermissions modifiedPermissions, int userId,
                                              int dbInsId) throws RSSDAOException;

    public void addUserPrivilegeGroup(PrivilegeGroup privGroup) throws RSSDAOException;

    public void removeUserPrivilegeGroup(int privGroupId) throws RSSDAOException;

    public void editUserPrivilegeGroup(PrivilegeGroup privGroup) throws RSSDAOException;

    public List<PrivilegeGroup> getAllUserPrivilegeGroups() throws RSSDAOException;

    public PrivilegeGroup getPrivilegeGroupById(int privGroupId) throws RSSDAOException;

    public List<DatabaseInstance> getTenantsDatabaseInstanceList() throws RSSDAOException;

    public List<DatabaseInstanceEntry> getAllTenantSpecificDatabaseInstanceEntries()
            throws RSSDAOException;

    public RSSInstance getRSSInstanceDataById(int rssInsId) throws RSSDAOException;
}
