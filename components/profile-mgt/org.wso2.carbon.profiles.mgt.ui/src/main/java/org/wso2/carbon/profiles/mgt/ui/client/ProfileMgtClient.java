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
package org.wso2.carbon.profiles.mgt.ui.client;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.profiles.mgt.stub.ProfileAdminServiceStub;
import org.wso2.carbon.profiles.mgt.stub.ProfileManagementExceptionException;
import org.wso2.carbon.profiles.mgt.stub.dto.AvailableProfileConfigurationDTO;
import org.wso2.carbon.profiles.mgt.stub.dto.ClaimConfigurationDTO;
import org.wso2.carbon.profiles.mgt.stub.dto.DetailedProfileConfigurationDTO;

import java.rmi.RemoteException;

public class ProfileMgtClient {

    private ProfileAdminServiceStub stub = null;
    private String serviceEndPoint = null;

    private static Log log = LogFactory.getLog(ProfileMgtClient.class);

    public ProfileMgtClient(String cookie, String url, ConfigurationContext configContext)
            throws Exception {
        try {

            this.serviceEndPoint = url + "ProfileAdminService";
            stub = new ProfileAdminServiceStub(configContext, serviceEndPoint);

            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (java.lang.Exception e) {
            log.error(e);
            throw e;
        }
    }

    public AvailableProfileConfigurationDTO getAllAvailableProfileConfiguraions()
            throws Exception {
        try {
            return stub.getAllAvailableProfileConfiguraions();
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public DetailedProfileConfigurationDTO getAllAvailableProfileConfiguraionsForDialect(
            String dialect) throws Exception {
        try {
            return stub.getAllAvailableProfileConfiguraionsForDialect(dialect);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public void updateClaimMappingBehavior(String profileName,
            ClaimConfigurationDTO[] claimConfiguration) throws java.lang.Exception {
        try {
            stub.updateClaimMappingBehavior(profileName, claimConfiguration);
        } catch (java.lang.Exception e) {
            log.error(e);
            throw e;
        }
    }

    public ClaimConfigurationDTO[] getProfileConfiguration(String dialect,
            String profile) throws java.lang.Exception {
        try {
            return stub.getProfileConfiguration(dialect, profile);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public ClaimConfigurationDTO[] getClaimMappings(String dialect)
            throws java.lang.Exception {
        try {
            return stub.getClaimConfigurations(dialect);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public void addProfile(String profileName, String dialect,
            ClaimConfigurationDTO[] claimConfiguration) throws Exception {
        try {
            stub.addProfile(profileName, dialect, claimConfiguration);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public void deleteProfileConfiguraiton(String dialect, String profileName)
            throws Exception {
        try {
            stub.deleteProfileConfiguraiton(dialect, profileName);
        } catch (Exception e) {
            log.error(e);
            throw e;
        }
    }

    public boolean isAddProfileEnabled() throws Exception {
        try {
            return stub.isAddProfileEnabled();
        } catch (Exception e) {
           log.error(e);
            throw e;
        }
    }

}
