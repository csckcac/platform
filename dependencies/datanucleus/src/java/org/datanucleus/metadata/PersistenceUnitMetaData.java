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

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * MetaData representation of a "persistence.xml" persistence unit.
 * Corresponds to the JPA spec section 6.2.1
 */
public class PersistenceUnitMetaData extends MetaData
{
    /** Name of the persistence unit. */
    String name = null;

    /** Root of the persistence unit. */
    URI rootURI = null;

    /** Transaction type for this persistence unit. */
    TransactionType transactionType = null;

    /** Description of the persistence unit. */
    String description = null;

    /** Provider for the persistence unit. */
    String provider = null;

    /** Validation Mode for Bean Validator. */
    String validationMode = "AUTO";

    /** shared cache mode */
    String sharedCacheMode = null;
    
    /** JTA data source for the persistence unit. */
    String jtaDataSource = null;

    /** Non-JTA data source for the persistence unit. */
    String nonJtaDataSource = null;

    /** Names of the classes specified. */
    HashSet<String> classNames = null;

    /** Names/URLs of the JAR files specified. */
    HashSet jarFiles = null;

    /** Names of the mapping files specified. */
    HashSet<String> mappingFileNames = null;

    /** Vendor properties. */
    Properties properties = null;

    /** Whether to exclude unlisted classes. */
    boolean excludeUnlistedClasses = false;

    /** Caching policy for persistable objects. */
    String caching = "UNSPECIFIED";

    /**
     * Constructor.
     * @param name Name of the persistence unit
     * @param transactionType Transaction type for this unit
     * @param rootURI Root of the persistence-unit
     */
    public PersistenceUnitMetaData(String name, String transactionType, URI rootURI)
    {
        this.name = name;
        this.transactionType = TransactionType.getValue(transactionType);
        this.rootURI = rootURI;
    }

    /**
     * Accessor for the persistence unit name.
     * @return Name of the persistence unit
     */
    public String getName()
    {
        return name;
    }

    /**
     * Accessor for the persistence unit root.
     * @return Root of the persistence unit
     */
    public URI getRootURI()
    {
        return rootURI;
    }

    /**
     * Accessor for the persistence unit transaction type
     * @return Transaction type for the persistence unit
     */
    public TransactionType getTransactionType()
    {
        return transactionType;
    }

    /**
     * Accessor for the persistence unit caching policy.
     * @return Caching policy: ALL, NONE, ENABLE_SELECTIVE, DISABLE_SELECTIVE, UNSPECIFIED.
     */
    public String getCaching()
    {
        return caching;
    }

    /**
     * Mutator for the unit caching policy
     * @param cache The caching policy: ALL, NONE, ENABLE_SELECTIVE, DISABLE_SELECTIVE, UNSPECIFIED.
     */
    public void setCaching(String cache)
    {
        this.caching = cache;
    }

    /**
     * Accessor for the persistence unit description.
     * @return Description of the persistence unit
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * Mutator for the unit description
     * @param desc The description
     */
    public void setDescription(String desc)
    {
        this.description = desc;
    }

    /**
     * Accessor for the persistence unit provider.
     * @return Provider for the persistence unit
     */
    public String getProvider()
    {
        return provider;
    }

    /**
     * Mutator for the unit provider
     * @param provider The provider
     */
    public void setProvider(String provider)
    {
        this.provider = provider;
    }

    /**
     * Accessor for the persistence unit JTA data source.
     * @return JTA data source for the persistence unit
     */
    public String getJtaDataSource()
    {
        return jtaDataSource;
    }

    /**
     * Mutator for the unit JTA data source
     * @param data The JTA data source
     */
    public void setJtaDataSource(String data)
    {
        this.jtaDataSource = data;
    }

    /**
     * Accessor for the persistence unit non-JTA data source.
     * @return non-JTA data source for the persistence unit
     */
    public String getNonJtaDataSource()
    {
        return nonJtaDataSource;
    }

    /**
     * Mutator for the unit non-JTA data source
     * @param data The non-JTA data source
     */
    public void setNonJtaDataSource(String data)
    {
        this.nonJtaDataSource = data;
    }

    /**
     * Mutator for the validation mode
     * @param validationMode AUTO, CALLBACK or NONE
     */
    public void setValidationMode(String validationMode)
    {
        this.validationMode = validationMode;
    }
    
    /**
     * Accessor to the Validation Mode
     * @return AUTO, CALLBACK or NONE
     */
    public String getValidationMode()
    {
        return validationMode;
    }

    /**
     * Method to exclude unlisted classes
     */
    public void setExcludeUnlistedClasses()
    {
        excludeUnlistedClasses = true;
    }

    /**
     * Whether we should exclude any unlisted classes from this persistence-unit.
     * If not then the implementation should scan below the root URL for any classes.
     * @return Whether to exclude unlisted classes.
     */
    public boolean getExcludeUnlistedClasses()
    {
        return excludeUnlistedClasses;
    }

    /**
     * Method to add a class name to the persistence unit.
     * @param className Name of the class
     */
    public void addClassName(String className)
    {
        if (classNames == null)
        {
            classNames = new HashSet();
        }
        classNames.add(className);
    }

