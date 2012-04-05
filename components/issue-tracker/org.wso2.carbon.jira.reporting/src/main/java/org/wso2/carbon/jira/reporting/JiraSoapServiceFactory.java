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

package org.wso2.carbon.jira.reporting;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;

import javax.xml.rpc.ServiceException;
import java.net.URL;

/**
 * class to represent SOAP session with a JIRA
 */
public class JiraSoapServiceFactory {

    public static JiraSoapService getJiraSoapService(URL url) {
        JiraSoapServiceService serviceLocator = new JiraSoapServiceServiceLocator();
        try {
            return (url != null) ?
                    serviceLocator.getJirasoapserviceV2(url) :
                    serviceLocator.getJirasoapserviceV2();
        } catch (ServiceException e) {
            throw new RuntimeException("Unable to construct SOAP client to " + url, e);
        }
    }
}
