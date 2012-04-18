/**********************************************************************
Copyright (c) 2010 Andy Jefferson and others. All rights reserved.
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
package org.datanucleus;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import javax.jdo.spi.JDOImplHelper;

import org.datanucleus.api.ApiAdapter;
import org.datanucleus.api.ApiAdapterFactory;
import org.datanucleus.cache.Level2Cache;
import org.datanucleus.cache.NullLevel2Cache;
import org.datanucleus.exceptions.ClassNotResolvedException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.exceptions.TransactionIsolationNotSupportedException;
import org.datanucleus.identity.IdentityKeyTranslator;
import org.datanucleus.identity.IdentityStringTranslator;
import org.datanucleus.jta.TransactionManagerFinder;
import org.datanucleus.management.ManagementManager;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.Extension;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.state.JDOStateManagerImpl;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.federation.FederatedStoreManager;
import org.datanucleus.store.types.TypeManager;
import org.datanucleus.transaction.NucleusTransactionException;
import org.datanucleus.transaction.TransactionManager;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Representation of the context being run within DataNucleus. Provides a series of services and can be used
 * by JDO persistence, JPA persistence, JDO enhancement, JPA enhancement, amongst other things,
 */
public class NucleusContext
{
    /** Localisation of messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        ClassConstants.NUCLEUS_CONTEXT_LOADER);

    public enum ContextType
    {
        PERSISTENCE,
        ENHANCEMENT
    }

    private final ContextType type;

    /** Manager for the datastore used by this PMF/EMF. */
    private StoreManager storeMgr = null;

    /** MetaDataManager for handling the MetaData for this PMF/EMF. */
    private MetaDataManager metaDataManager = null;

    /** Flag defining if this is running within the JDO JCA adaptor. */
    private boolean jca = false;

    /** The PersistenceConfiguration defining features of the persistence process. */
    private final PersistenceConfiguration config;

    /** Manager for Plug-ins. */
    private final PluginManager pluginManager;

    /** Manager for types and JavaTypeMappings **/
    private final TypeManager typeManager;

    /** ApiAdapter used by the context. **/
    private final ApiAdapter apiAdapter;

    /** Name of the class providing the ClassLoaderResolver. */
    private final String classLoaderResolverClassName;

    /** Level 2 Cache, caching across ObjectManagers. */
    private Level2Cache cache;

    /** Transaction Manager. */
    private TransactionManager txManager = null;

    /** JTA Transaction Manager (if using JTA). */
    private javax.transaction.TransactionManager jtaTxManager = null;

    /** Map of the ClassLoaderResolver, keyed by the clr class and the primaryLoader name. */
    private Map<String, ClassLoaderResolver> classLoaderResolverMap = new HashMap<String, ClassLoaderResolver>();

    /** Manager for JMX features. */
    private ManagementManager jmxManager = null;

    /** Class to use for datastore-identity. */
    private Class datastoreIdentityClass = null;

    /** Identity string translator (if any). */
    private IdentityStringTranslator idStringTranslator = null;

    /** Flag for whether we have initialised the id string translator. */
    private boolean idStringTranslatorInit = false;

    /** Identity key translator (if any). */
    private IdentityKeyTranslator idKeyTranslator = null;

    /** Flag for whether we have initialised the id key translator. */
    private boolean idKeyTranslatorInit = false;

    /** ImplementationCreator for any persistent interfaces. */
    private ImplementationCreator implCreator;

    /** Flag for whether we have initialised the implementation creator. */
    private boolean implCreatorInit = false;

    private List<ExecutionContext.LifecycleListener> objectManagerListeners = new ArrayList();

    /** Manager for dynamic fetch groups defined on the PMF/EMF. */
    private FetchGroupManager fetchGrpMgr;

    public static final Set<String> STARTUP_PROPERTIES = new HashSet<String>();
    static
    {
        STARTUP_PROPERTIES.add("datanucleus.plugin.pluginRegistryClassName");
        STARTUP_PROPERTIES.add("datanucleus.plugin.pluginRegistryBundleCheck");
        STARTUP_PROPERTIES.add("datanucleus.plugin.allowUserBundles");
        STARTUP_PROPERTIES.add("datanucleus.plugin.validatePlugins");
        STARTUP_PROPERTIES.add("datanucleus.classLoaderResolverName");
        STARTUP_PROPERTIES.add("datanucleus.persistenceXmlFilename");
        STARTUP_PROPERTIES.add("datanucleus.primaryClassLoader");
    };

    /**
     * Constructor for a persistence context.
     * @param apiName Name of the API that we need a context for (JDO, JPA, etc)
     * @param startupProps Any properties that could define behaviour of this context
     *                These could be plugin registry properties, or class loading properties
     */
    public NucleusContext(String apiName, Map startupProps)
    {
        this(apiName, ContextType.PERSISTENCE, startupProps);
    }

