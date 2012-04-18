/**********************************************************************
Copyright (c) 2002 Kelly Grizzle and others. All rights reserved.
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
2002 Mike Martin - unknown changes
2003 Erik Bengtson - removed exist() operation
2004 Andy Jefferson - added getHighestFieldNumber()
2005 Andy Jefferson - javadocs
2007 Xuan Baldauf - Contrib of notifyMadePersistentClean()
2007 Xuan Baldauf - Contrib of internalXXXFieldXXX() methods
2008 Andy Jefferson - removed all deps on org.datanucleus.store.mapped
2011 Andy Jefferson - removed many methods added during merge with ObjectProvider
    ...
**********************************************************************/
package org.datanucleus.state;

import java.io.PrintWriter;

import javax.jdo.spi.PersistenceCapable;

import org.datanucleus.FetchPlan;
import org.datanucleus.ObjectManager;
import org.datanucleus.Transaction;
import org.datanucleus.cache.CachedPC;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.store.FieldValues;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.StoreManager;

/**
 * StateManager for a persistable object. Based around the JDO PersistenceCapable contract.
 * Makes the assumption that a StateManager corresponds to ONE persistable object.
 */
public interface StateManager extends ObjectProvider
{
    /**
     * Initialises a state manager to manage a hollow instance having the given object ID and the given
     * (optional) field values. This constructor is used for creating new instances of existing persistent
     * objects, and consequently shouldnt be used when the StoreManager controls the creation of such objects
     * (such as in an ODBMS).
     * @param id the identity of the object.
     * @param fv the initial field values of the object (optional)
     * @param pcClass Class of the object that this will manage the state for
     */
    void initialiseForHollow(Object id, FieldValues fv, Class pcClass);

    /**
     * Initialises a state manager to manage a HOLLOW / P_CLEAN instance having the given FieldValues.
     * This constructor is used for creating new instances of existing persistent objects using application 
     * identity, and consequently shouldnt be used when the StoreManager controls the creation of such objects
     * (such as in an ODBMS).
     * @param fv the initial field values of the object.
     * @param pcClass Class of the object that this will manage the state for
     */
    void initialiseForHollowAppId(FieldValues fv, Class pcClass);

    /**
     * Initialises a state manager to manage the given hollow instance having the given object ID.
     * Unlike the {@link #initialiseForHollow} method, this method does not create a new instance and instead 
     * takes a pre-constructed instance.
     * @param id the identity of the object.
     * @param pc the object to be managed.
     */
    void initialiseForHollowPreConstructed(Object id, Object pc);

    /**
     * Initialises a state manager to manage the passed persistent instance having the given object ID.
     * Used where we have retrieved a PC object from a datastore directly (not field-by-field), for example on
     * an object datastore. This initialiser will not add StateManagers to all related PCs. This must be done by
     * any calling process. This simply adds the StateManager to the specified object and records the id, setting
     * all fields of the object as loaded.
     * @param id the identity of the object.
     * @param pc The object to be managed
     */
    void initialiseForPersistentClean(Object id, Object pc);

    /**
     * Initialises a state manager to manage a PersistenceCapable instance that will be EMBEDDED/SERIALISED 
     * into another PersistenceCapable object. The instance will not be assigned an identity in the process 
     * since it is a SCO.
     * @param pc The PersistenceCapable to manage (see copyPc also)
     * @param copyPc Whether the SM should manage a copy of the passed PC or that one
     */
    void initialiseForEmbedded(Object pc, boolean copyPc);

    /**
     * Initialises a state manager to manage a transient instance that is becoming newly persistent.
     * A new object ID for the instance is obtained from the store manager and the object is inserted
     * in the data store.
     * <p>This constructor is used for assigning state managers to existing
     * instances that are transitioning to a persistent state.
     * @param pc the instance being make persistent.
     * @param preInsertChanges Any changes to make before inserting
     */
    void initialiseForPersistentNew(Object pc, FieldValues preInsertChanges);

    /**
     * Initialises a state manager to manage a Transactional Transient instance.
     * A new object ID for the instance is obtained from the store manager and the object is inserted in the data store.
     * <p>
     * This constructor is used for assigning state managers to Transient
     * instances that are transitioning to a transient clean state.
     * @param pc the instance being make persistent.
     */
    void initialiseForTransactionalTransient(Object pc);

    /**
     * Initialises the StateManager to manage a PersistenceCapable object in detached state.
     * @param pc the detach object.
     * @param id the identity of the object.
     * @param version the detached version
     */
    void initialiseForDetached(Object pc, Object id, Object version);

