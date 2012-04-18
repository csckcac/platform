/**********************************************************************
Copyright (c) 2011 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus;

import org.datanucleus.state.FetchPlanState;
import org.datanucleus.state.StateManager;
import org.datanucleus.store.Extent;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.query.Query;

/**
 * ObjectManager for handling the multithreaded PM/EM cases.
 * Locks various methods in an attempt to prevent conflicting thread updates.
 * Note we could have just put this code in ObjectManagerImpl.
 * TODO Evaluate all of the places we currently lock (when multithreaded) to find corner cases not caught.
 */
public class MultithreadedObjectManager extends ObjectManagerImpl
{
    /**
     * @param ctx NucleusContext
     * @param owner Owner object (PM, EM)
     * @param userName Username for the datastore
     * @param password Password for the datastore
     */
    public MultithreadedObjectManager(NucleusContext ctx, Object owner, String userName, String password)
    {
        super(ctx, owner, userName, password);
    }

    /**
     * Accessor for whether the object manager is multithreaded.
     * @return Whether to run multithreaded.
     */
    public boolean getMultithreaded()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#processNontransactionalUpdate()
     */
    @Override
    public void processNontransactionalUpdate()
    {
        try
        {
            lock.lock();

            super.processNontransactionalUpdate();
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#enlistInTransaction(org.datanucleus.store.ObjectProvider)
     */
    @Override
    public void enlistInTransaction(ObjectProvider sm)
    {
        try
        {
            lock.lock();

            super.enlistInTransaction(sm);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#evictFromTransaction(org.datanucleus.store.ObjectProvider)
     */
    @Override
    public void evictFromTransaction(ObjectProvider sm)
    {
        try
        {
            lock.lock();

            super.evictFromTransaction(sm);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#addStateManager(org.datanucleus.state.StateManager)
     */
    @Override
    public void addStateManager(StateManager sm)
    {
        try
        {
            lock.lock();

            super.addStateManager(sm);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#removeStateManager(org.datanucleus.state.StateManager)
     */
    @Override
    public void removeStateManager(StateManager sm)
    {
        try
        {
            lock.lock();

            super.removeStateManager(sm);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#getStateManagerById(java.lang.Object)
     */
    @Override
    public StateManager getStateManagerById(Object id)
    {
        try
        {
            lock.lock();

            return super.getStateManagerById(id);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#findStateManager(java.lang.Object)
     */
    @Override
    public StateManager findStateManager(Object pc)
    {
        try
        {
            lock.lock();

            return super.findStateManager(pc);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#findObjectProvider(java.lang.Object)
     */
    @Override
    public ObjectProvider findObjectProvider(Object pc)
    {
        try
        {
            lock.lock();

            return super.findObjectProvider(pc);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#hereIsObjectProvider(org.datanucleus.store.ObjectProvider, java.lang.Object)
     */
    @Override
    public void hereIsObjectProvider(ObjectProvider sm, Object pc)
    {
        try
        {
            lock.lock();

            super.hereIsObjectProvider(sm, pc);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#close()
     */
    @Override
    public void close()
    {
        try
        {
            lock.lock();

            super.close();
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#evictObject(java.lang.Object)
     */
    @Override
    public void evictObject(Object obj)
    {
        try
        {
            lock.lock();

            super.evictObject(obj);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#refreshObject(java.lang.Object)
     */
    @Override
    public void refreshObject(Object obj)
    {
        try
        {
            lock.lock();

            super.refreshObject(obj);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#retrieveObject(java.lang.Object, boolean)
     */
    @Override
    public void retrieveObject(Object obj, boolean fgOnly)
    {
        try
        {
            lock.lock();

            super.retrieveObject(obj, fgOnly);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#persistObject(java.lang.Object, boolean)
     */
    @Override
    public Object persistObject(Object obj, boolean merging)
    {
        try
        {
            lock.lock();

            return super.persistObject(obj, merging);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#persistObjects(java.lang.Object[])
     */
    @Override
    public Object[] persistObjects(Object[] objs)
    {
        try
        {
            lock.lock();

            return super.persistObjects(objs);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#deleteObject(java.lang.Object)
     */
    @Override
    public void deleteObject(Object obj)
    {
        try
        {
            lock.lock();

            super.deleteObject(obj);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#deleteObjects(java.lang.Object[])
     */
    @Override
    public void deleteObjects(Object[] objs)
    {
        try
        {
            lock.lock();

            super.deleteObjects(objs);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#makeObjectTransient(java.lang.Object, org.datanucleus.state.FetchPlanState)
     */
    @Override
    public void makeObjectTransient(Object obj, FetchPlanState state)
    {
        try
        {
            lock.lock();

            super.makeObjectTransient(obj, state);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#makeObjectTransactional(java.lang.Object)
     */
    @Override
    public void makeObjectTransactional(Object obj)
    {
        try
        {
            lock.lock();

            super.makeObjectTransactional(obj);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#attachObject(org.datanucleus.store.ObjectProvider, java.lang.Object, boolean)
     */
    @Override
    public void attachObject(ObjectProvider ownerOP, Object pc, boolean sco)
    {
        try
        {
            lock.lock();

            super.attachObject(ownerOP, pc, sco);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#attachObjectCopy(org.datanucleus.store.ObjectProvider, java.lang.Object, boolean)
     */
    @Override
    public Object attachObjectCopy(ObjectProvider ownerOP, Object pc, boolean sco)
    {
        try
        {
            lock.lock();

            return super.attachObjectCopy(ownerOP, pc, sco);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#detachObject(java.lang.Object, org.datanucleus.state.FetchPlanState)
     */
    @Override
    public void detachObject(Object obj, FetchPlanState state)
    {
        try
        {
            lock.lock();

            super.detachObject(obj, state);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#detachObjectCopy(java.lang.Object, org.datanucleus.state.FetchPlanState)
     */
    @Override
    public Object detachObjectCopy(Object pc, FetchPlanState state)
    {
        try
        {
            lock.lock();

            return super.detachObjectCopy(pc, state);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#clearDirty(org.datanucleus.state.StateManager)
     */
    @Override
    public void clearDirty(StateManager sm)
    {
        try
        {
            lock.lock();

            super.clearDirty(sm);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#clearDirty()
     */
    @Override
    public void clearDirty()
    {
        try
        {
            lock.lock();

            super.clearDirty();
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#markDirty(org.datanucleus.store.ObjectProvider, boolean)
     */
    @Override
    public void markDirty(ObjectProvider op, boolean directUpdate)
    {
        try
        {
            lock.lock();

            super.markDirty(op, directUpdate);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#flush()
     */
    @Override
    public void flush()
    {
        try
        {
            lock.lock();

            super.flush();
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#flushInternal(boolean)
     */
    @Override
    public void flushInternal(boolean flushToDatastore)
    {
        try
        {
            lock.lock();

            super.flushInternal(flushToDatastore);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#replaceObjectId(java.lang.Object, java.lang.Object, java.lang.Object)
     */
    @Override
    public void replaceObjectId(Object pc, Object oldID, Object newID)
    {
        try
        {
            lock.lock();

            super.replaceObjectId(pc, oldID, newID);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#getExtent(java.lang.Class, boolean)
     */
    @Override
    public Extent getExtent(Class pcClass, boolean subclasses)
    {
        try
        {
            lock.lock();

            return super.getExtent(pcClass, subclasses);
        }
        finally
        {
            lock.unlock();
        }
    }

    /* (non-Javadoc)
     * @see org.datanucleus.ObjectManagerImpl#newQuery()
     */
    @Override
    public Query newQuery()
    {
        try
        {
            lock.lock();

            return super.newQuery();
        }
        finally
        {
            lock.unlock();
        }
    }
}