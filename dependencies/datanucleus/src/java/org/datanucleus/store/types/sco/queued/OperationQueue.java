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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.scostore.Store;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Queue of operations to be performed on a second class collection/map.
 * When we are queueing operations until flush()/commit() they wait in this queue until the moment
 * arrives for flushing to the datastore (and <pre>performAll</pre> is called).
 */
public class OperationQueue<TStore extends Store>
{
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    private List<QueuedOperation<? super TStore>> queuedOperations = new ArrayList<QueuedOperation<? super TStore>>();

    /**
     * Method to add the specified operation to the operation queue.
     * @param oper Operation
     */
    public void enqueue(QueuedOperation<? super TStore> oper)
    {
        queuedOperations.add(oper);
    }

    /**
     * Method to perform all operations in the queue.
     * @param store The backing store
     * @param op ObjectProvider for the owner object.
     * @param fieldName Name of the field for this container in the owner object.
     */
    public void performAll(TStore store, ObjectProvider op, String fieldName)
    {
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("023005", 
                op.toPrintableID(), fieldName));
        }

        List<QueuedOperation<? super TStore>> flushOperations = queuedOperations;
        queuedOperations = new ArrayList<QueuedOperation<? super TStore>>();
        ListIterator<QueuedOperation<? super TStore>> operIter = flushOperations.listIterator();
        while (operIter.hasNext())
        {
            QueuedOperation<? super TStore> oper = operIter.next();
            if (AddOperation.class.isInstance(oper))
            {
                if (operIter.hasNext())
                {
                    // Check the next operation isn't a remove of the same element
                    QueuedOperation<? super TStore> operNext = operIter.next();
                    boolean addThenRemove = false;
                    if (RemoveCollectionOperation.class.isInstance(operNext))
                    {
                        Object value = AddOperation.class.cast(oper).getValue();
                        if (value == RemoveCollectionOperation.class.cast(operNext).getValue())
                        {
                            addThenRemove = true;
                            NucleusLogger.PERSISTENCE.info(
                                "Field " + fieldName + " of " + StringUtils.toJVMIDString(op.getObject()) +
                                " had an add then a remove of element " + 
                                StringUtils.toJVMIDString(value) + " - operations ignored");
                        }
                    }

                    if (!addThenRemove)
                    {
                        // Move back
                        operIter.previous();
                        oper.perform(store, op);
                    }
                }
                else
                {
                    oper.perform(store, op);
                }
            }
            else if (RemoveCollectionOperation.class.isInstance(oper))
            {
                if (operIter.hasNext())
                {
                    // Check the next operation isn't an add of the same element
                    QueuedOperation opNext = operIter.next();
                    boolean removeThenAdd = false;
                    if (AddOperation.class.isInstance(opNext))
                    {
                        Object value = RemoveCollectionOperation.class.cast(oper).getValue();
                        if (value == AddOperation.class.cast(opNext).getValue())
                        {
                            removeThenAdd = true;
                            NucleusLogger.PERSISTENCE.info(
                                "Field " + fieldName + " of " + StringUtils.toJVMIDString(op.getObject()) +
                                " had a remove then add of element " + 
                                StringUtils.toJVMIDString(value) + " - operations ignored");
                        }
                    }

                    if (!removeThenAdd)
                    {
                        // Move back
                        operIter.previous();
                        oper.perform(store, op);
                    }
                }
                else
                {
                    oper.perform(store, op);
                }
            }
            else
            {
                oper.perform(store, op);
            }
        }

        if (!queuedOperations.isEmpty())
        {
            // TODO Log warning that new operations were queued?
            queuedOperations.clear();
        }
    }
}