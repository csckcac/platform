/**********************************************************************
Copyright (c) 2009 Andy Jefferson and others. All rights reserved.
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

/**
 * Helper class for handling mappings.
 */
public class MappingHelper
{
    /**
     * Convenience method to return an array of positions for datastore columns for the supplied
     * mapping and the initial position value. For example if the mapping has a single datastore
     * column and the initial position is 1 then returns the array {1}.
     * @param initialPosition the initialPosition
     * @param mapping the Mapping
     * @return an array containing indexes for parameters
     */
    public static int[] getMappingIndices(int initialPosition, JavaTypeMapping mapping)
    {
        if (mapping.getNumberOfDatastoreMappings() < 1)
        {
            return new int[]{initialPosition};
        }

        int parameter[] = new int[mapping.getNumberOfDatastoreMappings()];
        for (int i=0; i<parameter.length; i++)
        {
            parameter[i] = initialPosition+i;
        }
        return parameter;
    }
}