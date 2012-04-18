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
package org.datanucleus.jta;

import java.lang.reflect.Constructor;

import javax.transaction.TransactionManager;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;

/**
 * Entry point for locating a JTA TransactionManager.
 */
public class TransactionManagerFinder
{
    /** The NucleusContext. */
    NucleusContext nucleusContext;

    /** List of available locator class names. */
    static String[] locators = null;

    /** Locator class to use (if any) */
    String locator = null;

    /**
     * Constructor.
     * @param ctx Context for persistence
     */
    public TransactionManagerFinder(NucleusContext ctx)
    {
        if (locators == null)
        {
            // Load up all available locators for future reference
            locators = ctx.getPluginManager().getAttributeValuesForExtension("org.datanucleus.jta_locator", null, null,
                "class-name");
        }
        String jtaLocator = ctx.getPersistenceConfiguration().getStringProperty("datanucleus.jtaLocator");
        if (jtaLocator != null)
        {
            // User has selected a locator via persistence properties
            locator = ctx.getPluginManager().getAttributeValueForExtension("org.datanucleus.jta_locator", 
                "name", jtaLocator, "class-name");
        }
        nucleusContext = ctx;
    }

    /**
     * Accessor for the accessible JTA transaction manager.
     * @param clr ClassLoader resolver
     * @return The JTA manager found (if any)
     */
    public TransactionManager getTransactionManager(ClassLoaderResolver clr)
    {
        if (locator != null)
        {
            // User has specified the locator to use
            TransactionManager tm = getTransactionManagerForLocator(clr, locator);
            if (tm != null)
            {
                return tm;
            }
        }
        else
        {
            // User hasnt specified which locator so go through them all
            for (int i=0;i<locators.length;i++)
            {
                TransactionManager tm = getTransactionManagerForLocator(clr, locators[i]);
                if (tm != null)
                {
                    return tm;
                }
            }
        }
        return null;
    }

    /**
     * Convenience method to get the TransactionManager for the specified locator class.
     * @param clr ClassLoader resolver
     * @param locatorClassName Class name for the locator
     * @return The TransactionManager (if found)
     */
    protected TransactionManager getTransactionManagerForLocator(ClassLoaderResolver clr, String locatorClassName)
    {
        try
        {
            Class cls = clr.classForName(locatorClassName);
            if (cls != null)
            {
                // Find the locator
                TransactionManagerLocator loc = null;
                try
                {
                    // Try with constructor taking NucleusContext
                    Class []params = new Class[] {NucleusContext.class};
                    Constructor ctor = cls.getConstructor(params);
                    Object []args = new Object[] {nucleusContext};
                    loc = (TransactionManagerLocator)ctor.newInstance(args);
                }
                catch (NoSuchMethodException nsme)
                {
                    // No constructor taking context so use default constructor
                    loc = (TransactionManagerLocator)cls.newInstance();
                }

                if (loc != null)
                {
                    // Return the actual TransactionManager
                    TransactionManager tm = loc.getTransactionManager(clr);
                    if (tm != null)
                    {
                        return tm;
                    }
                }
            }
        }
        catch (Exception e)
        {
            // Do nothing
        }
        return null;
    }
}