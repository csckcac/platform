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
2004 Andy Jefferson - added description of addition process.
2004 Andy Jefferson - added constructor, and try-catch on initialisation
2006 Andy Jefferson - renamed to PersistenceConfiguration so that it is API agnostic
2008 Andy Jefferson - rewritten to have properties map and not need Java beans setters/getters
2011 Andy Jefferson - default properties, user properties, datastore properties concepts
    ...
**********************************************************************/
package org.datanucleus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.properties.BooleanPropertyValidator;
import org.datanucleus.properties.IntegerPropertyValidator;
import org.datanucleus.properties.PersistencePropertyValidator;
import org.datanucleus.properties.PropertyStore;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.PersistenceUtils;

/**
 * Class providing configuration for persistence. 
 * Properties are defined in plugin.xml (aliases, default value, validators etc). 
 * Property values are stored in two maps. 
 * <ul>
 * <li>The first is the default value for the property (where a default is defined). The default comes from
 *     either the plugin defining it, or for the API being used (overrides any plugin default).</li>
 * <li>The second is the user-provided value (where the user has provided one).</li>
 * </ul>
 * Components can then access these properties using any of the convenience accessors for boolean, Boolean, long, 
 * int, Object, String types. When accessing properties the user-provided value is taken first (if available),
 * otherwise the default value is used (or null).
 */
public class PersistenceConfiguration extends PropertyStore
{
    /** Localisation of messages. */
    protected static final Localiser LOCALISER = Localiser.getInstance("org.datanucleus.Localisation",
        ClassConstants.NUCLEUS_CONTEXT_LOADER);

    /** Map of default properties, used as a fallback. */
    private Map<String, Object> defaultProperties = new HashMap<String, Object>();

    /** Mapping for the properties of the plugins, PropertyMapping, keyed by the property name. */
    private Map<String, PropertyMapping> propertyMappings = new HashMap<String, PropertyMapping>();

    /**
     * Convenience class wrapping the plugin property specification information.
     */
    static class PropertyMapping
    {
        String name;
        String internalName;
        String validatorName;
        boolean datastore;
        boolean managerOverride;
        public PropertyMapping(String name, String intName, String validator, boolean datastore, boolean managerOverride)
        {
            this.name = name;
            this.internalName = intName;
            this.validatorName = validator;
            this.datastore = datastore;
            this.managerOverride = managerOverride;
        }
    }

    /**
     * Constructor.
     */
    public PersistenceConfiguration()
    {
    }

    /**
     * Accessor for the names of the supported persistence properties.
     * @return The persistence properties that we support
     */
    public Set<String> getSupportedProperties()
    {
        return propertyMappings.keySet();
    }

    /**
     * Convenience method to return all properties that are user-specified and should be specified on the StoreManager.
     * @return Datastore properties
     */
    public Map<String, Object> getDatastoreProperties()
    {
        Map<String, Object> props = new HashMap<String, Object>();
        Iterator<String> propKeyIter = properties.keySet().iterator();
        while (propKeyIter.hasNext())
        {
            String name = propKeyIter.next();
            if (isPropertyForDatastore(name))
            {
                props.put(name, properties.get(name));
            }
        }
        return props;
    }

    /**
     * Method that removes all properties from this store that are marked as "datastore".
     */
    public void removeDatastoreProperties()
    {
        Iterator<String> propKeyIter = properties.keySet().iterator();
        while (propKeyIter.hasNext())
        {
            String name = propKeyIter.next();
            if (isPropertyForDatastore(name))
            {
                propKeyIter.remove();
            }
        }
    }

    /**
     * Accessor for whether the specified property name should be stored with the StoreManager.
     * @param name Name of the property
     * @return Whether it is for the datastore
     */
    public boolean isPropertyForDatastore(String name)
    {
        PropertyMapping mapping = propertyMappings.get(name.toLowerCase(Locale.ENGLISH));
        return (mapping != null ? mapping.datastore : false);
    }

    public String getInternalNameForProperty(String name)
    {
        PropertyMapping mapping = propertyMappings.get(name.toLowerCase(Locale.ENGLISH));
        return (mapping != null && mapping.internalName != null ? mapping.internalName : name);
    }

    /**
     * Convenience method to return all properties that are overrideable on the PM/EM.
     * @return PM/EM overrideable properties
     */
    public Map<String, Object> getManagerOverrideableProperties()
    {
        Map<String, Object> props = new HashMap<String, Object>();
        Iterator<Map.Entry<String, PropertyMapping>> propIter = propertyMappings.entrySet().iterator();
        while (propIter.hasNext())
        {
            Map.Entry<String, PropertyMapping> entry = propIter.next();
            PropertyMapping mapping = entry.getValue();
            if (mapping.managerOverride)
            {
                String propName = (mapping.internalName != null ? 
                        mapping.internalName.toLowerCase(Locale.ENGLISH) : mapping.name.toLowerCase(Locale.ENGLISH));
                props.put(propName, getProperty(propName));
            }
            else if (mapping.internalName != null)
            {
                PropertyMapping intMapping = propertyMappings.get(mapping.internalName.toLowerCase(Locale.ENGLISH));
                if (intMapping != null && intMapping.managerOverride)
                {
                    props.put(mapping.name.toLowerCase(Locale.ENGLISH), getProperty(mapping.internalName));
                }
            }
        }
        return props;
    }