    /**
     * Initialise to create a StateManager for a PersistenceCapable object, assigning the specified id to the object. 
     * This is used when getting objects out of the L2 Cache, where they have no StateManager assigned, and returning 
     * them as associated with a particular PM.
     * @param cachedPC Cache object for persistable object from L2 cache
     * @param id Id to assign to the PersistenceCapable object
     * @param pcClass Class of the object that this will manage the state for
     */
    void initialiseForCachedPC(CachedPC cachedPC, Object id, Class pcClass);

    /**
     * Initialises the StateManager to manage a PersistenceCapable object that is not persistent but that
     * is about to be deleted. The initial state will be P_NEW, but when the delete call comes in will be
     * P_NEW_DELETED. The object will not be enlisted in the transaction.
     * @param pc the object to delete
     */
    void initialiseForPNewToBeDeleted(Object pc);

    /**
     * Accessor for the object managed by this StateManager.
     * @return The object
     */
    Object getObject();

    /**
     * return a copy from the object Id
     * @param obj the persistable object
     * @return the object id
     */
    Object getExternalObjectId(Object obj);

    /**
      * Returns the ObjectManager that owns the StateManager instance
      * @return The ObjectManager
     */
    ObjectManager getObjectManager();

    /**
     * Accessor for the manager for the store.
     * @return Store Manager
     */
    StoreManager getStoreManager();

    /**
     * Accessor for the manager for MetaData.
     * @return MetaData manager
     */
    MetaDataManager getMetaDataManager();

    /**
     * Method to make the managed object transactional.
     */
    void makeTransactional();

    /**
     * Method to make the managed object nontransactional.
     */
    void makeNontransactional();

    /**
     * Method to make the managed object transient.
     * @param state Object containing the state of any fetch plan processing
     */
    void makeTransient(FetchPlanState state);

    /**
     * Method to make the managed object persistent.
     */
    void makePersistent();
    
    /**
     * Method to make Transactional Transient instances persistent
     */
    void makePersistentTransactionalTransient();

    /**
     * Method to delete the object from persistence.
     */
    void deletePersistent();

    /**
     * Method to attach to this the detached persistable instance
     * @param detachedPC the detached persistable instance to be attached
     * @param embedded Whether it is embedded
     * @return The attached copy
     */
    Object attachCopy(Object detachedPC, boolean embedded);

    /**
     * Method to attach the object managed by this StateManager.
     * @param embedded Whether it is embedded
     */
    void attach(boolean embedded);

    /**
     * Method to attach the provided transient into the managed instance.
     * @param trans Transient object
     */
    void attach(Object trans);

    /**
     * Method to make detached copy of this instance
     * @param state State for the detachment process
     * @return the detached PersistenceCapable instance
     */
    Object detachCopy(FetchPlanState state);

    /**
     * Method to detach the PersistenceCapable object.
     * @param state State for the detachment process
     */
    void detach(FetchPlanState state);

    /**
     * Method to return an L2 cacheable object representing the managed object.
     * @return The object suitable for L2 caching
     */
    CachedPC cache();

    /**
     * Validates whether the persistence capable instance exists in the
     * datastore. If the instance does not exist in the datastore, this method
     * will fail raising a NucleusObjectNotFoundException.
     */
    void validate();

    /**
     * Mark the state manager as needing to validate the inheritance of the managed object existence 
     * before loading fields.
     */
    void markForInheritanceValidation();

    /**
     * Method to change the object state to evicted.
     */
    void evict();

    /**
     * Method to refresh the values of the currently loaded fields in the managed object.
     */
    void refresh();

    /**
     * Method to retrieve the fields for this object.
     * @param fgOnly Whether to retrieve just the current fetch plan fields
     */
    void retrieve(boolean fgOnly);

    /**
     * Method to retrieve the object.
     * @param fetchPlan the fetch plan to load fields
     **/
    void retrieve(FetchPlan fetchPlan);

    /**
     * Convenience interceptor to allow operations to be performed before the begin is performed
     * @param tx The transaction
     */
    void preBegin(Transaction tx);
    
    /**
     * Convenience interceptor to allow operations to be performed after the commit is performed
     * but before returning control to the application.
     * @param tx The transaction
     */
    void postCommit(Transaction tx);

    /**
     * Convenience interceptor to allow operations to be performed before any rollback is
     * performed.
     * @param tx The transaction
     */
    void preRollback(Transaction tx);

