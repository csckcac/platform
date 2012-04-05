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
package org.wso2.carbon.endpoint.common.to;

import java.util.HashMap;
import java.util.Map;

public class TemplateEndpointData {

    private String targetTemplate = null;

//    private Map<String, String> parameters = new HashMap<String, String>();

    private String[]  parameterArray = new String[0];

    private int epType = 5;

    public String getTargetTemplate() {
        return targetTemplate;
    }

    public void setTargetTemplate(String targetTemplate) {
        this.targetTemplate = targetTemplate;
    }


/*
    public Map<String, String> getParameters() {
        return parameters;
    }
*/

    /**
     * this method is included to avoid Code Gen issues in e
     * @return
     */
    public String[]  getParametersAsColonSepArray() {
        return parameterArray;
    }

    public void  setParametersAsColonSepArray(String[] paramArray) {
        parameterArray = paramArray;
    }

/*
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
*/

/*
    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }
*/

    public int getEpType() {
        return epType;
    }

    public void setEpType(int epType) {
        this.epType = epType;
    }
}
