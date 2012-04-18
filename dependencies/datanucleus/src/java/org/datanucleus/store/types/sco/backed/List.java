/**********************************************************************
Copyright (c) 2003 David Jencks and others. All rights reserved.
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
2003 Andy Jefferson - updated to make correct use of makeDirty() etc.
2004 Andy Jefferson - rewritten to always have delegate.
2004 Andy Jefferson - rewritten to allow caching
2005 Andy Jefferson - allowed for serialised collection
    ...
**********************************************************************/
package org.datanucleus.store.types.sco.backed;

import java.io.ObjectStreamException;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.FieldPersistenceModifier;
import org.datanucleus.state.ObjectProviderFactory;
import org.datanucleus.store.BackedSCOStoreManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.scostore.ListStore;
import org.datanucleus.store.types.sco.SCOListIterator;
import org.datanucleus.store.types.sco.SCOUtils;
import org.datanucleus.store.types.sco.queued.AddAtOperation;
import org.datanucleus.store.types.sco.queued.AddOperation;
import org.datanucleus.store.types.sco.queued.ClearCollectionOperation;
import org.datanucleus.store.types.sco.queued.OperationQueue;
import org.datanucleus.store.types.sco.queued.QueuedOperation;
import org.datanucleus.store.types.sco.queued.RemoveAtOperation;
import org.datanucleus.store.types.sco.queued.RemoveCollectionOperation;
import org.datanucleus.store.types.sco.queued.SetOperation;
import org.datanucleus.util.NucleusLogger;

