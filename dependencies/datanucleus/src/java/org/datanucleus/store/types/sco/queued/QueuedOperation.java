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
import org.datanucleus.store.scostore.Store;

/**
 * Interface for an operation that a SCO wrapper should support when queueing
 * its operations (optimistic transactions).
 */
public interface QueuedOperation<TStore extends Store>
{
    /**
     * Method to perform the operation once we are no longer queueing.
     * @param store The backing store to apply the operation to
     * @param sm StateManager for the owner of the container.
     */
    void perform(TStore store, ObjectProvider sm);
}