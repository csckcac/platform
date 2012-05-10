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

package org.wso2.carbon.analytics.hive.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.HiveScriptStoreException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HiveScriptPersistenceManager {

    private static final Log log = LogFactory.getLog(HiveScriptPersistenceManager.class);

    private HiveScriptPersistenceManager() {

    }

    public static String retrieveScript(String scriptName) throws HiveScriptStoreException {
        Registry registry;
        Resource resource;
        InputStream scriptStream = null;
        String script = "";
        try {
            registry = ServiceHolder.getRegistryService().getConfigSystemRegistry();
        } catch (RegistryException e) {
            throw new HiveScriptStoreException("Failed to get registry", e);
        }
        String scriptPath = HiveConstants.HIVE_SCRIPT_BASE_PATH + scriptName + HiveConstants.HIVE_SCRIPT_EXT;

        try {
            resource = registry.get(scriptPath);
            scriptStream = resource.getContentStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(scriptStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            br.close();
            script = sb.toString();
            script = script.replaceAll(";", ";\n");
            return script;
        } catch (RegistryException e) {
            log.error("Error while retrieving the script - " + scriptName + " from registry", e);
            throw new HiveScriptStoreException("Error while retrieving the script - " + scriptName + " from registry", e);
        } catch (IOException e) {
            log.error("Error while retrieving the script - " + scriptName + " from registry", e);
            throw new HiveScriptStoreException("Error while retrieving the script - " + scriptName + " from registry", e);
        }
    }

    public static void saveScript(String scriptName, String scriptContent) throws HiveScriptStoreException {
        Registry registry = null;
        try {
            registry = ServiceHolder.getRegistryService().getConfigSystemRegistry();
            Resource resource = registry.newResource();
            resource.setContent(scriptContent);
            registry.put(HiveConstants.HIVE_SCRIPT_BASE_PATH + scriptName + HiveConstants.HIVE_SCRIPT_EXT, resource);
        } catch (RegistryException e) {
            throw new HiveScriptStoreException("Failed to get registry", e);
        }
    }


    public static String[] getAllHiveScriptNames() throws HiveScriptStoreException {
        Resource resource;
        ArrayList<String> scriptNames = null;
        try {
            Registry registry = ServiceHolder.getRegistryService().getConfigSystemRegistry();
            if (registry.resourceExists(HiveConstants.HIVE_SCRIPT_BASE_PATH)) {
                resource = registry.get(HiveConstants.HIVE_SCRIPT_BASE_PATH);
                if (resource instanceof Collection) {
                    scriptNames = new ArrayList<String>();
                    String[] paths = ((Collection) resource).getChildren();
                    for (String resourcePath : paths) {
                        Resource childResource = registry.get(resourcePath);

                        if (!(childResource instanceof Collection)) {
                            String name = ((ResourceImpl) childResource).getName();
                            scriptNames.add(name.split(HiveConstants.HIVE_SCRIPT_EXT)[0]);
                        }

                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.info("No scripts avaliable");
                }
            }
        } catch (RegistryException e) {
            throw new HiveScriptStoreException("Error occurred getting all the script names", e);
        }
        String[] nameArray = new String[0];
        if(scriptNames != null && scriptNames.size()>0){
            nameArray = new String[scriptNames.size()];
            nameArray = scriptNames.toArray(nameArray);
        }
        return nameArray;
    }


    public static void deleteScript(String scriptName) throws HiveScriptStoreException {
        try {
                  Registry registry = ServiceHolder.getRegistryService().getConfigSystemRegistry();
                   if (registry.resourceExists(HiveConstants.HIVE_SCRIPT_BASE_PATH)) {
                       registry.delete(HiveConstants.HIVE_SCRIPT_BASE_PATH + scriptName + HiveConstants.HIVE_SCRIPT_EXT);
                   } else {
                       if (log.isDebugEnabled()) {
                           log.info("no any script called " + scriptName + " , to delete");
                       }
                   }
               } catch (RegistryException e) {
                   throw new HiveScriptStoreException("Error occurred deleting the script : "+ scriptName, e);
               }


    }
}
