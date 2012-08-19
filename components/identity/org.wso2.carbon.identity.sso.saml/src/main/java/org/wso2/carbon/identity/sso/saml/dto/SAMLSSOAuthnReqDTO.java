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
package org.wso2.carbon.identity.sso.saml.dto;


public class SAMLSSOAuthnReqDTO {

    private String username;
    private String password;
    private String issuer;
    private String subject;
    private String assertionConsumerURL;
    private String id;
    private String claim;
    private String nameIDFormat;
    private String logoutURL;
    private String loginPageURL;
    private String rpSessionId;
    private String assertionString;
    private boolean doSingleLogout;
    private boolean doSignAssertions;
    private boolean useFullyQualifiedUsernameAsSubject;
    private String[] requestedClaims;
    

    public String getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    private String certAlias;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAssertionConsumerURL() {
        return assertionConsumerURL;
    }

    public void setAssertionConsumerURL(String assertionConsumerURL) {
        this.assertionConsumerURL = assertionConsumerURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameIDFormat() {
        return nameIDFormat;
    }

    public void setNameIDFormat(String nameIDFormat) {
        this.nameIDFormat = nameIDFormat;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public String getLogoutURL() {
        return logoutURL;
    }

    public void setLogoutURL(String logoutURL) {
        this.logoutURL = logoutURL;
    }

    public boolean getUseFullyQualifiedUsernameAsSubject() {
        return useFullyQualifiedUsernameAsSubject;
    }

    public void setUseFullyQualifiedUsernameAsSubject(boolean useFullyQualifiedUsernameAsSubject) {
        this.useFullyQualifiedUsernameAsSubject = useFullyQualifiedUsernameAsSubject;
    }

    public boolean isDoSingleLogout() {
        return doSingleLogout;
    }

    public void setDoSingleLogout(boolean doSingleLogout) {
        this.doSingleLogout = doSingleLogout;
    }

    public String getLoginPageURL() {
        return loginPageURL;
    }

    public void setLoginPageURL(String loginPageURL) {
        this.loginPageURL = loginPageURL;
    }

    public String getRpSessionId() {
        return rpSessionId;
    }

    public void setRpSessionId(String rpSessionId) {
        this.rpSessionId = rpSessionId;
    }

    public boolean getDoSignAssertions() {
        return doSignAssertions;
    }

    public void setDoSignAssertions(boolean doSignAssertions) {
        this.doSignAssertions = doSignAssertions;
    }

    public String getAssertionString() {
        return assertionString;
    }

    public void setAssertionString(String assertionString) {
        this.assertionString = assertionString;
    }

	public String[] getRequestedClaims() {
	    return requestedClaims;
    }

	public void setRequestedClaims(String[] requestedClaims) {
	    this.requestedClaims = requestedClaims;
    }

 }
