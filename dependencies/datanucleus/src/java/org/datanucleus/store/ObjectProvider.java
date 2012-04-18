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
2011 Andy Jefferson - all javadocs, many methods added during merge with StateManager
    ...
**********************************************************************/
package org.datanucleus.store;

import java.util.Set;

import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.state.ActivityState;
import org.datanucleus.state.FetchPlanState;
import org.datanucleus.state.LifeCycleState;
import org.datanucleus.store.fieldmanager.FieldManager;

/**
 * Provider of field information for a managed object.
 */
public interface ObjectProvider
{
    /** PC **/
    public static short PC = 0;
    /** Embedded (or serialised) PC **/
    public static short EMBEDDED_PC = 1;
    /** Embedded (or serialised) Collection Element PC **/
    public static short EMBEDDED_COLLECTION_ELEMENT_PC = 2;
    /** Embedded (or serialised) Map Key PC **/
    public static short EMBEDDED_MAP_KEY_PC = 3;
    /** Embedded (or serialised) Map Value PC **/
    public static short EMBEDDED_MAP_VALUE_PC = 4;

    /**
     * Accessor for the ClassMetaData for this object.
     * @return The ClassMetaData.
     */
    AbstractClassMetaData getClassMetaData();

    ExecutionContext getExecutionContext();

    /**
     * Accessor for the LifeCycleState
     * @return the LifeCycleState
     */
    LifeCycleState getLifecycleState();

    /**
     * Returns a printable form of the identity of the managed object.
     * @return The printable form of the id
     */
    String toPrintableID();

    /**
     * Method to change the value of the specified field. This will not make the field dirty
     * @param fieldNumber (absolute) field number of the field
     * @param value The new value.
     */
    void replaceField(int fieldNumber, Object value);

    /**
     * Method to change the value of the specified field. This will make the field dirty.
     * @param fieldNumber (absolute) field number of the field
     * @param value The new value.
     */
    void replaceFieldMakeDirty(int fieldNumber, Object value);

    /**
     * Convenience method to change the value of a field that is assumed loaded.
     * Will mark the object/field as dirty if it isnt previously.
     * Only for use in management of relations.
     * @param fieldNumber Number of field
     * @param newValue The new value
     */
    void replaceFieldValue(int fieldNumber, Object newValue);

    /**
     * Method to update the data in the object with the values from the passed FieldManager
     * @param fieldNumbers (absolute) field numbers of the fields to update
     * @param fm The FieldManager
     */
    void replaceFields(int fieldNumbers[], FieldManager fm);

    /**
     * Method to update the data in the object with the values from the passed FieldManager
     * @param fieldNumbers (absolute) field numbers of the fields to update
     * @param fm The FieldManager
     * @param replaceWhenDirty Whether to replace these fields if the field is dirty
     */
    void replaceFields(int fieldNumbers[], FieldManager fm, boolean replaceWhenDirty);

    /**
     * Method to update the data in the object with the values from the passed
     * FieldManager. Only non loaded fields are updated
     * @param fieldNumbers (absolute) field numbers of the fields to update
     * @param fm The FieldManager
     */
    void replaceNonLoadedFields(int fieldNumbers[], FieldManager fm);

    /**
     * Method to replace all loaded SCO fields with wrappers.
     * If the loaded field already uses a SCO wrapper nothing happens to that field.
     */
    void replaceAllLoadedSCOFieldsWithWrappers();

    /**
     * Method to replace all loaded (wrapped) SCO fields with unwrapped values.
     * If the loaded field doesnt use a SCO wrapper nothing happens to that field.
     */
    void replaceAllLoadedSCOFieldsWithValues();

    /**
     * Method to obtain updated field values from the passed FieldManager.
     * @param fieldNumbers The numbers of the fields
     * @param fm The fieldManager
     */
    void provideFields(int fieldNumbers[], FieldManager fm);

    /**
     * Method to return the current value of the specified field.
     * @param fieldNumber (absolute) field number of the field
     * @return The current value
     */
    Object provideField(int fieldNumber);

    /**
     * Method to wrap a SCO field (if not wrapped currently) and return the wrapped value.
     * If the field is not a SCO field will just return the value.
     * If "replaceFieldIfChanged" is set, we replace the value in the object with the wrapped value.
     * @param fieldNumber Number of the field
     * @param value The value to give it
     * @param forInsert Whether the creation of any wrapper should insert this value into the datastore
     * @param forUpdate Whether the creation of any wrapper should update the datastore with this value
     * @param replaceFieldIfChanged Whether to replace the field in the object if wrapping the value
     * @return The wrapper (or original value if not wrappable)
     */
    Object wrapSCOField(int fieldNumber, Object value, boolean forInsert, boolean forUpdate, boolean replaceFieldIfChanged);

