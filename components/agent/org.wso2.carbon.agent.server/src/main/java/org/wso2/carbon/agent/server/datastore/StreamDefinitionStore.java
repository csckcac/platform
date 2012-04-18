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
package org.wso2.carbon.agent.server.datastore;

import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.server.exception.StreamDefinitionNotFoundException;

import java.util.Collection;
import java.util.List;

public interface StreamDefinitionStore {

    boolean containsStreamDefinition(String domainName, String name, String version);

    EventStreamDefinition getStreamDefinition(String domainName, String name,
                                              String version)
            throws StreamDefinitionNotFoundException;

    void saveStreamDefinition(String domainName,
                              EventStreamDefinition eventStreamDefinition)
            throws DifferentStreamDefinitionAlreadyDefinedException;

    EventStreamDefinition getStreamDefinition(String domainName, String streamId)
            throws StreamDefinitionNotFoundException;

    Collection<EventStreamDefinition> getAllStreamDefinitions(String domainName)
            throws StreamDefinitionNotFoundException;

    String getStreamId(String domainName, String streamName, String streamVersion)
            throws StreamDefinitionNotFoundException;

    List<EventStreamDefinition> getStreamDefinition(String domainName)
            throws StreamDefinitionNotFoundException;
}
