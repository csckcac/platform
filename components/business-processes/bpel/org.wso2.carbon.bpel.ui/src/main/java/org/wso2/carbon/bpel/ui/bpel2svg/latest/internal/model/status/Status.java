/*
   Copyright 2010 Gregor Latuske

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
*/
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status;

/**
 * This interface is the basis of all status enums.
 */
public interface Status {

    /**
     * Returns the name of the status starting with an upper case.
     *
     * @return The name of the status starting with an upper case.
     */
    String getName();

    /**
     * Returns the name of the status with lower case.
     *
     * @return The name of the status with lower case.
     */
    String getNameLowerCase();
}
