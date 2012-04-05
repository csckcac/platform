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
 * Date: Dec 4, 2008
 * Time: 4:14:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class EncryptedPartsPropertyFactory extends PropertyFactory {

	public static final String K_EncData = "EncData";
	public static final String K_SignRef = "SignatureRef";
	public static final String K_Body = "SoapBody";

	Map getProperties() {
		properties.put(K_EncData, "{" + Consts.XML_ENCRYPTION_NAMESPACE + "}" + Consts.ENC_DATA_PROPERTY);
		properties.put(K_SignRef, "{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.SIGN_REF_PROPERTY);
		properties.put(K_Body, "{" + Consts.SOAP_ENV_NAMESPACE + "}" + Consts.SOAP_BODY_PROPERTY);
		return properties;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
