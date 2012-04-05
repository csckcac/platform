
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

import org.wso2.carbon.issue.tracker.adapter.api.GenericCredentials;

/**
 * this class is used to store issue tracker account data
 * key is a unique name to represent an account
 * credentials    to connect to the given account
 * autoReportingEnable whether or not automatic error reporting is enabled for the project
 * autoReportingSettings setting for automatic error reporting
 *
 */
public class AccountInfo {

    private String key;
    private GenericCredentials credentials;
    private boolean autoReportingEnable;
    private AutoReportingSettings autoReportingSettings;
    private boolean hasSupportAccount;
    private String email;  // jira email
    private String uid;    // jira username

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public AutoReportingSettings getAutoReportingSettings() {
        return autoReportingSettings;
    }

    public void setAutoReportingSettings(AutoReportingSettings autoReportingSettings) {
        this.autoReportingSettings = autoReportingSettings;
    }

    public boolean isAutoReportingEnable() {
        return autoReportingEnable;
    }

    public void setAutoReportingEnable(boolean autoReportingEnable) {
        this.autoReportingEnable = autoReportingEnable;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public GenericCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(GenericCredentials credentials) {
        this.credentials = credentials;
    }

    public boolean isHasSupportAccount() {
        return hasSupportAccount;
    }

    public void setHasSupportAccount(boolean hasSupportAccount) {
        this.hasSupportAccount = hasSupportAccount;
    }
}
