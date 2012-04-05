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
package org.wso2.carbon.policybuilder.ui.internal.context;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 24, 2008
 * Time: 2:07:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class PolicyContext extends Context {

	public Boolean getValue(String key) {
		Boolean temp = (Boolean) contextProperties.get(key);
		return temp;
	}


	public String getStringValue(String key) {
		String temp = (String) contextProperties.get(key);
		return temp;
	}


	public void setValue(String key, boolean value) {
		Boolean temp = new Boolean(value);
		contextProperties.put(key, temp);
	}

	public void setValue(String key, String value) {
		contextProperties.put(key, value);
	}

	public void setNullValue(String key) {

		//  Boolean temp = new Boolean(value);
		contextProperties.put(key, null);
	}

	public void init() {
		contextProperties = new HashMap();
	}
}