    /**
     * Constructor for the context.
     * @param apiName Name of the API that we need a context for (JDO, JPA, etc)
     * @param type The type of context required (persistence, enhancement)
     * @param startupProps Any properties that could define behaviour of this context
     *                These could be plugin registry properties, or class loading properties
     */
    public NucleusContext(String apiName, ContextType type, Map startupProps)
    {
        this.type = type;
        this.config = new PersistenceConfiguration();

        // Use JDOClassLoaderResolver here since we need the plugin mechanism before being able to create our specified CLR
        ClassLoaderResolver clr = new JDOClassLoaderResolver(this.getClass().getClassLoader());
        if (startupProps != null)
        {
            clr.registerUserClassLoader((ClassLoader)startupProps.get("datanucleus.primaryClassLoader"));
        }

        // Plugin management
        Properties pluginProps = new Properties();
        String registryClassName = null;
        if (startupProps != null)
        {
            registryClassName = (String)startupProps.get("datanucleus.plugin.pluginRegistryClassName");

            if (startupProps.containsKey("datanucleus.plugin.pluginRegistryBundleCheck"))
            {
                pluginProps.setProperty("bundle-check-action",
                    (String)startupProps.get("datanucleus.plugin.pluginRegistryBundleCheck"));
            }
            if (startupProps.containsKey("datanucleus.plugin.allowUserBundles"))
            {
                pluginProps.setProperty("allow-user-bundles",
                    (String)startupProps.get("datanucleus.plugin.allowUserBundles"));
            }
            if (startupProps.containsKey("datanucleus.plugin.validatePlugins"))
            {
                pluginProps.setProperty("validate-plugins",
                    (String)startupProps.get("datanucleus.plugin.validatePlugins"));
            }
        }
        this.pluginManager = new PluginManager(registryClassName, clr, pluginProps);

        // Load up any default properties from the plugins, and superimpose startup props
        config.setDefaultProperties(pluginManager);
        if (startupProps != null && !startupProps.isEmpty())
        {
            config.setPersistenceProperties(startupProps);
        }

        // Set the name of class loader resolver
        String clrName = config.getStringProperty("datanucleus.classLoaderResolverName");
        classLoaderResolverClassName = pluginManager.getAttributeValueForExtension(
            "org.datanucleus.classloader_resolver", "name", clrName, "class-name");
        if (classLoaderResolverClassName == null)
        {
            // User has specified a classloader_resolver plugin that has not registered
            throw new NucleusUserException(LOCALISER.msg("001001", clrName)).setFatal();
        }

        // Initialise support for API, java types over the top of plugin support
        this.apiAdapter = ApiAdapterFactory.getInstance().getApiAdapter(apiName, pluginManager);
        config.setDefaultProperties(apiAdapter.getDefaultFactoryProperties());
        this.typeManager = new TypeManager(apiAdapter, this.pluginManager, getClassLoaderResolver(null));

        if (type == ContextType.PERSISTENCE)
        {
            // Register the StateManager class with JDOImplHelper for security
            AccessController.doPrivileged(new PrivilegedAction() 
            {
                public Object run()
                {
                    JDOImplHelper.registerAuthorizedStateManagerClass(JDOStateManagerImpl.class);
                    return null;
                }
            });
        }
    }

    /**
     * Method to initialise the context for use.
     * This creates the required StoreManager(s).
     */
    public void initialise()
    {
        // Set user classloader
        ClassLoaderResolver clr = getClassLoaderResolver(null);
        clr.registerUserClassLoader((ClassLoader)getPersistenceConfiguration().getProperty("datanucleus.primaryClassLoader"));

        Set<String> propNamesWithDatastore = getPersistenceConfiguration().getPropertyNamesWithPrefix("datanucleus.datastore.");
        if (propNamesWithDatastore == null)
        {
            // Find the StoreManager using the persistence property if specified
            Map<String, Object> datastoreProps = getPersistenceConfiguration().getDatastoreProperties();
            StoreManager storeMgr = createStoreManagerForProperties(
                getPersistenceConfiguration().getPersistenceProperties(), datastoreProps, clr, this);
            setStoreManager(storeMgr);

            // Make sure the isolation level is valid for this StoreManager and correct if necessary
            String transactionIsolation = config.getStringProperty("datanucleus.transactionIsolation");
            if (transactionIsolation != null)
            {
                String reqdIsolation = getTransactionIsolationForStoreManager(storeMgr, transactionIsolation);
                if (!transactionIsolation.equalsIgnoreCase(reqdIsolation))
                {
                    config.setProperty("datanucleus.transactionIsolation", reqdIsolation);
                }
            }
        }
        else
        {
            NucleusLogger.DATASTORE.info("Creating FederatedStoreManager to handle federation of primary StoreManager and " + propNamesWithDatastore.size() + " secondary datastores");
            FederatedStoreManager fedStoreMgr = new FederatedStoreManager(clr, this);
            setStoreManager(fedStoreMgr);
        }

        // Load up any persistence-unit classes into the StoreManager
        String puName = config.getStringProperty("datanucleus.PersistenceUnitName");
        if (puName != null)
        {
            boolean loadClasses = config.getBooleanProperty("datanucleus.persistenceUnitLoadClasses");
            if (loadClasses)
            {
                // Load all classes into StoreManager so it knows about them
                Collection<String> loadedClasses = getMetaDataManager().getClassesWithMetaData();
                storeMgr.addClasses(loadedClasses.toArray(new String[loadedClasses.size()]), clr);
            }
        }

        logConfiguration();
    }

