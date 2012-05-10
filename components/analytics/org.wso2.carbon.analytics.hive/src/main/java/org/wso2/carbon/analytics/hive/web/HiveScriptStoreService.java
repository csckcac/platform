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


package org.wso2.carbon.analytics.hive.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.exception.HiveScriptStoreException;
import org.wso2.carbon.analytics.hive.persistence.HiveScriptPersistenceManager;

public class HiveScriptStoreService {
    private static final Log log = LogFactory.getLog(HiveScriptStoreService.class);

    public String  retrieveHiveScript(String scriptName) throws HiveScriptStoreException {
      scriptName = validateScriptName(scriptName);
       if(null != scriptName){
       return HiveScriptPersistenceManager.retrieveScript(scriptName);
       }else {
          log.error("Script name is empty. Please provide a valid script name!");
          throw new HiveScriptStoreException("Script name is empty. Please provide a valid script name!");
       }
    }

    public void  saveHiveScript(String scriptName, String scriptContent) throws HiveScriptStoreException {
     scriptName = validateScriptName(scriptName);
       if(null != scriptName){
         HiveScriptPersistenceManager.saveScript(scriptName, scriptContent);
       }else {
          log.error("Script name is empty. Please provide a valid script name!");
          throw new HiveScriptStoreException("Script name is empty. Please provide a valid script name!");
       }
    }

    public String[] getAllScriptNames() throws HiveScriptStoreException {
        try {
            return HiveScriptPersistenceManager.getAllHiveScriptNames();
        } catch (HiveScriptStoreException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void deleteScript(String scriptName) throws HiveScriptStoreException {
      scriptName = validateScriptName(scriptName);
      if(null != scriptName){
         HiveScriptPersistenceManager.deleteScript(scriptName);
       }else {
          log.error("Script name is empty. Please provide a valid script name!");
          throw new HiveScriptStoreException("Script name is empty. Please provide a valid script name!");
       }
    }

    private String validateScriptName(String scriptName){
         if (scriptName.endsWith(HiveConstants.HIVE_SCRIPT_EXT)){
           scriptName = scriptName.substring(0, scriptName.length()-HiveConstants.HIVE_SCRIPT_EXT.length());
       }
        return scriptName;
    }
}
