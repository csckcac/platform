/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.dao;

import java.io.Serializable;

/**
 * Interface marks class which can be persisted.
 *
 * @param <PK> type of the primary key, it must be serializable
 */
@Deprecated
public interface EntityInterface<PK extends Serializable> extends Serializable{
    /**
     * Property which represents id
     */
    String P_ID = "id";

    /**
     * Get primary key
     * @return primary key
     */
    PK getId();

    /**
     * Set primary key
     * @param id primary key
     */
    void setId(PK id);
}