    /**
     * Method to return the transaction isolation level that will be used for the provided StoreManager
     * bearing in mind the specified level the user requested.
     * @param storeMgr The Store Manager
     * @param transactionIsolation Requested isolation level
     * @return Isolation level to use
     * @throws TransactionIsolationNotSupportedException When no suitable level available given the requested level
     */
    public static String getTransactionIsolationForStoreManager(StoreManager storeMgr, String transactionIsolation)
    {
        if (transactionIsolation != null)
        {
            // Transaction isolation has been specified and we need to provide at least this level
            // Order of priority is :-
            // read-uncommitted (lowest), read-committed, repeatable-read, serializable (highest)
            Collection srmOptions = storeMgr.getSupportedOptions();
            if (!srmOptions.contains("TransactionIsolationLevel." + transactionIsolation))
            {
                // Requested transaction isolation isn't supported by datastore so check for higher
                if (transactionIsolation.equals("read-uncommitted"))
                {
                    if (srmOptions.contains("TransactionIsolationLevel.read-committed"))
                    {
                        return "read-committed";
                    }
                    else if (srmOptions.contains("TransactionIsolationLevel.repeatable-read"))
                    {
                        return "repeatable-read";
                    }
                    else if (srmOptions.contains("TransactionIsolationLevel.serializable"))
                    {
                        return "serializable";
                    }
                }
                else if (transactionIsolation.equals("read-committed"))
                {
                    if (srmOptions.contains("TransactionIsolationLevel.repeatable-read"))
                    {
                        return "repeatable-read";
                    }
                    else if (srmOptions.contains("TransactionIsolationLevel.serializable"))
                    {
                        return "serializable";
                    }
                }
                else if (transactionIsolation.equals("repeatable-read"))
                {
                    if (srmOptions.contains("TransactionIsolationLevel.serializable"))
                    {
                        return "serializable";
                    }
                }
                else
                {
                    throw new TransactionIsolationNotSupportedException(transactionIsolation);
                }
            }
        }
        return transactionIsolation;
    }

