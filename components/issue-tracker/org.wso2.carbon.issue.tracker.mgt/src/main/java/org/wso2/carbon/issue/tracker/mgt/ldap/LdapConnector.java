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
package org.wso2.carbon.issue.tracker.mgt.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.stratos.common.util.StratosConfiguration;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * this class is responsible for initializing ldap connection
 */
public class LdapConnector {

    public static Log log = LogFactory.getLog(LdapConnector.class.getName());

    public DirContext initLDAPConn() throws NamingException {

        StratosConfiguration stratosConfig = CommonUtil.getStratosConfig();
        String LDAP_PROVIDER_URL =
                stratosConfig.getStratosEventListenerPropertyValue("ldapProviderUrl");

        String LDAP_SECURITY_PRINCIPAL =
                stratosConfig.getStratosEventListenerPropertyValue("ldapSecurityPrincipal");

        String LDAP_SECURITY_CREDENTIALS =
                stratosConfig.getStratosEventListenerPropertyValue("ldapSecurityCredentials");

        Hashtable env = new Hashtable();

        env.put(Context.INITIAL_CONTEXT_FACTORY, LdapConstants.LDAP_INITIAL_CONTEXT_FACTORY);
        env.put(Context.PROVIDER_URL, LDAP_PROVIDER_URL);
        env.put(Context.SECURITY_PRINCIPAL, LDAP_SECURITY_PRINCIPAL);
        env.put(Context.SECURITY_CREDENTIALS, LDAP_SECURITY_CREDENTIALS);

        try {
            DirContext ctx = new InitialDirContext(env);
            return ctx;
        } catch (NamingException e) {

            log.error("Apache LDAP connection failed. PROVIDER_URL:" + LDAP_PROVIDER_URL
                    + ". SECURITY_PRINCIPAL:" + LDAP_SECURITY_PRINCIPAL, e);
            throw e;
        }
    }
}
