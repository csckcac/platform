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

package org.wso2.carbon.appfactory.svn.repository.mgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.core.ArtifactStorage;
import org.wso2.carbon.appfactory.core.RevisionControlDriver;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.svn.repository.mgt.builder.RepositoryManagerHolder;
import org.wso2.carbon.appfactory.svn.repository.mgt.impl.FileArtifactStorage;
import org.wso2.carbon.appfactory.svn.repository.mgt.impl.SCMManagerBasedRepositoryManager;
import org.wso2.carbon.appfactory.svn.repository.mgt.impl.SVNArtifactStorage;
import org.wso2.carbon.appfactory.svn.repository.mgt.impl.SVNManager;
import org.wso2.carbon.appfactory.svn.repository.mgt.util.Util;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.svn.repository.mgt" immediate="true"
 * @scr.reference name="appfactory.configuration" interface=
 * "org.wso2.carbon.appfactory.common.AppFactoryConfiguration"
 * cardinality="1..1" policy="dynamic"
 * bind="setAppFactoryConfiguration"
 * unbind="unsetAppFactoryConfiguration"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class SVNRepositoryMgtServiceComponent {
    Log log = LogFactory.getLog(SVNRepositoryMgtServiceComponent.class);

    protected void unsetAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        Util.setConfiguration(null);
    }

    protected void setAppFactoryConfiguration(AppFactoryConfiguration appFactoryConfiguration) {
        Util.setConfiguration(appFactoryConfiguration);
    }
    protected void setRealmService(RealmService realmService) {

        Util.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        Util.setRealmService(null);
    }

    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.info("**************SVN repository mgt bundle is activated*************");
        }
        try {
            BundleContext bundleContext = context.getBundleContext();

            SVNManager repositoryManager = new SVNManager();
            bundleContext.registerService(RevisionControlDriver.class.getName(), repositoryManager, null);

            //SVNArtifactStorage svnArtifactStorage = new SVNArtifactStorage();
            //bundleContext.registerService(ArtifactStorage.class.getName(), svnArtifactStorage, null);

            // Registering File artifact storage
            FileArtifactStorage fileArtifactStorage = new FileArtifactStorage();
            bundleContext.registerService(ArtifactStorage.class.getName(), fileArtifactStorage, null);

            RepositoryManagerHolder holder = RepositoryManagerHolder.getInstance();
            RepositoryManager repositoryManagerOld = holder.getRepositoryManager();
            bundleContext.registerService(RepositoryManager.class.getName(),repositoryManagerOld,null);

        } catch (Throwable e) {
            log.error("Error in registering Repository Management Service  ", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("*************SVN repository mgt bundle is deactivated*************");
        }
    }

}
