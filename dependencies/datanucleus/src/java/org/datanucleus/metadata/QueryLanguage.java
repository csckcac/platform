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
 * Representation of the primary query languages.
 * Other query languages can be supported but this is just for the primary ones as shortcuts.
 */
public class QueryLanguage implements Serializable
{
    /** language="JDOQL" */
    public static final QueryLanguage JDOQL = new QueryLanguage(1);

    /** language="SQL" */
    public static final QueryLanguage SQL = new QueryLanguage(2);

    /** language="JPQL" */
    public static final QueryLanguage JPQL = new QueryLanguage(3);

    private final int typeId;

    private QueryLanguage(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof QueryLanguage)
        {
            return ((QueryLanguage)o).typeId == typeId;
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
                return "JDOQL";
            case 2 :
                return "SQL";
            case 3 :
                return "JPQL";
        }
        return "";
    }

    /**
     * Accessor to the query language type
     * @return the type
     */    
    public int getType()
    {
        return typeId;
    }

    /**
     * Return QueryLanguage from String.
     * @param value identity-type attribute value
     * @return Instance of QueryLanguage. If parse failed, return null.
     */
    public static QueryLanguage getQueryLanguage(final String value)
    {
        if (value == null)
        {
            // Default to JDOQL if nothing passed in
            return QueryLanguage.JDOQL;
        }
        else if (QueryLanguage.JDOQL.toString().equalsIgnoreCase(value))
        {
            return QueryLanguage.JDOQL;
        }
        else if (QueryLanguage.SQL.toString().equalsIgnoreCase(value))
        {
            return QueryLanguage.SQL;
        }
        else if (QueryLanguage.JPQL.toString().equalsIgnoreCase(value))
        {
            return QueryLanguage.JPQL;
        }
        return null;
    }
}