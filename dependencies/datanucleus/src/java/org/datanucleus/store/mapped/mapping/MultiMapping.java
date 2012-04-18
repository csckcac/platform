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
package org.datanucleus.store.mapped.mapping;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.api.ApiAdapter;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.identity.OID;
import org.datanucleus.identity.OIDFactory;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.exceptions.NotYetFlushedException;
import org.datanucleus.store.exceptions.NullValueException;
import org.datanucleus.util.NucleusLogger;

/**
 * Mapping to represent multiple mappings within the single overall java type mapping.
 * This mapping can be used to represent, for example,
 * <UL>
 * <LI>a reference field (where there are multiple implementations - of Object or interface)</LI>
 * <LI>an element in a collection and the element has no table of its own, but multiple subclasses</LI>
 * <LI>a FK of a PC field (where there may be multiple fields in the PK of the PC object)</LI>
 * </UL>
 */
public abstract class MultiMapping extends JavaTypeMapping
{
    /** The Java mappings represented by this mapping. */
    protected JavaTypeMapping[] javaTypeMappings = new JavaTypeMapping[0];

    /** Number of datastore mappings - for convenience to improve performance **/
    protected int numberOfDatastoreMappings = 0;

    /**
     * Method to add a Java type mapping for a field
     * @param mapping The mapping to add
     */
    public void addJavaTypeMapping(JavaTypeMapping mapping)
    {
        JavaTypeMapping[] jtm = javaTypeMappings; 
        javaTypeMappings = new JavaTypeMapping[jtm.length+1]; 
        System.arraycopy(jtm, 0, javaTypeMappings, 0, jtm.length);
        javaTypeMappings[jtm.length] = mapping;
    }

    /**
     * Accessor for the Java type mappings
     * @return The Java type mappings
     */
    public JavaTypeMapping[] getJavaTypeMapping()
    {
        return javaTypeMappings;
    }

    /**
     * Accessor for the number of datastore mappings.
     * @return The number of datastore mappings.
     */
    public int getNumberOfDatastoreMappings()
    {
        if (numberOfDatastoreMappings == 0)
        {
            for (int i=0; i<javaTypeMappings.length; i++)
            {
                numberOfDatastoreMappings += javaTypeMappings[i].getNumberOfDatastoreMappings();
            }
        }
        return numberOfDatastoreMappings;
    }

    public DatastoreMapping[] getDatastoreMappings()
    {
        if (datastoreMappings.length == 0)
        {
            datastoreMappings = new DatastoreMapping[getNumberOfDatastoreMappings()];
            int num = 0;
            for (int i=0; i<javaTypeMappings.length; i++)
            {
                for (int j=0;j<javaTypeMappings[i].getNumberOfDatastoreMappings();j++)
                {
                    datastoreMappings[num++] = javaTypeMappings[i].getDatastoreMapping(j);
                }
            }
        }
        return super.getDatastoreMappings();
    }

    /**
     * Accessor for a datastore mapping.
     * @param index The position of the mapping to return
     * @return The datastore mapping
     */
    public DatastoreMapping getDatastoreMapping(int index)
    {
        int currentIndex = 0;
        int numberJavaMappings = javaTypeMappings.length;
        for (int i=0; i<numberJavaMappings; i++)
        {
            int numberDatastoreMappings = javaTypeMappings[i].getNumberOfDatastoreMappings();
            for (int j=0; j<numberDatastoreMappings; j++)
            {
                if (currentIndex == index)
                {
                    return javaTypeMappings[i].getDatastoreMapping(j);
                }
                currentIndex++;
            }
        }

        // TODO Localise this message
        throw new NucleusException("Invalid index " + index + " for DataStoreMapping.").setFatal();
    }

