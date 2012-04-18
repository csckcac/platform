/**********************************************************************
Copyright (c) 2008 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.properties;

import java.util.TimeZone;

import org.datanucleus.store.connection.ConnectionFactory;

/**
 * Validator for persistence properties used by core.
 */
public class CorePropertyValidator implements PersistencePropertyValidator
{
    /**
     * Validate the specified property.
     * @param name Name of the property
     * @param value Value
     * @return Whether it is valid
     */
    public boolean validate(String name, Object value)
    {
        if (name == null)
        {
            return false;
        }
        else if (name.equalsIgnoreCase("datanucleus.autoStartMechanismMode"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("Quiet") ||
                    strVal.equalsIgnoreCase("Ignored") ||
                    strVal.equalsIgnoreCase("Checked"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.readOnlyDatastoreAction"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("EXCEPTION") ||
                    strVal.equalsIgnoreCase("LOG"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.deletionPolicy"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("JDO2") ||
                    strVal.equalsIgnoreCase("DataNucleus"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.defaultInheritanceStrategy"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("JDO2") ||
                    strVal.equalsIgnoreCase("TABLE_PER_CLASS"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.identifier.case"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("UPPERCASE") ||
                    strVal.equalsIgnoreCase("lowercase") ||
                    strVal.equalsIgnoreCase("PreserveCase"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.valuegeneration.transactionAttribute"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("New") ||
                    strVal.equalsIgnoreCase("UsePM"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.valuegeneration.transactionIsolation") ||
                (name.equalsIgnoreCase("datanucleus.transactionIsolation")))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("none") ||
                    strVal.equalsIgnoreCase("read-committed") ||
                    strVal.equalsIgnoreCase("read-uncommitted") ||
                    strVal.equalsIgnoreCase("repeatable-read") ||
                    strVal.equalsIgnoreCase("serializable") ||
                    strVal.equalsIgnoreCase("snapshot"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.plugin.pluginRegistryBundleCheck"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("EXCEPTION") ||
                    strVal.equalsIgnoreCase("LOG") ||
                    strVal.equalsIgnoreCase("NONE"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.TransactionType"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("RESOURCE_LOCAL") ||
                    strVal.equalsIgnoreCase("JTA"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.ServerTimeZoneID"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;

                String[] availableIDs = TimeZone.getAvailableIDs();
                boolean validZone = false;
                for (int i=0; i<availableIDs.length;i++)
                {
                    if (availableIDs[i].equals(strVal))
                    {
                        return true;
                    }
                }
                if (!validZone)
                {
                    return false;
                }
            }
        }
        else if (name.equalsIgnoreCase(ConnectionFactory.DATANUCLEUS_CONNECTION_RESOURCE_TYPE) 
                || name.equalsIgnoreCase(ConnectionFactory.DATANUCLEUS_CONNECTION2_RESOURCE_TYPE))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("RESOURCE_LOCAL") ||
                    strVal.equalsIgnoreCase("JTA"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.schemaTool.mode"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("create") ||
                    strVal.equalsIgnoreCase("delete") ||
                    strVal.equalsIgnoreCase("validate") ||
                    strVal.equalsIgnoreCase("schemainfo") ||
                    strVal.equalsIgnoreCase("dbinfo"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.detachmentFields"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("load-fields") ||
                    strVal.equalsIgnoreCase("unload-fields") ||
                    strVal.equalsIgnoreCase("load-unload-fields"))
                {
                    return true;
                }
            }
        }
        else if (name.equalsIgnoreCase("datanucleus.detachedState"))
        {
            if (value instanceof String)
            {
                String strVal = (String)value;
                if (strVal.equalsIgnoreCase("all") ||
                    strVal.equalsIgnoreCase("fetch-groups") ||
                    strVal.equalsIgnoreCase("loaded"))
                {
                    return true;
                }
            }
        }
        return false;
    }
}