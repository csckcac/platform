/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.handlers.ext;

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.rest.RESTConstants;

import java.util.Map;

/**
 * A simple extension mediator for the APIs deployed in the API gateway. This mediator first
 * looks for a sequence named WSO2AM--Ext--[Dir], where [Dir] could be either In or Out
 * depending on the direction of the message. If such a sequence is found, it is invoked.
 * Following that a more API specific extension sequence is looked up by using the name
 * pattern provider--api--version--[Dir]. If such an API specific sequence is found, that
 * is also invoked. If no extension is found either at the global level or at the per API level
 * this mediator simply returns true.
 */
public class APIManagerExtensionMediator extends AbstractMediator {
    
    private static final String EXT_SEQUENCE_PREFIX = "WSO2AM--Ext--";
    private static final String DIRECTION_IN = "In";
    private static final String DIRECTION_OUT = "Out";

    public boolean mediate(MessageContext messageContext) {
        // In order to avoid a remote registry call occurring on each invocation, we
        // directly get the extension sequences from the local registry.
        Map localRegistry = messageContext.getConfiguration().getLocalRegistry();

        String direction = messageContext.isResponse() ? DIRECTION_OUT : DIRECTION_IN;
        Object sequence = localRegistry.get(EXT_SEQUENCE_PREFIX + direction);
        if (sequence != null && sequence instanceof Mediator) {
            if (!((Mediator) sequence).mediate(messageContext)) {
                return false;
            }
        }
        
        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        String version = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        sequence = localRegistry.get(apiName + "--" + version + "--" + direction);
        if (sequence != null && sequence instanceof Mediator) {
            return ((Mediator) sequence).mediate(messageContext);
        }
        return true;
    }
}
