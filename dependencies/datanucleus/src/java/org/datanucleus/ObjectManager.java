/**********************************************************************
Copyright (c) 2002 Kelly Grizzle (TJDO) and others. All rights reserved.
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
2003 Erik Bengtson - added getObjectByAID
2004 Andy Jefferson - added MetaDataManager
2005 Andy Jefferson - javadocs
   ...
**********************************************************************/
package org.datanucleus;

import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.datanucleus.exceptions.ClassNotPersistableException;
import org.datanucleus.exceptions.NoPersistenceInformationException;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.state.CallbackHandler;
import org.datanucleus.state.StateManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.ObjectProvider;

/**
 * Definition of an ObjectManager.
 * Provides the basics of object persistence internally, upon which are built
 * javax.jdo.PersistenceManager and javax.persistence.EntityManager.
 */
public interface ObjectManager extends ExecutionContext
{
    /**
     * Method to return the owner object.
     * For JDO this will return the PersistenceManager owning this ObjectManager.
     * For JPA this will return the EntityManager owning this ObjectManager.
     * @return The owner manager object
     */
    Object getOwner();

    /**
     * Retrieve the callback handler for this PM
     * @return the callback handler
     */
    CallbackHandler getCallbackHandler();

    /**
     * Convenience method to assert if the passed class is not persistable.
     * @param cls The class of which we want to persist objects
     * @throws ClassNotPersistableException When the class is not persistable
     * @throws NoPersistenceInformationException When the class has no available persistence information
     */
    void assertClassPersistable(Class cls);

    /**
     * Method to evict the passed object.
     * @param pc The object
     */
    void evictObject(Object pc);

    /**
     * Method to evict all objects of the specified type (and optionaly its subclasses).
     * @param cls Type of persistable object
     * @param subclasses Whether to include subclasses
     */
    void evictObjects(Class cls, boolean subclasses);

    /**
     * Method to evict all L1 cache objects
     */
    void evictAllObjects();

    /**
     * Method to refresh all L1 cache objects
     */
    void refreshAllObjects();

    /**
     * Method to retrieve the passed object.
     * @param pc The object
     * @param fgOnly Just retrieve the current fetch group
     */
    void retrieveObject(Object pc, boolean fgOnly);

    /**
     * Method to persist the passed object.
     * @param pc The object
     * @param merging Whether this object (and dependents) is being merged
     * @return The persisted object
     */
    Object persistObject(Object pc, boolean merging);

    /**
     * Method to persist the passed object(s).
     * @param pcs The objects to persist
     * @return The persisted objects
     */
    Object[] persistObjects(Object[] pcs);

    /**
     * Method to persist the passed object (internally).
     * @param pc The object
     * @param preInsertChanges Changes to be made before inserting
     * @param ownerSM ObjectProvider of the owner when embedded
     * @param ownerFieldNum Field number in the owner where this is embedded (or -1 if not embedded)
     * @param objectType Type of object (see org.datanucleus.store.ObjectProvider, e.g ObjectProvider.PC)
     * @return The persisted object
     */
    Object persistObjectInternal(Object pc, FieldValues preInsertChanges, ObjectProvider ownerSM, int ownerFieldNum, 
            int objectType);

    /**
     * Method to delete an array of objects from the datastore.
     * @param objs The objects to delete
     */
    void deleteObjects(Object[] objs);

    /**
     * Method to delete an object from the datastore.
     * @param obj The object
     */
    void deleteObject(Object obj);

    /**
     * Method to make the passed object transactional.
     * @param pc The object
     */
    void makeObjectTransactional(Object pc);

    /**
     * Method to make the passed object nontransactional.
     * @param pc The object
     */
    void makeObjectNontransactional(Object pc);

    /**
     * Method to detach all objects in the PM.
     */
    void detachAll();

    /**
     * Method called at the completion of a nontransactional write.
     * If "datanucleus.nontx.atomic" is false then returns immediately.
     * Otherwise will flush any updates that are outstanding (updates to an object), will perform detachAllOnCommit
     * if enabled (so user always has detached objects), update objects in any L2 cache, and migrates any 
     * objects through lifecycle changes.
     * Is similar in content to "flush"+"preCommit"+"postCommit"
     */
    void processNontransactionalUpdate();

    /**
     * Method to return if the specified object exists in the datastore.
     * @param obj The (persistable) object
     * @return Whether it exists
     */
    boolean exists(Object obj);

    /**
     * Accessor for the currently managed objects for the current transaction.
     * If the transaction is not active this returns null.
     * @return Collection of managed objects enlisted in the current transaction
     */
    Set getManagedObjects();

    /**
     * Accessor for the currently managed objects for the current transaction.
     * If the transaction is not active this returns null.
     * @param classes Classes that we want the objects for
     * @return Collection of managed objects enlisted in the current transaction
     */
    Set getManagedObjects(Class[] classes);

    /**
     * Accessor for the currently managed objects for the current transaction.
     * If the transaction is not active this returns null.
     * @param states States that we want the objects for
     * @return Collection of managed objects enlisted in the current transaction
     */
    Set getManagedObjects(String[] states);

    /**
     * Accessor for the currently managed objects for the current transaction.
     * If the transaction is not active this returns null.
     * @param states States that we want the objects for
     * @param classes Classes that we want the objects for
     * @return Collection of managed objects enlisted in the current transaction
     */
    Set getManagedObjects(String[] states, Class[] classes);

