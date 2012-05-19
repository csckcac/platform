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

package org.wso2.carbon.business.messaging.hl7.transport;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.ProtocolEndpoint;
import org.wso2.carbon.business.messaging.hl7.common.HL7Constants;
import org.wso2.carbon.business.messaging.hl7.common.HL7ProcessingContext;

import ca.uhn.hl7v2.HL7Exception;

public class HL7Endpoint extends ProtocolEndpoint {

    private int port = HL7Constants.DEFAULT_SYNAPSE_HL7_PORT;
    
    private HL7ProcessingContext processingContext;
    
    @Override
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
    	if (params instanceof AxisService) {
            this.port = ParamUtils.getOptionalParamInt(params, HL7Constants.HL7_PORT, -1);
            if (this.port == -1) {
            	return false;
            }
            this.processingContext = this.createProcessingContext(params);
            return true;
        } else {
        	return false;
        }
    }
    
    private HL7ProcessingContext createProcessingContext(ParameterInclude params) throws AxisFault {
    	try {
    		return new HL7ProcessingContext(params);
    	} catch (HL7Exception e) {
			throw new AxisFault("Error creating HL7 processing context: " + e.getMessage(), e);
		}
    }
    
    public HL7ProcessingContext getProcessingContext() {
    	return processingContext;
    }

    public EndpointReference[] getEndpointReferences(AxisService axisService, String ip) throws AxisFault {
        String url = HL7Constants.TRANSPORT_NAME + "://" + ip + ":" + port;
        String context = getListener().getConfigurationContext().getServiceContextPath();
        if (!context.startsWith("/")) {
            context = "/" + context;
        }

        if (!context.endsWith("/")) {
            context += "/";
        }

        url += context + axisService.getName();

        return new EndpointReference[] {
            new EndpointReference(url)
        };
    }

    public int getPort() {
        return port;
    }
}
