/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.registry.extensions.validators;

import org.wso2.carbon.governance.registry.extensions.interfaces.CustomValidations;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

public class PropertyValueValidator implements CustomValidations {

    private String propertyName = null;
    private String propertyValue = null;
    private boolean isMultiValued = false;

    public void init(Map parameterMap) {
        if (parameterMap != null) {
            propertyName = (String) parameterMap.get("propertyName");
            propertyValue = (String) parameterMap.get("propertyValue");
            isMultiValued = Boolean.toString(true).equals(parameterMap.get("isMultiValued"));
        }
    }

    public boolean validate(RequestContext context) {
        Resource resource = context.getResource();
        return validatePropertyOfResource(resource);
    }

    protected boolean validatePropertyOfResource(Resource resource) {
        if (propertyName != null && propertyValue != null) {
            if (!isMultiValued) {
                return propertyValue.equals(resource.getProperty(propertyName));
            }
            for (String value : resource.getPropertyValues(propertyName)) {
                if (propertyValue.equals(value)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
