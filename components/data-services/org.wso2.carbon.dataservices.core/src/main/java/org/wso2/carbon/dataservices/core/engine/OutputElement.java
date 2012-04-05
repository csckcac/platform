/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.dataservices.core.engine;

import java.util.Set;

import javax.xml.stream.XMLStreamWriter;

import org.wso2.carbon.dataservices.core.DataServiceFault;

/**
 * Represents an entity which can yield a result, i.e. elements in a result section.
 */
public interface OutputElement {

	/**
	 * Executes and writes the contents of this element, given the parameters.
	 */
	void execute(XMLStreamWriter xmlWriter, 
			ExternalParamCollection params, int queryLevel)
			throws DataServiceFault;
	
	/**
	 * Returns the requires roles to view this element.
	 */
	Set<String> getRequiredRoles();
	
	/**
	 * Checks if this element is optional, 
	 * if so, this has to be mentioned in the schema for WSDL generation.
	 */
	boolean isOptional();
	
}
