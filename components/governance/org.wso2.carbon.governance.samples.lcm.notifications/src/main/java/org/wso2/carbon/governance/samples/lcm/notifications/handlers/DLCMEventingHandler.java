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
package org.wso2.carbon.governance.samples.lcm.notifications.handlers;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.samples.lcm.notifications.internal.Utils;
import org.wso2.carbon.governance.samples.lcm.notifications.handlers.utils.events.ApprovalNeededEvent;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.common.eventing.RegistryEvent;
import org.wso2.carbon.registry.core.session.CurrentSession;

public class DLCMEventingHandler extends Handler {
    private static final Log log = LogFactory.getLog(DLCMEventingHandler.class);

    public void init(String defaultNotificationEndpoint) {
        Utils.setDefaultNotificationServiceURL(defaultNotificationEndpoint);
    }

    public DLCMEventingHandler() {
        try {
            Utils.getRegistryNotificationService().registerEventType("custom:Approval Needed", ApprovalNeededEvent.EVENT_NAME, ApprovalNeededEvent.EVENT_NAME);
            /*Utils.getRegistryNotificationService().registerEventTypeExclusion("custom:Approval Needed", "/");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("custom:Approval Needed", "/system");
            Utils.getRegistryNotificationService().registerEventTypeExclusion("custom:Approval Needed", "/system/.*");*/
        } catch (Exception e) {
            handleException("Unable to register Event Types", e);
        }
    }

    public void put(RequestContext requestContext) throws RegistryException {
        String path = requestContext.getResourcePath().getPath();
        Resource resource = requestContext.getResource();
        if (resource == null || resource.getProperties() == null) {
            return;
        }
        boolean allItemsAreChecked = true;
        String currentState = null;
        String lastState = resource.getProperty("registry.dlcm.last.state");
        String aspectName = resource.getProperty("registry.LC.name");
        Properties props = resource.getProperties();
        Iterator iKeys = props.keySet().iterator();
        while (iKeys.hasNext()) {
            String propKey = (String) iKeys.next();
            if (propKey.matches("registry\\p{Punct}lifecycle\\p{Punct}.*\\p{Punct}state")) {
                currentState = resource.getProperty(propKey);
                if (lastState != null) {
                    break;
                }
            }
        }
        if (currentState == null || (lastState != null && lastState.equals(currentState))) {
            return;
        }
        iKeys = props.keySet().iterator();
        while (iKeys.hasNext()) {
            String propKey = (String) iKeys.next();
            if (propKey.matches("registry\\p{Punct}.*\\p{Punct}checklist\\p{Punct}.*")) {
                List<String> propValues = (List<String>) props.get(propKey);
                if (propValues == null)
                    continue;
                if (propValues.size() > 2) {
                    String value = null;
                    String lifeCycleState = null;
                    for (String param : propValues) {
                        if (param.startsWith("status:")) {
                            lifeCycleState = param.substring(7);
                        } else if (param.startsWith("value:")) {
                            value = param.substring(6);
                        }
                    }
                    if ((lifeCycleState != null) && (value != null)) {
                        if (lifeCycleState.equalsIgnoreCase(currentState)) {
                            if (value.equalsIgnoreCase("false")) {
                                allItemsAreChecked = false;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (!allItemsAreChecked) {
            return;
        }
        String[] actions = null;
        try {
            Resource oldResource = requestContext.getRepository().get(path);
            requestContext.getRepository().put(path, resource);
            actions = requestContext.getRegistry().getAspectActions(path, aspectName);
            requestContext.getRepository().put(path, oldResource);
            if (actions == null) {
                return;
            }
        } catch (RegistryException e) {
            return;
        }
        if (actions.length > 0 && !actions[0].equals("promote")) {
            RegistryEvent<String> event = new ApprovalNeededEvent<String>(
                    "Approval is needed to Promote '" + path + "' from lifecycle state '" +
                            currentState + "'.");
            ((ApprovalNeededEvent)event).setResourcePath(path);
            event.setTenantId(CurrentSession.getCallerTenantId());
            try {
                notify(event, requestContext.getRegistry(), path);
            } catch (Exception e) {
                handleException("Unable to send notification for Put Operation", e);
            }
        }
    }

    protected void notify(RegistryEvent event, Registry registry, String path) throws Exception {
        try {
            if (Utils.getRegistryNotificationService() == null) {
                log.debug("Eventing service is unavailable.");
                return;
            }
            if (registry == null || registry.getEventingServiceURL(path) == null) {
                Utils.getRegistryNotificationService().notify(event);
                return;
            } else if (Utils.getDefaultNotificationServiceURL() == null) {
                log.error("DLCM Eventing Handler is not properly initialized");
            } else if (registry.getEventingServiceURL(path).equals(Utils.getDefaultNotificationServiceURL())) {
                Utils.getRegistryNotificationService().notify(event);
                return;
            } else {
                Utils.getRegistryNotificationService().notify(event, registry.getEventingServiceURL(path));
                return;
            }
        } catch (RegistryException e) {
            log.error("Unable to send notification", e);
        }
        log.error("Unable to send notification");
    }

    private void handleException(String message) {
        log.error(message);
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
    }
}

