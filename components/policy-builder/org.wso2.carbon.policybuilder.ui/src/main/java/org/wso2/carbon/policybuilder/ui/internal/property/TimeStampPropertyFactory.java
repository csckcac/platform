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

import org.wso2.carbon.policybuilder.ui.internal.assertions.Consts;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 18, 2008
 * Time: 2:17:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class TimeStampPropertyFactory extends PropertyFactory {

	public static final String K_TimeStamp = "TimeStamp";


	public Map getProperties() {
		properties.put(K_TimeStamp, "{" + Consts.WS_UTILITY_NAMESPACE + "}" + Consts.TIMESTAMP_PROPERTY);
		//  properties.put(K_STokenRef,"{"+Consts.WS_SECURITY_NAMESPACE+"}"+ Consts.SYM_SECTOKEN_REF_PROPERTY);
		return properties;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
