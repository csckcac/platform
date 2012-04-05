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

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 13, 2008
 * Time: 12:09:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class SymmetricPropertyFactory extends PropertyFactory {

	public static final String K_DerivedKey = "DerivedKey";
	public static final String K_STokenRef = "STokenRef";
	public static final String K_Ref = "Ref";
	public static final String K_SignMethod = "SignMethod";
	public static final String K_EncryptMethod = "EncryptMethod";
	public static final String K_ValueType = "ValueType";
	public static final String K_AlgorithmType = "Algorithm";


	public Map getProperties() {
		properties.put(K_DerivedKey, "{" + Consts.WS_SC_NAMESPACE + "}" + Consts.SYM_DERIVEDKEY_PROPERTY);
		properties.put(K_STokenRef, "{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.SYM_SECTOKEN_REF_PROPERTY);
		properties.put(K_Ref, "{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.SYM_REF_PROPERTY);
		properties.put(K_SignMethod, "{" + Consts.DIGITAL_SIGN_NAMESPACE + "}" + Consts.SYM_SIGN_METHOD_PROPERTY);
		properties.put(K_EncryptMethod, "{" + Consts.XML_ENCRYPTION_NAMESPACE + "}" + Consts.SYM_ENC_METHOD_PROPERTY);
		ArrayList valueTypes = new ArrayList();
		valueTypes.add("http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#EncryptedKey");
		properties.put(K_ValueType, valueTypes);
		ArrayList algoTypes = new ArrayList();
		algoTypes.add("http://www.w3.org/2000/09/xmldsig#hmac-sha1");
		//algoTypes.add("http://www.w3.org/2001/04/xmlenc#aes256-cbc");
		algoTypes.add("http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p");
		properties.put(K_AlgorithmType, algoTypes);

		//properties.put(Consts.SYM_DERIVEDKEY_PROPERTY2,"");
		return properties;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
