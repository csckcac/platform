/**********************************************************************
Copyright (c) 2004 Erik Bengtson and others. All rights reserved. 
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
2004 Andy Jefferson - toString(), "column", javadocs, initialise()
    ...
**********************************************************************/
package org.datanucleus.metadata;

import org.datanucleus.util.StringUtils;

/**
 * For schema generation, it might be useful to specify that a column or columns
 * be indexed, and to provide the name of the index. For this purpose, an index
 * element can be contained within a field, element, key, value, or join
 * element, and this indicates that the column(s) associated with the referenced
 * element should be indexed. Indexes can also be specified at the class level,
 * by including index elements containing column elements. In this case, the
 * column elements are mapped elsewhere, and the column elements contain only
 * the column name.
 */
public class IndexMetaData extends AbstractConstraintMetaData implements ColumnMetaDataContainer
{
    /**
     * You can use UNIQUE constraints to ensure that no duplicate values are
     * entered in specific columns that do not participate in a primary key.
     * Although both a UNIQUE constraint and a PRIMARY KEY constraint enforce
     * uniqueness, use a UNIQUE constraint instead of a PRIMARY KEY constraint
     * when you want to enforce the uniqueness of:
     * <ul>
     * <li>
     * A column, or combination of columns, that is not the primary key.
     * Multiple UNIQUE constraints can be defined on a table, whereas only one
     * PRIMARY KEY constraint can be defined on a table.
     * </li>
     * <li>
     * A column that allows null values. UNIQUE constraints can be defined on
     * columns that allow null values, whereas PRIMARY KEY constraints can be
     * defined only on columns that do not allow null values.
     * </li>
     * </ul>
     * A UNIQUE constraint can also be referenced by a FOREIGN KEY constraint.
     */
    boolean unique = false;

    /**
     * Constructor to create a copy of the passed metadata.
     * @param imd The metadata to copy
     */
    public IndexMetaData(IndexMetaData imd)
    {
        super(imd);
        this.name = imd.name;
        this.table = imd.table;
        this.unique = imd.unique;
        AbstractMemberMetaData[] apmd = null;
        if (imd.members != null)
        {
            apmd = imd.members.toArray(new AbstractMemberMetaData[imd.members.size()]);
        }
        else
        {
            apmd = imd.memberMetaData;
        }
        if (apmd != null)
        {
            for (int i = 0; i < apmd.length; i++)
            {
                if (apmd[i] instanceof FieldMetaData)
                {
                    addMember(new FieldMetaData(this, apmd[i]));
                }
                else
                {
                    addMember(new PropertyMetaData(this, (PropertyMetaData) apmd[i]));
                }
            }
        }
        ColumnMetaData[] colmd = null;
        if (imd.columns != null)
        {
            colmd = imd.columns.toArray(new ColumnMetaData[imd.columns.size()]);
        }
        else
        {
            colmd = imd.columnMetaData;
        }
        if (colmd != null)
        {
            for (int i = 0; i < colmd.length; i++)
            {
                addColumn(new ColumnMetaData(colmd[i]));
            }
        }
    }

    /**
     * Default constructor. Set fields using setters, before populate().
     */
    public IndexMetaData()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = (StringUtils.isWhitespace(name) ? null : name);
    }

    public String getTable()
    {
        return table;
    }

    public void setTable(String table)
    {
        this.table = (StringUtils.isWhitespace(table) ? null : table);
    }

    public final boolean isUnique()
    {
        return unique;
    }

    public IndexMetaData setUnique(boolean unique)
    {
        this.unique = unique;
        return this;
    }

    public IndexMetaData setUnique(String unique)
    {
        if (!StringUtils.isWhitespace(unique))
        {
            this.unique = Boolean.valueOf(unique);
        }
        return this;
    }

    // -------------------------------- Utilities ------------------------------

    /**
     * Returns a string representation of the object.
     * This can be used as part of a facility to output a MetaData file. 
     * @param prefix prefix string
     * @param indent indent string
     * @return a string representation of the object.
     */
    public String toString(String prefix,String indent)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(prefix).append("<index unique=\"" + unique + "\"");
        if (table != null)
        {
            sb.append(" table=\"" + table + "\"");
        }
        sb.append(name != null ? (" name=\"" + name + "\">\n") : ">\n");

        // Add fields
        if (memberMetaData != null)
        {
            for (int i=0;i<memberMetaData.length;i++)
            {
                sb.append(memberMetaData[i].toString(prefix + indent,indent));
            }
        }

        // Add columns
        if (columnMetaData != null)
        {
            for (int i=0;i<columnMetaData.length;i++)
            {
                sb.append(columnMetaData[i].toString(prefix + indent,indent));
            }
        }

        // Add extensions
        sb.append(super.toString(prefix + indent,indent));

        sb.append(prefix).append("</index>\n");
        return sb.toString();
    }
}