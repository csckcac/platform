/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.jcr.util.test.data;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.jcr.RegistrySession;
import org.wso2.carbon.registry.jcr.retention.RegistryRetentionPolicy;

import javax.jcr.RepositoryException;
import javax.jcr.retention.RetentionPolicy;
import java.util.Map;

public class TCKTestDataLoader {

public static void loadRetentionPolicies(Map<String, RetentionPolicy> retentionPolicies, RegistrySession session) throws RegistryException, RepositoryException {

//    if(!session.getUserRegistry().resourceExists("/jcr_system/workspaces/default_workspace/test_policy_holder")){
//       session.getRootNode().addNode("test_policy_holder");
//    }
//    retentionPolicies.put("/jcr_system/workspaces/default_workspace/test_policy_holder",
//            new RegistryRetentionPolicy("test_policy_holder","test_policy_description"));
}

}
