/**********************************************************************
Copyright (c) 2003 Andy Jefferson and others. All rights reserved.
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
2004 Erik Bengtson - added interfaces methods
2008 Andy Jefferson - added java type handling separate from mapped types
    ...
**********************************************************************/
package org.datanucleus.store.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.api.ApiAdapter;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.util.JavaUtils;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Registry of java type support. 
 * Provides information applicable to all datastores for how a field of a class is treated; 
 * whether it is by default persistent, whether it is by default embedded, whether it is in the DFG, 
 * and if it has a wrapper for SCO operations. Also stores whether the type can be converted to/from
 * a String (for datastores that don't provide storage natively).
 * Uses the plugin mechanism extension-point "org.datanucleus.java_type".
 * TODO Consider adding "api" as an attribute to the "java_type" extension-point so that we can have
 * some a type definition for JDO, and also for JPA.
 */
public class TypeManager
{
    private static final Localiser LOCALISER = Localiser.getInstance(
        "org.datanucleus.Localisation", org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    protected final ClassLoaderResolver clr;

    /** Map of java types, keyed by the class name. */
    Map<String, JavaType> javaTypes = new HashMap();

    /** Map of String Converter objects keyed by the type that they convert. */
    Map<Class, ObjectStringConverter> stringConvertersByType = null;

    /** Map of Long Converter objects keyed by the type that they convert. */
    Map<Class, ObjectLongConverter> longConvertersByType = null;

    /**
     * Constructor, loading support for type mappings using the plugin mechanism.
     * @param api The API in use // NEVER USED currently
     * @param clr the ClassLoader Resolver
     * @param mgr the PluginManager
     **/
    public TypeManager(ApiAdapter api, PluginManager mgr, ClassLoaderResolver clr)
    {
        this.clr = clr;
        loadJavaTypes(mgr, clr);
    }

    /**
     * Accessor for the supported second-class Types.
     * This may or may not be a complete list, just that it provides the principal ones.
     * @return Set of supported types (fully qualified class names).
     **/
    public Set<String> getSupportedSecondClassTypes()
    {
        return new HashSet(javaTypes.keySet());
    }

    /**
     * Accessor for whether a class is supported as being second class.
     * @param className The class name
     * @return Whether the class is supported (to some degree)
     */
    public boolean isSupportedSecondClassType(String className)
    {
        if (className == null)
        {
            return false;
        }
        JavaType type = javaTypes.get(className);
        if (type == null)
        {
            try
            {
                Class cls = clr.classForName(className);
                type = findJavaTypeForClass(cls);
                return (type != null);
            }
            catch (Exception e)
            {
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * Convenience method to filter out any supported classes from a list.
     * @param inputClassNames Names of the classes
     * @return Names of the classes (omitting supported types)
     */
    public String[] filterOutSupportedSecondClassNames(String[] inputClassNames)
    {
        // Filter out any "simple" type classes
        int filteredClasses = 0;
        for (int i = 0; i < inputClassNames.length; ++i)
        {
            if (isSupportedSecondClassType(inputClassNames[i]))
            {
                inputClassNames[i] = null;
                ++filteredClasses;
            }
        }
        if (filteredClasses == 0)
        {
            return inputClassNames;
        }
        String[] restClasses = new String[inputClassNames.length - filteredClasses];
        int m = 0;
        for (int i = 0; i < inputClassNames.length; ++i)
        {
            if (inputClassNames[i] != null)
            {
                restClasses[m++] = inputClassNames[i];
            }
        }
        return restClasses;
    }

    /**
     * Accessor for whether the type is by default persistent.
     * @param c The type
     * @return Whether persistent
     */
    public boolean isDefaultPersistent(Class c)
    {
        if (c == null)
        {
            return false;
        }

        JavaType type = javaTypes.get(c.getName());
        if (type != null)
        {
            return type.persistent;
        }

        // Try to find a class that this class extends that is supported
        type = findJavaTypeForClass(c);
        if (type != null)
        {
            return type.persistent;
        }

        return false;
    }

    /**
     * Accessor for whether the type is by default in the DFG.
     * @param c The type
     * @return Whether in the DFG
     */
    public boolean isDefaultFetchGroup(Class c)
    {
        if (c == null)
        {
            return false;
        }

        JavaType type = javaTypes.get(c.getName());
        if (type != null)
        {
            return type.dfg;
        }

        // Try to find a class that this class extends that is supported
        type = findJavaTypeForClass(c);
        if (type != null)
        {
            return type.dfg;
        }

        return false;
    }

    /**
     * Accessor for whether the generic collection type is by default in the DFG.
     * @param c The type
     * @param genericType The element generic type
     * @return Whether in the DFG
     */
    public boolean isDefaultFetchGroupForCollection(Class c, Class genericType)
    {
        if (c != null && genericType == null)
        {
            return isDefaultFetchGroup(c);
        }
        else if (c == null)
        {
            return false;
        }

        String name = c.getName() + "<" + genericType.getName() + ">";
        JavaType type = javaTypes.get(name);
        if (type != null)
        {
            return type.dfg;
        }

        // Try to find a class that this class extends that is supported
        type = findJavaTypeForCollectionClass(c, genericType);
        if (type != null)
        {
            return type.dfg;
        }

        return false;
    }

    /**
     * Accessor for whether the type is by default embedded.
     * @param c The type
     * @return Whether embedded
     */
    public boolean isDefaultEmbeddedType(Class c)
    {
        if (c == null)
        {
            return false;
        }

        JavaType type = javaTypes.get(c.getName());
        if (type != null)
        {
            return type.embedded;
        }

        // Try to find a class that this class extends that is supported
        type = findJavaTypeForClass(c);
        if (type != null)
        {
            return type.embedded;
        }

        return false;
    }

    /**
     * Accessor for whether the type is SCO mutable.
     * @param className The type
     * @return Whether SCO mutable
     */
    public boolean isSecondClassMutableType(String className)
    {
        return (getWrapperTypeForType(className) != null);
    }

    /**
     * Accessor for the SCO wrapper for the type
     * @param className The type
     * @return SCO wrapper
     */
    public Class getWrapperTypeForType(String className)
    {
        if (className == null)
        {
            return null;
        }

        JavaType type = javaTypes.get(className);
        return type == null ? null : type.wrapperType;
    }

    /**
     * Accessor for the backing-store Second Class Wrapper class for the supplied class.
     * A type will have a SCO wrapper if it is SCO supported and is mutable.
     * If there is no backed wrapper provided returns the simple wrapper.
     * @param className The class name
     * @return The second class wrapper
     */
    public Class getWrappedTypeBackedForType(String className)
    {
        if (className == null)
        {
            return null;
        }

        JavaType type = javaTypes.get(className);
        return type == null ? null : type.wrapperTypeBacked;
    }

    /**
     * Accessor for whether the type is a SCO wrapper itself.
     * @param className The type
     * @return Whether is SCO wrapper
     */
    public boolean isSecondClassWrapper(String className)
    {
        if (className == null)
        {
            return false;
        }

        // Check java types with wrappers
        Iterator iter = javaTypes.values().iterator();
        while (iter.hasNext())
        {
            JavaType type = (JavaType)iter.next();
            if (type.wrapperType != null && type.wrapperType.getName().equals(className))
            {
                return true;
            }
            if (type.wrapperTypeBacked != null && type.wrapperTypeBacked.getName().equals(className))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Accessor for a java type that the supplied class is a SCO wrapper for.
     * If the supplied class is not a SCO wrapper for anything then returns null.
     * @param className Name of the class
     * @return The java class that this is a wrapper for (or null)
     */
    public Class getTypeForSecondClassWrapper(String className)
    {
        Iterator iter = javaTypes.values().iterator();
        while (iter.hasNext())
        {
            JavaType type = (JavaType)iter.next();
            if (type.wrapperType != null && type.wrapperType.getName().equals(className))
            {
                return type.cls;
            }
            if (type.wrapperTypeBacked != null && type.wrapperTypeBacked.getName().equals(className))
            {
                return type.cls;
            }
        }
        return null;
    }

    /**
     * Method to return a string converter for the specified type.
     * Returns an instance of the converter (implementation of ObjectStringConverter)
     * or null if not mappable as a String.
     * @param type The type
     * @return The string converter (or null)
     */
    public ObjectStringConverter getStringConverter(Class type)
    {
        ObjectStringConverter converter = null;
        if (stringConvertersByType != null)
        {
            converter = stringConvertersByType.get(type);
            if (converter != null)
            {
                return converter;
            }
        }

        JavaType javaType = findJavaTypeForClass(type);
        if (javaType != null && javaType.stringConverter != null)
        {
            try
            {
                converter = (ObjectStringConverter)javaType.stringConverter.newInstance();
                if (converter != null)
                {
                    if (stringConvertersByType == null)
                    {
                        stringConvertersByType = new Hashtable();
                    }
                    stringConvertersByType.put(type, converter);
                }
                return converter;
            }
            catch (Exception e)
            {
                // Error instantiating type
                throw new NucleusUserException("Error instantiating string converter for type=" + type.getName(), e);
            }
        }

        return null;
    }

    /**
     * Method to return a long converter for the specified type.
     * Returns an instance of the converter (implementation of ObjectLongConverter)
     * or null if not mappable as a Long.
     * @param type The type
     * @return The long converter (or null)
     */
    public ObjectLongConverter getLongConverter(Class type)
    {
        ObjectLongConverter converter = null;
        if (longConvertersByType != null)
        {
            converter = longConvertersByType.get(type);
            if (converter != null)
            {
                return converter;
            }
        }

        JavaType javaType = findJavaTypeForClass(type);
        if (javaType != null && javaType.longConverter != null)
        {
            try
            {
                converter = (ObjectLongConverter)javaType.longConverter.newInstance();
                if (converter != null)
                {
                    if (longConvertersByType == null)
                    {
                        longConvertersByType = new Hashtable();
                    }
                    longConvertersByType.put(type, converter);
                }
                return converter;
            }
            catch (Exception e)
            {
                // Error instantiating type
                throw new NucleusUserException("Error instantiating long converter for type=" + type.getName(), e);
            }
        }

        return null;
    }

    /**
     * Convenience method to return the JavaType for the specified class. If this class has a defined
     * JavaType then returns it. If not then tries to find a superclass that is castable to the specified
     * type.
     * @param cls The class required
     * @return The JavaType
     */
    protected JavaType findJavaTypeForClass(Class cls)
    {
        if (cls == null)
        {
            return null;
        }
        JavaType type = javaTypes.get(cls.getName());
        if (type != null)
        {
            return type;
        }

        // Not supported so try to find one that is supported that this class derives from
        Collection supportedTypes = new HashSet(javaTypes.values());
        Iterator iter = supportedTypes.iterator();
        while (iter.hasNext())
        {
            type = (JavaType)iter.next();
            if (type.cls == cls && type.genericType == null)
            {
                return type;
            }
            if (!type.cls.getName().equals("java.lang.Object") && !type.cls.getName().equals("java.io.Serializable"))
            {
                Class componentCls = (cls.isArray() ? cls.getComponentType() : null);
                if (componentCls != null)
                {
                    // Array type
                    if (type.cls.isArray() && type.cls.getComponentType().isAssignableFrom(componentCls))
                    {
                        javaTypes.put(cls.getName(), type); // Register this subtype for reference
                        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                        {
                            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("016001",
                                cls.getName(), type.cls.getName()));
                        }
                        return type;
                    }
                }
                else
                {
                    // Basic type
                    if (type.cls.isAssignableFrom(cls) && type.genericType == null)
                    {
                        javaTypes.put(cls.getName(), type); // Register this subtype for reference
                        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
                        {
                            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("016001", 
                                cls.getName(), type.cls.getName()));
                        }
                        return type;
                    }
                }
            }
        }

        // Not supported
        return null;
    }

    /**
     * Convenience method to return the JavaType for the specified class. If this class has a defined
     * JavaType then returns it. If not then tries to find a superclass that is castable to the specified
     * type.
     * @param cls The class required
     * @return The JavaType
     */
    protected JavaType findJavaTypeForCollectionClass(Class cls, Class genericType)
    {
        if (cls == null)
        {
            return null;
        }
        else if (genericType == null)
        {
            return findJavaTypeForClass(cls);
        }

        String typeName = cls.getName() + "<" + genericType.getName() + ">";
        JavaType type = javaTypes.get(typeName);
        if (type != null)
        {
            return type;
        }

        // Not supported so try to find one that is supported that this class derives from
        Collection supportedTypes = new HashSet(javaTypes.values());
        Iterator iter = supportedTypes.iterator();
        while (iter.hasNext())
        {
            type = (JavaType)iter.next();
            if (type.cls.isAssignableFrom(cls))
            {
                if (type.genericType != null && type.genericType.isAssignableFrom(genericType))
                {
                    javaTypes.put(typeName, type); // Register this subtype for reference
                    return type;
                }
            }
        }

        // Fallback to just matching the collection type and forget the generic detail
        return findJavaTypeForClass(cls);
    }

    static class JavaType
    {
        final Class cls;
        final Class genericType;
        final boolean persistent;
        final boolean embedded;
        final boolean dfg;
        final Class wrapperType;
        final Class wrapperTypeBacked;
        final Class stringConverter;
        final Class longConverter;
        public JavaType(Class cls, Class genericType, boolean persistent, boolean embedded, boolean dfg, 
                Class wrapperType, Class wrapperTypeBacked, Class stringConverter, Class longConverter)
        {
            this.cls = cls;
            this.genericType = genericType;
            this.persistent = persistent;
            this.embedded = embedded;
            this.dfg = dfg;
            this.wrapperType = wrapperType;
            this.wrapperTypeBacked = (wrapperTypeBacked != null ? wrapperTypeBacked : wrapperType);
            this.stringConverter = stringConverter;
            this.longConverter = longConverter;
        }
    }

    /**
     * Method to load the java type that are currently registered in the PluginManager.
     * @param mgr the PluginManager
     * @param clr the ClassLoaderResolver
     */
    private void loadJavaTypes(PluginManager mgr, ClassLoaderResolver clr)
    {
        ConfigurationElement[] elems = mgr.getConfigurationElementsForExtension("org.datanucleus.java_type", null, null);
        if (elems != null)
        {
            for (int i=0; i<elems.length; i++)
            {
                String javaName = elems[i].getAttribute("name").trim();
                String genericTypeName = elems[i].getAttribute("generic-type");
                String persistentString = elems[i].getAttribute("persistent");
                String embeddedString = elems[i].getAttribute("embedded");
                String dfgString = elems[i].getAttribute("dfg");
                String javaVersionRestrict = elems[i].getAttribute("java-version-restricted");
                String javaVersion = elems[i].getAttribute("java-version");
                String wrapperType = elems[i].getAttribute("wrapper-type");
                String wrapperTypeBacked = elems[i].getAttribute("wrapper-type-backed");
                String stringConverterName = elems[i].getAttribute("string-converter");
                String longConverterName = elems[i].getAttribute("long-converter");

                boolean persistent = false;
                if (persistentString != null && persistentString.equalsIgnoreCase("true"))
                {
                    persistent = true;
                }
                boolean embedded = false;
                if (embeddedString != null && embeddedString.equalsIgnoreCase("true"))
                {
                    embedded = true;
                }
                boolean dfg = false;
                if (dfgString != null && dfgString.equalsIgnoreCase("true"))
                {
                    dfg = true;
                }
                boolean versionRestricted = false;
                if (javaVersionRestrict != null && javaVersionRestrict.equalsIgnoreCase("true"))
                {
                    versionRestricted = true;
                }
                if (!StringUtils.isWhitespace(wrapperType))
                {
                    wrapperType = wrapperType.trim();
                }
                else
                {
                    wrapperType = null;
                }
                if (!StringUtils.isWhitespace(wrapperTypeBacked))
                {
                    wrapperTypeBacked = wrapperTypeBacked.trim();
                }
                else
                {
                    wrapperTypeBacked = null;
                }
                if (!StringUtils.isWhitespace(stringConverterName))
                {
                    stringConverterName = stringConverterName.trim();
                }
                else
                {
                    stringConverterName = null;
                }
                if (!StringUtils.isWhitespace(longConverterName))
                {
                    longConverterName = longConverterName.trim();
                }
                else
                {
                    longConverterName = null;
                }

                if (StringUtils.isWhitespace(javaVersion))
                {
                    javaVersion = "1.3"; // Must be for 1.3 or later since not defined
                }

                // Only process this java type if we are using a consistent JRE
                if ((JavaUtils.isGreaterEqualsThan(javaVersion) && !versionRestricted) ||
                        (JavaUtils.isEqualsThan(javaVersion) && versionRestricted))
                {
                    try
                    {
                        Class cls = clr.classForName(javaName);
                        Class genericType = null;
                        String javaTypeName = cls.getName();
                        if (!StringUtils.isWhitespace(genericTypeName))
                        {
                            genericType = clr.classForName(genericTypeName);
                            javaTypeName += "<" + genericTypeName + ">";
                        }

                        if (!javaTypes.containsKey(javaTypeName))
                        {
                            // Only add first entry for a java type (ordered by the "priority" flag)
                            Class wrapperClass = null;
                            if (wrapperType != null)
                            {
                                try
                                {
                                    wrapperClass = mgr.loadClass(
                                        elems[i].getExtension().getPlugin().getSymbolicName(), wrapperType);
                                }
                                catch (NucleusException jpe)
                                {
                                    // Impossible to load the wrapper type from this plugin
                                    NucleusLogger.PERSISTENCE.error(LOCALISER.msg("016004", wrapperType));
                                    throw new NucleusException(LOCALISER.msg("016004", wrapperType));
                                }
                            }
                            Class wrapperClassBacked = null;
                            if (wrapperTypeBacked != null)
                            {
                                try
                                {
                                    wrapperClassBacked = mgr.loadClass(
                                        elems[i].getExtension().getPlugin().getSymbolicName(), wrapperTypeBacked);
                                }
                                catch (NucleusException jpe)
                                {
                                    // Impossible to load the wrapper type from this plugin
                                    NucleusLogger.PERSISTENCE.error(LOCALISER.msg("016004", wrapperTypeBacked));
                                    throw new NucleusException(LOCALISER.msg("016004", wrapperTypeBacked));
                                }
                            }
                            Class stringConverterClass = null;
                            if (stringConverterName != null)
                            {
                                try
                                {
                                    stringConverterClass = mgr.loadClass(
                                        elems[i].getExtension().getPlugin().getSymbolicName(), stringConverterName);
                                }
                                catch (NucleusException jpe)
                                {
                                    // Impossible to load the wrapper type from this plugin
                                    NucleusLogger.PERSISTENCE.error(LOCALISER.msg("016004", stringConverterName));
                                    throw new NucleusException(LOCALISER.msg("016004", stringConverterName));
                                }
                            }
                            Class longConverterClass = null;
                            if (longConverterName != null)
                            {
                                try
                                {
                                    longConverterClass = mgr.loadClass(
                                        elems[i].getExtension().getPlugin().getSymbolicName(), longConverterName);
                                }
                                catch (NucleusException jpe)
                                {
                                    // Impossible to load the wrapper type from this plugin
                                    NucleusLogger.PERSISTENCE.error(LOCALISER.msg("016004", longConverterName));
                                    throw new NucleusException(LOCALISER.msg("016004", longConverterName));
                                }
                            }

                            JavaType type = new JavaType(cls, genericType, persistent, embedded, dfg, wrapperClass, 
                                wrapperClassBacked, stringConverterClass, longConverterClass);
                            addJavaType(type);
                        }
                    }
                    catch (Exception e)
                    {
                        // Class not found so ignore. Should log this
                    }
                }
                else
                {
                    continue;
                }
            }
        }
    }

    protected void addJavaType(JavaType type)
    {
        String typeName = type.cls.getName();
        if (type.genericType != null)
        {
            // "Collection<String>"
            typeName += "<" + type.genericType.getName() + ">";
        }

        javaTypes.put(typeName, type);
        if (NucleusLogger.PERSISTENCE.isDebugEnabled())
        {
            NucleusLogger.PERSISTENCE.debug(LOCALISER.msg("016000", typeName, 
                "" + type.persistent, "" + type.dfg, "" + type.embedded));
        }
    }
}