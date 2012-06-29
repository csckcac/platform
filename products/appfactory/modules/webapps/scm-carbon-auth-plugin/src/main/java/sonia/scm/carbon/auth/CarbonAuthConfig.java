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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 *
 */
@XmlRootElement(name = "wso2-carbon-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class CarbonAuthConfig {
    public String getBackEndServerUrl() {
        return backEndServerUrl;
    }

    public void setBackEndServerUrl(String backEndServerUrl) {
        this.backEndServerUrl = backEndServerUrl;
    }

    public String getAdminUserName() {
        return adminUserName;
    }


    public String getDefaultTenantPassword() {
        return defaultTenantPassword;
    }

    public String getTenantDomainExtension() {
        return tenantDomainExtension;
    }

    //~--- fields ---------------------------------------------------------------

    /**
     * Field description
     */
    @XmlElement(name = "backEndServerUrl")
    private String backEndServerUrl = "https://192.168.4.26:9444/services/";
    @XmlElement(name = "adminUserName")
    private String adminUserName = "admin";
    @XmlElement(name = "defaultTenantPassword")
    private String defaultTenantPassword = "user123";
    @XmlElement(name = "tenantDomainExtension")
    private String tenantDomainExtension = ".apps";
    @XmlElement(name = "roleOfSVNRW")
    private String roleOfSVNRW = "RW";
    @XmlElement(name = "keyStoreLocation")
    private String keyStoreLocation = ".apps";
    @XmlElement(name = "keyStorePassword")
    private String keyStorePassword = ".apps";

    public String getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public void setKeyStoreLocation(String keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getRoleOfSVNRW() {
        return roleOfSVNRW;
    }

    public void setRoleOfSVNRW(String roleOfSVNRW) {
        this.roleOfSVNRW = roleOfSVNRW;
    }
}
