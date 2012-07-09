/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package sonia.scm.carbon.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryManager;
import org.wso2.carbon.appfactory.svn.repository.mgt.RepositoryMgtException;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@Singleton
@Extension
public class CarbonAuthHandler implements AuthenticationHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(CarbonAuthHandler.class);


    /**
     * Field description
     */
    public static final String TYPE = "carbon";



    //~--- methods --------------------------------------------------------------

    /**
     * Method description
     *
     * @param request
     * @param response
     * @param username
     * @param password
     * @return
     */
    @Override
    public AuthenticationResult authenticate(HttpServletRequest request,
                                             HttpServletResponse response, String username,
                                             String password) {
        String applicationName = request.getRequestURI().split("/")[3];
        AssertUtil.assertIsNotEmpty(username);
        AssertUtil.assertIsNotEmpty(password);
        AssertUtil.assertIsNotEmpty(applicationName);
        RepositoryManager repositoryManager = (RepositoryManager) SuperTenantCarbonContext.
                getCurrentContext().getOSGiService(RepositoryManager.class);

            if (repositoryManager != null) {
                if (repositoryManager.hasAccess(username, password, applicationName)) {
                    return new AuthenticationResult(getUser(username),
                                                    getGroups(applicationName));
                }
            } else {
                logger.error("Could not get Repository manager from appfactory");
            }

        return AuthenticationResult.FAILED;
    }

    /**
     * Method description
     *
     * @throws java.io.IOException
     */
    @Override
    public void close() throws IOException {

        // nothing todo
    }


    /**
     * Method description
     *
     * @return
     */
    @Override
    public String getType() {
        return TYPE;
    }


    //~--- methods --------------------------------------------------------------

    private User getUser(String userName) {
        User user = new User();
        user.setName(userName);
        user.setType(CarbonAuthHandler.TYPE);
        user.setDisplayName(userName);
        user.setMail("dummy@example.com"); //we have to just pass an email to get this passed.
        return user;
    }

    private Set<String> getGroups(String projectKey) {
        Set<String> groups = new HashSet<String>();
        groups.add(projectKey);
        return groups;
    }


    @Override
    public void init(SCMContextProvider scmContextProvider) {
        logger.info("initializing  Carbon Auth Handler");
    }
}
