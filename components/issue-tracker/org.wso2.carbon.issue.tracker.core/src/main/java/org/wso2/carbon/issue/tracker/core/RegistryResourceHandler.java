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
package org.wso2.carbon.issue.tracker.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.issue.tracker.adapter.api.GenericCredentials;
import org.wso2.carbon.issue.tracker.adapter.exceptions.IssueTrackerException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle registry resources. this is used to persist account credentials.
 */
public class RegistryResourceHandler {

    private static final Log log = LogFactory.getLog(RegistryResourceHandler.class);


    /**
     * method to persist credentials of a jira account
     *
     * @param registry    Registry
     * @param accountInfo AccountInfo
     * @throws IssueTrackerException thrown id unable to store resources in the registry
     */
    public static void persistCredentials(Registry registry,
                                          AccountInfo accountInfo) throws IssueTrackerException {

        Resource resource = null;

        String path = IssueTrackerConstants.ISSUE_TRACKERS_RESOURCE_PATH + accountInfo.getKey();

        try {
            // if the collection does not exist create one
            if (!registry.resourceExists(IssueTrackerConstants.ISSUE_TRACKERS_RESOURCE_PATH)) {
                Collection collection = registry.newCollection();
                registry.put(IssueTrackerConstants.ISSUE_TRACKERS_RESOURCE_PATH, collection);
            }

            //get registry resource
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            } else {
                resource = registry.newResource();
            }
        } catch (RegistryException e) {
            ExceptionHandler.handleException("Error accessing registry", e, log);
        }

        // get credentials from account info
        GenericCredentials credentials;
        credentials = accountInfo.getCredentials();

        // set properties of the registry resources
        if (resource != null) {
            resource.addProperty(IssueTrackerConstants.ACCOUNT_KEY, accountInfo.getKey());
            resource.addProperty(IssueTrackerConstants.ISSUE_TRACKER_URL, credentials.getUrl());
            resource.addProperty(IssueTrackerConstants.ACCOUNT_LOGIN_USERNAME, credentials.getUsername());
            resource.addProperty(IssueTrackerConstants.ACCOUNT_EMAIL, accountInfo.getEmail());
            resource.addProperty(IssueTrackerConstants.ACCOUNT_UID, accountInfo.getUid());
            resource.addProperty(IssueTrackerConstants.HAS_SUPPORT_ACCOUNT, String.valueOf(accountInfo.isHasSupportAccount()));

            // set properties related with automatic reporting
            if (accountInfo.isAutoReportingEnable()) {

                AutoReportingSettings settings = accountInfo.getAutoReportingSettings();
                resource.addProperty(IssueTrackerConstants.AUTO_REPORTING, IssueTrackerConstants.IS_AUTO_REPORTING_ENABLED);
                resource.addProperty(IssueTrackerConstants.AUTO_REPORTING_PROJECT, settings.getProjectName());
                resource.addProperty(IssueTrackerConstants.AUTO_REPORTING_PRIORITY, settings.getPriority());
                resource.addProperty(IssueTrackerConstants.AUTO_REPORTING_ISSUE_TYPE, settings.getIssueType());

            } else {
                resource.addProperty(IssueTrackerConstants.AUTO_REPORTING, IssueTrackerConstants.IS_AUTO_REPORTING_DISABLED);
            }

            //encrypt and store password
            String password = credentials.getPassword();

            if (null != password && !"".equals(password)){
            byte[] bytes = (password).getBytes();

            try {
                String base64String = CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(bytes);
                resource.addProperty(IssueTrackerConstants.ACCOUNT_PASSWORD_HIDDEN_PROPERTY, base64String);
            } catch (org.wso2.carbon.core.util.CryptoException e) {
                ExceptionHandler.handleException("Error accessing registry", e, log);
            }
            }
        }

