/**********************************************************************
Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Contributors:
    ...
**********************************************************************/
package org.datanucleus.store.types.sco.queued;

import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.scostore.MapStore;

/**
 * Put operation for a map.
 */
public class PutOperation implements QueuedOperation<MapStore>
{
    /** The key to add. */
    private final Object key;

    /** The value to add. */
    private final Object value;

    /**
     * Constructor.
     * @param key The key to add
     * @param value The value to add
     */
    public PutOperation(Object key, Object value)
    {
        this.key = key;
        this.value = value;
    }

    /**
     * Perform the put(Object, Object) operation to the backing store.
     * @param store The backing store to perform it on
     * @param sm StateManager for the owner of the map
     */
    public void perform(MapStore store, ObjectProvider sm)
    {
        store.put(sm, key, value);
    }
}