    /**
     * Convenience accessor for the number of the java type mapping where the passed value would be
     * stored. If no suitable mapping is found will return -1. If is a persistent interface then will
     * return -2 meaning persist against *any* mapping
     * @param ec ExecutionContext
     * @param value The value
     * @return The index of javaTypeMappings to use (if any), or -1 (none), or -2 (any)
     */
    public int getMappingNumberForValue(ExecutionContext ec, Object value)
    {
        if (value == null)
        {
            return -1;
        }

        ClassLoaderResolver clr = ec.getClassLoaderResolver();

        // Find the appropriate mapping
        for (int i=0; i<javaTypeMappings.length; i++)
        {
            Class cls = clr.classForName(javaTypeMappings[i].getType());
            if (cls.isAssignableFrom(value.getClass()))
            {
                return i;
            }
        }

        // PERSISTENT INTERFACE : allow for the value (impl) not be directly assignable from the superclass impl
        // e.g If we have interface "Base" with impl "BaseImpl", and sub-interface "Sub1" with impl "Sub1Impl"
        // So if the mapping is of type BaseImpl and the value is Sub1Impl then they don't come up as "assignable"
        // but they are
        Class mappingJavaType = null;
        MetaDataManager mmgr = storeMgr.getNucleusContext().getMetaDataManager();
        boolean isPersistentInterface = mmgr.isPersistentInterface(getType());
        if (isPersistentInterface)
        {
            // Field is declared as a "persistent-interface" type so all impls of that type should match
            mappingJavaType = clr.classForName(getType());
        }
        else if (mmd != null && mmd.getFieldTypes() != null && mmd.getFieldTypes().length == 1)
        {
            isPersistentInterface = mmgr.isPersistentInterface(mmd.getFieldTypes()[0]);
            if (isPersistentInterface)
            {
                // Field is declared as interface and accepts "persistent-interface" value, so all impls should match
                mappingJavaType = clr.classForName(mmd.getFieldTypes()[0]);
            }
        }
        if (mappingJavaType != null && mappingJavaType.isAssignableFrom(value.getClass()))
        {
            return -2; // Persistent interface persistable in all mappings. Should only be 1 anyway
        }

        return -1;
    }

    /**
     * Method to set the parameters in the PreparedStatement with the fields of
     * this object.
     * @param ec execution context
     * @param ps The PreparedStatement
     * @param pos The parameter positions
     * @param value The object to populate the statement with
     * @throws NotYetFlushedException Thrown if the object is not yet flushed to the datastore
     */
    public void setObject(ExecutionContext ec, Object ps, int[] pos, Object value)
    {
        setObject(ec, ps, pos, value, null, -1);
    }

    /**
     * Sets the specified positions in the PreparedStatement associated with this field, and value.
     * @param ec execution context
     * @param ps a datastore object that executes statements in the database
     * @param pos The position(s) of the PreparedStatement to populate
     * @param value the value stored in this field
     * @param ownerSM the owner StateManager
     * @param ownerFieldNumber the owner absolute field number
     */
    public void setObject(ExecutionContext ec, Object ps, int[] pos, Object value, ObjectProvider ownerSM, int ownerFieldNumber)
    {
        // Make sure that this field has a sub-mapping appropriate for the specified value
        int javaTypeMappingNumber = getMappingNumberForValue(ec, value);
        if (value != null && javaTypeMappingNumber == -1)
        {
            // as required by the JDO spec, a ClassCastException is thrown since not valid implementation
            // TODO Change this to a multiple field mapping localised message
            throw new ClassCastException(LOCALISER.msg("041044",
                mmd != null ? mmd.getFullFieldName() : "", getType(), value.getClass().getName()));
        }

        if (value != null)
        {
            ApiAdapter api = ec.getApiAdapter();
            ClassLoaderResolver clr = ec.getClassLoaderResolver();

            // Make sure the value is persisted if it is persistable in its own right
            if (!ec.isInserting(value))
            {
                // Object either already exists, or is not yet being inserted.
                Object id = api.getIdForObject(value);

                // Check if the PersistenceCapable exists in this datastore
                boolean requiresPersisting = false;
                if (ec.getApiAdapter().isDetached(value) && ownerSM != null)
                {
                    // Detached object that needs attaching (or persisting if detached from a different datastore)
                    requiresPersisting = true;
                }
                else if (id == null)
                {
                    // Transient object, so we need to persist it
                    requiresPersisting = true;
                }
                else
                {
                    ExecutionContext valueEC = api.getExecutionContext(value);
                    if (valueEC != null && ec != valueEC)
                    {
                        throw new NucleusUserException(LOCALISER.msg("041015"), id);
                    }
                }

                if (requiresPersisting)
                {
                    // The object is either not yet persistent or is detached and so needs attaching
                    Object pcNew = ec.persistObjectInternal(value, null, -1, ObjectProvider.PC);
                    ec.flushInternal(false);
                    id = api.getIdForObject(pcNew);
                    if (ec.getApiAdapter().isDetached(value) && ownerSM != null)
                    {
                        // Update any detached reference to refer to the attached variant
                        ownerSM.replaceFieldMakeDirty(ownerFieldNumber, pcNew);
                        int relationType = mmd.getRelationType(clr);
                        if (relationType == Relation.ONE_TO_ONE_BI)
                        {
                            ObjectProvider relatedSM = ec.findObjectProvider(pcNew);
                            AbstractMemberMetaData[] relatedMmds = mmd.getRelatedMemberMetaData(clr);
                            // TODO Allow for multiple related fields
                            relatedSM.replaceFieldMakeDirty(relatedMmds[0].getAbsoluteFieldNumber(), ownerSM.getObject());
                        }
                        else if (relationType == Relation.MANY_TO_ONE_BI)
                        {
                            // TODO Update the container element with the attached variant
                            if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                            {
                                NucleusLogger.PERSISTENCE.debug("PCMapping.setObject : object " + ownerSM.getInternalObjectId() + 
                                    " has field " + ownerFieldNumber +
                                    " that is 1-N bidirectional - should really update the reference in the relation. Not yet supported");
                            }
                        }
                    }
                }

                if (getNumberOfDatastoreMappings() <= 0)
                {
                    // If the field doesn't map to any datastore fields, omit the set process
                    return;
                }
            }
        }

        if (pos == null)
        {
            return;
        }

        // Obtain StateManager for the value (if persistable)
        ObjectProvider sm = null;
        if (value != null)
        {
            sm = ec.findObjectProvider(value);
        }

        try
        {
            if (sm != null)
            {
                sm.setStoringPC();
            }

            int n = 0;
            NotYetFlushedException notYetFlushed = null;
            for (int i=0; i<javaTypeMappings.length; i++)
            {
                // Set the PreparedStatement positions for this implementation mapping
                int[] posMapping;
                if (n >= pos.length)
                {
                    n = 0; // store all implementations to the same columns, so we reset the index
                }
                if (javaTypeMappings[i].getReferenceMapping() != null)
                {
                    posMapping = new int[javaTypeMappings[i].getReferenceMapping().getNumberOfDatastoreMappings()];
                }
                else
                {
                    posMapping = new int[javaTypeMappings[i].getNumberOfDatastoreMappings()];
                }
                for (int j=0; j<posMapping.length; j++)
                {
                    posMapping[j] = pos[n++];
                }

                try
                {
                    if (javaTypeMappingNumber == -2 || (value != null && javaTypeMappingNumber == i))
                    {
                        // This mapping is where the value is to be stored, or using persistent interfaces
                        javaTypeMappings[i].setObject(ec, ps, posMapping, value);
                    }
                    else
                    {
                        // Set null for this mapping, since the value is null or is for something else
                        javaTypeMappings[i].setObject(ec, ps, posMapping, null);
                    }
                }
                catch (NotYetFlushedException e)
                {
                    notYetFlushed = e;
                }
            }
            if (notYetFlushed != null)
            {
                throw notYetFlushed;
            }
        }
        finally
        {
            if (sm != null)
            {
                sm.unsetStoringPC();
            }
        }
    }

