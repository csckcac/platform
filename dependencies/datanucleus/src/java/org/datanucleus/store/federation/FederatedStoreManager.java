/**********************************************************************
Copyright (c) 2008 Erik Bengtson and others. All rights reserved. 
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
2011 Andy Jefferson - completely rewritten to be usable
    ...
**********************************************************************/
package org.datanucleus.store.federation;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.datanucleus.ClassConstants;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;
import org.datanucleus.api.ApiAdapter;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.management.runtime.StoreManagerRuntime;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.IdentityStrategy;
import org.datanucleus.metadata.SequenceMetaData;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.Extent;
import org.datanucleus.store.NucleusConnection;
import org.datanucleus.store.NucleusSequence;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.StorePersistenceHandler;
import org.datanucleus.store.connection.ConnectionManager;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.query.QueryManager;
import org.datanucleus.store.schema.StoreSchemaHandler;
import org.datanucleus.store.valuegenerator.ValueGenerationManager;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.PersistenceUtils;

/**
 * A federated StoreManager orchestrates the persistence/retrieval for multiple datastores.
 * It is responsible for creating the individual StoreManager instances for the datastore(s)
 * that are being federated. Has a "primary" StoreManager where data is stored by default when no metadata
 * is specified for a class, and then has a map of "secondary" StoreManagers keyed by name that can be used
 * for persistence as defined in metadata. When a request comes in to persist some data, this class is
 * responsible for selecting the appropriate StoreManager for persistence. When a request comes in to query
 * some data, this class is responsible for selecting the appropriate StoreManager to query.
 */
public class FederatedStoreManager implements StoreManager
{
    /** Localisation of messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /** Primary StoreManager. */
    StoreManager primaryStoreMgr;

    /** Map of secondary StoreManager keyed by their symbolic name. */
    Map<String, StoreManager> secondaryStoreMgrMap = null;

    final NucleusContext nucleusContext;

    /** Persistence handler. */
    protected StorePersistenceHandler persistenceHandler = null;

    /** Query Manager. Lazy initialised, so use getQueryManager() to access. */
    private QueryManager queryMgr = null;

    public FederatedStoreManager(ClassLoaderResolver clr, NucleusContext nucleusContext)
    {
        this.nucleusContext = nucleusContext;

        // Primary StoreManager
        Map<String, Object> datastoreProps = nucleusContext.getPersistenceConfiguration().getDatastoreProperties();
        this.primaryStoreMgr = NucleusContext.createStoreManagerForProperties(
            nucleusContext.getPersistenceConfiguration().getPersistenceProperties(), 
            datastoreProps, clr, nucleusContext);

        // Correct transaction isolation level to match the datastore capabilities
        String transactionIsolation = nucleusContext.getPersistenceConfiguration().getStringProperty("datanucleus.transactionIsolation");
        if (transactionIsolation != null)
        {
            String reqdIsolation = NucleusContext.getTransactionIsolationForStoreManager(primaryStoreMgr, transactionIsolation);
            if (!transactionIsolation.equalsIgnoreCase(reqdIsolation))
            {
                nucleusContext.getPersistenceConfiguration().setProperty("datanucleus.transactionIsolation", reqdIsolation);
            }
        }

        Set<String> propNamesWithDatastore = nucleusContext.getPersistenceConfiguration().getPropertyNamesWithPrefix("datanucleus.datastore.");
        if (propNamesWithDatastore != null)
        {
            secondaryStoreMgrMap = new HashMap<String, StoreManager>();

            Iterator<String> nameIter = propNamesWithDatastore.iterator();
            while (nameIter.hasNext())
            {
                String datastorePropName = nameIter.next();
                String datastoreName = datastorePropName.substring("datanucleus.datastore.".length());
                String filename = nucleusContext.getPersistenceConfiguration().getStringProperty(datastorePropName);
                Properties fileProps = PersistenceUtils.setPropertiesUsingFile(filename);
                Map<String, Object> storeProps = new HashMap<String, Object>();
                Iterator filePropNamesIter = fileProps.entrySet().iterator();
                while (filePropNamesIter.hasNext())
                {
                    Map.Entry entry = (Map.Entry)filePropNamesIter.next();
                    String filePropName = (String)entry.getKey();
                    storeProps.put(filePropName, entry.getValue());
                }
                StoreManager storeMgr = NucleusContext.createStoreManagerForProperties(
                    nucleusContext.getPersistenceConfiguration().getPersistenceProperties(), storeProps, clr, 
                    nucleusContext);
                NucleusLogger.DATASTORE.info("Created StoreManager of type " + storeMgr.getClass().getName() + 
                    " for datastore " + datastoreName);
                secondaryStoreMgrMap.put(datastoreName, storeMgr);
            }
        }

        this.persistenceHandler = new FederatedPersistenceHandler(this);
    }