    /**
     * Method to create a StoreManager based on the specified properties passed in.
     * @param props The overall persistence properties
     * @param datastoreProps Persistence properties to apply to the datastore
     * @param clr ClassLoader resolver
     * @return The StoreManager
     * @throws NucleusUserException if impossible to create the StoreManager (not in CLASSPATH?, invalid definition?)
     */
    public static StoreManager createStoreManagerForProperties(Map<String, Object> props,
            Map<String, Object> datastoreProps, ClassLoaderResolver clr,
            NucleusContext nucCtx)
    {
        Extension[] exts = nucCtx.getPluginManager().getExtensionPoint("org.datanucleus.store_manager").getExtensions();
        Class[] ctrArgTypes = new Class[] {ClassLoaderResolver.class, NucleusContext.class, Map.class};
        Object[] ctrArgs = new Object[] {clr, nucCtx, datastoreProps};

        StoreManager storeMgr = null;

        String storeManagerType = (String) props.get("datanucleus.storemanagertype");
        if (storeManagerType != null)
        {
            // User defined the store manager type, so find the appropriate plugin
            for (int e=0; storeMgr == null && e<exts.length; e++)
            {
                ConfigurationElement[] confElm = exts[e].getConfigurationElements();
                for (int c=0; storeMgr == null && c<confElm.length; c++)
                {
                    String key = confElm[c].getAttribute("key");
                    if (key.equalsIgnoreCase(storeManagerType))
                    {
                        try
                        {
                            storeMgr = (StoreManager)nucCtx.getPluginManager().createExecutableExtension(
                                "org.datanucleus.store_manager", "key", storeManagerType, 
                                "class-name", ctrArgTypes, ctrArgs);
                        }
                        catch (InvocationTargetException ex)
                        {
                            Throwable t = ex.getTargetException();
                            if (t instanceof RuntimeException)
                            {
                                throw (RuntimeException) t;
                            }
                            else if (t instanceof Error)
                            {
                                throw (Error) t;
                            }
                            else
                            {
                                throw new NucleusException(t.getMessage(), t).setFatal();
                            }
                        }
                        catch (Exception ex)
                        {
                            throw new NucleusException(ex.getMessage(), ex).setFatal();
                        }
                    }
                }
            }
            if (storeMgr == null)
            {
                // No StoreManager of the specified type exists in the CLASSPATH!
                throw new NucleusUserException(LOCALISER.msg("008004", storeManagerType)).setFatal();
            }
        }

        if (storeMgr == null)
        {
            // Try using the URL of the data source
            String url = (String) props.get("datanucleus.connectionurl");
            if (url != null)
            {
                int idx = url.indexOf(':');
                if (idx > -1)
                {
                    url = url.substring(0, idx);
                }
            }

            for (int e=0; storeMgr == null && e<exts.length; e++)
            {
                ConfigurationElement[] confElm = exts[e].getConfigurationElements();
                for (int c=0; storeMgr == null && c<confElm.length; c++)
                {
                    String urlKey = confElm[c].getAttribute("url-key");
                    if (url == null || urlKey.equalsIgnoreCase(url))
                    {
                        // Either no URL, or url defined so take this StoreManager
                        try
                        {
                            storeMgr = (StoreManager)nucCtx.getPluginManager().createExecutableExtension(
                                "org.datanucleus.store_manager", "url-key", url == null ? urlKey : url, 
                                "class-name", ctrArgTypes, ctrArgs);
                        }
                        catch (InvocationTargetException ex)
                        {
                            Throwable t = ex.getTargetException();
                            if (t instanceof RuntimeException)
                            {
                                throw (RuntimeException) t;
                            }
                            else if (t instanceof Error)
                            {
                                throw (Error) t;
                            }
                            else
                            {
                                throw new NucleusException(t.getMessage(), t).setFatal();
                            }
                        }
                        catch (Exception ex)
                        {
                            throw new NucleusException(ex.getMessage(), ex).setFatal();
                        }
                    }
                }
            }

            if (storeMgr == null)
            {
                throw new NucleusUserException(LOCALISER.msg("008004", url)).setFatal();
            }
        }

        return storeMgr;
    }

    /**
     * Clear out resources
     */
    public synchronized void close()
    {
        // Clear out all fetch groups
        if (fetchGrpMgr != null)
        {
            fetchGrpMgr.clearFetchGroups();
        }

        if (storeMgr != null)
        {
            storeMgr.close();
            storeMgr = null;
        }

        if (metaDataManager != null)
        {
            metaDataManager.close();
            metaDataManager = null;
        }

        if (jmxManager != null)
        {
            jmxManager.close();
            jmxManager = null;
        }

        if (cache != null)
        {
            // Close the L2 Cache
            cache.close();
            NucleusLogger.CACHE.info(LOCALISER.msg("004009"));
        }

        classLoaderResolverMap.clear();
        classLoaderResolverMap = null;

        datastoreIdentityClass = null;
    }

    /**
     * Accessor for the type of this context (persistence, enhancer etc).
     * @return The type
     */
    public ContextType getType()
    {
        return type;
    }

    /**
     * Accessor for the ApiAdapter
     * @return the ApiAdapter
     */
    public ApiAdapter getApiAdapter()
    {
        return apiAdapter;
    }

    /**
     * Accessor for the name of the API (JDO, JPA, etc).
     * @return the api
     */
    public String getApiName()
    {
        return apiAdapter.getName();
    }

    /**
     * Accessor for the persistence configuration.
     * @return Returns the persistence configuration.
     */
    public PersistenceConfiguration getPersistenceConfiguration()
    {
        return config;
    }
    
    /**
     * Accessor for the Plugin Manager
     * @return the PluginManager
     */
    public PluginManager getPluginManager()
    {
        return pluginManager;
    }
    
    /**
     * Accessor for the Type Manager
     * @return the TypeManager
     */
    public TypeManager getTypeManager()
    {
        return typeManager;
    }

