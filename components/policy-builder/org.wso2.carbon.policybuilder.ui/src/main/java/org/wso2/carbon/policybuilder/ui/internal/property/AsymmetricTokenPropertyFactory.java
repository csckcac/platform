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
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Dec 2, 2008
 * Time: 4:38:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class AsymmetricTokenPropertyFactory extends PropertyFactory {

	public static final int INITIATOR_TOKEN = 1001;
	public static final int RECIPIENT_TOKEN = 1002;

	public static final String K_Signature = "Signature";
	public static final String K_EncryptKey = "EncryptedKey";
	public static final String K_SecurityToken = "SecurityToken";
	public static final String K_BinarySecurityToken = "BinarySecurityToken";
	public static final String K_Ref = "Ref";
	public static final String K_SecurityTokenContext = "TokenContext";

	Map getProperties() {
		properties.put(K_Signature, "{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.SIGNATURE_PROPERTY);
		properties.put(K_EncryptKey, "{" + Consts.XML_ENCRYPTION_NAMESPACE + "}" + Consts.ENC_KEY_PROPERTY);
		ArrayList securityTkns = new ArrayList();
		securityTkns.add("{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.ENC_BINARY_TOKEN_PROPERTY);
		securityTkns.add("{" + Consts.WS_SC_NAMESPACE + "}" + Consts.SEC_TOKEN_CONTEXT_PROPERTY);
		properties.put(K_SecurityToken, securityTkns);
		properties.put(K_BinarySecurityToken, "{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.ENC_BINARY_TOKEN_PROPERTY);
		properties.put(K_Ref, "{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.SYM_REF_PROPERTY);
		properties.put(K_SecurityTokenContext, "{" + Consts.WS_SC_NAMESPACE + "}" + Consts.SEC_TOKEN_CONTEXT_PROPERTY);
		return properties;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
