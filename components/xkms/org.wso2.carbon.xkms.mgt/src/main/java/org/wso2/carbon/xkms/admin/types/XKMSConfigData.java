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
package org.wso2.carbon.xkms.admin.types;

public class XKMSConfigData {

    private String authenCode;
    private String keystoreLocation;
    private String keystorePassword;
    private String serverCertAlias;
    private String serverKeyPassword;
    private String issuerCertAlias;
    private String issuerKeyPassword;
    private String defaultPrivateKeyPassword;
    private int defaultExpriyInterval;
    private boolean persistenceEnabled;

    public String getAuthenCode() {
        return authenCode;
    }

    public void setAuthenCode(String authenCode) {
        this.authenCode = authenCode;
    }

    public String getKeystoreLocation() {
        return keystoreLocation;
    }

    public void setKeystoreLocation(String keystoreLocation) {
        this.keystoreLocation = keystoreLocation;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public String getServerCertAlias() {
        return serverCertAlias;
    }

    public void setServerCertAlias(String serverCertAlias) {
        this.serverCertAlias = serverCertAlias;
    }

    public String getServerKeyPassword() {
        return serverKeyPassword;
    }

    public void setServerKeyPassword(String serverKeyPassword) {
        this.serverKeyPassword = serverKeyPassword;
    }

    public String getIssuerCertAlias() {
        return issuerCertAlias;
    }

    public void setIssuerCertAlias(String issuerCertAlias) {
        this.issuerCertAlias = issuerCertAlias;
    }

    public String getIssuerKeyPassword() {
        return issuerKeyPassword;
    }

    public void setIssuerKeyPassword(String issuerKeyPassword) {
        this.issuerKeyPassword = issuerKeyPassword;
    }

    public String getDefaultPrivateKeyPassword() {
        return defaultPrivateKeyPassword;
    }

    public void setDefaultPrivateKeyPassword(String defaultPrivateKeyPassword) {
        this.defaultPrivateKeyPassword = defaultPrivateKeyPassword;
    }

    public int getDefaultExpriyInterval() {
        return defaultExpriyInterval;
    }

    public void setDefaultExpriyInterval(int defaultExpriyInterval) {
        this.defaultExpriyInterval = defaultExpriyInterval;
    }

    public boolean isPersistenceEnabled() {
        return persistenceEnabled;
    }

    public void setPersistenceEnabled(boolean persistenceEnabled) {
        this.persistenceEnabled = persistenceEnabled;
    }
}
