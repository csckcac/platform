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
package org.wso2.carbon.policybuilder.ui.internal.assertions;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 10, 2008
 * Time: 3:04:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Consts {
	//main assertion types
	/*
		public static final  int ASSYM_BINDING_ASSERT=999;
		public static final  int ASSYM_INITIATOR_TOKEN_ASSERT=1000;
		public static final  int ASSYM_RECIEPENT_TOKEN_ASSERT=1001;
		public static final  int ASSYM_ALGORITHM_SUITE_ASSERT=1002;
		public static final  int ASSYM_LAYOUT_ASSERT=1003;
		public static final  int ASSYM_INCLUDE_TIMESTAMP_ASSERT=1004;
		public static final  int ASSYM_ENCRYPT_BEFORE_SIGN_ASSERT=1005;
		public static final  int ASSYM_ENCRYPT_SIGNATURE_ASSERT=1006;
		public static final  int ASSYM_PROTECT_TOKENS_ASSERT=1007;
		public static final  int ASSYM_ONLY_SIGN_ENTIRE_HEADERS_AND_BODY_ASSERT=1008;

		public static final  int SYM_BINDING_ASSERT=998;
		public static final  int SYM_ENCRYPTION_TOKEN_ASSERT=1009;
		public static final  int SYM_SIGNATURE_TOKEN_ASSERT=1010;
		public static final  int SYM_PROTECTION_SUITE_ASSERT=1011;
		public static final  int SYM_ALGORITHM_SUITE_ASSERT=1012;
		public static final  int SYM_LAYOUT_ASSERT=1013;
		public static final  int SYM_INCLUDE_TIMESTAMP_ASSERT=1014;
		public static final  int SYM_ENCRYPT_BEFORE_SIGN_ASSERT=1015;
		public static final  int SYM_ENCRYPT_SIGNATURE_ASSERT=1016;
		public static final  int SYM_PROTECT_TOKENS_ASSERT=1017;
		public static final  int SYM_ONLY_SIGN_ENTIRE_HEADERS_AND_BODY_ASSERT=1018;

		public static final  int TRANS_TRANSPORT_TOKEN_ASSERT=1019;
		public static final  int TRANS_ALGORITHM_SUITE_ASSERT=1020;
		public static final  int TRANS_LAYOUT_ASSERT=1021;
		public static final  int TRANS_INCLUDE_TIMESTAMP_ASSERT=1022;

		public static final  int SIGNED_PARTS_ASSERT=1023;
		public static final  int ENCRYPTED_PARTS_ASSERT=1024;    */

	//namespaces
	public static final String DIGITAL_SIGN_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";
	// public static final String WS_SECURITY_NAMESPACE ="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	//  public static final String WS_UTILITY_NAMESPACE ="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	// public static final String XML_ENCRYPTION_NAMESPACE ="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	//message properties

	public static final String SYM_DERIVEDKEY_PROPERTY = "DerivedKeyToken";
	public static final String SYM_DERIVEDKEY_PROPERTY2 = "http://schemas.xmlsoap.org/ws/2005/02/sc";
	public static final String SYM_SECTOKEN_REF_PROPERTY = "SecurityTokenReference";
	public static final String SYM_REF_PROPERTY = "Reference";
	public static final String SYM_SIGN_METHOD_PROPERTY = "SignatureMethod";
	public static final String SYM_ENC_METHOD_PROPERTY = "EncryptionMethod";
	public static final String TIMESTAMP_PROPERTY = "Timestamp";
	public static final String KEY_IDENTIFIER_PROPERTY = "KeyIdentifier";
	public static final String X509_DATA_PROPERTY = "X509Data";
	public static final String X509_ISSUER_SERIAL_PROPERTY = "X509IssuerSerial";
	public static final String SIGNATURE_PROPERTY = "Signature";
	public static final String ENC_DATA_PROPERTY = "EncryptedData";
	public static final String ENC_BINARY_TOKEN_PROPERTY = "BinarySecurityToken";
	public static final String ENC_KEY_PROPERTY = "EncryptedKey";
	public static final String SEC_TOKEN_CONTEXT_PROPERTY = "SecurityTokenContext";
	public static final String KEY_INFO_PROPERTY = "KeyInfo";
	public static final String KEY_NAME_PROPERTY = "KeyName";
	public static final String DIGEST_METHOD_PROPERTY = "DigestMethod";
	public static final String REFERENCE_LIST_PROPERTY = "ReferenceList";
	public static final String DATA_REFERENCE_PROPERTY = "DataReference";
	public static final String EMBEDDED_PROPERTY = "Embedded";
	public static final String WSA_TO_PROPERTY = "To";
	public static final String SIGN_REF_PROPERTY = "Reference";
	public static final String SOAP_BODY_PROPERTY = "Body";

	//   public static final String SIGNATURE_PROPERTY = "Signature";

	//values
	//
	public static final String IssuerSerial_VALUE = "{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.X509_ISSUER_SERIAL_PROPERTY;
	public static final String KeyIdentifier_VALUE = "{" + Consts.WS_SECURITY_NAMESPACE + "}" + Consts.KEY_IDENTIFIER_PROPERTY;
	public static final String ThumbPrint_VALUE = "http://docs.oasis-open.org/wss/oasis-wss-soap-message-security-1.1#ThumbprintSHA1";

	public static final String WS_SC_NAMESPACE = "http://schemas.xmlsoap.org/ws/2005/02/sc";
	public static final String WS_SECURITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	public static final String WS_UTILITY_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
	public static final String XML_ENCRYPTION_NAMESPACE = "http://www.w3.org/2001/04/xmlenc#";
	public static final String XML_DIGITAL_SIGN_NAMESPACE = "http://www.w3.org/2000/09/xmldsig#";
	public static final String WS_ADDRESSING_NAMESPACE = "http://www.w3.org/2005/08/addressing";
	public static final String SOAP_ENV_NAMESPACE = "http://www.w3.org/2003/05/soap-envelope";
	//algorithms

	public static final String HmacSha1 = "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
	public static final String RsaSha1 = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
	public static final String Sha1 = "http://www.w3.org/2000/09/xmldsig#sha1";
	public static final String Sha256 = "http://www.w3.org/2001/04/xmlenc#sha256";
	public static final String Sha512 = "http://www.w3.org/2001/04/xmlenc#sha512";
	public static final String Aes128 = "http://www.w3.org/2001/04/xmlenc#aes128-cbc";
	public static final String Aes192 = "http://www.w3.org/2001/04/xmlenc#aes192-cbc";
	public static final String Aes256 = "http://www.w3.org/2001/04/xmlenc#aes256-cbc";
	public static final String TripleDes = "http://www.w3.org/2001/04/xmlenc#tripledes-cbc";
	public static final String KwAes128 = "http://www.w3.org/2001/04/xmlenc#kw-aes256";
	public static final String KwAes192 = "http://www.w3.org/2001/04/xmlenc#kw-aes192";
	public static final String KwAes256 = "http://www.w3.org/2001/04/xmlenc#kw-aes128";
	public static final String KwTripleDes = "http://www.w3.org/2001/04/xmlenc#kw-tripledes";
	public static final String KwRsaOaep = "http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p";
	public static final String KwRsa15 = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";
	public static final String PSha1 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
	public static final String PSha1L128 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
	public static final String PSha1L192 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
	public static final String PSha1L256 = "http://schemas.xmlsoap.org/ws/2005/02/sc/dk/p_sha1";
	public static final String XPath = "http://www.w3.org/TR/1999/REC-xpath-19991116";
	public static final String XPath20 = "http://www.w3.org/2002/06/xmldsig-filter2";
	public static final String C14n = "http://www.w3.org/2001/10/xml-c14n#";
	public static final String ExC14n = "http://www.w3.org/2001/10/xml-exc-c14n#";
	public static final String SNT = "http://www.w3.org/TR/soap12-n11n";
	public static final String STRT10 = "http://docs.oasis-open.org/wss/2004/xx/oasis-2004xx-wss-soap-message-security-1.0#STR-Transform";
}
