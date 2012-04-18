/**********************************************************************
Copyright (c) 2005 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.store.types.sco.backed;

import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.state.ObjectProviderFactory;
import org.datanucleus.state.RelationshipManager;
import org.datanucleus.store.BackedSCOStoreManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.scostore.SetStore;
import org.datanucleus.store.types.sco.SCOCollectionIterator;
import org.datanucleus.store.types.sco.SCOUtils;
import org.datanucleus.store.types.sco.queued.AddOperation;
import org.datanucleus.store.types.sco.queued.ClearCollectionOperation;
import org.datanucleus.store.types.sco.queued.OperationQueue;
import org.datanucleus.store.types.sco.queued.QueuedOperation;
import org.datanucleus.store.types.sco.queued.RemoveCollectionOperation;
import org.datanucleus.util.NucleusLogger;

/**
 * A mutable second-class SortedSet object.
 * This class extends AbstractSet, using that class to contain the current objects, and the backing SetStore 
 * to be the interface to the datastore. A "backing store" is not present for datastores that dont use
 * DatastoreClass, or if the container is serialised or non-persistent.
 * 
 * <H3>Modes of Operation</H3>
 * The user can operate the list in 2 modes.
 * The <B>cached</B> mode will use an internal cache of the elements (in the "delegate") reading them at
 * the first opportunity and then using the cache thereafter.
 * The <B>non-cached</B> mode will just go direct to the "backing store" each call.
 *
 * <H3>Mutators</H3>
 * When the "backing store" is present any updates are passed direct to the datastore as well as to the "delegate".
 * If the "backing store" isn't present the changes are made to the "delegate" only.
 *
 * <H3>Accessors</H3>
 * When any accessor method is invoked, it typically checks whether the container has been loaded from its
 * "backing store" (where present) and does this as necessary. Some methods (<B>size()</B>) just check if 
 * everything is loaded and use the delegate if possible, otherwise going direct to the datastore.
 */
public class SortedSet extends org.datanucleus.store.types.sco.simple.SortedSet
{
    protected transient boolean allowNulls = false;
    protected transient SetStore backingStore;
    protected transient boolean useCache = true;
    protected transient boolean isCacheLoaded = false;
    protected transient boolean queued = false;
    protected transient OperationQueue<SetStore> operationQueue = null;

