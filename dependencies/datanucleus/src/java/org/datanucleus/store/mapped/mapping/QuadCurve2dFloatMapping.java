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

import java.awt.geom.QuadCurve2D;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.mapped.DatastoreContainerObject;
import org.datanucleus.store.mapped.MappedStoreManager;

/**
 * Mapping for java.awt.geom.QuadCurve2D.Float, maps the x1, y1, ctrlx, ctrly, x2 and y2 values to float-precision
 * datastore fields.
 */
public class QuadCurve2dFloatMapping extends SingleFieldMultiMapping
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
        addDatastoreField(ClassNameConstants.FLOAT); // X1
        addDatastoreField(ClassNameConstants.FLOAT); // Y1
        addDatastoreField(ClassNameConstants.FLOAT); // CtrlX
        addDatastoreField(ClassNameConstants.FLOAT); // CtrlY
        addDatastoreField(ClassNameConstants.FLOAT); // X2
        addDatastoreField(ClassNameConstants.FLOAT); // Y2
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.mapping.JavaTypeMapping#getJavaType()
     */
    public Class getJavaType()
    {
        return QuadCurve2D.Float.class;
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
        QuadCurve2D.Float qc = (QuadCurve2D.Float)value;
        if (index == 0)
        {
            return qc.getX1();
        }
        else if (index == 1)
        {
            return qc.getY1();
        }
        else if (index == 2)
        {
            return qc.getCtrlX();
        }
        else if (index == 3)
        {
            return qc.getCtrlY();
        }
        else if (index == 4)
        {
            return qc.getX2();
        }
        else if (index == 5)
        {
            return qc.getY2();
        }
        throw new IndexOutOfBoundsException();
    }

    /* (non-Javadoc)
     * @see org.datanucleus.store.mapping.JavaTypeMapping#setObject(org.datanucleus.ObjectManager, java.lang.Object, int[], java.lang.Object)
     */
    public void setObject(ExecutionContext ec, Object preparedStatement, int[] exprIndex, Object value)
    {
    	QuadCurve2D.Float line = (QuadCurve2D.Float)value;
        if (line == null)
        {
    		for (int i = 0; i < exprIndex.length; i++) 
    		{
    			getDatastoreMapping(i).setObject(preparedStatement, exprIndex[i], null);					
			}
        }
        else
        {
            getDatastoreMapping(0).setFloat(preparedStatement,exprIndex[0],line.x1);
            getDatastoreMapping(1).setFloat(preparedStatement,exprIndex[1],line.y1);
            getDatastoreMapping(2).setFloat(preparedStatement,exprIndex[2],line.ctrlx);
            getDatastoreMapping(3).setFloat(preparedStatement,exprIndex[3],line.ctrly);
            getDatastoreMapping(4).setFloat(preparedStatement,exprIndex[4],line.x2);
            getDatastoreMapping(5).setFloat(preparedStatement,exprIndex[5],line.y2);
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

        float x1 = getDatastoreMapping(0).getFloat(resultSet,exprIndex[0]); 
        float y1 = getDatastoreMapping(1).getFloat(resultSet,exprIndex[1]); 
        float ctrlx = getDatastoreMapping(2).getFloat(resultSet,exprIndex[2]); 
        float ctrly = getDatastoreMapping(3).getFloat(resultSet,exprIndex[3]);
        float x2 = getDatastoreMapping(4).getFloat(resultSet,exprIndex[5]); 
        float y2 = getDatastoreMapping(5).getFloat(resultSet,exprIndex[6]);
        return new QuadCurve2D.Float(x1, y1, ctrlx, ctrly, x2, y2);
    }
}