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
import org.datanucleus.store.scostore.CollectionStore;

/**
 * Add operation for a collection.
 */
public class AddOperation implements QueuedOperation<CollectionStore>
{
    /** The value to add. */
    private final Object value;

    /**
     * Constructor.
     * @param value The value to add
     */
    public AddOperation(Object value)
    {
        this.value = value;
    }

    /**
     * Accessor for the value being added.
     * @return Value being added
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Perform the add(Object) operation to the backing store.
     * @param store The backing store to perform it on
     * @param sm StateManager for the owner of the collection
     */
    public void perform(CollectionStore store, ObjectProvider sm)
    {
        store.add(sm, value, -1);
    }
}