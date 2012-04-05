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
package org.wso2.carbon.endpoint.ui.util;

import org.wso2.carbon.endpoint.common.to.TemplateEndpointData;

import java.util.HashMap;
import java.util.Map;

public class TemplateEndpointHelper {

    public static final String SEP_CHAR = ";";
    private String[] colonSepArray = new String[0];
    private Map<String ,String> cachedMap = null;

    public TemplateEndpointHelper(TemplateEndpointData templateEndpointData){
        colonSepArray = templateEndpointData.getParametersAsColonSepArray();
    }

    public static String[] getColonSepArrayFromMap(Map<String ,String> parameters){
        String paramArray[] = new String[parameters.size()];
        int i = 0;
        for (String key : parameters.keySet()) {
            paramArray[i] = key + SEP_CHAR +parameters.get(key);
            i++;
        }
        return paramArray;
    }
    
    public Map<String ,String> getMapFromColonSepArray(){
        if (cachedMap == null) {
            Map<String ,String> map = new HashMap<String ,String>();
            for (String entry : colonSepArray) {
                String[] entries = entry.split(SEP_CHAR);
                if(entries!=null && entries.length >= 2){
                    map.put(entries[0], entries[1]);
                }
            }
            cachedMap = map;
            return map;
        }
        else{
            return cachedMap;
        }
    }

    public boolean containsKey(String key){
        Map<String,String> mappings = getMapFromColonSepArray();
        return mappings.containsKey(key);
    }
}
