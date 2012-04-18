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
    ...
**********************************************************************/
package org.datanucleus.metadata;

import java.io.Serializable;

/**
 * Three common strategies for versioning instances are supported by standard
 * metadata. These include state-comparison, timestamp, and version-number.
 * <ul>
 * <li><b>state-image</b> involves comparing the values in specific columns to
 * determine if the database row was changed.</li>
 * <li><b>date-time</b> involves comparing the value in a date-time column in the table.
 * The first time in a transaction the row is updated, the timestamp value is
 * updated to the current time.</li>
 * <li><b>version-number</b> involves comparing the value in a numeric column in the table.
 * The first time in a transaction the row is updated, the version-number column
 * value is incremented.</li>
 * </ul>
 */
public class VersionStrategy implements Serializable
{
    /** strategy="none" */
    public static final VersionStrategy NONE = new VersionStrategy(0);

    /** strategy="state-image" */
    public static final VersionStrategy STATE_IMAGE = new VersionStrategy(1);

    /** strategy="date-time" */
    public static final VersionStrategy DATE_TIME = new VersionStrategy(2);

    /** strategy="version-number" */
    public static final VersionStrategy VERSION_NUMBER = new VersionStrategy(3);

    /** state-image|date-time|version-number */
    private final int typeId;

    /**
     * constructor
     * @param i type id
     */
    private VersionStrategy(int i)
    {
        this.typeId = i;
    }

    public int hashCode()
    {
        return typeId;
    }

    public boolean equals(Object o)
    {
        if (o instanceof VersionStrategy)
        {
            return ((VersionStrategy)o).typeId == typeId;
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
            case 0 :
                return "none";
            case 1 :
                return "state-image";
            case 2 :
                return "date-time";
            case 3 :
                return "version-number";
        }
        return "";
    }

    protected int getType()
    {
        return typeId;
    }

    /**
     * Return VersionStrategy from String.
     * @param value strategy attribute value
     * @return Instance of VersionStrategy. 
     *         If value invalid, return null.
     */
    public static VersionStrategy getVersionStrategy(final String value)
    {
        if (value == null)
        {
            return null;
        }
        else if (VersionStrategy.NONE.toString().equalsIgnoreCase(value))
        {
            return VersionStrategy.NONE;
        }
        else if (VersionStrategy.STATE_IMAGE.toString().equalsIgnoreCase(value))
        {
            return VersionStrategy.STATE_IMAGE;
        }
        else if (VersionStrategy.DATE_TIME.toString().equalsIgnoreCase(value))
        {
            return VersionStrategy.DATE_TIME;
        }
        else if (VersionStrategy.VERSION_NUMBER.toString().equalsIgnoreCase(value))
        {
            return VersionStrategy.VERSION_NUMBER;
        }
        return null;
    }
}