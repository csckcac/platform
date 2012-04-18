/**********************************************************************
Copyright (c) 2002 Mike Martin and others. All rights reserved.
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
2003 Andy Jefferson - commented
    ...
**********************************************************************/
package org.datanucleus.store.types.sco;

import org.datanucleus.store.fieldmanager.AbstractFieldManager;

/**
 * Utility class to handle the unsetting of owner fields.
 **/
public class UnsetOwners extends AbstractFieldManager
{
    public void storeObjectField(int fieldNumber, Object value)
    {
        if (value instanceof SCO)
        {
            ((SCO)value).unsetOwner();
        }
    }
}