    /**
     * Accessor for objects with the specified identities.
     * @param ids Ids of the object(s).
     * @param validate Whether to validate the object state
     * @return The Objects with these ids (same order)
     * @throws NucleusObjectNotFoundException if an object doesn't exist in the datastore
     */
    Object[] findObjects(Object[] ids, boolean validate);

    /**
     * Method to generate an instance of an interface, abstract class, or concrete PC class.
     * @param persistenceCapable The class of the interface or abstract class, or concrete class defined in MetaData
     * @return The instance of this type
     */
    Object newInstance(Class persistenceCapable);

    /**
     * Method to return if an object is enlisted in the current transaction.
     * @param id Identity for the object
     * @return Whether it is enlisted in the current transaction
     */
    boolean isEnlistedInTransaction(Object id);

    /**
     * Method to find the StateManager for the passed persistable object when it is managed by this manager.
     * @param pc The persistable object
     * @return The StateManager
     */
    StateManager findStateManager(Object pc);

    /**
     * Method to register the ObjectProvider as being for the passed object.
     * Used during the process of identifying ObjectProvider for persistable object.
     * @param sm The ObjectProvider
     * @param pc The object managed by the ObjectProvider
     */
    void hereIsObjectProvider(ObjectProvider sm, Object pc);

    /**
     * Method to add the object managed by the specified StateManager to the cache.
     * @param sm The StateManager
     */    
    void addStateManager(StateManager sm);

    /**
     * Method to remove the object managed by the specified StateManager from the cache.
     * @param sm The StateManager
     */
    void removeStateManager(StateManager sm);

    /**
     * Accessor for the StateManager of an object given the object id.
     * @param myID Id of the object.
     * @return The StateManager
     */
    StateManager getStateManagerById(Object myID);

    /**
     * Mark the specified ObjectProvider as dirty
     * @param sm ObjectProvider
     * @param directUpdate Whether the object has had a direct update made on it (if known)
     */
    void markDirty(ObjectProvider sm, boolean directUpdate);

    /**
     * Mark the specified StateManager as clean.
     * @param sm The StateManager
     */
    void clearDirty(StateManager sm);

    /**
     * Method to mark as clean all StateManagers of dirty objects.
     */
    void clearDirty();

    /**
     * Accessor for whether the object with this identity is modified in the current transaction.
     * @param id The identity.
     * @return Whether it is modified/new/deleted in this transaction
     */
    boolean isObjectModifiedInTransaction(Object id);

    boolean getManageRelationsChecks();

    /**
     * Returns whether this ObjectManager is currently performing the manage relationships task.
     * @return Whether in the process of managing relations
     */
    boolean isManagingRelations();

    /**
     * Method called during the rollback process, after the actual datastore rollback.
     */
    public void preRollback();

    /**
     * Method called during the rollback process, after the actual datastore rollback.
     */
    public void postRollback();

    /**
     * Method called during the begin process, after the actual begin.
     */
    public void postBegin();
    
    /**
     * Method called during the commit process, before the actual datastore commit.
     */
    public void preCommit();

    /**
     * Method called during the commit process, after the actual datastore commit.
     */
    public void postCommit();

    /**
     * Method callable from external APIs for user-management of flushing.
     * Called by JDO PM.flush, or JPA EM.flush().
     * Performs management of relations, prior to performing internal flush of all dirty/new/deleted
     * instances to the datastore.
     */
    void flush();

    /**
     * Convenience method to inspect the list of objects with outstanding changes to flush.
     * @return StateManagers for the objects to be flushed.
     */
    List<StateManager> getObjectsToBeFlushed();

    /**
     * Accessor for whether this ObjectManager is currently running detachAllOnCommit.
     * @return Whether running detachAllOnCommit
     */
    boolean isRunningDetachAllOnCommit();

    /**
     * Replace the previous object id for a PC object to a new
     * @param pc The Persistable object
     * @param oldID the old id
     * @param newID the new id
     */
    void replaceObjectId(Object pc, Object oldID, Object newID);

    /**
     * Disconnect SM instances, clear cache and reset settings 
     */
    public void disconnectSMCache();

    /**
     * Method to register a listener for instances of the specified classes.
     * @param listener The listener to sends events to
     * @param classes The classes that it is interested in
     */
    void addListener(Object listener, Class[] classes);

    /**
     * Method to remove a currently registered listener.
     * @param listener The instance lifecycle listener to remove.
     */
    void removeListener(Object listener);

    /**
     * Disconnect the registered LifecycleListener
     */
    public void disconnectLifecycleListener();

    /**
     * Accessor for an internal fetch group for the specified class.
     * @param cls The class
     * @param name Name of the group
     * @return The FetchGroup
     */
    FetchGroup getInternalFetchGroup(Class cls, String name);

    /**
     * Method to add an internal fetch group to this ObjectManager.
     * @param grp The internal fetch group
     */
    void addInternalFetchGroup(FetchGroup grp);

    /**
     * Accessor for the fetch groups for the specified name.
     * @param name Name of the group
     * @return The FetchGroup
     */
    Set getFetchGroupsWithName(String name);

    /**
     * Convenience method to return the identity as a String.
     * @param id The id
     * @return String form
     */
    String getIdentityAsString(Object id);

    /**
     * Accessor for the lock object for this ObjectManager.
     * @return The lock object
     */
    public Lock getLock();
}