/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.discovery.proxy;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAbstractFactory;
import org.wso2.carbon.discovery.DiscoveryOMUtils;
import org.wso2.carbon.discovery.DiscoveryException;
import org.wso2.carbon.discovery.DiscoveryConstants;
import org.wso2.carbon.discovery.util.DiscoveryServiceUtils;
import org.wso2.carbon.discovery.messages.*;

/**
 * Web Services Dynamic Discovery proxy implementation. This service implementation
 * supports the four basic operations of a WS-Discovery proxy (managed mode) as described
 * in the protocol specification. Supported operations are Hello, Bye, Probe and Resolve.
 * Service metadata are stored in a WSO2 governance registry instance for later reference.
 */
public class DiscoveryProxy {

    public void Hello(OMElement helloElement) throws DiscoveryException {
        Notification hello = DiscoveryOMUtils.getHelloFromOM(helloElement);
        try {
            DiscoveryServiceUtils.addService(hello.getTargetService());
        } catch (Exception e) {
            throw new DiscoveryException("Error while persisting the " +
                    "service description", e);
        }
    }

    public void Bye(OMElement byeElement) throws DiscoveryException {
        Notification bye = DiscoveryOMUtils.getByeFromOM(byeElement);
        try {
            DiscoveryServiceUtils.removeServiceEndpoints(bye.getTargetService());
        } catch (Exception e) {
            throw new DiscoveryException("Error while persisting the " +
                    "service description", e);
        }
    }

    public OMElement Probe(OMElement probeElement) throws DiscoveryException {
        Probe probe = DiscoveryOMUtils.getProbeFromOM(probeElement);
        try {
            TargetService[] services = DiscoveryServiceUtils.findServices(probe);
            QueryMatch match = new QueryMatch(DiscoveryConstants.RESULT_TYPE_PROBE_MATCH,
                    services);
            return DiscoveryOMUtils.toOM(match, OMAbstractFactory.getSOAP11Factory());
        } catch (Exception e) {
            throw new DiscoveryException("Error while searching for services", e);
        }
    }

    public OMElement Resolve(OMElement resolveElement) throws DiscoveryException {
        Resolve resolve = DiscoveryOMUtils.getResolveFromOM(resolveElement);
        try {
            TargetService service = DiscoveryServiceUtils.getService(resolve.getEpr());
            QueryMatch match = new QueryMatch(DiscoveryConstants.RESULT_TYPE_RESOLVE_MATCH,
                    new TargetService[] { service });
            return DiscoveryOMUtils.toOM(match, OMAbstractFactory.getSOAP11Factory());
        } catch (Exception e) {
            throw new DiscoveryException("Error while resolving the service with ID: " +
                resolve.getEpr().getAddress());
        }
    }

}
