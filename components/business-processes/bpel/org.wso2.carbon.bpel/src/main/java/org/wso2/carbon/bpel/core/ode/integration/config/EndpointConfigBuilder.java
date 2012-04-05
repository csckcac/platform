/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bpel.core.ode.integration.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.wso2.carbon.bpel.core.ode.integration.BPELConstants;
import org.wso2.carbon.bpel.core.ode.integration.utils.Messages;
import org.wso2.carbon.unifiedendpoint.core.UnifiedEndpointConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Builds the endpoint configuration {@link EndpointConfiguration}
 * using the endpoint details from the deployment descriptor (i.e. deploy.xml)
 */
public class EndpointConfigBuilder {

    private static Log log = LogFactory.getLog(EndpointConfigBuilder.class);

    public static EndpointConfiguration buildEndpointConfiguration(OMElement ele, String basePath) {
        EndpointConfiguration endPointConfig = new EndpointConfiguration();
        endPointConfig.setBasePath(basePath);

        String endpointRef = ele.getAttributeValue(new QName(null, BPELConstants.ENDPOINTREF));

        endPointConfig.setUnifiedEndPointReference(endpointRef);
        log.info("Endpoint reference file:" + endpointRef);

        if (ele.getFirstChildWithName(
                new QName(UnifiedEndpointConstants.WSA_NS, UnifiedEndpointConstants.UNIFIED_EPR)) !=
                                        null) {
            log.info("Found in-line unified endpoint. This will take precedence over the reference");
            endPointConfig.setUepOM(ele.getFirstChildWithName(
                    new QName(UnifiedEndpointConstants.WSA_NS, UnifiedEndpointConstants.UNIFIED_EPR)));
        }

        String mexTimeout = getElementAttributeValue(BPELConstants.MEX_TIMEOUT,
                                                     BPELConstants.VALUE,
                                                     ele);
        if (mexTimeout != null) {
            endPointConfig.setMexTimeout(mexTimeout);
        }

        return endPointConfig;
    }

    /**
     * Get attribute value of the element when attribute name and element is given.
     *
     * @param elementName element name
     * @param attributeName  attribute name of the element.
     * @param parentEle OMElement 
     * @return Value of the attribute as a String
     */
    private static String getElementAttributeValue(String elementName, String attributeName,
                                                   OMElement parentEle) {
        OMElement ele = parentEle.getFirstChildWithName(
                new QName(BPELConstants.BPEL_PKG_ENDPOINT_CONFIG_NS, elementName));
        if (ele != null) {
            Iterator attributes = ele.getAllAttributes();
            while (attributes.hasNext()) {
                OMAttribute attribute = (OMAttribute) attributes.next();
                if (attribute.getLocalName().equals(attributeName)) {
                    return attribute.getAttributeValue();
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(Messages.msgElementAttributeValueNotFound(elementName, attributeName));
        }

        return null;
    }
}


