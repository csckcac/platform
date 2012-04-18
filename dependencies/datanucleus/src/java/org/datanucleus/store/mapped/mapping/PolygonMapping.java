/**********************************************************************
Copyright (c) 2007 Thomas Marti and others. All rights reserved.
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
package org.datanucleus.store.mapped.mapping;

import java.awt.Polygon;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.mapped.DatastoreContainerObject;
import org.datanucleus.store.mapped.MappedStoreManager;

/**
 * Mapping for java.awt.Polygon, maps the points to int-precision datastore fields.
 */
public class PolygonMapping extends SingleFieldMultiMapping
{
    /* (non-Javadoc)
     * @see org.datanucleus.store.mapping.JavaTypeMapping#initialize(AbstractMemberMetaData, DatastoreContainerObject, ClassLoaderResolver)
     */
    public void initialize(AbstractMemberMetaData fmd, DatastoreContainerObject container, ClassLoaderResolver clr)
    {
        super.initialize(fmd, container, clr);
        addDatastoreFields();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.mapped.mapping.JavaTypeMapping#initialize(MappedStoreManager, java.lang.String)
     */
    public void initialize(MappedStoreManager storeMgr, String type)
    {
        super.initialize(storeMgr, type);
        addDatastoreFields();
    }

    protected void addDatastoreFields()
    {
		addDatastoreField(ClassNameConstants.INT_ARRAY); // X-Points
        addDatastoreField(ClassNameConstants.INT_ARRAY); // Y-Points
        addDatastoreField(ClassNameConstants.INT); // Npoints
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.mapping.JavaTypeMapping#getJavaType()
     */
    public Class getJavaType()
    {
        return Polygon.class;
    }

    /**
     * Method to return the value to be stored in the specified datastore index given the overall
     * value for this java type.
     * @param index The datastore index
     * @param value The overall value for this java type
     * @return The value for this datastore index
     */
    public Object getValueForDatastoreMapping(NucleusContext nucleusCtx, int index, Object value)
    {
        Polygon poly = (Polygon)value;
        if (index == 0)
        {
            return poly.xpoints;
        }
        else if (index == 1)
        {
            return poly.ypoints;
        }
        else if (index == 2)
        {
            return poly.npoints;
        }
        throw new IndexOutOfBoundsException();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.mapping.JavaTypeMapping#setObject(org.datanucleus.ObjectManager, java.lang.Object, int[], java.lang.Object)
     */
    public void setObject(ExecutionContext ec, Object preparedStatement, int[] exprIndex, Object value)
    {
    	Polygon poly = (Polygon)value;
        if (poly == null)
        {
            getDatastoreMapping(0).setObject(preparedStatement, exprIndex[0], null);
            getDatastoreMapping(1).setObject(preparedStatement, exprIndex[1], null);
            getDatastoreMapping(2).setObject(preparedStatement, exprIndex[2], null);
        }
        else
        {
            getDatastoreMapping(0).setObject(preparedStatement,exprIndex[0],poly.xpoints);
            getDatastoreMapping(1).setObject(preparedStatement,exprIndex[1],poly.ypoints);
            getDatastoreMapping(2).setInt(preparedStatement,exprIndex[2],poly.npoints);
        }
    }
    
    /* (non-Javadoc)
     * @see org.datanucleus.store.mapping.JavaTypeMapping#getObject(org.datanucleus.ObjectManager, java.lang.Object, int[])
     */
    public Object getObject(ExecutionContext ec, Object resultSet, int[] exprIndex)
    {
        // Check for null entries
        if (getDatastoreMapping(0).getObject(resultSet, exprIndex[0]) == null)
        {
            return null;
        }

        int[] xpoints = (int[])getDatastoreMapping(0).getObject(resultSet,exprIndex[0]); 
        int[] ypoints = (int[])getDatastoreMapping(1).getObject(resultSet,exprIndex[1]); 
        int npoints = getDatastoreMapping(2).getInt(resultSet,exprIndex[2]); 
        return new Polygon(xpoints, ypoints, npoints);
    }
}