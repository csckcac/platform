/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.rulecep.service;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.AxisFault;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;

/**
 * this class is used to do the CEP specifice or Rule specifice deployment
 * extensions. For an example CEP suppose to subscribe to Event brokers at the
 * deployment time.
 */

public interface DeploymentExtenstion {

    void doDeploy(AxisService axisService, ServiceDescription serviceDescription) throws AxisFault;
}
