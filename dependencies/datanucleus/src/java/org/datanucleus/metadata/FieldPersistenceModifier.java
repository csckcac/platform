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
 * Class defining the possibilities for persistence, in terms of the type of
 * persistence, and the types that are capable to be supported. 
 */
public class FieldPersistenceModifier implements Serializable
{
    /** persistence-modifier="persistent" */
    public static final FieldPersistenceModifier PERSISTENT = new FieldPersistenceModifier(1);

    /** persistence-modifier="transactional" */
    public static final FieldPersistenceModifier TRANSACTIONAL = new FieldPersistenceModifier(2);

    /** persistence-modifier="none" */
    public static final FieldPersistenceModifier NONE = new FieldPersistenceModifier(3);

    /** default persistence modifier */
    public static final FieldPersistenceModifier DEFAULT = new FieldPersistenceModifier(-1);

    /** persistent|transactional|none id */
    private final int typeId;

    /**
     * constructor
     * @param i type id
     */
    private FieldPersistenceModifier(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof FieldPersistenceModifier)
        {
            return ((FieldPersistenceModifier)o).typeId == typeId;
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
                return "persistent";
            case 2 :
                return "transactional";
            case 3 :
                return "none";
        }
        return "";
    }

    protected int getType()
    {
        return typeId;
    }

    /**
     * Return FieldPersistenceModifier from String.
     * @param value persistence-modifier attribute value
     * @return Instance of FieldPersistenceModifier. 
     *         If value invalid, return null.
     */
    public static FieldPersistenceModifier getFieldPersistenceModifier(final String value)
    {
        if (value == null)
        {
            return null;
        }
        else if (FieldPersistenceModifier.PERSISTENT.toString().equalsIgnoreCase(value))
        {
            return FieldPersistenceModifier.PERSISTENT;
        }
        else if (FieldPersistenceModifier.TRANSACTIONAL.toString().equalsIgnoreCase(value))
        {
            return FieldPersistenceModifier.TRANSACTIONAL;
        }
        else if (FieldPersistenceModifier.NONE.toString().equalsIgnoreCase(value))
        {
            return FieldPersistenceModifier.NONE;
        }
        return null;
    }
}
