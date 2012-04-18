/**********************************************************************
Copyright (c) 2007 Erik Bengtson and others. All rights reserved. 
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
package org.datanucleus.store.connection;

import javax.transaction.xa.XAResource;

/**
 * Wrapper for a connection to the datastore, allowing management.
 */
public interface ManagedConnection
{
    /**
     * Connection to the datastore
     * @return The underlying connection for this datastore
     */
    Object getConnection();

    /**
     * An XAResoure for this datastore connection.
     * Returns null if the connection is not transactional
     * @return The XAResource
     */
    XAResource getXAResource();

    /**
     * Method to release the connection when non-transactional.
     * If this is a managed connection resource does nothing.
     */
    void release();

    /**
     * Flush the connection. It must invoke the operation
     * {@link ManagedConnectionResourceListener#transactionFlushed()}
     */
    void transactionFlushed();

    /**
     * Prepare the connection for end of transaction. It must invoke the operation
     * {@link ManagedConnectionResourceListener#transactionPreClose()}
     */
    void transactionPreClose();

    /**
     * Close the connection to the datastore. It most invoke the operations
     * {@link ManagedConnectionResourceListener#managedConnectionPreClose()} and
     * {@link ManagedConnectionResourceListener#managedConnectionPostClose()}.
     * The listeners are unregistered after this method is invoked.
     */
    void close();

    /**
     * Whether this connection is managed by a transaction manager
     */
    void setManagedResource();
    
    /**
     * whether access to this ManagedConnection has been locked.
     * @return true if locked
     */
    boolean isLocked();
    
    /**
     * lock the access to this ManagedConnection
     */
    void lock();
    
    /**
     * unlock the access to this ManagedConnection
     */
    void unlock();

    /**
     * Registers a ManagedConnectionResourceListener
     * @param listener The listener
     */
    void addListener(ManagedConnectionResourceListener listener);

    /**
     * Deregister a ManagedConnectionResourceListener
     * @param listener The listener
     */
    void removeListener(ManagedConnectionResourceListener listener);
}