    /**
     * Method to unwrap a SCO field (if it is wrapped currently) and return the unwrapped value.
     * If the field is not a SCO field will just return the value.
     * If "replaceFieldIfChanged" is set, we replace the value in the object with the unwrapped value.
     * @param fieldNumber The field number
     * @param value The value to unwrap for this field
     * @param replaceFieldIfChanged Whether to replace the field value in the object if unwrapping the value
     * @return The unwrapped field value
     */
    Object unwrapSCOField(int fieldNumber, Object value, boolean replaceFieldIfChanged);

    /**
     * The object being persisted, or a virtual object containing properties to be persisted
     * For persistence of (depends on the API/language):
     * - Java objects, the object returned is the actual Java object being persisted
     * - JSON objects, the object returned is an instance of org.json.JSONObject
     * - XML objects, the object returned is an instance of org.w3c.dom.Node
     * 
     * Warning: to obtain the type being persisted use getClassMetaData(), otherwise the store
     * will not be able objects from any language
     * @return the object being persisted, or a virtual object containing properties to be persisted
     */
    Object getObject();

    /**
     * Accessor for the object id of the managed object.
     * @return The (external) id
     */
    Object getObjectId();

    /**
     * Accessor for the id of the object managed by this ObjectProvider.
     * @return The identity of the object
     */
    Object getInternalObjectId();

    Object getExternalObjectId();

    /**
     * Method to set an associated value stored with this object.
     * This is for a situation such as in ORM where this object can have an "external" foreign-key
     * provided by an owning object (e.g 1-N uni relation and this is the element with no knowledge
     * of the owner, so the associated value is the FK value).
     * @param key Key for the value
     * @param value The associated value
     */
    void setAssociatedValue(Object key, Object value);

    /**
     * Accessor for the value of an external field.
     * This is for a situation such as in ORM where this object can have an "external" foreign-key
     * provided by an owning object (e.g 1-N uni relation and this is the element with no knowledge
     * of the owner, so the associated value is the FK value).
     * @param key The key for this associated information
     * @return The value stored (if any) against this key
     */
    Object getAssociatedValue(Object key);
    
    /**
     * Accessor for the field numbers of all dirty fields.
     * @return Absolute field numbers of the dirty fields in this instance.
     */
    int[] getDirtyFieldNumbers();
    
    /**
     * Accessor for the names of the fields that are dirty.
     * @return Names of the dirty fields
     */
    String[] getDirtyFieldNames();

    /**
     * Creates a copy of the internal dirtyFields array.
     * @return a copy of the internal dirtyFields array.
     */
    public boolean[] getDirtyFields();

    /**
     * Marks the given field dirty.
     * @param field The no of field to mark as dirty. 
     */
    void makeDirty(int field);

    /**
     * Method to register an owner ObjectProvider with this embedded/serialised object.
     * @param ownerSM The owning State Manager.
     * @param ownerFieldNumber The field number in the owner that the embedded/serialised object is stored as
     */
    void addEmbeddedOwner(ObjectProvider ownerSM, int ownerFieldNumber);

    /**
     * Accessor for the overall owner ObjectProviders of the managed object when embedded.
     * @return Owning ObjectProviders when embedded (if any)
     */
    ObjectProvider[] getEmbeddedOwners();

    /**
     * Convenience accessor for whether this ObjectProvider manages an embedded/serialised object.
     * @return Whether the managed object is embedded/serialised.
     */
    boolean isEmbedded();

    /**
     * Convenience method to update our object with the field values from the passed object.
     * Objects need to be of the same type, and the other object should not have a ObjectProvider.
     * @param pc The object that we should copy fields from
     */
    void copyFieldsFromObject(Object pc, int[] fieldNumbers);

    /**
     * Method to run reachability from this ObjectProvider.
     * @param reachables List of reachable ObjectProviders so far
     */
    void runReachability(Set reachables);

    /**
     * Method to set this ObjectProvider as managing an embedded/serialised object.
     * @param type The type of object being managed
     */
    void setPcObjectType(short type);

    /**
     * Method to set the storing PC flag.
     */
    void setStoringPC();

    /**
     * Method to unset the storing PC flag.
     */
    void unsetStoringPC();

    /**
     * Accessor for whether all changes have been written to the datastore.
     * @return Whether the datastore has all changes
     */
    boolean isFlushedToDatastore();

    /**
     * Whether this record has been flushed to the datastore in this transaction (i.e called persist() and is in
     * the datastore now). If user has called persist() on it yet not yet persisted then returns false.
     * @return Whether this is flushed new.
     */
    boolean isFlushedNew();

    void setFlushedNew(boolean flag);

    /**
     * Method to flush all changes to the datastore.
     */
    void flush();

    void setFlushing(boolean flushing);

    /**
     * Method to notify the object provider that the object has now been flushed to the datastore.
     * This is performed when handling inserts or deletes in a batch external to the ObjectProvider.
     */
    void markAsFlushed();

    /**
     * Method to locate that the object exists in the datastore.
     * @throws NucleusObjectNotFoundException if not present
     */
    void locate();

    /**
     * Tests whether this object is new yet waiting to be flushed to the datastore.
     * @return true if this instance is waiting to be flushed
     */
    boolean isWaitingToBeFlushedToDatastore();

