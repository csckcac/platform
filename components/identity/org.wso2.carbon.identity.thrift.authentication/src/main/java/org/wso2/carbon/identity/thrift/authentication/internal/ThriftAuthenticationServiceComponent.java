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
package org.wso2.carbon.identity.thrift.authentication.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.authentication.AuthenticationService;
import org.wso2.carbon.identity.thrift.authentication.AuthenticatorService;
import org.wso2.carbon.identity.thrift.authentication.AuthenticatorServiceImpl;
import org.wso2.carbon.identity.thrift.authentication.AuthenticatorServlet;
import org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService;
import org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorServiceImpl;
import org.wso2.carbon.user.core.service.RealmService;

import javax.servlet.ServletException;
import java.util.Hashtable;

/**
 * @scr.component name="org.wso2.carbon.identity.thrift.authentication.internal.ThriftAuthenticationServiceComponent" immediate="true"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService" unbind="unsetHttpService"
 * @scr.reference name="org.wso2.carbon.identity.authentication.internal.AuthenticationServiceComponent"
 * interface="org.wso2.carbon.identity.authentication.AuthenticationService"
 * cardinality="1..1" policy="dynamic" bind="setAuthenticationService"  unbind="unsetAuthenticationService"
 * @scr.reference name="org.wso2.carbon.user.core"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class ThriftAuthenticationServiceComponent {

    private static Log log = LogFactory.getLog(ThriftAuthenticationServiceComponent.class);

    private static HttpService httpServiceInstance;
    private AuthenticationService authenticationService;
    private static RealmService realmServiceInstance;

    private ServiceRegistration thriftAuthenticationservice;

    protected void activate(ComponentContext compCtx) {

        //get an instance of this to register as an osgi service
        ThriftAuthenticatorServiceImpl thriftAuthenticatorServiceImpl =
                ThriftAuthenticatorServiceImpl.getInstance();
        //hand over handler to authentication service
        thriftAuthenticatorServiceImpl.init(authenticationService, realmServiceInstance);

        //register as an osgi service
        thriftAuthenticationservice = compCtx.getBundleContext().registerService(
                ThriftAuthenticatorService.class.getName(), thriftAuthenticatorServiceImpl, null);

        //register AuthenticatorServiceImpl as a thrift service.
        startThriftServices();
    }

    protected void deactivate(ComponentContext compCtx) {

        compCtx.getBundleContext().ungetService(thriftAuthenticationservice.getReference());

    }

    protected void setHttpService(HttpService httpService) {

        httpServiceInstance = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {

        httpServiceInstance = null;
    }

    protected void setAuthenticationService(AuthenticationService authService) {

        authenticationService = authService;
    }

    protected void unsetAuthenticationService(AuthenticationService authService) {
        authenticationService = null;
    }

    protected void setRealmService(RealmService realmService) {
        realmServiceInstance = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        realmServiceInstance = null;
    }

    private void startThriftServices() {
        startThriftAuthenticatorService();
    }

    private void startThriftAuthenticatorService() {
        // Authenticator service should be exposed over SSL. Since Thrift 0.5 doesn't have support
        // for SSL transport this is commented out for now until later Thrift version is used. Using
        // servlet based authenticator service for authentication for now.
        try {
            AuthenticatorService.Processor authServiceProcessor = new AuthenticatorService.Processor(
                    new AuthenticatorServiceImpl());

            TCompactProtocol.Factory inProtFactory = new TCompactProtocol.Factory();
            TCompactProtocol.Factory outProtFactory = new TCompactProtocol.Factory();

            httpServiceInstance.registerServlet("/thriftAuthenticator",
                                                new AuthenticatorServlet(authServiceProcessor,
                                                                         inProtFactory,
                                                                         outProtFactory),
                                                new Hashtable(),
                                                httpServiceInstance.createDefaultHttpContext());
        } catch (ServletException e) {
            log.error("Unable to start thrift Authenticator Service.");
        } catch (NamespaceException e) {
            log.error("Unable to start thrift Authenticator Service");
        }
    }

}
