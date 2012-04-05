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

public class IssueTrackerConstants {

    // constants to represent operating environmental variables
    public static final String OS_NAME = "os.name";
    public static final String OS_VERSION = "os.version";
    public static final String OS_ARCHITECTURE = "os.arch";
    public static final String JAVA_VENDOR = "java.vendor";
    public static final String JAVA_VERSION = "java.version";
    public static final String PRODUCT_NAME = "Name";
    public static final String PRODUCT_VERSION = "Version";
    public static final String USER_HOME = "user.home";
    public static final String JAVA_IO_TEMP_DIR = "java.io.tmpdir";


    public static final String ISSUE_TRACKERS_RESOURCE_PATH =
            "/repository/components/org.wso2.carbon.issue.tracker/";

    public static final String BUNDLE_INFO_PATH =
            "/repository/components/configuration/org.eclipse.equinox.simpleconfigurator/";

    public static final String BUNDLE_INFO_FILE_NAME = "bundles.info";

    public static final String THREAD_DUMP_FILE_NAME = "thread-dump.txt";

    public static final String LOG_FILE_PATH = "/repository/logs/";

    public static final String PATCH_DIR_PATH = "/repository/components/patches";

    public static final String LOG_FILE_NAME = "wso2carbon.log";

    protected static final String ACCOUNT_KEY = "key";
    protected static final String ISSUE_TRACKER_URL = "url";
    protected static final String ACCOUNT_LOGIN_USERNAME = "login-username";
    protected static final String ACCOUNT_PASSWORD = "password";
    protected static final String AUTO_REPORTING = "autoReporting";
    protected static final String IS_AUTO_REPORTING_ENABLED = "enable";
    protected static final String IS_AUTO_REPORTING_DISABLED = "disable";
    protected static final String AUTO_REPORTING_PROJECT = "autoReporting-project";
    protected static final String AUTO_REPORTING_PRIORITY = "autoReporting-priority";
    protected static final String AUTO_REPORTING_ISSUE_TYPE = "autoReporting-type";
    protected static final String ACCOUNT_UID = "uid";
    protected static final String ACCOUNT_EMAIL = "email";
    protected static final String HAS_SUPPORT_ACCOUNT = "supportAccount";
    protected static final String ACCOUNT_PASSWORD_HIDDEN_PROPERTY = "registry.password";

    public static final int ISSUES_PER_PAGE = 15;

    public static final String JIRA_CREDENTIALS_REGISTRY_PREFIX_PAID = "-PAID-JIRA";
    public static final String JIRA_CREDENTIALS_REGISTRY_PREFIX_NONPAID = "-FREE-JIRA";
    public static final String MANAGER_SERVICE_NAME = "WSO2 Stratos Manager";

    public static final String FREE_SUBSCRIPTION_PLAN_NAME = "Demo" ;


}
