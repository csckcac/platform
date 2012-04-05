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
package org.wso2.carbon.identity.sso.saml.session;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.persistence.IdentityPersistenceManager;
import org.wso2.carbon.identity.sso.saml.SSOServiceProviderConfigManager;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to persist the sessions established with Service providers
 */
public class SSOSessionPersistenceManager {

    private static Log log = LogFactory.getLog(SSOSessionPersistenceManager.class);
    private static SSOSessionPersistenceManager sessionPersistenceManager;
    private Map<String, SessionInfoData> sessionMap = new ConcurrentHashMap<String, SessionInfoData>();

    public static SSOSessionPersistenceManager getPersistenceManager() {
        if (sessionPersistenceManager == null) {
            sessionPersistenceManager = new SSOSessionPersistenceManager();
        }
        return sessionPersistenceManager;
    }

    /**
     * Get the session map
     *
     * @return
     */
    public Map<String, SessionInfoData> getSessionMap() {
        return sessionMap;
    }

    /**
     * Persist session in memory
     *
     * @param sessionId
     * @param subject
     * @param spDO
     */
    public void persistSession(String sessionId, String subject, SAMLSSOServiceProviderDO spDO,
                               String rpSessionId)
            throws IdentityException {
        if (!sessionMap.containsKey(sessionId)) {
            SessionInfoData sessionInfoData = new SessionInfoData(subject);
            sessionInfoData.addServiceProvider(spDO.getIssuer(), spDO, rpSessionId);
            sessionMap.put(sessionId, sessionInfoData);
            replicateSessionInfo(sessionId, subject, spDO, rpSessionId);
        }
        else{
            persistSession(sessionId, spDO.getIssuer(), spDO.getAssertionConsumerUrl(), rpSessionId);
        }
    }

    public boolean persistSession(String sessionId, String issuer, String assertionConsumerURL, String rpSessionId)
            throws IdentityException {
        try {
            if (sessionId != null) {
                if (sessionMap.containsKey(sessionId)) {
                    SessionInfoData sessionInfoData = sessionMap.get(sessionId);
                    String subject = sessionInfoData.getSubject();
                    SAMLSSOServiceProviderDO spDO = SSOServiceProviderConfigManager.getInstance().getServiceProvider(issuer);
                    if (spDO == null) {
                        IdentityPersistenceManager identityPersistenceManager = IdentityPersistenceManager
                                .getPersistanceManager();
                        spDO = identityPersistenceManager.getServiceProvider(
                                AnonymousSessionUtil.getSystemRegistryByUserName(SAMLSSOUtil.getRegistryService(),
                                        SAMLSSOUtil.getRealmService(), subject), issuer);
                    }
                    //give priority to assertion consuming URL if specified in the request
                    if (assertionConsumerURL != null) {
                        spDO.setAssertionConsumerUrl(assertionConsumerURL);
                    }
                    sessionInfoData.addServiceProvider(spDO.getIssuer(), spDO, rpSessionId);
                    replicateSessionInfo(sessionId, subject, spDO, rpSessionId);
                    return true;
                } else {
                    log.error("Error persisting the new session, there is no previously established session for this " +
                            "user");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Error obtaining the service provider info from registry", e);
            throw new IdentityException("Error obtaining the service provider info from registry", e);
        }
        return false;
    }

    /**
     * Get the session infodata for a particular session
     *
     * @param sessionId
     * @return
     */
    public SessionInfoData getSessionInfo(String sessionId) {
        if (sessionId != null) {
            return sessionMap.get(sessionId);
        }
        return null;
    }

    /**
     * Remove a particular session
     *
     * @param sessionId
     */
    public void removeSession(String sessionId, String issuer) {
        if (sessionId != null) {
            sessionMap.remove(sessionId);
        }
    }

    /**
     * Check whether this is an existing session
     *
     * @return
     */
    public boolean isExistingSession(String sessionId) {
        if (sessionMap.containsKey(sessionId)) {
            return true;
        }
        return false;
    }

    private void replicateSessionInfo(String sessionId, String subject,
                                      SAMLSSOServiceProviderDO spDO, String rpSessionId) {
        SSOSessionCommand sessionCommand = new SSOSessionCommand();
        sessionCommand.setUsername(subject);
        sessionCommand.setAssertionConsumerURL(spDO.getAssertionConsumerUrl());
        sessionCommand.setIssuer(spDO.getIssuer());
        sessionCommand.setRpSessionID(rpSessionId);
        sessionCommand.setSsoTokenID(sessionId);
        sessionCommand.setSignOut(false);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Starting to replicate Session Info for TokenID : " + sessionId);
            }
            Replicator.replicateState(sessionCommand,
                                      SAMLSSOUtil.getConfigCtxService().getServerConfigContext().getAxisConfiguration() );
            if (log.isDebugEnabled()) {
                log.debug("Completed replicating Session Info for TokenID : " + sessionId);
            }
        } catch (AxisFault axisFault) {
            log.error("Error when replicating the session info within the cluster", axisFault);
        }
    }
}