    /**
     * Method to retrieve an object of this type from the ResultSet.
     * @param ec execution context
     * @param rs The ResultSet
     * @param pos The parameter positions
     * @return The object
     */
    public Object getObject(ExecutionContext ec, final Object rs, int[] pos)
    {
        // Go through the possible types for this field and find a non-null value (if there is one)
        int n = 0;
        for (int i=0; i<javaTypeMappings.length; i++)
        {
            int[] posMapping;
            if (n >= pos.length)
            {
                //this means we store all implementations to the same columns, so we reset the index
                n = 0;
            }

            if (javaTypeMappings[i].getReferenceMapping() != null)
            {
                posMapping = new int[javaTypeMappings[i].getReferenceMapping().getNumberOfDatastoreMappings()];
            }
            else
            {
                posMapping = new int[javaTypeMappings[i].getNumberOfDatastoreMappings()];
            }
            for (int j=0; j<posMapping.length; j++)
            {
                posMapping[j] = pos[n++];
            }

            Object value = null;
            try
            {
                // Retrieve the value (PC object) for this mappings' object
                value = javaTypeMappings[i].getObject(ec, rs, posMapping);
            }
            catch (NullValueException e)
            {
                // expected if implementation object is null and has primitive
                // fields in the primary key
            }
            catch (NucleusObjectNotFoundException onfe)
            {
                // expected, will try next implementation
            }
            if (value != null)
            {
                if (value instanceof OID)
                {
                    // What situation is this catering for exactly ?
                    String className;
                    if (javaTypeMappings[i].getReferenceMapping() != null)
                    {
                        className = javaTypeMappings[i].getReferenceMapping().getDatastoreMapping(0).getDatastoreField().getStoredJavaType();
                    }
                    else
                    {
                        className = javaTypeMappings[i].getDatastoreMapping(0).getDatastoreField().getStoredJavaType();
                    }
                    value = OIDFactory.getInstance(ec.getNucleusContext(), className, ((OID)value).getKeyValue());
                    return ec.findObject(value, false, true, null);
                }
                else if (ec.getClassLoaderResolver().classForName(getType()).isAssignableFrom(value.getClass()))               	
                {
                    return value;
                }
            }
        }
        return null;
    }
}