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

package org.wso2.carbon.appfactory.svn.repository.mgt.impl;

import org.apache.commons.logging.LogFactory;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.core.RevisionControlDriver;
import org.wso2.carbon.appfactory.core.RevisionControlDriverListener;
import org.wso2.carbon.appfactory.svn.repository.mgt.util.Util;

import java.io.File;
import java.text.ParseException;


public class SVNManager implements RevisionControlDriver {
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(SCMManagerBasedRepositoryManager.class);
    private static AppFactoryConfiguration appFactoryConfiguration = Util.getConfiguration();

    @Override
    public void getSource(String applicationId, String version, String revision, RevisionControlDriverListener listener) throws AppFactoryException {

        SCMManagerBasedRepositoryManager scm = new SCMManagerBasedRepositoryManager();
        String checkoutUrl=getApplicationUrl()+"/"+"svn"+ "/"+ applicationId;
        //TODO:if it tag how to detect?
        try {
            if("trunk".equals(version)) {
            checkoutUrl =checkoutUrl+"/"+"trunk";
            }else{
            checkoutUrl=checkoutUrl+"/"+"branch"+"/"+version;
            }
            scm.checkoutApplication(checkoutUrl, applicationId, revision);

        } catch (SCMManagerExceptions scmManagerExceptions) {
            log.error("Error in checkout" + scmManagerExceptions);
        }
          listener.onGetSourceCompleted(applicationId,version,revision);
    }

    @Override
    public void branch(String appId, String currentVersion, String targetVersion,
                       String currentRevision) throws AppFactoryException {
        String baseURL=appFactoryConfiguration.getFirstProperty(AppFactoryConstants.SCM_SERVER_URL)+"/svn/"+appId;
        String sourceURL;
        String destinationURL=baseURL+"/branch/"+targetVersion;
        if("trunk".equals(currentVersion)){
            sourceURL=baseURL+"/"+currentVersion;
        }else {
            sourceURL=baseURL+"/branch/"+currentVersion;
        }
        SCMManagerBasedRepositoryManager scm = new SCMManagerBasedRepositoryManager();
        scm.svnCopy(sourceURL,destinationURL,"branching "+currentVersion+" to "+targetVersion,currentRevision);

    }
    @Override
    public void tag(String appId, String currentVersion, String targetVersion,
                       String currentRevision) throws AppFactoryException {
        String baseURL=appFactoryConfiguration.getFirstProperty(AppFactoryConstants.SCM_SERVER_URL)+"/svn/"+appId;
        String sourceURL;
        String destinationURL=baseURL+"/tag/"+targetVersion;
        if("trunk".equals(currentVersion)){
            sourceURL=baseURL+"/"+currentVersion;
        }else {
            sourceURL=baseURL+"/branch/"+currentVersion;
        }
        SCMManagerBasedRepositoryManager scm = new SCMManagerBasedRepositoryManager();
        scm.svnCopy(sourceURL,destinationURL,"branching "+currentVersion+" to "+targetVersion,currentRevision);

    }

    public String getApplicationUrl() {
        return (appFactoryConfiguration.getFirstProperty(
                    AppFactoryConstants.SCM_SERVER_URL));
    }


}
