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
package org.wso2.carbon.url.mapper.clustermessage.commands;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;

import java.util.*;

public class VirtualHostActivityResponse extends ClusteringCommand{
    private Map<String, String> urlmappings = new HashMap<String, String>();

    public VirtualHostActivityResponse(Map<String, String> urlMappings) {
        urlmappings = urlMappings;
    }

    public Map<String, String> getMappings() {
        return Collections.unmodifiableMap(urlmappings);
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
