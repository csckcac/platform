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

        try {
            //TODO:remove this temp logic after changing BPEL API
            String baseURL = getApplicationUrl()+File.separator+"svn"+ File.separator+ applicationId;
            String sourceURL=baseURL+"/trunk/";
            String destinationURL =baseURL+"/branch/"+version;
                try {
                scm.svnCopy(sourceURL, destinationURL,
                            "branching trunk to " + version, SVNRevision.getRevision(revision));
            } catch (ParseException e) {
                log.error("Error in branching" +e);
            }
            scm.checkoutApplication(destinationURL, applicationId, revision);
            listener.onGetSourceCompleted(applicationId, version, revision);
        } catch (SCMManagerExceptions scmManagerExceptions) {
            log.error("Error in checkout" + scmManagerExceptions);
        }

    }

    public String getApplicationUrl() {
        return (appFactoryConfiguration.getFirstProperty(
                    AppFactoryConstants.SCM_SERVER_URL));
    }


}
