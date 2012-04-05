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

import org.apache.ws.secpolicy.Constants;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 25, 2008
 * Time: 11:07:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class AlgorithmSuitePropertyFactory extends PropertyFactory {


	public static final String K_EncData = "EncData";
	public static final String K_EncryptKey = "EncryptedKey";
	public static final String K_DigestMethod = "DigestMethod";
	public static final String K_EncryptMethod = "EncryptMethod";
	public static final String K_AlgoList = "AlgoList";


	Map getProperties() {
		properties.put(K_EncData, "{" + Consts.XML_ENCRYPTION_NAMESPACE + "}" + Consts.ENC_DATA_PROPERTY);
		properties.put(K_EncryptKey, "{" + Consts.XML_ENCRYPTION_NAMESPACE + "}" + Consts.ENC_KEY_PROPERTY);
		properties.put(K_DigestMethod, "{" + Consts.XML_DIGITAL_SIGN_NAMESPACE + "}" + Consts.DIGEST_METHOD_PROPERTY);
		properties.put(K_EncryptMethod, "{" + Consts.XML_ENCRYPTION_NAMESPACE + "}" + Consts.SYM_ENC_METHOD_PROPERTY);
		ArrayList Basic256 = new ArrayList();
		Basic256.add(Consts.Sha1);
		Basic256.add(Consts.Aes256);
		Basic256.add(Consts.KwRsaOaep);
		Basic256.add(Constants.ALGO_SUITE_BASIC256);
		ArrayList Basic192 = new ArrayList();
		Basic192.add(Consts.Sha1);
		Basic192.add(Consts.Aes192);
		Basic192.add(Consts.KwRsaOaep);
		Basic192.add(Constants.ALGO_SUITE_BASIC192);
		ArrayList Basic128 = new ArrayList();
		Basic128.add(Consts.Sha1);
		Basic128.add(Consts.Aes128);
		Basic128.add(Consts.KwRsaOaep);
		Basic128.add(Constants.ALGO_SUITE_BASIC128);
		ArrayList TripleDes = new ArrayList();
		TripleDes.add(Consts.Sha1);
		TripleDes.add(Consts.TripleDes);
		TripleDes.add(Consts.KwRsaOaep);
		TripleDes.add(Constants.ALGO_SUITE_TRIPLE_DES);
		ArrayList Basic256Rsa15 = new ArrayList();
		Basic256Rsa15.add(Consts.Sha1);
		Basic256Rsa15.add(Consts.Aes256);
		Basic256Rsa15.add(Consts.KwRsa15);
		Basic256Rsa15.add(Constants.ALGO_SUITE_BASIC256_RSA15);
		ArrayList Basic192Rsa15 = new ArrayList();
		Basic192Rsa15.add(Consts.Sha1);
		Basic192Rsa15.add(Consts.Aes192);
		Basic192Rsa15.add(Consts.KwRsa15);
		Basic192Rsa15.add(Constants.ALGO_SUITE_BASIC192_RSA15);
		ArrayList Basic128Rsa15 = new ArrayList();
		Basic128Rsa15.add(Consts.Sha1);
		Basic128Rsa15.add(Consts.Aes128);
		Basic128Rsa15.add(Consts.KwRsa15);
		Basic128Rsa15.add(Constants.ALGO_SUITE_BASIC128_RSA15);
		ArrayList TripleDesRsa15 = new ArrayList();
		TripleDesRsa15.add(Consts.Sha1);
		TripleDesRsa15.add(Consts.TripleDes);
		TripleDesRsa15.add(Consts.KwRsa15);
		TripleDesRsa15.add(Constants.ALGO_SUITE_TRIPLE_DES_RSA15);
		ArrayList Basic256Sha256 = new ArrayList();
		Basic256Sha256.add(Consts.Sha256);
		Basic256Sha256.add(Consts.Aes256);
		Basic256Sha256.add(Consts.KwRsaOaep);
		Basic256Sha256.add(Constants.ALGO_SUITE_BASIC256_SHA256);
		ArrayList Basic192Sha256 = new ArrayList();
		Basic192Sha256.add(Consts.Sha256);
		Basic192Sha256.add(Consts.Aes192);
		Basic192Sha256.add(Consts.KwRsaOaep);
		Basic192Sha256.add(Constants.ALGO_SUITE_BASIC192_SHA256);
		ArrayList Basic128Sha256 = new ArrayList();
		Basic128Sha256.add(Consts.Sha256);
		Basic128Sha256.add(Consts.Aes128);
		Basic128Sha256.add(Consts.KwRsaOaep);
		Basic128Sha256.add(Constants.ALGO_SUITE_BASIC128_SHA256);
		ArrayList TripleDesSha256 = new ArrayList();
		TripleDesSha256.add(Consts.Sha256);
		TripleDesSha256.add(Consts.TripleDes);
		TripleDesSha256.add(Consts.KwRsaOaep);
		TripleDesSha256.add(Constants.ALGO_SUITE_TRIPLE_DES_SHA256);
		ArrayList Basic256Sha256Rsa15 = new ArrayList();
		Basic256Sha256Rsa15.add(Consts.Sha256);
		Basic256Sha256Rsa15.add(Consts.Aes256);
		Basic256Sha256Rsa15.add(Consts.KwRsa15);
		Basic256Sha256Rsa15.add(Constants.ALGO_SUITE_BASIC256_SHA256_RSA15);
		ArrayList Basic192Sha256Rsa15 = new ArrayList();
		Basic192Sha256Rsa15.add(Consts.Sha256);
		Basic192Sha256Rsa15.add(Consts.Aes192);
		Basic192Sha256Rsa15.add(Consts.KwRsa15);
		Basic192Sha256Rsa15.add(Constants.ALGO_SUITE_BASIC192_SHA256_RSA15);
		ArrayList Basic128Sha256Rsa15 = new ArrayList();
		Basic128Sha256Rsa15.add(Consts.Sha256);
		Basic128Sha256Rsa15.add(Consts.Aes128);
		Basic128Sha256Rsa15.add(Consts.KwRsa15);
		Basic128Sha256Rsa15.add(Constants.ALGO_SUITE_BASIC128_SHA256_RSA15);
		ArrayList TripleDesSha256Rsa15 = new ArrayList();
		TripleDesSha256Rsa15.add(Consts.Sha256);
		TripleDesSha256Rsa15.add(Consts.TripleDes);
		TripleDesSha256Rsa15.add(Consts.KwRsa15);
		TripleDesSha256Rsa15.add(Constants.ALGO_SUITE_TRIPLE_DES_SHA256_RSA15);
		ArrayList algoTypes = new ArrayList();
		algoTypes.add(Basic256);
		algoTypes.add(Basic256Rsa15);
		algoTypes.add(Basic192);
		algoTypes.add(Basic128);
		algoTypes.add(TripleDes);
		algoTypes.add(Basic192Rsa15);
		algoTypes.add(Basic128Rsa15);
		algoTypes.add(TripleDesRsa15);
		algoTypes.add(Basic256Sha256);
		algoTypes.add(Basic192Sha256);
		algoTypes.add(Basic128Sha256);
		algoTypes.add(TripleDesSha256);
		algoTypes.add(Basic256Sha256Rsa15);
		algoTypes.add(Basic192Sha256Rsa15);
		algoTypes.add(Basic128Sha256Rsa15);
		algoTypes.add(TripleDesSha256Rsa15);
		properties.put(K_AlgoList, algoTypes);
		return properties;
	}
}
