/**********************************************************************
Copyright (c) 2011 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus.cache;

import java.util.Collection;

import org.datanucleus.NucleusContext;
import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;

/**
 * Abstract starting point for a third-party L2 cache plugin.
 * Override the pin/unpin methods if supportable by your plugin.
 */
public abstract class AbstractLevel2Cache implements Level2Cache
{
    /** Localiser for messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance(
        "org.datanucleus.Localisation", org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /** Maximum size of cache (if supported by the plugin). */
    protected int maxSize = -1;

    /** Whether to clear out all objects at close(). */
    protected boolean clearAtClose = true;

    /** Name of the cache to use. */
    protected String cacheName;

    public AbstractLevel2Cache(NucleusContext nucleusCtx)
    {
        PersistenceConfiguration conf = nucleusCtx.getPersistenceConfiguration();
        maxSize = conf.getIntProperty("datanucleus.cache.level2.maxSize");
        clearAtClose = conf.getBooleanProperty("datanucleus.cache.level2.clearAtClose", true);

        cacheName = conf.getStringProperty("datanucleus.cache.level2.cacheName");
        if (cacheName == null)
        {
            NucleusLogger.CACHE.warn("No 'datanucleus.cache.level2.cacheName' specified so using name of 'dataNucleus'");
            cacheName = "dataNucleus";
        }
    }

    /**
     * Accessor for whether the cache is empty
     * @see org.datanucleus.cache.Level2Cache#isEmpty()
     */
    public boolean isEmpty()
    {
        return getSize() == 0;
    }

    public int getNumberOfPinnedObjects()
    {
        throw new UnsupportedOperationException("getNumberOfPinnedObjects() method not supported by this plugin");
    }

    public int getNumberOfUnpinnedObjects()
    {
        throw new UnsupportedOperationException("getNumberOfUnpinnedObjects() method not supported by this plugin");
    }

    public void pin(Object arg0)
    {
        throw new UnsupportedOperationException("pin(Object) method not supported by this plugin");
    }

    public void pinAll(Collection arg0)
    {
        throw new UnsupportedOperationException("pinAll(Collection) method not supported by this plugin");
    }

    public void pinAll(Object[] arg0)
    {
        throw new UnsupportedOperationException("pinAll(Object[]) method not supported by this plugin");
    }

    public void pinAll(Class arg0, boolean arg1)
    {
        throw new UnsupportedOperationException("pinAll(Class,boolean) method not supported by this plugin");
    }

    public void unpin(Object arg0)
    {
        throw new UnsupportedOperationException("unpin(Object) method not supported by this plugin");
    }

    public void unpinAll(Collection arg0)
    {
        throw new UnsupportedOperationException("unpinAll(Collection) method not supported by this plugin");
    }

    public void unpinAll(Object[] arg0)
    {
        throw new UnsupportedOperationException("unpinAll(Object[]) method not supported by this plugin");
    }

    public void unpinAll(Class arg0, boolean arg1)
    {
        throw new UnsupportedOperationException("unpinAll(Class,boolean) method not supported by this plugin");
    }
}