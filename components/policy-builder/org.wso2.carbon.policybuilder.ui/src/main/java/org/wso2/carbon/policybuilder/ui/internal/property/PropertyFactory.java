/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.policybuilder.ui.internal.property;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 13, 2008
 * Time: 12:03:33 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PropertyFactory {

	// protected ArrayList properties;
	protected HashMap properties;

	public PropertyFactory() {

		//  properties = new ArrayList();
		properties = new HashMap();
	}

	abstract Map getProperties();
}