    /**
     * Convenience method to flush any dirty (updated) fields to the datastore.
     * This is useful where you maybe are deleting the object (so it is marked as provisionally deleted)
     * yet you don't want it deleted yet due to ordering of actions.
     */
    void flushDirtyFields();

    /**
     * Accessor for the highest field number
     * @return Highest field number
     */
    int getHighestFieldNumber();

    /**
     * Nullify fields with reference to PersistenceCapable or SCO instances 
     */
    void nullifyFields();

    /**
     * Fetchs from the database all fields that are not currently loaded and that are in the current
     * fetch group. Called by lifecycle transitions.
     */
    void loadUnloadedFieldsInFetchPlan();

    /**
     * Loads all unloaded fields of the managed class that are in the current FetchPlan.
     * Called by life-cycle transitions.
     * @param fetchPlan The FetchPlan
     * @since 1.1
     */
    void loadUnloadedFieldsOfClassInFetchPlan(FetchPlan fetchPlan);

    /**
     * Fetch from the database all fields that are not currently loaded regardless of whether
     * they are in the current fetch group or not. Called by lifecycle transitions.
     */
    void loadUnloadedFields();

    /**
     * Method that will unload all fields that are not in the FetchPlan.
     */
    void unloadNonFetchPlanFields();

    /**
     * Convenience method to reset the detached state in the current object.
     */
    void resetDetachState();

    /**
     * Disconnect the StateManager from the PersistenceManager and PC object.
     */
    void disconnect();
    
    //called by lifecycle ops
    void evictFromTransaction();

    void enlistInTransaction();
    
    /**
     * Refreshes from the database all fields currently loaded.
     * Called by life-cycle transitions.
     */
    void refreshLoadedFields();

    /**
     * Method to clear all saved fields on the object.
     **/
    void clearSavedFields();
    
    /**
     * Refreshes from the database all fields in fetch plan.
     * Called by life-cycle transitions.
     */
    void refreshFieldsInFetchPlan();
    
    /**
     * Method to clear all fields that are not part of the primary key of the object.
     **/
    void clearNonPrimaryKeyFields();
    
    /**
     * Method to restore all fields of the object.
     **/
    void restoreFields();

    /**
     * Method to save all fields of the object.
     **/
    void saveFields();
    
    /**
     * Method to clear all fields of the object.
     **/
    void clearFields();
    
    /**
     * Registers the pc class in the cache
     */
    void registerTransactional();

    /**
     * Accessor for the Restore Values flag 
     * @return Whether to restore values
     */
    boolean isRestoreValues();
    
    /**
     * Method to clear all loaded flags on the object.
     **/
    void clearLoadedFlags();

    /**
     * Method to register an owner StateManager with this embedded/serialised object.
     * @param ownerSM The owning State Manager.
     * @param ownerFieldNumber The field number in the owner that the embedded/serialised object is stored as
     */
    void addEmbeddedOwner(ObjectProvider ownerSM, int ownerFieldNumber);

    /**
     * Look to the database to determine which
     * class this object is. This parameter is a hint. Set false, if it's
     * already determined the correct pcClass for this pc "object" in a certain
     * level in the hierarchy. Set to true and it will look to the database.
     * @param fv the initial field values of the object.
     */
    void checkInheritance(FieldValues fv);

    /**
     * Convenience method to retrieve the detach state from the passed State Manager's object
     * @param sm The State Manager
     */
    void retrieveDetachState(org.datanucleus.state.StateManager sm);

    /**
     * Tests whether this object is in the process of being detached.
     * @return true if this instance is being detached.
     */
    boolean isDetaching();

    /**
     * Convenience method to return if we are in the phase of performing postInsert updates
     * due to related objects having been inserted and so allowing the field to be inserted.
     * @return Whether we are updating for postInsert
     */
    boolean isUpdatingFieldForPostInsert();

    /**
     * Diagnostic method to dump the current state to the provided PrintWriter.
     * @param out The PrintWriter
     */
    void dump(PrintWriter out);

    // ---------------------------------- JDO-specific ------------------------------------
    // TODO Move these to JDOStateManagerImpl or similar

    /**
     * Accessor for the object id.
     * @param pc The PC object
     * @return The (external) id
     */
    Object getObjectId(PersistenceCapable pc);

    Object getVersion(PersistenceCapable pc);

    boolean isLoaded(PersistenceCapable pc, int fieldNumber);

    void setObjectField(PersistenceCapable pc, int fieldNumber, Object oldValue, Object newValue);
}