    /**
     * Method to set the persistence property defaults based on what is defined for plugins.
     * This should only be called after the other setDefaultProperties method is called, which sets up the mappings
     * @param props Properties to use in the default set
     */
    public void setDefaultProperties(Map props)
    {
        if (props != null && props.size() > 0)
        {
            Iterator<Map.Entry> entryIter = props.entrySet().iterator();
            while (entryIter.hasNext())
            {
                Map.Entry entry = entryIter.next();
                PropertyMapping mapping = propertyMappings.get(((String)entry.getKey()).toLowerCase(Locale.ENGLISH));
                Object propValue = entry.getValue();
                if (mapping != null && mapping.validatorName != null && propValue instanceof String)
                {
                    propValue = getValueForPropertyWithValidator((String)propValue, mapping.validatorName);
                }
                defaultProperties.put(((String)entry.getKey()).toLowerCase(Locale.ENGLISH), propValue);
            }
        }
    }

    /**
     * Method to set the persistence property defaults based on what is defined for plugins.
     * @param pluginMgr The plugin manager
     */
    public void setDefaultProperties(PluginManager pluginMgr)
    {
        ConfigurationElement[] propElements =
            pluginMgr.getConfigurationElementsForExtension("org.datanucleus.persistence_properties", null, null);
        if (propElements != null)
        {
            for (int i=0;i<propElements.length;i++)
            {
                String name = propElements[i].getAttribute("name");
                String intName = propElements[i].getAttribute("internal-name");
                String value = propElements[i].getAttribute("value");
                String datastoreString = propElements[i].getAttribute("datastore");
                String validatorName = propElements[i].getAttribute("validator");
                boolean datastore = (datastoreString != null && datastoreString.equalsIgnoreCase("true"));
                String mgrOverrideString = propElements[i].getAttribute("manager-overrideable");
                boolean mgrOverride = (mgrOverrideString != null && mgrOverrideString.equalsIgnoreCase("true"));

                propertyMappings.put(name.toLowerCase(Locale.ENGLISH), new PropertyMapping(name, intName, validatorName, datastore, 
                    mgrOverride));

                Object propValue = System.getProperty(name);
                if (propValue != null)
                {
                    // System value provided so add property with that value
                    // TODO Should this be stored as "default" value when provided by user as system value? don't think so
                    if (validatorName != null)
                    {
                        propValue = getValueForPropertyWithValidator((String)propValue, validatorName);
                    }
                    this.defaultProperties.put(intName != null ? intName.toLowerCase(Locale.ENGLISH) : name.toLowerCase(Locale.ENGLISH), propValue);
                }
                else
                {
                    propValue = value;
                    if (validatorName != null && propValue != null)
                    {
                        propValue = getValueForPropertyWithValidator(value, validatorName);
                    }
                    if (!defaultProperties.containsKey(name) && value != null)
                    {
                        // Property has default value and not yet set so add property with that value
                        this.defaultProperties.put(intName != null ? intName.toLowerCase(Locale.ENGLISH) : name.toLowerCase(Locale.ENGLISH), propValue);
                    }
                }
            }
        }
    }

    protected Object getValueForPropertyWithValidator(String value, String validatorName)
    {
        if (validatorName.equals(BooleanPropertyValidator.class.getName()))
        {
            return Boolean.valueOf(value);
        }
        else if (validatorName.equals(IntegerPropertyValidator.class.getName()))
        {
            return Integer.valueOf(value);
        }
        return value;
    }

