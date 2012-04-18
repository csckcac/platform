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
 * Representation of the values for identity-type.
 */
public class IdentityType implements Serializable
{
    /** identity-type="application" */
    public static final IdentityType APPLICATION = new IdentityType(1);

    /** identity-type="datastore" */
    public static final IdentityType DATASTORE = new IdentityType(2);

    /** identity-type="nondurable" */
    public static final IdentityType NONDURABLE = new IdentityType(3);

    private final int typeId;
    private IdentityType(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof IdentityType)
        {
            return ((IdentityType)o).typeId == typeId;
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
                return "application";
            case 2 :
                return "datastore";
            case 3 :
                return "nondurable";
        }
        return "";
    }

    /**
     * Accessor to the identity type
     * @return the type
     */
    public int getType()
    {
        return typeId;
    }

    /**
     * Return IdentityType from String.
     * @param value identity-type attribute value
     * @return Instance of IdentityType. If parse failed, return null.
     */
    public static IdentityType getIdentityType(final String value)
    {
        if (value == null)
        {
            return null;
        }
        else if (IdentityType.APPLICATION.toString().equalsIgnoreCase(value))
        {
            return IdentityType.APPLICATION;
        }
        else if (IdentityType.DATASTORE.toString().equalsIgnoreCase(value))
        {
            return IdentityType.DATASTORE;
        }
        else if (IdentityType.NONDURABLE.toString().equalsIgnoreCase(value))
        {
            return IdentityType.NONDURABLE;
        }
        return null;
    }
}