/**********************************************************************
Copyright (c) 2009 Erik Bengtson and others. All rights reserved.
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
package org.datanucleus.store;

import java.util.Map;
import java.util.Set;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.FetchPlan;
import org.datanucleus.NucleusContext;
import org.datanucleus.Transaction;
import org.datanucleus.api.ApiAdapter;
import org.datanucleus.exceptions.NucleusOptimisticException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.state.FetchPlanState;
import org.datanucleus.state.RelationshipManager;
import org.datanucleus.state.lock.LockManager;
import org.datanucleus.store.query.Query;
import org.datanucleus.store.types.TypeManager;

/**
 * Context of execution for persistence operations.
 * In the traditional Java persistence role this will be an ObjectManager.
 */
public interface ExecutionContext
{
    static final String PROP_COPY_ON_ATTACH = "datanucleus.CopyOnAttach";
    static final String PROP_DETACH_ON_CLOSE = "datanucleus.DetachOnClose";
    static final String PROP_DETACH_ON_COMMIT = "datanucleus.DetachAllOnCommit";
    static final String PROP_DETACH_ON_ROLLBACK = "datanucleus.DetachAllOnRollback";
    static final String PROP_PBR_AT_COMMIT = "datanucleus.persistenceByReachabilityAtCommit";
    static final String PROP_MANAGE_RELATIONS = "datanucleus.manageRelationships";
    static final String PROP_MANAGE_RELATIONS_CHECKS = "datanucleus.manageRelationshipsChecks";
    static final String PROP_MULTITHREADED = "datanucleus.Multithreaded";
    static final String PROP_IGNORE_CACHE = "datanucleus.IgnoreCache";
    static final String PROP_SERIALISE_READ = "datanucleus.SerializeRead";

    static final String PROP_READ_TIMEOUT = "datanucleus.datastoreReadTimeout";
    static final String PROP_WRITE_TIMEOUT = "datanucleus.datastoreWriteTimeout";

    /**
     * Accessor for the current transaction for this execution context.
     * @return The current transaction
     */
    Transaction getTransaction();

    /**
     * Accessor for the Store Manager.
     * @return Store Manager
     */
    StoreManager getStoreManager();

    /**
     * Accessor for the MetaData Manager.
     * @return The MetaData Manager
     */
    MetaDataManager getMetaDataManager();

    /**
     * Accessor for the context in which this execution context is running.
     * @return Returns the context.
     */
    NucleusContext getNucleusContext();

    /**
     * Accessor for the API adapter.
     * @return API adapter.
     */
    ApiAdapter getApiAdapter();

    /**
     * Acessor for the current FetchPlan
     * @return FetchPlan
     */
    FetchPlan getFetchPlan();

    /**
     * Accessor for the ClassLoader resolver to use in class loading issues.
     * @return The ClassLoader resolver
     */
    ClassLoaderResolver getClassLoaderResolver();

    /**
     * Accessor for the lock manager for this execution context.
     * @return The lock manager
     */
    LockManager getLockManager();

    /**
     * Method to set a property on the execution context
     * @param name Name of the property
     * @param value Value to set
     */
    void setProperty(String name, Object value);

    /**
     * Accessor for a property.
     * @param name Name of the property
     * @return The value
     */
    Object getProperty(String name);

    /**
     * Accessor for a boolean property value.
     * @param name Name of the property
     * @return the value
     */
    Boolean getBooleanProperty(String name);

    /**
     * Accessor for an int property value.
     * @param name Name of the property
     * @return the value
     */
    Integer getIntProperty(String name);

    /**
     * Accessor for the defined properties.
     * @return Properties for this execution context
     */
    Map<String, Object> getProperties();

    /**
     * Accessor for the supported property names.
     * @return Set of names
     */
    Set<String> getSupportedProperties();

    /**
     * TODO should we keep this here? this is api/language dependent
     * @return The type manager
     */
    TypeManager getTypeManager();

