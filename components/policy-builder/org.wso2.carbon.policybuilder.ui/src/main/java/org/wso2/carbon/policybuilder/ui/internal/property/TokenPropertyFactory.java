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
 * Date: Nov 19, 2008
 * Time: 2:26:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class TokenPropertyFactory extends PropertyFactory {

	public static final String K_BinarySecurityToken = "BinaryToken";
	public static final String K_SecurityTokenContext = "TokenContext";
	public static final String K_EncryptKey = "EncryptedKey";

	public static final String K_KeyInfo = "KeyInfo";
	public static final String K_Signature = "Signature";
	public static final String K_Ref = "Ref";
	public static final String K_ValueType = "ValueType";
	public static final String K_X509RefValueType = "XRefValueType";
	public static final String K_X509DirectValueType = "XDirValueType";
	public static final String K_KeyIdentifier = "KeyId";
	public static final String K_ThumbPrint = "ThumbPrint";
	public static final String K_X509 = "X509";
	public static final String K_KeyName = "KeyName";
	public static final String K_IssuerSerial = "IssuerSerial";


	public Map getProperties() {
		properties.put(K_BinarySecurityToken, "{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.ENC_BINARY_TOKEN_PROPERTY);
		properties.put(K_EncryptKey, "{" + Consts.XML_ENCRYPTION_NAMESPACE + "}" + Consts.ENC_KEY_PROPERTY);
		ArrayList valueTypes = new ArrayList();
		valueTypes.add("http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#ThumbprintSHA1");
		valueTypes.add("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509SubjectKeyIdentifier");
		valueTypes.add("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-3.0#X509SubjectKeyIdentifier");
		valueTypes.add("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
		valueTypes.add("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v1");
		properties.put(K_ValueType, valueTypes);
		ArrayList x509ValueTypes = new ArrayList();
		x509ValueTypes.add("http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#ThumbprintSHA1");
		x509ValueTypes.add("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509SubjectKeyIdentifier");
		x509ValueTypes.add("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-3.0#X509SubjectKeyIdentifier");
		properties.put(K_X509RefValueType, x509ValueTypes);
		x509ValueTypes = new ArrayList();
		x509ValueTypes.add("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
		x509ValueTypes.add("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v1");
		properties.put(K_X509DirectValueType, x509ValueTypes);
		properties.put(K_KeyInfo, "{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.KEY_INFO_PROPERTY);
		properties.put(K_Signature, "{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.SIGNATURE_PROPERTY);
		properties.put(K_Ref, "{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.SYM_REF_PROPERTY);
		ArrayList keyName = new ArrayList();

		//  keyName.add("{"+Consts.XML_DIGITAL_SIGN_NAMESPACE+"}"+ Consts.X509_DATA_PROPERTY);
		keyName.add("{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.KEY_NAME_PROPERTY);
		properties.put(K_KeyName, keyName);
		ArrayList x509 = new ArrayList();
		x509.add("{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.X509_DATA_PROPERTY);
		x509.add("{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.X509_ISSUER_SERIAL_PROPERTY);
		properties.put(K_X509, x509);
		properties.put(K_KeyIdentifier, "{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.KEY_IDENTIFIER_PROPERTY);
		properties.put(K_SecurityTokenContext, "{" + Consts.WS_SC_NAMESPACE + "}" + Consts.SEC_TOKEN_CONTEXT_PROPERTY);
		properties.put(K_ThumbPrint, "http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#ThumbprintSHA1");
		properties.put(K_IssuerSerial, "{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.X509_ISSUER_SERIAL_PROPERTY);
		return properties;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