    /**
     * Accessor for the specified property as an Object.
     * Returns user-specified value if provided, otherwise the default value, otherwise null.
     * @param name Name of the property
     * @return Value for the property
     */
    public Object getProperty(String name)
    {
        // Use local property value if present, otherwise relay back to default value
        if (properties.containsKey(name.toLowerCase(Locale.ENGLISH)))
        {
            return super.getProperty(name);
        }
        return defaultProperties.get(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Method to set the persistence properties using those defined in a file.
     * @param filename Name of the file containing the properties
     */
    public synchronized void setPropertiesUsingFile(String filename)
    {
        if (filename == null)
        {
            return;
        }

        Properties props = null;
        try
        {
            props = PersistenceUtils.setPropertiesUsingFile(filename);
            setPropertyInternal("datanucleus.propertiesFile", filename);
        }
        catch (NucleusUserException nue)
        {
            properties.remove("datanucleus.propertiesFile");
            throw nue;
        }
        if (props != null && !props.isEmpty())
        {
            setPersistenceProperties(props);
        }
    }

    /**
     * Accessor for the persistence properties default values.
     * This returns the defaulted properties
     * @return The persistence properties
     */
    public Map<String, Object> getPersistencePropertiesDefaults()
    {
        return Collections.unmodifiableMap(defaultProperties);
    }

    /**
     * Accessor for the persistence properties.
     * This returns just the user-supplied properties, not the defaulted properties
     * @see #getPersistenceProperties()
     * @return The persistence properties
     */
    public Map<String, Object> getPersistenceProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    public Set<String> getPropertyNamesWithPrefix(String prefix)
    {
        Set<String> propNames = null;
        Iterator<String> nameIter = properties.keySet().iterator();
        while (nameIter.hasNext())
        {
            String name = nameIter.next();
            if (name.startsWith(prefix.toLowerCase(Locale.ENGLISH)))
            {
                if (propNames == null)
                {
                    propNames = new HashSet<String>();
                }
                propNames.add(name);
            }
        }
        return propNames;
    }

    /**
     * Set the properties for this configuration.
     * Note : this has this name so it has a getter/setter pair for use by things like Spring.
     * @see #getPersistencePropertiesDefaults()
     * @param props The persistence properties
     */
    public void setPersistenceProperties(Map props)
    {
        Set entries = props.entrySet();
        Iterator<Map.Entry> entryIter = entries.iterator();
        while (entryIter.hasNext())
        {
            Map.Entry entry = entryIter.next();
            Object keyObj = entry.getKey();
            if (keyObj instanceof String)
            {
                String key = (String)keyObj;
                setProperty(key, entry.getValue());
            }
        }
    }

    /**
     * Convenience method to set a persistence property.
     * Uses any validator defined for the property to govern whether the value is suitable.
     * @param name Name of the property
     * @param value Value
     */
    public void setProperty(String name, Object value)
    {
        if (name != null)
        {
            String propertyName = name.trim();
            PropertyMapping mapping = propertyMappings.get(propertyName.toLowerCase(Locale.ENGLISH));
            if (mapping != null)
            {
                if (mapping.validatorName != null)
                {
                    // TODO Use ClassLoaderResolver
                    PersistencePropertyValidator validator = null;
                    try
                    {
                        Class validatorCls = Class.forName(mapping.validatorName);
                        validator = (PersistencePropertyValidator)validatorCls.newInstance();
                    }
                    catch (Exception e)
                    {
                        NucleusLogger.PERSISTENCE.warn("Error creating validator of type " + mapping.validatorName, e);
                    }

                    if (validator != null)
                    {
                        boolean validated = (mapping.internalName != null ? 
                                validator.validate(mapping.internalName, value) : 
                                    validator.validate(propertyName, value));
                        if (!validated)
                        {
                            throw new IllegalArgumentException(LOCALISER.msg("008012", propertyName, value));
                        }
                    }
                }

                if (value != null && value instanceof String && mapping.validatorName != null)
                {
                    value = getValueForPropertyWithValidator((String)value, mapping.validatorName);
                }

                if (mapping.internalName != null)
                {
                    setPropertyInternal(mapping.internalName, value);
                }
                else
                {
                    setPropertyInternal(mapping.name, value);
                }

                // Special behaviour properties
                if (propertyName.equals("datanucleus.propertiesFile"))
                {
                    // Load all properties from the specified file
                    setPropertiesUsingFile((String)value);
                }
                else if (propertyName.equals("datanucleus.localisation.messageCodes"))
                {
                    // Set global log message code flag
                    boolean included = getBooleanProperty("datanucleus.localisation.messageCodes");
                    Localiser.setDisplayCodesInMessages(included);
                }
                else if (propertyName.equals("datanucleus.localisation.language"))
                {
                    String language = getStringProperty("datanucleus.localisation.language");
                    Localiser.setLanguage(language);
                }
            }
            else
            {
                // Unknown property so just add it.
                setPropertyInternal(propertyName, value);
                if (propertyMappings.size() > 0)
                {
                    NucleusLogger.PERSISTENCE.info(LOCALISER.msg("008015", propertyName));
                }
            }
        }
    }

    /**
     * Equality operator.
     * @param obj Object to compare against.
     * @return Whether the objects are equal.
     */
    public synchronized boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (!(obj instanceof PersistenceConfiguration))
        {
            return false;
        }

        PersistenceConfiguration config = (PersistenceConfiguration)obj;
        if (properties == null)
        {
            if (config.properties != null)
            {
                return false;
            }
        }
        else if (!properties.equals(config.properties))
        {
            return false;
        }

        if (defaultProperties == null)
        {
            if (config.defaultProperties != null)
            {
                return false;
            }
        }
        else if (!defaultProperties.equals(config.defaultProperties))
        {
            return false;
        }

        return true;
    }
}