    /**
     * Method to close the execution context.
     */
    void close();

    /**
     * Accessor for whether this execution context is closed.
     * @return Whether this manager is closed.
     */
    boolean isClosed();

    /**
     * Accessor for whether to ignore the cache.
     * @return Whether to ignore the cache.
     */
    boolean getIgnoreCache();

    /**
     * Accessor for the datastore read timeout in milliseconds.
     * @return Datastore read timeout in milliseconds (if specified)
     */
    Integer getDatastoreReadTimeoutMillis();

    /**
     * Accessor for the datastore write timeout in milliseconds.
     * @return Datastore write timeout in milliseconds (if specified)
     */
    Integer getDatastoreWriteTimeoutMillis();

    /**
     * Method to find the ObjectProvider for the passed persistable object when it is managed by this manager.
     * @param pc The persistable object
     * @return The ObjectProvider
     */
    ObjectProvider findObjectProvider(Object pc);

    /**
     * Method to find the ObjectProvider for the passed persistable object when it is managed by this manager, 
     * and if not yet persistent to persist it.
     * @param pc The persistable object
     * @param persist Whether to persist if not yet persistent
     * @return The ObjectProvider
     */
    ObjectProvider findObjectProvider(Object pc, boolean persist);

    /**
     * Method to find the ObjectProvider for the passed embedded persistable object.
     * Will create one if not already registered, and tie it to the specified owner.
     * @param value The embedded object
     * @param owner The owner ObjectProvider (if known).
     * @param mmd Metadata for the field of the owner
     * @return The ObjectProvider for the embedded object
     */
    ObjectProvider findObjectProviderForEmbedded(Object value, ObjectProvider owner, AbstractMemberMetaData mmd);

    /**
     * Method to create an ObjectProvider for an embedded object at the specified field of the owner object.
     * @param ownerMmd Metadata for the field/property
     * @param cmd Metadata for the embedded object
     * @param ownerOP ObjectProvider for the owner
     * @param ownerFieldNumber Field number in the owner where the embedded object is
     * @return The ObjectProvider for the embedded (persistable) object
     */
    ObjectProvider newObjectProviderForEmbedded(AbstractMemberMetaData ownerMmd, AbstractClassMetaData cmd, 
            ObjectProvider ownerOP, int ownerFieldNumber);

    /**
     * Method to persist the passed object (internally).
     * @param pc The object
     * @param ownerSM StateManager of the owner when embedded
     * @param ownerFieldNum Field number in the owner where this is embedded (or -1 if not embedded)
     * @param objectType Type of object (see org.datanucleus.StateManager, e.g StateManager.PC)
     * @return The persisted object
     */
    Object persistObjectInternal(Object pc, ObjectProvider ownerSM, int ownerFieldNum, int objectType);

    /**
     * Method to persist the passed object (internally).
     * @param pc The object
     * @param preInsertChanges Changes to be made before inserting
     * @param objectType Type of object (see org.datanucleus.StateManager, e.g StateManager.PC)
     * @return The persisted object
     */
    Object persistObjectInternal(Object pc, FieldValues preInsertChanges, int objectType);

    /**
     * Method to delete an array of objects from the datastore.
     * @param objs The objects to delete
     */
    void deleteObjects(Object[] objs);

    /**
     * Method to delete the passed object (internally).
     * @param pc The object
     */
    void deleteObjectInternal(Object pc);

    /**
     * Method to make transient the passed object.
     * @param pc The object
     * @param state Object containing the state of the fetchplan processing
     */
    void makeObjectTransient(Object pc, FetchPlanState state);

    /**
     * Method to detach the passed object.
     * @param pc The object to detach
     * @param state State for the detachment process.
     */
    void detachObject(Object pc, FetchPlanState state);

    /**
     * Method to detach a copy of the passed object using the provided state.
     * @param pc The object
     * @param state State for the detachment process
     * @return The detached copy of the object
     */
    Object detachObjectCopy(Object pc, FetchPlanState state);