    /**
     * Update the acitvity state.
     * @param state the activity state
     */
    void changeActivityState(ActivityState state);

    /**
     * Tests whether this object is being inserted.
     * @return true if this instance is inserting.
     */
    boolean isInserting();

    /**
     * Tests whether this object is in the process of being deleted.
     * @return true if this instance is being deleted.
     */
    boolean isDeleting();

    /**
     * Whether this object is moving to a deleted state.
     * @return Whether the object will be moved into a deleted state during this operation
     */
    boolean becomingDeleted();

    /**
     * Convenience method to load the passed field values.
     * Loads the fields using any required fetch plan and calls jdoPostLoad() as appropriate.
     * @param fv Field Values to load (including any fetch plan to use when loading)
     */
    void loadFieldValues(FieldValues fv);

    /**
     * Accessor for the referenced PC object when we are attaching or detaching.
     * When attaching and this is the detached object this returns the newly attached object.
     * When attaching and this is the newly attached object this returns the detached object.
     * When detaching and this is the newly detached object this returns the attached object.
     * When detaching and this is the attached object this returns the newly detached object.
     * @return The referenced object (or null).
     */
    Object getReferencedPC();

    /**
     * Fetch from the database all fields that are not currently loaded regardless of whether
     * they are in the current fetch group or not. Called by lifecycle transitions.
     */
    void loadUnloadedFields();

    /**
     * Convenience method to load the specified field if not loaded.
     * @param fieldNumber Absolute field number
     */
    void loadField(int fieldNumber);

    /**
     * Method to load all unloaded fields in the FetchPlan.
     * Recurses through the FetchPlan objects and loads fields of sub-objects where needed.
     * @param state The FetchPlan state
     */
    void loadFieldsInFetchPlan(FetchPlanState state);

    /**
     * Convenience method to load a field from the datastore.
     * Used in attaching fields and checking their old values (so we don't
     * want any postLoad method being called).
     * TODO Merge this with one of the loadXXXFields methods.
     * @param fieldNumber The field number.
     */
    void loadFieldFromDatastore(int fieldNumber);

    /**
     * Mark the specified field as not loaded so that it will be reloaded on next access.
     * @param fieldName Name of the field
     */
    void unloadField(String fieldName);

    boolean[] getLoadedFields();

    /**
     * Accessor for the field numbers of all loaded fields.
     * @return Absolute field numbers of the loaded fields in this instance.
     */
    int[] getLoadedFieldNumbers();
    
    /**
     * Accessor for the names of the fields that are loaded.
     * @return Names of the loaded fields
     */
    String[] getLoadedFieldNames();

    boolean isLoaded(int absoluteFieldNumber);

    /**
     * Returns whether all fields are loaded.
     * @return Returns true if all fields are loaded.
     */
    boolean getAllFieldsLoaded();

    /**
     * Accessor for whether a field is currently loaded.
     * Just returns the status, unlike "isLoaded" which also loads it if not.
     * @param fieldNumber The (absolute) field number
     * @return Whether it is loaded
     */
    boolean isFieldLoaded(int fieldNumber);

    /**
     * Marks the given field dirty for issuing an update after the insert.
     * @param pc The Persistable object
     * @param fieldNumber The no of field to mark as dirty. 
     */
    void updateFieldAfterInsert(Object pc, int fieldNumber);

    /**
     * Method to allow the setting of the id of the PC object. This is used when it is obtained after persisting
     * the object to the datastore. In the case of RDBMS, this may be via auto-increment, or in the case of ODBMS
     * this may be an accessor for the id after storing.
     * @param id the id received from the datastore. May be an OID, or the key value for an OID, or an application id.
     */
    void setPostStoreNewObjectId(Object id);

    /**
     * Method to swap the managed object for the supplied object.
     * This is of particular use for object datastores where the datastore is responsible for creating
     * the in-memory object and where we have a temporary object that we want to swap for the datastore
     * generated object. Makes no change to what fields are loaded/dirty etc, just swaps the managed object.
     * @param pc The persistable object to use
     */
    void replaceManagedPC(Object pc);

    /**
     * Sets the value for the version column in a transaction not yet committed
     * @param nextVersion version to use
     */
    void setTransactionalVersion(Object nextVersion);

    /**
     * Return the object representing the transactional version of the managed object.
     * @return the object representing the version of the calling instance
     */
    Object getTransactionalVersion();

    /**
     * Method to set the current version of the managed object.
     * @param version The version
     */
    void setVersion(Object version);

    /**
     * Method to return the current version of the managed object.
     * @return The version
     */
    Object getVersion();

    /**
     * Method to lock the object owned by this ObjectProvider.
     * @param lockMode Lock mode to apply
     */
    void lock(short lockMode);

    /**
     * Method to unlock the object owned by this ObjectProvider (if locked).
     */
    void unlock();

    /**
     * Accessor for the current lock mode.
     * @return Lock mode
     */
    short getLockMode();
}