/**
 * A mutable second-class List object.
 * This class extends AbstractList, using that class to contain the current objects, and the backing ListStore 
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
public class List extends org.datanucleus.store.types.sco.simple.List
{
    protected transient boolean allowNulls = false;
    protected transient ListStore backingStore;
    protected transient boolean useCache=true;
    protected transient boolean isCacheLoaded=false;
    protected transient boolean queued = false;
    protected transient OperationQueue<ListStore> operationQueue = null;

    /**
     * Constructor, using the StateManager of the "owner" and the field name.
     * @param ownerSM The owner StateManager
     * @param fieldName The name of the field of the SCO.
     */
    public List(ObjectProvider ownerSM, String fieldName)
    {
        super(ownerSM, fieldName);

        // Set up our delegate
        this.delegate = new java.util.ArrayList();

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
            this.backingStore = (ListStore)
            ((BackedSCOStoreManager)ec.getStoreManager()).getBackingStoreForField(clr,fmd,java.util.List.class);
        }

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(SCOUtils.getContainerInfoMessage(ownerSM, fieldName, this,
                useCache, queued, allowNulls, SCOUtils.useCachedLazyLoading(ownerSM, fieldName)));
        }
    }

    /**
     * Method to initialise the SCO from an existing value.
     * @param o The object to set from
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

    // ----------------------------- Implementation of SCO methods -----------------------------------

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
    protected void addQueuedOperation(QueuedOperation<? super ListStore> op)
    {
        if (operationQueue == null)
        {
            operationQueue = new OperationQueue<ListStore>();
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

    // ------------------- Implementation of List methods ----------------------
 
    /**
     * Creates and returns a copy of this object.
     * <P>Mutable second-class Objects are required to provide a public
     * clone method in order to allow for copying PersistenceCapable
     * objects. In contrast to Object.clone(), this method must not throw a
     * CloneNotSupportedException.
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
     * Accessor for whether an element is contained in the List.
     * @param element The element
     * @return Whether the element is contained here
     **/
    public synchronized boolean contains(Object element)
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
     * Accessor for whether a collection of elements are contained here.
     * @param c The collection of elements.
     * @return Whether they are contained.
     **/
    public synchronized boolean containsAll(java.util.Collection c)
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            java.util.HashSet h=new java.util.HashSet(c);
            Iterator iter=iterator();
            while (iter.hasNext())
            {
                h.remove(iter.next());
            }

            return h.isEmpty();
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
        if (!(o instanceof java.util.List))
        {
            return false;
        }
        java.util.List l=(java.util.List)o;

        if (l.size() != size())
        {
        	return false;	
        }
        if (!(containsAll(l) && l.containsAll(this))) 
        {
        	return false;
        }
        
        // test for equal order of contained items
        return super.equals(o);
    }

    /**
     * Method to retrieve an element no.
     * @param index The item to retrieve
     * @return The element at that position.
     **/
    public Object get(int index)
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return backingStore.get(ownerSM, index);
        }

        return delegate.get(index);
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
     * Method to the position of an element.
     * @param element The element.
     * @return The position.
     **/
    public int indexOf(Object element)
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return backingStore.indexOf(ownerSM, element);
        }
 
        return delegate.indexOf(element);
    }

    /**
     * Accessor for whether the List is empty.
     * @return Whether it is empty.
     **/
    public synchronized boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Method to retrieve the last position of the element.
     * @param element The element
     * @return The last position of this element in the List.
     **/
    public int lastIndexOf(Object element)
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return backingStore.lastIndexOf(ownerSM, element);
        }
 
        return delegate.lastIndexOf(element);
    }

    /**
     * Accessor for an iterator for the List
     * @return The iterator
     **/
    public synchronized Iterator iterator()
    {
        // Populate the cache if necessary
        if (useCache)
        {
            loadFromStore();
        }

        return new SCOListIterator(this, ownerSM, delegate, backingStore, useCache, -1);
    }

    /**
     * Method to retrieve a List iterator for the list.
     * @return The iterator
     **/
    public ListIterator listIterator()
    {
        // Populate the cache if necessary
        if (useCache)
        {
            loadFromStore();
        }

        return new SCOListIterator(this, ownerSM, delegate, backingStore, useCache, -1);
    }

    /**
     * Method to retrieve a List iterator for the list from the index.
     * @param index The start point 
     * @return The iterator
     **/
    public ListIterator listIterator(int index)
    {
        // Populate the cache if necessary
        if (useCache)
        {
            loadFromStore();
        }

        return new SCOListIterator(this, ownerSM, delegate, backingStore, useCache, index);
    }

    /**
     * Accessor for the size of the List
     * @return The size
     **/
    public synchronized int size()
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
     * Accessor for the subList of elements between from and to of the List
     * @param from Start index (inclusive)
     * @param to End index (exclusive) 
     * @return The subList
     **/
    public synchronized java.util.List subList(int from,int to)
    {
        if (useCache)
        {
            loadFromStore();
        }
        else if (backingStore != null)
        {
            return backingStore.subList(ownerSM,from,to);
        }

        return delegate.subList(from,to);
    }

    /**
     * Method to return the List as an array.
     * @return The array
     **/
    public synchronized Object[] toArray()
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
     * Method to return the List as an array.
     * @param a The array to copy to
     * @return The array
     **/
    public synchronized Object[] toArray(Object a[])
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

    // ----------------------------- Mutators methods --------------------------
 
    /**
     * Method to add an element to the List
     * @param element The element to add
     * @return Whether it was added successfully.
     **/
    public synchronized boolean add(Object element)
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

        // Only make it dirty after adding the element(s) to the datastore so we give it time
        // to be inserted - otherwise jdoPreStore on this object would have been called before completing the addition
        makeDirty();

        boolean delegateSuccess = delegate.add(element);
        return (backingStore != null ? backingSuccess : delegateSuccess);
    }

    /**
     * Method to add an element to the List at a position.
     * @param element The element to add
     * @param index The position
     **/
    public void add(int index, Object element)
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

        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                addQueuedOperation(new AddAtOperation(index, element));
            }
            else
            {
                try
                {
                    backingStore.add(ownerSM, element, index, (useCache ? delegate.size() : -1));
                }
                catch (NucleusDataStoreException dse)
                {
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("023013", "add", fieldName, dse));
                }
            }
        }

        // Only make it dirty after adding the element(s) to the datastore so we give it time
        // to be inserted - otherwise jdoPreStore on this object would have been called before completing the addition
        makeDirty();

        delegate.add(index, element);
    }

    /**
     * Method to add a Collection to the ArrayList.
     * @param elements The collection
     * @return Whether it was added ok.
     */
    public boolean addAll(Collection elements)
    {
        if (useCache)
        {
            loadFromStore();
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

        // Only make it dirty after adding the element(s) to the datastore so we give it time
        // to be inserted - otherwise jdoPreStore on this object would have been called before completing the addition
        makeDirty();

        boolean delegateSuccess = delegate.addAll(elements);
        return (backingStore != null ? backingSuccess : delegateSuccess);
    }

    /**
     * Method to add a collection of elements.
     * @param elements The collection of elements to add.
     * @param index The position to add them
     * @return Whether they were added successfully.
     **/
    public boolean addAll(int index, Collection elements)
    {
        if (useCache)
        {
            loadFromStore();
        }

        boolean backingSuccess = true;
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                int pos = index;
                for (Object element : elements)
                {
                    addQueuedOperation(new AddAtOperation(pos++, element));
                }
            }
            else
            {
                try
                {
                    backingSuccess = backingStore.addAll(ownerSM, elements, index, (useCache ? delegate.size() : -1));
                }
                catch (NucleusDataStoreException dse)
                {
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("023013", "addAll", fieldName, dse));
                    backingSuccess = false;
                }
            }
        }

        // Only make it dirty after adding the element(s) to the datastore so we give it time
        // to be inserted - otherwise jdoPreStore on this object would have been called before completing the addition
        makeDirty();

        boolean delegateSuccess = delegate.addAll(index, elements);
        return (backingStore != null ? backingSuccess : delegateSuccess);
    }

    /**
     * Method to clear the List
     **/
    public synchronized void clear()
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
     * Method to remove an element from the List
     * @param element The Element to remove
     * @return Whether it was removed successfully.
     **/
    public synchronized boolean remove(Object element)
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
     * Method to remove an element from the ArrayList.
     * @param index The element position.
     * @return The object that was removed
     */
    public Object remove(int index)
    {
        makeDirty();

        if (useCache)
        {
            loadFromStore();
        }

        int size = (useCache ? delegate.size() : -1);
        Object delegateObject = (useCache ? delegate.remove(index) : null);

        Object backingObject = null;
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                backingObject = delegateObject;
                addQueuedOperation(new RemoveAtOperation(index));
            }
            else
            {
                try
                {
                    backingObject = backingStore.remove(ownerSM, index, size);
                }
                catch (NucleusDataStoreException dse)
                {
                    NucleusLogger.PERSISTENCE.warn(LOCALISER.msg("023013", "remove", fieldName, dse));
                    backingObject = null;
                }
            }
        }

        return (backingStore != null ? backingObject : delegateObject);
    }

    /**
     * Method to remove a collection of elements from the List.
     * @param elements Collection of elements to remove
     * @return Whether it was successful.
     */
    public boolean removeAll(Collection elements)
    {
        makeDirty();

        if (useCache)
        {
            loadFromStore();
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

            delegate.removeAll(elements);
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
     * Wrapper addition that allows turning off of the dependent-field checks
     * when doing the position setting. This means that we can prevent the deletion of
     * the object that was previously in that position. This particular feature is used
     * when attaching a list field and where some elements have changed positions.
     * @param index The position
     * @param element The new element
     * @return The element previously at that position
     */
    public Object set(int index, Object element, boolean allowDependentField)
    {
        // Reject inappropriate elements
        if (!allowNulls && element == null)
        {
            throw new NullPointerException("Nulls not allowed for collection at field " + fieldName + " but element is null");
        }

        makeDirty();

        if (useCache)
        {
            loadFromStore();
        }

        // Update the delegate since updating this can cause removal of the original object
        Object delegateReturn = delegate.set(index, element);
        if (backingStore != null)
        {
            if (SCOUtils.useQueuedUpdate(queued, ownerSM))
            {
                addQueuedOperation(new SetOperation(index, element, allowDependentField));
            }
            else
            {
                backingStore.set(ownerSM, index, element, allowDependentField);
            }
        }
        return delegateReturn;
    }

    /**
     * Method to set the element at a position in the ArrayList.
     * @param index The position
     * @param element The new element
     * @return The element previously at that position
     */
    public Object set(int index, Object element)
    {
        return set(index, element, true);
    }

    /**
     * The writeReplace method is called when ObjectOutputStream is preparing
     * to write the object to the stream. The ObjectOutputStream checks whether
     * the class defines the writeReplace method. If the method is defined, the
     * writeReplace method is called to allow the object to designate its 
     * replacement in the stream. The object returned should be either of the
     * same type as the object passed in or an object that when read and
     * resolved will result in an object of a type that is compatible with all
     * references to the object.
     * @return the replaced object
     * @throws ObjectStreamException
     */
    protected Object writeReplace() throws ObjectStreamException
    {
        if (useCache)
        {
            loadFromStore();
            return new java.util.ArrayList(delegate);
        }
        else
        {
            // TODO Cater for non-cached collection, load elements in a DB call.
            return new java.util.ArrayList(delegate);
        }
    }
}