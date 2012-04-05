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
package org.wso2.carbon.bpel.ui.bpel2svg.latest.internal.service.mapping;


/**
 * This class is used to map the status, that is of type M to the according status S.
 * 
 * @param <S> The type of the status in the data model.
 * @param <M> The type of the status in the implementation of the workflow engine adapter.
 * @author Gregor Latuske
 */
public interface StatusMapping<S, M> {

	/**
	 * Maps the value of type M to the associated status of type S.
	 * <p>
	 * If no mapping is possible a {@link IllegalArgumentException} is thrown.
	 *
	 * @param value The value to map.
	 * @return The mapped status.
	 * @throws IllegalArgumentException
	 */
	public S mapToStatus(M value) throws IllegalArgumentException;

}