    /**
     * Method to log the configuration of this context.
     */
    protected void logConfiguration()
    {
        // Log the Factory configuration
        NucleusLogger.PERSISTENCE.info("================= Persistence Configuration ===============");
        NucleusLogger.PERSISTENCE.info(LOCALISER.msg("008000", "DataNucleus", 
            pluginManager.getVersionForBundle("org.datanucleus")));
        NucleusLogger.PERSISTENCE.info(LOCALISER.msg("008001", 
            config.getStringProperty("datanucleus.ConnectionURL"), 
            config.getStringProperty("datanucleus.ConnectionDriverName"), 
            config.getStringProperty("datanucleus.ConnectionUserName")));
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug("JDK : " + System.getProperty("java.version") + " on " + System.getProperty("os.name"));
            NucleusLogger.PERSISTENCE.debug("Persistence API : " + apiAdapter.getName());
            NucleusLogger.PERSISTENCE.debug("Plugin Registry : " + pluginManager.getRegistryClassName());
            if (config.hasPropertyNotNull("datanucleus.PersistenceUnitName"))
            {
                NucleusLogger.PERSISTENCE.debug("Persistence-Unit : " + config.getStringProperty("datanucleus.PersistenceUnitName"));
            }

            String timeZoneID = config.getStringProperty("datanucleus.ServerTimeZoneID");
            if (timeZoneID == null)
            {
                timeZoneID = TimeZone.getDefault().getID();
            }
            NucleusLogger.PERSISTENCE.debug("Standard Options : " + 
                (config.getBooleanProperty("datanucleus.Multithreaded") ? "pm-multithreaded" : "pm-singlethreaded") +
                (config.getBooleanProperty("datanucleus.RetainValues") ? ", retain-values" : "") +
                (config.getBooleanProperty("datanucleus.RestoreValues") ? ", restore-values" : "") +
                (config.getBooleanProperty("datanucleus.NontransactionalRead") ? ", nontransactional-read" : "") +
                (config.getBooleanProperty("datanucleus.NontransactionalWrite") ? ", nontransactional-write" : "") +
                (config.getBooleanProperty("datanucleus.IgnoreCache") ? ", ignoreCache" : "") +
                ", serverTimeZone=" + timeZoneID);
            NucleusLogger.PERSISTENCE.debug("Persistence Options :" +
                (config.getBooleanProperty("datanucleus.persistenceByReachabilityAtCommit") ? " reachability-at-commit" : "") +
                (config.getBooleanProperty("datanucleus.DetachAllOnCommit") ? " detach-all-on-commit" : "") +
                (config.getBooleanProperty("datanucleus.DetachAllOnRollback") ? " detach-all-on-rollback" : "") +
                (config.getBooleanProperty("datanucleus.DetachOnClose") ? " detach-on-close" : "") +
                (config.getBooleanProperty("datanucleus.manageRelationships") ? 
                    (config.getBooleanProperty("datanucleus.manageRelationshipsChecks") ? " managed-relations(checked)" : "managed-relations(unchecked)") : "") +
                " deletion-policy=" + config.getStringProperty("datanucleus.deletionPolicy"));
            NucleusLogger.PERSISTENCE.debug("Transactions : type=" + config.getStringProperty("datanucleus.TransactionType") +
                " mode=" + (config.getBooleanProperty("datanucleus.Optimistic") ? "optimistic" : "datastore") +
                " isolation=" + config.getStringProperty("datanucleus.transactionIsolation"));
            NucleusLogger.PERSISTENCE.debug("Value Generation :" +
                " txn-isolation=" + config.getStringProperty("datanucleus.valuegeneration.transactionIsolation") +
                " connection=" + (config.getStringProperty("datanucleus.valuegeneration.transactionAttribute").equalsIgnoreCase("New") ? "New" : "Existing"));
            Object primCL = config.getProperty("datanucleus.primaryClassLoader");
            NucleusLogger.PERSISTENCE.debug("ClassLoading : " + config.getStringProperty("datanucleus.classLoaderResolverName") +
                (primCL != null ? ("primary=" + primCL) : ""));
            NucleusLogger.PERSISTENCE.debug("Cache : Level1 (" + config.getStringProperty("datanucleus.cache.level1.type") + ")" +
                ", Level2 (" + config.getStringProperty("datanucleus.cache.level2.type") + ")" +
                ", QueryResults (" + config.getStringProperty("datanucleus.cache.queryResults.type") + ")" +
                (config.getBooleanProperty("datanucleus.cache.collections") ? ", Collections/Maps " : ""));
        }
        NucleusLogger.PERSISTENCE.info("===========================================================");
    }

    /**
     * Accessor for the class to use for datastore identity.
     * @return Class for datastore-identity
     */
    public synchronized Class getDatastoreIdentityClass()
    {
        if (datastoreIdentityClass == null)
        {
            String dsidName = config.getStringProperty("datanucleus.datastoreIdentityType");
            String datastoreIdentityClassName = pluginManager.getAttributeValueForExtension(
                "org.datanucleus.store_datastoreidentity", "name", dsidName, "class-name");
            if (datastoreIdentityClassName == null)
            {
                // User has specified a datastore_identity plugin that has not registered
                throw new NucleusUserException(LOCALISER.msg("002001", dsidName)).setFatal();
            }

            // Try to load the class
            ClassLoaderResolver clr = getClassLoaderResolver(null);
            try
            {
                datastoreIdentityClass = clr.classForName(datastoreIdentityClassName,org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);
            }
            catch (ClassNotResolvedException cnre)
            {
                throw new NucleusUserException(LOCALISER.msg("002002", dsidName, 
                    datastoreIdentityClassName)).setFatal();
            }
        }
        return datastoreIdentityClass;
    }

    /**
     * Accessor for the current identity string translator to use (if any).
     * @return Identity string translator instance (or null if persistence property not set)
     */
    public synchronized IdentityStringTranslator getIdentityStringTranslator()
    {
        if (idStringTranslatorInit)
        {
            return idStringTranslator;
        }

        // Identity translation
        idStringTranslatorInit = true;
        String translatorType = config.getStringProperty("datanucleus.identityStringTranslatorType");
        if (translatorType != null)
        {
            try
            {
                idStringTranslator = (IdentityStringTranslator)pluginManager.createExecutableExtension(
                    "org.datanucleus.identity_string_translator", "name", translatorType, "class-name", null, null);
                return idStringTranslator;
            }
            catch (Exception e)
            {
                // User has specified a string identity translator plugin that has not registered
                throw new NucleusUserException(LOCALISER.msg("002001", translatorType)).setFatal();
            }
        }
        return null;
    }

    /**
     * Accessor for the current identity key translator to use (if any).
     * @return Identity key translator instance (or null if persistence property not set)
     */
    public synchronized IdentityKeyTranslator getIdentityKeyTranslator()
    {
        if (idKeyTranslatorInit)
        {
            return idKeyTranslator;
        }

        // Identity key translation
        idKeyTranslatorInit = true;
        String translatorType = config.getStringProperty("datanucleus.identityKeyTranslatorType");
        if (translatorType != null)
        {
            try
            {
                idKeyTranslator = (IdentityKeyTranslator)pluginManager.createExecutableExtension(
                    "org.datanucleus.identity_key_translator", "name", translatorType, "class-name", null, null);
                return idKeyTranslator;
            }
            catch (Exception e)
            {
                // User has specified a identity key translator plugin that has not registered
                throw new NucleusUserException(LOCALISER.msg("002001", translatorType)).setFatal();
            }
        }
        return null;
    }

    /**
     * Accessor for the JMX manager (if required).
     * If the user has set the persistence property "datanucleus.managedRuntime" to true then this will
     * return a JMX manager.
     * @return The JMX manager
     */
    public synchronized ManagementManager getJMXManager()
    {
        if (jmxManager == null && config.getBooleanProperty("datanucleus.managedRuntime"))
        {
            // User requires managed runtime, and not yet present so create manager
            jmxManager = new ManagementManager(this);
        }
        return jmxManager;
    }

    /**
     * Accessor for a ClassLoaderResolver to use in resolving classes.
     * Caches the resolver for the specified primary loader, and hands it out if present.
     * @param primaryLoader Loader to use as the primary loader (or null)
     * @return The ClassLoader resolver
     */
    public ClassLoaderResolver getClassLoaderResolver(ClassLoader primaryLoader)
    {
        // Set the key we will refer to this loader by
        String key = classLoaderResolverClassName;
        if (primaryLoader != null)
        {
            key += ":[" + StringUtils.toJVMIDString(primaryLoader) + "]"; 
        }

        // See if we have the loader cached
        ClassLoaderResolver clr = classLoaderResolverMap.get(key);
        if (clr == null)
        {
            // Create the ClassLoaderResolver of this type with this primary loader
            try
            {
                Class cls = Class.forName(classLoaderResolverClassName);
                Class[] ctrArgs = null;
                Object[] ctrParams = null;
                if (primaryLoader != null)
                {
                    ctrArgs = new Class[] {ClassLoader.class};
                    ctrParams = new Object[] {primaryLoader};
                }
                Constructor ctor = cls.getConstructor(ctrArgs);
                clr = (ClassLoaderResolver)ctor.newInstance(ctrParams);
                clr.registerUserClassLoader((ClassLoader)config.getProperty("datanucleus.primaryClassLoader"));
            }
            catch (ClassNotFoundException cnfe)
            {
                throw new NucleusUserException(LOCALISER.msg("001002", classLoaderResolverClassName), cnfe).setFatal();
            }
            catch (Exception e)
            {
                throw new NucleusUserException(LOCALISER.msg("001003", classLoaderResolverClassName), e).setFatal();
            }
            classLoaderResolverMap.put(key, clr);
        }

        return clr;
    }

    /**
     * Accessor for the implementation creator for this context.
     * @return The implementation creator
     */
    public synchronized ImplementationCreator getImplementationCreator()
    {
        if (implCreatorInit)
        {
            return implCreator;
        }

        String implCreatorName = config.getStringProperty("datanucleus.implementationCreatorName");
        if (implCreatorName != null && implCreatorName.equalsIgnoreCase("None"))
        {
            implCreator = null;
            implCreatorInit = true;
            return implCreator;
        }

        try
        {
            implCreator = (ImplementationCreator)getPluginManager().createExecutableExtension(
                "org.datanucleus.implementation_creator",
                "name", implCreatorName, "class-name", 
                new Class[] {MetaDataManager.class},
                new Object[] {getMetaDataManager()});
        }
        catch (Exception e)
        {
            // Creator not found
            NucleusLogger.PERSISTENCE.info(LOCALISER.msg("008006", implCreatorName));
        }
        if (implCreator == null)
        {
            // Selection not found so find the first available
            ConfigurationElement[] elems = getPluginManager().getConfigurationElementsForExtension("org.datanucleus.implementation_creator", null, null);
            String first = null;
            if (elems != null && elems.length > 0)
            {
                first = elems[0].getAttribute("name");
                try
                {
                    implCreator = (ImplementationCreator)getPluginManager().createExecutableExtension(
                        "org.datanucleus.implementation_creator",
                        "name", first, "class-name", new Class[] {MetaDataManager.class},
                        new Object[] {getMetaDataManager()});
                }
                catch (Exception e)
                {
                    // Creator not found
                    NucleusLogger.PERSISTENCE.info(LOCALISER.msg("008006", first));
                }
            }
        }

        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            if (implCreator == null)
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("008007"));
            }
            else
            {
                NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("008008", StringUtils.toJVMIDString(implCreator)));
            }
        }
        implCreatorInit = true;
        return implCreator;
    }

    /**
     * Accessor for the Meta-Data Manager.
     * @return Returns the MetaDataManager.
     */
    public synchronized MetaDataManager getMetaDataManager()
    {
        if (metaDataManager == null)
        {
            String apiName = apiAdapter.getName();
            try
            {
                metaDataManager = (MetaDataManager) getPluginManager().createExecutableExtension(
                    "org.datanucleus.metadata_manager", new String[]{"name"}, new String[]{apiName}, 
                    "class", new Class[] {NucleusContext.class}, new Object[]{this});
            }
            catch (Exception e)
            {
                throw new NucleusException(LOCALISER.msg("008010", apiName, e.getMessage()), e);
            }
            if (metaDataManager == null)
            {
                throw new NucleusException(LOCALISER.msg("008009", apiName));
            }
        }

        return metaDataManager;
    }

    /**
     * Accessor for the transaction manager.
     * @return The transaction manager.
     */
    public synchronized TransactionManager getTransactionManager()
    {
        if (txManager == null)
        {
            // Initialise support for transactions and register with JMX if running
            this.jmxManager = getJMXManager();
            txManager = new TransactionManager();
            if (jmxManager != null)
            {
                txManager.registerMbean(jmxManager.getDomainName(), jmxManager.getInstanceName(),
                    jmxManager.getManagementServer());
            }
        }
        return txManager;
    }

    /**
     * Accessor for the JTA transaction manager (if using JTA).
     * @return the JTA Transaction Manager
     */
    public synchronized javax.transaction.TransactionManager getJtaTransactionManager()
    {
        if (jtaTxManager == null)
        {
            // Find the JTA transaction manager
            // Before J2EE 5 there is no standard way to do this so use the finder process.
            // See http://www.onjava.com/pub/a/onjava/2005/07/20/transactions.html
            jtaTxManager = new TransactionManagerFinder(this).getTransactionManager(
                getClassLoaderResolver((ClassLoader)config.getProperty("datanucleus.primaryClassLoader")));
            if (jtaTxManager == null)
            {
                throw new NucleusTransactionException(LOCALISER.msg("015030"));
            }
        }
        return jtaTxManager;
    }

    /**
     * Accessor for the StoreManager
     * @return the StoreManager
     */
    public StoreManager getStoreManager()
    {
        return storeMgr;
    }

    /**
     * Mutator for the store manager. Can only be set once.
     * @param storeMgr The store manager
     */
    public synchronized void setStoreManager(StoreManager storeMgr)
    {
        if (this.storeMgr == null)
        {
            this.storeMgr = storeMgr;
        }
    }

    /**
     * Return whether there is an L2 cache.
     * @return Whether the L2 cache is enabled
     */
    public boolean hasLevel2Cache()
    {
        getLevel2Cache();
        return !(cache instanceof NullLevel2Cache);
    }

    /**
     * Accessor for the DataStore (level 2) Cache
     * @return The datastore cache
     */
    public Level2Cache getLevel2Cache()
    {
        if (cache == null)
        {
            String level2Type = config.getStringProperty("datanucleus.cache.level2.type");

            // Find the L2 cache class name from its plugin name
            String level2ClassName = pluginManager.getAttributeValueForExtension(
                "org.datanucleus.cache_level2", "name", level2Type, "class-name");
            if (level2ClassName == null)
            {
                // Plugin of this name not found
                throw new NucleusUserException(LOCALISER.msg("004000", level2Type)).setFatal();
            }

            try
            {
                // Create an instance of the L2 Cache
                cache = (Level2Cache)pluginManager.createExecutableExtension(
                    "org.datanucleus.cache_level2", "name", level2Type, "class-name",
                    new Class[]{NucleusContext.class}, new Object[]{this});
                if (NucleusLogger.CACHE.isDebugEnabled())
                {
                    NucleusLogger.CACHE.debug(LOCALISER.msg("004002", level2Type));
                }
            }
            catch (Exception e)
            {
                // Class name for this L2 cache plugin is not found!
                throw new NucleusUserException(LOCALISER.msg("004001", level2Type, level2ClassName), e).setFatal();
            }
        }
        return cache;
    }

    /**
     * Object the array of registered ObjectManagerListener's
     * @return array of {@link org.datanucleus.store.ExecutionContext.LifecycleListener}
     */
    public ExecutionContext.LifecycleListener[] getObjectManagerListeners()
    {
        return objectManagerListeners.toArray(new ExecutionContext.LifecycleListener[objectManagerListeners.size()]);
    }
    
    /**
     * Register a new Listener for ObjectManager's events
     * @param listener the listener to register
     */
    public void addObjectManagerListener(ExecutionContext.LifecycleListener listener)
    {
        objectManagerListeners.add(listener);
    }

    /**
     * Unregister a Listener from ObjectManager's events
     * @param listener the listener to unregister
     */
    public void removeObjectManagerListener(ExecutionContext.LifecycleListener listener)
    {
        objectManagerListeners.remove(listener);
    }

    /**
     * Mutator for whether we are in JCA mode.
     * @param jca true if using JCA connector
     */
    public synchronized void setJcaMode(boolean jca)
    {
        this.jca = jca;
    }

    /**
     * Accessor for the JCA mode.
     * @return true if using JCA connector.
     */
    public boolean isJcaMode()
    {
        return jca;
    }

    // --------------------------- Fetch Groups ---------------------------------

    /** 
     * Convenience accessor for the FetchGroupManager.
     * Creates it if not yet existing.
     * @return The FetchGroupManager
     */
    public synchronized FetchGroupManager getFetchGroupManager()
    {
        if (fetchGrpMgr == null)
        {
            fetchGrpMgr = new FetchGroupManager(this);
        }
        return fetchGrpMgr;
    }

    /**
     * Method to add a dynamic FetchGroup for use by this OMF.
     * @param grp The group
     */
    public void addInternalFetchGroup(FetchGroup grp)
    {
        getFetchGroupManager().addFetchGroup(grp);
    }

    /**
     * Method to remove a dynamic FetchGroup from use by this OMF.
     * @param grp The group
     */
    public void removeInternalFetchGroup(FetchGroup grp)
    {
        getFetchGroupManager().removeFetchGroup(grp);
    }

    /**
     * Method to create a new internal fetch group for the class+name.
     * @param cls Class that it applies to
     * @param name Name of group
     * @return The group
     */
    public FetchGroup createInternalFetchGroup(Class cls, String name)
    {
        if (!cls.isInterface() && !getApiAdapter().isPersistable(cls))
        {
            // Class but not persistable!
            throw new NucleusUserException("Cannot create FetchGroup for " + cls + " since it is not persistable");
        }
        else if (cls.isInterface() && !getMetaDataManager().isPersistentInterface(cls.getName()))
        {
            // Interface but not persistent
            throw new NucleusUserException("Cannot create FetchGroup for " + cls + " since it is not persistable");
        }
        return getFetchGroupManager().createFetchGroup(cls, name);
    }

    /**
     * Accessor for an internal fetch group for the specified class.
     * @param cls The class
     * @param name Name of the group
     * @return The FetchGroup
     * @throws NucleusUserException if the class is not persistable
     */
    public FetchGroup getInternalFetchGroup(Class cls, String name)
    {
        if (!cls.isInterface() && !getApiAdapter().isPersistable(cls))
        {
            // Class but not persistable!
            throw new NucleusUserException("Cannot create FetchGroup for " + cls + " since it is not persistable");
        }
        else if (cls.isInterface() && !getMetaDataManager().isPersistentInterface(cls.getName()))
        {
            // Interface but not persistent
            throw new NucleusUserException("Cannot create FetchGroup for " + cls + " since it is not persistable");
        }
        return getFetchGroupManager().getFetchGroup(cls, name);
    }

    /**
     * Accessor for the fetch groups for the specified name.
     * @param name Name of the group
     * @return The FetchGroup
     */
    public Set<FetchGroup> getFetchGroupsWithName(String name)
    {
        return getFetchGroupManager().getFetchGroupsWithName(name);
    }
}