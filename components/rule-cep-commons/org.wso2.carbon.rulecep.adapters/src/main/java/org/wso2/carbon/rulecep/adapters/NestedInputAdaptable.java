/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rulecep.adapters;

import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;


/**
 * An input adapter type that can adapt child resources. For example, in POJO adapter, properties
 * of the POJO class will be children resources
 */
public interface NestedInputAdaptable {

    /**
     * Adapts the given object based on the given parent object and the given resource
     * description of the child input
     *
     * @param childDescription ResourceDescription of the Child
     * @param value            Value of the Parent
     * @param parent           Final calculated value of the child
     */
    void adaptNestedInput(ResourceDescription childDescription,
                          Object value,
                          Object parent);
}
