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
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;
import sonia.scm.util.AssertUtil;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@Singleton
@Extension
public class CarbonAuthHandler implements AuthenticationHandler {

    /**
     * Field description
     */
    public static final String STORE_NAME = "carbon-auth";

    /**
     * Field description
     */
    public static final String TYPE = "carbon";

    //~--- constructors ---------------------------------------------------------


    @Inject
    public CarbonAuthHandler(StoreFactory storeFactory) {
        store = storeFactory.getStore(CarbonAuthConfig.class, STORE_NAME);

    }

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

        AssertUtil.assertIsNotEmpty(username);
        AssertUtil.assertIsNotEmpty(password);

        if (client.authenticateUser(username, password, request.getRemoteAddr())) {
            return client.authorizeUser(request, username);
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
     * @param context
     */
    @Override
    public void init(SCMContextProvider context) {
        config = store.get();

        if (config == null) {
            config = new CarbonAuthConfig();
        }


        client = new CarbonAuthClient(context.getBaseDirectory().getAbsolutePath());
        client.setConfig(config);
        client.init();


    }

    public void storeConfig() {
        store.set(config);
    }

    //~--- get methods ----------------------------------------------------------

    /**
     * Method description
     *
     * @return
     */
    public CarbonAuthConfig getConfig() {
        return config;
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

    //~--- set methods ----------------------------------------------------------

    /**
     * Method description
     *
     * @param config
     */
    public void setConfig(CarbonAuthConfig config) {
        this.config = config;
        client.setConfig(config);

        client.init();

    }

    //~--- methods --------------------------------------------------------------


    /**
     * Method description
     *
     *
     * @param username
     * @param password
     *
     * @return
     */

    //~--- fields ---------------------------------------------------------------

    /**
     * Field description
     */
    private CarbonAuthConfig config;

    /**
     * Field description
     */
    private Store<CarbonAuthConfig> store;

    private CarbonAuthClient client;
}