    /**
     * Method to add a jar file to the persistence unit.
     * @param jarName Jar file name
     */
    public void addJarFile(String jarName)
    {
        if (jarFiles == null)
        {
            jarFiles = new HashSet();
        }
        jarFiles.add(jarName);
    }

    /**
     * Method to add a jar file to the persistence unit.
     * @param jarURL Jar file URL
     */
    public void addJarFile(URL jarURL)
    {
        if (jarFiles == null)
        {
            jarFiles = new HashSet();
        }
        jarFiles.add(jarURL);
    }

    /**
     * Convenience method to clear out all jar files.
     */
    public void clearJarFiles()
    {
        if (jarFiles != null)
        {
            jarFiles.clear();
        }
        jarFiles = null;
    }

    /**
     * Method to add a mapping file to the persistence unit.
     * @param mappingFile Mapping file name
     */
    public void addMappingFile(String mappingFile)
    {
        if (mappingFileNames == null)
        {
            mappingFileNames = new HashSet();
        }
        mappingFileNames.add(mappingFile);
    }

    /**
     * Method to add a vendor property to the persistence unit.
     * @param key Property name
     * @param value Property value
     */
    public void addProperty(String key, String value)
    {
        if (key == null || value == null)
        {
            // Ignore null properties
            return;
        }
        if (properties == null)
        {
            properties = new Properties();
        }
        properties.setProperty(key, value);
    }

    /**
     * Accessor for the class names for this persistence unit.
     * @return The class names
     */
    public HashSet getClassNames()
    {
        return classNames;
    }

    /**
     * Accessor for the class names for this persistence unit.
     * @return The mapping files
     */
    public HashSet getMappingFiles()
    {
        return mappingFileNames;
    }

    /**
     * Accessor for the jar files for this persistence unit.
     * The contents of the Set may be Strings (the names) or URLs
     * @return The jar names
     */
    public HashSet getJarFiles()
    {
        return jarFiles;
    }

    /**
     * Accessor for the properties for this persistence unit.
     * @return The properties
     */
    public Properties getProperties()
    {
        return properties;
    }

    // ------------------------------- Utilities -------------------------------

    /**
     * Returns a string representation of the object using a prefix
     * This can be used as part of a facility to output a MetaData file. 
     * @param prefix prefix string
     * @param indent indent string
     * @return a string representation of the object.
     */
    public String toString(String prefix,String indent)
    {
        // Field needs outputting so generate metadata
        StringBuffer sb = new StringBuffer();
        sb.append(prefix).append("<persistence-unit name=\"" + name + "\"");
        if (transactionType != null)
        {
            sb.append(" transaction-type=\"" + transactionType + "\"");
        }
        sb.append(">\n");

        // Description of unit
        if (description != null)
        {
            sb.append(prefix).append(indent).append("<description>" + description + "</description>\n");
        }

        // Provider for unit
        if (provider != null)
        {
            sb.append(prefix).append(indent).append("<provider>" + provider + "</provider>\n");
        }

        // JTA data source for unit
        if (jtaDataSource != null)
        {
            sb.append(prefix).append(indent).append("<jta-data-source>" + jtaDataSource + "</jta-data-source>\n");
        }

        // Non-JTA data source for unit
        if (nonJtaDataSource != null)
        {
            sb.append(prefix).append(indent).append("<non-jta-data-source>" + nonJtaDataSource + "</non-jta-data-source>\n");
        }

        // Add class names
        if (classNames != null)
        {
            Iterator<String> iter = classNames.iterator();
            while (iter.hasNext())
            {
                sb.append(prefix).append(indent).append("<class>" + iter.next() + "</class>\n");
            }
        }

        // Add mapping files
        if (mappingFileNames != null)
        {
            Iterator<String> iter = mappingFileNames.iterator();
            while (iter.hasNext())
            {
                sb.append(prefix).append(indent).append("<mapping-file>" + iter.next() + "</mapping-file>\n");
            }
        }

        // Add jar files
        if (jarFiles != null)
        {
            Iterator iter = jarFiles.iterator();
            while (iter.hasNext())
            {
                sb.append(prefix).append(indent).append("<jar-file>" + iter.next() + "</jar-file>\n");
            }
        }

        // Add properties
        if (properties != null)
        {
            sb.append(prefix).append(indent).append("<properties>\n");
            Set entries = properties.entrySet();
            Iterator iter = entries.iterator();
            while (iter.hasNext())
            {
                Map.Entry entry = (Map.Entry)iter.next();
                sb.append(prefix).append(indent).append(indent).append("<property name=" + entry.getKey() + 
                    " value=" + entry.getValue() + "</property>\n");
            }
            sb.append(prefix).append(indent).append("</properties>\n");
        }

        if (excludeUnlistedClasses)
        {
            sb.append(prefix).append(indent).append("<exclude-unlisted-classes/>\n");
        }

        sb.append(prefix).append("</persistence-unit>\n");
        return sb.toString();
    }
}