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
 * Time: 2:06:47 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Context {

	protected HashMap contextProperties = new HashMap();

	public abstract Boolean getValue(String key);

	public abstract String getStringValue(String key);

	public abstract void setValue(String key, boolean value);

	public abstract void setNullValue(String key);

	public abstract void setValue(String key, String value);

	public abstract void init();
}
