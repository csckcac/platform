/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth.preprocessor;

import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;

/**
 * <Code>TokenPersistencePreprocessor</Code> is used to modify the token before it is stored
 * in the database. For instance, tokens need to be encrypted, hashed, etc before storing in the
 * database. There can be one active <Code>TokenPersistencePreprocessor</Code> at a given runtime
 * and it is configured through the identity.xml. <Code>getPreprocessedToken</Code> method should
 * behave like a hash function where it should result in 2 distinct preprocessed tokens for 2
 * different plain text tokens. For the same plain text token, it should result in the same
 * preprocessed token value.
 * @see PlainTextTokenPersistencePreprocessor
 * @see SHA256TokenPersistencePreprocessor
 */
public interface TokenPersistencePreprocessor {

    /**
     * get the preprocessed token value for a given plain token.
     * @param plainToken plain token
     * @return preprocessed token.
     */
    public String getPreprocessedToken(String plainToken) throws IdentityOAuth2Exception;

}
