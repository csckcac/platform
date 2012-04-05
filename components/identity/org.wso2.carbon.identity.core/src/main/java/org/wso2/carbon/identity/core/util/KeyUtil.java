/*                                                                             
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.identity.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.core.util.KeyStoreUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.security.keystore.KeyStoreAdmin;
import org.wso2.carbon.security.keystore.service.KeyStoreData;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class KeyUtil {

	private static Log log = LogFactory.getLog(KeyUtil.class);

	/**
	 * 
	 * @param alias
	 * @return
	 * @throws IdentityException
	 */
	public static X509Certificate[] getServiceCertificateChain(String alias)
			throws IdentityException {

		if (log.isDebugEnabled()) {
			log.debug("Retreiving certificate for alias " + alias);
		}

		try {
			KeyStoreAdmin keyAdmin = new KeyStoreAdmin(IdentityTenantUtil.getRegistry(null, null));
			KeyStoreData[] keystores = keyAdmin.getKeyStores();
			if (keystores.length == 0 || keystores.length > 1) {
				throw new IdentityException("There should be only one keystore");
			}
			KeyStoreManager keyMan = KeyStoreManager.getInstance(null);
			KeyStore store = keyMan.getKeyStore(keystores[0].getKeyStoreName());
			Certificate[] certChain = store.getCertificateChain(alias);
			X509Certificate[] certs = new X509Certificate[certChain.length];
			for (int i = 0; i < certs.length; i++) {
				certs[i] = (X509Certificate) certChain[i];
			}
			return certs;
		} catch (Exception e) {
			log.error("Error while retreiving certificate for alias", e);
			throw new IdentityException("Error while retreiving certificate for alias", e);
		}

	}

	/**
	 * 
	 * @param serviceName
	 * @return
	 * @throws IdentityException
	 */
	public static X509Certificate getCertificateToIncludeInMex(String serviceName)
			throws IdentityException {
		X509Certificate cert = null;

		if (log.isDebugEnabled()) {
			log.debug("Retreiving certificate to include in Mex for service " + serviceName);
		}

		try {
			KeyStoreData[] keystores = getServiceKeyStores(serviceName);
			KeyStoreManager keyMan = KeyStoreManager.getInstance(null);
			KeyStoreAdmin keyAdmin = new KeyStoreAdmin(IdentityTenantUtil.getRegistry(null, null));
			KeyStoreData privateStore = null;
			KeyStoreData keyStoreData = null;

			if (keystores != null && keystores.length > 0) {
				for (int i = 0; i < keystores.length; i++) {
					if (KeyStoreUtil.isPrimaryStore(keystores[i].getKeyStoreName())) {
						privateStore = keystores[i];
						break;
					}
				}
			}

			if (privateStore != null) {
				// policy has a private key store
				keyStoreData = keyAdmin.getKeystoreInfo(privateStore.getKeyStoreName());
				cert = getCertificate(privateStore.getKeyStoreName(), keyStoreData.getKey()
						.getAlias());
			} else {
				// this is for UT token policy
				ServerConfiguration config = ServerConfiguration.getInstance();
				String keyalias = config.getFirstProperty("Security.KeyStore.KeyAlias");
				KeyStore store = keyMan.getPrimaryKeyStore();
				cert = (X509Certificate) store.getCertificate(keyalias);
			}

		} catch (Exception e) {
			log.error("Error while retreiving certificate to include in Mex for service ", e);
			throw new IdentityException(
					"Error while retreiving certificate to include in Mex for service ", e);
		}

		return cert;
	}

	/**
	 * 
	 * @param alias
	 * @return
	 * @throws IdentityException
	 */
	public static Key getPrivateKey(String alias) throws IdentityException {

		if (log.isDebugEnabled()) {
			log.debug("Retreiving private key for alias " + alias);
		}

		try {
			KeyStoreAdmin keyAdmin = new KeyStoreAdmin(IdentityTenantUtil.getRegistry(null, null));
			return keyAdmin.getPrivateKey(alias);
		} catch (Exception e) {
			log.error("Error while retreiving private key for alias ", e);
			throw new IdentityException("Error while retreiving private key for alias ", e);
		}
	}

	/**
	 * 
	 * @param keyStoreName
	 * @param alias
	 * @return
	 * @throws IdentityException
	 */
	public static X509Certificate getCertificate(String keyStoreName, String alias)
			throws IdentityException {

		if (log.isDebugEnabled()) {
			log.debug("Retreiving certificate for alias " + alias);
		}

		try {
			KeyStoreManager keyMan = KeyStoreManager.getInstance(null);
			KeyStore store = keyMan.getKeyStore(keyStoreName);
			return (X509Certificate) store.getCertificate(alias);
		} catch (Exception e) {
			log.error("Error while retreiving certificate for alias ", e);
			throw new IdentityException("Error while retreiving certificate for alias ", e);
		}
	}

	/**
	 * 
	 * @param serviceName
	 * @return
	 */
	private static KeyStoreData[] getServiceKeyStores(String serviceName) {
		return null;
	}
}
