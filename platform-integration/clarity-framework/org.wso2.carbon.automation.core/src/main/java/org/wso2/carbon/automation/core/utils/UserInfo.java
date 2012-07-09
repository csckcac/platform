/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.automation.core.utils;

public class UserInfo {

    private String id = "";
    private String userName = "";
    private String password = "";
    private String domainName = "";

    public UserInfo(String id, String userName, String password, String domainName) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.domainName = domainName;
    }

    public String getUserId() {
        return id;
    }

    public void setUserId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        setDomain(userName);
    }

    public String getPassword() {
        return password;
    }

    public void setUserPassword(String password) {
        this.password = password;
    }

    public void setDomain(String domainName) {

        this.domainName = domainName;
    }

    public String getDomain() {
        return domainName;
    }
}
