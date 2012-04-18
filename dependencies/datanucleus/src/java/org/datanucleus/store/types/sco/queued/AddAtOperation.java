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
 * Add operation at a position for a list.
 */
public class AddAtOperation implements QueuedOperation<ListStore>
{
    /** The value to add. */
    private final Object value;

    /** Index to add the object at. */
    private final int index;

    /**
     * Constructor.
     * @param index The index to add it at
     * @param value The value to add
     */
    public AddAtOperation(int index, Object value)
    {
        this.index = index;
        this.value = value;
    }

    /**
     * Perform the add(int, Object) operation on the specified list.
     * @param store The backing store to perform it on
     * @param sm StateManager for the owner of the list
     */
    public void perform(ListStore store, ObjectProvider sm)
    {
        store.add(sm, value, index, -1);
    }
}