    /**
     * Constructor, using the StateManager of the "owner" and the field name.
     * @param ownerSM The owner StateManager
     * @param fieldName The name of the field of the SCO.
     */
    public SortedSet(ObjectProvider ownerSM, String fieldName)
    {
        super(ownerSM, fieldName);

        ExecutionContext ec = ownerSM.getExecutionContext();
        AbstractMemberMetaData fmd = ownerSM.getClassMetaData().getMetaDataForMember(fieldName);
        fieldNumber = fmd.getAbsoluteFieldNumber();
        allowNulls = SCOUtils.allowNullsInContainer(allowNulls, fmd);
        queued = ec.isDelayDatastoreOperationsEnabled();
        useCache = SCOUtils.useContainerCache(ownerSM, fieldName);

        if (!SCOUtils.collectionHasSerialisedElements(fmd) && 
                fmd.getPersistenceModifier() == FieldPersistenceModifier.PERSISTENT)
        {
            ClassLoaderResolver clr = ec.getClassLoaderResolver();
            this.backingStore = (SetStore)
            ((BackedSCOStoreManager)ec.getStoreManager()).getBackingStoreForField(clr,fmd,java.util.SortedSet.class);
        }

        // Set up our delegate, using a suitable comparator
        Comparator comparator = SCOUtils.getComparator(fmd, ownerSM.getExecutionContext().getClassLoaderResolver());
        if (comparator != null)
        {
            this.delegate = new java.util.TreeSet(comparator);
        }
        else
        {
            this.delegate = new java.util.TreeSet();
        }

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(SCOUtils.getContainerInfoMessage(ownerSM, fieldName, this,
                useCache, queued, allowNulls, SCOUtils.useCachedLazyLoading(ownerSM, fieldName)));
        }
    }

    /**
     * Method to initialise the SCO from an existing value.
     * @param o  The object to set from
     * @param forInsert Whether the object needs inserting in the datastore with this value
     * @param forUpdate Whether to update the datastore with this value
     */
    public void initialise(Object o, boolean forInsert, boolean forUpdate)
    {
        Collection c = (Collection)o;
        if (c != null)
        {
            // Check for the case of serialised PC elements, and assign StateManagers to the elements without
            AbstractMemberMetaData fmd = ownerSM.getClassMetaData().getMetaDataForMember(fieldName);
            if (SCOUtils.collectionHasSerialisedElements(fmd) && fmd.getCollection().elementIsPersistent())
            {
                ExecutionContext ec = ownerSM.getExecutionContext();
                Iterator iter = c.iterator();
                while (iter.hasNext())
                {
                    Object pc = iter.next();
                    ObjectProvider objSM = ec.findObjectProvider(pc);
                    if (objSM == null)
                    {
                        objSM = ObjectProviderFactory.newForEmbedded(ec, pc, false, ownerSM, fieldNumber);
                    }
                }
            }

            if (backingStore != null && useCache && !isCacheLoaded)
            {
                // Mark as loaded
                isCacheLoaded = true;
            }

            if (forInsert)
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023007", 
                        ownerSM.toPrintableID(), fieldName, "" + c.size()));
                }
                addAll(c);
            }
            else if (forUpdate)
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023008", 
                        ownerSM.toPrintableID(), fieldName, "" + c.size()));
                }
                clear();
                addAll(c);
            }
            else
            {
                if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                {
                    NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023007", 
                        ownerSM.toPrintableID(), fieldName, "" + c.size()));
                }
                delegate.clear();
                delegate.addAll(c);
            }
        }
    }

    /**
     * Method to initialise the SCO for use.
     */
    public void initialise()
    {
        if (useCache && !SCOUtils.useCachedLazyLoading(ownerSM, fieldName))
        {
            // Load up the container now if not using lazy loading
            loadFromStore();
        }
    }

    // ----------------------- Implementation of SCO methods -------------------

    /**
     * Accessor for the unwrapped value that we are wrapping.
     * @return The unwrapped value
     */
    public Object getValue()
    {
        loadFromStore();
        return super.getValue();
    }

    /**
     * Method to effect the load of the data in the SCO.
     * Used when the SCO supports lazy-loading to tell it to load all now.
     */
    public void load()
    {
        if (useCache)
        {
            loadFromStore();
        }
    }

    /**
     * Method to return if the SCO has its contents loaded.
     * If the SCO doesn't support lazy loading will just return true.
     * @return Whether it is loaded
     */
    public boolean isLoaded()
    {
        return useCache ? isCacheLoaded : false;
    }

    /**
     * Method to load all elements from the "backing store" where appropriate.
     */
    protected void loadFromStore()
    {
        if (backingStore != null && !isCacheLoaded)
        {
            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023006", 
                    ownerSM.toPrintableID(), fieldName));
            }
            delegate.clear();
            Iterator iter=backingStore.iterator(ownerSM);
            while (iter.hasNext())
            {
                delegate.add(iter.next());
            }

            isCacheLoaded = true;
        }
    }

    /**
     * Method to flush the changes to the datastore when operating in queued mode.
     * Does nothing in "direct" mode.
     */
    public void flush()
    {
        if (queued)
        {
            if (operationQueue != null)
            {
                operationQueue.performAll(backingStore, ownerSM, fieldName);
            }
        }
    }

    /**
     * Convenience method to add a queued operation to the operations we perform at commit.
     * @param op The operation
     */
    protected void addQueuedOperation(QueuedOperation<? super SetStore> op)
    {
        if (operationQueue == null)
        {
            operationQueue = new OperationQueue<SetStore>();
        }
        operationQueue.enqueue(op);
    }

    /**
     * Method to update an embedded element in this collection.
     * @param element The element
     * @param fieldNumber Number of field in the element
     * @param value New value for this field
     */
    public void updateEmbeddedElement(Object element, int fieldNumber, Object value)
    {
        if (backingStore != null)
        {
            backingStore.updateEmbeddedElement(ownerSM, element, fieldNumber, value);
        }
    }

    /**
     * Method to unset the owner and field information.
     */
    public synchronized void unsetOwner()
    {
        super.unsetOwner();
        if (backingStore != null)
        {
            backingStore = null;
        }
    }

    // ------------------ Implementation of SortedSet methods --------------------

    /**
     * Creates and returns a copy of this object.
     * @return The cloned object
     */
    public Object clone()
    {
        if (useCache)
        {
            loadFromStore();
        }

        return delegate.clone();
    }

    /**
     * Accessor for the comparator.
     * @return The comparator
     */
    public Comparator comparator()
    {
        return delegate.comparator();
    }

    /**
     * Accessor for whether an element is contained in this Set.
     * @param element The element
     * @return Whether it is contained.
     **/
    public boolean contains(Object element)
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.contains(element);
        }
        else if (backingStore != null)
        {
            return backingStore.contains(ownerSM,element);
        }

        return delegate.contains(element);
    }

    /**
     * Accessor for whether a collection is contained in this Set.
     * @param c The collection
     * @return Whether it is contained.
     **/
    public synchronized boolean containsAll(java.util.Collection c)
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            java.util.SortedSet sorted = new java.util.TreeSet(c);
            Iterator iter=iterator();
            while (iter.hasNext())
            {
                sorted.remove(iter.next());
            }

            return sorted.isEmpty();
        }

        return delegate.containsAll(c);
    }

    /**
     * Equality operator.
     * @param o The object to compare against.
     * @return Whether this object is the same.
     **/
    public synchronized boolean equals(Object o)
    {
        if (useCache)
        {
            loadFromStore();
        }

        if (o == this)
        {
            return true;
        }
        if (!(o instanceof java.util.Set))
        {
            return false;
        }
        java.util.Set s = (java.util.Set)o;

        return s.size() == size() && containsAll(s);
    }

    /**
     * Accessor for the first element in the sorted set.
     * @return The first element
     **/
    public Object first()
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.first();
        }
        else if (useCache)
        {
            loadFromStore();
        }
        else
        {
            // Use Iterator to get element
            Iterator iter = iterator();
            return iter.next();
        }

        return delegate.first();
    }

    /**
     * Hashcode operator.
     * @return The Hash code.
     **/
    public synchronized int hashCode()
    {
        if (useCache)
        {
            loadFromStore();
        }
        return delegate.hashCode();
    }

    /**
     * Accessor for whether the SortedSet is empty.
     * @return Whether it is empty.
     **/
    public boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Accessor for an iterator for the Set.
     * @return The iterator
     **/
    public Iterator iterator()
    {
        // Populate the cache if necessary
        if (useCache)
        {
            loadFromStore();
        }
        return new SCOCollectionIterator(this, ownerSM, delegate, backingStore, useCache);
    }

    /**
     * Method to retrieve the head elements up to the specified element.
     * @param toElement the element to return up to.
     * @return The set of elements meeting the input
     */
    public java.util.SortedSet headSet(Object toElement)
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.headSet(toElement);
        }
        else if (useCache)
        {
            loadFromStore();
        }
        else
        {
            // TODO Provide a datastore method to do this
            throw new NucleusUserException("Don't currently support SortedSet.headSet() when not using cached collections");
        }

        return delegate.headSet(toElement);
    }

    /**
     * Method to retrieve the subset of elements between the specified elements.
     * @param fromElement The start element
     * @param toElement The end element
     * @return The set of elements meeting the input
     */
    public java.util.SortedSet subSet(Object fromElement, Object toElement)
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.subSet(fromElement, toElement);
        }
        else if (useCache)
        {
            loadFromStore();
        }
        else
        {
            // TODO Provide a datastore method to do this
            throw new NucleusUserException("Don't currently support SortedSet.subSet() when not using cached collections");
        }

        return delegate.subSet(fromElement, toElement);
    }

    /**
     * Method to retrieve the set of elements after the specified element.
     * @param fromElement The start element
     * @return The set of elements meeting the input
     */
    public java.util.SortedSet tailSet(Object fromElement)
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.headSet(fromElement);
        }
        else if (useCache)
        {
            loadFromStore();
        }
        else
        {
            // TODO Provide a datastore method to do this
            throw new NucleusUserException("Don't currently support SortedSet.tailSet() when not using cached collections");
        }

        return delegate.headSet(fromElement);
    }

    /**
     * Accessor for the last element in the sorted set.
     * @return The last element
     **/
    public Object last()
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.last();
        }
        else if (useCache)
        {
            loadFromStore();
        }
        else
        {
            // Use Iterator to get element
            Iterator iter = iterator();
            Object last = null;
            while (iter.hasNext())
            {
                last = iter.next();
            }
            return last;
        }

        return delegate.last();
    }

    /**
     * Accessor for the size of the SortedSet.
     * @return The size.
     **/
    public int size()
    {
        if (useCache && isCacheLoaded)
        {
            // If the "delegate" is already loaded, use it
            return delegate.size();
        }
        else if (backingStore != null)
        {
            return backingStore.size(ownerSM);
        }

        return delegate.size();
    }

    /**
     * Method to return the list as an array.
     * @return The array
     **/
    public Object[] toArray()
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return SCOUtils.toArray(backingStore,ownerSM);
        }  
        return delegate.toArray();
    }

    /**
     * Method to return the list as an array.
     * @param a The runtime types of the array being defined by this param
     * @return The array
     **/
    public Object[] toArray(Object a[])
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return SCOUtils.toArray(backingStore,ownerSM,a);
        }  
        return delegate.toArray(a);
    }
 
    // ------------------------------ Mutator methods --------------------------

    /**
     * Method to add an element to the SortedSet.
     * @param element The new element
     * @return Whether it was added ok.
     **/
    public boolean add(Object element)
    {
        // Reject inappropriate elements
        if (!allowNulls && element == null)
        {
            throw new NullPointerException("Nulls not allowed for collection at field " + fieldName + " but element is null");
        }

        if (useCache)
        {
            loadFromStore();
        }
        if (contains(element))
        {
            return false;
        }

        if (ownerSM != null && ownerSM.getExecutionContext().getManageRelations())
        {
            // Relationship management
            ownerSM.getExecutionContext().getRelationshipManager(ownerSM).relationAdd(fieldNumber, element);
        }

        boolean backingSuccess = true;
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                addQueuedOperation(new AddOperation(element));
            }
            else
            {
                try
                {
                    backingStore.add(ownerSM,element, (useCache ? delegate.size() : -1));
                }
                catch (NucleusDataStoreException dse)
                {
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("023013", "add", fieldName, dse));
                    backingSuccess = false;
                }
            }
        }

        // Only make it dirty after adding the field to the datastore so we give it time
        // to be inserted - otherwise jdoPreStore on this object would have been called before completing the addition
        makeDirty();

        boolean delegateSuccess = delegate.add(element);
        return (backingStore != null ? backingSuccess : delegateSuccess);
    }

    /**
     * Method to add a collection to the SortedSet.
     * @param elements The collection
     * @return Whether it was added ok.
     **/
    public boolean addAll(Collection elements)
    {
        if (useCache)
        {
            loadFromStore();
        }
        if (ownerSM != null && ownerSM.getExecutionContext().getManageRelations())
        {
            // Relationship management
            Iterator iter = elements.iterator();
            RelationshipManager relMgr = ownerSM.getExecutionContext().getRelationshipManager(ownerSM);
            while (iter.hasNext())
            {
                relMgr.relationAdd(fieldNumber, iter.next());
            }
        }

        boolean backingSuccess = true;
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                for (Object element : elements)
                {
                    addQueuedOperation(new AddOperation(element));
                }
            }
            else
            {
                try
                {
                    backingSuccess = backingStore.addAll(ownerSM, elements, (useCache ? delegate.size() : -1));
                }
                catch (NucleusDataStoreException dse)
                {
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("023013", "addAll", fieldName, dse));
                    backingSuccess = false;
                }
            }
        }

        // Only make it dirty after adding the field to the datastore so we give it time
        // to be inserted - otherwise jdoPreStore on this object would have been called before completing the addition
        makeDirty();

        boolean delegateSuccess = delegate.addAll(elements);
        return (backingStore != null ? backingSuccess : delegateSuccess);
    }

    /**
     * Method to clear the SortedSet
     **/
    public void clear()
    {
        makeDirty();

        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                addQueuedOperation(new ClearCollectionOperation());
            }
            else
            {
                backingStore.clear(ownerSM);
            }
        }
        delegate.clear();
    }

    /**
     * Method to remove an element from the SortedSet.
     * @param element The element
     * @return Whether it was removed ok.
     **/
    public boolean remove(Object element)
    {
        return remove(element, true);
    }

    /**
     * Method to remove an element from the collection, and observe the flag for whether to allow cascade delete.
     * @param element The element
     * @param allowCascadeDelete Whether to allow cascade delete
     */
    public boolean remove(Object element, boolean allowCascadeDelete)
    {
        makeDirty();

        if (useCache)
        {
            loadFromStore();
        }

        int size = (useCache ? delegate.size() : -1);
        boolean contained = delegate.contains(element);
        boolean delegateSuccess = delegate.remove(element);
        if (ownerSM != null && ownerSM.getExecutionContext().getManageRelations())
        {
            ownerSM.getExecutionContext().getRelationshipManager(ownerSM).relationRemove(fieldNumber, element);
        }

        boolean backingSuccess = true;
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                backingSuccess = contained;
                if (backingSuccess)
                {
                    addQueuedOperation(new RemoveCollectionOperation(element, allowCascadeDelete));
                }
            }
            else
            {
                try
                {
                    backingSuccess = backingStore.remove(ownerSM, element, size, allowCascadeDelete);
                }
                catch (NucleusDataStoreException dse)
                {
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("023013", "remove", fieldName, dse));
                    backingSuccess = false;
                }
            }
        }

        return (backingStore != null ? backingSuccess : delegateSuccess);
    }

    /**
     * Method to remove all elements from the collection from the SortedSet.
     * @param elements The collection of elements to remove 
     * @return Whether it was removed ok.
     **/
    public boolean removeAll(java.util.Collection elements)
    {
        makeDirty();
 
        if (useCache)
        {
            loadFromStore();
        }

        if (ownerSM != null && ownerSM.getExecutionContext().getManageRelations())
        {
            // Relationship management
            Iterator iter = elements.iterator();
            RelationshipManager relMgr = ownerSM.getExecutionContext().getRelationshipManager(ownerSM);
            while (iter.hasNext())
            {
                relMgr.relationRemove(fieldNumber, iter.next());
            }
        }

        if (backingStore != null)
        {
            boolean backingSuccess = true;
            int size = (useCache ? delegate.size() : -1);

            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                backingSuccess = false;
                for (Object element : elements)
                {
                    if (contains(element))
                    {
                        backingSuccess = true;
                        addQueuedOperation(new RemoveCollectionOperation(element, true));
                    }
                }
            }
            else
            {
                try
                {
                    backingSuccess = backingStore.removeAll(ownerSM, elements, size);
                }
                catch (NucleusDataStoreException dse)
                {
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("023013", "removeAll", fieldName, dse));
                    backingSuccess = false;
                }
            }

            delegate.removeAll(elements); // Remove from the delegate too
            return backingSuccess;
        }
        else
        {
            return delegate.removeAll(elements);
        }
    }

    /**
     * Method to retain a Collection of elements (and remove all others).
     * @param c The collection to retain
     * @return Whether they were retained successfully.
     **/
    public synchronized boolean retainAll(java.util.Collection c)
    {
        makeDirty();

        if (useCache)
        {
            loadFromStore();
        }

        boolean modified = false;
        Iterator iter=iterator();
        while (iter.hasNext())
        {
            Object element = iter.next();
            if (!c.contains(element))
            {
                iter.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * The writeReplace method is called when ObjectOutputStream is preparing
     * to write the object to the stream. The ObjectOutputStream checks
     * whether the class defines the writeReplace method. If the method is
     * defined, the writeReplace method is called to allow the object to
     * designate its replacement in the stream. The object returned should be
     * either of the same type as the object passed in or an object that when
     * read and resolved will result in an object of a type that is compatible
     * with all references to the object.
     * 
     * @return the replaced object
     * @throws ObjectStreamException
     */
    protected Object writeReplace() throws ObjectStreamException
    {
        if (useCache)
        {
            loadFromStore();
            return new java.util.TreeSet(delegate);
        }
        else
        {
            // TODO Cater for non-cached collection, load elements in a DB call.
            return new java.util.TreeSet(delegate);
        }
    }
}