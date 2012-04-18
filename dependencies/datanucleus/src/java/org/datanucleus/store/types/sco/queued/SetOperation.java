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
import org.datanucleus.store.scostore.ListStore;

/**
 * Set operation for a list.
 */
public class SetOperation implements QueuedOperation<ListStore>
{
    /** The position to set the value at. */
    private final int index;

    /** The value to set. */
    private final Object value;

    /** Whether to allow cascade-delete checks. */
    boolean allowCascadeDelete = true;

    /**
     * Constructor.
     * @param index The position to set
     * @param value The value to use
     */
    public SetOperation(int index, Object value, boolean allowCascadeDelete)
    {
        this.index = index;
        this.value = value;
        this.allowCascadeDelete = allowCascadeDelete;
    }

    /**
     * Perform the set(int, Object) operation to the backing store.
     * @param store The backing store to perform it on
     * @param sm StateManager for the owner of the map
     */
    public void perform(ListStore store, ObjectProvider sm)
    {
        store.set(sm, index, value, allowCascadeDelete);
    }
}