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
package org.wso2.carbon.identity.entitlement.thrift;

import org.apache.thrift.TException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.thrift.authentication.ThriftAuthenticatorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.ThriftSession;
import org.wso2.carbon.utils.multitenancy.CarbonContextHolder;

import java.util.List;

/**
 * Thrift based EntitlementService that is exposed by wrapping EntitlementService.
 */
public class ThriftEntitlementServiceImpl extends AbstractAdmin
        implements EntitlementService.Iface {
    private static Log log = LogFactory.getLog(ThriftEntitlementServiceImpl.class);
    /*Handler to ThriftAuthenticatorService which handles authentication to admin services.*/
    private static ThriftAuthenticatorService thriftAuthenticatorService;
    /*Handler to actual entitlement service which is going to be wrapped by thrift interface*/
    private static org.wso2.carbon.identity.entitlement.EntitlementService entitlementService;

    /**
     * Init the AuthenticationService handler to be used for authentication.
     */
    public static void init(ThriftAuthenticatorService authenticatorService) {
        thriftAuthenticatorService = authenticatorService;
        entitlementService = new org.wso2.carbon.identity.entitlement.EntitlementService();

    }

    /**
     * Thrift based service method that wraps the same in EntitlementService
     *
     * @param request   : XACML request
     * @param sessionId : a sessionId obtained by authenticating to thrift based authentication
     *                  service.
     * @return
     * @throws EntitlementException
     * @throws TException
     */
    public String getDecision(String request, String sessionId)
            throws EntitlementException, TException {

        try {
            if (thriftAuthenticatorService != null && entitlementService != null) {

                /*Authenticate session from thrift based authentication service.*/

                if (thriftAuthenticatorService.isAuthenticated(sessionId)) {

                    //obtain the thrift session for this session id
                    ThriftSession currentSession = thriftAuthenticatorService.getSessionInfo(sessionId);

                    //obtain a dummy carbon context holder
                    CarbonContextHolder carbonContextHolder =
                            CarbonContextHolder.getCurrentCarbonContextHolder();

                    /*start tenant flow to stack up any existing carbon context holder base,
                    and initialize a raw one */
                    carbonContextHolder.startTenantFlow();

                    try {

                        //need to populate current carbon context from the one created at authentication
                        populateCurrentCarbonContextFromAuthSession(carbonContextHolder, currentSession);

                        //perform the actual operation
                        return entitlementService.getDecision(request);

                    } finally {
                        carbonContextHolder.endTenantFlow();
                    }

                } else {
                    String authErrorMsg = "User is not authenticated. Please login first.";
                    log.error(authErrorMsg);
                    throw new EntitlementException(authErrorMsg);
                }

            } else {
                String initErrorMsg = "Thrift Authenticator service or Entitlement " +
                                      "service is not initialized.";
                log.error(initErrorMsg);
                throw new EntitlementException(initErrorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred when invoking the Thrift based Entitlement Service.";
            log.error(errorMsg, e);
            throw new EntitlementException(errorMsg);
        }
    }

    public String getDecisionByAttributes(String subject, String resource, String action,
                                          List<String> environment, String sessionID)
            throws EntitlementException, TException {
        try {
            if (thriftAuthenticatorService != null && entitlementService != null) {

                /*Authenticate session from thrift based authentication service.*/

                if (thriftAuthenticatorService.isAuthenticated(sessionID)) {

                    //obtain the thrift session for this session id
                    ThriftSession currentSession = thriftAuthenticatorService.getSessionInfo(sessionID);

                    //obtain a dummy carbon context holder
                    CarbonContextHolder carbonContextHolder =
                            CarbonContextHolder.getCurrentCarbonContextHolder();

                    /*start tenant flow to stack up any existing carbon context holder base,
                    and initialize a raw one */
                    carbonContextHolder.startTenantFlow();

                    try {

                        //need to populate current carbon context from the one created at authentication
                        populateCurrentCarbonContextFromAuthSession(carbonContextHolder, currentSession);

                        //perform the actual operation
                        return entitlementService.getDecisionByAttributes(
                                subject, resource, action, environment.toArray(
                                        new String[environment.size()]));

                    } finally {
                        carbonContextHolder.endTenantFlow();
                    }

                } else {
                    String authErrorMsg = "User is not authenticated. Please login first.";
                    log.error(authErrorMsg);
                    throw new EntitlementException(authErrorMsg);
                }

            } else {
                String initErrorMsg = "Thrift Authenticator service or Entitlement " +
                                      "service is not initialized.";
                log.error(initErrorMsg);
                throw new EntitlementException(initErrorMsg);
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred when invoking the Thrift based Entitlement Service.";
            log.error(errorMsg, e);
            throw new EntitlementException(errorMsg);
        }
    }

    /**
     * CarbonContextHolderBase is thread local. So we need to populate it with the one created
     * at user authentication.
     *
     * @param authSession
     */
    private void populateCurrentCarbonContextFromAuthSession(
            CarbonContextHolder carbonContextHolder, ThriftSession authSession) {

        //read parameters from it and set it in surrent carbon context for this thread
        CarbonContextHolder storedCarbonCtxHolder = (CarbonContextHolder)
                authSession.getSessionCarbonContextHolder();

        carbonContextHolder.setUsername(storedCarbonCtxHolder.getUsername());
        carbonContextHolder.setTenantDomain(storedCarbonCtxHolder.getTenantDomain());
        carbonContextHolder.setTenantId(storedCarbonCtxHolder.getTenantId());
        carbonContextHolder.setProperty(CarbonContextHolder.CONFIG_SYSTEM_REGISTRY_INSTANCE,
                                        storedCarbonCtxHolder.getProperty(
                                                CarbonContextHolder.CONFIG_SYSTEM_REGISTRY_INSTANCE));
        carbonContextHolder.setProperty(CarbonContextHolder.GOVERNANCE_SYSTEM_REGISTRY_INSTANCE,
                                        storedCarbonCtxHolder.getProperty(
                                                CarbonContextHolder.GOVERNANCE_SYSTEM_REGISTRY_INSTANCE));
        carbonContextHolder.setProperty(CarbonContextHolder.CONFIG_USER_REGISTRY_INSTANCE,
                                        storedCarbonCtxHolder.getProperty(
                                                CarbonContextHolder.CONFIG_USER_REGISTRY_INSTANCE));
        carbonContextHolder.setProperty(CarbonContextHolder.GOVERNANCE_USER_REGISTRY_INSTANCE,
                                        storedCarbonCtxHolder.getProperty(
                                                CarbonContextHolder.GOVERNANCE_USER_REGISTRY_INSTANCE));
        carbonContextHolder.setProperty(CarbonContextHolder.USER_REALM,
                                        storedCarbonCtxHolder.getProperty(CarbonContextHolder.USER_REALM));

    }
}
