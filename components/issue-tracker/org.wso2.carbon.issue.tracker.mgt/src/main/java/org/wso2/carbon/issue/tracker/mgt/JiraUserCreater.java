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


package org.wso2.carbon.issue.tracker.mgt;


import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.wso2.carbon.issue.tracker.core.util.Util;
import org.wso2.carbon.utils.ConfigurationContextService;
public class JiraUserCreater {

    public void createUser() {

        ConfigurationContextService configContext = Util.getConfigurationContextService();
        ServiceClient serviceClient;
        try {
            serviceClient = new ServiceClient(configContext.getClientConfigContext(), null);
            Options options = new Options();
            options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
            

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}
