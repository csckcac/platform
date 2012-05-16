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

import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.svn.repository.mgt.builder.RepositoryManagerHolder;
import org.wso2.carbon.core.AbstractAdmin;

/**
 *
 *
 */
public class RepositoryManagementService extends AbstractAdmin{
    private RepositoryManager repositoryManager;

    public RepositoryManager getRepositoryManager() {
        return repositoryManager;
    }

    public void setRepositoryManager(RepositoryManager repositoryManager) {
        this.repositoryManager = repositoryManager;
    }

    public RepositoryManagementService() {
        RepositoryManagerHolder holder= RepositoryManagerHolder.getInstance();
        this.repositoryManager=holder.getRepositoryManager();
    }

    public String createRepository(String projectKey) throws RepositoryMgtException {
        return repositoryManager.createRepository(projectKey);
    }


    public String getURL(String projectKey) throws RepositoryMgtException {
        return repositoryManager.getURL(projectKey);
    }
}
