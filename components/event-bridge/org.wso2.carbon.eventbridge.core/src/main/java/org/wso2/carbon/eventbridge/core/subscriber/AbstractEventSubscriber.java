package org.wso2.carbon.eventbridge.core.subscriber;

import org.wso2.carbon.eventbridge.core.beans.EventStreamDefinition;
import org.wso2.carbon.eventbridge.core.exceptions.DifferentStreamDefinitionAlreadyDefinedException;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public abstract class AbstractEventSubscriber implements EventSubscriber {

    private String constructNameVersionKey(String name, String version) {
        return name + "::" + version;
    }


    public void saveStreamDefinition(String domainName,
                                     EventStreamDefinition eventStreamDefinition, String username, String password)
            throws DifferentStreamDefinitionAlreadyDefinedException {

            saveStreamIdToStore(domainName, constructNameVersionKey(eventStreamDefinition.getName(), eventStreamDefinition.getVersion()), eventStreamDefinition.getStreamId(),  username, password);
            saveStreamDefinitionToStore(domainName, eventStreamDefinition.getStreamId(), eventStreamDefinition,  username, password);


    }


    protected abstract void saveStreamIdToStore(String domainName, String streamIdKey,
                                                String streamId, String username, String password) throws DifferentStreamDefinitionAlreadyDefinedException;

    protected abstract void saveStreamDefinitionToStore(String domainName, String streamId,
                                                        EventStreamDefinition streamDefinition, String username, String password) throws DifferentStreamDefinitionAlreadyDefinedException;




}
