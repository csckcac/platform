/**********************************************************************
 Copyright (c) 2007 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.metadata;

import java.util.ArrayList;
import java.util.List;

import org.datanucleus.ClassLoaderResolver;

/**
 * Abstract representation of an ORM constraint.
 */
public class AbstractConstraintMetaData extends MetaData
{
    /** the constraint name */
    protected String name;

    /** the constraint table name. Name of the table to which this applies (null implies the enclosing class' table). */
    protected String table;

    /** Contains the metadata for fields/properties. */
    protected AbstractMemberMetaData[] memberMetaData;

    /** Contains the metadata for columns */
    protected ColumnMetaData[] columnMetaData;

    // -------------------------------------------------------------------------
    // Fields below here are used in the metadata parse process where the parser
    // dynamically adds fields/columns as it encounters them in the MetaData files.
    // They are typically cleared at the point of initialise() and not used thereafter.

    /** The fields/properties for this constraint. */
    protected List<AbstractMemberMetaData> members = new ArrayList();

    /** The columns for this constraint. */
    protected List<ColumnMetaData> columns = new ArrayList();

    /**
     * Default constructor. Set fields using setters before populate().
     */
    public AbstractConstraintMetaData()
    {
    }

    /**
     * Copy constructor.
     */
    public AbstractConstraintMetaData(AbstractConstraintMetaData acmd)
    {
        super(null, acmd);
    }

    /**
     * Method to initialise the object, creating internal convenience arrays.
     * Initialise all sub-objects.
     */
    public void initialise(ClassLoaderResolver clr, MetaDataManager mmgr)
    {
        if (isInitialised())
        {
            return;
        }

        // Set up the fieldMetaData
        if (members.size() == 0)
        {
            memberMetaData = null;
        }
        else
        {
            memberMetaData = new AbstractMemberMetaData[members.size()];
            for (int i=0; i<memberMetaData.length; i++)
            {
                memberMetaData[i] = members.get(i);
                memberMetaData[i].initialise(clr, mmgr);
            }
        }

        // Set up the columnMetaData
        if (columns.size() == 0)
        {
            columnMetaData = null;
        }
        else
        {
            columnMetaData = new ColumnMetaData[columns.size()];
            for (int i=0; i<columnMetaData.length; i++)
            {
                columnMetaData[i] = columns.get(i);
                columnMetaData[i].initialise(clr, mmgr);
            }
        }

        // Clear out parsing data
        members.clear();
        members = null;
        columns.clear();
        columns = null;

        setInitialised();
    }

    /**
     * Add a new member that is part of this constraint.
     * @param mmd MetaData for the field/property
     */
    public void addMember(AbstractMemberMetaData mmd)
    {
        members.add(mmd);
        mmd.parent = this;
    }

    /**
     * Method to create a new field, add it, and return it.
     * @return The field metadata
     */
    public FieldMetaData newFieldMetaData(String name)
    {
        FieldMetaData fmd = new FieldMetaData(this, name);
        addMember(fmd);
        return fmd;
    }

    /**
     * Method to create a new property, add it, and return it.
     * @return The property metadata
     */
    public PropertyMetaData newPropertyMetaData(String name)
    {
        PropertyMetaData pmd = new PropertyMetaData(this, name);
        addMember(pmd);
        return pmd;
    }

    /**
     * Add a new ColumnMetaData element
     * @param colmd MetaData for the column
     */
    public void addColumn(ColumnMetaData colmd)
    {
        columns.add(colmd);
        colmd.parent = this;
    }

    /**
     * Method to create a new column, add it, and return it.
     * @return The column metadata
     */
    public ColumnMetaData newColumnMetaData()
    {
        ColumnMetaData colmd = new ColumnMetaData();
        addColumn(colmd);
        return colmd;
    }

    /**
     * Accessor for metadata for all fields/properties that this constraint relates to.
     * @return Returns the memberMetaData.
     */
    public final AbstractMemberMetaData[] getMemberMetaData()
    {
        return memberMetaData;
    }

    /**
     * Accessor for columnMetaData
     * @return Returns the columnMetaData.
     */
    public final ColumnMetaData[] getColumnMetaData()
    {
        return columnMetaData;
    }

    /**
     * Accessor for the number of fields/properties for this constraint.
     * @return Number of fields/properties
     */
    public int getNumberOfMembers()
    {
        if (members != null)
        {
            return members.size();
        }
        else if (memberMetaData != null)
        {
            return memberMetaData.length;
        }
        return 0;
    }

    /**
     * Accessor for the number of columns for this constraint.
     * @return Number of columns
     */
    public int getNumberOfColumns()
    {
        if (columns != null)
        {
            return columns.size();
        }
        else if (columnMetaData != null)
        {
            return columnMetaData.length;
        }
        return 0;
    }
}