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
package org.wso2.carbon.identity.authenticator.sso.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.core.common.IAuthenticationAdmin;
import org.wso2.carbon.core.common.IFileUpload;
import org.wso2.carbon.core.common.IFileDownload;
import org.wso2.carbon.core.services.authentication.AuthenticationAdmin;
import org.wso2.carbon.core.services.filedownload.FileDownloadService;
import org.wso2.carbon.core.services.fileupload.FileUploadService;
import org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService;
import org.wso2.carbon.core.services.callback.LoginSubscriptionManagerServiceImpl;

/**
 * @scr.component name="sso.authenticator.dscomponent" immediate="true"
 * @scr.reference name="registry.service"
 *                interface="org.wso2.carbon.registry.core.service.RegistryService"
 *                cardinality="1..1" policy="dynamic" bind="setRegistryService"
 *                unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 *                interface="org.wso2.carbon.user.core.service.RealmService"
 *                cardinality="1..1" policy="dynamic" bind="setRealmService"
 *                unbind="unsetRealmService"
 */
public class SSOAuthenticatorDSComponent {

    private static LoginSubscriptionManagerServiceImpl loginSubscriptionManagerServiceImpl =
            new LoginSubscriptionManagerServiceImpl();

    private static final Log log = LogFactory.getLog(SSOAuthenticatorDSComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            BundleContext bc = ctxt.getBundleContext();
            bc.registerService(IAuthenticationAdmin.class.getName(), new AuthenticationAdmin(), null);
            bc.registerService(IFileUpload.class.getName(), new FileUploadService(), null);
            bc.registerService(IFileDownload.class.getName(), new FileDownloadService(), null);
            bc.registerService(LoginSubscriptionManagerService.class.getName(),
                    loginSubscriptionManagerServiceImpl, null);

            log.debug("Carbon Core Services bundle is activated ");
        } catch (Throwable e) {
            log.error("Failed to activate Carbon Core Services bundle ", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        log.debug("Carbon Core Services bundle is deactivated ");
    }
   
    protected void setRegistryService(RegistryService registryService) {
        SSOAuthBEDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        SSOAuthBEDataHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        SSOAuthBEDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        SSOAuthBEDataHolder.getInstance().setRealmService(null);
    }
}