    /**
     * Method to attach the passed object (and related objects).
     * Throws an exception if another (persistent) object with the same id exists in the L1 cache already.
     * @param ownerOP ObjectProvider of the owning object that has this in a field causing its attach
     * @param pc The (detached) object
     * @param sco Whether the object has no identity (embedded or serialised)
     */
    void attachObject(ObjectProvider ownerOP, Object pc, boolean sco);

    /**
     * Method to attach a copy of the passed object (and related objects).
     * @param ownerOP ObjectProvider of the owning object that has this in a field causing its attach
     * @param pc The object
     * @param sco Whether it has no identity (second-class object)
     * @return The attached copy of the input object
     */
    Object attachObjectCopy(ObjectProvider ownerOP, Object pc, boolean sco);

    /**
     * Convenience method to return the attached object for the specified id if one exists.
     * @param id The id
     * @return The attached object
     */
    Object getAttachedObjectForId(Object id);

    /**
     * Accessor for the owner of an object that is being attached. This is the object that contains
     * this object and that has caused it to be attached.
     * @param pc The object being attached
     * @return ObjectProvider of the owning object
     */
    ObjectProvider getObjectProviderOfOwnerForAttachingObject(Object pc);

    /**
     * Method to refresh the passed object.
     * @param pc The object
     */
    void refreshObject(Object pc);

    /**
     * Method to enlist the specified ObjectProvider in the current transaction.
     * @param sm The ObjectProvider
     */
    void enlistInTransaction(ObjectProvider sm);

    /**
     * Method to evict the specified ObjectProvider from the current transaction.
     * @param sm The ObjectProvider
     */
    void evictFromTransaction(ObjectProvider sm);

    /**
     * Whether the datastore operations are delayed until commit.
     * In optimistic transactions this is automatically enabled.
     * @return true if datastore operations are delayed until commit
     */
    boolean isDelayDatastoreOperationsEnabled();

    /**
     * Mark the specified ObjectProvider as dirty
     * @param sm ObjectProvider
     * @param directUpdate Whether the object has had a direct update made on it (if known)
     */
    void markDirty(ObjectProvider sm, boolean directUpdate);

    /**
     * Accessor for the Extent for a class (and optionally its subclasses).
     * @param candidateClass The class
     * @param includeSubclasses Whether to include subclasses
     * @return The Extent
     */
    Extent getExtent(Class candidateClass, boolean includeSubclasses);

    /**
     * Accessor for a new Query.
     * @return The new Query
     */
    Query newQuery();

    /**
     * Method to put a Persistable object associated to the ObjectProvider into the L1 cache.
     * @param op The ObjectProvider
     */
    void putObjectIntoCache(ObjectProvider op);

    /**
     * Convenience method to access an object in the cache.
     * Firstly looks in the L1 cache for this PM, and if not found looks in the L2 cache.
     * @param id Id of the object
     * @return Persistable object (with connected StateManager).
     */
    Object getObjectFromCache(Object id);

    /**
     * Method to remove an object from the L1 cache.
     * @param id The id of the object
     */
    void removeObjectFromCache(Object id);

    /**
     * Method to remove an object from the L2 cache.
     * @param id The id of the object
     */
    void removeObjectFromLevel2Cache(Object id);

    /**
     * Whether an object with the specified identity exists in the cache(s).
     * Used as a check on identity (inheritance-level) validity
     * @param id The identity
     * @return Whether it exists
     */
    boolean hasIdentityInCache(Object id);

    /**
     * Accessor for an object given the object id.
     * @param id Id of the object.
     * @param validate Whether to validate the object state
     * @param checkInheritance Whether look to the database to determine which
     * class this object is. This parameter is a hint. Set false, if it's
     * already determined the correct pcClass for this pc "object" in a certain
     * level in the hierarchy. Set to true and it will look to the database.
     * @param objectClassName Class name for the object with this id (if known, optional)
     * @return The Object
     */
    Object findObject(Object id, boolean validate, boolean checkInheritance, String objectClassName);

