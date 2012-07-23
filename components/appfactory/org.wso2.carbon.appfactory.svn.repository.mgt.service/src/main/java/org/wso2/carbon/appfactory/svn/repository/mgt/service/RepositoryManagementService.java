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

package org.wso2.carbon.appfactory.svn.repository.mgt.service;

import java.io.File;

import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.svn.repository.mgt.impl.SCMManagerExceptions;
import org.wso2.carbon.core.AbstractAdmin;

/**
 *
 *
 */
public class RepositoryManagementService extends AbstractAdmin {
    private RepositoryManager repositoryManager;

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public String createRepository(String applicationKey) throws RepositoryMgtException {
        return repositoryManager.createRepository(applicationKey);
    }


    public String getURL(String applicationKey) throws RepositoryMgtException {
        return repositoryManager.getURL(applicationKey);
    }

    public void createDirectory(String url, String commitMessage) {
        repositoryManager.createDirectory(url, commitMessage);
    }


    public void svnCopy(String sourceUrl, String destinationUrl, String commitMessage, String svnRevision) {
        repositoryManager.svnCopy(sourceUrl, destinationUrl, commitMessage, svnRevision);
    }


    public void svnMove(String sourceUrl, String destinationUrl, String commitMessage, String svnRevision) {
        repositoryManager.svnMove(sourceUrl, destinationUrl, commitMessage, svnRevision);
    }

    public void initSVNClient() throws SCMManagerExceptions {
        repositoryManager.initSVNClient();
    }

    public String checkoutApplication(String applicationSvnUrl, String applicationId, String svnRevision)
            throws SCMManagerExceptions {
        return repositoryManager.checkoutApplication(applicationSvnUrl, applicationId, svnRevision);
    }



    public File createApplicationCheckoutDirectory(String applicationName)
            throws SCMManagerExceptions {
        return repositoryManager.createApplicationCheckoutDirectory(applicationName);

    }



    public void cleanApplicationDir(String applicationPath) {
        repositoryManager.cleanApplicationDir(applicationPath);
    }

    public String getAdminUsername(String applicationId) {
        return repositoryManager.getAdminUsername(applicationId);
    }
    public boolean hasAccess(String username,String password,String applicationId)
            throws RepositoryMgtException {
        return repositoryManager.hasAccess(username,password,applicationId);
    }

}