    public NucleusContext getNucleusContext()
    {
        return nucleusContext;
    }

    public void close()
    {
        primaryStoreMgr.close();
        primaryStoreMgr = null;

        if (secondaryStoreMgrMap != null)
        {
            Iterator<String> secondaryNameIter = secondaryStoreMgrMap.keySet().iterator();
            while (secondaryNameIter.hasNext())
            {
                String name = secondaryNameIter.next();
                StoreManager secStoreMgr = secondaryStoreMgrMap.get(name);
                secStoreMgr.close();
            }
            secondaryStoreMgrMap.clear();
            secondaryStoreMgrMap = null;
        }

        persistenceHandler.close();

        if (queryMgr != null)
        {
            queryMgr.close();
            queryMgr = null;
        }
    }

    /**
     * Accessor for the StoreManager to use for persisting the specified class.
     * TODO Extend this so that we can persist some objects of one type into one datastore, and other
     * objects of that type into a different datastore.
     * @param cmd Metadata for the class
     * @return The StoreManager to use
     */
    public StoreManager getStoreManagerForClass(AbstractClassMetaData cmd)
    {
        if (cmd.hasExtension("datastore"))
        {
            String datastoreName = cmd.getValueForExtension("datastore");
            if (secondaryStoreMgrMap == null || !secondaryStoreMgrMap.containsKey(datastoreName))
            {
                throw new NucleusUserException("Class " + cmd.getFullClassName() + " specified to persist to datastore " +
                    datastoreName + " yet not defined");
            }
            return secondaryStoreMgrMap.get(datastoreName);
        }
        return primaryStoreMgr;
    }

    /**
     * Accessor for the StoreManager to use for the specified class.
     * @param className Name of the class
     * @param clr ClassLoader resolver
     * @return The StoreManager to use
     */
    public StoreManager getStoreManagerForClass(String className, ClassLoaderResolver clr)
    {
        // TODO Cater for class being persisted to multiple datastores
        AbstractClassMetaData cmd = nucleusContext.getMetaDataManager().getMetaDataForClass(className, clr);
        return getStoreManagerForClass(cmd);
    }

    public void addClass(String className, ClassLoaderResolver clr)
    {
        getStoreManagerForClass(className, clr).addClass(className, clr);
    }

    public void addClasses(String[] classNames, ClassLoaderResolver clr)
    {
        primaryStoreMgr.addClasses(classNames, clr);        
    }

    public ApiAdapter getApiAdapter()
    {
        return nucleusContext.getApiAdapter();
    }

    public String getClassNameForObjectID(Object id, ClassLoaderResolver clr, ExecutionContext ec)
    {
        return primaryStoreMgr.getClassNameForObjectID(id, clr, ec);
    }

    public Date getDatastoreDate()
    {
        return primaryStoreMgr.getDatastoreDate();
    }

    public Extent getExtent(ExecutionContext ec, Class c, boolean subclasses)
    {
        return primaryStoreMgr.getExtent(ec, c, subclasses);
    }

    public NucleusConnection getNucleusConnection(ExecutionContext ec)
    {
        return primaryStoreMgr.getNucleusConnection(ec);
    }

    public NucleusSequence getNucleusSequence(ExecutionContext ec, SequenceMetaData seqmd)
    {
        return primaryStoreMgr.getNucleusSequence(ec, seqmd);
    }

    public StoreSchemaHandler getSchemaHandler()
    {
        return primaryStoreMgr.getSchemaHandler();
    }

