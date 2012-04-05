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

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 11, 2008
 * Time: 4:38:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessageProperty extends Property {

	private HashMap properties;
	private PropertyFactory msgPropFactory;


	public MessageProperty(PropertyFactory pf) {
		this.msgPropFactory = pf;
		properties = (HashMap) pf.getProperties();
	}

	public int getPropertiesCount() {
		return properties.size();
	}

	public Object getProperties(String key) {
		return properties.get(key);
	}

	public boolean contains(Object value) {
		return properties.containsValue(value);
	}


	public void setProperty(String property, Object value) {
		if (property != null) {
			properties.put(property, value);
		}
	}
}
