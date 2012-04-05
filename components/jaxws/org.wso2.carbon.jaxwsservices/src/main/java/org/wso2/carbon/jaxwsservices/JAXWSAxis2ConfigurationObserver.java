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

package org.wso2.carbon.jaxwsservices;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.PreAxisConfigurationPopulationObserver;

/**
 * This class handles JAXWS related stuff when a new ConfigurationContext is created for a tenant
 */
public class JAXWSAxis2ConfigurationObserver implements PreAxisConfigurationPopulationObserver {

    private static final Log log = LogFactory.getLog(JAXWSAxis2ConfigurationObserver.class);

    public void createdAxisConfiguration(AxisConfiguration axisConfig) {
        if (axisConfig.getParameter(MDQConstants.USE_GENERATED_WSDL) == null) {
            try {
                axisConfig.addParameter(new Parameter(MDQConstants.USE_GENERATED_WSDL, "true"));
            } catch (AxisFault axisFault) {
                log.error("Error while setting the " + MDQConstants.USE_GENERATED_WSDL +
                        " parameter into AxisConfiguration.", axisFault);
            }
        }
    }
}
