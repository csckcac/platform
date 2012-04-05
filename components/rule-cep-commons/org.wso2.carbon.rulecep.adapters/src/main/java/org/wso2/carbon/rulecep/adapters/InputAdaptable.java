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
 * Adapts the given object based on the resource description. Note here that the given tobeAdapted is
 * the data and only need to convert that into the correct target type.
 */
public interface InputAdaptable {

    /**
     * Converts a provided object into the object type defined in the resource description
     *
     * @param resourceDescription input resource description
     * @param tobeAdapted         The final calculated value ,
     *                            only need to convert it into correct type
     * @return the converted object representing expected type
     */
    Object adaptInput(ResourceDescription resourceDescription,
                      Object tobeAdapted);
}
