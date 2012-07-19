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
package org.wso2.carbon.rssmanager.core.internal.dao;

import org.wso2.carbon.rssmanager.core.RSSManagerException;
import org.wso2.carbon.rssmanager.core.internal.dao.entity.*;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for WSO2 RSS based database operations.
 */
public interface RSSDAO {

	public void createRSSInstance(RSSInstance rssInstance) throws RSSManagerException;
	
	public List<RSSInstance> getAllTenantSpecificRSSInstances() throws RSSManagerException;
	
	public List<RSSInstance> getAllSystemRSSInstances() throws RSSManagerException;
	
	public void dropRSSInstance(String rssInstanceName) throws RSSManagerException;
	
	public void updateRSSInstance(RSSInstance rssInstance) throws RSSManagerException;

	public void createDatabase(Database database) throws RSSManagerException;

	public List<Database> getAllDatabases(int tid) throws RSSManagerException;

	public List<Database> getAllDatabasesByRSSInstance(String rssInstanceName) throws
            RSSManagerException;

	public void dropDatabase(String rssInstanceName, String databaseName) throws RSSManagerException;

	public void createDatabaseUser(DatabaseUser user) throws RSSManagerException;

	public void dropDatabaseUser(String rssInstanceName, String username) throws RSSManagerException;
	
	public void updateDatabaseUser(DatabaseUser user) throws RSSManagerException;

	public void addUserDatabaseEntry(UserDatabaseEntry userDBEntry) throws RSSManagerException;
	
	public void updateUserDatabaseEntry(UserDatabaseEntry userDBEntry) throws RSSManagerException;
	
	public void deleteUserDatabaseEntry(String rssInstanceName, String username) throws RSSManagerException;
	
	public void incrementSystemRSSDatabaseCount() throws RSSManagerException;
	
	public int getSystemRSSDatabaseCount() throws RSSManagerException;

    public List<RSSInstance> getAllRSSInstances(int tid) throws RSSManagerException;

    public RSSInstance getRSSInstance(String rssInstanceName) throws
            RSSManagerException;

    public List<DatabaseUser> getUsersByDatabase(
            String databaseName) throws RSSManagerException;

    public Database getDatabase(String databaseName) throws RSSManagerException;

    public DatabaseUser getDatabaseUser(String username) throws RSSManagerException;

    public List<UserDatabaseEntry> getUserDatabaseEntriesByDatabase(String rssInstanceName,
            String databaseName) throws RSSManagerException;

    public Map<String, Object> getUserDatabasePermissions(
            String username, String databaseName) throws RSSManagerException;

    public void updateDatabaseUser(DatabasePermissions modifiedPermissions, String username,
                                              String databaseName) throws RSSManagerException;

    public void createDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template) throws RSSManagerException;

    public void dropDatabasePrivilegesTemplate(String templateName) throws RSSManagerException;

    public void editDatabasePrivilegesTemplate(
            DatabasePrivilegeTemplate template) throws RSSManagerException;

    public List<DatabasePrivilegeTemplate> getAllDatabasePrivilegesTemplates(int tid) throws
            RSSManagerException;

    public DatabasePrivilegeTemplate getDatabasePrivilegesTemplate(
            String templateName) throws RSSManagerException;
    
    public List<DatabaseUser> getAllDatabaseUsers(int tid) throws RSSManagerException;

    public List<DatabaseUser> getUsersByRSSInstance(String rssInstanceName) throws
            RSSManagerException;

    public List<String> getUsersAssignedToDatabase(
            String rssInstanceName, String databaseName) throws RSSManagerException;

    public List<String> getAvailableUsersToBeAssigned(
            String rssInstanceName, String databaseName) throws RSSManagerException;

}
