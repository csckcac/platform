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

import java.awt.geom.Ellipse2D;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.mapped.DatastoreContainerObject;
import org.datanucleus.store.mapped.MappedStoreManager;

/**
 * Mapping for java.awt.geom.Ellipse2D.Double, maps the x, y, width and height values to double-precision 
 * datastore fields.
 */
public class Ellipse2dDoubleMapping extends SingleFieldMultiMapping
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
        addDatastoreField(ClassNameConstants.DOUBLE); // X
        addDatastoreField(ClassNameConstants.DOUBLE); // Y
        addDatastoreField(ClassNameConstants.DOUBLE); // Width
        addDatastoreField(ClassNameConstants.DOUBLE); // Height
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.mapping.JavaTypeMapping#getJavaType()
     */
    public Class getJavaType()
    {
        return Ellipse2D.Double.class;
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
        Ellipse2D.Double el = (Ellipse2D.Double)value;
        if (index == 0)
        {
            return el.getX();
        }
        else if (index == 1)
        {
            return el.getY();
        }
        else if (index == 2)
        {
            return el.getWidth();
        }
        else if (index == 3)
        {
            return el.getHeight();
        }
        throw new IndexOutOfBoundsException();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.mapping.JavaTypeMapping#setObject(org.datanucleus.ObjectManager, java.lang.Object, int[], java.lang.Object)
     */
    public void setObject(ExecutionContext ec, Object preparedStatement, int[] exprIndex, Object value)
    {
    	Ellipse2D ellipse = (Ellipse2D)value;
        if (ellipse == null)
        {
    		for (int i = 0; i < exprIndex.length; i++) 
    		{
    			getDatastoreMapping(i).setObject(preparedStatement, exprIndex[i], null);					
			}
        }
        else
        {
            getDatastoreMapping(0).setDouble(preparedStatement,exprIndex[0],ellipse.getX());
            getDatastoreMapping(1).setDouble(preparedStatement,exprIndex[1],ellipse.getY());
            getDatastoreMapping(2).setDouble(preparedStatement,exprIndex[2],ellipse.getWidth());
            getDatastoreMapping(3).setDouble(preparedStatement,exprIndex[3],ellipse.getHeight());
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

        double x = getDatastoreMapping(0).getDouble(resultSet,exprIndex[0]); 
        double y = getDatastoreMapping(1).getDouble(resultSet,exprIndex[1]); 
        double width  = getDatastoreMapping(2).getDouble(resultSet,exprIndex[2]); 
        double height = getDatastoreMapping(3).getDouble(resultSet,exprIndex[3]);
        return new Ellipse2D.Double(x, y, width, height);
    }
}