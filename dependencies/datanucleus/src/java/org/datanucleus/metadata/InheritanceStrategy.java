/**********************************************************************
Copyright (c) 2004 Andy Jefferson and others. All rights reserved.
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

import java.io.Serializable;

/**
 * Representation of the values for inheritance "strategy".
 * A brief description of the strategies
 * <ul>
 * <li><b>subclass-table</b> - the fields of this class are persisted into the table(s) of subclasses.</li>
 * <li><b>new-table</b> - the fields of this class are persisted into its own table.</li>
 * <li><b>superclass-table</b> - the fields of this class are persisted into the table of its superclass</li>
 * <li><b>complete-table</b> - the fields of this class and all fields of superclasses are persisted into its own table.</li>
 * </ul>
 */
public class InheritanceStrategy implements Serializable
{
    /** strategy="subclass-table" */
    public static final InheritanceStrategy SUBCLASS_TABLE=new InheritanceStrategy(1);

    /** strategy="new-table" */
    public static final InheritanceStrategy NEW_TABLE=new InheritanceStrategy(2);

    /** strategy="superclass-table" */
    public static final InheritanceStrategy SUPERCLASS_TABLE=new InheritanceStrategy(3);

    /** strategy="complete-table" (specified on root only) */
    public static final InheritanceStrategy COMPLETE_TABLE = new InheritanceStrategy(4);

    /** The type id. */
    private final int typeId;

    /**
     * constructor
     * @param i type id
     */
    private InheritanceStrategy(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof InheritanceStrategy)
        {
            return ((InheritanceStrategy)o).typeId == typeId;
        }
        return false;
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    public String toString()
    {
        switch (typeId)
        {
            case 1 :
                return "subclass-table";
            case 2 :
                return "new-table";
            case 3 :
                return "superclass-table";
            case 4 :
                return "complete-table";
        }
        return "";
    }

    /**
     * Accessor for the type.
     * @return Type
     **/
    public int getType()
    {
        return typeId;
    }

    /**
     * Obtain a InheritanceStrategy for the given name by <code>value</code>
     * @param value the name
     * @return the InheritanceStrategy found or InheritanceStrategy.NEW_TABLE if not found.
     *     Nothing specified returns null
     */
    public static InheritanceStrategy getInheritanceStrategy(final String value)
    {
        if (value == null)
        {
            return null;
        }
        else if (InheritanceStrategy.SUBCLASS_TABLE.toString().equals(value))
        {
            return InheritanceStrategy.SUBCLASS_TABLE;
        }
        else if (InheritanceStrategy.NEW_TABLE.toString().equals(value))
        {
            return InheritanceStrategy.NEW_TABLE;
        }
        else if (InheritanceStrategy.SUPERCLASS_TABLE.toString().equals(value))
        {
            return InheritanceStrategy.SUPERCLASS_TABLE;
        }
        else if (InheritanceStrategy.COMPLETE_TABLE.toString().equals(value))
        {
            return InheritanceStrategy.COMPLETE_TABLE;
        }
        return InheritanceStrategy.NEW_TABLE;
    }
}