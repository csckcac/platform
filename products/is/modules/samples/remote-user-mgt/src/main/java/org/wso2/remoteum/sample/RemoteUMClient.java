/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.remoteum.sample;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.um.ws.api.WSUserStoreManager;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.File;

/**
 * This demonstrates how to use remote user management API to add, delete and read users.
 */
public class RemoteUMClient {

    private String serverUrl = "https://localhost:9443/services/";

    private AuthenticationAdminStub authstub = null;
    private ConfigurationContext ctx;
    private String authCookie = null;
    private WSUserStoreManager remoteUserStoreManager = null;


    /**
     * Initialization of environment
     *
     * @throws Exception
     */
    public RemoteUMClient() throws Exception {
        ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        String authEPR = serverUrl + "AuthenticationAdmin";
        authstub = new AuthenticationAdminStub(ctx, authEPR);
        ServiceClient client = authstub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, authCookie);
    }

    /**
     * Authenticate to carbon as admin user and obtain the authentication cookie
     *
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public String login(String username, String password) throws Exception {
        //String cookie = null;
        boolean loggedIn = authstub.login(username, password, "localhost");
        if (loggedIn) {
            System.out.println("The user " + username + " logged in successfully.");
            authCookie = (String) authstub._getServiceClient().getServiceContext().getProperty(
                    HTTPConstants.COOKIE_STRING);
        } else {
            System.out.println("Error logging in " + username);
        }
        return authCookie;
    }

    /**
     * create web service client for RemoteUserStoreManager service from the wrapper api provided
     * in carbon - remote-usermgt component
     *
     * @throws UserStoreException
     */
    public void createRemoteUserStoreManager() throws UserStoreException {

        remoteUserStoreManager = new WSUserStoreManager(serverUrl, authCookie, ctx);
    }

    /**
     * Add a user store to the system.
     *
     * @throws UserStoreException
     */
    public void addUser(String userName, String password) throws UserStoreException {

        remoteUserStoreManager.addUser(userName, password, null, null, null);
        System.out.println("Added user: " + userName);
    }

    /**
     * Add a role to the system
     *
     * @throws Exception
     */
    public void addRole(String roleName) throws UserStoreException {
        remoteUserStoreManager.addRole(roleName, null, null);
        System.out.println("Added role: " + roleName);
    }

    /**
     * Add a new user by assigning him to a new role
     *
     * @throws Exception
     */
    public void addUserWithRole(String userName, String password, String roleName)
            throws UserStoreException {
        remoteUserStoreManager.addUser(userName, password, new String[]{roleName}, null, null);
        System.out.println("Added user: " + userName + " with role: " + roleName);
    }

    /**
     * Retrieve all the users in the system
     *
     * @throws Exception
     */
    public String[] listUsers() throws UserStoreException {
        return remoteUserStoreManager.listUsers("*", -1);
    }

    /**
     * Delete an exisitng user from the system
     *
     * @throws Exception
     */
    public void deleteUser(String userName) throws UserStoreException {
        remoteUserStoreManager.deleteUser(userName);
        System.out.println("Deleted user:" + userName);
    }

    public static void main(String[] args) throws Exception {
        //set trust store properties required in SSL communication.
        String is_Home = ".." + File.separator + ".." + File.separator;
        System.setProperty("javax.net.ssl.trustStore", is_Home + "repository" + File.separator + "resources" +
                           File.separator + "security" + File.separator + "wso2carbon.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        RemoteUMClient remoteUMClient = new RemoteUMClient();
        //log in as admin user and obtain the cookie
        remoteUMClient.login("admin", "admin");

        /*Create client for RemoteUserStoreManagerService and perform user management operations*/

        //create web service client
        remoteUMClient.createRemoteUserStoreManager();
        //add a new user to the system
        remoteUMClient.addUser("kamal", "kamal");
        //add a role to the system
        remoteUMClient.addRole("eng");
        //add a new user with a role
        remoteUMClient.addUserWithRole("saman", "saman", "eng");
        //print a list of all the users in the system
        String[] users = remoteUMClient.listUsers();
        System.out.println("List of users in the system:");
        for (String user : users) {
            System.out.println(user);
        }
        //delete an existing user
        remoteUMClient.deleteUser("kamal");
        //print the current list of users
        String[] userList = remoteUMClient.listUsers();
        System.out.println("List of users in the system currently:");
        for (String user : userList) {
            System.out.println(user);
        }
    }

}