        // put resource to registry
        try {
            registry.put(path, resource);
        } catch (RegistryException e) {
            ExceptionHandler.handleException("Error while persisting accountInfo", e, log);
        }
    }


    /**
     * method to get stored accounts from registry
     *
     * @param registry Registry
     * @return list of AccountInfo
     * @throws IssueTrackerException thrown if unable to obtain resources from registry
     */
    public static List<AccountInfo> getAccounts(Registry registry) throws IssueTrackerException {

        List<AccountInfo> accounts = new ArrayList<AccountInfo>();
        try {

            // check whether resources exist at the registry path
            if (registry.resourceExists(IssueTrackerConstants.ISSUE_TRACKERS_RESOURCE_PATH)) {

                // get the collection
                Collection collection =
                        (Collection) registry.get(IssueTrackerConstants.ISSUE_TRACKERS_RESOURCE_PATH);

                if (null != collection) {

                    //get paths of resources in the collection
                    String[] paths = collection.getChildren();

                    // for each resource construct the corresponding AccountInfo instance and add it to the list
                    for (String path : paths) {

                        AccountInfo accountInfo = new AccountInfo();
                        GenericCredentials credentials = new GenericCredentials();

                        if (registry.resourceExists(path)) {
                            Resource resource = registry.get(path);

                            List<String> accountPropertySet = resource.getPropertyValues(IssueTrackerConstants.ACCOUNT_KEY);
                            accountInfo.setKey(accountPropertySet.get(accountPropertySet.size() - 1));

                            List<String> urlPropertySet = resource.getPropertyValues(IssueTrackerConstants.ISSUE_TRACKER_URL);
                            String url = urlPropertySet.get(urlPropertySet.size() - 1);

                            List<String> usernamePropertySet = resource.getPropertyValues(IssueTrackerConstants.ACCOUNT_LOGIN_USERNAME);
                            String username = usernamePropertySet.get(usernamePropertySet.size() - 1);

                            List<String> emailPropertySet = resource.getPropertyValues(IssueTrackerConstants.ACCOUNT_EMAIL);
                            String email = emailPropertySet.get(emailPropertySet.size() - 1);

                            List<String> uidPropertySet = resource.getPropertyValues(IssueTrackerConstants.ACCOUNT_UID);
                            String uid = uidPropertySet.get(uidPropertySet.size() - 1);

                            List<String> hasSupportPropertySet = resource.getPropertyValues(IssueTrackerConstants.HAS_SUPPORT_ACCOUNT);
                            String hasSupport = hasSupportPropertySet.get(usernamePropertySet.size() - 1);

                            List<String> passwordPropertySet;

                            passwordPropertySet = resource.getPropertyValues(IssueTrackerConstants.ACCOUNT_PASSWORD_HIDDEN_PROPERTY);

                            //for users with older accounts hidden password property does not exist. for them, have to get the older property
                            //until the account is edited.
                            if(null == passwordPropertySet){
                            passwordPropertySet = resource.getPropertyValues(IssueTrackerConstants.ACCOUNT_PASSWORD);
                            }

                            String password = "";
                            if(null != passwordPropertySet){

                                String encryptedPassword = passwordPropertySet.get(passwordPropertySet.size() - 1);

                                password = new String(CryptoUtil.getDefaultCryptoUtil().
                                        base64DecodeAndDecrypt(encryptedPassword));

                            }
                            String isAutoReportingEnabled = resource.getProperty(IssueTrackerConstants.AUTO_REPORTING);

                            if (null != isAutoReportingEnabled &&
                                    IssueTrackerConstants.IS_AUTO_REPORTING_ENABLED.equals(isAutoReportingEnabled)) {
                                accountInfo.setAutoReportingEnable(true);

                                AutoReportingSettings settings = new AutoReportingSettings();
                                String projectName = resource.getProperty(IssueTrackerConstants.AUTO_REPORTING_PROJECT);
                                settings.setProjectName(projectName);
                                String priority = resource.getProperty(IssueTrackerConstants.AUTO_REPORTING_PRIORITY);
                                settings.setPriority(priority);
                                String type = resource.getProperty(IssueTrackerConstants.AUTO_REPORTING_ISSUE_TYPE);
                                settings.setIssueType(type);
                                accountInfo.setAutoReportingSettings(settings);
                            } else {
                                accountInfo.setAutoReportingEnable(false);
                            }


                            if (!"".equals(url) && !"".equals(username) && !"".equals(password)) {
                                credentials.setUrl(url);
                                credentials.setUsername(username);
                                credentials.setPassword(password);
                                accountInfo.setCredentials(credentials);
                                accountInfo.setEmail(email);
                                accountInfo.setUid(uid);
                                accountInfo.setHasSupportAccount(Boolean.getBoolean(hasSupport));
                                accounts.add(accountInfo);
                            }
                        }
                    }
                }


            }
        } catch (RegistryException e) {
            ExceptionHandler.handleException("Error getting resources from registry", e, log);
        } catch (CryptoException e) {
            ExceptionHandler.handleException("Error decrypting password", e, log);
        }


        return accounts;
    }
}
