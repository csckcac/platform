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
package org.wso2.carbon.issue.tracker.mgt.config;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.common.util.StratosConfiguration;
import javax.xml.namespace.QName;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ManagerConfigurations {

    private static final String CONFIG_FILE = "throttling-agent-config.xml";

    private static final String MANAGER_SERVICE_URL_PARAM_NAME = "managerServiceUrl";
    private static final String USERNAME_PARAM_NAME = "userName";
    private static final String PASSWORD_PARAM_NAME = "password";


    private String managerServerUrl;
    private String userName;
    private String password;

    private final static Log log = LogFactory.getLog(ManagerConfigurations.class);

    private static final String CONFIG_NS =
            "http://wso2.com/carbon/multitenancy/throttling/agent/config";
    private static final String PARAMTERS_ELEMENT_NAME = "parameters";
    private static final String PARAMTER_ELEMENT_NAME = "parameter";
    private static final String PARAMTER_NAME_ATTR_NAME = "name";
    private Map<String, String> parameters = new HashMap<String, String>();


    private StratosConfiguration stratosConfiguration=null;

    public StratosConfiguration getStratosConfiguration() {
        return stratosConfiguration;
    }

    public void setStratosConfiguration(StratosConfiguration stratosConfiguration) {
        this.stratosConfiguration = stratosConfiguration;
    }

}
