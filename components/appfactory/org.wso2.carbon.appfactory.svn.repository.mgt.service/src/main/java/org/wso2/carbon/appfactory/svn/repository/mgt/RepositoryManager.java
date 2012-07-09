/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.wso2.carbon.appfactory.svn.repository.mgt;

import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.svn.repository.mgt.impl.SCMManagerExceptions;

import java.io.File;

/**
 *
 *
 */
public interface RepositoryManager {
    /**
     * @param applicationKey
     * @return URL of the created repository
     */
    public String createRepository(String applicationKey) throws RepositoryMgtException;

    /**
     * @param applicationKey
     * @return
     * @throws RepositoryMgtException
     */
    public String getURL(String applicationKey) throws RepositoryMgtException;

    public boolean hasAccess(String username,String password,String applicationId);

    public void setConfig(AppFactoryConfiguration configuration);

    public AppFactoryConfiguration getConfig();

    public void createDirectory(String url, String commitMessage);

    public void svnCopy(String sourceUrl, String destinationUrl, String commitMessage, SVNRevision rev);

    public void svnMove(String sourceUrl, String destinationUrl, String commitMessage, SVNRevision rev);

    public void initSVNClient() throws SCMManagerExceptions;

    public String checkoutApplication(String applicationSvnUrl, String applicationId, String svnRevision)
            throws SCMManagerExceptions;

    public void buildApplication(String sourcePath) throws SCMManagerExceptions;

    public File createApplicationCheckoutDirectory(String applicationName)
            throws SCMManagerExceptions;

    public boolean executeMavenGoal(String applicationPath)
            throws SCMManagerExceptions;

    public void cleanApplicationDir(String applicationPath);

    public String getAdminUsername(String applicationId);
}
