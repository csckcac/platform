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

package org.wso2.carbon.registry.jcr.retention;

import javax.jcr.RepositoryException;
import javax.jcr.retention.RetentionPolicy;


public class RegistryRetentionPolicy implements RetentionPolicy {

    private String name = "";
    public String description = "";

    /**
     * @param name        -name of the policy
     * @param description - An RetentionPolicy is an object with a name and an optional description.So here we
     *                    set the constructor to set a description to the policy
     */

    public RegistryRetentionPolicy(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() throws RepositoryException {
        return name;
    }


}
