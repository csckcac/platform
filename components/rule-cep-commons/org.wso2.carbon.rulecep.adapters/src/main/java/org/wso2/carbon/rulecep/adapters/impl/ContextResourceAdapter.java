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
package org.wso2.carbon.rulecep.adapters.impl;

import org.wso2.carbon.rulecep.adapters.InputAdaptable;
import org.wso2.carbon.rulecep.adapters.ResourceAdapter;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;


/**
 * Adapts the message context 'as is'
 */
public class ContextResourceAdapter extends ResourceAdapter implements InputAdaptable {

    public final static String TYPE = "context";

    public String getType() {
        return TYPE;
    }

    /**
     * Adapts the message context 'as is'
     *
     * @param resourceDescription Input ResourceDescription
     * @param tobeAdapted         The final calculated value ,
     *                            only need to convert that into correct type
     * @return Returns the provided message context 'as is'
     */
    public Object adaptInput(ResourceDescription resourceDescription, Object tobeAdapted) {
        return tobeAdapted;
    }
}
