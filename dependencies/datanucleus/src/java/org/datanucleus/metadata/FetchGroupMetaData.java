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
2004 Andy Jefferson - changed to extend MetaData
2004 Andy Jefferson - added toString()
    ...
**********************************************************************/
package org.datanucleus.metadata;

import java.util.ArrayList;
import java.util.List;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.util.StringUtils;

/**
 * A fetch group defines a particular loaded state for an object graph. 
 * It specifies fields/properties to be loaded for all of the instances in the graph when
 * this fetch group is active.
 */
public class FetchGroupMetaData extends MetaData
{
    /**
     * The post-load attribute on the fetch-group element indicates whether the
     * jdoPost-Load callback will be made when the fetch group is loaded. It
     * defaults to false, for all fetch groups except the default fetch group,
     * on which it defaults to true.
     */
    Boolean postLoad;

    /**
     * The name attribute on a field element contained within a fetch-group
     * element is the name of field in the enclosing class or a dot-separated
     * expression identifying a field reachable from the class by navigating a
     * reference, collection or map. For maps of persistencecapable classes
     * "#key" or "#value" may be appended to the name of the map field to
     * navigate the key or value respectively (e.g. to include a field of the
     * key class or value class in the fetch group).
     * 
     * For collection and arrays of persistence-capable classes, "#element" may
     * be appended to the name of the field to navigate the element. This is
     * optional; if omitted for collections and arrays, #element is assumed.
     */
    final String name;

    /**
     * A contained fetch-group element indicates that the named group is to be
     * included in the group being defined. Nested fetch group elements are
     * limited to only the name attribute.
     */
    protected FetchGroupMetaData[] fetchGroupMetaData;

    /** members declared to be in this fetch group. */
    protected AbstractMemberMetaData[] memberMetaData;
    
    // -------------------------------------------------------------------------
    // Fields below here are used in the parsing process, up to the call to
    // initialise(). Thereafter they are typically cleared out and shouldn't be used.

    /**
     * A contained fetch-group element indicates that the named group is to be
     * included in the group being defined. Nested fetch group elements are
     * limited to only the name attribute.
     */
    protected List<FetchGroupMetaData> fetchGroups = new ArrayList();    

    /** members (fields/properties) declared to be in this fetch group. */
    protected List<AbstractMemberMetaData> members = new ArrayList();    

    /**
     * Constructor for a named fetch group. Set fields using setters, before populate().
     * @param name Name of fetch group
     */
    public FetchGroupMetaData(String name)
    {
        this.name = name;
    }

    /**
     * Initialisation method. This should be called AFTER using the populate
     * method if you are going to use populate. It creates the internal
     * convenience arrays etc needed for normal operation.
     */
    public void initialise(ClassLoaderResolver clr, MetaDataManager mmgr)
    {
        if (this.postLoad == null)
        {
            this.postLoad = Boolean.FALSE;
        }

        if (members.isEmpty())
        {
            memberMetaData = new AbstractMemberMetaData[0];
        }
        else
        {
            memberMetaData = new AbstractMemberMetaData[members.size()];
            for (int i=0; i<memberMetaData.length; i++)
            {
                memberMetaData[i] = members.get(i);
            }
        }

        if (fetchGroups.isEmpty())
        {
            fetchGroupMetaData = new FetchGroupMetaData[0];
        }
        else
        {
            fetchGroupMetaData = new FetchGroupMetaData[fetchGroups.size()];
            for (int i=0; i<fetchGroupMetaData.length; i++)
            {
                fetchGroupMetaData[i] = fetchGroups.get(i);
            }
        }
        for (int i=0; i<fetchGroupMetaData.length; i++)
        {
            fetchGroupMetaData[i].initialise(clr, mmgr);
        }

        super.initialise(clr, mmgr);

        // Clear out parsing data
        fetchGroups.clear();
        fetchGroups = null;
        members.clear();
        members = null;
    }

    public final String getName()
    {
        return name;
    }

    public final Boolean getPostLoad()
    {
        return postLoad;
    }

    public FetchGroupMetaData setPostLoad(Boolean postLoad)
    {
        this.postLoad = postLoad;
        return this;
    }

    public FetchGroupMetaData setPostLoad(String postLoad)
    {
        if (!StringUtils.isWhitespace(postLoad))
        {
            if (postLoad.equalsIgnoreCase("true"))
            {
                this.postLoad = Boolean.TRUE;
            }
            else if (postLoad.equalsIgnoreCase("false"))
            {
                this.postLoad = Boolean.FALSE;
            }
        }
        return this;
    }

    /**
     * Accessor for fetchGroupMetaData
     * @return Returns the fetchGroupMetaData.
     */
    public final FetchGroupMetaData[] getFetchGroupMetaData()
    {
        return fetchGroupMetaData;
    }

    /**
     * Accessor for metadata for the members of this group.
     * @return Returns the metadata for members
     */
    public final AbstractMemberMetaData[] getMemberMetaData()
    {
        return memberMetaData;
    }

    public int getNumberOfMembers()
    {
        return members.size();
    }

    /**
     * Add a new FetchGroupMetaData
     * @param fgmd the fetch group
     */
    public void addFetchGroup(FetchGroupMetaData fgmd)
    {
        fetchGroups.add(fgmd);
        fgmd.parent = this;
    }

    /**
     * Add a new field/property.
     * @param mmd the field/property metadata
     */
    public void addMember(AbstractMemberMetaData mmd)
    {
        members.add(mmd);
        mmd.parent = this;
    }

    /**
     * Method to create a new field, add it, and return it.
     * @return The field metadata
     */
    public FieldMetaData newFieldMetaData(String name)
    {
        FieldMetaData fmd = new FieldMetaData(this, name);
        addMember(fmd);
        return fmd;
    }

    /**
     * Method to create a new property, add it, and return it.
     * @return The property metadata
     */
    public PropertyMetaData newPropertyMetaData(String name)
    {
        PropertyMetaData pmd = new PropertyMetaData(this, name);
        addMember(pmd);
        return pmd;
    }

    // ----------------------------- Utilities ------------------------------------

    /**
     * Returns a string representation of the object.
     * This can be used as part of a facility to output a MetaData file. 
     * @param prefix prefix string
     * @param indent indent string
     * @return a string representation of the object.
     */
    public String toString(String prefix, String indent)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(prefix).append("<fetch-group name=\"" + name + "\"\n");

        // Add fetch-groups
        if (fetchGroupMetaData != null)
        {
            for (int i=0; i<fetchGroupMetaData.length; i++)
            {
                sb.append(fetchGroupMetaData[i].toString(prefix + indent, indent));
            }
        }

        // Add fields
        if (memberMetaData != null)
        {
            for (int i=0; i<memberMetaData.length; i++)
            {
                sb.append(memberMetaData[i].toString(prefix + indent, indent));
            }
        }

        sb.append(prefix + "</fetch-group>\n");
        return sb.toString();
    }
}