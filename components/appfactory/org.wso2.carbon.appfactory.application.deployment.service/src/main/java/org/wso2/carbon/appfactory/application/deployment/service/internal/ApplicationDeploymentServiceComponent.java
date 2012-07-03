/*
* Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.appfactory.application.deployment.service.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.application.deployment.service.util.RepositoryManagerUtil;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryManager;


/**
 * this class is used to get the AppFactory Configuration.
 *
 * @scr.component name="org.wso2.carbon.appfactory.application.deployment.service.internal.ApplicationDeploymentServiceComponent" immediate="true"
 * @scr.reference name="appfactory.common"
 * interface="org.wso2.carbon.appfactory.common.AppFactoryConfiguration" cardinality="1..1"
 * policy="dynamic" bind="setAppFactoryConfiguration" unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="appfactory.svn"
 * interface="org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryManager" cardinality="1..1"
 * policy="dynamic" bind="setRepositoryManager" unbind="unsetRepositoryManager"
 */
public class ApplicationDeploymentServiceComponent {

    private Log log = LogFactory.getLog(ApplicationDeploymentServiceComponent.class);

    protected void activate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Activated ApplicationDeploymentServiceComponent");

        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivated ApplicationDeploymentServiceComponent");
        }
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        AppFactoryConfigurationHolder.getInstance().registerAppFactoryConfiguration(appFactoryConfiguration);
    }

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        AppFactoryConfigurationHolder.getInstance().unRegisterAppFactoryConfiguration(appFactoryConfiguration);
    }

    protected void setRepositoryManager(RepositoryManager repositoryManager) {
        RepositoryManagerUtil.setConfiguration(repositoryManager);
    }

    protected void unsetRepositoryManager(RepositoryManager repositoryManager) {
        RepositoryManagerUtil.setConfiguration(null);
    }


}