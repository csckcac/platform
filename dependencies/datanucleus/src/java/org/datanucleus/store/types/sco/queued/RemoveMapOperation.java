/**********************************************************************
Copyright (c) 2010 Peter Dettman and others. All rights reserved.
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
 * Remove operation for a collection or map.
 */
public class RemoveMapOperation implements QueuedOperation<MapStore>
{
    /** The value to remove. */
    private final Object value;

    /**
     * Constructor.
     * @param value The value to remove
     */
    public RemoveMapOperation(Object value)
    {
        this.value = value;
    }

    /**
     * Accessor for the value being removed.
     * @return Value being removed
     */
    public Object getValue()
    {
        return value;
    }

    /**
     * Perform the remove(Object) operation on the specified container.
     * @param store The backing store to perform it on
     * @param sm StateManager for the owner of the container
     */
    public void perform(MapStore store, ObjectProvider sm)
    {
        store.remove(sm, value);
    }
}