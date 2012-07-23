/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.appfactory.core.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.RevisionControlDriver;
import org.wso2.carbon.appfactory.core.build.DefaultRevisionControlDriverListener;
import org.wso2.carbon.appfactory.core.internal.AppFactoryCoreServiceComponent;
import org.wso2.carbon.core.AbstractAdmin;

/**
 * .
 */
public class RevisionControllerService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(RevisionControllerService.class);
    public static final String DEFAULT_REPOSITORY_TYPE = "svn";
    private RevisionControlDriver revisionControlDriver;


    public void getSource(String applicationId, String version, String revision)
            throws AppFactoryException {
        loadRevisionController(applicationId);
        revisionControlDriver.getSource(applicationId, version, revision,
                                        new DefaultRevisionControlDriverListener());
    }

    private void loadRevisionController(String applicationId) throws AppFactoryException {
        String repositoryTypeOfApplication = getRepositoryTypeOfApplication(applicationId);
        revisionControlDriver=AppFactoryCoreServiceComponent.getRevisionControlDriverMap(repositoryTypeOfApplication);
                    if (revisionControlDriver==null) {
                        String msg = "The application " + applicationId + " " +
                                     "does not have repository type";
                        log.error(msg);
                        throw new AppFactoryException(msg);
                    }
    }

    private String getRepositoryTypeOfApplication(String applicationId) {
        return RevisionControllerService.DEFAULT_REPOSITORY_TYPE;
    }


    public void branch(String appId, String currentVersion, String targetVersion,
                       String currentRevision) throws AppFactoryException {
        loadRevisionController(appId);
        revisionControlDriver.branch(appId, currentVersion, targetVersion, currentRevision);
    }
    public void tag(String appId, String currentVersion, String targetVersion,
                    String currentRevision) throws AppFactoryException{
        loadRevisionController(appId);
        revisionControlDriver.tag(appId, currentVersion, targetVersion, currentRevision);
    }
}
