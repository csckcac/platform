/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.bam.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

public class CryptographyManager {

    private static final Log log = LogFactory.getLog(RegistryManager.class);
    private CryptoUtil cryptoUtil;

    public CryptographyManager(){
        cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
    }

    public String encryptAndBase64Encode(String plainText) {
        try {
            return cryptoUtil.encryptAndBase64Encode(plainText.getBytes());
        } catch (CryptoException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public String base64DecodeAndDecrypt(String cipherText) {
        try {
            return new String(cryptoUtil.base64DecodeAndDecrypt(cipherText));
        } catch (CryptoException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}