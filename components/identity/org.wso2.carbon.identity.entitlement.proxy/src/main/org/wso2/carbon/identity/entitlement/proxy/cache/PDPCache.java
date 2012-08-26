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
package org.wso2.carbon.identity.entitlement.proxy.cache;

import java.util.LinkedHashMap;
import java.util.Map.Entry;


public class PDPCache<K, V> extends LinkedHashMap<K, V> {


    private static final long serialVersionUID = 1L;

    private int mMaxEntries;

    public PDPCache(int maxEntries) {
        // removeEldestEntry() is called after a put(). To allow maxEntries in
        // cache, capacity should be maxEntries + 1 (for the entry which will be
        // removed). Load factor is taken as 1 because size is fixed. This is
        // less space efficient when very less entries are present, but there
        // will be no effect on time complexity for get(). The third parameter
        // in the base class constructor says that this map is
        // insertion-order oriented.
        super(maxEntries + 1, 1, false);
        mMaxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        // After size exceeds max entries, this statement returns true and the
        // oldest value will be removed. Behaves like a queue, the first
        // inserted value will go away.
        return size() > mMaxEntries;
    }

}