    /**
     * Accessor for an object given the object id.
     * @param id Id of the object.
     * @param fv FieldValues to apply to the object (optional)
     * @param pcClass the type which the object is. This type will be used to instanciat the object
     * @param ignoreCache true if the cache is ignored
     * @return the Object
     */
    Object findObject(Object id, FieldValues fv, Class pcClass, boolean ignoreCache);

    /**
     * @deprecated Please make use of IdentityUtils.getApplicationIdentityForResultSetRow()
     * and then call findObject(Object oid, FieldValues2 fieldValues2, Class pcClass, boolean ignoreCache)
     */
    Object findObjectUsingAID(Type pcClass, FieldValues fv, boolean ignoreCache, boolean checkInheritance);

    /**
     * This method returns an object id instance corresponding to the pcClass and key arguments.
     * Operates in 2 modes :-
     * <ul>
     * <li>The class uses SingleFieldIdentity and the key is the value of the key field</li>
     * <li>In all other cases the key is the String form of the object id instance</li>
     * </ul>
     * @param pcClass Class of the PersistenceCapable to create the identity for
     * @param key Value of the key for SingleFieldIdentity (or the toString value)
     * @return The new object-id instance
     */
    Object newObjectId(Class pcClass, Object key);

    /**
     * This method returns an object id instance corresponding to the class name, and the passed
     * object (when using app identity).
     * @param className Name of the class of the object.
     * @param pc The persistable object. Used for application-identity
     * @return A new object ID.
     */
    Object newObjectId(String className, Object pc);

    /**
     * Convenience method to return the setting for serialize read for the current transaction for
     * the specified class name. Returns the setting for the transaction (if set), otherwise falls back to
     * the setting for the class, otherwise returns false.
     * @param className Name of the class
     * @return Setting for serialize read
     */
    boolean getSerializeReadForClass(String className);

    /**
     * Utility method to check if the specified class has reachable metadata or annotations.
     * @param cls The class to check
     * @return Whether the class has reachable metadata or annotations
     */
    boolean hasPersistenceInformationForClass(Class cls);

    /**
     * Tests whether this persistable object is being inserted.
     * @param pc the object to verify the status
     * @return true if this instance is inserting.
     */
    boolean isInserting(Object pc);

    /**
     * Accessor for whether the ObjectManager is flushing changes to the datastore.
     * @return Whether it is currently flushing
     */
    boolean isFlushing();

    /**
     * Method to flushes all dirty, new, and deleted instances to the datastore.
     * It has no effect if a transaction is not active. 
     * If a datastore transaction is active, this method synchronizes the cache with
     * the datastore and reports any exceptions. 
     * If an optimistic transaction is active, this method obtains a datastore connection
     * and synchronizes the cache with the datastore using this connection.
     * The connection obtained by this method is held until the end of the transaction.
     * @param flushToDatastore Whether to ensure any changes reach the datastore
     *     Otherwise they will be flushed to the datastore manager and leave it to
     *     decide the opportune moment to actually flush them to teh datastore
     * @throws NucleusOptimisticException when optimistic locking error(s) occur
     */
    void flushInternal(boolean flushToDatastore);

    /**
     * Accessor for whether this context is multithreaded.
     * @return Whether multithreaded (and hence needing locking)
     */
    boolean getMultithreaded();

    /**
     * Whether managed relations are supported by this execution context.
     * @return Supporting managed relations
     */
    boolean getManageRelations();

    /**
     * Accessor for the RelationshipManager for the provided ObjectProvider.
     * @param op ObjectProvider
     * @return The RelationshipManager
     */
    RelationshipManager getRelationshipManager(ObjectProvider op);

    public static interface LifecycleListener
    {
        /**
         * Invoked before closing the ExecutionContext
         * @param ec execution context
         */
        void preClose(ExecutionContext ec);
    }
}