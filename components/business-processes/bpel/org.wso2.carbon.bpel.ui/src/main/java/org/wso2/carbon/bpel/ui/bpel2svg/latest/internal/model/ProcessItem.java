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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model;

import org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.model.status.Status;

/**
 * This interface is the basis for all selectable classes with status.
 * 
 * @author Gregor Latuske
 */
public interface ProcessItem<S extends Status> {

	/**
	 * Returns the status of the entry.
	 * 
	 * @return The status of the entry.
	 */
	public S getStatus();

}
