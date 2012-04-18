/**********************************************************************
Copyright (c) 2006 Andy Jefferson and others. All rights reserved.
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
 * Utility class providing enums for the different relation types.
 * TODO Consider adding the other subtypes of relations ... join table, foreign key etc
 */
public class Relation implements Serializable
{
    /** No relation. */
    public static final int NONE = 0;

    /** One to One bidirectional (object reference at one side only). */
    public static final int ONE_TO_ONE_UNI = 1;

    /** One to One bidirectional (object reference at both sides). */
    public static final int ONE_TO_ONE_BI = 2;

    /** One to Many unidirectional (collection/map of object with no reference back). */
    public static final int ONE_TO_MANY_UNI = 3;

    /** One to Many bidirectional (collection/map of object with reference back). */
    public static final int ONE_TO_MANY_BI = 4;

    /** Many to Many bidirectional (collection/map at both sides). */
    public static final int MANY_TO_MANY_BI = 5;

    /** Many to One bidirectional (reference back to a collection of the object). */
    public static final int MANY_TO_ONE_BI = 6;

    /** Many to One unidirectional (reference back to owner of the object when using join table). */
    public static final int MANY_TO_ONE_UNI = 7;

    public static boolean isRelationSingleValued(int type)
    {
        if (type == ONE_TO_ONE_UNI || type == ONE_TO_ONE_BI || type == MANY_TO_ONE_UNI || type == MANY_TO_ONE_BI)
        {
            return true;
        }
        return false;
    }

    public static boolean isRelationMultiValued(int type)
    {
        if (type == ONE_TO_MANY_UNI || type == ONE_TO_MANY_BI || type == MANY_TO_MANY_BI)
        {
            return true;
        }
        return false;
    }

    public static boolean isBidirectional(int type)
    {
        if (type == ONE_TO_ONE_BI || type == ONE_TO_MANY_BI || type == MANY_TO_MANY_BI || type == MANY_TO_ONE_BI)
        {
            return true;
        }
        return false;
    }
}