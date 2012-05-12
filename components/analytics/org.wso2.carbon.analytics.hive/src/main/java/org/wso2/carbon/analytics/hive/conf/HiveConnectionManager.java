/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.hive.conf;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.HiveConnectionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class HiveConnectionManager {
    private static final Log log = LogFactory.getLog(HiveConnectionManager.class);
    private HashMap<String, String> credentials;
    public static HiveConnectionManager instance = null;

    private HiveConnectionManager() {
        credentials = new HashMap<String, String>();
    }


    public static HiveConnectionManager getInstance() {
        if (instance == null) {
            instance = new HiveConnectionManager();
        }
        return instance;
    }

    public void loadHiveConnectionConfiguration(BundleContext bundleContext) {
        URL configURL = bundleContext.getBundle().getResource(HiveConstants.HIVE_CONF_LOCAL_PATH +
                HiveConstants.HIVE_CONNECTION_FILE_NAME);
        if (configURL != null) {
            InputStream input = null;
            try {
                input = configURL.openStream();
                if (input != null) {
                    RegistryService registryService = ServiceHolder.getRegistryService();
                    Registry registry = registryService.getConfigSystemRegistry();
                    registry.beginTransaction();
                    try {
                        registry.get(HiveConstants.HIVE_CONNECTION_CONF_PATH + HiveConstants.HIVE_CONNECTION_FILE_NAME);
                    } catch (RegistryException e) {
                        Resource reportFilesResource = registry.newResource();
                        reportFilesResource.setContentStream(input);
                        String location = HiveConstants.HIVE_CONNECTION_CONF_PATH + HiveConstants.HIVE_CONNECTION_FILE_NAME;
                        registry.put(location, reportFilesResource);
                    }
                    input.close();
                    registry.commitTransaction();
                    retrieveConfiguration();
                }
            } catch (RegistryException e) {
                log.error("Exception occured in loading the hive connection configuration", e);
            } catch (IOException e) {
                log.error("No content found in hive-jdbc conf xml", e);
            } catch (HiveConnectionException e) {
                log.error("Error while retireving the credentials from the hive-jdbc-conf.xml");
            } finally {

            }
        }
    }


    public void saveConfiguration(String driver, String url, String username, String password) throws HiveConnectionException {
        Registry registry = null;
        credentials.put(HiveConstants.HIVE_DRIVER_KEY, driver);
        credentials.put(HiveConstants.HIVE_URL_KEY, url);
        credentials.put(HiveConstants.HIVE_USERNAME_KEY, username);
        credentials.put(HiveConstants.HIVE_PASSWORD_KEY, password);
        try {
            registry = ServiceHolder.getRegistryService().getConfigSystemRegistry();
            Resource resource = registry.newResource();
            resource.setContent(getXMLCredentials());
            registry.put(HiveConstants.HIVE_CONNECTION_CONF_PATH + HiveConstants.HIVE_CONNECTION_FILE_NAME, resource);
        } catch (RegistryException e) {
            throw new HiveConnectionException("Failed to get registry", e);
        }
    }

    private String getXMLCredentials() {
        Set<String> keys = credentials.keySet();
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement parent = fac.createOMElement(new QName("configuration"));
        if (keys != null) {
            for (String aKey : keys) {
                OMElement element = fac.createOMElement(new QName(aKey));
                element.setText(credentials.get(aKey));
                parent.addChild(element);
            }
        }
        return  parent.toString();
    }

    private void retrieveConfiguration() throws HiveConnectionException {
        try {
            XMLInputFactory xif = XMLInputFactory.newInstance();
            InputStream confInputStream = getConfigurationFromRegistry();
            XMLStreamReader reader = null;

            reader = xif.createXMLStreamReader(confInputStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement document = builder.getDocument().getOMDocumentElement();
            Iterator iterator = document.getChildElements();
            while (iterator.hasNext()) {
                OMElement element = (OMElement) iterator.next();
                String key = element.getQName().getLocalPart();
                String value = element.getText();
                credentials.put(key, value);
            }
        } catch (XMLStreamException e) {
            log.error("XML Error when saving the hive configuration");
        }

    }


    private InputStream getConfigurationFromRegistry() throws HiveConnectionException {
        String confPath = HiveConstants.HIVE_CONNECTION_CONF_PATH + HiveConstants.HIVE_CONNECTION_FILE_NAME;
        Registry registry = null;
        try {
            registry = ServiceHolder.getRegistryService().getConfigSystemRegistry();
        } catch (RegistryException e) {
            log.error("Error while retrieving the hive configuration from registry", e);
            throw new HiveConnectionException(e.getMessage(), e);
        }
        Resource resource;
        InputStream confOmStream;
        try {
            resource = registry.get(confPath);
            confOmStream = resource.getContentStream();
            return confOmStream;
        } catch (RegistryException e) {
            log.error("Error while retrieving the hive configuration from registry", e);
            throw new HiveConnectionException("Error while retrieving the hive configuration from registry", e);
        }
    }

    public String getConfValue(String confkey){
         return credentials.get(confkey);
    }
}
