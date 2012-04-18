/**********************************************************************
Copyright (c) 2004 Andy Jefferson and others. All rights reserved.
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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.util.ClassUtils;
import org.datanucleus.util.Localiser;
import org.datanucleus.util.NucleusLogger;
import org.datanucleus.util.StringUtils;

/**
 * Utilities needed for the processing of MetaData.
 */
public class MetaDataUtils
{
    protected static final Localiser LOCALISER = Localiser.getInstance(
        "org.datanucleus.Localisation", org.datanucleus.ClassConstants.NUCLEUS_CONTEXT_LOADER);

    private static MetaDataUtils instance;

    /**
     * Gets an instance of MetaDataUtils
     * @return a singleton instance of MetaDataUtils
     */
    public static synchronized MetaDataUtils getInstance()
    {
        if (instance == null)
        {
            instance = new MetaDataUtils();
        }
        return instance;
    }

    /**
     * Protected constructor to prevent outside instantiation
     */
    protected MetaDataUtils()
    {
    }

    /**
     * Convenience method to determine if an array is storable in a single column as a byte
     * array.
     * @param fmd The field
     * @return Whether this is an array that can be stored in a single column as non-serialised
     */
    public boolean arrayStorableAsByteArrayInSingleColumn(AbstractMemberMetaData fmd)
    {
        if (fmd == null || !fmd.hasArray())
        {
            return false;
        }

        String arrayComponentType = fmd.getType().getComponentType().getName();
        if (arrayComponentType.equals(ClassNameConstants.BOOLEAN) ||
            arrayComponentType.equals(ClassNameConstants.BYTE) ||
            arrayComponentType.equals(ClassNameConstants.CHAR) ||
            arrayComponentType.equals(ClassNameConstants.DOUBLE) ||
            arrayComponentType.equals(ClassNameConstants.FLOAT) ||
            arrayComponentType.equals(ClassNameConstants.INT) ||
            arrayComponentType.equals(ClassNameConstants.LONG) ||
            arrayComponentType.equals(ClassNameConstants.SHORT) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_LANG_BOOLEAN) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_LANG_BYTE) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_LANG_CHARACTER) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_LANG_DOUBLE) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_LANG_FLOAT) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_LANG_INTEGER) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_LANG_LONG) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_LANG_SHORT) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_MATH_BIGDECIMAL) ||
            arrayComponentType.equals(ClassNameConstants.JAVA_MATH_BIGINTEGER))
        {
            // These types can be stored as a single-column but setting the bytes only
            return true;
        }

        // All other arrays must be serialised into a single column
        return false;
    }

    /**
     * Convenience method that returns if a field stores a persistable object.
     * Doesn't care if the persistable object is serialised or embedded, just that it is persistable.
     * @param fmd MetaData for the field
     * @param ec ObjectManager
     * @return Whether it stores a persistable object
     */
    public boolean storesPersistable(AbstractMemberMetaData fmd, ExecutionContext ec)
    {
        if (fmd == null)
        {
            return false;
        }

        ClassLoaderResolver clr = ec.getClassLoaderResolver();
        MetaDataManager mmgr = ec.getMetaDataManager();
        if (fmd.hasCollection())
        {
            if (fmd.getCollection().elementIsPersistent())
            {
                // Collection of PC elements
                return true;
            }
            else
            {
                String elementType = fmd.getCollection().getElementType();
                Class elementCls = clr.classForName(elementType);
                if (mmgr.getMetaDataForImplementationOfReference(elementCls, null, clr) != null)
                {
                    // Collection of reference type for FCOs
                    return true;
                }
                if (elementCls != null && ClassUtils.isReferenceType(elementCls))
                {
                    try
                    {
                        String[] impls = getImplementationNamesForReferenceField(fmd,
                            FieldRole.ROLE_COLLECTION_ELEMENT, clr, mmgr);
                        if (impls != null)
                        {
                            elementCls = clr.classForName(impls[0]);
                            if (ec.getApiAdapter().isPersistable(elementCls))
                            {
                                // Collection of reference type for FCOs
                                return true;
                            }
                        }
                    }
                    catch (NucleusUserException nue)
                    {
                        // No implementations found so not persistable
                    }
                }
            }
        }
        else if (fmd.hasMap())
        {
            if (fmd.getMap().keyIsPersistent())
            {
                // Map of PC keys
                return true;
            }
            else
            {
                String keyType = fmd.getMap().getKeyType();
                Class keyCls = clr.classForName(keyType);
                if (mmgr.getMetaDataForImplementationOfReference(keyCls, null, clr) != null)
                {
                    // Map with keys of reference type for FCOs
                    return true;
                }
                if (keyCls != null && ClassUtils.isReferenceType(keyCls))
                {
                    try
                    {
                        String[] impls = getImplementationNamesForReferenceField(fmd,
                            FieldRole.ROLE_MAP_KEY, clr, mmgr);
                        if (impls != null)
                        {
                            keyCls = clr.classForName(impls[0]);
                            if (ec.getApiAdapter().isPersistable(keyCls))
                            {
                                // Map with keys of reference type for FCOs
                                return true;
                            }
                        }
                    }
                    catch (NucleusUserException nue)
                    {
                        // No implementations found so not persistable
                    }
                }
            }

            if (fmd.getMap().valueIsPersistent())
            {
                // Map of PC values
                return true;
            }
            else
            {
                String valueType = fmd.getMap().getValueType();
                Class valueCls = clr.classForName(valueType);
                if (mmgr.getMetaDataForImplementationOfReference(valueCls, null, clr) != null)
                {
                    // Map with values of reference type for FCOs
                    return true;
                }
                if (valueCls != null && ClassUtils.isReferenceType(valueCls))
                {
                    try
                    {
                        String[] impls = getImplementationNamesForReferenceField(fmd,
                            FieldRole.ROLE_MAP_VALUE, clr, mmgr);
                        if (impls != null)
                        {
                            valueCls = clr.classForName(impls[0]);
                            if (ec.getApiAdapter().isPersistable(valueCls))
                            {
                                // Map with values of reference type for FCOs
                                return true;
                            }
                        }
                    }
                    catch (NucleusUserException nue)
                    {
                        // No implementations found so not persistable
                    }
                }
            }
        }
        else if (fmd.hasArray())
        {
            if (mmgr.getApiAdapter().isPersistable(fmd.getType().getComponentType()))
            {
                // PersistenceCapable[]
                return true;
            }
            else
            {
                String elementType = fmd.getArray().getElementType();
                Class elementCls = clr.classForName(elementType);
                if (mmgr.getApiAdapter().isPersistable(elementCls))
                {
                    // Array of reference type for FCOs
                    return true;
                }
                else if (mmgr.getMetaDataForImplementationOfReference(elementCls, null, clr) != null)
                {
                    // Array of reference type for FCOs
                    return true;
                }
                else if (elementCls != null && ClassUtils.isReferenceType(elementCls))
                {
                    try
                    {
                        String[] impls = getImplementationNamesForReferenceField(fmd, 
                            FieldRole.ROLE_ARRAY_ELEMENT, clr, mmgr);
                        if (impls != null)
                        {
                            elementCls = clr.classForName(impls[0]);
                            if (ec.getApiAdapter().isPersistable(elementCls))
                            {
                                // Array of reference type for FCOs
                                return true;
                            }
                        }
                    }
                    catch (NucleusUserException nue)
                    {
                        // No implementations found so not persistable
                    }
                }
            }
        }
        else
        {
            // 1-1 relation with PC
            if (ClassUtils.isReferenceType(fmd.getType()) &&
                mmgr.getMetaDataForImplementationOfReference(fmd.getType(), null, clr) != null)
            {
                // Reference type for an FCO
                return true;
            }
            if (mmgr.getMetaDataForClass(fmd.getType(), clr) != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method that returns if a field stores a First-Class object (FCO).
     * If a field object is serialised/embedded then doesn't count the object as FCO - use
     * storesPersistable() if you want that not checking.
     * @param fmd MetaData for the field
     * @param ec ExecutionContext
     * @return Whether it stores a FCO
     */
    public boolean storesFCO(AbstractMemberMetaData fmd, ExecutionContext ec)
    {
        if (fmd == null)
        {
            return false;
        }

        ClassLoaderResolver clr = ec.getClassLoaderResolver();
        MetaDataManager mgr = ec.getMetaDataManager();
        if (fmd.isSerialized() || fmd.isEmbedded())
        {
            // Serialised or embedded fields have no FCO
            return false;
        }
        else if (fmd.hasCollection() && !fmd.getCollection().isSerializedElement() && !fmd.getCollection().isEmbeddedElement())
        {
            if (fmd.getCollection().elementIsPersistent())
            {
                // Collection of PC elements
                return true;
            }
            else
            {
                String elementType = fmd.getCollection().getElementType();
                Class elementCls = clr.classForName(elementType);
                if (elementCls != null && ClassUtils.isReferenceType(elementCls) &&
                    mgr.getMetaDataForImplementationOfReference(elementCls, null, clr) != null)
                {
                    // Collection of reference type for FCOs
                    return true;
                }
            }
        }
        else if (fmd.hasMap())
        {
            if (fmd.getMap().keyIsPersistent() && !fmd.getMap().isEmbeddedKey() && !fmd.getMap().isSerializedKey())
            {
                // Map of PC keys
                return true;
            }
            else
            {
                String keyType = fmd.getMap().getKeyType();
                Class keyCls = clr.classForName(keyType);
                if (keyCls != null && ClassUtils.isReferenceType(keyCls) &&
                    mgr.getMetaDataForImplementationOfReference(keyCls, null, clr) != null)
                {
                    // Map with keys of reference type for FCOs
                    return true;
                }
            }
                
            if (fmd.getMap().valueIsPersistent() && !fmd.getMap().isEmbeddedValue() && !fmd.getMap().isSerializedValue())
            {
                // Map of PC values
                return true;
            }
            else
            {
                String valueType = fmd.getMap().getValueType();
                Class valueCls = clr.classForName(valueType);
                if (valueCls != null && ClassUtils.isReferenceType(valueCls) &&
                    mgr.getMetaDataForImplementationOfReference(valueCls, null, clr) != null)
                {
                    // Map with values of reference type for FCOs
                    return true;
                }
            }
        }
        else if (fmd.hasArray() && !fmd.getArray().isSerializedElement() && !fmd.getArray().isEmbeddedElement())
        {
            if (mgr.getApiAdapter().isPersistable(fmd.getType().getComponentType()))
            {
                // PersistenceCapable[]
                return true;
            }
        }
        else
        {
            // 1-1 relation with PC
            if (ClassUtils.isReferenceType(fmd.getType()) &&
                mgr.getMetaDataForImplementationOfReference(fmd.getType(), null, clr) != null)
            {
                // Reference type for an FCO
                return true;
            }
            if (mgr.getMetaDataForClass(fmd.getType(), clr) != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method that splits a comma-separated list of values into a String array (removing whitespace).
     * @param attr The attribute value
     * @return The string components
     */
    public String[] getValuesForCommaSeparatedAttribute(String attr)
    {
        if (attr == null || attr.length() == 0)
        {
            return null;
        }

        String[] values=StringUtils.split(attr, ",");

        // Remove any whitespace around the values
        if (values != null)
        {
            for (int i=0;i<values.length;i++)
            {
                values[i] = values[i].trim();
            }
        }
        return values;
    }

    /**
     * Convenience method to return the class names of the available implementation types for 
     * an interface/Object field, given its required role. Removes all duplicates from the list.
     * @param fmd MetaData for the field
     * @param fieldRole The role of the field
     * @param clr the ClassLoaderResolver
     * @param mmgr MetaData manager
     * @return Names of the classes of the possible implementations of this interface/Object
     * @throws NucleusUserException if no implementation types are found for the reference type field
     */
    public String[] getImplementationNamesForReferenceField(AbstractMemberMetaData fmd, int fieldRole, 
            ClassLoaderResolver clr, MetaDataManager mmgr)
    {
        // Check the JDO2 standard attribute for implementation types
        String[] implTypes = null;

        // TODO Support specification of collection/map implementation types using element-type,key-type,value-type
        /*if (DatastoreField.ROLE_COLLECTION_ELEMENT == role)
        {
            implTypeStr = fmd.getCollection().getElementType();
        }
        else if (DatastoreField.ROLE_MAP_KEY == role)
        {
            implTypeStr = fmd.getMap().getKeyImplementationType();
        }
        else if (DatastoreField.ROLE_MAP_VALUE == role)
        {
            implTypeStr = fmd.getMap().getValueImplementationType();
        }
        else */
        if (FieldRole.ROLE_ARRAY_ELEMENT == fieldRole)
        {
            // TODO Change ElementMetaData to have elementTypes
            String implTypeStr = fmd.getArray().getElementType();
            if (implTypeStr != null)
            {
                implTypes = getValuesForCommaSeparatedAttribute(implTypeStr);
            }
        }
        else
        {
            implTypes = fmd.getFieldTypes();
        }

        // Check if the user has defined an interface type being "implemented by" an interface ("persistent-interface")
        if (implTypes != null && implTypes.length == 1)
        {
            // Single class/interface name specified
            Class implCls = clr.classForName(implTypes[0].trim());
            if (implCls.isInterface())
            {
                // The specified "implementation" is itself an interface so assume it is a "persistent-interface"
                implTypes = mmgr.getClassesImplementingInterface(implTypes[0], clr);
            }
        }

        if (implTypes == null)
        {
            // No implementation(s) specified using JDO2 mechanism, so fallback to extensions
            if (FieldRole.ROLE_COLLECTION_ELEMENT == fieldRole)
            {
                implTypes = fmd.getValuesForExtension("implementation-classes");
            }
            else if (FieldRole.ROLE_ARRAY_ELEMENT == fieldRole)
            {
                implTypes = fmd.getValuesForExtension("implementation-classes");
            }
            else if (FieldRole.ROLE_MAP_KEY == fieldRole)
            {
                implTypes = fmd.getValuesForExtension("key-implementation-classes");
            }
            else if (FieldRole.ROLE_MAP_VALUE == fieldRole)
            {
                implTypes = fmd.getValuesForExtension("value-implementation-classes");
            }
            else
            {
                implTypes = fmd.getValuesForExtension("implementation-classes");
            }
        }

        if (implTypes == null)
        {
            // Nothing specified, so check if it is an interface and if so use the <implements> definition to get some types
            String type = null;
            if (fmd.hasCollection() && fieldRole == FieldRole.ROLE_COLLECTION_ELEMENT)
            {
                type = fmd.getCollection().getElementType();
            }
            else if (fmd.hasMap() && fieldRole == FieldRole.ROLE_MAP_KEY)
            {
                type = fmd.getMap().getKeyType();
            }
            else if (fmd.hasMap() && fieldRole == FieldRole.ROLE_MAP_VALUE)
            {
                type = fmd.getMap().getValueType();
            }
            else if (fmd.hasArray() && fieldRole == FieldRole.ROLE_ARRAY_ELEMENT)
            {
                type = fmd.getType().getComponentType().getName();
            }
            else
            {
                type = fmd.getTypeName();
            }

            if (!type.equals(ClassNameConstants.Object))
            {
                implTypes = mmgr.getClassesImplementingInterface(type,clr);
            }

            if (implTypes == null)
            {
                // Generate error since no implementations available
                throw new NucleusUserException(LOCALISER.msg("044161", fmd.getFullFieldName(), type));
            }
        }

        // Remove all duplicates from the list but retain the original ordering
        int noOfDups = 0;
        for (int i=0;i<implTypes.length;i++)
        {
            for (int j=0;j<i;j++)
            {
                if (implTypes[j].equals(implTypes[i]))
                {
                    noOfDups++;
                    break;
                }
            }
        }
        String[] impls = new String[implTypes.length-noOfDups];
        int n = 0;
        for (int i=0;i<implTypes.length;i++)
        {
            boolean dup = false;
            for (int j=0;j<i;j++)
            {
                if (implTypes[j].equals(implTypes[i]))
                {
                    dup = true;
                    break;
                }
            }
            if (!dup)
            {
                impls[n++] = implTypes[i];
            }
        }

        return impls;
    }

    /**
     * Convenience method to return a boolean from the String value.
     * If the string is null then dflt is returned.
     * @param str The string (should be "true", "false")
     * @param dflt The default
     * @return The boolean to use
     */
    public static boolean getBooleanForString(String str, boolean dflt)
    {
        if (StringUtils.isWhitespace(str))
        {
            return dflt;
        }
        return Boolean.parseBoolean(str);
    }

    /**
     * Convenience method that takes a result set that contains a discriminator column and returns
     * the class name that it represents.
     * @param discrimValue Discriminator value
     * @param dismd Metadata for the discriminator at the root (defining the strategy)
     * @param ec ExecutionContext
     * @return The class name for the object represented by this value
     */
    public static String getClassNameFromDiscriminatorValue(String discrimValue, 
            DiscriminatorMetaData dismd, ExecutionContext ec)
    {
        if (discrimValue == null)
        {
            return null;
        }
        String rowClassName = null;

        ClassLoaderResolver clr = ec.getClassLoaderResolver();
        AbstractClassMetaData baseCmd =
            (AbstractClassMetaData)((InheritanceMetaData)dismd.getParent()).getParent();
        if (dismd.getStrategy() == DiscriminatorStrategy.CLASS_NAME)
        {
            rowClassName = discrimValue;
        }
        else if (dismd.getStrategy() == DiscriminatorStrategy.VALUE_MAP)
        {
            // Check the main class type for the table
            String className = baseCmd.getFullClassName();
            if (dismd.getValue().equals(discrimValue))
            {
                rowClassName = className;
            }
            else
            {
                // Go through all possible subclasses to find one with this value
                Iterator iterator =
                    ec.getStoreManager().getSubClassesForClass(baseCmd.getFullClassName(), true, clr).iterator();
                while (iterator.hasNext())
                {
                    className = (String)iterator.next();
                    AbstractClassMetaData cmd =
                        ec.getMetaDataManager().getMetaDataForClass(className, clr);
                    if (discrimValue.equals(cmd.getInheritanceMetaData().getDiscriminatorMetaData().getValue()))
                    {
                        rowClassName = className;
                        break;
                    }
                }
            }
        }
        return rowClassName;
    }

    /**
     * Searches the meta data tree upwards starting with the given leaf, stops as 
     * soon as it finds an extension with the given key.
     *
     * @param metadata Leaf of the meta data tree, where the search should start
     * @param key The key of the extension
     * @return The value of the extension (null if not existing)
     */
    public static String getValueForExtensionRecursively(MetaData metadata, String key)
    {
        if (metadata == null)
        {
            return null;
        }

        String value = metadata.getValueForExtension(key);
        if (value == null)
        {
            value = getValueForExtensionRecursively(metadata.getParent(), key);
        }

        return value;
    }

    /**
     * Searches the meta data tree upwards starting with the given leaf, stops as 
     * soon as it finds an extension with the given key.
     *
     * @param metadata Leaf of the meta data tree, where the search should start
     * @param key The key of the extension
     * @return The values of the extension (null if not existing)
     */
    public static String[] getValuesForExtensionRecursively(MetaData metadata, String key)
    {
        if (metadata == null)
        {
            return null;
        }

        String[] values = metadata.getValuesForExtension(key);
        if (values == null)
        {
            values = getValuesForExtensionRecursively(metadata.getParent(), key);
        }

        return values;
    }

    /**
     * Convenience method to return if a jdbc-type is numeric.
     * @param jdbcType The type string
     * @return Whether it is numeric
     */
    public static boolean isJdbcTypeNumeric(String jdbcType)
    {
        if (jdbcType == null)
        {
            return false;
        }
        if (jdbcType.equalsIgnoreCase("INTEGER") || jdbcType.equalsIgnoreCase("SMALLINT") ||
            jdbcType.equalsIgnoreCase("TINYINT") || jdbcType.equalsIgnoreCase("NUMERIC") ||
            jdbcType.equalsIgnoreCase("BIGINT"))
        {
            return true;
        }
        return false;
    }

    /**
     * Convenience method to return if a jdbc-type is character based.
     * @param jdbcType The type string
     * @return Whether it is character based
     */
    public static boolean isJdbcTypeString(String jdbcType)
    {
        if (jdbcType == null)
        {
            return false;
        }
        if (jdbcType.equalsIgnoreCase("VARCHAR") || jdbcType.equalsIgnoreCase("CHAR") || 
            jdbcType.equalsIgnoreCase("LONGVARCHAR"))
        {
            return true;
        }
        return false;
    }

    /**
     * Convenience method to return the class metadata for the candidate and optionally its subclasses.
     * Caters for the class being a persistent interface.
     * @param cls The class
     * @param subclasses Include subclasses?
     * @param ec ExecutionContext
     * @return The metadata, starting with the candidate
     * @throws NucleusUserException if candidate is an interface with no metadata (i.e not persistent)
     */
    public static List<AbstractClassMetaData> getMetaDataForCandidates(Class cls, boolean subclasses, 
        ExecutionContext ec)
    {
        ClassLoaderResolver clr = ec.getClassLoaderResolver();
        List<AbstractClassMetaData> cmds = new ArrayList();
        if (cls.isInterface())
        {
            // Query of interface(+subclasses)
            AbstractClassMetaData icmd = ec.getMetaDataManager().getMetaDataForInterface(cls, clr);
            if (icmd == null)
            {
                throw new NucleusUserException("Attempting to query an interface yet it is not declared 'persistent'." +
                    " Define the interface in metadata as being persistent to perform this operation, and make sure" +
                " any implementations use the same identity and identity member(s)");
            }

            String[] impls = ec.getMetaDataManager().getClassesImplementingInterface(cls.getName(), clr);
            for (int i=0;i<impls.length;i++)
            {
                AbstractClassMetaData implCmd = ec.getMetaDataManager().getMetaDataForClass(impls[i], clr);
                cmds.add(implCmd);
                if (subclasses)
                {
                    String[] subclassNames = 
                        ec.getMetaDataManager().getSubclassesForClass(implCmd.getFullClassName(), true);
                    if (subclassNames != null && subclassNames.length > 0)
                    {
                        for (int j=0;j<subclassNames.length;j++)
                        {
                            AbstractClassMetaData subcmd = 
                                ec.getMetaDataManager().getMetaDataForClass(subclassNames[j], clr);
                            cmds.add(subcmd);
                        }
                    }
                }
            }
        }
        else
        {
            // Query of candidate(+subclasses)
            AbstractClassMetaData cmd = ec.getMetaDataManager().getMetaDataForClass(cls, clr);
            cmds.add(cmd);
            if (subclasses)
            {
                String[] subclassNames = ec.getMetaDataManager().getSubclassesForClass(cls.getName(), true);
                if (subclassNames != null && subclassNames.length > 0)
                {
                    for (int j=0;j<subclassNames.length;j++)
                    {
                        AbstractClassMetaData subcmd = 
                            ec.getMetaDataManager().getMetaDataForClass(subclassNames[j], clr);
                        cmds.add(subcmd);
                    }
                }
            }
        }
        return cmds;
    }

    /**
     * Method to take the provided input files and returns the FileMetaData that they implies.
     * Loads the files into the provided MetaDataManager in the process.
     * @param metaDataMgr Manager for MetaData
     * @param clr ClassLoader resolver
     * @param inputFiles Input metadata/class files
     * @return The FileMetaData for the input
     * @throws NucleusException Thrown if error(s) occur in processing the input
     */
    public static FileMetaData[] getFileMetaDataForInputFiles(MetaDataManager metaDataMgr, ClassLoaderResolver clr, 
            String[] inputFiles)
    {
        FileMetaData[] filemds = null;

        // Read in the specified MetaData files - errors in MetaData will return exceptions and so we stop
        String msg = null;
        try
        {
            // Split the input files into MetaData files and classes
            HashSet metadataFiles = new HashSet();
            HashSet classNames = new HashSet();
            for (int i=0;i<inputFiles.length;i++)
            {
                if (inputFiles[i].endsWith(".class"))
                {
                    // Class file
                    URL classFileURL = null;
                    try
                    {
                        classFileURL = new URL("file:" + inputFiles[i]);
                    }
                    catch (Exception e)
                    {
                        msg = LOCALISER.msg(false, "014013", inputFiles[i]);
                        NucleusLogger.METADATA.error(msg);
                        throw new NucleusUserException(msg);
                    }

                    String className = null;
                    try
                    {
                        className = ClassUtils.getClassNameForFileURL(classFileURL);
                    }
                    catch (Exception e)
                    {
                        NucleusLogger.METADATA.info("URL " + inputFiles[i] + " could not be resolved to a class name, so ignoring." +
                            " Specify it as a class explicitly using persistence.xml to overcome this", e);
                    }
                    catch (Error err)
                    {
                        NucleusLogger.METADATA.info("URL " + inputFiles[i] + " could not be resolved to a class name, so ignoring." +
                            " Specify it as a class explicitly using persistence.xml to overcome this", err);
                    }
                    classNames.add(className);
                }
                else
                {
                    // MetaData file
                    metadataFiles.add(inputFiles[i]);
                }
            }

            // Initialise the MetaDataManager using the mapping files and class names
            FileMetaData[] filemds1 = metaDataMgr.loadMetadataFiles(
                (String[])metadataFiles.toArray(new String[metadataFiles.size()]), null);
            FileMetaData[] filemds2 = metaDataMgr.loadClasses(
                (String[])classNames.toArray(new String[classNames.size()]), null);
            filemds = new FileMetaData[filemds1.length + filemds2.length];
            int pos = 0;
            for (int i=0;i<filemds1.length;i++)
            {
                filemds[pos++] = filemds1[i];
            }
            for (int i=0;i<filemds2.length;i++)
            {
                filemds[pos++] = filemds2[i];
            }
        }
        catch (Exception e)
        {
            // Error reading input files
            msg = LOCALISER.msg(false, "014014", e.getMessage());
            NucleusLogger.METADATA.error(msg, e);
            throw new NucleusUserException(msg, e);
        }

        return filemds;
    }
}