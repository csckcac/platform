/**********************************************************************
Copyright (c) 2007 Guido Anzuoni and others. All rights reserved.
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

import org.datanucleus.NucleusContext;
import org.datanucleus.exceptions.NucleusException;

/**
 * Locator for the TransactionManager in a user-defined JNDI location defined by persistence properties.
 */
public class CustomJNDITransactionManagerLocator extends JNDIBasedTransactionManagerLocator
{
    /** The JNDI Location to use with this locator. */
    protected String jndiLocation;

    /**
     * Constructor.
     */
    public CustomJNDITransactionManagerLocator()
    {
        super();
    }

    /**
     * Constructor.
     * @param nucleusCtx the context this locator operates in
     */
    public CustomJNDITransactionManagerLocator(NucleusContext nucleusCtx)
    {
        super();
        setJNDILocation(nucleusCtx);
    }

    /**
     * Extracts the custom JNDI location from persistence configuration.
     * @param nucleusCtx Context
     */
    private void setJNDILocation(NucleusContext nucleusCtx)
    {
        jndiLocation = nucleusCtx.getPersistenceConfiguration().getStringProperty("datanucleus.jtaJndiLocation");
        if (jndiLocation == null)
        {
            // TODO Localise this
            new NucleusException("NO Custom JNDI Location specified in configuration.").setFatal();
        }
    }

    /**
     * Accessor for the JNDI name to lookup the txn manager under.
     * @return The JNDI name
     */
    public String getJNDIName()
    {
        return jndiLocation;
    }
}