    public StorePersistenceHandler getPersistenceHandler()
    {
        return persistenceHandler;
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getQueryManager()
     */
    public QueryManager getQueryManager()
    {
        if (queryMgr == null)
        {
            queryMgr = new FederatedQueryManager(nucleusContext, this);
        }
        return queryMgr;
    }

    public ValueGenerationManager getValueGenerationManager()
    {
        return primaryStoreMgr.getValueGenerationManager();
    }

    public StoreManagerRuntime getRuntimeManager()
    {
        return primaryStoreMgr.getRuntimeManager();
    }

    public String getStoreManagerKey()
    {
        return primaryStoreMgr.getStoreManagerKey();
    }

    public String getQueryCacheKey()
    {
        return primaryStoreMgr.getQueryCacheKey();
    }

    public Object getStrategyValue(ExecutionContext ec, AbstractClassMetaData cmd, int absoluteFieldNumber)
    {
        return primaryStoreMgr.getStrategyValue(ec, cmd, absoluteFieldNumber);
    }

    public HashSet getSubClassesForClass(String className, boolean includeDescendents, ClassLoaderResolver clr)
    {
        return primaryStoreMgr.getSubClassesForClass(className, includeDescendents, clr);
    }

    public boolean isStrategyDatastoreAttributed(IdentityStrategy identityStrategy, boolean datastoreIdentityField)
    {
        return primaryStoreMgr.isStrategyDatastoreAttributed(identityStrategy, datastoreIdentityField);
    }

    public String manageClassForIdentity(Object id, ClassLoaderResolver clr)
    {
        return primaryStoreMgr.manageClassForIdentity(id, clr);
    }

    public boolean managesClass(String className)
    {
        return primaryStoreMgr.managesClass(className);
    }

    public void printInformation(String category, PrintStream ps) throws Exception
    {
        primaryStoreMgr.printInformation(category, ps);        
    }

    public void removeAllClasses(ClassLoaderResolver clr)
    {
        primaryStoreMgr.removeAllClasses(clr);
    }

    public boolean supportsQueryLanguage(String language)
    {
        return primaryStoreMgr.supportsQueryLanguage(language);
    }

    public boolean supportsValueStrategy(String language)
    {
        return primaryStoreMgr.supportsValueStrategy(language);
    }

    public Collection getSupportedOptions()
    {
        return primaryStoreMgr.getSupportedOptions();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionManager()
     */
    public ConnectionManager getConnectionManager()
    {
        return primaryStoreMgr.getConnectionManager();
    }

    public ManagedConnection getConnection(ExecutionContext ec)
    {
        return primaryStoreMgr.getConnection(ec);
    }
    
    public ManagedConnection getConnection(ExecutionContext ec, Map options)
    {
        return primaryStoreMgr.getConnection(ec, options);
    }
    
    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionDriverName()
     */
    public String getConnectionDriverName()
    {
        return primaryStoreMgr.getConnectionDriverName();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionURL()
     */
    public String getConnectionURL()
    {
        return primaryStoreMgr.getConnectionURL();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionUserName()
     */
    public String getConnectionUserName()
    {
        return primaryStoreMgr.getConnectionUserName();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionPassword()
     */
    public String getConnectionPassword()
    {
        return primaryStoreMgr.getConnectionPassword();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionFactory()
     */
    public Object getConnectionFactory()
    {
        return primaryStoreMgr.getConnectionFactory();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionFactory2()
     */
    public Object getConnectionFactory2()
    {
        return primaryStoreMgr.getConnectionFactory2();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionFactory2Name()
     */
    public String getConnectionFactory2Name()
    {
        return primaryStoreMgr.getConnectionFactory2Name();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getConnectionFactoryName()
     */
    public String getConnectionFactoryName()
    {
        return primaryStoreMgr.getConnectionFactoryName();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getProperty(java.lang.String)
     */
    public Object getProperty(String name)
    {
        return primaryStoreMgr.getProperty(name);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#hasProperty(java.lang.String)
     */
    public boolean hasProperty(String name)
    {
        return primaryStoreMgr.hasProperty(name);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getIntProperty(java.lang.String)
     */
    public int getIntProperty(String name)
    {
        return primaryStoreMgr.getIntProperty(name);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getBooleanProperty(java.lang.String)
     */
    public boolean getBooleanProperty(String name)
    {
        return primaryStoreMgr.getBooleanProperty(name);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getBooleanProperty(java.lang.String, boolean)
     */
    public boolean getBooleanProperty(String name, boolean resultIfNotSet)
    {
        return primaryStoreMgr.getBooleanProperty(name, resultIfNotSet);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getBooleanObjectProperty(java.lang.String)
     */
    public Boolean getBooleanObjectProperty(String name)
    {
        return primaryStoreMgr.getBooleanObjectProperty(name);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#getStringProperty(java.lang.String)
     */
    public String getStringProperty(String name)
    {
        return primaryStoreMgr.getStringProperty(name);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#transactionStarted(org.datanucleus.store.ExecutionContext)
     */
    public void transactionStarted(ExecutionContext ec)
    {
        primaryStoreMgr.transactionStarted(ec);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#transactionCommitted(org.datanucleus.store.ExecutionContext)
     */
    public void transactionCommitted(ExecutionContext ec)
    {
        primaryStoreMgr.transactionCommitted(ec);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#transactionRolledBack(org.datanucleus.store.ExecutionContext)
     */
    public void transactionRolledBack(ExecutionContext ec)
    {
        primaryStoreMgr.transactionRolledBack(ec);
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#isAutoCreateTables()
     */
    public boolean isAutoCreateTables()
    {
        return primaryStoreMgr.isAutoCreateTables();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#isAutoCreateConstraints()
     */
    public boolean isAutoCreateConstraints()
    {
        return primaryStoreMgr.isAutoCreateConstraints();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.StoreManager#isAutoCreateColumns()
     */
    public boolean isAutoCreateColumns()
    {
        return primaryStoreMgr.isAutoCreateColumns();
    }
}