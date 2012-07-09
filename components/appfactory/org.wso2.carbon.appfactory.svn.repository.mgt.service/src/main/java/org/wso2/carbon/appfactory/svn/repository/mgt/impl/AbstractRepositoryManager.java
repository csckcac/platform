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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryConfiguration;
import org.wso2.carbon.appfactory.common.AppFactoryConstants;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.appfactory.svn.repository.mgt.util.Util;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.Arrays;

/**
 *
 *
 */
public abstract class AbstractRepositoryManager implements RepositoryManager {
    private static final Log log = LogFactory.getLog(AbstractRepositoryManager.class);


    protected HttpClient getClient( AppFactoryConfiguration config) {

        HttpClient client = new HttpClient();

        String userName = config.getFirstProperty(AppFactoryConstants.SCM_ADMIN_NAME);
        String password = config.getFirstProperty(AppFactoryConstants.SCM_ADMIN_PASSWORD);

        AuthScope authScope = AuthScope.ANY;

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(userName, password);

        client.getState().setCredentials(authScope, credentials);

        return client;
    }

    protected String getServerURL( AppFactoryConfiguration config) {
        //TODO:create a field backend url instead of ip+port
        return  config.getFirstProperty(AppFactoryConstants.SCM_SERVER_URL);
    }
   public boolean hasAccess(String username,String password,String applicationId)
            {
       Integer tID= null;
       try {
           tID = Util.getRealmService().getTenantManager().getTenantId(applicationId);
           UserStoreManager userStoreManager= Util.getRealmService().getTenantUserRealm(tID).getUserStoreManager();
           if(userStoreManager.authenticate(username,password)){
               if(Arrays.asList(userStoreManager.getRoleListOfUser(username)).contains(Util.getConfiguration().getFirstProperty(
                       AppFactoryConstants.SCM_READ_WRITE_ROLE)
               )){
                   return true;
               }
           }
       } catch (UserStoreException e) {
           String msg = "Error while checking permission for accessing svn repository of "+applicationId+" by "+username;
           log.error(msg, e);
       }


       return false;
   }
}
